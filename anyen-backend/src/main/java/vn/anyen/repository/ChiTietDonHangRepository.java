package vn.anyen.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.anyen.entity.ChiTietDonHang;

import java.util.List;
import java.util.Optional;

public interface ChiTietDonHangRepository
        extends JpaRepository<ChiTietDonHang, Integer> {

    List<ChiTietDonHang> findByDonHang_MaDonHang(Integer maDonHang);

    @Query("""
            SELECT DISTINCT ct.sanPham.maDoiTac
            FROM ChiTietDonHang ct
            WHERE ct.donHang.maDonHang = :maDonHang
              AND ct.sanPham.maDoiTac IS NOT NULL
            """)
    List<Integer> findMaDoiTacsByDonHang(
            @Param("maDonHang") Integer maDonHang
    );

    @Query("""
            SELECT ct
            FROM ChiTietDonHang ct
            WHERE ct.donHang.maDonHang = :maDonHang
              AND ct.sanPham.maDoiTac = :maDoiTac
            """)
    List<ChiTietDonHang> findByDonHangAndDoiTac(
            @Param("maDonHang") Integer maDonHang,
            @Param("maDoiTac") Integer maDoiTac
    );

    @Query("""
            SELECT COUNT(ct)
            FROM ChiTietDonHang ct
            WHERE ct.sanPham.maSanPham = :maSanPham
            AND ct.donHang.trangThai NOT IN (:hoanThanh, :daHuy)
            """)
    long demDonHangChuaKetThuc(
            @Param("maSanPham") Integer maSanPham,
            @Param("hoanThanh") Integer hoanThanh,
            @Param("daHuy") Integer daHuy
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
       SELECT ct
       FROM ChiTietDonHang ct
       WHERE ct.maDonHangChiTiet = :id
       """)
    Optional<ChiTietDonHang> findByIdForUpdate(
            @Param("id") Integer id
    );
}