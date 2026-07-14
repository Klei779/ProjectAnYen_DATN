import api from "../api/api.js";

const API_URL = "/api/doi-tac/quan-ly-don-hang";

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

    trangThai:
        dh.trangThai ??
        dh.TrangThai ??
        "",

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

export async function xuLyDonHang(maDonHang, thoiGianUocTinh) {
  const response = await api.post(`${API_URL}/${maDonHang}/xu-ly`, null, {
    params: { thoiGianUocTinh }
  });
  return response.data;
}

export async function daGiaoDonHang(maDonHang) {
  const response = await api.post(`${API_URL}/${maDonHang}/da-giao`);
  return response.data;
}

export async function huyDonHang(maDonHang, lyDo) {
  const response = await api.post(`${API_URL}/${maDonHang}/huy`, null, {
    params: { lyDo }
  });
  return response.data;
}

export async function baoCaoSuCo(maDonHang, lyDo) {
  const response = await api.post(`${API_URL}/${maDonHang}/bao-cao-su-co`, null, {
    params: { lyDo }
  });
  return response.data;
}