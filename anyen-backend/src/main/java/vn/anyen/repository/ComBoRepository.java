package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.anyen.entity.ComBo;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComBoRepository extends JpaRepository<ComBo, Integer> {

    List<ComBo> findAllByOrderByComboIdDesc();

    List<ComBo> findByTrangThaiOrderByComboIdDesc(Integer trangThai);

    Optional<ComBo> findByComboIdAndTrangThai(Integer comboId, Integer trangThai);
}