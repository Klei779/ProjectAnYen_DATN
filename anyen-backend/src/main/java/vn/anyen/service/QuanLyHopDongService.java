package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vn.anyen.dto.response.HopDongPageResponse;
import vn.anyen.dto.response.HopDongResponse;
import vn.anyen.entity.CongNo;
import vn.anyen.entity.DonHang;
import vn.anyen.entity.HopDong;
import vn.anyen.repository.ChiTietDonHangRepository;
import vn.anyen.repository.CongNoRepository;
import vn.anyen.repository.DonHangRepository;
import vn.anyen.repository.HDongCTRepository;
import vn.anyen.repository.HoaDonRepository;
import vn.anyen.repository.HopDongRepository;
import vn.anyen.repository.LichSuCongNoRepository;
import vn.anyen.repository.ThongBaoDoiTacRepository;

import java.util.List;

/**
 * Nghiệp vụ dành cho ADMIN: xem lịch sử / danh sách hợp đồng (chỉ xem),
 * ẩn / hiện tạm thời và xóa hợp đồng cùng toàn bộ dữ liệu liên quan.
 */
@Service
@RequiredArgsConstructor
public class QuanLyHopDongService {

    private final HopDongRepository hopDongRepository;
    private final HopDongService hopDongService;

    private final HDongCTRepository hDongCTRepository;
    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final HoaDonRepository hoaDonRepository;
    private final CongNoRepository congNoRepository;
    private final LichSuCongNoRepository lichSuCongNoRepository;
    private final ThongBaoDoiTacRepository thongBaoDoiTacRepository;

    public HopDongPageResponse getHopDongs(
            String keyword,
            String trangThai,
            boolean includeHidden,
            int page,
            int pageSize
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                pageSize,
                Sort.by(Sort.Direction.DESC, "maHopDong")
        );

        String searchKeyword = normalizeKeyword(keyword);

        Page<HopDong> hopDongPage = hopDongRepository.searchHopDongAdmin(
                searchKeyword,
                trangThai,
                includeHidden,
                pageable
        );

        return HopDongPageResponse.builder()
                .items(
                        hopDongPage.getContent()
                                .stream()
                                .map(hopDongService::toResponse)
                                .toList()
                )
                .total(hopDongPage.getTotalElements())
                .build();
    }

    public HopDongResponse getChiTiet(Integer id) {
        return hopDongService.getChiTiet(id);
    }

    @Transactional
    public HopDongResponse anHopDong(Integer id) {
        return capNhatTrangThaiAn(id, true);
    }

    @Transactional
    public HopDongResponse hienHopDong(Integer id) {
        return capNhatTrangThaiAn(id, false);
    }

    private HopDongResponse capNhatTrangThaiAn(Integer id, boolean an) {
        HopDong hopDong = hopDongRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy hợp đồng"
                ));

        hopDong.setAn(an);

        return hopDongService.toResponse(hopDongRepository.save(hopDong));
    }

    /**
     * Xóa hợp đồng và toàn bộ dữ liệu liên quan (không thể khôi phục):
     * chi tiết hợp đồng, và nếu có đơn hàng gắn với hợp đồng thì xóa luôn
     * đơn hàng cùng chi tiết đơn, hóa đơn, công nợ (kèm lịch sử) và thông
     * báo đối tác của đơn đó.
     */
    @Transactional
    public void xoaHopDong(Integer id) {
        HopDong hopDong = hopDongRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy hợp đồng"
                ));

        Integer maDonHang = hopDong.getDonHang() != null
                ? hopDong.getDonHang().getMaDonHang()
                : null;

        // Xóa chi tiết hợp đồng
        hDongCTRepository.deleteAll(
                hDongCTRepository.findByHopDong_MaHopDong(hopDong.getMaHopDong())
        );

        // Xóa hợp đồng
        hopDongRepository.delete(hopDong);
        hopDongRepository.flush();

        if (maDonHang == null) {
            return;
        }

        // Xóa lịch sử công nợ + công nợ của đơn hàng
        List<CongNo> congNos = congNoRepository.findByDonHang_MaDonHang(maDonHang);
        for (CongNo congNo : congNos) {
            lichSuCongNoRepository.deleteAll(
                    lichSuCongNoRepository.findByCongNo_MaCongNo(congNo.getMaCongNo())
            );
        }
        congNoRepository.deleteAll(congNos);

        // Xóa hóa đơn của đơn hàng
        hoaDonRepository.deleteAll(
                hoaDonRepository.findAllByDonHang_MaDonHang(maDonHang)
        );

        // Xóa thông báo đối tác gắn với đơn hàng
        thongBaoDoiTacRepository.deleteAll(
                thongBaoDoiTacRepository.findByDonHang_MaDonHang(maDonHang)
        );

        // Xóa chi tiết đơn hàng
        chiTietDonHangRepository.deleteAll(
                chiTietDonHangRepository.findByDonHang_MaDonHang(maDonHang)
        );

        // Xóa đơn hàng
        donHangRepository.findById(maDonHang)
                .ifPresent(donHangRepository::delete);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "";
        }

        String value = keyword.trim();
        String upperValue = value.toUpperCase();

        if (upperValue.matches("^HD0*\\d+$")) {
            return upperValue.replaceFirst("^HD0*", "");
        }

        if (upperValue.matches("^DH0*\\d+$")) {
            return upperValue.replaceFirst("^DH0*", "");
        }

        return value;
    }
}
