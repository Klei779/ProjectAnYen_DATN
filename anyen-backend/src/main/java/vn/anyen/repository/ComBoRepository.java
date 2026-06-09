package vn.anyen.repository;

import vn.anyen.entity.ComBo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComBoRepository extends JpaRepository<ComBo, Integer> {

}
