package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.anyen.entity.ComBoHinhAnh;

import java.util.List;

public interface ComBoHinhAnhRepository extends JpaRepository<ComBoHinhAnh, Integer> {

    List<ComBoHinhAnh> findByComboIdOrderByThuTuAscMaHinhAnhAsc(Integer comboId);

    List<ComBoHinhAnh> findByComboIdAndLoaiHinhAnhOrderByThuTuAscMaHinhAnhAsc(
            Integer comboId,
            String loaiHinhAnh
    );

    long countByComboIdAndLoaiHinhAnh(Integer comboId, String loaiHinhAnh);

    @Modifying
    @Query("""
        DELETE FROM ComBoHinhAnh h
        WHERE h.comboId = :comboId
          AND h.loaiHinhAnh = :loaiHinhAnh
    """)
    void deleteByComboIdAndLoaiHinhAnh(
            @Param("comboId") Integer comboId,
            @Param("loaiHinhAnh") String loaiHinhAnh
    );

    @Modifying
    @Query("DELETE FROM ComBoHinhAnh h WHERE h.comboId = :comboId")
    void deleteByComboId(@Param("comboId") Integer comboId);
}