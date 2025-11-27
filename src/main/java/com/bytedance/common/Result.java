package com.bytedance.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code; // 0: 成功, 1: 失败
    private String msg;   // 错误信息
    private T data;       // 数据载荷

    public static <T> Result<T> success(T data) {
        return new Result<>(0, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(0, "success", null);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(1, msg, null);
    }
}
