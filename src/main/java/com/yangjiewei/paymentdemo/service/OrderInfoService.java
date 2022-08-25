package com.yangjiewei.paymentdemo.service;

import com.yangjiewei.paymentdemo.entity.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrderInfoService extends IService<OrderInfo> {

    OrderInfo createOrderByProductId(Long productId);

}
