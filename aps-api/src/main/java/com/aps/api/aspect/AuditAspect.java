package com.aps.api.aspect;

import com.aps.domain.annotation.Audited;
import com.aps.api.security.UserPrincipal;
import com.aps.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    /** 敏感字段名，序列化时跳过 */
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "passwordHash", "oldPassword", "newPassword",
            "token", "accessToken", "refreshToken", "secret"
    );

    @Around("@annotation(com.aps.domain.annotation.Audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Audited audited = method.getAnnotation(Audited.class);

        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";
        UUID userId = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getId();
        }

        // 获取请求信息
        HttpServletRequest request = getCurrentRequest();
        String ipAddress = request != null ? getClientIpAddress(request) : null;

        // 获取方法参数作为详细信息（脱敏敏感字段）
        String details = serializeArgsSafely(joinPoint.getArgs());

        // 执行方法，无论成功失败都记录审计日志
        Object result;
        try {
            result = joinPoint.proceed();

            // 成功时记录
            String successDetails = details;
            if (successDetails == null) {
                successDetails = "{\"result\":\"SUCCESS\"}";
            }
            auditService.logAudit(userId, username, audited.action(), audited.resource(), successDetails, ipAddress);

            return result;
        } catch (Throwable t) {
            // 失败时也记录
            String failureDetails = details != null
                    ? details.substring(0, Math.min(details.length(), 500)) + ",\"error\":\"" + t.getMessage() + "\"}"
                    : "{\"result\":\"FAILURE\",\"error\":\"" + t.getMessage() + "\"}";
            auditService.logAudit(userId, username, audited.action(), audited.resource(), failureDetails, ipAddress);

            throw t;
        }
    }

    /**
     * 安全序列化方法参数，脱敏敏感字段
     */
    private String serializeArgsSafely(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(args);
            // 简单脱敏：替换敏感字段值
            for (String field : SENSITIVE_FIELDS) {
                // 匹配 "password":"xxx" 或 "password":"xxx" 模式并替换值
                json = json.replaceAll(
                        "(\"" + field + "\"\\s*:\\s*\")[^\"]*\"",
                        "$1******\""
                );
            }
            // 截断过长的详情
            if (json.length() > 2000) {
                json = json.substring(0, 2000) + "...(truncated)";
            }
            return json;
        } catch (Exception e) {
            log.warn("无法序列化方法参数: {}", e.getMessage());
            return "{\"serializationFailed\":true}";
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        // 取 X-Forwarded-For 最后一个有效IP（最接近真实客户端）
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(forwardedFor)) {
            String[] ips = forwardedFor.split(",");
            for (int i = ips.length - 1; i >= 0; i--) {
                String ip = ips[i].trim();
                if (!ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                    return ip;
                }
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty() && !"unknown".equalsIgnoreCase(realIp)) {
            return realIp;
        }
        // 将IPv6回环地址转换为IPv4格式，更易读
        String remoteAddr = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        return remoteAddr;
    }
}
