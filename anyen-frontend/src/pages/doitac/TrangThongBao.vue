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
          </div>
        </div>
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
    <div
        v-if="selectedNotification"
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
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <div class="alert-box">
          <i class="fa-solid fa-circle-exclamation"></i>
          <div>
            <strong>Đơn hàng mới</strong>
            <p>Vui lòng xem xét và xác nhận đơn hàng này.</p>
          </div>
        </div>

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
            <img :src="selectedNotification.product.image" alt="" />

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

        <div class="detail-actions">
          <button class="reject-btn" @click="rejectOrder">
            Từ chối
          </button>

          <button class="accept-btn" @click="acceptOrder">
            Chấp nhận
          </button>
        </div>

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
  </div>
</template>

<script setup>
import { ref, computed } from "vue";

const activeTab = ref("all");
const selectedNotification = ref(null);

const tabs = [
  { key: "all", label: "Tất cả" },
  { key: "order", label: "Đơn hàng" },
  { key: "system", label: "Hệ thống" },
];

const notifications = ref([
  {
    id: 1,
    category: "order",
    type: "order",
    icon: "fa-regular fa-clipboard",
    title: "Đơn hàng mới #AY24050128",
    desc: "Khách hàng: Nguyễn Văn An",
    actionText: "Vui lòng xác nhận đơn hàng",
    time: "25/05/2024 - 14:30",
    isNew: true,

    order: {
      id: 128,
      code: "#AY24050128",
      date: "25/05/2024 - 14:30",
      status: "Chờ xác nhận",
      payment: "Chuyển khoản",
    },

    customer: {
      id: 1,
      name: "Nguyễn Văn An",
      phone: "0901 234 567",
      email: "an.nguyenvan@gmail.com",
      address: "123 Đường An Lành, Phường Yên Hòa, Quận Cầu Giấy, Hà Nội",
    },

    product: {
      id: 1,
      name: "Gói An Lạc",
      desc: "Dịch vụ tang lễ trọn gói cơ bản",
      quantity: 1,
      price: 25000000,
      image: "https://via.placeholder.com/70x70",
    },

    note: "Gia đình cần hỗ trợ trang trí hoa sen trắng.",
  },

  {
    id: 2,
    category: "order",
    type: "order",
    icon: "fa-regular fa-clipboard",
    title: "Đơn hàng mới #AY24050127",
    desc: "Khách hàng: Trần Thị Bình",
    actionText: "Vui lòng xác nhận đơn hàng",
    time: "25/05/2024 - 10:15",
    isNew: true,

    order: {
      id: 127,
      code: "#AY24050127",
      date: "25/05/2024 - 10:15",
      status: "Chờ xác nhận",
      payment: "Tiền mặt",
    },

    customer: {
      id: 2,
      name: "Trần Thị Bình",
      phone: "0912 345 678",
      email: "binh@gmail.com",
      address: "TP.HCM",
    },

    product: {
      id: 2,
      name: "Gói An Nhiên",
      desc: "Dịch vụ tang lễ đầy đủ nghi thức",
      quantity: 1,
      price: 35000000,
      image: "https://via.placeholder.com/70x70",
    },

    note: "Cần tư vấn thêm về nghi thức.",
  },

  {
    id: 3,
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
  },

  {
    id: 4,
    category: "order",
    type: "order",
    icon: "fa-regular fa-clipboard",
    title: "Đơn hàng mới #AY24050126",
    desc: "Khách hàng: Phạm Thị Dung",
    actionText: "Vui lòng xác nhận đơn hàng",
    time: "24/05/2024 - 09:20",
    isNew: true,

    order: {
      id: 126,
      code: "#AY24050126",
      date: "24/05/2024 - 09:20",
      status: "Chờ xác nhận",
      payment: "Chuyển khoản",
    },

    customer: {
      id: 4,
      name: "Phạm Thị Dung",
      phone: "0988 111 222",
      email: "dung@gmail.com",
      address: "Hà Nội",
    },

    product: {
      id: 3,
      name: "Dịch vụ tang lễ trọn gói",
      desc: "Gói dịch vụ hỗ trợ đầy đủ theo yêu cầu gia đình",
      quantity: 1,
      price: 24000000,
      image: "https://via.placeholder.com/70x70",
    },

    note: "Gia đình cần liên hệ sớm.",
  },

  {
    id: 5,
    category: "system",
    type: "system",
    icon: "fa-regular fa-bell",
    title: "Bảo trì hệ thống",
    desc: "Hệ thống sẽ bảo trì vào 23:00 tối nay.",
    actionText: "Xem thông báo hệ thống",
    time: "23/05/2024 - 08:00",
    isNew: true,

    system: {
      id: 2,
      code: "#SYSTEM24050002",
      title: "Bảo trì hệ thống",
      type: "Thông báo bảo trì",
      level: "Quan trọng",
      time: "23/05/2024 - 08:00",
      module: "Toàn bộ hệ thống",
      sender: "Quản trị hệ thống",
      shortContent: "Hệ thống sẽ bảo trì vào 23:00 tối nay.",
      content:
          "Hệ thống sẽ tiến hành bảo trì từ 23:00 đến 23:30. Trong thời gian này, một số chức năng như tạo đơn hàng, cập nhật sản phẩm và xác nhận thông báo có thể tạm thời gián đoạn.",
    },
  },
]);

const filteredNotifications = computed(() => {
  if (activeTab.value === "all") {
    return notifications.value;
  }

  return notifications.value.filter(
      item => item.category === activeTab.value
  );
});

const getCount = (key) => {
  if (key === "all") {
    return notifications.value.length;
  }

  return notifications.value.filter(
      item => item.category === key
  ).length;
};

const selectNotification = (item) => {
  selectedNotification.value = item;
};

const closePopup = () => {
  selectedNotification.value = null;
};

const formatPrice = (price) => {
  return new Intl.NumberFormat("vi-VN").format(price) + " đ";
};

const acceptOrder = () => {
  alert("Đã chấp nhận đơn hàng!");

  selectedNotification.value.order.status = "Đã xác nhận";
  selectedNotification.value.isNew = false;

  closePopup();
};

const rejectOrder = () => {
  alert("Đã từ chối đơn hàng!");

  selectedNotification.value.order.status = "Đã từ chối";
  selectedNotification.value.isNew = false;

  closePopup();
};

const readSystemNotification = () => {
  selectedNotification.value.isNew = false;
  closePopup();
};
</script>

<style scoped src="../../assets/styles/TrangThongBao.css"></style>