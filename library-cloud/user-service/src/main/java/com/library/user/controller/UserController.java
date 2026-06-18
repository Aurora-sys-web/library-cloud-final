package com.library.user.controller;

import com.library.common.Result;
import com.library.user.entity.User;
import com.library.user.mapper.UserMapper;
import com.library.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<?> login(@RequestBody User user) {
        return userService.login(user.getUsername(), user.getPassword());
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<?> register(@RequestBody User user) {
        return userService.register(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    public Result<?> getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userService.update(user);
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户信息")
    public Result<?> update(@RequestBody User user) {
        return userService.update(user);
    }

    @PutMapping("/alow/{id}")
    @Operation(summary = "授予借阅权限")
    public Result<?> updateAlow(@PathVariable Long id) {
        return userService.updateAlow(id);
    }

    @GetMapping("/checkAlow/{id}")
    @Operation(summary = "检查借阅权限")
    public Result<?> checkAlow(@PathVariable Long id) {
        return userService.checkAlow(id);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询用户")
    public Result<?> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "") String search) {
        return userService.pageQuery(pageNum, pageSize, search);
    }

    @GetMapping("/usersearch")
    @Operation(summary = "分页查询用户（多条件）")
    public Result<?> userSearch(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "") String search1,
            @RequestParam(defaultValue = "") String search2,
            @RequestParam(defaultValue = "") String search3,
            @RequestParam(defaultValue = "") String search4) {
        return userService.pageQueryWithConditions(pageNum, pageSize, search1, search2, search3, search4);
    }

    @PostMapping("/deleteBatch")
    @Operation(summary = "批量禁用用户")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        return userService.deleteBatch(ids);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "禁用用户")
    public Result<?> delete(@PathVariable Long id) {
        return userService.deleteById(id);
    }

    @GetMapping("/getcode")
    @Operation(summary = "获取短信验证码")
    public Result<?> getCode(@RequestParam String phone) {
        return userService.sendSmsCode(phone);
    }

    @PostMapping("/resetPassword")
    @Operation(summary = "重置密码")
    public Result<?> resetPassword(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String code = params.get("code");
        return userService.resetPassword(username, password, code);
    }

    @PostMapping("/forget/getcode")
    @Operation(summary = "获取短信验证码（忘记密码用）")
    public Result<?> forgetGetCode(@RequestParam String username) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return Result.error("-1", "用户不存在");
        }
        String phone = user.getPhone();
        if (phone == null || phone.isEmpty()) {
            return Result.error("-1", "该用户未绑定手机号");
        }
        return userService.sendSmsCode(phone);
    }

    @PostMapping("/forget/register")
    @Operation(summary = "忘记密码重置")
    public Result<?> forgetReset(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String code = params.get("code");
        return userService.resetPassword(username, password, code);
    }
}