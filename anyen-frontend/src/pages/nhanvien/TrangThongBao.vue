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
            <i
                v-if="item.category === 'customer'"
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

    <!-- POPUP CHI TIẾT KHÁCH HÀNG -->
    <div
        v-if="selectedNotification"
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
          <h4>Thông tin khách hàng</h4>

          <div class="info-row">
            <span>Mã khách hàng</span>
            <b>{{ selectedNotification.customer.code }}</b>
          </div>

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

          <div class="info-row">
            <span>Ngày đăng ký</span>
            <b>{{ selectedNotification.customer.createdAt }}</b>
          </div>

          <div class="info-row">
            <span>Trạng thái</span>
            <em>{{ selectedNotification.customer.status }}</em>
          </div>

          <div class="info-row">
            <span>Nguồn đăng ký</span>
            <b>{{ selectedNotification.customer.source }}</b>
          </div>
        </div>

        <div class="detail-section">
          <h6>Nhu cầu hỗ trợ</h6>
          <p class="note">
            {{ selectedNotification.customer.need }}
          </p>
        </div>

        <div class="detail-section">
          <h6>Ghi chú</h6>
          <p class="note">
            {{ selectedNotification.note }}
          </p>
        </div>

        <div class="detail-actions">
          <button class="reject-btn" @click="rejectCustomer">
            Từ chối
          </button>

          <button class="accept-btn" @click="acceptCustomer">
            Tiếp nhận
          </button>
        </div>

        <p class="hint">
          <i class="fa-solid fa-lock"></i>
          Nếu bạn tiếp nhận khách hàng, hệ thống sẽ chuyển khách hàng sang danh sách quản lý khách hàng.
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
  { key: "customer", label: "Khách hàng" },
  { key: "system", label: "Hệ thống" },
];

const notifications = ref([
  {
    id: 1,
    category: "customer",
    type: "customer",
    icon: "fa-regular fa-user",
    title: "Thông tin khách hàng mới",
    desc: "Khách hàng: Lê Văn Cường",
    actionText: "Khách hàng vừa đăng ký",
    time: "24/05/2024 - 16:45",
    isNew: true,
    customer: {
      code: "#KH24050126",
      name: "Lê Văn Cường",
      phone: "0912 345 678",
      email: "cuong@gmail.com",
      address: "Bình Dương",
      createdAt: "24/05/2024 - 16:45",
      status: "Chờ tiếp nhận",
      source: "Website An Yên",
      need: "Khách hàng cần được tư vấn dịch vụ tang lễ phù hợp với gia đình.",
    },
    note: "Khách hàng mới đăng ký thông tin, cần nhân viên liên hệ lại.",
  },
  {
    id: 2,
    category: "customer",
    type: "customer",
    icon: "fa-regular fa-user",
    title: "Thông tin khách hàng mới",
    desc: "Khách hàng: Nguyễn Văn An",
    actionText: "Khách hàng vừa đăng ký",
    time: "25/05/2024 - 09:20",
    isNew: true,
    customer: {
      code: "#KH24050127",
      name: "Nguyễn Văn An",
      phone: "0901 234 567",
      email: "an.nguyenvan@gmail.com",
      address: "Cầu Giấy, Hà Nội",
      createdAt: "25/05/2024 - 09:20",
      status: "Chờ tiếp nhận",
      source: "Website An Yên",
      need: "Khách hàng muốn được tư vấn gói dịch vụ trọn gói.",
    },
    note: "Gia đình cần hỗ trợ tư vấn sớm.",
  },
  {
    id: 3,
    category: "system",
    type: "system",
    icon: "fa-regular fa-bell",
    title: "Cập nhật hệ thống",
    desc: "Hệ thống vừa cập nhật chính sách vận chuyển mới.",
    actionText: "Thông báo hệ thống",
    time: "24/05/2024 - 09:00",
    isNew: false,
    customer: null,
    note: "Cập nhật chính sách vận chuyển.",
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
  if (item.category !== "customer") {
    selectedNotification.value = null;
    return;
  }

  item.isNew = false;
  selectedNotification.value = item;
};

const acceptCustomer = () => {
  alert("Đã tiếp nhận khách hàng!");
  selectedNotification.value = null;
};

const rejectCustomer = () => {
  alert("Đã từ chối khách hàng!");
  selectedNotification.value = null;
};
</script>

<style scoped src="../../assets/styles/TrangThongBaoNhanVienTrucTiep.css"></style>