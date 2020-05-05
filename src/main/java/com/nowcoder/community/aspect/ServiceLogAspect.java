package com.nowcoder.community.aspect;

import com.nowcoder.community.controller.advice.ExceptionAdvice;
import com.nowcoder.community.model.Comment;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sun.misc.Request;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    //定义连接点
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){}

    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
       //格式：用户[1.2.3.4]，在[xxx],访问了[com.nowcoder.community.service.xxx()]
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String time = new SimpleDateFormat("YYYY-MM-dd HH-mm-ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName();
        String method = joinPoint.getSignature().getName();
        logger.error("用户[" + ip + "]在[" + time + "]访问了["+target+"."+method+"]");
        
    }
}
