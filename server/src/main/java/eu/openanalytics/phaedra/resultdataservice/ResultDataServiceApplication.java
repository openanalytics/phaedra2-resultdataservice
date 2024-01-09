/**
 * Phaedra II
 *
 * Copyright (C) 2016-2024 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.resultdataservice;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.openanalytics.phaedra.plateservice.client.config.PlateServiceClientAutoConfiguration;
import eu.openanalytics.phaedra.protocolservice.client.config.ProtocolServiceClientAutoConfiguration;
import eu.openanalytics.phaedra.util.PhaedraRestTemplate;
import eu.openanalytics.phaedra.util.auth.AuthenticationConfigHelper;
import eu.openanalytics.phaedra.util.auth.AuthorizationServiceFactory;
import eu.openanalytics.phaedra.util.auth.IAuthorizationService;
import eu.openanalytics.phaedra.util.jdbc.JDBCUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;
import java.time.Clock;

@EnableWebSecurity
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@Import({PlateServiceClientAutoConfiguration.class,
        ProtocolServiceClientAutoConfiguration.class,})
public class ResultDataServiceApplication {

	private final Environment environment;

    public ResultDataServiceApplication(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(ResultDataServiceApplication.class, args);
    }

	@Bean
	@LoadBalanced
	public PhaedraRestTemplate restTemplate() {
		return new PhaedraRestTemplate();
	}

    @Bean
    public DataSource dataSource() {
        String url = environment.getProperty("DB_URL");
        if (StringUtils.isEmpty(url)) {
            throw new RuntimeException("No database URL configured: " + url);
        }
        String driverClassName = JDBCUtils.getDriverClassName(url);
        if (driverClassName == null) {
            throw new RuntimeException("Unsupported database type: " + url);
        }

        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(60000);
        config.setJdbcUrl(url);
        config.setDriverClassName(driverClassName);
        config.setUsername(environment.getProperty("DB_USER"));
        config.setPassword(environment.getProperty("DB_PASSWORD"));
        config.setAutoCommit(true);

        String schema = environment.getProperty("DB_SCHEMA");
        if (!StringUtils.isEmpty(schema)) {
            config.setConnectionInitSql("set search_path to " + schema);
        }

        return new HikariDataSource(config);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server().url(environment.getProperty("API_URL")).description("Default Server URL");
        return new OpenAPI().addServersItem(server);
    }

    @Bean
    public IAuthorizationService authService() {
        return AuthorizationServiceFactory.create();
    }

    @Bean
    public SecurityFilterChain httpSecurity(HttpSecurity http) throws Exception {
        return AuthenticationConfigHelper.configure(http);
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
