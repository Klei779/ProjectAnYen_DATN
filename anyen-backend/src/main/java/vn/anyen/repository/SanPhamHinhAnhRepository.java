package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.anyen.entity.SanPhamHinhAnh;

import java.util.List;

public interface SanPhamHinhAnhRepository extends JpaRepository<SanPhamHinhAnh, Integer> {
    List<SanPhamHinhAnh> findByMaSanPhamOrderByThuTuAsc(Integer maSanPham);
    void deleteByMaSanPham(Integer maSanPham);
}
