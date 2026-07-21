package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.anyen.entity.TinNhanTuVan;

import java.util.List;
import java.util.Optional;

public interface TinNhanTuVanRepository extends JpaRepository<TinNhanTuVan, Long> {

    List<TinNhanTuVan> findByMaPhienOrderByCreatedAtAscMaTinNhanAsc(Long maPhien);

    boolean existsByMaPhienAndNguoiGui(Long maPhien, String nguoiGui);

    Optional<TinNhanTuVan> findFirstByMaPhienAndNguoiGuiOrderByCreatedAtDescMaTinNhanDesc(
            Long maPhien,
            String nguoiGui
    );

    @Modifying
    @Query("""
        UPDATE TinNhanTuVan t
        SET t.daDoc = true
        WHERE t.maPhien = :maPhien
          AND t.nguoiGui = :nguoiGui
          AND t.daDoc = false
    """)
    int markRead(
            @Param("maPhien") Long maPhien,
            @Param("nguoiGui") String nguoiGui
    );
}
