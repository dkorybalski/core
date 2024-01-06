package pl.edu.amu.wmi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import pl.edu.amu.wmi.UserManagementConfiguration;

@SpringBootConfiguration
@EnableAutoConfiguration
public class TestConfig extends UserManagementConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/**")).permitAll()
                );
        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("Coordinator 1")
                .password("Haslo123")
                .authorities("COORDINATOR").and()
                .withUser("Supervisor 1")
                .password("Haslo123")
                .authorities("SUPERVISOR").and()
                .withUser("Student 1")
                .password("Haslo123")
                .authorities("STUDENT");
    }

    @Bean("testUserDetailsServiceUserManagement")
    public UserDetailsService users() {
        UserDetails coordinator = User.builder()
                .username("Coordinator 1")
                .password("Haslo123")
                .authorities("COORDINATOR")
                .build();
        UserDetails supervisor = User.builder()
                .username("Supervisor 1")
                .password("Haslo123")
                .authorities("SUPERVISOR")
                .build();
        UserDetails student = User.builder()
                .username("Student 1")
                .password("Haslo123")
                .authorities("STUDENT")
                .build();
        return new InMemoryUserDetailsManager(coordinator, supervisor, student);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

}
