package com.springboot.MyTodoList.security;

import com.springboot.MyTodoList.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    
    private final JwtService jwtService;

    public WebSecurityConfiguration(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    @Bean
    public BearerTokenFilter bearerTokenFilter() {
        return new BearerTokenFilter(jwtService);
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeRequests(requests -> requests
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .antMatchers("/api/*/request-code", "/api/*/validate-code").permitAll()
                .antMatchers("/api/v*/**").permitAll()  // Added this line
                .antMatchers("/api/**").authenticated()
                .anyRequest().permitAll())
            .addFilterBefore(bearerTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
