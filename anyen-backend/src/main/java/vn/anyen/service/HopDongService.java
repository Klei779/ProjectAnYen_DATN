package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vn.anyen.dto.request.HopDongCreateRequest;
import vn.anyen.dto.response.DonHangHopDongDetailResponse;
import vn.anyen.dto.response.DonHangHopDongOptionResponse;
import vn.anyen.dto.response.HopDongPageResponse;
import vn.anyen.dto.response.HopDongResponse;
import vn.anyen.entity.ChiTietDonHang;
import vn.anyen.entity.DonHang;
import vn.anyen.entity.HopDong;
import vn.anyen.entity.KhachHang;
import vn.anyen.repository.ChiTietDonHangRepository;
import vn.anyen.repository.DonHangRepository;
import vn.anyen.repository.HopDongRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HopDongService {

    private final HopDongRepository hopDongRepository;
    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;

    public HopDongPageResponse getHopDongs(
            String keyword,
            String trangThai,
            int page,
            int pageSize
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                pageSize,
                Sort.by(Sort.Direction.DESC, "maHopDong")
        );

        Page<HopDong> hopDongPage =
                hopDongRepository.searchHopDong(keyword, trangThai, pageable);

        return HopDongPageResponse.builder()
                .items(
                        hopDongPage.getContent()
                                .stream()
                                .map(this::toResponse)
                                .toList()
                )
                .total(hopDongPage.getTotalElements())
                .build();
    }

    public HopDongResponse getChiTiet(Integer id) {
        HopDong hopDong = hopDongRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy hợp đồng"
                        )
                );

        return toResponse(hopDong);
    }

    public List<DonHangHopDongOptionResponse> getDonHangOptions() {
        return donHangRepository
                .findAll(Sort.by(Sort.Direction.DESC, "maDonHang"))
                .stream()
                .map(this::toDonHangOptionResponse)
                .toList();
    }

    public DonHangHopDongDetailResponse getDonHangDetail(Integer maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy đơn hàng"
                        )
                );

        return toDonHangDetailResponse(donHang);
    }

    @Transactional
    public HopDongResponse taoHopDong(HopDongCreateRequest request) {

        if (request.getMaDonHang() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vui lòng chọn đơn hàng trước khi lưu hợp đồng"
            );
        }

        DonHang donHang = donHangRepository.findById(request.getMaDonHang())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Đơn hàng không tồn tại"
                        )
                );

        boolean daCoHopDong =
                hopDongRepository.existsByDonHang_MaDonHang(request.getMaDonHang());

        if (daCoHopDong) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Đơn hàng này đã có hợp đồng rồi"
            );
        }

        LocalDate today = LocalDate.now();

        HopDong hopDong = HopDong.builder()
                .donHang(donHang)
                .ngayKyHD(
                        request.getNgayKyHD() != null
                                ? request.getNgayKyHD()
                                : today
                )
                .ngayViet(
                        request.getNgayViet() != null
                                ? request.getNgayViet()
                                : today
                )
                .trangThai(
                        request.getTrangThai() != null
                                && !request.getTrangThai().isBlank()
                                ? request.getTrangThai()
                                : "Chờ ký"
                )
                .build();

        HopDong saved = hopDongRepository.save(hopDong);

        return toResponse(saved);
    }

    private DonHangHopDongOptionResponse toDonHangOptionResponse(DonHang donHang) {
        KhachHang khachHang = donHang.getKhachHang();

        return DonHangHopDongOptionResponse.builder()
                .maDonHang(donHang.getMaDonHang())
                .maDonHangText(formatDonHangCode(donHang.getMaDonHang()))
                .tenKhachHang(
                        khachHang != null
                                ? khachHang.getTenKhachHang()
                                : null
                )
                .soDienThoai(
                        khachHang != null
                                ? khachHang.getSoDienThoai()
                                : null
                )
                .ngayTaoDon(donHang.getNgayTaoDon())
                .tongTien(donHang.getTongTien())
                .trangThai(donHang.getTrangThai())
                .daCoHopDong(
                        hopDongRepository.existsByDonHang_MaDonHang(
                                donHang.getMaDonHang()
                        )
                )
                .build();
    }

    private DonHangHopDongDetailResponse toDonHangDetailResponse(DonHang donHang) {
        KhachHang khachHang = donHang.getKhachHang();

        List<ChiTietDonHang> chiTiets =
                chiTietDonHangRepository.findByDonHang_MaDonHang(
                        donHang.getMaDonHang()
                );

        List<DonHangHopDongDetailResponse.SanPhamTrongDonHangResponse> sanPhams =
                chiTiets.stream()
                        .map(ct -> {
                            BigDecimal giaTien =
                                    ct.getGiaTien() != null
                                            ? ct.getGiaTien()
                                            : BigDecimal.ZERO;

                            int soLuong =
                                    ct.getSoLuong() != null
                                            ? ct.getSoLuong()
                                            : 0;

                            return DonHangHopDongDetailResponse
                                    .SanPhamTrongDonHangResponse
                                    .builder()
                                    .maSanPham(
                                            ct.getSanPham() != null
                                                    ? ct.getSanPham().getMaSanPham()
                                                    : null
                                    )
                                    .tenSanPham(
                                            ct.getSanPham() != null
                                                    ? ct.getSanPham().getTenSanPham()
                                                    : null
                                    )
                                    .loai(
                                            ct.getSanPham() != null
                                                    ? ct.getSanPham().getLoai()
                                                    : null
                                    )
                                    .soLuong(soLuong)
                                    .giaTien(giaTien)
                                    .thanhTien(
                                            giaTien.multiply(
                                                    BigDecimal.valueOf(soLuong)
                                            )
                                    )
                                    .hinhAnh(
                                            ct.getSanPham() != null
                                                    ? ct.getSanPham().getHinhAnh()
                                                    : null
                                    )
                                    .build();
                        })
                        .toList();

        return DonHangHopDongDetailResponse.builder()
                .maDonHang(donHang.getMaDonHang())
                .maDonHangText(formatDonHangCode(donHang.getMaDonHang()))
                .ngayTaoDon(donHang.getNgayTaoDon())
                .tongTien(donHang.getTongTien())
                .trangThai(donHang.getTrangThai())
                .ghiChu(donHang.getGhiChu())

                .maKhachHang(
                        khachHang != null
                                ? khachHang.getMaKhachHang()
                                : null
                )
                .tenKhachHang(
                        khachHang != null
                                ? khachHang.getTenKhachHang()
                                : null
                )
                .cccd(
                        khachHang != null
                                ? khachHang.getCccd()
                                : null
                )
                .email(
                        khachHang != null
                                ? khachHang.getEmail()
                                : null
                )
                .soDienThoai(
                        khachHang != null
                                ? khachHang.getSoDienThoai()
                                : null
                )
                .diaChi(
                        khachHang != null
                                ? khachHang.getDiaChi()
                                : null
                )

                .maNhanVien(
                        donHang.getNhanVien() != null
                                ? donHang.getNhanVien().getMaNhanVien()
                                : null
                )
                .tenNhanVien(
                        donHang.getNhanVien() != null
                                ? donHang.getNhanVien().getHoTen()
                                : null
                )
                .sanPhams(sanPhams)
                .build();
    }

    private HopDongResponse toResponse(HopDong hopDong) {
        DonHang donHang = hopDong.getDonHang();
        KhachHang khachHang =
                donHang != null
                        ? donHang.getKhachHang()
                        : null;

        return HopDongResponse.builder()
                .maHopDong(hopDong.getMaHopDong())
                .maHopDongText(formatHopDongCode(hopDong.getMaHopDong()))

                .maDonHang(
                        donHang != null
                                ? donHang.getMaDonHang()
                                : null
                )
                .maDonHangText(
                        donHang != null
                                ? formatDonHangCode(donHang.getMaDonHang())
                                : null
                )
                .ngayTaoDon(
                        donHang != null
                                ? donHang.getNgayTaoDon()
                                : null
                )
                .giaTriHopDong(
                        donHang != null
                                ? donHang.getTongTien()
                                : null
                )

                .tenKhachHang(
                        khachHang != null
                                ? khachHang.getTenKhachHang()
                                : null
                )
                .soDienThoai(
                        khachHang != null
                                ? khachHang.getSoDienThoai()
                                : null
                )

                .ngayKyHD(hopDong.getNgayKyHD())
                .ngayViet(hopDong.getNgayViet())
                .trangThai(hopDong.getTrangThai())
                .build();
    }

    private String formatHopDongCode(Integer id) {
        if (id == null) return "";
        return String.format("HD%04d", id);
    }

    private String formatDonHangCode(Integer id) {
        if (id == null) return "";
        return String.format("DH%04d", id);
    }
}