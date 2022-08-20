/*
 * @author yangjiewei
 * @date 2022/8/20 23:07
 */
package com.yangjiewei.paymentdemo.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan("com.yangjiewei.paymentdemo.mapper")
public class MyBatisPlusConfig {

}
