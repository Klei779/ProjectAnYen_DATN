import api from "../api/api.js";

const API_URL = "/api/san-pham";

// 1. Sửa hàm lấy danh sách chờ duyệt hỗ trợ phân trang
export async function getSanPhamChoDuyet(page = 1, pageSize = 16) {
    const response = await api.get(`${API_URL}/cho-duyet`, {
        params: {
            page: page,
            pageSize: pageSize
        }
    });

    // Vì Backend trả về SanPhamPageResponse (gồm items và total)
    // Nên ta trả về đúng object đó, nếu lỗi hoặc không có dữ liệu thì fallback về object rỗng
    return response.data || { items: [], total: 0 };
}

export async function duyetSanPham(id) {
    const response = await api.put(`${API_URL}/${id}/duyet`);
    return response.data;
}

export async function tuChoiSanPham(maSanPham, lyDoTuChoi) {
    const response = await api.put(`${API_URL}/${id}/tu-choi`, {
        lyDoTuChoi: lyDoTuChoi ? lyDoTuChoi.trim() : "",
    });
    return response.data;
}