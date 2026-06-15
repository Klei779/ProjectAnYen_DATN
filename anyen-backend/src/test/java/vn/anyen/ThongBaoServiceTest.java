package vn.anyen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.anyen.dto.response.ThongBaoResponse;
import vn.anyen.entity.KhachHang;
import vn.anyen.entity.NhanVien;
import vn.anyen.entity.ThongBao;
import vn.anyen.repository.KhachHangRepository;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.ThongBaoRepository;
import vn.anyen.service.ThongBaoService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThongBaoServiceTest {

    @Mock
    private ThongBaoRepository thongBaoRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private NhanVienRepository nhanVienRepository;

    @InjectMocks
    private ThongBaoService thongBaoService;

    private ThongBao thongBaoCongViec;
    private KhachHang khachHang;
    private NhanVien nguoiGui;
    private NhanVien nguoiNhan;

    @BeforeEach
    void setUp() {
        thongBaoCongViec = new ThongBao();
        thongBaoCongViec.setMaThongBao(1);
        thongBaoCongViec.setTieuDe("Thông báo công việc mới");
        thongBaoCongViec.setNoiDung("Bạn được phân công tiếp nhận khách hàng mới");
        thongBaoCongViec.setLoaiThongBao("CONG_VIEC");
        thongBaoCongViec.setTrangThai("CHUA_DOC");
        thongBaoCongViec.setNguoiGuiId(100);
        thongBaoCongViec.setNguoiNhanId(200);
        thongBaoCongViec.setMaKhachHang(10);
        thongBaoCongViec.setNgayTao(LocalDateTime.of(2026, 6, 15, 14, 30));

        khachHang = new KhachHang();
        khachHang.setMaKhachHang(10);
        khachHang.setTenKhachHang("Nguyễn Văn An");
        khachHang.setSoDienThoai("0901111111");
        khachHang.setEmail("an@gmail.com");
        khachHang.setDiaChi("TP.HCM");
        khachHang.setCccd("079201000001");
        khachHang.setNguonDangKy("Website");
        khachHang.setNhuCauHoTro("Tư vấn dịch vụ mai táng");
        khachHang.setGhiChu("Khách cần hỗ trợ gấp");
        khachHang.setNgayDangKy(LocalDateTime.of(2026, 6, 15, 13, 0));

        nguoiGui = new NhanVien();
        nguoiGui.setMaNhanVien(100);
        nguoiGui.setHoTen("Nhân viên Hotline");

        nguoiNhan = new NhanVien();
        nguoiNhan.setMaNhanVien(200);
        nguoiNhan.setHoTen("Nhân viên Phụ Trách");
    }

    @Test
    void getThongBaoByNguoiNhan_layThongBaoCongViecTheoIdNguoiNhan_thanhCong() {
        Integer nguoiNhanId = 200;

        when(thongBaoRepository.findByNguoiNhan(nguoiNhanId))
                .thenReturn(List.of(thongBaoCongViec));

        when(nhanVienRepository.findById(100))
                .thenReturn(Optional.of(nguoiGui));

        when(khachHangRepository.findById(10))
                .thenReturn(Optional.of(khachHang));

        List<ThongBaoResponse> result = thongBaoService.getThongBaoByNguoiNhan(nguoiNhanId);

        System.out.println("===== TEST LẤY THÔNG BÁO THEO ID NGƯỜI NHẬN =====");
        System.out.println("Số lượng thông báo: " + result.size());

        for (ThongBaoResponse item : result) {
            System.out.println("--------------------------------");
            System.out.println("Mã thông báo: " + item.getMaThongBao());
            System.out.println("Tiêu đề: " + item.getTieuDe());
            System.out.println("Nội dung: " + item.getNoiDung());
            System.out.println("Loại thông báo: " + item.getLoaiThongBao());
            System.out.println("Trạng thái: " + item.getTrangThai());
            System.out.println("Người gửi ID: " + item.getNguoiGuiId());
            System.out.println("Tên người gửi: " + item.getTenNguoiGui());
            System.out.println("Mã khách hàng: " + item.getMaKhachHang());
            System.out.println("Tên khách hàng: " + item.getTenKhachHang());
            System.out.println("Số điện thoại: " + item.getSoDienThoai());
            System.out.println("Ngày tạo: " + item.getNgayTao());
        }

        assertNotNull(result);
        assertEquals(1, result.size());

        ThongBaoResponse response = result.get(0);

        assertEquals(1, response.getMaThongBao());
        assertEquals("Thông báo công việc mới", response.getTieuDe());
        assertEquals("Bạn được phân công tiếp nhận khách hàng mới", response.getNoiDung());
        assertEquals("CONG_VIEC", response.getLoaiThongBao());
        assertEquals("CHUA_DOC", response.getTrangThai());
        assertEquals(100, response.getNguoiGuiId());
        assertEquals("Nhân viên Hotline", response.getTenNguoiGui());
        assertEquals(10, response.getMaKhachHang());
        assertEquals("Nguyễn Văn An", response.getTenKhachHang());
        assertEquals("0901111111", response.getSoDienThoai());
        assertEquals("15/06/2026 - 14:30", response.getNgayTao());

        verify(thongBaoRepository, times(1)).findByNguoiNhan(nguoiNhanId);
        verify(nhanVienRepository, times(1)).findById(100);
        verify(khachHangRepository, times(1)).findById(10);
    }

    @Test
    void chapNhan_thongBaoCongViec_thanhCong() {
        Integer maThongBao = 1;
        Integer nguoiNhanId = 200;

        when(thongBaoRepository.findById(maThongBao))
                .thenReturn(Optional.of(thongBaoCongViec));

        when(khachHangRepository.findById(10))
                .thenReturn(Optional.of(khachHang));

        thongBaoService.chapNhan(maThongBao, nguoiNhanId);

        System.out.println("===== TEST CHẤP NHẬN THÔNG BÁO CÔNG VIỆC =====");
        System.out.println("Mã thông báo: " + thongBaoCongViec.getMaThongBao());
        System.out.println("Trạng thái sau khi chấp nhận: " + thongBaoCongViec.getTrangThai());
        System.out.println("Mã khách hàng: " + khachHang.getMaKhachHang());
        System.out.println("Nhân viên phụ trách sau khi nhận: " + khachHang.getMaNhanVienPhuTrach());

        assertEquals("DA_CHAP_NHAN", thongBaoCongViec.getTrangThai());
        assertEquals(200, khachHang.getMaNhanVienPhuTrach());

        verify(thongBaoRepository, times(1)).findById(maThongBao);
        verify(thongBaoRepository, times(1)).save(thongBaoCongViec);
        verify(khachHangRepository, times(1)).findById(10);
        verify(khachHangRepository, times(1)).save(khachHang);
    }

    @Test
    void chapNhan_thongBaoDaXuLy_thiBaoLoi() {
        thongBaoCongViec.setTrangThai("DA_CHAP_NHAN");

        when(thongBaoRepository.findById(1))
                .thenReturn(Optional.of(thongBaoCongViec));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> thongBaoService.chapNhan(1, 200)
        );

        System.out.println("===== TEST CHẤP NHẬN THÔNG BÁO ĐÃ XỬ LÝ =====");
        System.out.println("Lỗi: " + exception.getMessage());

        assertEquals("Thông báo này đã được xử lý", exception.getMessage());

        verify(thongBaoRepository, times(1)).findById(1);
        verify(thongBaoRepository, never()).save(any());
        verify(khachHangRepository, never()).save(any());
    }

    @Test
    void tuChoi_thongBaoCongViec_thanhCong() {
        Integer maThongBao = 1;
        Integer nguoiNhanId = 200;
        String lyDoTuChoi = "Đang bận xử lý khách hàng khác";

        when(thongBaoRepository.findById(maThongBao))
                .thenReturn(Optional.of(thongBaoCongViec));

        when(nhanVienRepository.findById(nguoiNhanId))
                .thenReturn(Optional.of(nguoiNhan));

        when(khachHangRepository.findById(10))
                .thenReturn(Optional.of(khachHang));

        thongBaoService.tuChoi(maThongBao, nguoiNhanId, lyDoTuChoi);

        ArgumentCaptor<ThongBao> thongBaoCaptor = ArgumentCaptor.forClass(ThongBao.class);

        verify(thongBaoRepository, times(2)).save(thongBaoCaptor.capture());

        List<ThongBao> savedList = thongBaoCaptor.getAllValues();

        ThongBao thongBaoGocDaTuChoi = savedList.get(0);
        ThongBao thongBaoPhanHoi = savedList.get(1);

        System.out.println("===== TEST TỪ CHỐI THÔNG BÁO CÔNG VIỆC =====");
        System.out.println("Thông báo gốc:");
        System.out.println("Mã thông báo: " + thongBaoGocDaTuChoi.getMaThongBao());
        System.out.println("Trạng thái: " + thongBaoGocDaTuChoi.getTrangThai());
        System.out.println("Lý do từ chối: " + thongBaoGocDaTuChoi.getLyDoTuChoi());

        System.out.println("--------------------------------");
        System.out.println("Thông báo phản hồi gửi về hotline:");
        System.out.println("Tiêu đề: " + thongBaoPhanHoi.getTieuDe());
        System.out.println("Nội dung: " + thongBaoPhanHoi.getNoiDung());
        System.out.println("Loại thông báo: " + thongBaoPhanHoi.getLoaiThongBao());
        System.out.println("Người gửi ID: " + thongBaoPhanHoi.getNguoiGuiId());
        System.out.println("Người nhận ID: " + thongBaoPhanHoi.getNguoiNhanId());
        System.out.println("Mã khách hàng: " + thongBaoPhanHoi.getMaKhachHang());
        System.out.println("Trạng thái: " + thongBaoPhanHoi.getTrangThai());

        assertEquals("DA_TU_CHOI", thongBaoGocDaTuChoi.getTrangThai());
        assertEquals(lyDoTuChoi, thongBaoGocDaTuChoi.getLyDoTuChoi());

        assertEquals("Từ chối tiếp nhận khách hàng", thongBaoPhanHoi.getTieuDe());
        assertEquals("TU_CHOI", thongBaoPhanHoi.getLoaiThongBao());
        assertEquals(nguoiNhanId, thongBaoPhanHoi.getNguoiGuiId());
        assertEquals(100, thongBaoPhanHoi.getNguoiNhanId());
        assertEquals(10, thongBaoPhanHoi.getMaKhachHang());
        assertEquals("CHUA_DOC", thongBaoPhanHoi.getTrangThai());
        assertEquals(lyDoTuChoi, thongBaoPhanHoi.getLyDoTuChoi());

        assertTrue(thongBaoPhanHoi.getNoiDung().contains("Nhân viên Phụ Trách"));
        assertTrue(thongBaoPhanHoi.getNoiDung().contains("Nguyễn Văn An"));
        assertTrue(thongBaoPhanHoi.getNoiDung().contains(lyDoTuChoi));

        verify(thongBaoRepository, times(1)).findById(maThongBao);
        verify(nhanVienRepository, times(1)).findById(nguoiNhanId);
        verify(khachHangRepository, times(1)).findById(10);
    }

    @Test
    void tuChoi_thongBaoDaXuLy_thiBaoLoi() {
        thongBaoCongViec.setTrangThai("DA_TU_CHOI");

        when(thongBaoRepository.findById(1))
                .thenReturn(Optional.of(thongBaoCongViec));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> thongBaoService.tuChoi(1, 200, "Không nhận")
        );

        System.out.println("===== TEST TỪ CHỐI THÔNG BÁO ĐÃ XỬ LÝ =====");
        System.out.println("Lỗi: " + exception.getMessage());

        assertEquals("Thông báo này đã được xử lý", exception.getMessage());

        verify(thongBaoRepository, times(1)).findById(1);
        verify(thongBaoRepository, never()).save(any());
        verify(nhanVienRepository, never()).findById(any());
        verify(khachHangRepository, never()).save(any());
    }
}