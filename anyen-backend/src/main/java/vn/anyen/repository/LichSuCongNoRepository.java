package vn.anyen.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.anyen.entity.LichSuCongNo;

import java.util.List;

@Repository
public interface LichSuCongNoRepository
        extends JpaRepository<LichSuCongNo, Integer> {

    List<LichSuCongNo> findByCongNo_MaCongNo(
            Integer maCongNo
    );

    Page<LichSuCongNo> findAll(Pageable pageable);

    Page<LichSuCongNo> findAllByOrderByNgayThanhToanDesc(
            Pageable pageable
    );
}