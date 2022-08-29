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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
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
     * 查询订单列表并按照创建时间降序返回
     * 按理来说应该根据用户信息去获取的，这里简化了
     */
    @Override
    public List<OrderInfo> listOrderByCreateTimeDesc() {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 更新订单支付状态
     */
    @Override
    public void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus) {
        log.info("更新订单状态：{}", orderStatus.getType());
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);

        // 这里要两次数据库交互，可以试下直接更新
        // OrderInfo orderInfo = baseMapper.selectOne(queryWrapper);
        // orderInfo.setOrderStatus(orderStatus.getType());
        // baseMapper.updateById(orderInfo);

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderStatus(orderStatus.getType());
        baseMapper.update(orderInfo, queryWrapper);
    }

    @Override
    public String getOrderStatus(String orderNo) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        OrderInfo orderInfo = baseMapper.selectOne(queryWrapper);
        if (Objects.isNull(orderInfo)) {
            return null;
        }
        return orderInfo.getOrderStatus();
    }

    /**
     * 找出创建超过minutes分钟并且未支付的订单
     */
    @Override
    public List<OrderInfo> getNoPayOrderByDuration(int minutes) {
        // 比当前时间少五分钟
        Instant instant = Instant.now().minus(Duration.ofMinutes(minutes));
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_status", OrderStatus.NOTPAY.getType());
        queryWrapper.le("create_time", instant);
        return baseMapper.selectList(queryWrapper);
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
