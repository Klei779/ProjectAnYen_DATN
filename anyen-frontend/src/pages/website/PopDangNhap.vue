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
                  src="../../assets/images/icon/boat_login.png"
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
                    src="../../assets/images/icon/icon_flower_large.png"
                    alt="Flower"
                    class="flower-divider"
                />
              </div>

            </div>

            <!-- Content -->
            <div class="col-7 login-content">

              <h2>ĐĂNG NHẬP</h2>

              <img
                  src="../../assets/images/icon/icon_flower_large.png"
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

                <div v-if="hasError" class="error-msg-box">
                  <i class="fa-solid fa-triangle-exclamation"></i> {{ errorMessage }}
                </div>

                <div class="form-group" :class="{'has-error': hasError}">
                  <label>Email hoặc số điện thoại</label>

                  <el-input v-model="form.username" :prefix-icon="User"
                            placeholder="Nhập email hoặc số điện thoại" @input="hasError = false" />
                </div>

                <div class="form-group" :class="{'has-error': hasError}">

                  <div class="password-header">
                    <label>Mật khẩu</label>

                    <a href="#">
                      Quên mật khẩu?
                    </a>
                  </div>

                  <el-input v-model="form.password" :prefix-icon="Lock" type="password" show-password
                            placeholder="Nhập mật khẩu" @input="hasError = false" />

                </div>

                <div v-if="loginFailures >= 10" class="captcha-container" style="margin-top: 15px; margin-bottom: 15px; display: flex; justify-content: center;">
                   <vue-turnstile site-key="1x00000000000000000000AA" v-model="captchaToken" />
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
import VueTurnstile from "vue-turnstile";

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
const loginFailures = ref(0);
const captchaToken = ref("");
const hasError = ref(false);
const errorMessage = ref("");

const form = reactive({
  username: "",
  password: ""
});

const handleLogin = async () => {
  hasError.value = false;
  errorMessage.value = "";

  if (!form.username.trim() || !form.password.trim()) {
      hasError.value = true;
      errorMessage.value = "Vui lòng nhập đầy đủ tài khoản và mật khẩu";
      return;
  }

  if (loginFailures.value >= 10 && !captchaToken.value) {
      hasError.value = true;
      errorMessage.value = "Vui lòng xác nhận bạn không phải là robot";
      return;
  }

  try {

    const response = await axios.post(
        "http://localhost:8080/api/auth/login",
        {
          tenDangNhap: form.username,
          matKhau: form.password,
          loaiTaiKhoan:
              activeTab.value === "staff"
                  ? "NHAN_VIEN"
                  : "DOI_TAC",
          captchaToken: captchaToken.value
        }
    );

    if (response.data.success) {
      
      loginFailures.value = 0; // Reset failures on success

      // Lưu thông tin user
      localStorage.setItem(
          "user",
          JSON.stringify(response.data)
      );

      // Lưu JWT
      localStorage.setItem(
          "token",
          response.data.token
      );
      
      // Update session global state via custom event if needed
      window.dispatchEvent(new Event('session-updated'));

      emit(
          "login-success",
          response.data
      );

      emit("close");

      ElMessage.success(
          `Xin chào ${response.data.hoTen}`
      );

      // Điều hướng theo role
      const vaiTroChiTiet = response.data.vaiTroChiTiet;
      if (vaiTroChiTiet === "DOITAC") {
        router.push("/doi-tac/tong-quan");
      } else if (vaiTroChiTiet === "ADMIN") {
        router.push("/admin/tong-quan");
      } else if (vaiTroChiTiet === "HOTLINE") {
        router.push("/hotline/quan-ly-cong-viec");
      } else if (vaiTroChiTiet === "NHANVIEN") {
        router.push("/nhan-vien/tong-quan");
      } else {
        hasError.value = true;
        errorMessage.value = "Không xác định được quyền truy cập";
        form.username = "";
        form.password = "";
      }

    } else {
      loginFailures.value++;
      hasError.value = true;
      errorMessage.value = "Sai tài khoản, mật khẩu hoặc không có quyền truy cập";
      form.username = "";
      form.password = "";
    }

  } catch (error) {

    console.error(error);
    loginFailures.value++;
    hasError.value = true;
    errorMessage.value = "Không thể kết nối máy chủ";
  }

};
</script>

<style scoped src="../../assets/styles/components/PopDangNhap.css"></style>