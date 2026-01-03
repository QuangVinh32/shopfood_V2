package com.example.shopfood.Config;

import com.example.shopfood.Service.Class.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private UserService userService;

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
                .cors(withDefaults()) // 🔹 Bật CORS config bạn đã định nghĩa bên dưới
                .authorizeHttpRequests(authz -> authz
                        // Public APIs
                        .requestMatchers(HttpMethod.POST, "/api/register", "/api/login").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/pages/**",
                                "/auth/**",
                                "/api/products/**",
                                "/files/image/**",
                                "/api/categories/**",
                                "/api/v1/carts/summary"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/carts/add/{productId}", "/api/v1/carts/remove/{productId}").permitAll()

                        // Admin-only
                        .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAuthority("ADMIN")

                        // User-only
//                        .requestMatchers(HttpMethod.DELETE, "/api/v1/carts/clear", "/api/v1/carts/remove/{id}", "/api/v1/carts/delete/{id}")
//                        .hasAuthority("USER,ADMIN")

                        // Authenticated users
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders/**", "/api/v1/carts/**", "/api/v1/categories/**", "/api/v1/type/**")
                        .hasAnyAuthority("ADMIN", "MANAGER", "USER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**", "/api/v1/orders/**", "/api/v1/carts/**", "/api/v1/categories/**", "/api/v1/type/**")
                        .hasAnyAuthority("ADMIN", "MANAGER", "USER")

                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList(
//                "http://localhost:3000",
//                "http://localhost:3001",
//                "http://localhost:3002",
//                "http://localhost:5173",
//                "http://localhost:5174",
//                "http://localhost:*"
//
//        ));
//        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
//        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:3002",
                "http://localhost:5173",
                "http://localhost:5174"
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
