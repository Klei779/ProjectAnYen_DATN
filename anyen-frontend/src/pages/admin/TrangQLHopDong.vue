<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

import PopXemHopDong from "../nhanvien/PopChiTietHopDong.vue";

import {
  getHopDongsAdmin,
  xoaHopDong,
} from "../../services/quanLyHopDongService.js";

const keyword = ref("");
const trangThai = ref(null);

const showDetailModal = ref(false);

const hopDongs = ref([]);
const selectedHopDongId = ref(null);

const page = ref(1);
const pageSize = ref(10);
const total = ref(0);

const loading = ref(false);

const statusOptions = [
  {
    label: "Tất cả",
    value: null,
  },
  {
    label: "Chờ ký",
    value: 0,
  },
  {
    label: "Đang hiệu lực",
    value: 1,
  },
  {
    label: "Đã hủy",
    value: 2,
  },
];

const loadHopDongs = async () => {
  try {
    loading.value = true;

    const data = await getHopDongsAdmin({
      keyword: keyword.value?.trim() || "",
      trangThai: trangThai.value,
      page: page.value,
      pageSize: pageSize.value,
    });

    hopDongs.value = Array.isArray(data?.items)
        ? data.items
        : [];

    total.value = Number(data?.total) || 0;
  } catch (error) {
    console.error("Lỗi tải hợp đồng:", error);

    hopDongs.value = [];
    total.value = 0;

    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể tải danh sách hợp đồng"
    );
  } finally {
    loading.value = false;
  }
};

const openDetail = (id) => {
  selectedHopDongId.value = id;
  showDetailModal.value = true;
};

const searchHopDong = () => {
  page.value = 1;
  loadHopDongs();
};

const changePage = (newPage) => {
  if (newPage < 1 || newPage > totalPages.value) return;

  page.value = newPage;
  loadHopDongs();
};

const changePageSize = () => {
  page.value = 1;
  loadHopDongs();
};

const deleteContract = async (item) => {
  if (!item?.maHopDong) return;

  try {
    await ElMessageBox.confirm(
        `Xóa vĩnh viễn hợp đồng ${getContractCode(item)}? ` +
        "Toàn bộ dữ liệu liên quan (chi tiết hợp đồng, đơn hàng, hóa đơn, " +
        "công nợ...) sẽ bị xóa và KHÔNG THỂ khôi phục.",
        "Xác nhận xóa hợp đồng",
        {
          confirmButtonText: "Xóa vĩnh viễn",
          cancelButtonText: "Không",
          type: "error",
        }
    );

    await xoaHopDong(item.maHopDong);

    ElMessage.success("Đã xóa hợp đồng và toàn bộ dữ liệu liên quan");

    if (hopDongs.value.length === 1 && page.value > 1) {
      page.value -= 1;
    }

    await loadHopDongs();
  } catch (error) {
    if (error === "cancel" || error === "close") return;

    console.error("Lỗi xóa hợp đồng:", error);

    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể xóa hợp đồng"
    );
  }
};

const formatMoney = (value) => {
  if (value === null || value === undefined) return "0";
  return Number(value).toLocaleString("vi-VN");
};

const formatDate = (value) => {
  if (!value) return "---";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleDateString("vi-VN");
};

const getContractCode = (item) => {
  return (
      item.soHopDong ||
      item.maHopDongText ||
      `HD${String(item.maHopDong).padStart(7, "0")}`
  );
};

const getEndDateRaw = (item) => {
  if (item.ngayHetHan) return item.ngayHetHan;
  if (item.ngayKetThuc) return item.ngayKetThuc;

  if (!item.ngayKyHD) return null;

  const date = new Date(item.ngayKyHD);

  if (Number.isNaN(date.getTime())) return null;

  date.setMonth(date.getMonth() + 3);
  return date;
};

const getEndDate = (item) => {
  return formatDate(getEndDateRaw(item));
};
const displayStatus = (status) => {
  if (
      status === null ||
      status === undefined ||
      status === ""
  ) {
    return "---";
  }

  const numericStatus = Number(status);

  if (!Number.isNaN(numericStatus)) {
    switch (numericStatus) {
      case 0:
        return "Chờ ký";

      case 1:
        return "Đang hiệu lực";

      case 2:
        return "Đã hủy";

      default:
        return "Không xác định";
    }
  }

  // Tương thích với dữ liệu String cũ nếu còn cache
  const textStatus = String(status).trim();

  if (
      textStatus === "Đã ký" ||
      textStatus === "Đã ký / Hiệu lực" ||
      textStatus === "Đã hoàn tất"
  ) {
    return "Đang hiệu lực";
  }

  if (textStatus === "Mới tạo") {
    return "Chờ ký";
  }

  return textStatus;
};

const statusClass = (status) => {
  const display = displayStatus(status);

  if (display === "Đang hiệu lực") return "green";
  if (display === "Đang thực hiện") return "green";
  if (display === "Chờ ký") return "yellow";
  if (display === "Chờ thanh toán") return "yellow";
  if (display === "Sắp hết hạn") return "purple";
  if (display === "Đã hết hạn") return "red";
  if (display === "Đã hủy") return "gray";

  return "gray";
};

const totalPages = computed(() => {
  return Math.max(Math.ceil(total.value / pageSize.value), 1);
});

const startItem = computed(() => {
  if (total.value === 0) return 0;
  return (page.value - 1) * pageSize.value + 1;
});

const endItem = computed(() => {
  return Math.min(page.value * pageSize.value, total.value);
});

const visiblePages = computed(() => {
  const pages = [];
  const max = totalPages.value;

  if (max <= 5) {
    for (let i = 1; i <= max; i++) pages.push(i);
    return pages;
  }

  if (page.value <= 3) {
    return [1, 2, 3, "...", max];
  }

  if (page.value >= max - 2) {
    return [1, "...", max - 2, max - 1, max];
  }

  return [1, "...", page.value, "...", max];
});

onMounted(() => {
  loadHopDongs();
});
</script>

<template>
  <div class="contract-page">
    <div class="filter-bar">
      <div class="search-box">
        <input
            v-model="keyword"
            type="text"
            placeholder="Tìm kiếm theo mã HĐ, khách hàng, SĐT..."
            @keyup.enter="searchHopDong"
        />

        <button @click="searchHopDong">
          <i class="fa-solid fa-magnifying-glass"></i>
        </button>
      </div>

      <div class="filter-group">
        <label>Trạng thái</label>

        <select v-model="trangThai" @change="searchHopDong">
          <option
              v-for="item in statusOptions"
              :key="item.label"
              :value="item.value"
          >
            {{ item.label }}
          </option>
        </select>
      </div>
    </div>

    <div class="list-header">
      <h3>Lịch sử &amp; danh sách hợp đồng</h3>
    </div>

    <div class="table-card">
      <table>
        <thead>
        <tr>
          <th>Mã hợp đồng</th>
          <th>Khách hàng</th>
          <th>Giá trị (VNĐ)</th>
          <th>Ngày ký</th>
          <th>Ngày hết hạn</th>
          <th>Trạng thái</th>
          <th class="action-title">Thao tác</th>
        </tr>
        </thead>

        <tbody>
        <tr v-if="loading">
          <td colspan="8" class="empty-row">
            Đang tải dữ liệu...
          </td>
        </tr>

        <template v-else>
          <tr
              v-for="item in hopDongs"
              :key="item.maHopDong"
              :class="{ 'hidden-row': item.an }"
          >
            <td>
              <strong>{{ getContractCode(item) }}</strong>
            </td>

            <td>
              <strong>{{ item.tenKhachHang || "---" }}</strong>
              <p>{{ item.soDienThoai || "---" }}</p>
            </td>

            <td>
              {{ formatMoney(item.giaTriHopDong) }}
            </td>

            <td>
              {{ formatDate(item.ngayKyHD || item.ngayTaoDon) }}
            </td>

            <td>
              {{ getEndDate(item) }}
            </td>

            <td>
              <span class="badge" :class="statusClass(item.trangThai)">
                {{ displayStatus(item.trangThai) }}
              </span>
            </td>

            <td class="action-cell">
              <button
                  class="action-btn view"
                  title="Xem chi tiết"
                  @click="openDetail(item.maHopDong)"
              >
                <i class="fa-regular fa-eye"></i>
              </button>

              <button
                  class="action-btn delete"
                  title="Xóa vĩnh viễn"
                  @click="deleteContract(item)"
              >
                <i class="fa-regular fa-trash-can"></i>
              </button>
            </td>
          </tr>

          <tr v-if="hopDongs.length === 0">
            <td colspan="8" class="empty-row">
              Không có hợp đồng nào.
            </td>
          </tr>
        </template>
        </tbody>
      </table>

      <div class="pagination-row">
        <p>
          Hiển thị {{ startItem }} - {{ endItem }} trên {{ total }} hợp đồng
        </p>

        <div class="pagination">
          <button @click="changePage(page - 1)">
            <i class="fa-solid fa-chevron-left"></i>
          </button>

          <template
              v-for="p in visiblePages"
              :key="p"
          >
            <span v-if="p === '...'" class="dots">...</span>

            <button
                v-else
                :class="{ active: page === p }"
                @click="changePage(p)"
            >
              {{ p }}
            </button>
          </template>

          <button @click="changePage(page + 1)">
            <i class="fa-solid fa-chevron-right"></i>
          </button>

          <select v-model="pageSize" @change="changePageSize">
            <option :value="10">10 / trang</option>
            <option :value="20">20 / trang</option>
            <option :value="50">50 / trang</option>
          </select>
        </div>
      </div>
    </div>

    <PopXemHopDong
        v-model="showDetailModal"
        :hop-dong-id="selectedHopDongId"
        :readonly="true"
    />
  </div>
</template>

<style scoped src="../../assets/styles/nhanvien/QLHopDong/TrangQLHopDong.css"></style>

<style scoped>
.hidden-filter {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  white-space: nowrap;
}

.hidden-filter input {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.hidden-row {
  opacity: 0.6;
}

.action-btn.hide {
  color: #6b7280;
}
</style>
