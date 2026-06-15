package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.anyen.entity.HoaDon;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    boolean existsByDonHang_MaDonHang(Integer maDonHang);

    Optional<HoaDon> findByDonHang_MaDonHang(Integer maDonHang);
    List<HoaDon> findByDonHang_KhachHang_MaKhachHang(Integer maKhachHang);

    @Query("""
    SELECT COUNT(hd) > 0
    FROM HoaDon hd
    WHERE hd.donHang.khachHang.maKhachHang = :maKhachHang
    AND LOWER(COALESCE(hd.trangThai, '')) IN ('da_thanh_toan', 'đã thanh toán', 'da thanh toan', 'paid', 'hoàn thành', 'hoan thanh')
    """)
    boolean existsHoaDonDaThanhToanByKhachHang(@Param("maKhachHang") Integer maKhachHang);
}
