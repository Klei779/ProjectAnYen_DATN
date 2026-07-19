package vn.anyen.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AiTrichXuatKhachHangResult {

    /**
     * Câu trả lời gửi lại cho khách hàng.
     */
    private String reply;

    /**
     * Thông tin khách hàng AI trích xuất được.
     */
    private CustomerInfo customerInfo;

    /**
     * Danh sách trường còn thiếu.
     */
    private List<String> missingFields = new ArrayList<>();

    /**
     * Đã đủ dữ liệu bắt buộc hay chưa.
     */
    private Boolean readyForHotline = false;

    /**
     * Khách đã xác nhận thông tin vừa tổng hợp hay chưa.
     */
    private Boolean customerConfirmed = false;

    @Getter
    @Setter
    public static class CustomerInfo {

        private String hoTen;

        private String soDienThoai;

        private String diaChi;

        private String nhuCau;

        private String thoiGianMongMuon;

        private BigDecimal nganSachDuKien;

        private String ghiChu;
    }
}