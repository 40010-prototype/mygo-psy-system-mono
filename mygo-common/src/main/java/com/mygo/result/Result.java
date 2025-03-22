package com.mygo.result;

import com.mygo.constant.ResultConstant;
import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private int code; //编码：1成功，0为失败

    private String msg; //错误信息

    private T data; //数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = ResultConstant.SUCCESS;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = ResultConstant.SUCCESS;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result result = new Result<>();
        result.msg = msg;
        result.code = ResultConstant.FAILURE;
        return result;
    }
}