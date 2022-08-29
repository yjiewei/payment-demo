package com.yangjiewei.paymentdemo.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * @author yangjiewei
 * @date 2022/8/24
 */
public interface WxPayService {

    /**
     * native下单
     * @param productId 产品ID
     * @return 支付二维码及订单号
     */
    Map<String, Object> nativePay(Long productId) throws Exception;

    /**
     * 支付通知中的订单处理
     * @param bodyMap 支付通知参数
     * @throws GeneralSecurityException
     */
    void processOrder(Map<String, Object> bodyMap) throws GeneralSecurityException;

    /**
     * 取消订单
     * @param orderNo
     */
    void cancelOrder(String orderNo) throws IOException;

    /**
     * 查询订单
     */
    String queryOrder(String orderNo) throws IOException;
}
