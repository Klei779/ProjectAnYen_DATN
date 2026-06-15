package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.anyen.entity.ChiTietDonHang;

import java.util.List;

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
}