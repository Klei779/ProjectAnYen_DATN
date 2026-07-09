<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { getHoaDonCuaToi } from "../../services/hoaDonCuaToiService.js";

const loading = ref(false);
const hoaDons = ref([]);
const total = ref(0);
const isAdmin = ref(false);

const filters = ref({
  keyword: "",
  trangThai: "Tất cả",
  phuongThucThanhToan: "Tất cả",
  dateRange: [],
  page: 1,
  pageSize: 10,
});

const trangThaiOptions = [
  "Tất cả",
  "Đã in",
  "Đã thanh toán",
  "Chưa thanh toán",
  "Đã hủy",
];

const phuongThucOptions = [
  "Tất cả",
  "Tiền mặt",
  "Chuyển khoản",
  "VietQR",
  "Sepay",
];

const tongTienTrangHienTai = computed(() => {
  return hoaDons.value.reduce((sum, item) => sum + Number(item.tongTien || 0), 0);
});

const formatMoney = (value) => {
  return Number(value || 0).toLocaleString("vi-VN") + "đ";
};

const formatDate = (value) => {
  if (!value) return "---";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleDateString("vi-VN");
};

const statusClass = (status) => {
  const value = String(status || "").toLowerCase();

  if (value.includes("hủy")) return "status-cancel";
  if (value.includes("đã in") || value.includes("thanh toán")) return "status-success";
  if (value.includes("chưa")) return "status-wait";

  return "status-default";
};

const loadHoaDon = async () => {
  try {
    loading.value = true;

    const data = await getHoaDonCuaToi({
      keyword: filters.value.keyword,
      trangThai: filters.value.trangThai,
      phuongThucThanhToan: filters.value.phuongThucThanhToan,
      tuNgay: filters.value.dateRange?.[0],
      denNgay: filters.value.dateRange?.[1],
      page: filters.value.page,
      pageSize: filters.value.pageSize,
    });

    hoaDons.value = data.items;
    total.value = data.total;
    isAdmin.value = data.admin;
  } catch (error) {
    console.error("Lỗi tải hóa đơn của tôi:", error);
    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Không thể tải danh sách hóa đơn"
    );
  } finally {
    loading.value = false;
  }
};

const searchHoaDon = () => {
  filters.value.page = 1;
  loadHoaDon();
};

const resetFilter = () => {
  filters.value.keyword = "";
  filters.value.trangThai = "Tất cả";
  filters.value.phuongThucThanhToan = "Tất cả";
  filters.value.dateRange = [];
  filters.value.page = 1;
  loadHoaDon();
};

const handleSizeChange = (size) => {
  filters.value.pageSize = size;
  filters.value.page = 1;
  loadHoaDon();
};

const handlePageChange = (page) => {
  filters.value.page = page;
  loadHoaDon();
};

onMounted(() => {
  loadHoaDon();
});
</script>

<template>
  <div class="hoa-don-page">
    <div class="page-header">
      <div>
        <span class="page-subtitle">Quản lý hóa đơn</span>
        <h2 class="page-title">
          {{ isAdmin ? "Tất cả hóa đơn" : "Hóa đơn của tôi" }}
        </h2>
        <p class="page-desc">
          {{ isAdmin
            ? "Admin có thể xem toàn bộ hóa đơn đã phát sinh trong hệ thống."
            : "Nhân viên chỉ xem được hóa đơn thuộc các đơn hàng do mình phụ trách." }}
        </p>
      </div>

      <button class="btn-refresh" :disabled="loading" @click="loadHoaDon">
        <i class="fa-solid fa-rotate-right"></i>
        Làm mới
      </button>
    </div>

    <div class="summary-grid">
      <div class="summary-card">
        <div class="summary-icon">
          <i class="fa-solid fa-file-invoice"></i>
        </div>
        <p>Tổng hóa đơn</p>
        <h3>{{ total }}</h3>
      </div>

      <div class="summary-card money">
        <div class="summary-icon">
          <i class="fa-solid fa-sack-dollar"></i>
        </div>
        <p>Tổng tiền trang hiện tại</p>
        <h3>{{ formatMoney(tongTienTrangHienTai) }}</h3>
      </div>

      <div class="summary-card">
        <div class="summary-icon">
          <i class="fa-solid fa-user-shield"></i>
        </div>
        <p>Quyền xem</p>
        <h3>{{ isAdmin ? "Admin" : "Của tôi" }}</h3>
      </div>
    </div>

    <div class="filter-card">
      <div class="filter-item keyword">
        <label>Tìm kiếm</label>
        <el-input
            v-model="filters.keyword"
            placeholder="Mã hóa đơn, mã đơn, khách hàng, SĐT..."
            clearable
            @keyup.enter="searchHoaDon"
        >
          <template #prefix>
            <i class="fa-solid fa-magnifying-glass"></i>
          </template>
        </el-input>
      </div>

      <div class="filter-item">
        <label>Trạng thái</label>
        <el-select v-model="filters.trangThai">
          <el-option
              v-for="item in trangThaiOptions"
              :key="item"
              :label="item"
              :value="item"
          />
        </el-select>
      </div>

      <div class="filter-item">
        <label>Thanh toán</label>
        <el-select v-model="filters.phuongThucThanhToan">
          <el-option
              v-for="item in phuongThucOptions"
              :key="item"
              :label="item"
              :value="item"
          />
        </el-select>
      </div>

      <div class="filter-item date">
        <label>Ngày in</label>
        <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="Từ ngày"
            end-placeholder="Đến ngày"
            format="DD/MM/YYYY"
            value-format="YYYY-MM-DD"
        />
      </div>

      <div class="filter-actions">
        <button class="btn-soft" @click="resetFilter">
          <i class="fa-solid fa-eraser"></i>
          Xóa lọc
        </button>

        <button class="btn-primary" @click="searchHoaDon">
          <i class="fa-solid fa-filter"></i>
          Lọc
        </button>
      </div>
    </div>

    <div class="table-card" v-loading="loading">
      <div class="table-header">
        <div>
          <h3>Danh sách hóa đơn</h3>
          <p>Hiển thị {{ hoaDons.length }} / {{ total }} hóa đơn</p>
        </div>
      </div>

      <div class="table-wrap">
        <table class="hoa-don-table">
          <thead>
          <tr>
            <th>Mã hóa đơn</th>
            <th>Mã đơn</th>
            <th>Khách hàng</th>
            <th>Nhân viên</th>
            <th>Ngày in</th>
            <th>Phương thức</th>
            <th>Trạng thái</th>
            <th>Tổng tiền</th>
          </tr>
          </thead>

          <tbody>
          <tr v-if="hoaDons.length === 0">
            <td colspan="8" class="empty-cell">
              Không có hóa đơn phù hợp.
            </td>
          </tr>

          <tr v-for="item in hoaDons" :key="item.maHoaDon">
            <td>
              <div class="code-main">{{ item.soHoaDon }}</div>
              <small>#{{ item.maHoaDon }}</small>
            </td>

            <td>
              <div class="code-main">{{ item.maDonHangCode }}</div>
              <small>#{{ item.maDonHang }}</small>
            </td>

            <td>
              <div class="customer-name">{{ item.tenKhachHang || "---" }}</div>
              <small>{{ item.soDienThoai || "---" }}</small>
            </td>

            <td>{{ item.tenNhanVien || "---" }}</td>

            <td>{{ formatDate(item.ngayIn) }}</td>

            <td>
              <span class="payment-badge">
                {{ item.phuongThucThanhToan || "Chưa cập nhật" }}
              </span>
            </td>

            <td>
              <span class="status-badge" :class="statusClass(item.trangThai)">
                {{ item.trangThai || "Chưa cập nhật" }}
              </span>
            </td>

            <td class="money-cell">
              {{ formatMoney(item.tongTien) }}
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-wrap">
        <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :total="total"
            :current-page="filters.page"
            :page-size="filters.pageSize"
            :page-sizes="[5, 10, 20, 50]"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped src="../../assets/styles/nhanvien/QLHoaDon/TrangHoaDonCuaToi.css"></style>