package vn.anyen.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.anyen.entity.HopDong;

import java.util.List;

public interface HopDongRepository extends JpaRepository<HopDong, Integer> {

    boolean existsByDonHang_MaDonHang(Integer maDonHang);

    boolean existsByDonHang_KhachHang_MaKhachHang(Integer maKhachHang);

    List<HopDong> findByDonHang_KhachHang_MaKhachHang(Integer maKhachHang);

    @Query("SELECT COALESCE(MAX(hd.maHopDong), 0) FROM HopDong hd")
    Integer getMaxMaHopDong();

    @Query(
            value = """
                SELECT hd.*
                FROM hopdong hd
                LEFT JOIN donhang dh ON dh.MaDonHang = hd.MaDonHang
                LEFT JOIN khachhang kh ON kh.MaKhachHang = dh.MaKhachHang
                WHERE
                    (
                        :keyword IS NULL
                        OR :keyword = ''
                        OR LOWER(kh.TenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR kh.SoDienThoai LIKE CONCAT('%', :keyword, '%')
                        OR CAST(hd.MaHopDong AS CHAR) LIKE CONCAT('%', :keyword, '%')
                        OR CAST(dh.MaDonHang AS CHAR) LIKE CONCAT('%', :keyword, '%')
                    )
                AND
                    (
                        :trangThai IS NULL
                        OR :trangThai = ''
                        OR :trangThai = 'Tất cả'
                        OR hd.TrangThai = :trangThai
                    )
                """,
            countQuery = """
                SELECT COUNT(*)
                FROM hopdong hd
                LEFT JOIN donhang dh ON dh.MaDonHang = hd.MaDonHang
                LEFT JOIN khachhang kh ON kh.MaKhachHang = dh.MaKhachHang
                WHERE
                    (
                        :keyword IS NULL
                        OR :keyword = ''
                        OR LOWER(kh.TenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR kh.SoDienThoai LIKE CONCAT('%', :keyword, '%')
                        OR CAST(hd.MaHopDong AS CHAR) LIKE CONCAT('%', :keyword, '%')
                        OR CAST(dh.MaDonHang AS CHAR) LIKE CONCAT('%', :keyword, '%')
                    )
                AND
                    (
                        :trangThai IS NULL
                        OR :trangThai = ''
                        OR :trangThai = 'Tất cả'
                        OR hd.TrangThai = :trangThai
                    )
                """,
            nativeQuery = true
    )
    Page<HopDong> searchHopDong(
            @Param("keyword") String keyword,
            @Param("trangThai") String trangThai,
            Pageable pageable
    );
}