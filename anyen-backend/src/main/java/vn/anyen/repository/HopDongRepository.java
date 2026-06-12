package vn.anyen.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.anyen.entity.HopDong;

public interface HopDongRepository extends JpaRepository<HopDong, Integer> {

    @Query("""
        SELECT hd
        FROM HopDong hd
        LEFT JOIN hd.donHang dh
        LEFT JOIN dh.khachHang kh
        WHERE
            (
                :keyword IS NULL
                OR :keyword = ''
                OR LOWER(kh.tenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR kh.soDienThoai LIKE CONCAT('%', :keyword, '%')
                OR CAST(hd.maHopDong AS string) LIKE CONCAT('%', :keyword, '%')
                OR CAST(dh.maDonHang AS string) LIKE CONCAT('%', :keyword, '%')
            )
        AND
            (
                :trangThai IS NULL
                OR :trangThai = ''
                OR :trangThai = 'Tất cả'
                OR hd.trangThai = :trangThai
            )
        """)
    Page<HopDong> searchHopDong(
            @Param("keyword") String keyword,
            @Param("trangThai") String trangThai,
            Pageable pageable
    );
}