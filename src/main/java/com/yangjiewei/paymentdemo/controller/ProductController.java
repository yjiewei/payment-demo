/*
 * @author yangjiewei
 * @date 2022/8/17 21:38
 */
package com.yangjiewei.paymentdemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    /**
     * http://localhost:8090/api/product/test
     */
    @GetMapping("/test")
    public String test() {
        return "hello";
    }

}
