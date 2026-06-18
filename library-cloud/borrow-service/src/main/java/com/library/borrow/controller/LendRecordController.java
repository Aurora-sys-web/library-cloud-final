package com.library.borrow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.borrow.entity.LendRecord;
import com.library.borrow.mapper.LendRecordMapper;
import com.library.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/LendRecord")
@RequiredArgsConstructor
@Tag(name = "借阅记录管理", description = "借阅记录相关接口")
public class LendRecordController {

  private final LendRecordMapper lendRecordMapper;

  @GetMapping
  @Operation(summary = "分页查询借阅记录")
  public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
      @RequestParam(defaultValue = "10") Integer pageSize,
      @RequestParam(defaultValue = "") String search1,
      @RequestParam(defaultValue = "") String search2,
      @RequestParam(defaultValue = "") String search3) {
    LambdaQueryWrapper<LendRecord> wrapper = Wrappers.lambdaQuery();
    if (StringUtils.isNotBlank(search1)) {
      wrapper.like(LendRecord::getIsbn, search1);
    }
    if (StringUtils.isNotBlank(search2)) {
      wrapper.like(LendRecord::getBookname, search2);
    }
    if (StringUtils.isNotBlank(search3)) {
      try {
        wrapper.eq(LendRecord::getReaderId, Long.valueOf(search3));
      } catch (NumberFormatException e) {
        // ignore
      }
    }
    wrapper.orderByDesc(LendRecord::getLendTime);
    Page<LendRecord> page = lendRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    return Result.success(page);
  }

  @PostMapping
  @Operation(summary = "新增借阅记录")
  public Result<?> save(@RequestBody LendRecord lendRecord) {
    lendRecordMapper.insert(lendRecord);
    return Result.success();
  }

  // 修复后的更新借阅记录接口
  @PutMapping("/{isbn}")
  @Operation(summary = "更新借阅记录")
  public Result<?> update(@PathVariable String isbn, @RequestBody LendRecord lendRecord) {
    // 优先使用 ID 精确更新
    if (lendRecord.getId() != null) {
      LendRecord existing = lendRecordMapper.selectById(lendRecord.getId());
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

    // 如果没有 ID，使用 ISBN + 读者ID 查询，但只取最新的一条
    LambdaQueryWrapper<LendRecord> wrapper = Wrappers.lambdaQuery();
    wrapper.eq(LendRecord::getIsbn, isbn);
    if (lendRecord.getReaderId() != null) {
      wrapper.eq(LendRecord::getReaderId, lendRecord.getReaderId());
    }
    wrapper.orderByDesc(LendRecord::getLendTime);
    wrapper.last("LIMIT 1"); // 只取最新的一条记录

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

  @PostMapping("/deleteRecords")
  @Operation(summary = "批量删除借阅记录")
  public Result<?> deleteBatch(@RequestBody List<LendRecord> records) {
    for (LendRecord record : records) {
      if (record.getId() != null) {
        lendRecordMapper.deleteById(record.getId());
      }
    }
    return Result.success();
  }

  @PostMapping("/deleteRecord")
  @Operation(summary = "删除单条借阅记录")
  public Result<?> delete(@RequestBody LendRecord record) {
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
}