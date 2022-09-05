package com.yangjiewei.paymentdemo.controller;

import com.yangjiewei.paymentdemo.service.WxPayService;
import com.yangjiewei.paymentdemo.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author yangjiewei
 * @date 2022/9/5
 */
@Slf4j
@CrossOrigin
@RestController
@Api(tags = "网站微信支付APIv2")
@RequestMapping("/api/wx-pay-v2")
public class WxPayV2Controller {

    @Resource
    private WxPayService wxPayService;

    /**
     * native下单 v2
     * @param productId
     * @param request
     * @return
     */
    @ApiOperation("调用统一下单API，生成支付二维码")
    @PostMapping("/native/{productId}")
    public R createNative(@PathVariable Long productId, HttpServletRequest request) throws Exception {
        log.info("发起V2支付请求");
        String remoteAddr = request.getRemoteAddr();
        Map<String, Object> map = wxPayService.nativePayV2(productId, remoteAddr);
        return R.ok().setData(map);
    }

}
