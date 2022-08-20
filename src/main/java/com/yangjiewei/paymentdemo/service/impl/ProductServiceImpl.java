package com.yangjiewei.paymentdemo.service.impl;

import com.yangjiewei.paymentdemo.entity.Product;
import com.yangjiewei.paymentdemo.mapper.ProductMapper;
import com.yangjiewei.paymentdemo.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

}
