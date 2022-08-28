package com.yangjiewei.paymentdemo.controller;

import com.yangjiewei.paymentdemo.entity.OrderInfo;
import com.yangjiewei.paymentdemo.enums.OrderStatus;
import com.yangjiewei.paymentdemo.service.OrderInfoService;
import com.yangjiewei.paymentdemo.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 订单信息
 * @author yangjiewei
 * @date 2022/8/25
 */
@Api(tags = "商品订单管理")
@CrossOrigin
@RestController
@RequestMapping("/api/order-info")
public class OrderInfoController {

    @Resource
    private OrderInfoService orderInfoService;

    /**
     * {
     *   "code": 0,
     *   "message": "成功",
     *   "data": {
     *     "list": [
     *       {
     *         "id": "2",
     *         "createTime": "2022-08-25 07:36:10",
     *         "updateTime": "2022-08-25 07:36:10",
     *         "title": "大数据课程",
     *         "orderNo": "ORDER_20220825153610144",
     *         "userId": null,
     *         "productId": 2,
     *         "totalFee": 1,
     *         "codeUrl": "weixin://wxpay/bizpayurl?pr=eYIRr3yzz",
     *         "orderStatus": "未支付"
     *       }
     *     ]
     *   }
     * }
     */
    @ApiOperation("订单列表")
    @GetMapping("/list")
    public R list() {
        List<OrderInfo> orderInfoList = orderInfoService.listOrderByCreateTimeDesc();
        return R.ok().data("list", orderInfoList);
    }

    /**
     * 查询本地订单状态
     *
     */
    @ApiOperation("查询本地订单状态")
    @GetMapping("/query-order-status/{orderNo}")
    public R queryOrderStatus(@PathVariable String orderNo) {
        String orderStatus = orderInfoService.getOrderStatus(orderNo);
        if (OrderStatus.SUCCESS.getType().equals(orderStatus)) {
            return R.ok().setMessage("支付成功");
        }
        return R.ok().setCode(101).setMessage("支付中...");
    }

}

