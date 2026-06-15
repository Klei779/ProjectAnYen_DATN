package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.anyen.dto.response.DonHangResponse;
import vn.anyen.entity.ChiTietDonHang;
import vn.anyen.entity.DonHang;
import vn.anyen.repository.ChiTietDonHangRepository;
import vn.anyen.repository.DonHangRepository;
import vn.anyen.entity.HoaDon;
import vn.anyen.repository.HoaDonRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonHangService {

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final HoaDonRepository hoaDonRepository;

    // Thứ tự trạng thái đơn hàng theo quy trình
    private static final List<String> TRANG_THAI_ORDER = Arrays.asList(
            "Mới tạo",
            "Đã xác nhận",
            "Đang xử lý",
            "Chờ thanh toán",
            "Hoàn thành"
    );

    public List<DonHangResponse> getAllDonHang() {
        List<DonHang> donHangs = donHangRepository.findAll();
        return donHangs.stream().map(this::mapToDonHangResponse).collect(Collectors.toList());
    }

    public DonHangResponse getDonHangById(Integer maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + maDonHang));
        return mapToDonHangResponse(donHang);
    }

    @Transactional
    public DonHangResponse capNhatTrangThai(Integer maDonHang, String trangThaiMoi) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + maDonHang));

        String trangThaiHienTai = donHang.getTrangThai();

        // Không cho cập nhật nếu đã hủy
        if ("Đã hủy".equals(trangThaiHienTai)) {
            throw new RuntimeException("Đơn hàng đã bị hủy, không thể cập nhật trạng thái.");
        }

        // Validate trạng thái mới phải hợp lệ
        if (!"Đã hủy".equals(trangThaiMoi) && !TRANG_THAI_ORDER.contains(trangThaiMoi)) {
            throw new RuntimeException("Trạng thái '" + trangThaiMoi + "' không hợp lệ.");
        }

        // Nếu chuyển tiếp, kiểm tra thứ tự (chỉ được chuyển đúng 1 bước tiếp theo, hoặc hủy)
        if (!"Đã hủy".equals(trangThaiMoi)) {
            int currentIdx = TRANG_THAI_ORDER.indexOf(trangThaiHienTai);
            int nextIdx = TRANG_THAI_ORDER.indexOf(trangThaiMoi);
            if (nextIdx != currentIdx + 1) {
                throw new RuntimeException("Chỉ có thể chuyển sang trạng thái tiếp theo. " +
                        "Hiện tại: '" + trangThaiHienTai + "', yêu cầu: '" + trangThaiMoi + "'.");
            }
        }

        donHang.setTrangThai(trangThaiMoi);
        donHangRepository.save(donHang);

        return mapToDonHangResponse(donHang);
    }

    @Transactional
    public DonHangResponse huyDonHang(Integer maDonHang) {
        return capNhatTrangThai(maDonHang, "Đã hủy");
    }

    private DonHangResponse mapToDonHangResponse(DonHang donHang) {
        List<ChiTietDonHang> chiTiets = chiTietDonHangRepository.findByDonHang_MaDonHang(donHang.getMaDonHang());
        HoaDon hoaDon = hoaDonRepository
                .findByDonHang_MaDonHang(donHang.getMaDonHang())
                .orElse(null);

        List<DonHangResponse.ChiTietDonHangResponse> sanPhams = chiTiets.stream().map(ct -> 
            DonHangResponse.ChiTietDonHangResponse.builder()
                .MaSanPham(ct.getSanPham().getMaSanPham())
                .tenSanPham(ct.getSanPham().getTenSanPham())
                .maSKU("SP" + String.format("%03d", ct.getSanPham().getMaSanPham()))
                .phanLoai(ct.getSanPham().getLoai())
                .HinhAnh(ct.getSanPham().getHinhAnh())
                .giaTien(ct.getGiaTien())
                .SoLuong(ct.getSoLuong())
                .thanhTien(ct.getGiaTien().multiply(BigDecimal.valueOf(ct.getSoLuong())))
                .build()
        ).collect(Collectors.toList());

        // Build lịch sử theo đúng thứ tự trạng thái
        String currentTrangThai = donHang.getTrangThai();
        int currentIdx = TRANG_THAI_ORDER.indexOf(currentTrangThai);
        boolean isDaHuy = "Đã hủy".equals(currentTrangThai);

        List<DonHangResponse.LichSuDonHangResponse> lichSu = new ArrayList<>();
        for (int i = 0; i < TRANG_THAI_ORDER.size(); i++) {
            String step = TRANG_THAI_ORDER.get(i);
            boolean done = !isDaHuy && i <= currentIdx;
            String time = "";
            String moTa = "";

            if (i == 0 && donHang.getNgayTaoDon() != null) {
                time = donHang.getNgayTaoDon().toString();
                moTa = "Đơn hàng đã được tạo.";
            } else if (done) {
                moTa = "Đã hoàn thành.";
            } else {
                moTa = "Chưa cập nhật";
            }

            String color;
            switch (step) {
                case "Mới tạo": color = "yellow"; break;
                case "Đã xác nhận": color = "blue"; break;
                case "Đang xử lý": color = "orange"; break;
                case "Chờ thanh toán": color = "purple"; break;
                case "Hoàn thành": color = "green"; break;
                default: color = "gray"; break;
            }

            lichSu.add(DonHangResponse.LichSuDonHangResponse.builder()
                    .trangThai(step)
                    .thoiGian(time)
                    .moTa(moTa)
                    .color(color)
                    .done(done)
                    .build());
        }

        return DonHangResponse.builder()
                .MaDonHang(donHang.getMaDonHang())
                .maCode(String.format("DH%04d", donHang.getMaDonHang()))
                .MaKhachHang(donHang.getKhachHang() != null ? donHang.getKhachHang().getMaKhachHang() : null)
                .tenKhachHang(donHang.getKhachHang() != null ? donHang.getKhachHang().getTenKhachHang() : null)
                .emailKH(donHang.getKhachHang() != null ? donHang.getKhachHang().getEmail() : null)
                .soDienThoaiKH(donHang.getKhachHang() != null ? donHang.getKhachHang().getSoDienThoai() : null)
                .diaChiKH(donHang.getKhachHang() != null ? donHang.getKhachHang().getDiaChi() : null)
                .loaiKH("Thường")
                .tongDonKH(1)
                .tongChiTieuKH(donHang.getTongTien() != null ? donHang.getTongTien() : BigDecimal.ZERO)
                .ghiChuKH("")
                .ghiChuNoiBo(donHang.getGhiChu())
                .MaNhanVien(donHang.getNhanVien() != null ? donHang.getNhanVien().getMaNhanVien() : null)
                .tenNhanVien(donHang.getNhanVien() != null ? donHang.getNhanVien().getHoTen() : null)
                .NgayTaoDon(donHang.getNgayTaoDon())
                .tongTien(donHang.getTongTien())
                .trangThai(donHang.getTrangThai())
                .GhiChu(donHang.getGhiChu())
                .phuongThucThanhToan(donHang.getPhuongThucThanhToan())
                .trangThaiThanhToan(donHang.getTrangThaiThanhToan())

                .maHoaDon(hoaDon != null ? hoaDon.getMaHoaDon() : null)
                .daCoHoaDon(hoaDon != null)
                .trangThaiHoaDon(hoaDon != null ? hoaDon.getTrangThai() : null)

                .phuongThucGiaoHang("Giao hàng tận nơi")
                .phiVanChuyen(BigDecimal.ZERO)
                .giamGia(BigDecimal.ZERO)
                .sanPhams(sanPhams)
                .lichSu(lichSu)
                .build();
    }
    @Transactional
    public DonHangResponse capNhatTrangThaiNhanVien(Integer maDonHang, String trangThaiMoi) {

        if (trangThaiMoi == null || trangThaiMoi.trim().isEmpty()) {
            throw new RuntimeException("Trạng thái đơn hàng không được để trống.");
        }

        trangThaiMoi = trangThaiMoi.trim();

        List<String> trangThaiHopLe = Arrays.asList(
                "Mới tạo",
                "Đã xác nhận",
                "Đang xử lý",
                "Chờ thanh toán",
                "Hoàn thành",
                "Đã hủy"
        );

        if (!trangThaiHopLe.contains(trangThaiMoi)) {
            throw new RuntimeException("Trạng thái '" + trangThaiMoi + "' không hợp lệ.");
        }

        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + maDonHang));

        donHang.setTrangThai(trangThaiMoi);

        DonHang saved = donHangRepository.save(donHang);

        return mapToDonHangResponse(saved);
    }
}

