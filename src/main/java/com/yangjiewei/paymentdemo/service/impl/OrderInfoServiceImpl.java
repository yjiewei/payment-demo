package com.yangjiewei.paymentdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yangjiewei.paymentdemo.entity.OrderInfo;
import com.yangjiewei.paymentdemo.entity.Product;
import com.yangjiewei.paymentdemo.enums.OrderStatus;
import com.yangjiewei.paymentdemo.mapper.OrderInfoMapper;
import com.yangjiewei.paymentdemo.mapper.ProductMapper;
import com.yangjiewei.paymentdemo.service.OrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yangjiewei.paymentdemo.util.OrderNoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Slf4j
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Resource
    private ProductMapper productMapper;

    /**
     * 保存订单
     */
    @Override
    public OrderInfo createOrderByProductId(Long productId) {
        // 1.查找已存在但未支付的订单  这里没有用用户去做区分，这里仅用商品
        OrderInfo orderInfo = this.getNoPayOrderByProductId(productId);
        if (Objects.nonNull(orderInfo)) {
            log.info("存在未支付的订单，订单id：{}", orderInfo.getId());
            return orderInfo;
        }

        // 2.获取商品信息
        Product product = productMapper.selectById(productId);

        // 3.生成订单
        orderInfo = new OrderInfo();
        orderInfo.setTitle(product.getTitle());
        orderInfo.setOrderNo(OrderNoUtils.getOrderNo());
        orderInfo.setProductId(productId);
        orderInfo.setTotalFee(product.getPrice());
        orderInfo.setOrderStatus(OrderStatus.NOTPAY.getType());
        baseMapper.insert(orderInfo);
        log.info("返回的订单id：{}", orderInfo.getId());

        return orderInfo;
    }

    /**
     * 下单成功之后才能去缓存二维码，保存订单的时候还没有二维码
     */
    @Override
    public void saveCodeUrl(String orderNo, String codeUrl) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        OrderInfo orderInfo = baseMapper.selectOne(queryWrapper);
        orderInfo.setCodeUrl(codeUrl);
        baseMapper.updateById(orderInfo);
    }

    /**
     * 查找已存在但未支付的订单,防止重复创建订单对象
     */
    private OrderInfo getNoPayOrderByProductId(Long productId) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.eq("order_status", OrderStatus.NOTPAY.getType());
        //queryWrapper.eq("user_id", userId);
        return baseMapper.selectOne(queryWrapper);
    }
}
