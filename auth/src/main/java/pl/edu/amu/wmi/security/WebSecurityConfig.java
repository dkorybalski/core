package pl.edu.amu.wmi.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableMethodSecurity(
        securedEnabled = true
)
public class WebSecurityConfig {

    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    @Value(("${spring.ldap.base}"))
    private String ldapBase;

    @Value(("${pri.ldap.domain}"))
    private String ldapDomain;

    @Value(("${ldap.authentication.enabled}"))
    private boolean ldapAuthenticationEnabled;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    private CustomLdapUserDetailsMapper customLdapUserDetailsMapper;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder authManagerBuilder) throws Exception {
        if (ldapAuthenticationEnabled) {
            ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(
                    ldapDomain,
                    ldapUrl,
                    ldapBase);
            // TODO: 12/6/2023 SYSPRI-315 should any filters be added?
            provider.setConvertSubErrorCodesToExceptions(true);
            provider.setUseAuthenticationRequestCredentials(true);
            provider.setUserDetailsContextMapper(customLdapUserDetailsMapper);
            authManagerBuilder.authenticationProvider(provider);
        } else {
            useMockLdapData(authManagerBuilder);
        }

    }

    private void useMockLdapData(AuthenticationManagerBuilder authManagerBuilder) throws Exception {
        authManagerBuilder
                .ldapAuthentication()
                .userDnPatterns("uid={0},ou=people")
                .groupSearchBase("ou=groups")
                .userDetailsContextMapper(customLdapUserDetailsMapper)
                .contextSource()
                .url("ldap://localhost:8389/dc=springframework,dc=org")
                .and()
                .passwordCompare()
                .passwordEncoder(new BCryptPasswordEncoder())
                .passwordAttribute("userPassword");
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(antMatcher("/auth/**")).permitAll()
                                .requestMatchers(antMatcher("pri/auth/**")).permitAll()
                                .requestMatchers(antMatcher("/v3/api-docs**")).permitAll()
                                .requestMatchers(antMatcher("pri/v3/api-docs**")).permitAll()
                                .anyRequest().authenticated()
                );

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
