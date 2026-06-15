package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.anyen.entity.ComBoChiTietHinhAnh;

import java.util.List;

public interface ComBoChiTietHinhAnhRepository
        extends JpaRepository<ComBoChiTietHinhAnh, Integer> {

    @Query("""
        SELECT h
        FROM ComBoChiTietHinhAnh h
        WHERE h.comboChiTietId IN :ids
        ORDER BY h.comboChiTietId ASC, h.thuTu ASC
    """)
    List<ComBoChiTietHinhAnh> findByComboChiTietIds(List<Integer> ids);
}
