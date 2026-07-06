package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.anyen.dto.response.ThongBaoResponse;
import vn.anyen.entity.KhachHang;
import vn.anyen.entity.NhanVien;
import vn.anyen.entity.ThongBao;
import vn.anyen.repository.KhachHangRepository;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.ThongBaoRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThongBaoService {

    private final ThongBaoRepository thongBaoRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

    /**
     * Lấy danh sách thông báo cho nhân viên (cá nhân + broadcast)
     */
    public List<ThongBaoResponse> getThongBaoByNguoiNhan(Integer nguoiNhanId) {

        List<ThongBao> list = thongBaoRepository.findByNguoiNhan(nguoiNhanId);

        return list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Đếm thông báo chưa đọc
     */
    public long countChuaDoc(Integer nguoiNhanId) {
        return thongBaoRepository.countChuaDoc(nguoiNhanId);
    }

    /**
     * Chấp nhận thông báo công việc
     * - Đổi trạng thái thông báo → DA_CHAP_NHAN
     * - Gán nhân viên phụ trách cho khách hàng
     */
    @Transactional
    public void chapNhan(Integer maThongBao, Integer nguoiNhanId) {

        ThongBao thongBao = thongBaoRepository.findById(maThongBao)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        // Kiểm tra đã xử lý chưa
        if (ThongBao.TT_DA_CHAP_NHAN.equals(thongBao.getTrangThai())
                || ThongBao.TT_DA_TU_CHOI.equals(thongBao.getTrangThai())) {
            throw new RuntimeException("Thông báo này đã được xử lý");
        }

        thongBao.setTrangThai(ThongBao.TT_DA_CHAP_NHAN);
        thongBaoRepository.save(thongBao);

        // Gán nhân viên phụ trách cho khách hàng
        if (thongBao.getMaKhachHang() != null) {
            KhachHang kh = khachHangRepository.findById(thongBao.getMaKhachHang())
                    .orElse(null);

            if (kh != null) {
                kh.setMaNhanVienPhuTrach(nguoiNhanId);
                khachHangRepository.save(kh);
            }
        }
    }

    /**
     * Từ chối thông báo công việc
     * - Đổi trạng thái → DA_TU_CHOI + lưu lý do
     * - Tạo thông báo TU_CHOI gửi về nguoiGuiId (hotline)
     */
    @Transactional
    public void tuChoi(Integer maThongBao, Integer nguoiNhanId, String lyDoTuChoi) {

        ThongBao thongBao = thongBaoRepository.findById(maThongBao)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        // Kiểm tra đã xử lý chưa
        if (ThongBao.TT_DA_CHAP_NHAN.equals(thongBao.getTrangThai())
                || ThongBao.TT_DA_TU_CHOI.equals(thongBao.getTrangThai())) {
            throw new RuntimeException("Thông báo này đã được xử lý");
        }

        thongBao.setTrangThai(ThongBao.TT_DA_TU_CHOI);
        thongBao.setLyDoTuChoi(lyDoTuChoi);
        thongBaoRepository.save(thongBao);

        // Tạo thông báo phản hồi gửi về hotline (nguoiGuiId)
        if (thongBao.getNguoiGuiId() != null) {

            // Lấy tên nhân viên từ chối
            String tenNguoiTuChoi = "Nhân viên";
            NhanVien nv = nhanVienRepository.findById(nguoiNhanId).orElse(null);
            if (nv != null) {
                tenNguoiTuChoi = nv.getHoTen();
            }

            // Lấy tên khách hàng
            String tenKhachHang = "";
            if (thongBao.getMaKhachHang() != null) {
                KhachHang kh = khachHangRepository.findById(thongBao.getMaKhachHang()).orElse(null);
                if (kh != null) {
                    tenKhachHang = kh.getTenKhachHang();
                }
            }

            ThongBao phanHoi = ThongBao.builder()
                    .tieuDe("Từ chối tiếp nhận khách hàng")
                    .noiDung(tenNguoiTuChoi + " đã từ chối tiếp nhận khách hàng "
                            + tenKhachHang + ". Lý do: " + lyDoTuChoi)
                    .loaiThongBao("TU_CHOI")
                    .nguoiGuiId(nguoiNhanId)
                    .nguoiNhanId(thongBao.getNguoiGuiId())
                    .maKhachHang(thongBao.getMaKhachHang())
                    .trangThai(ThongBao.TT_CHUA_DOC)
                    .lyDoTuChoi(lyDoTuChoi)
                    .build();

            thongBaoRepository.save(phanHoi);
        }
    }

    /**
     * Đánh dấu đã đọc
     */
    @Transactional
    public void danhDauDaDoc(Integer maThongBao) {

        ThongBao thongBao = thongBaoRepository.findById(maThongBao)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        if (ThongBao.TT_CHUA_DOC.equals(thongBao.getTrangThai())) {
            thongBao.setTrangThai(ThongBao.TT_DA_DOC);
            thongBaoRepository.save(thongBao);
        }
    }

    /**
     * Đánh dấu tất cả đã đọc
     */
    @Transactional
    public void danhDauTatCaDaDoc(Integer nguoiNhanId) {
        thongBaoRepository.markAllAsRead(nguoiNhanId);
    }

    /**
     * Map Entity → DTO
     */
    private ThongBaoResponse toResponse(ThongBao tb) {

        ThongBaoResponse.ThongBaoResponseBuilder builder = ThongBaoResponse.builder()
                .maThongBao(tb.getMaThongBao())
                .tieuDe(tb.getTieuDe())
                .noiDung(tb.getNoiDung())
                .loaiThongBao(tb.getLoaiThongBao())
                .trangThai(tb.getTrangThai())
                .lyDoTuChoi(tb.getLyDoTuChoi())
                .nguoiGuiId(tb.getNguoiGuiId())
                .maKhachHang(tb.getMaKhachHang());

        // Format ngày tạo
        if (tb.getNgayTao() != null) {
            builder.ngayTao(tb.getNgayTao().format(FORMATTER));
        }

        // Lấy tên người gửi
        if (tb.getNguoiGuiId() != null) {
            NhanVien nguoiGui = nhanVienRepository
                    .findById(tb.getNguoiGuiId()).orElse(null);

            if (nguoiGui != null) {
                builder.tenNguoiGui(nguoiGui.getHoTen());
            }
        }

        // Lấy thông tin khách hàng
        if (tb.getMaKhachHang() != null) {
            KhachHang kh = khachHangRepository
                    .findById(tb.getMaKhachHang()).orElse(null);

            if (kh != null) {
                builder.tenKhachHang(kh.getTenKhachHang())
                        .soDienThoai(kh.getSoDienThoai())
                        .email(kh.getEmail())
                        .diaChi(kh.getDiaChi())
                        .cccd(kh.getCccd())
                        .nguonDangKy(kh.getNguonDangKy())
                        .nhuCauHoTro(kh.getNhuCauHoTro())
                        .ghiChu(kh.getGhiChu());
                        
                if (kh.getNgayDangKy() != null) {
                    builder.ngayDangKy(kh.getNgayDangKy().format(FORMATTER));
                }
            }
        }

        return builder.build();
    }
}
