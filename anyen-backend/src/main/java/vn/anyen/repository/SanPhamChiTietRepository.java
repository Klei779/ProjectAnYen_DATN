package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.anyen.entity.SanPhamChiTiet;

import java.util.List;

public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {
    List<SanPhamChiTiet> findByMaSanPhamOrderByThuTuAsc(Integer maSanPham);
    void deleteByMaSanPham(Integer maSanPham);
}
