<script setup>
import { computed, onMounted, ref, watch } from "vue";
import api from "../../api/api.js";

const API_URL = "/api/nhan-vien/quan-ly-khach-hang";
const PAGE_SIZE = 10;

const keyword = ref("");
const statusFilter = ref("Tất cả");
const employeeFilter = ref("Tất cả");

const customers = ref([]);
const loading = ref(false);
const errorMessage = ref("");
const currentPage = ref(1);

const selectedCustomer = ref(null);
const customerOrders = ref([]);
const loadingOrders = ref(false);
const selectedOrder = ref(null);

const loadCustomers = async () => {
  try {
    loading.value = true;
    errorMessage.value = "";

    const response = await api.get(API_URL);
    const data = response?.data;

    if (Array.isArray(data)) {
      customers.value = data;
    } else if (Array.isArray(data?.content)) {
      customers.value = data.content;
    } else {
      customers.value = [];
    }
  } catch (error) {
    console.error("Lỗi tải danh sách khách hàng:", error);

    customers.value = [];
    errorMessage.value =
        error.response?.data?.message ||
        "Không thể tải danh sách khách hàng.";
  } finally {
    loading.value = false;
  }
};

onMounted(loadCustomers);

const getAvatar = (name) => {
  if (!name) return "KH";

  const words = name
      .trim()
      .split(/\s+/)
      .filter(Boolean);

  if (!words.length) return "KH";

  if (words.length === 1) {
    return words[0].substring(0, 2).toUpperCase();
  }

  return (
      words[0][0] +
      words[words.length - 1][0]
  ).toUpperCase();
};

const getCustomerStatus = (customer) => {
  return customer?.trangThaiHienTai || "Tư vấn mới";
};

const getCustomerStage = (customer) => {
  return customer?.giaiDoanHienTai || "Hỗ trợ khách hàng";
};

const employeeOptions = computed(() => {
  const employeeMap = new Map();

  customers.value.forEach((customer) => {
    if (customer.maNhanVienPhuTrach == null) return;

    employeeMap.set(String(customer.maNhanVienPhuTrach), {
      id: String(customer.maNhanVienPhuTrach),
      name:
          customer.tenNhanVienPhuTrach ||
          `Nhân viên #${customer.maNhanVienPhuTrach}`,
    });
  });

  return Array.from(employeeMap.values()).sort((a, b) =>
      a.name.localeCompare(b.name, "vi")
  );
});

const filteredCustomers = computed(() => {
  const searchValue = keyword.value.trim().toLowerCase();

  return customers.value.filter((customer) => {
    const searchableFields = [
      customer.maKhachHang,
      customer.tenKhachHang,
      customer.soDienThoai,
      customer.email,
      customer.cccd,
      customer.diaChi,
      customer.tenNhanVienPhuTrach,
      customer.emailNhanVienPhuTrach,
      customer.soDienThoaiNhanVienPhuTrach,
    ]
        .filter((value) => value !== null && value !== undefined)
        .map((value) => String(value).toLowerCase());

    const matchKeyword =
        !searchValue ||
        searchableFields.some((value) =>
            value.includes(searchValue)
        );

    const matchStatus =
        statusFilter.value === "Tất cả" ||
        getCustomerStatus(customer) === statusFilter.value;

    let matchEmployee = true;

    if (employeeFilter.value === "Chưa phân công") {
      matchEmployee = customer.maNhanVienPhuTrach == null;
    } else if (employeeFilter.value !== "Tất cả") {
      matchEmployee =
          String(customer.maNhanVienPhuTrach) ===
          employeeFilter.value;
    }

    return matchKeyword && matchStatus && matchEmployee;
  });
});

const totalPages = computed(() => {
  return Math.max(
      1,
      Math.ceil(filteredCustomers.value.length / PAGE_SIZE)
  );
});

const paginatedCustomers = computed(() => {
  const startIndex = (currentPage.value - 1) * PAGE_SIZE;
  const endIndex = startIndex + PAGE_SIZE;

  return filteredCustomers.value.slice(startIndex, endIndex);
});

const firstDisplayedItem = computed(() => {
  if (!filteredCustomers.value.length) return 0;

  return (currentPage.value - 1) * PAGE_SIZE + 1;
});

const lastDisplayedItem = computed(() => {
  return Math.min(
      currentPage.value * PAGE_SIZE,
      filteredCustomers.value.length
  );
});

const pageNumbers = computed(() => {
  const pages = [];

  let startPage = Math.max(1, currentPage.value - 2);
  let endPage = Math.min(totalPages.value, startPage + 4);

  startPage = Math.max(1, endPage - 4);

  for (let page = startPage; page <= endPage; page++) {
    pages.push(page);
  }

  return pages;
});

watch([keyword, statusFilter, employeeFilter], () => {
  currentPage.value = 1;
});

watch(totalPages, (newTotalPages) => {
  if (currentPage.value > newTotalPages) {
    currentPage.value = newTotalPages;
  }
});

const statusClass = (status) => {
  if (status === "Đang làm việc") return "green";
  if (status === "Hoàn thành") return "green";
  if (status === "Tạm dừng") return "orange";

  return "blue";
};

const stageClass = (stage) => {
  if (stage === "Chốt sản phẩm") return "green";
  if (stage === "Chốt hợp đồng") return "orange";
  if (stage === "Quản lý dịch vụ") return "purple";
  if (stage === "Hoàn thành") return "green";

  return "blue";
};

const openDetail = async (customer) => {
  selectedCustomer.value = customer;
  selectedOrder.value = null;
  
  // Load orders for this customer
  loadingOrders.value = true;
  customerOrders.value = [];
  
  try {
    const response = await api.get(`/api/don-hang/khach-hang/${customer.maKhachHang}`);
    console.log('Orders response:', response.data);
    customerOrders.value = response.data || [];
  } catch (error) {
    console.error("Lỗi tải đơn hàng khách hàng:", error);
    customerOrders.value = [];
  } finally {
    loadingOrders.value = false;
  }
};

const closeDetail = () => {
  selectedCustomer.value = null;
  selectedOrder.value = null;
  customerOrders.value = [];
};

const openOrderDetail = (order) => {
  selectedOrder.value = order;
};

const closeOrderDetail = () => {
  selectedOrder.value = null;
};

const getOrderStatusText = (trangThai) => {
  const statusMap = {
    1: 'Mới tạo',
    2: 'Chờ xác nhận',
    3: 'Đã xác nhận',
    4: 'Đang xử lý',
    5: 'Chờ thanh toán',
    6: 'Hoàn thành',
    7: 'Đã hủy',
    8: 'Đối tác từ chối',
    9: 'Đã giao',
    10: 'Đã thanh toán',
    11: 'Gặp sự cố'
  };
  return statusMap[trangThai] || 'Không rõ';
};

const getOrderStatusClass = (trangThai) => {
  if (trangThai === 6 || trangThai === 9 || trangThai === 10) return 'green';
  if (trangThai === 4 || trangThai === 5) return 'orange';
  if (trangThai === 7 || trangThai === 8 || trangThai === 11) return 'red';
  return 'blue';
};

const formatCurrency = (value) => {
  if (value == null) return '---';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value);
};

const getPaymentMethodText = (method) => {
  const methodMap = {
    0: 'Chưa chọn',
    1: 'Tiền mặt',
    2: 'Chuyển khoản'
  };
  return methodMap[method] || '---';
};

const getPaymentStatusText = (status) => {
  const statusMap = {
    0: 'Chưa thanh toán',
    1: 'Đã thanh toán',
    2: 'Chờ xác nhận'
  };
  return statusMap[status] || '---';
};

const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return;

  currentPage.value = page;
};
</script>

<template>
  <div class="customer-management admin-customer-management">
    <section class="page-content">
      <div class="card">
        <div class="filter-row admin-filter-row">
          <div class="search-box">
            <i class="fa-solid fa-magnifying-glass search-icon"></i>

            <input
                v-model="keyword"
                type="text"
                placeholder="Tìm khách hàng, SĐT, email, CCCD hoặc nhân viên..."
            />
          </div>

          <select
              v-model="statusFilter"
              aria-label="Lọc trạng thái khách hàng"
          >
            <option value="Tất cả">Tất cả trạng thái</option>
            <option value="Tư vấn mới">Tư vấn mới</option>
            <option value="Đang làm việc">Đang làm việc</option>
            <option value="Tạm dừng">Tạm dừng</option>
            <option value="Hoàn thành">Hoàn thành</option>
          </select>

          <select
              v-model="employeeFilter"
              aria-label="Lọc nhân viên phụ trách"
          >
            <option value="Tất cả">Tất cả nhân viên</option>
            <option value="Chưa phân công">Chưa phân công</option>

            <option
                v-for="employee in employeeOptions"
                :key="employee.id"
                :value="employee.id"
            >
              {{ employee.name }}
            </option>
          </select>

          <button
              type="button"
              class="filter-btn reload-btn"
              @click="loadCustomers"
          >
            <svg
                class="button-svg-icon"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
                aria-hidden="true"
            >
              <path
                  d="M20 11A8 8 0 1 0 17.66 16.66"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
              />
              <path
                  d="M20 4V11H13"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
              />
            </svg>

            Tải lại
          </button>
        </div>

        <div v-if="loading" class="table-state">
          <i class="fa-solid fa-spinner fa-spin"></i>
          Đang tải danh sách khách hàng...
        </div>

        <div
            v-else-if="errorMessage"
            class="table-state error-state"
        >
          <div>
            <i class="fa-solid fa-circle-exclamation"></i>

            <p>{{ errorMessage }}</p>

            <button
                type="button"
                class="retry-btn"
                @click="loadCustomers"
            >
              Thử lại
            </button>
          </div>
        </div>

        <div v-else class="table-wrapper">
          <table class="customer-table admin-customer-table">
            <thead>
            <tr>
              <th>Khách hàng</th>
              <th>Trạng thái hiện tại</th>
              <th>Giai đoạn hiện tại</th>
              <th>Nhân viên phụ trách</th>
              <th>Địa chỉ</th>
              <th>Thao tác</th>
            </tr>
            </thead>

            <tbody>
            <tr
                v-for="customer in paginatedCustomers"
                :key="customer.maKhachHang"
            >
              <td data-label="Khách hàng">
                <div class="customer-cell">
                  <div class="avatar">
                    {{
                      customer.avatar ||
                      getAvatar(customer.tenKhachHang)
                    }}
                  </div>

                  <div>
                    <strong>
                      {{
                        customer.tenKhachHang ||
                        "Chưa cập nhật"
                      }}
                    </strong>

                    <p>#KH{{ customer.maKhachHang }}</p>
                  </div>
                </div>
              </td>

              <td data-label="Trạng thái hiện tại">
                  <span
                      class="badge"
                      :class="
                      statusClass(
                        getCustomerStatus(customer)
                      )
                    "
                  >
                    {{ getCustomerStatus(customer) }}
                  </span>
              </td>

              <td data-label="Giai đoạn hiện tại">
                  <span
                      class="badge"
                      :class="
                      stageClass(
                        getCustomerStage(customer)
                      )
                    "
                  >
                    {{ getCustomerStage(customer) }}
                  </span>
              </td>

              <td data-label="Nhân viên phụ trách">
                <div
                    v-if="
                      customer.maNhanVienPhuTrach != null
                    "
                    class="employee-cell"
                >
                  <div class="employee-avatar">
                    {{
                      getAvatar(
                          customer.tenNhanVienPhuTrach
                      )
                    }}
                  </div>

                  <div class="employee-info">
                    <strong>
                      {{
                        customer.tenNhanVienPhuTrach ||
                        "Chưa cập nhật"
                      }}
                    </strong>

                    <small>
                      #NV{{ customer.maNhanVienPhuTrach }}
                    </small>
                  </div>
                </div>

                <span
                    v-else
                    class="unassigned-badge"
                >
                    Chưa phân công
                  </span>
              </td>

              <td data-label="Địa chỉ">
                {{ customer.diaChi || "---" }}
              </td>

              <td data-label="Thao tác">
                <div class="action-group">
                  <button
                      type="button"
                      class="history-btn detail-btn"
                      @click="openDetail(customer)"
                  >
                    <svg
                        class="button-svg-icon"
                        viewBox="0 0 24 24"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                        aria-hidden="true"
                    >
                      <circle
                          cx="11"
                          cy="11"
                          r="7"
                          stroke="currentColor"
                          stroke-width="2"
                      />

                      <path
                          d="M16.5 16.5L21 21"
                          stroke="currentColor"
                          stroke-width="2"
                          stroke-linecap="round"
                      />
                    </svg>

                    Xem chi tiết
                  </button>
                </div>
              </td>
            </tr>

            <tr v-if="!paginatedCustomers.length">
              <td colspan="6">
                <div class="table-state empty">
                  Không có khách hàng phù hợp
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <div
            v-if="!loading && !errorMessage"
            class="pagination-row"
        >
          <p>
            Hiển thị {{ firstDisplayedItem }} -
            {{ lastDisplayedItem }} của
            {{ filteredCustomers.length }} khách hàng
          </p>

          <div class="pagination">
            <button
                type="button"
                :disabled="currentPage === 1"
                @click="goToPage(currentPage - 1)"
            >
              &lt;
            </button>

            <button
                v-for="page in pageNumbers"
                :key="page"
                type="button"
                :class="{ active: currentPage === page }"
                @click="goToPage(page)"
            >
              {{ page }}
            </button>

            <button
                type="button"
                :disabled="currentPage === totalPages"
                @click="goToPage(currentPage + 1)"
            >
              &gt;
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Modals outside main content -->
    <Teleport to="body">
      <transition name="modal">
        <div
            v-if="selectedCustomer"
            class="customer-modal-overlay"
            @click.self="closeDetail"
        >
          <div
              class="customer-detail-modal"
              role="dialog"
              aria-modal="true"
          >
            <div class="modal-header">
              <div class="modal-customer-heading">
                <div class="avatar modal-avatar">
                  {{
                    selectedCustomer.avatar ||
                    getAvatar(
                        selectedCustomer.tenKhachHang
                    )
                  }}
                </div>

                <div>
                  <h3>
                    {{
                      selectedCustomer.tenKhachHang ||
                      "Khách hàng"
                    }}
                  </h3>

                  <p>
                    #KH{{ selectedCustomer.maKhachHang }}
                  </p>
                </div>
              </div>

              <button
                  type="button"
                  class="modal-close-btn"
                  @click="closeDetail"
              >
                <i class="fa-solid fa-xmark"></i>
              </button>
            </div>

            <div class="modal-body">
              <section class="detail-section">
                <h4>Thông tin khách hàng</h4>

                <div class="detail-grid">
                  <div class="detail-item">
                    <span>Số điện thoại</span>

                    <strong>
                      {{
                        selectedCustomer.soDienThoai ||
                        "Chưa cập nhật"
                      }}
                    </strong>
                  </div>

                  <div class="detail-item">
                    <span>Email</span>

                    <strong>
                      {{
                        selectedCustomer.email ||
                        "Chưa cập nhật"
                      }}
                    </strong>
                  </div>

                  <div class="detail-item">
                    <span>CCCD</span>

                    <strong>
                      {{
                        selectedCustomer.cccd ||
                        "Chưa cập nhật"
                      }}
                    </strong>
                  </div>

                  <div class="detail-item">
                    <span>Ngày đăng ký</span>

                    <strong>
                      {{
                        selectedCustomer.ngayDangKy ||
                        "Chưa cập nhật"
                      }}
                    </strong>
                  </div>

                  <div class="detail-item full-width">
                    <span>Địa chỉ</span>

                    <strong>
                      {{
                        selectedCustomer.diaChi ||
                        "Chưa cập nhật"
                      }}
                    </strong>
                  </div>

                  <div class="detail-item">
                    <span>Nguồn đăng ký</span>

                    <strong>
                      {{
                        selectedCustomer.nguonDangKy ||
                        "Chưa cập nhật"
                      }}
                    </strong>
                  </div>

                  <div class="detail-item">
                    <span>Nhu cầu hỗ trợ</span>

                    <strong>
                      {{
                        selectedCustomer.nhuCauHoTro ||
                        "Chưa cập nhật"
                      }}
                    </strong>
                  </div>
                </div>
              </section>

              <section class="detail-section">
                <h4>Tiến độ chăm sóc</h4>

                <div class="status-detail-row">
                  <div>
                    <span>Trạng thái hiện tại</span>

                    <strong>
                      <span
                          class="badge"
                          :class="
                          statusClass(
                            getCustomerStatus(
                              selectedCustomer
                            )
                          )
                        "
                      >
                        {{
                          getCustomerStatus(
                              selectedCustomer
                          )
                        }}
                      </span>
                    </strong>
                  </div>

                  <div>
                    <span>Giai đoạn hiện tại</span>

                    <strong>
                      <span
                          class="badge"
                          :class="
                          stageClass(
                            getCustomerStage(
                              selectedCustomer
                            )
                          )
                        "
                      >
                        {{
                          getCustomerStage(
                              selectedCustomer
                          )
                        }}
                      </span>
                    </strong>
                  </div>
                </div>
              </section>

              <section class="detail-section">
                <h4>Nhân viên phụ trách</h4>

                <div
                    v-if="
                    selectedCustomer.maNhanVienPhuTrach !=
                    null
                  "
                    class="assigned-employee-card"
                >
                  <div class="employee-avatar large">
                    {{
                      getAvatar(
                          selectedCustomer.tenNhanVienPhuTrach
                      )
                    }}
                  </div>

                  <div>
                    <strong>
                      {{
                        selectedCustomer.tenNhanVienPhuTrach
                      }}
                    </strong>

                    <p>
                      {{
                        selectedCustomer.emailNhanVienPhuTrach ||
                        "Chưa có email"
                      }}
                    </p>

                    <p>
                      {{
                        selectedCustomer.soDienThoaiNhanVienPhuTrach ||
                        "Chưa có số điện thoại"
                      }}
                    </p>
                  </div>
                </div>

                <div
                    v-else
                    class="not-assigned-message"
                >
                  Khách hàng này chưa được phân công cho
                  nhân viên phụ trách.
                </div>
              </section>

              <section
                  v-if="selectedCustomer.ghiChu"
                  class="detail-section"
              >
                <h4>Ghi chú</h4>

                <p class="note-content">
                  {{ selectedCustomer.ghiChu }}
                </p>
              </section>

              <section class="detail-section">
                <h4>Lịch sử đơn hàng</h4>

                <div v-if="loadingOrders" class="loading-state">
                  <i class="fa-solid fa-spinner fa-spin"></i>
                  Đang tải đơn hàng...
                </div>

                <div v-else-if="customerOrders.length === 0" class="empty-state">
                  Khách hàng chưa có đơn hàng nào
                </div>

                <div v-else class="orders-list">
                  <div
                      v-for="order in customerOrders"
                      :key="order.MaDonHang"
                      class="order-item"
                      @click="openOrderDetail(order)"
                  >
                    <div class="order-header">
                      <strong>{{ order.maCode || `#DH${order.MaDonHang}` }}</strong>
                      <span
                          class="badge"
                          :class="getOrderStatusClass(order.trangThai)"
                      >
                        {{ getOrderStatusText(order.trangThai) }}
                      </span>
                    </div>

                    <div class="order-info">
                      <span>Ngày tạo: {{ order.NgayTaoDon || '---' }}</span>
                      <span>Tổng tiền: {{ formatCurrency(order.tongTien) }}</span>
                    </div>

                    <div class="order-products-count">
                      {{ order.sanPhams?.length || 0 }} sản phẩm
                    </div>
                  </div>
                </div>
              </section>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>

    <!-- Modal chi tiết đơn hàng -->
    <Teleport to="body">
      <transition name="modal">
        <div
            v-if="selectedOrder"
            class="customer-modal-overlay"
            @click.self="closeOrderDetail"
        >
          <div
              class="customer-detail-modal"
              role="dialog"
              aria-modal="true"
          >
            <div class="modal-header">
              <div class="modal-customer-heading">
                <div class="avatar modal-avatar">
                  DH
                </div>

                <div>
                  <h3>
                    Đơn hàng {{ selectedOrder.maCode || `#${selectedOrder.MaDonHang}` }}
                  </h3>

                  <p>
                    {{ getOrderStatusText(selectedOrder.trangThai) }}
                  </p>
                </div>
              </div>

              <button
                  type="button"
                  class="modal-close-btn"
                  @click="closeOrderDetail"
              >
                <i class="fa-solid fa-xmark"></i>
              </button>
            </div>

            <div class="modal-body">
              <section class="detail-section">
                <h4>Thông tin đơn hàng</h4>

                <div class="detail-grid">
                  <div class="detail-item">
                    <span>Ngày tạo đơn</span>
                    <strong>{{ selectedOrder.NgayTaoDon || '---' }}</strong>
                  </div>

                  <div class="detail-item">
                    <span>Tổng tiền</span>
                    <strong>{{ formatCurrency(selectedOrder.tongTien) }}</strong>
                  </div>

                  <div class="detail-item">
                    <span>Phương thức thanh toán</span>
                    <strong>{{ getPaymentMethodText(selectedOrder.phuongThucThanhToan) }}</strong>
                  </div>

                  <div class="detail-item">
                    <span>Trạng thái thanh toán</span>
                    <strong>{{ getPaymentStatusText(selectedOrder.trangThaiThanhToan) }}</strong>
                  </div>

                  <div class="detail-item full-width" v-if="selectedOrder.GhiChu">
                    <span>Ghi chú</span>
                    <strong>{{ selectedOrder.GhiChu }}</strong>
                  </div>
                </div>
              </section>

              <section class="detail-section">
                <h4>Danh sách sản phẩm</h4>

                <div v-if="!selectedOrder.sanPhams || selectedOrder.sanPhams.length === 0" class="empty-state">
                  Đơn hàng không có sản phẩm
                </div>

                <div v-else class="products-list">
                  <div
                      v-for="(product, index) in selectedOrder.sanPhams"
                      :key="index"
                      class="product-item"
                  >
                    <div class="product-info">
                      <div class="product-name">
                        {{ product.tenSanPham || 'Sản phẩm không tên' }}
                      </div>
                      <div class="product-quantity">
                        Số lượng: {{ product.SoLuong }}
                      </div>
                    </div>
                    <div class="product-price">
                      {{ formatCurrency(product.giaTien) }}
                    </div>
                  </div>
                </div>
              </section>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<style
    scoped
    src="../../assets/styles/nhanvien/QLKhachHang/TrangQLKhachHang.css"
></style>

<style scoped>
.admin-filter-row {
  grid-template-columns:
    minmax(280px, 1fr)
    180px
    220px
    110px;
}

.search-icon {
  color: #94a3b8;
  font-size: 13px;
}

.reload-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  white-space: nowrap;
}

.table-wrapper {
  width: 100%;
  overflow-x: auto;
}

.admin-customer-table {
  min-width: 900px;
}

.employee-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 170px;
}

.employee-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.employee-info strong {
  color: #111827;
  font-size: 13px;
  font-weight: 600;
}

.employee-info small {
  color: #64748b;
  font-size: 12px;
}

.employee-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  background: #eef2ff;
  color: #4f46e5;
  box-shadow: inset 0 0 0 1px #c7d2fe;
  font-size: 11px;
  font-weight: 700;
}

.employee-avatar.large {
  width: 48px;
  height: 48px;
  font-size: 14px;
}

.unassigned-badge {
  display: inline-flex;
  align-items: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 12px;
  white-space: nowrap;
}

.detail-btn {
  gap: 7px;
}

.error-state {
  color: #b91c1c;
  text-align: center;
}

.error-state > div {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.error-state p {
  margin: 0;
}

.retry-btn {
  height: 34px;
  border: none;
  border-radius: 7px;
  padding: 0 16px;
  background: #dc2626;
  color: white;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.admin-summary {
  grid-template-columns: 1fr 1.5fr;
}

.summary-numbers {
  display: flex;
  align-items: center;
  gap: 26px;
  flex-wrap: wrap;
}

.summary-numbers span {
  display: flex;
  align-items: baseline;
  gap: 7px;
  color: #64748b;
}

.summary-numbers b {
  color: #dc2626;
  font-size: 22px;
}

.customer-modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 20000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(3px);
}

.customer-detail-modal {
  width: min(760px, 100%);
  max-height: calc(100vh - 48px);
  overflow: hidden;
  border-radius: 18px;
  background: white;
  box-shadow: 0 28px 80px rgba(15, 23, 42, 0.28);
  font-family: Arial, Helvetica, sans-serif;
}

/* Modal Transition Animations */
.modal-enter-active {
  transition: opacity 0.2s ease-out;
}

.modal-leave-active {
  transition: opacity 0.15s ease-in;
}

.modal-enter-active .customer-detail-modal {
  transition: transform 0.3s ease-out, opacity 0.3s ease-out;
}

.modal-leave-active .customer-detail-modal {
  transition: transform 0.2s ease-in, opacity 0.2s ease-in;
}

.modal-enter-from {
  opacity: 0;
}

.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .customer-detail-modal {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.modal-leave-to .customer-detail-modal {
  opacity: 0;
  transform: translateY(-10px) scale(0.98);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-customer-heading {
  display: flex;
  align-items: center;
  gap: 13px;
}

.modal-customer-heading h3 {
  margin: 0 0 5px;
  color: #0f172a;
  font-size: 19px;
}

.modal-customer-heading p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.modal-avatar {
  width: 44px;
  height: 44px;
}

.modal-close-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: #f8fafc;
  color: #64748b;
  cursor: pointer;
  font-size: 17px;
}

.modal-body {
  max-height: calc(100vh - 145px);
  overflow-y: auto;
  padding: 22px 24px 26px;
}

.detail-section + .detail-section {
  margin-top: 22px;
  padding-top: 22px;
  border-top: 1px solid #eef2f7;
}

.detail-section h4 {
  margin: 0 0 14px;
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.detail-item,
.status-detail-row > div {
  display: flex;
  flex-direction: column;
  gap: 7px;
  padding: 13px 14px;
  border: 1px solid #e9eef5;
  border-radius: 10px;
  background: #fafcff;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-item span,
.status-detail-row > div > span {
  color: #64748b;
  font-size: 12px;
}

.detail-item strong {
  color: #0f172a;
  font-size: 13px;
  font-weight: 600;
  overflow-wrap: anywhere;
}

.status-detail-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.assigned-employee-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 15px;
  border: 1px solid #e0e7ff;
  border-radius: 12px;
  background: #f8faff;
}

.assigned-employee-card strong {
  color: #111827;
}

.assigned-employee-card p {
  margin: 5px 0 0;
  color: #64748b;
  font-size: 13px;
}

.not-assigned-message {
  padding: 16px;
  border: 1px dashed #cbd5e1;
  border-radius: 10px;
  background: #f8fafc;
  color: #64748b;
  font-size: 13px;
}

.note-content {
  margin: 0;
  padding: 14px;
  border-radius: 10px;
  background: #fff7ed;
  color: #7c2d12;
  line-height: 1.6;
  white-space: pre-wrap;
}

@media (max-width: 1200px) {
  .admin-filter-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .admin-summary {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 800px) {
  .admin-filter-row,
  .detail-grid,
  .status-detail-row {
    grid-template-columns: 1fr;
  }

  .detail-item.full-width {
    grid-column: auto;
  }

  .customer-modal-overlay {
    padding: 12px;
  }

  .modal-header,
  .modal-body {
    padding-left: 16px;
    padding-right: 16px;
  }
}
.button-svg-icon {
  width: 16px;
  height: 16px;
  display: block;
  flex-shrink: 0;
}

.reload-btn,
.detail-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
}

.reload-btn .button-svg-icon {
  width: 15px;
  height: 15px;
}

.detail-btn .button-svg-icon {
  width: 16px;
  height: 16px;
}

/* Orders List Styles */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  padding: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #fafcff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.order-item:hover {
  border-color: #c7d2fe;
  background: #f8faff;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.1);
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.order-header strong {
  color: #0f172a;
  font-size: 14px;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 6px;
}

.order-info span {
  color: #64748b;
  font-size: 12px;
}

.order-products-count {
  color: #94a3b8;
  font-size: 11px;
}

.loading-state,
.empty-state {
  padding: 20px;
  text-align: center;
  color: #64748b;
  font-size: 13px;
  border: 1px dashed #cbd5e1;
  border-radius: 10px;
  background: #f8fafc;
}

/* Products List Styles */
.products-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.product-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fafcff;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name {
  color: #0f172a;
  font-size: 13px;
  font-weight: 600;
}

.product-quantity {
  color: #64748b;
  font-size: 12px;
}

.product-price {
  color: #dc2626;
  font-size: 14px;
  font-weight: 700;
}

.badge.green {
  background: #dcfce7;
  color: #166534;
}

.badge.orange {
  background: #ffedd5;
  color: #9a3412;
}

.badge.red {
  background: #fee2e2;
  color: #991b1b;
}

.badge.blue {
  background: #dbeafe;
  color: #1e40af;
}
</style>