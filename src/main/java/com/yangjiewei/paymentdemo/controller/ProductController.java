/*
 * @author yangjiewei
 * @date 2022/8/17 21:38
 */
package com.yangjiewei.paymentdemo.controller;

import com.yangjiewei.paymentdemo.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Api(tags = "商品管理")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    /**
     * http://localhost:8090/api/product/test
     */
    @ApiOperation("测试接口")
    @GetMapping("/test")
    public R test() {
        // 返回的时间对不上 2022-08-20T14:40:04.949+00:00，需要设置时间，为了方便查看就格式化
        return R.ok().data("数据", "成功").data("时间", new Date());
    }

}
