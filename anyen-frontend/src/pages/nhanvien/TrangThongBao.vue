<template>
  <div class="notification-page">
    <section class="notification-left">
      <div class="page-title">
        <h2>Thông báo</h2>
        <p>Cập nhật các thông báo mới nhất từ hệ thống</p>
      </div>

      <div class="tabs">
        <button
            v-for="tab in tabs"
            :key="tab.key"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
        >
          {{ tab.label }} ({{ getCount(tab.key) }})
        </button>
      </div>

      <!-- Loading state -->
      <div v-if="loading" class="loading-state">
        <i class="fa-solid fa-spinner fa-spin"></i>
        <p>Đang tải thông báo...</p>
      </div>

      <!-- Empty state -->
      <div v-else-if="filteredNotifications.length === 0" class="empty-state">
        <i class="fa-regular fa-bell-slash"></i>
        <p>Không có thông báo nào</p>
      </div>

      <!-- Notification list -->
      <div v-else class="notification-list">
        <div
            v-for="item in filteredNotifications"
            :key="item.maThongBao"
            class="notification-card"
            :class="{
              active: selectedNotification?.maThongBao === item.maThongBao,
              'is-read': item.trangThai !== 'CHUA_DOC'
            }"
            @click="selectNotification(item)"
        >
          <div class="noti-icon" :class="getIconClass(item.loaiThongBao)">
            <i :class="getIconName(item.loaiThongBao)"></i>
          </div>

          <div class="noti-content">
            <div class="noti-head">
              <h4>{{ item.tieuDe }}</h4>
              <span v-if="item.trangThai === 'CHUA_DOC'" class="badge-new">Mới</span>
              <span v-if="item.trangThai === 'DA_CHAP_NHAN'" class="badge-accepted">
                <i class="fa-solid fa-check"></i> Đã chấp nhận
              </span>
              <span v-if="item.trangThai === 'DA_TU_CHOI'" class="badge-rejected">
                <i class="fa-solid fa-xmark"></i> Đã từ chối
              </span>
            </div>

            <p>{{ item.noiDung }}</p>
            <small>{{ item.ngayTao }}</small>
          </div>

          <div class="noti-action">
            <p v-if="item.loaiThongBao === 'CONG_VIEC'">Công việc mới</p>
            <p v-else-if="item.loaiThongBao === 'HE_THONG'">Thông báo hệ thống</p>
            <p v-else-if="item.loaiThongBao === 'TU_CHOI'">Phản hồi từ chối</p>
            <i
                v-if="item.loaiThongBao === 'CONG_VIEC' && item.trangThai === 'CHUA_DOC'"
                class="fa-solid fa-chevron-right"
            ></i>
          </div>
        </div>
      </div>

      <div class="pagination-row">
        <p>
          Hiển thị 1 - {{ filteredNotifications.length }}
          trong {{ filteredNotifications.length }} thông báo
        </p>

        <div class="pagination">
          <button><i class="fa-solid fa-chevron-left"></i></button>
          <button class="active">1</button>
          <button><i class="fa-solid fa-chevron-right"></i></button>
        </div>
      </div>
    </section>

    <!-- POPUP CHI TIẾT KHÁCH HÀNG (chỉ cho CONG_VIEC) -->
    <div
        v-if="selectedNotification && selectedNotification.loaiThongBao === 'CONG_VIEC'"
        class="detail-popup-overlay"
        @click.self="selectedNotification = null"
    >
      <aside class="order-detail">
        <div class="detail-header">
          <h3>Chi tiết khách hàng</h3>

          <button @click="selectedNotification = null">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <div class="detail-section">
          <h5>Thông tin khách hàng</h5>

          <div class="info-row">
            <span>Họ và tên</span>
            <b>{{ selectedNotification.tenKhachHang || '—' }}</b>
          </div>

          <div class="info-row">
            <span>Số điện thoại</span>
            <b>{{ selectedNotification.soDienThoai || '—' }}</b>
          </div>

          <div class="info-row">
            <span>Email</span>
            <b>{{ selectedNotification.email || '—' }}</b>
          </div>

          <div class="info-row">
            <span>Địa chỉ</span>
            <b>{{ selectedNotification.diaChi || '—' }}</b>
          </div>

          <div class="info-row">
            <span>CCCD</span>
            <b>{{ selectedNotification.cccd || '—' }}</b>
          </div>

          <div class="info-row">
            <span>Trạng thái</span>
            <em :class="getStatusBadgeClass(selectedNotification.trangThai)">
              {{ getStatusLabel(selectedNotification.trangThai) }}
            </em>
          </div>
        </div>

        <div class="detail-section" v-if="selectedNotification.tenNguoiGui">
          <h6>Người gửi thông báo</h6>
          <p class="note">{{ selectedNotification.tenNguoiGui }}</p>
        </div>

        <div class="detail-section">
          <h6>Nội dung thông báo</h6>
          <p class="note">{{ selectedNotification.noiDung }}</p>
        </div>

        <!-- Lý do từ chối (nếu đã từ chối) -->
        <div class="detail-section" v-if="selectedNotification.lyDoTuChoi">
          <h6>Lý do từ chối</h6>
          <p class="note reject-reason">{{ selectedNotification.lyDoTuChoi }}</p>
        </div>

        <!-- Nút action chỉ hiện khi chưa xử lý -->
        <template v-if="selectedNotification.trangThai === 'CHUA_DOC' || selectedNotification.trangThai === 'DA_DOC'">
          <div class="detail-actions">
            <button class="reject-btn" @click="openRejectPopup" :disabled="actionLoading">
              Từ chối
            </button>

            <button class="accept-btn" @click="acceptCustomer" :disabled="actionLoading">
              <i v-if="actionLoading" class="fa-solid fa-spinner fa-spin"></i>
              Tiếp nhận
            </button>
          </div>

          <p class="hint">
            <i class="fa-solid fa-lock"></i>
            Nếu bạn tiếp nhận khách hàng, hệ thống sẽ chuyển khách hàng sang danh sách quản lý khách hàng.
          </p>
        </template>

        <!-- Đã xử lý -->
        <div v-else class="processed-info">
          <div v-if="selectedNotification.trangThai === 'DA_CHAP_NHAN'" class="processed-badge accepted">
            <i class="fa-solid fa-circle-check"></i>
            Đã tiếp nhận khách hàng
          </div>
          <div v-if="selectedNotification.trangThai === 'DA_TU_CHOI'" class="processed-badge rejected">
            <i class="fa-solid fa-circle-xmark"></i>
            Đã từ chối khách hàng
          </div>
        </div>
      </aside>
    </div>

    <!-- POPUP DETAIL CHO THÔNG BÁO TỪ CHỐI (TU_CHOI) -->
    <div
        v-if="selectedNotification && selectedNotification.loaiThongBao === 'TU_CHOI'"
        class="detail-popup-overlay"
        @click.self="selectedNotification = null"
    >
      <aside class="order-detail">
        <div class="detail-header">
          <h3>Chi tiết phản hồi từ chối</h3>
          <button @click="selectedNotification = null">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <div class="detail-section">
          <h5>Thông tin</h5>

          <div class="info-row" v-if="selectedNotification.tenNguoiGui">
            <span>Người từ chối</span>
            <b>{{ selectedNotification.tenNguoiGui }}</b>
          </div>

          <div class="info-row" v-if="selectedNotification.tenKhachHang">
            <span>Khách hàng</span>
            <b>{{ selectedNotification.tenKhachHang }}</b>
          </div>
        </div>

        <div class="detail-section">
          <h6>Lý do từ chối</h6>
          <p class="note reject-reason">{{ selectedNotification.lyDoTuChoi || selectedNotification.noiDung }}</p>
        </div>
      </aside>
    </div>

    <!-- POPUP NHẬP LÝ DO TỪ CHỐI -->
    <div
        v-if="showRejectPopup"
        class="reject-popup-overlay"
        @click.self="closeRejectPopup"
    >
      <div class="reject-popup">
        <div class="reject-popup-header">
          <h3>Từ chối tiếp nhận</h3>
          <button @click="closeRejectPopup">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <div class="reject-popup-body">
          <p class="reject-popup-desc">
            Vui lòng nhập lý do từ chối để gửi phản hồi về nhân viên hotline.
          </p>

          <label for="reject-reason">Lý do từ chối <span class="required">*</span></label>
          <textarea
              id="reject-reason"
              v-model="rejectReason"
              placeholder="Nhập lý do từ chối..."
              rows="4"
          ></textarea>

          <p v-if="rejectError" class="reject-error">
            <i class="fa-solid fa-triangle-exclamation"></i>
            {{ rejectError }}
          </p>
        </div>

        <div class="reject-popup-actions">
          <button class="cancel-btn" @click="closeRejectPopup" :disabled="actionLoading">
            Hủy
          </button>
          <button class="confirm-reject-btn" @click="confirmReject" :disabled="actionLoading">
            <i v-if="actionLoading" class="fa-solid fa-spinner fa-spin"></i>
            Xác nhận từ chối
          </button>
        </div>
      </div>
    </div>

    <!-- TOAST THÔNG BÁO -->
    <Transition name="toast">
      <div v-if="toast.show" class="toast" :class="toast.type">
        <i :class="toast.type === 'success' ? 'fa-solid fa-circle-check' : 'fa-solid fa-circle-xmark'"></i>
        <span>{{ toast.message }}</span>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import api from "../../api/api.js";

const activeTab = ref("all");
const selectedNotification = ref(null);
const loading = ref(false);
const actionLoading = ref(false);

// Popup từ chối
const showRejectPopup = ref(false);
const rejectReason = ref("");
const rejectError = ref("");

// Toast notification
const toast = ref({ show: false, message: "", type: "success" });

const tabs = [
  { key: "all", label: "Tất cả" },
  { key: "CONG_VIEC", label: "Công việc" },
  { key: "HE_THONG", label: "Hệ thống" },
  { key: "TU_CHOI", label: "Phản hồi" },
];

const notifications = ref([]);

const API_URL = "/api/nhan-vien/thong-bao";

// =================== LOAD DATA ===================

const loadNotifications = async () => {
  loading.value = true;
  try {
    const res = await api.get(API_URL);
    notifications.value = res.data;
  } catch (error) {
    console.error("Lỗi load thông báo:", error);
    showToast("Không thể tải thông báo", "error");
  } finally {
    loading.value = false;
  }
};

onMounted(loadNotifications);

// =================== COMPUTED ===================

const filteredNotifications = computed(() => {
  if (activeTab.value === "all") {
    return notifications.value;
  }

  return notifications.value.filter(
      item => item.loaiThongBao === activeTab.value
  );
});

const getCount = (key) => {
  if (key === "all") {
    return notifications.value.length;
  }

  return notifications.value.filter(item => item.loaiThongBao === key).length;
};

// =================== ICON & STATUS HELPERS ===================

const getIconClass = (loaiThongBao) => {
  if (loaiThongBao === "CONG_VIEC") return "customer";
  if (loaiThongBao === "HE_THONG") return "system";
  if (loaiThongBao === "TU_CHOI") return "reject";
  return "system";
};

const getIconName = (loaiThongBao) => {
  if (loaiThongBao === "CONG_VIEC") return "fa-regular fa-user";
  if (loaiThongBao === "HE_THONG") return "fa-regular fa-bell";
  if (loaiThongBao === "TU_CHOI") return "fa-solid fa-reply";
  return "fa-regular fa-bell";
};

const getStatusLabel = (trangThai) => {
  const map = {
    CHUA_DOC: "Chờ xử lý",
    DA_DOC: "Đã xem",
    DA_CHAP_NHAN: "Đã chấp nhận",
    DA_TU_CHOI: "Đã từ chối",
  };
  return map[trangThai] || trangThai;
};

const getStatusBadgeClass = (trangThai) => {
  if (trangThai === "DA_CHAP_NHAN") return "status-accepted";
  if (trangThai === "DA_TU_CHOI") return "status-rejected";
  return "";
};

// =================== SELECT NOTIFICATION ===================

const selectNotification = async (item) => {
  // Thông báo hệ thống không mở popup
  if (item.loaiThongBao === "HE_THONG") {
    // Đánh dấu đã đọc
    if (item.trangThai === "CHUA_DOC") {
      try {
        await api.put(`${API_URL}/${item.maThongBao}/da-doc`);
        item.trangThai = "DA_DOC";
      } catch (e) {
        console.error("Lỗi đánh dấu đã đọc:", e);
      }
    }
    selectedNotification.value = null;
    return;
  }

  // Đánh dấu đã đọc
  if (item.trangThai === "CHUA_DOC") {
    try {
      await api.put(`${API_URL}/${item.maThongBao}/da-doc`);
      item.trangThai = "DA_DOC";
    } catch (e) {
      console.error("Lỗi đánh dấu đã đọc:", e);
    }
  }

  selectedNotification.value = item;
};

// =================== ACCEPT ===================

const acceptCustomer = async () => {
  if (!selectedNotification.value) return;

  actionLoading.value = true;
  try {
    await api.put(`${API_URL}/${selectedNotification.value.maThongBao}/chap-nhan`);

    selectedNotification.value.trangThai = "DA_CHAP_NHAN";
    showToast("Đã tiếp nhận khách hàng thành công!", "success");

    selectedNotification.value = null;
    await loadNotifications();

  } catch (error) {
    console.error("Lỗi chấp nhận:", error);
    const msg = error.response?.data?.message || "Có lỗi xảy ra khi tiếp nhận";
    showToast(msg, "error");
  } finally {
    actionLoading.value = false;
  }
};

// =================== REJECT ===================

const openRejectPopup = () => {
  rejectReason.value = "";
  rejectError.value = "";
  showRejectPopup.value = true;
};

const closeRejectPopup = () => {
  showRejectPopup.value = false;
  rejectReason.value = "";
  rejectError.value = "";
};

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    rejectError.value = "Vui lòng nhập lý do từ chối";
    return;
  }

  if (!selectedNotification.value) return;

  actionLoading.value = true;
  try {
    await api.put(
        `${API_URL}/${selectedNotification.value.maThongBao}/tu-choi`,
        { lyDoTuChoi: rejectReason.value.trim() }
    );

    selectedNotification.value.trangThai = "DA_TU_CHOI";
    selectedNotification.value.lyDoTuChoi = rejectReason.value.trim();

    showToast("Đã từ chối và gửi phản hồi về hotline!", "success");

    closeRejectPopup();
    selectedNotification.value = null;
    await loadNotifications();

  } catch (error) {
    console.error("Lỗi từ chối:", error);
    const msg = error.response?.data?.message || "Có lỗi xảy ra khi từ chối";
    showToast(msg, "error");
  } finally {
    actionLoading.value = false;
  }
};

// =================== TOAST ===================

const showToast = (message, type = "success") => {
  toast.value = { show: true, message, type };
  setTimeout(() => {
    toast.value.show = false;
  }, 3500);
};
</script>

<style scoped src="../../assets/styles/TrangThongBaoNhanVienTrucTiep.css"></style>