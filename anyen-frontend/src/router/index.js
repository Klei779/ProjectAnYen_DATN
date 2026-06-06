import { createRouter, createWebHistory } from "vue-router";

// Layout
import WebsiteLayout from "../layouts/WebsiteLayout.vue";
import DoiTacLayout from "../layouts/DoiTacLayout.vue";

// Website
import TrangChu from "../pages/website/TrangChu.vue";
import TrangSanPham from "../pages/website/TrangSanPham.vue";
import TrangDichVu from "../pages/website/TrangDichVu.vue";
import DichVuChiTiet from "../pages/website/TrangDichVuChiTiet.vue";
import TrangGioiThieu from "../pages/website/TrangGioiThieu.vue";
import TrangLienHe from "../pages/website/TrangLienHe.vue";

// Đối tác
import TrangTongQuan from "../pages/doitac/TrangTongQuan.vue";
import TrangQLSanPham from "../pages/doitac/TrangQLSanPham.vue";
import TrangQLDonHang from "../pages/doitac/TrangQLDonHang.vue";
import TrangThongBao from "../pages/doitac/TrangThongBao.vue";
import TrangThongTinTK from "../pages/doitac/TrangThongTinTK.vue";
import TrangDoiMatKhau from "../pages/doitac/TrangDoiMatKhau.vue";

const routes = [
    // WEBSITE
    {
        path: "/",
        component: WebsiteLayout,
        children: [
            {
                path: "",
                component: TrangChu,
            },
            {
                path: "san-pham",
                component: TrangSanPham,
            },
            {
                path: "dich-vu",
                component: TrangDichVu,
            },
            {
                path: "dich-vu/:id",
                name: "DichVuChiTiet",
                component: DichVuChiTiet,
            },
            {
                path: "gioi-thieu",
                component: TrangGioiThieu,
            },
            {
                path: "lien-he",
                component: TrangLienHe,
            },
        ],
    },

    // ĐỐI TÁC
    {
        path: "/doi-tac",
        component: DoiTacLayout,
        children: [
            {
                path: "",
                redirect: "/doi-tac/tong-quan",
            },
            {
                path: "tong-quan",
                component: TrangTongQuan,
            },
            {
                path: "quan-ly-san-pham",
                component: TrangQLSanPham,
            },
            {
                path: "quan-ly-don-hang",
                component: TrangQLDonHang,
            },
            {
                path: "thong-bao",
                component: TrangThongBao,
            },
            {
                path: "thong-tin-tai-khoan",
                component: TrangThongTinTK,
            },
            {
                path: "doi-mat-khau",
                component: TrangDoiMatKhau,
            },
        ],
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;