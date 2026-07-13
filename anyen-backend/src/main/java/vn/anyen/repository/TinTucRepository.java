package vn.anyen.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.anyen.entity.TinTuc;

import java.util.List;

public interface TinTucRepository extends JpaRepository<TinTuc, Integer> {


    /**
     * Hiển thị tất cả theo trạng thái
     */
    Page<TinTuc> findByTrangThai(Integer trangThai, Pageable pageable);


    /**
     * Lọc theo loại tin
     */
    Page<TinTuc> findByLoaiTin(Integer loaiTin, Pageable pageable);


    /**
     * Lọc theo loại + trạng thái
     */
    Page<TinTuc> findByLoaiTinAndTrangThai(
            Integer loaiTin,
            Integer trangThai,
            Pageable pageable
    );


    /**
     * Tìm theo tiêu đề
     */
    Page<TinTuc> findByTieuDeContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );


    /**
     * Tìm theo tiêu đề + trạng thái
     */
    Page<TinTuc> findByTieuDeContainingIgnoreCaseAndTrangThai(
            String keyword,
            Integer trangThai,
            Pageable pageable
    );


    /**
     * Tìm kiếm tổng hợp
     */
    @Query("""
            SELECT t
            FROM TinTuc t
            WHERE
                (:keyword IS NULL OR LOWER(t.tieuDe) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND
                (:loaiTin IS NULL OR t.loaiTin = :loaiTin)
            AND
                (:trangThai IS NULL OR t.trangThai = :trangThai)
            ORDER BY t.ngayDang DESC
            """)
    Page<TinTuc> search(
            @Param("keyword") String keyword,
            @Param("loaiTin") Integer loaiTin,
            @Param("trangThai") Integer trangThai,
            Pageable pageable
    );


    /**
     * Tin mới nhất
     */
    List<TinTuc> findTop5ByTrangThaiOrderByNgayDangDesc(
            Integer trangThai
    );


    /**
     * Tin liên quan
     */
    List<TinTuc> findTop4ByLoaiTinAndTrangThaiAndMaTinTucNotOrderByNgayDangDesc(
            Integer loaiTin,
            Integer trangThai,
            Integer maTinTuc
    );

}