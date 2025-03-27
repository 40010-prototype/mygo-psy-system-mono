package com.mygo.handler;

import com.mygo.exception.BaseException;
import com.mygo.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 自定义异常处理器
     */
    @ExceptionHandler(BaseException.class)
    public Object handleBadRequestException(BaseException e) {
        log.error("自定义异常 -> {} , 异常原因：{}  ", e.getClass()
                .getName(), e.getMessage());
        log.debug("", e);
        return processResponse(e);
    }

    private ResponseEntity<Result<Void>> processResponse(BaseException e) {
        return ResponseEntity.status(e.getCode())
                .body(Result.error(e.getMessage()));
    }
}
