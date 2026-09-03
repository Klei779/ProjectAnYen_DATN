import api from "../api/api.js"

const API_URL =
    "/api/doi-tac/tai-chinh"


// ===============================
// LẤY QUỸ + VÍ
// ===============================

export async function getTaiChinhDoiTac() {

    const response =
        await api.get(API_URL)

    return response.data
}


// ===============================
// MỞ QUỸ
// ===============================

export async function moQuyDoiTac() {

    const response =
        await api.post(
            `${API_URL}/mo-quy`
        )

    return response.data
}


// ===============================
// NẠP QUỸ QUA PAYOO
// ===============================

export async function taoPayooNapQuy(
    soTien
) {

    const response =
        await api.post(
            `${API_URL}/payoo/nap-quy`,
            {
                soTien: Number(soTien)
            }
        )

    return response.data
}


// ===============================
// RÚT QUỸ QUA PAYOO
// ===============================

export async function taoPayooRutQuy(
    soTien
) {

    const response =
        await api.post(
            `${API_URL}/payoo/rut-quy`,
            {
                soTien: Number(soTien)
            }
        )

    return response.data
}


// ===============================
// RÚT VÍ QUA PAYOO
// ===============================

export async function taoPayooRutVi(
    soTien
) {

    const response =
        await api.post(
            `${API_URL}/payoo/rut-vi`,
            {
                soTien: Number(soTien)
            }
        )

    return response.data
}


// ===============================
// CHUYỂN VÍ -> QUỸ
// ===============================

export async function chuyenViVaoQuy(
    soTien
) {

    const response =
        await api.post(
            `${API_URL}/chuyen-vi-vao-quy`,
            {
                soTien: Number(soTien)
            }
        )

    return response.data
}


// ===============================
// LẤY LỊCH SỬ GIAO DỊCH
// ===============================

export async function getLichSuGiaoDich() {

    const response =
        await api.get(
            `${API_URL}/lich-su-giao-dich`
        )

    return response.data
}