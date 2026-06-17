package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.anyen.entity.HDongCT;

import java.util.Optional;

public interface HDongCTRepository extends JpaRepository<HDongCT, Integer> {

    Optional<HDongCT> findFirstByHopDong_MaHopDong(Integer maHopDong);
}