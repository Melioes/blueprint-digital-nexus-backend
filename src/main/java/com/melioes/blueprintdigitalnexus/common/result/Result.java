package com.melioes.blueprintdigitalnexus.common.result;

import lombok.Data;

/**
 * 后端统一返回结果
 */
@Data
public class Result<T> {

    private Integer code; // 1成功，0失败
    private String msg;   // 提示信息
    private T data;       // 数据

    /**
     * 成功（无数据）
     */
    public static Result<Void> success() {
        Result<Void> result = new Result<>();
        result.code = 1;
        result.msg = "操作成功";
        return result;
    }

    /**
     * 成功（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 1;
        result.msg = "操作成功";
        result.data = data;
        return result;
    }

    /**
     * 成功（自定义提示）
     */
    public static <T> Result<T> success(T data, String msg) {
        Result<T> result = new Result<>();
        result.code = 1;
        result.msg = msg;
        result.data = data;
        return result;
    }

    /**
     * 失败
     */
    public static Result<Void> error(String msg) {
        Result<Void> result = new Result<>();
        result.code = 0;
        result.msg = msg;
        return result;
    }
}