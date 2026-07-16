package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.anyen.entity.DoiTac;

import java.util.List;
import java.util.Optional;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
@Repository
public interface DoiTacRepository extends JpaRepository<DoiTac, Integer> {

    Optional<DoiTac> findByTenDangNhap(String tenDangNhap);
    Optional<DoiTac> findByEmail(String email);
    List<DoiTac> findByTrangThaiAndCreatedAtBefore(Integer trangThai, LocalDateTime time);

    DoiTac findByTenDangNhapAndMatKhau(
            String tenDangNhap,
            String matKhau
    );

    Optional<DoiTac> findByConfirmationToken(String confirmationToken);

    boolean existsByTenDangNhap(String tenDangNhap);

    boolean existsByEmail(String email);

    boolean existsBySoDienThoai(String soDienThoai);

    boolean existsByMaSoThue(String maSoThue);

    List<DoiTac> findAllByOrderByMaDoiTacDesc();

    boolean existsByEmailAndMaDoiTacNot(String email, Integer maDoiTac);

    boolean existsBySoDienThoaiAndMaDoiTacNot(String soDienThoai, Integer maDoiTac);

    boolean existsByMaSoThueAndMaDoiTacNot(String maSoThue, Integer maDoiTac);
    @Query(
            value = """
                SELECT COUNT(*)
                FROM chitietdonhang ctdh
                INNER JOIN sanpham sp
                    ON sp.MaSanPham = ctdh.MaSanPham
                WHERE sp.MaDoiTac = :maDoiTac
                """,
            nativeQuery = true
    )
    long countChiTietDonHangByMaDoiTac(
            @Param("maDoiTac") Integer maDoiTac
    );
}