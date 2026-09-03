import api from "../api/api.js";

const API_URL = "/api/nhan-vien/hop-dong";

export async function getHopDongs(params = {}) {
    const requestParams = {
        keyword: params.keyword?.trim() || "",
        page: params.page ?? 1,
        pageSize: params.pageSize ?? 10,
    };

    if (
        params.trangThai !== null &&
        params.trangThai !== undefined &&
        params.trangThai !== "" &&
        params.trangThai !== "Tất cả"
    ) {
        const statusValue = Number(params.trangThai);

        if (Number.isInteger(statusValue)) {
            requestParams.trangThai = statusValue;
        }
    }

    const response = await api.get(API_URL, {
        params: requestParams,
    });

    return response.data;
}
export async function getNextHopDongCode() {
    const response = await api.get(`${API_URL}/next-code`);
    return response.data;
}
export async function getChiTietHopDong(id) {
    const response = await api.get(`${API_URL}/${id}`);
    return response.data;
}

export async function getDonHangOptionsForHopDong() {
    const response = await api.get(`${API_URL}/don-hang-options`);
    return response.data;
}

export async function getDonHangDetailForHopDong(maDonHang) {
    const response = await api.get(`${API_URL}/don-hang/${maDonHang}`);
    return response.data;
}

export async function createHopDong(payload) {
    const response = await api.post(API_URL, payload);
    return response.data;
}
export async function cancelHopDong(id) {
    const response = await api.put(`${API_URL}/${id}/huy`);
    return response.data;
}
export async function deleteHopDong(id) {
    const response = await api.delete(`${API_URL}/${id}`);
    return response.data;
}