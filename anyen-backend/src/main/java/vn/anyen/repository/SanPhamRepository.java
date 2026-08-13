package vn.anyen.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.anyen.entity.SanPham;

import java.util.List;
import java.util.Optional;

public interface SanPhamRepository
        extends JpaRepository<SanPham, Integer>, JpaSpecificationExecutor<SanPham> {

    @Query("""
SELECT sp
FROM SanPham sp, DoiTac dt
WHERE sp.trangThai = 1
  AND sp.maDoiTac = dt.maDoiTac
  AND dt.trangThai = 1
ORDER BY sp.maSanPham DESC
""")
List<SanPham> findAllVisibleForTaoDonHang();

    @Query("""
            SELECT sp.loai, COUNT(sp)
            FROM SanPham sp
            WHERE sp.loai IS NOT NULL
              AND sp.loai <> ''
              AND sp.trangThai = 1
            GROUP BY sp.loai
            ORDER BY sp.loai
            """)
    List<Object[]> countVisibleByLoai();

    @Query("""
            SELECT sp.vatLieu, COUNT(sp)
            FROM SanPham sp
            WHERE sp.vatLieu IS NOT NULL
              AND sp.vatLieu <> ''
              AND sp.trangThai = 1
            GROUP BY sp.vatLieu
            ORDER BY sp.vatLieu
            """)
    List<Object[]> countVisibleByVatLieu();

    @Query("""
            SELECT sp.tonGiao, COUNT(sp)
            FROM SanPham sp
            WHERE sp.tonGiao IS NOT NULL
              AND sp.tonGiao <> ''
              AND sp.trangThai = 1
            GROUP BY sp.tonGiao
            ORDER BY sp.tonGiao
            """)
    List<Object[]> countVisibleByTonGiao();

    @Query("""
            SELECT sp.mauSac, COUNT(sp)
            FROM SanPham sp
            WHERE sp.mauSac IS NOT NULL
              AND sp.mauSac <> ''
              AND sp.trangThai = 1
            GROUP BY sp.mauSac
            ORDER BY sp.mauSac
            """)
    List<Object[]> countVisibleByMauSac();

    /**
     * Lấy sản phẩm theo trạng thái.
     * Dùng cho chức năng nhân viên duyệt sản phẩm.
     * Entity SanPham không có createdAt nên sắp xếp theo maSanPham DESC.
     */
    List<SanPham> findByTrangThaiOrderByMaSanPhamDesc(Integer trangThai);

    List<SanPham> findByTrangThai(Integer trangThai);
    @Query("""

    SELECT dt.tenDoiTac 

    FROM DoiTac dt 

    WHERE dt.maDoiTac = :maDoiTac

""")

    String findTenDoiTacByMaDoiTac(@Param("maDoiTac") Integer maDoiTac);

    /**
     * Dùng để khôi phục liên kết sản phẩm cho các thông báo kết quả duyệt cũ
     * chưa có marker [MASP:id] trong nội dung.
     */
    Optional<SanPham> findFirstByMaDoiTacAndTenSanPhamIgnoreCaseOrderByMaSanPhamDesc(
            Integer maDoiTac,
            String tenSanPham
    );

    /**
     * Khóa sản phẩm khi nhiều đối tác cùng lúc bấm nhận đơn.
     * Người lock được trước sẽ xử lý tồn kho trước.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT sp
        FROM SanPham sp
        WHERE sp.maSanPham = :id
        """)
    Optional<SanPham> findByIdForUpdate(
            @Param("id") Integer id
    );


    /**
     * Tìm các sản phẩm đang bán của đối tác khác.
     * Dùng khi đối tác hiện tại từ chối đơn lẻ.
     */
    List<SanPham> findByTrangThaiAndMaDoiTacNot(
            Integer trangThai,
            Integer maDoiTac
    );
}
