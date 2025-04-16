package com.psu.rouen.cphbox.config;

import com.psu.rouen.cphbox.aop.logging.ApplicationLogAspect;
import com.psu.rouen.cphbox.aop.logging.LoggingAspect;
import com.psu.rouen.cphbox.service.ApplicationLogService;
import com.psu.rouen.cphbox.service.UserService;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import tech.jhipster.config.JHipsterConstants;

@Configuration
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {

    @Bean
    @Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
    public LoggingAspect loggingAspect(Environment env) {
        return new LoggingAspect(env);
    }

    @Bean
    public ApplicationLogAspect applicationLogAspect(ApplicationLogService applicationLogService, UserService userService) {
        return new ApplicationLogAspect(applicationLogService, userService);
    }
}
