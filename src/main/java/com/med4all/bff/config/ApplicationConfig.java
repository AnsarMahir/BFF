
// config/ApplicationConfig.java
package com.med4all.bff.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.med4all.bff.client")
public class ApplicationConfig {
}