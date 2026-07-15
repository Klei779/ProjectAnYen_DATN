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

              :key="item.MaThongBao"

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

                  <h4>{{ item.TieuDe || 'Thông báo hệ thống' }}</h4>

                </div>

                

                <p class="customer-info">

                  {{ item.NoiDung }}

                </p>

                <p class="time-info">

                  <i class="fa-regular fa-clock"></i> {{ formatTime(item.ThoiGianTao) }}

                </p>

              </div>

            </div>



            <!-- Action Buttons Right (Inline) -->

            <div class="card-actions-wrapper">

              <span class="time-ago" v-if="item.DaDoc === false">Mới</span>



              <div

                  class="card-buttons"

                  v-if="item.TrangThaiThongBao === 'CHO_XAC_NHAN'"

              >

                <button class="btn-outline" @click.stop="openRejectPopup(item)" :disabled="actionLoading">

                  Từ chối

                </button>

                <button class="btn-primary" @click.stop="quickAccept(item)" :disabled="actionLoading">

                  <i v-if="actionLoading" class="fa-solid fa-spinner fa-spin"></i>

                  Chấp nhận

                </button>

              </div>



              <div

                  class="card-buttons processed"

                  v-else

              >

                <span class="text-success fw-bold" v-if="item.TrangThaiThongBao === 'DA_CHAP_NHAN'"><i class="fa-solid fa-check"></i> Đã chấp nhận</span>

                <span class="text-danger fw-bold" v-if="item.TrangThaiThongBao === 'DA_TU_CHOI'"><i class="fa-solid fa-xmark"></i> Đã từ chối</span>

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

          <h3>Chi tiết thông báo</h3>

          <button class="close-btn" @click="selectedNotification = null">

            <i class="fa-solid fa-xmark"></i>

          </button>

        </div>

        

        <!-- SIDEBAR BODY -->

        <div class="sidebar-body">

          <h4 class="mb-4 fw-bold">Thông tin thông báo</h4>

            

          <div class="info-table-clean">

            <div class="info-row">

              <span class="label">Mã thông báo</span>

              <span class="value fw-bold">#{{ selectedNotification.MaThongBao || '—' }}</span>

            </div>

            <div class="info-row">

              <span class="label">Loại thông báo</span>

              <span class="value fw-bold">{{ selectedNotification.Loai || '—' }}</span>

            </div>

            <div class="info-row">

              <span class="label">Tiêu đề</span>

              <span class="value fw-bold">{{ selectedNotification.TieuDe || '—' }}</span>

            </div>

            <div class="info-row">

              <span class="label">Mã đơn hàng</span>

              <span class="value fw-bold">#DH{{ selectedNotification.MaDonHang || '—' }}</span>

            </div>

            <div class="info-row">

              <span class="label">Thời gian tạo</span>

              <span class="value fw-bold">{{ formatTime(selectedNotification.ThoiGianTao) }}</span>

            </div>

            <div class="info-row">

              <span class="label">Trạng thái</span>

              <span class="value">

                <span v-if="selectedNotification.TrangThaiThongBao === 'CHO_XAC_NHAN'" class="status-pill warning">Chờ xác nhận</span>

                <span v-else-if="selectedNotification.TrangThaiThongBao === 'DA_CHAP_NHAN'" class="status-pill success">Đã chấp nhận</span>

                <span v-else-if="selectedNotification.TrangThaiThongBao === 'DA_TU_CHOI'" class="status-pill error">Đã từ chối</span>

              </span>

            </div>

          </div>



          <div class="text-block">

            <h5>Nội dung</h5>

            <p>{{ selectedNotification.NoiDung || '—' }}</p>

          </div>



          <div v-if="selectedNotification.LyDoTuChoi" class="text-block">

            <h5 class="text-danger">Lý do từ chối</h5>

            <p class="text-danger">{{ selectedNotification.LyDoTuChoi }}</p>

          </div>



          <!-- Buttons -->

          <div class="sidebar-actions" v-if="selectedNotification.TrangThaiThongBao === 'CHO_XAC_NHAN'">

            <button class="btn-outline-modal" @click="openRejectPopup(selectedNotification)">Từ chối</button>

            <button class="btn-primary-modal" @click="quickAccept(selectedNotification)">Chấp nhận</button>

          </div>

          

          <p class="sidebar-note" v-if="selectedNotification.TrangThaiThongBao === 'CHO_XAC_NHAN'">

            <i class="fa-solid fa-lock"></i> Nếu bạn chấp nhận thông báo, hệ thống sẽ cập nhật trạng thái đơn hàng

          </p>

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

          <h3>Từ chối thông báo</h3>

          <button class="close-btn" @click="closeRejectPopup">

            <i class="fa-solid fa-xmark"></i>

          </button>

        </div>



        <div class="modal-body">

          <p class="mb-3 text-muted">Vui lòng nhập lý do từ chối.</p>

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

import { ref, computed, onMounted } from "vue";

import { useRouter } from "vue-router";

import {

  getThongBaoDoiTac,

  chapNhanThongBao,

  tuChoiThongBao

} from "../../services/thongBaoDoiTacService.js";



const router = useRouter();



// Layout & State

const activeTab = ref("all");

const selectedNotification = ref(null);

const loading = ref(false);

const actionLoading = ref(false);

const itemToReject = ref(null);



// Popup Từ Chối

const showRejectPopup = ref(false);

const rejectReason = ref("");

const rejectError = ref("");



// Toast

const toast = ref({ show: false, message: "", type: "success" });



// Tabs

const tabs = [

  { key: "all", label: "Tất cả" },

  { key: "CHO_XAC_NHAN", label: "Chờ xác nhận" },

  { key: "DA_CHAP_NHAN", label: "Đã chấp nhận" },

  { key: "DA_TU_CHOI", label: "Đã từ chối" }

];



const notifications = ref([]);



// =================== LOAD DATA ===================



const loadThongBao = async () => {
  loading.value = true;

  try {
    const data = await getThongBaoDoiTac();
    const list = Array.isArray(data) ? data : [];

    notifications.value = list.map(item => ({
      MaThongBao:
          item.MaThongBao ??
          item.maThongBao ??
          item.id,

      Loai:
          item.Loai ??
          item.loai,

      TieuDe:
          item.TieuDe ??
          item.tieuDe,

      NoiDung:
          item.NoiDung ??
          item.noiDung,

      MaDonHang:
          item.MaDonHang ??
          item.maDonHang,

      ThoiGianTao:
          item.ThoiGianTao ??
          item.thoiGianTao ??
          item.thoiGian,

      TrangThaiThongBao:
          item.TrangThaiThongBao ??
          item.trangThaiThongBao ??
          item.trangThai,

      DaDoc:
          item.DaDoc ??
          item.daDoc ??
          false,

      LyDoTuChoi:
          item.LyDoTuChoi ??
          item.lyDoTuChoi
    }));

    console.log("Thông báo sau khi chuẩn hóa:", notifications.value);
  } catch (error) {
    console.error("Lỗi tải thông báo:", error);
    showToast("Không thể tải danh sách thông báo", "error");
  } finally {
    loading.value = false;
  }
};



onMounted(() => {

  loadThongBao();

});



// =================== COMPUTED ===================



const filteredNotifications = computed(() => {

  let list = notifications.value;



  if (activeTab.value === "all") {

    return list;

  }



  return list.filter(item => item.TrangThaiThongBao === activeTab.value);

});



const getCount = (key) => {

  let list = notifications.value;

  if (key === "all") return list.length;

  return list.filter(item => item.TrangThaiThongBao === key).length;

};



// =================== ICONS ===================



const getIconClass = (loai) => {

  if (loai === "DON_HANG") return "bg-red";

  if (loai === "DUYET_SAN_PHAM") return "bg-yellow";

  return "bg-blue";

};



const getIconName = (loai) => {

  if (loai === "DON_HANG") return "fa-solid fa-box";

  if (loai === "DUYET_SAN_PHAM") return "fa-solid fa-check-circle";

  return "fa-solid fa-bell";

};



// =================== ACTIONS ===================



const selectNotification = (item) => {

  selectedNotification.value = item;

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



const quickAccept = async (item) => {

  actionLoading.value = true;



  try {

    await chapNhanThongBao(item.MaThongBao);



    // Update local state

    notifications.value = notifications.value.map(n => {

      if (n.MaThongBao === item.MaThongBao) {

        return {

          ...n,

          TrangThaiThongBao: "DA_CHAP_NHAN"

        };

      }

      return n;

    });



    showToast("Đã chấp nhận thông báo!", "success");

    selectedNotification.value = null;

  } catch (error) {

    console.error("Lỗi chấp nhận:", error);

    showToast(error.response?.data?.message || "Chấp nhận thất bại", "error");

  } finally {

    actionLoading.value = false;

  }

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

    await tuChoiThongBao(itemToReject.value.MaThongBao, rejectReason.value.trim());



    // Update local state

    notifications.value = notifications.value.map(n => {

      if (n.MaThongBao === itemToReject.value.MaThongBao) {

        return {

          ...n,

          TrangThaiThongBao: "DA_TU_CHOI",

          LyDoTuChoi: rejectReason.value.trim()

        };

      }

      return n;

    });



    showToast("Đã từ chối thông báo!", "success");

    closeRejectPopup();

    selectedNotification.value = null;

  } catch (error) {

    console.error("Lỗi từ chối:", error);

    showToast(error.response?.data?.message || "Từ chối thất bại", "error");

  } finally {

    actionLoading.value = false;

  }

};



const showToast = (message, type = "success") => {

  toast.value = { show: true, message, type };

  setTimeout(() => { toast.value.show = false; }, 3000);

};



const formatTime = (dateTime) => {

  if (!dateTime) return 'N/A';

  const date = new Date(dateTime);

  return date.toLocaleString('vi-VN', {

    day: '2-digit',

    month: '2-digit',

    year: 'numeric',

    hour: '2-digit',

    minute: '2-digit'

  });

};

</script>

<style scoped src="../../assets/styles/nhanvien/QLThongBao/TrangThongBaoNV.css"></style>