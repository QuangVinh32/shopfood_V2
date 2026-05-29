package com.example.shopfood.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration {

    @Autowired
    private UserDetailsService userService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .authorizeHttpRequests(authz -> authz
                        // PUBLIC
                        .requestMatchers(HttpMethod.POST,
                                "/api/login",
                                "/api/register",
                                "/api/auth/refresh",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/payments/momo/ipn"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/get-all",
                                "/api/products/user/**",
                                "/api/products/find-all-reviews/**",
                                "/api/products/find-by-id/**",
                                "/api/categories/get-all",
                                "/api/product_sizes/product/**",
                                "/files/image/**",
                                "/api/auth/verify-email",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()

                        // ADMIN-ONLY
                        .requestMatchers(HttpMethod.POST, "/api/products", "/api/products/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/product_sizes/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/product_sizes/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/product_sizes/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/categories/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/banners/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/vouchers").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/vouchers/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/vouchers/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/v1/vouchers/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/v1/orders/admin/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/notifications").hasAuthority("ADMIN")
                        .requestMatchers("/api/notifications/admin/**").hasAuthority("ADMIN")

                        // AUTHENTICATED USERS (USER + MANAGER + ADMIN)
                        .requestMatchers("/api/carts/**").authenticated()
                        .requestMatchers("/api/v1/orders/**").authenticated()
                        .requestMatchers("/api/v1/users/me/**", "/api/v1/users/me").authenticated()
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/api/favourites/**").authenticated()
                        .requestMatchers("/api/reviews/**").authenticated()
                        .requestMatchers("/files/image").authenticated() // upload (GET đã permitAll ở trên)

                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "Cache-Control"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
