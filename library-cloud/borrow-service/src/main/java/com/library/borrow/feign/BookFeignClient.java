package com.library.borrow.feign;

import com.library.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "book-service", path = "/book")
public interface BookFeignClient {

    @GetMapping("/isbn/{isbn}")
    Result<?> getByIsbn(@PathVariable("isbn") String isbn);

    @PutMapping("/status")
    Result<?> updateStatus(@RequestParam("isbn") String isbn, @RequestParam("status") String status);

    @PutMapping("/increment/{isbn}")
    Result<?> incrementBorrowNum(@PathVariable("isbn") String isbn);
}