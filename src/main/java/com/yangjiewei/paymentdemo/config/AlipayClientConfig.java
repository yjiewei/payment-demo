/*
 * @author yangjiewei
 * @date 2022/9/11 9:34
 */
package com.yangjiewei.paymentdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 *  // 通过spring的环境配置去获取对应的参数
 *  @Resource
 *  private Environment config;
 */
@Configuration
@PropertySource("classpath:alipay-sandbox.properties")
public class AlipayClientConfig {

}
