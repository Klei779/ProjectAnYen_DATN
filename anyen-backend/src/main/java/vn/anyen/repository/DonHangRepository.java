package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.anyen.entity.DonHang;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DonHangRepository extends JpaRepository<DonHang, Integer> {
    List<DonHang> findByTrangThai(String trangThai);
    List<DonHang> findByKhachHang_MaKhachHangOrderByNgayTaoDonDesc(Integer maKhachHang);

    boolean existsByKhachHang_MaKhachHang(Integer maKhachHang);

    @Query("""
    SELECT COUNT(dh) > 0
    FROM DonHang dh
    WHERE dh.khachHang.maKhachHang = :maKhachHang
    AND (
        LOWER(COALESCE(dh.trangThaiThanhToan, '')) IN ('da_thanh_toan', 'đã thanh toán', 'da thanh toan', 'paid')
        OR LOWER(COALESCE(dh.trangThai, '')) IN ('da_thanh_toan', 'đã thanh toán', 'da thanh toan', 'paid', 'hoàn thành', 'hoan thanh')
    )
    """)
    boolean existsDonHangDaThanhToanByKhachHang(@Param("maKhachHang") Integer maKhachHang);
}
