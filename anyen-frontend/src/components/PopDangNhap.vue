<template>
  <Transition name="login-popup">
    <div v-if="show" class="modal-overlay" @click="$emit('close')">
      <div class="login-modal" @click.stop>
        <div class="login-modal">

          <button class="close-btn" @click="$emit('close')">
            ✕
          </button>

          <div class="row g-0 h-100">

            <!-- Banner -->
            <div class="col-5 login-banner">

              <img
                  src="../assets/images/icon/boat_login.png"
                  alt="Boat"
                  class="boat-image"
              />

              <div class="quote">
                <span class="quote-icon">❝</span>

                <p>
                  An yên trong tâm thức,<br />
                  trọn vẹn trong từng khoảnh khắc.
                </p>

                <img
                    src="../assets/images/icon/icon_flower_large.png"
                    alt="Flower"
                    class="flower-divider"
                />
              </div>

            </div>

            <!-- Content -->
            <div class="col-7 login-content">

              <h2>ĐĂNG NHẬP</h2>

              <img
                  src="../assets/images/icon/icon_flower_large.png"
                  class="title-flower"
                  alt=""
              />

              <div class="tabs">

                <button
                    type="button"
                    :class="{ active: activeTab === 'staff' }"
                    @click="activeTab = 'staff'"
                >
                  Nhân viên An Yên
                </button>

                <button
                    type="button"
                    :class="{ active: activeTab === 'partner' }"
                    @click="activeTab = 'partner'"
                >
                  Đối tác
                </button>

              </div>

              <form @submit.prevent="handleLogin">

                <div class="form-group">
                  <label>Email hoặc số điện thoại</label>

                  <el-input v-model="form.username" :prefix-icon="User"
                            placeholder="Nhập email hoặc số điện thoại" />
                </div>

                <div class="form-group">

                  <div class="password-header">
                    <label>Mật khẩu</label>

                    <a href="#">
                      Quên mật khẩu?
                    </a>
                  </div>

                  <el-input v-model="form.password" :prefix-icon="Lock" type="password" show-password
                            placeholder="Nhập mật khẩu" />

                </div>

                <el-button class="login-btn" :class="activeTab" :icon="Lock" native-type="submit">
                  ĐĂNG NHẬP
                </el-button>

              </form>

            </div>

          </div>

        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import axios from "axios";
import { useRouter } from "vue-router";

defineProps({
  show: {
    type: Boolean,
    default: false
  }
});

const router = useRouter();

const emit = defineEmits([
  "close",
  "login-success"
]);

const activeTab = ref("staff");

const form = reactive({
  username: "",
  password: ""
});

const handleLogin = async () => {

  try {

    const response = await axios.post(
        "http://localhost:8080/api/auth/login",
        {
          tenDangNhap: form.username,
          matKhau: form.password,
          loaiTaiKhoan:
              activeTab.value === "staff"
                  ? "NHAN_VIEN"
                  : "DOI_TAC"
        }
    );

    if (response.data.success) {

      localStorage.setItem(
          "user",
          JSON.stringify(response.data)
      );

      emit("login-success", response.data);

      emit("close");

      ElMessage.success(
          `Xin chào ${response.data.hoTen}`
      );

      // Chuyển trang
      if (response.data.loaiTaiKhoan === "DOI_TAC") {

        router.push("/doi-tac/tong-quan");

      }

      if (response.data.loaiTaiKhoan === "NHAN_VIEN") {

        router.push("/nhan-vien/tong-quan");

      }

    } else {

      ElMessage.error(
          "Sai tài khoản hoặc mật khẩu"
      );

    }

  } catch (error) {

    console.error(error);

    ElMessage.error(
        "Không thể kết nối máy chủ"
    );

  }

};
</script>

<style scoped src="../assets/styles/PopDangNhap.css"></style>