package com.library.book.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.Result;
import com.library.book.entity.Book;
import com.library.book.mapper.BookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;

    public Result<?> save(Book book) {
        LambdaQueryWrapper<Book> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Book::getIsbn, book.getIsbn());
        if (bookMapper.selectOne(wrapper) != null) {
            return Result.error("-1", "图书编号已存在");
        }
        book.setStatus("1");
        book.setBorrownum(0);
        bookMapper.insert(book);
        return Result.success();
    }

    public Result<?> update(Book book) {
        LambdaQueryWrapper<Book> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Book::getIsbn, book.getIsbn()).ne(Book::getId, book.getId());
        if (bookMapper.selectOne(wrapper) != null) {
            return Result.error("-1", "图书编号已存在");
        }
        bookMapper.updateById(book);
        return Result.success();
    }

    public Result<?> delete(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            return Result.error("-1", "图书不存在");
        }
        if ("0".equals(book.getStatus())) {
            return Result.error("-1", "书籍正在借阅中，无法删除");
        }
        bookMapper.deleteById(id);
        return Result.success();
    }

    public Result<?> deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要下架的图书");
        }
        for (Long id : ids) {
            Book book = bookMapper.selectById(id);
            if (book != null && "0".equals(book.getStatus())) {
                return Result.error("-1", "图书《" + book.getName() + "》正在借阅中，无法下架");
            }
        }
        for (Long id : ids) {
            bookMapper.deleteById(id);
        }
        return Result.success();
    }

    public Result<?> getById(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            return Result.error("-1", "图书不存在");
        }
        return Result.success(book);
    }

    public Result<?> getByIsbn(String isbn) {
        Book book = bookMapper.selectOne(Wrappers.<Book>lambdaQuery().eq(Book::getIsbn, isbn));
        if (book == null) {
            return Result.error("-1", "图书不存在");
        }
        return Result.success(book);
    }

    public Result<?> updateStatus(String isbn, String status) {
        Book book = bookMapper.selectOne(Wrappers.<Book>lambdaQuery().eq(Book::getIsbn, isbn));
        if (book == null) {
            return Result.error("-1", "图书不存在");
        }
        book.setStatus(status);
        bookMapper.updateById(book);
        return Result.success();
    }

    public Result<?> incrementBorrowNum(String isbn) {
        Book book = bookMapper.selectOne(Wrappers.<Book>lambdaQuery().eq(Book::getIsbn, isbn));
        if (book == null) {
            return Result.error("-1", "图书不存在");
        }
        book.setBorrownum(book.getBorrownum() + 1);
        bookMapper.updateById(book);
        return Result.success();
    }

    public Result<?> pageQuery(Integer pageNum, Integer pageSize, String isbn, String name, String author) {
        LambdaQueryWrapper<Book> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(isbn)) {
            wrapper.like(Book::getIsbn, isbn);
        }
        if (StringUtils.isNotBlank(name)) {
            wrapper.like(Book::getName, name);
        }
        if (StringUtils.isNotBlank(author)) {
            wrapper.like(Book::getAuthor, author);
        }
        wrapper.orderByDesc(Book::getBorrownum);
        Page<Book> page = bookMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }
}