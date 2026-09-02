import api from "../api/api.js";

const API_URL = "/api/doi-tac/quan-ly-don-hang";

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
  11: "Gặp sự cố",
});

function getTrangThaiDonHangText(value) {
  // Nếu giá trị đã là text hợp lệ, giữ nguyên
  if (typeof value === 'string' && value.trim()) {
    return value.trim();
  }

  const code = Number(value);
  return ORDER_STATUS_LABELS[code] || "Chưa xác định";
}

function getItems(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.items)) return data.items;
  if (Array.isArray(data?.content)) return data.content;
  return [];
}

function getTotal(data, items) {
  return data?.total ?? data?.totalElements ?? items.length;
}

function formatCode(id, code) {
  if (code) return String(code).replace("#", "");
  if (!id) return "";
  return `DH${String(id).padStart(4, "0")}`;
}

function normalizeDonHang(dh) {
  const maDonHang = dh.maDonHang ?? dh.MaDonHang ?? dh.id;

  return {
    ...dh,

    maDonHang,
    maCode: formatCode(maDonHang, dh.maCode ?? dh.code),

    nhanVienVaiTro: dh.nhanVienVaiTro ?? null,

    tenKhachHang:
        dh.tenKhachHang ??
        dh.customerName ??
        dh.khachHang?.tenKhachHang ??
        "",

    soDienThoai:
        dh.soDienThoai ??
        dh.soDienThoaiKH ??
        dh.customerPhone ??
        dh.khachHang?.soDienThoai ??
        "",

    email:
        dh.email ??
        dh.emailKH ??
        dh.khachHang?.email ??
        "",

    diaChi:
        dh.diaChi ??
        dh.diaChiKH ??
        dh.khachHang?.diaChi ??
        "",

    ngayDat:
        dh.ngayDat ??
        dh.NgayTaoDon ??
        dh.ngayTaoDon ??
        "",

    tongCong: Number(
        dh.tongCong ??
        dh.tongTien ??
        dh.TongTien ??
        0
    ),

    trangThai: getTrangThaiDonHangText(
        dh.trangThai ?? dh.TrangThai ?? ""
    ),

    ghiChu:
        dh.ghiChu ??
        dh.GhiChu ??
        "",

    sanPhams:
        dh.sanPhams ??
        dh.chiTietDonHangs ??
        []
  };
}


export async function getDoiTacDonHangs(params = {}) {
  const response = await api.get(API_URL, {
    params: {
      keyword: params.keyword || "",
      trangThai: params.trangThai || "Tất cả",
      page: params.page || 1,
      pageSize: params.pageSize || 10,
    },
  });

  const items = getItems(response.data).map(normalizeDonHang);

  return {
    items,
    total: getTotal(response.data, items)
  };
}

export async function getDonHangsDoiTac(params = {}) {
  return getDoiTacDonHangs(params);
}


export async function getDoiTacDonHangDetail(maDonHang) {
  const response = await api.get(`${API_URL}/${maDonHang}`);
  return normalizeDonHang(response.data);
}


export async function getChiTietDonHangDoiTac(maDonHang) {
  return getDoiTacDonHangDetail(maDonHang);
}


export async function updateTrangThaiDonHang(maDonHang, payload) {
  const response = await api.put(
      `${API_URL}/${maDonHang}/trang-thai`,
      payload
  );

  return response.data;
}

export async function xuLyDonHang(maDonHang, ngayGiaoDuKien) {
  const response = await api.put(
      `${API_URL}/${maDonHang}/xu-ly`,
      { ngayGiaoDuKien }
  );

  return response.data;
}