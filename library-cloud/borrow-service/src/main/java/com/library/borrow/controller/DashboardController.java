package com.library.borrow.controller;

import com.library.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "数据看板")
public class DashboardController {

    @GetMapping
    @Operation(summary = "获取统计数据")
    public Result<?> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        data.put("lendRecordCount", 0);
        data.put("visitCount", 0);
        data.put("bookCount", 0);
        data.put("userCount", 0);
        return Result.success(data);
    }
}