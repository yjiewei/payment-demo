package com.yangjiewei.paymentdemo.service;

import com.yangjiewei.paymentdemo.entity.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrderInfoService extends IService<OrderInfo> {

    /**
     * 保存订单
     */
    OrderInfo createOrderByProductId(Long productId);

    /**
     * 缓存二维码
     */
    void saveCodeUrl(String orderNo, String codeUrl);

}
