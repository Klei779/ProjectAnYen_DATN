<template>
  <Teleport to="body">
    <Transition name="hotline-fade">
      <div
          v-if="show"
          class="hotline-modal-overlay"
          role="dialog"
          aria-modal="true"
          aria-labelledby="hotline-modal-title"
          @click.self="closePopup"
      >
        <div class="hotline-modal">
          <!-- HEADER -->
          <div class="hotline-modal-header">
            <div class="hotline-header-icon">
              <i class="fa-solid fa-headset"></i>
            </div>

            <div class="hotline-header-content">
              <span class="hotline-eyebrow">
                HỖ TRỢ KHÁCH HÀNG
              </span>

              <h2 id="hotline-modal-title">
                Hotline An Yên
              </h2>

              <p>
                Đội ngũ An Yên luôn sẵn sàng lắng nghe và hỗ trợ quý khách.
              </p>
            </div>

            <button
                type="button"
                class="hotline-close-btn"
                aria-label="Đóng popup"
                @click="closePopup"
            >
              <i class="fa-solid fa-xmark"></i>
            </button>
          </div>

          <!-- TRẠNG THÁI -->
          <div class="hotline-status">
            <span class="status-dot"></span>

            <span>
              Đang trực tuyến
            </span>

            <strong>
              Hỗ trợ 24/7
            </strong>
          </div>

          <!-- DANH SÁCH HOTLINE -->
          <div class="phone-list">
            <article
                v-for="item in hotlines"
                :key="item.id"
                class="phone-card"
            >
              <div
                  class="phone-icon"
                  :class="item.iconClass"
              >
                <i :class="item.icon"></i>
              </div>

              <div class="phone-information">
                <span class="phone-role">
                  {{ item.chucVu }}
                </span>

                <a
                    class="phone-number"
                    :href="`tel:${item.soDienThoai}`"
                >
                  {{ formatPhoneNumber(item.soDienThoai) }}
                </a>

                <small>
                  {{ item.moTa }}
                </small>
              </div>

              <a
                  class="call-button"
                  :href="`tel:${item.soDienThoai}`"
                  :aria-label="`Gọi ${item.chucVu}`"
              >
                <i class="fa-solid fa-phone"></i>
                <span>Gọi ngay</span>
              </a>
            </article>
          </div>

          <!-- FOOTER -->
          <div class="hotline-modal-footer">
            <div class="footer-note">
              <i class="fa-solid fa-shield-heart"></i>

              <span>
                Mọi thông tin của quý khách đều được bảo mật.
              </span>
            </div>

            <button
                type="button"
                class="footer-close-button"
                @click="closePopup"
            >
              Đóng
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import {
  onBeforeUnmount,
  onMounted
} from "vue";

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(["close"]);

const hotlines = [
  {
    id: 1,
    chucVu: "Tư vấn dịch vụ",
    soDienThoai: "0826772109",
    moTa: "Tư vấn sản phẩm và dịch vụ mai táng",
    icon: "fa-solid fa-comments",
    iconClass: "consulting"
  },
  {
    id: 2,
    chucVu: "Hỗ trợ khách hàng",
    soDienThoai: "0357402033",
    moTa: "Giải đáp thông tin và hỗ trợ khách hàng",
    icon: "fa-solid fa-headset",
    iconClass: "support"
  },
  {
    id: 3,
    chucVu: "Hotline trực 24/7",
    soDienThoai: "0392168473",
    moTa: "Hỗ trợ khẩn cấp mọi thời điểm",
    icon: "fa-solid fa-phone-volume",
    iconClass: "emergency"
  }
];

function closePopup() {
  emit("close");
}

function formatPhoneNumber(phoneNumber) {
  const cleanedNumber = String(phoneNumber).replace(/\D/g, "");

  if (cleanedNumber.length !== 10) {
    return phoneNumber;
  }

  return cleanedNumber.replace(
      /(\d{4})(\d{3})(\d{3})/,
      "$1 $2 $3"
  );
}

function handleKeydown(event) {
  if (
      event.key === "Escape"
      && props.show
  ) {
    closePopup();
  }
}

onMounted(() => {
  window.addEventListener(
      "keydown",
      handleKeydown
  );
});

onBeforeUnmount(() => {
  window.removeEventListener(
      "keydown",
      handleKeydown
  );
});
</script>

<style scoped>
.hotline-modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 99999;

  display: flex;
  align-items: center;
  justify-content: center;

  padding: 20px;
  box-sizing: border-box;

  overflow-y: auto;

  background: rgba(0, 0, 0, 0.48);

  -webkit-backdrop-filter: blur(4px);
  backdrop-filter: blur(4px);
}

.hotline-modal {
  position: relative;

  width: min(470px, 100%);
  max-height: calc(100dvh - 40px);

  overflow-x: hidden;
  overflow-y: auto;

  border: none;
  border-radius: 18px;

  background: #fffdf9;

  box-shadow:
      0 20px 55px rgba(0, 0, 0, 0.26),
      0 6px 18px rgba(0, 0, 0, 0.12);
}

/* ==================================================
   HEADER
================================================== */

.hotline-modal-header {
  position: relative;

  display: flex;
  align-items: flex-start;
  gap: 13px;

  padding: 22px 62px 20px 22px;

  border-radius: 18px 18px 0 0;

  color: #ffffff;

  background:
      radial-gradient(
          circle at top right,
          rgba(255, 255, 255, 0.1),
          transparent 38%
      ),
      linear-gradient(
          135deg,
          #0b3453 0%,
          #102f47 100%
      );
}

.hotline-header-icon {
  width: 46px;
  height: 46px;
  flex-shrink: 0;

  display: inline-flex;
  align-items: center;
  justify-content: center;

  border: none;
  border-radius: 14px;

  color: #ffffff;
  background: rgba(255, 255, 255, 0.12);

  font-size: 19px;
}

.hotline-header-content {
  min-width: 0;
}

.hotline-eyebrow {
  display: block;

  margin-bottom: 5px;

  color: #e7c47c;

  font-size: 9px;
  font-weight: 800;
  letter-spacing: 1.5px;
}

.hotline-header-content h2 {
  margin: 0 0 6px;

  color: #ffffff;

  font-size: 22px;
  font-weight: 800;
  line-height: 1.2;
}

.hotline-header-content p {
  margin: 0;

  color: rgba(255, 255, 255, 0.76);

  font-size: 11px;
  line-height: 1.55;
}

.hotline-close-btn {
  position: absolute;
  top: 14px;
  right: 14px;

  width: 34px;
  height: 34px;

  display: inline-flex;
  align-items: center;
  justify-content: center;

  padding: 0;

  border: none;
  border-radius: 50%;

  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.1);

  font-size: 14px;

  cursor: pointer;

  transition:
      color 0.22s ease,
      background-color 0.22s ease,
      transform 0.22s ease;
}

.hotline-close-btn:hover {
  color: #ffffff;
  background: #a60b27;

  transform: rotate(90deg);
}

/* ==================================================
   STATUS
================================================== */

.hotline-status {
  min-height: 40px;

  display: flex;
  align-items: center;
  gap: 8px;

  padding: 0 22px;

  border-bottom: 1px solid rgba(11, 52, 83, 0.08);

  color: #667984;
  background: #ffffff;

  font-size: 11px;
}

.hotline-status strong {
  margin-left: auto;

  color: #a60b27;

  font-size: 10px;
  font-weight: 800;
}

.status-dot {
  position: relative;

  width: 7px;
  height: 7px;
  flex-shrink: 0;

  border-radius: 50%;

  background: #28a745;

  box-shadow: 0 0 0 4px rgba(40, 167, 69, 0.12);
}

.status-dot::after {
  content: "";

  position: absolute;
  inset: -4px;

  border: 1px solid rgba(40, 167, 69, 0.35);
  border-radius: 50%;

  animation: hotline-status-pulse 1.8s infinite;
}

/* ==================================================
   PHONE LIST
================================================== */

.phone-list {
  display: flex;
  flex-direction: column;
  gap: 10px;

  padding: 17px 22px;

  background: #fffdf9;
}

.phone-card {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;

  padding: 12px;

  border: 1px solid rgba(11, 52, 83, 0.08);
  border-radius: 13px;

  background: #ffffff;

  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.035);

  transition:
      border-color 0.25s ease,
      box-shadow 0.25s ease,
      transform 0.25s ease;
}

.phone-card:hover {
  border-color: rgba(166, 11, 39, 0.25);

  box-shadow: 0 9px 20px rgba(0, 0, 0, 0.08);

  transform: translateY(-1px);
}

.phone-icon {
  width: 42px;
  height: 42px;
  flex-shrink: 0;

  display: inline-flex;
  align-items: center;
  justify-content: center;

  border-radius: 12px;

  font-size: 16px;
}

.phone-icon.consulting {
  color: #a60b27;
  background: rgba(166, 11, 39, 0.09);
}

.phone-icon.support {
  color: #0b3453;
  background: rgba(11, 52, 83, 0.09);
}

.phone-icon.emergency {
  color: #996811;
  background: rgba(180, 130, 38, 0.13);
}

.phone-information {
  min-width: 0;

  display: flex;
  flex-direction: column;
}

.phone-role {
  margin-bottom: 3px;

  color: #586b76;

  font-size: 10px;
  font-weight: 700;
}

.phone-number {
  width: fit-content;

  color: #0b3453;

  font-size: 17px;
  font-weight: 900;
  letter-spacing: 0.2px;
  text-decoration: none;

  transition: color 0.2s ease;
}

.phone-number:hover {
  color: #a60b27;
}

.phone-information small {
  margin-top: 4px;

  overflow: hidden;

  color: #89979e;

  font-size: 9px;
  line-height: 1.4;

  text-overflow: ellipsis;
  white-space: nowrap;
}

.call-button {
  min-height: 36px;
  padding: 0 12px;

  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;

  border-radius: 999px;

  color: #ffffff;

  background:
      linear-gradient(
          135deg,
          #b51230,
          #8b091f
      );

  font-size: 10px;
  font-weight: 800;
  text-decoration: none;
  white-space: nowrap;

  box-shadow: 0 5px 12px rgba(166, 11, 39, 0.16);

  transition:
      box-shadow 0.23s ease,
      transform 0.23s ease,
      filter 0.23s ease;
}

.call-button:hover {
  box-shadow: 0 8px 17px rgba(166, 11, 39, 0.25);

  transform: translateY(-1px);

  filter: brightness(1.05);
}

/* ==================================================
   FOOTER
================================================== */

.hotline-modal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;

  padding: 14px 22px 17px;

  border-top: 1px solid rgba(11, 52, 83, 0.08);
  border-radius: 0 0 18px 18px;

  background: #fbf7f0;
}

.footer-note {
  display: flex;
  align-items: center;
  gap: 8px;

  color: #74848d;

  font-size: 9px;
  line-height: 1.5;
}

.footer-note i {
  flex-shrink: 0;

  color: #a60b27;

  font-size: 14px;
}

.footer-close-button {
  min-width: 68px;
  min-height: 34px;
  padding: 0 14px;

  border: 1px solid rgba(11, 52, 83, 0.18);
  border-radius: 999px;

  color: #0b3453;
  background: #ffffff;

  font-size: 10px;
  font-weight: 700;

  cursor: pointer;

  transition:
      color 0.22s ease,
      border-color 0.22s ease,
      background-color 0.22s ease;
}

.footer-close-button:hover {
  color: #ffffff;
  border-color: #0b3453;
  background: #0b3453;
}

/* ==================================================
   TRANSITION
================================================== */

.hotline-fade-enter-active,
.hotline-fade-leave-active {
  transition: opacity 0.25s ease;
}

.hotline-fade-enter-active .hotline-modal,
.hotline-fade-leave-active .hotline-modal {
  transition:
      opacity 0.25s ease,
      transform 0.25s ease;
}

.hotline-fade-enter-from,
.hotline-fade-leave-to {
  opacity: 0;
}

.hotline-fade-enter-from .hotline-modal {
  opacity: 0;

  transform: translateY(14px) scale(0.97);
}

.hotline-fade-leave-to .hotline-modal {
  opacity: 0;

  transform: translateY(8px) scale(0.98);
}

@keyframes hotline-status-pulse {
  0% {
    opacity: 1;
    transform: scale(0.8);
  }

  100% {
    opacity: 0;
    transform: scale(1.7);
  }
}

/* ==================================================
   TABLET VÀ MOBILE
================================================== */

@media (max-width: 600px) {
  .hotline-modal-overlay {
    align-items: flex-end;

    padding: 0;

    background: rgba(0, 0, 0, 0.5);
  }

  .hotline-modal {
    width: 100%;
    max-height: calc(100dvh - 16px);

    border-radius: 18px 18px 0 0;
  }

  .hotline-modal-header {
    gap: 11px;

    padding: 20px 54px 18px 16px;

    border-radius: 18px 18px 0 0;
  }

  .hotline-header-icon {
    width: 42px;
    height: 42px;

    border-radius: 12px;

    font-size: 17px;
  }

  .hotline-header-content h2 {
    font-size: 20px;
  }

  .hotline-header-content p {
    font-size: 10px;
  }

  .hotline-close-btn {
    top: 12px;
    right: 12px;

    width: 32px;
    height: 32px;
  }

  .hotline-status {
    min-height: 39px;

    padding: 0 16px;
  }

  .phone-list {
    gap: 9px;

    padding: 15px 16px;
  }

  .phone-card {
    grid-template-columns: 40px minmax(0, 1fr);
    gap: 11px;

    padding: 12px;
  }

  .phone-icon {
    width: 40px;
    height: 40px;

    border-radius: 11px;
  }

  .phone-number {
    font-size: 16px;
  }

  .phone-information small {
    white-space: normal;
  }

  .call-button {
    grid-column: 1 / -1;

    width: 100%;
    min-height: 38px;
  }

  .hotline-modal-footer {
    padding: 13px 16px 16px;
  }
}

/* ==================================================
   MOBILE NHỎ
================================================== */

@media (max-width: 380px) {
  .hotline-header-icon {
    display: none;
  }

  .hotline-modal-header {
    padding-left: 16px;
  }

  .hotline-header-content h2 {
    font-size: 19px;
  }

  .phone-list {
    padding-right: 12px;
    padding-left: 12px;
  }

  .hotline-modal-footer {
    padding-right: 12px;
    padding-left: 12px;
  }

  .footer-note span {
    font-size: 8px;
  }
}
</style>