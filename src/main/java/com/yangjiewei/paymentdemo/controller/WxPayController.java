package com.yangjiewei.paymentdemo.controller;

import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.yangjiewei.paymentdemo.service.WxPayService;
import com.yangjiewei.paymentdemo.util.HttpUtils;
import com.yangjiewei.paymentdemo.util.WechatPay2ValidatorForRequest;
import com.yangjiewei.paymentdemo.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    @Resource
    private Verifier verifier;

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
     * {
     *    "code": "FAIL",
     *    "message": "失败"
     * }
     *
     * 微信发送过来的通知可能因为网络不稳定而出现网络超时，5S
     * 如果微信未能成功获取我们的响应，就会重复发送支付通知
     *
     */
    @ApiOperation("支付通知")
    @PostMapping("/native/notify")
    public String nativeNotify(HttpServletRequest request, HttpServletResponse response) {
        Gson gson = new Gson();
        // 构造应答对象
        Map<String, String> map = new HashMap<>();

        try{
            // 1.处理通知参数
            String body = HttpUtils.readData(request);
            Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
            log.info("支付通知的id:{}", bodyMap.get("id"));
            log.info("支付通知的完整数据:{}", body);

            // 2.签名验证
            WechatPay2ValidatorForRequest validator
                    = new WechatPay2ValidatorForRequest(verifier, body, (String) bodyMap.get("id"));
            if (!validator.validate(request)) {
                log.error("通知验签失败");
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "通知验签失败");
                return gson.toJson(map);
            }
            log.info("通知验签成功");

            // 3.处理订单 微信返回的通知数据是加密的
            wxPayService.processOrder(bodyMap);

            // 测试超时应答：添加睡眠时间使应答超时
            // 模拟超时，微信会重复请求，需要排除已处理过的订单
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

    /**
     * 用户取消订单
     * @return
     */
    @ApiOperation("取消订单")
    @PostMapping("/cancel/{orderNo}")
    public R cancel(@PathVariable String orderNo) throws IOException {
        log.info("取消订单");
        wxPayService.cancelOrder(orderNo);
        return R.ok().setMessage("订单已取消");
    }

    /**
     * 微信支付查单API
     * https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_2.shtml
     * 商户可以通过查询订单接口主动查询订单状态，完成下一步的业务逻辑。查询订单状态可通过微信支付订单号或商户订单号两种方式查询
     * 通常商户后台未收到异步支付结果通知时，商户主动调用查单接口，同步订单状态。
     * {
     *   "code": 0,
     *   "message": "查询成功",
     *   "data": {
     *     "bodyAsString": "{\"amount\":{\"currency\":\"CNY\",\"payer_currency\":\"CNY\",\"payer_total\":1,\"total\":1},\"appid\":\"wx74862e0dfcf69954\",\"attach\":\"\",\"bank_type\":\"OTHERS\",\"mchid\":\"1558950191\",\"out_trade_no\":\"ORDER_20220828172344338\",\"payer\":{\"openid\":\"oHwsHuCgDFPyqFo2Sawg6yA0Pu4A\"},\"promotion_detail\":[],\"success_time\":\"2022-08-28T17:23:59+08:00\",\"trade_state\":\"SUCCESS\",\"trade_state_desc\":\"支付成功\",\"trade_type\":\"NATIVE\",\"transaction_id\":\"4200001550202208284738686219\"}"
     *   }
     * }
     */
    @ApiOperation("查询订单：测试订单状态用")
    @GetMapping("/query/{orderNo}")
    public R queryOrder(@PathVariable String orderNo) throws IOException {
        log.info("查询订单");
        String bodyAsString = wxPayService.queryOrder(orderNo);
        return R.ok().setMessage("查询成功").data("bodyAsString", bodyAsString);
    }

    /**
     * https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_4_9.shtml
     * @return
     */
    @ApiOperation("申请退款")
    @PostMapping("/refunds/{orderNo}/{reason}")
    public R refunds(@PathVariable String orderNo,
                     @PathVariable String reason) throws IOException {
        log.info("申请退款");
        // 保存退款记录，调用微信退款接口，更新订单状态，更新退款单
        wxPayService.refund(orderNo, reason);
        return R.ok();
    }

    /**
     * 查询退款情况
     * @param refundNo 退款编号，商户内部生成
     * @return
     * {
     *   "code": 0,
     *   "message": "查询成功",
     *   "data": {
     *     "result": "{\"amount\":{\"currency\":\"CNY\",\"discount_refund\":0,\"from\":[],\"payer_refund\":1,\"payer_total\":1,\"refund\":1,\"settlement_refund\":1,\"settlement_total\":1,\"total\":1},\"channel\":\"ORIGINAL\",\"create_time\":\"2022-08-31T15:50:34+08:00\",\"funds_account\":\"AVAILABLE\",\"out_refund_no\":\"REFUND_20220831155032315\",\"out_trade_no\":\"ORDER_20220828172344338\",\"promotion_detail\":[],\"refund_id\":\"50302003012022083124382080035\",\"status\":\"SUCCESS\",\"success_time\":\"2022-08-31T15:50:41+08:00\",\"transaction_id\":\"4200001550202208284738686219\",\"user_received_account\":\"支付用户零钱\"}"
     *   }
     * }
     */
    @ApiOperation("查询退款：测试用")
    @GetMapping("/query-refund/{refundNo}")
    public R queryRefund(@PathVariable String refundNo) throws IOException {
        log.info("查询退款信息，refundNo:{}", refundNo);
        String result = wxPayService.queryRefund(refundNo);
        return R.ok().setMessage("查询成功").data("result", result);
    }

    /**
     * 退款结果通知
     * 退款状态改变后，微信会把相关的退款结果发送给用户
     * https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_4_11.shtml
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/refunds/notify")
    public String refundsNotify(HttpServletRequest request, HttpServletResponse response) {

        // 1.日志处理
        log.info("处理退款通知...");

        // 2.获取请求参数
        // 为了读返回的json字符串
        Gson gson = new Gson();
        // 为了响应的数据
        Map<String, String> map = new HashMap<>();
        try {
            // 2.1处理通知参数
            String data = HttpUtils.readData(request);
            Map<String, Object> dataMap = gson.fromJson(data, HashMap.class);
            // 为了校验请求签名
            String requestId = (String) dataMap.get("id");
            // 验证签名
            WechatPay2ValidatorForRequest wechatPay2ValidatorForRequest = new WechatPay2ValidatorForRequest(verifier, data, requestId);
            if (!wechatPay2ValidatorForRequest.validate(request)) {
                log.error("退款通知验签失败...");
                // 失败应答
                response.setStatus(500);
                map.put("code", "FAIL");
                map.put("message", "退款通知验签失败");
                return gson.toJson(map);
            }
            log.info("退款通知验签成功...");
            // 3.处理退款订单
            wxPayService.processRefund(dataMap);
            // 成功应答
            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("message", "成功");
            return gson.toJson(map);
        } catch (Exception e) {
            e.printStackTrace();
            // 4.设置响应参数
            response.setStatus(500);
            map.put("code", "FAIL");
            map.put("message", "失败");
            return gson.toJson(map);
        }
    }


    /**
     * 获取交易账单URL
     * 微信支付按天提供交易账单文件，服务商可以通过该接口获取账单文件的下载地址。
     * 文件内包含交易相关的金额、时间、营销等信息，供商户核对订单、退款、银行到账等情况。
     * @param billDate
     * @param type
     * @return
     * @throws Exception
     */
    @ApiOperation("获取账单url：测试用")
    @GetMapping("/querybill/{billDate}/{type}")
    public R queryTradeBill(
            @PathVariable String billDate,
            @PathVariable String type) throws Exception {
        log.info("获取交易账单URL");
        String downloadUrl = wxPayService.queryBill(billDate, type);
        return R.ok().setMessage("获取交易账单URL成功").data("downloadUrl", downloadUrl);
    }


    /**
     * 下载账单API为通用接口，交易/资金账单都可以通过该接口获取到对应的账单。
     * 账单文件的下载地址的有效时间为30s
     * 这个微信接口响应信息头不包含微信接口响应的签名值，因此需要跳过验签的流程，所以需要我们在获取下载地址的时候对url进行hash比较，但是都对响应验签了，还有必要吗
     * 感觉不是文件啊，是个json
     * @param billDate
     * @param type
     * @return
     * @throws Exception
     */
    @ApiOperation("下载账单")
    @GetMapping("/downloadbill/{billDate}/{type}")
    public R downloadBill(
            @PathVariable String billDate,
            @PathVariable String type) throws Exception {
        log.info("下载账单");
        String result = wxPayService.downloadBill(billDate, type);
        return R.ok().data("result", result);
    }

}
