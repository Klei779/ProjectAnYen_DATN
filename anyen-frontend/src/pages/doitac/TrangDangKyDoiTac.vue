<script setup>
import { computed, nextTick, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";

import logoAnYen from "../../assets/images/icon/logoAnYen.png";
import dividerIcon from "../../assets/images/icon/divider_icon.png";

const route = useRoute();
const router = useRouter();

/* =========================
   TOKEN VÀ TRẠNG THÁI TRANG
========================= */

const token = ref("");
const currentStep = ref(1);

const errorMessage = ref("");
const successMessage = ref("");
const isSubmitting = ref(false);

/* =========================
   FORM
========================= */

const formData = ref({
  tenDoiTac: "",
  tenDoanhNghiep: "",
  maSoThue: "",
  tenDangNhap: "",
  matKhau: "",
  soDienThoai: "",
  diaChi: ""
});

const confirmPassword = ref("");

const showPassword = ref(false);
const showConfirmPassword = ref(false);

/* =========================
   ĐIỀU KHOẢN
========================= */

const termsContent = ref(null);
const isTermsRead = ref(false);
const isAcceptedTerms = ref(false);

/* =========================
   STEPPER
========================= */

const steps = [
  {
    number: 1,
    title: "Thông tin đối tác",
    icon: "bi-building"
  },
  {
    number: 2,
    title: "Tài khoản đăng nhập",
    icon: "bi-person-lock"
  },
  {
    number: 3,
    title: "Điều khoản hợp tác",
    icon: "bi-file-earmark-check"
  },
  {
    number: 4,
    title: "Hoàn tất",
    icon: "bi-check-lg"
  }
];

const progressWidth = computed(() => {
  if (currentStep.value <= 1) {
    return "0%";
  }

  const progress =
      ((currentStep.value - 1) / (steps.length - 1)) * 100;

  return `${progress}%`;
});

/* =========================
   KHỞI TẠO
========================= */

onMounted(() => {
  const queryToken = route.query.token;

  token.value =
      typeof queryToken === "string"
          ? queryToken.trim()
          : "";

  if (!token.value) {
    errorMessage.value =
        "Liên kết đăng ký không hợp lệ hoặc thiếu mã xác nhận.";
  }
});

/* =========================
   HỖ TRỢ
========================= */

const clearMessage = () => {
  errorMessage.value = "";
};

const scrollToForm = async () => {
  await nextTick();

  const element =
      document.querySelector(".registration-card");

  element?.scrollIntoView({
    behavior: "smooth",
    block: "start"
  });
};

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};

const toggleConfirmPasswordVisibility = () => {
  showConfirmPassword.value =
      !showConfirmPassword.value;
};

const handleTermsScroll = () => {
  const element = termsContent.value;

  if (!element) {
    return;
  }

  const distanceToBottom =
      element.scrollHeight -
      element.scrollTop -
      element.clientHeight;

  if (distanceToBottom <= 8) {
    isTermsRead.value = true;
  }
};

/* =========================
   VALIDATE
========================= */

const validatePhoneNumber = (phoneNumber) => {
  const normalizedPhone =
      phoneNumber.replace(/\s+/g, "");

  return /^(0|\+84)[0-9]{9,10}$/.test(
      normalizedPhone
  );
};

const validateTaxCode = (taxCode) => {
  if (!taxCode) {
    return true;
  }

  return /^[0-9-]{10,14}$/.test(taxCode);
};

const validateStepOne = () => {
  const data = formData.value;

  if (!data.tenDoiTac.trim()) {
    errorMessage.value =
        "Vui lòng nhập họ tên người đại diện.";

    return false;
  }

  if (!data.soDienThoai.trim()) {
    errorMessage.value =
        "Vui lòng nhập số điện thoại liên hệ.";

    return false;
  }

  if (!validatePhoneNumber(data.soDienThoai)) {
    errorMessage.value =
        "Số điện thoại không đúng định dạng.";

    return false;
  }

  if (
      data.maSoThue.trim() &&
      !validateTaxCode(data.maSoThue.trim())
  ) {
    errorMessage.value =
        "Mã số thuế không đúng định dạng.";

    return false;
  }

  if (!data.diaChi.trim()) {
    errorMessage.value =
        "Vui lòng nhập địa chỉ kinh doanh.";

    return false;
  }

  return true;
};

const validateStepTwo = () => {
  const data = formData.value;

  if (!data.tenDangNhap.trim()) {
    errorMessage.value =
        "Vui lòng nhập tên đăng nhập.";

    return false;
  }

  if (data.tenDangNhap.trim().length < 4) {
    errorMessage.value =
        "Tên đăng nhập phải có ít nhất 4 ký tự.";

    return false;
  }

  if (!data.matKhau) {
    errorMessage.value =
        "Vui lòng nhập mật khẩu.";

    return false;
  }

  if (data.matKhau.length < 6) {
    errorMessage.value =
        "Mật khẩu phải có ít nhất 6 ký tự.";

    return false;
  }

  if (data.matKhau !== confirmPassword.value) {
    errorMessage.value =
        "Mật khẩu xác nhận không khớp.";

    return false;
  }

  return true;
};

const validateStepThree = () => {
  if (!isTermsRead.value) {
    errorMessage.value =
        "Vui lòng đọc hết nội dung điều khoản.";

    return false;
  }

  if (!isAcceptedTerms.value) {
    errorMessage.value =
        "Vui lòng đồng ý với điều khoản hợp tác.";

    return false;
  }

  return true;
};

/* =========================
   CHUYỂN BƯỚC
========================= */

const goToNextStep = async () => {
  clearMessage();

  let isValid = false;

  if (currentStep.value === 1) {
    isValid = validateStepOne();
  } else if (currentStep.value === 2) {
    isValid = validateStepTwo();
  } else if (currentStep.value === 3) {
    isValid = validateStepThree();
  }

  if (!isValid) {
    await scrollToForm();
    return;
  }

  if (currentStep.value < 3) {
    currentStep.value += 1;
    await scrollToForm();
    return;
  }

  await submitRegistration();
};

const goToPreviousStep = async () => {
  clearMessage();

  if (currentStep.value > 1) {
    currentStep.value -= 1;
    await scrollToForm();
  }
};

/* =========================
   GỬI ĐĂNG KÝ
========================= */

const submitRegistration = async () => {
  if (!token.value) {
    errorMessage.value =
        "Không tìm thấy mã xác nhận đăng ký.";

    return;
  }

  if (!validateStepOne()) {
    currentStep.value = 1;
    await scrollToForm();
    return;
  }

  if (!validateStepTwo()) {
    currentStep.value = 2;
    await scrollToForm();
    return;
  }

  if (!validateStepThree()) {
    currentStep.value = 3;
    await scrollToForm();
    return;
  }

  try {
    isSubmitting.value = true;
    clearMessage();

    const payload = {
      token: token.value,
      tenDoiTac:
          formData.value.tenDoiTac.trim(),
      tenDoanhNghiep:
          formData.value.tenDoanhNghiep.trim(),
      maSoThue:
          formData.value.maSoThue.trim(),
      tenDangNhap:
          formData.value.tenDangNhap.trim(),
      matKhau:
      formData.value.matKhau,
      soDienThoai:
          formData.value.soDienThoai.trim(),
      diaChi:
          formData.value.diaChi.trim()
    };

    await axios.post(
        "http://localhost:8080/api/auth/doi-tac/ky-hop-dong",
        payload
    );

    successMessage.value =
        "Hồ sơ đối tác đã được đăng ký thành công.";

    currentStep.value = 4;

    await scrollToForm();
  } catch (error) {
    console.error(
        "Lỗi đăng ký đối tác:",
        error
    );

    if (
        typeof error.response?.data === "string"
    ) {
      errorMessage.value =
          error.response.data;
    } else if (
        error.response?.data?.message
    ) {
      errorMessage.value =
          error.response.data.message;
    } else if (
        error.response?.status === 400
    ) {
      errorMessage.value =
          "Thông tin đăng ký không hợp lệ hoặc liên kết đã hết hạn.";
    } else if (
        error.response?.status === 409
    ) {
      errorMessage.value =
          "Tên đăng nhập hoặc thông tin đối tác đã tồn tại.";
    } else {
      errorMessage.value =
          "Không thể hoàn tất đăng ký. Vui lòng thử lại.";
    }
  } finally {
    isSubmitting.value = false;
  }
};

/* =========================
   ĐIỀU HƯỚNG
========================= */

const goToHomePage = () => {
  router.push("/");
};
</script>

<template>
  <div class="registration-page">
    <!-- HEADER -->
    <header class="public-header">
      <div class="site-container header-inner">
        <button
            type="button"
            class="brand-button"
            aria-label="Về trang chủ"
            @click="goToHomePage"
        >
          <img
              :src="logoAnYen"
              alt="Logo An Yên"
              class="header-logo"
          />
        </button>

        <a
            href="tel:19001234"
            class="hotline-button"
        >
                    <span class="hotline-icon">
                        <i class="bi bi-telephone-fill"></i>
                    </span>

          <span>
                        <small>Hotline hỗ trợ</small>
                        <strong>1900 1234</strong>
                    </span>
        </a>
      </div>
    </header>

    <!-- HERO -->
    <section class="registration-hero">
      <div class="hero-overlay"></div>

      <div class="site-container hero-content">
        <p class="hero-eyebrow">
          Cổng đăng ký đối tác
        </p>

        <h1>
          Đồng hành cùng
          <span>An Yên</span>
        </h1>

        <div class="hero-divider">
          <span></span>

          <img
              :src="dividerIcon"
              alt=""
          />

          <span></span>
        </div>

        <p class="hero-description">
          Hoàn tất hồ sơ để trở thành đối tác cung cấp
          sản phẩm và dịch vụ trong hệ sinh thái An Yên.
        </p>
      </div>
    </section>

    <!-- MAIN -->
    <main class="main-content">
      <div class="site-container">
        <!-- LINK KHÔNG HỢP LỆ -->
        <section
            v-if="!token"
            class="invalid-token-card"
        >
          <div class="invalid-icon">
            <i class="bi bi-link-45deg"></i>
          </div>

          <h2>Liên kết không hợp lệ</h2>

          <p>
            Đường dẫn đăng ký không tồn tại,
            đã hết hạn hoặc thiếu mã xác nhận.
          </p>

          <button
              type="button"
              class="primary-button"
              @click="goToHomePage"
          >
            Về trang chủ
          </button>
        </section>

        <!-- FORM -->
        <section
            v-else
            class="registration-card"
        >
          <div class="card-heading">
            <div>
                            <span class="section-eyebrow">
                                Hồ sơ đối tác
                            </span>

              <h2>
                Hoàn tất thông tin đăng ký
              </h2>

              <p>
                Thông tin của Quý Đối tác sẽ được
                bảo mật và sử dụng trong quá trình hợp tác.
              </p>
            </div>

            <div class="secure-badge">
              <i class="bi bi-shield-check"></i>
              Bảo mật thông tin
            </div>
          </div>

          <!-- STEPPER -->
          <div class="stepper">
            <div class="stepper-line">
              <div
                  class="stepper-progress"
                  :style="{ width: progressWidth }"
              ></div>
            </div>

            <div
                v-for="step in steps"
                :key="step.number"
                class="step-item"
                :class="{
                                active: currentStep === step.number,
                                completed: currentStep > step.number
                            }"
            >
              <div class="step-circle">
                <i
                    v-if="currentStep > step.number"
                    class="bi bi-check-lg"
                ></i>

                <i
                    v-else
                    class="bi"
                    :class="step.icon"
                ></i>
              </div>

              <span>
                                {{ step.title }}
                            </span>
            </div>
          </div>

          <!-- THÔNG BÁO -->
          <div
              v-if="errorMessage"
              class="message-box error-message"
          >
            <i class="bi bi-exclamation-circle-fill"></i>
            <span>{{ errorMessage }}</span>
          </div>

          <!-- BƯỚC 1 -->
          <form
              v-if="currentStep === 1"
              class="step-content"
              @submit.prevent="goToNextStep"
          >
            <div class="step-header">
              <div class="step-number">
                01
              </div>

              <div>
                <h3>Thông tin đối tác</h3>

                <p>
                  Cung cấp thông tin người đại diện
                  và đơn vị kinh doanh.
                </p>
              </div>
            </div>

            <div class="form-grid">
              <div class="field-group">
                <label for="tenDoiTac">
                  Người đại diện
                  <span>*</span>
                </label>

                <div class="input-wrapper">
                  <i class="bi bi-person"></i>

                  <input
                      id="tenDoiTac"
                      v-model="formData.tenDoiTac"
                      type="text"
                      maxlength="100"
                      autocomplete="name"
                      placeholder="Nhập họ và tên người đại diện"
                  />
                </div>
              </div>

              <div class="field-group">
                <label for="soDienThoai">
                  Số điện thoại
                  <span>*</span>
                </label>

                <div class="input-wrapper">
                  <i class="bi bi-telephone"></i>

                  <input
                      id="soDienThoai"
                      v-model="formData.soDienThoai"
                      type="tel"
                      maxlength="15"
                      autocomplete="tel"
                      placeholder="Ví dụ: 0901234567"
                  />
                </div>
              </div>

              <div class="field-group">
                <label for="tenDoanhNghiep">
                  Tên doanh nghiệp
                </label>

                <div class="input-wrapper">
                  <i class="bi bi-building"></i>

                  <input
                      id="tenDoanhNghiep"
                      v-model="formData.tenDoanhNghiep"
                      type="text"
                      maxlength="150"
                      autocomplete="organization"
                      placeholder="Nhập tên doanh nghiệp nếu có"
                  />
                </div>
              </div>

              <div class="field-group">
                <label for="maSoThue">
                  Mã số thuế
                </label>

                <div class="input-wrapper">
                  <i class="bi bi-receipt"></i>

                  <input
                      id="maSoThue"
                      v-model="formData.maSoThue"
                      type="text"
                      maxlength="14"
                      inputmode="numeric"
                      placeholder="Nhập mã số thuế"
                  />
                </div>
              </div>

              <div class="field-group full-width">
                <label for="diaChi">
                  Địa chỉ kinh doanh
                  <span>*</span>
                </label>

                <div class="input-wrapper">
                  <i class="bi bi-geo-alt"></i>

                  <input
                      id="diaChi"
                      v-model="formData.diaChi"
                      type="text"
                      maxlength="255"
                      autocomplete="street-address"
                      placeholder="Nhập địa chỉ đầy đủ"
                  />
                </div>
              </div>
            </div>

            <div class="form-actions end">
              <button
                  type="submit"
                  class="primary-button"
              >
                Tiếp tục

                <i class="bi bi-arrow-right"></i>
              </button>
            </div>
          </form>

          <!-- BƯỚC 2 -->
          <form
              v-if="currentStep === 2"
              class="step-content"
              @submit.prevent="goToNextStep"
          >
            <div class="step-header">
              <div class="step-number">
                02
              </div>

              <div>
                <h3>Tài khoản đăng nhập</h3>

                <p>
                  Tạo thông tin đăng nhập cho
                  cổng quản lý đối tác An Yên.
                </p>
              </div>
            </div>

            <div class="account-notice">
              <i class="bi bi-info-circle"></i>

              <p>
                Tên đăng nhập nên viết liền, không dấu
                và có ít nhất 4 ký tự.
              </p>
            </div>

            <div class="form-grid">
              <div class="field-group full-width">
                <label for="tenDangNhap">
                  Tên đăng nhập
                  <span>*</span>
                </label>

                <div class="input-wrapper">
                  <i class="bi bi-person-badge"></i>

                  <input
                      id="tenDangNhap"
                      v-model="formData.tenDangNhap"
                      type="text"
                      maxlength="50"
                      autocomplete="username"
                      placeholder="Ví dụ: doitac_anyen"
                  />
                </div>
              </div>

              <div class="field-group">
                <label for="matKhau">
                  Mật khẩu
                  <span>*</span>
                </label>

                <div class="input-wrapper">
                  <i class="bi bi-lock"></i>

                  <input
                      id="matKhau"
                      v-model="formData.matKhau"
                      :type="showPassword ? 'text' : 'password'"
                      maxlength="100"
                      autocomplete="new-password"
                      placeholder="Tối thiểu 6 ký tự"
                  />

                  <button
                      type="button"
                      class="password-button"
                      aria-label="Hiển thị mật khẩu"
                      @click="togglePasswordVisibility"
                  >
                    <i
                        class="bi"
                        :class="
                                                showPassword
                                                    ? 'bi-eye-slash'
                                                    : 'bi-eye'
                                            "
                    ></i>
                  </button>
                </div>
              </div>

              <div class="field-group">
                <label for="confirmPassword">
                  Xác nhận mật khẩu
                  <span>*</span>
                </label>

                <div class="input-wrapper">
                  <i class="bi bi-shield-lock"></i>

                  <input
                      id="confirmPassword"
                      v-model="confirmPassword"
                      :type="
                                            showConfirmPassword
                                                ? 'text'
                                                : 'password'
                                        "
                      maxlength="100"
                      autocomplete="new-password"
                      placeholder="Nhập lại mật khẩu"
                  />

                  <button
                      type="button"
                      class="password-button"
                      aria-label="Hiển thị mật khẩu xác nhận"
                      @click="
                                            toggleConfirmPasswordVisibility
                                        "
                  >
                    <i
                        class="bi"
                        :class="
                                                showConfirmPassword
                                                    ? 'bi-eye-slash'
                                                    : 'bi-eye'
                                            "
                    ></i>
                  </button>
                </div>
              </div>
            </div>

            <div class="form-actions">
              <button
                  type="button"
                  class="secondary-button"
                  @click="goToPreviousStep"
              >
                <i class="bi bi-arrow-left"></i>
                Quay lại
              </button>

              <button
                  type="submit"
                  class="primary-button"
              >
                Tiếp tục
                <i class="bi bi-arrow-right"></i>
              </button>
            </div>
          </form>

          <!-- BƯỚC 3 -->
          <form
              v-if="currentStep === 3"
              class="step-content"
              @submit.prevent="goToNextStep"
          >
            <div class="step-header">
              <div class="step-number">
                03
              </div>

              <div>
                <h3>Điều khoản hợp tác</h3>

                <p>
                  Vui lòng đọc đầy đủ nội dung trước
                  khi gửi hồ sơ đăng ký.
                </p>
              </div>
            </div>

            <div
                ref="termsContent"
                class="terms-container"
                @scroll="handleTermsScroll"
            >
              <h4>1. Điều khoản dịch vụ</h4>

              <p>
                Khi đăng ký trở thành đối tác của An Yên,
                Quý Đối tác đồng ý tuân thủ các điều khoản
                và điều kiện dưới đây.
              </p>

              <p>
                <strong>1.1.</strong>
                Quý Đối tác cam kết cung cấp thông tin
                chính xác, đầy đủ và cập nhật trong suốt
                quá trình hợp tác.
              </p>

              <p>
                <strong>1.2.</strong>
                Đối tác có trách nhiệm bảo mật thông tin
                tài khoản và không chia sẻ thông tin đăng
                nhập cho bên thứ ba.
              </p>

              <p>
                <strong>1.3.</strong>
                Sản phẩm và dịch vụ cung cấp trên hệ thống
                phải có nguồn gốc rõ ràng, đúng mô tả và
                tuân thủ quy định pháp luật.
              </p>

              <p>
                <strong>1.4.</strong>
                An Yên có quyền tạm ngừng hoặc chấm dứt
                hợp tác khi phát hiện hành vi gian lận,
                cung cấp thông tin sai lệch hoặc ảnh hưởng
                đến khách hàng.
              </p>

              <h4>2. Chính sách bảo mật</h4>

              <p>
                <strong>2.1.</strong>
                An Yên cam kết bảo mật thông tin cá nhân
                và doanh nghiệp do Quý Đối tác cung cấp.
              </p>

              <p>
                <strong>2.2.</strong>
                Thông tin được sử dụng cho mục đích xác thực,
                hỗ trợ vận hành, quản lý đơn hàng và các
                hoạt động hợp tác liên quan.
              </p>

              <p>
                <strong>2.3.</strong>
                An Yên không bán, trao đổi hoặc cho thuê
                thông tin của đối tác, trừ trường hợp có
                yêu cầu của cơ quan có thẩm quyền.
              </p>

              <h4>3. Trách nhiệm của các bên</h4>

              <p>
                <strong>3.1.</strong>
                An Yên hỗ trợ đối tác trong quá trình sử
                dụng hệ thống và tiếp nhận đơn hàng.
              </p>

              <p>
                <strong>3.2.</strong>
                Đối tác chịu trách nhiệm về chất lượng,
                giá bán, số lượng và tiến độ cung cấp
                sản phẩm hoặc dịch vụ.
              </p>

              <p>
                <strong>3.3.</strong>
                Hai bên phối hợp xử lý khiếu nại trên
                tinh thần minh bạch, trách nhiệm và bảo
                vệ quyền lợi khách hàng.
              </p>

              <div class="terms-end">
                Bạn đã đọc đến cuối nội dung điều khoản.
              </div>
            </div>

            <div
                v-if="!isTermsRead"
                class="scroll-notice"
            >
              <i class="bi bi-arrow-down-circle"></i>

              Kéo xuống cuối nội dung để mở chức năng xác nhận.
            </div>

            <label
                class="agreement-box"
                :class="{
                                disabled: !isTermsRead
                            }"
            >
              <input
                  v-model="isAcceptedTerms"
                  type="checkbox"
                  :disabled="!isTermsRead"
              />

              <span class="custom-checkbox">
                                <i class="bi bi-check-lg"></i>
                            </span>

              <span>
                                Tôi đã đọc, hiểu và đồng ý với
                                <strong>Điều khoản dịch vụ</strong>
                                và
                                <strong>Chính sách bảo mật</strong>
                                của An Yên.
                            </span>
            </label>

            <div class="form-actions">
              <button
                  type="button"
                  class="secondary-button"
                  :disabled="isSubmitting"
                  @click="goToPreviousStep"
              >
                <i class="bi bi-arrow-left"></i>
                Quay lại
              </button>

              <button
                  type="submit"
                  class="primary-button"
                  :disabled="
                                    isSubmitting ||
                                    !isAcceptedTerms
                                "
              >
                                <span
                                    v-if="isSubmitting"
                                    class="spinner-border spinner-border-sm"
                                ></span>

                <template v-if="isSubmitting">
                  Đang xử lý
                </template>

                <template v-else>
                  Hoàn tất đăng ký
                  <i class="bi bi-check-circle"></i>
                </template>
              </button>
            </div>
          </form>

          <!-- BƯỚC 4 -->
          <div
              v-if="currentStep === 4"
              class="success-content"
          >
            <div class="success-icon">
              <i class="bi bi-check-lg"></i>
            </div>

            <span class="section-eyebrow">
                            Đăng ký thành công
                        </span>

            <h3>
              Chào mừng Quý Đối tác đến với An Yên
            </h3>

            <p>
              {{ successMessage }}
              Tài khoản của Quý Đối tác đã được cập nhật
              trên hệ thống.
            </p>

            <div class="success-summary">
              <div>
                <i class="bi bi-person-check"></i>

                <span>
                                    <small>Người đại diện</small>
                                    <strong>
                                        {{ formData.tenDoiTac }}
                                    </strong>
                                </span>
              </div>

              <div>
                <i class="bi bi-person-badge"></i>

                <span>
                                    <small>Tên đăng nhập</small>
                                    <strong>
                                        {{ formData.tenDangNhap }}
                                    </strong>
                                </span>
              </div>
            </div>

            <button
                type="button"
                class="primary-button"
                @click="goToHomePage"
            >
              Về trang chủ
              <i class="bi bi-house-door"></i>
            </button>
          </div>
        </section>
      </div>
    </main>

    <!-- FOOTER -->
    <footer class="site-footer">
      <div class="site-container footer-content">
        <div class="footer-brand">
          <div class="footer-logo-box">
            <img
                :src="logoAnYen"
                alt="An Yên"
            />
          </div>

          <p>
            Đồng hành cùng gia đình trong những
            khoảnh khắc thiêng liêng.
          </p>
        </div>

        <div class="footer-information">
          <h4>Công ty Cổ phần Dịch vụ An Yên</h4>

          <p>
            <i class="bi bi-geo-alt"></i>
            123 Đường An Lạc, Phường Yên Hòa,
            Quận Cầu Giấy, Hà Nội
          </p>
        </div>

        <div class="footer-contact">
          <a href="tel:19001234">
            <i class="bi bi-telephone"></i>
            1900 1234
          </a>

          <a href="mailto:info@anyen.vn">
            <i class="bi bi-envelope"></i>
            info@anyen.vn
          </a>

          <a href="http://localhost:5173/">
            <i class="bi bi-globe"></i>
            www.anyen.vn
          </a>
        </div>
      </div>

      <div class="footer-bottom">
        © 2026 An Yên. All rights reserved.
      </div>
    </footer>
  </div>
</template>

<style scoped>
:root {
  --navy: #0f2a43;
  --navy-dark: #091d30;
  --red: #9f1429;
  --red-dark: #821022;
  --cream: #f7f2e9;
  --cream-light: #fcfaf6;
  --white: #ffffff;
  --text: #26384a;
  --muted: #6f7c89;
  --border: #e8e1d7;
}

* {
  box-sizing: border-box;
}

.registration-page {
  min-height: 100vh;
  background: #f8f5ef;
  color: #26384a;
  font-family: "Inter", "Segoe UI", Arial, sans-serif;
}

.site-container {
  width: min(1180px, calc(100% - 40px));
  margin: 0 auto;
}

/* HEADER */

.public-header {
  position: relative;
  z-index: 20;
  background: rgba(255, 255, 255, 0.97);
  border-bottom: 1px solid #ebe5dc;
}

.header-inner {
  min-height: 82px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 28px;
}

.brand-button {
  padding: 0;
  background: transparent;
  border: 0;
  cursor: pointer;
}

.header-logo {
  display: block;
  width: 132px;
  height: auto;
}

.header-navigation {
  display: flex;
  align-items: center;
  gap: 32px;
}

.header-navigation a {
  position: relative;
  padding: 28px 0 25px;
  color: #26384a;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
}

.header-navigation a::after {
  position: absolute;
  right: 0;
  bottom: 18px;
  left: 0;
  width: 0;
  height: 2px;
  margin: auto;
  background: #9f1429;
  content: "";
  transition: width 0.25s ease;
}

.header-navigation a:hover {
  color: #9f1429;
}

.header-navigation a:hover::after {
  width: 100%;
}

.hotline-button {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 18px 8px 10px;
  border-radius: 999px;
  background: #9f1429;
  color: #ffffff;
  text-decoration: none;
  box-shadow: 0 6px 18px rgba(159, 20, 41, 0.18);
}

.hotline-icon {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.14);
}

.hotline-button span:last-child {
  display: flex;
  flex-direction: column;
}

.hotline-button small {
  font-size: 10px;
  opacity: 0.85;
}

.hotline-button strong {
  font-size: 14px;
  letter-spacing: 0.4px;
}

/* HERO */

.registration-hero {
  position: relative;
  min-height: 310px;
  overflow: hidden;
  display: grid;
  place-items: center;
  background:
      radial-gradient(
          circle at 50% 0%,
          rgba(255, 255, 255, 0.86),
          transparent 52%
      ),
      linear-gradient(
          135deg,
          #f8f3ea 0%,
          #eee4d5 100%
      );
}

.registration-hero::before,
.registration-hero::after {
  position: absolute;
  border-radius: 50%;
  content: "";
}

.registration-hero::before {
  top: -130px;
  left: -80px;
  width: 360px;
  height: 360px;
  border: 1px solid rgba(159, 20, 41, 0.08);
}

.registration-hero::after {
  right: -120px;
  bottom: -180px;
  width: 430px;
  height: 430px;
  border: 1px solid rgba(15, 42, 67, 0.08);
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background-image:
      linear-gradient(
          rgba(255, 255, 255, 0.08) 1px,
          transparent 1px
      ),
      linear-gradient(
          90deg,
          rgba(255, 255, 255, 0.08) 1px,
          transparent 1px
      );
  background-size: 48px 48px;
}

.hero-content {
  position: relative;
  z-index: 2;
  padding: 48px 20px;
  text-align: center;
}

.hero-eyebrow,
.section-eyebrow {
  display: block;
  margin: 0 0 10px;
  color: #9f1429;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 2.5px;
  text-transform: uppercase;
}

.hero-content h1 {
  margin: 0;
  color: #0f2a43;
  font-family: "Faustina", Georgia, serif;
  font-size: clamp(36px, 5vw, 54px);
  font-weight: 700;
}

.hero-content h1 span {
  color: #9f1429;
}

.hero-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 13px;
  margin: 18px auto;
}

.hero-divider span {
  width: 68px;
  height: 1px;
  background: #bba98e;
}

.hero-divider img {
  width: 27px;
  height: auto;
}

.hero-description {
  max-width: 600px;
  margin: 0 auto;
  color: #60707f;
  font-size: 15px;
  line-height: 1.75;
}

/* MAIN */

.main-content {
  position: relative;
  padding: 58px 0 72px;
}

.registration-card,
.invalid-token-card {
  width: min(930px, 100%);
  margin: 0 auto;
  background: #ffffff;
  border: 1px solid #e8e1d7;
  border-radius: 18px;
  box-shadow: 0 18px 55px rgba(15, 42, 67, 0.1);
}

.registration-card {
  padding: 38px 42px 42px;
}

.card-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  padding-bottom: 28px;
  border-bottom: 1px solid #ece7df;
}

.card-heading h2 {
  margin: 0 0 8px;
  color: #0f2a43;
  font-family: "Faustina", Georgia, serif;
  font-size: 30px;
  font-weight: 700;
}

.card-heading p {
  margin: 0;
  color: #6f7c89;
  line-height: 1.65;
}

.secure-badge {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 9px 13px;
  border-radius: 999px;
  background: #edf4f0;
  color: #27704c;
  font-size: 12px;
  font-weight: 700;
}

/* STEPPER */

.stepper {
  position: relative;
  display: flex;
  justify-content: space-between;
  margin: 34px 18px 42px;
}

.stepper-line {
  position: absolute;
  top: 21px;
  right: 11%;
  left: 11%;
  height: 2px;
  overflow: hidden;
  background: #e7e1d8;
}

.stepper-progress {
  height: 100%;
  background: #9f1429;
  transition: width 0.35s ease;
}

.step-item {
  position: relative;
  z-index: 2;
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 9px;
  color: #8b949e;
  text-align: center;
}

.step-circle {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border: 3px solid #ffffff;
  border-radius: 50%;
  background: #ebe6df;
  color: #8b949e;
  box-shadow: 0 0 0 1px #e2dbd2;
  transition: all 0.25s ease;
}

.step-item span {
  max-width: 140px;
  font-size: 12px;
  font-weight: 600;
}

.step-item.active {
  color: #9f1429;
}

.step-item.active .step-circle {
  background: #9f1429;
  color: #ffffff;
  box-shadow:
      0 0 0 1px #9f1429,
      0 7px 18px rgba(159, 20, 41, 0.2);
}

.step-item.completed {
  color: #0f2a43;
}

.step-item.completed .step-circle {
  background: #0f2a43;
  color: #ffffff;
  box-shadow: 0 0 0 1px #0f2a43;
}

/* MESSAGES */

.message-box {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 24px;
  padding: 13px 15px;
  border-radius: 9px;
  font-size: 14px;
  line-height: 1.5;
}

.error-message {
  border: 1px solid #f1c5cb;
  background: #fff4f5;
  color: #9f1429;
}

/* FORM */

.step-content {
  animation: fadeSlide 0.28s ease;
}

@keyframes fadeSlide {
  from {
    opacity: 0;
    transform: translateY(8px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.step-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 27px;
}

.step-number {
  flex-shrink: 0;
  color: rgba(159, 20, 41, 0.15);
  font-family: "Faustina", Georgia, serif;
  font-size: 43px;
  font-weight: 700;
  line-height: 1;
}

.step-header h3 {
  margin: 0 0 5px;
  color: #0f2a43;
  font-size: 20px;
  font-weight: 700;
}

.step-header p {
  margin: 0;
  color: #788491;
  font-size: 13px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 22px;
}

.field-group.full-width {
  grid-column: 1 / -1;
}

.field-group label {
  display: block;
  margin-bottom: 8px;
  color: #34495c;
  font-size: 13px;
  font-weight: 700;
}

.field-group label span {
  color: #9f1429;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 54px;
  border: 1px solid #ddd6cc;
  border-radius: 10px;
  background: #ffffff;
  transition: 0.2s ease;
}

.input-wrapper:focus-within {
  border-color: #9f1429;
  box-shadow: 0 0 0 3px rgba(159, 20, 41, 0.09);
}

.input-wrapper > i {
  flex-shrink: 0;
  margin-left: 16px;
  color: #9f1429;
  font-size: 17px;
}

.input-wrapper input {
  width: 100%;
  min-width: 0;
  padding: 15px 45px 15px 13px;
  border: 0;
  outline: 0;
  background: transparent;
  color: #26384a;
  font-family: inherit;
  font-size: 14px;
}

.input-wrapper input::placeholder {
  color: #a2aab2;
}

.password-button {
  position: absolute;
  right: 14px;
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  padding: 0;
  border: 0;
  background: transparent;
  color: #788491;
  cursor: pointer;
}

.account-notice {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 22px;
  padding: 13px 15px;
  border-radius: 9px;
  background: #f3f7fa;
  color: #4d6478;
  font-size: 13px;
}

.account-notice p {
  margin: 0;
}

/* TERMS */

.terms-container {
  max-height: 310px;
  overflow-y: auto;
  padding: 22px 24px;
  border: 1px solid #ddd6cc;
  border-radius: 12px;
  background: #fcfaf6;
  color: #536271;
  font-size: 13px;
  line-height: 1.75;
}

.terms-container::-webkit-scrollbar {
  width: 7px;
}

.terms-container::-webkit-scrollbar-track {
  background: #ebe5dc;
  border-radius: 10px;
}

.terms-container::-webkit-scrollbar-thumb {
  background: #9f1429;
  border-radius: 10px;
}

.terms-container h4 {
  margin: 18px 0 8px;
  color: #0f2a43;
  font-size: 15px;
}

.terms-container h4:first-child {
  margin-top: 0;
}

.terms-container p {
  margin: 0 0 12px;
}

.terms-end {
  margin-top: 18px;
  padding: 12px;
  border-radius: 8px;
  background: #edf4f0;
  color: #27704c;
  text-align: center;
  font-weight: 700;
}

.scroll-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 13px;
  color: #9f1429;
  font-size: 12px;
  font-weight: 600;
}

.agreement-box {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-top: 21px;
  padding: 16px;
  border: 1px solid #ddd6cc;
  border-radius: 11px;
  cursor: pointer;
}

.agreement-box.disabled {
  background: #f5f3ef;
  color: #929aa2;
  cursor: not-allowed;
}

.agreement-box input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.custom-checkbox {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  display: grid;
  place-items: center;
  border: 2px solid #c9c1b7;
  border-radius: 5px;
  color: transparent;
}

.agreement-box input:checked +
.custom-checkbox {
  border-color: #9f1429;
  background: #9f1429;
  color: #ffffff;
}

.agreement-box > span:last-child {
  color: #536271;
  font-size: 13px;
  line-height: 1.6;
}

/* BUTTONS */

.form-actions {
  display: flex;
  justify-content: space-between;
  gap: 13px;
  margin-top: 34px;
}

.form-actions.end {
  justify-content: flex-end;
}

.primary-button,
.secondary-button {
  min-height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  padding: 12px 24px;
  border-radius: 8px;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: 0.2s ease;
}

.primary-button {
  border: 1px solid #9f1429;
  background: #9f1429;
  color: #ffffff;
  box-shadow: 0 7px 16px rgba(159, 20, 41, 0.15);
}

.primary-button:hover:not(:disabled) {
  border-color: #821022;
  background: #821022;
  transform: translateY(-1px);
}

.primary-button:disabled {
  border-color: #c9afb4;
  background: #c9afb4;
  box-shadow: none;
  cursor: not-allowed;
}

.secondary-button {
  border: 1px solid #d8d1c8;
  background: #ffffff;
  color: #405468;
}

.secondary-button:hover:not(:disabled) {
  border-color: #0f2a43;
  color: #0f2a43;
}

/* SUCCESS */

.success-content {
  max-width: 630px;
  margin: 10px auto 0;
  padding: 25px 10px 10px;
  text-align: center;
  animation: fadeSlide 0.3s ease;
}

.success-icon {
  width: 82px;
  height: 82px;
  display: grid;
  place-items: center;
  margin: 0 auto 21px;
  border-radius: 50%;
  background: #edf4f0;
  color: #27704c;
  font-size: 37px;
  box-shadow: 0 0 0 10px #f7faf8;
}

.success-content h3 {
  margin: 5px 0 12px;
  color: #0f2a43;
  font-family: "Faustina", Georgia, serif;
  font-size: 30px;
}

.success-content > p {
  max-width: 550px;
  margin: 0 auto 25px;
  color: #6f7c89;
  line-height: 1.7;
}

.success-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 13px;
  margin-bottom: 27px;
}

.success-summary > div {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px;
  border: 1px solid #e3ddd4;
  border-radius: 10px;
  background: #fcfaf6;
  text-align: left;
}

.success-summary i {
  color: #9f1429;
  font-size: 22px;
}

.success-summary span {
  display: flex;
  flex-direction: column;
}

.success-summary small {
  color: #8b949e;
  font-size: 11px;
}

.success-summary strong {
  margin-top: 3px;
  color: #0f2a43;
  font-size: 13px;
}

/* INVALID TOKEN */

.invalid-token-card {
  padding: 60px 30px;
  text-align: center;
}

.invalid-icon {
  width: 82px;
  height: 82px;
  display: grid;
  place-items: center;
  margin: 0 auto 20px;
  border-radius: 50%;
  background: #fff0f2;
  color: #9f1429;
  font-size: 38px;
}

.invalid-token-card h2 {
  margin: 0 0 10px;
  color: #0f2a43;
  font-family: "Faustina", Georgia, serif;
}

.invalid-token-card p {
  margin: 0 auto 25px;
  color: #6f7c89;
}

/* FOOTER */

.site-footer {
  background: #0f2a43;
  color: #dbe4ec;
}

.footer-content {
  display: grid;
  grid-template-columns: 1.1fr 1.4fr 0.8fr;
  gap: 45px;
  padding: 42px 0 35px;
}

.footer-logo-box {
  display: inline-block;
  margin-bottom: 13px;
  padding: 7px 12px;
  border-radius: 8px;
  background: #ffffff;
}

.footer-logo-box img {
  display: block;
  width: 110px;
  height: auto;
}

.footer-brand p,
.footer-information p {
  margin: 0;
  color: #b7c4cf;
  font-size: 13px;
  line-height: 1.7;
}

.footer-information h4 {
  margin: 0 0 14px;
  color: #ffffff;
  font-size: 14px;
  text-transform: uppercase;
}

.footer-information i {
  margin-right: 7px;
  color: #e5a6b0;
}

.footer-contact {
  display: flex;
  flex-direction: column;
  gap: 11px;
}

.footer-contact a {
  color: #dbe4ec;
  font-size: 13px;
  text-decoration: none;
}

.footer-contact a:hover {
  color: #ffffff;
}

.footer-contact i {
  width: 20px;
  color: #e5a6b0;
}

.footer-bottom {
  padding: 13px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  background: #091d30;
  color: #8092a2;
  font-size: 11px;
  text-align: center;
}

/* RESPONSIVE */

@media (max-width: 900px) {
  .header-navigation {
    display: none;
  }

  .footer-content {
    grid-template-columns: 1fr 1fr;
  }

  .footer-contact {
    grid-column: 1 / -1;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 20px;
  }
}

@media (max-width: 700px) {
  .site-container {
    width: min(100% - 24px, 1180px);
  }

  .header-inner {
    min-height: 70px;
  }

  .header-logo {
    width: 105px;
  }

  .hotline-button {
    padding-right: 13px;
  }

  .hotline-button small {
    display: none;
  }

  .registration-hero {
    min-height: 270px;
  }

  .main-content {
    padding: 30px 0 48px;
  }

  .registration-card {
    padding: 25px 18px 28px;
    border-radius: 14px;
  }

  .card-heading {
    display: block;
  }

  .secure-badge {
    width: fit-content;
    margin-top: 16px;
  }

  .card-heading h2 {
    font-size: 25px;
  }

  .stepper {
    margin: 28px 0 35px;
  }

  .stepper-line {
    right: 12%;
    left: 12%;
  }

  .step-circle {
    width: 38px;
    height: 38px;
  }

  .stepper-line {
    top: 18px;
  }

  .step-item span {
    display: none;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .field-group.full-width {
    grid-column: auto;
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .form-actions.end {
    flex-direction: column;
  }

  .primary-button,
  .secondary-button {
    width: 100%;
  }

  .success-summary {
    grid-template-columns: 1fr;
  }

  .footer-content {
    grid-template-columns: 1fr;
    gap: 25px;
  }

  .footer-contact {
    grid-column: auto;
    flex-direction: column;
    gap: 11px;
  }
}
</style>