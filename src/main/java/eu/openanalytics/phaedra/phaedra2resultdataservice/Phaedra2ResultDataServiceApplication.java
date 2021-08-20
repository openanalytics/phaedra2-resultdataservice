package eu.openanalytics.phaedra.phaedra2resultdataservice;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.openanalytics.phaedra.util.jdbc.JDBCUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.util.Properties;

@SpringBootApplication
@EnableEurekaClient
public class Phaedra2ResultDataServiceApplication {

    private final Environment environment;
    private final ServletContext servletContext;

    private static final String PROP_DB_URL = "phaedra2.result-data-service.db.url";
    private static final String PROP_DB_USERNAME = "phaedra2.result-data-service.db.username";
    private static final String PROP_DB_PASSWORD = "phaedra2.result-data-service.db.password";
    private static final String PROP_DB_SCHEMA = "phaedra2.result-data-service.db.schema";

    public Phaedra2ResultDataServiceApplication(Environment environment, ServletContext servletContext) {
        this.environment = environment;
        this.servletContext = servletContext;
    }

    public static void main(String[] args) {
        var app = new SpringApplication(Phaedra2ResultDataServiceApplication.class);
        app.setDefaultProperties(getDefaultProperties());
        app.run(args);
    }

    @Bean
    public DataSource dataSource() {
        String url = environment.getProperty(PROP_DB_URL);
        if (StringUtils.isEmpty(url)) {
            throw new RuntimeException("No database URL configured: " + PROP_DB_URL);
        }
        String driverClassName = JDBCUtils.getDriverClassName(url);
        if (driverClassName == null) {
            throw new RuntimeException("Unsupported database type: " + url);
        }

        HikariConfig config = new HikariConfig();
        config.setAutoCommit(false);
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(60000);
        config.setJdbcUrl(url);
        config.setDriverClassName(driverClassName);
        config.setUsername(environment.getProperty(PROP_DB_USERNAME));
        config.setPassword(environment.getProperty(PROP_DB_PASSWORD));

        String schema = environment.getProperty(PROP_DB_SCHEMA);
        if (!StringUtils.isEmpty(schema)) {
            config.setConnectionInitSql("set search_path to " + schema);
        }

        return new HikariDataSource(config);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server().url(servletContext.getContextPath()).description("Default Server URL");
        return new OpenAPI().addServersItem(server);
    }

    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog("classpath:liquibase-changeLog.xml");

        String schema = environment.getProperty(PROP_DB_SCHEMA);
        if (!StringUtils.isEmpty(schema)) {
            liquibase.setDefaultSchema(schema);
        }

        liquibase.setDataSource(dataSource());
        return liquibase;
    }

    private static Properties getDefaultProperties() {
        return new Properties();
    }

}
