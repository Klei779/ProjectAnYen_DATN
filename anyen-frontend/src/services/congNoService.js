import api from "../api/api.js";

const API_URL = "/api/admin/congno";

export async function getCongNoList(params = {}) {
  const response = await api.get(API_URL, { params });
  const rawItems = Array.isArray(response.data?.content)
    ? response.data.content
    : response.data?.items || response.data || [];

  return {
    items: rawItems.map(normalizeCongNo),
    total: response.data?.totalElements || rawItems.length,
    page: response.data?.number || 0,
    size: response.data?.size || 10,
  };
}

export async function getCongNoByDoiTac(maDoiTac) {
  const response = await api.get(`${API_URL}/doi-tac/${maDoiTac}`);
  return (response.data || []).map(normalizeCongNo);
}

export async function getCongNoByDonHang(maDonHang) {
  const response = await api.get(`${API_URL}/don-hang/${maDonHang}`);
  return (response.data || []).map(normalizeCongNo);
}

export async function getTongQuanCongNo() {
  const response = await api.get(`${API_URL}/tong-quan`);
  return response.data;
}

export async function taoCongNoTuDonHang(maDonHang) {
  const response = await api.post(`${API_URL}/tao-tu-don-hang/${maDonHang}`);
  return response.data;
}

function normalizeCongNo(cn = {}) {
  return {
    ...cn,
    maCongNo: cn.maCongNo ?? cn.id ?? null,
    maDonHang: cn.maDonHang ?? null,
    maDoiTac: cn.maDoiTac ?? null,
    tenDoiTac: cn.tenDoiTac ?? "",
    tongTien: cn.tongTien ?? 0,
    daThanhToan: cn.daThanhToan ?? 0,
    conLai: cn.conLai ?? 0,
    hanThanhToan: cn.hanThanhToan ?? null,
    trangThai: cn.trangThai ?? 0,
    trangThaiText: cn.trangThaiText ?? "Không rõ",
    ghiChu: cn.ghiChu ?? "",
    ngayTaoDon: cn.ngayTaoDon ?? null,
    trangThaiDonHang: cn.trangThaiDonHang ?? null,
    soTaiKhoan: cn.soTaiKhoan ?? "",
    tenNganHang: cn.tenNganHang ?? "",
    tenChuTaiKhoan: cn.tenChuTaiKhoan ?? "",
  };
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

export function getTrangThaiBadgeClass(trangThai) {
  switch (trangThai) {
    case 0:
      return "badge-blue"; // Chưa thanh toán
    case 1:
      return "badge-yellow"; // Thanh toán một phần
    case 2:
      return "badge-green"; // Đã thanh toán
    case 3:
      return "badge-red"; // Quá hạn
    default:
      return "badge-gray";
  }
}

export function getTrangThaiTextClass(trangThai) {
  switch (trangThai) {
    case 0:
      return "text-gray";
    case 1:
      return "text-yellow";
    case 2:
      return "text-green";
    case 3:
      return "text-red";
    default:
      return "text-gray";
  }
}
