package com.example.naejango.global.config;

import com.example.naejango.global.auth.filter.JwtAuthenticationFilter;
import com.example.naejango.global.auth.handler.AccessDeniedHandlerImpl;
import com.example.naejango.global.auth.handler.OAuthLoginFailureHandler;
import com.example.naejango.global.auth.handler.OAuthLoginSuccessHandler;
import com.example.naejango.global.auth.jwt.JwtAuthenticator;
import com.example.naejango.global.auth.principal.PrincipalOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static com.example.naejango.domain.user.domain.Role.*;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final PrincipalOAuth2UserService principalOauth2UserService;
    private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;
    private final OAuthLoginFailureHandler oAuthLoginFailureHandler;
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
                .addFilter(new JwtAuthenticationFilter(authenticationManager(), jwtAuthenticator))
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
                .and()
                .authorizeRequests()
                .antMatchers("/api/auth/**")
                .permitAll()
                .antMatchers(HttpMethod.POST, "/api/user/profile")
                .hasAnyRole(TEMPORAL.toString(), ADMIN.toString())
                .antMatchers("/api/user/**")
                .hasAnyRole(ADMIN.toString(), USER.toString(), GUEST.toString())
                .anyRequest().permitAll()
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .userService(principalOauth2UserService)
                .and()
                .successHandler(oAuthLoginSuccessHandler)
                .failureHandler(oAuthLoginFailureHandler);
        return http.build();
    }
}

