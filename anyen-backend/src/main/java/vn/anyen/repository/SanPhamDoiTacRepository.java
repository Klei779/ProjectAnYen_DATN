package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.anyen.entity.SanPham;

import java.util.Optional;

public interface SanPhamDoiTacRepository
        extends JpaRepository<SanPham, Integer>, JpaSpecificationExecutor<SanPham> {

    Optional<SanPham> findByMaSanPhamAndMaDoiTac(
            Integer maSanPham,
            Integer maDoiTac
    );
}