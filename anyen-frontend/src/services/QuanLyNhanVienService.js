import api from "../api/api.js";

const API_URL = "/api/nhan-vien/quanlynhanvien";

export async function createNhanVien(payload) {
    const response = await api.post(`${API_URL}/create-nhanvien`, payload);
    return response.data;
}