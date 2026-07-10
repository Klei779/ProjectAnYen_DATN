import api from "../api/api.js";

const API_URL = "/api/san-pham";

export async function getSanPhamChoDuyet() {
    const response = await api.get( `${API_URL}/cho-duyet`);
    return response.data || [];
}

export async function duyetSanPham(id) {
    const response = await api.put(`${API_URL}/${id}/duyet`);
    return response.data;
}

export async function tuChoiSanPham(maSanPham, lyDoTuChoi) {
    const response = await api.put(`${API_URL}/${maSanPham}/tu-choi`, {
        lyDoTuChoi: lyDoTuChoi.trim(),
    });
    return response.data;
}