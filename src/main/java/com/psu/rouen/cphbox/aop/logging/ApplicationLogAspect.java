package com.psu.rouen.cphbox.aop.logging;

import com.psu.rouen.cphbox.domain.ApplicationLog;
import com.psu.rouen.cphbox.domain.User;
import com.psu.rouen.cphbox.service.ApplicationLogService;
import com.psu.rouen.cphbox.service.UserService;
import java.lang.reflect.Method;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Aspect for logging execution of service and repository Spring components.
 *
 * By default, it only runs with the "dev" profile.
 */
@Aspect
public class ApplicationLogAspect {

    private final ApplicationLogService applicationLogService;
    private final UserService userService;
    private Logger log = LoggerFactory.getLogger(ApplicationLogAspect.class.getName());

    public ApplicationLogAspect(ApplicationLogService applicationLogService, UserService userService) {
        this.applicationLogService = applicationLogService;
        this.userService = userService;
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void springBeanPointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Pointcut that matches all Spring beans in the application's main packages.
     */
    @Pointcut("within(com.psu.rouen.cphbox.web.rest..*) && !execution(* com.psu.rouen.cphbox.web.rest.ApplicationLogResource.*(..)) ")
    public void applicationPackagePointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    //    @Pointcut("within(@com.psu.rouen.cphbox.web.rest.annotations.AppLog *)")
    //    public void pointcut() {
    //    }

    //    @Around("pointcut()")
    @Around("applicationPackagePointcut() && springBeanPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        try {
            //Execution method
            result = joinPoint.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //Save log
        saveLog(joinPoint);
        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ApplicationLog applicationLog = new ApplicationLog();
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        applicationLog.setMethod(className + "." + methodName + "()");
        Object[] args = joinPoint.getArgs();
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = u.getParameterNames(method);
        if (args != null && paramNames != null) {
            String params = "";
            for (int i = 0; i < args.length; i++) {
                params += "  " + paramNames[i] + ": " + args[i];
            }
            applicationLog.setParams(params);
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        applicationLog.setEndPoint(request.getServletPath());
        Optional<User> user = userService.getUserWithAuthorities();
        if (user.isPresent()) {
            applicationLog.setUser(user.get());
        }

        applicationLog.setOperation(request.getMethod());
        applicationLogService.save(applicationLog);
    }
}
