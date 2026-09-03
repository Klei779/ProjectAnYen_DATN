package vn.anyen.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.anyen.entity.CongNo;

import java.util.List;

@Repository
public interface CongNoRepository
        extends JpaRepository<CongNo, Integer> {

    /*
     * Lấy công nợ theo đối tác.
     */
    List<CongNo> findByDoiTac_MaDoiTac(
            Integer maDoiTac
    );


    /*
     * Lấy công nợ theo đơn hàng.
     */
    List<CongNo> findByDonHang_MaDonHang(
            Integer maDonHang
    );


    /*
     * Phân trang tất cả công nợ.
     */
    Page<CongNo> findAll(
            Pageable pageable
    );


    /*
     * Lọc theo trạng thái:
     *
     * 0 = Chưa thanh toán
     * 1 = Thanh toán một phần
     * 2 = Đã thanh toán
     * 3 = Quá hạn
     */
    Page<CongNo> findByTrangThai(
            Integer trangThai,
            Pageable pageable
    );


    /*
     * Phân trang theo đối tác.
     */
    Page<CongNo> findByDoiTac_MaDoiTac(
            Integer maDoiTac,
            Pageable pageable
    );


    /*
     * Chống tạo trùng công nợ
     * cho cùng đơn + cùng đối tác.
     */
    boolean existsByDonHang_MaDonHangAndDoiTac_MaDoiTac(
            Integer maDonHang,
            Integer maDoiTac
    );
}