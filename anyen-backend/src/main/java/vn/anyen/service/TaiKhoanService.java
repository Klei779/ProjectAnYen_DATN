package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.DoiMatKhauRequest;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.NhanVien;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.NhanVienRepository;

@Service
@RequiredArgsConstructor
public class TaiKhoanService {

    private final NhanVienRepository nhanVienRepository;
    private final DoiTacRepository doiTacRepository;
    private final PasswordEncoder passwordEncoder;

    public String doiMatKhau(DoiMatKhauRequest request, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập");
        }

        String tenDangNhap = authentication.getName();

        if (!request.getMatKhauMoi().equals(request.getXacNhanMatKhau())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Xác nhận mật khẩu không khớp");
        }

        if (request.getMatKhauCu().equals(request.getMatKhauMoi())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới không được trùng mật khẩu cũ");
        }

        boolean laDoiTac = authentication.getAuthorities()
                .stream()
                .anyMatch(q -> q.getAuthority().contains("DOI_TAC"));

        if (laDoiTac) {
            return doiMatKhauDoiTac(tenDangNhap, request);
        }

        return doiMatKhauNhanVien(tenDangNhap, request);
    }

    private String doiMatKhauNhanVien(String tenDangNhap, DoiMatKhauRequest request) {
        NhanVien nhanVien = nhanVienRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản nhân viên"
                ));

        if (!passwordEncoder.matches(request.getMatKhauCu(), nhanVien.getMatKhau())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu cũ không đúng");
        }

        nhanVien.setMatKhau(passwordEncoder.encode(request.getMatKhauMoi()));
        nhanVienRepository.save(nhanVien);

        return "Đổi mật khẩu thành công";
    }

    private String doiMatKhauDoiTac(String tenDangNhap, DoiMatKhauRequest request) {
        DoiTac doiTac = doiTacRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản đối tác"
                ));

        if (!passwordEncoder.matches(request.getMatKhauCu(), doiTac.getMatKhau())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu cũ không đúng");
        }

        doiTac.setMatKhau(passwordEncoder.encode(request.getMatKhauMoi()));
        doiTacRepository.save(doiTac);

        return "Đổi mật khẩu thành công";
    }
}