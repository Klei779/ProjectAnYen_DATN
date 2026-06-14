package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.anyen.entity.ThongBaoDoiTac;

import java.util.List;
import java.util.Optional;

public interface ThongBaoDoiTacRepository
        extends JpaRepository<ThongBaoDoiTac, Integer> {

    List<ThongBaoDoiTac>
    findByDoiTac_MaDoiTacAndLoaiAndTrangThaiThongBaoOrderByThoiGianTaoDesc(
            Integer maDoiTac,
            String loai,
            String trangThaiThongBao
    );

    List<ThongBaoDoiTac>
    findByDoiTac_MaDoiTacAndLoaiAndTrangThaiThongBaoOrderByThoiGianXuLyDesc(
            Integer maDoiTac,
            String loai,
            String trangThaiThongBao
    );

    Optional<ThongBaoDoiTac> findByMaThongBaoAndDoiTac_MaDoiTac(
            Integer maThongBao,
            Integer maDoiTac
    );

    boolean existsByDoiTac_MaDoiTacAndDonHang_MaDonHangAndLoai(
            Integer maDoiTac,
            Integer maDonHang,
            String loai
    );

    Optional<ThongBaoDoiTac> findByDoiTac_MaDoiTacAndDonHang_MaDonHangAndLoai(
            Integer maDoiTac,
            Integer maDonHang,
            String loai
    );
}