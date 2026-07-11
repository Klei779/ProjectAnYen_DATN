package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.anyen.entity.ThongBao;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {

    /**
     * Lấy thông báo cá nhân (nguoiNhanId = id) + broadcast (nguoiNhanId IS NULL)
     * Sắp xếp mới nhất trước
     */
    @Query("SELECT t FROM ThongBao t WHERE t.nguoiNhanId = :nguoiNhanId OR t.nguoiNhanId IS NULL ORDER BY t.ngayTao DESC")
    List<ThongBao> findByNguoiNhan(@Param("nguoiNhanId") Integer nguoiNhanId);

    /**
     * Đếm thông báo chưa đọc
     */
    //test
    @Query("SELECT COUNT(t) FROM ThongBao t WHERE (t.nguoiNhanId = :nguoiNhanId OR t.nguoiNhanId IS NULL) AND t.trangThai = 0")
    long countChuaDoc(@Param("nguoiNhanId") Integer nguoiNhanId);

    @Modifying
    @Query("UPDATE ThongBao t SET t.trangThai = 1 WHERE (t.nguoiNhanId = :nguoiNhanId OR t.nguoiNhanId IS NULL) AND t.trangThai = 0")
    void markAllAsRead(@Param("nguoiNhanId") Integer nguoiNhanId);

    boolean existsByMaKhachHangAndNguoiNhanIdAndTrangThai(
            Integer maKhachHang,
            Integer nguoiNhanId,
            Integer trangThai
    );

    List<ThongBao> findByMaKhachHangOrderByNgayTaoDesc(Integer maKhachHang);

    /**
     * Lấy danh sách thông báo duyệt sản phẩm đang chờ xác nhận
     */
//    List<ThongBao> findByLoaiThongBaoAndTrangThaiOrderByNgayTaoDesc(
//            String loaiThongBao,
//            Integer trangThai
//    );
//
//    /**
//     * Tìm thông báo duyệt mới nhất theo mã sản phẩm
//     */
//    Optional<ThongBao> findFirstByMaSanPhamAndLoaiThongBaoOrderByNgayTaoDesc(
//            Integer maSanPham,
//            String loaiThongBao
//    );

    boolean existsByLoaiThongBaoAndNoiDungContainingAndTrangThai(
            String loaiThongBao,
            String noiDung,
            Integer trangThai
    );
}