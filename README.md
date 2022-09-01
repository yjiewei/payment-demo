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
    
    每个人的认证信息不一致，需要自行注册账号。
   
    认证信息：C:\Users\yangjiewei/.ngrok2/ngrok.yml
    
    官网地址：https://ngrok.com/
    
    Web Interface     http://127.0.0.1:4040
    
    Forwarding        http://7af6-59-42-111-64.ngrok.io -> http://localhost:8090   
                                 
    Forwarding        https://7af6-59-42-111-64.ngrok.io -> http://localhost:8090
  
    成功访问测试地址：https://7af6-59-42-111-64.ngrok.io/api/product/test
    
    注意每次启动访问地址都不同，需要改动配置。
    
3. try catch快捷键 ctrl+win+alt+t

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

