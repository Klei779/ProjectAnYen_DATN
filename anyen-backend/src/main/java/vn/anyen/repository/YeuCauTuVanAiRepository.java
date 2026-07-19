package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.anyen.entity.YeuCauTuVanAi;

import java.util.Optional;

public interface YeuCauTuVanAiRepository
        extends JpaRepository<YeuCauTuVanAi, Long> {

    Optional<YeuCauTuVanAi> findByMaPhien(
            Long maPhien
    );
}