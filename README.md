# 工程简介
## 微信支付
> https://wechatpay-api.gitbook.io/wechatpay-api-v3/
>
> https://github.com/wechatpay-apiv3/wechatpay-apache-httpclient
### APIv3
1. 引入支付参数
2. 加载商户私钥 商户用私钥签名，微信用商户公钥去验签
3. 获取平台证书和验签器  平台会给我们发送数据
4. 获取HttpClient对象  使用HTTP请求去做连接
5. API字典和接口规则 
6. 内网穿透 开发服务器需要有外网能够访问的外网地址，开发机通常是局域网里的，没有独立IP，所以需要内网穿透，映射到外网
7. API v3 

### 问题

微信AES解密报错 Illegal key size，这里我该用java11版本解决。

简单来说，微信用256位加密，但是我们jdk8默认是128位，所以报错了。

具体说明看：https://www.cnblogs.com/operationhome/p/11886340.html

## 支付宝支付


# 延伸阅读

