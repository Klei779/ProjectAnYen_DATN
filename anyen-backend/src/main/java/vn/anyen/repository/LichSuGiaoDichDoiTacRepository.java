package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.anyen.entity.LichSuGiaoDichDoiTac;

import java.util.List;

@Repository
public interface LichSuGiaoDichDoiTacRepository
        extends JpaRepository<LichSuGiaoDichDoiTac, Integer> {

    List<LichSuGiaoDichDoiTac> findByDoiTac_MaDoiTacOrderByThoiGianDesc(
            Integer maDoiTac
    );
}
