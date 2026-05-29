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
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION = "Authorization";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    // Whitelist: method -> list of path patterns
    private static final Map<String, List<String>> PUBLIC_PATHS = Map.of(
            HttpMethod.POST.name(), List.of(
                    "/api/login",
                    "/api/register",
                    "/api/auth/refresh",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password",
                    "/api/payments/momo/ipn"
            ),
            HttpMethod.GET.name(), List.of(
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
            )
    );

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    @NotNull HttpServletResponse httpServletResponse,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        if (isPublic(httpServletRequest)) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        String token = httpServletRequest.getHeader(AUTHORIZATION);
        if (this.jwtTokenUtils.checkToken(token, httpServletResponse, httpServletRequest)) {
            LoginDTO loginDto = this.jwtTokenUtils.parseAccessToken(token);
            if (loginDto.getUsername() == null || loginDto.getRole() == null) {
                // parse fail (đã log trong parseAccessToken)
                return;
            }
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(loginDto.getRole());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

    private boolean isPublic(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        List<String> patterns = PUBLIC_PATHS.get(method);
        if (patterns == null) return false;
        for (String pattern : patterns) {
            if (PATH_MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }
}
