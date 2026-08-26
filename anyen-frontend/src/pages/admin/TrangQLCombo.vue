<template>
  <main class="combo-admin-list">
    <header class="list-header">
      <div>
        <p class="eyebrow">QUẢN TRỊ COMBO</p>
        <h1>Quản lý combo</h1>
        <p>Combo chỉ do Admin tạo và có thể dùng sản phẩm của nhiều đối tác.</p>
      </div>

      <RouterLink class="primary-button" to="/admin/tao-combo">
        <i class="fa-solid fa-plus"></i>
        Tạo combo mới
      </RouterLink>
    </header>

    <div v-if="notice" class="notice notice-success">
      <i class="fa-solid fa-circle-check"></i>
      <span>{{ notice }}</span>
      <button type="button" @click="notice = ''">×</button>
    </div>

    <div v-if="pageError" class="notice notice-error">
      <i class="fa-solid fa-circle-exclamation"></i>
      <span>{{ pageError }}</span>
      <button type="button" @click="pageError = ''">×</button>
    </div>

    <section class="filter-card">
      <label class="search-box">
        <i class="fa-solid fa-magnifying-glass"></i>
        <input
            v-model.trim="keyword"
            type="search"
            placeholder="Tìm theo tên combo hoặc người tạo..."
        />
      </label>

      <label class="status-filter">
        <span>Trạng thái</span>
        <select v-model="statusFilter">
          <option value="">Tất cả</option>
          <option value="1">Đang hoạt động</option>
          <option value="0">Đang ẩn</option>
          <option value="2">Ngừng kinh doanh</option>
        </select>
      </label>

      <button class="refresh-button" type="button" :disabled="loading" @click="loadCombos">
        <i class="fa-solid fa-rotate" :class="{ 'fa-spin': loading }"></i>
        Tải lại
      </button>
    </section>

    <section class="list-card">
      <div class="list-card-heading">
        <div>
          <h2>Danh sách combo</h2>
          <p>{{ filteredCombos.length }} / {{ combos.length }} combo</p>
        </div>
        <div class="status-legend">
          <span><i class="dot active"></i> Hoạt động</span>
          <span><i class="dot hidden"></i> Ẩn</span>
          <span><i class="dot stopped"></i> Ngừng</span>
        </div>
      </div>

      <div v-if="loading" class="list-state">
        <span class="spinner-border spinner-border-sm"></span>
        Đang tải danh sách combo...
      </div>

      <div v-else-if="filteredCombos.length === 0" class="list-state empty">
        <i class="fa-solid fa-box-open"></i>
        <strong>Chưa có combo phù hợp</strong>
        <span>Hãy tạo combo mới hoặc thay đổi bộ lọc.</span>
      </div>

      <div v-else class="table-wrap">
        <table class="combo-table">
          <thead>
          <tr>
            <th>Combo</th>
            <th>3 ảnh đại diện</th>
            <th>Sản phẩm đã tick</th>
            <th>Giá</th>
            <th>Người tạo</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="combo in filteredCombos" :key="combo.comboId">
            <td class="combo-name-cell">
              <strong>{{ combo.tenCombo }}</strong>
              <span>#{{ combo.comboId }}</span>
              <small :title="combo.moTa">{{ combo.moTa || 'Chưa có mô tả' }}</small>
            </td>

            <td>
              <div class="cover-stack">
                <div
                    v-for="slot in 3"
                    :key="slot"
                    class="cover-thumb"
                    :class="{ missing: !combo.hinhAnhDaiDiens?.[slot - 1] }"
                >
                  <img
                      v-if="combo.hinhAnhDaiDiens?.[slot - 1]"
                      :src="normalizeImage(combo.hinhAnhDaiDiens[slot - 1])"
                      :alt="`Ảnh ${slot}`"
                      @error="useFallbackImage"
                  />
                  <i v-else class="fa-regular fa-image"></i>
                </div>
              </div>
              <small
                  class="image-count-text"
                  :class="{ invalid: (combo.hinhAnhDaiDiens?.length || 0) !== 3 }"
              >
                {{ combo.hinhAnhDaiDiens?.length || 0 }}/3 ảnh
                · {{ combo.hinhAnhQuyTrinhs?.length || 0 }} ảnh quy trình
              </small>
            </td>

            <td>
              <strong>{{ combo.sanPhams?.length || 0 }} sản phẩm</strong>
              <div class="type-chips">
                  <span
                      v-for="type in productTypes(combo)"
                      :key="type"
                  >{{ type }}</span>
              </div>
            </td>

            <td class="price-cell">
              <strong>{{ formatMoney(combo.gia) }}</strong>
              <small>Tổng lẻ: {{ formatMoney(combo.tongGiaSanPham) }}</small>
            </td>

            <td>
              <strong>{{ combo.tenNguoiTao || 'Không xác định' }}</strong>
              <small v-if="combo.maNhanVienTao">NV #{{ combo.maNhanVienTao }}</small>
            </td>

            <td>
              <select
                  class="inline-status"
                  :class="statusClass(combo.trangThai)"
                  :value="combo.trangThai"
                  :disabled="updatingId === combo.comboId"
                  @change="changeStatus(combo, $event.target.value)"
              >
                <option :value="1">Đang hoạt động</option>
                <option :value="0">Đang ẩn</option>
                <option :value="2">Ngừng kinh doanh</option>
              </select>
            </td>

            <td>
              <RouterLink
                  class="edit-button"
                  :to="`/admin/combo/${combo.comboId}/chinh-sua`"
              >
                <i class="fa-solid fa-pen-to-square"></i>
                Chỉnh sửa
              </RouterLink>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  getCombosAdmin,
  updateTrangThaiComboAdmin
} from "../../services/comboAdminService.js";

const route = useRoute();
const router = useRouter();
const combos = ref([]);
const loading = ref(false);
const updatingId = ref(null);
const keyword = ref("");
const statusFilter = ref("");
const pageError = ref("");
const notice = ref("");

const fallbackImage =
    "data:image/svg+xml;charset=UTF-8," +
    encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="200" height="140">
      <rect width="100%" height="100%" fill="#f3f4f6"/>
      <text x="50%" y="50%" text-anchor="middle" dominant-baseline="middle"
        font-family="Arial" font-size="18" fill="#9ca3af">Chưa có ảnh</text>
    </svg>
  `);

const filteredCombos = computed(() => {
  const search = keyword.value.toLocaleLowerCase("vi");
  return combos.value.filter((combo) => {
    const matchesSearch = !search || [
      combo.tenCombo,
      combo.tenNguoiTao,
      combo.comboId
    ].some((value) => String(value ?? "").toLocaleLowerCase("vi").includes(search));

    const matchesStatus = statusFilter.value === ""
        || Number(combo.trangThai) === Number(statusFilter.value);

    return matchesSearch && matchesStatus;
  });
});

const normalizeImage = (url) => {
  if (!url) return fallbackImage;
  if (/^(https?:|data:|blob:)/i.test(url)) return url;
  if (url.startsWith("//")) return `https:${url}`;
  if (url.startsWith("/")) return `http://localhost:8080${url}`;
  return `http://localhost:8080/${url}`;
};

const useFallbackImage = (event) => {
  event.target.onerror = null;
  event.target.src = fallbackImage;
};

const formatMoney = (value) =>
    `${Number(value || 0).toLocaleString("vi-VN")} đ`;

const productTypes = (combo) => [
  ...new Set((combo.sanPhams || []).map((product) => product.loai || "Chưa phân loại"))
].slice(0, 3);

const statusClass = (status) => {
  if (Number(status) === 1) return "status-active";
  if (Number(status) === 2) return "status-stopped";
  return "status-hidden";
};

const extractError = (error, fallback) =>
    error?.response?.data?.detail ||
    error?.response?.data?.message ||
    error?.message ||
    fallback;

const loadCombos = async () => {
  loading.value = true;
  pageError.value = "";
  try {
    const response = await getCombosAdmin();
    combos.value = Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    pageError.value = extractError(error, "Không thể tải danh sách combo.");
  } finally {
    loading.value = false;
  }
};

const changeStatus = async (combo, rawStatus) => {
  const previousStatus = combo.trangThai;
  const status = Number(rawStatus);
  combo.trangThai = status;
  updatingId.value = combo.comboId;
  pageError.value = "";
  try {
    const response = await updateTrangThaiComboAdmin(combo.comboId, status);
    Object.assign(combo, response.data);
    notice.value = `Đã cập nhật trạng thái “${combo.tenCombo}”.`;
  } catch (error) {
    combo.trangThai = previousStatus;
    pageError.value = extractError(error, "Không thể cập nhật trạng thái combo.");
  } finally {
    updatingId.value = null;
  }
};

onMounted(async () => {
  if (route.query.saved === "created") {
    notice.value = "Tạo combo thành công.";
  } else if (route.query.saved === "updated") {
    notice.value = "Cập nhật combo thành công.";
  }
  if (route.query.saved) {
    await router.replace({ path: route.path });
  }
  await loadCombos();
});
</script>

<style scoped src="../../assets/styles/admin/QLCombo/ComboAdmin.css"></style>