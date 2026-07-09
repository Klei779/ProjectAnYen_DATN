import api from "../api/api.js";

const API_URL = "/api/nhan-vien/hoa-don-cua-toi";

export async function getHoaDonCuaToi(params = {}) {
    const response = await api.get(API_URL, {
        params: {
            keyword: params.keyword || "",
            trangThai: params.trangThai || "Tất cả",
            phuongThucThanhToan: params.phuongThucThanhToan || "Tất cả",
            tuNgay: params.tuNgay || undefined,
            denNgay: params.denNgay || undefined,
            page: params.page || 1,
            pageSize: params.pageSize || 10,
        },
    });

    return {
        items: response.data.items || [],
        total: Number(response.data.total || 0),
        page: Number(response.data.page || 1),
        pageSize: Number(response.data.pageSize || 10),
        totalPages: Number(response.data.totalPages || 0),
        admin: Boolean(response.data.admin),
    };
}