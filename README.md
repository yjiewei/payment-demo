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
    
    每个人的认证信息不一致，需要自行注册账号，更改认证信息
   
    认证信息：C:\Users\yangjiewei/.ngrok2/ngrok.yml
    
    官网地址：https://ngrok.com/
    
    启动ngrok，然后输入命令：ngrok http 8090，就可以获取地址了。
    
    Web Interface     http://127.0.0.1:4040
    
    Forwarding        http://7af6-59-42-111-64.ngrok.io -> http://localhost:8090   
                                 
    Forwarding        https://7af6-59-42-111-64.ngrok.io -> http://localhost:8090
  
    成功访问测试地址：https://7af6-59-42-111-64.ngrok.io/api/product/test
    
    注意每次启动访问地址都不同，需要改动配置。
    
3. try catch快捷键 ctrl+win+alt+t

4. MySQL时区问题

    由于MySQL的默认时区没有加8小时，导致我的订单每次都超时关闭了，这里在application.yml指定mysql连接是没有作用的，需要修改数据库变量才行。
    
    参考这篇文章：https://blog.csdn.net/cm15835106905/article/details/124696976

5. 下载交易订单时显示账单日期格式不正确，是因为你不能下载当天的账单，只能第二天才去下载。

6. 下载交易订单显示 Caused by: org.apache.http.HttpException: 应答的微信支付签名验证失败
    
    com/yangjiewei/paymentdemo/config/WxPayConfig.java:111
    
    https://github.com/wechatpay-apiv3/wechatpay-apache-httpclient
    
    https://developers.weixin.qq.com/community/develop/article/doc/0008ccfe4680f0fe2d2e63f0756813
    
    下载账单文件可能过大，为了平衡性能和签名验签成本，这里已经分为两步进行了，第一步获取账单连接，这里已经验证签名，第二步微信不设置响应签名，这里我们就不需要再次验签了，官网有说明。
    
    ```text
    因为下载的账单文件可能会很大，为了平衡系统性能和签名验签的实现成本，账单下载API被分成了两个步骤：
    
    /v3/bill/tradebill 获取账单下载链接和账单摘要
    /v3/billdownload/file 账单文件下载，请求需签名但应答不签名
    因为第二步不包含应答签名，我们可以参考上一个问题下载平台证书的方法，使用withValidator(response -> true)“跳过”应答的签名校验。
    
    注意：开发者在下载文件之后，应使用第一步获取的账单摘要校验文件的完整性。
    ```

## 二、支付宝支付


# 延伸阅读
## 三、其他

### 1. 代码同步问题

   由于GitHub访问速度慢，代码提交到Gitee，但是我想同步提交到GitHub，可以参考这两篇文章进行配置。

   配置拉取的项目下的隐藏文件夹.git里的config文件，我这里的配置是这样的

   当你配置了两个url就会把之前另外一个分支不存在的提交同步过去。
   ```text
   [core]
       repositoryformatversion = 0
       filemode = false
       bare = false
       logallrefupdates = true
       symlinks = false
       ignorecase = true
   [remote "origin"]
       url = https://gitee.com/jaysony/payment-demo.git
       url = https://github.com/yjiewei/payment-demo.git
       fetch = +refs/heads/*:refs/remotes/origin/*
   [branch "master"]
       remote = origin
       merge = refs/heads/master
   ```

   https://blog.csdn.net/yuan_jlj/article/details/125599716

   https://blog.csdn.net/weixin_44893902/article/details/125147574

