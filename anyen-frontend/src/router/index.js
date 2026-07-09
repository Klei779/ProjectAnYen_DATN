import { createRouter, createWebHistory } from "vue-router";
import { jwtDecode } from "jwt-decode";
// Layout
import WebsiteLayout from "../layouts/WebsiteLayout.vue";
import DoiTacLayout from "../layouts/DoiTacLayout.vue";
import NhanVienLayout from "../layouts/NhanVienLayout.vue";
import AdminLayout from "../layouts/AdminLayout.vue";
import HotlineLayout from "../layouts/HotlineLayout.vue";
import TrangThongKeDoanhThuDT from "../pages/doitac/TrangThongKeDoanhThuDT.vue";
import TrangThongKeDoanhThuNV from "../pages/nhanvien/TrangThongKeDoanhThuNV.vue";
// Website
import TrangChu from "../pages/website/TrangChu.vue";
import TrangSanPham from "../pages/website/TrangSanPham.vue";
import TrangDichVu from "../pages/website/TrangDichVu.vue";
import DichVuChiTiet from "../pages/website/TrangDichVuChiTiet.vue";
import TrangGioiThieu from "../pages/website/TrangGioiThieu.vue";
import TrangLienHe from "../pages/website/TrangLienHe.vue";
import XacNhanDoiTac from "../pages/website/XacNhanDoiTac.vue";

// Đối tác
import TrangTongQuan from "../pages/doitac/TrangTongQuan.vue";
import TrangQLSanPham from "../pages/doitac/TrangQLSanPham.vue";
import TrangQLDonHang from "../pages/doitac/TrangQLDonHang.vue";
import TrangThongBao from "../pages/doitac/TrangThongBaoDT.vue";
import TrangThongTinTK from "../pages/doitac/TrangThongTinTK.vue";
import TrangDoiMatKhau from "../pages/doitac/TrangDoiMatKhau.vue";
import TrangTaoSanPham from "../pages/doitac/TrangTaoSanPham.vue";
import TrangDangKyDoiTac from "../pages/doitac/TrangDangKyDoiTac.vue";

// Nhân viên
import TrangTongQuanNV from "../pages/nhanvien/TrangTongQuan.vue";
import TrangQLDoiTac from "../pages/admin/TrangQLDoiTac.vue";
import TrangQLDonHangNV from "../pages/nhanvien/TrangQLDonHang.vue";
import TrangThongBaoNV from "../pages/nhanvien/TrangThongBaoNV.vue";
import TrangThongTinNV from "../pages/nhanvien/TrangThongTinTK.vue";
import TrangQLKhachHang from "../pages/nhanvien/TrangQLKhachHang.vue";
import TrangQLHopDong from "../pages/nhanvien/TrangQLHopDong.vue";
import TrangQLNhanVien from "../pages/nhanvien/TrangQLNhanVien.vue";
import TrangDuyetSanPham from "../pages/admin/TrangDuyetSanPham.vue";
import TrangHoaDonCuaToi from "../pages/nhanvien/TrangHoaDonCuaToi.vue";
// Hotline
import TrangQLCongViec from "../pages/hotline/TrangQLCongViec.vue";
import TrangQLDonHangHL from "../pages/hotline/TrangQLDonHang.vue";

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
            {
                path: "xac-nhan-doi-tac",
                component: XacNhanDoiTac,
            },
        ],
    },

    // TRANG PUBLIC (Không cần login)
    {
        path: "/doitac/register",
        name: "TrangDangKyDoiTac",
        component: TrangDangKyDoiTac,
        meta: { requiresAuth: false }
    },

    // ĐỐI TÁC
    {
        path: "/doi-tac",
        component: DoiTacLayout,
        meta: {
            requiresAuth: true,
            role: "DOITAC",
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
                path: "tao-san-pham",
                component: TrangTaoSanPham,
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
                path: "thong-ke-doanh-thu",
                component: TrangThongKeDoanhThuDT,
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

    // NHÂN VIÊN TRỰC TIẾP
    {
        path: "/nhan-vien",
        component: NhanVienLayout,
        meta: {
            requiresAuth: true,
            role: "NHANVIEN",
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
            },
            {
                path: "thong-ke-doanh-thu",
                component: TrangThongKeDoanhThuNV,
            },
            {
                path: "hoa-don-cua-toi",
                component: TrangHoaDonCuaToi,
            },
        ],
    },

    // QUẢN LÝ AN YÊN (ADMIN)
    {
        path: "/admin",
        component: AdminLayout,
        meta: {
            requiresAuth: true,
            role: "ADMIN",
        },
        children: [
            {
                path: "",
                redirect: "/admin/tong-quan",
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
                path: "quan-ly-nhan-vien",
                component: TrangQLNhanVien,
            },
            {
                path: "thong-bao",
                component: TrangThongBaoNV,
            },
            {
                path: "duyet-san-pham",
                component: TrangDuyetSanPham,
            },
            {
                path: "thong-ke-doanh-thu",
                component: TrangThongKeDoanhThuNV,
            },
            {
                path: "thong-tin-tai-khoan",
                component: TrangThongTinNV,
            },
            {
                path: "doi-mat-khau",
                component: TrangDoiMatKhau,
            },
            {
                path: "quan-ly-hoa-don",
                component: TrangHoaDonCuaToi,
            },
        ],
    },

    // HOTLINE
    {
        path: "/hotline",
        component: HotlineLayout,
        meta: {
            requiresAuth: true,
            role: "HOTLINE",
        },
        children: [
            {
                path: "",
                redirect: "/hotline/quan-ly-cong-viec",
            },
            {
                path: "quan-ly-cong-viec",
                component: TrangQLCongViec,
            },
            {
                path: "quan-ly-don-hang",
                component: TrangQLDonHangHL,
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
        if (roleFromToken === "NHANVIEN") {
            next("/nhan-vien/tong-quan");
        } else if (roleFromToken === "DOITAC") {
            next("/doi-tac/tong-quan");
        } else if (roleFromToken === "ADMIN") {
            next("/admin/tong-quan");
        } else if (roleFromToken === "HOTLINE") {
            next("/hotline/quan-ly-cong-viec");
        } else {
            next("/");
        }
        return;
    }

    next();
});
export default router;