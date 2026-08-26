<script setup>
import {
  computed,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
} from "vue";
import { ElMessage } from "element-plus";

import api from "../../api/api.js";
import TrangTaoSanPham from "./TrangTaoSanPham.vue";

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
const products = ref([]);

const stockDrafts = ref({});
const savingStockId = ref(null);

const selectedProductDetail = ref(null);
const loadingProductDetail = ref(false);

let previousBodyOverflow = "";
let previousHtmlOverflow = "";

/**
 * Lấy ID sản phẩm, hỗ trợ nhiều định dạng response.
 */
const getProductId = (product) => {
  return product.id ?? product.maSanPham ?? product.maSP;
};

/**
 * Lấy trạng thái bán dựa theo trạng thái duyệt và tồn kho.
 */
const getProductStatus = (product) => {
  const approvalCode = Number(
      product.trangThai ?? product.status ?? 2
  );

  if (approvalCode === 0) {
    return "Ngưng bán";
  }

  if (approvalCode === 2) {
    return "Chờ duyệt";
  }

  const stock = Number(
      product.stock ?? product.soLuong ?? 0
  );

  return stock > 0 ? "Còn hàng" : "Hết hàng";
};

/**
 * Nhãn trạng thái duyệt.
 */
const getApprovalLabel = (code) => {
  const statusCode = Number(code);

  if (statusCode === 1) {
    return "Đã duyệt";
  }

  if (statusCode === 2) {
    return "Chờ duyệt";
  }

  return "Đang ẩn / Đã từ chối";
};

/**
 * Class CSS trạng thái duyệt.
 */
const getApprovalClass = (code) => {
  const statusCode = Number(code);

  if (statusCode === 1) {
    return "approved";
  }

  if (statusCode === 2) {
    return "pending";
  }

  return "hidden";
};

/**
 * Kiểm tra sản phẩm đang ngưng bán.
 */
const isHiddenProduct = (product) => {
  return product.trangThaiCode === 0;
};

/**
 * Chuẩn hóa dữ liệu sản phẩm từ backend.
 */
const normalizeProduct = (product) => {
  const id = getProductId(product);

  const stock = Number(
      product.stock ?? product.soLuong ?? 0
  );

  const trangThaiCode = Number(
      product.trangThai ?? product.status ?? 2
  );

  return {
    id,

    name:
        product.name ||
        product.tenSanPham ||
        "Chưa có tên",

    sku:
        product.sku ||
        `SP-${id}`,

    category:
        product.category ||
        product.loai ||
        "Chưa phân loại",

    price: Number(
        product.price ??
        product.giaTien ??
        0
    ),

    stock,
    status: getProductStatus(product),

    image:
        product.image ||
        product.hinhAnh ||
        "https://via.placeholder.com/350x180?text=San+Pham",

    maSanPham: id,

    tenSanPham:
        product.tenSanPham ||
        product.name ||
        "",

    loai:
        product.loai ||
        product.category ||
        "",

    noiThat: product.noiThat || "",
    quyCach: product.quyCach || "",
    tonGiao: product.tonGiao || "",

    giaTien: Number(
        product.giaTien ??
        product.price ??
        0
    ),

    maDoiTac: product.maDoiTac || "",
    soLuong: stock,
    thietKe: product.thietKe || "",
    xuatXu: product.xuatXu || "",
    ghiChu: product.ghiChu || "",
    khuyenMai: product.khuyenMai ?? "",
    mauSac: product.mauSac || "",

    hinhAnh:
        product.hinhAnh ||
        product.image ||
        "",

    vatLieu: product.vatLieu || "",
    trangThaiCode,

    trangThai:
        trangThaiCode === 1
            ? "Đang bán"
            : trangThaiCode === 2
                ? "Chờ duyệt"
                : "Ngưng bán",

    kichThuoc: product.kichThuoc || "",
    trongLuong: product.trongLuong || "",

    cnsx:
        product.cnsx ||
        product.CNSX ||
        "",

    chiTietList: Array.isArray(product.chiTietList)
        ? product.chiTietList
        : [],

    hinhAnhList: Array.isArray(product.hinhAnhList)
        ? product.hinhAnhList
        : [],
  };
};

/**
 * Tải toàn bộ sản phẩm của đối tác.
 */
const loadProducts = async () => {
  try {
    loading.value = true;

    const response = await api.get(API_URL, {
      params: {
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
        products.value.map((product) => [
          product.id,
          product.stock,
        ])
    );
  } catch (error) {
    console.error(
        "Lỗi load sản phẩm đối tác:",
        error
    );

    if (error.response?.status === 403) {
      ElMessage.error(
          "Bạn không có quyền xem sản phẩm đối tác. Hãy đăng nhập bằng tài khoản đối tác."
      );
      return;
    }

    if (error.response?.status === 401) {
      ElMessage.error(
          "Bạn chưa đăng nhập hoặc token đã hết hạn."
      );
      return;
    }

    ElMessage.error(
        error.response?.data?.message ||
        "Không thể tải danh sách sản phẩm."
    );
  } finally {
    loading.value = false;
  }
};

/**
 * Danh sách danh mục.
 */
const categories = computed(() => {
  const categoryMap = new Map();

  products.value.forEach((product) => {
    const categoryName =
        product.category ||
        product.loai ||
        "Chưa phân loại";

    if (!categoryMap.has(categoryName)) {
      categoryMap.set(categoryName, {
        name: categoryName,
        total: 0,
        hidden: 0,
      });
    }

    const category = categoryMap.get(categoryName);

    category.total += 1;

    if (isHiddenProduct(product)) {
      category.hidden += 1;
    }
  });

  return Array.from(categoryMap.values()).map(
      (category, index) => ({
        id: index + 1,
        name: category.name,
      })
  );
});

/**
 * Sản phẩm đã được duyệt.
 */
const availableProducts = computed(() => {
  return products.value.filter(
      (product) => product.trangThaiCode === 1
  );
});

/**
 * Sản phẩm đã duyệt nhưng hết hàng.
 */
const outOfStockProducts = computed(() => {
  return products.value.filter(
      (product) =>
          product.trangThaiCode === 1 &&
          product.status === "Hết hàng"
  );
});

/**
 * Sản phẩm ngưng bán.
 */
const stoppedProducts = computed(() => {
  return products.value.filter(
      (product) => product.trangThaiCode === 0
  );
});

/**
 * Lọc sản phẩm.
 */
const filteredProducts = computed(() => {
  const searchText = keyword.value
      .trim()
      .toLowerCase();

  return products.value.filter((product) => {
    const productName = (
        product.name || ""
    ).toLowerCase();

    const productSku = (
        product.sku || ""
    ).toLowerCase();

    const productCategory = (
        product.category || ""
    ).toLowerCase();

    const matchKeyword =
        !searchText ||
        productName.includes(searchText) ||
        productSku.includes(searchText) ||
        productCategory.includes(searchText);

    const matchCategory =
        !selectedCategory.value ||
        product.category === selectedCategory.value ||
        product.loai === selectedCategory.value;

    let matchStatus = true;

    if (selectedStatus.value === "con-hang") {
      matchStatus =
          product.trangThaiCode === 1 &&
          product.status === "Còn hàng";
    } else if (selectedStatus.value === "het-hang") {
      matchStatus =
          product.trangThaiCode === 1 &&
          product.status === "Hết hàng";
    } else if (selectedStatus.value === "ngung-ban") {
      matchStatus = product.trangThaiCode === 0;
    }

    return (
        matchKeyword &&
        matchCategory &&
        matchStatus
    );
  });
});

/**
 * Tổng số trang.
 */
const totalPages = computed(() => {
  return (
      Math.ceil(
          filteredProducts.value.length / pageSize
      ) || 1
  );
});

/**
 * Sản phẩm trên trang hiện tại.
 */
const paginatedProducts = computed(() => {
  const startIndex =
      (currentPage.value - 1) * pageSize;

  return filteredProducts.value.slice(
      startIndex,
      startIndex + pageSize
  );
});

const startItem = computed(() => {
  if (filteredProducts.value.length === 0) {
    return 0;
  }

  return (
      (currentPage.value - 1) * pageSize + 1
  );
});

const endItem = computed(() => {
  return Math.min(
      currentPage.value * pageSize,
      filteredProducts.value.length
  );
});

/**
 * Bộ lọc thay đổi thì quay về trang đầu.
 */
watch(
    [
      keyword,
      selectedStatus,
      selectedCategory,
      activeTab,
    ],
    () => {
      currentPage.value = 1;
    }
);

/**
 * Khóa cuộn trang khi popup đang mở.
 */
watch(
    () => Boolean(selectedProductDetail.value),
    (isOpen) => {
      if (isOpen) {
        previousBodyOverflow =
            document.body.style.overflow;

        previousHtmlOverflow =
            document.documentElement.style.overflow;

        document.body.style.overflow = "hidden";
        document.documentElement.style.overflow = "hidden";
        return;
      }

      document.body.style.overflow =
          previousBodyOverflow;

      document.documentElement.style.overflow =
          previousHtmlOverflow;
    }
);

const formatPrice = (price) => {
  return (
      new Intl.NumberFormat("vi-VN").format(
          Number(price || 0)
      ) + " đ"
  );
};

const changePage = (page) => {
  if (
      page >= 1 &&
      page <= totalPages.value
  ) {
    currentPage.value = page;
  }
};

/**
 * Mở form sửa sản phẩm.
 */
const editProduct = (product) => {
  editingProduct.value = product;
  activeTab.value = "create";
};

/**
 * Xử lý sau khi lưu sản phẩm.
 */
const handleEditSaved = async () => {
  editingProduct.value = null;
  activeTab.value = "list";

  await loadProducts();
};

/**
 * Đóng form sửa sản phẩm.
 */
const handleEditClose = () => {
  editingProduct.value = null;
  activeTab.value = "list";
};

/**
 * Lấy tồn kho đang chỉnh sửa.
 */
const getDraftStock = (product) => {
  const value = stockDrafts.value[product.id];

  return Math.max(
      0,
      Number(value ?? product.stock ?? 0)
  );
};

/**
 * Kiểm tra tồn kho đã thay đổi chưa.
 */
const isStockDirty = (product) => {
  return (
      getDraftStock(product) !==
      Number(product.stock || 0)
  );
};

/**
 * Trạng thái tồn kho đang chỉnh sửa.
 */
const getDraftStockStatus = (product) => {
  if (product.trangThaiCode === 0) {
    return "Ngưng bán";
  }

  if (product.trangThaiCode === 2) {
    return "Chờ duyệt";
  }

  return getDraftStock(product) > 0
      ? "Còn hàng"
      : "Hết hàng";
};

/**
 * Thay đổi tồn kho tạm thời.
 */
const changeStockDraft = (
    product,
    amount
) => {
  stockDrafts.value = {
    ...stockDrafts.value,

    [product.id]: Math.max(
        0,
        getDraftStock(product) + amount
    ),
  };
};

const increaseStock = (product) => {
  changeStockDraft(product, 1);
};

const decreaseStock = (product) => {
  changeStockDraft(product, -1);
};

/**
 * Lưu tồn kho.
 */
const saveStock = async (product) => {
  if (
      !isStockDirty(product) ||
      savingStockId.value === product.id
  ) {
    return;
  }

  try {
    savingStockId.value = product.id;

    await api.patch(
        `${API_URL}/${product.id}/ton-kho`,
        {
          soLuong: getDraftStock(product),
        }
    );

    ElMessage.success(
        "Đã lưu số lượng tồn kho."
    );

    await loadProducts();
  } catch (error) {
    console.error(
        "Lỗi lưu tồn kho:",
        error
    );

    ElMessage.error(
        error?.response?.data?.message ||
        "Không thể lưu tồn kho."
    );
  } finally {
    savingStockId.value = null;
  }
};

/**
 * Escape nội dung trước khi xuất Excel.
 */
const escapeExcelCell = (value) => {
  return String(value ?? "")
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;");
};

/**
 * Xuất danh sách sản phẩm.
 */
const exportProductsToExcel = () => {
  const rows = filteredProducts.value;

  if (!rows.length) {
    ElMessage.warning(
        "Không có sản phẩm để xuất."
    );
    return;
  }

  const headers = [
    "STT",
    "Mã sản phẩm",
    "Tên sản phẩm",
    "Loại",
    "Giá bán",
    "Khuyến mãi",
    "Tồn kho",
    "Trạng thái duyệt",
    "Trạng thái bán",
    "Vật liệu",
    "Màu sắc",
    "Kích thước",
    "Xuất xứ",
    "CNSX",
    "Ghi chú",
  ];

  const bodyRows = rows.map(
      (product, index) => [
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
      ]
  );

  const table = `
    <table border="1">
      <thead>
        <tr>
          ${headers
      .map(
          (header) =>
              `<th>${escapeExcelCell(header)}</th>`
      )
      .join("")}
        </tr>
      </thead>

      <tbody>
        ${bodyRows
      .map(
          (row) => `
              <tr>
                ${row
              .map(
                  (cell) =>
                      `<td>${escapeExcelCell(cell)}</td>`
              )
              .join("")}
              </tr>
            `
      )
      .join("")}
      </tbody>
    </table>
  `;

  const workbook = `
    <!DOCTYPE html>
    <html lang="vi">
      <head>
        <meta charset="UTF-8">
      </head>

      <body>
        ${table}
      </body>
    </html>
  `;

  const blob = new Blob(
      ["\ufeff", workbook],
      {
        type: "application/vnd.ms-excel;charset=utf-8",
      }
  );

  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");

  link.href = url;

  link.download =
      `danh-sach-san-pham-` +
      `${new Date().toISOString().slice(0, 10)}.xls`;

  document.body.appendChild(link);

  link.click();
  link.remove();

  URL.revokeObjectURL(url);

  ElMessage.success(
      `Đã xuất ${rows.length} sản phẩm.`
  );
};

/**
 * Ngưng bán sản phẩm.
 */
const ngungBanProduct = async (product) => {
  const confirmed = window.confirm(
      `Bạn có chắc chắn muốn ngưng bán "${product.name}" không?`
  );

  if (!confirmed) {
    return;
  }

  try {
    await api.patch(
        NGUNG_BAN_API(product.id)
    );

    ElMessage.success(
        "Ngưng bán sản phẩm thành công."
    );

    await loadProducts();
  } catch (error) {
    console.error(
        "Lỗi ngưng bán sản phẩm:",
        error
    );

    ElMessage.error(
        error?.response?.data?.message ||
        error?.response?.data ||
        "Không thể ngưng bán sản phẩm."
    );
  }
};

/**
 * Bán lại sản phẩm.
 */
const banLaiProduct = async (product) => {
  try {
    await api.patch(
        BAN_LAI_API(product.id)
    );

    ElMessage.success(
        "Đã chuyển sản phẩm sang bán lại."
    );

    await loadProducts();
  } catch (error) {
    console.error(
        "Lỗi bán lại sản phẩm:",
        error
    );

    ElMessage.error(
        error?.response?.data?.message ||
        error?.response?.data ||
        "Không thể bán lại sản phẩm."
    );
  }
};

/**
 * Hiển thị chi tiết sản phẩm.
 */
const showDetail = async (product) => {
  loadingProductDetail.value = true;
  selectedProductDetail.value = product;

  try {
    const response = await api.get(
        `${API_URL}/${product.id}`
    );

    selectedProductDetail.value =
        normalizeProduct(
            response.data || product
        );
  } catch (error) {
    console.error(
        "Lỗi tải chi tiết sản phẩm:",
        error
    );

    ElMessage.error(
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        "Không thể tải chi tiết sản phẩm."
    );

    selectedProductDetail.value = null;
  } finally {
    loadingProductDetail.value = false;
  }
};

/**
 * Đóng popup chi tiết.
 */
const closeDetail = () => {
  selectedProductDetail.value = null;
};

/**
 * Đóng popup khi bấm phím Escape.
 */
const handleEscapeKey = (event) => {
  if (
      event.key === "Escape" &&
      selectedProductDetail.value
  ) {
    closeDetail();
  }
};

onMounted(() => {
  loadProducts();

  window.addEventListener(
      "keydown",
      handleEscapeKey
  );
});

onBeforeUnmount(() => {
  window.removeEventListener(
      "keydown",
      handleEscapeKey
  );

  document.body.style.overflow =
      previousBodyOverflow;

  document.documentElement.style.overflow =
      previousHtmlOverflow;
});
</script>

<template>
  <div class="admin-layout">
    <section class="page-content">
      <template v-if="activeTab === 'list'">
        <div class="filter-row">
          <button
              class="date-btn"
              type="button"
          >
            01/05/2024 - 26/05/2024

            <i class="fa-regular fa-calendar"></i>
          </button>

          <select v-model="selectedStatus">
            <option value="">
              Tất cả trạng thái sản phẩm
            </option>

            <option value="con-hang">
              Còn hàng
            </option>

            <option value="het-hang">
              Hết hàng
            </option>

            <option value="ngung-ban">
              Ngưng bán
            </option>
          </select>

          <select v-model="selectedCategory">
            <option value="">
              Tất cả danh mục
            </option>

            <option
                v-for="category in categories"
                :key="category.id"
                :value="category.name"
            >
              {{ category.name }}
            </option>
          </select>

          <div class="search-input">
            <input
                v-model="keyword"
                type="text"
                placeholder="Tìm kiếm sản phẩm..."
            />

            <i
                class="fa-solid fa-magnifying-glass"
            ></i>
          </div>

          <button
              class="create-btn"
              type="button"
              @click="editingProduct = null; activeTab = 'create'"
          >
            <i class="fa-solid fa-plus"></i>
            Thêm sản phẩm
          </button>

          <button
              class="export-btn"
              type="button"
              @click="exportProductsToExcel"
          >
            <i class="fa-solid fa-download"></i>
            Xuất danh sách
          </button>
        </div>

        <div class="stats">
          <div class="stat-card">
            <div class="stat-icon red">
              <i
                  class="fa-solid fa-table-cells-large"
              ></i>
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

        <p
            v-if="loading"
            class="loading-message"
        >
          Đang tải sản phẩm...
        </p>

        <div
            v-else-if="paginatedProducts.length"
            class="product-grid"
        >
          <article
              v-for="product in paginatedProducts"
              :key="product.id"
              class="product-card"
          >
            <div
                class="approval-bar"
                :class="
                getApprovalClass(
                  product.trangThaiCode
                )
              "
            >
              <i
                  :class="
                  product.trangThaiCode === 1
                    ? 'fa-solid fa-circle-check'
                    : 'fa-regular fa-clock'
                "
              ></i>

              {{
                getApprovalLabel(
                    product.trangThaiCode
                )
              }}
            </div>

            <img
                :src="product.image"
                :alt="product.name"
            />

            <div class="product-info">
              <h3>{{ product.name }}</h3>

              <p class="product-sku">
                SKU: {{ product.sku }}
              </p>

              <p class="product-price">
                {{ formatPrice(product.price) }}
              </p>

              <div
                  class="stock-row"
                  :class="{
                  dirty: isStockDirty(product),
                }"
              >
                <div class="stock-copy">
                  <p class="stock-label">
                    Tồn kho:

                    <strong>
                      {{ getDraftStock(product) }}
                    </strong>
                  </p>

                  <span
                      class="status"
                      :class="
                      getDraftStockStatus(product) ===
                      'Còn hàng'
                        ? 'available'
                        : 'empty'
                    "
                  >
                    {{
                      getDraftStockStatus(product)
                    }}
                  </span>

                  <small
                      v-if="isStockDirty(product)"
                      class="unsaved-stock"
                  >
                    Chưa lưu
                  </small>
                </div>

                <div class="stock-control">
                  <button
                      type="button"
                      :disabled="
                      getDraftStock(product) === 0
                    "
                      @click="decreaseStock(product)"
                  >
                    −
                  </button>

                  <span>
                    {{ getDraftStock(product) }}
                  </span>

                  <button
                      type="button"
                      @click="increaseStock(product)"
                  >
                    +
                  </button>
                </div>
              </div>
            </div>

            <div class="card-actions">
              <button
                  class="detail-btn"
                  type="button"
                  @click="showDetail(product)"
              >
                <i class="fa-regular fa-eye"></i>
                Chi tiết
              </button>

              <button
                  class="edit-btn"
                  type="button"
                  @click="editProduct(product)"
              >
                <i class="fa-solid fa-pen"></i>
                Sửa
              </button>

              <button
                  v-if="product.trangThaiCode === 1"
                  class="hide-btn"
                  type="button"
                  @click="ngungBanProduct(product)"
              >
                <i class="fa-solid fa-ban"></i>
                Ngưng bán
              </button>

              <button
                  v-else-if="
                  product.trangThaiCode === 0
                "
                  class="hide-btn"
                  type="button"
                  @click="banLaiProduct(product)"
              >
                <i class="fa-solid fa-play"></i>
                Bán lại
              </button>

              <button
                  class="save-btn"
                  type="button"
                  :disabled="
                  !isStockDirty(product) ||
                  savingStockId === product.id
                "
                  @click="saveStock(product)"
              >
                <i
                    class="fa-regular fa-floppy-disk"
                ></i>

                {{
                  savingStockId === product.id
                      ? "Đang lưu"
                      : "Lưu"
                }}
              </button>
            </div>
          </article>
        </div>

        <div
            v-else
            class="empty-product-list"
        >
          Không tìm thấy sản phẩm phù hợp.
        </div>

        <div class="pagination-row">
          <p>
            Hiển thị {{ startItem }} -
            {{ endItem }} trong
            {{ filteredProducts.length }}
            sản phẩm
          </p>

          <div class="pagination">
            <button
                type="button"
                :disabled="currentPage === 1"
                @click="
                changePage(currentPage - 1)
              "
            >
              <i
                  class="fa-solid fa-chevron-left"
              ></i>
            </button>

            <button
                v-for="page in totalPages"
                :key="page"
                type="button"
                :class="{
                active: currentPage === page,
              }"
                @click="changePage(page)"
            >
              {{ page }}
            </button>

            <button
                type="button"
                :disabled="
                currentPage === totalPages
              "
                @click="
                changePage(currentPage + 1)
              "
            >
              <i
                  class="fa-solid fa-chevron-right"
              ></i>
            </button>
          </div>
        </div>
      </template>

      <template v-else-if="activeTab === 'create'">
        <TrangTaoSanPham
            :edit-id="
            editingProduct
              ? editingProduct.id
              : null
          "
            @saved="handleEditSaved"
            @close="handleEditClose"
        />
      </template>
    </section>

    <!--
      Teleport đưa popup ra ngoài layout hiện tại,
      tránh bị sidebar, header, overflow hoặc transform giới hạn.
    -->
    <Teleport to="body">
      <div
          v-if="selectedProductDetail"
          class="product-detail-overlay"
          @click.self="closeDetail"
      >
        <article
            class="product-detail-modal"
            role="dialog"
            aria-modal="true"
            aria-label="Chi tiết sản phẩm"
        >
          <button
              class="detail-close"
              type="button"
              aria-label="Đóng chi tiết sản phẩm"
              @click="closeDetail"
          >
            <i class="fa-solid fa-xmark"></i>
          </button>

          <div class="detail-image-wrap">
            <img
                :src="selectedProductDetail.image"
                :alt="selectedProductDetail.name"
            />

            <span
                class="approval-bar detail-status"
                :class="
                getApprovalClass(
                  selectedProductDetail.trangThaiCode
                )
              "
            >
              {{
                getApprovalLabel(
                    selectedProductDetail.trangThaiCode
                )
              }}
            </span>
          </div>

          <div class="detail-content">
            <div
                v-if="loadingProductDetail"
                class="detail-loading"
            >
              <i class="fa-solid fa-spinner fa-spin"></i>
              Đang tải chi tiết sản phẩm...
            </div>

            <template v-else>
              <div class="detail-heading-row">
                <div>
                  <p class="detail-eyebrow">
                    SẢN PHẨM
                    #{{ selectedProductDetail.id }}
                  </p>

                  <h2>
                    {{ selectedProductDetail.name }}
                  </h2>
                </div>

                <span
                    class="detail-stock-pill"
                    :class="
                    selectedProductDetail.stock > 0
                      ? 'in-stock'
                      : 'out-stock'
                  "
                >
                  <i
                      class="fa-solid fa-boxes-stacked"
                  ></i>

                  Tồn kho
                  {{ selectedProductDetail.stock }}
                </span>
              </div>

              <div class="detail-price-row">
                <p class="detail-price">
                  {{
                    formatPrice(
                        selectedProductDetail.price
                    )
                  }}
                </p>

                <span
                    v-if="
                    Number(
                      selectedProductDetail.khuyenMai ||
                      0
                    ) > 0
                  "
                    class="detail-promotion"
                >
                  Khuyến mãi
                  {{
                    formatPrice(
                        selectedProductDetail.khuyenMai
                    )
                  }}
                </span>
              </div>

              <div class="detail-grid">
                <div>
                  <span>Loại sản phẩm</span>

                  <strong>
                    {{
                      selectedProductDetail.category ||
                      "—"
                    }}
                  </strong>
                </div>

                <div>
                  <span>Vật liệu</span>

                  <strong>
                    {{
                      selectedProductDetail.vatLieu ||
                      "—"
                    }}
                  </strong>
                </div>

                <div>
                  <span>Nội thất</span>

                  <strong>
                    {{
                      selectedProductDetail.noiThat ||
                      "—"
                    }}
                  </strong>
                </div>

                <div>
                  <span>Màu sắc</span>

                  <strong>
                    {{
                      selectedProductDetail.mauSac ||
                      "—"
                    }}
                  </strong>
                </div>

                <div>
                  <span>Kích thước</span>

                  <strong>
                    {{
                      selectedProductDetail.kichThuoc ||
                      "—"
                    }}
                  </strong>
                </div>

                <div>
                  <span>Trọng lượng</span>

                  <strong>
                    {{
                      selectedProductDetail.trongLuong ||
                      "—"
                    }}
                  </strong>
                </div>

                <div>
                  <span>Quy cách</span>

                  <strong>
                    {{
                      selectedProductDetail.quyCach ||
                      "—"
                    }}
                  </strong>
                </div>

                <div>
                  <span>Thiết kế</span>

                  <strong>
                    {{
                      selectedProductDetail.thietKe ||
                      "—"
                    }}
                  </strong>
                </div>

                <div>
                  <span>Tôn giáo</span>

                  <strong>
                    {{
                      selectedProductDetail.tonGiao ||
                      "—"
                    }}
                  </strong>
                </div>

                <div>
                  <span>Xuất xứ / CNSX</span>

                  <strong>
                    {{
                      selectedProductDetail.xuatXu ||
                      "—"
                    }}
                    /
                    {{
                      selectedProductDetail.cnsx ||
                      "—"
                    }}
                  </strong>
                </div>
              </div>

              <section class="database-detail-section">
                <div class="detail-section-heading">
                  <div>
                    <h3>
                      Chi tiết cấu tạo sản phẩm
                    </h3>
                  </div>

                  <span class="detail-count">
                    {{
                      selectedProductDetail
                          .chiTietList.length
                    }}
                    mục
                  </span>
                </div>

                <div
                    v-if="
                    selectedProductDetail
                      .chiTietList.length
                  "
                    class="database-detail-list"
                >
                  <article
                      v-for="(
                      chiTiet, index
                    ) in selectedProductDetail.chiTietList"
                      :key="
                      chiTiet.maChiTiet || index
                    "
                      class="database-detail-item"
                  >
                    <div class="detail-order">
                      {{
                        chiTiet.thuTu ??
                        index + 1
                      }}
                    </div>

                    <div class="database-detail-content">
                      <h4>
                        {{
                          chiTiet.loaiKhoi ||
                          `Chi tiết ${index + 1}`
                        }}
                      </h4>

                      <p>
                        {{
                          chiTiet.noiDung ||
                          "Chưa có nội dung."
                        }}
                      </p>

                      <div
                          v-if="
                          selectedProductDetail.hinhAnhList.some(
                            (image) =>
                              image.maChiTiet ===
                              chiTiet.maChiTiet
                          )
                        "
                          class="detail-image-gallery inline-gallery"
                      >
                        <img
                            v-for="image in selectedProductDetail.hinhAnhList.filter(
                            (item) =>
                              item.maChiTiet ===
                              chiTiet.maChiTiet
                          )"
                            :key="
                            image.maHinhAnh ||
                            image.urlHinhAnh
                          "
                            :src="image.urlHinhAnh"
                            :alt="
                            image.loaiHinhAnh ||
                            chiTiet.loaiKhoi
                          "
                        />
                      </div>
                    </div>
                  </article>
                </div>

                <div
                    v-else
                    class="empty-database-detail"
                >
                  Sản phẩm này chưa có dữ liệu
                  trong bảng sanphamchitiet.
                </div>
              </section>

              <section
                  v-if="
                  selectedProductDetail.hinhAnhList.some(
                    (image) => !image.maChiTiet
                  )
                "
                  class="database-detail-section"
              >
                <div class="detail-section-heading">
                  <div>
                    <span
                        class="detail-section-kicker"
                    >
                      SANPHAMHINHANH
                    </span>

                    <h3>Hình ảnh bổ sung</h3>
                  </div>
                </div>

                <div class="detail-image-gallery">
                  <figure
                      v-for="image in selectedProductDetail.hinhAnhList.filter(
                      (item) => !item.maChiTiet
                    )"
                      :key="
                      image.maHinhAnh ||
                      image.urlHinhAnh
                    "
                  >
                    <img
                        :src="image.urlHinhAnh"
                        :alt="
                        image.loaiHinhAnh ||
                        selectedProductDetail.name
                      "
                    />

                    <figcaption>
                      {{
                        image.loaiHinhAnh ||
                        "Hình ảnh sản phẩm"
                      }}
                    </figcaption>
                  </figure>
                </div>
              </section>

              <div class="detail-description">
                <h4>Ghi chú</h4>

                <p>
                  {{
                    selectedProductDetail.ghiChu ||
                    "Chưa có ghi chú cho sản phẩm."
                  }}
                </p>
              </div>
            </template>
          </div>
        </article>
      </div>
    </Teleport>
  </div>
</template>

<style
    scoped
    src="../../assets/styles/doitac/QLSanPham/TrangQLSanPham.css"
></style>

<style scoped>
.loading-message,
.empty-product-list {
  padding: 32px 20px;
  color: #64748b;
  font-weight: 600;
  text-align: center;
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
  top: 12px;
  left: 12px;
  z-index: 2;
  box-shadow: 0 5px 15px rgba(15, 23, 42, 0.14);
}

.approval-bar.approved {
  color: #166534;
  background: #dcfce7;
}

.approval-bar.pending {
  color: #92400e;
  background: #fef3c7;
}

.approval-bar.hidden {
  color: #991b1b;
  background: #fee2e2;
}

/* =========================
   POPUP CHI TIẾT SẢN PHẨM
   ========================= */

.product-detail-overlay {
  position: fixed;
  inset: 0;
  z-index: 999999;

  display: flex;
  align-items: center;
  justify-content: center;

  width: 100vw;
  height: 100dvh;
  padding: 24px;
  box-sizing: border-box;

  overflow: auto;
  overscroll-behavior: contain;

  background: rgba(15, 23, 42, 0.74);
  backdrop-filter: blur(7px);
  -webkit-backdrop-filter: blur(7px);
}

.product-detail-modal {
  position: relative;

  display: grid;
  grid-template-columns:
    minmax(320px, 38%)
    minmax(0, 1fr);

  width: min(1120px, calc(100vw - 48px));
  height: min(760px, calc(100dvh - 48px));

  min-width: 0;
  min-height: 0;
  max-width: 1120px;
  max-height: calc(100dvh - 48px);

  margin: auto;
  box-sizing: border-box;
  overflow: hidden;

  background: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 24px;
  box-shadow:
      0 38px 100px rgba(15, 23, 42, 0.46),
      0 8px 28px rgba(15, 23, 42, 0.18);
}

.detail-close {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;

  display: inline-flex;
  align-items: center;
  justify-content: center;

  width: 42px;
  height: 42px;
  padding: 0;

  color: #334155;
  font-size: 18px;
  cursor: pointer;

  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(15, 23, 42, 0.1);
  border-radius: 50%;

  box-shadow: 0 7px 18px rgba(15, 23, 42, 0.14);

  transition:
      color 0.2s ease,
      background 0.2s ease,
      transform 0.2s ease;
}

.detail-close:hover {
  color: #ffffff;
  background: #9f1239;
  transform: rotate(8deg);
}

.detail-image-wrap {
  position: relative;

  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;

  overflow: hidden;

  background:
      radial-gradient(
          circle at 35% 20%,
          #fff7f8,
          #e9eef5 65%
      );
}

.detail-image-wrap::after {
  position: absolute;
  inset: auto 0 0;
  z-index: 1;

  height: 40%;
  pointer-events: none;

  content: "";

  background:
      linear-gradient(
          to top,
          rgba(15, 23, 42, 0.46),
          transparent
      );
}

.detail-image-wrap img {
  display: block;

  width: 100%;
  height: 100%;

  object-fit: cover;
  object-position: center;

  transition: transform 0.35s ease;
}

.detail-image-wrap:hover img {
  transform: scale(1.025);
}

.detail-status {
  position: absolute;
  bottom: 22px;
  left: 22px;
  z-index: 3;

  max-width: calc(100% - 44px);
}

.detail-content {
  min-width: 0;
  min-height: 0;
  height: 100%;

  padding: 42px 40px 36px;
  box-sizing: border-box;

  overflow-x: hidden;
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;

  background:
      linear-gradient(
          180deg,
          #ffffff 0%,
          #fbfcfe 100%
      );
}

.detail-content::-webkit-scrollbar {
  width: 9px;
}

.detail-content::-webkit-scrollbar-track {
  background: #f1f5f9;
}

.detail-content::-webkit-scrollbar-thumb {
  background: #94a3b8;
  border: 2px solid #f1f5f9;
  border-radius: 999px;
}

.detail-content::-webkit-scrollbar-thumb:hover {
  background: #64748b;
}

.detail-heading-row {
  display: flex;
  gap: 18px;
  align-items: flex-start;
  justify-content: space-between;
  padding-right: 30px;
}

.detail-heading-row > div {
  min-width: 0;
}

.detail-eyebrow {
  margin: 0 0 7px;

  color: #9f1239;
  font-size: 11px;
  font-weight: 850;
  letter-spacing: 0.12em;
}

.detail-content h2 {
  margin: 0;

  overflow-wrap: anywhere;

  color: #172033;
  font-size: clamp(25px, 3vw, 34px);
  line-height: 1.18;
  letter-spacing: -0.025em;
}

.detail-stock-pill {
  display: inline-flex;
  flex: 0 0 auto;
  gap: 7px;
  align-items: center;

  padding: 8px 11px;

  font-size: 11px;
  font-weight: 800;
  white-space: nowrap;

  border-radius: 999px;
}

.detail-stock-pill.in-stock {
  color: #166534;
  background: #dcfce7;
}

.detail-stock-pill.out-stock {
  color: #991b1b;
  background: #fee2e2;
}

.detail-price-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;

  margin: 16px 0 23px;
  padding-bottom: 20px;

  border-bottom: 1px solid #e7ebf0;
}

.detail-price {
  margin: 0;

  color: #9f1239;
  font-size: 27px;
  font-weight: 850;
}

.detail-promotion {
  padding: 6px 9px;

  color: #c2410c;
  font-size: 11px;
  font-weight: 750;

  background: #fff7ed;
  border-radius: 7px;
}

.detail-grid {
  display: grid;
  grid-template-columns:
    repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.detail-grid div {
  min-width: 0;
  min-height: 67px;
  padding: 12px 14px;
  box-sizing: border-box;

  background: #ffffff;
  border: 1px solid #e5eaf0;
  border-radius: 12px;

  box-shadow: 0 5px 14px rgba(15, 23, 42, 0.035);
}

.detail-grid span {
  display: block;
  margin-bottom: 5px;

  color: #7a8698;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.detail-grid strong {
  display: block;
  overflow: hidden;

  color: #263244;
  font-size: 13px;
  line-height: 1.4;

  overflow-wrap: anywhere;
  text-overflow: ellipsis;
}

.detail-description {
  margin-top: 22px;
  padding: 18px;

  background: #ffffff;
  border: 1px solid #e5eaf0;
  border-radius: 13px;
}

.detail-description h4 {
  margin: 0 0 8px;
  color: #263244;
}

.detail-description p {
  margin: 0;

  color: #596579;
  line-height: 1.7;
  overflow-wrap: anywhere;
}

.detail-loading {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: center;

  min-height: 420px;

  color: #64748b;
  font-weight: 700;
}

.database-detail-section {
  margin-top: 24px;
  padding-top: 22px;
  border-top: 1px solid #e5e7eb;
}

.detail-section-heading {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;

  margin-bottom: 14px;
}

.detail-section-heading h3 {
  margin: 3px 0 0;

  color: #1f2937;
  font-size: 18px;
}

.detail-section-kicker {
  color: #991b1b;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.detail-count {
  flex: 0 0 auto;

  padding: 5px 9px;

  color: #991b1b;
  font-size: 12px;
  font-weight: 700;

  background: #fef2f2;
  border-radius: 999px;
}

.database-detail-list {
  display: grid;
  gap: 12px;
}

.database-detail-item {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr);
  gap: 13px;

  padding: 16px;

  background: #ffffff;
  border: 1px solid #e5eaf0;
  border-radius: 13px;

  box-shadow: 0 5px 14px rgba(15, 23, 42, 0.03);
}

.database-detail-content {
  min-width: 0;
}

.detail-order {
  display: grid;
  width: 32px;
  height: 32px;

  color: #ffffff;
  font-size: 12px;
  font-weight: 800;

  background: #9f1239;
  border-radius: 10px;

  place-items: center;
}

.database-detail-item h4 {
  margin: 2px 0 7px;

  color: #1f2937;
  font-size: 15px;
}

.database-detail-item p {
  margin: 0;

  color: #4b5563;
  line-height: 1.65;

  overflow-wrap: anywhere;
  white-space: pre-line;
}

.empty-database-detail {
  padding: 20px;

  color: #64748b;
  text-align: center;

  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
}

.detail-image-gallery {
  display: grid;
  grid-template-columns:
    repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.detail-image-gallery figure {
  min-width: 0;
  margin: 0;
}

.detail-image-gallery img {
  display: block;

  width: 100%;
  height: 124px;

  object-fit: cover;

  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
}

.detail-image-gallery figcaption {
  margin-top: 5px;

  overflow: hidden;

  color: #64748b;
  font-size: 11px;

  text-overflow: ellipsis;
  white-space: nowrap;
}

.inline-gallery {
  grid-template-columns:
    repeat(2, minmax(0, 1fr));

  margin-top: 12px;
}

.inline-gallery img {
  height: 102px;
}

/* Tablet */
@media (max-width: 900px) {
  .product-detail-overlay {
    padding: 16px;
  }

  .product-detail-modal {
    grid-template-columns: 1fr;

    width: min(700px, calc(100vw - 32px));
    height: calc(100dvh - 32px);
    max-height: calc(100dvh - 32px);

    overflow-x: hidden;
    overflow-y: auto;

    border-radius: 20px;
  }

  .detail-image-wrap {
    flex: none;

    width: 100%;
    height: 280px;
    min-height: 280px;
    max-height: 280px;
  }

  .detail-content {
    height: auto;
    min-height: 0;

    padding: 32px 24px 28px;

    overflow: visible;
  }

  .detail-status {
    bottom: 18px;
    left: 18px;
  }
}

/* Mobile */
@media (max-width: 620px) {
  .product-detail-overlay {
    padding: 10px;
  }

  .product-detail-modal {
    width: calc(100vw - 20px);
    height: calc(100dvh - 20px);
    max-height: calc(100dvh - 20px);

    border-radius: 16px;
  }

  .detail-image-wrap {
    height: 220px;
    min-height: 220px;
    max-height: 220px;
  }

  .detail-close {
    top: 12px;
    right: 12px;

    width: 38px;
    height: 38px;
  }

  .detail-heading-row {
    display: grid;
    gap: 12px;
    padding-right: 25px;
  }

  .detail-stock-pill {
    width: fit-content;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .detail-image-gallery {
    grid-template-columns:
      repeat(2, minmax(0, 1fr));
  }

  .detail-content {
    padding: 26px 17px 22px;
  }

  .database-detail-item {
    grid-template-columns: 32px minmax(0, 1fr);
    gap: 10px;
    padding: 13px;
  }

  .detail-order {
    width: 29px;
    height: 29px;
  }
}

/* Màn hình rất nhỏ */
@media (max-width: 420px) {
  .product-detail-overlay {
    padding: 0;
  }

  .product-detail-modal {
    width: 100vw;
    height: 100dvh;
    max-height: 100dvh;

    border: none;
    border-radius: 0;
  }

  .detail-image-wrap {
    height: 200px;
    min-height: 200px;
    max-height: 200px;
  }

  .detail-image-gallery {
    grid-template-columns: 1fr;
  }

  .inline-gallery {
    grid-template-columns: 1fr;
  }
}
</style>