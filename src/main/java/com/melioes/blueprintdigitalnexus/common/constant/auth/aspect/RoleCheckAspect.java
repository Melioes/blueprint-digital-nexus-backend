package com.melioes.blueprintdigitalnexus.common.constant.auth.aspect;

import com.melioes.blueprintdigitalnexus.common.constant.auth.annotation.RequiresRole;
import com.melioes.blueprintdigitalnexus.common.context.UserContext;
import com.melioes.blueprintdigitalnexus.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
//JWT拦截器（解析 roles）
//        ↓
//        UserContext.setRoles(["USER","ADMIN"])
//        ↓
//        AOP拦截
//        ↓
//      字符串匹配 "ADMIN"
//        ↓
//通过 / 拒绝
@Slf4j
@Aspect
@Component
public class RoleCheckAspect {
    // ① 定义切点（统一管理）
    @Pointcut("@annotation(com.melioes.blueprintdigitalnexus.common.constant.auth.annotation.RequiresRole)")

    /**
     * 拦截所有标记了 @RequiresRole 的方法
     */
//    @Around("@annotation(com.melioes.blueprintdigitalnexus.common.constant.auth.annotation.RequiresRole)")
    public void rolePointcut() {}

    // ② 环绕通知
    @Around("rolePointcut()")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {

        // 1. 获取当前执行的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 2. 获取方法上的权限注解
        RequiresRole requiresRole = method.getAnnotation(RequiresRole.class);

        // 如果没有配置权限，直接放行
        if (requiresRole == null) {
            log.info("[权限] 方法未配置权限，直接放行 -> {}", method.getName());
            return joinPoint.proceed();
        }

        // 3. 获取当前用户角色（从 ThreadLocal 中）
        List<String> userRoles = UserContext.getRoles();
        log.info("[权限] 方法: {}", method.getName());
        log.info("[权限] 需要角色: {}", String.join(",", requiresRole.value()));
        log.info("[权限] 当前用户角色: {}", userRoles);

        // 如果用户没有角色信息，直接拒绝
        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("[权限] 用户角色为空，拒绝访问");
            throw new BusinessException("未获取到用户角色信息");
        }

        // 4. 校验是否匹配任意一个角色
        for (String needRole : requiresRole.value()) {
            log.info("[权限] 校验角色: {}", needRole);
            if (userRoles.contains(needRole)) {
                log.info("[权限] 权限通过 -> {}", needRole);
                return joinPoint.proceed();
            }
        }

        // 5. 无权限
        log.warn("[权限] 权限不足，拒绝访问 -> userRoles={}, need={}",
                userRoles, requiresRole.value());
        throw new BusinessException("无权限访问该接口");
    }
}