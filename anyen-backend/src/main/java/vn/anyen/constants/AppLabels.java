package vn.anyen.constants;


public class AppLabels {
    public AppLabels() {
    }

    public static final String[] TRANG_THAI_NHAN_VIEN = {
            "Nghỉ việc",
            "Đang hoạt động"
    };
    public static final String[] TRANG_THAI_THONG_BAO = {
            "Chưa đọc",
            "Đã đọc",
            "Đã chấp nhận",
            "Đã từ chối",
            "Chờ xác nhận"
    };
    public static final String[] TRANG_THAI_THONG_BAO_DOI_TAC = {
            "Chờ xác nhận",
            "Đã chấp nhận",
            "Đã từ chối"
    };
    public static final String[] TRANG_THAI_SAN_PHAM = {
            "Ẩn",
            "Đang bán",
            "Chờ xác nhận",
            "Từ chối duyệt",
            "Hết hàng"
    };
    public static final String[] TEN_VAI_TRO = {
            "Unknow",
            "Quản lý",
            "Nhân viên trực tiếp",
            "Hotline"
    };
    public static String getLabel(String[] labels, Integer code) {
        if (labels == null || code == null || code < 0 || code >= labels.length) {
            return "Không xác định";
        }

        return labels[code];
    }

    // Trạng thái nhân viên
    public static final Integer NV_NGHI_VIEC = 0;
    public static final Integer NV_DANG_HOAT_DONG = 1;

    // Vai trò nhân viên
    public static final Integer VT_ADMIN = 1;

    // Trạng thái hóa đơn
    public static final Integer HD_DA_HUY = 0;
    public static final Integer HD_DA_TAO = 1;

    // Trạng thái đơn hàng
    public static final Integer DH_HOAN_THANH = 5;

    // Trạng thái thông báo
    public static final Integer TB_CHUA_DOC = 0;
    public static final Integer TB_DA_DOC = 1;
    public static final Integer TB_DA_CHAP_NHAN = 2;
    public static final Integer TB_DA_TU_CHOI = 3;
    public static final Integer TB_CHO_XAC_NHAN = 4;

    // Loại thông báo hủy hóa đơn
    public static final String TB_YEU_CAU_HUY_HOA_DON =
            "YEU_CAU_HUY_HOA_DON";

    public static final String TB_KET_QUA_HUY_HOA_DON =
            "KET_QUA_HUY_HOA_DON";
}
