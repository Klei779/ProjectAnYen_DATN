package vn.anyen.config;

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

                        // API đăng nhập cho phép không cần token
                        .requestMatchers("/api/auth/**")
                        .permitAll()

                        // API public cho khách hàng xem
                        .requestMatchers(
                                "/api/gioi-thieu/**",
                                "/api/gioi-thieu",
                                "/api/san-pham",
                                "/api/san-pham/**",
                                "/api/dich-vu/**",
                                "/api/lien-he/**",
                                "/api/dich-vu",
                                "/api/khach-hang",
                                "/api/khach-hang/**",
                                "/api/lien-he"

                        )
                        .permitAll()

                        // API đối tác bắt buộc role DOI_TAC
                        .requestMatchers("/api/doi-tac/**")
                        .hasRole("DOI_TAC")

                        // API nhân viên bắt buộc role NHAN_VIEN
                        .requestMatchers("/api/nhan-vien/**")

                        .hasRole("NHAN_VIEN")

                        // Còn lại phải đăng nhập
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