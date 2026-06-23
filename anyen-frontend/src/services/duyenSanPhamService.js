import api from "../api/api.js";

const API_URL = "/api/nhan-vien/duyet-san-pham";

export async function getSanPhamChoDuyet() {
    const response = await api.get(API_URL);
    return response.data || [];
}

export async function duyetSanPham(maSanPham) {
    const response = await api.put(`${API_URL}/${maSanPham}/duyet`);
    return response.data;
}

export async function tuChoiSanPham(maSanPham, lyDoTuChoi) {
    const response = await api.put(`${API_URL}/${maSanPham}/tu-choi`, {
        lyDoTuChoi: lyDoTuChoi.trim(),
    });
    return response.data;
}