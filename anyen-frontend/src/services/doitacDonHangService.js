import api from "../api/api.js";

/**
 * Lấy danh sách đơn hàng đã được đối tác chấp nhận
 */
export async function getDonHangsDoiTac(params = {}) {
  try {
    const response = await api.get("/api/doi-tac/don-hang", {
      params: {
        keyword: params.keyword || "",
        trangThai: params.trangThai || "",
        page: params.page || 1,
        pageSize: params.pageSize || 10
      }
    });

    return response.data;
  } catch (error) {
    console.error("Lỗi getDonHangsDoiTac:", error);
    throw error;
  }
}

/**
 * Lấy chi tiết đơn hàng cho đối tác
 */
export async function getChiTietDonHangDoiTac(maDonHang) {
  try {
    const response = await api.get(
        `/api/doi-tac/don-hang/${maDonHang}`
    );

    return response.data;
  } catch (error) {
    console.error("Lỗi getChiTietDonHangDoiTac:", error);
    throw error;
  }
}

/**
 * Cập nhật trạng thái đơn hàng
 */
export async function updateTrangThaiDonHang(maDonHang, payload) {
  try {
    const response = await api.put(
        `/api/doi-tac/don-hang/${maDonHang}/trang-thai`,
        payload
    );

    return response.data;
  } catch (error) {
    console.error("Lỗi updateTrangThaiDonHang:", error);
    throw error;
  }
}