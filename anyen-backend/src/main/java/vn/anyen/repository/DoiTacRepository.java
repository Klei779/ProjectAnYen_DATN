package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.anyen.entity.DoiTac;

import java.util.Optional;

@Repository
public interface DoiTacRepository
        extends JpaRepository<DoiTac,Integer> {

    Optional<DoiTac> findByTenDangNhap(String tenDangNhap);

    DoiTac findByTenDangNhapAndMatKhau(
            String tenDangNhap,
            String matKhau
    );
}
