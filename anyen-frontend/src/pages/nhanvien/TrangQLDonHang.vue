<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import QRCode from "qrcode";

import PopTaoDonHang from "./PopTaoDonHang.vue";
import PopChiTietDonHang from "./PopChiTietDonHang.vue";
import PopTaoHoaDon from "./PopTaoHoaDon.vue";

import {
  getDonHangs,
  taoDonHang,
  capNhatDonHang,
  capNhatTrangThai,
  huyDonHang as huyDonHangAPI,
  kiemTraDonHangCoHopDong,
  formatDate,
  formatCurrency,
  getTrangThaiDonHangText,
  taoPayooDonHang,
  thanhToanTienMat,
} from "../../services/donHangService.js";
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
  EditPen,
  Delete,
  Plus,
  Tickets,
  View,
  Warning,
  Money,
  Iphone,
} from "@element-plus/icons-vue";

// ── Trạng thái popup ─────────────────────────────
const showCreateOrder = ref(false);
const showChiTiet = ref(false);
const selectedDonHang = ref(null);

// ── Trạng thái Chọn phương thức & Thanh toán ───────────────────
const showPaymentMethodDialog = ref(false);
const showCashConfirmDialog = ref(false);
const selectedPaymentType = ref("qr");
const cashSubmitting = ref(false);

const showPayooDialog = ref(false);
const payooStatus = ref("waiting"); // waiting | processing | success
const payooQrImage = ref("");
const currentPayooTransaction = ref(null);
const payooSubmitting = ref(false);
const selectedOrderForPayment = ref(null);

const showTaoHoaDon = ref(false);
const selectedDonHangHoaDon = ref(null);
const hoaDonMode = ref("create");

// ── Bộ lọc ─────────────────────────────
const keyword = ref("");
const trangThaiFilter = ref("Tất cả");
const ptThanhToanFilter = ref("Tất cả");
const dateRange = ref([]);

const currentPage = ref(1);
const pageSize = ref(4);

// ── Dữ liệu ─────────────────────────────
const donHangs = ref([]);
const loading = ref(false);

const loadDonHangs = async () => {
  try {
    loading.value = true;

    const data = await getDonHangs();

    donHangs.value = Array.isArray(data)
        ? data
        : data?.items || [];

    // Convert trangThai from number to text
    donHangs.value = donHangs.value.map(dh => ({
      ...dh,
      trangThai: getTrangThaiDonHangText(dh.trangThai)
    }));
  } catch (error) {
    console.error("Lỗi khi tải đơn hàng:", error);
    ElMessage.error("Không thể tải danh sách đơn hàng");
  } finally {
    loading.value = false;
  }
};

const getOrderCode = (order) => {
  return String(
      order?.maCode ??
      order?.maDonHang ??
      order?.MaDonHang ??
      ""
  ).trim();
};

const getOrderAmount = (order) => {
  const rawAmount =
      order?.tongTien ??
      order?.TongTien ??
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

let stompClient = null;

const connectWebSocket = () => {
  const socket = new SockJS("http://localhost:8080/ws");
  stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    onConnect: () => {
      console.log("Connected to WebSocket");

      const userStr = localStorage.getItem("user");
      let maNhanVien = null;
      if (userStr) {
        try {
          const userObj = JSON.parse(userStr);
          maNhanVien = userObj.maNhanVien || userObj.id;
        } catch (e) {
          console.error("Error parsing user info", e);
        }
      }

      // Lắng nghe thông báo chung
      stompClient.subscribe("/topic/nhanvien", (message) => {
        ElMessage.success(message.body || "Có thông báo mới!");
        loadDonHangs(); // Tự động reload danh sách đơn hàng
      });

      // Lắng nghe thông báo cá nhân
      if (maNhanVien) {
        stompClient.subscribe(`/topic/nhanvien/${maNhanVien}`, (message) => {
          ElMessage.success(message.body || "Bạn có thông báo mới!");
          loadDonHangs();
        });
      }
    },
    onStompError: (frame) => {
      console.error("Broker reported error: " + frame.headers["message"]);
      console.error("Additional details: " + frame.body);
    },
  });

  stompClient.activate();
};

onMounted(() => {
  loadDonHangs();
  connectWebSocket();
});

onUnmounted(() => {
  if (stompClient) {
    stompClient.deactivate();
  }
});

// ── Helper mã đơn hàng ─────────────────────────────
const getMaDonHang = (dh) => {
  return dh?.maDonHang || dh?.MaDonHang || dh?.id;
};

// ── Hủy đơn hàng ─────────────────────────────
const showCancelDialog = ref(false);
const selectedCancelOrder = ref(null);
const cancelReason = ref("");

// ── Báo cáo sự cố ─────────────────────────────
const showSuCoDialog = ref(false);
const selectedSuCoOrder = ref(null);
const suCoReason = ref("");

const canBaoCaoSuCo = (order) => {
  const status = order?.trangThai ?? order?.TrangThai ?? "";

  return ![
    "Đã hủy",
    "Hoàn thành",
    "Từ chối",
    "Gặp sự cố",
  ].includes(status);
};

const canGiaiQuyetSuCo = (order) => {
  const status = order?.trangThai ?? order?.TrangThai ?? "";
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
  await loadDonHangs();

  // Lấy đơn hàng đã cập nhật
  const maDonHang = getMaDonHang(order);
  if (maDonHang) {
    const updatedOrder = donHangs.value.find(dh => getMaDonHang(dh) === maDonHang);
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
    const maDonHang = getMaDonHang(selectedSuCoOrder.value);

    await api.post(`/api/nhan-vien/don-hang/${maDonHang}/bao-cao-su-co`, {
      lyDoSuCo: suCoReason.value.trim()
    });

    ElMessage.success("Báo cáo sự cố thành công");

    closeSuCoDialog();
    showChiTiet.value = false;

    await loadDonHangs();
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

    const maDonHang = getMaDonHang(order);

    await api.post(`/api/nhan-vien/don-hang/${maDonHang}/giai-quyet-su-co`);

    ElMessage.success("Giải quyết sự cố thành công");

    showChiTiet.value = false;

    // Reload danh sách không ảnh hưởng đến thông báo thành công
    try {
      await loadDonHangs();
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

const isCancelReasonValid = computed(() => {
  return cancelReason.value.trim().length > 3;
});

const canCancelOrder = (order) => {
  const status = order?.trangThai ?? order?.TrangThai ?? "";

  return ![
    "Đã hủy",
    "Hoàn thành",
    "Từ chối",
  ].includes(status);
};

const openCancelDialog = (order) => {
  selectedCancelOrder.value = order;
  cancelReason.value = "";
  showCancelDialog.value = true;
};

const closeCancelDialog = () => {
  showCancelDialog.value = false;
  selectedCancelOrder.value = null;
  cancelReason.value = "";
};

const confirmCancelOrder = async () => {
  if (!selectedCancelOrder.value) {
    ElMessage.warning("Chưa chọn đơn hàng cần hủy");
    return;
  }

  if (!isCancelReasonValid.value) {
    ElMessage.warning("Lý do hủy phải trên 3 ký tự");
    return;
  }

  try {
    const maDonHang = getMaDonHang(selectedCancelOrder.value);

    await huyDonHangAPI(maDonHang, cancelReason.value.trim());

    ElMessage.success("Hủy đơn hàng thành công");

    closeCancelDialog();
    showChiTiet.value = false;

    await loadDonHangs();
  } catch (error) {
    console.error("Lỗi khi hủy đơn hàng:", error);

    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Hủy đơn hàng thất bại"
    );
  }
};

// Hàm này dùng cho popup chi tiết nếu PopChiTietDonHang emit @huy-don
const huyDon = (orderOrId) => {
  if (typeof orderOrId === "object") {
    openCancelDialog(orderOrId);
    return;
  }

  const order = selectedDonHang.value || {
    maDonHang: orderOrId,
  };

  openCancelDialog(order);
};

// ── Lọc + tìm kiếm ─────────────────────────────
const filteredList = computed(() => {
  return donHangs.value.filter((dh) => {
    const kw = keyword.value.trim().toLowerCase();

    const matchKw =
        !kw ||
        dh.maCode?.toLowerCase().includes(kw) ||
        dh.tenKhachHang?.toLowerCase().includes(kw) ||
        dh.tenNhanVien?.toLowerCase().includes(kw);

    const matchTT =
        trangThaiFilter.value === "Tất cả" ||
        dh.trangThai === trangThaiFilter.value;

    const matchPT =
        ptThanhToanFilter.value === "Tất cả" ||
        dh.phuongThucThanhToan === ptThanhToanFilter.value;

    let matchDate = true;

    if (dateRange.value && dateRange.value.length === 2) {
      const orderDate = new Date(dh.ngayTaoDon || dh.NgayTaoDon);
      const startDate = new Date(dateRange.value[0]);
      const endDate = new Date(dateRange.value[1]);

      if (orderDate < startDate || orderDate > endDate) {
        matchDate = false;
      }
    }

    return matchKw && matchTT && matchPT && matchDate;
  });
});

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredList.value.slice(start, start + pageSize.value);
});

const apDungBoLoc = () => {
  currentPage.value = 1;
  ElMessage.success("Đã áp dụng bộ lọc");
};

// ── Chi tiết / sửa đơn hàng ─────────────────────────────
const hienThongBaoDonDaCoHopDong = async (dh) => {
  await ElMessageBox.alert(
      `Đơn hàng ${dh?.maCode ? "#" + dh.maCode : "này"} đã ký hợp đồng, không thể chỉnh sửa.\n\nNếu có sai sót thông tin vui lòng hủy đơn hàng và hủy hợp đồng.`,
      "Không thể chỉnh sửa đơn hàng",
      {
        confirmButtonText: "Đã hiểu",
        type: "warning",
      }
  );
};

const xemChiTiet = async (dh) => {
  try {
    const maDonHang = getMaDonHang(dh);

    if (!maDonHang) {
      ElMessage.error("Không tìm thấy mã đơn hàng");
      return;
    }

    if (dh.daCoHopDong === true) {
      await hienThongBaoDonDaCoHopDong(dh);
      return;
    }

    const daCoHopDong = await kiemTraDonHangCoHopDong(maDonHang);

    if (daCoHopDong) {
      dh.daCoHopDong = true;
      await hienThongBaoDonDaCoHopDong(dh);
      return;
    }

    selectedDonHang.value = JSON.parse(JSON.stringify(dh));
    showChiTiet.value = true;
  } catch (error) {
    console.error("Lỗi khi kiểm tra hợp đồng của đơn hàng:", error);

    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Không kiểm tra được hợp đồng của đơn hàng"
    );
  }
};

const buildUpdateOrderPayload = (order) => {
  const sanPhams = order?.sanPhams || [];

  return {
    maKhachHang: order?.MaKhachHang || order?.maKhachHang || null,
    tenKhachHang: order?.tenKhachHang || "",
    soDienThoai: order?.soDienThoaiKH || order?.soDienThoai || "",
    cccd: order?.cccd || order?.CCCD || "",
    email: order?.emailKH || order?.email || "",
    diaChi: order?.diaChiKH || order?.diaChi || "",
    ghiChu: order?.GhiChu || order?.ghiChuNoiBo || order?.ghiChu || "",
    phuongThucThanhToan:
        order?.phuongThucThanhToanCode ??
        order?.phuongThucThanhToan ??
        0,
    trangThaiThanhToan:
        order?.trangThaiThanhToanCode ??
        order?.trangThaiThanhToan ??
        0,
    items: sanPhams.map((sp) => ({
      maSanPham: sp.MaSanPham || sp.maSanPham,
      soLuong: Number(sp.SoLuong || sp.soLuong || 1),
    })),
  };
};

const handleUpdateOrder = async (order) => {
  try {
    const maDonHang = getMaDonHang(order);

    if (!maDonHang) {
      ElMessage.error("Không tìm thấy mã đơn hàng");
      return;
    }

    const payload = buildUpdateOrderPayload(order);

    if (!payload.items || payload.items.length === 0) {
      ElMessage.error("Đơn hàng phải có ít nhất 1 sản phẩm");
      return;
    }

    await capNhatDonHang(maDonHang, payload);

    ElMessage.success("Cập nhật đơn hàng thành công");

    showChiTiet.value = false;
    selectedDonHang.value = null;

    await loadDonHangs();
  } catch (error) {
    console.error("Lỗi khi cập nhật đơn hàng:", error);

    const message =
        error.response?.data?.message ||
        error.response?.data ||
        "Cập nhật đơn hàng thất bại";

    if (error.response?.status === 409) {
      await ElMessageBox.alert(message, "Không thể chỉnh sửa đơn hàng", {
        confirmButtonText: "Đã hiểu",
        type: "warning",
      });

      showChiTiet.value = false;
      selectedDonHang.value = null;

      await loadDonHangs();
      return;
    }

    ElMessage.error(message);
  }
};

// ── Hóa đơn ─────────────────────────────
const daCoHoaDon = (dh) => {
  return Boolean(dh.maHoaDon || dh.MaHoaDon || dh.daCoHoaDon);
};

const canTaoHoaDon = (dh) => {
  return (
      ["Chờ thanh toán", "Hoàn thành"].includes(dh.trangThai) &&
      !daCoHoaDon(dh)
  );
};

const taoHoaDon = (dh) => {
  hoaDonMode.value = "create";
  selectedDonHangHoaDon.value = dh;
  showTaoHoaDon.value = true;
};

const xemHoaDon = (dh) => {
  if (!dh) {
    ElMessage.warning("Không tìm thấy dữ liệu đơn hàng");
    return;
  }

  hoaDonMode.value = "view";
  selectedDonHangHoaDon.value = {
    ...dh,
    trangThaiHoaDon: dh.trangThaiHoaDon || dh.trangThaiHoaDon || "Đã in",
  };
  showTaoHoaDon.value = true;
};

// ── Stepper trạng thái ─────────────────────────────
const STEPS = [
  "Mới tạo",
  "Xác nhận",
  "Đã nhận",
  "Xử lý",
  "Đã giao",
  "Thanh toán",
  "Hoàn thành",
];

/*
 * Chuẩn hóa trạng thái trước khi đem so sánh.
 * Tránh trường hợp backend / merge code trả:
 * "xác nhận", "Xác nhận", "Đang xử lý", ...
 */
const normalizeStepStatus = (value) => {
  const status = String(value ?? "").trim().toLowerCase();

  if (status === "mới tạo") {
    return "Mới tạo";
  }

  if (
      status === "xác nhận" ||
      status === "đã xác nhận" ||
      status === "chờ đối tác xác nhận"
  ) {
    return "Xác nhận";
  }

  if (status === "đã nhận") {
    return "Đã nhận";
  }

  if (
      status === "xử lý" ||
      status === "đang xử lý"
  ) {
    return "Xử lý";
  }

  if (status === "đã giao") {
    return "Đã giao";
  }

  if (
      status === "thanh toán" ||
      status === "chờ thanh toán" ||
      status === "đã thanh toán"
  ) {
    return "Thanh toán";
  }

  if (status === "hoàn thành") {
    return "Hoàn thành";
  }

  return String(value ?? "").trim();
};

const getStepIndex = (trangThai) => {
  return STEPS.indexOf(
      normalizeStepStatus(trangThai)
  );
};

const isStepCompleted = (dh, stepName) => {
  if (
      dh.trangThai === "Đã hủy" ||
      dh.trangThai === "Từ chối" ||
      dh.trangThai === "Gặp sự cố"
  ) {
    return false;
  }

  const currentIdx =
      getStepIndex(dh.trangThai);

  const targetIdx =
      getStepIndex(stepName);

  return (
      currentIdx >= 0 &&
      targetIdx >= 0 &&
      targetIdx < currentIdx
  );
};

const isStepActive = (dh, stepName) => {
  return (
      normalizeStepStatus(dh.trangThai) ===
      normalizeStepStatus(stepName)
  );
};

const isLineCompleted = (dh, targetStep) => {
  return (
      isStepCompleted(dh, targetStep) ||
      isStepActive(dh, targetStep)
  );
};

const nextStatus = (dh) => {
  const currentIdx =
      getStepIndex(dh.trangThai);

  if (
      currentIdx >= 0 &&
      currentIdx < STEPS.length - 1 &&
      dh.trangThai !== "Đã hủy"
  ) {
    return STEPS[currentIdx + 1];
  }

  return null;
};

// ── Badge class ─────────────────────────────
const trangThaiBadgeClass = (tt) => {
  const status = normalizeStepStatus(tt);

  if (status === "Mới tạo") return "badge-yellow";
  if (status === "Xác nhận") return "badge-pink";
  if (status === "Đã nhận") return "badge-blue";
  if (status === "Xử lý") return "badge-orange";
  if (status === "Đã giao") return "badge-teal";
  if (status === "Thanh toán") return "badge-indigo";
  if (status === "Hoàn thành") return "badge-green";

  if (
      tt === "Đã hủy" ||
      tt === "Từ chối" ||
      tt === "Gặp sự cố"
  ) {
    return "badge-red";
  }

  return "badge-gray";
};

// ── Tạo đơn hàng ─────────────────────────────
const handleSaveDraft = (payload) => {
  console.log("Tạm lưu đơn hàng:", payload);
  ElMessage.success("Đã tạm lưu đơn hàng");
};

const handleCreateOrder = async (payload) => {
  try {
    await taoDonHang(payload);

    ElMessage.success("Tạo đơn hàng và gửi thông báo đối tác thành công");

    showCreateOrder.value = false;
    await loadDonHangs();
  } catch (error) {
    console.error("Lỗi khi tạo đơn hàng:", error);

    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Tạo đơn hàng thất bại"
    );
  }
};

// ── Cập nhật trạng thái ─────────────────────────────
const doUpdateStatus = async (dh, next) => {
  try {
    await capNhatTrangThai(getMaDonHang(dh), next);

    ElMessage.success(`Đã cập nhật trạng thái: ${next}`);
    await loadDonHangs();

    return true;
  } catch (error) {
    console.error("Lỗi khi cập nhật trạng thái:", error);

    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Cập nhật trạng thái thất bại"
    );

    return false;
  }
};

// Gửi đơn hàng cho đối tác (từ trạng thái "Mới tạo" → "Chờ đối tác xác nhận")
const guiDonChoDoiTac = async (dh) => {
  try {
    const maDonHang = getMaDonHang(dh);
    await api.put(`/api/don-hang/${maDonHang}/gui-doi-tac`);

    ElMessage.success("Đã gửi đơn hàng cho đối tác thành công");
    await loadDonHangs();
  } catch (error) {
    console.error("Lỗi khi gửi đơn cho đối tác:", error);

    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Gửi đơn hàng cho đối tác thất bại"
    );
  }
};

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const resetPayoo = () => {
  payooStatus.value = "waiting";
  payooQrImage.value = "";
  currentPayooTransaction.value = null;
  selectedOrderForPayment.value = null;
  payooSubmitting.value = false;
  cashSubmitting.value = false;
};

const updateNextStatus = async (dh) => {
  // Trạng thái "Mới tạo": dùng API gui-doi-tac riêng biệt
  if (dh.trangThai === "Mới tạo") {
    await guiDonChoDoiTac(dh);
    return;
  }

  const next = nextStatus(dh);

  if (!next) return;

  // Trạng thái "Đã giao" hoặc "Thanh toán": mở dialog chọn phương thức thanh toán
  if (dh.trangThai === "Đã giao" || dh.trangThai === "Thanh toán") {
    openPaymentDialog(dh);
    return;
  }

  await doUpdateStatus(dh, next);
};

const openPaymentDialog = (dh) => {
  selectedOrderForPayment.value = dh;
  selectedPaymentType.value = "qr";
  showPaymentMethodDialog.value = true;
};

const handleSelectPaymentMethod = async () => {
  showPaymentMethodDialog.value = false;
  if (selectedPaymentType.value === "cash") {
    showCashConfirmDialog.value = true;
  } else {
    await startQrPayment(selectedOrderForPayment.value);
  }
};

const handleCashPaymentConfirm = async () => {
  if (!selectedOrderForPayment.value) return;
  const maDonHang = getMaDonHang(selectedOrderForPayment.value);
  cashSubmitting.value = true;

  try {
    await thanhToanTienMat(maDonHang);
    ElMessage.success("Xác nhận thanh toán tiền mặt thành công");
    showCashConfirmDialog.value = false;
    await loadDonHangs();
    resetPayoo();
  } catch (error) {
    console.error("Lỗi khi xác nhận thanh toán tiền mặt:", error);
    ElMessage.error(
        error?.response?.data?.message ||
        error?.response?.data ||
        "Xác nhận thanh toán tiền mặt thất bại"
    );
  } finally {
    cashSubmitting.value = false;
  }
};

const startQrPayment = async (dh) => {
  selectedOrderForPayment.value = dh;
  const amount = getOrderAmount(dh);

  if (amount < 1000) {
    ElMessage.warning("Số tiền đơn hàng tối thiểu là 1.000đ để thanh toán qua Payoo");
    return;
  }

  payooSubmitting.value = true;
  try {
    const maDonHang = getMaDonHang(dh);
    const transaction = await taoPayooDonHang(maDonHang, amount);
    currentPayooTransaction.value = transaction;

    const qrContent = [
      "PAYOO MOCK",
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
    console.error("Không tạo được Payoo:", error);
    ElMessage.error(
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        error?.response?.data ||
        "Không tạo được giao dịch Payoo"
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

    await loadDonHangs();
    ElMessage.success("Thanh toán đơn hàng thành công");

    await delay(1700);
    showPayooDialog.value = false;
    resetPayoo();
  } catch (error) {
    console.error("Callback Payoo lỗi:", error);
    payooStatus.value = "waiting";
    ElMessage.error(
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        "Thanh toán Payoo thất bại"
    );
  }
};
</script>

<template>
  <div class="don-hang-page">
    <!-- ── Bộ lọc ── -->
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
          <option>Mới tạo</option>
          <option>Xác nhận</option>
          <option>Đã nhận</option>
          <option>Xử lý</option>
          <option>Thanh toán</option>
          <option>Hoàn thành</option>
          <option>Đã hủy</option>
          <option>Từ chối</option>
        </select>
      </div>

      <div class="filter-item">
        <span class="filter-label">Phương thức thanh toán:</span>

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
          @click="showCreateOrder = true"
      >
        <el-icon>
          <Plus />
        </el-icon>
        Tạo đơn hàng
      </button>
    </div>

    <!-- ── Grid đơn hàng ── -->
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

      <div
          v-for="dh in pagedList"
          :key="dh.maDonHang || dh.MaDonHang"
          class="order-card"
      >
        <!-- Header -->
        <div class="card-header">
          <h3 class="order-code">#{{ dh.maCode }}</h3>

          <span
              class="badge"
              :class="trangThaiBadgeClass(dh.trangThai)"
          >
            {{ dh.trangThai }}
          </span>
        </div>

        <!-- Stepper ngang -->
        <div class="card-stepper">
          <div class="stepper-track">
            <template
                v-for="(step, idx) in STEPS"
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
                  v-if="idx < STEPS.length - 1"
                  class="step-line"
                  :class="{ completed: isLineCompleted(dh, STEPS[idx + 1]) }"
              ></div>
            </template>
          </div>
        </div>

        <!-- Thông tin -->
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
            <strong>{{ dh.tenNhanVien || "Không có" }}</strong>
          </div>

          <div class="info-row">
            <el-icon>
              <Wallet />
            </el-icon>

            <span>Phương thức thanh toán:</span>
            <strong>{{ dh.phuongThucThanhToan || "Không có" }}</strong>
          </div>

          <div class="info-row">
            <el-icon>
              <Calendar />
            </el-icon>

            <span>Ngày tạo:</span>
            <strong>
              {{ formatDate(dh.ngayTaoDon || dh.NgayTaoDon) }}
            </strong>
          </div>
        </div>

        <!-- Actions -->
        <div class="card-actions">
          <button
              class="btn-outline-green"
              @click="xemChiTiet(dh)"
          >
            <el-icon>
              <EditPen />
            </el-icon>
            Sửa
          </button>

          <button
              v-if="canCancelOrder(dh)"
              class="btn-outline-red"
              @click="openCancelDialog(dh)"
          >
            <el-icon>
              <Delete />
            </el-icon>
            Hủy
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
              v-if="daCoHoaDon(dh)"
              class="btn-invoice-created"
              @click.stop="xemHoaDon(dh)"
          >
            <el-icon><View /></el-icon>
            Xem hóa đơn
          </button>

          <button
              v-else-if="canTaoHoaDon(dh)"
              class="btn-filled-blue"
              @click.stop="taoHoaDon(dh)"
          >
            <el-icon><Tickets /></el-icon>
            Tạo hóa đơn
          </button>

          <button
              v-if="dh.trangThai === 'Mới tạo'"
              class="btn-filled-green"
              @click="updateNextStatus(dh)"
          >
            Gửi đối tác
          </button>

          <button
              v-else-if="nextStatus(dh) && dh.trangThai === 'Đã giao'"
              class="btn-filled-green"
              @click="openPaymentDialog(dh)"
          >
            Thanh toán
          </button>

          <button
              v-else-if="dh.trangThai === 'Xác nhận' || dh.trangThai === 'Xử lý'"
              class="btn-disabled"
              disabled
          >
            Chờ đối tác giao
          </button>

          <button
              v-else
              class="btn-disabled"
              disabled
          >
            Hoàn tất
          </button>
        </div>
      </div>
    </div>

    <!-- ── Phân trang ── -->
    <div class="pagination-bar">
      <span class="pag-info">
        Hiển thị
        {{ Math.min((currentPage - 1) * pageSize + 1, filteredList.length) }}
        -
        {{ Math.min(currentPage * pageSize, filteredList.length) }}
        của {{ filteredList.length }} đơn hàng
      </span>

      <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="filteredList.length"
          layout="prev, pager, next"
      />
    </div>

    <!-- ── Popup tạo đơn hàng ── -->
    <PopTaoDonHang
        v-if="showCreateOrder"
        @close="showCreateOrder = false"
        @submit="handleCreateOrder"
        @save-draft="handleSaveDraft"
    />

    <!-- ── Popup tạo / xem hóa đơn ── -->
    <PopTaoHoaDon
        v-model="showTaoHoaDon"
        :don-hang="selectedDonHangHoaDon"
        :mode="hoaDonMode"
        @created="loadDonHangs"
    />

    <!-- ── Popup chi tiết / sửa đơn hàng ── -->
    <PopChiTietDonHang
        v-model="showChiTiet"
        :don-hang="selectedDonHang"
        @huy-don="huyDon"
        @dong="showChiTiet = false"
        @luu="handleUpdateOrder"
        @cap-nhat="loadDonHangs"
    />

    <!-- ── Popup hủy đơn hàng ── -->
    <el-dialog
        v-model="showCancelDialog"
        title="Lý do hủy đơn hàng"
        width="460px"
        :close-on-click-modal="false"
        :append-to-body="true"
        :z-index="10070"
    >
      <div class="cancel-dialog-content">
        <p class="cancel-warning">
          Bạn cần nhập lý do hủy đơn hàng. Lý do phải trên 3 ký tự.
        </p>

        <el-input
            v-model="cancelReason"
            type="textarea"
            :rows="4"
            placeholder="Nhập lý do hủy đơn hàng..."
            maxlength="500"
            show-word-limit
        />
      </div>

      <template #footer>
        <el-button @click="showCancelDialog = false">Hủy</el-button>
        <el-button
            type="danger"
            :disabled="!isCancelReasonValid"
            @click="confirmCancelOrder"
        >
          Xác nhận hủy
        </el-button>
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
         POPUP CHỌN PHƯƠNG THỨC THANH TOÁN
    ====================================================== -->
    <el-dialog
        v-model="showPaymentMethodDialog"
        width="520px"
        title="Chọn phương thức thanh toán"
        :close-on-click-modal="false"
        :append-to-body="true"
        :z-index="10060"
    >
      <div class="payment-method-modal">
        <div class="order-summary-box">
          <div class="summary-line">
            <span>Đơn hàng:</span>
            <strong>#{{ selectedOrderForPayment?.maCode || selectedOrderForPayment?.maDonHang }}</strong>
          </div>
          <div class="summary-line" v-if="selectedOrderForPayment?.tenKhachHang">
            <span>Khách hàng:</span>
            <strong>{{ selectedOrderForPayment?.tenKhachHang }}</strong>
          </div>
          <div class="summary-line total-line">
            <span>Tổng tiền thanh toán:</span>
            <strong class="money-text">{{ formatCurrency(getOrderAmount(selectedOrderForPayment)) }}</strong>
          </div>
        </div>

        <p class="select-label">Vui lòng chọn hình thức khách hàng thanh toán:</p>

        <div class="payment-options-grid">
          <div
              class="payment-option-card"
              :class="{ active: selectedPaymentType === 'cash' }"
              @click="selectedPaymentType = 'cash'"
          >
            <div class="option-icon cash-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="option-info">
              <h4>Tiền mặt (Trực tiếp / COD)</h4>
              <p>Thu tiền mặt trực tiếp từ khách hàng khi bàn giao đơn hàng</p>
            </div>
            <div class="option-radio">
              <span class="radio-circle"></span>
            </div>
          </div>

          <div
              class="payment-option-card"
              :class="{ active: selectedPaymentType === 'qr' }"
              @click="selectedPaymentType = 'qr'"
          >
            <div class="option-icon qr-icon">
              <el-icon><Iphone /></el-icon>
            </div>
            <div class="option-info">
              <h4>Chuyển khoản (Quét mã QR)</h4>
              <p>Khách quét mã QR chuyển khoản ngân hàng nhanh chóng 24/7</p>
            </div>
            <div class="option-radio">
              <span class="radio-circle"></span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showPaymentMethodDialog = false">Hủy</el-button>
        <el-button
            type="primary"
            @click="handleSelectPaymentMethod"
        >
          Tiếp tục
        </el-button>
      </template>
    </el-dialog>

    <!-- =====================================================
         POPUP XÁC NHẬN THANH TOÁN TIỀN MẶT
    ====================================================== -->
    <el-dialog
        v-model="showCashConfirmDialog"
        width="480px"
        title="Xác nhận thanh toán tiền mặt"
        :close-on-click-modal="false"
        :append-to-body="true"
        :z-index="10065"
    >
      <div class="cash-confirm-modal">
        <div class="cash-alert-banner">
          <el-icon><Check /></el-icon>
          <div>
            <strong>Xác nhận thu tiền mặt</strong>
            <p>Vui lòng kiểm tra kỹ số tiền đã nhận từ khách hàng.</p>
          </div>
        </div>

        <div class="cash-detail-list">
          <div class="detail-row">
            <span>Mã đơn hàng:</span>
            <strong>#{{ selectedOrderForPayment?.maCode || selectedOrderForPayment?.maDonHang }}</strong>
          </div>
          <div class="detail-row" v-if="selectedOrderForPayment?.tenKhachHang">
            <span>Khách hàng:</span>
            <strong>{{ selectedOrderForPayment?.tenKhachHang }}</strong>
          </div>
          <div class="detail-row" v-if="selectedOrderForPayment?.soDienThoai">
            <span>Số điện thoại:</span>
            <span>{{ selectedOrderForPayment?.soDienThoai }}</span>
          </div>
          <div class="detail-row highlight-amount">
            <span>Số tiền cần thu:</span>
            <strong class="amount-val">{{ formatCurrency(getOrderAmount(selectedOrderForPayment)) }}</strong>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showCashConfirmDialog = false; showPaymentMethodDialog = true">Quay lại</el-button>
        <el-button
            type="success"
            :loading="cashSubmitting"
            @click="handleCashPaymentConfirm"
        >
          Xác nhận đã nhận đủ tiền
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
          <p class="payoo-description">Quét mã QR để thanh toán.</p>

          <img
              v-if="payooQrImage"
              :src="payooQrImage"
              class="payoo-qr"
              alt="QR Payoo"
              @click="handlePayooQrClick"
          />

          <div class="payoo-amount">
            {{ formatCurrency(currentPayooTransaction?.soTien) }}
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
            <strong>{{ formatCurrency(currentPayooTransaction?.soTien) }}</strong>
          </div>
        </template>

        <!-- SUCCESS -->
        <template v-else-if="payooStatus === 'success'">
          <div class="success-state">
            <i class="fa-solid fa-circle-check"></i>
            <h3>Thanh toán thành công</h3>
            <strong>{{ formatCurrency(currentPayooTransaction?.soTien) }}</strong>
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

<style scoped src="../../assets/styles/nhanvien/QLDonHang/TrangQLDonHang.css"></style>
<style scoped>
.payment-method-modal {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.order-summary-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.summary-line {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #64748b;
}
.summary-line strong {
  color: #1e293b;
}
.summary-line.total-line {
  border-top: 1px dashed #cbd5e1;
  padding-top: 8px;
  margin-top: 4px;
}
.money-text {
  font-size: 16px !important;
  color: #16a34a !important;
  font-weight: 700;
}
.select-label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin: 0;
}
.payment-options-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.payment-option-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fff;
}
.payment-option-card:hover {
  border-color: #cbd5e1;
  background: #fdfdfd;
}
.payment-option-card.active {
  border-color: #3b82f6;
  background: #eff6ff;
}
.option-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}
.cash-icon {
  background: #dcfce7;
  color: #16a34a;
}
.qr-icon {
  background: #dbeafe;
  color: #2563eb;
}
.option-info {
  flex: 1;
}
.option-info h4 {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}
.option-info p {
  margin: 0;
  font-size: 12px;
  color: #64748b;
  line-height: 1.4;
}
.option-radio {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.payment-option-card.active .option-radio {
  border-color: #3b82f6;
}
.payment-option-card.active .radio-circle {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #3b82f6;
}
.cash-confirm-modal {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.cash-alert-banner {
  display: flex;
  gap: 12px;
  padding: 12px 14px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  color: #15803d;
}
.cash-alert-banner .el-icon {
  font-size: 20px;
  margin-top: 2px;
}
.cash-alert-banner strong {
  display: block;
  font-size: 13px;
  margin-bottom: 2px;
}
.cash-alert-banner p {
  margin: 0;
  font-size: 12px;
  color: #166534;
}
.cash-detail-list {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.detail-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #64748b;
}
.detail-row strong {
  color: #1e293b;
}
.detail-row.highlight-amount {
  border-top: 1px dashed #cbd5e1;
  padding-top: 10px;
  margin-top: 4px;
}
.amount-val {
  font-size: 18px !important;
  color: #16a34a !important;
  font-weight: 700;
}
</style>