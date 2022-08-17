/*
 * @author yangjiewei
 * @date 2022/8/17 21:38
 */
package com.yangjiewei.paymentdemo.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "商品管理")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    /**
     * http://localhost:8090/api/product/test
     */
    @ApiOperation("测试接口")
    @GetMapping("/test")
    public String test() {
        return "hello";
    }

}
