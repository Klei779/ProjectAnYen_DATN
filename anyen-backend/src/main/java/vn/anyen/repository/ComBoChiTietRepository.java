package vn.anyen.repository;

import org.springframework.data.jpa.repository.Query;
import vn.anyen.entity.ComBoChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComBoChiTietRepository
        extends JpaRepository<ComBoChiTiet, Integer>{
    @Query("""
        SELECT c
        FROM ComBoChiTiet c
        WHERE c.comboId = :comboId
        ORDER BY c.comboChiTietId ASC
    """)
    List<ComBoChiTiet> findByComboId(Integer comboId);
}
