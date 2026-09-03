package vn.anyen.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.anyen.entity.HoaDon;
import vn.anyen.repository.projection.HoaDonCuaToiProjection;

import java.time.LocalDate;

public interface HoaDonCuaToiRepository extends JpaRepository<HoaDon, Integer> {

    @Query(value = """
            SELECT
                hd.MaHoaDon AS maHoaDon,
                hd.MaDonHang AS maDonHang,
                kh.TenKhachHang AS tenKhachHang,
                kh.SoDienThoai AS soDienThoai,
                kh.Email AS email,
                kh.DiaChi AS diaChi,
                nv.HoTen AS tenNhanVien,
                hd.NgayIn AS ngayIn,
                hd.TongTien AS tongTien,
                hd.PhuongThucThanhToan AS phuongThucThanhToan,
                hd.TrangThai AS trangThai
            FROM hoadon hd
            JOIN donhang dh ON dh.MaDonHang = hd.MaDonHang
            LEFT JOIN khachhang kh ON kh.MaKhachHang = dh.MaKhachHang
            LEFT JOIN nhanvien nv ON nv.MaNhanVien = dh.MaNhanVien
            WHERE (:maNhanVien IS NULL OR dh.MaNhanVien = :maNhanVien)
              AND (:keyword IS NULL OR :keyword = ''
                    OR CAST(hd.MaHoaDon AS CHAR) LIKE CONCAT('%', :keyword, '%')
                    OR CAST(hd.MaDonHang AS CHAR) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(kh.TenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR kh.SoDienThoai LIKE CONCAT('%', :keyword, '%'))
              AND (:trangThai IS NULL OR :trangThai = '' OR :trangThai = 'Tất cả' OR hd.TrangThai = :trangThai)
              AND (:phuongThucThanhToan IS NULL OR :phuongThucThanhToan = '' OR :phuongThucThanhToan = 'Tất cả'
                    OR hd.PhuongThucThanhToan = :phuongThucThanhToan)
              AND (:tuNgay IS NULL OR hd.NgayIn >= :tuNgay)
              AND (:denNgay IS NULL OR hd.NgayIn <= :denNgay)
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM hoadon hd
            JOIN donhang dh ON dh.MaDonHang = hd.MaDonHang
            LEFT JOIN khachhang kh ON kh.MaKhachHang = dh.MaKhachHang
            WHERE (:maNhanVien IS NULL OR dh.MaNhanVien = :maNhanVien)
              AND (:keyword IS NULL OR :keyword = ''
                    OR CAST(hd.MaHoaDon AS CHAR) LIKE CONCAT('%', :keyword, '%')
                    OR CAST(hd.MaDonHang AS CHAR) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(kh.TenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR kh.SoDienThoai LIKE CONCAT('%', :keyword, '%'))
              AND (:trangThai IS NULL OR :trangThai = '' OR :trangThai = 'Tất cả' OR hd.TrangThai = :trangThai)
              AND (:phuongThucThanhToan IS NULL OR :phuongThucThanhToan = '' OR :phuongThucThanhToan = 'Tất cả'
                    OR hd.PhuongThucThanhToan = :phuongThucThanhToan)
              AND (:tuNgay IS NULL OR hd.NgayIn >= :tuNgay)
              AND (:denNgay IS NULL OR hd.NgayIn <= :denNgay)
            """,
            nativeQuery = true)
    Page<HoaDonCuaToiProjection> findHoaDonCuaToi(
            @Param("maNhanVien") Integer maNhanVien,
            @Param("keyword") String keyword,
            @Param("trangThai") String trangThai,
            @Param("phuongThucThanhToan") String phuongThucThanhToan,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay,
            Pageable pageable
    );

    @Query(value = """
            SELECT
                hd.MaHoaDon AS maHoaDon,
                hd.MaDonHang AS maDonHang,
                kh.TenKhachHang AS tenKhachHang,
                kh.SoDienThoai AS soDienThoai,
                kh.Email AS email,
                kh.DiaChi AS diaChi,
                nv.HoTen AS tenNhanVien,
                hd.NgayIn AS ngayIn,
                hd.TongTien AS tongTien,
                hd.PhuongThucThanhToan AS phuongThucThanhToan,
                hd.TrangThai AS trangThai
            FROM hoadon hd
            JOIN donhang dh ON dh.MaDonHang = hd.MaDonHang
            LEFT JOIN khachhang kh ON kh.MaKhachHang = dh.MaKhachHang
            LEFT JOIN nhanvien nv ON nv.MaNhanVien = dh.MaNhanVien
            WHERE LOWER(nv.TenDangNhap) = 'website'
              AND EXISTS (
                  SELECT 1
                  FROM chitietdonhang ctdh
                  JOIN sanpham sp ON sp.MaSanPham = ctdh.MaSanPham
                  WHERE ctdh.MaDonHang = dh.MaDonHang
                    AND sp.MaDoiTac = :maDoiTac
              )
              AND (:keyword IS NULL OR :keyword = ''
                    OR CAST(hd.MaHoaDon AS CHAR) LIKE CONCAT('%', :keyword, '%')
                    OR CAST(hd.MaDonHang AS CHAR) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(kh.TenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR kh.SoDienThoai LIKE CONCAT('%', :keyword, '%'))
              AND (:trangThai IS NULL OR :trangThai = '' OR :trangThai = 'Tất cả' OR hd.TrangThai = :trangThai)
              AND (:phuongThucThanhToan IS NULL OR :phuongThucThanhToan = '' OR :phuongThucThanhToan = 'Tất cả'
                    OR hd.PhuongThucThanhToan = :phuongThucThanhToan)
              AND (:tuNgay IS NULL OR hd.NgayIn >= :tuNgay)
              AND (:denNgay IS NULL OR hd.NgayIn <= :denNgay)
            ORDER BY hd.MaHoaDon DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM hoadon hd
            JOIN donhang dh ON dh.MaDonHang = hd.MaDonHang
            LEFT JOIN khachhang kh ON kh.MaKhachHang = dh.MaKhachHang
            LEFT JOIN nhanvien nv ON nv.MaNhanVien = dh.MaNhanVien
            WHERE LOWER(nv.TenDangNhap) = 'website'
              AND EXISTS (
                  SELECT 1
                  FROM chitietdonhang ctdh
                  JOIN sanpham sp ON sp.MaSanPham = ctdh.MaSanPham
                  WHERE ctdh.MaDonHang = dh.MaDonHang
                    AND sp.MaDoiTac = :maDoiTac
              )
              AND (:keyword IS NULL OR :keyword = ''
                    OR CAST(hd.MaHoaDon AS CHAR) LIKE CONCAT('%', :keyword, '%')
                    OR CAST(hd.MaDonHang AS CHAR) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(kh.TenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR kh.SoDienThoai LIKE CONCAT('%', :keyword, '%'))
              AND (:trangThai IS NULL OR :trangThai = '' OR :trangThai = 'Tất cả' OR hd.TrangThai = :trangThai)
              AND (:phuongThucThanhToan IS NULL OR :phuongThucThanhToan = '' OR :phuongThucThanhToan = 'Tất cả'
                    OR hd.PhuongThucThanhToan = :phuongThucThanhToan)
              AND (:tuNgay IS NULL OR hd.NgayIn >= :tuNgay)
              AND (:denNgay IS NULL OR hd.NgayIn <= :denNgay)
            """,
            nativeQuery = true)
    Page<HoaDonCuaToiProjection> findHoaDonOnlineDoiTac(
            @Param("maDoiTac") Integer maDoiTac,
            @Param("keyword") String keyword,
            @Param("trangThai") String trangThai,
            @Param("phuongThucThanhToan") String phuongThucThanhToan,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay,
            Pageable pageable
    );
}