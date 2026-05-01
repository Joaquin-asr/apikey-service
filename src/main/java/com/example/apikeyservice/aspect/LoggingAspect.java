package com.example.apikeyservice.aspect;

import com.example.apikeyservice.dto.ApiKeyResponseDto;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.example.apikeyservice.service..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String operation = joinPoint.getSignature().getName();
        long start = System.currentTimeMillis();

        log.info("[{}] Inicio", operation);

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;

            if (result instanceof ApiKeyResponseDto dto) {
                log.info("[{}] Fin - code={} ({}ms)", operation, dto.code(), elapsed);
            } else {
                log.info("[{}] Fin ({}ms)", operation, elapsed);
            }

            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.debug("[{}] Excepcion lanzada: {} ({}ms)", operation, e.getClass().getSimpleName(), elapsed);
            throw e;
        }
    }
}