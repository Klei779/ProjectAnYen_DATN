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
    private final NhanVienOnlineService nhanVienOnlineService;
    private final PhanCongTuVanService phanCongTuVanService;

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
                if (nv.getTrangThai() != 1) {
                    return response;
                }
                if (passwordEncoder.matches(
                        request.getMatKhau(),
                        nv.getMatKhau())) {

                    // Determine specific role
                    int rawRole = nv.getVaiTro();
                    String specificRole = null;
                    if (rawRole == NhanVien.VAI_TRO_ADMIN) {
                        specificRole = "ROLE_ADMIN";
                    } else if (rawRole == NhanVien.VAI_TRO_TU_VAN
                            || rawRole == NhanVien.VAI_TRO_HOTLINE) {
                        // Vai trò 3 là nhân viên trực tuyến/tư vấn, vai trò 4 là Hotline.
                        // Cả hai cùng sử dụng màn hình tiếp nhận cuộc gọi và giao việc.
                        specificRole = "ROLE_HOTLINE";
                    } else if (rawRole == NhanVien.VAI_TRO_BAN_HANG) {
                        specificRole = "ROLE_NHANVIEN";
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
                    response.setVaiTroChiTiet(specificRole.replace("ROLE_", ""));
                    response.setToken(token);

                    if ("ROLE_HOTLINE".equals(specificRole)) {
                        nhanVienOnlineService.markOnline(nv);
                        phanCongTuVanService.phanCongCacPhienDangCho();
                    }
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

                if (!DoiTac.TT_DANG_HOAT_DONG.equals(dt.getTrangThai())) {
                    response.setSuccess(false);
                    return response;
                }

                if (passwordEncoder.matches(
                        request.getMatKhau(),
                        dt.getMatKhau())) {

                    String token =
                            jwtService.generateToken(
                                    dt.getMaDoiTac(),
                                    dt.getTenDangNhap(),
                                    "ROLE_DOITAC"
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