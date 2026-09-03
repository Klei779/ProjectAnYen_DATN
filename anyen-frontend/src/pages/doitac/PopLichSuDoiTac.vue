<script setup>
const props = defineProps({
  doiTac: {
    type: Object,
    default: null,
  },
  finance: {
    type: Object,
    default: null,
  },
  items: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["close"]);

const close = () => emit("close");

const iconByTransaction = (loaiVi, loaiGiaoDich) => {
  if (loaiVi === "QUY") {
    if (loaiGiaoDich === "+") return "fa-plus-circle";
    return "fa-minus-circle";
  }
  if (loaiVi === "VI") {
    if (loaiGiaoDich === "+") return "fa-arrow-down";
    return "fa-arrow-up";
  }
  return "fa-exchange-alt";
};

const classByTransaction = (loaiVi, loaiGiaoDich) => {
  if (loaiVi === "QUY") {
    return loaiGiaoDich === "+" ? "green" : "orange";
  }
  if (loaiVi === "VI") {
    return loaiGiaoDich === "+" ? "blue" : "red";
  }
  return "purple";
};

const formatMoney = (value) => {
  return Number(value || 0).toLocaleString("vi-VN") + " đ";
};

const formatDateTime = (dateTime) => {
  if (!dateTime) return "---";
  const date = new Date(dateTime);
  return date.toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};
</script>

<template>
  <div
      class="history-overlay"
      @click.self="close"
  >
    <div class="history-popup">
      <!-- Header -->
      <div class="history-header">
        <div class="history-header-info">
          <p class="eyebrow">
            Lịch sử giao dịch
          </p>

          <h3>
            {{ props.doiTac?.tenDoiTac || "Đối tác" }}
          </h3>

          <span>
            {{ props.doiTac?.soDienThoai || "Chưa có SĐT" }}

            <template v-if="props.doiTac?.email">
              • {{ props.doiTac.email }}
            </template>
          </span>
        </div>

        <button
            type="button"
            class="close-btn"
            aria-label="Đóng popup"
            @click="close"
        >
          <i class="fa-solid fa-xmark"></i>
        </button>
      </div>

      <!-- Thông tin tổng quan tài chính -->
      <div class="finance-summary-box">
        <div class="summary-item fund-item">
          <label>Quỹ khả dụng</label>

          <b>
            {{ formatMoney(props.finance?.soDuQuyKhaDung) }}
          </b>
        </div>

        <div class="summary-item wallet-item">
          <label>Số dư Ví</label>

          <b>
            {{ formatMoney(props.finance?.soDuVi) }}
          </b>
        </div>

        <div class="summary-item status-item">
          <label>Trạng thái Quỹ</label>

          <b :class="{ active: props.finance?.daMoQuy }">
            {{ props.finance?.daMoQuy ? "Đã mở quỹ" : "Chưa mở quỹ" }}
          </b>
        </div>
      </div>

      <!-- Nội dung lịch sử -->
      <div class="history-body">
        <!-- Đang tải -->
        <div
            v-if="props.loading"
            class="empty-state"
        >
          <div class="empty-state-icon loading-icon">
            <i class="fa-solid fa-spinner fa-spin"></i>
          </div>

          <span>Đang tải lịch sử giao dịch...</span>
        </div>

        <!-- Không có dữ liệu -->
        <div
            v-else-if="!props.items.length"
            class="empty-state"
        >
          <div class="empty-state-icon">
            <i class="fa-regular fa-folder-open"></i>
          </div>

          <span>
            Chưa có lịch sử giao dịch
          </span>
        </div>

        <!-- Timeline -->
        <div
            v-else
            class="timeline"
        >
          <div
              v-for="(item, index) in props.items"
              :key="index"
              class="timeline-item"
          >
            <div
                class="timeline-icon"
                :class="classByTransaction(item.loaiVi, item.loaiGiaoDich)"
            >
              <i
                  class="fa-solid"
                  :class="iconByTransaction(item.loaiVi, item.loaiGiaoDich)"
              ></i>
            </div>

            <div class="timeline-content">
              <div class="timeline-top">
                <h4>
                  {{ item.loaiVi === "QUY" ? "Quỹ bảo đảm" : "Ví đối tác" }}
                </h4>

                <span>
                  {{ formatDateTime(item.thoiGian) }}
                </span>
              </div>

              <p>
                {{ item.noiDung || "Giao dịch" }}
              </p>

              <div class="timeline-amount">
                <span
                    :class="item.loaiGiaoDich === '+' ? 'amount-plus' : 'amount-minus'"
                >
                  {{ item.loaiGiaoDich }}{{ formatMoney(item.soTien) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* =========================
   Font chữ của popup
   ========================= */

.history-popup,
.history-popup div,
.history-popup p,
.history-popup h1,
.history-popup h2,
.history-popup h3,
.history-popup h4,
.history-popup span,
.history-popup label,
.history-popup b,
.history-popup button {
  font-family:
      Inter,
      "Segoe UI",
      Roboto,
      "Helvetica Neue",
      Arial,
      sans-serif !important;

  font-synthesis: none;
}

.history-popup i.fa-solid,
.history-popup i.fas {
  font-family: "Font Awesome 6 Free" !important;
  font-style: normal !important;
  font-weight: 900 !important;
}

.history-popup i.fa-regular,
.history-popup i.far {
  font-family: "Font Awesome 6 Free" !important;
  font-style: normal !important;
  font-weight: 400 !important;
}

/* =========================
   Overlay
   ========================= */

.history-overlay {
  position: fixed;
  inset: 0;
  z-index: 10090;

  display: flex;
  align-items: center;
  justify-content: center;

  padding: 20px;
  box-sizing: border-box;
  overflow-y: auto;

  background: rgba(15, 23, 42, 0.62);

  backdrop-filter: blur(3px);
  -webkit-backdrop-filter: blur(3px);

  animation: overlay-fade-in 0.2s ease;
}

/* =========================
   Popup chính
   ========================= */

.history-popup {
  width: min(94vw, 820px);
  max-height: calc(100dvh - 40px);

  display: flex;
  flex-direction: column;

  overflow: hidden;

  color: #1e293b;
  background: #ffffff;

  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 18px;

  box-shadow:
      0 25px 70px rgba(15, 23, 42, 0.28),
      0 8px 25px rgba(15, 23, 42, 0.12);

  animation: popup-show 0.22s ease;
}

/* =========================
   Header
   ========================= */

.history-header {
  flex-shrink: 0;

  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;

  padding: 22px 24px;

  border-bottom: 1px solid #eef2f7;

  background:
      linear-gradient(
          135deg,
          rgba(59, 130, 246, 0.08),
          rgba(255, 255, 255, 0.95)
      );
}

.history-header-info {
  min-width: 0;
}

.eyebrow {
  margin: 0 0 5px;

  color: #2563eb;

  font-size: 11px;
  font-weight: 800;
  line-height: 1.4;
  letter-spacing: 0.09em;
  text-transform: uppercase;
}

.history-header h3 {
  margin: 0;

  color: #111827;

  font-size: 22px;
  font-weight: 700;
  line-height: 1.4;

  overflow-wrap: anywhere;
}

.history-header span {
  display: block;

  margin-top: 7px;

  color: #64748b;

  font-size: 13px;
  font-weight: 400;
  line-height: 1.55;

  overflow-wrap: anywhere;
}

.close-btn {
  width: 38px;
  height: 38px;
  flex-shrink: 0;

  display: inline-flex;
  align-items: center;
  justify-content: center;

  padding: 0;

  border: 1px solid #e2e8f0;
  border-radius: 50%;

  color: #475569;
  background: rgba(255, 255, 255, 0.85);

  font-size: 16px;

  cursor: pointer;

  transition:
      color 0.2s ease,
      background-color 0.2s ease,
      border-color 0.2s ease,
      transform 0.2s ease;
}

.close-btn:hover {
  color: #ffffff;
  background: #2563eb;
  border-color: #2563eb;

  transform: rotate(5deg);
}

.close-btn:focus-visible {
  outline: 3px solid rgba(37, 99, 235, 0.2);
  outline-offset: 2px;
}

/* =========================
   Tổng quan tài chính
   ========================= */

.finance-summary-box {
  flex-shrink: 0;

  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;

  margin: 17px 24px 0;
  padding: 14px;

  border: 1px solid #edf0f4;
  border-radius: 15px;

  background: #f8fafc;
}

.summary-item {
  min-width: 0;

  padding: 10px 12px;

  border: 1px solid transparent;
  border-radius: 11px;

  background: #ffffff;

  transition:
      border-color 0.2s ease,
      box-shadow 0.2s ease;
}

.summary-item:hover {
  border-color: #bfdbfe;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.05);
}

.summary-item.fund-item {
  border-left: 3px solid #f59e0b;
}

.summary-item.wallet-item {
  border-left: 3px solid #10b981;
}

.summary-item.status-item {
  border-left: 3px solid #64748b;
}

.finance-summary-box label {
  display: block;

  margin-bottom: 6px;

  color: #64748b;

  font-size: 11px;
  font-weight: 500;
  line-height: 1.4;
}

.finance-summary-box b {
  display: block;

  overflow: hidden;

  color: #1e293b;

  font-size: 13px;
  font-weight: 700;
  line-height: 1.45;

  text-overflow: ellipsis;
  white-space: nowrap;
}

.finance-summary-box b.active {
  color: #10b981;
}

/* =========================
   Nội dung popup
   ========================= */

.history-body {
  flex: 1;
  min-height: 0;

  padding: 20px 24px 25px;

  overflow-y: auto;
  overscroll-behavior: contain;

  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}

.history-body::-webkit-scrollbar {
  width: 7px;
}

.history-body::-webkit-scrollbar-track {
  background: transparent;
}

.history-body::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 20px;
}

.history-body::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* =========================
   Loading và không có dữ liệu
   ========================= */

.empty-state {
  min-height: 200px;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;

  padding: 25px;
  box-sizing: border-box;

  color: #64748b;

  font-size: 14px;
  font-weight: 400;
  line-height: 1.6;
  text-align: center;
}

.empty-state-icon {
  width: 58px;
  height: 58px;

  display: grid;
  place-items: center;

  color: #2563eb;
  background: #eff6ff;

  border: 1px solid #dbeafe;
  border-radius: 18px;

  font-size: 22px;
}

.loading-icon {
  color: #2563eb;
  background: #eff6ff;
  border-color: #dbeafe;
}

/* =========================
   Timeline
   ========================= */

.timeline {
  position: relative;

  display: flex;
  flex-direction: column;
  gap: 15px;
}

.timeline-item {
  position: relative;

  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 13px;
}

.timeline-item:not(:last-child)::before {
  content: "";

  position: absolute;
  top: 42px;
  bottom: -15px;
  left: 20px;

  width: 2px;

  background: #e2e8f0;
  border-radius: 10px;
}

.timeline-icon {
  position: relative;
  z-index: 1;

  width: 42px;
  height: 42px;

  display: grid;
  place-items: center;

  box-sizing: border-box;

  border: 3px solid #ffffff;
  border-radius: 50%;

  font-size: 14px;

  box-shadow: 0 0 0 1px #e2e8f0;
}

.timeline-icon.green {
  color: #16a34a;
  background: #ecfdf5;
}

.timeline-icon.orange {
  color: #ea580c;
  background: #fff7ed;
}

.timeline-icon.blue {
  color: #2563eb;
  background: #eff6ff;
}

.timeline-icon.red {
  color: #dc2626;
  background: #fff1f2;
}

.timeline-icon.purple {
  color: #9333ea;
  background: #faf5ff;
}

/* =========================
   Nội dung từng lịch sử
   ========================= */

.timeline-content {
  min-width: 0;

  padding: 15px;

  border: 1px solid #e9edf3;
  border-radius: 14px;

  background: #ffffff;

  box-shadow: 0 5px 15px rgba(15, 23, 42, 0.035);

  transition:
      border-color 0.2s ease,
      box-shadow 0.2s ease,
      transform 0.2s ease;
}

.timeline-content:hover {
  border-color: #d8dee8;

  box-shadow: 0 9px 22px rgba(15, 23, 42, 0.07);

  transform: translateY(-1px);
}

.timeline-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.timeline-top h4 {
  min-width: 0;

  margin: 0;

  color: #0f172a;

  font-size: 14px;
  font-weight: 700;
  line-height: 1.5;

  overflow-wrap: anywhere;
}

.timeline-top span {
  flex-shrink: 0;

  color: #94a3b8;

  font-size: 11px;
  font-weight: 500;
  line-height: 1.5;

  white-space: nowrap;
}

.timeline-content p {
  margin: 9px 0 11px;

  color: #475569;

  font-size: 13px;
  font-weight: 400;
  line-height: 1.65;

  overflow-wrap: anywhere;
}

.timeline-amount {
  min-height: 25px;

  display: inline-flex;
  align-items: center;

  padding: 0 10px;

  border: 1px solid #e7ebf0;
  border-radius: 999px;

  font-size: 13px;
  font-weight: 700;
  line-height: 1.4;
}

.amount-plus {
  color: #16a34a;
  background: #ecfdf5;
}

.amount-minus {
  color: #dc2626;
  background: #fff1f2;
}

/* =========================
   Animation
   ========================= */

@keyframes overlay-fade-in {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes popup-show {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.985);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* =========================
   Responsive tablet
   ========================= */

@media (max-width: 720px) {
  .history-overlay {
    padding: 12px;
  }

  .history-popup {
    width: 100%;
    max-height: calc(100dvh - 24px);

    border-radius: 16px;
  }

  .history-header {
    padding: 18px 16px;
  }

  .history-header h3 {
    font-size: 19px;
  }

  .finance-summary-box {
    grid-template-columns: 1fr;

    gap: 9px;

    margin-right: 16px;
    margin-left: 16px;
  }

  .summary-item {
    padding: 9px 11px;
  }

  .history-body {
    padding-right: 16px;
    padding-left: 16px;
  }

  .timeline-top {
    flex-direction: column;
    gap: 4px;
  }

  .timeline-top span {
    white-space: normal;
  }
}

/* =========================
   Responsive điện thoại
   ========================= */

@media (max-width: 480px) {
  .history-overlay {
    padding: 7px;
  }

  .history-popup {
    max-height: calc(100dvh - 14px);

    border-radius: 14px;
  }

  .history-header {
    gap: 12px;

    padding: 16px 14px;
  }

  .eyebrow {
    font-size: 10px;
  }

  .history-header h3 {
    font-size: 17px;
  }

  .history-header span {
    font-size: 12px;
  }

  .close-btn {
    width: 34px;
    height: 34px;

    font-size: 14px;
  }

  .finance-summary-box {
    margin: 12px 14px 0;
    padding: 10px;
  }

  .history-body {
    padding: 16px 14px 20px;
  }

  .timeline-item {
    grid-template-columns: 38px minmax(0, 1fr);
    gap: 9px;
  }

  .timeline-item:not(:last-child)::before {
    top: 38px;
    left: 18px;
  }

  .timeline-icon {
    width: 38px;
    height: 38px;

    font-size: 12px;
  }

  .timeline-content {
    padding: 12px;
  }

  .timeline-top h4 {
    font-size: 13px;
  }

  .timeline-content p {
    font-size: 12px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .history-overlay,
  .history-popup {
    animation: none;
  }

  .close-btn,
  .summary-item,
  .timeline-content {
    transition: none;
  }
}
</style>
