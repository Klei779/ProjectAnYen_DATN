import api from "../api/api.js"

const API_URL = "/api/payoo-mock"


export async function getPayooTransaction(
    maGiaoDich
) {

    const response = await api.get(
        `${API_URL}/${maGiaoDich}`
    )

    return response.data
}


/*
 * Click QR
 * =>
 * giả lập Payoo báo đã nhận tiền.
 */
export async function confirmPayooTransaction(
    maGiaoDich
) {

    const response = await api.post(
        `${API_URL}/${maGiaoDich}/xac-nhan`
    )

    return response.data
}