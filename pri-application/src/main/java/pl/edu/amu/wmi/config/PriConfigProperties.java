package pl.edu.amu.wmi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("pri")
public record PriConfigProperties(String dbPassword, String jwtToken) {
}
