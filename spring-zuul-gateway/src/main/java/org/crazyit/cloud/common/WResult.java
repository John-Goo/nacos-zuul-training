package org.crazyit.cloud.common;

import lombok.Data;

@Data
public class WResult<T> implements Cloneable{


    public static final int _Err_COMMON_CODE = 444;
    // 错误代码，默认0-未出错
    private int code =  0;
    // 错误消息，默认success-成功
    private String msg = "操作成功";
    // 目录对象
    private T data;



    public void ok(T data){
        this.data = data;
    }

    public void ok(T data,String msg){
        this.data = data;
        this.msg = msg;
    }



    public void failer(String msg){
        this.code = _Err_COMMON_CODE;
        this.msg = msg;
    }

    public void failer(int code,String msg){
        this.code = code;
        this.msg = msg;
    }


    public static WResult newInstance()  {
        WResult wResult = null;
        try {
            wResult = (WResult) prototype.clone();
        } catch (CloneNotSupportedException e) {
            wResult = new WResult();
        }
        return wResult;
    }

    protected final static WResult prototype = new WResult();
}