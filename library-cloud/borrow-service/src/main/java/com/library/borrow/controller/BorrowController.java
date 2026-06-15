package com.library.borrow.controller;

import com.library.common.Result;
import com.library.borrow.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/borrow")
@RequiredArgsConstructor
@Tag(name = "借阅管理", description = "借阅相关接口")
public class BorrowController {

    private final BorrowService borrowService;

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

    @GetMapping("/admin/records")
    @Operation(summary = "管理员查询所有借阅记录")
    public Result<?> getAllLendRecords(@RequestParam(defaultValue = "") String isbn,
                                       @RequestParam(defaultValue = "") String bookname,
                                       @RequestParam(required = false) Long readerId,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return borrowService.getAllLendRecords(isbn, bookname, readerId, pageNum, pageSize);
    }
}