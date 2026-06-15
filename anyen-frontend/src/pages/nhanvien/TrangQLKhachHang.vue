<script setup>
import { ref, computed, onMounted } from "vue";
import api from "../../api/api.js";
import PopThemKhachHang from "./PopThemKhachHang.vue";
import PopLichSuKhachHang from "./PopLichSuKhachHang.vue";

const keyword = ref("");
const statusFilter = ref("Tất cả");
const customers = ref([]);
const loading = ref(false);

const showAddPopup = ref(false);
const showHistoryPopup = ref(false);
const selectedCustomer = ref(null);
const historyItems = ref([]);
const historyLoading = ref(false);

const API_URL = "/api/nhan-vien/khach-hang";
const STATUS_OPTIONS = ["Tư vấn mới", "Đang làm việc", "Tạm dừng", "Hoàn thành"];

const loadCustomers = async () => {
  try {
    loading.value = true;
    const res = await api.get(API_URL);
    customers.value = Array.isArray(res.data) ? res.data : [];
  } catch (error) {
    console.error("Lỗi load khách hàng:", error);
    customers.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(loadCustomers);

const getAvatar = (name) => {
  if (!name) return "KH";

  const arr = name.trim().split(/\s+/);

  if (arr.length === 1) {
    return arr[0].substring(0, 2).toUpperCase();
  }

  return (arr[0][0] + arr[arr.length - 1][0]).toUpperCase();
};

const getCustomerStatus = (customer) => {
  return customer?.trangThaiHienTai || "Tư vấn mới";
};

const getCustomerStage = (customer) => {
  return customer?.giaiDoanHienTai || "Hỗ trợ khách hàng";
};

const filteredCustomers = computed(() => {
  const kw = keyword.value.trim().toLowerCase();

  return customers.value.filter((item) => {
    const name = item.tenKhachHang || "";
    const phone = item.soDienThoai || "";
    const email = item.email || "";
    const cccd = item.cccd || "";

    const matchKeyword =
        !kw ||
        name.toLowerCase().includes(kw) ||
        phone.includes(keyword.value.trim()) ||
        email.toLowerCase().includes(kw) ||
        cccd.includes(keyword.value.trim());

    if (statusFilter.value === "Tất cả") return matchKeyword;

    return matchKeyword && getCustomerStatus(item) === statusFilter.value;
  });
});

const statusClass = (status) => {
  if (status === "Đang làm việc" || status === "Hoàn thành") return "green";
  if (status === "Tạm dừng") return "orange";
  return "blue";
};

const stageClass = (stage) => {
  if (stage === "Hỗ trợ khách hàng") return "blue";
  if (stage === "Chốt sản phẩm") return "green";
  if (stage === "Chốt hợp đồng") return "orange";
  if (stage === "Quản lý dịch vụ") return "purple";
  if (stage === "Hoàn thành") return "green";
  return "blue";
};

const openHistory = async (customer) => {
  selectedCustomer.value = customer;
  showHistoryPopup.value = true;
  historyItems.value = [];

  try {
    historyLoading.value = true;
    const res = await api.get(`${API_URL}/${customer.maKhachHang}/lich-su`);
    historyItems.value = Array.isArray(res.data) ? res.data : [];
  } catch (error) {
    console.error("Lỗi load lịch sử khách hàng:", error);
    historyItems.value = [];
  } finally {
    historyLoading.value = false;
  }
};

const closeHistory = () => {
  showHistoryPopup.value = false;
  selectedCustomer.value = null;
  historyItems.value = [];
};

const handleSavedCustomer = async () => {
  showAddPopup.value = false;
  await loadCustomers();
};

const updateWorkStatus = async (customer) => {
  const current = getCustomerStatus(customer);

  const nextStatus = window.prompt(
      `Nhập trạng thái làm việc:\n${STATUS_OPTIONS.join(" / ")}`,
      current
  );

  if (!nextStatus || nextStatus.trim() === current) return;

  if (!STATUS_OPTIONS.includes(nextStatus.trim())) {
    alert("Trạng thái không hợp lệ. Chỉ được nhập: Tư vấn mới, Đang làm việc, Tạm dừng, Hoàn thành");
    return;
  }

  try {
    const res = await api.put(`${API_URL}/${customer.maKhachHang}/trang-thai-lam-viec`, {
      trangThaiLamViec: nextStatus.trim(),
    });

    const index = customers.value.findIndex((item) => item.maKhachHang === customer.maKhachHang);

    if (index !== -1) {
      customers.value[index] = res.data;
    }
  } catch (error) {
    console.error("Lỗi cập nhật trạng thái làm việc:", error);
    alert(error.response?.data?.message || "Không thể cập nhật trạng thái làm việc");
  }
};
</script>

<template>
  <div class="customer-management">
    <section class="page-content">
      <div class="card">
        <div class="filter-row">
          <div class="search-box">
            <span class="search-label">Tìm</span>
            <input
                v-model="keyword"
                placeholder="Tìm kiếm khách hàng, số điện thoại, email, CCCD..."
            />
          </div>

          <select v-model="statusFilter">
            <option>Tất cả</option>
            <option>Tư vấn mới</option>
            <option>Đang làm việc</option>
            <option>Tạm dừng</option>
            <option>Hoàn thành</option>
          </select>

          <button class="filter-btn" @click="loadCustomers">
            Tải lại
          </button>

          <button class="add-btn" @click="showAddPopup = true">
            Thêm khách hàng
          </button>
        </div>

        <div v-if="loading" class="table-state">
          Đang tải danh sách khách hàng...
        </div>

        <table v-else class="customer-table">
          <thead>
          <tr>
            <th>Khách hàng</th>
            <th>Số điện thoại</th>
            <th>Email</th>
            <th>Trạng thái hiện tại</th>
            <th>Giai đoạn hiện tại</th>
            <th>Địa chỉ</th>
            <th>CCCD</th>
            <th>Thao tác</th>
          </tr>
          </thead>

          <tbody>
          <tr
              v-for="customer in filteredCustomers"
              :key="customer.maKhachHang"
          >
            <td data-label="Khách hàng">
              <div class="customer-cell">
                <div class="avatar">
                  {{ customer.avatar || getAvatar(customer.tenKhachHang) }}
                </div>

                <div>
                  <strong>{{ customer.tenKhachHang }}</strong>
                  <p>#KH{{ customer.maKhachHang }}</p>
                </div>
              </div>
            </td>

            <td data-label="Số điện thoại">{{ customer.soDienThoai || "---" }}</td>
            <td data-label="Email">{{ customer.email || "---" }}</td>

            <td data-label="Trạng thái">
              <button
                  class="status-update-btn"
                  :class="statusClass(getCustomerStatus(customer))"
                  title="Cập nhật trạng thái làm việc"
                  @click="updateWorkStatus(customer)"
              >
                <span>{{ getCustomerStatus(customer) }}</span>
              </button>
            </td>

            <td data-label="Giai đoạn">
              <span
                  class="badge"
                  :class="stageClass(getCustomerStage(customer))"
              >
                {{ getCustomerStage(customer) }}
              </span>
            </td>

            <td data-label="Địa chỉ">{{ customer.diaChi || "---" }}</td>
            <td data-label="CCCD">{{ customer.cccd || "---" }}</td>

            <td data-label="Thao tác">
              <div class="action-group">
                <button class="history-btn" @click="openHistory(customer)">
                  Xem lịch sử
                </button>
                
              </div>
            </td>
          </tr>

          <tr v-if="!filteredCustomers.length">
            <td colspan="8">
              <div class="table-state empty">
                Không có khách hàng phù hợp
              </div>
            </td>
          </tr>
          </tbody>
        </table>

        <div class="pagination-row">
          <p>
            Hiển thị {{ filteredCustomers.length ? 1 : 0 }} - {{ filteredCustomers.length }} của
            {{ filteredCustomers.length }} khách hàng
          </p>

          <div class="pagination">
            <button>&lt;</button>
            <button class="active">1</button>
            <button>&gt;</button>
          </div>
        </div>
      </div>

      <div class="legend">
        <div>
          <h5>Chú thích trạng thái & giai đoạn</h5>

          <div class="status-list">
            <span><span class="dot blue"></span> Tư vấn mới</span>
            <span><span class="dot green"></span> Đang làm việc</span>
            <span><span class="dot orange"></span> Tạm dừng</span>
            <span><span class="dot green"></span> Hoàn thành</span>
          </div>
        </div>

        <div>
          <h5>Giai đoạn làm việc</h5>
          <p>
            1. Hỗ trợ khách hàng &nbsp;&nbsp;
            2. Chốt sản phẩm &nbsp;&nbsp;
            3. Chốt hợp đồng &nbsp;&nbsp;
            4. Quản lý dịch vụ &nbsp;&nbsp;
            ✓ Hoàn thành
          </p>
        </div>
      </div>
    </section>

    <PopThemKhachHang
        v-if="showAddPopup"
        @close="showAddPopup = false"
        @saved="handleSavedCustomer"
    />

    <PopLichSuKhachHang
        v-if="showHistoryPopup"
        :customer="selectedCustomer"
        :items="historyItems"
        :loading="historyLoading"
        @close="closeHistory"
    />
  </div>
</template>

<style scoped src="../../assets/styles/nhanvien/QLKhachHang/TrangQLKhachHang.css"></style>