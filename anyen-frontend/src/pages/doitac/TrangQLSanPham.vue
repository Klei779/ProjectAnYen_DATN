<script setup>
import { ref, computed, watch } from "vue";
import axios from "axios";

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

const products = ref([
  {
    id: 1,
    name: "Quan tài gỗ Hương cao cấp",
    sku: "QT-001",
    category: "Quan tài",
    price: 25000000,
    stock: 8,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Quan+Tai+Go+Huong",
  },
  {
    id: 2,
    name: "Quan tài gỗ Căm Xe",
    sku: "QT-002",
    category: "Quan tài",
    price: 18000000,
    stock: 15,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Quan+Tai+Cam+Xe",
  },
  {
    id: 3,
    name: "Bình tro cốt gốm sứ Bát Tràng",
    sku: "BTC-001",
    category: "Bình tro cốt",
    price: 3500000,
    stock: 50,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Binh+Tro+Cot",
  },
  {
    id: 4,
    name: "Bình tro cốt men rạn",
    sku: "BTC-002",
    category: "Bình tro cốt",
    price: 4200000,
    stock: 25,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Binh+Tro+Cot+Men+Ran",
  },
  {
    id: 5,
    name: "Bàn thờ tang lễ tiêu chuẩn",
    sku: "BT-001",
    category: "Bàn thờ tang lễ",
    price: 6500000,
    stock: 10,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Ban+Tho",
  },
  {
    id: 6,
    name: "Bàn thờ tang lễ cao cấp",
    sku: "BT-002",
    category: "Bàn thờ tang lễ",
    price: 12000000,
    stock: 6,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Ban+Tho+Cao+Cap",
  },
  {
    id: 7,
    name: "Xe tang lễ Mercedes",
    sku: "XT-001",
    category: "Xe tang lễ",
    price: 5500000,
    stock: 4,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Xe+Tang+Le",
  },
  {
    id: 8,
    name: "Xe tang lễ Limousine",
    sku: "XT-002",
    category: "Xe tang lễ",
    price: 7500000,
    stock: 2,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Xe+Tang+Le+VIP",
  },
  {
    id: 9,
    name: "Vòng hoa tang lễ loại A",
    sku: "VH-001",
    category: "Hoa tang lễ",
    price: 850000,
    stock: 40,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Vong+Hoa",
  },
  {
    id: 10,
    name: "Vòng hoa tang lễ cao cấp",
    sku: "VH-002",
    category: "Hoa tang lễ",
    price: 1500000,
    stock: 22,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Vong+Hoa+Cao+Cap",
  },
  {
    id: 11,
    name: "Bộ áo tang truyền thống",
    sku: "AT-001",
    category: "Trang phục tang lễ",
    price: 450000,
    stock: 120,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Ao+Tang",
  },
  {
    id: 12,
    name: "Khăn tang trắng",
    sku: "AT-002",
    category: "Trang phục tang lễ",
    price: 50000,
    stock: 300,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Khan+Tang",
  },
  {
    id: 13,
    name: "Lư hương đồng",
    sku: "LT-001",
    category: "Đồ thờ cúng",
    price: 2200000,
    stock: 18,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Lu+Huong",
  },
  {
    id: 14,
    name: "Đèn thờ đồng",
    sku: "LT-002",
    category: "Đồ thờ cúng",
    price: 1800000,
    stock: 12,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Den+Tho",
  },
  {
    id: 15,
    name: "Bộ chén cúng sứ",
    sku: "LT-003",
    category: "Đồ thờ cúng",
    price: 550000,
    stock: 80,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Chen+Cung",
  },
  {
    id: 16,
    name: "Quan tài gỗ Lim",
    sku: "QT-003",
    category: "Quan tài",
    price: 32000000,
    stock: 3,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Quan+Tai+Go+Lim",
  },
  {
    id: 17,
    name: "Bình tro cốt đá cẩm thạch",
    sku: "BTC-003",
    category: "Bình tro cốt",
    price: 6500000,
    stock: 10,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Binh+Da+Cam+Thach",
  },
  {
    id: 18,
    name: "Nhà quàn di động",
    sku: "DV-001",
    category: "Dịch vụ tang lễ",
    price: 12000000,
    stock: 5,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Nha+Quan",
  },
  {
    id: 19,
    name: "Dịch vụ mai táng trọn gói",
    sku: "DV-002",
    category: "Dịch vụ tang lễ",
    price: 55000000,
    stock: 999,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Mai+Tang+Tron+Goi",
  },
  {
    id: 20,
    name: "Dịch vụ hỏa táng",
    sku: "DV-003",
    category: "Dịch vụ tang lễ",
    price: 18000000,
    stock: 999,
    status: "Còn hàng",
    image: "https://via.placeholder.com/350x180?text=Hoa+Tang",
  },
]);

const filteredProducts = computed(() =>
    products.value.filter((p) =>
        p.name.toLowerCase().includes(keyword.value.toLowerCase())
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
  const imagePreview = ref("");

  const handleImageUpload = (event) => {
    const file = event.target.files[0];

    if (!file) return;

    newProduct.value.hinhAnh = file;
    imagePreview.value = URL.createObjectURL(file);
  };
};

const hideProduct = async (product) => {
  if (!confirm("Bạn có chắc muốn ẩn sản phẩm này?")) return;

  await axios.patch(`http://localhost:8080/api/san-pham/${product.id}/an`);

  product.status = "Ẩn";
  product.trangThai = "Ẩn";

  alert("Ẩn sản phẩm thành công!");
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

              <button class="edit-btn">
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
          <h4>Thông tin sản phẩm mới</h4>

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
            <button class="submit-btn" @click="addProduct">Tạo sản phẩm</button>
          </div>
        </div>
      </template>
    </section>
  </div>
</template>

<style scoped src="../../assets/styles/TrangQLSanPham.css"></style>