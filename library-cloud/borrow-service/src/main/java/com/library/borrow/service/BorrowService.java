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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@RefreshScope
public class BorrowService {

    private final LendRecordMapper lendRecordMapper;
    private final BookWithUserMapper bookWithUserMapper;
    private final UserFeignClient userFeignClient;
    private final BookFeignClient bookFeignClient;

    // 从 application.yml 读取最大借阅数量，默认值为 5
    @Value("${book.max.borrow:5}")
    private int maxBorrowCount;

    @PostConstruct
    public void init() {
        System.out.println("========== maxBorrowCount = " + maxBorrowCount + " ==========");
    }

    // 新增：获取最大借阅数量配置
    public int getMaxBorrowCount() {
        return maxBorrowCount;
    }

    @Transactional
    public Result<?> borrow(Long userId, String isbn) {
        // 1. 检查用户权限
        Result<?> alowResult = userFeignClient.checkAlow(userId);
        if (!"0".equals(alowResult.getCode())) {
            return alowResult;
        }

        // 2. 检查当前借阅数量（使用配置中心的 maxBorrowCount）
        LambdaQueryWrapper<BookWithUser> countWrapper = Wrappers.lambdaQuery();
        countWrapper.eq(BookWithUser::getId, userId);
        long borrowCount = bookWithUserMapper.selectCount(countWrapper);
        if (borrowCount >= maxBorrowCount) {
            return Result.error("-1", "您已达到最大借阅数量（" + maxBorrowCount + "本）");
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

        // 4. 更新图书状态为已借出
        Result<?> updateStatusResult = bookFeignClient.updateStatus(isbn, "0");
        if (!"0".equals(updateStatusResult.getCode())) {
            return Result.error("-1", "借阅失败，请重试");
        }

        // 5. 增加借阅次数
        bookFeignClient.incrementBorrowNum(isbn);

        // 6. 获取用户信息
        Result<?> userResult = userFeignClient.getUserById(userId);
        Map<String, Object> userData = (Map<String, Object>) userResult.getData();

        Date now = new Date();
        Date deadtime = new Date(now.getTime() + 30L * 24 * 60 * 60 * 1000);

        // 7. 创建借阅记录
        LendRecord lendRecord = new LendRecord();
        lendRecord.setReaderId(userId);
        lendRecord.setIsbn(isbn);
        lendRecord.setBookname((String) bookData.get("name"));
        lendRecord.setLendTime(now);
        lendRecord.setReturnTime(null);
        lendRecord.setStatus("0");
        lendRecord.setBorrownum(((Number) bookData.get("borrownum")).intValue() + 1);
        lendRecordMapper.insert(lendRecord);

        // 8. 添加到借阅状态表
        BookWithUser bookWithUser = new BookWithUser();
        bookWithUser.setId(userId);
        bookWithUser.setIsbn(isbn);
        bookWithUser.setBookName((String) bookData.get("name"));
        bookWithUser.setNickName((String) userData.get("nickName"));
        bookWithUser.setLendtime(now);
        bookWithUser.setDeadtime(deadtime);
        bookWithUser.setProlong(1);
        bookWithUserMapper.insert(bookWithUser);

        return Result.success();
    }

    @Transactional
    public Result<?> returnBook(Long userId, String isbn) {
        // 1. 检查是否存在借阅记录
        Map<String, Object> map = new HashMap<>();
        map.put("id", userId);
        map.put("isbn", isbn);
        List<BookWithUser> bookWithUsers = bookWithUserMapper.selectByMap(map);
        if (bookWithUsers.isEmpty()) {
            return Result.error("-1", "没有该书的借阅记录");
        }

        // 2. 删除借阅状态表记录
        bookWithUserMapper.deleteByMap(map);

        // 3. 更新图书状态为可借
        Result<?> updateStatusResult = bookFeignClient.updateStatus(isbn, "1");
        if (!"0".equals(updateStatusResult.getCode())) {
            return Result.error("-1", "还书失败，请重试");
        }

        // 4. 更新借阅记录（设置归还时间和状态）
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
        List<BookWithUser> bookWithUsers = bookWithUserMapper.selectByMap(map);
        if (bookWithUsers.isEmpty()) {
            return Result.error("-1", "没有该书的借阅记录");
        }

        BookWithUser bookWithUser = bookWithUsers.get(0);
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