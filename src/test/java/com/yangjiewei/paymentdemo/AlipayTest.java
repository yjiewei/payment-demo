/*
 * @author yangjiewei
 * @date 2022/9/11 9:35
 */
package com.yangjiewei.paymentdemo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

@Slf4j
@SpringBootTest
public class AlipayTest {

    @Resource
    private Environment config;

    /**
     * appid = 2021003147669234
     */
    @Test
    void testGetAlipayConfig(){
        log.info("appid = " + config.getProperty("alipay.app-id"));
    }

}
