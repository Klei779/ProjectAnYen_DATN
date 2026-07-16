<script setup>
import {ref, computed, watch, onMounted} from "vue";
import api from "../../api/api.js";
import { ElMessage } from "element-plus";

const API_URL = "/api/doi-tac/san-pham";
const NGUNG_BAN_API = (id) => `${API_URL}/${id}/an`;
const BAN_LAI_API = (id) => `${API_URL}/${id}/hien`;

const editingProduct = ref(null);
const activeTab = ref("list");
const keyword = ref("");
const selectedStatus = ref("");
const selectedCategory = ref("");

const currentPage = ref(1);
const pageSize = 16;

const loading = ref(false);
const imagePreview = ref("");
const products = ref([]);
const stockDrafts = ref({});
const savingStockId = ref(null);
const total = ref(0);
const selectedProductDetail = ref(null);
const loadingProductDetail = ref(false);

const emptyProduct = () => ({
  tenSanPham: "",
  loai: "",
  noiThat: "",
  quyCach: "",
  tonGiao: "",
  giaTien: "",
  maDoiTac: "",
  soLuong: "",
  thietKe: "",
  xuatXu: "",
  ghiChu: "",
  khuyenMai: "",
  mauSac: "",
  hinhAnh: "",
  vatLieu: "",
  trangThai: "Đang bán",
  kichThuoc: "",
  trongLuong: "",
  cnsx: "",
});

const newProduct = ref(emptyProduct());

const getProductId = (sp) => sp.id ?? sp.maSanPham ?? sp.maSP;

const getProductStatus = (sp) => {
  const approvalCode = Number(sp.trangThai ?? sp.status ?? 2);
  if (approvalCode === 0) return "Ngưng bán";
  if (approvalCode === 2) return "Chờ duyệt";
  const stock = Number(sp.stock ?? sp.soLuong ?? 0);
  return stock > 0 ? "Còn hàng" : "Hết hàng";
};

const getApprovalLabel = (code) => {
  if (Number(code) === 1) return "Đã duyệt";
  if (Number(code) === 2) return "Chờ duyệt";
  return "Đang ẩn / Đã từ chối";
};

const getApprovalClass = (code) => {
  if (Number(code) === 1) return "approved";
  if (Number(code) === 2) return "pending";
  return "hidden";
};

const normalizeProduct = (sp) => {
  const id = getProductId(sp);
  const stock = Number(sp.stock ?? sp.soLuong ?? 0);
  const status = getProductStatus(sp);
  const trangThaiCode = Number(sp.trangThai ?? sp.status ?? 2);

  return {
    id,
    name: sp.name || sp.tenSanPham || "Chưa có tên",
    sku: sp.sku || `SP-${id}`,
    category: sp.category || sp.loai || "Chưa phân loại",
    price: Number(sp.price ?? sp.giaTien ?? 0),
    stock,
    status,
    image:
        sp.image ||
        sp.hinhAnh ||
        "https://via.placeholder.com/350x180?text=San+Pham",

    maSanPham: id,
    tenSanPham: sp.tenSanPham || sp.name || "",
    loai: sp.loai || sp.category || "",
    noiThat: sp.noiThat || "",
    quyCach: sp.quyCach || "",
    tonGiao: sp.tonGiao || "",
    giaTien: Number(sp.giaTien ?? sp.price ?? 0),
    maDoiTac: sp.maDoiTac || "",
    soLuong: stock,
    thietKe: sp.thietKe || "",
    xuatXu: sp.xuatXu || "",
    ghiChu: sp.ghiChu || "",
    khuyenMai: sp.khuyenMai ?? "",
    mauSac: sp.mauSac || "",
    hinhAnh: sp.hinhAnh || sp.image || "",
    vatLieu: sp.vatLieu || "",
    trangThaiCode,
    trangThai: trangThaiCode === 1 ? "Đang bán" : trangThaiCode === 2 ? "Chờ duyệt" : "Ngưng bán",
    kichThuoc: sp.kichThuoc || "",
    trongLuong: sp.trongLuong || "",
    cnsx: sp.cnsx || sp.CNSX || "",
    chiTietList: Array.isArray(sp.chiTietList) ? sp.chiTietList : [],
    hinhAnhList: Array.isArray(sp.hinhAnhList) ? sp.hinhAnhList : [],
  };
};

const loadProducts = async () => {
  try {
    loading.value = true;

    const response = await api.get(API_URL, {
      params: {
        // Spring Pageable bắt đầu từ 0. Nếu để page: 1 + pageSize: 9999
        // thì backend sẽ nhảy sang trang thứ 2 và dễ trả về mảng rỗng.
        page: 0,
        pageSize: 9999,
        sortBy: "newest",
      },
    });

    const data = response.data || {};
    const items = Array.isArray(data)
        ? data
        : Array.isArray(data.items)
            ? data.items
            : Array.isArray(data.content)
                ? data.content
                : Array.isArray(data.data)
                    ? data.data
                    : [];

    products.value = items.map(normalizeProduct);
    stockDrafts.value = Object.fromEntries(
        products.value.map(product => [product.id, product.stock])
    );
    total.value = data.total ?? data.totalElements ?? products.value.length;
  } catch (error) {
    console.error("Lỗi load sản phẩm đối tác:", error);

    if (error.response?.status === 403) {
      alert("Bạn không có quyền xem sản phẩm đối tác. Hãy đăng nhập bằng tài khoản đối tác.");
    } else if (error.response?.status === 401) {
      alert("Bạn chưa đăng nhập hoặc token hết hạn.");
    } else {
      alert("Không thể tải danh sách sản phẩm.");
    }
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadProducts();
});

const categories = computed(() => {
  const map = new Map();

  products.value.forEach((p) => {
    const name = p.category || p.loai || "Chưa phân loại";

    if (!map.has(name)) {
      map.set(name, {
        id: name,
        name,
        total: 0,
        hidden: 0,
      });
    }

    const cate = map.get(name);
    cate.total += 1;

    if (isHiddenProduct(p)) {
      cate.hidden += 1;
    }
  });

  return Array.from(map.values()).map((cate, index) => ({
    id: index + 1,
    name: cate.name,
    total: cate.total,
    status: cate.total > 0 && cate.hidden === cate.total ? "Ngưng bán" : "Đang bán",
  }));
});

const availableProducts = computed(() =>
    products.value.filter((p) => p.trangThaiCode === 1)
);

const outOfStockProducts = computed(() =>
    products.value.filter((p) => p.status === "Hết hàng")
);

const stoppedProducts = computed(() =>
    products.value.filter(p => p.trangThaiCode === 0)
);

const filteredProducts = computed(() => {
  const searchText = keyword.value.trim().toLowerCase();

  return products.value.filter((p) => {
    const matchKeyword =
        !searchText ||
        (p.name || "").toLowerCase().includes(searchText) ||
        (p.sku || "").toLowerCase().includes(searchText) ||
        (p.category || "").toLowerCase().includes(searchText);

    const matchCategory =
        !selectedCategory.value || p.category === selectedCategory.value || p.loai === selectedCategory.value;

    let matchStatus = true;

    if (selectedStatus.value === "con-hang") {
      matchStatus = p.status === "Còn hàng";
    }

    if (selectedStatus.value === "het-hang") {
      matchStatus = p.status === "Hết hàng";
    }

    if (selectedStatus.value === "ngung-ban") {
      matchStatus = p.trangThaiCode === 0;
    }

    return matchKeyword && matchCategory && matchStatus;
  });
});

const totalPages = computed(() => Math.ceil(filteredProducts.value.length / pageSize) || 1);

const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return filteredProducts.value.slice(start, start + pageSize);
});

const startItem = computed(() =>
    filteredProducts.value.length === 0 ? 0 : (currentPage.value - 1) * pageSize + 1
);

const endItem = computed(() =>
    Math.min(currentPage.value * pageSize, filteredProducts.value.length)
);

watch([keyword, selectedStatus, selectedCategory, activeTab], () => {
  currentPage.value = 1;
});

const formatPrice = (price) => {
  return new Intl.NumberFormat("vi-VN").format(Number(price || 0)) + " đ";
};

const changeTab = (tab) => {
  activeTab.value = tab;

  if (tab === "create" && !editingProduct.value) {
    resetForm();
  }
};

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

const resetForm = () => {
  editingProduct.value = null;
  newProduct.value = emptyProduct();
  imagePreview.value = "";
};

const validateProduct = () => {
  if (
      !newProduct.value.tenSanPham ||
      !newProduct.value.loai ||
      newProduct.value.giaTien === "" ||
      newProduct.value.soLuong === ""
  ) {
    alert("Vui lòng nhập tên sản phẩm, loại, giá tiền và số lượng!");
    return false;
  }

  if (Number(newProduct.value.giaTien) < 0 || Number(newProduct.value.soLuong) < 0) {
    alert("Giá tiền và số lượng không được nhỏ hơn 0!");
    return false;
  }

  return true;
};

const buildPayload = () => ({
  tenSanPham: newProduct.value.tenSanPham,
  loai: newProduct.value.loai,
  noiThat: newProduct.value.noiThat,
  quyCach: newProduct.value.quyCach,
  tonGiao: newProduct.value.tonGiao,
  giaTien: Number(newProduct.value.giaTien || 0),
  maDoiTac: newProduct.value.maDoiTac || null,
  soLuong: Number(newProduct.value.soLuong || 0),
  thietKe: newProduct.value.thietKe,
  xuatXu: newProduct.value.xuatXu,
  ghiChu: newProduct.value.ghiChu,
  khuyenMai: newProduct.value.khuyenMai === "" ? null : Number(newProduct.value.khuyenMai),
  mauSac: newProduct.value.mauSac,
  hinhAnh: typeof newProduct.value.hinhAnh === "string" ? newProduct.value.hinhAnh : "",
  vatLieu: newProduct.value.vatLieu,
  trangThai: newProduct.value.trangThai,
  kichThuoc: newProduct.value.kichThuoc,
  trongLuong: newProduct.value.trongLuong,
  cnsx: newProduct.value.cnsx,
});

const editProduct = (product) => {
  editingProduct.value = product;

  newProduct.value = {
    tenSanPham: product.tenSanPham || product.name || "",
    loai: product.loai || product.category || "",
    noiThat: product.noiThat || "",
    quyCach: product.quyCach || "",
    tonGiao: product.tonGiao || "",
    giaTien: product.giaTien ?? product.price ?? 0,
    maDoiTac: product.maDoiTac || "",
    soLuong: product.soLuong ?? product.stock ?? 0,
    thietKe: product.thietKe || "",
    xuatXu: product.xuatXu || "",
    ghiChu: product.ghiChu || "",
    khuyenMai: product.khuyenMai ?? "",
    mauSac: product.mauSac || "",
    hinhAnh: product.hinhAnh || product.image || "",
    vatLieu: product.vatLieu || "",
    trangThai: product.trangThai || (product.status === "Ẩn" ? "Ẩn" : "Đang bán"),
    kichThuoc: product.kichThuoc || "",
    trongLuong: product.trongLuong || "",
    cnsx: product.cnsx || "",
  };

  imagePreview.value = product.image || "";
  activeTab.value = "create";
};

const updateProduct = async () => {
  if (!editingProduct.value || !validateProduct()) return;

  try {
    await api.put(`${API_URL}/${editingProduct.value.id}`, buildPayload());

    alert("Cập nhật sản phẩm thành công!");
    resetForm();
    activeTab.value = "list";
    await loadProducts();
  } catch (error) {
    console.error("Lỗi cập nhật sản phẩm:", error);
    alert("Không thể cập nhật sản phẩm.");
  }
};

const getDraftStock = (product) => {
  const value = stockDrafts.value[product.id];
  return Math.max(0, Number(value ?? product.stock ?? 0));
};

const isStockDirty = (product) => getDraftStock(product) !== Number(product.stock || 0);

const getDraftStockStatus = (product) => {
  if (product.trangThaiCode === 0) return "Ngưng bán";
  if (product.trangThaiCode === 2) return "Chờ duyệt";
  return getDraftStock(product) > 0 ? "Còn hàng" : "Hết hàng";
};

const changeStockDraft = (product, delta) => {
  stockDrafts.value = {
    ...stockDrafts.value,
    [product.id]: Math.max(0, getDraftStock(product) + delta),
  };
};

const increaseStock = (product) => changeStockDraft(product, 1);
const decreaseStock = (product) => changeStockDraft(product, -1);

const saveStock = async (product) => {
  if (!isStockDirty(product) || savingStockId.value === product.id) return;

  try {
    savingStockId.value = product.id;
    await api.patch(`${API_URL}/${product.id}/ton-kho`, {
      soLuong: getDraftStock(product),
    });

    ElMessage.success("Đã lưu số lượng tồn kho");
    await loadProducts();
  } catch (error) {
    console.error("Lỗi lưu tồn kho:", error);
    ElMessage.error(error?.response?.data?.message || "Không thể lưu tồn kho");
  } finally {
    savingStockId.value = null;
  }
};

const escapeExcelCell = (value) => String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");

const exportProductsToExcel = () => {
  const rows = filteredProducts.value;
  if (!rows.length) {
    ElMessage.warning("Không có sản phẩm để xuất");
    return;
  }

  const headers = [
    "STT", "Mã sản phẩm", "Tên sản phẩm", "Loại", "Giá bán", "Khuyến mãi",
    "Tồn kho", "Trạng thái duyệt", "Trạng thái bán", "Vật liệu", "Màu sắc",
    "Kích thước", "Xuất xứ", "CNSX", "Ghi chú"
  ];
  const bodyRows = rows.map((product, index) => [
    index + 1,
    product.sku,
    product.name,
    product.category,
    Number(product.price || 0),
    Number(product.khuyenMai || 0),
    Number(product.stock || 0),
    getApprovalLabel(product.trangThaiCode),
    product.status,
    product.vatLieu,
    product.mauSac,
    product.kichThuoc,
    product.xuatXu,
    product.cnsx,
    product.ghiChu,
  ]);

  const table = `
    <table border="1">
      <thead><tr>${headers.map(header => `<th>${escapeExcelCell(header)}</th>`).join("")}</tr></thead>
      <tbody>${bodyRows.map(row => `<tr>${row.map(cell => `<td>${escapeExcelCell(cell)}</td>`).join("")}</tr>`).join("")}</tbody>
    </table>`;
  const workbook = `<!DOCTYPE html><html><head><meta charset="UTF-8"></head><body>${table}</body></html>`;
  const blob = new Blob(["\ufeff", workbook], { type: "application/vnd.ms-excel;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `danh-sach-san-pham-${new Date().toISOString().slice(0, 10)}.xls`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
  ElMessage.success(`Đã xuất ${rows.length} sản phẩm`);
};

const isHiddenProduct = (product) => {
  return product.trangThaiCode === 0;
};

const ngungBanProduct = async (product) => {
  if (!confirm(`Ngưng bán "${product.name}" ?`)) {
    return;
  }

  try {
    await api.patch(NGUNG_BAN_API(product.id));

    alert("Ngưng bán thành công!");

    await loadProducts();
  } catch (e) {
    console.error(e);

    alert(
        e.response?.data ||
        "Không thể ngưng bán sản phẩm."
    );
  }
};

const banLaiProduct = async (product) => {
  try {
    await api.patch(BAN_LAI_API(product.id));

    alert("Đã bán lại sản phẩm.");

    await loadProducts();
  } catch (e) {
    console.error(e);

    alert(
        e.response?.data ||
        "Không thể bán lại."
    );
  }
};

const showDetail = async (product) => {
  loadingProductDetail.value = true;
  selectedProductDetail.value = product;

  try {
    const response = await api.get(`${API_URL}/${product.id}`);
    selectedProductDetail.value = normalizeProduct(response.data || product);
  } catch (error) {
    console.error("Lỗi tải chi tiết sản phẩm:", error);
    alert(
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        "Không thể tải chi tiết sản phẩm."
    );
    selectedProductDetail.value = null;
  } finally {
    loadingProductDetail.value = false;
  }
};

const closeDetail = () => {
  selectedProductDetail.value = null;
};


const buildPayloadFromProduct = (product, overrides = {}) => ({
  tenSanPham: overrides.tenSanPham ?? product.tenSanPham ?? product.name ?? "",
  loai: overrides.loai ?? product.loai ?? product.category ?? "",
  noiThat: overrides.noiThat ?? product.noiThat ?? "",
  quyCach: overrides.quyCach ?? product.quyCach ?? "",
  tonGiao: overrides.tonGiao ?? product.tonGiao ?? "",
  giaTien: Number(overrides.giaTien ?? product.giaTien ?? product.price ?? 0),
  maDoiTac: overrides.maDoiTac ?? product.maDoiTac ?? null,
  soLuong: Number(overrides.soLuong ?? product.soLuong ?? product.stock ?? 0),
  thietKe: overrides.thietKe ?? product.thietKe ?? "",
  xuatXu: overrides.xuatXu ?? product.xuatXu ?? "",
  ghiChu: overrides.ghiChu ?? product.ghiChu ?? "",
  khuyenMai:
      (overrides.khuyenMai ?? product.khuyenMai ?? "") === ""
          ? null
          : Number(overrides.khuyenMai ?? product.khuyenMai ?? 0),
  mauSac: overrides.mauSac ?? product.mauSac ?? "",
  hinhAnh: overrides.hinhAnh ?? product.hinhAnh ?? product.image ?? "",
  vatLieu: overrides.vatLieu ?? product.vatLieu ?? "",
  trangThai: Number(overrides.trangThai ?? product.trangThaiCode ?? 1),
  kichThuoc: overrides.kichThuoc ?? product.kichThuoc ?? "",
  trongLuong: overrides.trongLuong ?? product.trongLuong ?? "",
  cnsx: overrides.cnsx ?? product.cnsx ?? "",
});

const addCategory = () => {
  resetForm();
  activeTab.value = "create";
};

const viewCategory = (cate) => {
  selectedCategory.value = cate.name;
  selectedStatus.value = "";
  keyword.value = "";
  activeTab.value = "list";
  currentPage.value = 1;
};

const editCategory = async (cate) => {
  const newName = prompt("Nhập tên danh mục mới:", cate.name);

  if (!newName || newName.trim() === cate.name) return;

  const list = products.value.filter(
      (p) => (p.category || p.loai || "Chưa phân loại") === cate.name
  );

  if (list.length === 0) return;

  try {
    await Promise.all(
        list.map((product) =>
            api.put(`${API_URL}/${product.id}`, buildPayloadFromProduct(product, {
              loai: newName.trim(),
            }))
        )
    );

    alert("Cập nhật danh mục thành công!");
    selectedCategory.value = "";
    await loadProducts();
  } catch (error) {
    console.error("Lỗi cập nhật danh mục:", error);
    alert("Không thể cập nhật danh mục.");
  }
};

const hideCategory = async (cate) => {
  if (!confirm(`Bạn có chắc muốn ẩn toàn bộ sản phẩm trong danh mục "${cate.name}" không?`)) {
    return;
  }

  const list = products.value.filter(
      (p) => (p.category || p.loai || "Chưa phân loại") === cate.name && !isHiddenProduct(p)
  );

  if (list.length === 0) return;

  try {
    await Promise.all(list.map((product) => api.patch(NGUNG_BAN_API(product.id))));
    alert("Đã ẩn danh mục sản phẩm!");
    await loadProducts();
  } catch (error) {
    console.error("Lỗi ẩn danh mục:", error);
    alert("Không thể ẩn danh mục sản phẩm.");
  }
};

const showCategory = async (cate) => {
  const list = products.value.filter(
      (p) => (p.category || p.loai || "Chưa phân loại") === cate.name && isHiddenProduct(p)
  );

  if (list.length === 0) return;

  try {
    await Promise.all(list.map((product) => api.patch(BAN_LAI_API(product.id))));
    alert("Đã hiện lại danh mục sản phẩm!");
    await loadProducts();
  } catch (error) {
    console.error("Lỗi hiện lại danh mục:", error);
    alert("Không thể hiện lại danh mục sản phẩm.");
  }
};

const handleImageUpload = (event) => {
  const file = event.target.files[0];
  if (!file) return;

  newProduct.value.hinhAnh = file;
  imagePreview.value = URL.createObjectURL(file);
};
</script>

<template>
  <div class="admin-layout">
    <section class="page-content">
      <div class="title-box">
        <h2>
          {{
            activeTab === "list"
                ? "Danh sách sản phẩm"
                : activeTab === "category"
                    ? "Danh mục sản phẩm"
                    : editingProduct
                        ? "Cập nhật sản phẩm"
                        : "Thêm sản phẩm mới"
          }}
        </h2>
        <p>Quản lý sản phẩm trên website đối tác.</p>
      </div>

      <div class="tabs">
        <button :class="{ active: activeTab === 'list' }" @click="changeTab('list')">
          Danh sách sản phẩm
        </button>
        <button :class="{ active: activeTab === 'category' }" @click="changeTab('category')">
          Danh mục sản phẩm
        </button>
      </div>

      <template v-if="activeTab === 'list'">
        <div class="filter-row">
          <button class="date-btn">
            01/05/2024 - 26/05/2024
            <i class="fa-regular fa-calendar"></i>
          </button>

          <select v-model="selectedStatus">
            <option value="">Tất cả trạng thái sản phẩm</option>
            <option value="con-hang">Còn hàng</option>
            <option value="het-hang">Hết hàng</option>
            <option value="ngung-ban">
              Ngưng bán
            </option>
          </select>

          <select v-model="selectedCategory">
            <option value="">Tất cả danh mục</option>
            <option v-for="cate in categories" :key="cate.id" :value="cate.name">
              {{ cate.name }}
            </option>
          </select>

          <div class="search-input">
            <input v-model="keyword" placeholder="Tìm kiếm sản phẩm..."/>
            <i class="fa-solid fa-magnifying-glass"></i>
          </div>

          <button class="export-btn" type="button" @click="exportProductsToExcel">
            <i class="fa-solid fa-download"></i>
            Xuất danh sách
          </button>
        </div>

        <div class="stats">
          <div class="stat-card">
            <div class="stat-icon red">
              <i class="fa-solid fa-table-cells-large"></i>
            </div>
            <div>
              <p>Tổng sản phẩm</p>
              <h3>{{ products.length }}</h3>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon green">
              <i class="fa-solid fa-tag"></i>
            </div>
            <div>
              <p>Sản phẩm đang bán</p>
              <h3>{{ availableProducts.length }}</h3>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon gray">
              <i class="fa-regular fa-clock"></i>
            </div>
            <div>
              <p>Sản phẩm hết hàng</p>
              <h3>{{ outOfStockProducts.length }}</h3>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon blue">
              <i class="fa-solid fa-list"></i>
            </div>
            <div>
              <p>Sản phẩm ngưng bán</p>
              <h3>{{ stoppedProducts.length }}</h3>
            </div>
          </div>
        </div>

        <p v-if="loading">Đang tải sản phẩm...</p>

        <div class="product-grid">
          <div class="product-card" v-for="product in paginatedProducts" :key="product.id">
            <div class="approval-bar" :class="getApprovalClass(product.trangThaiCode)">
              <i :class="product.trangThaiCode === 1 ? 'fa-solid fa-circle-check' : 'fa-regular fa-clock'"></i>
              {{ getApprovalLabel(product.trangThaiCode) }}
            </div>
            <img :src="product.image" :alt="product.name"/>

            <div class="product-info">
              <h3>{{ product.name }}</h3>

              <p class="product-sku">SKU: {{ product.sku }}</p>

              <p class="product-price">
                {{ formatPrice(product.price) }}
              </p>

              <div class="stock-row" :class="{ dirty: isStockDirty(product) }">
                <div class="stock-copy">
                  <p class="stock-label">Tồn kho: <strong>{{ getDraftStock(product) }}</strong></p>
                  <span
                      class="status"
                      :class="getDraftStockStatus(product) === 'Còn hàng' ? 'available' : 'empty'"
                  >
                    {{ getDraftStockStatus(product) }}
                  </span>
                  <small v-if="isStockDirty(product)" class="unsaved-stock">Chưa lưu</small>
                </div>

                <div class="stock-control">
                  <button type="button" :disabled="getDraftStock(product) === 0" @click="decreaseStock(product)">−</button>
                  <span>{{ getDraftStock(product) }}</span>
                  <button type="button" @click="increaseStock(product)">+</button>
                </div>
              </div>
            </div>

            <div class="card-actions">
              <button class="detail-btn" @click="showDetail(product)">
                <i class="fa-regular fa-eye"></i>
                chi tiết
              </button>

              <button class="edit-btn" @click="editProduct(product)">
                <i class="fa-solid fa-pen"></i>
                Sửa
              </button>

              <button
                  v-if="product.trangThaiCode === 1"
                  class="hide-btn"
                  @click="ngungBanProduct(product)"
              >
                <i class="fa-solid fa-ban"></i>
                Ngưng bán
              </button>

              <button
                  v-else-if="product.trangThaiCode === 0"
                  class="hide-btn"
                  @click="banLaiProduct(product)"
              >
                <i class="fa-solid fa-play"></i>
                Bán lại
              </button>

              <button
                  class="save-btn"
                  type="button"
                  :disabled="!isStockDirty(product) || savingStockId === product.id"
                  @click="saveStock(product)"
              >
                <i class="fa-regular fa-floppy-disk"></i>
                {{ savingStockId === product.id ? "Đang lưu" : "Lưu" }}
              </button>
            </div>
          </div>
        </div>

        <div class="pagination-row">
          <p>
            Hiển thị {{ startItem }} - {{ endItem }}
            trong {{ filteredProducts.length }} sản phẩm
          </p>

          <div class="pagination">
            <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1">
              <i class="fa-solid fa-chevron-left"></i>
            </button>

            <button
                v-for="page in totalPages"
                :key="page"
                :class="{ active: currentPage === page }"
                @click="changePage(page)"
            >
              {{ page }}
            </button>

            <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages">
              <i class="fa-solid fa-chevron-right"></i>
            </button>
          </div>
        </div>
      </template>

      <template v-if="activeTab === 'category'">
        <div class="category-header">
          <h4>Danh mục sản phẩm</h4>

          <button class="add-category-btn" @click="addCategory">
            <i class="fa-solid fa-plus"></i>
            Thêm danh mục
          </button>
        </div>

        <p v-if="loading">Đang tải danh mục sản phẩm...</p>

        <div class="category-table">
          <div class="table-head">
            <span>Tên danh mục</span>
            <span>Số sản phẩm</span>
            <span>Trạng thái</span>
            <span>Thao tác</span>
          </div>

          <div class="table-row" v-for="cate in categories" :key="cate.id">
            <span>{{ cate.name }}</span>

            <span>{{ cate.total }}</span>

            <span
                class="status category-status"
                :class="cate.status === 'Đang bán' ? 'available' : 'empty'"
            >
              {{ cate.status }}
            </span>

            <span class="table-actions">
              <button title="Sửa danh mục" @click="editCategory(cate)">
                <i class="fa-solid fa-pen"></i>
              </button>

              <button
                  v-if="cate.status === 'Đang bán'"
                  title="Ẩn danh mục"
                  @click="hideCategory(cate)"
              >
                <i class="fa-regular fa-trash-can"></i>
              </button>

              <button
                  v-else
                  title="Hiện lại danh mục"
                  @click="showCategory(cate)"
              >
                <i class="fa-regular fa-eye"></i>
              </button>

              <button title="Xem sản phẩm trong danh mục" @click="viewCategory(cate)">
                <i class="fa-regular fa-eye"></i>
              </button>
            </span>
          </div>
        </div>
      </template>


      <div v-if="selectedProductDetail" class="product-detail-overlay" @click.self="closeDetail">
        <article class="product-detail-modal">
          <button class="detail-close" type="button" @click="closeDetail"><i class="fa-solid fa-xmark"></i></button>
          <div class="detail-image-wrap">
            <img :src="selectedProductDetail.image" :alt="selectedProductDetail.name" />
            <span class="approval-bar detail-status" :class="getApprovalClass(selectedProductDetail.trangThaiCode)">
              {{ getApprovalLabel(selectedProductDetail.trangThaiCode) }}
            </span>
          </div>
          <div class="detail-content">
            <div v-if="loadingProductDetail" class="detail-loading">Đang tải chi tiết sản phẩm...</div>
            <template v-else>
              <div class="detail-heading-row">
                <div>
                  <p class="detail-eyebrow">SẢN PHẨM #{{ selectedProductDetail.id }}</p>
                  <h2>{{ selectedProductDetail.name }}</h2>
                </div>
                <span class="detail-stock-pill" :class="selectedProductDetail.stock > 0 ? 'in-stock' : 'out-stock'">
                  <i class="fa-solid fa-boxes-stacked"></i>
                  Tồn kho {{ selectedProductDetail.stock }}
                </span>
              </div>

              <div class="detail-price-row">
                <p class="detail-price">{{ formatPrice(selectedProductDetail.price) }}</p>
                <span v-if="Number(selectedProductDetail.khuyenMai || 0) > 0" class="detail-promotion">
                  Khuyến mãi {{ formatPrice(selectedProductDetail.khuyenMai) }}
                </span>
              </div>

              <div class="detail-grid">
                <div><span>Loại sản phẩm</span><strong>{{ selectedProductDetail.category || '—' }}</strong></div>
                <div><span>Vật liệu</span><strong>{{ selectedProductDetail.vatLieu || '—' }}</strong></div>
                <div><span>Nội thất</span><strong>{{ selectedProductDetail.noiThat || '—' }}</strong></div>
                <div><span>Màu sắc</span><strong>{{ selectedProductDetail.mauSac || '—' }}</strong></div>
                <div><span>Kích thước</span><strong>{{ selectedProductDetail.kichThuoc || '—' }}</strong></div>
                <div><span>Trọng lượng</span><strong>{{ selectedProductDetail.trongLuong || '—' }}</strong></div>
                <div><span>Quy cách</span><strong>{{ selectedProductDetail.quyCach || '—' }}</strong></div>
                <div><span>Thiết kế</span><strong>{{ selectedProductDetail.thietKe || '—' }}</strong></div>
                <div><span>Tôn giáo</span><strong>{{ selectedProductDetail.tonGiao || '—' }}</strong></div>
                <div><span>Xuất xứ / CNSX</span><strong>{{ selectedProductDetail.xuatXu || '—' }} / {{ selectedProductDetail.cnsx || '—' }}</strong></div>
              </div>

              <section class="database-detail-section">
                <div class="detail-section-heading">
                  <div>
                    <h3>Chi tiết cấu tạo sản phẩm</h3>
                  </div>
                  <span class="detail-count">{{ selectedProductDetail.chiTietList.length }} mục</span>
                </div>

                <div v-if="selectedProductDetail.chiTietList.length" class="database-detail-list">
                  <article
                      v-for="(chiTiet, index) in selectedProductDetail.chiTietList"
                      :key="chiTiet.maChiTiet || index"
                      class="database-detail-item"
                  >
                    <div class="detail-order">{{ chiTiet.thuTu ?? index + 1 }}</div>
                    <div>
                      <h4>{{ chiTiet.loaiKhoi || `Chi tiết ${index + 1}` }}</h4>
                      <p>{{ chiTiet.noiDung || 'Chưa có nội dung.' }}</p>

                      <div v-if="selectedProductDetail.hinhAnhList.some(img => img.maChiTiet === chiTiet.maChiTiet)" class="detail-image-gallery inline-gallery">
                        <img
                            v-for="image in selectedProductDetail.hinhAnhList.filter(img => img.maChiTiet === chiTiet.maChiTiet)"
                            :key="image.maHinhAnh"
                            :src="image.urlHinhAnh"
                            :alt="image.loaiHinhAnh || chiTiet.loaiKhoi"
                        />
                      </div>
                    </div>
                  </article>
                </div>

                <div v-else class="empty-database-detail">
                  Sản phẩm này chưa có dữ liệu trong bảng sanphamchitiet.
                </div>
              </section>

              <section v-if="selectedProductDetail.hinhAnhList.some(img => !img.maChiTiet)" class="database-detail-section image-section">
                <div class="detail-section-heading">
                  <div>
                    <span class="detail-section-kicker">SANPHAMHINHANH</span>
                    <h3>Hình ảnh bổ sung</h3>
                  </div>
                </div>
                <div class="detail-image-gallery">
                  <figure
                      v-for="image in selectedProductDetail.hinhAnhList.filter(img => !img.maChiTiet)"
                      :key="image.maHinhAnh"
                  >
                    <img :src="image.urlHinhAnh" :alt="image.loaiHinhAnh || selectedProductDetail.name" />
                    <figcaption>{{ image.loaiHinhAnh || 'Hình ảnh sản phẩm' }}</figcaption>
                  </figure>
                </div>
              </section>

              <div class="detail-description">
                <h4>Ghi chú</h4>
                <p>{{ selectedProductDetail.ghiChu || 'Chưa có ghi chú cho sản phẩm.' }}</p>
              </div>
            </template>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped src="../../assets/styles/doitac/QLSanPham/TrangQLSanPham.css"></style>

<style scoped>
.category-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 36px 0 18px;
}

.category-header h4 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #2d3748;
}

.add-category-btn,
.category-header button {
  border: none;
  border-radius: 8px;
  background: #b91c1c;
  color: #fff;
  padding: 12px 22px;
  font-weight: 600;
  cursor: pointer;
}

.category-table {
  width: 100%;
  background: #fff;
}

.table-head,
.table-row {
  display: grid;
  grid-template-columns: 2.1fr 1.1fr 1.1fr 1fr;
  align-items: center;
  column-gap: 24px;
}

.table-head {
  padding: 26px 8px 18px;
  color: #1f2937;
  font-weight: 700;
  border-bottom: 1px solid #edf2f7;
}

.table-row {
  min-height: 72px;
  padding: 0 8px;
  color: #4b5563;
  border-bottom: 1px solid #edf2f7;
}

.category-status {
  width: 220px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  padding-left: 14px;
  border-radius: 3px;
  font-size: 13px;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.table-actions button {
  border: none;
  background: transparent;
  color: #263238;
  font-size: 16px;
  cursor: pointer;
  padding: 4px;
}

.table-actions button:hover {
  color: #b91c1c;
}


.product-card {
  position: relative;
  overflow: hidden;
}

.approval-bar {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.product-card > .approval-bar {
  position: absolute;
  z-index: 2;
  top: 12px;
  left: 12px;
  box-shadow: 0 5px 15px rgba(15, 23, 42, .14);
}

.approval-bar.approved { background: #dcfce7; color: #166534; }
.approval-bar.pending { background: #fef3c7; color: #92400e; }
.approval-bar.hidden { background: #fee2e2; color: #991b1b; }

.product-detail-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(15, 23, 42, .72);
  backdrop-filter: blur(8px);
}

.product-detail-modal {
  position: relative;
  width: min(1120px, 96vw);
  height: min(760px, 92vh);
  max-height: 92vh;
  min-height: 0;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(330px, 38%) minmax(0, 1fr);
  border: 1px solid rgba(255,255,255,.18);
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 38px 100px rgba(15, 23, 42, .44);
}

.detail-close {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 4;
  width: 40px;
  height: 40px;
  border: 1px solid rgba(15, 23, 42, .08);
  border-radius: 50%;
  background: rgba(255,255,255,.94);
  color: #334155;
  cursor: pointer;
  box-shadow: 0 7px 18px rgba(15, 23, 42, .13);
}

.detail-close:hover { color: #9f1239; transform: rotate(5deg); }
.detail-image-wrap {
  position: relative;
  min-height: 0;
  height: 100%;
  overflow: hidden;
  background: radial-gradient(circle at 35% 20%, #fff7f8, #e9eef5 65%);
}
.detail-image-wrap::after {
  content: "";
  position: absolute;
  inset: auto 0 0;
  height: 40%;
  background: linear-gradient(to top, rgba(15,23,42,.42), transparent);
  pointer-events: none;
}
.detail-image-wrap img { width: 100%; height: 100%; object-fit: cover; transition: transform .35s ease; }
.detail-image-wrap:hover img { transform: scale(1.025); }
.detail-status { position: absolute; z-index: 2; left: 22px; bottom: 22px; }
.detail-content {
  min-width: 0;
  min-height: 0;
  height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
  padding: 42px 40px 36px;
  background: linear-gradient(180deg, #fff 0%, #fbfcfe 100%);
}
.detail-content::-webkit-scrollbar { width: 9px; }
.detail-content::-webkit-scrollbar-track { background: #f1f5f9; }
.detail-content::-webkit-scrollbar-thumb { border: 2px solid #f1f5f9; border-radius: 999px; background: #94a3b8; }
.detail-content::-webkit-scrollbar-thumb:hover { background: #64748b; }
.detail-heading-row { display: flex; align-items: flex-start; justify-content: space-between; gap: 18px; padding-right: 26px; }
.detail-eyebrow { margin: 0 0 7px; color: #9f1239; font-size: 11px; font-weight: 850; letter-spacing: .12em; }
.detail-content h2 { margin: 0; color: #172033; font-size: clamp(25px, 3vw, 34px); line-height: 1.18; letter-spacing: -.025em; }
.detail-stock-pill { flex: 0 0 auto; display: inline-flex; align-items: center; gap: 7px; padding: 8px 11px; border-radius: 999px; font-size: 11px; font-weight: 800; }
.detail-stock-pill.in-stock { background: #dcfce7; color: #166534; }
.detail-stock-pill.out-stock { background: #fee2e2; color: #991b1b; }
.detail-price-row { display: flex; align-items: center; flex-wrap: wrap; gap: 12px; margin: 16px 0 23px; padding-bottom: 20px; border-bottom: 1px solid #e7ebf0; }
.detail-price { margin: 0; color: #9f1239; font-size: 27px; font-weight: 850; }
.detail-promotion { padding: 6px 9px; border-radius: 7px; background: #fff7ed; color: #c2410c; font-size: 11px; font-weight: 750; }
.detail-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
.detail-grid div { min-height: 67px; padding: 12px 14px; border: 1px solid #e5eaf0; border-radius: 12px; background: #fff; box-shadow: 0 5px 14px rgba(15,23,42,.035); }
.detail-grid span { display: block; margin-bottom: 5px; color: #7a8698; font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: .05em; }
.detail-grid strong { display: block; overflow: hidden; color: #263244; font-size: 13px; line-height: 1.4; text-overflow: ellipsis; }
.detail-description { margin-top: 22px; padding: 18px; border: 1px solid #e5eaf0; border-radius: 13px; background: #fff; }
.detail-description h4 { margin: 0 0 8px; color: #263244; }
.detail-description p { margin: 0; color: #596579; line-height: 1.7; }
.detail-loading { min-height: 420px; display: grid; place-items: center; color: #64748b; font-weight: 700; }
.database-detail-section { margin-top: 24px; padding-top: 22px; border-top: 1px solid #e5e7eb; }
.detail-section-heading { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 14px; }
.detail-section-heading h3 { margin: 3px 0 0; color: #1f2937; font-size: 18px; }
.detail-section-kicker { color: #991b1b; font-size: 11px; font-weight: 800; letter-spacing: .08em; }
.detail-count { flex: 0 0 auto; padding: 5px 9px; border-radius: 999px; background: #fef2f2; color: #991b1b; font-size: 12px; font-weight: 700; }
.database-detail-list { display: grid; gap: 12px; }
.database-detail-item { display: grid; grid-template-columns: 36px 1fr; gap: 13px; padding: 16px; border: 1px solid #e5eaf0; border-radius: 13px; background: #fff; box-shadow: 0 5px 14px rgba(15,23,42,.03); }
.detail-order { width: 32px; height: 32px; display: grid; place-items: center; border-radius: 10px; background: #9f1239; color: #fff; font-weight: 800; font-size: 12px; }
.database-detail-item h4 { margin: 2px 0 7px; color: #1f2937; font-size: 15px; }
.database-detail-item p { margin: 0; color: #4b5563; line-height: 1.65; white-space: pre-line; }
.empty-database-detail { padding: 20px; border: 1px dashed #cbd5e1; border-radius: 12px; color: #64748b; text-align: center; background: #f8fafc; }
.detail-image-gallery { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px; }
.detail-image-gallery figure { margin: 0; }
.detail-image-gallery img { width: 100%; height: 124px; object-fit: cover; border-radius: 10px; border: 1px solid #e5e7eb; background: #f3f4f6; }
.detail-image-gallery figcaption { margin-top: 5px; color: #64748b; font-size: 11px; }
.inline-gallery { margin-top: 12px; grid-template-columns: repeat(2, minmax(0, 1fr)); }
.inline-gallery img { height: 102px; }

@media (max-width: 900px) {
  .product-detail-modal {
    height: auto;
    max-height: 92vh;
    grid-template-columns: 1fr;
    overflow-x: hidden;
    overflow-y: auto;
    overscroll-behavior: contain;
  }
  .detail-image-wrap { min-height: 300px; height: 320px; max-height: 360px; }
  .detail-content { height: auto; min-height: auto; overflow: visible; padding: 32px 24px 28px; }
}

@media (max-width: 620px) {
  .product-detail-overlay { padding: 10px; }
  .product-detail-modal { width: 100%; height: auto; max-height: 96vh; border-radius: 17px; }
  .detail-heading-row { display: grid; padding-right: 22px; }
  .detail-grid { grid-template-columns: 1fr; }
  .detail-image-gallery { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .detail-content { padding: 26px 17px 22px; }
}
</style>

