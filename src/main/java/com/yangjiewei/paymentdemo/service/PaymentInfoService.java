package com.yangjiewei.paymentdemo.service;

import java.util.Map;

public interface PaymentInfoService {

    /**
     * 创建支付信息，记录微信支付日志
     * @param plainText
     */
    void createPaymentInfo(String plainText);

    /**
     * 创建支付宝支付日志
     * @param params
     */
    void createPaymentInfoForAlipay(Map<String, String> params);
}
