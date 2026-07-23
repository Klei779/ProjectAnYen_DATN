import api from "../api/api.js";

const API_URL = "/api/nhan-vien/don-hang";

const ORDER_STATUS_LABELS = Object.freeze({
  1: "Mới tạo",
  2: "Chờ đối tác xác nhận",
  3: "Đã nhận",
  4: "Xử lý",
  5: "Thanh toán",
  6: "Hoàn thành",
  7: "Đã hủy",
  8: "Từ chối",
  9: "Đã giao",
  10: "Đã thanh toán",
});

const PAYMENT_METHOD_LABELS = Object.freeze({
  0: "Chưa chọn",
  1: "Tiền mặt",
  2: "Chuyển khoản",
});

const PAYMENT_STATUS_LABELS = Object.freeze({
  0: "Chưa thanh toán",
  1: "Đã thanh toán",
  2: "Chờ xác nhận",
});

function normalizeLabel(value) {
  return String(value ?? "")
    .trim()
    .toLocaleLowerCase("vi-VN");
}

function buildReverseMap(labels) {
  return Object.entries(labels).reduce((result, [code, label]) => {
    result[normalizeLabel(label)] = Number(code);
    return result;
  }, {});
}

const ORDER_STATUS_CODES = Object.freeze(buildReverseMap(ORDER_STATUS_LABELS));
const PAYMENT_METHOD_CODES = Object.freeze(buildReverseMap(PAYMENT_METHOD_LABELS));
const PAYMENT_STATUS_CODES = Object.freeze(buildReverseMap(PAYMENT_STATUS_LABELS));

function toCode(value, reverseMap, fallback = 0) {
  if (typeof value === "number" && Number.isFinite(value)) {
    return Math.trunc(value);
  }

  const text = String(value ?? "").trim();

  if (/^-?\d+$/.test(text)) {
    return Number(text);
  }

  return reverseMap[normalizeLabel(text)] ?? fallback;
}

export function getTrangThaiDonHangCode(value) {
  const code = toCode(value, ORDER_STATUS_CODES, 0);

  // Dữ liệu cũ từng lưu 0. Trong nghiệp vụ hiện tại trạng thái hợp lệ bắt đầu từ 1.
  return code === 0 ? 1 : code;
}

export function getTrangThaiDonHangText(value) {
  const code = getTrangThaiDonHangCode(value);
  return ORDER_STATUS_LABELS[code] || "Chưa xác định";
}

export function getPhuongThucThanhToanCode(value) {
  return toCode(value, PAYMENT_METHOD_CODES, 0);
}

export function getPhuongThucThanhToanText(value) {
  const code = getPhuongThucThanhToanCode(value);
  return PAYMENT_METHOD_LABELS[code] || "Chưa chọn";
}

export function getTrangThaiThanhToanCode(value) {
  return toCode(value, PAYMENT_STATUS_CODES, 0);
}

export function getTrangThaiThanhToanText(value) {
  const code = getTrangThaiThanhToanCode(value);
  return PAYMENT_STATUS_LABELS[code] || "Chưa thanh toán";
}

function normalizeSanPham(sp = {}) {
  return {
    ...sp,
    maSanPham: sp.maSanPham ?? sp.MaSanPham ?? sp.id ?? null,
    MaSanPham: sp.MaSanPham ?? sp.maSanPham ?? sp.id ?? null,
    soLuong: Number(sp.soLuong ?? sp.SoLuong ?? 0),
    SoLuong: Number(sp.SoLuong ?? sp.soLuong ?? 0),
    giaTien: Number(sp.giaTien ?? sp.donGia ?? 0),
    thanhTien: Number(sp.thanhTien ?? 0),
  };
}

export function normalizeDonHang(dh = {}) {
  const rawTrangThai =
    dh.trangThai ??
    dh.TrangThai ??
    dh.trangThaiDonHang ??
    dh.status;

  const rawPhuongThuc =
    dh.phuongThucThanhToan ??
    dh.PhuongThucThanhToan;

  const rawTrangThaiThanhToan =
    dh.trangThaiThanhToan ??
    dh.TrangThaiThanhToan;

  const trangThaiCode = getTrangThaiDonHangCode(rawTrangThai);
  const phuongThucThanhToanCode = getPhuongThucThanhToanCode(rawPhuongThuc);
  const trangThaiThanhToanCode = getTrangThaiThanhToanCode(rawTrangThaiThanhToan);

  return {
    ...dh,
    maDonHang: dh.maDonHang ?? dh.MaDonHang ?? dh.id ?? null,
    MaDonHang: dh.MaDonHang ?? dh.maDonHang ?? dh.id ?? null,
    maCode:
      dh.maCode ||
      (dh.maDonHang ?? dh.MaDonHang ?? dh.id
        ? `DH${String(dh.maDonHang ?? dh.MaDonHang ?? dh.id).padStart(4, "0")}`
        : ""),
    ngayTaoDon: dh.ngayTaoDon ?? dh.NgayTaoDon ?? null,
    NgayTaoDon: dh.NgayTaoDon ?? dh.ngayTaoDon ?? null,
    ghiChu: dh.ghiChu ?? dh.GhiChu ?? dh.ghiChuNoiBo ?? "",
    GhiChu: dh.GhiChu ?? dh.ghiChu ?? dh.ghiChuNoiBo ?? "",
    trangThaiCode,
    trangThai: getTrangThaiDonHangText(trangThaiCode),
    phuongThucThanhToanCode,
    phuongThucThanhToan: getPhuongThucThanhToanText(phuongThucThanhToanCode),
    trangThaiThanhToanCode,
    trangThaiThanhToan: getTrangThaiThanhToanText(trangThaiThanhToanCode),
    sanPhams: (dh.sanPhams || dh.chiTietDonHangs || []).map(normalizeSanPham),
  };
}

function normalizeOrderPayload(payload = {}) {
  return {
    ...payload,
    phuongThucThanhToan: getPhuongThucThanhToanCode(
      payload.phuongThucThanhToanCode ?? payload.phuongThucThanhToan
    ),
    trangThaiThanhToan: getTrangThaiThanhToanCode(
      payload.trangThaiThanhToanCode ?? payload.trangThaiThanhToan
    ),
    items: (payload.items || []).map((item) => ({
      maSanPham: item.maSanPham ?? item.MaSanPham,
      soLuong: Number(item.soLuong ?? item.SoLuong ?? 1),
    })),
  };
}

export async function huyDonHang(maDonHang, lyDoHuy) {
  const response = await api.put(`${API_URL}/${maDonHang}/huy`, {
    lyDoHuy: String(lyDoHuy || "").trim(),
  });

  return normalizeDonHang(response.data);
}

export async function getDonHangs(params = {}) {
  const response = await api.get("/api/don-hang", { params });
  const rawItems = Array.isArray(response.data)
    ? response.data
    : response.data?.items || response.data?.content || [];

  const items = rawItems.map(normalizeDonHang);

  return {
    items,
    total:
      response.data?.total ??
      response.data?.totalElements ??
      items.length,
  };
}

export async function taoDonHang(payload) {
  const response = await api.post(API_URL, normalizeOrderPayload(payload));
  return normalizeDonHang(response.data);
}

export async function capNhatDonHang(maDonHang, payload) {
  const response = await api.put(
    `${API_URL}/${maDonHang}`,
    normalizeOrderPayload(payload)
  );

  return normalizeDonHang(response.data);
}

export async function kiemTraDonHangCoHopDong(maDonHang) {
  const response = await api.get(`${API_URL}/${maDonHang}/co-hop-dong`);
  return Boolean(response.data?.daCoHopDong);
}

export async function getSanPhamTaoDonHang() {
  const response = await api.get(`${API_URL}/san-pham-options`);
  return response.data?.items || response.data || [];
}

export async function getKhachHangTaoDonHang() {
  const response = await api.get("/api/nhan-vien/khach-hang");
  return response.data?.items || response.data || [];
}

export async function getDonHangById(maDonHang) {
  const response = await api.get(`/api/don-hang/${maDonHang}`);
  return normalizeDonHang(response.data);
}

export async function capNhatTrangThai(maDonHang, trangThai) {
  const trangThaiCode = getTrangThaiDonHangCode(trangThai);

  const response = await api.put(`${API_URL}/${maDonHang}/trang-thai`, {
    trangThai: trangThaiCode,
  });

  return normalizeDonHang(response.data);
}

export async function getChiTietDonHang(maDonHang) {
  const response = await api.get(`/api/don-hang/${maDonHang}/chi-tiet`);
  return response.data || [];
}

export function formatCurrency(value) {
  if (!value && value !== 0) return "—";

  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
}

export function formatDate(dateStr) {
  if (!dateStr) return "—";

  const d = new Date(dateStr);

  if (Number.isNaN(d.getTime())) return dateStr;

  return d.toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}
