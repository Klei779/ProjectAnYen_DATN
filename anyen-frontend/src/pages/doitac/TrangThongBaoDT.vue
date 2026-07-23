  <template>
  <div class="notification-page">
    <section class="notification-left">
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
          <div
              class="noti-icon"
              :class="[
                item.type,
                {
                  approved: item.category === 'product' && isProductApproved(item),
                  rejected: item.category === 'product' && isProductRejected(item)
                }
              ]"
          >
            <i :class="item.icon"></i>
          </div>

          <div class="noti-content">
            <div class="noti-head">
              <h4>{{ item.title }}</h4>
              <span v-if="item.isNew">Mới</span>
            </div>

            <p>{{ cleanNotificationContent(item.desc) }}</p>
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
          </button>
        </div>

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
      </aside>

      <!-- FORM KẾT QUẢ DUYỆT SẢN PHẨM -->
      <aside
          v-else-if="selectedNotification.category === 'product'"
          class="order-detail"
      >
        <div class="detail-header">
          <h3>Chi tiết duyệt sản phẩm</h3>
          <button type="button" @click="closePopup">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <div
            class="alert-box product-result-alert"
            :class="{
              approved: isProductApproved(selectedNotification),
              rejected: isProductRejected(selectedNotification)
            }"
        >
          <i
              :class="isProductApproved(selectedNotification)
                ? 'fa-solid fa-circle-check'
                : 'fa-solid fa-circle-xmark'"
          ></i>
          <div>
            <strong>{{ selectedNotification.title }}</strong>
            <p>{{ getProductResultMessage(selectedNotification) }}</p>
          </div>
        </div>

        <div class="detail-section">
          <h4>Thông tin sản phẩm</h4>

          <div class="product-line product-review-line">
            <img
                :src="getProductImage(selectedNotification.product?.image)"
                :alt="selectedNotification.product?.name || 'Sản phẩm'"
                @error="handleImageError"
            />

            <div>
              <h5>{{ selectedNotification.product?.name || 'Không tìm thấy sản phẩm' }}</h5>
              <p>{{ selectedNotification.product?.desc || 'Chưa có phân loại' }}</p>
            </div>

            <div class="product-price">
              <span>
                {{ selectedNotification.product?.id
                  ? `#SP${selectedNotification.product.id}`
                  : 'Chưa có mã' }}
              </span>
              <b>{{ formatPrice(selectedNotification.product?.price) }}</b>
            </div>
          </div>

          <div class="info-row product-info-row">
            <span>Số lượng tồn kho</span>
            <b>{{ selectedNotification.product?.quantity ?? 0 }}</b>
          </div>

          <div class="info-row">
            <span>Trạng thái duyệt</span>
            <em
                class="product-status-badge"
                :class="{
                  approved: isProductApproved(selectedNotification),
                  rejected: isProductRejected(selectedNotification)
                }"
            >
              {{ getTrangThaiThongBaoText(selectedNotification.trangThaiThongBao) }}
            </em>
          </div>
        </div>


        <div class="detail-section">
          <h4>Nội dung thông báo</h4>
          <p class="note">
            {{ cleanNotificationContent(selectedNotification.desc) }}
          </p>
        </div>

        <div class="detail-actions">
          <button class="accept-btn" type="button" @click="closePopup">
            Đóng
          </button>
        </div>

        <p class="hint">
          <i class="fa-solid fa-circle-info"></i>
          Sản phẩm vẫn được giữ trong Quản lý sản phẩm để đối tác theo dõi và chỉnh sửa.
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
          
          <!-- Nút tạo hợp đồng cho thông báo yêu cầu tạo hợp đồng -->
          <button 
            v-if="selectedNotification.system?.title === 'Đã tạo hợp đồng'" 
            class="accept-btn" 
            @click="goToContractPage"
            style="margin-left: 8px;"
          >
            Xem hợp đồng
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

import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import {
  getThongBaoDoiTac,
  chapNhanThongBao,
  tuChoiThongBao
} from "../../services/thongBaoDoiTacService.js";
import "../../assets/styles/doitac/QLThongBao/TrangThongBaoDT.css";
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

const activeTab = ref("all");
const selectedNotification = ref(null);
const notifications = ref([]);
const TT_DT_CHO_XAC_NHAN = "CHO_XAC_NHAN";
const TT_DT_DA_CHAP_NHAN = "DA_CHAP_NHAN";
const TT_DT_DA_TU_CHOI = "DA_TU_CHOI";

const normalizeNotificationStatus = (status) => {
  const raw = String(status ?? "").trim().toUpperCase();

  const legacyStatusMap = {
    "0": TT_DT_CHO_XAC_NHAN,
    "1": TT_DT_DA_CHAP_NHAN,
    "2": TT_DT_DA_TU_CHOI,
  };

  return legacyStatusMap[raw] || raw;
};

const isChoXacNhan = (item) =>
    normalizeNotificationStatus(item?.trangThaiThongBao) === TT_DT_CHO_XAC_NHAN;

const isProductApproved = (item) =>
    normalizeNotificationStatus(item?.trangThaiThongBao) === TT_DT_DA_CHAP_NHAN;

const isProductRejected = (item) =>
    normalizeNotificationStatus(item?.trangThaiThongBao) === TT_DT_DA_TU_CHOI;

const getTrangThaiThongBaoText = (status) => {
  const map = {
    [TT_DT_CHO_XAC_NHAN]: "Chờ xác nhận",
    [TT_DT_DA_CHAP_NHAN]: "Đã duyệt",
    [TT_DT_DA_TU_CHOI]: "Đã từ chối",
  };

  return map[normalizeNotificationStatus(status)] || "Không xác định";
};

const cleanNotificationContent = (content) =>
    String(content ?? "")
        .replace(/\s*\[MASP:\d+]/gi, "")
        .trim();

const getProductResultMessage = (item) => {
  if (isProductApproved(item)) {
    return "Sản phẩm đã được duyệt và có thể hiển thị trên hệ thống.";
  }

  if (isProductRejected(item)) {
    return "Sản phẩm chưa được duyệt. Xem lý do bên dưới để chỉnh sửa và gửi duyệt lại.";
  }

  return "Thông báo liên quan đến quá trình duyệt sản phẩm.";
};
const tabs = [
  { key: "all", label: "Tất cả" },
  { key: "order", label: "Đơn hàng" },
  { key: "product", label: "Sản phẩm" },
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
const loadThongBao = async () => {
  try {
    const data = await getThongBaoDoiTac();

    const orderNotifications = Array.isArray(data)
        ? data.map(item => ({
          ...item,
          trangThaiThongBao: normalizeNotificationStatus(item.trangThaiThongBao),
        }))
        : [];

    notifications.value = [
      ...orderNotifications,
      ...systemNotifications
    ];
  } catch (error) {
    console.error("Lỗi tải thông báo:", error);
    alert("Không thể tải danh sách thông báo");
  }
};

// Polling để reload thông báo mỗi 5 giây
let pollingInterval = null;

const startPolling = () => {
  pollingInterval = setInterval(() => {
    loadThongBao();
  }, 5000);
};

const stopPolling = () => {
  if (pollingInterval) {
    clearInterval(pollingInterval);
    pollingInterval = null;
  }
};

onMounted(() => {
  loadThongBao();
  startPolling();
});

onUnmounted(() => {
  stopPolling();
});

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
  item.isNew = false;
  selectedNotification.value = item;
};

const closePopup = () => {
  selectedNotification.value = null;
};

const formatPrice = (price) => {
  const value = Number(price || 0);

  return new Intl.NumberFormat("vi-VN").format(value) + " đ";
};

const acceptOrder = async () => {
  if (!selectedNotification.value) return;

  try {
    const maThongBao = selectedNotification.value.id;

    const result = await chapNhanThongBao(maThongBao);

    notifications.value = notifications.value.map(item => {
      if (item.id === maThongBao) {
        return {
          ...item,
          trangThaiThongBao: TT_DT_DA_CHAP_NHAN,
          isNew: false,
          actionText: "Đã chấp nhận đơn hàng",
        };
      }

      return item;
    });

    closePopup();

    await router.push(
        result.redirectUrl || "/doi-tac/quan-ly-don-hang"
    );
  } catch (error) {
    console.error("Lỗi chấp nhận đơn hàng:", error);
    alert(error.response?.data?.message || "Chấp nhận đơn hàng thất bại");
  }
};

const rejectOrder = async () => {
  if (!selectedNotification.value) return;

  const lyDo = prompt("Nhập lý do từ chối đơn hàng:");

  if (!lyDo || !lyDo.trim()) {
    alert("Vui lòng nhập lý do từ chối");
    return;
  }

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
        };
      }

      return item;
    });

    closePopup();

    alert("Đã từ chối đơn hàng");
  } catch (error) {
    console.error("Lỗi từ chối đơn hàng:", error);
    alert(error.response?.data?.message || "Từ chối đơn hàng thất bại");
  }
};

const readSystemNotification = () => {
  if (!selectedNotification.value) return;

  selectedNotification.value.isNew = false;
  closePopup();
};

// Parse maDonHang từ nội dung thông báo
const parseMaDonHangFromContent = (content) => {
  if (!content) return null;
  const match = content.match(/DH(\d+)/);
  return match ? parseInt(match[1]) : null;
};

// Điều hướng tới trang hợp đồng với đơn hàng đã chọn
const goToContractPage = () => {
  const maDonHang = parseMaDonHangFromContent(selectedNotification.value?.system?.content);
  if (maDonHang) {
    // Điều hướng tới trang quản lý đơn hàng của đối tác với filter đơn hàng đã có hợp đồng
    router.push({
      path: '/doi-tac/quan-ly-don-hang',
      query: { showContract: true, maDonHang: maDonHang }
    });
  } else {
    alert("Không tìm thấy mã đơn hàng trong thông báo");
  }
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
};
</script>