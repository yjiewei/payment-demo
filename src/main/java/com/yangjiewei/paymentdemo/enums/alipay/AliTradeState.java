/*
 * @author yangjiewei
 * @date 2022/9/18 20:20
 */
package com.yangjiewei.paymentdemo.enums.alipay;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum  AliTradeState {

    /**
     * 支付成功
     */
    TRADE_SUCCESS("TRADE_SUCCESS");

    /**
     * 交易状态
     */
    private final String status;

}
