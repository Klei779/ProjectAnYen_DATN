package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.anyen.entity.ThongBao;

import java.util.List;

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
    @Query("SELECT COUNT(t) FROM ThongBao t WHERE (t.nguoiNhanId = :nguoiNhanId OR t.nguoiNhanId IS NULL) AND t.trangThai = 'CHUA_DOC'")
    long countChuaDoc(@Param("nguoiNhanId") Integer nguoiNhanId);

    @Modifying
    @Query("UPDATE ThongBao t SET t.trangThai = 'DA_DOC' WHERE (t.nguoiNhanId = :nguoiNhanId OR t.nguoiNhanId IS NULL) AND t.trangThai = 'CHUA_DOC'")
    void markAllAsRead(@Param("nguoiNhanId") Integer nguoiNhanId);
}
