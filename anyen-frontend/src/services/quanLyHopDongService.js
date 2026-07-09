import api from "../api/api.js";

const API_URL = "/api/nhan-vien/quanlyhopdong";

export async function getHopDongsAdmin(params = {}) {
    const response = await api.get(API_URL, {
        params: {
            keyword: params.keyword || "",
            trangThai: params.trangThai || "Tất cả",
            includeHidden:
                params.includeHidden === undefined ? true : params.includeHidden,
            page: params.page || 1,
            pageSize: params.pageSize || 10,
        },
    });

    return response.data;
}

export async function getChiTietHopDongAdmin(id) {
    const response = await api.get(`${API_URL}/${id}`);
    return response.data;
}

export async function anHopDong(id) {
    const response = await api.put(`${API_URL}/${id}/an`);
    return response.data;
}

export async function hienHopDong(id) {
    const response = await api.put(`${API_URL}/${id}/hien`);
    return response.data;
}

export async function xoaHopDong(id) {
    const response = await api.delete(`${API_URL}/${id}`);
    return response.data;
}
