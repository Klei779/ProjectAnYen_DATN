import api from "../api/api.js";

const API_URL = "/api/doi-tac/san-pham";

export async function getSanPhamDoiTac(params = {}) {
    const response = await api.get(API_URL, {
        params: {
            keyword: params.keyword || "",
            loai: params.loai || "",
            vatLieu: Array.isArray(params.vatLieu)
                ? params.vatLieu.join(",")
                : "",
            tonGiao: Array.isArray(params.tonGiao)
                ? params.tonGiao.join(",")
                : "",
            mauSac: params.mauSac || "",
            minPrice: params.minPrice ?? null,
            maxPrice: params.maxPrice ?? null,
            sortBy: params.sortBy || "newest",
            page: params.page || 1,
            pageSize: params.pageSize || 16
        }
    });

    return {
        items: response.data.items || [],
        total: response.data.total || 0
    };
}