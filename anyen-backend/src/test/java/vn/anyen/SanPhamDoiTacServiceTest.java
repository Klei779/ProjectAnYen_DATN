package vn.anyen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import vn.anyen.dto.SanPhamDoiTacPageResponse;
import vn.anyen.dto.SanPhamDoiTacResponse;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.SanPham;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.SanPhamDoiTacRepository;
import vn.anyen.service.SanPhamDoiTacService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SanPhamDoiTacServiceTest {

    @Mock
    private SanPhamDoiTacRepository sanPhamDoiTacRepository;

    @Mock
    private DoiTacRepository doiTacRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SanPhamDoiTacService sanPhamDoiTacService;

    private DoiTac doiTac;
    private SanPham sanPham1;
    private SanPham sanPham2;

    @BeforeEach
    void setUp() {
        doiTac = new DoiTac();
        doiTac.setMaDoiTac(1);
        doiTac.setTenDangNhap("anphuc");

        sanPham1 = new SanPham();
        sanPham1.setMaSanPham(1);
        sanPham1.setTenSanPham("Quan tài gỗ cao cấp");
        sanPham1.setLoai("Quan tài");
        sanPham1.setTrangThai("Đang bán");
        sanPham1.setMaDoiTac(1);

        sanPham2 = new SanPham();
        sanPham2.setMaSanPham(2);
        sanPham2.setTenSanPham("Bàn thờ gỗ");
        sanPham2.setLoai("Bàn thờ");
        sanPham2.setTrangThai("Ẩn");
        sanPham2.setMaDoiTac(1);
    }

    @Test
    void getSanPhamDoiTac_timSanPhamTheoTenMaDanhMucTrangThai_thanhCong() {
        // Giả lập tài khoản đối tác đang đăng nhập
        when(authentication.getName()).thenReturn("anphuc");

        // Giả lập tìm thấy đối tác trong database
        when(doiTacRepository.findByTenDangNhap("anphuc"))
                .thenReturn(Optional.of(doiTac));

        // Giả lập database trả về danh sách sản phẩm
        Page<SanPham> pageResult = new PageImpl<>(
                List.of(sanPham1, sanPham2),
                PageRequest.of(0, 10),
                2
        );

        when(sanPhamDoiTacRepository.findAll(
                ArgumentMatchers.<Specification<SanPham>>any(),
                any(Pageable.class)
        )).thenReturn(pageResult);

        // Gọi hàm cần test
        SanPhamDoiTacPageResponse result = sanPhamDoiTacService.getSanPhamDoiTac(
                authentication,
                null,       // keyword
                null,       // loai
                null,       // vatLieu
                null,       // tonGiao
                null,       // mauSac
                null,       // trangThai
                null,       // minPrice
                null,       // maxPrice
                null,       // sortBy
                0,          // page
                10          // pageSize
        );

        // In kết quả ra console
        System.out.println("===== KẾT QUẢ TEST TÌM SẢN PHẨM ĐỐI TÁC =====");
        System.out.println("Tổng số sản phẩm: " + result.getTotal());

        for (SanPhamDoiTacResponse item : result.getItems()) {
            System.out.println("--------------------------------");
            System.out.println("Mã sản phẩm: " + item.getMaSanPham());
            System.out.println("Tên sản phẩm: " + item.getTenSanPham());
            System.out.println("Danh mục: " + item.getLoai());
            System.out.println("Trạng thái: " + item.getTrangThai());
        }

        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getItems().size());

        SanPhamDoiTacResponse item1 = result.getItems().get(0);
        assertEquals(1, item1.getMaSanPham());
        assertEquals("Quan tài gỗ cao cấp", item1.getTenSanPham());
        assertEquals("Quan tài", item1.getLoai());
        assertEquals("Đang bán", item1.getTrangThai());

        SanPhamDoiTacResponse item2 = result.getItems().get(1);
        assertEquals(2, item2.getMaSanPham());
        assertEquals("Bàn thờ gỗ", item2.getTenSanPham());
        assertEquals("Bàn thờ", item2.getLoai());
        assertEquals("Ẩn", item2.getTrangThai());

        // Kiểm tra repository có được gọi đúng không
        verify(doiTacRepository, times(1)).findByTenDangNhap("anphuc");
        verify(sanPhamDoiTacRepository, times(1))
                .findAll(
                        ArgumentMatchers.<Specification<SanPham>>any(),
                        any(Pageable.class)
                );
    }
}