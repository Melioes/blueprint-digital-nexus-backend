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

@Component
@Slf4j
public class UserJwtInterceptor implements HandlerInterceptor {

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

        if (token == null || token.isEmpty()) {
            writeError(response, "未登录");
            return false;
        }

        try {
            Claims claims = JwtUtil.parseToken(jwtProperties.getSecretKey(), token);

            Long userId = Long.valueOf(claims.get("userId").toString());

            UserContext.set(userId);
//            UserContext.setRoles(roles);
            log.info("[User] userId={}", userId);

            return true;

        } catch (Exception e) {
            log.error("[User] token无效", e);
            writeError(response, "token无效或已过期");
            return false;
        }
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