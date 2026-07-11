package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.CapNhatTaiKhoanDTRequest;
import vn.anyen.dto.response.TaiKhoanDoiTacResponse;
import vn.anyen.entity.DoiTac;
import vn.anyen.repository.DoiTacRepository;

@Service
@RequiredArgsConstructor
public class TaiKhoanDoiTacService {

    private final DoiTacRepository doiTacRepository;

    public TaiKhoanDoiTacResponse getTaiKhoan(String tenDangNhap) {
        DoiTac doiTac = findByTenDangNhap(tenDangNhap);
        return toResponse(doiTac);
    }

    public TaiKhoanDoiTacResponse capNhatTaiKhoan(
            String tenDangNhap,
            CapNhatTaiKhoanDTRequest request
    ) {
        DoiTac doiTac = findByTenDangNhap(tenDangNhap);

        doiTac.setTenDoiTac(trim(request.getTenDoiTac()));
        doiTac.setTenDoanhNghiep(trimToNull(request.getTenDoanhNghiep()));
        doiTac.setMaSoThue(trimToNull(request.getMaSoThue()));
        doiTac.setEmail(trimToNull(request.getEmail()));
        doiTac.setSoDienThoai(trimToNull(request.getSoDienThoai()));
        doiTac.setDiaChi(trimToNull(request.getDiaChi()));

        DoiTac saved = doiTacRepository.save(doiTac);

        return toResponse(saved);
    }

    private DoiTac findByTenDangNhap(String tenDangNhap) {
        return doiTacRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản đối tác"
                ));
    }

    private TaiKhoanDoiTacResponse toResponse(DoiTac dt) {
        return TaiKhoanDoiTacResponse.builder()
                .maDoiTac(dt.getMaDoiTac())
                .tenDoiTac(dt.getTenDoiTac())
                .tenDoanhNghiep(dt.getTenDoanhNghiep())
                .maSoThue(dt.getMaSoThue())
                .tenDangNhap(dt.getTenDangNhap())
                .email(dt.getEmail())
                .soDienThoai(dt.getSoDienThoai())
                .diaChi(dt.getDiaChi())
                .trangThai(dt.getTrangThai())
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