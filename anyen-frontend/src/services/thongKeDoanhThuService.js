import api from "../api/api.js";

function getEndpoint(loaiTaiKhoan) {
    return loaiTaiKhoan === "DOI_TAC"
        ? "/api/doi-tac/thong-ke-doanh-thu"
        : "/api/nhan-vien/thong-ke-doanh-thu";
}

export async function getThongKeDoanhThu(
    loaiTaiKhoan,
    params = {}
) {
    const response = await api.get(
        getEndpoint(loaiTaiKhoan),
        {
            params: {
                tuNgay:
                    params.tuNgay || undefined,

                denNgay:
                    params.denNgay || undefined,

                kieuThongKe:
                    params.kieuThongKe || "NGAY",
            },
        }
    );

    return normalizeThongKe(response.data);
}

function normalizeDoiTuong(item = {}) {
    return {
        maDoiTuong:
        item.maDoiTuong,

        tenDoiTuong:
            item.tenDoiTuong || "---",

        soDonHang:
            Number(item.soDonHang || 0),

        doanhThu:
            Number(item.doanhThu || 0),
    };
}

function normalizeThongKe(data = {}) {
    return {
        tuNgay: data.tuNgay || "",
        denNgay: data.denNgay || "",

        kieuThongKe:
            data.kieuThongKe || "NGAY",

        tongQuan: {
            tongDoanhThu: Number(
                data.tongQuan?.tongDoanhThu || 0
            ),

            doanhThuThucNhan: Number(
                data.tongQuan
                    ?.doanhThuThucNhan || 0
            ),

            tyLeDoanhThu: Number(
                data.tongQuan?.tyLeDoanhThu || 0
            ),

            tongHoaDon: Number(
                data.tongQuan?.tongHoaDon || 0
            ),

            tongDonHang: Number(
                data.tongQuan?.tongDonHang || 0
            ),

            doanhThuTrungBinh: Number(
                data.tongQuan
                    ?.doanhThuTrungBinh || 0
            ),
        },

        bieuDoDoanhThu: Array.isArray(
            data.bieuDoDoanhThu
        )
            ? data.bieuDoDoanhThu.map(
                (item) => ({
                    thoiGian:
                        item.thoiGian || "",

                    doanhThu:
                        Number(item.doanhThu || 0),

                    soDonHang:
                        Number(item.soDonHang || 0),
                })
            )
            : [],

        topSanPham: Array.isArray(
            data.topSanPham
        )
            ? data.topSanPham.map(
                (item) => ({
                    maSanPham:
                    item.maSanPham,

                    tenSanPham:
                        item.tenSanPham || "---",

                    soLuongBan:
                        Number(item.soLuongBan || 0),

                    doanhThu:
                        Number(item.doanhThu || 0),
                })
            )
            : [],

        topNhanVien: Array.isArray(
            data.topNhanVien
        )
            ? data.topNhanVien.map(
                normalizeDoiTuong
            )
            : [],

        topDoiTac: Array.isArray(
            data.topDoiTac
        )
            ? data.topDoiTac.map(
                normalizeDoiTuong
            )
            : [],

        phuongThucThanhToan:
            Array.isArray(
                data.phuongThucThanhToan
            )
                ? data.phuongThucThanhToan.map(
                    (item) => ({
                        phuongThucThanhToan:
                            item.phuongThucThanhToan ??
                            "0",

                        soHoaDon:
                            Number(item.soHoaDon || 0),

                        doanhThu:
                            Number(item.doanhThu || 0),
                    })
                )
                : [],
    };
}