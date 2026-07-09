<template>
  <div class="container py-4">
    <!-- TIÊU ĐỀ -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3 class="fw-bold text-dark mb-0">Duyệt Sản Phẩm Đối Tác</h3>
      <button class="btn btn-outline-secondary d-flex align-items-center gap-2 px-3 shadow-sm" @click="fetchDanhSachSanPhamChoDuyet">
        <i class="fa-solid fa-rotate-right"></i> Làm mới
      </button>
    </div>

    <!-- BẢNG DANH SÁCH -->
    <div class="card border-0 shadow-sm rounded-3 overflow-hidden">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary">
          <tr>
            <th class="ps-4">Mã SP</th>
            <th>Hình ảnh</th>
            <th>Tên sản phẩm</th>
            <th>Loại</th>
            <th>Giá bán</th>
            <th>Trạng thái</th>
            <th class="text-end pe-4">Hành động</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="sp in danhSachSanPham" :key="sp.id">
            <td class="ps-4 fw-semibold text-muted">#{{ sp.id }}</td>
            <td>
              <img v-if="sp.image" :src="sp.image" alt="Ảnh SP" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;" />
              <span v-else class="text-muted small">Không có ảnh</span>
            </td>
            <td class="fw-medium text-dark">{{ sp.name }}</td>
            <td><span class="badge bg-light text-dark border border-secondary-subtle px-2 py-1">{{ sp.loai }}</span></td>
            <td><code class="text-danger fw-bold">{{ formatCurrency(sp.price) }}</code></td>
            <td>
              <span class="badge bg-warning text-dark px-2.5 py-1.5 rounded-pill">
                Chờ duyệt
              </span>
            </td>
            <td class="text-end pe-4">
              <button
                  class="btn btn-sm btn-success px-3 rounded-2 shadow-sm"
                  @click="confirmDuyet(sp)"
                  :disabled="loadingStates[sp.id]"
              >
                <span v-if="loadingStates[sp.id]" class="spinner-border spinner-border-sm me-1"></span>
                <i v-else class="fa-solid fa-check me-1"></i>
                Duyệt hiển thị
              </button>
            </td>
          </tr>
          <tr v-if="danhSachSanPham.length === 0 && !isLoading">
            <td colspan="7" class="text-center py-4 text-muted">Không có sản phẩm nào đang chờ duyệt.</td>
          </tr>
          <tr v-if="isLoading">
            <td colspan="7" class="text-center py-4 text-muted">Đang tải dữ liệu...</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import api from "../../api/api.js";

const danhSachSanPham = ref([]);
const loadingStates = reactive({});
const isLoading = ref(false);

const fetchDanhSachSanPhamChoDuyet = async () => {
  isLoading.value = true;
  try {
    const res = await api.get("/api/san-pham/cho-duyet?page=1&pageSize=100");
    danhSachSanPham.value = res.data.items || [];
  } catch (error) {
    console.error("Lỗi khi tải danh sách sản phẩm chờ duyệt:", error);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchDanhSachSanPhamChoDuyet();
});

const confirmDuyet = async (sp) => {
  if (!confirm(`Bạn có chắc chắn muốn duyệt hiển thị sản phẩm [${sp.name}]?`)) return;

  loadingStates[sp.id] = true;
  try {
    await api.patch(`/api/san-pham/${sp.id}/duyet`);
    alert("Duyệt sản phẩm thành công!");
    
    // Xóa sản phẩm khỏi danh sách chờ
    danhSachSanPham.value = danhSachSanPham.value.filter(item => item.id !== sp.id);
  } catch (error) {
    console.error("Lỗi khi duyệt sản phẩm:", error);
    alert("Đã xảy ra lỗi khi duyệt sản phẩm.");
  } finally {
    loadingStates[sp.id] = false;
  }
};

const formatCurrency = (value) => {
  if (!value) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
};
</script>

<style scoped>
/* Sử dụng Bootstrap 5 utility classes */
</style>
