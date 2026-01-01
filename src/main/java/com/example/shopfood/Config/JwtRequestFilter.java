package com.example.shopfood.Config;
import com.example.shopfood.Config.JWT.JwtTokenUtils;
import com.example.shopfood.Model.DTO.LoginDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION = "Authorization";
    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    protected void doFilterInternal(HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String token = httpServletRequest.getHeader("Authorization");
        String request = httpServletRequest.getRequestURI();
        if (!StringUtils.containsAnyIgnoreCase(request,
                "/api/login",
                "/api/register",
                "/api/products/user",
                "/api/products/get-all",
                "/api/categories/get-all",
//                "/api/products/{id}",
                "/files/image/",
                "/api/products/find-all-reviews",
                "/api/products/find-by-id"
        )) {
            if (this.jwtTokenUtils.checkToken(token, httpServletResponse, httpServletRequest)) {
                LoginDTO loginDto = this.jwtTokenUtils.parseAccessToken(token);
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(loginDto.getRole());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(loginDto.getUsername(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            }
        } else {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
    }
}