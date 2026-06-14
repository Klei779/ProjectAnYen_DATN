<script setup>
import { ref, computed, watch, onMounted } from "vue";
import api from "../../api/api.js";

const API_URL = "/api/doi-tac/san-pham";

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
const total = ref(0);

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
  const trangThai = sp.trangThai || sp.status || "";

  if (trangThai === "Ẩn" || trangThai === "Đã ẩn") {
    return "Ẩn";
  }

  if (trangThai === "Hết hàng") {
    return "Hết hàng";
  }

  const stock = Number(sp.stock ?? sp.soLuong ?? 0);
  return stock > 0 ? "Còn hàng" : "Hết hàng";
};

const normalizeProduct = (sp) => {
  const id = getProductId(sp);
  const stock = Number(sp.stock ?? sp.soLuong ?? 0);
  const status = getProductStatus(sp);

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
    trangThai: sp.trangThai || (status === "Ẩn" ? "Ẩn" : stock > 0 ? "Đang bán" : "Hết hàng"),
    kichThuoc: sp.kichThuoc || "",
    trongLuong: sp.trongLuong || "",
    cnsx: sp.cnsx || sp.CNSX || "",
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

const isHiddenProduct = (product) => {
  return product.status === "Ẩn" || product.trangThai === "Ẩn" || product.trangThai === "Đã ẩn";
};

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
    status: cate.total > 0 && cate.hidden === cate.total ? "Đang ẩn" : "Đang hiển thị",
  }));
});

const availableProducts = computed(() =>
    products.value.filter((p) => p.status === "Còn hàng" || p.trangThai === "Đang bán")
);

const outOfStockProducts = computed(() =>
    products.value.filter((p) => p.status === "Hết hàng")
);

const hiddenProducts = computed(() =>
    products.value.filter((p) => p.status === "Ẩn" || p.trangThai === "Ẩn")
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

    if (selectedStatus.value === "an") {
      matchStatus = p.status === "Ẩn" || p.trangThai === "Ẩn";
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

const addProduct = async () => {
  if (!validateProduct()) return;

  try {
    await api.post(API_URL, buildPayload());

    alert("Đã tạo sản phẩm mới!");
    resetForm();
    activeTab.value = "list";
    await loadProducts();
    currentPage.value = totalPages.value;
  } catch (error) {
    console.error("Lỗi tạo sản phẩm:", error);
    alert("Không thể tạo sản phẩm. Kiểm tra backend SanPhamDoiTac.");
  }
};

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

const increaseStock = (product) => {
  product.stock += 1;
  product.soLuong = product.stock;

  if (product.status !== "Ẩn") {
    product.status = product.stock > 0 ? "Còn hàng" : "Hết hàng";
    product.trangThai = product.stock > 0 ? "Đang bán" : "Hết hàng";
  }

  alert("Thêm tồn kho thành công!");
};

const decreaseStock = (product) => {
  if (product.stock <= 0) return;

  product.stock -= 1;
  product.soLuong = product.stock;

  if (product.status !== "Ẩn") {
    product.status = product.stock > 0 ? "Còn hàng" : "Hết hàng";
    product.trangThai = product.stock > 0 ? "Đang bán" : "Hết hàng";
  }

  alert("Trừ tồn kho thành công!");
};

const saveStock = async (product) => {
  try {
    // Backend SanPhamDoiTacController nhận @RequestBody { soLuong }
    await api.patch(`${API_URL}/${product.id}/ton-kho`, {
      soLuong: product.stock,
    });

    alert("Lưu tồn kho thành công!");
    await loadProducts();
  } catch (error) {
    console.error("Lỗi lưu tồn kho:", error);
    alert("Không thể lưu tồn kho.");
  }
};

const changeProductStatus = async (product, trangThai) => {
  try {
    // Backend SanPhamDoiTacController đang dùng /an và /hien
    const action = trangThai === "Ẩn" ? "an" : "hien";
    await api.patch(`${API_URL}/${product.id}/${action}`);

    alert(trangThai === "Ẩn" ? "Đã ẩn sản phẩm!" : "Đã hiện lại sản phẩm!");
    await loadProducts();
  } catch (error) {
    console.error("Lỗi cập nhật trạng thái sản phẩm:", error);
    alert("Không thể cập nhật trạng thái sản phẩm.");
  }
};

const hideProduct = (product) => {
  if (confirm("Bạn có chắc muốn ẩn sản phẩm này không?")) {
    changeProductStatus(product, "Ẩn");
  }
};

const showProduct = (product) => {
  changeProductStatus(product, Number(product.stock || 0) > 0 ? "Đang bán" : "Hết hàng");
};

const showDetail = (product) => {
  alert(
      `Tên sản phẩm: ${product.name}\n` +
      `Loại: ${product.category}\n` +
      `Giá: ${formatPrice(product.price)}\n` +
      `Tồn kho: ${product.stock}\n` +
      `Trạng thái: ${product.status}`
  );
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
  trangThai: overrides.trangThai ?? product.trangThai ?? "Đang bán",
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
    await Promise.all(list.map((product) => api.patch(`${API_URL}/${product.id}/an`)));
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
    await Promise.all(list.map((product) => api.patch(`${API_URL}/${product.id}/hien`)));
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
        <button :class="{ active: activeTab === 'create' }" @click="changeTab('create')">
          Tạo sản phẩm mới
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
            <option value="an">Đã ẩn</option>
          </select>

          <select v-model="selectedCategory">
            <option value="">Tất cả danh mục</option>
            <option v-for="cate in categories" :key="cate.id" :value="cate.name">
              {{ cate.name }}
            </option>
          </select>

          <div class="search-input">
            <input v-model="keyword" placeholder="Tìm kiếm sản phẩm..." />
            <i class="fa-solid fa-magnifying-glass"></i>
          </div>

          <button class="export-btn">
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
              <p>Sản phẩm đã ẩn</p>
              <h3>{{ hiddenProducts.length }}</h3>
            </div>
          </div>
        </div>

        <p v-if="loading">Đang tải sản phẩm...</p>

        <div class="product-grid">
          <div class="product-card" v-for="product in paginatedProducts" :key="product.id">
            <img :src="product.image" :alt="product.name" />

            <div class="product-info">
              <h3>{{ product.name }}</h3>

              <p class="product-sku">SKU: {{ product.sku }}</p>

              <p class="product-price">
                {{ formatPrice(product.price) }}
              </p>

              <div class="stock-row">
                <div>
                  <p>Tồn kho</p>

                  <span
                      class="status"
                      :class="product.status === 'Còn hàng' || product.status === 'Đang bán' ? 'available' : 'empty'"
                  >
                    {{ product.status }}
                  </span>
                </div>

                <div class="stock-control">
                  <button @click="decreaseStock(product)">−</button>

                  <span>{{ product.stock }}</span>

                  <button @click="increaseStock(product)">+</button>
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
                  v-if="product.status !== 'Ẩn'"
                  class="hide-btn"
                  @click="hideProduct(product)"
              >
                <i class="fa-regular fa-eye-slash"></i>
                Ẩn
              </button>

              <button
                  v-else
                  class="hide-btn"
                  @click="showProduct(product)"
              >
                <i class="fa-regular fa-eye"></i>
                Hiện
              </button>

              <button class="save-btn" @click="saveStock(product)">
                <i class="fa-regular fa-floppy-disk"></i>
                Lưu
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
                :class="cate.status === 'Đang hiển thị' ? 'available' : 'empty'"
            >
              {{ cate.status }}
            </span>

            <span class="table-actions">
              <button title="Sửa danh mục" @click="editCategory(cate)">
                <i class="fa-solid fa-pen"></i>
              </button>

              <button
                  v-if="cate.status === 'Đang hiển thị'"
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

      <template v-if="activeTab === 'create'">
        <div class="create-form">
          <h4>
            {{ editingProduct ? "Cập nhật thông tin sản phẩm" : "Thông tin sản phẩm mới" }}
          </h4>

          <div class="form-grid">
            <div class="form-group">
              <label>Tên sản phẩm</label>
              <input v-model="newProduct.tenSanPham" placeholder="Nhập tên sản phẩm" />
            </div>

            <div class="form-group">
              <label>Loại</label>
              <input v-model="newProduct.loai" placeholder="VD: Quan tài, bình tro cốt..." />
            </div>

            <div class="form-group">
              <label>Nội thất</label>
              <input v-model="newProduct.noiThat" placeholder="Nhập nội thất" />
            </div>

            <div class="form-group">
              <label>Quy cách</label>
              <input v-model="newProduct.quyCach" placeholder="Nhập quy cách" />
            </div>

            <div class="form-group">
              <label>Tôn giáo</label>
              <select v-model="newProduct.tonGiao">
                <option value="">Chọn tôn giáo</option>
                <option>Phật giáo</option>
                <option>Công giáo</option>
                <option>Tin lành</option>
                <option>Cao Đài</option>
                <option>Không yêu cầu</option>
              </select>
            </div>

            <div class="form-group">
              <label>Giá tiền</label>
              <input v-model="newProduct.giaTien" type="number" placeholder="Nhập giá tiền" />
            </div>

            <div class="form-group">
              <label>Mã đối tác</label>
              <input v-model="newProduct.maDoiTac" type="number" placeholder="Backend có thể tự lấy từ token" />
            </div>

            <div class="form-group">
              <label>Số lượng</label>
              <input v-model="newProduct.soLuong" type="number" placeholder="Nhập số lượng" />
            </div>

            <div class="form-group">
              <label>Thiết kế</label>
              <input v-model="newProduct.thietKe" placeholder="Nhập thiết kế" />
            </div>

            <div class="form-group">
              <label>Xuất xứ</label>
              <input v-model="newProduct.xuatXu" placeholder="Nhập xuất xứ" />
            </div>

            <div class="form-group">
              <label>Khuyến mãi</label>
              <input v-model="newProduct.khuyenMai" type="number" placeholder="Nhập khuyến mãi" />
            </div>

            <div class="form-group">
              <label>Màu sắc</label>
              <input v-model="newProduct.mauSac" placeholder="Nhập màu sắc" />
            </div>

            <div class="form-group">
              <label>Hình ảnh</label>

              <input type="file" accept="image/*" @change="handleImageUpload" />

              <div v-if="imagePreview" class="image-preview">
                <img :src="imagePreview" alt="Ảnh sản phẩm" />
              </div>
            </div>

            <div class="form-group">
              <label>Vật liệu</label>
              <input v-model="newProduct.vatLieu" placeholder="Nhập vật liệu" />
            </div>

            <div class="form-group">
              <label>Trạng thái</label>
              <select v-model="newProduct.trangThai">
                <option>Đang bán</option>
                <option>Ẩn</option>
                <option>Hết hàng</option>
              </select>
            </div>

            <div class="form-group">
              <label>Kích thước</label>
              <input v-model="newProduct.kichThuoc" placeholder="VD: 120x60x80cm" />
            </div>

            <div class="form-group">
              <label>Trọng lượng</label>
              <input v-model="newProduct.trongLuong" placeholder="VD: 25kg" />
            </div>

            <div class="form-group">
              <label>CNSX</label>
              <input v-model="newProduct.cnsx" placeholder="Nhập CNSX" />
            </div>

            <div class="form-group full">
              <label>Ghi chú</label>
              <textarea v-model="newProduct.ghiChu" placeholder="Nhập ghi chú"></textarea>
            </div>
          </div>

          <div class="form-actions">
            <button class="cancel-btn" @click="resetForm(); activeTab = 'list'">Hủy</button>
            <button class="submit-btn" @click="editingProduct ? updateProduct() : addProduct()">
              {{ editingProduct ? "Cập nhật sản phẩm" : "Tạo sản phẩm" }}
            </button>
          </div>
        </div>
      </template>
    </section>
  </div>
</template>

<style scoped src="../../assets/styles/TrangQLSanPham.css"></style>

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
</style>

