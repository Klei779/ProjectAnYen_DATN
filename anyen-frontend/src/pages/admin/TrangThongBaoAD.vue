<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import api from "../../api/api.js";

const loading = ref(false);
const notifications = ref([]);
const activeTab = ref("all");
const actionLoading = ref(false);

const selectedNotification = ref(null);
const showSidebar = ref(false);

const showRejectPopup = ref(false);
const rejectReason = ref("");
const rejectError = ref("");
const itemToReject = ref(null);

const toast = ref({ show: false, message: "", type: "success" });

const userHoTen = ref("Admin");

const TT_CHUA_DOC = 0;
const TT_DA_DOC = 1;
const TT_DA_CHAP_NHAN = 2;
const TT_DA_TU_CHOI = 3;
const TT_CHO_XAC_NHAN = 4;

const canProcessNotification = (item) => {
  const status = Number(item?.trangThai);
  if (item?.loaiThongBao === "DUYET_SAN_PHAM") {
    return [TT_CHUA_DOC, TT_DA_DOC, TT_CHO_XAC_NHAN].includes(status);
  }
  return false;
};

const detailTitle = (item) => {
  if (!item) return "Thông báo";
  if (item.loaiThongBao === "DUYET_SAN_PHAM") return "Chi tiết duyệt sản phẩm";
  return "Thông báo hệ thống";
};

const tabs = [
  { key: "all", label: "Tất cả" },
  { key: TT_CHUA_DOC, label: "Chưa đọc" },
  { key: TT_DA_DOC, label: "Đã đọc" },
  { key: TT_CHO_XAC_NHAN, label: "Chờ duyệt sản phẩm" },
  { key: TT_DA_CHAP_NHAN, label: "Đã duyệt" },
  { key: TT_DA_TU_CHOI, label: "Đã từ chối" }
];

const API_URL = "/api/nhan-vien/thong-bao";

let pollingInterval = null;

const loadNotifications = async (isBackground = false) => {
  if (!isBackground) loading.value = true;

  try {
    const res = await api.get(API_URL);

    let data = res.data.map(item => ({
      ...item,
      trangThai: Number(item.trangThai)
    }));

    // Chỉ lấy thông báo liên quan đến admin: HE_THONG, DUYET_SAN_PHAM, DON_HANG
    // Loại bỏ thông báo lịch sử sản phẩm đã duyệt/từ chối (HE_THONG với tieuDe "Đã duyệt/từ chối sản phẩm")
    data = data.filter(item => {
      if (item.loaiThongBao === 'HE_THONG') {
        // Loại bỏ thông báo lịch sử sản phẩm đã xử lý
        const isProductHistory = item.tieuDe?.includes('Đã duyệt sản phẩm') || item.tieuDe?.includes('Đã từ chối sản phẩm');
        return !isProductHistory;
      }
      return item.loaiThongBao === 'DUYET_SAN_PHAM' || item.loaiThongBao === 'DON_HANG';
    });

    const currentUnread = notifications.value.filter(
        n => Number(n.trangThai) === TT_CHUA_DOC
    ).length;

    const newUnread = data.filter(
        n => Number(n.trangThai) === TT_CHUA_DOC
    ).length;

    if (isBackground && newUnread > currentUnread) {
      showToast("Bạn có thông báo mới!", "success");
    }

    notifications.value = data;
  } catch (error) {
    console.error("Lỗi load thông báo:", error);
  } finally {
    if (!isBackground) loading.value = false;
  }
};

const startPolling = () => {
  pollingInterval = setInterval(() => {
    loadNotifications(true);
  }, 5000);
};

const stopPolling = () => {
  if (pollingInterval) {
    clearInterval(pollingInterval);
  }
};

onMounted(() => {
  try {
    const userData = JSON.parse(localStorage.getItem('user'));
    if (userData && userData.hoTen) {
      userHoTen.value = userData.hoTen;
    }
  } catch (e) {}

  loadNotifications();
  startPolling();
});

onBeforeUnmount(() => {
  stopPolling();
});

const filteredNotifications = computed(() => {
  if (activeTab.value === "all") {
    return notifications.value;
  }
  return notifications.value.filter(n => Number(n.trangThai) === activeTab.value);
});

const getCount = (tabKey) => {
  if (tabKey === "all") return notifications.value.length;
  return notifications.value.filter(n => Number(n.trangThai) === tabKey).length;
};

const selectNotification = async (item) => {
  selectedNotification.value = item;

  if (Number(item.trangThai) === TT_CHUA_DOC) {
    try {
      await api.put(`${API_URL}/${item.maThongBao}/da-doc`);
      item.trangThai = TT_DA_DOC;
    } catch (error) {
      console.error("Lỗi đánh dấu đã đọc:", error);
    }
  }

  showSidebar.value = true;
};

const closeSidebar = () => {
  showSidebar.value = false;
  selectedNotification.value = null;
};

const openRejectPopup = (item) => {
  itemToReject.value = item;
  rejectReason.value = "";
  rejectError.value = "";
  showRejectPopup.value = true;
};

const closeRejectPopup = () => {
  showRejectPopup.value = false;
  itemToReject.value = null;
  rejectReason.value = "";
  rejectError.value = "";
};

const confirmReject = async () => {
  if (!rejectReason.value || rejectReason.value.trim().length < 3) {
    rejectError.value = "Lý do phải từ 3 ký tự trở lên";
    return;
  }

  actionLoading.value = true;
  rejectError.value = "";

  try {
    await api.put(`${API_URL}/${itemToReject.value.maThongBao}/tu-choi`, {
      lyDoTuChoi: rejectReason.value.trim()
    });

    itemToReject.value.trangThai = TT_DA_TU_CHOI;
    itemToReject.value.lyDoTuChoi = rejectReason.value.trim();

    if (itemToReject.value.loaiThongBao === "DUYET_SAN_PHAM") {
      showToast("Đã từ chối sản phẩm và chuyển sản phẩm sang trạng thái ẩn.", "success");
    }

    closeRejectPopup();
    closeSidebar();
    await loadNotifications();
  } catch (error) {
    console.error("Lỗi từ chối:", error);
    rejectError.value = error.response?.data?.message || "Không thể từ chối. Vui lòng thử lại!";
  } finally {
    actionLoading.value = false;
  }
};

const acceptCustomer = async (item) => {
  actionLoading.value = true;

  try {
    await api.put(`${API_URL}/${item.maThongBao}/chap-nhan`);

    item.trangThai = TT_DA_CHAP_NHAN;

    if (item.loaiThongBao === "DUYET_SAN_PHAM") {
      showToast("Đã duyệt sản phẩm. Sản phẩm đã được bày bán!", "success");
    }

    closeSidebar();
    await loadNotifications();
  } catch (error) {
    console.error("Lỗi chấp nhận:", error);
    showToast(error.response?.data?.message || "Không thể chấp nhận. Vui lòng thử lại!", "error");
  } finally {
    actionLoading.value = false;
  }
};

const showToast = (message, type = "success") => {
  toast.value = { show: true, message, type };
  setTimeout(() => {
    toast.value.show = false;
  }, 3000);
};

const markAllAsRead = async () => {
  try {
    await api.put(`${API_URL}/da-doc-tat-ca`);
    notifications.value.forEach(n => {
      if (Number(n.trangThai) === TT_CHUA_DOC) {
        n.trangThai = TT_DA_DOC;
      }
    });
    showToast("Đã đánh dấu tất cả là đã đọc!");
  } catch (error) {
    console.error("Lỗi đánh dấu đã đọc tất cả:", error);
  }
};
</script>

<template>
  <div class="notification-page">
    <div class="content-wrapper">
      <!-- Main Content (Left) -->
      <section class="main-column">

        <div class="custom-tabs">
          <button
              v-for="tab in tabs"
              :key="tab.key"
              :class="{ active: activeTab === tab.key }"
              @click="activeTab = tab.key"
          >
            {{ tab.label }}
            <span class="tab-badge" v-if="getCount(tab.key) > 0">{{ getCount(tab.key) }}</span>
          </button>
        </div>

        <!-- Loading & Empty States -->
        <div v-if="loading && filteredNotifications.length === 0" class="loading-state">
          <i class="fa-solid fa-spinner fa-spin"></i>
          <p>Đang tải dữ liệu...</p>
        </div>

        <div v-else-if="filteredNotifications.length === 0" class="empty-state">
          <img src="../../assets/images/icon/logoAnYen.png" alt="Empty" style="width: 100px; opacity: 0.5; margin-bottom: 20px;">
          <p>Không có thông báo nào trong mục này</p>
        </div>

        <!-- Cards -->
        <div class="cards-container">
          <div
              v-for="item in filteredNotifications"
              :key="item.maThongBao"
              class="task-card clickable-card"
              @click="selectNotification(item)"
          >
            <!-- Content Left -->
            <div class="card-main-content">
              <div class="card-icon">
                <i class="fa-solid fa-bell" v-if="item.loaiThongBao === 'HE_THONG'"></i>
                <i class="fa-solid fa-box-open" v-else-if="item.loaiThongBao === 'DUYET_SAN_PHAM'"></i>
                <i class="fa-solid fa-file-contract" v-else-if="item.loaiThongBao === 'DON_HANG'"></i>
                <i class="fa-solid fa-file-lines" v-else></i>
              </div>
              <div class="card-details">
                <div class="card-title-row">
                  <h4>{{ item.tieuDe || 'Thông báo' }}</h4>
                </div>
                
                <p class="customer-info text-muted">
                  {{ item.noiDung }}
                </p>
                <p class="time-info">
                  <i class="fa-regular fa-clock"></i> {{ item.ngayTao }}
                </p>
              </div>
            </div>

            <!-- Action Buttons Right (Inline) -->
            <div class="card-actions-wrapper">
              <span class="time-ago" v-if="item.trangThai === 0">Vừa xong</span>

              <div
                  class="card-buttons"
                  v-if="item.loaiThongBao === 'DUYET_SAN_PHAM' && canProcessNotification(item)"
              >
                <button class="btn-outline" @click.stop="openRejectPopup(item)" :disabled="actionLoading || item.trangThai === 2 || item.trangThai === 3">
                  Từ chối
                </button>
                <button class="btn-primary" @click.stop="acceptCustomer(item)" :disabled="actionLoading || item.trangThai === 2 || item.trangThai === 3">
                  <i v-if="actionLoading" class="fa-solid fa-spinner fa-spin"></i>
                  Duyệt
                </button>
              </div>
              <div
                  class="card-buttons processed"
                  v-else-if="item.loaiThongBao === 'DUYET_SAN_PHAM'"
              >
                <span class="text-success fw-bold" v-if="item.trangThai === 2"><i class="fa-solid fa-check"></i> Đã duyệt</span>
                <span class="text-danger fw-bold" v-if="item.trangThai === 3"><i class="fa-solid fa-xmark"></i> Đã từ chối</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- DETAIL SIDEBAR -->
    <div class="detail-sidebar" :class="{ 'slide-in': showSidebar }">
      <div class="sidebar-header">
        <h3>{{ detailTitle(selectedNotification) }}</h3>
        <button class="close-btn" @click="closeSidebar">
          <i class="fa-solid fa-xmark"></i>
        </button>
      </div>

      <div class="sidebar-body" v-if="selectedNotification">
        <!-- SIDEBAR BODY CHO DUYỆT SẢN PHẨM -->
        <div class="sidebar-body" v-if="selectedNotification.loaiThongBao === 'DUYET_SAN_PHAM'">
          <div class="system-noti-wrapper">
            <div class="system-icon-large"><i class="fa-solid fa-box-open text-primary"></i></div>
            <h4 class="text-center mt-3">{{ selectedNotification.tieuDe }}</h4>
            <p class="text-center text-muted">Mã sản phẩm: #SP{{ selectedNotification.maSanPham || '—' }}</p>
            <div class="system-content-box mt-4"><p>{{ selectedNotification.noiDung }}</p></div>
            <div class="info-table-clean mt-4">
              <div class="info-row">
                <span class="label">Trạng thái</span>
                <span class="value">
                  <span v-if="canProcessNotification(selectedNotification)" class="status-pill warning">Chờ duyệt</span>
                  <span v-else-if="selectedNotification.trangThai === 2" class="status-pill success">Đã duyệt</span>
                  <span v-else-if="selectedNotification.trangThai === 3" class="status-pill error">Đã từ chối</span>
                </span>
              </div>
              <div v-if="selectedNotification.lyDoTuChoi" class="info-row">
                <span class="label">Lý do từ chối</span>
                <span class="value text-danger">{{ selectedNotification.lyDoTuChoi }}</span>
              </div>
            </div>
            <div class="sidebar-actions" v-if="canProcessNotification(selectedNotification)">
              <button class="btn-outline-modal" @click="openRejectPopup(selectedNotification)" :disabled="actionLoading">Từ chối</button>
              <button class="btn-primary-modal" @click="acceptCustomer(selectedNotification)" :disabled="actionLoading">Duyệt sản phẩm</button>
            </div>
          </div>
        </div>

        <!-- SIDEBAR BODY CHO HỆ THỐNG -->
        <div class="sidebar-body" v-else>
          <div class="system-noti-wrapper">
             <div class="system-icon-large">
               <i class="fa-solid fa-bullhorn text-primary"></i>
             </div>
             <h4 class="text-center mt-3">{{ selectedNotification.tieuDe }}</h4>
             <p class="text-center text-muted"><i class="fa-regular fa-clock"></i> Thời gian: {{ selectedNotification.ngayTao }}</p>
             
             <div class="system-content-box mt-4">
               <p>{{ selectedNotification.noiDung.replace(/\[MA_DON_HANG:\d+\]/, '') }}</p>
             </div>
          </div>
        </div>
      </div>
    </div>

    <!-- POPUP TỪ CHỐI -->
    <div
        v-if="showRejectPopup"
        class="custom-modal-overlay"
        @click.self="closeRejectPopup"
    >
      <div class="custom-modal modal-small">
        <div class="modal-header">
          <h3>Từ chối sản phẩm</h3>
          <button class="close-btn" @click="closeRejectPopup">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <div class="modal-body">
          <p class="mb-3 text-muted">Vui lòng nhập lý do để phản hồi cho đối tác.</p>
          <div class="form-group">
            <label>Lý do từ chối <span class="text-danger">*</span></label>
            <textarea
                v-model="rejectReason"
                placeholder="Nhập lý do chi tiết..."
                rows="4"
                class="form-control"
            ></textarea>
            <small v-if="rejectError" class="text-danger mt-1 d-block"><i class="fa-solid fa-circle-exclamation"></i> {{ rejectError }}</small>
          </div>
        </div>

        <div class="modal-footer right-align">
          <button class="btn-outline-modal" @click="closeRejectPopup" :disabled="actionLoading">Hủy</button>
          <button class="btn-danger-modal" @click="confirmReject" :disabled="actionLoading">
            <i v-if="actionLoading" class="fa-solid fa-spinner fa-spin"></i> Xác nhận
          </button>
        </div>
      </div>
    </div>

    <!-- TOAST -->
    <Transition name="toast">
      <div v-if="toast.show" class="toast-notification" :class="toast.type">
        <i :class="toast.type === 'success' ? 'fa-solid fa-circle-check' : 'fa-solid fa-circle-xmark'"></i>
        <span>{{ toast.message }}</span>
      </div>
    </Transition>
  </div>
</template>

<style src="../../assets/styles/admin/QLThongBao/TrangThongBaoAD.css"></style>
<style scoped>
.text-primary {
  color: #dc2626;
}

/* Popup từ chối sản phẩm */
.custom-modal.modal-small {
  width: min(525px, calc(100vw - 32px));
  border-radius: 18px;
  overflow: hidden;
  background: #ffffff;
}

/* Phần tiêu đề */
.custom-modal .modal-header {
  padding: 20px 24px 8px;
  border-bottom: none;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.custom-modal .modal-header h3 {
  margin: 0;
  font-size: 22px;
  line-height: 1.35;
}

/* Phần nội dung */
.custom-modal .modal-body {
  padding: 0 24px 22px;
}

.custom-modal .modal-body > p {
  margin: 0 0 18px;
  line-height: 1.5;
}

.custom-modal .form-group {
  width: 100%;
}

.custom-modal .form-group label {
  display: block;
  margin-bottom: 10px;
  font-weight: 600;
  line-height: 1.4;
}

/* Tạo khoảng cách giữa chữ và viền textarea */
.custom-modal .form-control {
  display: block;
  width: 100%;
  min-height: 130px;
  padding: 14px 16px !important;
  border: 1px solid #d1d5db;
  border-radius: 12px;
  box-sizing: border-box;
  line-height: 1.5;
  resize: vertical;
  outline: none;
}

.custom-modal .form-control:focus {
  border-color: #dc2626;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
}

.custom-modal .form-control::placeholder {
  color: #9ca3af;
}

/* Thông báo lỗi */
.custom-modal .form-group small {
  display: block;
  margin-top: 8px;
  line-height: 1.4;
}

/* Phần nút */
.custom-modal .modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px 20px;
  border-top: 1px solid #eeeeee;
  background: #ffffff;
}

.custom-modal .modal-footer button {
  min-width: 120px;
  min-height: 46px;
  border-radius: 10px;
}
</style>