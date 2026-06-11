package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.anyen.dto.response.DonHangResponse;
import vn.anyen.entity.ChiTietDonHang;
import vn.anyen.entity.DonHang;
import vn.anyen.repository.ChiTietDonHangRepository;
import vn.anyen.repository.DonHangRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonHangService {

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;

    public List<DonHangResponse> getAllDonHang() {
        List<DonHang> donHangs = donHangRepository.findAll();
        return donHangs.stream().map(this::mapToDonHangResponse).collect(Collectors.toList());
    }

    private DonHangResponse mapToDonHangResponse(DonHang donHang) {
        List<ChiTietDonHang> chiTiets = chiTietDonHangRepository.findByDonHang_MaDonHang(donHang.getMaDonHang());

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

        List<DonHangResponse.LichSuDonHangResponse> lichSu = new ArrayList<>();
        lichSu.add(DonHangResponse.LichSuDonHangResponse.builder()
                .trangThai("Đơn đã được tạo").thoiGian(donHang.getNgayTaoDon() != null ? donHang.getNgayTaoDon().toString() : "").moTa("Đơn hàng đã được tạo.").color("green").done(true).build());
        
        boolean isDangXuLy = "Đang xử lý".equals(donHang.getTrangThai());
        boolean isHoanThanh = "Hoàn thành".equals(donHang.getTrangThai()) || "Đã xác nhận".equals(donHang.getTrangThai());
        
        lichSu.add(DonHangResponse.LichSuDonHangResponse.builder()
                .trangThai("Đang chờ đối tác tiếp nhận").thoiGian("").moTa("").color("orange").done(isDangXuLy || isHoanThanh).build());
        lichSu.add(DonHangResponse.LichSuDonHangResponse.builder()
                .trangThai("Ký hợp đồng").thoiGian("").moTa("").color("purple").done(isHoanThanh).build());
        lichSu.add(DonHangResponse.LichSuDonHangResponse.builder()
                .trangThai("Đối tác đang chuẩn bị đơn").thoiGian("").moTa("").color("blue").done(false).build());
        lichSu.add(DonHangResponse.LichSuDonHangResponse.builder()
                .trangThai("Đang giao").thoiGian("").moTa("").color("cyan").done(false).build());
        lichSu.add(DonHangResponse.LichSuDonHangResponse.builder()
                .trangThai("Đã giao").thoiGian("").moTa("").color("gray").done(false).build());


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
                .phuongThucThanhToan("Chuyển khoản")
                .phuongThucGiaoHang("Giao hàng tận nơi")
                .phiVanChuyen(BigDecimal.ZERO)
                .giamGia(BigDecimal.ZERO)
                .sanPhams(sanPhams)
                .lichSu(lichSu)
                .build();
    }
}
