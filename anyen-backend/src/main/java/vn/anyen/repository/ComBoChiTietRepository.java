package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.anyen.entity.ComBoChiTiet;

import java.util.List;

public interface ComBoChiTietRepository extends JpaRepository<ComBoChiTiet, Integer> {
    @Query("""
        SELECT c
        FROM ComBoChiTiet c
        WHERE c.comboId = :comboId
        ORDER BY c.comboChiTietId ASC
    """)
    List<ComBoChiTiet> findByComboId(@Param("comboId") Integer comboId);

    @Modifying
    @Query("DELETE FROM ComBoChiTiet c WHERE c.comboId = :comboId")
    void deleteByComboId(@Param("comboId") Integer comboId);
}
