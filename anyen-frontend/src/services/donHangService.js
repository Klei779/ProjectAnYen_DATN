import api from "../api/api.js";

// ── Đơn hàng ──────────────────────────────────────────────
export async function getDonHangs(params = {}) {
  const response = await api.get("/api/don-hang", { params });
  return {
    items: response.data.items || response.data || [],
    total: response.data.total || 0,
  };
}

export async function getDonHangById(maDonHang) {
  const response = await api.get(`/api/don-hang/${maDonHang}`);
  return response.data;
}

export async function capNhatTrangThai(maDonHang, trangThai) {
  const response = await api.put(`/api/don-hang/${maDonHang}/trang-thai`, {
    trangThai,
  });
  return response.data;
}

export async function huyDonHang(maDonHang) {
  const response = await api.put(`/api/don-hang/${maDonHang}/huy`);
  return response.data;
}

// ── Chi tiết đơn hàng (sản phẩm trong đơn) ───────────────
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
  if (isNaN(d)) return dateStr;
  return d.toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

