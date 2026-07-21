package vn.anyen.config;
import org.springframework.http.HttpMethod;
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
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

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

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/ai/health"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/ai/chat"
                        )
                        .permitAll()

                        .requestMatchers(
                                "/api/ai/yeu-cau-tu-van/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/ollama/health",
                                "/api/ollama/test",
                                "/api/ollama/test-json"
                        ).permitAll()
                        
                        // Public website
                        .requestMatchers(
                                "/images/**",
                                "/uploads/**",
                                "/api/tu-van/**",
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
                                "/api/nhan-vien/quanlydoitac/**",
                                "/api/nhan-vien/quanlynhanvien",
                                "/api/nhan-vien/quanlynhanvien/**",
                                "/api/nhan-vien/quanlyhopdong",
                                "/api/nhan-vien/quanlyhopdong/**",
                                "/api/nhan-vien/quan-ly-khach-hang",
                                "/api/nhan-vien/quan-ly-khach-hang/**"
                        )
                        .hasAuthority("ROLE_ADMIN")

                        // ADMIN quản lý nhân viên


                        // ADMIN quản lý hợp đồng


                        // Hotline được tìm nhân viên gần nhất
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/nhan-vien/don-hang/de-xuat-nhan-vien"
                        )
                        .hasAnyRole("HOTLINE", "NHANVIEN", "ADMIN")

                        // Hotline được giao việc
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/nhan-vien/thong-bao/giao-cong-viec"
                        )
                        .hasAnyRole("HOTLINE", "ADMIN")

                        // Hotline được xem thông báo
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/nhan-vien/thong-bao"
                        )
                        .hasAnyRole("NHANVIEN", "HOTLINE", "ADMIN")

                        // Nhân viên trực tuyến quản lý phiên tư vấn
                        .requestMatchers("/api/nhan-vien/tu-van/**")
                        .hasAnyRole("HOTLINE", "ADMIN")

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