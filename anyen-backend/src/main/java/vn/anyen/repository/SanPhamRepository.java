package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import vn.anyen.entity.SanPham;

public interface SanPhamRepository extends JpaRepository<SanPham, Integer>, JpaSpecificationExecutor<SanPham> {

    @Query(value = "SELECT COUNT(*) FROM SanPham ", nativeQuery = true)
    long countNativeSanPham();
}