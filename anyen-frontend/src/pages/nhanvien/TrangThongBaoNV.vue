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
          <p>Không có công việc nào trong mục này</p>
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
                <i class="fa-solid fa-file-lines"></i>
              </div>
              <div class="card-details">
                <div class="card-title-row">
                  <h4>{{ item.tieuDe || 'Tư vấn trực tiếp khách hàng' }}</h4>
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
              <span class="time-ago" v-if="item.trangThai === 0">Vừa xong</span>

              <div
                  class="card-buttons"
                  v-if="item.loaiThongBao === 'CONG_VIEC' && (item.trangThai === 0 || item.trangThai === 1)"
              >
                <button class="btn-outline" @click.stop="openRejectPopup(item)" :disabled="actionLoading">
                  Từ chối
                </button>
                <button class="btn-primary" @click.stop="acceptCustomer(item)" :disabled="actionLoading">
                  <i v-if="actionLoading" class="fa-solid fa-spinner fa-spin"></i>
                  Nhận công việc
                </button>
              </div>

              <div
                  class="card-buttons"
                  v-else-if="item.loaiThongBao === 'DUYET_SAN_PHAM' && item.trangThai === 4"
              >
                <button class="btn-outline" @click.stop="openRejectPopup(item)" :disabled="actionLoading">
                  Từ chối
                </button>
                <button class="btn-primary" @click.stop="acceptCustomer(item)" :disabled="actionLoading">
                  <i v-if="actionLoading" class="fa-solid fa-spinner fa-spin"></i>
                  Đồng ý
                </button>
              </div>
              <div
                  class="card-buttons processed"
                  v-else-if="item.loaiThongBao === 'CONG_VIEC' || item.loaiThongBao === 'DUYET_SAN_PHAM'"
              >
                <span class="text-success fw-bold" v-if="item.trangThai === 2"><i class="fa-solid fa-check"></i> Đã nhận</span>
                <span class="text-danger fw-bold" v-if="item.trangThai === 3"><i class="fa-solid fa-xmark"></i> Đã từ chối</span>
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
                <span v-if="selectedNotification.trangThai === 0 || selectedNotification.trangThai === 1" class="status-pill warning">Chờ tiếp nhận</span>
                <span v-else-if="selectedNotification.trangThai === 2" class="status-pill success">Đã tiếp nhận</span>
                <span v-else-if="selectedNotification.trangThai === 3" class="status-pill error">Đã từ chối</span>
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
          <div class="sidebar-actions" v-if="selectedNotification.trangThai === 0 || selectedNotification.trangThai === 1">
            <button class="btn-outline-modal" @click="openRejectPopup(selectedNotification)">Từ chối</button>
            <button class="btn-primary-modal" @click="acceptCustomer(selectedNotification)">Tiếp nhận</button>
          </div>
          
          <p class="sidebar-note" v-if="selectedNotification.trangThai === 0 || selectedNotification.trangThai === 1">
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
const TT_CHUA_DOC = 0;
const TT_DA_DOC = 1;
const TT_DA_CHAP_NHAN = 2;
const TT_DA_TU_CHOI = 3;
const TT_CHO_XAC_NHAN = 4;

const tabs = [
  { key: "all", label: "Tất cả" },
  { key: TT_CHUA_DOC, label: "Chờ nhận việc" },
  { key: TT_CHO_XAC_NHAN, label: "Chờ duyệt sản phẩm" },
  { key: TT_DA_TU_CHOI, label: "Đã từ chối" },
  { key: TT_DA_CHAP_NHAN, label: "Đã nhận / Đã duyệt" }
];

const notifications = ref([]);
const API_URL = "/api/nhan-vien/thong-bao";

let pollingInterval = null;

// =================== POLLING & LOAD DATA ===================

const loadNotifications = async (isBackground = false) => {
  if (!isBackground) loading.value = true;

  try {
    const res = await api.get(API_URL);

    const data = res.data.map(item => ({
      ...item,
      trangThai: Number(item.trangThai)
    }));

    const currentUnread = notifications.value.filter(
        n => Number(n.trangThai) === TT_CHUA_DOC
    ).length;

    const newUnread = data.filter(
        n => Number(n.trangThai) === TT_CHUA_DOC
    ).length;

    if (isBackground && newUnread > currentUnread) {
      showToast("Bạn có thông báo công việc mới!", "success");
    }

    notifications.value = data;
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

  return list.filter(item => Number(item.trangThai) === Number(activeTab.value));
});

// Mini list in right panel/dropdown shows ALL types, sorted by date (handled by backend)
const miniNotifications = computed(() => {
  return notifications.value.slice(0, 10); // Show max 10
});

const unreadCount = computed(() => {
  return notifications.value.filter(
      item => Number(item.trangThai) === TT_CHUA_DOC
  ).length;
});

const getCount = (key) => {
  let list = notifications.value;
  if (key === "all") return list.length;
  return list.filter(item => Number(item.trangThai) === Number(key)).length;
};

// =================== ICONS ===================

const getMiniIconClass = (item) => {
  if (item.loaiThongBao === "CONG_VIEC") return "bg-red";
  if (item.loaiThongBao === "HE_THONG") return "bg-blue";
  if (item.loaiThongBao === "TU_CHOI") return "bg-yellow";
  if (item.trangThai === TT_DA_CHAP_NHAN) return "bg-green";
  return "bg-purple";
};

const getMiniIconName = (item) => {
  if (item.loaiThongBao === "CONG_VIEC") return "fa-solid fa-briefcase";
  if (item.loaiThongBao === "HE_THONG") return "fa-solid fa-gear";
  if (item.loaiThongBao === "TU_CHOI") return "fa-solid fa-xmark";
  if (item.trangThai === TT_DA_CHAP_NHAN) return "fa-solid fa-check";
  return "fa-solid fa-bell";
};

// =================== ACTIONS ===================

const selectNotification = async (item) => {
  selectedNotification.value = item;

  if (Number(item.trangThai) === TT_CHUA_DOC) {
    try {
      await api.put(`${API_URL}/${item.maThongBao}/da-doc`);

      // Cập nhật local ngay để UI đổi liền
      notifications.value = notifications.value.map(n => {
        if (n.maThongBao === item.maThongBao) {
          return {
            ...n,
            trangThai: TT_DA_DOC,
            isNew: false,
          };
        }

        return n;
      });

      // Cập nhật luôn popup đang mở
      selectedNotification.value = {
        ...item,
        trangThai: TT_DA_DOC,
        isNew: false,
      };

    } catch (e) {
      console.error("Lỗi đánh dấu đã đọc:", e);
    }
  }
};

const markAllAsRead = async () => {
  try {
    await api.put(`${API_URL}/da-doc-tat-ca`);

    notifications.value = notifications.value.map(n => {
      if (Number(n.trangThai) === TT_CHUA_DOC) {
        return {
          ...n,
          trangThai: TT_DA_DOC,
          isNew: false,
        };
      }

      return n;
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

    item.trangThai = TT_DA_CHAP_NHAN;

    if (item.loaiThongBao === "DUYET_SAN_PHAM") {
      showToast("Đã duyệt sản phẩm. Sản phẩm đã được bày bán!", "success");
    } else {
      showToast("Nhận công việc thành công!", "success");
    }

    selectedNotification.value = null;
  } catch (error) {
    showToast(error.response?.data?.message || "Lỗi khi xử lý thông báo", "error");
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

  if (rejectReason.value.trim().length < 3) {
    rejectError.value = "Lý do từ chối phải từ 3 ký tự trở lên";
    return;
  }

  actionLoading.value = true;

  try {
    await api.put(
        `${API_URL}/${itemToReject.value.maThongBao}/tu-choi`,
        { lyDoTuChoi: rejectReason.value.trim() }
    );

    itemToReject.value.trangThai = TT_DA_TU_CHOI;
    itemToReject.value.lyDoTuChoi = rejectReason.value.trim();

    if (itemToReject.value.loaiThongBao === "DUYET_SAN_PHAM") {
      showToast("Đã từ chối và xóa sản phẩm khỏi database!", "success");
    } else {
      showToast("Đã từ chối công việc!", "success");
    }

    closeRejectPopup();
    selectedNotification.value = null;
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

<style scoped src="../../assets/styles/nhanvien/QLThongBao/TrangThongBaoNV.css"></style>