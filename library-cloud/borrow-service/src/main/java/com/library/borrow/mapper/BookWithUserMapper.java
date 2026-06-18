package com.library.borrow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.borrow.entity.BookWithUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookWithUserMapper extends BaseMapper<BookWithUser> {
}