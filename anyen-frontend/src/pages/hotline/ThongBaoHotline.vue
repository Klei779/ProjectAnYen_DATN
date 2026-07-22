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
                <i class="fa-solid fa-bell"></i>
              </div>
              <div class="card-details">
                <div class="card-title-row">
                  <h4>{{ item.tieuDe || 'Thông báo' }}</h4>
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

            <!-- Status Badge Right -->
            <div class="card-actions-wrapper">
              <span class="time-ago" v-if="item.trangThai === 0">Vừa xong</span>

              <div class="card-status-badge">
                <span class="status-pill" :class="getStatusClass(item.trangThai)">
                  {{ getStatusText(item.trangThai) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>

    </div>

    <!-- SIDEBAR: Chi tiết thông báo -->
    <div
        v-if="selectedNotification"
        class="custom-modal-overlay"
        @click.self="selectedNotification = null"
    >
      <div class="detail-sidebar" :class="{'slide-in': selectedNotification}">
        <div class="sidebar-header">
          <h3>{{ detailTitle(selectedNotification) }}</h3>
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
              <span class="label">Địa chỉ</span>
              <span class="value fw-bold">{{ selectedNotification.diaChi || '—' }}</span>
            </div>
            <div class="info-row">
              <span class="label">Ngày gửi</span>
              <span class="value fw-bold">{{ selectedNotification.ngayTao }}</span>
            </div>
            <div class="info-row">
              <span class="label">Trạng thái</span>
              <span class="value">
                <span v-if="selectedNotification.trangThai === 0" class="status-pill warning">Chưa đọc</span>
                <span v-else-if="selectedNotification.trangThai === 1" class="status-pill read">Đã đọc</span>
                <span v-else-if="selectedNotification.trangThai === 2" class="status-pill success">Đã tiếp nhận</span>
                <span v-else-if="selectedNotification.trangThai === 3" class="status-pill error">Đã từ chối</span>
              </span>
            </div>
            <div class="info-row">
              <span class="label">Nhân viên xử lý</span>
              <span class="value fw-bold">{{ selectedNotification.tenNguoiNhan || selectedNotification.tenNguoiGui || '—' }}</span>
            </div>
          </div>

          <div class="text-block">
            <h5>Nội dung</h5>
            <p>{{ selectedNotification.noiDung || '—' }}</p>
          </div>

          <div v-if="selectedNotification.lyDoTuChoi" class="text-block">
            <h5 class="text-danger">Lý do từ chối</h5>
            <p class="text-danger">{{ selectedNotification.lyDoTuChoi }}</p>
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

             <!-- Nút tạo hợp đồng cho thông báo yêu cầu tạo hợp đồng -->
             <div class="action-buttons mt-4" v-if="selectedNotification.tieuDe === 'Yêu cầu tạo hợp đồng'">
               <button class="btn btn-primary w-100" @click="openHopDongPopup(selectedNotification)">
                 <i class="fa-solid fa-file-contract"></i> Tạo hợp đồng
               </button>
             </div>
          </div>
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

    <!-- POPUP TẠO HỢP ĐỒNG -->
    <PopTaoHopDong 
      v-model="showHopDongPopup" 
      :initial-ma-don-hang="selectedMaDonHangForHopDong"
      @success="onHopDongSuccess"
    />

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import api from "../../api/api.js";
import PopTaoHopDong from "../nhanvien/PopTaoHopDong.vue";

const router = useRouter();

// Layout & State
const activeTab = ref("all");
const selectedNotification = ref(null);
const loading = ref(false);

// Toast
const toast = ref({ show: false, message: "", type: "success" });

// User Info
const userHoTen = ref("Hotline");

// Popup tạo hợp đồng
const showHopDongPopup = ref(false);
const selectedMaDonHangForHopDong = ref(null);

// Tabs matching the design
const TT_CHUA_DOC = 0;
const TT_DA_DOC = 1;
const TT_DA_CHAP_NHAN = 2;
const TT_DA_TU_CHOI = 3;

const detailTitle = (item) => {
  if (item?.loaiThongBao === "CONG_VIEC") return "Chi tiết công việc";
  return "Thông báo hệ thống";
};

const tabs = [
  { key: "all", label: "Tất cả" },
  { key: TT_CHUA_DOC, label: "Chưa đọc" },
  { key: TT_DA_CHAP_NHAN, label: "Đã tiếp nhận" },
  { key: TT_DA_TU_CHOI, label: "Đã từ chối" },
  { key: TT_DA_DOC, label: "Đã đọc" }
];

const notifications = ref([]);
const API_URL = "/api/nhan-vien/thong-bao/hotline";

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

const getCount = (key) => {
  let list = notifications.value;
  if (key === "all") return list.length;
  return list.filter(item => Number(item.trangThai) === Number(key)).length;
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
          };
        }

        return n;
      });

      // Cập nhật luôn popup đang mở
      selectedNotification.value = {
        ...item,
        trangThai: TT_DA_DOC,
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
        };
      }

      return n;
    });

    showToast("Đã đánh dấu tất cả là đã đọc");
  } catch (error) {
    console.error(error);
  }
};

const getStatusClass = (trangThai) => {
  switch (trangThai) {
    case 0: return 'warning';
    case 1: return 'read';
    case 2: return 'success';
    case 3: return 'error';
    default: return '';
  }
};

const getStatusText = (trangThai) => {
  switch (trangThai) {
    case 0: return 'Chưa đọc';
    case 1: return 'Đã đọc';
    case 2: return 'Đã tiếp nhận';
    case 3: return 'Đã từ chối';
    default: return '';
  }
};

const showToast = (message, type = "success") => {
  toast.value = { show: true, message, type };
  setTimeout(() => { toast.value.show = false; }, 3000);
};

// Parse maDonHang từ nội dung thông báo
const parseMaDonHangFromContent = (content) => {
  if (!content) return null;
  const match = content.match(/\[MA_DON_HANG:(\d+)\]/);
  return match ? parseInt(match[1]) : null;
};

// Mở popup tạo hợp đồng với đơn hàng đã chọn
const openHopDongPopup = (notification) => {
  const maDonHang = parseMaDonHangFromContent(notification.noiDung);
  if (maDonHang) {
    selectedMaDonHangForHopDong.value = maDonHang;
    showHopDongPopup.value = true;
  } else {
    showToast("Không tìm thấy mã đơn hàng trong thông báo", "error");
  }
};

// Xử lý khi tạo hợp đồng thành công
const onHopDongSuccess = () => {
  showHopDongPopup.value = false;
  selectedMaDonHangForHopDong.value = null;
  selectedNotification.value = null;
  showToast("Đã tạo hợp đồng thành công");
  loadNotifications();
};
</script>

<style scoped src="../../assets/styles/nhanvien/QLThongBao/TrangThongBaoNV.css"></style>
