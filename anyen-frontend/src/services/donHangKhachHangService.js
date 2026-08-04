import api from "../api/api.js";

const CUSTOMER_ORDER_URL =
    "/api/khach-hang/don-hang";

function normalizeCustomerOrderPayload(
    payload = {}
) {
    return {
        tenKhachHang:
            String(
                payload.tenKhachHang || ""
            ).trim(),

        soDienThoai:
            String(
                payload.soDienThoai || ""
            ).trim(),

        cccd:
            String(
                payload.cccd || ""
            ).trim(),

        diaChi:
            String(
                payload.diaChi || ""
            ).trim(),

        email:
            String(
                payload.email || ""
            ).trim(),

        ghiChu:
            String(
                payload.ghiChu || ""
            ).trim(),

        phuongThucThanhToan:
            Number(
                payload.phuongThucThanhToan ?? 0
            ),

        trangThaiThanhToan:
            Number(
                payload.trangThaiThanhToan ?? 0
            ),

        /*
         * Chỉ gửi mã và số lượng.
         * Backend tự lấy giá, loại và đối tác.
         */
        items: (payload.items || []).map(
            item => ({
                maSanPham:
                    Number(
                        item.maSanPham ??
                        item.id
                    ),

                soLuong:
                    Math.max(
                        1,
                        Number(
                            item.soLuong ??
                            item.quantity
                        ) || 1
                    )
            })
        )
    };
}

export async function taoDonHangKhachHang(
    payload
) {
    const response = await api.post(
        CUSTOMER_ORDER_URL,
        normalizeCustomerOrderPayload(payload)
    );

    return response.data;
}