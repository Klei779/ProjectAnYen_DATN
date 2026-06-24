import api from "../api/api.js";

const API_URL = "/api/nhan-vien/quanlydoitac";

export async function createDoiTac(payload) {
    const response = await api.post(`${API_URL}/create-doitac`, payload);
    return response.data;
}
