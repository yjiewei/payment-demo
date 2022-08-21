/*
 * @author yangjiewei
 * @date 2022/8/21 9:43
 */
package com.yangjiewei.paymentdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

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

}
