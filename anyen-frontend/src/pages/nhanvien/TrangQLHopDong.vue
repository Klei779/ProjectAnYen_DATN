<script setup>
import { computed, onMounted, ref } from "vue";
import api from "../../api/api.js";
import PopTaoHopDong from "./PopTaoHopDong.vue";

const keyword = ref("");
const trangThai = ref("Tất cả");
const loaiHopDong = ref("Tất cả");
const dateRange = ref("01/05/2024 - 31/05/2024");

const showCreateContract = ref(false);
const hopDongs = ref([]);
const selectedHopDong = ref(null);

const page = ref(1);
const pageSize = ref(10);
const total = ref(0);

const loading = ref(false);
const showDetailModal = ref(false);

const statusOptions = [
  "Tất cả",
  "Đang hiệu lực",
  "Chờ ký",
  "Sắp hết hạn",
  "Đã hết hạn",
  "Đã hủy",
];

const contractTypes = [
  "Tất cả",
  "Tư vấn dịch vụ",
  "Hợp đồng dịch vụ",
];

const mapStatusToBackend = (status) => {
  if (status === "Đang hiệu lực") return "Đã ký / Hiệu lực";
  return status;
};

const loadHopDongs = async () => {
  try {
    loading.value = true;

    const response = await api.get("/api/nhan-vien/hop-dong", {
      params: {
        keyword: keyword.value,
        trangThai: mapStatusToBackend(trangThai.value),
        loaiHopDong: loaiHopDong.value === "Tất cả" ? "" : loaiHopDong.value,
        dateRange: dateRange.value,
        page: page.value,
        pageSize: pageSize.value,
      },
    });

    hopDongs.value = response.data.items || [];
    total.value = response.data.total || 0;
  } catch (error) {
    console.error("Lỗi load hợp đồng:", error);
    alert("Không thể tải danh sách hợp đồng");
  } finally {
    loading.value = false;
  }
};

const openDetail = async (id) => {
  try {
    const response = await api.get(`/api/nhan-vien/hop-dong/${id}`);

    selectedHopDong.value = response.data;
    showDetailModal.value = true;
  } catch (error) {
    console.error("Lỗi xem chi tiết hợp đồng:", error);
    alert("Không thể xem chi tiết hợp đồng");
  }
};

const closeDetail = () => {
  selectedHopDong.value = null;
  showDetailModal.value = false;
};

const searchHopDong = () => {
  page.value = 1;
  loadHopDongs();
};

const resetFilter = () => {
  keyword.value = "";
  trangThai.value = "Tất cả";
  loaiHopDong.value = "Tất cả";
  dateRange.value = "01/05/2024 - 31/05/2024";
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

const afterCreateContract = () => {
  showCreateContract.value = false;
  loadHopDongs();
};

const exportExcel = () => {
  alert("Chức năng xuất Excel đang được phát triển");
};

const editContract = () => {
  alert("Chức năng sửa hợp đồng đang được phát triển");
};

const cancelContract = () => {
  alert("Chức năng hủy hợp đồng đang được phát triển");
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
  return item.maHopDongText || `HD${String(item.maHopDong).padStart(6, "0")}`;
};

const getOrderCode = (item) => {
  return item.maDonHangText || item.maDonHang || "---";
};

const getProjectName = (item) => {
  return item.duAn || item.tenDuAn || item.tenDuAnHopDong || "An Yên Residence";
};

const getContractType = (item) => {
  return item.loaiHopDong || item.tenLoaiHopDong || "Tư vấn dịch vụ";
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
  if (status === "Đã ký / Hiệu lực") return "Đang hiệu lực";
  if (!status) return "---";
  return status;
};

const statusClass = (status) => {
  const display = displayStatus(status);

  if (display === "Đang hiệu lực") return "green";
  if (display === "Chờ ký") return "yellow";
  if (display === "Sắp hết hạn") return "purple";
  if (display === "Đã hết hạn") return "red";
  if (display === "Đã hủy") return "gray";

  return "gray";
};

const tableHopDongs = computed(() => {
  return hopDongs.value.filter((item) => {
    const sameType =
        loaiHopDong.value === "Tất cả" ||
        getContractType(item) === loaiHopDong.value;

    const sameStatus =
        trangThai.value === "Tất cả" ||
        displayStatus(item.trangThai) === trangThai.value;

    return sameType && sameStatus;
  });
});

const countByStatus = (statuses) => {
  return hopDongs.value.filter((item) => {
    return statuses.includes(displayStatus(item.trangThai));
  }).length;
};

const stats = computed(() => {
  const effective = countByStatus(["Đang hiệu lực"]);
  const pending = countByStatus(["Chờ ký"]);
  const expiring = countByStatus(["Sắp hết hạn"]);
  const expiredOrCanceled = countByStatus(["Đã hết hạn", "Đã hủy"]);

  return [
    {
      title: "Tổng hợp đồng",
      value: total.value,
      desc: "Trong khoảng thời gian",
      icon: "fa-regular fa-rectangle-list",
      type: "red",
    },
    {
      title: "Đang hiệu lực",
      value: effective,
      desc: getPercent(effective),
      icon: "fa-regular fa-file-circle-check",
      type: "green",
    },
    {
      title: "Chờ ký",
      value: pending,
      desc: getPercent(pending),
      icon: "fa-regular fa-clock",
      type: "yellow",
    },
    {
      title: "Sắp hết hạn",
      value: expiring,
      desc: getPercent(expiring),
      icon: "fa-regular fa-bell",
      type: "purple",
    },
    {
      title: "Đã hết hạn / Đã hủy",
      value: expiredOrCanceled,
      desc: getPercent(expiredOrCanceled),
      icon: "fa-regular fa-circle-xmark",
      type: "red",
    },
  ];
});

const getPercent = (count) => {
  const all = total.value || hopDongs.value.length || 0;

  if (all === 0) return "0%";

  return ((count / all) * 100).toFixed(2) + "%";
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
    <div class="page-header">
      <div class="title-left">

      </div>
    </div>

    <div class="filter-bar">
      <div class="search-box">
        <input
            v-model="keyword"
            type="text"
            placeholder="Tìm kiếm theo mã HĐ, khách hàng, dự án..."
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
              :key="item"
              :value="item"
          >
            {{ item }}
          </option>
        </select>
      </div>

      <div class="filter-group">
        <label>Loại hợp đồng</label>

        <select v-model="loaiHopDong" @change="searchHopDong">
          <option
              v-for="item in contractTypes"
              :key="item"
              :value="item"
          >
            {{ item }}
          </option>
        </select>
      </div>

      <div class="filter-group date-filter">
        <label>Khoảng thời gian</label>

        <div class="date-input">
          <input v-model="dateRange" type="text" />
          <i class="fa-regular fa-calendar"></i>
        </div>
      </div>

      <button class="reset-btn" @click="resetFilter">
        <i class="fa-solid fa-rotate-right"></i>
        Đặt lại
      </button>

      <button class="add-btn" @click="showCreateContract = true">
        <i class="fa-solid fa-plus"></i>
        Thêm hợp đồng
      </button>
    </div>

    <div class="stat-grid">
      <div
          v-for="item in stats"
          :key="item.title"
          class="stat-card"
      >
        <div>
          <p>{{ item.title }}</p>
          <h3>{{ item.value }}</h3>
          <small>{{ item.desc }}</small>
        </div>

        <div class="stat-icon" :class="item.type">
          <i :class="item.icon"></i>
        </div>
      </div>
    </div>

    <div class="list-header">
      <h3>Danh sách hợp đồng</h3>

      <div class="list-actions">
        <button class="outline-btn" @click="exportExcel">
          <i class="fa-solid fa-download"></i>
          Xuất Excel
        </button>

        <button class="square-btn">
          <i class="fa-solid fa-sliders"></i>
        </button>
      </div>
    </div>

    <div class="table-card">
      <div class="contract-mobile-list">
        <div
            v-for="item in tableHopDongs"
            :key="item.maHopDong"
            class="contract-mobile-card"
            @click="openDetail(item.maHopDong)"
        >
          <div class="mobile-card-header">
            <div>
              <h4>{{ getContractCode(item) }}</h4>
              <p>{{ item.tenKhachHang || "---" }}</p>
            </div>

            <span class="badge" :class="statusClass(item.trangThai)">
        {{ displayStatus(item.trangThai) }}
      </span>
          </div>

          <div class="mobile-info-row">
            <span>Loại HĐ</span>
            <b>{{ getContractType(item) }}</b>
          </div>

          <div class="mobile-info-row">
            <span>Giá trị</span>
            <b>{{ formatMoney(item.giaTriHopDong) }} đ</b>
          </div>

          <div class="mobile-info-row">
            <span>Ngày ký</span>
            <b>{{ formatDate(item.ngayKyHD || item.ngayTaoDon) }}</b>
          </div>

          <div class="mobile-info-row">
            <span>Hết hạn</span>
            <b>{{ getEndDate(item) }}</b>
          </div>

          <div class="mobile-actions">
            <button
                class="action-btn edit"
                @click.stop="editContract(item)"
            >
              <i class="fa-regular fa-pen-to-square"></i>
            </button>

            <button
                class="action-btn delete"
                @click.stop="cancelContract(item)"
            >
              <i class="fa-regular fa-trash-can"></i>
            </button>
          </div>
        </div>
      </div>
      <table>
        <thead>
        <tr>
          <th>Mã hợp đồng</th>
          <th>Khách hàng</th>
          <th>Dự án</th>
          <th>Loại hợp đồng</th>
          <th>Giá trị (VNĐ)</th>
          <th>Ngày ký</th>
          <th>Ngày hết hạn</th>
          <th>Trạng thái</th>
          <th class="action-title">Thao tác</th>
        </tr>
        </thead>

        <tbody>
        <tr v-if="loading">
          <td colspan="10" class="empty-row">
            Đang tải dữ liệu...
          </td>
        </tr>

        <template v-else>
          <tr
              v-for="item in tableHopDongs"
              :key="item.maHopDong"
          >
           

            <td>
              <strong>{{ getContractCode(item) }}</strong>
            </td>

            <td>
              <strong>{{ item.tenKhachHang || "---" }}</strong>
              <p>{{ item.soDienThoai || "---" }}</p>
            </td>

            <td>
              {{ getProjectName(item) }}
            </td>

            <td>
              {{ getContractType(item) }}
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
                  class="action-btn edit"
                  title="Sửa hợp đồng"
                  @click="editContract(item)"
              >
                <i class="fa-regular fa-pen-to-square"></i>
              </button>

              <button
                  class="action-btn delete"
                  title="Hủy hợp đồng"
                  @click="cancelContract(item)"
              >
                <i class="fa-regular fa-trash-can"></i>
              </button>
            </td>
          </tr>

          <tr v-if="tableHopDongs.length === 0">
            <td colspan="10" class="empty-row">
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

    <div
        v-if="showDetailModal"
        class="modal-overlay"
        @click.self="closeDetail"
    >
      <div class="modal">
        <div class="modal-header">
          <h3>Chi tiết hợp đồng</h3>

          <button @click="closeDetail">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <div v-if="selectedHopDong" class="detail-grid">
          <p>
            <span>Mã hợp đồng</span>
            <b>{{ getContractCode(selectedHopDong) }}</b>
          </p>

          <p>
            <span>Khách hàng</span>
            <b>{{ selectedHopDong.tenKhachHang || "---" }}</b>
          </p>

          <p>
            <span>Số điện thoại</span>
            <b>{{ selectedHopDong.soDienThoai || "---" }}</b>
          </p>

          <p>
            <span>Mã đơn hàng</span>
            <b>{{ getOrderCode(selectedHopDong) }}</b>
          </p>

          <p>
            <span>Dự án</span>
            <b>{{ getProjectName(selectedHopDong) }}</b>
          </p>

          <p>
            <span>Loại hợp đồng</span>
            <b>{{ getContractType(selectedHopDong) }}</b>
          </p>

          <p>
            <span>Ngày ký</span>
            <b>{{ formatDate(selectedHopDong.ngayKyHD) }}</b>
          </p>

          <p>
            <span>Ngày hết hạn</span>
            <b>{{ getEndDate(selectedHopDong) }}</b>
          </p>

          <p>
            <span>Giá trị hợp đồng</span>
            <b>{{ formatMoney(selectedHopDong.giaTriHopDong) }} VNĐ</b>
          </p>

          <p>
            <span>Trạng thái</span>
            <b>{{ displayStatus(selectedHopDong.trangThai) }}</b>
          </p>
        </div>
      </div>
    </div>

    <PopTaoHopDong
        v-model="showCreateContract"
        @saved="afterCreateContract"
        @success="afterCreateContract"
    />
  </div>
</template>

<style scoped src="../../assets/styles/TrangQLHopDong.css"></style>