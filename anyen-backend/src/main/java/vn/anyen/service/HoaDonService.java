package vn.anyen.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.constants.AppLabels;
import vn.anyen.dto.request.HuyHoaDonRequest;
import vn.anyen.dto.request.TaoHoaDonRequest;
import vn.anyen.dto.request.TuChoiHoaDonRequest;
import vn.anyen.dto.response.HoaDonResponse;
import vn.anyen.entity.DonHang;
import vn.anyen.entity.HoaDon;
import vn.anyen.entity.NhanVien;
import vn.anyen.entity.ThongBao;
import vn.anyen.repository.DonHangRepository;
import vn.anyen.repository.HoaDonRepository;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.ThongBaoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final DonHangRepository donHangRepository;
    private final ThongBaoRepository thongBaoRepository;
    private final NhanVienRepository nhanVienRepository;
    private final ObjectMapper objectMapper;

    private static final List<Integer> TRANG_THAI_DUOC_TAO_HOA_DON =
            Arrays.asList(
                    DonHang.TT_HOAN_THANH
            );

    // =====================================================
    // TẠO HÓA ĐƠN
    // =====================================================

    @Transactional
    public HoaDonResponse taoHoaDon(TaoHoaDonRequest request) {

        DonHang donHang = donHangRepository
                .findById(request.getMaDonHang())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đơn hàng #" + request.getMaDonHang()
                ));

        if (hoaDonRepository.existsByDonHang_MaDonHang(
                request.getMaDonHang()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Đơn hàng này đã có hóa đơn."
            );
        }

        if (!TRANG_THAI_DUOC_TAO_HOA_DON.contains(
                donHang.getTrangThai()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chỉ được tạo hóa đơn khi đơn hàng ở trạng thái Chờ thanh toán hoặc Hoàn thành."
            );
        }

        BigDecimal tongTien = donHang.getTongTien() != null
                ? donHang.getTongTien()
                : BigDecimal.ZERO;

        Integer phuongThucThanhToan =
                request.getPhuongThucThanhToan();

        if (phuongThucThanhToan == null) {
            phuongThucThanhToan =
                    donHang.getPhuongThucThanhToan();
        }

        Integer trangThaiHoaDon = request.getTrangThai();

        if (trangThaiHoaDon == null) {
            trangThaiHoaDon = HoaDon.TT_DA_TAO;
        }

        HoaDon hoaDon = HoaDon.builder()
                .donHang(donHang)
                .ngayIn(request.getNgayIn())
                .tongTien(tongTien)
                .phuongThucThanhToan(phuongThucThanhToan)
                .trangThai(trangThaiHoaDon)
                .build();

        HoaDon saved = hoaDonRepository.save(hoaDon);

        if (HoaDon.TT_DA_TAO.equals(trangThaiHoaDon)) {
            donHang.setTrangThaiThanhToan(
                    DonHang.TTTT_DA_THANH_TOAN
            );
        } else {
            donHang.setTrangThaiThanhToan(
                    DonHang.TTTT_CHUA_THANH_TOAN
            );
        }

        donHangRepository.save(donHang);

        return HoaDonResponse.fromEntity(saved);
    }

    // =====================================================
    // XEM HÓA ĐƠN THEO ĐƠN HÀNG
    // =====================================================

    public HoaDonResponse getHoaDonByDonHang(Integer maDonHang) {

        HoaDon hoaDon = hoaDonRepository
                .findByDonHang_MaDonHang(maDonHang)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Đơn hàng này chưa có hóa đơn."
                ));

        return HoaDonResponse.fromEntity(hoaDon);
    }

    // =====================================================
    // NHÂN VIÊN GỬI YÊU CẦU HỦY HÓA ĐƠN
    // =====================================================

    @Transactional
    public Map<String, Object> guiYeuCauHuy(
            Integer maHoaDon,
            String tenDangNhap,
            HuyHoaDonRequest request
    ) {
        HoaDon hoaDon = hoaDonRepository
                .findById(maHoaDon)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy hóa đơn #" + maHoaDon
                ));

        if (AppLabels.HD_DA_HUY.equals(
                hoaDon.getTrangThai()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Hóa đơn này đã bị hủy."
            );
        }

        DonHang donHang = hoaDon.getDonHang();

        if (donHang == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Hóa đơn không liên kết với đơn hàng."
            );
        }

        /*
         * Chỉ kiểm tra điều kiện hủy hóa đơn.
         * Không cập nhật trạng thái đơn hàng.
         */
        if (AppLabels.DH_HOAN_THANH.equals(
                donHang.getTrangThai()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Không thể hủy hóa đơn vì đơn hàng đã hoàn thành."
            );
        }

        String lyDoHuy = request.getLyDoHuy() == null
                ? ""
                : request.getLyDoHuy().trim();

        if (lyDoHuy.length() < 4) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lý do hủy phải từ 4 ký tự."
            );
        }

        /*
         * Nội dung JSON được lưu dạng:
         * {"maHoaDon":3,"maDonHang":6,"lyDoHuy":"..."}
         */
        String tuKhoaHoaDon =
                "\"maHoaDon\":" + maHoaDon;

        boolean dangChoDuyet =
                thongBaoRepository
                        .existsByLoaiThongBaoAndNoiDungContainingAndTrangThai(
                                AppLabels.TB_YEU_CAU_HUY_HOA_DON,
                                tuKhoaHoaDon,
                                AppLabels.TB_CHO_XAC_NHAN
                        );

        if (dangChoDuyet) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Hóa đơn này đang có yêu cầu hủy chờ Admin xử lý."
            );
        }

        NhanVien nhanVienGui = nhanVienRepository
                .findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy nhân viên đang đăng nhập."
                ));

        NhanVien admin = nhanVienRepository
                .findFirstByVaiTroAndTrangThai(
                        AppLabels.VT_ADMIN,
                        AppLabels.NV_DANG_HOAT_DONG
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy Admin đang hoạt động."
                ));

        Map<String, Object> noiDungMap =
                new LinkedHashMap<>();

        noiDungMap.put(
                "maHoaDon",
                hoaDon.getMaHoaDon()
        );

        noiDungMap.put(
                "maDonHang",
                donHang.getMaDonHang()
        );

        noiDungMap.put(
                "lyDoHuy",
                lyDoHuy
        );

        String noiDungJson;

        try {
            noiDungJson = objectMapper.writeValueAsString(
                    noiDungMap
            );
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể tạo nội dung yêu cầu hủy."
            );
        }

        ThongBao thongBao = ThongBao.builder()
                .tieuDe(
                        "Yêu cầu hủy hóa đơn HD" +
                                String.format(
                                        "%04d",
                                        hoaDon.getMaHoaDon()
                                )
                )
                .noiDung(noiDungJson)
                .loaiThongBao(
                        AppLabels.TB_YEU_CAU_HUY_HOA_DON
                )
                .nguoiGuiId(
                        nhanVienGui.getMaNhanVien()
                )
                .nguoiNhanId(
                        admin.getMaNhanVien()
                )
                .trangThai(
                        AppLabels.TB_CHO_XAC_NHAN
                )
                .lyDoTuChoi(null)
                .build();

        ThongBao savedThongBao =
                thongBaoRepository.save(thongBao);

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "message",
                "Đã gửi yêu cầu hủy hóa đơn cho Admin"
        );

        response.put(
                "maThongBao",
                savedThongBao.getMaThongBao()
        );

        response.put(
                "maHoaDon",
                maHoaDon
        );

        return response;
    }

    // =====================================================
    // ADMIN CHẤP NHẬN HỦY HÓA ĐƠN
    // =====================================================

    @Transactional
    public Map<String, Object> chapNhanHuy(
            Integer maThongBao,
            String tenDangNhapAdmin
    ) {
        ThongBao yeuCau =
                getYeuCauHuyDangCho(maThongBao);

        NhanVien admin =
                getAdminDangNhap(tenDangNhapAdmin);

        Integer maHoaDon =
                docMaHoaDon(yeuCau.getNoiDung());

        HoaDon hoaDon = hoaDonRepository
                .findById(maHoaDon)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy hóa đơn cần hủy."
                ));

        if (AppLabels.HD_DA_HUY.equals(
                hoaDon.getTrangThai()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Hóa đơn đã bị hủy trước đó."
            );
        }

        DonHang donHang = hoaDon.getDonHang();

        /*
         * Kiểm tra lại vì đơn hàng có thể hoàn thành
         * trong thời gian yêu cầu chờ Admin xử lý.
         */
        if (donHang != null &&
                AppLabels.DH_HOAN_THANH.equals(
                        donHang.getTrangThai()
                )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Không thể duyệt hủy vì đơn hàng đã hoàn thành."
            );
        }

        /*
         * Chỉ hủy hóa đơn.
         * Không cập nhật đơn hàng.
         */
        hoaDon.setTrangThai(AppLabels.HD_DA_HUY);
        hoaDonRepository.save(hoaDon);

        yeuCau.setTrangThai(
                AppLabels.TB_DA_CHAP_NHAN
        );

        yeuCau.setLyDoTuChoi(null);
        yeuCau.setNgayCapNhat(LocalDateTime.now());

        thongBaoRepository.save(yeuCau);

        ThongBao ketQua = ThongBao.builder()
                .tieuDe(
                        "Yêu cầu hủy hóa đơn đã được chấp nhận"
                )
                .noiDung(
                        "Hóa đơn HD" +
                                String.format(
                                        "%04d",
                                        hoaDon.getMaHoaDon()
                                ) +
                                " đã được Admin chấp nhận hủy."
                )
                .loaiThongBao(
                        AppLabels.TB_KET_QUA_HUY_HOA_DON
                )
                .nguoiGuiId(
                        admin.getMaNhanVien()
                )
                .nguoiNhanId(
                        yeuCau.getNguoiGuiId()
                )
                .trangThai(
                        AppLabels.TB_CHUA_DOC
                )
                .lyDoTuChoi(null)
                .build();

        thongBaoRepository.save(ketQua);

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "message",
                "Đã chấp nhận hủy hóa đơn"
        );

        response.put(
                "maHoaDon",
                maHoaDon
        );

        return response;
    }

    // =====================================================
    // ADMIN TỪ CHỐI HỦY HÓA ĐƠN
    // =====================================================

    @Transactional
    public Map<String, Object> tuChoiHuy(
            Integer maThongBao,
            String tenDangNhapAdmin,
            TuChoiHoaDonRequest request
    ) {
        ThongBao yeuCau =
                getYeuCauHuyDangCho(maThongBao);

        NhanVien admin =
                getAdminDangNhap(tenDangNhapAdmin);

        Integer maHoaDon =
                docMaHoaDon(yeuCau.getNoiDung());

        String lyDoTuChoi =
                request.getLyDoTuChoi() == null
                        ? ""
                        : request.getLyDoTuChoi().trim();

        if (lyDoTuChoi.length() < 4) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lý do từ chối phải từ 4 ký tự."
            );
        }

        yeuCau.setTrangThai(
                AppLabels.TB_DA_TU_CHOI
        );

        yeuCau.setLyDoTuChoi(lyDoTuChoi);
        yeuCau.setNgayCapNhat(LocalDateTime.now());

        thongBaoRepository.save(yeuCau);

        /*
         * Không cập nhật hóa đơn.
         * Hóa đơn vẫn ở trạng thái Đã tạo.
         */

        ThongBao ketQua = ThongBao.builder()
                .tieuDe(
                        "Yêu cầu hủy hóa đơn bị từ chối"
                )
                .noiDung(
                        "Yêu cầu hủy hóa đơn HD" +
                                String.format(
                                        "%04d",
                                        maHoaDon
                                ) +
                                " đã bị từ chối. Lý do: " +
                                lyDoTuChoi
                )
                .loaiThongBao(
                        AppLabels.TB_KET_QUA_HUY_HOA_DON
                )
                .nguoiGuiId(
                        admin.getMaNhanVien()
                )
                .nguoiNhanId(
                        yeuCau.getNguoiGuiId()
                )
                .trangThai(
                        AppLabels.TB_CHUA_DOC
                )
                .lyDoTuChoi(
                        lyDoTuChoi
                )
                .build();

        thongBaoRepository.save(ketQua);

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "message",
                "Đã từ chối yêu cầu hủy hóa đơn"
        );

        response.put(
                "maHoaDon",
                maHoaDon
        );

        response.put(
                "lyDoTuChoi",
                lyDoTuChoi
        );

        return response;
    }

    // =====================================================
    // HÀM HỖ TRỢ
    // =====================================================

    private ThongBao getYeuCauHuyDangCho(
            Integer maThongBao
    ) {
        ThongBao thongBao = thongBaoRepository
                .findById(maThongBao)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy yêu cầu hủy hóa đơn."
                ));

        if (!AppLabels.TB_YEU_CAU_HUY_HOA_DON.equals(
                thongBao.getLoaiThongBao()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Thông báo này không phải yêu cầu hủy hóa đơn."
            );
        }

        if (!AppLabels.TB_CHO_XAC_NHAN.equals(
                thongBao.getTrangThai()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Yêu cầu hủy hóa đơn đã được xử lý."
            );
        }

        return thongBao;
    }

    private NhanVien getAdminDangNhap(
            String tenDangNhap
    ) {
        NhanVien admin = nhanVienRepository
                .findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản đang đăng nhập."
                ));

        if (!AppLabels.VT_ADMIN.equals(
                admin.getVaiTro()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Chỉ Admin mới được xử lý yêu cầu hủy hóa đơn."
            );
        }

        return admin;
    }

    private Integer docMaHoaDon(
            String noiDungJson
    ) {
        try {
            JsonNode node =
                    objectMapper.readTree(noiDungJson);

            if (!node.has("maHoaDon")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Thông báo không có mã hóa đơn."
                );
            }

            return node.get("maHoaDon").asInt();

        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nội dung yêu cầu hủy hóa đơn không hợp lệ."
            );
        }
    }
}