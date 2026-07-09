package vn.anyen.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.anyen.entity.CongNo;

import java.util.List;

@Repository
public interface CongNoRepository extends JpaRepository<CongNo, Integer> {
    
    List<CongNo> findByDoiTac_MaDoiTac(Integer maDoiTac);
    
    List<CongNo> findByDonHang_MaDonHang(Integer maDonHang);
    
    Page<CongNo> findAll(Pageable pageable);
    
    Page<CongNo> findByTrangThai(Integer trangThai, Pageable pageable);
    
    Page<CongNo> findByDoiTac_MaDoiTac(Integer maDoiTac, Pageable pageable);
}
