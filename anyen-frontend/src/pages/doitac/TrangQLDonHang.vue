<script setup>
import { ref, onMounted } from 'vue';
import { getDonHangsDoiTac } from '../../services/doitacDonHangService.js';
import PopChiTietDonHang from './PopChiTietDonHang.vue';
import { Search, Filter, View } from "@element-plus/icons-vue";

const donHangs = ref([]);
const showPopup = ref(false);
const selectedDonHang = ref(null);

const fetchDonHangs = async () => {
  try {
    const data = await getDonHangsDoiTac();
    donHangs.value = data.items;
  } catch (error) {
    console.error("Lỗi khi fetch đơn hàng:", error);
  }
};

onMounted(() => {
  fetchDonHangs();
});

const openChiTiet = (dh) => {
  selectedDonHang.value = dh;
  showPopup.value = true;
};
</script>

<template>
  <div class="partner-don-hang-page">
    <div class="page-header">
      <div class="page-title-wrap">
        <h2 class="page-title">Quản lý đơn hàng</h2>
      </div>
    </div>

    <!-- ── Bảng đơn hàng ── -->
    <div class="table-card">
      <table class="dh-table">
        <thead>
          <tr>
            <th>Mã đơn hàng</th>
            <th>Khách hàng</th>
            <th>Ngày đặt</th>
            <th>Tổng tiền</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="donHangs.length === 0">
            <td colspan="5" class="empty-row">Không có đơn hàng nào.</td>
          </tr>
          <tr
            v-for="dh in donHangs"
            :key="dh.maCode"
            class="dh-row"
          >
            <td class="col-code">
              <span class="code-text">#{{ dh.maCode }}</span>
            </td>
            <td>
              <div class="kh-cell">
                <div>
                  <p class="kh-name">{{ dh.tenKhachHang }}</p>
                  <p class="kh-phone">{{ dh.soDienThoai }}</p>
                </div>
              </div>
            </td>
            <td>{{ dh.ngayDat }}</td>
            <td class="col-tien">{{ dh.tongCong?.toLocaleString('vi-VN') }}đ</td>
            <td class="col-action">
              <el-button size="small" type="primary" plain @click="openChiTiet(dh)">
                <el-icon><View /></el-icon>
                Xem chi tiết
              </el-button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Popup Chi tiết -->
    <PopChiTietDonHang v-if="showPopup" v-model="showPopup" :don-hang="selectedDonHang" />
  </div>
</template>

<style scoped>
.partner-don-hang-page {
  padding: 24px 28px;
  min-height: 100vh;
  background: #f4f6f8;
  font-family: 'Inter', Arial, sans-serif;
}

.page-header {
  margin-bottom: 20px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}

/* ── Table ── */
.table-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.dh-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.dh-table th {
  background: #f8f9fa;
  color: #495057;
  font-weight: 600;
  padding: 16px;
  border-bottom: 1px solid #e9ecef;
  text-align: left;
}
.dh-table td {
  padding: 16px;
  border-bottom: 1px solid #f1f3f5;
  color: #212529;
  vertical-align: middle;
}
.dh-row {
  transition: background 0.15s;
}
.dh-row:hover {
  background: #f8f9fa;
}

/* Cells */
.col-code { width: 150px; }
.col-tien { font-weight: 600; color: #d32f2f; }
.col-action { width: 120px; }

.code-text {
  font-weight: 700;
  color: #1a1a2e;
}
.kh-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.kh-name {
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 4px;
}
.kh-phone {
  font-size: 13px;
  color: #6c757d;
  margin: 0;
}
</style>