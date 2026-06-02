package com.healthcare.appointment_service.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class AppointmentServiceAOP {
    private final Logger log = LoggerFactory.getLogger(AppointmentServiceAOP.class);

    @Pointcut("execution(* com.healthcare.appointment_service.service.*.*(..))")
    public void appointment_service() {}

    @Pointcut("execution(* com.healthcare.appointment_service.controller.*.*(..))")
    public void appointment_controller() {}

    @Around("appointment_service() || appointment_controller()")
    public Object logAroun(ProceedingJoinPoint joinPoint) throws Throwable
    {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("Entering method: {}", methodName);
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long elapsed = System.currentTimeMillis() - start;
        log.info("Exiting: {} | took {}ms", methodName, elapsed);
        return result;
        
    }

    @AfterThrowing(pointcut = "appointment_service() || appointment_controller()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Exception ex)
    {
        log.error("Exception thrown: {} | message: {}", ex.getClass().getSimpleName(), ex.getMessage());
    }
}
