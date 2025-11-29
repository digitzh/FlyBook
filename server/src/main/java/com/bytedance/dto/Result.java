package com.bytedance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Integer success;
    private String msg;
    private Object data;

    public static Result ok(){
        return new Result(0, null, null);
    }
    public static Result ok(Object data){
        return new Result(0, null, data);
    }
    public static Result ok(List<?> data, Long total){
        return new Result(0, null, data);
    }
    public static Result fail(String errorMsg){
        return new Result(1, errorMsg, null);
    }
}
