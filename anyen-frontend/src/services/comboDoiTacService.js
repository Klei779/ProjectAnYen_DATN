import api from "../api/api.js";

const BASE_URL = "/api/doi-tac/combo";

export const getCombosDoiTac = () => api.get(BASE_URL);
export const getSanPhamComboDoiTac = () => api.get(`${BASE_URL}/san-pham`);
export const createComboDoiTac = (payload) => api.post(BASE_URL, payload, { timeout: 120000 });
export const updateComboDoiTac = (comboId, payload) => api.put(`${BASE_URL}/${comboId}`, payload);
export const updateTrangThaiComboDoiTac = (comboId, trangThai) =>
    api.patch(`${BASE_URL}/${comboId}/trang-thai`, { trangThai });
