package com.yangjiewei.paymentdemo.service;

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
}
