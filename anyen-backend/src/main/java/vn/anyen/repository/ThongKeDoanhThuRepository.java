package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.anyen.entity.HoaDon;
import vn.anyen.repository.projection.DoanhThuDoiTuongProjection;
import vn.anyen.repository.projection.DoanhThuPhuongThucProjection;
import vn.anyen.repository.projection.DoanhThuSanPhamProjection;
import vn.anyen.repository.projection.DoanhThuTheoThoiGianProjection;
import vn.anyen.repository.projection.DoanhThuTongQuanProjection;

import java.time.LocalDate;
import java.util.List;

public interface ThongKeDoanhThuRepository
        extends JpaRepository<HoaDon, Integer> {

    @Query(value = """
            SELECT
                COALESCE(SUM(hd.TongTien), 0) AS tongDoanhThu,
                COUNT(DISTINCT hd.MaHoaDon) AS tongHoaDon,
                COUNT(DISTINCT hd.MaDonHang) AS tongDonHang,
                COALESCE(AVG(hd.TongTien), 0) AS doanhThuTrungBinh
            FROM hoadon hd
            JOIN donhang dh
                ON dh.MaDonHang = hd.MaDonHang
            WHERE hd.NgayIn BETWEEN :tuNgay AND :denNgay
              AND (
                    :maNhanVien IS NULL
                    OR dh.MaNhanVien = :maNhanVien
              )
              AND (
                    hd.TrangThai IS NULL
                    OR LOWER(hd.TrangThai) NOT LIKE '%hủy%'
              )
            """, nativeQuery = true)
    DoanhThuTongQuanProjection getTongQuanNhanVien(
            @Param("maNhanVien") Integer maNhanVien,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay
    );

    @Query(value = """
            SELECT
                CASE
                    WHEN :kieuThongKe = 'NAM'
                        THEN DATE_FORMAT(hd.NgayIn, '%Y')
                    WHEN :kieuThongKe = 'THANG'
                        THEN DATE_FORMAT(hd.NgayIn, '%Y-%m')
                    ELSE DATE_FORMAT(hd.NgayIn, '%Y-%m-%d')
                END AS thoiGian,

                COALESCE(SUM(hd.TongTien), 0) AS doanhThu,

                COUNT(
                    DISTINCT hd.MaDonHang
                ) AS soDonHang

            FROM hoadon hd

            JOIN donhang dh
                ON dh.MaDonHang = hd.MaDonHang

            WHERE hd.NgayIn BETWEEN :tuNgay AND :denNgay

              AND (
                    :maNhanVien IS NULL
                    OR dh.MaNhanVien = :maNhanVien
              )

              AND (
                    hd.TrangThai IS NULL
                    OR LOWER(hd.TrangThai) NOT LIKE '%hủy%'
              )

            GROUP BY thoiGian
            ORDER BY MIN(hd.NgayIn)
            """, nativeQuery = true)
    List<DoanhThuTheoThoiGianProjection> getBieuDoNhanVien(
            @Param("maNhanVien") Integer maNhanVien,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay,
            @Param("kieuThongKe") String kieuThongKe
    );

    @Query(value = """
            SELECT
                sp.MaSanPham AS maSanPham,
                sp.TenSanPham AS tenSanPham,

                COALESCE(
                    SUM(ct.SoLuong),
                    0
                ) AS soLuongBan,

                COALESCE(
                    SUM(ct.SoLuong * ct.GiaTien),
                    0
                ) AS doanhThu

            FROM hoadon hd

            JOIN donhang dh
                ON dh.MaDonHang = hd.MaDonHang

            JOIN chitietdonhang ct
                ON ct.MaDonHang = hd.MaDonHang

            JOIN sanpham sp
                ON sp.MaSanPham = ct.MaSanPham

            WHERE hd.NgayIn BETWEEN :tuNgay AND :denNgay

              AND (
                    :maNhanVien IS NULL
                    OR dh.MaNhanVien = :maNhanVien
              )

              AND (
                    hd.TrangThai IS NULL
                    OR LOWER(hd.TrangThai) NOT LIKE '%hủy%'
              )

            GROUP BY
                sp.MaSanPham,
                sp.TenSanPham

            ORDER BY doanhThu DESC
            LIMIT 5
            """, nativeQuery = true)
    List<DoanhThuSanPhamProjection> getTopSanPhamNhanVien(
            @Param("maNhanVien") Integer maNhanVien,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay
    );

    @Query(value = """
            SELECT
                COALESCE(
                    NULLIF(hd.PhuongThucThanhToan, ''),
                    'Chưa cập nhật'
                ) AS phuongThucThanhToan,

                COUNT(
                    DISTINCT hd.MaHoaDon
                ) AS soHoaDon,

                COALESCE(
                    SUM(hd.TongTien),
                    0
                ) AS doanhThu

            FROM hoadon hd

            JOIN donhang dh
                ON dh.MaDonHang = hd.MaDonHang

            WHERE hd.NgayIn BETWEEN :tuNgay AND :denNgay

              AND (
                    :maNhanVien IS NULL
                    OR dh.MaNhanVien = :maNhanVien
              )

              AND (
                    hd.TrangThai IS NULL
                    OR LOWER(hd.TrangThai) NOT LIKE '%hủy%'
              )

            GROUP BY phuongThucThanhToan
            ORDER BY doanhThu DESC
            """, nativeQuery = true)
    List<DoanhThuPhuongThucProjection> getPhuongThucNhanVien(
            @Param("maNhanVien") Integer maNhanVien,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay
    );

    @Query(value = """
            SELECT
                COALESCE(
                    SUM(ct.SoLuong * ct.GiaTien),
                    0
                ) AS tongDoanhThu,

                COUNT(
                    DISTINCT hd.MaHoaDon
                ) AS tongHoaDon,

                COUNT(
                    DISTINCT hd.MaDonHang
                ) AS tongDonHang,

                CASE
                    WHEN COUNT(DISTINCT hd.MaDonHang) = 0
                        THEN 0
                    ELSE
                        COALESCE(
                            SUM(ct.SoLuong * ct.GiaTien),
                            0
                        ) / COUNT(DISTINCT hd.MaDonHang)
                END AS doanhThuTrungBinh

            FROM hoadon hd

            JOIN chitietdonhang ct
                ON ct.MaDonHang = hd.MaDonHang

            JOIN sanpham sp
                ON sp.MaSanPham = ct.MaSanPham

            WHERE hd.NgayIn BETWEEN :tuNgay AND :denNgay

              AND sp.MaDoiTac = :maDoiTac

              AND (
                    hd.TrangThai IS NULL
                    OR LOWER(hd.TrangThai) NOT LIKE '%hủy%'
              )
            """, nativeQuery = true)
    DoanhThuTongQuanProjection getTongQuanDoiTac(
            @Param("maDoiTac") Integer maDoiTac,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay
    );

    @Query(value = """
            SELECT
                CASE
                    WHEN :kieuThongKe = 'NAM'
                        THEN DATE_FORMAT(hd.NgayIn, '%Y')
                    WHEN :kieuThongKe = 'THANG'
                        THEN DATE_FORMAT(hd.NgayIn, '%Y-%m')
                    ELSE DATE_FORMAT(hd.NgayIn, '%Y-%m-%d')
                END AS thoiGian,

                COALESCE(
                    SUM(ct.SoLuong * ct.GiaTien),
                    0
                ) AS doanhThu,

                COUNT(
                    DISTINCT hd.MaDonHang
                ) AS soDonHang

            FROM hoadon hd

            JOIN chitietdonhang ct
                ON ct.MaDonHang = hd.MaDonHang

            JOIN sanpham sp
                ON sp.MaSanPham = ct.MaSanPham

            WHERE hd.NgayIn BETWEEN :tuNgay AND :denNgay

              AND sp.MaDoiTac = :maDoiTac

              AND (
                    hd.TrangThai IS NULL
                    OR LOWER(hd.TrangThai) NOT LIKE '%hủy%'
              )

            GROUP BY thoiGian
            ORDER BY MIN(hd.NgayIn)
            """, nativeQuery = true)
    List<DoanhThuTheoThoiGianProjection> getBieuDoDoiTac(
            @Param("maDoiTac") Integer maDoiTac,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay,
            @Param("kieuThongKe") String kieuThongKe
    );

    @Query(value = """
            SELECT
                sp.MaSanPham AS maSanPham,
                sp.TenSanPham AS tenSanPham,

                COALESCE(
                    SUM(ct.SoLuong),
                    0
                ) AS soLuongBan,

                COALESCE(
                    SUM(ct.SoLuong * ct.GiaTien),
                    0
                ) AS doanhThu

            FROM hoadon hd

            JOIN chitietdonhang ct
                ON ct.MaDonHang = hd.MaDonHang

            JOIN sanpham sp
                ON sp.MaSanPham = ct.MaSanPham

            WHERE hd.NgayIn BETWEEN :tuNgay AND :denNgay

              AND sp.MaDoiTac = :maDoiTac

              AND (
                    hd.TrangThai IS NULL
                    OR LOWER(hd.TrangThai) NOT LIKE '%hủy%'
              )

            GROUP BY
                sp.MaSanPham,
                sp.TenSanPham

            ORDER BY doanhThu DESC
            LIMIT 5
            """, nativeQuery = true)
    List<DoanhThuSanPhamProjection> getTopSanPhamDoiTac(
            @Param("maDoiTac") Integer maDoiTac,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay
    );

    @Query(value = """
            SELECT
                COALESCE(
                    NULLIF(hd.PhuongThucThanhToan, ''),
                    'Chưa cập nhật'
                ) AS phuongThucThanhToan,

                COUNT(
                    DISTINCT hd.MaHoaDon
                ) AS soHoaDon,

                COALESCE(
                    SUM(ct.SoLuong * ct.GiaTien),
                    0
                ) AS doanhThu

            FROM hoadon hd

            JOIN chitietdonhang ct
                ON ct.MaDonHang = hd.MaDonHang

            JOIN sanpham sp
                ON sp.MaSanPham = ct.MaSanPham

            WHERE hd.NgayIn BETWEEN :tuNgay AND :denNgay

              AND sp.MaDoiTac = :maDoiTac

              AND (
                    hd.TrangThai IS NULL
                    OR LOWER(hd.TrangThai) NOT LIKE '%hủy%'
              )

            GROUP BY phuongThucThanhToan
            ORDER BY doanhThu DESC
            """, nativeQuery = true)
    List<DoanhThuPhuongThucProjection> getPhuongThucDoiTac(
            @Param("maDoiTac") Integer maDoiTac,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay
    );

    /*
     * Hai method mới phải nằm trong interface,
     * tức là nằm trước dấu } cuối cùng.
     */

    @Query(value = """
            SELECT
                nv.MaNhanVien AS maDoiTuong,

                COALESCE(
                    NULLIF(nv.HoTen, ''),
                    nv.TenDangNhap,
                    'Chưa cập nhật'
                ) AS tenDoiTuong,

                COUNT(
                    DISTINCT hd.MaDonHang
                ) AS soDonHang,

                COALESCE(
                    SUM(hd.TongTien),
                    0
                ) AS doanhThu

            FROM hoadon hd

            JOIN donhang dh
                ON dh.MaDonHang = hd.MaDonHang

            JOIN nhanvien nv
                ON nv.MaNhanVien = dh.MaNhanVien

            WHERE hd.NgayIn BETWEEN :tuNgay AND :denNgay

              AND (
                    hd.TrangThai IS NULL
                    OR LOWER(hd.TrangThai) NOT LIKE '%hủy%'
              )

            GROUP BY
                nv.MaNhanVien,
                nv.HoTen,
                nv.TenDangNhap

            ORDER BY doanhThu DESC
            LIMIT 5
            """, nativeQuery = true)
    List<DoanhThuDoiTuongProjection> getTopNhanVienAdmin(
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay
    );

    @Query(value = """
            SELECT
                dt.MaDoiTac AS maDoiTuong,

                COALESCE(
                    NULLIF(dt.TenDoanhNghiep, ''),
                    NULLIF(dt.TenDoiTac, ''),
                    dt.TenDangNhap,
                    'Chưa cập nhật'
                ) AS tenDoiTuong,

                COUNT(
                    DISTINCT hd.MaDonHang
                ) AS soDonHang,

                COALESCE(
                    SUM(ct.SoLuong * ct.GiaTien),
                    0
                ) AS doanhThu

            FROM hoadon hd

            JOIN chitietdonhang ct
                ON ct.MaDonHang = hd.MaDonHang

            JOIN sanpham sp
                ON sp.MaSanPham = ct.MaSanPham

            JOIN doitac dt
                ON dt.MaDoiTac = sp.MaDoiTac

            WHERE hd.NgayIn BETWEEN :tuNgay AND :denNgay

              AND (
                    hd.TrangThai IS NULL
                    OR LOWER(hd.TrangThai) NOT LIKE '%hủy%'
              )

            GROUP BY
                dt.MaDoiTac,
                dt.TenDoanhNghiep,
                dt.TenDoiTac,
                dt.TenDangNhap

            ORDER BY doanhThu DESC
            LIMIT 5
            """, nativeQuery = true)
    List<DoanhThuDoiTuongProjection> getTopDoiTacAdmin(
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay
    );
}