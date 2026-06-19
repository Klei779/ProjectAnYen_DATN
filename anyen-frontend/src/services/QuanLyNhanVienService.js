import api from "../api/api.js";

const API_URL = "/api/nhan-vien/quanlynhanvien";

export async function getAllNhanVien() {
    const response = await api.get(`${API_URL}`);
    return response.data;
}

export async function createNhanVien(payload) {
    const response = await api.post(`${API_URL}/create-nhanvien`, payload);
    return response.data;
}

export async function nghiViecNhanVien(maNhanVien) {
    const response = await api.put(`${API_URL}/nghi-viec/${maNhanVien}`);
    return response.data;
}