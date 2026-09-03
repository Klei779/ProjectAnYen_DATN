package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.anyen.entity.DamBaoDonHangDoiTac;

import java.util.List;
import java.util.Optional;

public interface DamBaoDonHangDoiTacRepository
        extends JpaRepository<
        DamBaoDonHangDoiTac,
        Integer
        > {

    Optional<DamBaoDonHangDoiTac>
    findByMaDonHangAndMaDoiTac(
            Integer maDonHang,
            Integer maDoiTac
    );


    List<DamBaoDonHangDoiTac>
    findByMaDonHang(
            Integer maDonHang
    );
}