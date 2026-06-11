package com.melioes.blueprintdigitalnexus.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.melioes.blueprintdigitalnexus.common.context.UserContext;
import com.melioes.blueprintdigitalnexus.common.properties.JwtProperties;
import com.melioes.blueprintdigitalnexus.common.result.Result;
import com.melioes.blueprintdigitalnexus.common.utils.JwtUtil;
import com.melioes.blueprintdigitalnexus.service.PermissionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AdminJwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    @Lazy
    private PermissionService permissionService;

    // 复用ObjectMapper实例，避免每次创建
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
            writeJsonResponse(response, 401, "未登录，请先登录");
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
            List<String> roles = getRolesFromClaims(claims);
            UserContext.set(userId);
            UserContext.setRoles(roles);

            // 加权限
            List<String> permissions = permissionService.getUserPermissions(userId);
            UserContext.setPermissions(permissions);
            log.info("[Admin] 认证成功 userId={}, roles={}", userId, roles);

            // 4. 这里只做"登录校验"，不做权限判断

            return true;

        } catch (ExpiredJwtException e) {
            // ✅ 专门处理token过期异常，只打印一行警告日志，不打印堆栈
            log.warn("[Admin] token已过期: {}", e.getMessage());
            writeJsonResponse(response, 401, "登录已过期，请重新登录");
            return false;
        } catch (JwtException e) {
            // ✅ 处理其他所有JWT异常（签名错误、格式错误、篡改等）
            log.warn("[Admin] token无效: {}", e.getMessage());
            writeJsonResponse(response, 401, "无效的登录凭证，请重新登录");
            return false;
        } catch (Exception e) {
            // ✅ 处理其他未知异常，打印完整堆栈方便排查
            log.error("[Admin] 认证过程发生未知错误", e);
            writeJsonResponse(response, 500, "系统认证失败，请联系管理员");
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

    // ✅ 统一返回JSON格式响应，使用你标准的Result格式
//    private void writeJsonResponse(HttpServletResponse response, Integer code, String message) throws IOException {
//        response.setContentType("application/json;charset=UTF-8");
//        response.setStatus(HttpServletResponse.SC_OK); // HTTP状态码统一返回200，业务状态码用Result里的code
//
//        // 使用你统一的Result格式
//        Result<Void> result = Result.error(code, message);
//        String json = OBJECT_MAPPER.writeValueAsString(result);
//
//        response.getWriter().write(json);
//        response.getWriter().flush();
//    }

    private void writeJsonResponse(HttpServletResponse response, Integer code, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        int httpStatus = switch (code) {
            case 400 -> HttpServletResponse.SC_BAD_REQUEST;
            case 401 -> HttpServletResponse.SC_UNAUTHORIZED;
            case 403 -> HttpServletResponse.SC_FORBIDDEN;
            case 500 -> HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            default  -> HttpServletResponse.SC_OK;
        };

        response.setStatus(httpStatus);
        Result<Void> result = Result.error(code, message);
        String json = OBJECT_MAPPER.writeValueAsString(result);

        response.getWriter().write(json);
        response.getWriter().flush();
    }


    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        UserContext.remove();
    }
}