package com.yangjiewei.paymentdemo.service;

import com.alipay.api.AlipayApiException;

import java.util.Map;

public interface AliPayService {

    /**
     * 创建下单，会获取一个html形式的form表单，会自动提交，action指向支付宝支付页面
     * @param productId
     * @return
     */
    String tradeCreate(Long productId) throws AlipayApiException;

    /**
     * 处理订单
     *   1.更新订单状态
     *   2.记录支付日志
     * @param params
     */
    void processOrder(Map<String, String> params);

    /**
     * 取消订单
     * @param orderNo
     */
    void cancelOrder(String orderNo);

    /**
     * 查询订单
     * @param orderNo
     * @return 返回订单查询结果，如果返回null则表示支付宝端尚未创建订单
     */
    String queryOrder(String orderNo);

    /**
     * 查看订单状态
     * @param orderNo
     */
    void checkOrderStatus(String orderNo);

    /**
     * 根据订单号退款
     * @param orderNo
     * @param reason
     */
    void refund(String orderNo, String reason);

    /**
     * 根据订单号查询退款单
     * @param orderNo
     * @return
     */
    String queryRefund(String orderNo);

    /**
     * 检查退款状态，并做对应的处理
     * @param refundNo
     */
    void checkRefundStatus(String refundNo);

    /**
     * 查询账单地址
     * @param billDate
     * @param type
     * @return
     */
    String queryBill(String billDate, String type);
}
