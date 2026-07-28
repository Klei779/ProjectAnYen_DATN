<template>
  <Teleport to="body">
    <Transition name="login-popup">
      <div
          v-if="show"
          class="modal-overlay"
          @click="closeModal"
      >
        <div
            class="login-modal"
            role="dialog"
            aria-modal="true"
            @click.stop
        >
          <button
              type="button"
              class="close-btn"
              aria-label="Đóng"
              @click="closeModal"
          >
            <i class="fa-solid fa-xmark"></i>
          </button>

          <div class="row g-0 login-modal-row">
            <!-- Banner bên trái -->
            <div class="col-md-5 login-banner">
              <img
                  src="../../assets/images/icon/boat_login.png"
                  alt="Boat"
                  class="boat-image"
              />

              <div class="quote">
                <span class="quote-icon">
                  ❝
                </span>

                <p>
                  An yên trong tâm thức,<br/>
                  trọn vẹn trong từng khoảnh khắc.
                </p>

                <img
                    src="../../assets/images/icon/icon_flower_large.png"
                    alt="Flower"
                    class="flower-divider"
                />
              </div>
            </div>

            <!-- Nội dung bên phải -->
            <div class="col-md-7 login-content">
              <h2>
                {{
                  forgotMode
                      ? "QUÊN MẬT KHẨU"
                      : "ĐĂNG NHẬP"
                }}
              </h2>

              <img
                  src="../../assets/images/icon/icon_flower_large.png"
                  class="title-flower"
                  alt=""
              />

              <!-- Chọn loại tài khoản -->
              <div class="tabs">
                <button
                    type="button"
                    :class="{
                      active: activeTab === 'staff'
                    }"
                    @click="changeTab('staff')"
                >
                  Nhân viên An Yên
                </button>

                <button
                    type="button"
                    :class="{
                      active: activeTab === 'partner'
                    }"
                    @click="changeTab('partner')"
                >
                  Đối tác
                </button>
              </div>

              <!-- FORM ĐĂNG NHẬP -->
              <form
                  v-if="!forgotMode"
                  @submit.prevent="handleLogin"
              >
                <div
                    v-if="hasError"
                    class="error-msg-box"
                >
                  <i
                      class="fa-solid
                             fa-triangle-exclamation"
                  ></i>

                  {{ errorMessage }}
                </div>

                <div
                    v-if="successMessage"
                    class="success-msg-box"
                >
                  <i
                      class="fa-solid
                             fa-circle-check"
                  ></i>

                  {{ successMessage }}
                </div>

                <div
                    class="form-group"
                    :class="{
                      'has-error': hasError
                    }"
                >
                  <label>
                    Tên đăng nhập
                  </label>

                  <el-input
                      v-model="form.username"
                      :prefix-icon="User"
                      placeholder="Nhập tên đăng nhập"
                      @input="clearMessages"
                  />
                </div>

                <div
                    class="form-group"
                    :class="{
                      'has-error': hasError
                    }"
                >
                  <div class="password-header">
                    <label>
                      Mật khẩu
                    </label>

                    <a
                        href="#"
                        @click.prevent="
                          openForgotPassword
                        "
                    >
                      Quên mật khẩu?
                    </a>
                  </div>

                  <el-input
                      v-model="form.password"
                      :prefix-icon="Lock"
                      type="password"
                      show-password
                      placeholder="Nhập mật khẩu"
                      @input="clearMessages"
                  />
                </div>

                <div
                    v-if="loginFailures >= 10"
                    class="captcha-container"
                >
                  <VueTurnstile
                      site-key="
                        1x00000000000000000000AA
                      "
                      v-model="captchaToken"
                  />
                </div>

                <el-button
                    class="login-btn"
                    :class="activeTab"
                    :icon="Lock"
                    :loading="loginLoading"
                    native-type="submit"
                >
                  ĐĂNG NHẬP
                </el-button>
              </form>

              <!-- FORM QUÊN MẬT KHẨU -->
              <form
                  v-else
                  @submit.prevent="
                    handleForgotPassword
                  "
              >
                <p class="forgot-description">
                  Nhập email đã đăng ký với tài khoản
                  {{
                    activeTab === "staff"
                        ? "nhân viên"
                        : "đối tác"
                  }}.

                  Hệ thống sẽ tạo mật khẩu mới
                  và gửi vào email này.
                </p>

                <div
                    v-if="hasError"
                    class="error-msg-box"
                >
                  <i
                      class="fa-solid
                             fa-triangle-exclamation"
                  ></i>

                  {{ errorMessage }}
                </div>

                <div
                    v-if="successMessage"
                    class="success-msg-box"
                >
                  <i
                      class="fa-solid
                             fa-circle-check"
                  ></i>

                  {{ successMessage }}
                </div>

                <div
                    class="form-group"
                    :class="{
                      'has-error': hasError
                    }"
                >
                  <label>
                    Email tài khoản
                  </label>

                  <el-input
                      v-model="forgotForm.email"
                      :prefix-icon="Message"
                      type="email"
                      placeholder="
                        Ví dụ: example@gmail.com
                      "
                      @input="clearMessages"
                  />
                </div>

                <el-button
                    class="login-btn"
                    :class="activeTab"
                    :icon="Message"
                    :loading="forgotLoading"
                    native-type="submit"
                >
                  GỬI MẬT KHẨU MỚI
                </el-button>

                <button
                    type="button"
                    class="back-login-btn"
                    :disabled="forgotLoading"
                    @click="backToLogin"
                >
                  <i
                      class="fa-solid
                             fa-arrow-left"
                  ></i>

                  Quay lại đăng nhập
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import {
  reactive,
  ref,
} from "vue";

import {
  Lock,
  Message,
  User,
} from "@element-plus/icons-vue";

import {
  ElMessage,
} from "element-plus";

import axios from "axios";

import {
  useRouter,
} from "vue-router";

import VueTurnstile
  from "vue-turnstile";

defineProps({
  show: {
    type: Boolean,
    default: false,
  },
});

const router = useRouter();

const emit = defineEmits([
  "close",
  "login-success",
]);

const activeTab = ref("staff");

const forgotMode = ref(false);

const loginFailures = ref(0);

const captchaToken = ref("");

const hasError = ref(false);

const errorMessage = ref("");

const successMessage = ref("");

const loginLoading = ref(false);

const forgotLoading = ref(false);

const form = reactive({
  username: "",
  password: "",
});

const forgotForm = reactive({
  email: "",
});

function clearMessages() {
  hasError.value = false;

  errorMessage.value = "";

  successMessage.value = "";
}

function changeTab(tab) {
  activeTab.value = tab;

  clearMessages();
}

function openForgotPassword() {
  forgotMode.value = true;

  forgotForm.email = "";

  clearMessages();
}

function backToLogin() {
  forgotMode.value = false;

  clearMessages();
}

function resetAll() {
  form.username = "";

  form.password = "";

  forgotForm.email = "";

  forgotMode.value = false;

  captchaToken.value = "";

  clearMessages();
}

function closeModal() {
  if (
      loginLoading.value
      || forgotLoading.value
  ) {
    return;
  }

  resetAll();

  emit("close");
}

function getErrorMessage(
    error,
    fallback
) {
  const data =
      error.response?.data;

  if (
      typeof data === "string"
  ) {
    return data;
  }

  if (
      typeof data?.message === "string"
  ) {
    return data.message;
  }

  if (
      data
      && typeof data === "object"
  ) {
    const firstMessage =
        Object.values(data)
            .find(
                value =>
                    typeof value === "string"
            );

    if (firstMessage) {
      return firstMessage;
    }
  }

  return fallback;
}

const handleForgotPassword =
    async () => {

      clearMessages();

      const email =
          forgotForm.email.trim();

      if (!email) {
        hasError.value = true;

        errorMessage.value =
            "Vui lòng nhập email tài khoản";

        return;
      }

      const emailPattern =
          /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

      if (
          !emailPattern.test(email)
      ) {
        hasError.value = true;

        errorMessage.value =
            "Email không đúng định dạng";

        return;
      }

      forgotLoading.value = true;

      try {
        const response =
            await axios.post(
                "http://localhost:8080/api/auth/forgot-password",
                {
                  email,

                  loaiTaiKhoan:
                      activeTab.value === "staff"
                          ? "NHAN_VIEN"
                          : "DOI_TAC",
                }
            );

        successMessage.value =
            response.data?.message
            || "Mật khẩu mới đã được gửi đến email của bạn";

        forgotForm.email = "";

      } catch (error) {
        console.error(
            "Lỗi quên mật khẩu:",
            error
        );

        hasError.value = true;

        errorMessage.value =
            getErrorMessage(
                error,
                "Không thể gửi mật khẩu mới. Vui lòng thử lại"
            );

      } finally {
        forgotLoading.value = false;
      }
    };

const handleLogin =
    async () => {

      clearMessages();

      if (
          !form.username.trim()
          || !form.password.trim()
      ) {
        hasError.value = true;

        errorMessage.value =
            "Vui lòng nhập đầy đủ tài khoản và mật khẩu";

        return;
      }

      if (
          loginFailures.value >= 10
          && !captchaToken.value
      ) {
        hasError.value = true;

        errorMessage.value =
            "Vui lòng xác nhận bạn không phải là robot";

        return;
      }

      loginLoading.value = true;

      try {
        const response =
            await axios.post(
                "http://localhost:8080/api/auth/login",
                {
                  tenDangNhap:
                  form.username,

                  matKhau:
                  form.password,

                  loaiTaiKhoan:
                      activeTab.value === "staff"
                          ? "NHAN_VIEN"
                          : "DOI_TAC",

                  captchaToken:
                  captchaToken.value,
                }
            );

        if (
            response.data.success
        ) {
          loginFailures.value = 0;

          localStorage.setItem(
              "user",
              JSON.stringify(
                  response.data
              )
          );

          localStorage.setItem(
              "token",
              response.data.token
          );

          window.dispatchEvent(
              new Event(
                  "session-updated"
              )
          );

          emit(
              "login-success",
              response.data
          );

          emit("close");

          ElMessage.success(
              `Xin chào ${response.data.hoTen}`
          );

          const vaiTroChiTiet =
              response.data
                  .vaiTroChiTiet;

          if (
              vaiTroChiTiet === "DOITAC"
          ) {
            router.push(
                "/doi-tac/tong-quan"
            );

          } else if (
              vaiTroChiTiet === "ADMIN"
          ) {
            router.push(
                "/admin/tong-quan"
            );

          } else if (
              vaiTroChiTiet === "HOTLINE"
          ) {
            router.push(
                "/hotline/quan-ly-cong-viec"
            );

          } else if (
              vaiTroChiTiet === "NHANVIEN"
          ) {
            router.push(
                "/nhan-vien/tong-quan"
            );

          } else {
            hasError.value = true;

            errorMessage.value =
                "Không xác định được quyền truy cập";

            form.username = "";

            form.password = "";
          }

        } else {
          loginFailures.value++;

          hasError.value = true;

          errorMessage.value =
              "Sai tài khoản, mật khẩu hoặc không có quyền truy cập";

          form.password = "";
        }

      } catch (error) {
        console.error(error);

        loginFailures.value++;

        hasError.value = true;

        errorMessage.value =
            "Không thể kết nối máy chủ";

      } finally {
        loginLoading.value = false;
      }
    };
</script>

<style
    scoped
    src="../../assets/styles/components/PopDangNhap.css"
></style>