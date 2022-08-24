package com.yangjiewei.paymentdemo.controller;

import com.yangjiewei.paymentdemo.service.WxPayService;
import com.yangjiewei.paymentdemo.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 要实现的接口有这些
 * https://pay.weixin.qq.com/wiki/doc/apiv3/open/pay/chapter2_7_3.shtml
 * @author yangjiewei
 * @date 2022/8/24
 */
@Slf4j
@CrossOrigin
@RestController
@Api(tags = "网站微信支付 native")
@RequestMapping("/api/wx-pay")
public class WxPayController {

    @Resource
    private WxPayService wxPayService;

    /**
     * https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_4_1.shtml
     * native下单api
     * 根据商品信息获取费用信息等，这里没有实际获取商品，调用接口获取code_url，前端显示二维码
     * {
     *   "code": 0,
     *   "message": "成功",
     *   "data": {
     *     "codeUrl": "weixin://wxpay/bizpayurl?pr=JiLa01azz",
     *     "orderNo": "ORDER_20220824172547251"
     *   }
     * }
     */
    @ApiOperation("调用统一下单API，生成支付二维码")
    @PostMapping("/native/{productId}")
    public R nativePay(@PathVariable Long productId) throws Exception {
        log.info("发起支付请求");
        // 返回支付二维码链接和订单号
        Map<String, Object> map = wxPayService.nativePay(productId);
        return R.ok().setData(map);
    }
}
