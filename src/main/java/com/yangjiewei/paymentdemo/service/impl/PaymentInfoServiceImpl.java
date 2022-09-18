package com.yangjiewei.paymentdemo.service.impl;

import com.google.gson.Gson;
import com.yangjiewei.paymentdemo.entity.PaymentInfo;
import com.yangjiewei.paymentdemo.enums.PayType;
import com.yangjiewei.paymentdemo.mapper.PaymentInfoMapper;
import com.yangjiewei.paymentdemo.service.PaymentInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

    /**
     * 创建支付信息，记录微信支付日志
     * @param plainText
     */
    @Override
    public void createPaymentInfo(String plainText) {

        log.info("记录微信支付日志");

        Gson gson = new Gson();
        Map<String, Object> plainTextMap = gson.fromJson(plainText, HashMap.class);

        String orderNo = (String)plainTextMap.get("out_trade_no");
        String transactionId = (String)plainTextMap.get("transaction_id");
        String tradeType = (String)plainTextMap.get("trade_type");
        String tradeState = (String)plainTextMap.get("trade_state");
        Map<String, Object> amount = (Map)plainTextMap.get("amount");
        Integer payerTotal = ((Double) amount.get("payer_total")).intValue();

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderNo(orderNo);
        paymentInfo.setPaymentType(PayType.WXPAY.getType());
        paymentInfo.setTransactionId(transactionId);
        paymentInfo.setTradeType(tradeType);
        paymentInfo.setTradeState(tradeState);
        paymentInfo.setPayerTotal(payerTotal);
        paymentInfo.setContent(plainText);

        baseMapper.insert(paymentInfo);
    }

    /**
     * 记录支付宝支付日志
     * @param params
     */
    @Override
    public void createPaymentInfoForAlipay(Map<String, String> params) {

        log.info("记录支付宝支付日志");

        // 获取订单号
        String orderNo = params.get("out_trade_no");
        // 业务编号
        String transactionId = params.get("trade_no");
        // 交易状态
        String tradeStatus = params.get("trade_status");
        // 交易金额
        String totalAmount = params.get("total_amount");
        int totalAmountInt = new BigDecimal(totalAmount).multiply(new BigDecimal("100")).intValue();

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderNo(orderNo);
        paymentInfo.setPaymentType(PayType.ALIPAY.getType());
        paymentInfo.setTransactionId(transactionId);
        paymentInfo.setTradeType("电脑网站支付");
        paymentInfo.setTradeState(tradeStatus);
        paymentInfo.setPayerTotal(totalAmountInt);

        Gson gson = new Gson();
        String json = gson.toJson(params, HashMap.class);
        paymentInfo.setContent(json);

        baseMapper.insert(paymentInfo);
    }
}
