package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.anyen.entity.KhachHang;
import vn.anyen.repository.KhachHangRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KhachHangService {

    private final KhachHangRepository khachHangRepository;

    public List<KhachHang> getAll() {
        return khachHangRepository.findAll();
    }

    /**
     * Lấy danh sách khách hàng theo nhân viên phụ trách
     */
    public List<KhachHang> getByNhanVien(Integer maNhanVien) {
        return khachHangRepository.findByMaNhanVienPhuTrach(maNhanVien);
    }

    public KhachHang getById(Integer maKhachHang) {
        return khachHangRepository.findById(maKhachHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
    }

}