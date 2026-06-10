package com.melioes.blueprintdigitalnexus.common.aspect;

import com.melioes.blueprintdigitalnexus.common.annotation.OperLog;
import com.melioes.blueprintdigitalnexus.common.context.UserContext;
import com.melioes.blueprintdigitalnexus.entity.SysOperLog;
import com.melioes.blueprintdigitalnexus.entity.SysUser;
import com.melioes.blueprintdigitalnexus.service.OperLogService;
import com.melioes.blueprintdigitalnexus.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 操作日志切面
 * 拦截标记了 @OperLog 的方法，自动记录操作日志
 */
@Slf4j
@Aspect
@Component
public class OperLogAspect {

    @Autowired
    private OperLogService operLogService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 切点：拦截所有标记了 @OperLog 的方法
     */
    @Pointcut("@annotation(com.melioes.blueprintdigitalnexus.common.annotation.OperLog)")
    public void operLogPointcut() {
    }

    /**
     * 环绕通知：在方法执行前后记录日志
     */
    @Around("operLogPointcut()")
    public Object recordLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 2. 获取注解上的模块和操作信息
        OperLog operLogAnnotation = method.getAnnotation(OperLog.class);
        String module = operLogAnnotation.module();
        String operation = operLogAnnotation.operation();

        // 3. 获取当前用户信息
        String operName = "未知用户";
        String roleName = "未知角色";

        Long userId = UserContext.get();
        if (userId != null) {
            SysUser user = sysUserService.getById(userId);
            if (user != null) {
                operName = user.getUsername();
            }
            List<String> roles = UserContext.getRoles();
            if (roles != null && !roles.isEmpty()) {
                roleName = String.join(",", roles);
            }
        }

        // 4. 执行原方法
        Object result = joinPoint.proceed();

        // 5. 方法执行成功，记录日志
        try {
            SysOperLog logEntity = new SysOperLog();
            logEntity.setOperName(operName);
            logEntity.setRoleName(roleName);
            logEntity.setOperModule(module);
            logEntity.setOperContent(operation);
            // operTime 是由数据库CURRENT_TIMESTAMP在表定义中设置的默认值。有数据库维护
            operLogService.save(logEntity);

            log.info("[操作日志] {} - {} - {}", module, operation, operName);
        } catch (Exception e) {
            log.error("[操作日志] 记录失败", e);
        }

        return result;
    }
}
