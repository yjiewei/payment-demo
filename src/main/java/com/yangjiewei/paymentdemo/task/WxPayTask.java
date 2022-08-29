package com.yangjiewei.paymentdemo.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * cron表达式生成器
 *   https://cron.qqe2.com/
 *   https://www.pppet.net/
 * @author yangjiewei
 * @date 2022/8/29
 */
@Slf4j
@Component
public class WxPayTask {

//    /**
//     * 测试
//     * (cron="秒 分 时 日 月 周")
//     * *：每隔一秒执行
//     * 0/3：从第0秒开始，每隔3秒执行一次
//     * 1-3: 从第1秒开始执行，到第3秒结束执行
//     * 1,2,3：第1、2、3秒执行
//     * ?：不指定，若指定日期，则不指定周，反之同理
//     */
//    @Scheduled(cron="0/3 * * * * ?")
//    public void testTask() {
//        log.info("测试定时任务执行");
//    }


}
