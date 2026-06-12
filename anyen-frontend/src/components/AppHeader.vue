<template>
  <header class="header">

    <h2>An Yên</h2>

    <nav>
      <RouterLink to="/">Trang chủ</RouterLink>

      <RouterLink to="/san-pham">
        Sản phẩm
      </RouterLink>

      <RouterLink to="/dich-vu">
        Dịch vụ
      </RouterLink>

      <RouterLink to="/gioi-thieu">
        Giới thiệu
      </RouterLink>

      <RouterLink to="/lien-he">
        Liên hệ
      </RouterLink>
    </nav>

    <!-- Chưa đăng nhập -->
    <el-button
        v-if="!user"
        class="login-btn"
        :icon="User"
        @click="showLogin = true"
    >
      ĐĂNG NHẬP
    </el-button>

    <!-- Đã đăng nhập -->
    <el-dropdown v-else>

      <span class="user-info">
        Xin chào {{ user.hoTen }}
      </span>

      <template #dropdown>
        <el-dropdown-menu>

          <el-dropdown-item disabled>
            {{ user.loaiTaiKhoan }}
          </el-dropdown-item>

          <el-dropdown-item divided class="logout-dropdown-item">
            <button
                class="btn btn-outline-danger btn-sm rounded-pill px-4 fw-bold"
                @click.stop="logout"
            >
              Đăng xuất
            </button>
          </el-dropdown-item>

        </el-dropdown-menu>
      </template>

    </el-dropdown>

    <LoginModal
        :show="showLogin"
        @close="showLogin = false"
        @login-success="handleLoginSuccess"
    />
    <el-button
        class="hotline-btn"
        type="danger"
        @click="showHotline = true"
    >
      ☎ HOTLINE
    </el-button>
    <HotlineModal
        :show="showHotline"
        @close="showHotline = false"
    />
  </header>
</template>

<script setup>
import {ref, computed} from "vue";
import {useRouter} from "vue-router";
import {User} from "@element-plus/icons-vue";

import LoginModal from "../components/PopDangNhap.vue";
import HotlineModal from "../components/PopLienHeHotline.vue";

const router = useRouter();

const showLogin = ref(false);
const showHotline = ref(false);

const user = ref(
    JSON.parse(localStorage.getItem("user"))
);

const isNhanVien = computed(() =>
    user.value?.loaiTaiKhoan === "NHAN_VIEN"
);

const isDoiTac = computed(() =>
    user.value?.loaiTaiKhoan === "DOI_TAC"
);

const handleLoginSuccess = (userData) => {
  user.value = userData;

  // Lưu đủ dữ liệu để router guard kiểm tra
  localStorage.setItem("user", JSON.stringify(userData));
  localStorage.setItem("token", userData.token);
  localStorage.setItem("loaiTaiKhoan", userData.loaiTaiKhoan);
  localStorage.setItem("tenDangNhap", userData.tenDangNhap);
  localStorage.setItem("id", userData.id);

  showLogin.value = false;

  // Điều hướng theo loại tài khoản
  if (userData.loaiTaiKhoan === "NHAN_VIEN") {
    router.push("/nhan-vien/tong-quan");
  } else if (userData.loaiTaiKhoan === "DOI_TAC") {
    router.push("/doi-tac/tong-quan");
  }
};

const logout = () => {
  localStorage.removeItem("user");
  localStorage.removeItem("token");
  localStorage.removeItem("loaiTaiKhoan");
  localStorage.removeItem("tenDangNhap");
  localStorage.removeItem("id");


  router.push("/");
};
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  border-bottom: 1px solid #eee;
}

nav {
  display: flex;
  gap: 20px;
}

.user-info {
  cursor: pointer;
  font-weight: 600;
}

:deep(.logout-dropdown-item) {
  padding: 8px 12px !important;
  background: transparent !important;
}

:deep(.logout-dropdown-item:hover) {
  background: transparent !important;
}

.logout-btn {
  width: 120px;
  height: 36px;
  border: 1px solid #8b1024;
  border-radius: 999px;
  background-color: #fff;
  color: #8b1024;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s ease;

  appearance: none;
  -webkit-appearance: none;
  outline: none;
  box-shadow: none;

  display: flex;
  align-items: center;
  justify-content: center;
}

.logout-btn:hover {
  background-color: #8b1024;
  color: #fff;
}

</style>