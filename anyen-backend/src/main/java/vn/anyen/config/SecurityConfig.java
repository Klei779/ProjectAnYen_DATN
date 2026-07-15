package vn.anyen.config;
import org.springframework.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {

                    CorsConfiguration config =
                            new CorsConfiguration();

                    config.addAllowedOrigin("http://localhost:5173");
                    config.addAllowedMethod("*");
                    config.addAllowedHeader("*");
                    config.setAllowCredentials(true);

                    return config;
                }))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/geocoding/**").permitAll()

                        // CỰC QUAN TRỌNG: cho phép preflight OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // Đăng nhập, xác nhận tài khoản
                        .requestMatchers("/api/auth/**")
                        .permitAll()

                        // Public website
                        .requestMatchers(
                                "/images/**",
                                "/api/gioi-thieu",
                                "/api/gioi-thieu/**",
                                "/api/san-pham",
                                "/api/san-pham/**",
                                "/api/dich-vu",
                                "/api/dich-vu/**",
                                "/api/lien-he",
                                "/api/lien-he/**",
                                "/api/khach-hang",
                                "/api/khach-hang/**",
                                "/api/tin-tuc",
                                "/api/tin-tuc/**"
                        )
                        .permitAll()

                        // ADMIN quản lý đối tác
                        .requestMatchers(
                                "/api/nhan-vien/quanlydoitac",
                                "/api/nhan-vien/quanlydoitac/**"
                        )
                        .hasAuthority("ROLE_ADMIN")

                        // ADMIN quản lý nhân viên
                        .requestMatchers(
                                "/api/nhan-vien/quanlynhanvien",
                                "/api/nhan-vien/quanlynhanvien/**"
                        )
                        .hasAuthority("ROLE_ADMIN")

                        // ADMIN quản lý hợp đồng
                        .requestMatchers(
                                "/api/nhan-vien/quanlyhopdong",
                                "/api/nhan-vien/quanlyhopdong/**"
                        )
                        .hasAuthority("ROLE_ADMIN")

                        // API đối tác
                        .requestMatchers("/api/doi-tac/**")
                        .hasAuthority("ROLE_DOITAC")

                        // API nhân viên còn lại
                        .requestMatchers("/api/nhan-vien/**")
                        .authenticated()
                        .anyRequest()
                        .authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}