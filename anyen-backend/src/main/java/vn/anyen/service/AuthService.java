package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.anyen.dto.request.LoginRequest;
import vn.anyen.dto.response.LoginResponse;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.NhanVien;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.NhanVienRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final NhanVienRepository nhanVienRepository;
    private final DoiTacRepository doiTacRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Turnstile test secret key (always passes with test sitekey)
    // Replace with real secret key in production
    private final String TURNSTILE_SECRET = "1x0000000000000000000000000000000AA";

    public LoginResponse login(LoginRequest request) {

        // Validate Captcha if provided
        if (request.getCaptchaToken() != null && !request.getCaptchaToken().isEmpty()) {
            boolean isValidCaptcha = verifyTurnstile(request.getCaptchaToken());
            if (!isValidCaptcha) {
                LoginResponse response = new LoginResponse();
                response.setSuccess(false);
                return response;
            }
        }

        LoginResponse response = new LoginResponse();

        // Đăng nhập nhân viên
        if ("NHAN_VIEN".equals(request.getLoaiTaiKhoan())) {

            Optional<NhanVien> optionalNv =
                    nhanVienRepository.findByTenDangNhap(
                            request.getTenDangNhap());

            if (optionalNv.isPresent()) {

                NhanVien nv = optionalNv.get();

                if (passwordEncoder.matches(
                        request.getMatKhau(),
                        nv.getMatKhau())) {

                    // Determine specific role
                    Integer rawRole = nv.getVaiTro();
                    String specificRole = null;
                    if (rawRole != null) {
                        if (rawRole.equals(NhanVien.VAI_TRO_ADMIN)) {
                            specificRole = "ADMIN";
                        } else if (rawRole.equals(NhanVien.VAI_TRO_HOTLINE)) {
                            specificRole = "HOTLINE";
                        } else if (rawRole.equals(NhanVien.VAI_TRO_BAN_HANG)) {
                            specificRole = "NHANVIEN";
                        }
                    }

                    if (specificRole == null) {
                        response.setSuccess(false);
                        return response; // Role không tồn tại
                    }

                    String token =
                            jwtService.generateToken(
                                    nv.getMaNhanVien(),
                                    nv.getTenDangNhap(),
                                    specificRole
                            );

                    response.setSuccess(true);
                    response.setId(nv.getMaNhanVien());
                    response.setHoTen(nv.getHoTen());
                    response.setTenDangNhap(nv.getTenDangNhap());
                    response.setLoaiTaiKhoan("NHAN_VIEN");
                    response.setVaiTroChiTiet(specificRole);
                    response.setToken(token);
                }
            }
        }

        // Đăng nhập đối tác
        else if ("DOI_TAC".equals(request.getLoaiTaiKhoan())) {

            Optional<DoiTac> optionalDt =
                    doiTacRepository.findByTenDangNhap(
                            request.getTenDangNhap());

            if (optionalDt.isPresent()) {

                DoiTac dt = optionalDt.get();

                if (passwordEncoder.matches(
                        request.getMatKhau(),
                        dt.getMatKhau())) {

                    String token =
                            jwtService.generateToken(
                                    dt.getMaDoiTac(),
                                    dt.getTenDangNhap(),
                                    "DOITAC"
                            );

                    response.setSuccess(true);
                    response.setId(dt.getMaDoiTac());
                    response.setHoTen(dt.getTenDoiTac());
                    response.setTenDangNhap(dt.getTenDangNhap());
                    response.setLoaiTaiKhoan("DOI_TAC");
                    response.setVaiTroChiTiet("DOITAC");
                    response.setToken(token);
                }
            }
        }

        return response;
    }

    private boolean verifyTurnstile(String token) {
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

            org.springframework.util.MultiValueMap<String, String> map = new org.springframework.util.LinkedMultiValueMap<>();
            map.add("secret", TURNSTILE_SECRET);
            map.add("response", token);

            org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = new org.springframework.http.HttpEntity<>(map, headers);

            org.springframework.http.ResponseEntity<java.util.Map> response = restTemplate.postForEntity("https://challenges.cloudflare.com/turnstile/v0/siteverify", request, java.util.Map.class);

            if (response.getBody() != null && Boolean.TRUE.equals(response.getBody().get("success"))) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}