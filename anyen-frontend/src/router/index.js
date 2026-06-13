import { createRouter, createWebHistory } from "vue-router";
import { jwtDecode } from "jwt-decode";
// Layout
import WebsiteLayout from "../layouts/WebsiteLayout.vue";
import DoiTacLayout from "../layouts/DoiTacLayout.vue";
import NhanVienLayout from "../layouts/NhanVienLayout.vue";

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

// Nhân viên
import TrangTongQuanNV from "../pages/nhanvien/TrangTongQuan.vue";
import TrangQLDoiTac from "../pages/nhanvien/TrangQLDoiTac.vue";
import TrangQLDonHangNV from "../pages/nhanvien/TrangQLDonHang.vue";
import TrangThongBaoNV from "../pages/nhanvien/TrangThongBao.vue";
import TrangThongTinNV from "../pages/nhanvien/TrangThongTinTK.vue";
import TrangQLKhachHang from "../pages/nhanvien/TrangQLKhachHang.vue";
import TrangQLHopDong from "../pages/nhanvien/TrangQLHopDong.vue";
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
        meta: {
            requiresAuth: true,
            role: "DOI_TAC",
        },
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

    // NHÂN VIÊN
    {
        path: "/nhan-vien",
        component: NhanVienLayout,
        meta: {
            requiresAuth: true,
            role: "NHAN_VIEN",
        },
        children: [
            {
                path: "",
                redirect: "/nhan-vien/tong-quan",
            },
            {
                path: "tong-quan",
                component: TrangTongQuanNV,
            },
            {
                path: "quan-ly-doi-tac",
                component: TrangQLDoiTac,
            },
            {
                path: "quan-ly-don-hang",
                component: TrangQLDonHangNV,
            },
            {
                path: "thong-bao",
                component: TrangThongBaoNV,
            },
            {
                path: "thong-tin-tai-khoan",
                component: TrangThongTinNV,
            },
            {
                path: "quan-ly-khach-hang",
                component: TrangQLKhachHang,
            },
            {
                path: "quan-ly-hop-dong",
                component:TrangQLHopDong,
            }
        ],
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem("token");

    const requiresAuth = to.matched.some(
        (record) => record.meta.requiresAuth
    );

    const requiredRole = to.matched.find(
        (record) => record.meta.role
    )?.meta.role;

    // Route không cần đăng nhập thì cho qua
    if (!requiresAuth) {
        next();
        return;
    }

    // Route cần đăng nhập nhưng không có token
    if (!token) {
        next("/");
        return;
    }

    let roleFromToken = null;

    try {
        const decoded = jwtDecode(token);

        roleFromToken = decoded.role;

        // Kiểm tra token hết hạn
        const now = Date.now() / 1000;

        if (decoded.exp && decoded.exp < now) {
            localStorage.removeItem("user");
            localStorage.removeItem("token");
            localStorage.removeItem("loaiTaiKhoan");
            localStorage.removeItem("tenDangNhap");
            localStorage.removeItem("id");

            next("/");
            return;
        }

    } catch (error) {
        localStorage.removeItem("user");
        localStorage.removeItem("token");
        localStorage.removeItem("loaiTaiKhoan");
        localStorage.removeItem("tenDangNhap");
        localStorage.removeItem("id");

        next("/");
        return;
    }

    // Có token nhưng sai quyền
    if (requiredRole && roleFromToken !== requiredRole) {
        if (roleFromToken === "NHAN_VIEN") {
            next("/nhan-vien/tong-quan");
        } else if (roleFromToken === "DOI_TAC") {
            next("/doi-tac/tong-quan");
        } else {
            next("/");
        }
        return;
    }

    next();
});
export default router;