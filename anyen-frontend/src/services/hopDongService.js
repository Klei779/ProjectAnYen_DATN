import api from "../api/api.js";

const API_URL = "/api/nhan-vien/hop-dong";

export async function getHopDongs(params = {}) {
    const response = await api.get(API_URL, {
        params: {
            keyword: params.keyword || "",
            trangThai: params.trangThai || "Tất cả",
            page: params.page || 1,
            pageSize: params.pageSize || 10,
        },
    });

    return response.data;
}

export async function getChiTietHopDong(id) {
    const response = await api.get(`${API_URL}/${id}`);
    return response.data;
}