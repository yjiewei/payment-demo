/*
 * @author yangjiewei
 * @date 2022/8/17 21:46
 */
package com.yangjiewei.paymentdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2 // 表示这是swagger配置
public class Swagger2Config {

    /**
     * 添加docket对象即可访问页面
     * http://localhost:8090/swagger-ui.html
     */
    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2);
    }
}
