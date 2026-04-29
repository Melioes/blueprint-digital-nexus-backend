package com.melioes.blueprintdigitalnexus.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.melioes.blueprintdigitalnexus.common.context.UserContext;
import com.melioes.blueprintdigitalnexus.common.properties.JwtProperties;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AdminJwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getTokenName());
        log.info("[Admin] 请求进入拦截器, token={}", token);

        if (token == null || token.isEmpty()) {
            writeError(response, "未登录");
            return false;
        }

        try {
            // =========================
            // 1. 解析 JWT
            // =========================
            Claims claims = JwtUtil.parseToken(jwtProperties.getSecretKey(), token);

            // =========================
            // 2. 获取用户信息
            // =========================
            Long userId = Long.valueOf(claims.get("userId").toString());

            // 一个用户可能有多个角色
            //List<String> roles = (List<String>) claims.get("roleKeys");
            List<String> roles = getRolesFromClaims(claims);
            UserContext.set(userId);
            UserContext.setRoles(roles);

            log.info("[Admin] 认证成功 userId={}, roles={}", userId, roles);

            // =========================
            // 4. 这里只做“登录校验”，不做权限判断
            // =========================
            return true;

        } catch (Exception e) {
            log.error("[Admin] token解析失败", e);
            writeError(response, "token无效或已过期");
            return false;
        }
    }
    /// 获取角色
    private List<String> getRolesFromClaims(Claims claims) {
        Object roleKeysObj = claims.get("roleKeys");

        if (roleKeysObj == null) {
            return Collections.emptyList();  // 返回空列表
        }

        if (!(roleKeysObj instanceof List<?>)) {
            log.warn("roleKeys 类型错误");
            return Collections.emptyList();
        }

        List<?> rawRoles = (List<?>) roleKeysObj;
        return rawRoles.stream()
                .filter(item -> item instanceof String)
                .map(item -> (String) item)
                .collect(Collectors.toList());
    }

    private void writeError(HttpServletResponse response, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                new ObjectMapper().writeValueAsString(Result.error(msg))
        );
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        UserContext.remove();
    }
}