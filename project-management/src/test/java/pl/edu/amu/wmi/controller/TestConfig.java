package pl.edu.amu.wmi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import pl.edu.amu.wmi.ProjectManagementConfiguration;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableWebSecurity
public class TestConfig extends ProjectManagementConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(antMatcher("/auth/**")).permitAll()
                        .requestMatchers(antMatcher("pri/auth/**")).permitAll()
                        .requestMatchers(antMatcher("/v3/api-docs**")).permitAll()
                        .requestMatchers(antMatcher("pri/v3/api-docs**")).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // Configure an in-memory authentication manager for testing
        auth.inMemoryAuthentication()
                .withUser("Coordinator 1")
                .password("Haslo123")
                .authorities("COORDINATOR");
    }

    @Bean("testUserDetailsService")
    public UserDetailsService users() {
        UserDetails user = User.builder()
                .username("Coordinator 1")
                .password("Haslo123")
                .authorities("COORDINATOR")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

}
