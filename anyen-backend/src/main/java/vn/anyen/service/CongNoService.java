package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.anyen.entity.ChiTietDonHang;
import vn.anyen.entity.CongNo;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.DonHang;
import vn.anyen.repository.ChiTietDonHangRepository;
import vn.anyen.repository.CongNoRepository;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.DonHangRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CongNoService {

    private final CongNoRepository congNoRepository;
    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final DoiTacRepository doiTacRepository;

    @Transactional
    public void taoCongNoTuDonHang(Integer maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang).orElse(null);
        if (donHang == null) {
            return;
        }

        List<ChiTietDonHang> chiTietList = chiTietDonHangRepository.findByDonHang_MaDonHang(maDonHang);
        if (chiTietList == null || chiTietList.isEmpty()) {
            return;
        }

        // Gom nhóm theo đối tác
        Map<Integer, BigDecimal> tongTienTheoDoiTac = new HashMap<>();
        for (ChiTietDonHang ct : chiTietList) {
            if (ct.getSanPham() != null && ct.getSanPham().getMaDoiTac() != null) {
                Integer maDoiTac = ct.getSanPham().getMaDoiTac();
                BigDecimal thanhTien = ct.getGiaTien().multiply(BigDecimal.valueOf(ct.getSoLuong()));
                
                tongTienTheoDoiTac.put(maDoiTac, tongTienTheoDoiTac.getOrDefault(maDoiTac, BigDecimal.ZERO).add(thanhTien));
            }
        }

        // Tạo công nợ cho mỗi đối tác
        for (Map.Entry<Integer, BigDecimal> entry : tongTienTheoDoiTac.entrySet()) {
            Integer maDoiTac = entry.getKey();
            BigDecimal tongTien = entry.getValue();

            Optional<DoiTac> doiTacOpt = doiTacRepository.findById(maDoiTac);
            if (doiTacOpt.isPresent()) {
                CongNo congNo = CongNo.builder()
                        .donHang(donHang)
                        .doiTac(doiTacOpt.get())
                        .tongTien(tongTien)
                        .daThanhToan(BigDecimal.ZERO)
                        .conLai(tongTien)
                        .hanThanhToan(LocalDate.now().plusDays(30))
                        .trangThai(CongNo.TT_CHUA_THANH_TOAN)
                        .ghiChu("Công nợ sinh tự động từ đơn hàng #" + maDonHang)
                        .build();

                congNoRepository.save(congNo);
            }
        }
    }
}
