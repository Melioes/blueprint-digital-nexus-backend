package com.melioes.blueprintdigitalnexus.common.exception;

import com.melioes.blueprintdigitalnexus.common.result.Result;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理 SQL异常 / 运行时异常 / 业务异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    //  专门处理JWT token过期异常
    /**
     * JWT token已过期
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public Result<?> handleExpiredJwtException(ExpiredJwtException e) {
        // 只打印一行简洁的警告日志，不打印完整堆栈
        // 生产环境可以把日志级别改成debug
        return Result.error(401, "登录已过期，请重新登录");
    }
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
    // 处理唯一索引冲突
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> handleDuplicateKeyException(DuplicateKeyException e){
        String msg = e.getMessage().toLowerCase();

        // 只有 用户手动传ID 新增时才会触发
        if (msg.contains("primary")) {
            return Result.error("主键ID已存在，新增请勿手动指定ID");
        }

        return Result.error("数据重复，请检查后重试");
    }

    // 处理 JSON 格式错误
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<?>> handleJsonParseException(HttpMessageNotReadableException e) {
        return new ResponseEntity<>(Result.error("请求格式不正确，请检查请求体是否符合规范"), HttpStatus.BAD_REQUEST);
    }


    /**
     * 请求方法不正确（如POST请求访问了GET接口）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return new ResponseEntity<>(Result.error("不支持的请求方法，请检查请求方式。"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 缺少请求参数（如没有传递必须的字段）
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<?>> handleMissingParams(MissingServletRequestParameterException e) {
        return new ResponseEntity<>(Result.error("缺少必要的请求参数: " + e.getParameterName()), HttpStatus.BAD_REQUEST);
    }


    /**
     * 路径参数缺失（@PathVariable 为空/不传）
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public Result<?> handleMissingPathVariable(MissingPathVariableException e) {
        return Result.error("请求地址非法，缺少必要的路径参数：" + e.getVariableName());
    }
    /**
     * 参数类型不匹配（如传入参数类型错误）
     */
    @ExceptionHandler(TypeMismatchException.class)
    public Result<?> handleTypeMismatch(TypeMismatchException e) {
        return Result.error("参数类型错误，请输入正确的数据格式" + e.getMessage());
    }


    /**
     * 兜底异常（所有未捕获异常）
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.error("系统开小差啦，请联系管理员");
    }
}