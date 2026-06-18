package com.library.borrow.feign;

import com.library.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/user")
public interface UserFeignClient {

    @GetMapping("/{id}")
    Result<?> getUserById(@PathVariable("id") Long id);

    @GetMapping("/checkAlow/{id}")
    Result<?> checkAlow(@PathVariable("id") Long id);
}