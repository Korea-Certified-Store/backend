package com.nainga.nainga.global.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Order(1)
@Component
public class LogAspect {

    @Pointcut("bean(*Service)")
    private void allService() {
    }

    @Pointcut("bean(*Controller)")
    private void allRequest() {
    }

    @AfterThrowing(pointcut = "allService()", throwing = "ex")
    private void logException(JoinPoint joinPoint, RuntimeException ex) {
        String Name = ex.getClass().getSimpleName();
        String Message = ex.getMessage();
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        log.warn("[Exception] {} Name=[{}] Message=[{}] args=[{}]",
                methodName, Name, Message, args);
    }

    @Around("allRequest()")
    private Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long beforeRequest = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        Object result = joinPoint.proceed();
        long timeTaken = System.currentTimeMillis() - beforeRequest;
        log.debug("[Request] {} time=[{}ms]", methodName, timeTaken);
        return result;
    }
}