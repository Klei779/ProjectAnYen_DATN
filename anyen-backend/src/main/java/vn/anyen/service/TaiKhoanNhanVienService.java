package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.constants.AppLabels;
import vn.anyen.dto.request.CapNhatTaiKhoanNVRequest;
import vn.anyen.dto.response.TaiKhoanNhanVienResponse;
import vn.anyen.entity.NhanVien;
import vn.anyen.repository.NhanVienRepository;

@Service
@RequiredArgsConstructor
public class TaiKhoanNhanVienService {

    private final NhanVienRepository nhanVienRepository;

    public TaiKhoanNhanVienResponse getTaiKhoan(String tenDangNhap) {
        NhanVien nhanVien = findByTenDangNhap(tenDangNhap);
        return toResponse(nhanVien);
    }

    public TaiKhoanNhanVienResponse capNhatTaiKhoan(
            String tenDangNhap,
            CapNhatTaiKhoanNVRequest request
    ) {
        NhanVien nhanVien = findByTenDangNhap(tenDangNhap);

        nhanVien.setHoTen(trim(request.getHoTen()));
        nhanVien.setEmail(trimToNull(request.getEmail()));
        nhanVien.setSoDienThoai(trimToNull(request.getSoDienThoai()));
        nhanVien.setDiaChi(trimToNull(request.getDiaChi()));

        NhanVien saved = nhanVienRepository.save(nhanVien);
        return toResponse(saved);
    }

    private NhanVien findByTenDangNhap(String tenDangNhap) {
        return nhanVienRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản nhân viên"
                ));
    }

    private TaiKhoanNhanVienResponse toResponse(NhanVien nv) {
        return TaiKhoanNhanVienResponse.builder()
                .maNhanVien(nv.getMaNhanVien())
                .hoTen(nv.getHoTen())
                .tenDangNhap(nv.getTenDangNhap())
                .email(nv.getEmail())
                .soDienThoai(nv.getSoDienThoai())
                .diaChi(nv.getDiaChi())
                .vaiTro(nv.getVaiTro())
                .tenVaiTro(AppLabels.getLabel(
                        AppLabels.TEN_VAI_TRO,
                        nv.getVaiTro()))
                .tenTrangThai(AppLabels.getLabel(
                        AppLabels.TRANG_THAI_NHAN_VIEN,
                        nv.getTrangThai()))
                .trangThai(nv.getTrangThai())
                .tenTrangThai(AppLabels.getLabel(AppLabels.TRANG_THAI_NHAN_VIEN,nv.getTrangThai()))
                .tenVaiTro(AppLabels.getLabel(AppLabels.TEN_VAI_TRO,nv.getVaiTro()))
                .build();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}