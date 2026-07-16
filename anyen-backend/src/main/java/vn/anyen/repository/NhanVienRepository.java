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

    boolean existsByTenDangNhapAndMaNhanVienNot(
            String tenDangNhap,
            Integer maNhanVien
    );

    boolean existsByEmailAndMaNhanVienNot(
            String email,
            Integer maNhanVien
    );

    boolean existsBySoDienThoaiAndMaNhanVienNot(
            String soDienThoai,
            Integer maNhanVien
    );

    List<NhanVien> findByVaiTro(Integer vaiTro);

    List<NhanVien> findByVaiTroAndTrangThaiOrderByHoTenAsc(
            Integer vaiTro,
            Integer trangThai
    );

    Optional<NhanVien> findFirstByVaiTroAndTrangThai(
            Integer vaiTro,
            Integer trangThai
    );

    @Query("SELECT nv FROM NhanVien nv WHERE nv.latitude IS NOT NULL AND nv.longitude IS NOT NULL AND nv.trangThai = 1 AND nv.vaiTro = 2")
    List<NhanVien> findNhanVienTrucTiepCoToaDoDangHoatDong();
}
