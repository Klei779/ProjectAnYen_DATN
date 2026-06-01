import { createRouter, createWebHistory } from "vue-router";

import TrangChu from "../pages/TrangChu.vue";
import TrangSanPham from "../pages/TrangSanPham.vue";
import TrangDichVu from "../pages/TrangDichVu.vue";
import TrangGioiThieu from "../pages/TrangGioiThieu.vue";
import TrangLienHe from "../pages/TrangLienHe.vue";

const routes = [
    {
        path: "/",
        component: TrangChu,
    },
    {
        path: "/san-pham",
        component: TrangSanPham,
    },
    {
        path: "/dich-vu",
        component: TrangDichVu,
    },
    {
        path: "/gioi-thieu",
        component: TrangGioiThieu,
    },
    {
        path: "/lien-he",
        component: TrangLienHe,
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;