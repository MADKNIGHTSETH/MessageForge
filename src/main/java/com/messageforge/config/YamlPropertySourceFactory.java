package com.messageforge.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class YamlPropertySourceFactory implements PropertySourceFactory {
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        try {
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            Properties properties = factory.getObject();
            if (properties == null) {
                properties = new Properties();
            }
            return new PropertiesPropertySource(
                    name != null ? name : resource.getResource().getFilename(), properties);
        } catch (Exception e) {
            // Ignore missing optional files
            return new PropertiesPropertySource(
                    name != null ? name : resource.getResource().getFilename(), new Properties());
        }
    }
}
