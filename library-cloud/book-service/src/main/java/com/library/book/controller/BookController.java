package com.library.book.controller;

import com.library.common.Result;
import com.library.book.entity.Book;
import com.library.book.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
@Tag(name = "图书管理", description = "图书相关接口")
public class BookController {

    private final BookService bookService;

    @PostMapping
    @Operation(summary = "新增图书")
    public Result<?> save(@RequestBody Book book) {
        return bookService.save(book);
    }

    @PutMapping
    @Operation(summary = "更新图书")
    public Result<?> update(@RequestBody Book book) {
        return bookService.update(book);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除图书")
    public Result<?> delete(@PathVariable Long id) {
        return bookService.delete(id);
    }

    @PostMapping("/deleteBatch")
    @Operation(summary = "批量删除图书")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        return bookService.deleteBatch(ids);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询图书")
    public Result<?> getById(@PathVariable Long id) {
        return bookService.getById(id);
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "根据ISBN查询图书")
    public Result<?> getByIsbn(@PathVariable String isbn) {
        return bookService.getByIsbn(isbn);
    }

    @PutMapping("/status")
    @Operation(summary = "更新图书借阅状态")
    public Result<?> updateStatus(@RequestParam String isbn, @RequestParam String status) {
        return bookService.updateStatus(isbn, status);
    }

    @PutMapping("/increment/{isbn}")
    @Operation(summary = "增加借阅次数")
    public Result<?> incrementBorrowNum(@PathVariable String isbn) {
        return bookService.incrementBorrowNum(isbn);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询图书")
    public Result<?> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "") String isbn,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String author) {
        return bookService.pageQuery(pageNum, pageSize, isbn, name, author);
    }

    @GetMapping
    @Operation(summary = "分页查询图书（兼容前端参数名）")
    public Result<?> pageCompatible(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "") String search1,
            @RequestParam(defaultValue = "") String search2,
            @RequestParam(defaultValue = "") String search3) {
        // search3 前端用于传递用户ID（查询该用户借阅的图书），这里返回普通分页
        return bookService.pageQuery(pageNum, pageSize, search1, search2, search3);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询图书（兼容另一种调用）")
    public Result<?> pageList(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "") String search1,
            @RequestParam(defaultValue = "") String search2,
            @RequestParam(defaultValue = "") String search3) {
        return bookService.pageQuery(pageNum, pageSize, search1, search2, search3);
    }
}