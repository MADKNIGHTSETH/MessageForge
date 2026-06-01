package com.messageforge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan(
        basePackages = "com.messageforge",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class),
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Controller.class),
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = RestController.class)
        })
public class AppConfig {
}
