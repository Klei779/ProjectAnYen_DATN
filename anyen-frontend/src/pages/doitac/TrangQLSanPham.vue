<script setup>
import { ref, computed, watch, onMounted } from "vue";
import api from "../../api/api.js";

const editingProduct = ref(null);

const activeTab = ref("list");
const keyword = ref("");

const currentPage = ref(1);
const pageSize = 16;

const newProduct = ref({
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

const categories = ref([
  { id: 1, name: "Quan tài", total: 25, status: "Đang hiển thị" },
  { id: 2, name: "Bàn thờ tang lễ", total: 18, status: "Đang hiển thị" },
  { id: 3, name: "Bình tro cốt", total: 32, status: "Đang hiển thị" },
  { id: 4, name: "Hoa tang lễ", total: 40, status: "Đang hiển thị" },
  { id: 5, name: "Xe tang lễ", total: 12, status: "Đang hiển thị" },
  { id: 6, name: "Đồ thờ cúng", total: 20, status: "Đang ẩn" },
  { id: 7, name: "Trang phục tang lễ", total: 14, status: "Đang hiển thị" },
  { id: 8, name: "Dịch vụ tang lễ", total: 9, status: "Đang hiển thị" },
]);

const products = ref([]);
const total = ref(0);
const loading = ref(false);
const imagePreview = ref("");

const loadProducts = async () => {
  try {
    loading.value = true;

    const response = await api.get("/api/doi-tac/san-pham", {
      params: {
        page: 1,
        pageSize: 9999,
        sortBy: "newest"
      }
    });

    const items = response.data.items || [];

    products.value = items.map((sp) => ({
      id: sp.id || sp.maSanPham || sp.maSP,
      name: sp.name || sp.tenSanPham,
      sku: sp.sku || `SP-${sp.maSanPham || sp.id}`,
      category: sp.category || sp.loai,
      price: sp.price || sp.giaTien || 0,
      stock: sp.stock ?? sp.soLuong ?? 0,
      status:
          sp.status ||
          sp.trangThai ||
          ((sp.soLuong || 0) > 0 ? "Còn hàng" : "Hết hàng"),
      image:
          sp.image ||
          sp.hinhAnh ||
          "https://via.placeholder.com/350x180?text=San+Pham",
      loai: sp.loai,
      noiThat: sp.noiThat,
      quyCach: sp.quyCach,
      tonGiao: sp.tonGiao,
      giaTien: sp.giaTien,
      maDoiTac: sp.maDoiTac,
      soLuong: sp.soLuong,
      thietKe: sp.thietKe,
      xuatXu: sp.xuatXu,
      ghiChu: sp.ghiChu,
      khuyenMai: sp.khuyenMai,
      mauSac: sp.mauSac,
      hinhAnh: sp.hinhAnh,
      vatLieu: sp.vatLieu,
      trangThai: sp.trangThai,
      kichThuoc: sp.kichThuoc,
      trongLuong: sp.trongLuong,
      cnsx: sp.cnsx
    }));

    total.value = response.data.total || products.value.length;

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

const filteredProducts = computed(() =>
    products.value.filter((p) =>
        (p.name || p.tenSanPham || "")
            .toLowerCase()
            .includes(keyword.value.toLowerCase())
    )
);

const totalPages = computed(() =>
    Math.ceil(filteredProducts.value.length / pageSize)
);

const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return filteredProducts.value.slice(start, start + pageSize);
});

const startItem = computed(() =>
    filteredProducts.value.length === 0
        ? 0
        : (currentPage.value - 1) * pageSize + 1
);

const endItem = computed(() =>
    Math.min(currentPage.value * pageSize, filteredProducts.value.length)
);

watch(keyword, () => {
  currentPage.value = 1;
});

const formatPrice = (price) => {
  return new Intl.NumberFormat("vi-VN").format(price) + " đ";
};

const changeTab = (tab) => {
  activeTab.value = tab;
};

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

const addProduct = () => {
  if (
      !newProduct.value.tenSanPham ||
      !newProduct.value.loai ||
      !newProduct.value.giaTien ||
      !newProduct.value.soLuong
  ) {
    alert("Vui lòng nhập tên sản phẩm, loại, giá tiền và số lượng!");
    return;
  }

  products.value.push({
    id: Date.now(),
    name: newProduct.value.tenSanPham,
    sku: "SP-" + Date.now(),
    category: newProduct.value.loai,
    price: Number(newProduct.value.giaTien),
    stock: Number(newProduct.value.soLuong),
    status: Number(newProduct.value.soLuong) > 0 ? "Còn hàng" : "Hết hàng",
    image: imagePreview.value || "https://via.placeholder.com/350x180?text=San+Pham+Moi",
  });

  newProduct.value = {
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
  };

  activeTab.value = "list";
  currentPage.value = totalPages.value;
  alert("Đã tạo sản phẩm mới!");
};
const increaseStock = (product) => {
  product.stock++;

  if (product.stock > 0) {
    product.status = "Còn hàng";
  }

  alert("Thêm tồn kho thành công!");
};

const decreaseStock = (product) => {
  if (product.stock > 0) {
    product.stock--;

    if (product.stock === 0) {
      product.status = "Hết hàng";
    }

    alert("Trừ tồn kho thành công!");
  }
};

const saveStock = () => {
  alert("Lưu tồn kho thành công!");
};
const deleteProduct = (id) => {
  if (confirm("Bạn có chắc muốn xóa sản phẩm này?")) {
    products.value = products.value.filter((p) => p.id !== id);

    if (currentPage.value > totalPages.value) {
      currentPage.value = totalPages.value || 1;
    }
  }
};

const handleImageUpload = (event) => {
  const file = event.target.files[0];

  if (!file) return;

  newProduct.value.hinhAnh = file;
  imagePreview.value = URL.createObjectURL(file);
};

const editProduct = (product) => {
  editingProduct.value = product;

  newProduct.value = {
    tenSanPham: product.name,
    loai: product.loai,
    noiThat: product.noiThat || "",
    quyCach: product.quyCach || "",
    tonGiao: product.tonGiao || "",
    giaTien: product.price,
    maDoiTac: product.maDoiTac || "",
    soLuong: product.soLuong || product.stock || 0,
    thietKe: product.thietKe || "",
    xuatXu: product.xuatXu || "",
    ghiChu: product.ghiChu || "",
    khuyenMai: product.oldPrice || "",
    mauSac: product.mauSac || "",
    hinhAnh: product.image || "",
    vatLieu: product.vatLieu || "",
    trangThai: product.trangThai || "Đang bán",
    kichThuoc: product.kichThuoc || "",
    trongLuong: product.trongLuong || "",
    CNSX: product.CNSX || "",
  };

  activeTab.value = "create";
};

const updateProduct = async () => {
  if (!editingProduct.value) return;

  await api.put(
      `/api/san-pham/${editingProduct.value.id}`,
      newProduct.value
  );

  alert("Cập nhật sản phẩm thành công!");

  editingProduct.value = null;
  activeTab.value = "list";

  await loadProducts();
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
                    : "Thêm sản phẩm mới"
          }}
        </h2>
        <p>Quản lý sản phẩm trên website đối tác.</p>
      </div>

      <div class="tabs">
        <button :class="{ active: activeTab === 'list' }" @click="changeTab('list')">
          Danh sách sản phẩm
        </button>
        <button
            :class="{ active: activeTab === 'category' }"
            @click="changeTab('category')"
        >
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

          <select>
            <option>Tất cả trạng thái sản phẩm</option>
            <option>Còn hàng</option>
            <option>Hết hàng</option>
          </select>

          <select>
            <option>Tất cả danh mục</option>
            <option v-for="cate in categories" :key="cate.id">
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
              <h3>{{ products.filter((p) => p.stock > 0).length }}</h3>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon gray">
              <i class="fa-regular fa-clock"></i>
            </div>
            <div>
              <p>Sản phẩm hết hàng</p>
              <h3>{{ products.filter((p) => p.stock === 0).length }}</h3>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon blue">
              <i class="fa-solid fa-list"></i>
            </div>
            <div>
              <p>Tổng danh mục</p>
              <h3>{{ categories.length }}</h3>
            </div>
          </div>
        </div>

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
                      :class="product.status === 'Còn hàng' ? 'available' : 'empty'"
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
              <button class="detail-btn">
                <i class="fa-regular fa-eye"></i>
                chi tiết
              </button>

              <button class="edit-btn" @click="editProduct(product)">
                <i class="fa-solid fa-pen"></i>
                Sửa
              </button>

              <button class="hide-btn" @click="hideProduct(product)">
                <i class="fa-regular fa-eye-slash"></i>
                Ẩn
              </button>

              <button
                  class="save-btn"
                  @click="saveStock(product)"
              >
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

            <button
                @click="changePage(currentPage + 1)"
                :disabled="currentPage === totalPages"
            >
              <i class="fa-solid fa-chevron-right"></i>
            </button>
          </div>
        </div>
      </template>

      <template v-if="activeTab === 'category'">
        <div class="category-header">
          <h4>Danh mục sản phẩm</h4>
          <button>
            <i class="fa-solid fa-plus"></i>
            Thêm danh mục
          </button>
        </div>

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
                class="status"
                :class="cate.status === 'Đang hiển thị' ? 'available' : 'empty'"
            >
              {{ cate.status }}
            </span>
            <span class="table-actions">
              <button><i class="fa-solid fa-pen"></i></button>
              <button><i class="fa-regular fa-trash-can"></i></button>
              <button><i class="fa-regular fa-eye"></i></button>
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
              <input
                  v-model="newProduct.loai"
                  placeholder="VD: Quan tài, bình tro cốt..."
              />
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
              <input
                  v-model="newProduct.maDoiTac"
                  type="number"
                  placeholder="Nhập mã đối tác"
              />
            </div>

            <div class="form-group">
              <label>Số lượng</label>
              <input
                  v-model="newProduct.soLuong"
                  type="number"
                  placeholder="Nhập số lượng"
              />
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
              <input
                  v-model="newProduct.khuyenMai"
                  type="number"
                  placeholder="Nhập khuyến mãi"
              />
            </div>

            <div class="form-group">
              <label>Màu sắc</label>
              <input v-model="newProduct.mauSac" placeholder="Nhập màu sắc" />
            </div>

            <div class="form-group">
              <label>Hình ảnh</label>

              <input
                  type="file"
                  accept="image/*"
                  @change="handleImageUpload"
              />

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
            <button class="cancel-btn" @click="activeTab = 'list'">Hủy</button>
            <button
                class="submit-btn"
                @click="editingProduct ? updateProduct() : addProduct()"
            >
              {{ editingProduct ? "Cập nhật sản phẩm" : "Tạo sản phẩm" }}
            </button>
          </div>
        </div>
      </template>
    </section>
  </div>
</template>

<style scoped src="../../assets/styles/TrangQLSanPham.css"></style>