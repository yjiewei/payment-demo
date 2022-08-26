package com.yangjiewei.paymentdemo.service;

public interface PaymentInfoService {

    /**
     * 创建支付信息，记录支付日志
     * @param plainText
     */
    void createPaymentInfo(String plainText);
}
