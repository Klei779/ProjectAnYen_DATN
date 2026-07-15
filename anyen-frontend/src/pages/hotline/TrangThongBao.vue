<template>
  <div class="hotline-page">
    <main class="hotline-main">
      <section class="page-content">
        <div class="content-layout">
          <section class="notification-panel">
            <div class="panel-card notification-card">
              <div class="notification-header">
                <h2>Thông báo</h2>
                <button
                    class="mark-read-button"
                    @click="markAllAsRead"
                    v-if="notifications.length > 0"
                >
                  <i class="fa-solid fa-check-double"></i>
                  Đánh dấu đã đọc
                </button>
              </div>

              <div v-if="loading" class="notification-loading">
                <span class="spinner-border spinner-border-sm"></span>
                Đang tải thông báo...
              </div>

              <div v-else-if="notifications.length === 0" class="empty-notifications">
                <i class="fa-solid fa-bell-slash"></i>
                <p>Không có thông báo nào</p>
              </div>

              <div v-else class="notification-list">
                <div
                    v-for="tb in notifications"
                    :key="tb.maThongBao"
                    class="notification-item"
                    :class="{
                      unread: tb.trangThai === 0,
                      accepted: tb.trangThai === 2,
                      rejected: tb.trangThai === 3
                    }"
                >
                  <div class="notification-icon">
                    <i
                        class="fa-solid"
                        :class="getNotificationIcon(tb.loaiThongBao, tb.trangThai)"
                    ></i>
                  </div>

                  <div class="notification-content">
                    <div class="notification-title">
                      {{ tb.tieuDe }}
                    </div>

                    <div class="notification-body">
                      {{ tb.noiDung }}
                    </div>

                    <div v-if="tb.lyDoTuChoi" class="notification-reason">
                      <strong>Lý do từ chối:</strong> {{ tb.lyDoTuChoi }}
                    </div>

                    <div class="notification-meta">
                      <span class="notification-time">{{ tb.ngayTao }}</span>
                      <span v-if="tb.tenNguoiGui" class="notification-sender">
                        Từ: {{ tb.tenNguoiGui }}
                      </span>
                    </div>
                  </div>

                  <div class="notification-status">
                    <span
                        class="status-badge"
                        :class="getStatusClass(tb.trangThai)"
                    >
                      {{ getStatusText(tb.trangThai) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const notifications = ref([]);
const loading = ref(false);

const fetchNotifications = async () => {
  loading.value = true;
  try {
    const token = localStorage.getItem('token');
    const response = await axios.get('/api/nhan-vien/thong-bao/hotline', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    notifications.value = response.data;
  } catch (error) {
    console.error('Lỗi khi tải thông báo:', error);
    notifications.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchNotifications();
});

const markAllAsRead = async () => {
  try {
    const token = localStorage.getItem('token');
    await axios.put('/api/nhan-vien/thong-bao/hotline/da-doc-tat-ca', {}, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    await fetchNotifications();
  } catch (error) {
    console.error('Lỗi khi đánh dấu đã đọc:', error);
  }
};

const getNotificationIcon = (loaiThongBao, trangThai) => {
  if (loaiThongBao === 'TU_CHOI') {
    return 'fa-circle-xmark';
  }
  if (trangThai === 2) {
    return 'fa-circle-check';
  }
  return 'fa-bell';
};

const getStatusClass = (trangThai) => {
  switch (trangThai) {
    case 0: return 'unread';
    case 1: return 'read';
    case 2: return 'accepted';
    case 3: return 'rejected';
    default: return '';
  }
};

const getStatusText = (trangThai) => {
  switch (trangThai) {
    case 0: return 'Chưa đọc';
    case 1: return 'Đã đọc';
    case 2: return 'Đã chấp nhận';
    case 3: return 'Đã từ chối';
    default: return '';
  }
};

onMounted(() => {
  fetchNotifications();
});
</script>

<style scoped>
.hotline-page {
  --primary-red: #e60012;
  --soft-red: #fff0f1;
  --dark-text: #151b2b;
  --muted-text: #687083;
  --border-color: #e8eaf0;
  --page-background: #fafbfc;

  display: flex;
  width: 100%;
  min-height: 100vh;
  background: #fafbfc;
  color: #151b2b;
  font-family: Inter, "Segoe UI", Arial, sans-serif;
}

.hotline-main {
  width: 100%;
  min-height: 100vh;
  margin-left: 0;
}

.page-content {
  padding: 20px 22px 25px;
}

.content-layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 22px;
  max-width: 1200px;
  margin: 0 auto;
}

.panel-card {
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: 11px;
  box-shadow: 0 2px 12px rgba(24, 32, 50, 0.025);
}

.notification-card {
  padding: 24px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.notification-header h2 {
  margin: 0;
  color: #171717;
  font-size: 19px;
  font-weight: 700;
}

.mark-read-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  color: var(--primary-red);
  font-size: 12px;
  font-weight: 600;
  background: #ffffff;
  border: 1px solid var(--primary-red);
  border-radius: 6px;
  cursor: pointer;
  transition: 0.18s ease;
}

.mark-read-button:hover {
  color: #ffffff;
  background: var(--primary-red);
}

.notification-loading,
.empty-notifications {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  min-height: 200px;
  color: #7d8594;
  font-size: 13px;
}

.empty-notifications i {
  font-size: 48px;
  opacity: 0.3;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e0e3e8;
  border-radius: 8px;
  transition: 0.18s ease;
}

.notification-item.unread {
  background: #fff8f8;
  border-color: #ffe0e2;
}

.notification-item.accepted {
  background: #f0fff4;
  border-color: #c6f6d5;
}

.notification-item.rejected {
  background: #fff5f5;
  border-color: #fed7d7;
}

.notification-icon {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  color: var(--primary-red);
  font-size: 18px;
  background: var(--soft-red);
  border-radius: 50%;
}

.notification-item.accepted .notification-icon {
  color: #12af48;
  background: #f0fff4;
}

.notification-item.rejected .notification-icon {
  color: #e53e3e;
  background: #fff5f5;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  color: #242b3c;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 6px;
}

.notification-body {
  color: #5a6375;
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 8px;
  white-space: pre-line;
}

.notification-reason {
  color: #e53e3e;
  font-size: 12px;
  margin-bottom: 8px;
  padding: 8px 12px;
  background: #fff5f5;
  border-radius: 6px;
}

.notification-meta {
  display: flex;
  gap: 12px;
  color: #9096a3;
  font-size: 11px;
}

.notification-status {
  display: flex;
  align-items: flex-start;
}

.status-badge {
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 600;
  border-radius: 12px;
}

.status-badge.unread {
  color: var(--primary-red);
  background: var(--soft-red);
}

.status-badge.read {
  color: #7d8594;
  background: #f5f5f5;
}

.status-badge.accepted {
  color: #12af48;
  background: #f0fff4;
}

.status-badge.rejected {
  color: #e53e3e;
  background: #fff5f5;
}
</style>
