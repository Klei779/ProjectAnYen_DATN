package vn.anyen.repository;

import vn.anyen.entity.ComBoChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComBoChiTietRepository
        extends JpaRepository<ComBoChiTiet, Integer>{
    List<ComBoChiTiet> findByComboId(Integer comboId);
}
