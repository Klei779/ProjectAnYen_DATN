<script setup>
const props = defineProps({
  customer: {
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

const iconByType = (type) => {
  if (type === "THONG_BAO") return "fa-bell";
  if (type === "DON_HANG") return "fa-cart-shopping";
  if (type === "HOP_DONG") return "fa-file-signature";
  if (type === "HOA_DON") return "fa-receipt";
  return "fa-user-check";
};

const classByType = (type) => {
  if (type === "THONG_BAO") return "blue";
  if (type === "DON_HANG") return "orange";
  if (type === "HOP_DONG") return "purple";
  if (type === "HOA_DON") return "green";
  return "red";
};
</script>

<template>
  <div class="history-overlay" @click.self="close">
    <div class="history-popup">
      <div class="history-header">
        <div>
          <p class="eyebrow">Lịch sử công việc</p>
          <h3>{{ props.customer?.tenKhachHang || "Khách hàng" }}</h3>
          <span>
            {{ props.customer?.soDienThoai || "Chưa có SĐT" }}
            <template v-if="props.customer?.email"> • {{ props.customer.email }}</template>
          </span>
        </div>

        <button class="close-btn" @click="close">
          <i class="fa-solid fa-xmark"></i>
        </button>
      </div>

      <div class="customer-summary-box">
        <div>
          <label>Trạng thái hiện tại</label>
          <b>{{ props.customer?.trangThaiHienTai || "---" }}</b>
        </div>

        <div>
          <label>Giai đoạn hiện tại</label>
          <b>{{ props.customer?.giaiDoanHienTai || "---" }}</b>
        </div>

        <div>
          <label>Nguồn đăng ký</label>
          <b>{{ props.customer?.nguonDangKy || "---" }}</b>
        </div>
      </div>

      <div class="history-body">
        <div v-if="props.loading" class="empty-state">
          <i class="fa-solid fa-spinner fa-spin"></i>
          Đang tải lịch sử...
        </div>

        <div v-else-if="!props.items.length" class="empty-state">
          <i class="fa-regular fa-folder-open"></i>
          Chưa có lịch sử công việc với khách hàng này
        </div>

        <div v-else class="timeline">
          <div
              v-for="(item, index) in props.items"
              :key="index"
              class="timeline-item"
          >
            <div class="timeline-icon" :class="classByType(item.loai)">
              <i class="fa-solid" :class="iconByType(item.loai)"></i>
            </div>

            <div class="timeline-content">
              <div class="timeline-top">
                <h4>{{ item.tieuDe }}</h4>
                <span>{{ item.thoiGian || "---" }}</span>
              </div>

              <p>{{ item.noiDung }}</p>

              <div class="timeline-status">
                {{ item.trangThai || "Chưa cập nhật" }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.history-overlay {
  position: fixed;
  inset: 0;
  z-index: 10090;
  background: rgba(15, 23, 42, 0.58);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px;
  box-sizing: border-box;
}

.history-popup {
  width: min(94vw, 820px);
  max-height: 90vh;
  overflow: hidden;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.3);
  display: flex;
  flex-direction: column;
}

.history-header {
  padding: 20px 24px;
  border-bottom: 1px solid #eef2f7;
  display: flex;
  justify-content: space-between;
  gap: 18px;
}

.eyebrow {
  margin: 0 0 4px;
  color: #dc2626;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.history-header h3 {
  margin: 0;
  color: #0f172a;
  font-size: 22px;
}

.history-header span {
  display: block;
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
}

.close-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: #f8fafc;
  color: #334155;
  cursor: pointer;
}

.customer-summary-box {
  margin: 16px 24px 0;
  padding: 14px;
  border-radius: 15px;
  background: #f8fafc;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.customer-summary-box label {
  display: block;
  color: #64748b;
  font-size: 12px;
  margin-bottom: 5px;
}

.customer-summary-box b {
  color: #0f172a;
  font-size: 13px;
}

.history-body {
  padding: 20px 24px 24px;
  overflow-y: auto;
}

.empty-state {
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #64748b;
  font-size: 14px;
}

.timeline {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.timeline-item {
  display: grid;
  grid-template-columns: 42px 1fr;
  gap: 12px;
  position: relative;
}

.timeline-item:not(:last-child)::before {
  content: "";
  position: absolute;
  left: 20px;
  top: 42px;
  bottom: -14px;
  width: 1px;
  background: #e2e8f0;
}

.timeline-icon {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  z-index: 1;
}

.timeline-icon.red {
  background: #fff1f2;
  color: #dc2626;
}

.timeline-icon.blue {
  background: #eff6ff;
  color: #2563eb;
}

.timeline-icon.orange {
  background: #fff7ed;
  color: #ea580c;
}

.timeline-icon.purple {
  background: #faf5ff;
  color: #9333ea;
}

.timeline-icon.green {
  background: #ecfdf5;
  color: #16a34a;
}

.timeline-content {
  border: 1px solid #eef2f7;
  border-radius: 15px;
  padding: 14px;
  background: #fff;
}

.timeline-top {
  display: flex;
  justify-content: space-between;
  gap: 14px;
}

.timeline-top h4 {
  margin: 0;
  color: #0f172a;
  font-size: 14px;
}

.timeline-top span {
  color: #94a3b8;
  font-size: 12px;
  white-space: nowrap;
}

.timeline-content p {
  margin: 8px 0 10px;
  color: #475569;
  font-size: 13px;
  line-height: 1.55;
}

.timeline-status {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 9px;
  border-radius: 999px;
  background: #f8fafc;
  color: #475569;
  font-size: 12px;
  font-weight: 700;
}

@media (max-width: 720px) {
  .customer-summary-box {
    grid-template-columns: 1fr;
    margin-left: 16px;
    margin-right: 16px;
  }

  .history-header,
  .history-body {
    padding-left: 16px;
    padding-right: 16px;
  }

  .timeline-top {
    flex-direction: column;
    gap: 4px;
  }
}
</style>