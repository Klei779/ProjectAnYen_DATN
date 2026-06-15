<script setup>
import { ref, computed, onMounted } from "vue";
import api from "../../api/api.js";

const keyword = ref("");
const statusFilter = ref("Tất cả");

const customers = ref([]);

const API_URL = "/api/nhan-vien/khach-hang";

const loadCustomers = async () => {
  try {
    const res = await api.get(API_URL);
    customers.value = res.data;
  } catch (error) {
    console.error("Lỗi load khách hàng:", error);
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

const filteredCustomers = computed(() => {
  return customers.value.filter((item) => {
    const name = item.tenKhachHang || "";
    const phone = item.soDienThoai || "";
    const email = item.email || "";

    const matchKeyword =
        name.toLowerCase().includes(keyword.value.toLowerCase()) ||
        phone.includes(keyword.value) ||
        email.toLowerCase().includes(keyword.value.toLowerCase());

    if (statusFilter.value === "Tất cả") return matchKeyword;

    return matchKeyword && getCustomerStatus(item) === statusFilter.value;
  });
});

const getCustomerStatus = () => {
  return "Tư vấn mới";
};

const getCustomerStage = () => {
  return "Hỗ trợ khách hàng";
};

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
  return "blue";
};
</script>
<template>
  <div class="customer-management">
    <section class="page-content">

        <div class="card">
          <div class="filter-row">
            <div class="search-box">
              <i class="fa-solid fa-magnifying-glass"></i>
              <input
                  v-model="keyword"
                  placeholder="Tìm kiếm khách hàng, số điện thoại, email..."
              />
            </div>

            <select v-model="statusFilter">
              <option>Tất cả</option>
              <option>Tư vấn mới</option>
              <option>Đang làm việc</option>
              <option>Tạm dừng</option>
              <option>Hoàn thành</option>
            </select>

            <button class="filter-btn">
              <i class="fa-solid fa-filter"></i>
              Bộ lọc
            </button>

            <button class="add-btn">
              <i class="fa-solid fa-plus"></i>
              Thêm khách hàng
            </button>
          </div>

          <table class="customer-table">
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
              <td>
                <div class="customer-cell">
                  <div class="avatar">
                    {{ getAvatar(customer.tenKhachHang) }}
                  </div>

                  <div>
                    <strong>{{ customer.tenKhachHang }}</strong>
                    <p>Khách hàng</p>
                  </div>
                </div>
              </td>

              <td>{{ customer.soDienThoai }}</td>
              <td>{{ customer.email }}</td>

              <td>
                  <span
                      class="badge"
                      :class="statusClass(getCustomerStatus(customer))"
                  >
                    {{ getCustomerStatus(customer) }}
                  </span>
              </td>

              <td>
                  <span
                      class="badge"
                      :class="stageClass(getCustomerStage(customer))"
                  >
                    {{ getCustomerStage(customer) }}
                  </span>
              </td>

              <td>{{ customer.diaChi }}</td>
              <td>{{ customer.cccd }}</td>

              <td>
                <button class="history-btn" @click="openHistory(customer)">
                  Xem lịch sử
                </button>

                <button class="more-btn">
                  <i class="fa-solid fa-ellipsis-vertical"></i>
                </button>
              </td>
            </tr>
            </tbody>
          </table>

          <div class="pagination-row">
            <p>
              Hiển thị 1 - {{ filteredCustomers.length }} của
              {{ filteredCustomers.length }} khách hàng
            </p>

            <div class="pagination">
              <button>
                <i class="fa-solid fa-chevron-left"></i>
              </button>
              <button class="active">1</button>
              <button>
                <i class="fa-solid fa-chevron-right"></i>
              </button>
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
  </div>
</template>


<style scoped src="../../assets/styles/nhanvien/QLKhachHang/TrangQLKhachHang.css"></style>