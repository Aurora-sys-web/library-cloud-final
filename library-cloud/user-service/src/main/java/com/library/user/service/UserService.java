package com.library.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.Result;
import com.library.user.entity.User;
import com.library.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final Map<String, String> smsCodeCache = new HashMap<>();

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
        if (StringUtils.isNotBlank(search)) {
            wrapper.like(User::getNickName, search);
        }
        wrapper.orderByAsc(User::getId);
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        page.getRecords().forEach(user -> user.setPassword(null));
        return Result.success(page);
    }

    public Result<?> pageQueryWithConditions(Integer pageNum, Integer pageSize,
            String search1, String search2,
            String search3, String search4) {
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getRole, 2);

        if (StringUtils.isNotBlank(search1)) {
            try {
                wrapper.eq(User::getId, Long.valueOf(search1));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        if (StringUtils.isNotBlank(search2)) {
            wrapper.like(User::getNickName, search2);
        }
        if (StringUtils.isNotBlank(search3)) {
            wrapper.like(User::getPhone, search3);
        }
        if (StringUtils.isNotBlank(search4)) {
            wrapper.like(User::getAddress, search4);
        }

        wrapper.orderByAsc(User::getId);
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        page.getRecords().forEach(user -> user.setPassword(null));
        return Result.success(page);
    }

    public Result<?> deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要禁用的用户");
        }
        for (Long id : ids) {
            User user = userMapper.selectById(id);
            if (user != null) {
                user.setAlow("0");
                userMapper.updateById(user);
            }
        }
        return Result.success();
    }

    public Result<?> deleteById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setAlow("0");
            userMapper.updateById(user);
        }
        return Result.success();
    }

    public Result<?> sendSmsCode(String phone) {
        if (phone == null || phone.isEmpty()) {
            return Result.error("-1", "手机号不能为空");
        }
        String code = String.format("%06d", new Random().nextInt(999999));
        smsCodeCache.put(phone, code);
        System.out.println("验证码已发送至 " + phone + "：" + code);
        return Result.success();
    }

    public Result<?> resetPassword(String username, String password, String code) {
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, username));
        if (user == null) {
            return Result.error("-1", "用户不存在");
        }

        String phone = user.getPhone();
        if (phone == null) {
            return Result.error("-1", "用户未绑定手机号");
        }

        String savedCode = smsCodeCache.get(phone);
        if (savedCode == null || !savedCode.equals(code)) {
            return Result.error("-1", "验证码错误或已过期");
        }

        user.setPassword(password);
        userMapper.updateById(user);
        smsCodeCache.remove(phone);
        return Result.success();
    }
}