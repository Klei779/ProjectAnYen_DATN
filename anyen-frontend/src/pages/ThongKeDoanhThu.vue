<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { getThongKeDoanhThu } from "../services/thongKeDoanhThuService.js";

const props = defineProps({
  loaiTaiKhoan: {
    type: String,
    default: "NHAN_VIEN",
  },
});

const loading = ref(false);
const dateRange = ref([]);
const kieuThongKe = ref("NGAY");

const thongKe = ref({
  tongQuan: {
    tongDoanhThu: 0,
    tongHoaDon: 0,
    tongDonHang: 0,
    doanhThuTrungBinh: 0,
  },
  bieuDoDoanhThu: [],
  topSanPham: [],
  phuongThucThanhToan: [],
});

const isDoiTac = computed(() => props.loaiTaiKhoan === "DOI_TAC");

const pageInfo = computed(() => {
  if (isDoiTac.value) {
    return {
      subtitle: "Đối tác An Yên",
      title: "Thống kê doanh thu đối tác",
      desc: "Theo dõi doanh thu từ các sản phẩm của đối tác đã phát sinh trong hóa đơn.",
      note: "Doanh thu đối tác được tính theo sản phẩm thuộc đối tác trong từng hóa đơn.",
    };
  }

  return {
    subtitle: "Nhân viên An Yên",
    title: "Thống kê doanh thu",
    desc: "Theo dõi doanh thu theo hóa đơn, sản phẩm bán chạy và phương thức thanh toán.",
    note: "Doanh thu nhân viên được tính theo tổng tiền hóa đơn đã lập.",
  };
});

const maxRevenue = computed(() => {
  const values = thongKe.value.bieuDoDoanhThu.map((item) => item.doanhThu);
  return Math.max(...values, 0);
});

const hasChartData = computed(() => thongKe.value.bieuDoDoanhThu.length > 0);

const chartRows = computed(() => {
  return thongKe.value.bieuDoDoanhThu.map((item) => ({
    ...item,
    width: maxRevenue.value > 0
        ? Math.max((item.doanhThu / maxRevenue.value) * 100, 4)
        : 0,
  }));
});

const totalPaymentRevenue = computed(() => {
  return thongKe.value.phuongThucThanhToan.reduce((sum, item) => {
    return sum + Number(item.doanhThu || 0);
  }, 0);
});

const paymentRows = computed(() => {
  return thongKe.value.phuongThucThanhToan.map((item) => ({
    ...item,
    percent: totalPaymentRevenue.value > 0
        ? Math.round((item.doanhThu / totalPaymentRevenue.value) * 100)
        : 0,
  }));
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

const formatPeriod = (value) => {
  if (!value) return "---";

  if (kieuThongKe.value === "NAM") {
    return `Năm ${value}`;
  }

  if (kieuThongKe.value === "THANG") {
    const [year, month] = String(value).split("-");
    return `Tháng ${month}/${year}`;
  }

  return formatDate(value);
};

const toDateString = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const setThisMonth = () => {
  const now = new Date();
  const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
  dateRange.value = [toDateString(firstDay), toDateString(now)];
  kieuThongKe.value = "NGAY";
  loadThongKe();
};

const setThisYear = () => {
  const now = new Date();
  const firstDay = new Date(now.getFullYear(), 0, 1);
  dateRange.value = [toDateString(firstDay), toDateString(now)];
  kieuThongKe.value = "THANG";
  loadThongKe();
};

const loadThongKe = async () => {
  try {
    loading.value = true;

    const data = await getThongKeDoanhThu(props.loaiTaiKhoan, {
      tuNgay: dateRange.value?.[0],
      denNgay: dateRange.value?.[1],
      kieuThongKe: kieuThongKe.value,
    });

    thongKe.value = data;
  } catch (error) {
    console.error("Lỗi tải thống kê doanh thu:", error);
    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Không thể tải thống kê doanh thu"
    );
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  setThisMonth();
});
</script>

<template>
  <div class="thong-ke-page">
    <div class="page-header">
      <div>
        <span class="page-subtitle">{{ pageInfo.subtitle }}</span>
        <h2 class="page-title">{{ pageInfo.title }}</h2>
        <p class="page-desc">{{ pageInfo.desc }}</p>
      </div>

      <button class="btn-refresh" :disabled="loading" @click="loadThongKe">
        <i class="fa-solid fa-rotate-right"></i>
        Làm mới
      </button>
    </div>

    <div class="filter-card">
      <div class="filter-group">
        <label>Khoảng thời gian</label>
        <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="Từ ngày"
            end-placeholder="Đến ngày"
            format="DD/MM/YYYY"
            value-format="YYYY-MM-DD"
            class="date-range"
        />
      </div>

      <div class="filter-group small">
        <label>Hiển thị theo</label>
        <el-select v-model="kieuThongKe" class="select-type">
          <el-option label="Theo ngày" value="NGAY" />
          <el-option label="Theo tháng" value="THANG" />
          <el-option label="Theo năm" value="NAM" />
        </el-select>
      </div>

      <div class="filter-actions">
        <button class="btn-soft" @click="setThisMonth">Tháng này</button>
        <button class="btn-soft" @click="setThisYear">Năm này</button>
        <button class="btn-primary" :disabled="loading" @click="loadThongKe">
          <i class="fa-solid fa-filter"></i>
          Lọc doanh thu
        </button>
      </div>
    </div>

    <div class="range-note">
      <i class="fa-solid fa-circle-info"></i>
      {{ pageInfo.note }} Khoảng lọc:
      <strong>{{ formatDate(thongKe.tuNgay) }}</strong>
      đến
      <strong>{{ formatDate(thongKe.denNgay) }}</strong>
    </div>

    <div class="summary-grid" v-loading="loading">
      <div class="summary-card revenue">
        <div class="summary-icon">
          <i class="fa-solid fa-sack-dollar"></i>
        </div>
        <p>Tổng doanh thu</p>
        <h3>{{ formatMoney(thongKe.tongQuan.tongDoanhThu) }}</h3>
      </div>

      <div class="summary-card">
        <div class="summary-icon">
          <i class="fa-solid fa-file-invoice"></i>
        </div>
        <p>Tổng hóa đơn</p>
        <h3>{{ thongKe.tongQuan.tongHoaDon }}</h3>
      </div>

      <div class="summary-card">
        <div class="summary-icon">
          <i class="fa-solid fa-cart-shopping"></i>
        </div>
        <p>Tổng đơn hàng</p>
        <h3>{{ thongKe.tongQuan.tongDonHang }}</h3>
      </div>

      <div class="summary-card average">
        <div class="summary-icon">
          <i class="fa-solid fa-chart-line"></i>
        </div>
        <p>Trung bình/đơn</p>
        <h3>{{ formatMoney(thongKe.tongQuan.doanhThuTrungBinh) }}</h3>
      </div>
    </div>

    <div class="content-grid">
      <div class="panel chart-panel" v-loading="loading">
        <div class="panel-header">
          <div>
            <h3>Biểu đồ doanh thu</h3>
            <p>Doanh thu được nhóm theo ngày, tháng hoặc năm</p>
          </div>
        </div>

        <div v-if="!hasChartData" class="empty-state">
          Không có dữ liệu doanh thu trong khoảng thời gian này.
        </div>

        <div v-else class="bar-chart">
          <div v-for="item in chartRows" :key="item.thoiGian" class="bar-row">
            <div class="bar-label">{{ formatPeriod(item.thoiGian) }}</div>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: item.width + '%' }"></div>
            </div>
            <div class="bar-value">
              <strong>{{ formatMoney(item.doanhThu) }}</strong>
              <span>{{ item.soDonHang }} đơn</span>
            </div>
          </div>
        </div>
      </div>

      <div class="panel" v-loading="loading">
        <div class="panel-header">
          <div>
            <h3>Phương thức thanh toán</h3>
            <p>Tỷ trọng doanh thu theo hình thức thanh toán</p>
          </div>
        </div>

        <div v-if="paymentRows.length === 0" class="empty-state small-empty">
          Chưa có dữ liệu thanh toán.
        </div>

        <div v-else class="payment-list">
          <div
              v-for="item in paymentRows"
              :key="item.phuongThucThanhToan"
              class="payment-item"
          >
            <div class="payment-top">
              <strong>{{ item.phuongThucThanhToan }}</strong>
              <span>{{ item.percent }}%</span>
            </div>
            <div class="payment-track">
              <div class="payment-fill" :style="{ width: item.percent + '%' }"></div>
            </div>
            <div class="payment-bottom">
              <span>{{ item.soHoaDon }} hóa đơn</span>
              <b>{{ formatMoney(item.doanhThu) }}</b>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="panel table-panel" v-loading="loading">
      <div class="panel-header">
        <div>
          <h3>Top sản phẩm có doanh thu cao</h3>
          <p>{{ isDoiTac ? 'Chỉ hiển thị sản phẩm thuộc đối tác đang đăng nhập' : 'Tính theo chi tiết sản phẩm trong các hóa đơn' }}</p>
        </div>
      </div>

      <div class="table-wrap">
        <table class="stat-table">
          <thead>
          <tr>
            <th>#</th>
            <th>Sản phẩm</th>
            <th>Số lượng bán</th>
            <th>Doanh thu</th>
          </tr>
          </thead>

          <tbody>
          <tr v-if="thongKe.topSanPham.length === 0">
            <td colspan="4" class="empty-cell">Không có dữ liệu sản phẩm.</td>
          </tr>

          <tr v-for="(item, index) in thongKe.topSanPham" :key="item.maSanPham || item.tenSanPham">
            <td>
              <span class="rank-badge">{{ index + 1 }}</span>
            </td>
            <td>
              <div class="product-name">{{ item.tenSanPham }}</div>
              <small>SP{{ String(item.maSanPham || 0).padStart(4, '0') }}</small>
            </td>
            <td>{{ item.soLuongBan }}</td>
            <td class="money-cell">{{ formatMoney(item.doanhThu) }}</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped src="../assets/styles/thongke/ThongKeDoanhThu.css"></style>