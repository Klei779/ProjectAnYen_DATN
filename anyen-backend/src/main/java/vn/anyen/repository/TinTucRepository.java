package vn.anyen.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.anyen.entity.TinTuc;

import java.util.List;
import java.util.Optional;

public interface TinTucRepository
        extends JpaRepository<TinTuc, Integer> {

    /**
     * Website chỉ hiển thị tin có TrangThai = 1.
     */
    List<TinTuc> findAllByTrangThaiOrderByNgayDangDesc(
            Integer trangThai
    );

    /**
     * Website chỉ được xem chi tiết bài viết đang hiển thị.
     */
    Optional<TinTuc> findByMaTinTucAndTrangThai(
            Integer maTinTuc,
            Integer trangThai
    );

    Page<TinTuc> findByTrangThai(
            Integer trangThai,
            Pageable pageable
    );

    Page<TinTuc> findByLoaiTin(
            Integer loaiTin,
            Pageable pageable
    );

    Page<TinTuc> findByLoaiTinAndTrangThai(
            Integer loaiTin,
            Integer trangThai,
            Pageable pageable
    );

    Page<TinTuc> findByTieuDeContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    Page<TinTuc> findByTieuDeContainingIgnoreCaseAndTrangThai(
            String keyword,
            Integer trangThai,
            Pageable pageable
    );

    /**
     * API quản lý dành cho Admin:
     * - Tìm theo tiêu đề
     * - Lọc loại tin
     * - Lọc trạng thái
     * - Phân trang
     */
    @Query("""
            SELECT t
            FROM TinTuc t
            WHERE
                (
                    :keyword IS NULL
                    OR LOWER(t.tieuDe)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
            AND
                (
                    :loaiTin IS NULL
                    OR t.loaiTin = :loaiTin
                )
            AND
                (
                    :trangThai IS NULL
                    OR t.trangThai = :trangThai
                )
            ORDER BY t.ngayDang DESC, t.maTinTuc DESC
            """)
    Page<TinTuc> search(
            @Param("keyword") String keyword,
            @Param("loaiTin") Integer loaiTin,
            @Param("trangThai") Integer trangThai,
            Pageable pageable
    );

    /**
     * Tin mới nhất.
     */
    List<TinTuc> findTop5ByTrangThaiOrderByNgayDangDesc(
            Integer trangThai
    );

    /**
     * Tin liên quan.
     */
    List<TinTuc>
    findTop4ByLoaiTinAndTrangThaiAndMaTinTucNotOrderByNgayDangDesc(
            Integer loaiTin,
            Integer trangThai,
            Integer maTinTuc
    );
}