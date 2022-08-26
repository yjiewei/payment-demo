package com.yangjiewei.paymentdemo.service;

import com.yangjiewei.paymentdemo.entity.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yangjiewei.paymentdemo.enums.OrderStatus;

import java.util.List;

public interface OrderInfoService extends IService<OrderInfo> {

    /**
     * 保存订单
     */
    OrderInfo createOrderByProductId(Long productId);

    /**
     * 缓存二维码
     */
    void saveCodeUrl(String orderNo, String codeUrl);

    /**
     * 查询订单列表并按照创建时间降序返回
     */
    List<OrderInfo> listOrderByCreateTimeDesc();

    /**
     * 更新订单支付状态
     */
    void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus);
}
