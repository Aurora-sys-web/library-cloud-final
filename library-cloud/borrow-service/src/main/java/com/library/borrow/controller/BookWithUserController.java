package com.library.borrow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.borrow.entity.BookWithUser;
import com.library.borrow.mapper.BookWithUserMapper;
import com.library.borrow.service.BorrowService;
import com.library.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookwithuser")
@RequiredArgsConstructor
@Tag(name = "用户借阅状态管理", description = "用户借阅状态相关接口")
public class BookWithUserController {

    private final BookWithUserMapper bookWithUserMapper;
    private final BorrowService borrowService;

    @GetMapping
    @Operation(summary = "分页查询用户借阅状态")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "") String search1,
            @RequestParam(defaultValue = "") String search2,
            @RequestParam(defaultValue = "") String search3) {
        LambdaQueryWrapper<BookWithUser> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(search1)) {
            wrapper.like(BookWithUser::getIsbn, search1);
        }
        if (StringUtils.isNotBlank(search2)) {
            wrapper.like(BookWithUser::getBookName, search2);
        }
        if (StringUtils.isNotBlank(search3)) {
            try {
                wrapper.eq(BookWithUser::getId, Long.valueOf(search3));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        wrapper.orderByDesc(BookWithUser::getLendtime);
        Page<BookWithUser> page = bookWithUserMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    // 关键接口：根据用户ID查询当前借阅列表
    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户当前借阅列表")
    public Result<?> getUserBorrow(@PathVariable Long userId) {
        LambdaQueryWrapper<BookWithUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(BookWithUser::getId, userId);
        wrapper.orderByDesc(BookWithUser::getLendtime);
        List<BookWithUser> list = bookWithUserMapper.selectList(wrapper);
        return Result.success(list);
    }

    @PostMapping("/insertNew")
    @Operation(summary = "新增用户借阅状态")
    public Result<?> insert(@RequestBody BookWithUser bookWithUser) {
        bookWithUserMapper.insert(bookWithUser);
        return Result.success();
    }

    @PostMapping("/deleteRecord")
    @Operation(summary = "删除用户借阅状态")
    public Result<?> delete(@RequestBody BookWithUser bookWithUser) {
        if (bookWithUser.getId() != null && bookWithUser.getIsbn() != null) {
            LambdaQueryWrapper<BookWithUser> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(BookWithUser::getId, bookWithUser.getId())
                    .eq(BookWithUser::getIsbn, bookWithUser.getIsbn());
            bookWithUserMapper.delete(wrapper);
        }
        return Result.success();
    }

    @PostMapping("/deleteRecords")
    @Operation(summary = "批量删除用户借阅状态")
    public Result<?> deleteBatch(@RequestBody List<Map<String, Object>> records) {
        for (Map<String, Object> record : records) {
            Long userId = Long.valueOf(record.get("id").toString());
            String isbn = record.get("isbn").toString();
            LambdaQueryWrapper<BookWithUser> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(BookWithUser::getId, userId)
                    .eq(BookWithUser::getIsbn, isbn);
            bookWithUserMapper.delete(wrapper);
        }
        return Result.success();
    }
}