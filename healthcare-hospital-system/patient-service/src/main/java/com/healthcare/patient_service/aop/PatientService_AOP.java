package com.healthcare.patient_service.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class PatientService_AOP {
    
    @Pointcut("execution(* com.healthcare.patient_service.service.PatientService.*(..))")
    public void patient_service()
    {}

    @Pointcut("execution(* com.healthcare.patient_service.controller.PatientController.*(..))")
    public void patient_controller() {}

    private static final Logger log = LoggerFactory.getLogger(PatientService_AOP.class);

    /*
    @Before("patient_service() || patient_controller()")
    public void beforeAdvice(JoinPoint joinPoint)
    {
        log.info("Entered method: {}", joinPoint.getSignature().getName());
    }

    @After("patient_service() || patient_controller()")
    public void afterAdvice(JoinPoint joinPoint)
    {
        log.info("Exiting the method: {}", joinPoint.getSignature().getName());
    }

    */

    @AfterThrowing(pointcut = "patient_service() || patient_controller()", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Throwable ex) {
        log.error("Method {} threw: {} - {}", joinPoint.getSignature().getName(), 
        ex.getClass().getSimpleName(), ex.getMessage());
    }

    //we are using the @Around because
    // @Around does the work of both the @Abefore and @After.
    
    @Around("patient_service() || patient_controller()")
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
