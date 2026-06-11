package com.library.user.controller;

import com.library.common.Result;
import com.library.user.entity.User;
import com.library.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    private final UserService userService;

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
}