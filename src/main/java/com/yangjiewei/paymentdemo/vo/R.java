/*
 * @author yangjiewei
 * @date 2022/8/17 22:02
 */
package com.yangjiewei.paymentdemo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true) // TODO 这个是干嘛用的 链式调用吗？
public class R {

    /**
     * 响应码 响应消息 数据
     */
    private Integer code;
    private String message;
    private Map<String, Object> data = new HashMap<>();

    public static R ok(){
        R r = new R();
        r.setCode(0);
        r.setMessage("成功");
        return r;
    }

    public static R error(){
        R r = new R();
        r.setCode(-1);
        r.setMessage("失败");
        return r;
    }

    public R data(String key, Object value){
        this.data.put(key, value);
        return this;
    }
}
