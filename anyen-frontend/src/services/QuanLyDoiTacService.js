import api from "../api/api.js";

const API_URL = "/api/nhan-vien/quanlydoitac";
const AUTH_API_URL = "/api/auth/doi-tac";

export async function createDoiTac(payload) {
    const response = await api.post(`${API_URL}/create-doitac`, payload);
    return response.data;
}

export async function verifyDoiTacToken(token) {
    const response = await api.get(`${AUTH_API_URL}/xac-nhan?token=${token}`);
    return response;
}

export async function kyHopDongDoiTac(payload) {
    const response = await api.post(`${AUTH_API_URL}/ky-hop-dong`, payload);
    return response;
}
