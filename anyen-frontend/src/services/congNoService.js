import api from "../api/api.js";

const API_URL = "/api/admin/congno";

// =============================================
// DANH SÁCH CÔNG NỢ
// =============================================
export async function getCongNoList(params = {}) {
  const response = await api.get(API_URL, {
    params,
  });

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

// =============================================
// TỔNG QUAN CÔNG NỢ
// =============================================
export async function getTongQuanCongNo() {
  const response = await api.get(
      `${API_URL}/tong-quan`
  );

  return response.data;
}

// =============================================
// LỊCH SỬ THANH TOÁN CÔNG NỢ
// =============================================
export async function getLichSuCongNo(params = {}) {
  const response = await api.get(
      `${API_URL}/lich-su`,
      {
        params,
      }
  );

  const rawItems = Array.isArray(response.data?.content)
      ? response.data.content
      : response.data?.items || response.data || [];

  return {
    items: rawItems,
    total: response.data?.totalElements || rawItems.length,
    page: response.data?.number || 0,
    size: response.data?.size || 10,
  };
}

// =============================================
// CÔNG NỢ THEO ĐỐI TÁC
// =============================================
export async function getCongNoByDoiTac(maDoiTac) {
  const response = await api.get(
      `${API_URL}/doi-tac/${maDoiTac}`
  );

  return (response.data || []).map(
      normalizeCongNo
  );
}

// =============================================
// CÔNG NỢ THEO ĐƠN HÀNG
// =============================================
export async function getCongNoByDonHang(maDonHang) {
  const response = await api.get(
      `${API_URL}/don-hang/${maDonHang}`
  );

  return (response.data || []).map(
      normalizeCongNo
  );
}

// =============================================
// TẠO CÔNG NỢ TỪ ĐƠN HÀNG
// =============================================
export async function taoCongNoTuDonHang(maDonHang) {
  const response = await api.post(
      `${API_URL}/tao-tu-don-hang/${maDonHang}`
  );

  return response.data;
}

// =============================================
// TẠO GIAO DỊCH PAYOO THANH TOÁN CÔNG NỢ
// =============================================
export async function taoThanhToanCongNoPayoo(
    maCongNo,
    soTien
) {
  const response = await api.post(
      `${API_URL}/${maCongNo}/payoo`,
      {
        soTien,
      }
  );

  return response.data;
}

// =============================================
// CHUẨN HÓA DỮ LIỆU CÔNG NỢ
// =============================================
function normalizeCongNo(cn = {}) {
  return {
    ...cn,

    maCongNo:
        cn.maCongNo ??
        cn.id ??
        null,

    maDonHang:
        cn.maDonHang ??
        null,

    maDoiTac:
        cn.maDoiTac ??
        null,

    tenDoiTac:
        cn.tenDoiTac ??
        "",

    tongTien:
        Number(cn.tongTien ?? 0),

    daThanhToan:
        Number(cn.daThanhToan ?? 0),

    conLai:
        Number(cn.conLai ?? 0),

    hanThanhToan:
        cn.hanThanhToan ??
        null,

    trangThai:
        cn.trangThai ??
        0,

    trangThaiText:
        cn.trangThaiText ??
        "Không rõ",

    ghiChu:
        cn.ghiChu ??
        "",

    ngayTaoDon:
        cn.ngayTaoDon ??
        null,

    trangThaiDonHang:
        cn.trangThaiDonHang ??
        null,

    createdAt:
        cn.createdAt ??
        null,

    updatedAt:
        cn.updatedAt ??
        null,
  };
}

// =============================================
// FORMAT TIỀN
// =============================================
export function formatCurrency(value) {
  if (
      value === null ||
      value === undefined ||
      value === ""
  ) {
    return "—";
  }

  return new Intl.NumberFormat(
      "vi-VN",
      {
        style: "currency",
        currency: "VND",
        maximumFractionDigits: 0,
      }
  ).format(Number(value) || 0);
}

// =============================================
// FORMAT NGÀY
// =============================================
export function formatDate(dateStr) {
  if (!dateStr) {
    return "—";
  }

  const d = new Date(dateStr);

  if (Number.isNaN(d.getTime())) {
    return dateStr;
  }

  return d.toLocaleDateString(
      "vi-VN",
      {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
      }
  );
}

// =============================================
// CLASS TRẠNG THÁI
// =============================================
export function getTrangThaiBadgeClass(trangThai) {
  switch (Number(trangThai)) {
    case 0:
      return "badge-blue";

    case 1:
      return "badge-yellow";

    case 2:
      return "badge-green";

    case 3:
      return "badge-red";

    default:
      return "badge-gray";
  }
}

export function getTrangThaiTextClass(trangThai) {
  switch (Number(trangThai)) {
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