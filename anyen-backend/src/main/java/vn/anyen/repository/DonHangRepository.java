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
    List<DonHang> findByTrangThai(Integer trangThai);
    List<DonHang> findByKhachHang_MaKhachHangOrderByNgayTaoDonDesc(Integer maKhachHang);

    List<DonHang> findByNhanVien_MaNhanVienOrderByNgayTaoDonDesc(Integer maNhanVien);

    boolean existsByKhachHang_MaKhachHang(Integer maKhachHang);

    @Query("""
    SELECT COUNT(dh) > 0
    FROM DonHang dh
    WHERE dh.khachHang.maKhachHang = :maKhachHang
    AND (
        dh.trangThaiThanhToan = 1
        OR dh.trangThai = 6
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
    @Query(
            value = """
                SELECT COUNT(DISTINCT sp.MaDoiTac)
                FROM chitietdonhang ct
                JOIN sanpham sp ON sp.MaSanPham = ct.MaSanPham
                WHERE ct.MaDonHang = :maDonHang
                """,
            nativeQuery = true
    )
    long countDoiTacTrongDonHang(@Param("maDonHang") Integer maDonHang);


    @Query(
            value = """
                SELECT COUNT(DISTINCT tbdt.MaDoiTac)
                FROM thongbaodoitac tbdt
                WHERE tbdt.MaDonHang = :maDonHang
                  AND tbdt.Loai = 'DON_HANG'
                  AND tbdt.TrangThaiThongBao = 'DA_CHAP_NHAN'
                """,
            nativeQuery = true
    )
    long countDoiTacDaChapNhan(@Param("maDonHang") Integer maDonHang);


    @Query(
            value = """
                SELECT COUNT(DISTINCT tbdt.MaDoiTac)
                FROM thongbaodoitac tbdt
                WHERE tbdt.MaDonHang = :maDonHang
                  AND tbdt.Loai = 'DON_HANG'
                  AND tbdt.TrangThaiThongBao = 'DA_TU_CHOI'
                """,
            nativeQuery = true
    )
    long countDoiTacTuChoi(@Param("maDonHang") Integer maDonHang);

    @Query(
            value = """
                SELECT DISTINCT sp.MaDoiTac
                FROM chitietdonhang ct
                JOIN sanpham sp ON sp.MaSanPham = ct.MaSanPham
                WHERE ct.MaDonHang = :maDonHang
                """,
            nativeQuery = true
    )
    List<Integer> findDoiTacIdsByDonHang(@Param("maDonHang") Integer maDonHang);
}
