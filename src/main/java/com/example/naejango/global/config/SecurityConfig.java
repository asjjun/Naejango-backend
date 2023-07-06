package com.example.naejango.global.config;

import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.global.auth.filter.JwtAuthorizationFilter;
import com.example.naejango.global.auth.handler.AccessDeniedHandlerImpl;
import com.example.naejango.global.auth.handler.OauthLoginSuccessHandler;
import com.example.naejango.global.auth.jwt.JwtAuthenticator;
import com.example.naejango.global.auth.principal.PrincipalOauth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final PrincipalOauth2UserService principalOauth2UserService;
    private final OauthLoginSuccessHandler oauthLoginSuccessHandler;
    private final AccessDeniedHandlerImpl accessDeniedHandler;
    private final JwtAuthenticator jwtAuthenticator;
    private final CorsConfig corsConfig;

    @Bean
    public AuthenticationManager authenticationManager(){
        return new ProviderManager(new DaoAuthenticationProvider());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(corsConfig.corsFilter())
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), jwtAuthenticator))
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
                .and()
                .authorizeRequests()
                .antMatchers("/api/user/**")
                .hasAnyRole(Role.USER.toString(), Role.ADMIN.toString())
                .antMatchers("/api/admin/**")
                .hasRole(Role.ADMIN.toString())
                .anyRequest().permitAll()
                .and()
                .oauth2Login()
                .loginPage("/localtest")
                .userInfoEndpoint()
                .userService(principalOauth2UserService)
                .and()
                .successHandler(oauthLoginSuccessHandler);
        return http.build();
    }
}

