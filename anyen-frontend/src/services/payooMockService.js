import api from "../api/api.js";

const API_URL = "/api/payoo-mock";

// =====================================================
// LẤY THÔNG TIN GIAO DỊCH PAYOO
// =====================================================

export async function getPayooTransaction(maGiaoDich) {
    const response = await api.get(
        `${API_URL}/${maGiaoDich}`
    );

    return response.data;
}


// =====================================================
// XÁC NHẬN THANH TOÁN PAYOO MOCK
// =====================================================

export async function confirmPayooTransaction(maGiaoDich) {
    const response = await api.post(
        `${API_URL}/${maGiaoDich}/xac-nhan`
    );

    return response.data;
}