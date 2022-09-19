/*
 * @author yangjiewei
 * @date 2022/9/12 22:12
 */
package com.yangjiewei.paymentdemo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.yangjiewei.paymentdemo.entity.OrderInfo;
import com.yangjiewei.paymentdemo.enums.OrderStatus;
import com.yangjiewei.paymentdemo.enums.PayType;
import com.yangjiewei.paymentdemo.service.AliPayService;
import com.yangjiewei.paymentdemo.service.OrderInfoService;
import com.yangjiewei.paymentdemo.service.PaymentInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class AliPayServiceImpl implements AliPayService {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private Environment config;

    @Resource
    private PaymentInfoService paymentInfoService;

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    @Transactional // 允许回滚
    public String tradeCreate(Long productId) {
        try {
            // 1.日志记录
            log.info("生成订单");
            OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId, PayType.ALIPAY.getType());

            // 2.构造参数 包括公共参数和接口参数
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            // 支付成功之后支付宝跳转到我们商户的页面
            request.setReturnUrl(config.getProperty("alipay.return-url"));
            // 配置需要的公共请求参数
            // 支付完成后，支付宝向商户发起异步通知的地址
            request.setNotifyUrl(config.getProperty("alipay.notify-url"));
            // 组装当前业务下单所需要的参数 https://opendocs.alipay.com/open/028r8t?scene=22#%E5%93%8D%E5%BA%94%E5%8F%82%E6%95%B0_2
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderInfo.getOrderNo());
            BigDecimal total = new BigDecimal(orderInfo.getTotalFee().toString()).divide(new BigDecimal("100")); // 这里单位是元
            bizContent.put("total_amount", total);
            bizContent.put("subject", orderInfo.getTitle());
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

            request.setBizContent(bizContent.toString());

            // 3.调用支付宝下单接口
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (response.isSuccess()) {
                log.info("调用成功，返回结果是:{}", response.getBody());
                return response.getBody();
            }
            log.info("调用失败，返回码:{}，具体信息是:{}", response.getCode(), response.getMsg());
            throw new RuntimeException();
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new RuntimeException("创建支付交易失败");
        }
    }

    /**
     * 处理订单
     *   1.更新订单状态
     *   2.记录支付日志
     * @param params
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processOrder(Map<String, String> params) {

        log.info("处理订单");

        String outTradeNo = params.get("out_trade_no");

        // 数据并发问题，在业务处理时，应该加锁
        if (lock.tryLock()) {
            try{

                // 处理重复通知
                // 接口调用的幂等性：无论接口被调用多少次，以下业务执行一次
                String orderStatus = orderInfoService.getOrderStatus(outTradeNo);
                if (!OrderStatus.NOTPAY.getType().equals(orderStatus)) {
                    return;
                }

                orderInfoService.updateStatusByOrderNo(outTradeNo, OrderStatus.SUCCESS);

                paymentInfoService.createPaymentInfoForAlipay(params);

            }finally {
                lock.unlock();
            }
        }
    }

    /**
     * 用户取消订单:关闭支付宝订单，修改商户订单状态
     * @param orderNo
     */
    @Override
    public void cancelOrder(String orderNo) {

        this.closeOrder(orderNo);

        orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CANCEL);

    }

    /**
     * 查询订单
     * @param orderNo
     * @return 返回订单查询结果，如果返回null则表示支付宝端尚未创建订单
     */
    @Override
    public String queryOrder(String orderNo) {
        try {
            log.info("查单接口调用:{}", orderNo);
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderNo);
            request.setBizContent(bizContent.toString());
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if(response.isSuccess()){
                log.info("调用成功，返回结果:{}", response.getBody());
                return response.getBody();
            } else {
                log.info("调用失败，返回码:{}, 返回描述:{} ", response.getCode(), response.getMsg() + " " + response.getSubMsg());
                // 订单不存在
                return null;
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new RuntimeException("查单接口的调用失败");
        }
    }

    /**
     * 支付宝关单接口
     * @param orderNo
     */
    private void closeOrder(String orderNo) {
        try {
            log.info("关单接口的调用，订单号:{}", orderNo);
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("trade_no", "2013112611001004680073956707");
            request.setBizContent(bizContent.toString());
            AlipayTradeCloseResponse response = alipayClient.execute(request);
            if(response.isSuccess()){
                log.info("关单接口调用成功");
            } else {
                log.warn("关单接口调用失败，返回状态码是:{}，描述信息是:{}", response.getCode(), response.getMsg());
                // 为什么不抛异常呢？
                // throw new RuntimeException("关单接口的调用失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("关单接口调用失败");
        }
    }
}
