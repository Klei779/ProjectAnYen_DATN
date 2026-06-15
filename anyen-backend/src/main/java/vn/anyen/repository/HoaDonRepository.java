package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.anyen.entity.HoaDon;

import java.util.Optional;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    boolean existsByDonHang_MaDonHang(Integer maDonHang);

    Optional<HoaDon> findByDonHang_MaDonHang(Integer maDonHang);
}
