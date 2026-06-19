import api from "../api/api.js";

const API_URL = "/api/nhan-vien/don-hang";

export async function huyDonHang(maDonHang, lyDoHuy) {
  const response = await api.put(`${API_URL}/${maDonHang}/huy`, {
    lyDoHuy: lyDoHuy.trim(),
  });

  return response.data;
}

export async function getDonHangs(params = {}) {
  const response = await api.get("/api/don-hang", { params });

  return {
    items: response.data.items || response.data || [],
    total: response.data.total || 0,
  };
}

export async function taoDonHang(payload) {
  const response = await api.post("/api/nhan-vien/don-hang", payload);
  return response.data;
}

export async function getSanPhamTaoDonHang() {
  const response = await api.get("/api/nhan-vien/don-hang/san-pham-options");

  return response.data?.items || response.data || [];
}

export async function getKhachHangTaoDonHang() {
  const response = await api.get("/api/nhan-vien/khach-hang");

  return response.data?.items || response.data || [];
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