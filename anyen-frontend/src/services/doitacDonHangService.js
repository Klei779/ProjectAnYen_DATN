import api from "../api/api.js";

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
  return `DH${String(id).padStart(3, "0")}`;
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

    tongCong:
        Number(
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

/**
 * Lấy danh sách đơn hàng đối tác.
 * Ưu tiên API đối tác. Nếu API đối tác trả rỗng do lệch trạng thái DA_XAC_NHAN / DA_CHAP_NHAN
 * thì fallback sang /api/don-hang để frontend vẫn hiện được dữ liệu.
 */
export async function getDonHangsDoiTac(params = {}) {
  let firstError = null;

  try {
    const response = await api.get("/api/doi-tac/don-hang", {
      params: {
        keyword: params.keyword || "",
        trangThai: params.trangThai || "",
        page: params.page || 1,
        pageSize: params.pageSize || 10
      }
    });

    const items = getItems(response.data).map(normalizeDonHang);

    if (items.length > 0) {
      return {
        items,
        total: getTotal(response.data, items)
      };
    }
  } catch (error) {
    firstError = error;
    console.warn("API /api/doi-tac/don-hang lỗi hoặc rỗng, thử fallback:", error);
  }

  try {
    const response = await api.get("/api/don-hang");
    const items = getItems(response.data).map(normalizeDonHang);

    return {
      items,
      total: getTotal(response.data, items)
    };
  } catch (error) {
    console.error("Fallback /api/don-hang cũng lỗi:", error);
    throw firstError || error;
  }
}

/**
 * Lấy chi tiết đơn hàng cho đối tác.
 */
export async function getChiTietDonHangDoiTac(maDonHang) {
  try {
    const response = await api.get(`/api/doi-tac/don-hang/${maDonHang}`);
    return normalizeDonHang(response.data);
  } catch (error) {
    console.warn("Không lấy được chi tiết từ API đối tác, thử /api/don-hang:", error);

    const response = await api.get("/api/don-hang");
    const items = getItems(response.data).map(normalizeDonHang);

    const found = items.find(
        item => Number(item.maDonHang) === Number(maDonHang)
    );

    if (!found) {
      throw error;
    }

    return found;
  }
}

/**
 * Cập nhật trạng thái đơn hàng
 */
export async function updateTrangThaiDonHang(maDonHang, payload) {
  const response = await api.put(
      `/api/doi-tac/don-hang/${maDonHang}/trang-thai`,
      payload
  );

  return response.data;
}