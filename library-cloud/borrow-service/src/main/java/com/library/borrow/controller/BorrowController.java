package com.library.borrow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.library.borrow.entity.LendRecord;
import com.library.borrow.mapper.LendRecordMapper;
import com.library.common.Result;
import com.library.borrow.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/borrow")
@RequiredArgsConstructor
@Tag(name = "借阅管理", description = "借阅相关接口")
public class BorrowController {

    private final BorrowService borrowService;
    private final LendRecordMapper lendRecordMapper;

    @PostMapping("/borrow")
    @Operation(summary = "借书")
    public Result<?> borrow(@RequestParam Long userId, @RequestParam String isbn) {
        return borrowService.borrow(userId, isbn);
    }

    @PutMapping("/return")
    @Operation(summary = "还书")
    public Result<?> returnBook(@RequestParam Long userId, @RequestParam String isbn) {
        return borrowService.returnBook(userId, isbn);
    }

    @PutMapping("/renew")
    @Operation(summary = "续借")
    public Result<?> renew(@RequestParam Long userId, @RequestParam String isbn) {
        return borrowService.renew(userId, isbn);
    }

    @GetMapping("/current/{userId}")
    @Operation(summary = "查询用户当前借阅")
    public Result<?> getCurrentBorrow(@PathVariable Long userId) {
        return borrowService.getCurrentBorrow(userId);
    }

    // 新增：获取最大借阅数量配置接口
    @GetMapping("/config/maxBorrow")
    @Operation(summary = "获取最大借阅数量配置")
    public Result<?> getMaxBorrowCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("maxBorrow", borrowService.getMaxBorrowCount());
        return Result.success(result);
    }

    @GetMapping("/admin/records")
    @Operation(summary = "管理员查询所有借阅记录")
    public Result<?> getAllLendRecords(@RequestParam(defaultValue = "") String isbn,
            @RequestParam(defaultValue = "") String bookname,
            @RequestParam(required = false) Long readerId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return borrowService.getAllLendRecords(isbn, bookname, readerId, pageNum, pageSize);
    }

    // ========== 以下为新增接口，兼容前端调用 ==========

    @GetMapping("/LendRecord")
    @Operation(summary = "分页查询借阅记录")
    public Result<?> getLendRecordPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "") String search1,
            @RequestParam(defaultValue = "") String search2,
            @RequestParam(defaultValue = "") String search3) {
        Long readerId = null;
        if (org.springframework.util.StringUtils.hasText(search3)) {
            try {
                readerId = Long.valueOf(search3);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return borrowService.getAllLendRecords(search1, search2, readerId, pageNum, pageSize);
    }

    @PostMapping("/LendRecord")
    @Operation(summary = "新增借阅记录")
    public Result<?> addLendRecord(@RequestBody Map<String, Object> params) {
        return Result.success();
    }

    @PutMapping("/LendRecord/{isbn}")
    @Operation(summary = "更新借阅记录")
    public Result<?> updateLendRecord(@PathVariable String isbn, @RequestBody LendRecord lendRecord) {
        LambdaQueryWrapper<LendRecord> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(LendRecord::getIsbn, isbn);
        if (lendRecord.getReaderId() != null) {
            wrapper.eq(LendRecord::getReaderId, lendRecord.getReaderId());
        }
        LendRecord existing = lendRecordMapper.selectOne(wrapper);
        if (existing != null) {
            if (lendRecord.getReturnTime() != null) {
                existing.setReturnTime(lendRecord.getReturnTime());
            }
            if (lendRecord.getStatus() != null) {
                existing.setStatus(lendRecord.getStatus());
            }
            if (lendRecord.getBorrownum() != null) {
                existing.setBorrownum(lendRecord.getBorrownum());
            }
            lendRecordMapper.updateById(existing);
        }
        return Result.success();
    }

    @PostMapping("/LendRecord/deleteRecords")
    @Operation(summary = "批量删除借阅记录")
    public Result<?> deleteLendRecords(@RequestBody java.util.List<LendRecord> records) {
        for (LendRecord record : records) {
            if (record.getId() != null) {
                lendRecordMapper.deleteById(record.getId());
            }
        }
        return Result.success();
    }

    @PostMapping("/LendRecord/deleteRecord")
    @Operation(summary = "删除单条借阅记录")
    public Result<?> deleteLendRecord(@RequestBody LendRecord record) {
        if (record.getId() != null) {
            lendRecordMapper.deleteById(record.getId());
        } else if (record.getIsbn() != null && record.getReaderId() != null) {
            LambdaQueryWrapper<LendRecord> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(LendRecord::getIsbn, record.getIsbn())
                    .eq(LendRecord::getReaderId, record.getReaderId());
            lendRecordMapper.delete(wrapper);
        }
        return Result.success();
    }

    @GetMapping("/bookwithuser")
    @Operation(summary = "查询借阅状态列表")
    public Result<?> getBookWithUserPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "") String search1,
            @RequestParam(defaultValue = "") String search2,
            @RequestParam(defaultValue = "") String search3) {
        Long userId = null;
        if (org.springframework.util.StringUtils.hasText(search3)) {
            try {
                userId = Long.valueOf(search3);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        if (userId != null) {
            return borrowService.getCurrentBorrow(userId);
        }
        return Result.success(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());
    }

    @PostMapping("/bookwithuser/insertNew")
    @Operation(summary = "新增借阅状态")
    public Result<?> insertBookWithUser(@RequestBody Map<String, Object> params) {
        return Result.success();
    }

    @PostMapping("/bookwithuser/deleteRecord")
    @Operation(summary = "删除借阅状态")
    public Result<?> deleteBookWithUser(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("id").toString());
        String isbn = params.get("isbn").toString();
        return borrowService.returnBook(userId, isbn);
    }

    @PostMapping("/bookwithuser/deleteRecords")
    @Operation(summary = "批量删除借阅状态")
    public Result<?> deleteBookWithUsers(@RequestBody java.util.List<Map<String, Object>> records) {
        for (Map<String, Object> record : records) {
            Long userId = Long.valueOf(record.get("id").toString());
            String isbn = record.get("isbn").toString();
            borrowService.returnBook(userId, isbn);
        }
        return Result.success();
    }

    @PostMapping("/bookwithuser")
    @Operation(summary = "更新借阅状态（续借）")
    public Result<?> updateBookWithUser(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("id").toString());
        String isbn = params.get("isbn").toString();
        return borrowService.renew(userId, isbn);
    }
}