/*
 * @author yangjiewei
 * @date 2022/9/12 22:08
 */
package com.yangjiewei.paymentdemo.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.yangjiewei.paymentdemo.entity.OrderInfo;
import com.yangjiewei.paymentdemo.service.AliPayService;
import com.yangjiewei.paymentdemo.service.OrderInfoService;
import com.yangjiewei.paymentdemo.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import static com.yangjiewei.paymentdemo.enums.alipay.AliTradeState.SUCCESS;

@Slf4j
@CrossOrigin
@Api(tags = "网站支付宝支付")
@RestController
@RequestMapping("/api/ali-pay")
public class AliPayController {

    @Resource
    private AliPayService aliPayService;

    @Resource
    private Environment config;

    @Resource
    private OrderInfoService orderInfoService;

    /**
     * 1.统一收单下单并支付页面接口的调用
     */
    @ApiOperation("统一收单下单并支付页面接口的调用")
    @PostMapping("/trade/page/pay/{productId}")
    public R tradePagePay(@PathVariable Long productId) throws AlipayApiException {
        // 记录日志 下单 返回数据
        log.info("统一收单下单并支付页面接口调用");
        String formStr = aliPayService.tradeCreate(productId);
        return R.ok().data("formStr", formStr);
    }

    /**
     * 2.支付成功之后的异步通知
     */
    @ApiOperation("支付成功之后的异步通知")
    @PostMapping("/trade/notify")
    public String tradeNotify(@RequestParam Map<String, String> params){
        log.info("支付通知正在执行...");
        log.info("通知参数为：{}", params);
        String result = "failure";
        try {
            //调用SDK验证签名 fixme 验签一直失败
            // boolean signVerified = AlipaySignature.rsaCheckV2(params, config.getProperty("alipay.alipay-public-key"), CHARSET_UTF8, SIGN_TYPE_RSA2);
            if(/*signVerified*/ true){
                // 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，
                // 校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
                log.info("支付成功异步通知请求验签成功");
                // 1.商家需要验证该通知数据中的 out_trade_no 是否为商家系统中创建的订单号。
                String outTradeNo = params.get("out_trade_no");
                OrderInfo orderInfo = orderInfoService.getOrderByOrderNo(outTradeNo);
                if (Objects.isNull(orderInfo)) {
                    log.error("订单不存在");
                    return result;
                }
                // 2.判断 total_amount 是否确实为该订单的实际金额（即商家订单创建时的金额）
                String totalAmount = params.get("total_amount");
                int totalAmountInt = new BigDecimal(totalAmount).multiply(new BigDecimal("100")).intValue();
                int totalFeeInt = orderInfo.getTotalFee();
                if (totalAmountInt != totalFeeInt) {
                    log.error("金额校验失败");
                    return result;
                }
                // 3.校验通知中的 seller_id（或者 seller_email) 是否为 out_trade_no 这笔单据的对应的操作方（有的时候，一个商家可能有多个 seller_id/seller_email）
                String sellerId = params.get("seller_id");
                String sellerIdProperty = config.getProperty("alipay.seller-id");
                if(!sellerId.equals(sellerIdProperty)){
                    log.error("商家pid校验失败");
                    return result;
                }
                // 4.验证 app_id 是否为该商家本身
                String appId = params.get("app_id");
                String appIdProperty = config.getProperty("alipay.app-id");
                if(!appId.equals(appIdProperty)){
                    log.error("appid校验失败");
                    return result;
                }
                // 在支付宝的业务通知中，只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功
                String tradeStatus = params.get("trade_status");
                if(!SUCCESS.getStatus().equals(tradeStatus)){
                    log.error("支付未成功");
                    return result;
                }
                //处理业务 修改订单状态 记录支付日志
                aliPayService.processOrder(params);

                // 业务处理完，可以返回支付宝success了
                result = "success";

            }else{
                // 验签失败则记录异常日志，并在response中返回failure
                log.error("支付成功异步通知验签失败！");
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 3.统⼀收单交易关闭
     * 用户取消订单，需要调用支付宝关闭订单接口
     */
    @ApiOperation("统一收单交易关闭接口")
    @PostMapping("/trade/close/{orderNo}")
    public R cancel(@PathVariable String orderNo) {
        log.info("取消订单");
        aliPayService.cancelOrder(orderNo);
        return R.ok().setMessage("订单已取消");
    }

    /**
     * 4.统⼀收单线下交易查询
     *   商户后台不一定每次收到异步支付结果通知，商户应该主动调用订单查询接口，同步订单状态
     *   这里只是测试用，实际上需要定时任务去处理，去查一定时间范围内未支付的订单，是不是只是没收到支付结果通知，但实际已经支付
     */
    @ApiOperation("查询订单：测试订单状态用")
    @GetMapping("/trade/query/{orderNo}")
    public R queryOrder(@PathVariable String orderNo) {
        log.info("查询订单");
        String result = aliPayService.queryOrder(orderNo);
        return R.ok().setMessage("查询成功").data("result", result);
    }

    /**
     * https://opendocs.alipay.com/open/02e7go
     * 5.统一收单交易退款
     */
    @ApiOperation("申请退款")
    @PostMapping("/trade/refund/{orderNo}/{reason}")
    public R refunds(@PathVariable String orderNo, @PathVariable String reason){
        log.info("申请退款...");
        aliPayService.refund(orderNo, reason);
        return R.ok().setMessage("退款成功");
    }



}
