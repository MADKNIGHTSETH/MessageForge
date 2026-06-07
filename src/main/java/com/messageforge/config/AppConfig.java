package com.messageforge.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages = "com.messageforge")
@EnableAsync
@EnableScheduling
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
@PropertySource(value = "classpath:application-prod.yml", factory = YamlPropertySourceFactory.class, ignoreResourceNotFound = true)
public class AppConfig {
    // Root context configuration class
}
