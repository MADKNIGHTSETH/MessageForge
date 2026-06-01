package com.messageforge.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.messageforge.repository")
public class PersistenceConfig {

    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(AppProperties.get(
                "jdbc:postgresql://localhost:5432/messageforge",
                "DB_URL",
                "SPRING_DATASOURCE_URL"));
        config.setDriverClassName("org.postgresql.Driver");
        config.setUsername(AppProperties.get("postgres", "DB_USERNAME", "SPRING_DATASOURCE_USERNAME"));
        config.setPassword(AppProperties.get("postgres", "DB_PASSWORD", "SPRING_DATASOURCE_PASSWORD"));
        config.setMaximumPoolSize(AppProperties.getInt(20, "DB_POOL_MAX_SIZE"));
        config.setMinimumIdle(AppProperties.getInt(5, "DB_POOL_MIN_IDLE"));
        config.setConnectionTimeout(20000);
        return new HikariDataSource(config);
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
    }

    @Bean
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.messageforge.model");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaProperties(jpaProperties());
        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    private Properties jpaProperties() {
        Properties properties = new Properties();
        properties.setProperty(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty(AvailableSettings.HBM2DDL_AUTO, "validate");
        properties.setProperty(AvailableSettings.PHYSICAL_NAMING_STRATEGY,
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        properties.setProperty(AvailableSettings.SHOW_SQL, "false");
        properties.setProperty(AvailableSettings.FORMAT_SQL, "true");
        properties.setProperty(AvailableSettings.STATEMENT_BATCH_SIZE, "15");
        return properties;
    }
}
