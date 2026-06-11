package com.library.borrow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.Result;
import com.library.borrow.entity.BookWithUser;
import com.library.borrow.entity.LendRecord;
import com.library.borrow.feign.BookFeignClient;
import com.library.borrow.feign.UserFeignClient;
import com.library.borrow.mapper.BookWithUserMapper;
import com.library.borrow.mapper.LendRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final LendRecordMapper lendRecordMapper;
    private final BookWithUserMapper bookWithUserMapper;
    private final UserFeignClient userFeignClient;
    private final BookFeignClient bookFeignClient;

    @Transactional
    public Result<?> borrow(Long userId, String isbn) {
        // 1. 检查用户权限
        Result<?> alowResult = userFeignClient.checkAlow(userId);
        if (!"0".equals(alowResult.getCode())) {
            return alowResult;
        }
        
        // 2. 检查当前借阅数量
        LambdaQueryWrapper<BookWithUser> countWrapper = Wrappers.lambdaQuery();
        countWrapper.eq(BookWithUser::getId, userId);
        long borrowCount = bookWithUserMapper.selectCount(countWrapper);
        if (borrowCount >= 5) {
            return Result.error("-1", "您已达到最大借阅数量（5本）");
        }
        
        // 3. 检查图书
        Result<?> bookResult = bookFeignClient.getByIsbn(isbn);
        if (!"0".equals(bookResult.getCode())) {
            return Result.error("-1", "图书不存在");
        }
        Map<String, Object> bookData = (Map<String, Object>) bookResult.getData();
        String status = (String) bookData.get("status");
        if ("0".equals(status)) {
            return Result.error("-1", "图书已被借出");
        }
        
        // 4. 更新图书状态
        Result<?> updateStatusResult = bookFeignClient.updateStatus(isbn, "0");
        if (!"0".equals(updateStatusResult.getCode())) {
            return Result.error("-1", "借阅失败，请重试");
        }
        
        // 5. 增加借阅次数
        bookFeignClient.incrementBorrowNum(isbn);
        
        // 6. 获取用户信息
        Result<?> userResult = userFeignClient.getUserById(userId);
        Map<String, Object> userData = (Map<String, Object>) userResult.getData();
        
        // 7. 创建借阅记录
        LendRecord lendRecord = new LendRecord();
        lendRecord.setReaderId(userId);
        lendRecord.setIsbn(isbn);
        lendRecord.setBookname((String) bookData.get("name"));
        lendRecord.setLendTime(new Date());
        lendRecord.setStatus("0");
        lendRecord.setBorrownum((Integer) bookData.get("borrownum") + 1);
        lendRecordMapper.insert(lendRecord);
        
        // 8. 添加到借阅状态表
        BookWithUser bookWithUser = new BookWithUser();
        bookWithUser.setId(userId);
        bookWithUser.setIsbn(isbn);
        bookWithUser.setBookName((String) bookData.get("name"));
        bookWithUser.setNickName((String) userData.get("nickName"));
        bookWithUser.setLendtime(new Date());
        Date deadtime = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);
        bookWithUser.setDeadtime(deadtime);
        bookWithUser.setProlong(1);
        bookWithUserMapper.insert(bookWithUser);
        
        return Result.success();
    }

    @Transactional
    public Result<?> returnBook(Long userId, String isbn) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", userId);
        map.put("isbn", isbn);
        List<BookWithUser> bookWithUsers = bookWithUserMapper.selectByMap(map);
        if (bookWithUsers.isEmpty()) {
            return Result.error("-1", "没有该书的借阅记录");
        }
        
        bookWithUserMapper.deleteByMap(map);
        
        Result<?> updateStatusResult = bookFeignClient.updateStatus(isbn, "1");
        if (!"0".equals(updateStatusResult.getCode())) {
            return Result.error("-1", "还书失败，请重试");
        }
        
        LambdaQueryWrapper<LendRecord> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(LendRecord::getReaderId, userId)
               .eq(LendRecord::getIsbn, isbn)
               .eq(LendRecord::getStatus, "0");
        LendRecord lendRecord = lendRecordMapper.selectOne(wrapper);
        if (lendRecord != null) {
            lendRecord.setStatus("1");
            lendRecord.setReturnTime(new Date());
            lendRecordMapper.updateById(lendRecord);
        }
        
        return Result.success();
    }

    @Transactional
    public Result<?> renew(Long userId, String isbn) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", userId);
        map.put("isbn", isbn);
        BookWithUser bookWithUser = bookWithUserMapper.selectByMap(map).stream().findFirst().orElse(null);
        if (bookWithUser == null) {
            return Result.error("-1", "没有该书的借阅记录");
        }
        if (bookWithUser.getProlong() <= 0) {
            return Result.error("-1", "续借次数已用完");
        }
        
        Date newDeadtime = new Date(bookWithUser.getDeadtime().getTime() + 30L * 24 * 60 * 60 * 1000);
        bookWithUser.setDeadtime(newDeadtime);
        bookWithUser.setProlong(bookWithUser.getProlong() - 1);
        bookWithUserMapper.updateById(bookWithUser);
        
        return Result.success();
    }

    public Result<?> getCurrentBorrow(Long userId) {
        LambdaQueryWrapper<BookWithUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(BookWithUser::getId, userId);
        wrapper.orderByDesc(BookWithUser::getLendtime);
        List<BookWithUser> list = bookWithUserMapper.selectList(wrapper);
        return Result.success(list);
    }

    public Result<?> getAllLendRecords(String isbn, String bookname, Long readerId,
                                        Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<LendRecord> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(isbn)) {
            wrapper.like(LendRecord::getIsbn, isbn);
        }
        if (StringUtils.isNotBlank(bookname)) {
            wrapper.like(LendRecord::getBookname, bookname);
        }
        if (readerId != null) {
            wrapper.eq(LendRecord::getReaderId, readerId);
        }
        wrapper.orderByDesc(LendRecord::getLendTime);
        Page<LendRecord> page = lendRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }
}