package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.anyen.entity.GoiDichVu;

@Repository
public interface GoiDichVuRepository
        extends JpaRepository<GoiDichVu, Integer> {
}