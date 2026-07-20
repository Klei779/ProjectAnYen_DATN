package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.anyen.entity.KhachHang;

import java.util.List;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    /**
     * Lấy danh sách khách hàng theo nhân viên phụ trách
     */
    List<KhachHang> findByMaNhanVienPhuTrach(Integer maNhanVienPhuTrach);
    List<KhachHang> findAll();
}