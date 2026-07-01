package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.anyen.entity.NhanVien;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository
        extends JpaRepository<NhanVien,Integer> {

    Optional<NhanVien> findByTenDangNhap(String tenDangNhap);

    boolean existsByTenDangNhap(String tenDangNhap);

    boolean existsByEmail(String email);

    boolean existsBySoDienThoai(String soDienThoai);

    List<NhanVien> findByVaiTro(Integer vaiTro);
}
