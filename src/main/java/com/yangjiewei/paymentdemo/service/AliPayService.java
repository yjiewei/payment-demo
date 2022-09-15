package com.yangjiewei.paymentdemo.service;

import com.alipay.api.AlipayApiException;

public interface AliPayService {

    /**
     * 创建下单，会获取一个html形式的form表单，会自动提交，action指向支付宝支付页面
     * @param productId
     * @return
     */
    String tradeCreate(Long productId) throws AlipayApiException;
}
