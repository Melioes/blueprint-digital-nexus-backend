package com.melioes.blueprintdigitalnexus.common.exception;

import com.melioes.blueprintdigitalnexus.common.result.Result;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理 SQL异常 / 运行时异常 / 业务异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getMessage());
    }

    /**
     * 参数校验异常（@NotBlank、@NotNull等）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(message);
    }

    /**
     * SQL语法错误
     */
    @ExceptionHandler(BadSqlGrammarException.class)
    public Result<?> handleSqlGrammarException(BadSqlGrammarException e) {
        e.printStackTrace();
        return Result.error("SQL语法错误，请检查字段或表结构");
    }

    /**
     * 数据完整性异常（主键冲突 / 外键约束）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        e.printStackTrace();
        return Result.error("数据冲突或约束错误（如重复数据或外键限制）");
    }

    /**
     * 兜底异常（所有未捕获异常）
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.error("系统异常，请联系管理员");
    }
}