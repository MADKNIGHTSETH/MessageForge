package com.messageforge;

import com.messageforge.config.AppConfig;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;

public class AppLauncher {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        String portStr = System.getProperty("server.port");
        if (portStr != null) {
            port = Integer.parseInt(portStr);
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // trigger creation of default connector

        // Base context mapped to /api for the REST API
        Context context = tomcat.addContext("/api", new File(".").getAbsolutePath());

        // Initialize Spring context
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(AppConfig.class);
        applicationContext.setServletContext(context.getServletContext());
        applicationContext.refresh();
        context.getServletContext().setAttribute(org.springframework.web.context.WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);

        // Add DispatcherServlet
        DispatcherServlet dispatcherServlet = new DispatcherServlet(applicationContext);
        Tomcat.addServlet(context, "dispatcherServlet", dispatcherServlet).setLoadOnStartup(1);
        context.addServletMappingDecoded("/*", "dispatcherServlet");
        
        // Add Spring Security Filter
        org.apache.tomcat.util.descriptor.web.FilterDef securityFilter = new org.apache.tomcat.util.descriptor.web.FilterDef();
        securityFilter.setFilterName("springSecurityFilterChain");
        securityFilter.setFilterClass("org.springframework.web.filter.DelegatingFilterProxy");
        securityFilter.setAsyncSupported("true");
        context.addFilterDef(securityFilter);

        org.apache.tomcat.util.descriptor.web.FilterMap securityFilterMap = new org.apache.tomcat.util.descriptor.web.FilterMap();
        securityFilterMap.setFilterName("springSecurityFilterChain");
        securityFilterMap.addURLPattern("/*");
        context.addFilterMap(securityFilterMap);
        
        System.out.println("Starting Embedded Tomcat on port " + port);
        tomcat.start();
        tomcat.getServer().await();
    }
}
