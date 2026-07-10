package vn.anyen.constants;


public class AppLabels {
    public AppLabels() {
    }
    public static final String[] TRANG_THAI_NHAN_VIEN ={
"Nghỉ việc",
            "Đang hoạt động"
    };
    public static final String[] TRANG_THAI_THONG_BAO ={
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
    public static final String[] ROLE_NHAN_VIEN ={
            "Unknow",
            "Quản lý",
            "Nhân viên tư vấn trực tiếp",
            "Nhân viên hotline"
    };
    public static final String[] TRANG_THAI_SAN_PHAM = {
            "Ẩn",
            "Đang bán",
            "Chờ xác nhận",
            "Đã duyệt",
            "Từ chối duyệt",
            "Hết hàng"
    };
    public static String getLabel(String[] labels, Integer code) {
        if (labels == null || code == null || code < 0 || code >= labels.length) {
            return "Không xác định";
        }

        return labels[code];
    }
}
