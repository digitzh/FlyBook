package com.bytedance.handler;

import com.bytedance.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获所有未知的异常 (Exception)
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.fail(e.getMessage());
    }

    /**
     * 捕获业务逻辑异常 (RuntimeException)
     * 如果你有自定义的 BusinessException，也可以在这里单独捕获
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }
}

