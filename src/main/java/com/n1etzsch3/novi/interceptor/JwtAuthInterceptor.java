package com.n1etzsch3.novi.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.pojo.dto.Result;
import com.n1etzsch3.novi.utils.JwtUtils;
import com.n1etzsch3.novi.utils.LoginUserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;
    
    @Value("${novi.jwt.header-name}")
    private String headerName;

    @Autowired
    private ObjectMapper objectMapper; // 用于将 Result 对象转为 JSON 字符串

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 1. 从请求头获取 Token
        String token = request.getHeader(headerName);
        log.info("Request URI: {}, Token: {}", request.getRequestURI(), token);

        // 2. 检查 Token 是否为空或格式不正确
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            log.warn("Token is missing or has invalid format.");
            sendErrorResponse(response, "INVALID_TOKEN");
            return false; // 拦截
        }

        // 3. 截取 "Bearer " 前缀
        String jwt = token.substring(7);

        // 4. 解析 Token
        try {
            Claims claims = jwtUtils.parseToken(jwt);
            
            // 5. 检查 Token 是否过期
            if (jwtUtils.isTokenExpired(claims)) {
                log.warn("Token is expired.");
                sendErrorResponse(response, "TOKEN_EXPIRED");
                return false; // 拦截
            }

            // 6. 提取用户信息 (userId) 存入 ThreadLocal
            Long userId = claims.get("userId", Long.class);
            LoginUserContext.setUserId(userId);
            
            log.info("Token validated successfully for user: {}", userId);
            return true; // 放行

        } catch (Exception e) {
            log.error("Token parsing error: {}", e.getMessage());
            sendErrorResponse(response, "INVALID_TOKEN");
            return false; // 拦截
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 7. 请求完成后，清理 ThreadLocal，防止内存泄漏
        LoginUserContext.clear();
        log.info("Cleaned up ThreadLocal for user.");
    }

    /**
     * 辅助方法：发送统一的认证失败响应 (HTTP 401)
     * 注意：这里我们不使用 Result 类，而是遵循 RESTful 规范，返回 401 状态码
     * GlobalExceptionHandler 不会捕获拦截器中的异常
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json;charset=UTF-8");
        
        // 返回一个错误信息，状态码是 401
        Result errorResult = Result.error(message);
        
        // 使用 ObjectMapper 将 Result 对象转为 JSON 字符串写入响应
        response.getWriter().write(objectMapper.writeValueAsString(errorResult));
    }
}