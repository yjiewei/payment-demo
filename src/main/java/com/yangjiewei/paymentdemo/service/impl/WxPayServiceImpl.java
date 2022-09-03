package com.yangjiewei.paymentdemo.service.impl;

import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import com.yangjiewei.paymentdemo.config.WxPayConfig;
import com.yangjiewei.paymentdemo.entity.OrderInfo;
import com.yangjiewei.paymentdemo.entity.RefundInfo;
import com.yangjiewei.paymentdemo.enums.OrderStatus;
import com.yangjiewei.paymentdemo.enums.wxpay.WxApiType;
import com.yangjiewei.paymentdemo.enums.wxpay.WxNotifyType;
import com.yangjiewei.paymentdemo.enums.wxpay.WxRefundStatus;
import com.yangjiewei.paymentdemo.enums.wxpay.WxTradeState;
import com.yangjiewei.paymentdemo.service.OrderInfoService;
import com.yangjiewei.paymentdemo.service.PaymentInfoService;
import com.yangjiewei.paymentdemo.service.RefundInfoService;
import com.yangjiewei.paymentdemo.service.WxPayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yangjiewei
 * @date 2022/8/24
 */
@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {

    /**
     * 获取微信支付的配置信息
     */
    @Resource
    private WxPayConfig wxPayConfig;

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private PaymentInfoService paymentInfoService;

    @Resource
    private RefundInfoService refundInfoService;

    /**
     * 获取微信支付的httpClient，可以签名验签
     */
    @Resource
    private CloseableHttpClient wxPayClient;

    /**
     * 获取微信支付的httpClient，不对响应进行验签
     */
    @Resource
    private CloseableHttpClient wxPayNoSignClient;

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 开发指引：https://pay.weixin.qq.com/wiki/doc/apiv3/open/pay/chapter2_7_2.shtml
     * 接口文档：https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_4_1.shtml
     * requestJson
     * {
     *   "amount":{
     *       "total":1,
     *       "currency":"CNY"
     *   },
     *   "mchid":"1558950191",
     *   "out_trade_no":"ORDER_20220825104830065",
     *   "appid":"wx74862e0dfcf69954",
     *   "description":"test",
     *   "notify_url":"https://500c-219-143-130-12.ngrok.io/api/wx-pay/native/notify"
     * }
     *
     * response
     * {
     *   "code": 0,
     *   "message": "成功",
     *   "data": {
     *     "codeUrl": "weixin://wxpay/bizpayurl?pr=tyq42wrzz",
     *     "orderNo": "ORDER_20220825104830065"
     *   }
     * }
     */
    @Override
    public Map<String, Object> nativePay(Long productId) throws Exception {
        log.info("1.生成订单");

        String codeUrl;
        OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId);
        if (Objects.nonNull(orderInfo ) && !StringUtils.isEmpty(orderInfo.getCodeUrl())) {
            log.info("订单已存在，二维码已保存");
            log.info("订单号:{}，二维码:{}", orderInfo.getOrderNo(), orderInfo.getCodeUrl());
            codeUrl = orderInfo.getCodeUrl();
            // 返回二维码
            Map<String, Object> map = new HashMap<>();
            map.put("codeUrl", codeUrl);
            map.put("orderNo", orderInfo.getOrderNo());
            return map;
        }

        log.info("2.调用统一下单api");

        // 创建post请求
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType()));

        // 构造请求参数
        // 这里请求参数很多，只传必填项就可以了，请求和响应都是json格式
        // gson是处理json的
        Gson gson = new Gson();
        // 你怎么知道要这些参数，参考文档啊 https://pay.weixin.qq.com/wiki/doc/apiv3_partner/apis/chapter4_4_1.shtml
        Map paramsMap = new HashMap();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        paramsMap.put("description", orderInfo.getTitle());
        paramsMap.put("out_trade_no", orderInfo.getOrderNo());
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));
        // 订单金额对象
        Map amountMap = new HashMap();
        amountMap.put("total", orderInfo.getTotalFee());
        amountMap.put("currency", "CNY");

        paramsMap.put("amount", amountMap);

        // 将参数转化成json字符串
        String requestJson = gson.toJson(paramsMap);
        log.info("3.构造请求参数");
        log.info("请求参数：{}", requestJson);

        // 设置请求体及请求头
        StringEntity entity = new StringEntity(requestJson, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        // 完成签名并执行请求 wxPayClient会自动的处理签名和验签，并进行证书自动更新
        CloseableHttpResponse nativePayResponse = wxPayClient.execute(httpPost);
        log.info("4.解析微信native下单响应");
        try{
            // 获取响应体并转为字符串和响应状态码
            String response = EntityUtils.toString(nativePayResponse.getEntity());
            int statusCode = nativePayResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                // 处理成功
                log.info("成功, 返回结果 = " + response);
            } else if (statusCode == 204) {
                // 处理成功，无返回Body
                log.info("成功");
            } else {
                log.info("Native下单失败,响应码 = " + statusCode+ ",返回结果 = " + response);
                throw new IOException("request failed");
            }
            // 响应结果 json字符串转对象
            Map<String, String> resultMap = gson.fromJson(response, HashMap.class);
            // 获取二维码并保存
            codeUrl = resultMap.get("code_url");
            String orderNo = orderInfo.getOrderNo();
            orderInfoService.saveCodeUrl(orderNo, codeUrl);

            Map<String, Object> map = new HashMap<>();
            map.put("codeUrl", codeUrl);
            map.put("orderNo", orderNo);
            log.info("5.响应二维码：{}，订单号：{}", codeUrl, orderNo);
            return map;
        }finally {
            // fixme 为什么要关闭这个？连接资源有限？
            nativePayResponse.close();
        }
    }

    /**
     * 处理订单
     * @param bodyMap 支付通知参数
     * @throws GeneralSecurityException
     */
    @Override
    public void processOrder(Map<String, Object> bodyMap) throws GeneralSecurityException {
        log.info("处理订单");

        // 1.密文解密
        String plainText = decryptFromResource(bodyMap);

        // 2.转换明文 https://pay.weixin.qq.com/wiki/doc/apiv3_partner/apis/chapter4_4_5.shtml
        Gson gson = new Gson();
        Map<String, Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
        String orderNo = (String) plainTextMap.get("out_trade_no");

        /**
         * 在对业务数据进行状态检查和处理之前，这里要使用数据锁进行并发控制，以避免函数重入导致的数据混乱
         * 尝试获取锁成功之后才去处理数据，相比于同步锁，这里不会去等待，获取不到则直接返回
         */
        if (lock.tryLock()) {
            try {
                // 处理重复通知 出于接口幂等性考虑（无论接口被调用多少次，产生的结果都是一致的）
                String orderStatus = orderInfoService.getOrderStatus(orderNo);
                if (!OrderStatus.NOTPAY.getType().equals(orderStatus)) {
                    return ;
                }

/*                // 模拟通知并发 try catch快捷键是 ctrl+wins+alt+t
                // 虽然前面处理了重复通知，但是这里是并发导致，这里要使用数据锁进行并发控制，以避免函数重入导致的数据混乱
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

                // 3.更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);

                // 4.记录支付日志
                paymentInfoService.createPaymentInfo(plainText);
            } finally {
                // 要主动释放锁
                lock.unlock();
            }
        }

    }

    /**
     * 用户取消订单
     */
    @Override
    public void cancelOrder(String orderNo) throws IOException {
        // 调用微信支付的关单接口
        this.closeOrder(orderNo);
        //更新商户端的订单状态
        orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CANCEL);
    }

    /**
     * https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_2.shtml
     * 文档上的path方法是指在url上的值，query则是参数
     * 查询订单调用
     */
    @Override
    public String queryOrder(String orderNo) throws IOException {
        log.info("查单接口调用：{}", orderNo);
        String url = String.format(WxApiType.ORDER_QUERY_BY_NO.getType(), orderNo);
        url = wxPayConfig.getDomain().concat(url).concat("?mchid=").concat(wxPayConfig.getMchId());

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        // 完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpGet);

        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("成功，结果是：{}", bodyAsString);
            }else if (statusCode == 204) {
                log.info("成功，无返回内容");
            }else {
                log.info("查询订单失败,响应码 = " + statusCode+ ",返回结果 = " +
                        bodyAsString);
                throw new IOException("queryOrder request failed");
            }
            return bodyAsString;
        } finally {
            response.close();
        }
    }

    /**
     * 根据订单号查询微信支付查单接口，核实订单状态
     * 如果订单已支付，则更新商户端订单状态，并记录支付日志
     * 如果订单未支付，则调用关单接口关闭订单，并更新商户端订单状态
     * @param orderNo
     */
    @Override
    public void checkOrderStatus(String orderNo) throws IOException {
        log.warn("根据订单号核实订单状态 orderNo:{}", orderNo);
        // 1.调用微信支付查单接口
        String result = this.queryOrder(orderNo);

        // 2.转换响应参数
        Gson gson = new Gson();
        Map resultMap = gson.fromJson(result, HashMap.class);

        // 3.获取微信支付端的订单状态
        String tradeState = (String) resultMap.get("trade_state");

        // 4.判断订单状态 确认已支付则更新订单状态，否则关闭订单
        if (WxTradeState.SUCCESS.getType().equals(tradeState)) {
            log.warn("核实订单已支付，orderNo:{}", orderNo);
            // 如果确认订单已支付则更新本地订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
            // 记录支付日志
            paymentInfoService.createPaymentInfo(result);
        }

        if (WxTradeState.NOTPAY.getType().equals(tradeState)) {
            log.warn("核实订单未支付，orderNo:{}", orderNo);
            // 订单未支付，则调用关单接口
            this.closeOrder(orderNo);
            // 更新本地订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CLOSED);
        }

    }

    /**
     * 申请退款，这个接口和文档不一样了，不知道能不能行呢
     * @param orderNo
     * @param reason
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(String orderNo, String reason) throws IOException {

        log.info("创建退款单记录");
        // 根据订单号创建退款单
        RefundInfo refundInfo = refundInfoService.createRefundByOrderNo(orderNo, reason);

        log.info("调用微信退款接口");
        String url = wxPayConfig.getDomain().concat(WxApiType.DOMESTIC_REFUNDS.getType());
        HttpPost httpPost = new HttpPost(url);
        // 请求参数封装
        Gson gson = new Gson();
        Map paramsMap = new HashMap();
        paramsMap.put("out_trade_no", orderNo);//订单编号
        paramsMap.put("out_refund_no", refundInfo.getRefundNo());//退款单编号
        paramsMap.put("reason",reason);//退款原因
        // 退款通知地址，退款也进行了回调通知，类似下单处理？
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.REFUND_NOTIFY.getType()));

        Map amountMap = new HashMap();
        amountMap.put("refund", refundInfo.getRefund());//退款金额
        amountMap.put("total", refundInfo.getTotalFee());//原订单金额
        amountMap.put("currency", "CNY");//退款币种
        paramsMap.put("amount", amountMap);

        //将参数转换成json字符串
        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数:{}" + jsonParams);

        // 封装到请求中，并设置请求格式和响应格式
        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        // 发起退款请求，内部对请求做了签名，响应也验签了
        CloseableHttpResponse response = wxPayClient.execute(httpPost);

        // 解析响应
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("成功, 退款返回结果 = " + bodyAsString);
            } else if (statusCode == 204) {
                log.info("成功");
            } else {
                throw new RuntimeException("退款异常, 响应码 = " + statusCode+ ", 退款返回结果 = " + bodyAsString);
            }
            // 更新订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_PROCESSING);

            // 更新退款单
            refundInfoService.updateRefund(bodyAsString);
        } finally {
            response.close();
        }
    }

    /**
     * 查询退款使用
     * @param refundNo
     * @return
     */
    @Override
    public String queryRefund(String refundNo) throws IOException {
        log.info("查询退款...");
        String url = wxPayConfig.getDomain().concat(String.format(WxApiType.DOMESTIC_REFUNDS_QUERY.getType(), refundNo));
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");
        CloseableHttpResponse response = wxPayClient.execute(httpGet);
        // 解析响应
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("成功, 查询退款返回结果 = " + bodyAsString);
            } else if (statusCode == 204) {
                log.info("成功");
            } else {
                throw new RuntimeException("查询退款异常, 响应码 = " + statusCode+ ", 返回结果 = " + bodyAsString);
            }
            return bodyAsString;
        } finally {
            response.close();
        }
    }

    /**
     * 核实订单状态：调用微信支付查询退款接口
     * @param refundNo
     */
    @Override
    public void checkRefundStatus(String refundNo) throws IOException {
        // 1.查询退款订单
        String refund = this.queryRefund(refundNo);

        // 2.解析响应信息
        Gson gson = new Gson();
        Map<String, Object> refundMap = gson.fromJson(refund, HashMap.class);
        // 获取微信支付端退款状态
        String status = (String) refundMap.get("status");
        String orderNo = (String) refundMap.get("out_trade_no");
        if (WxRefundStatus.SUCCESS.getType().equals(status)) {
            // 已经成功退款
            log.info("核实订单已经成功退款，orderNo:{}, refundNo:{}", orderNo, refundNo);
            // 3.更新订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_SUCCESS);
            // 4.更新退款单
            refundInfoService.updateRefund(refund);
        }
        if (WxRefundStatus.ABNORMAL.getType().equals(status)) {
            // 退款异常
            log.warn("退款异常，orderNo:{}, refundNo:{}", orderNo, refundNo);
            // 3.更新订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_ABNORMAL);
            // 4.更新退款单
            refundInfoService.updateRefund(refund);
        }

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRefund(Map<String, Object> dataMap) throws Exception {
        // 1.日志记录、上可重入锁
        log.info("处理退款订单...");

        // 2.转换响应中的密文
        String plainText = decryptFromResource(dataMap);
        // 将明文转换成map
        Gson gson = new Gson();
        HashMap plainTextMap = gson.fromJson(plainText, HashMap.class);
        String orderNo = (String)plainTextMap.get("out_trade_no");

        // 3.根据退款情况处理订单
        if (lock.tryLock()) {
            try {
                String orderStatus = orderInfoService.getOrderStatus(orderNo);
                // 订单状态不是退款中，则直接返回 fixme 不是特别理解 不是退款中，那么就是退款成功或者退款异常，状态不变即可。
                if (!OrderStatus.REFUND_PROCESSING.getType().equals(orderStatus)) {
                    return;
                }
                // 3.更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_SUCCESS);
                // 4.更新退款单
                refundInfoService.updateRefund(plainText);
            } finally {
                // 5.要主动释放锁
                lock.unlock();
            }
        }
    }

    /**
     * 获取交易账单URL
     */
    @Override
    public String queryBill(String billDate, String type) throws IOException {
        // 1.日志记录
        log.info("请求微信获取交易账单下载地址...，日期是:{}", billDate);

        // 2.构造参数和请求
        String url = "";
        if("tradebill".equals(type)){
            url = WxApiType.TRADE_BILLS.getType();
        }else if("fundflowbill".equals(type)){
            url = WxApiType.FUND_FLOW_BILLS.getType();
        }else{
            throw new RuntimeException("不支持的账单类型");
        }

        // 3.处理响应获取需要的url
        url = wxPayConfig.getDomain().concat(url).concat("?bill_date=").concat(billDate);
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", "application/json");
        CloseableHttpResponse response = wxPayClient.execute(httpGet);
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("成功, 申请账单返回结果 = " + bodyAsString);
            } else if (statusCode == 204) {
                log.info("成功");
            } else {
                throw new RuntimeException("申请账单异常, 响应码 = " + statusCode+ ", 申请账单返回结果 = " + bodyAsString);
            }
            // 获取账单下载地址
            Gson gson = new Gson();
            Map<String, String> resultMap = gson.fromJson(bodyAsString, HashMap.class);
            return resultMap.get("download_url");
        } finally {
            response.close();
        }
    }

    @Override
    public String downloadBill(String billDate, String type) throws IOException {
        // 1.日志记录
        log.info("下载{}的账单，类型是{}", billDate, type);

        // 2.获取交易账单URL
        String downloadUrl = this.queryBill(billDate, type);

        // 3.下载账单
        HttpGet httpGet = new HttpGet(downloadUrl);
        httpGet.addHeader("Accept", "application/json");
        // todo 为什么这里不能对响应进行验签？并且验不过  因为人家没有进行签名
        CloseableHttpResponse response = wxPayNoSignClient.execute(httpGet);
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("成功, 下载账单返回结果 = " + bodyAsString);
            } else if (statusCode == 204) {
                log.info("成功");
            } else {
                throw new RuntimeException("下载账单异常, 响应码 = " + statusCode+ ", 下载账单返回结果 = " + bodyAsString);
            }
            return bodyAsString;
        } finally {
            response.close();
        }
    }

    /**
     * 关单接口调用
     * https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_3.shtml
     * 以下情况需要调用关单接口：
     *    1、商户订单支付失败需要生成新单号重新发起支付，要对原订单号调用关单，避免重复支付；
     *    2、系统下单后，用户支付超时，系统退出不再受理，避免用户继续，请调用关单接口。
     * @param orderNo
     */
    private void closeOrder(String orderNo) throws IOException {
        log.info("关单接口的调用，订单号：{}", orderNo);
        // 创建远程请求对象
        String url = String.format(WxApiType.CLOSE_ORDER_BY_NO.getType(), orderNo);
        url = wxPayConfig.getDomain().concat(url);
        HttpPost httpPost = new HttpPost(url);

        // 组装json请求体
        Gson gson = new Gson();
        Map<String, String> paramsMap = new HashMap<>();
        // todo 目前文档是有 服务商务号、子商户号，如果是 JSAPI则对得上
        paramsMap.put("mchid", wxPayConfig.getMchId());
        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数：{}", jsonParams);

        // 将请求参数设置到请求对象中
        StringEntity entity = new StringEntity(jsonParams,"utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        // 完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpPost);

        try {
            int statusCode = response.getStatusLine().getStatusCode();
            // 响应状态码
            if (statusCode == 200) {
                // 处理成功
                log.info("成功200");
            } else if (statusCode == 204) {
                // 处理成功，无返回Body
                log.info("成功204");
            } else {
                log.info("Native下单失败,响应码 = " + statusCode);
                throw new IOException("request failed");
            }
        } finally {
            response.close();
        }
    }

    /**
     * 对称解密
     * 为了保证安全性，微信支付在回调通知和平台证书下载接口中，对关键信息进行了AES-256-GCM加密。
     * 证书和回调报文使用的加密密钥为APIv3密钥，32字节 https://wechatpay-api.gitbook.io/wechatpay-api-v3/ren-zheng/api-v3-mi-yao
     */
    private String decryptFromResource(Map<String, Object> bodyMap) throws GeneralSecurityException {
        log.info("密文解密");
        // 获取通知数据中的resource，这部分有加密数据
        Map<String, String> resourceMap = (Map) bodyMap.get("resource");
        // 数据密文
        String ciphertext = resourceMap.get("ciphertext");
        // 随机串
        String nonce = resourceMap.get("nonce");
        // 附加数据
        String associatedData = resourceMap.get("associated_data");

        log.info("密文数据：{}", ciphertext);

        // 用APIv3密钥去解密
        AesUtil aesUtil = new AesUtil(wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));

        // 使用封装好的工具类去解密
        String plainText = aesUtil.decryptToString(
                associatedData.getBytes(StandardCharsets.UTF_8),
                nonce.getBytes(StandardCharsets.UTF_8),
                ciphertext);

        log.info("明文：{}", plainText);
        return plainText;
    }
}
