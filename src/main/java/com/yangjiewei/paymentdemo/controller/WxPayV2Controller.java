package com.yangjiewei.paymentdemo.controller;

import com.github.wxpay.sdk.WXPayUtil;
import com.yangjiewei.paymentdemo.config.WxPayConfig;
import com.yangjiewei.paymentdemo.entity.OrderInfo;
import com.yangjiewei.paymentdemo.enums.OrderStatus;
import com.yangjiewei.paymentdemo.service.OrderInfoService;
import com.yangjiewei.paymentdemo.service.PaymentInfoService;
import com.yangjiewei.paymentdemo.service.WxPayService;
import com.yangjiewei.paymentdemo.util.HttpUtils;
import com.yangjiewei.paymentdemo.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yangjiewei
 * @date 2022/9/5
 */
@Slf4j
@CrossOrigin
@RestController
@Api(tags = "网站微信支付APIv2")
@RequestMapping("/api/wx-pay-v2")
public class WxPayV2Controller {

    @Resource
    private WxPayService wxPayService;

    @Resource
    private WxPayConfig wxPayConfig;

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private PaymentInfoService paymentInfoService;

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * native下单 v2
     * @param productId
     * @param request
     * @return
     */
    @ApiOperation("调用统一下单API，生成支付二维码")
    @PostMapping("/native/{productId}")
    public R createNative(@PathVariable Long productId, HttpServletRequest request) throws Exception {
        log.info("发起V2支付请求");
        String remoteAddr = request.getRemoteAddr();
        Map<String, Object> map = wxPayService.nativePayV2(productId, remoteAddr);
        return R.ok().setData(map);
    }

    /**
     * 支付通知
     * 微信支付通过支付通知接口将用户支付成功消息通知给商户
     */
    @PostMapping("/native/notify")
    public String wxNotify(HttpServletRequest request) throws Exception {
        log.info("微信native下单回调");
        Map<String, String> returnMap = new HashMap<>();
        String body = HttpUtils.readData(request);
        // 1.请求验签
        if (!WXPayUtil.isSignatureValid(body, wxPayConfig.getPartnerKey())) {
            log.error("通知验签失败");
            // 失败应答
            returnMap.put("return_code", "FAIL");
            returnMap.put("return_msg", "验签失败");
            return WXPayUtil.mapToXml(returnMap);
        }

        // 2.验签通过，解析xml数据
        Map<String, String> notifyMap = WXPayUtil.xmlToMap(body);
        // 3.判断通信和业务是否成功
        if(!"SUCCESS".equals(notifyMap.get("return_code")) ||
                !"SUCCESS".equals(notifyMap.get("result_code"))) {
            log.error("失败");
            // 失败应答
            returnMap.put("return_code", "FAIL");
            returnMap.put("return_msg", "失败");
            return WXPayUtil.mapToXml(returnMap);
        }
        // 4.通信和业务都成功了，处理响应及订单
        // 获取商户订单号
        String orderNo = notifyMap.get("out_trade_no");
        OrderInfo orderInfo = orderInfoService.getOrderByOrderNo(orderNo);
        // 4.1并校验返回的订单金额是否与商户侧的订单金额一致
        if (orderInfo != null && orderInfo.getTotalFee() !=
                Long.parseLong(notifyMap.get("total_fee"))) {
            log.error("失败");
            // 失败应答
            returnMap.put("return_code", "FAIL");
            returnMap.put("return_msg", "金额校验失败");
            return WXPayUtil.mapToXml(returnMap);
        }

        // 4.2处理订单
        if(lock.tryLock()){
            try {
                // 处理重复的通知
                // 接口调用的幂等性：无论接口被调用多少次，产生的结果是一致的。
                String orderStatus = orderInfoService.getOrderStatus(orderNo);
                if(OrderStatus.NOTPAY.getType().equals(orderStatus)){
                    // 更新订单状态
                    orderInfoService.updateStatusByOrderNo(orderNo,
                            OrderStatus.SUCCESS);
                    // 记录支付日志
                    paymentInfoService.createPaymentInfo(body);
                }
            } finally {
                // 要主动释放锁
                lock.unlock();
            }
        }
        // 4.3设置成功响应
        returnMap.put("return_code", "SUCCESS");
        returnMap.put("return_msg", "OK");
        String returnXml = WXPayUtil.mapToXml(returnMap);
        log.info("支付成功，已应答");
        return returnXml;
    }

}
