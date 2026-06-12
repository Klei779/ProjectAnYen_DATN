package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.anyen.entity.KhachHang;
import vn.anyen.entity.LichSuKhachHang;
import vn.anyen.repository.KhachHangRepository;
import vn.anyen.repository.LichSuKhachHangRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KhachHangService {

    private final KhachHangRepository khachHangRepository;
    private final LichSuKhachHangRepository lichSuKhachHangRepository;

    public List<KhachHang> getAll() {
        return khachHangRepository.findAll();
    }

    public KhachHang getById(Integer maKhachHang) {
        return khachHangRepository.findById(maKhachHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
    }

    public List<LichSuKhachHang> getLichSu(Integer maKhachHang) {
        return lichSuKhachHangRepository
                .findByKhachHang_MaKhachHangOrderByThoiGianAsc(maKhachHang);
    }
}