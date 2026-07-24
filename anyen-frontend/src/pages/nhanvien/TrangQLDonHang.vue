<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

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
} from "../../services/donHangService.js";
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
} from "@element-plus/icons-vue";

// ── Trạng thái popup ─────────────────────────────
const showCreateOrder = ref(false);
const showChiTiet = ref(false);
const selectedDonHang = ref(null);

const showPaymentDialog = ref(false);
const showCashConfirmDialog = ref(false);
const showTransferDialog = ref(false);
const selectedOrderForPayment = ref(null);
const selectedPaymentMethod = ref(null);

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

// ── Cấu hình tài khoản nhận tiền ─────────────────────────────
// VPBank có BIN/NAPAS ID là 970432.
// Quick Link VietQR sẽ tạo mã QR chuyển khoản thật để ứng dụng ngân hàng quét.
const BANK_CONFIG = Object.freeze({
  bankId: "970432",
  bankName: "VPBank",
  accountNumber: "140213032008",

  // Chỉ điền khi đây đúng chính xác tên chủ tài khoản tại ngân hàng.
  // Để trống vẫn quét và chuyển khoản bình thường; app ngân hàng sẽ
  // tự tra cứu và hiển thị tên người nhận thật trước khi xác nhận.
  accountName: "",
});

const qrLoadFailed = ref(false);

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

const toTransferText = (value) => {
  return String(value ?? "")
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .replace(/Đ/g, "D")
      .replace(/đ/g, "d")
      .replace(/[^a-zA-Z0-9 ]/g, " ")
      .replace(/\s+/g, " ")
      .trim()
      .toUpperCase()
      .slice(0, 50);
};

const transferContent = computed(() => {
  const orderCode = getOrderCode(selectedOrderForPayment.value);

  return toTransferText(
      orderCode
          ? `THANH TOAN DON ${orderCode}`
          : "THANH TOAN DON HANG"
  );
});

const qrPaymentUrl = computed(() => {
  const order = selectedOrderForPayment.value;

  if (!order) return "";

  const amount = getOrderAmount(order);

  if (amount <= 0) return "";

  const params = new URLSearchParams({
    amount: String(amount),
    addInfo: transferContent.value,
  });

  if (BANK_CONFIG.accountName.trim()) {
    params.set("accountName", BANK_CONFIG.accountName.trim());
  }

  return (
      `https://img.vietqr.io/image/` +
      `${BANK_CONFIG.bankId}-${BANK_CONFIG.accountNumber}-compact2.png?` +
      params.toString()
  );
});

const handleQrLoad = () => {
  qrLoadFailed.value = false;
};

const handleQrError = (event) => {
  qrLoadFailed.value = true;

  console.error(
      "Không tải được ảnh VietQR:",
      event?.target?.src
  );

  ElMessage.error(
      "Không tải được mã VietQR. Hãy kiểm tra kết nối Internet và thử lại."
  );
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
  "xác nhận",
  "Đã nhận",
  "Xử lý",
  "Đã giao",
  "Thanh toán",
  "Hoàn thành",
];

const getStepIndex = (trangThai) => {
  return STEPS.indexOf(trangThai);
};

const isStepCompleted = (dh, stepName) => {
  if (dh.trangThai === "Đã hủy") return false;

  const currentIdx = getStepIndex(dh.trangThai);
  const targetIdx = getStepIndex(stepName);

  return currentIdx >= 0 && targetIdx < currentIdx;
};

const isStepActive = (dh, stepName) => {
  return dh.trangThai === stepName;
};

const isLineCompleted = (dh, targetStep) => {
  return isStepCompleted(dh, targetStep) || isStepActive(dh, targetStep);
};

const nextStatus = (dh) => {
  const currentIdx = getStepIndex(dh.trangThai);

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
  if (tt === "Mới tạo") return "badge-yellow";
  if (tt === "tác xác nhận") return "badge-pink";
  if (tt === "Đã nhận") return "badge-blue";
  if (tt === "Xử lý") return "badge-orange";
  if (tt === "Đã giao") return "badge-teal";
  if (tt === "Thanh toán") return "badge-indigo";
  if (tt === "Hoàn thành") return "badge-green";
  if (tt === "Đã hủy" || tt === "Từ chối" || tt === "Gặp sự cố") {
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

const updateNextStatus = async (dh) => {
  const next = nextStatus(dh);

  if (!next) return;

  if (dh.trangThai === "Thanh toán") {
    if (dh.phuongThucThanhToan === "Chuyển khoản") {
      selectedOrderForPayment.value = dh;
      showPaymentDialog.value = true;
      return;
    }

    if (dh.phuongThucThanhToan === "Tiền mặt") {
      selectedOrderForPayment.value = dh;
      showCashConfirmDialog.value = true;
      return;
    }
  }

  await doUpdateStatus(dh, next);
};

const confirmPayment = async () => {
  if (!selectedOrderForPayment.value) return;

  const order = selectedOrderForPayment.value;

  await doUpdateStatus(order, "Hoàn thành");

  await loadDonHangs();

  const donMoi = donHangs.value.find(
      x => getMaDonHang(x) === getMaDonHang(order)
  );

  showPaymentDialog.value = false;
  selectedOrderForPayment.value = null;

  if (donMoi) {
    xemHoaDon(donMoi);
  }
};

const confirmCashPayment = async () => {
  if (!selectedOrderForPayment.value) return;

  const order = selectedOrderForPayment.value;

  const updated = await doUpdateStatus(order, "Hoàn thành");

  if (!updated) {
    return;
  }

  const invoiceCreated = await createInvoiceForOrder(order, 1);

  if (!invoiceCreated) {
    return;
  }

  await loadDonHangs();

  showCashConfirmDialog.value = false;
  selectedOrderForPayment.value = null;
  selectedPaymentMethod.value = null;

  ElMessage.success(
      "Đơn hàng đã hoàn thành và hóa đơn tiền mặt đã được tạo."
  );
};

const confirmTransferPayment = async () => {
  if (!selectedOrderForPayment.value) return;

  const order = selectedOrderForPayment.value;

  const updated = await doUpdateStatus(order, "Hoàn thành");

  if (!updated) {
    return;
  }

  const invoiceCreated = await createInvoiceForOrder(order, 2);

  if (!invoiceCreated) {
    return;
  }

  await loadDonHangs();

  showTransferDialog.value = false;
  selectedOrderForPayment.value = null;
  selectedPaymentMethod.value = null;

  ElMessage.success(
      "Đơn hàng đã hoàn thành và hóa đơn chuyển khoản đã được tạo."
  );
};

const openPaymentDialog = (dh) => {
  selectedOrderForPayment.value = dh;
  selectedPaymentMethod.value = null;
  qrLoadFailed.value = false;
  showPaymentDialog.value = true;
};

const confirmPaymentMethod = async () => {
  if (!selectedPaymentMethod.value) {
    ElMessage.warning("Vui lòng chọn phương thức thanh toán");
    return;
  }

  const order = selectedOrderForPayment.value;

  if (!order) {
    ElMessage.error("Không tìm thấy dữ liệu đơn hàng");
    return;
  }

  if (
      selectedPaymentMethod.value === "TRANSFER" &&
      getOrderAmount(order) <= 0
  ) {
    ElMessage.error(
        "Tổng tiền đơn hàng không hợp lệ nên chưa thể tạo mã VietQR"
    );
    return;
  }

  showPaymentDialog.value = false;
  await nextTick();

  if (selectedPaymentMethod.value === "COD") {
    showCashConfirmDialog.value = true;
    return;
  }

  qrLoadFailed.value = false;
  showTransferDialog.value = true;

  console.log("Dữ liệu đơn thanh toán:", order);
  console.log("URL VietQR:", qrPaymentUrl.value);
};
const createInvoiceForOrder = async (
    order,
    phuongThucThanhToan
) => {
  try {
    const maDonHang = getMaDonHang(order);

    if (!maDonHang) {
      ElMessage.error("Không tìm thấy mã đơn hàng để tạo hóa đơn");
      return false;
    }

    await api.post("/api/nhan-vien/hoa-don", {
      maDonHang,
      ngayIn: new Date().toISOString().split("T")[0],
      phuongThucThanhToan,
      trangThai: 1,
    });

    return true;
  } catch (error) {
    console.error("Lỗi khi tạo hóa đơn:", error);

    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Không thể tạo hóa đơn tự động"
    );

    return false;
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

    <!-- ── Popup chọn phương thức thanh toán ── -->
    <el-dialog
        v-model="showPaymentDialog"
        title="Chọn phương thức thanh toán"
        width="500px"
        :close-on-click-modal="false"
        :append-to-body="true"
        :z-index="10070"
    >
      <div class="payment-dialog-content">
        <p class="payment-info">
          Đơn hàng <b>
          #{{
            selectedOrderForPayment?.maCode ||
            selectedOrderForPayment?.maDonHang ||
            selectedOrderForPayment?.MaDonHang
          }}
        </b> đã được giao.
          Vui lòng chọn phương thức thanh toán.
        </p>

        <div class="payment-methods">
          <div
              class="payment-method-card"
              :class="{ selected: selectedPaymentMethod === 'COD' }"
              @click="selectedPaymentMethod = 'COD'"
          >
            <div class="payment-icon">
              <el-icon><Wallet /></el-icon>
            </div>
            <div class="payment-details">
              <h4>Tiền mặt (COD)</h4>
              <p>Khách hàng thanh toán bằng tiền mặt khi nhận hàng</p>
            </div>
          </div>

          <div
              class="payment-method-card"
              :class="{ selected: selectedPaymentMethod === 'TRANSFER' }"
              @click="selectedPaymentMethod = 'TRANSFER'"
          >
            <div class="payment-icon">
              <el-icon><Tickets /></el-icon>
            </div>
            <div class="payment-details">
              <h4>Chuyển khoản</h4>
              <p>Khách hàng thanh toán qua mã QR hoặc chuyển khoản</p>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showPaymentDialog = false">Hủy</el-button>
        <el-button
            type="primary"
            :disabled="!selectedPaymentMethod"
            @click="confirmPaymentMethod"
        >
          Tiếp tục
        </el-button>
      </template>
    </el-dialog>

    <!-- ── Popup xác nhận thanh toán COD ── -->
    <el-dialog
        v-model="showCashConfirmDialog"
        title="Xác nhận thanh toán tiền mặt"
        width="400px"
        :close-on-click-modal="false"
        :append-to-body="true"
        :z-index="10070"
    >
      <div class="cash-confirm-content">
        <p>Bạn xác nhận khách hàng đã thanh toán <b>tiền mặt</b> cho đơn hàng này?</p>
        <p class="order-code">#{{ selectedOrderForPayment?.maDonHang }}</p>
      </div>

      <template #footer>
        <el-button @click="showCashConfirmDialog = false">Hủy</el-button>
        <el-button type="primary" @click="confirmCashPayment">
          Xác nhận đã thanh toán
        </el-button>
      </template>
    </el-dialog>

    <!-- ── Popup thanh toán chuyển khoản ── -->
    <el-dialog
        v-model="showTransferDialog"
        title="Thanh toán chuyển khoản"
        width="500px"
        :close-on-click-modal="false"
        :append-to-body="true"
        :z-index="10070"
    >
      <div class="transfer-dialog-content">
        <p class="transfer-info">
          Vui lòng quét mã QR hoặc chuyển khoản theo thông tin bên dưới:
        </p>

        <div class="qr-section">
          <div
              v-if="qrLoadFailed"
              class="qr-error"
          >
            Không tải được mã QR. Vui lòng kiểm tra mạng rồi mở lại popup.
          </div>

          <img
              v-else-if="qrPaymentUrl"
              :key="qrPaymentUrl"
              :src="qrPaymentUrl"
              alt="Mã VietQR thanh toán đơn hàng"
              class="payment-qr-image"
              @load="handleQrLoad"
              @error="handleQrError"
          />

          <div
              v-else
              class="qr-error"
          >
            Không tạo được mã QR vì tổng tiền đơn hàng không hợp lệ.
          </div>
        </div>

        <div class="bank-info">
          <p><b>Ngân hàng:</b> {{ BANK_CONFIG.bankName }}</p>
          <p><b>Số tài khoản:</b> {{ BANK_CONFIG.accountNumber }}</p>
          <p>
            <b>Chủ tài khoản:</b>
            {{ BANK_CONFIG.accountName || "Kiểm tra tên người nhận trên ứng dụng ngân hàng" }}
          </p>
          <p>
            <b>Số tiền:</b>
            {{ formatCurrency(getOrderAmount(selectedOrderForPayment)) }}
          </p>
          <p><b>Nội dung:</b> {{ transferContent }}</p>

          <p
              class="payment-note"
              style="margin-top: 12px; color: #f59e0b; font-size: 13px;"
          >
            <i class="fa-solid fa-circle-info"></i>
            Trước khi xác nhận chuyển khoản, hãy kiểm tra đúng tên người nhận,
            số tiền và nội dung trên ứng dụng ngân hàng.
          </p>
        </div>
      </div>

      <template #footer>
        <el-button @click="showTransferDialog = false">Đóng</el-button>
        <el-button type="primary" @click="confirmTransferPayment">
          Xác nhận đã thanh toán
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped src="../../assets/styles/nhanvien/QLDonHang/TrangQLDonHang.css"></style>