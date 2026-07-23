<template>
  <div class="page-container">
    <h4>Quản lý đơn hàng</h4>
    <div v-if="loading" class="loading-state">
      <i class="fa-solid fa-spinner fa-spin loading-icon"></i>
      <p>Đang tải...</p>
    </div>
    <div v-else-if="donHangs.length === 0" class="empty-state">
      <i class="fa-solid fa-cart-shopping empty-icon"></i>
      <p>Chưa có dữ liệu</p>
    </div>
    <div v-else class="don-hang-list">
      <div
        v-for="donHang in donHangs"
        :key="donHang.MaDonHang"
        class="don-hang-card"
      >
        <div class="don-hang-header">
          <span class="don-hang-code">{{ donHang.maCode }}</span>
          <span class="don-hang-status" :class="getStatusClass(donHang.trangThai)">
            {{ getStatusText(donHang.trangThai) }}
          </span>
        </div>
        <div class="don-hang-info">
          <p><strong>Khách hàng:</strong> {{ donHang.tenKhachHang }}</p>
          <p><strong>SĐT:</strong> {{ donHang.soDienThoaiKH }}</p>
          <p><strong>Tổng tiền:</strong> {{ formatCurrency(donHang.tongTien) }}</p>
          <p><strong>Ngày tạo:</strong> {{ donHang.NgayTaoDon }}</p>
        </div>
        <div class="don-hang-actions">
          <button
            v-if="donHang.trangThai === 1"
            class="btn btn-primary btn-sm"
            @click="guiDonChoDoiTac(donHang.MaDonHang)"
          >
            Xác nhận gửi
          </button>
          <button
            v-if="donHang.trangThai === 3 && !donHang.daCoHopDong"
            class="btn btn-success btn-sm"
            @click="taoHopDong(donHang.MaDonHang)"
          >
            Tạo hợp đồng
          </button>
          <button
            v-if="donHang.trangThai === 9"
            class="btn btn-warning btn-sm"
            @click="thanhToanDonHang(donHang.MaDonHang)"
          >
            Thanh toán
          </button>
          <button
            v-if="donHang.trangThai === 10"
            class="btn btn-secondary btn-sm"
            disabled
          >
            Đã thanh toán
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import api from "../../api/api.js";

const router = useRouter();
const donHangs = ref([]);
const loading = ref(false);

const fetchDonHangs = async () => {
  loading.value = true;
  try {
    const response = await api.get("/api/don-hang");
    donHangs.value = response.data || [];
  } catch (error) {
    console.error("Lỗi khi tải đơn hàng:", error);
    donHangs.value = [];
  } finally {
    loading.value = false;
  }
};

const guiDonChoDoiTac = async (maDonHang) => {
  try {
    await api.put(`/api/don-hang/${maDonHang}/gui-doi-tac`);
    alert("Đã gửi đơn hàng cho đối tác");
    await fetchDonHangs();
  } catch (error) {
    console.error("Lỗi khi gửi đơn:", error);
    alert(error.response?.data?.message || "Không thể gửi đơn hàng");
  }
};

const taoHopDong = (maDonHang) => {
  // Điều hướng tới trang quản lý hợp đồng với tham số maDonHang
  router.push({
    path: '/nhan-vien/hop-dong',
    query: { maDonHang: maDonHang }
  });
};

const thanhToanDonHang = async (maDonHang) => {
  try {
    await api.put(`/api/don-hang/${maDonHang}/thanh-toan`);
    alert("Đã thanh toán đơn hàng");
    await fetchDonHangs();
  } catch (error) {
    console.error("Lỗi khi thanh toán:", error);
    alert(error.response?.data?.message || "Không thể thanh toán");
  }
};

const getStatusText = (trangThai) => {
  const statusMap = {
    1: "Mới tạo",
    2: "Chờ đối tác xác nhận",
    3: "Đã xác nhận",
    4: "Đang xử lý",
    5: "Chờ thanh toán",
    6: "Hoàn thành",
    7: "Đã hủy",
    8: "Đối tác từ chối",
    9: "Đã giao",
    10: "Đã thanh toán"
  };
  return statusMap[trangThai] || "Không rõ";
};

const getStatusClass = (trangThai) => {
  const classMap = {
    1: "status-new",
    2: "status-pending",
    3: "status-confirmed",
    4: "status-processing",
    5: "status-waiting-payment",
    6: "status-completed",
    7: "status-cancelled",
    8: "status-rejected",
    9: "status-delivered",
    10: "status-paid"
  };
  return classMap[trangThai] || "status-unknown";
};

const formatCurrency = (value) => {
  if (!value) return "0 đ";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND"
  }).format(value);
};

onMounted(() => {
  fetchDonHangs();
});
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #6b7280;
}

.loading-icon,
.empty-icon {
  font-size: 48px;
  color: #d1d5db;
  margin-bottom: 16px;
}

.don-hang-list {
  display: grid;
  gap: 16px;
}

.don-hang-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  background: white;
}

.don-hang-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.don-hang-code {
  font-weight: bold;
  font-size: 16px;
}

.don-hang-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-new { background: #fef3c7; color: #92400e; }
.status-pending { background: #fce7f3; color: #9d174d; }
.status-confirmed { background: #dbeafe; color: #1e40af; }
.status-processing { background: #ffedd5; color: #9a3412; }
.status-waiting-payment { background: #e0e7ff; color: #3730a3; }
.status-completed { background: #d1fae5; color: #065f46; }
.status-cancelled { background: #fee2e2; color: #991b1b; }
.status-rejected { background: #f3f4f6; color: #374151; }
.status-delivered { background: #ccfbf1; color: #0f766e; }
.status-paid { background: #d1fae5; color: #065f46; }
.status-unknown { background: #f3f4f6; color: #6b7280; }

.don-hang-info {
  margin-bottom: 12px;
}

.don-hang-info p {
  margin: 4px 0;
  color: #374151;
}

.don-hang-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.btn {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  border: none;
}

.btn-primary { background: #3b82f6; color: white; }
.btn-success { background: #10b981; color: white; }
.btn-warning { background: #f59e0b; color: white; }
.btn-secondary { background: #6b7280; color: white; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
