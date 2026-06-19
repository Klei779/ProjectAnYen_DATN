package vn.anyen.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.anyen.entity.DonHang;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

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

    @Query(
            value = """
                    SELECT DISTINCT dh.*
                    FROM donhang dh
                    JOIN chitietdonhang ct ON ct.MaDonHang = dh.MaDonHang
                    JOIN sanpham sp ON sp.MaSanPham = ct.MaSanPham
                    LEFT JOIN khachhang kh ON kh.MaKhachHang = dh.MaKhachHang
                    WHERE sp.MaDoiTac = :maDoiTac
                      AND (
                            :keyword IS NULL
                            OR :keyword = ''
                            OR LOWER(CONCAT('DH', LPAD(dh.MaDonHang, 4, '0'))) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(kh.TenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR kh.SoDienThoai LIKE CONCAT('%', :keyword, '%')
                      )
                      AND (
                            :trangThai IS NULL
                            OR :trangThai = ''
                            OR :trangThai = 'Tất cả'
                            OR dh.TrangThai = :trangThai
                      )
                    ORDER BY dh.MaDonHang DESC
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT dh.MaDonHang)
                    FROM donhang dh
                    JOIN chitietdonhang ct ON ct.MaDonHang = dh.MaDonHang
                    JOIN sanpham sp ON sp.MaSanPham = ct.MaSanPham
                    LEFT JOIN khachhang kh ON kh.MaKhachHang = dh.MaKhachHang
                    WHERE sp.MaDoiTac = :maDoiTac
                      AND (
                            :keyword IS NULL
                            OR :keyword = ''
                            OR LOWER(CONCAT('DH', LPAD(dh.MaDonHang, 4, '0'))) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(kh.TenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR kh.SoDienThoai LIKE CONCAT('%', :keyword, '%')
                      )
                      AND (
                            :trangThai IS NULL
                            OR :trangThai = ''
                            OR :trangThai = 'Tất cả'
                            OR dh.TrangThai = :trangThai
                      )
                    """,
            nativeQuery = true
    )
    Page<DonHang> findDoiTacDonHangs(
            @Param("maDoiTac") Integer maDoiTac,
            @Param("keyword") String keyword,
            @Param("trangThai") String trangThai,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT DISTINCT dh.*
                    FROM donhang dh
                    JOIN chitietdonhang ct ON ct.MaDonHang = dh.MaDonHang
                    JOIN sanpham sp ON sp.MaSanPham = ct.MaSanPham
                    WHERE dh.MaDonHang = :maDonHang
                      AND sp.MaDoiTac = :maDoiTac
                    """,
            nativeQuery = true
    )
    Optional<DonHang> findDoiTacDonHangById(
            @Param("maDonHang") Integer maDonHang,
            @Param("maDoiTac") Integer maDoiTac
    );
}
