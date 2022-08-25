package com.yangjiewei.paymentdemo.controller;

import com.yangjiewei.paymentdemo.entity.OrderInfo;
import com.yangjiewei.paymentdemo.service.OrderInfoService;
import com.yangjiewei.paymentdemo.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}

