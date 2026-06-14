import api from "../api/api.js";

const API_URL = "/api/doi-tac/san-pham";

function cleanParams(params = {}) {
    return {
        keyword: params.keyword || "",
        loai: params.loai || "",
        vatLieu: Array.isArray(params.vatLieu) ? params.vatLieu.join(",") : (params.vatLieu || ""),
        tonGiao: Array.isArray(params.tonGiao) ? params.tonGiao.join(",") : (params.tonGiao || ""),
        mauSac: params.mauSac || "",
        trangThai: params.trangThai || "ALL",
        minPrice: params.minPrice ?? "",
        maxPrice: params.maxPrice ?? "",
        sortBy: params.sortBy || "newest",
        page: params.page ?? 0,
        pageSize: params.pageSize ?? 16,
    };
}

export async function getSanPhamDoiTac(params = {}) {
    const response = await api.get(API_URL, { params: cleanParams(params) });
    return response.data;
}

export async function createSanPhamDoiTac(payload) {
    const response = await api.post(API_URL, payload);
    return response.data;
}

export async function updateSanPhamDoiTac(id, payload) {
    const response = await api.put(`${API_URL}/${id}`, payload);
    return response.data;
}

export async function updateTonKhoSanPhamDoiTac(id, soLuong) {
    const response = await api.patch(`${API_URL}/${id}/ton-kho`, { soLuong });
    return response.data;
}

export async function hideSanPhamDoiTac(id) {
    const response = await api.patch(`${API_URL}/${id}/an`);
    return response.data;
}

export async function showSanPhamDoiTac(id) {
    const response = await api.patch(`${API_URL}/${id}/hien`);
    return response.data;
}
