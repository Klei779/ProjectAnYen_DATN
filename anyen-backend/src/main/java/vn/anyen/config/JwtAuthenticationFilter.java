package vn.anyen.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.anyen.service.JwtService;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtService.isTokenValid(token)) {

                String tenDangNhap = jwtService.getUsernameFromToken(token);
                String roleFromToken = jwtService.getRoleFromToken(token);

                if (tenDangNhap == null || roleFromToken == null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String normalizedRole = normalizeRole(roleFromToken);
                String authority = "ROLE_" + normalizedRole;

                if (log.isDebugEnabled()) {
                    log.debug(
                            "JWT authenticated: uri={}, username={}, role={}, authority={}",
                            request.getRequestURI(),
                            tenDangNhap,
                            normalizedRole,
                            authority
                    );
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                tenDangNhap,
                                null,
                                List.of(new SimpleGrantedAuthority(authority))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            log.debug("JWT không hợp lệ: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }

        String value = role.trim();

        if (value.startsWith("ROLE_")) {
            value = value.substring(5);
        }

        if (value.equalsIgnoreCase("ADMIN")
                || value.equalsIgnoreCase("Admin")
                || value.equalsIgnoreCase("Quản lý")
                || value.equalsIgnoreCase("Quản lý An yên")) {
            return "ADMIN";
        }

        if (value.equalsIgnoreCase("DOITAC")
                || value.equalsIgnoreCase("DOI_TAC")
                || value.equalsIgnoreCase("Đối tác")) {
            return "DOITAC";
        }

        if (value.equalsIgnoreCase("HOTLINE")
                || value.equalsIgnoreCase("Nhân viên hotline")) {
            return "HOTLINE";
        }

        if (value.equalsIgnoreCase("NHANVIEN")
                || value.equalsIgnoreCase("NHAN_VIEN")
                || value.equalsIgnoreCase("Nhân viên bán hàng")
                || value.equalsIgnoreCase("Nhân viên tư vấn")
                || value.equalsIgnoreCase("Nhân viên trực tiếp")) {
            return "NHANVIEN";
        }

        return value.toUpperCase();
    }
}