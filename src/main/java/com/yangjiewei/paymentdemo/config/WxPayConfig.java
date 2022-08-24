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
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

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

    /**
     * 获取商户私钥
     * https://github.com/wechatpay-apiv3/wechatpay-apache-httpclient
     * @param filename 私钥文件路径
     * @return
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
    @Bean
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

}
