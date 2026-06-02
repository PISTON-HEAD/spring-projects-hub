package com.healthcare.doctor_service.aop;

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
public class DoctorService_AOP {

    private  Logger log = LoggerFactory.getLogger(DoctorService_AOP.class);

    @Pointcut("execution(* com.healthcare.doctor_service.service.*.*(..))")
    public void doctor_service(){}

    @Pointcut("execution(* com.healthcare.doctor_service.service.*.*(..))")
    public void doctor_controller(){}

    @AfterThrowing(pointcut = "doctor_service() || doctor_controller()", throwing = "ex")
    public void afterThrowingAdvice(JoinPoint joinPoint, Throwable ex)
    {
        log.error("Method {} threw: {} - {}", joinPoint.getSignature().getName(), 
        ex.getClass().getSimpleName(), ex.getMessage());
    }

    @Around("doctor_service() || doctor_controller()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable
    {
        log.info("Entered method: {}", joinPoint.getSignature().getName());
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long time = System.currentTimeMillis() - start;
        log.info("{}() took {}ms", joinPoint.getSignature().getName(), time);
        log.info("Exiting the method: {}", joinPoint.getSignature().getName());
        return result;
    }
}
