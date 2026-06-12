package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.anyen.entity.KhachHang;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
}