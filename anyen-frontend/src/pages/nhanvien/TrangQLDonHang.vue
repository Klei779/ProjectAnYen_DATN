<script setup>
import { ref, computed, onMounted } from "vue";
import PopTaoDonHang from "./PopTaoDonHang.vue";
import PopChiTietDonHang from "./PopChiTietDonHang.vue";
import { getDonHangs, formatCurrency, formatDate } from "../../services/donHangService.js";
import {
  Search,
  Plus,
  Filter,
  Printer,
  View,
} from "@element-plus/icons-vue";

// ── Trạng thái ──────────────────────────────────────────
const showCreateOrder = ref(false);
const showChiTiet         = ref(false);
const selectedDonHang     = ref(null);

const keyword      = ref("");
const trangThaiFilter = ref("Tất cả");
const currentPage  = ref(1);
const pageSize     = ref(10);

// ── Dữ liệu ─────────────────────────────
const donHangs = ref([]);

onMounted(async () => {
  try {
    const data = await getDonHangs();
    donHangs.value = data.items || data || [];
  } catch (error) {
    console.error("Lỗi khi tải đơn hàng:", error);
  }
});

// ── Lọc + tìm kiếm ─────────────────────────────────────
const filteredList = computed(() => {
  return donHangs.value.filter((dh) => {
    const kw = keyword.value.toLowerCase();
    const matchKw =
      !kw ||
      dh.maCode.toLowerCase().includes(kw) ||
      dh.tenKhachHang.toLowerCase().includes(kw) ||
      dh.tenNhanVien.toLowerCase().includes(kw);
    const matchTT =
      trangThaiFilter.value === "Tất cả" ||
      dh.trangThai === trangThaiFilter.value;
    return matchKw && matchTT;
  });
});

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredList.value.slice(start, start + pageSize.value);
});

// ── Hành động ───────────────────────────────────────────
const xemChiTiet = (dh) => {
  selectedDonHang.value = dh;
  showChiTiet.value = true;
};

const huyDon = (maDonHang) => {
  const idx = donHangs.value.findIndex((d) => d.MaDonHang === maDonHang);
  if (idx !== -1) {
    donHangs.value[idx].trangThai = "Đã hủy";
    // Cập nhật lichSu
    donHangs.value[idx].lichSu.forEach((ls) => { ls.done = false; });
  }
  showChiTiet.value = false;
};

// ── Badge class ─────────────────────────────────────────
const trangThaiClass = (tt) => {
  if (tt === "Đã xác nhận")   return "badge green";
  if (tt === "Đang xử lý")    return "badge orange";
  if (tt === "Chờ thanh toán") return "badge blue";
  if (tt === "Đã hủy")        return "badge red";
  if (tt === "Hoàn thành")    return "badge green";
  return "badge gray";
};

const handleSaveDraft = (payload) => {
  console.log("Tạm lưu đơn hàng:", payload);
  alert("Đã tạm lưu đơn hàng");
};

const handleCreateOrder = async (payload) => {
  try {
    console.log("Dữ liệu tạo đơn:", payload);

    // Sau này nối API thật:
    // await createDonHang(payload);

    alert("Tạo đơn hàng thành công");

    showCreateOrder.value = false;

    // Load lại danh sách đơn hàng
    const data = await getDonHangs();
    donHangs.value = data.items || data || [];
  } catch (error) {
    console.error("Lỗi khi tạo đơn hàng:", error);
    alert("Tạo đơn hàng thất bại");
  }
};

</script>

<template>
  <div class="don-hang-page">

    <!-- ── Tiêu đề ── -->
    <div class="page-header">
      <el-button type="primary" class="btn-tao" @click="showCreateOrder = true">
        <el-icon><Plus /></el-icon>
        Tạo đơn hàng
      </el-button>
    </div>

    <!-- ── Bộ lọc ── -->
    <div class="filter-bar">
      <div class="search-wrap">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          v-model="keyword"
          class="search-input"
          placeholder="Tìm kiếm mã đơn, khách hàng, nhân viên..."
        />
      </div>

      <select v-model="trangThaiFilter" class="select-filter">
        <option>Tất cả</option>
        <option>Đã xác nhận</option>
        <option>Đang xử lý</option>
        <option>Chờ thanh toán</option>
        <option>Đã hủy</option>
        <option>Hoàn thành</option>
      </select>

      <button class="btn-filter">
        <el-icon><Filter /></el-icon>
        Bộ lọc
      </button>
    </div>

    <!-- ── Bảng đơn hàng ── -->
    <div class="table-card">
      <table class="dh-table">
        <thead>
          <tr>
            <th>Mã đơn</th>
            <th>Khách hàng</th>
            <th>Nhân viên phụ trách</th>
            <th>Ngày tạo</th>
            <th>Tổng tiền</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="pagedList.length === 0">
            <td colspan="7" class="empty-row">Không có đơn hàng nào.</td>
          </tr>
          <tr
            v-for="dh in pagedList"
            :key="dh.MaDonHang"
            class="dh-row"
            @click="xemChiTiet(dh)"
          >
            <td class="col-code">
              <span class="code-text">#{{ dh.maCode }}</span>
            </td>
            <td>
              <div class="kh-cell">
                <div class="mini-avatar">{{ dh.avatarKH }}</div>
                <div>
                  <p class="kh-name">{{ dh.tenKhachHang }}</p>
                  <p class="kh-phone">{{ dh.soDienThoaiKH }}</p>
                </div>
              </div>
            </td>
            <td class="col-nv">{{ dh.tenNhanVien }}</td>
            <td>{{ formatDate(dh.NgayTaoDon) }}</td>
            <td class="col-tien">{{ formatCurrency(dh.tongTien) }}</td>
            <td>
              <span :class="trangThaiClass(dh.trangThai)">{{ dh.trangThai }}</span>
            </td>
            <td class="col-action" @click.stop>
              <el-button size="small" plain @click="xemChiTiet(dh)">
                <el-icon><View /></el-icon>
                Chi tiết
              </el-button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- ── Phân trang ── -->
      <div class="pagination-bar">
        <span class="pag-info">
          Hiển thị {{ Math.min((currentPage - 1) * pageSize + 1, filteredList.length) }}
          –
          {{ Math.min(currentPage * pageSize, filteredList.length) }}
          của {{ filteredList.length }} đơn hàng
        </span>
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="filteredList.length"
          layout="prev, pager, next"
          small
        />
      </div>
    </div>

    <!-- ── Popup tạo đơn hàng ── -->
    <PopTaoDonHang
        v-if="showCreateOrder"
        @close="showCreateOrder = false"
        @submit="handleCreateOrder"
        @save-draft="handleSaveDraft"
    />

    <!-- ── Popup chi tiết đơn hàng ── -->
    <PopChiTietDonHang
      v-model="showChiTiet"
      :don-hang="selectedDonHang"
      @huy-don="huyDon"
    />
  </div>
</template>

<style scoped>
.don-hang-page {
  padding: 24px 28px;
  min-height: 100vh;
  background: #fafafa;
  font-family: 'Inter', Arial, sans-serif;
}

/* ── Header ── */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}
.total-count {
  background: #fff1f2;
  color: #8a181a;
  font-size: 12px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 20px;
}
.btn-tao {
  background: #8a181a;
  border-color: #8a181a;
  font-weight: 600;
}
.btn-tao:hover {
  background: #a31c1e;
  border-color: #a31c1e;
}

/* ── Filter bar ── */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.search-wrap {
  flex: 1;
  min-width: 200px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 0 12px;
  height: 38px;
}
.search-icon {
  color: #aaa;
  font-size: 15px;
}
.search-input {
  border: none;
  outline: none;
  flex: 1;
  font-size: 13px;
  background: transparent;
}
.select-filter {
  height: 38px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 0 10px;
  font-size: 13px;
  background: #fff;
  min-width: 170px;
  cursor: pointer;
}
.btn-filter {
  height: 38px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 0 14px;
  font-size: 13px;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* ── Table ── */
.table-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 12px;
  overflow: hidden;
}
.dh-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.dh-table th {
  background: #f8f8f8;
  color: #555;
  font-weight: 600;
  padding: 12px 14px;
  border-bottom: 1px solid #eee;
  text-align: left;
  white-space: nowrap;
}
.dh-table td {
  padding: 14px 14px;
  border-bottom: 1px solid #f5f5f5;
  color: #333;
  vertical-align: middle;
}
.dh-row {
  cursor: pointer;
  transition: background 0.15s;
}
.dh-row:hover {
  background: #fafafa;
}
.dh-row:hover .code-text {
  color: #8a181a;
}
.empty-row {
  text-align: center;
  color: #aaa;
  padding: 40px !important;
}

/* Cells */
.col-code  { width: 100px; }
.col-nv    { white-space: nowrap; }
.col-tien  { font-weight: 600; white-space: nowrap; }
.col-action { width: 100px; }

.code-text {
  font-weight: 700;
  color: #1a1a2e;
  font-size: 13px;
  transition: color 0.15s;
}
.kh-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.mini-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #fff1f2;
  color: #8a181a;
  font-weight: 700;
  font-size: 12px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}
.kh-name {
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 2px;
}
.kh-phone {
  font-size: 12px;
  color: #888;
  margin: 0;
}

/* Badge trạng thái */
.badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}
.badge.green  { background: #e8f8ef; color: #17934a; }
.badge.orange { background: #fff4e5; color: #e67e22; }
.badge.blue   { background: #e8f0fe; color: #1565c0; }
.badge.red    { background: #fdecea; color: #c62828; }
.badge.gray   { background: #f5f5f5; color: #757575; }

/* ── Phân trang ── */
.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-top: 1px solid #f0f0f0;
}
.pag-info {
  font-size: 13px;
  color: #888;
}
</style>
