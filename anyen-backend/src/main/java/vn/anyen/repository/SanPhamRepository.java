package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.anyen.entity.SanPham;

import java.util.List;

public interface SanPhamRepository
        extends JpaRepository<SanPham, Integer>, JpaSpecificationExecutor<SanPham> {

    @Query("""
            SELECT sp
            FROM SanPham sp
            WHERE sp.trangThai = 1
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
    @Query("""

    SELECT dt.tenDoiTac 

    FROM DoiTac dt 

    WHERE dt.maDoiTac = :maDoiTac

""")

    String findTenDoiTacByMaDoiTac(@Param("maDoiTac") Integer maDoiTac);


}
