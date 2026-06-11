package com.library.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.Result;
import com.library.user.entity.User;
import com.library.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public Result<?> login(String username, String password) {
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, username)
                .eq(User::getPassword, password));
        if (user == null) {
            return Result.error("-1", "用户名或密码错误");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    public Result<?> register(User user) {
        User exist = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, user.getUsername()));
        if (exist != null) {
            return Result.error("-1", "用户名已存在");
        }
        user.setRole(2);
        user.setAlow("0");
        userMapper.insert(user);
        return Result.success();
    }

    public Result<?> getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("-1", "用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    public Result<?> update(User user) {
        userMapper.updateById(user);
        return Result.success();
    }

    public Result<?> updateAlow(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("-1", "用户不存在");
        }
        user.setAlow("1");
        userMapper.updateById(user);
        return Result.success();
    }

    public Result<?> checkAlow(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || !"1".equals(user.getAlow())) {
            return Result.error("-1", "您没有借阅权限");
        }
        return Result.success();
    }

    public Result<?> pageQuery(Integer pageNum, Integer pageSize, String search) {
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getRole, 2);
        if (search != null && !search.isEmpty()) {
            wrapper.like(User::getNickName, search);
        }
        wrapper.orderByAsc(User::getId);
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        page.getRecords().forEach(user -> user.setPassword(null));
        return Result.success(page);
    }
}