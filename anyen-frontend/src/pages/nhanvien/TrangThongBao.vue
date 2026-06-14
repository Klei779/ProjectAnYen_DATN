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
          <img src="../../assets/images/icon/logoAnYen.png.png" alt="Empty" style="width: 100px; opacity: 0.5; margin-bottom: 20px;">
          <p>Không có công việc nào trong mục này</p>
        </div>

        <!-- Cards -->
        <div class="cards-container">
          <div
              v-for="item in filteredNotifications"
              :key="item.maThongBao"
              class="task-card"
          >
            <!-- Content Left -->
            <div class="card-main-content">
              <div class="card-icon">
                <i class="fa-solid fa-file-lines"></i>
              </div>
              <div class="card-details">
                <div class="card-title-row">
                  <h4>{{ item.tieuDe || 'Tư vấn trực tiếp khách hàng' }}</h4>
                  <!-- Thẻ Xem chi tiết nhỏ -->
                  <button class="view-detail-link" @click="selectNotification(item)">Xem chi tiết</button>
                </div>
                
                <p class="customer-info" v-if="item.loaiThongBao === 'CONG_VIEC' && item.tenKhachHang">
                  {{ item.tenKhachHang }} • {{ item.soDienThoai }}
                </p>
                <p class="customer-info text-muted" v-else>
                  {{ item.noiDung }}
                </p>
                <p class="address-info" v-if="item.diaChi">
                  <i class="fa-solid fa-location-dot"></i> {{ item.diaChi }}
                </p>
                <p class="time-info">
                  <i class="fa-regular fa-clock"></i> {{ item.ngayTao }}
                </p>
              </div>
            </div>

            <!-- Action Buttons Right (Inline) -->
            <div class="card-actions-wrapper">
              <span class="time-ago" v-if="item.trangThai === 'CHUA_DOC'">Vừa xong</span>
              
              <div class="card-buttons" v-if="item.loaiThongBao === 'CONG_VIEC' && (item.trangThai === 'CHUA_DOC' || item.trangThai === 'DA_DOC')">
                <button class="btn-outline" @click.stop="openRejectPopup(item)" :disabled="actionLoading">Từ chối</button>
                <button class="btn-primary" @click.stop="acceptCustomer(item)" :disabled="actionLoading">
                   <i v-if="actionLoading" class="fa-solid fa-spinner fa-spin"></i> Nhận công việc
                </button>
              </div>
              <div class="card-buttons processed" v-else-if="item.loaiThongBao === 'CONG_VIEC'">
                <span class="text-success fw-bold" v-if="item.trangThai === 'DA_CHAP_NHAN'"><i class="fa-solid fa-check"></i> Đã nhận</span>
                <span class="text-danger fw-bold" v-if="item.trangThai === 'DA_TU_CHOI'"><i class="fa-solid fa-xmark"></i> Đã từ chối</span>
              </div>
            </div>
          </div>
        </div>
      </section>

    </div>

    <!-- SIDEBAR: Chi tiết công việc -->
    <div
        v-if="selectedNotification"
        class="custom-modal-overlay"
        @click.self="selectedNotification = null"
    >
      <div class="detail-sidebar" :class="{'slide-in': selectedNotification}">
        <div class="sidebar-header">
          <h3>{{ selectedNotification.loaiThongBao === 'HE_THONG' ? 'Thông báo hệ thống' : 'Chi tiết khách hàng' }}</h3>
          <button class="close-btn" @click="selectedNotification = null">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>
        
        <!-- SIDEBAR BODY CHO CÔNG VIỆC -->
        <div class="sidebar-body" v-if="selectedNotification.loaiThongBao === 'CONG_VIEC'">
          <h4 class="mb-4 fw-bold">Thông tin khách hàng</h4>
            
          <div class="info-table-clean">
            <div class="info-row">
              <span class="label">Mã khách hàng</span>
              <span class="value fw-bold">#KH{{ selectedNotification.maKhachHang || '—' }}</span>
            </div>
            <div class="info-row">
              <span class="label">Họ và tên</span>
              <span class="value fw-bold">{{ selectedNotification.tenKhachHang || '—' }}</span>
            </div>
            <div class="info-row">
              <span class="label">Số điện thoại</span>
              <span class="value fw-bold">{{ selectedNotification.soDienThoai || '—' }}</span>
            </div>
            <div class="info-row">
              <span class="label">Email</span>
              <span class="value fw-bold">{{ selectedNotification.email || '—' }}</span>
            </div>
            <div class="info-row">
              <span class="label">Địa chỉ</span>
              <span class="value fw-bold">{{ selectedNotification.diaChi || '—' }}</span>
            </div>
            <div class="info-row">
              <span class="label">Ngày đăng ký</span>
              <span class="value fw-bold">{{ selectedNotification.ngayDangKy || selectedNotification.ngayTao }}</span>
            </div>
            <div class="info-row">
              <span class="label">Trạng thái</span>
              <span class="value">
                <span v-if="selectedNotification.trangThai === 'CHUA_DOC' || selectedNotification.trangThai === 'DA_DOC'" class="status-pill warning">Chờ tiếp nhận</span>
                <span v-else-if="selectedNotification.trangThai === 'DA_CHAP_NHAN'" class="status-pill success">Đã tiếp nhận</span>
                <span v-else-if="selectedNotification.trangThai === 'DA_TU_CHOI'" class="status-pill error">Đã từ chối</span>
              </span>
            </div>
            <div class="info-row">
              <span class="label">Nguồn đăng ký</span>
              <span class="value fw-bold">{{ selectedNotification.nguonDangKy || 'Website An Yên' }}</span>
            </div>
          </div>

          <div class="text-block">
            <h5>Nhu cầu hỗ trợ</h5>
            <p>{{ selectedNotification.nhuCauHoTro || selectedNotification.noiDung || '—' }}</p>
          </div>

          <div class="text-block">
            <h5>Ghi chú</h5>
            <p>{{ selectedNotification.ghiChu || 'Khách hàng mới đăng ký thông tin, cần nhân viên liên hệ lại' }}</p>
          </div>
          
          <div v-if="selectedNotification.lyDoTuChoi" class="text-block">
            <h5 class="text-danger">Lý do từ chối</h5>
            <p class="text-danger">{{ selectedNotification.lyDoTuChoi }}</p>
          </div>

          <!-- Buttons -->
          <div class="sidebar-actions" v-if="selectedNotification.trangThai === 'CHUA_DOC' || selectedNotification.trangThai === 'DA_DOC'">
            <button class="btn-outline-modal" @click="openRejectPopup(selectedNotification)">Từ chối</button>
            <button class="btn-primary-modal" @click="acceptCustomer(selectedNotification)">Tiếp nhận</button>
          </div>
          
          <p class="sidebar-note" v-if="selectedNotification.trangThai === 'CHUA_DOC' || selectedNotification.trangThai === 'DA_DOC'">
            <i class="fa-solid fa-lock"></i> Nếu bạn tiếp nhận khách hàng, hệ thống sẽ chuyển khách hàng sang danh sách quản lý khách hàng
          </p>
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
               <p>{{ selectedNotification.noiDung }}</p>
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
          <h3>Từ chối nhận việc</h3>
          <button class="close-btn" @click="closeRejectPopup">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <div class="modal-body">
          <p class="mb-3 text-muted">Vui lòng nhập lý do từ chối để chuyển lại cho bộ phận Hotline xử lý.</p>
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

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import api from "../../api/api.js";

// Layout & State
const activeTab = ref("all");
const selectedNotification = ref(null);
const loading = ref(false);
const actionLoading = ref(false);
const itemToReject = ref(null);

// Bell & Mini Noti Dropdown
const showMiniNoti = ref(false);
const toggleMiniNoti = () => {
  showMiniNoti.value = !showMiniNoti.value;
  console.log("Bell clicked! showMiniNoti =", showMiniNoti.value);
};

// Đóng dropdown khi click ra ngoài
onMounted(() => {
  document.addEventListener('click', () => {
    if (showMiniNoti.value) showMiniNoti.value = false;
  });
});

// Popup Từ Chối
const showRejectPopup = ref(false);
const rejectReason = ref("");
const rejectError = ref("");

// Toast
const toast = ref({ show: false, message: "", type: "success" });

// User Info
const userHoTen = ref("Nhân viên");

// Tabs matching the design
const tabs = [
  { key: "all", label: "Tất cả" },
  { key: "CHUA_DOC", label: "Chờ nhận" },
  { key: "DA_TU_CHOI", label: "Đã từ chối" },
  { key: "DA_CHAP_NHAN", label: "Đã nhận" }
];

const notifications = ref([]);
const API_URL = "/api/nhan-vien/thong-bao";

let pollingInterval = null;

// =================== POLLING & LOAD DATA ===================

const loadNotifications = async (isBackground = false) => {
  if (!isBackground) loading.value = true;
  try {
    const res = await api.get(API_URL);
    
    // Check if there are new unread notifications compared to old state
    const currentUnread = notifications.value.filter(n => n.trangThai === 'CHUA_DOC').length;
    const newUnread = res.data.filter(n => n.trangThai === 'CHUA_DOC').length;
    
    if (isBackground && newUnread > currentUnread) {
        showToast("Bạn có thông báo công việc mới!", "success");
    }

    notifications.value = res.data;
  } catch (error) {
    console.error("Lỗi load thông báo:", error);
  } finally {
    if (!isBackground) loading.value = false;
  }
};

const startPolling = () => {
  // Poll every 5 seconds for real-time feel
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
  // Get user name from localStorage
  try {
    const userData = JSON.parse(localStorage.getItem('user'));
    if (userData && userData.hoTen) {
      userHoTen.value = userData.hoTen;
    }
  } catch (e) {}

  loadNotifications();
  startPolling();
});

onUnmounted(() => {
  stopPolling();
});

// =================== COMPUTED ===================

const filteredNotifications = computed(() => {
  let list = notifications.value;
  
  if (activeTab.value === "all") {
    return list;
  }

  return list.filter(item => item.trangThai === activeTab.value);
});

// Mini list in right panel/dropdown shows ALL types, sorted by date (handled by backend)
const miniNotifications = computed(() => {
  return notifications.value.slice(0, 10); // Show max 10
});

const unreadCount = computed(() => {
  return notifications.value.filter(item => item.trangThai === 'CHUA_DOC').length;
});

const getCount = (key) => {
  let list = notifications.value;
  if (key === "all") return list.length;
  return list.filter(item => item.trangThai === key).length;
};

// =================== ICONS ===================

const getMiniIconClass = (item) => {
  if (item.loaiThongBao === "CONG_VIEC") return "bg-red";
  if (item.loaiThongBao === "HE_THONG") return "bg-blue";
  if (item.loaiThongBao === "TU_CHOI") return "bg-yellow";
  if (item.trangThai === "DA_CHAP_NHAN") return "bg-green";
  return "bg-purple";
};

const getMiniIconName = (item) => {
  if (item.loaiThongBao === "CONG_VIEC") return "fa-solid fa-briefcase";
  if (item.loaiThongBao === "HE_THONG") return "fa-solid fa-gear";
  if (item.loaiThongBao === "TU_CHOI") return "fa-solid fa-xmark";
  if (item.trangThai === "DA_CHAP_NHAN") return "fa-solid fa-check";
  return "fa-solid fa-bell";
};

// =================== ACTIONS ===================

const selectNotification = async (item) => {
  selectedNotification.value = item;
  
  if (item.trangThai === "CHUA_DOC") {
    try {
      await api.put(`${API_URL}/${item.maThongBao}/da-doc`);
      item.trangThai = "DA_DOC";
    } catch (e) {
      console.error(e);
    }
  }
};

const markAllAsRead = async () => {
  try {
    await api.put(`${API_URL}/da-doc-tat-ca`);
    notifications.value.forEach(n => {
      if(n.trangThai === 'CHUA_DOC') n.trangThai = 'DA_DOC';
    });
    showToast("Đã đánh dấu tất cả là đã đọc");
  } catch (error) {
    console.error(error);
  }
};

const acceptCustomer = async (item) => {
  actionLoading.value = true;
  try {
    await api.put(`${API_URL}/${item.maThongBao}/chap-nhan`);
    item.trangThai = "DA_CHAP_NHAN";
    showToast("Nhận công việc thành công!", "success");
    selectedNotification.value = null; // Close modal if open
  } catch (error) {
    showToast(error.response?.data?.message || "Lỗi khi nhận việc", "error");
  } finally {
    actionLoading.value = false;
  }
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
};

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    rejectError.value = "Vui lòng nhập lý do từ chối";
    return;
  }

  actionLoading.value = true;
  try {
    await api.put(
        `${API_URL}/${itemToReject.value.maThongBao}/tu-choi`,
        { lyDoTuChoi: rejectReason.value.trim() }
    );
    itemToReject.value.trangThai = "DA_TU_CHOI";
    itemToReject.value.lyDoTuChoi = rejectReason.value.trim();
    showToast("Đã từ chối công việc!", "success");
    closeRejectPopup();
    selectedNotification.value = null; // Close modal if open
  } catch (error) {
    showToast(error.response?.data?.message || "Lỗi khi từ chối", "error");
  } finally {
    actionLoading.value = false;
  }
};

const showToast = (message, type = "success") => {
  toast.value = { show: true, message, type };
  setTimeout(() => { toast.value.show = false; }, 3000);
};

// Global click to close mini dropdown
window.addEventListener('click', () => {
  if (showMiniNoti.value) showMiniNoti.value = false;
});
</script>

<style scoped src="../../assets/styles/TrangThongBaoNhanVienTrucTiep.css"></style>