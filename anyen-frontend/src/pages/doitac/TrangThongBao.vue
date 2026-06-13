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
        <p>Hiển thị 1 - 5 trong 32 thông báo</p>

        <div class="pagination">
          <button><i class="fa-solid fa-chevron-left"></i></button>
          <button class="active">1</button>
          <button>2</button>
          <button>3</button>
          <button>4</button>
          <span>...</span>
          <button>7</button>
          <button><i class="fa-solid fa-chevron-right"></i></button>
        </div>
      </div>
    </section>

    <!-- POPUP CHI TIẾT -->
    <div
        v-if="selectedNotification"
        class="detail-popup-overlay"
        @click.self="selectedNotification = null"
    >
      <aside class="order-detail">
        <div class="detail-header">
          <h3>Chi tiết đơn hàng</h3>
          <button @click="selectedNotification = null">
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
  { key: "customer", label: "Khách hàng" },
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
      code: "#AY24050128",
      date: "25/05/2024 - 14:30",
      status: "Chờ xác nhận",
      payment: "Chuyển khoản",
    },
    customer: {
      name: "Nguyễn Văn An",
      phone: "0901 234 567",
      email: "an.nguyenvan@gmail.com",
      address: "123 Đường An Lành, Phường Yên Hòa, Quận Cầu Giấy, Hà Nội",
    },
    product: {
      name: "Gói An Lạc",
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
      code: "#AY24050127",
      date: "25/05/2024 - 10:15",
      status: "Chờ xác nhận",
      payment: "Tiền mặt",
    },
    customer: {
      name: "Trần Thị Bình",
      phone: "0912 345 678",
      email: "binh@gmail.com",
      address: "TP.HCM",
    },
    product: {
      name: "Gói An Nhiên",
      quantity: 1,
      price: 35000000,
      image: "https://via.placeholder.com/70x70",
    },
    note: "Cần tư vấn thêm về nghi thức.",
  },
  {
    id: 3,
    category: "customer",
    type: "customer",
    icon: "fa-regular fa-user",
    title: "Thông tin khách hàng mới",
    desc: "Khách hàng: Lê Văn Cường",
    actionText: "Khách hàng vừa đăng ký",
    time: "24/05/2024 - 16:45",
    isNew: false,
    order: {
      code: "#KH24050126",
      date: "24/05/2024 - 16:45",
      status: "Khách hàng mới",
      payment: "Chưa có",
    },
    customer: {
      name: "Lê Văn Cường",
      phone: "0912 345 678",
      email: "cuong@gmail.com",
      address: "Bình Dương",
    },
    product: {
      name: "Chưa chọn dịch vụ",
      quantity: 0,
      price: 0,
      image: "https://via.placeholder.com/70x70",
    },
    note: "Khách hàng mới đăng ký thông tin.",
  },
  {
    id: 4,
    category: "system",
    type: "system",
    icon: "fa-regular fa-bell",
    title: "Cập nhật hệ thống",
    desc: "Hệ thống vừa cập nhật chính sách vận chuyển mới.",
    actionText: "Thông báo hệ thống",
    time: "24/05/2024 - 09:00",
    isNew: false,
    order: {
      code: "#SYSTEM",
      date: "24/05/2024 - 09:00",
      status: "Thông báo",
      payment: "Không có",
    },
    customer: {
      name: "Hệ thống",
      phone: "-",
      email: "-",
      address: "-",
    },
    product: {
      name: "Không có",
      quantity: 0,
      price: 0,
      image: "https://via.placeholder.com/70x70",
    },
    note: "Cập nhật chính sách vận chuyển.",
  },
  {
    id: 5,
    category: "order",
    type: "order",
    icon: "fa-regular fa-clipboard",
    title: "Đơn hàng mới #AY24050126",
    desc: "Khách hàng: Phạm Thị Dung",
    actionText: "Vui lòng xác nhận đơn hàng",
    time: "24/05/2024 - 09:20",
    isNew: true,
    order: {
      code: "#AY24050126",
      date: "24/05/2024 - 09:20",
      status: "Chờ xác nhận",
      payment: "Chuyển khoản",
    },
    customer: {
      name: "Phạm Thị Dung",
      phone: "0988 111 222",
      email: "dung@gmail.com",
      address: "Hà Nội",
    },
    product: {
      name: "Dịch vụ tang lễ trọn gói",
      quantity: 1,
      price: 24000000,
      image: "https://via.placeholder.com/70x70",
    },
    note: "Gia đình cần liên hệ sớm.",
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

  return notifications.value.filter(item => item.category === key).length;
};

const selectNotification = (item) => {
  selectedNotification.value = item;
};

const formatPrice = (price) => {
  return new Intl.NumberFormat("vi-VN").format(price) + " đ";
};

const acceptOrder = () => {
  alert("Đã chấp nhận đơn hàng!");
  selectedNotification.value = null;
};

const rejectOrder = () => {
  alert("Đã từ chối đơn hàng!");
  selectedNotification.value = null;
};
</script>

<style scoped src="../../assets/styles/TrangThongBao.css"></style>