package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.anyen.entity.SanPham;

@Repository
public interface SanPhamDoiTacRepository extends JpaRepository<SanPham, Integer>, JpaSpecificationExecutor<SanPham> {
}
