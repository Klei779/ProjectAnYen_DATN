package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.anyen.entity.LichSuKhachHang;

import java.util.List;

public interface LichSuKhachHangRepository extends JpaRepository<LichSuKhachHang, Integer> {

    List<LichSuKhachHang> findByKhachHang_MaKhachHangOrderByThoiGianAsc(Integer maKhachHang);
}