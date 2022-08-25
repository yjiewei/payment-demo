package com.yangjiewei.paymentdemo.controller;

import com.google.gson.Gson;
import com.yangjiewei.paymentdemo.service.WxPayService;
import com.yangjiewei.paymentdemo.util.HttpUtils;
import com.yangjiewei.paymentdemo.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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


    /**
     * 支付通知
     * 微信支付通过支付通知接口将用户支付成功消息通知给商户
     * https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_4_5.shtml
     * 这个接口对请求信息要做签名验证，避免假通知
     * 需要对该请求进行应答，成功或失败
     */
    @ApiOperation("支付通知")
    @PostMapping("/native/notify")
    public String nativeNotify(HttpServletRequest request, HttpServletResponse response) {
        Gson gson = new Gson();
        // 构造应答对象
        Map<String, String> map = new HashMap<>();

        try{
            // 处理通知参数
            String body = HttpUtils.readData(request);
            Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
            log.info("支付通知的id ===> {}", bodyMap.get("id"));
            log.info("支付通知的完整数据 ===> {}", body);

            // todo 签名验证
            // todo 处理订单

            /**
             * 处理应答
             * 成功应答：成功应答必须为200或204，否则就是失败应答
             * {
             *    "code": "FAIL",
             *    "message": "失败"
             * }
             */
            // 测试超时应答：添加睡眠时间使应答超时
            // TimeUnit.SECONDS.sleep(5);
            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("message", "成功");
            return gson.toJson(map);
        }catch (Exception e) {
            e.printStackTrace();
            // 测试错误应答
            response.setStatus(500);
            map.put("code", "ERROR");
            map.put("message", "系统错误");
            return gson.toJson(map);
        }

    }
}
