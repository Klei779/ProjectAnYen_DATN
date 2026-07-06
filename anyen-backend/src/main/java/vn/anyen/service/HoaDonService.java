package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.anyen.dto.request.TaoHoaDonRequest;
import vn.anyen.dto.response.HoaDonResponse;
import vn.anyen.entity.DonHang;
import vn.anyen.entity.HoaDon;
import vn.anyen.repository.DonHangRepository;
import vn.anyen.repository.HoaDonRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final DonHangRepository donHangRepository;

    private static final List<Integer> TRANG_THAI_DUOC_TAO_HOA_DON = Arrays.asList(
            DonHang.TT_CHO_THANH_TOAN,
            DonHang.TT_HOAN_THANH
    );

    @Transactional
    public HoaDonResponse taoHoaDon(TaoHoaDonRequest request) {

        DonHang donHang = donHangRepository.findById(request.getMaDonHang())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy đơn hàng #" + request.getMaDonHang()
                ));

        if (hoaDonRepository.existsByDonHang_MaDonHang(request.getMaDonHang())) {
            throw new RuntimeException("Đơn hàng này đã có hóa đơn.");
        }

        if (!TRANG_THAI_DUOC_TAO_HOA_DON.contains(donHang.getTrangThai())) {
            throw new RuntimeException(
                    "Chỉ được tạo hóa đơn khi đơn hàng ở trạng thái Chờ thanh toán hoặc Hoàn thành."
            );
        }

        BigDecimal tongTien = donHang.getTongTien() != null
                ? donHang.getTongTien()
                : BigDecimal.ZERO;

        Integer phuongThucThanhToan = request.getPhuongThucThanhToan();

        if (phuongThucThanhToan == null) {
            phuongThucThanhToan = donHang.getPhuongThucThanhToan();
        }

        Integer trangThaiHoaDon = request.getTrangThai();

        HoaDon hoaDon = HoaDon.builder()
                .donHang(donHang)
                .ngayIn(request.getNgayIn())
                .tongTien(tongTien)
                .phuongThucThanhToan(phuongThucThanhToan)
                .trangThai(trangThaiHoaDon)
                .build();

        HoaDon saved = hoaDonRepository.save(hoaDon);

        if (HoaDon.TT_DA_TAO.equals(trangThaiHoaDon)) {
            donHang.setTrangThaiThanhToan(DonHang.TTTT_DA_THANH_TOAN);
        } else {
            donHang.setTrangThaiThanhToan(DonHang.TTTT_CHUA_THANH_TOAN);
        }

        donHangRepository.save(donHang);

        return HoaDonResponse.fromEntity(saved);
    }

    public HoaDonResponse getHoaDonByDonHang(Integer maDonHang) {
        HoaDon hoaDon = hoaDonRepository.findByDonHang_MaDonHang(maDonHang)
                .orElseThrow(() -> new RuntimeException("Đơn hàng này chưa có hóa đơn."));

        return HoaDonResponse.fromEntity(hoaDon);
    }
}
