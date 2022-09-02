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

    /**
     * 核实订单状态
     * @param orderNo
     */
    void checkOrderStatus(String orderNo) throws IOException;

    /**
     * 申请退款
     * @param orderNo
     * @param reason
     */
    void refund(String orderNo, String reason) throws IOException;

    /**
     * 查询退款接口用
     * @param refundNo
     * @return
     */
    String queryRefund(String refundNo) throws IOException;

    /**
     * 检查退款状态
     * @param refundNo
     */
    void checkRefundStatus(String refundNo) throws IOException;

    /**
     * 处理退款订单
     * @param dataMap
     */
    void processRefund(Map<String, Object> dataMap) throws Exception;

    /**
     * 获取交易账单URL
     */
    String queryBill(String billDate, String type) throws IOException;

    /**
     * 下载账单
     * @param billDate
     * @param type
     * @return
     */
    String downloadBill(String billDate, String type) throws IOException;
}
