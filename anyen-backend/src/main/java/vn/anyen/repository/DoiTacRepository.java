package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.ThongBaoDoiTac;

import java.util.List;
import java.util.Optional;

import java.time.LocalDateTime;

@Repository
public interface DoiTacRepository
        extends JpaRepository<DoiTac,Integer> {
    Optional<DoiTac> findByTenDangNhap(String tenDangNhap);
    Optional<DoiTac> findByEmail(String email);
    List<DoiTac> findByTrangThaiAndCreatedAtBefore(Integer trangThai, LocalDateTime time);

    DoiTac findByTenDangNhapAndMatKhau(
            String tenDangNhap,
            String matKhau
    );
}
