package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import vn.anyen.dto.response.HopDongPageResponse;
import vn.anyen.dto.response.HopDongResponse;
import vn.anyen.entity.DonHang;
import vn.anyen.entity.HopDong;
import vn.anyen.entity.KhachHang;
import vn.anyen.repository.HopDongRepository;

@Service
@RequiredArgsConstructor
public class HopDongService {

    private final HopDongRepository hopDongRepository;

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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng"));

        return toResponse(hopDong);
    }


        private HopDongResponse toResponse(HopDong hopDong) {
            DonHang donHang = hopDong.getDonHang();
            KhachHang khachHang = donHang != null ? donHang.getKhachHang() : null;

            return HopDongResponse.builder()
                    .maHopDong(hopDong.getMaHopDong())
                    .maHopDongText(formatHopDongCode(hopDong.getMaHopDong()))

                    // Lấy từ đơn hàng
                    .maDonHang(donHang != null ? donHang.getMaDonHang() : null)
                    .maDonHangText(donHang != null ? formatDonHangCode(donHang.getMaDonHang()) : null)
                    .ngayTaoDon(donHang != null ? donHang.getNgayTaoDon() : null)
                    .giaTriHopDong(donHang != null ? donHang.getTongTien() : null)

                    // Lấy từ khách hàng của đơn hàng
                    .tenKhachHang(khachHang != null ? khachHang.getTenKhachHang() : null)
                    .soDienThoai(khachHang != null ? khachHang.getSoDienThoai() : null)

                    // Lấy từ hợp đồng
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