package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.anyen.entity.NhanVien;

import java.util.Optional;

@Repository
public interface NhanVienRepository
        extends JpaRepository<NhanVien,Integer> {

    Optional<NhanVien> findByTenDangNhap(String tenDangNhap);

}