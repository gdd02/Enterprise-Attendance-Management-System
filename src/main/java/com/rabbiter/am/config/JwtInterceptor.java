package com.rabbiter.am.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbiter.am.context.UserContext;
import com.rabbiter.am.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * JWT拦截器
 * 用于验证请求中的JWT Token，并将用户信息存入ThreadLocal
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS请求（CORS预检请求）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        
        // 如果token为空，尝试从Bearer格式中提取
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        //12
        // 验证token
        if (token == null || token.isEmpty()) {
            responseUnauthorized(response, "未提供认证令牌");
            return false;
        }

        // 验证token是否有效
        if (!jwtUtil.validateToken(token)) {
            responseUnauthorized(response, "令牌无效或已过期");
            return false;
        }

        // 从token中提取用户信息
        String employeeNumber = jwtUtil.getEmployeeNumberFromToken(token);
        String employeeName = jwtUtil.getEmployeeNameFromToken(token);
        
        // 将用户信息存入ThreadLocal
        UserContext.UserInfo userInfo = new UserContext.UserInfo(employeeNumber, employeeName);
        UserContext.setUser(userInfo);
        
        // 同时保留原有方式，兼容旧代码
        request.setAttribute("employeeNumber", employeeNumber);
        request.setAttribute("employeeName", employeeName);
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 请求处理完成后，可以在这里做一些后置处理
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完全结束后，清除ThreadLocal中的用户信息，避免内存泄漏
        UserContext.clear();
    }

    /**
     * 返回401未授权响应
     */
    private void responseUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        Result result = Result.failure(ResultCode.TOKEN_INVALID);
        result.setMessage(message);
        
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(result);
        
        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.flush();
        writer.close();
    }
}

