# 工程简介
## 一、微信支付
> https://wechatpay-api.gitbook.io/wechatpay-api-v3/
>
> https://github.com/wechatpay-apiv3/wechatpay-apache-httpclient
### 1. APIv3
1. 引入支付参数
2. 加载商户私钥 商户用私钥签名，微信用商户公钥去验签
3. 获取平台证书和验签器  平台会给我们发送数据
4. 获取HttpClient对象  使用HTTP请求去做连接
5. API字典和接口规则 
6. 内网穿透 开发服务器需要有外网能够访问的外网地址，开发机通常是局域网里的，没有独立IP，所以需要内网穿透，映射到外网
7. API v3 

### 2. 问题

1. 微信AES解密报错 Illegal key size，这里我该用java11版本解决。

    简单来说，微信用256位加密，但是我们jdk8默认是128位，所以报错了。

    具体说明看：https://www.cnblogs.com/operationhome/p/11886340.html


2. 内网穿透
    
    微信下单成功响应时会把相关支付结果和用户信息发送回商户，商户需要接收处理并返回应答。
   
    https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_4_5.shtml


## 二、支付宝支付


# 延伸阅读
## 三、其他

### 1. 代码同步问题

   由于GitHub访问速度慢，代码提交到Gitee，但是我想同步提交到GitHub，可以参考这两篇文章进行配置。

   https://blog.csdn.net/yuan_jlj/article/details/125599716

   https://blog.csdn.net/weixin_44893902/article/details/125147574

