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

    public LoginResponse login(LoginRequest request) {

        LoginResponse response = new LoginResponse();

        if("NHAN_VIEN".equals(request.getLoaiTaiKhoan())) {

            if ("NHAN_VIEN".equals(request.getLoaiTaiKhoan())) {

                Optional<NhanVien> optionalNv =
                        nhanVienRepository.findByTenDangNhap(
                                request.getTenDangNhap());

                if (optionalNv.isPresent()) {

                    NhanVien nv = optionalNv.get();

                    if (passwordEncoder.matches(
                            request.getMatKhau(),
                            nv.getMatKhau())) {

                        response.setSuccess(true);
                        response.setId(nv.getMaNhanVien());
                        response.setHoTen(nv.getHoTen());
                        response.setTenDangNhap(nv.getTenDangNhap());
                        response.setLoaiTaiKhoan("NHAN_VIEN");
                    }
                }
            }
        }

        return response;
    }
}