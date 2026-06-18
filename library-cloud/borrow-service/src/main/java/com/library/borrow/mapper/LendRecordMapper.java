package com.library.borrow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.borrow.entity.LendRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LendRecordMapper extends BaseMapper<LendRecord> {
}