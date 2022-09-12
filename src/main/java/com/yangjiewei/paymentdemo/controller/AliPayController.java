/*
 * @author yangjiewei
 * @date 2022/9/12 22:08
 */
package com.yangjiewei.paymentdemo.controller;

import com.yangjiewei.paymentdemo.service.AliPayService;
import com.yangjiewei.paymentdemo.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@CrossOrigin
@Api(tags = "网站支付宝支付")
@RestController
@RequestMapping("/api/ali-pay")
public class AliPayController {

    @Resource
    private AliPayService aliPayService;

    /**
     * 统一收单下单并支付页面接口的调用
     */
    @ApiOperation("统一收单下单并支付页面接口的调用")
    @PostMapping("/trade/page/pay/{productId}")
    public R tradePagePay(@PathVariable Long productId) {
        // todo
        return null;
    }
}
