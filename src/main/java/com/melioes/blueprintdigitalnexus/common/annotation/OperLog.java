package com.melioes.blueprintdigitalnexus.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标记在 Controller 方法上，AOP 自动记录操作日志
 */
@Target(ElementType.METHOD)   // 只能加在方法上
@Retention(RetentionPolicy.RUNTIME)  // 运行时保留，AOP才能读到
@Documented
public @interface OperLog {
    /**
     * 所属模块（如"出库管理"、"仓库管理"） | 注解的属性，使用时写 `@OperLog(module = "xxx")`
     *
     */
    String module() default "";

    /**
     * 操作内容（如"创建出库单"、"删除仓库"）
     */
    String operation() default "";
}