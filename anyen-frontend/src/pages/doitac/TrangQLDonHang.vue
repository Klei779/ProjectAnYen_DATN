<script setup>
import { ref, onMounted, computed } from "vue";
import { getDonHangsDoiTac } from "../../services/doitacDonHangService.js";
import PopChiTietDonHang from "./PopChiTietDonHang.vue";
import { Search, Filter, View } from "@element-plus/icons-vue";

const donHangs = ref([]);
const showPopup = ref(false);
const selectedDonHang = ref(null);

const loading = ref(false);
const keyword = ref("");
const statusFilter = ref("");
const sortMode = ref("newest");

const normalizeDonHang = (dh) => {
  return {
    ...dh,
    maCode: dh.maCode || dh.maDonHang || dh.id,
    tenKhachHang:
        dh.tenKhachHang ||
        dh.khachHang?.tenKhachHang ||
        dh.customerName ||
        "---",
    soDienThoai:
        dh.soDienThoai ||
        dh.khachHang?.soDienThoai ||
        dh.phone ||
        "Chưa có SĐT",
    ngayDat:
        dh.ngayDat ||
        dh.ngayTaoDon ||
        dh.createdAt ||
        "---",
    tongCong:
        dh.tongCong ||
        dh.tongTien ||
        dh.total ||
        0,
    trangThai:
        dh.trangThai ||
        dh.trangThaiDonHang ||
        dh.status ||
        "Đơn mới",
  };
};

const fetchDonHangs = async () => {
  loading.value = true;

  try {
    const data = await getDonHangsDoiTac({
      keyword: keyword.value,
      trangThai: statusFilter.value || "Tất cả",
      page: 1,
      pageSize: 100
    });

    const items = Array.isArray(data)
        ? data
        : (data.items || []);

    donHangs.value = items.map(normalizeDonHang);

    console.log("Đơn hàng đối tác sau khi lọc backend:", donHangs.value);
  } catch (error) {
    console.error("Lỗi khi fetch đơn hàng:", error);
    donHangs.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchDonHangs();
});

const openChiTiet = (dh) => {
  selectedDonHang.value = dh;
  showPopup.value = true;
};

const getStatus = (dh) => {
  return dh.trangThai || dh.trangThaiDonHang || dh.status || "Đơn mới";
};

const getStatusClass = (status) => {
  const s = String(status || "").toLowerCase();

  if (s.includes("hoàn") || s.includes("xong")) return "success";
  if (s.includes("hủy") || s.includes("huỷ") || s.includes("từ chối")) return "danger";
  if (s.includes("đang") || s.includes("xử lý") || s.includes("chờ")) return "warning";

  return "primary";
};

const formatMoney = (value) => {
  const number = Number(value || 0);
  return number.toLocaleString("vi-VN") + "đ";
};

const parseDateValue = (date) => {
  if (!date || date === "---") return 0;

  if (String(date).includes("/")) {
    const [day, month, year] = String(date).split("/");
    return new Date(`${year}-${month}-${day}`).getTime();
  }

  return new Date(date).getTime();
};

const statusOptions = computed(() => {
  const set = new Set();

  donHangs.value.forEach((dh) => {
    set.add(getStatus(dh));
  });

  return Array.from(set);
});

const filteredDonHangs = computed(() => {
  const kw = keyword.value.trim().toLowerCase();

  let result = donHangs.value.filter((dh) => {
    const matchKeyword =
        !kw ||
        String(dh.maCode || "").toLowerCase().includes(kw) ||
        String(dh.tenKhachHang || "").toLowerCase().includes(kw) ||
        String(dh.soDienThoai || "").toLowerCase().includes(kw);

    const matchStatus =
        !statusFilter.value ||
        getStatus(dh) === statusFilter.value;

    return matchKeyword && matchStatus;
  });

  result = [...result].sort((a, b) => {
    if (sortMode.value === "newest") {
      return parseDateValue(b.ngayDat) - parseDateValue(a.ngayDat);
    }

    if (sortMode.value === "oldest") {
      return parseDateValue(a.ngayDat) - parseDateValue(b.ngayDat);
    }

    if (sortMode.value === "priceDesc") {
      return Number(b.tongCong || 0) - Number(a.tongCong || 0);
    }

    if (sortMode.value === "priceAsc") {
      return Number(a.tongCong || 0) - Number(b.tongCong || 0);
    }

    return 0;
  });

  return result;
});

const totalRevenue = computed(() => {
  return donHangs.value.reduce((sum, dh) => {
    return sum + Number(dh.tongCong || 0);
  }, 0);
});

const pendingCount = computed(() => {
  return donHangs.value.filter((dh) => {
    const s = getStatus(dh).toLowerCase();
    return s.includes("mới") || s.includes("chờ") || s.includes("đang");
  }).length;
});

const completedCount = computed(() => {
  return donHangs.value.filter((dh) => {
    const s = getStatus(dh).toLowerCase();
    return s.includes("hoàn") || s.includes("xong");
  }).length;
});
</script>

<template>
  <div class="partner-don-hang-page">
    <!-- SUMMARY -->
    <div class="summary-grid">
      <div class="summary-card">
        <p>Tổng đơn hàng</p>
        <h3>{{ donHangs.length }}</h3>
      </div>

      <div class="summary-card">
        <p>Đơn đang xử lý</p>
        <h3>{{ pendingCount }}</h3>
      </div>

      <div class="summary-card">
        <p>Đơn hoàn thành</p>
        <h3>{{ completedCount }}</h3>
      </div>

      <div class="summary-card money">
        <p>Tổng doanh thu</p>
        <h3>{{ formatMoney(totalRevenue) }}</h3>
      </div>
    </div>

    <!-- FILTER -->
    <div class="filter-card">
      <div class="search-box">
        <el-input
            v-model="keyword"
            clearable
            placeholder="Tìm theo mã đơn, tên khách hàng hoặc số điện thoại..."
        >
          <template #prefix>
            <el-icon>
              <Search />
            </el-icon>
          </template>
        </el-input>
      </div>

      <div class="filter-actions">
        <el-select
            v-model="statusFilter"
            clearable
            placeholder="Trạng thái"
            class="filter-select"
        >
          <template #prefix>
            <el-icon>
              <Filter />
            </el-icon>
          </template>

          <el-option
              v-for="status in statusOptions"
              :key="status"
              :label="status"
              :value="status"
          />
        </el-select>

        <el-select
            v-model="sortMode"
            class="sort-select"
            placeholder="Sắp xếp"
        >
          <el-option label="Mới nhất" value="newest" />
          <el-option label="Cũ nhất" value="oldest" />
          <el-option label="Tổng tiền cao nhất" value="priceDesc" />
          <el-option label="Tổng tiền thấp nhất" value="priceAsc" />
        </el-select>
      </div>
    </div>

    <!-- TABLE -->
    <div class="table-card">
      <table class="dh-table">
        <thead>
        <tr>
          <th>Mã đơn hàng</th>
          <th>Khách hàng</th>
          <th>Ngày đặt</th>
          <th>Trạng thái</th>
          <th>Tổng tiền</th>
          <th>Thao tác</th>
        </tr>
        </thead>

        <tbody>
        <tr v-if="loading">
          <td colspan="6" class="empty-row">
            Đang tải đơn hàng...
          </td>
        </tr>

        <tr v-else-if="filteredDonHangs.length === 0">
          <td colspan="6" class="empty-row">
            Không có đơn hàng nào.
          </td>
        </tr>

        <tr
            v-else
            v-for="dh in filteredDonHangs"
            :key="dh.maCode || dh.maDonHang"
            class="dh-row"
        >
          <td class="col-code">
            <span class="code-text">#{{ dh.maCode || dh.maDonHang }}</span>
          </td>

          <td>
            <div class="kh-cell">
              <div class="avatar-circle">
                {{ (dh.tenKhachHang || "K").charAt(0) }}
              </div>

              <div>
                <p class="kh-name">{{ dh.tenKhachHang || "---" }}</p>
                <p class="kh-phone">{{ dh.soDienThoai || "Chưa có SĐT" }}</p>
              </div>
            </div>
          </td>

          <td class="date-text">
            {{ dh.ngayDat || dh.ngayTaoDon || "---" }}
          </td>

          <td>
              <span
                  class="status-badge"
                  :class="getStatusClass(getStatus(dh))"
              >
                {{ getStatus(dh) }}
              </span>
          </td>

          <td class="col-tien">
            {{ Number(dh.tongCong || 0).toLocaleString('vi-VN') }}đ
          </td>

          <td class="col-action">
            <el-button
                size="small"
                type="primary"
                plain
                class="detail-btn"
                @click="openChiTiet(dh)"
            >
              <el-icon>
                <View />
              </el-icon>
              Xem chi tiết
            </el-button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <PopChiTietDonHang
        v-if="showPopup"
        v-model="showPopup"
        :don-hang="selectedDonHang"
    />
  </div>
</template>

<style scoped>
.partner-don-hang-page {
  padding: 24px 28px;
  min-height: 100vh;
  background: #f4f6f8;
  font-family: "Inter", Arial, sans-serif;
}

/* HEADER */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.page-subtitle {
  display: inline-block;
  font-size: 13px;
  font-weight: 600;
  color: #7c5c2e;
  margin-bottom: 6px;
}

.page-title {
  font-size: 26px;
  font-weight: 800;
  color: #1a1a2e;
  margin: 0;
}

.page-desc {
  margin: 8px 0 0;
  color: #6c757d;
  font-size: 14px;
  max-width: 650px;
  line-height: 1.5;
}

.reload-btn {
  height: 38px;
  border-radius: 10px;
  font-weight: 600;
}

/* SUMMARY */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 18px;
}

.summary-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 14px;
  padding: 18px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.summary-card p {
  margin: 0 0 10px;
  font-size: 13px;
  color: #6c757d;
  font-weight: 600;
}

.summary-card h3 {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #1a1a2e;
}

.summary-card.money h3 {
  color: #d32f2f;
}

/* FILTER */
.filter-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.search-box {
  flex: 1;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-select {
  width: 170px;
}

.sort-select {
  width: 190px;
}

/* TABLE */
.table-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.dh-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.dh-table th {
  background: #f8f9fa;
  color: #495057;
  font-weight: 700;
  padding: 16px;
  border-bottom: 1px solid #e9ecef;
  text-align: left;
  white-space: nowrap;
}

.dh-table td {
  padding: 16px;
  border-bottom: 1px solid #f1f3f5;
  color: #212529;
  vertical-align: middle;
}

.dh-row {
  transition: background 0.15s, transform 0.15s;
}

.dh-row:hover {
  background: #faf7f1;
}

.dh-row:last-child td {
  border-bottom: none;
}

/* CELLS */
.col-code {
  width: 150px;
}

.col-tien {
  font-weight: 800;
  color: #d32f2f;
  white-space: nowrap;
}

.col-action {
  width: 150px;
}

.code-text {
  font-weight: 800;
  color: #1a1a2e;
  background: #f1f3f5;
  padding: 6px 10px;
  border-radius: 999px;
}

.kh-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-circle {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: #f3eadc;
  color: #7c5c2e;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  text-transform: uppercase;
}

.kh-name {
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 4px;
}

.kh-phone {
  font-size: 13px;
  color: #6c757d;
  margin: 0;
}

.date-text {
  color: #495057;
  font-weight: 600;
  white-space: nowrap;
}

/* STATUS */
.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 11px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.status-badge.primary {
  color: #1d4ed8;
  background: #dbeafe;
}

.status-badge.warning {
  color: #b45309;
  background: #fef3c7;
}

.status-badge.success {
  color: #047857;
  background: #d1fae5;
}

.status-badge.danger {
  color: #b91c1c;
  background: #fee2e2;
}

.detail-btn {
  border-radius: 9px;
  font-weight: 600;
}

.empty-row {
  text-align: center;
  color: #868e96;
  padding: 36px 16px !important;
  font-weight: 600;
}

/* RESPONSIVE */
@media (max-width: 1100px) {
  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .filter-card {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-actions {
    width: 100%;
  }

  .filter-select,
  .sort-select {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .partner-don-hang-page {
    padding: 18px;
  }

  .page-header {
    flex-direction: column;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .table-card {
    overflow-x: auto;
  }

  .dh-table {
    min-width: 900px;
  }
}
</style>