package com.messageforge;

import com.messageforge.config.AppConfig;
import com.messageforge.config.PersistenceConfig;
import com.messageforge.config.SecurityConfig;
import com.messageforge.config.WebMvcConfig;
import jakarta.servlet.Filter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class MessageForgeApplication extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] {
                AppConfig.class,
                PersistenceConfig.class,
                SecurityConfig.class
        };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] {WebMvcConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] {"/"};
    }

    @Override
    protected Filter[] getServletFilters() {
        return new Filter[] {new DelegatingFilterProxy("springSecurityFilterChain")};
    }
}
