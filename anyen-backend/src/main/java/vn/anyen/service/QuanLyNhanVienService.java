package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.anyen.dto.request.QuanLyNhanVienRequest;
import vn.anyen.dto.response.QuanLyNhanVienResponse;
import vn.anyen.entity.NhanVien;
import vn.anyen.repository.NhanVienRepository;

@Service
@RequiredArgsConstructor
public class QuanLyNhanVienService {

    private final NhanVienRepository nhanVienRepository;
    private final PasswordEncoder passwordEncoder;

    public QuanLyNhanVienResponse createQuanLyNhanVien(QuanLyNhanVienRequest request) {

        if (nhanVienRepository.existsByTenDangNhap(request.getTenDangNhap())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        if (nhanVienRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        if (nhanVienRepository.existsBySoDienThoai(request.getSoDienThoai())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        NhanVien nhanVien = NhanVien.builder()
                .hoTen(request.getHoTen().trim())
                .tenDangNhap(request.getTenDangNhap().trim())
                .matKhau(passwordEncoder.encode(request.getMatKhau()))
                .email(request.getEmail().trim())
                .soDienThoai(request.getSoDienThoai().trim())
                .diaChi(request.getDiaChi())
                .vaiTro(request.getVaiTro())
                .trangThai("HOAT_DONG")
                .build();

        nhanVienRepository.save(nhanVien);

        return QuanLyNhanVienResponse.builder()
                .maNhanVien(nhanVien.getMaNhanVien())
                .hoTen(nhanVien.getHoTen())
                .tenDangNhap(nhanVien.getTenDangNhap())
                .email(nhanVien.getEmail())
                .soDienThoai(nhanVien.getSoDienThoai())
                .diaChi(nhanVien.getDiaChi())
                .vaiTro(nhanVien.getVaiTro())
                .trangThai(nhanVien.getTrangThai())
                .build();
    }
}