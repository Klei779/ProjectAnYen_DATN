<<<<<<< HEAD
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

=======
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

      <div class="notification-list">
        <div
            v-for="item in filteredNotifications"
            :key="item.id"
            class="notification-card"
            :class="{ active: selectedNotification?.id === item.id }"
            @click="selectNotification(item)"
        >
          <div class="noti-icon" :class="item.type">
            <i :class="item.icon"></i>
          </div>

          <div class="noti-content">
            <div class="noti-head">
              <h4>{{ item.title }}</h4>
              <span v-if="item.isNew">Mới</span>
            </div>

            <p>{{ item.desc }}</p>
            <small>{{ item.time }}</small>
          </div>

          <div class="noti-action">
            <p>{{ item.actionText }}</p>
            <i class="fa-solid fa-chevron-right"></i>
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
          </div>

        </div>
<<<<<<< HEAD

      </section>



    </div>



    <!-- SIDEBAR: Chi tiết thông báo -->

=======
      </div>

      <div class="pagination-row">
        <p>
          Hiển thị 1 - {{ filteredNotifications.length }}
          trong {{ notifications.length }} thông báo
        </p>

        <div class="pagination">
          <button>
            <i class="fa-solid fa-chevron-left"></i>
          </button>
          <button class="active">1</button>
          <button>2</button>
          <button>3</button>
          <button>4</button>
          <span>...</span>
          <button>7</button>
          <button>
            <i class="fa-solid fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </section>

    <!-- POPUP CHI TIẾT -->
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
    <div

        v-if="selectedNotification"
<<<<<<< HEAD

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

=======
        class="detail-popup-overlay"
        @click.self="closePopup"
    >
      <!-- FORM ĐƠN HÀNG -->
      <aside
          v-if="selectedNotification.category === 'order'"
          class="order-detail"
      >
        <div class="detail-header">
          <h3>Chi tiết đơn hàng</h3>
          <button @click="closePopup">
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
            <i class="fa-solid fa-xmark"></i>

          </button>

        </div>

<<<<<<< HEAD


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

=======
        <div class="alert-box">
          <i class="fa-solid fa-circle-exclamation"></i>
          <div>
            <strong>Đơn hàng mới</strong>
            <p>Vui lòng xem xét và xác nhận đơn hàng này.</p>
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
          </div>

        </div>

<<<<<<< HEAD


        <div class="modal-footer right-align">

          <button class="btn-outline-modal" @click="closeRejectPopup" :disabled="actionLoading">Hủy</button>

          <button class="btn-danger-modal" @click="confirmReject" :disabled="actionLoading">

            <i v-if="actionLoading" class="fa-solid fa-spinner fa-spin"></i> Xác nhận

=======
        <div class="detail-section">
          <h4>Thông tin đơn hàng</h4>

          <div class="info-row">
            <span>Mã đơn hàng</span>
            <b>{{ selectedNotification.order.code }}</b>
          </div>

          <div class="info-row">
            <span>Ngày đặt</span>
            <b>{{ selectedNotification.order.date }}</b>
          </div>

          <div class="info-row">
            <span>Trạng thái</span>
            <em>{{ selectedNotification.order.status }}</em>
          </div>

          <div class="info-row">
            <span>Phương thức thanh toán</span>
            <b>{{ selectedNotification.order.payment }}</b>
          </div>
        </div>

        <div class="detail-section">
          <h4>Thông tin khách hàng</h4>

          <div class="info-row">
            <span>Họ và tên</span>
            <b>{{ selectedNotification.customer.name }}</b>
          </div>

          <div class="info-row">
            <span>Số điện thoại</span>
            <b>{{ selectedNotification.customer.phone }}</b>
          </div>

          <div class="info-row">
            <span>Email</span>
            <b>{{ selectedNotification.customer.email }}</b>
          </div>

          <div class="info-row">
            <span>Địa chỉ</span>
            <b>{{ selectedNotification.customer.address }}</b>
          </div>
        </div>

        <div class="detail-section">
          <h4>Thông tin sản phẩm / dịch vụ</h4>

          <div class="product-line">
            <img
                :src="getProductImage(selectedNotification.product.image)"
                alt=""
                @error="handleImageError"
            />

            <div>
              <h5>{{ selectedNotification.product.name }}</h5>
              <p>{{ selectedNotification.product.desc }}</p>
            </div>

            <div class="product-price">
              <span>x{{ selectedNotification.product.quantity }}</span>
              <b>{{ formatPrice(selectedNotification.product.price) }}</b>
            </div>
          </div>

          <div class="total-row">
            <span>Tạm tính</span>
            <b>{{ formatPrice(selectedNotification.product.price) }}</b>
          </div>

          <div class="total-row final">
            <span>Tổng tiền</span>
            <strong>{{ formatPrice(selectedNotification.product.price) }}</strong>
          </div>
        </div>

        <div class="detail-section">
          <h4>Ghi chú của khách hàng</h4>
          <p class="note">{{ selectedNotification.note }}</p>
        </div>

        <div class="detail-actions" v-if="isChoXacNhan(selectedNotification)">
          <button class="reject-btn" @click="rejectOrder">
            Từ chối
          </button>

          <button class="accept-btn" @click="acceptOrder">
            Chấp nhận
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
          </button>

        </div>

<<<<<<< HEAD
      </div>

    </div>



    <!-- TOAST -->

    <Transition name="toast">

      <div v-if="toast.show" class="toast-notification" :class="toast.type">

        <i :class="toast.type === 'success' ? 'fa-solid fa-circle-check' : 'fa-solid fa-circle-xmark'"></i>

        <span>{{ toast.message }}</span>

      </div>

    </Transition>



=======
        <div class="detail-actions" v-else>
          <button class="accept-btn" type="button" @click="closePopup">
            Đóng
          </button>
        </div>

        <p class="hint" v-if="isChoXacNhan(selectedNotification)">
          <i class="fa-solid fa-lock"></i>
          Nếu bạn chấp nhận đơn hàng, hệ thống sẽ chuyển đơn sang Quản lý đơn hàng.
        </p>

        <p class="hint" v-else>
          <i class="fa-solid fa-circle-info"></i>
          Thông báo này đã được xử lý.
        </p>
        <p class="hint">
          <i class="fa-solid fa-lock"></i>
          Nếu bạn chấp nhận đơn hàng, hệ thống sẽ chuyển đơn sang Quản lý đơn hàng.
        </p>
      </aside>

      <!-- FORM HỆ THỐNG -->
      <aside
          v-else-if="selectedNotification.category === 'system'"
          class="order-detail"
      >
        <div class="detail-header">
          <h3>Thông báo hệ thống</h3>
          <button @click="closePopup">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <div class="alert-box">
          <i class="fa-regular fa-bell"></i>
          <div>
            <strong>{{ selectedNotification.system.title }}</strong>
            <p>{{ selectedNotification.system.shortContent }}</p>
          </div>
        </div>

        <div class="detail-section">
          <h4>Thông tin thông báo</h4>

          <div class="info-row">
            <span>Mã thông báo</span>
            <b>{{ selectedNotification.system.code }}</b>
          </div>

          <div class="info-row">
            <span>Loại thông báo</span>
            <b>{{ selectedNotification.system.type }}</b>
          </div>

          <div class="info-row">
            <span>Mức độ</span>
            <em>{{ selectedNotification.system.level }}</em>
          </div>

          <div class="info-row">
            <span>Thời gian</span>
            <b>{{ selectedNotification.system.time }}</b>
          </div>

          <div class="info-row">
            <span>Khu vực ảnh hưởng</span>
            <b>{{ selectedNotification.system.module }}</b>
          </div>

          <div class="info-row">
            <span>Người gửi</span>
            <b>{{ selectedNotification.system.sender }}</b>
          </div>
        </div>

        <div class="detail-section">
          <h4>Nội dung thông báo</h4>
          <p class="note">{{ selectedNotification.system.content }}</p>
        </div>

        <div class="detail-actions">
          <button class="accept-btn" @click="readSystemNotification">
            Đã hiểu
          </button>
        </div>

        <p class="hint">
          <i class="fa-solid fa-circle-info"></i>
          Đây là thông báo hệ thống, không liên quan đến xác nhận đơn hàng.
        </p>
      </aside>
    </div>
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
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

<<<<<<< HEAD


const router = useRouter();



// Layout & State

=======
const fallbackImage =
    "data:image/svg+xml;utf8," +
    encodeURIComponent(`
      <svg xmlns="http://www.w3.org/2000/svg" width="70" height="70">
        <rect width="70" height="70" rx="10" fill="#f3f3f3"/>
        <text x="35" y="40" text-anchor="middle" font-size="12" fill="#999">
          An Yên
        </text>
      </svg>
    `);
const productImages = import.meta.glob(
    "../../assets/images/TrangSanPham/*",
    {
      eager: true,
      import: "default"
    }
);
const router = useRouter();

>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
const activeTab = ref("all");

const selectedNotification = ref(null);
<<<<<<< HEAD

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

=======
const notifications = ref([]);
const TT_DT_CHO_XAC_NHAN = 0;
const TT_DT_DA_CHAP_NHAN = 1;
const TT_DT_DA_TU_CHOI = 2;

const isChoXacNhan = (item) => {
  return Number(item?.trangThaiThongBao) === TT_DT_CHO_XAC_NHAN;
};

const getTrangThaiThongBaoText = (status) => {
  const map = {
    0: "Chờ xác nhận",
    1: "Đã chấp nhận",
    2: "Đã từ chối",
  };

  return map[Number(status)] || "Không xác định";
};
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
const tabs = [

  { key: "all", label: "Tất cả" },
<<<<<<< HEAD

  { key: "CHO_XAC_NHAN", label: "Chờ xác nhận" },

  { key: "DA_CHAP_NHAN", label: "Đã chấp nhận" },

  { key: "DA_TU_CHOI", label: "Đã từ chối" }

];



const notifications = ref([]);



// =================== LOAD DATA ===================



=======
  { key: "order", label: "Đơn hàng" },
  { key: "system", label: "Hệ thống" },
];

const systemNotifications = [
  {
    id: "system-1",
    category: "system",
    type: "system",
    icon: "fa-regular fa-bell",
    title: "Cập nhật hệ thống",
    desc: "Hệ thống vừa cập nhật chính sách vận chuyển mới.",
    actionText: "Xem thông báo hệ thống",
    time: "24/05/2024 - 09:00",
    isNew: false,

    system: {
      id: 1,
      code: "#SYSTEM24050001",
      title: "Cập nhật hệ thống",
      type: "Thông báo nội bộ",
      level: "Bình thường",
      time: "24/05/2024 - 09:00",
      module: "Quản lý đơn hàng, Quản lý vận chuyển",
      sender: "Hệ thống An Yên",
      shortContent: "Hệ thống vừa cập nhật chính sách vận chuyển mới.",
      content:
          "Từ ngày 24/05/2024, hệ thống cập nhật chính sách vận chuyển mới. Nhân viên vui lòng kiểm tra kỹ khu vực giao hàng, phí vận chuyển và thời gian hỗ trợ trước khi xác nhận đơn hàng cho khách.",
    },
  }
];
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
const loadThongBao = async () => {
  try {
    const data = await getThongBaoDoiTac();
<<<<<<< HEAD
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
=======

    const orderNotifications = Array.isArray(data)
        ? data.map(item => ({
          ...item,
          trangThaiThongBao: Number(item.trangThaiThongBao),
        }))
        : [];

    notifications.value = [
      ...orderNotifications,
      ...systemNotifications
    ];
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
  } catch (error) {
    console.error("Lỗi tải thông báo:", error);
    alert("Không thể tải danh sách thông báo");
  }
};



onMounted(() => {

  loadThongBao();

});

<<<<<<< HEAD


// =================== COMPUTED ===================



const filteredNotifications = computed(() => {

  let list = notifications.value;



  if (activeTab.value === "all") {

    return list;

  }



  return list.filter(item => item.TrangThaiThongBao === activeTab.value);

=======
const filteredNotifications = computed(() => {
  if (activeTab.value === "all") {
    return notifications.value;
  }

  return notifications.value.filter(
      item => item.category === activeTab.value
  );
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
});



const getCount = (key) => {
<<<<<<< HEAD

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



=======
  if (key === "all") {
    return notifications.value.length;
  }

  return notifications.value.filter(
      item => item.category === key
  ).length;
};

>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
const selectNotification = (item) => {

  selectedNotification.value = item;

};

<<<<<<< HEAD


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
=======
const closePopup = () => {
  selectedNotification.value = null;
};

const formatPrice = (price) => {
  const value = Number(price || 0);

  return new Intl.NumberFormat("vi-VN").format(value) + " đ";
};

const acceptOrder = async () => {
  if (!selectedNotification.value) return;
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)



  try {
<<<<<<< HEAD

    await chapNhanThongBao(item.MaThongBao);



    // Update local state

    notifications.value = notifications.value.map(n => {

      if (n.MaThongBao === item.MaThongBao) {

        return {

          ...n,

          TrangThaiThongBao: "DA_CHAP_NHAN"

=======
    const maThongBao = selectedNotification.value.id;

    const result = await chapNhanThongBao(maThongBao);

    notifications.value = notifications.value.map(item => {
      if (item.id === maThongBao) {
        return {
          ...item,
          trangThaiThongBao: TT_DT_DA_CHAP_NHAN,
          isNew: false,
          actionText: "Đã chấp nhận đơn hàng",
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
        };

      }

<<<<<<< HEAD
      return n;

    });



    showToast("Đã chấp nhận thông báo!", "success");

    selectedNotification.value = null;

  } catch (error) {

    console.error("Lỗi chấp nhận:", error);

    showToast(error.response?.data?.message || "Chấp nhận thất bại", "error");

  } finally {

    actionLoading.value = false;

=======
      return item;
    });

    closePopup();

    await router.push(
        result.redirectUrl || "/doi-tac/quan-ly-don-hang"
    );
  } catch (error) {
    console.error("Lỗi chấp nhận đơn hàng:", error);
    alert(error.response?.data?.message || "Chấp nhận đơn hàng thất bại");
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
  }

};

<<<<<<< HEAD


const confirmReject = async () => {

  if (!rejectReason.value.trim()) {

    rejectError.value = "Vui lòng nhập lý do từ chối";

=======
const rejectOrder = async () => {
  if (!selectedNotification.value) return;

  const lyDo = prompt("Nhập lý do từ chối đơn hàng:");

  if (!lyDo || !lyDo.trim()) {
    alert("Vui lòng nhập lý do từ chối");
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
    return;

  }

<<<<<<< HEAD


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

=======
  try {
    const maThongBao = selectedNotification.value.id;
    const lyDoTrim = lyDo.trim();

    await tuChoiThongBao(maThongBao, lyDoTrim);

    notifications.value = notifications.value.map(item => {
      if (item.id === maThongBao) {
        return {
          ...item,
          trangThaiThongBao: TT_DT_DA_TU_CHOI,
          lyDoTuChoi: lyDoTrim,
          isNew: false,
          actionText: "Đã từ chối đơn hàng",
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
        };

      }

<<<<<<< HEAD
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

=======
      return item;
    });

    closePopup();

    alert("Đã từ chối đơn hàng");
  } catch (error) {
    console.error("Lỗi từ chối đơn hàng:", error);
    alert(error.response?.data?.message || "Từ chối đơn hàng thất bại");
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
  }

};

<<<<<<< HEAD


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

=======
const readSystemNotification = () => {
  if (!selectedNotification.value) return;

  selectedNotification.value.isNew = false;
  closePopup();
};
const getProductImage = (image) => {
  if (!image) {
    return fallbackImage;
  }

  if (
      image.startsWith("http") ||
      image.startsWith("data:") ||
      image.startsWith("blob:")
  ) {
    return image;
  }

  const imagePath = `../../assets/images/TrangSanPham/${image}`;

  return productImages[imagePath] || fallbackImage;
};

const handleImageError = (event) => {
  event.target.src = fallbackImage;
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
};

</script>
<<<<<<< HEAD

<style scoped src="../../assets/styles/nhanvien/QLThongBao/TrangThongBaoNV.css"></style>
=======
<style scoped src="../../assets/styles/doitac/QLThongBao/TrangThongBaoDT.css"></style>
>>>>>>> b4d7834 (Quản lý combo BE tạo combo, hotline giao task và FE chi tiết sản phẩm đối tác)
