/*
 * @author yangjiewei
 * @date 2022/8/21 9:43
 */
package com.yangjiewei.paymentdemo.config;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.ScheduledUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

@Slf4j
@Data // 使用set方法将wxpay节点中的值填充到当前类属性中
@Configuration
@PropertySource("classpath:wxpay.properties") // 读取配置文件
@ConfigurationProperties(prefix = "wxpay") // 读取wxpay节点
public class WxPayConfig {

    private String mchId;

    private String mchSerialNo;

    private String privateKeyPath;

    private String apiV3Key;

    private String appid;

    private String domain;

    private String notifyDomain;

    private String partnerKey;

    /**
     * 获取商户私钥
     * https://github.com/wechatpay-apiv3/wechatpay-apache-httpclient
     * @param filename 私钥文件路径
     * @return
     * SunRsaSign RSA private CRT key, 2048 bits
     *   params: null
     *   modulus: 29196557853371344615989409675856997261494628428355861963950906746529601942277298308632933777539328914504578036539453328803924534856360056526836846208106272284586763165135543674040624325386177794678913931993440095320725327930374188105291484337661926923964738639471498696135487713620903478323379675887571783008968308560543525374453713737215020011994691156294256411677756266727725889445999455904567407650336815977981806781608764967993226396089287868390927942256035766495372056122792641768934311707380459388854196421329304568250723518886141916420729411973764376469218726150169079325093924991584173701865647752277180774557
     *   private exponent: 20162763200722243709279715161200723851534024872041105888680502440471539043678048188815752168052435835353505612947481853126277072845324012364742188725951474393519879062069816609600754022043319358673313786839359526284704939451009435577725365665780263830381618110133826075169690990292025428772111032714277603905998594149013749533334958458079427791088015949208443987127330528857645818049469317133508749713858925696429908213017919090508752232862643059396502847862345155397220935776007996999697563983131754312126730180822916032280507578176235080880200536666606520100029546133905032859326623566559080892337940377264467464545
     */
    private PrivateKey getPrivateKey(String filename) {
        try {
            return PemUtil.loadPrivateKey(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("私钥文件不存在", e);
        }
    }

    /**
     * 获取签名验证器
     * https://github.com/wechatpay-apiv3/wechatpay-apache-httpclient 有定时更新平台证书功能
     * 平台证书：平台证书封装了微信的公钥，商户可以使用平台证书中的公钥进行验签。
     * 签名验证器：帮助我们进行验签工作，我们单独将它定义出来，方便后面的开发
     */
    @Bean
    public ScheduledUpdateCertificatesVerifier getVerifier(){
        // 获取商户私钥
        PrivateKey privateKey = getPrivateKey(privateKeyPath);
        // 私钥签名对象（签名）
        PrivateKeySigner privateKeySigner = new PrivateKeySigner(mchSerialNo, privateKey);
        // 身份认证对象（验签）
        WechatPay2Credentials wechatPay2Credentials = new WechatPay2Credentials(mchId, privateKeySigner);
        // 使用定时更新的签名验证器，不需要传入证书
        ScheduledUpdateCertificatesVerifier verifier = new ScheduledUpdateCertificatesVerifier(
                wechatPay2Credentials,
                apiV3Key.getBytes(StandardCharsets.UTF_8));
        return verifier;
    }

    /**
     * 获取httpClient对象
     * https://github.com/wechatpay-apiv3/wechatpay-apache-httpclient （定时更新平台证书功能）
     * HttpClient 对象：是建立远程连接的基础，我们通过SDK创建这个对象
     */
    @Bean(name = "wxPayClient")
    public CloseableHttpClient getWxPayClient(ScheduledUpdateCertificatesVerifier verifier){
        //获取商户私钥
        PrivateKey privateKey = getPrivateKey(privateKeyPath);
        //用于构造HttpClient
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                .withMerchant(mchId, mchSerialNo, privateKey)
                .withValidator(new WechatPay2Validator(verifier));
        // ... 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
        // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签，并进行证书自动更新
        CloseableHttpClient httpClient = builder.build();
        return httpClient;
    }

    /**
     * 获取无需验证响应签名的httpClient对象
     * @param verifier
     * @return
     */
    @Bean(name = "wxPayNoSignClient")
    public CloseableHttpClient getWxPayNoSignClient(ScheduledUpdateCertificatesVerifier verifier){
        log.info("初始化wxPayNoSignClient");
        //获取商户私钥
        PrivateKey privateKey = getPrivateKey(privateKeyPath);
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                .withMerchant(mchId, mchSerialNo, privateKey)
                //设置响应对象无需签名
                .withValidator((response) -> true);
        CloseableHttpClient wxPayNoSignClient = builder.build();
        log.info("wxPayNoSignClient初始化完成");
        return wxPayNoSignClient;
    }

}

/**
 * 微信SDK提供的获取httpClient方式会帮我们封装签名验签的方法等
 * https://pay.weixin.qq.com/wiki/doc/apiv3/wechatpay/wechatpay4_0.shtml
 * authorization token=[mchid="1558950191",nonce_str="ith8McWBWB2uffaa3cBPgho7CcCNKoyo",timestamp="1661395845",serial_no="34345964330B66427E0D3D28826C4993C77E631F",signature="tAOO8YN5oOX39NjsQhs0F+TPoTLvl30dgzh/bJ9AVkWUquGOw8+SB+xFtriZtGv1BeyJZzihjwhkaKQsVtuxhxFgCNFLApnhtbECDg4w3KzZCFtqsc3XCWgUHFAnEpiNtfM1mJ9aCEWQjKtdyEGYFdfiJFfQ2vaqCQxBFvQ0azntoizf1yaHOposuE7NWRCsXdwl1lmyvf/wE4PJywF5KgLnkp9XJkW/A5Z6PJxAzmXYe4j+pgk85CDHgxs0mwHR8Lz4FnEpMpuUqs5GmwRjCo9+DS6RVqyYWNbpB/yaYqA1daYVYezCxZTdYbLahRJfzCAC41HcOzPuYMvORn6sUA=="]
 * 生成签名：com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials#getToken(org.apache.http.client.methods.HttpRequestWrapper)
 *   1.构造签名串
 *     POST
 *     /v3/pay/transactions/native
 *     1661395845
 *     ith8McWBWB2uffaa3cBPgho7CcCNKoyo
 *     {"amount":{"total":1,"currency":"CNY"},"mchid":"1558950191","out_trade_no":"ORDER_20220825104830065","appid":"wx74862e0dfcf69954","description":"test","notify_url":"https://500c-219-143-130-12.ngrok.io/api/wx-pay/native/notify"}
 *
 *   2.计算签名值
 *     signature="tAOO8YN5oOX39NjsQhs0F+TPoTLvl30dgzh/bJ9AVkWUquGOw8+SB+xFtriZtGv1BeyJZzihjwhkaKQsVtuxhxFgCNFLApnhtbECDg4w3KzZCFtqsc3XCWgUHFAnEpiNtfM1mJ9aCEWQjKtdyEGYFdfiJFfQ2vaqCQxBFvQ0azntoizf1yaHOposuE7NWRCsXdwl1lmyvf/wE4PJywF5KgLnkp9XJkW/A5Z6PJxAzmXYe4j+pgk85CDHgxs0mwHR8Lz4FnEpMpuUqs5GmwRjCo9+DS6RVqyYWNbpB/yaYqA1daYVYezCxZTdYbLahRJfzCAC41HcOzPuYMvORn6sUA=="
 *
 *   3.设置HTTP头
 *     Authorization: WECHATPAY2-SHA256-RSA2048 mchid="1558950191",nonce_str="ith8McWBWB2uffaa3cBPgho7CcCNKoyo",timestamp="1661395845",serial_no="34345964330B66427E0D3D28826C4993C77E631F",signature="tAOO8YN5oOX39NjsQhs0F+TPoTLvl30dgzh/bJ9AVkWUquGOw8+SB+xFtriZtGv1BeyJZzihjwhkaKQsVtuxhxFgCNFLApnhtbECDg4w3KzZCFtqsc3XCWgUHFAnEpiNtfM1mJ9aCEWQjKtdyEGYFdfiJFfQ2vaqCQxBFvQ0azntoizf1yaHOposuE7NWRCsXdwl1lmyvf/wE4PJywF5KgLnkp9XJkW/A5Z6PJxAzmXYe4j+pgk85CDHgxs0mwHR8Lz4FnEpMpuUqs5GmwRjCo9+DS6RVqyYWNbpB/yaYqA1daYVYezCxZTdYbLahRJfzCAC41HcOzPuYMvORn6sUA=="
 *
 *
 * 签名验证：com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator#validate(org.apache.http.client.methods.CloseableHttpResponse)
 * https://pay.weixin.qq.com/wiki/doc/apiv3/wechatpay/wechatpay4_1.shtml
 *   1.获取平台证书
 *   2.检查平台证书序列号
 *   3.构造验签名串
 *   4.获取应答签名
 *   5.验证签名
 *
 */
