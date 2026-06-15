package vn.anyen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.anyen.dto.response.DonHangResponse;
import vn.anyen.entity.DonHang;
import vn.anyen.repository.ChiTietDonHangRepository;
import vn.anyen.repository.DonHangRepository;
import vn.anyen.service.DonHangService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonHangServiceTest {

    @Mock
    private DonHangRepository donHangRepository;

    @Mock
    private ChiTietDonHangRepository chiTietDonHangRepository;

    @InjectMocks
    private DonHangService donHangService;

    private DonHang donHang;

    @BeforeEach
    void setUp() {
        donHang = new DonHang();
        donHang.setMaDonHang(1);
        donHang.setTrangThai("Mới tạo");
        donHang.setTongTien(BigDecimal.valueOf(5500000));
        donHang.setGhiChu("Ghi chú test");
    }

    @Test
    void getAllDonHang_thanhCong() {
        when(donHangRepository.findAll()).thenReturn(List.of(donHang));
        when(chiTietDonHangRepository.findByDonHang_MaDonHang(1))
                .thenReturn(List.of());

        List<DonHangResponse> result = donHangService.getAllDonHang();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Mới tạo", result.get(0).getTrangThai());
        assertEquals(BigDecimal.valueOf(5500000), result.get(0).getTongTien());

        verify(donHangRepository, times(1)).findAll();
        verify(chiTietDonHangRepository, times(1))
                .findByDonHang_MaDonHang(1);
    }

    @Test
    void getDonHangById_thanhCong() {
        when(donHangRepository.findById(1)).thenReturn(Optional.of(donHang));
        when(chiTietDonHangRepository.findByDonHang_MaDonHang(1))
                .thenReturn(List.of());

        DonHangResponse result = donHangService.getDonHangById(1);

        assertNotNull(result);
        assertEquals("DH0001", result.getMaCode());
        assertEquals("Mới tạo", result.getTrangThai());
        assertEquals(BigDecimal.valueOf(5500000), result.getTongTien());

        verify(donHangRepository, times(1)).findById(1);
    }

    @Test
    void getDonHangById_khongTimThay_thiNemLoi() {
        when(donHangRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donHangService.getDonHangById(99)
        );

        assertEquals("Không tìm thấy đơn hàng #99", exception.getMessage());

        verify(donHangRepository, times(1)).findById(99);
        verifyNoInteractions(chiTietDonHangRepository);
    }

    @Test
    void capNhatTrangThai_chuyenDungTrangThaiTiepTheo_thanhCong() {
        when(donHangRepository.findById(1)).thenReturn(Optional.of(donHang));
        when(donHangRepository.save(donHang)).thenReturn(donHang);
        when(chiTietDonHangRepository.findByDonHang_MaDonHang(1))
                .thenReturn(List.of());

        DonHangResponse result = donHangService.capNhatTrangThai(1, "Đã xác nhận");

        assertNotNull(result);
        assertEquals("Đã xác nhận", result.getTrangThai());

        verify(donHangRepository, times(1)).findById(1);
        verify(donHangRepository, times(1)).save(donHang);
    }

    @Test
    void capNhatTrangThai_nhayCocTrangThai_thiNemLoi() {
        when(donHangRepository.findById(1)).thenReturn(Optional.of(donHang));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donHangService.capNhatTrangThai(1, "Đang xử lý")
        );

        assertTrue(exception.getMessage().contains("Chỉ có thể chuyển sang trạng thái tiếp theo"));

        verify(donHangRepository, times(1)).findById(1);
        verify(donHangRepository, never()).save(any());
    }

    @Test
    void capNhatTrangThai_trangThaiKhongHopLe_thiNemLoi() {
        when(donHangRepository.findById(1)).thenReturn(Optional.of(donHang));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donHangService.capNhatTrangThai(1, "Sai trạng thái")
        );

        assertEquals("Trạng thái 'Sai trạng thái' không hợp lệ.", exception.getMessage());

        verify(donHangRepository, times(1)).findById(1);
        verify(donHangRepository, never()).save(any());
    }

    @Test
    void capNhatTrangThai_donHangDaHuy_thiKhongChoCapNhat() {
        donHang.setTrangThai("Đã hủy");

        when(donHangRepository.findById(1)).thenReturn(Optional.of(donHang));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donHangService.capNhatTrangThai(1, "Đã xác nhận")
        );

        assertEquals("Đơn hàng đã bị hủy, không thể cập nhật trạng thái.", exception.getMessage());

        verify(donHangRepository, times(1)).findById(1);
        verify(donHangRepository, never()).save(any());
    }

    @Test
    void huyDonHang_thanhCong() {
        when(donHangRepository.findById(1)).thenReturn(Optional.of(donHang));
        when(donHangRepository.save(donHang)).thenReturn(donHang);
        when(chiTietDonHangRepository.findByDonHang_MaDonHang(1))
                .thenReturn(List.of());

        DonHangResponse result = donHangService.huyDonHang(1);

        assertNotNull(result);
        assertEquals("Đã hủy", result.getTrangThai());

        verify(donHangRepository, times(1)).findById(1);
        verify(donHangRepository, times(1)).save(donHang);
    }

    @Test
    void capNhatTrangThaiNhanVien_choPhepCapNhatTrucTiep_thanhCong() {
        when(donHangRepository.findById(1)).thenReturn(Optional.of(donHang));
        when(donHangRepository.save(donHang)).thenReturn(donHang);
        when(chiTietDonHangRepository.findByDonHang_MaDonHang(1))
                .thenReturn(List.of());

        DonHangResponse result = donHangService.capNhatTrangThaiNhanVien(1, "Hoàn thành");

        assertNotNull(result);
        assertEquals("Hoàn thành", result.getTrangThai());

        verify(donHangRepository, times(1)).findById(1);
        verify(donHangRepository, times(1)).save(donHang);
    }

    @Test
    void capNhatTrangThaiNhanVien_trangThaiRong_thiNemLoi() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donHangService.capNhatTrangThaiNhanVien(1, "")
        );

        assertEquals("Trạng thái đơn hàng không được để trống.", exception.getMessage());

        verifyNoInteractions(donHangRepository);
    }

    @Test
    void capNhatTrangThaiNhanVien_trangThaiKhongHopLe_thiNemLoi() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donHangService.capNhatTrangThaiNhanVien(1, "Đang giao hàng")
        );

        assertEquals("Trạng thái 'Đang giao hàng' không hợp lệ.", exception.getMessage());

        verifyNoInteractions(donHangRepository);
    }
}