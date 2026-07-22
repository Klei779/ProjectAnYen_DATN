package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.anyen.entity.ThongBaoDoiTac;

import java.util.List;
import java.util.Optional;

public interface ThongBaoDoiTacRepository
        extends JpaRepository<ThongBaoDoiTac, Integer> {

    List<ThongBaoDoiTac> findByDoiTac_MaDoiTacOrderByThoiGianTaoDesc(
            Integer maDoiTac
    );

    // Loai dùng String để khớp với cột VARCHAR trong DB
//    List<ThongBaoDoiTac>
//    findByDoiTac_MaDoiTacAndLoaiAndTrangThaiThongBaoOrderByThoiGianTaoDesc(
//            Integer maDoiTac,
//            String loai,
//            String trangThaiThongBao
//    );

    List<ThongBaoDoiTac>
    findByDoiTac_MaDoiTacAndLoaiAndTrangThaiThongBaoOrderByThoiGianXuLyDesc(
            Integer maDoiTac,
            String loai,
            Integer trangThaiThongBao
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

    boolean existsByDoiTac_MaDoiTacAndDonHang_MaDonHangAndLoaiAndTrangThaiThongBao(
            Integer maDoiTac,
            Integer maDonHang,
            String loai,
            Integer trangThaiThongBao
    );

    Optional<ThongBaoDoiTac> findByDoiTac_MaDoiTacAndDonHang_MaDonHangAndLoai(
            Integer maDoiTac,
            Integer donHang,
            String loai
    );

    List<ThongBaoDoiTac> findByDonHang_MaDonHangAndLoai(
            Integer donHang,
            String loai
    );

    List<ThongBaoDoiTac> findByDonHang_MaDonHang(Integer maDonHang);
}