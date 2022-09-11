/*
 * @author yangjiewei
 * @date 2022/9/11 9:34
 */
package com.yangjiewei.paymentdemo.config;

import com.alipay.api.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

/**
 *  // 通过spring的环境配置去获取对应的参数
 *  @Resource
 *  private Environment config;
 */
@Configuration
@PropertySource("classpath:alipay-sandbox.properties")
public class AlipayClientConfig {

    @Resource
    private Environment env;

    @Bean
    public AlipayClient alipayClient() throws AlipayApiException {

        AlipayConfig alipayConfig = new AlipayConfig();
        //设置网关地址
        alipayConfig.setServerUrl(env.getProperty("alipay.gateway-url"));
        //设置应用ID
        alipayConfig.setAppId(env.getProperty("alipay.app-id"));
        //设置应用私钥
        alipayConfig.setPrivateKey(env.getProperty("alipay.merchant-private-key"));
        //设置请求格式，固定值json
        alipayConfig.setFormat(AlipayConstants.FORMAT_JSON);
        //设置字符集
        alipayConfig.setCharset(AlipayConstants.CHARSET_UTF8);
        //设置支付宝公钥
        alipayConfig.setAlipayPublicKey(env.getProperty("alipay.alipay-public-key"));
        //设置签名类型
        alipayConfig.setSignType(AlipayConstants.SIGN_TYPE_RSA2);
        //构造client
        return new DefaultAlipayClient(alipayConfig);
    }

}
