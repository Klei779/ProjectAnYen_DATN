<script setup>
import {
  ref,
  computed,
  watch,
  onMounted,
  onUnmounted,
} from "vue";
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import QRCode from "qrcode";

import PopChiTietDonHang from "./PopChiTietDonHang.vue";
import {
  getDonHangsDoiTac,
  xuLyDonHang,
} from "../../services/doitacDonHangService.js";
import { taoPayooDonHang } from "../../services/donHangService.js";
import { confirmPayooTransaction } from "../../services/payooMockService.js";
import api from "../../api/api.js";

import {
  Search,
  Filter,
  Check,
  User,
  Avatar,
  Wallet,
  Calendar,
  View,
  Setting,
  Tickets,
  Refresh,
  Warning,
} from "@element-plus/icons-vue";

const route = useRoute();

// ─────────────────────────────────────────────
// Popup và trạng thái xử lý
// ─────────────────────────────────────────────
const showPopup = ref(false);
const selectedDonHang = ref(null);

const showXuLyDialog = ref(false);
const xuLyForm = ref({
  ngayGiaoDuKien: null,
});

// ── Trạng thái Payoo Mock ─────────────────────────
const showPayooDialog = ref(false);
const payooStatus = ref("waiting"); // waiting | processing | success
const payooQrImage = ref("");
const currentPayooTransaction = ref(null);
const payooSubmitting = ref(false);
const selectedOrderForPayment = ref(null);

// ── Báo cáo sự cố ─────────────────────────────
const showSuCoDialog = ref(false);
const selectedSuCoOrder = ref(null);
const suCoReason = ref("");

const canBaoCaoSuCo = (order) => {
  const status = normalizeStatus(order?.trangThai ?? order?.TrangThai ?? "");

  return ![
    "Chờ xác nhận",
    "Đã hủy",
    "Hoàn thành",
    "Từ chối",
    "Gặp sự cố",
  ].includes(status);
};

const canGiaiQuyetSuCo = (order) => {
  const status = normalizeStatus(order?.trangThai ?? order?.TrangThai ?? "");
  if (status !== "Gặp sự cố") return false;

  // Chỉ người báo cáo sự cố mới được giải quyết
  const nguoiBaoCao = order?.nguoiBaoCaoSuCo || order?.NguoiBaoCaoSuCo;
  if (!nguoiBaoCao) return false;

  // Lấy thông tin người đăng nhập hiện tại từ localStorage
  try {
    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : null;
    const currentUser = user?.tenDangNhap || localStorage.getItem('tenDangNhap');
    return currentUser === nguoiBaoCao;
  } catch (error) {
    console.error("Lỗi khi lấy thông tin user:", error);
    return false;
  }
};

const openSuCoDialog = async (order) => {
  // Reload danh sách đơn hàng để lấy trạng thái mới nhất
  await fetchDonHangs();

  // Lấy đơn hàng đã cập nhật
  const maDonHang = order?.maDonHang ?? order?.MaDonHang ?? order?.id;
  if (maDonHang) {
    const updatedOrder = donHangs.value.find(dh =>
      (dh.maDonHang ?? dh.MaDonHang ?? dh.id) === maDonHang
    );
    if (updatedOrder) {
      selectedSuCoOrder.value = updatedOrder;
    } else {
      selectedSuCoOrder.value = order;
    }
  } else {
    selectedSuCoOrder.value = order;
  }

  suCoReason.value = "";
  showSuCoDialog.value = true;
};

const closeSuCoDialog = () => {
  showSuCoDialog.value = false;
  selectedSuCoOrder.value = null;
  suCoReason.value = "";
};

const isSuCoReasonValid = computed(() => {
  return suCoReason.value.trim().length > 3;
});

const confirmBaoCaoSuCo = async () => {
  if (!selectedSuCoOrder.value) {
    ElMessage.warning("Chưa chọn đơn hàng cần báo cáo sự cố");
    return;
  }

  if (!isSuCoReasonValid.value) {
    ElMessage.warning("Lý do sự cố phải trên 3 ký tự");
    return;
  }

  try {
    const maDonHang = selectedSuCoOrder.value?.maDonHang || selectedSuCoOrder.value?.MaDonHang;

    await api.post(`/api/doi-tac/quan-ly-don-hang/${maDonHang}/bao-cao-su-co`, {
      lyDoSuCo: suCoReason.value.trim()
    });

    ElMessage.success("Báo cáo sự cố thành công");

    closeSuCoDialog();
    showPopup.value = false;

    await fetchDonHangs(true);
  } catch (error) {
    console.error("Lỗi khi báo cáo sự cố:", error);

    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Báo cáo sự cố thất bại"
    );
  }
};

const confirmGiaiQuyetSuCo = async (order) => {
  try {
    await ElMessageBox.confirm(
      "Bạn có chắc chắn muốn giải quyết sự cố này? Đơn hàng sẽ được khôi phục trạng thái cũ.",
      "Xác nhận giải quyết sự cố",
      {
        confirmButtonText: "Đồng ý",
        cancelButtonText: "Hủy",
        type: "warning",
      }
    );

    const maDonHang = order?.maDonHang || order?.MaDonHang;

    await api.post(`/api/doi-tac/quan-ly-don-hang/${maDonHang}/giai-quyet-su-co`);

    ElMessage.success("Giải quyết sự cố thành công");

    showPopup.value = false;

    // Reload danh sách không ảnh hưởng đến thông báo thành công
    try {
      await fetchDonHangs();
    } catch (loadError) {
      console.error("Lỗi khi tải lại danh sách đơn hàng:", loadError);
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("Lỗi khi giải quyết sự cố:", error);

      ElMessage.error(
          error.response?.data?.message ||
          error.response?.data ||
          "Giải quyết sự cố thất bại"
      );
    }
  }
};

const loading = ref(false);
const processingLoading = ref(false);
const actionLoadingId = ref(null);

// ─────────────────────────────────────────────
// Bộ lọc và phân trang
// ─────────────────────────────────────────────
const keyword = ref("");
const trangThaiFilter = ref("Tất cả");
const ptThanhToanFilter = ref("Tất cả");
const dateRange = ref([]);

const currentPage = ref(1);
const pageSize = ref(4);

const donHangs = ref([]);

// ─────────────────────────────────────────────
// Stepper cho đối tác:
// - Đơn hàng thông thường: 3 bước riêng của đối tác (Đã nhận -> Đang xử lý -> Đã giao)
// - Đơn hàng Website (người tạo là website): thêm Thanh toán và Hoàn thành
// ─────────────────────────────────────────────
const isWebsiteOrder = (dh) => {
  const username = String(
    dh?.tenDangNhapNhanVien ??
    dh?.nhanVienTenDangNhap ??
    dh?.nhanVien?.tenDangNhap ??
    ""
  ).trim().toLowerCase();

  const employeeName = String(
    dh?.tenNhanVien ??
    dh?.nhanVien?.hoTen ??
    dh?.nhanVien ??
    ""
  ).trim().toLowerCase();

  return username === "website" || employeeName === "website";
};

const DEFAULT_STEPS = [
  "Đã nhận",
  "Đang xử lý",
  "Đã giao",
];

const WEBSITE_STEPS = [
  "Đã nhận",
  "Đang xử lý",
  "Đã giao",
  "Thanh toán",
  "Hoàn thành",
];

const getOrderSteps = (dh) => {
  return isWebsiteOrder(dh) ? WEBSITE_STEPS : DEFAULT_STEPS;
};

const normalizeText = (value) =>
    String(value ?? "")
        .trim()
        .toLocaleLowerCase("vi-VN");
const normalizeVietnameseText = (value) => {
  return String(value ?? "")
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .replace(/đ/g, "d")
      .replace(/Đ/g, "D")
      .toLowerCase()
      .trim();
};

const getProductType = (product) => {
  return (
      product?.loai ??
      product?.phanLoai ??
      product?.tenLoai ??
      product?.category ??
      ""
  );
};

const isCoffinProduct = (product) => {
  const rawType = getProductType(product);
  const normalizedType =
      normalizeVietnameseText(rawType);

  /*
   * Có dữ liệu loại thì chỉ kiểm tra loại.
   */
  if (normalizedType) {
    return normalizedType === "quan tai" ||
        normalizedType.startsWith("quan tai ")||normalizedType === "dich vu" ||
    normalizedType.startsWith("dich vu ");
  }

  /*
   * Dữ liệu cũ chưa có loại:
   * kiểm tra tên sản phẩm làm phương án dự phòng.
   */
  const productName = normalizeVietnameseText(
      product?.ten ??
      product?.tenSanPham ??
      ""
  );

  return productName.includes("quan tai")|| productName.includes("dich vu");
};

const orderHasCoffin = (products = []) => {
  if (!Array.isArray(products)) {
    return false;
  }

  return products.some(isCoffinProduct);
};
const normalizeStatus = (value) => {
  const status = normalizeText(value);

  if (!status) return "Chờ xác nhận";

  // Xử lý số trạng thái
  if (value === 11 || value === "11") return "Gặp sự cố";

  // Ưu tiên xử lý 3 trạng thái riêng của đối tác
  if (status.includes("đã nhận")) return "Đã nhận";

  if (
      status === "xử lý" ||
      status === "đang xử lý" ||
      status.includes("đang xử lý") ||
      status.includes("đang chuẩn bị")
  ) {
    return "Đang xử lý";
  }

  if (status.includes("đã giao")) return "Đã giao";

  // Xử lý các trạng thái khác (cho tương thích)
  if (
      status === "đơn mới" ||
      status === "mới tạo" ||
      status.includes("mới tạo")
  ) {
    return "Mới tạo";
  }

  if (
      status === "xác nhận" ||
      status === "đã xác nhận" ||
      status.includes("đối tác xác nhận") ||
      status.includes("tác xác nhận")
  ) {
    return "Xác nhận";
  }

  if (
      status === "thanh toán" ||
      status.includes("chờ thanh toán")
  ) {
    return "Thanh toán";
  }

  if (
      status.includes("hoàn thành") ||
      status.includes("hoàn tất") ||
      status === "xong"
  ) {
    return "Hoàn thành";
  }

  if (status.includes("hủy") || status.includes("huỷ")) {
    return "Đã hủy";
  }

  if (status.includes("từ chối")) return "Từ chối";

  if (status.includes("sự cố") || status.includes("gặp sự cố")) {
    return "Gặp sự cố";
  }

  return String(value).trim();
};

const toBoolean = (value) => {
  if (value === true || value === 1) return true;

  const normalized = normalizeText(value);
  return normalized === "true" || normalized === "1" || normalized === "yes";
};

const normalizeDonHang = (dh) => {
  const maDonHang =
      dh.maDonHang ??
      dh.MaDonHang ??
      dh.id ??
      dh.maCode;

  // Trạng thái tổng thể của đơn hàng (do hệ thống/nhân viên quản lý)
  const overallStatus =
      dh.trangThai ??
      dh.trangThaiDonHang ??
      dh.status ??
      "";

  // Trạng thái riêng của đối tác (Đã nhận, Đang xử lý, Đã giao)
  const partnerStatus = dh.trangThaiRieng ?? "";

  // Kiểm tra nhân viên phụ trách có phải tài khoản 'website' không
  const nhanVienUsername = String(
      dh.tenDangNhapNhanVien ??
      dh.nhanVienTenDangNhap ??
      dh.nhanVien?.tenDangNhap ??
      ""
  ).trim().toLowerCase();

  const nhanVienName = String(
      dh.tenNhanVien ??
      dh.nhanVien?.hoTen ??
      dh.nhanVien ??
      ""
  ).trim().toLowerCase();

  const isWebsite = nhanVienUsername === "website" || nhanVienName === "website";

  // Các trạng thái chỉ có ở đơn hàng website (thanh toán Payoo, hoàn thành)
  const overallNorm = String(overallStatus).trim().toLowerCase();
  const isPaymentOrDone =
      overallNorm === "thanh toán" ||
      overallNorm === "hoàn thành" ||
      overallNorm === "chờ thanh toán" ||
      overallNorm.includes("hoàn thành") ||
      overallNorm.includes("thanh toán");

  /*
   * Logic chọn trạng thái hiển thị:
   * - Nếu là đơn hàng website VÀ trạng thái tổng đã là Thanh toán/Hoàn thành
   *   → ưu tiên trạng thái tổng thể để stepper tự động cập nhật
   * - Còn lại ưu tiên trangThaiRieng của đối tác (3 bước riêng)
   */
  const rawStatus =
      (isWebsite && isPaymentOrDone)
          ? (dh.trangThai ?? dh.trangThaiDonHang ?? dh.status ?? "Chờ xác nhận")
          : (partnerStatus || overallStatus || "Chờ xác nhận");

  const sanPhams = Array.isArray(dh.sanPhams)
      ? dh.sanPhams
      : Array.isArray(dh.chiTietDonHangs)
          ? dh.chiTietDonHangs
          : [];

  const coQuanTaiTuSanPham =
      orderHasCoffin(sanPhams);

  const coQuanTaiTuBackend = toBoolean(
      dh.coQuanTai ??
      dh.hasCoffin
  );

  const coQuanTai =
      coQuanTaiTuBackend ||
      coQuanTaiTuSanPham;

  const danhSachLoai = [
    ...new Set(
        sanPhams
            .map(getProductType)
            .map(type => String(type || "").trim())
            .filter(Boolean)
    )
  ];

  return {
    ...dh,

    maDonHang,

    maCode:
        dh.maCode ??
        maDonHang,

    tenKhachHang:
        dh.tenKhachHang ??
        dh.khachHang?.tenKhachHang ??
        dh.customerName ??
        "---",

    soDienThoai:
        dh.soDienThoai ??
        dh.soDienThoaiKH ??
        dh.khachHang?.soDienThoai ??
        dh.phone ??
        "Chưa có SĐT",

    tenNhanVien:
        dh.tenNhanVien ??
        dh.nhanVien?.hoTen ??
        dh.nhanVienPhuTrach ??
        dh.nhanVien ??
        "Chưa phân công",

    tenDangNhapNhanVien:
        dh.tenDangNhapNhanVien ??
        dh.nhanVienTenDangNhap ??
        dh.nhanVien?.tenDangNhap ??
        dh.tenDangNhap ??
        "",

    phuongThucThanhToan:
        dh.phuongThucThanhToan ??
        dh.tenPhuongThucThanhToan ??
        "Chưa cập nhật",

    ngayTaoDon:
        dh.ngayTaoDon ??
        dh.ngayDat ??
        dh.createdAt ??
        null,

    tongTien:
        dh.tongTien ??
        dh.tongCong ??
        dh.total ??
        0,

    trangThai: rawStatus,

    trangThaiTongThe:
        dh.trangThai ??
        dh.trangThaiDonHang ??
        dh.status,

    coHopDong: toBoolean(
        dh.coHopDong ??
        dh.daCoHopDong ??
        dh.hasContract
    ),

    sanPhams,

    coQuanTai,

    loaiSanPhamText:
        danhSachLoai.length > 0
            ? danhSachLoai.join(", ")
            : "Chưa cập nhật"
  };
};
const requiresContract = (dh) => {
  return dh?.coQuanTai === true;
};

const canProcessWithoutContract = (dh) => {
  return !requiresContract(dh);
};

const hasRequiredContract = (dh) => {
  return canProcessWithoutContract(dh) ||
      dh?.coHopDong === true;
};
const fetchDonHangs = async (showSuccess = false) => {
  loading.value = true;

  try {
    // Tải toàn bộ dữ liệu một lần rồi lọc ở frontend.
    // Làm như vậy tránh trường hợp đổi bộ lọc nhưng danh sách cũ
    // đã bị backend cắt mất các trạng thái khác.
    const data = await getDonHangsDoiTac({
      keyword: "",
      trangThai: "Tất cả",
      page: 1,
      pageSize: 100,
    });

    const items = Array.isArray(data)
        ? data
        : data?.items || [];

    donHangs.value = items.map(normalizeDonHang);

    if (showSuccess) {
      ElMessage.success("Đã làm mới danh sách đơn hàng");
    }
  } catch (error) {
    console.error("Lỗi khi tải đơn hàng đối tác:", error);
    donHangs.value = [];

    ElMessage.error(
        error.response?.data?.message ||
        "Không thể tải danh sách đơn hàng"
    );
  } finally {
    loading.value = false;
  }
};

// ─────────────────────────────────────────────
// WebSocket
// ─────────────────────────────────────────────
let stompClient = null;

const connectWebSocket = () => {
  stompClient = new Client({
    webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,

    onConnect: () => {
      const userStr = localStorage.getItem("user");
      let maDoiTac = null;

      if (userStr) {
        try {
          const userObj = JSON.parse(userStr);
          maDoiTac = userObj.maDoiTac || userObj.id;
        } catch (error) {
          console.error("Không đọc được thông tin đối tác:", error);
        }
      }

      if (!maDoiTac) return;

      stompClient.subscribe(
          `/topic/doitac/${maDoiTac}`,
          (message) => {
            ElMessage.success(
                message.body || "Bạn có cập nhật đơn hàng mới!"
            );
            fetchDonHangs();
          }
      );
    },

    onStompError: (frame) => {
      console.error("Lỗi STOMP:", frame.headers?.message);
      console.error("Chi tiết:", frame.body);
    },
  });

  stompClient.activate();
};

const openOrderFromQuery = () => {
  if (!route.query.showContract || !route.query.maDonHang) return;

  const maDonHang = Number(route.query.maDonHang);
  const targetDonHang = donHangs.value.find(
      (dh) => Number(dh.maDonHang) === maDonHang
  );

  if (targetDonHang) {
    openChiTiet(targetDonHang);
    ElMessage.success("Đã mở đơn hàng có hợp đồng");
  } else {
    ElMessage.warning("Không tìm thấy đơn hàng trong danh sách");
  }
};

onMounted(async () => {
  await fetchDonHangs();
  connectWebSocket();
  openOrderFromQuery();
});

onUnmounted(() => {
  if (stompClient) {
    stompClient.deactivate();
  }
});

// ─────────────────────────────────────────────
// Chi tiết đơn hàng
// ─────────────────────────────────────────────
const openChiTiet = (dh) => {
  selectedDonHang.value = dh;
  showPopup.value = true;
};

// ─────────────────────────────────────────────
// Xử lý đơn hàng của đối tác & Thanh toán Payoo
// ─────────────────────────────────────────────
const canPaymentOrder = (dh) => {
  if (!isWebsiteOrder(dh)) return false;
  const status = normalizeStatus(dh?.trangThai);
  return (
    status === "Đã giao" ||
    status === "Thanh toán" ||
    dh?.trangThai === "Đã giao" ||
    dh?.trangThai === "Thanh toán" ||
    dh?.trangThai === "Chờ thanh toán"
  );
};

const canStartProcessing = (dh) => {
  return (
      dh.trangThai === "Đã nhận" &&
      hasRequiredContract(dh)
  );
};

const canMarkDelivered = (dh) => {
  return (
      dh.trangThai === "Đang xử lý" &&
      hasRequiredContract(dh)
  );
};

const isPartnerActionEnabled = (dh) => {
  return (
      canStartProcessing(dh) ||
      canMarkDelivered(dh) ||
      canPaymentOrder(dh)
  );
};

const getPartnerActionLabel = (dh) => {
  if (canStartProcessing(dh)) {
    return "Bắt đầu xử lý";
  }

  if (canMarkDelivered(dh)) {
    return "Báo đã giao";
  }

  if (canPaymentOrder(dh)) {
    return "Thanh toán";
  }

  /*
   * Chỉ hiện Chờ hợp đồng khi có Quan tài.
   */
  if (
      dh.trangThai === "Đã nhận" &&
      requiresContract(dh) &&
      !dh.coHopDong
  ) {
    return "Chờ hợp đồng";
  }

  if (dh.trangThai === "Đã giao") {
    return "Đã giao hàng";
  }

  if (dh.trangThai === "Hoàn thành") {
    return "Hoàn thành";
  }

  if (dh.trangThai === "Đã hủy") {
    return "Đơn đã hủy";
  }

  if (dh.trangThai === "Từ chối") {
    return "Đơn đã từ chối";
  }

  if (dh.trangThai === "Gặp sự cố") {
    return "Gặp sự cố";
  }

  return "Chưa thể xử lý";
};

const openXuLy = (dh) => {
  /*
   * Chỉ chặn khi:
   * - Có sản phẩm Quan tài
   * - Chưa có hợp đồng
   */
  if (
      requiresContract(dh) &&
      !dh.coHopDong
  ) {
    ElMessage.warning(
        "Đơn hàng có sản phẩm Quan tài nên cần hợp đồng trước khi xử lý"
    );

    return;
  }

  selectedDonHang.value = dh;

  xuLyForm.value.ngayGiaoDuKien = null;

  showXuLyDialog.value = true;
};

const handleXuLy = async () => {
  if (!selectedDonHang.value) {
    ElMessage.warning("Không tìm thấy đơn hàng cần xử lý");
    return;
  }

  if (!xuLyForm.value.ngayGiaoDuKien) {
    ElMessage.warning("Vui lòng chọn ngày giao dự kiến");
    return;
  }

  try {
    processingLoading.value = true;

    await xuLyDonHang(
        selectedDonHang.value.maDonHang,
        xuLyForm.value.ngayGiaoDuKien
    );

    ElMessage.success(
        "Đã tiếp nhận xử lý và cập nhật ngày giao dự kiến"
    );

    showXuLyDialog.value = false;
    await fetchDonHangs();
  } catch (error) {
    console.error("Lỗi khi xử lý đơn hàng:", error);

    ElMessage.error(
        error.response?.data?.message ||
        "Có lỗi xảy ra khi xử lý đơn hàng"
    );
  } finally {
    processingLoading.value = false;
  }
};

const baoDaGiao = async (dh) => {
  try {
    actionLoadingId.value = dh.maDonHang;

    await api.put(
        `/api/don-hang/${dh.maDonHang}/doi-tac-bao-da-giao`
    );

    ElMessage.success("Đã cập nhật trạng thái: Đã giao");
    await fetchDonHangs();
  } catch (error) {
    console.error("Lỗi khi báo đã giao:", error);

    ElMessage.error(
        error.response?.data?.message ||
        "Có lỗi xảy ra khi báo đã giao"
    );
  } finally {
    actionLoadingId.value = null;
  }
};

const getOrderAmount = (order) => {
  const rawAmount =
      order?.tongTien ??
      order?.TongTien ??
      order?.tongCong ??
      order?.thanhTien ??
      order?.ThanhTien ??
      order?.totalAmount ??
      order?.total ??
      0;

  if (typeof rawAmount === "number") {
    return Number.isFinite(rawAmount)
        ? Math.max(0, Math.round(rawAmount))
        : 0;
  }

  let value = String(rawAmount)
      .replace(/[₫đ\s]/gi, "")
      .trim();

  // Dạng tiền hiển thị Việt Nam: 1.250.000
  if (/^\d{1,3}(\.\d{3})+$/.test(value)) {
    value = value.replace(/\./g, "");
  } else {
    // Dạng BigDecimal từ backend: 1250000.00
    value = value.replace(/,/g, "");
  }

  const amount = Number(value);

  return Number.isFinite(amount)
      ? Math.max(0, Math.round(amount))
      : 0;
};

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const resetPayoo = () => {
  payooStatus.value = "waiting";
  payooQrImage.value = "";
  currentPayooTransaction.value = null;
  selectedOrderForPayment.value = null;
  payooSubmitting.value = false;
};

const openPaymentDialog = async (dh) => {
  selectedOrderForPayment.value = dh;
  const amount = getOrderAmount(dh);

  if (amount < 1000) {
    ElMessage.warning("Số tiền đơn hàng tối thiểu là 1.000đ để thanh toán qua QR");
    return;
  }

  payooSubmitting.value = true;
  try {
    const maDonHang = dh?.maDonHang ?? dh?.MaDonHang ?? dh?.id;
    const transaction = await taoPayooDonHang(maDonHang, amount);
    currentPayooTransaction.value = transaction;

    const qrContent = [
      `MA_GIAO_DICH=${transaction.maGiaoDich}`,
      `LOAI=${transaction.loaiGiaoDich}`,
      `SO_TIEN=${transaction.soTien}`
    ].join("|");

    payooQrImage.value = await QRCode.toDataURL(qrContent, {
      width: 270,
      margin: 2
    });

    payooStatus.value = "waiting";
    showPayooDialog.value = true;
  } catch (error) {
    console.error("Không tạo được QR:", error);
    ElMessage.error(
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        error?.response?.data ||
        "Không tạo được giao dịch QR"
    );
  } finally {
    payooSubmitting.value = false;
  }
};

const handlePayooQrClick = async () => {
  if (!currentPayooTransaction.value?.maGiaoDich) return;
  if (payooStatus.value !== "waiting") return;

  payooStatus.value = "processing";

  try {
    await delay(1200);

    const result = await confirmPayooTransaction(
        currentPayooTransaction.value.maGiaoDich
    );

    currentPayooTransaction.value = result;
    payooStatus.value = "success";

    await fetchDonHangs();
    ElMessage.success("Thanh toán đơn hàng thành công");

    await delay(1700);
    showPayooDialog.value = false;
    resetPayoo();
  } catch (error) {
    console.error("QR lỗi:", error);
    payooStatus.value = "waiting";
    ElMessage.error(
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        "Thanh toán thất bại"
    );
  }
};

const handlePartnerAction = (dh) => {
  if (canStartProcessing(dh)) {
    openXuLy(dh);
    return;
  }

  if (canMarkDelivered(dh)) {
    baoDaGiao(dh);
    return;
  }

  if (canPaymentOrder(dh)) {
    openPaymentDialog(dh);
  }
};

// ─────────────────────────────────────────────
// Stepper
// ─────────────────────────────────────────────
const normalizeStepName = (value) => {
  const status = String(value ?? "").trim().toLowerCase();

  if (status === "mới tạo") return "Mới tạo";
  if (
      status === "xác nhận" ||
      status === "đã xác nhận" ||
      status === "chờ đối tác xác nhận" ||
      status === "chờ xác nhận"
  ) {
    return "Xác nhận";
  }

  if (status === "đã nhận") return "Đã nhận";

  if (
      status === "xử lý" ||
      status === "đang xử lý" ||
      status.includes("đang xử lý") ||
      status.includes("đang chuẩn bị")
  ) {
    return "Đang xử lý";
  }

  if (status === "đã giao" || status.includes("đã giao")) return "Đã giao";

  if (
      status === "thanh toán" ||
      status === "chờ thanh toán" ||
      status.includes("chờ thanh toán") ||
      status.includes("thanh toán")
  ) {
    return "Thanh toán";
  }

  if (
      status.includes("hoàn thành") ||
      status.includes("hoàn tất") ||
      status === "xong"
  ) {
    return "Hoàn thành";
  }

  return String(value ?? "").trim();
};

const isTerminalFailedStatus = (dh) =>
    ["Đã hủy", "Từ chối", "Gặp sự cố"].includes(dh?.trangThai) ||
    ["Đã hủy", "Từ chối", "Gặp sự cố"].includes(normalizeStatus(dh?.trangThai));

const isStepCompleted = (dh, stepName) => {
  if (isTerminalFailedStatus(dh)) return false;

  const currentStatusNorm = normalizeStepName(dh.trangThai);
  const targetStepNorm = normalizeStepName(stepName);
  const steps = getOrderSteps(dh);

  // Đối với đơn hàng thông thường: nếu đã sang Thanh toán hoặc Hoàn thành thì cả 3 bước đối tác đều hoàn thành
  if (!isWebsiteOrder(dh)) {
    if (currentStatusNorm === "Thanh toán" || currentStatusNorm === "Hoàn thành") {
      return true;
    }
  }

  const currentIdx = steps.indexOf(currentStatusNorm);
  const targetIdx = steps.indexOf(targetStepNorm);

  return currentIdx >= 0 && targetIdx >= 0 && targetIdx < currentIdx;
};

const isStepActive = (dh, stepName) => {
  if (isTerminalFailedStatus(dh)) return false;

  const currentStatusNorm = normalizeStepName(dh.trangThai);
  const targetStepNorm = normalizeStepName(stepName);

  if (!isWebsiteOrder(dh)) {
    if (currentStatusNorm === "Thanh toán" || currentStatusNorm === "Hoàn thành") {
      return false;
    }
  }

  return currentStatusNorm === targetStepNorm;
};

const isLineCompleted = (dh, targetStep) =>
    isStepCompleted(dh, targetStep) ||
    isStepActive(dh, targetStep);

const trangThaiBadgeClass = (status) => {
  const normalized = normalizeStatus(status);

  if (normalized === "Đã nhận") return "badge-blue";
  if (normalized === "Đang xử lý") return "badge-orange";
  if (normalized === "Đã giao") return "badge-teal";
  if (normalized === "Thanh toán") return "badge-indigo";
  if (normalized === "Hoàn thành") return "badge-green";
  if (["Đã hủy", "Từ chối", "Gặp sự cố"].includes(normalized)) {
    return "badge-red";
  }

  return "badge-gray";
};

// ─────────────────────────────────────────────
// Format dữ liệu
// ─────────────────────────────────────────────
const formatMoney = (value) =>
    Number(value || 0).toLocaleString("vi-VN") + "đ";

const parseDate = (value) => {
  if (!value) return null;

  if (value instanceof Date) return value;

  const text = String(value).trim();

  if (/^\d{2}\/\d{2}\/\d{4}/.test(text)) {
    const [datePart] = text.split(" ");
    const [day, month, year] = datePart.split("/");
    return new Date(Number(year), Number(month) - 1, Number(day));
  }

  const parsed = new Date(text);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
};

const formatDate = (value) => {
  const date = parseDate(value);

  if (!date) return "---";

  return date.toLocaleDateString("vi-VN");
};

// ─────────────────────────────────────────────
// Lọc danh sách
// ─────────────────────────────────────────────
const filteredList = computed(() => {
  const searchValue = normalizeText(keyword.value);

  return donHangs.value.filter((dh) => {
    const matchKeyword =
        !searchValue ||
        normalizeText(dh.maCode).includes(searchValue) ||
        normalizeText(dh.tenKhachHang).includes(searchValue) ||
        normalizeText(dh.soDienThoai).includes(searchValue) ||
        normalizeText(dh.tenNhanVien).includes(searchValue);

    const matchStatus =
        trangThaiFilter.value === "Tất cả" ||
        dh.trangThai === trangThaiFilter.value;

    const matchPayment =
        ptThanhToanFilter.value === "Tất cả" ||
        dh.phuongThucThanhToan === ptThanhToanFilter.value;

    let matchDate = true;

    if (dateRange.value?.length === 2) {
      const orderDate = parseDate(dh.ngayTaoDon);
      const startDate = new Date(dateRange.value[0]);
      const endDate = new Date(dateRange.value[1]);

      startDate.setHours(0, 0, 0, 0);
      endDate.setHours(23, 59, 59, 999);

      matchDate = Boolean(
          orderDate &&
          orderDate >= startDate &&
          orderDate <= endDate
      );
    }

    return (
        matchKeyword &&
        matchStatus &&
        matchPayment &&
        matchDate
    );
  });
});

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredList.value.slice(start, start + pageSize.value);
});

watch(
    [keyword, trangThaiFilter, ptThanhToanFilter, dateRange],
    () => {
      currentPage.value = 1;
    },
    { deep: true }
);

const apDungBoLoc = () => {
  currentPage.value = 1;
  ElMessage.success("Đã áp dụng bộ lọc");
};
</script>

<template>
  <div class="don-hang-page">
    <!-- Bộ lọc giống trang nhân viên -->
    <div class="filter-bar">
      <div class="search-wrap">
        <el-icon class="search-icon">
          <Search />
        </el-icon>

        <input
            v-model="keyword"
            class="search-input"
            placeholder="Tìm kiếm mã đơn, khách hàng..."
        />
      </div>

      <div class="filter-item">
        <span class="filter-label">Trạng thái:</span>

        <select
            v-model="trangThaiFilter"
            class="select-filter"
        >
          <option>Tất cả</option>
          <option>Đã nhận</option>
          <option>Đang xử lý</option>
          <option>Đã giao</option>
        </select>
      </div>

      <div class="filter-item">
        <span class="filter-label">Thanh toán:</span>

        <select
            v-model="ptThanhToanFilter"
            class="select-filter"
        >
          <option>Tất cả</option>
          <option>Chuyển khoản</option>
          <option>Tiền mặt</option>
        </select>
      </div>

      <div class="filter-item date-picker-item">
        <span class="filter-label">Ngày tạo:</span>

        <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="Bắt đầu"
            end-placeholder="Kết thúc"
            format="DD/MM/YYYY"
            size="small"
            style="width: 200px"
        />
      </div>

      <button
          class="btn-create-order"
          @click="apDungBoLoc"
      >
        <el-icon>
          <Filter />
        </el-icon>
        Bộ lọc
      </button>

      <button
          class="btn-create-order btn-tao"
          :disabled="loading"
          @click="fetchDonHangs(true)"
      >
        <el-icon :class="{ 'is-loading': loading }">
          <Refresh />
        </el-icon>
        Làm mới
      </button>
    </div>

    <!-- Danh sách dạng card giống trang nhân viên -->
    <div class="order-grid">
      <div
          v-if="loading"
          class="empty-state"
      >
        Đang tải đơn hàng...
      </div>

      <div
          v-else-if="pagedList.length === 0"
          class="empty-state"
      >
        Không có đơn hàng nào.
      </div>

      <template v-else>
        <div
            v-for="dh in pagedList"
            :key="dh.maDonHang"
            class="order-card"
        >
          <!-- Header -->
          <div class="card-header">
            <h3 class="order-code">
              #{{ dh.maCode }}
            </h3>

            <span
                class="badge"
                :class="trangThaiBadgeClass(dh.trangThai)"
            >
            {{ dh.trangThai }}
          </span>
          </div>

          <!-- Stepper tiến trình -->
          <div class="card-stepper">
            <div class="stepper-track">
              <template
                  v-for="(step, index) in getOrderSteps(dh)"
                  :key="step"
              >
                <div
                    class="step-item"
                    :class="{
                  completed: isStepCompleted(dh, step),
                  active: isStepActive(dh, step),
                }"
                >
                  <div class="step-circle">
                    <el-icon v-if="isStepCompleted(dh, step)">
                      <Check />
                    </el-icon>

                    <div
                        v-else
                        class="inner-dot"
                    ></div>
                  </div>

                  <div class="step-label">
                    {{ step }}
                  </div>
                </div>

                <div
                    v-if="index < getOrderSteps(dh).length - 1"
                    class="step-line"
                    :class="{
                  completed: isLineCompleted(
                    dh,
                    getOrderSteps(dh)[index + 1]
                  ),
                }"
                ></div>
              </template>
            </div>
          </div>

          <!-- Thông tin đơn hàng -->
          <div class="card-info">
            <div class="info-row">
              <el-icon>
                <User />
              </el-icon>

              <span>Khách hàng:</span>
              <strong>{{ dh.tenKhachHang }}</strong>
            </div>

            <div class="info-row">
              <el-icon>
                <Avatar />
              </el-icon>

              <span>Nhân viên phụ trách:</span>
              <strong>{{ dh.tenNhanVien }}</strong>
            </div>

            <div class="info-row">
              <el-icon>
                <Wallet />
              </el-icon>

              <span>Phương thức thanh toán:</span>
              <strong>{{ dh.phuongThucThanhToan }}</strong>
            </div>

            <div class="info-row">
              <el-icon>
                <Calendar />
              </el-icon>

              <span>Ngày tạo:</span>
              <strong>{{ formatDate(dh.ngayTaoDon) }}</strong>
            </div>

            <div class="info-row">
              <el-icon>
                <Tickets />
              </el-icon>

              <span>Hợp đồng:</span>
              <strong
                  :class="!dh.coQuanTai ? 'contract-success': dh.coHopDong ? 'contract-success': 'contract-warning'"
              >
                {{
                  !dh.coQuanTai
                      ? "Đơn không cần hợp đồng"
                      : dh.coHopDong
                          ? "Đã có hợp đồng"
                          : "Chưa có hợp đồng"
                }}
              </strong>
            </div>

            <div class="order-total-row">
              <span>Tổng tiền đơn hàng</span>
              <strong>{{ formatMoney(dh.tongTien) }}</strong>
            </div>
          </div>

          <!-- Nút thao tác giống kiểu nút cập nhật của nhân viên -->
          <div class="card-actions partner-card-actions">
            <button
                class="btn-outline-green"
                @click="openChiTiet(dh)"
            >
              <el-icon>
                <View />
              </el-icon>
              Xem chi tiết
            </button>

            <button
                v-if="canBaoCaoSuCo(dh)"
                class="btn-outline-orange"
                @click="openSuCoDialog(dh)"
            >
              <el-icon>
                <Warning />
              </el-icon>
              Báo cáo sự cố
            </button>

            <button
                v-if="canGiaiQuyetSuCo(dh)"
                class="btn-filled-green"
                @click="confirmGiaiQuyetSuCo(dh)"
            >
              <el-icon>
                <Check />
              </el-icon>
              Giải quyết sự cố
            </button>

            <button
                v-if="isPartnerActionEnabled(dh)"
                class="btn-filled-green"
                :disabled="actionLoadingId === dh.maDonHang"
                @click="handlePartnerAction(dh)"
            >
              <el-icon
                  v-if="actionLoadingId !== dh.maDonHang"
              >
                <Setting />
              </el-icon>

              <i
                  v-else
                  class="fa-solid fa-spinner fa-spin"
              ></i>

              {{ getPartnerActionLabel(dh) }}
            </button>

            <button
                v-else-if="!canBaoCaoSuCo(dh) && !canGiaiQuyetSuCo(dh)"
                class="btn-disabled"
                disabled
            >
              {{ getPartnerActionLabel(dh) }}
            </button>
          </div>
        </div>
      </template>
    </div>

    <!-- Phân trang -->
    <div class="pagination-bar">
      <span class="pag-info">
        Hiển thị
        {{
          filteredList.length === 0
              ? 0
              : Math.min(
                  (currentPage - 1) * pageSize + 1,
                  filteredList.length
              )
        }}
        -
        {{
          Math.min(
              currentPage * pageSize,
              filteredList.length
          )
        }}
        của {{ filteredList.length }} đơn hàng
      </span>

      <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="filteredList.length"
          layout="prev, pager, next"
      />
    </div>

    <!-- Popup chi tiết -->
    <PopChiTietDonHang
        v-if="showPopup"
        v-model="showPopup"
        :don-hang="selectedDonHang"
    />

    <!-- Popup xử lý đơn hàng -->
    <el-dialog
        v-model="showXuLyDialog"
        title="Xử lý đơn hàng"
        width="440px"
        :close-on-click-modal="false"
        :append-to-body="true"
    >
      <div
          v-if="selectedDonHang"
          class="process-dialog-content"
      >
        <div class="process-order-code">
          Đơn hàng
          <strong>#{{ selectedDonHang.maCode }}</strong>
        </div>

        <p>
          Chọn ngày dự kiến hoàn thành và giao đơn hàng cho
          khách hàng.
        </p>

        <el-date-picker
            v-model="xuLyForm.ngayGiaoDuKien"
            type="date"
            placeholder="Chọn ngày giao dự kiến"
            format="DD/MM/YYYY"
            value-format="YYYY-MM-DD"
            :disabled-date="(date) =>
            date.getTime() < Date.now() - 86400000"
            style="width: 100%"
        />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button
              :disabled="processingLoading"
              @click="showXuLyDialog = false"
          >
            Hủy
          </el-button>

          <el-button
              type="success"
              :loading="processingLoading"
              @click="handleXuLy"
          >
            Xác nhận xử lý
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ── Popup báo cáo sự cố ── -->
    <el-dialog
        v-model="showSuCoDialog"
        title="Lý do báo cáo sự cố"
        width="460px"
        :close-on-click-modal="false"
        :append-to-body="true"
        :z-index="10070"
    >
      <div class="cancel-dialog-content">
        <p class="cancel-warning">
          Bạn cần nhập lý do báo cáo sự cố. Lý do phải trên 3 ký tự.
        </p>

        <el-input
            v-model="suCoReason"
            type="textarea"
            :rows="4"
            placeholder="Nhập lý do báo cáo sự cố..."
            maxlength="500"
            show-word-limit
        />
      </div>

      <template #footer>
        <el-button @click="closeSuCoDialog">Hủy</el-button>
        <el-button
            type="warning"
            :disabled="!isSuCoReasonValid"
            @click="confirmBaoCaoSuCo"
        >
          Xác nhận báo cáo
        </el-button>
      </template>
    </el-dialog>

    <!-- =====================================================
         POPUP QR PAYOO
    ====================================================== -->
    <el-dialog
        v-model="showPayooDialog"
        width="440px"
        :show-close="payooStatus !== 'processing'"
        :close-on-click-modal="false"
        :close-on-press-escape="payooStatus !== 'processing'"
        :append-to-body="true"
        :z-index="10070"
    >
      <div class="payoo-box">

        <!-- WAITING -->
        <template v-if="payooStatus === 'waiting'">
          <h3 class="payoo-title">
            THANH TOÁN ĐƠN HÀNG #{{ selectedOrderForPayment?.maCode || selectedOrderForPayment?.maDonHang }}
          </h3>
          <p class="payoo-description">Quét mã để thanh toán.</p>

          <img
              v-if="payooQrImage"
              :src="payooQrImage"
              class="payoo-qr"
              alt="QR Payoo"
              @click="handlePayooQrClick"
          />

          <div class="payoo-amount">
            {{ formatMoney(currentPayooTransaction?.soTien) }}
          </div>

          <div class="payoo-code">
            <span>Mã giao dịch</span>
            <strong>{{ currentPayooTransaction?.maGiaoDich }}</strong>
          </div>
        </template>

        <!-- PROCESSING -->
        <template v-else-if="payooStatus === 'processing'">
          <div class="processing-state">
            <i class="fa-solid fa-spinner fa-spin"></i>
            <h3>Đang xử lý...</h3>
            <p>Đang xác nhận giao dịch</p>
            <strong>{{ formatMoney(currentPayooTransaction?.soTien) }}</strong>
          </div>
        </template>

        <!-- SUCCESS -->
        <template v-else-if="payooStatus === 'success'">
          <div class="success-state">
            <i class="fa-solid fa-circle-check"></i>
            <h3>Thanh toán thành công</h3>
            <strong>{{ formatMoney(currentPayooTransaction?.soTien) }}</strong>
            <p>
              Đã xác nhận giao dịch. Đơn hàng đã chuyển sang trạng thái Hoàn thành.
            </p>
            <small>{{ currentPayooTransaction?.maGiaoDich }}</small>
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<!-- Dùng lại giao diện của trang quản lý đơn hàng nhân viên -->
<style
    scoped
    src="../../assets/styles/doitac/QLDonHang/TrangQLDonHang.css"
></style>

<style scoped>
.partner-card-actions {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.partner-card-actions button {
  height: 38px;
  font-size: 12px;
  padding: 0 12px;
}

.order-total-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 3px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #f7f9f8;
  font-size: 13px;
}

.order-total-row span {
  color: #6b7280;
  font-weight: 600;
}

.order-total-row strong {
  color: #17934a;
  font-size: 16px;
  font-weight: 800;
}

.contract-success {
  color: #17934a !important;
}

.contract-warning {
  color: #d97706 !important;
}

.badge-pink {
  background: #fce7f3;
  color: #be185d;
}

.badge-teal {
  background: #ccfbf1;
  color: #0f766e;
}

.badge-indigo {
  background: #e0e7ff;
  color: #4338ca;
}

.process-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.process-dialog-content p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.6;
}

.process-order-code {
  padding: 12px 14px;
  border-radius: 8px;
  background: #f3f7f5;
  color: #4b5563;
}

.process-order-code strong {
  color: #17934a;
}

.btn-create-order:disabled,
.partner-card-actions button:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.is-loading {
  animation: partner-rotate 0.8s linear infinite;
}

@keyframes partner-rotate {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .partner-card-actions {
    grid-template-columns: 1fr;
  }

  .partner-card-actions button {
    height: 36px;
    font-size: 11px;
  }

  .stepper-track {
    min-width: 650px;
  }

  .card-stepper {
    overflow-x: auto;
    padding-bottom: 8px;
  }
}
</style>
