package vn.anyen.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.anyen.entity.PhienTuVan;

import java.util.List;
import java.util.Optional;

public interface PhienTuVanRepository extends JpaRepository<PhienTuVan, Long> {
    Optional<PhienTuVan> findByTokenPhien(String tokenPhien);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PhienTuVan p WHERE p.tokenPhien = :tokenPhien")
    Optional<PhienTuVan> findByTokenPhienForUpdate(@Param("tokenPhien") String tokenPhien);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PhienTuVan p WHERE p.maPhien = :maPhien")
    Optional<PhienTuVan> findByIdForUpdate(@Param("maPhien") Long maPhien);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p
            FROM PhienTuVan p
            WHERE p.maNhanVien IS NULL
              AND p.trangThai <> :trangThaiDaDong
            ORDER BY p.thoiGianTinNhanCuoi ASC, p.maPhien ASC
            """)
    List<PhienTuVan> findUnassignedOpenForUpdate(
            @Param("trangThaiDaDong") Integer trangThaiDaDong
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p
            FROM PhienTuVan p
            WHERE p.maNhanVien IS NOT NULL
              AND p.trangThai <> :trangThaiDaDong
            ORDER BY p.thoiGianTinNhanCuoi ASC, p.maPhien ASC
            """)
    List<PhienTuVan> findAssignedOpenForUpdate(
            @Param("trangThaiDaDong") Integer trangThaiDaDong
    );

    long countByMaNhanVienAndTrangThaiNot(
            Integer maNhanVien,
            Integer trangThai
    );

    @Query("""
            SELECT p
            FROM PhienTuVan p
            WHERE p.maNhanVien = :maNhanVien
               OR (
                    p.maNhanVien IS NULL
                    AND p.trangThai <> :trangThaiDaDong
                    AND EXISTS (
                        SELECT y.maYeuCau
                        FROM YeuCauTuVanAi y
                        WHERE y.maPhien = p.maPhien
                          AND y.daGuiHotline = true
                    )
               )
            ORDER BY p.thoiGianTinNhanCuoi DESC, p.maPhien DESC
            """)
    List<PhienTuVan> findVisibleForEmployee(
            @Param("maNhanVien") Integer maNhanVien,
            @Param("trangThaiDaDong") Integer trangThaiDaDong
    );
}
