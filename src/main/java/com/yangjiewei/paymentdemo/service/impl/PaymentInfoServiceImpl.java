package com.yangjiewei.paymentdemo.service.impl;

import com.yangjiewei.paymentdemo.entity.PaymentInfo;
import com.yangjiewei.paymentdemo.mapper.PaymentInfoMapper;
import com.yangjiewei.paymentdemo.service.PaymentInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

}
