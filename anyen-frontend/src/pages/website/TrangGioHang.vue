<template>
  <main class="cart-page">
    <!-- HERO -->
    <section class="cart-hero">
      <div class="cart-container">
        <div class="cart-hero-content">
          <span class="cart-eyebrow"><i class="fa-solid fa-cart-shopping"></i> AN YÊN</span>

          <h1>Giỏ hàng</h1>

          <p>
            Quản lý sản phẩm bạn muốn mua, điều chỉnh số lượng và xem tổng giá.
          </p>
        </div>
      </div>
    </section>

    <!-- BREADCRUMB -->
    <div class="breadcrumb-bar">
      <div class="cart-container">
        <nav class="breadcrumb">
          <RouterLink to="/">
            Trang chủ
          </RouterLink>

          <i class="fa-solid fa-chevron-right"></i>

          <span>Giỏ hàng</span>
        </nav>
      </div>
    </div>

    <!-- CONTENT -->
    <section class="cart-content-section">
      <div class="cart-container">
        <!-- EMPTY -->
        <div
            v-if="cartItems.length === 0"
            class="empty-cart"
        >
          <div class="empty-cart-icon">
            <i class="fa-solid fa-cart-shopping"></i>
          </div>

          <h2>Giỏ hàng trống</h2>

          <p>
            Nhấn vào biểu tượng giỏ hàng trên sản phẩm để thêm vào giỏ hàng.
          </p>

          <RouterLink
              to="/san-pham"
              class="view-products-button"
          >
            <i class="fa-solid fa-arrow-left"></i>
            Xem danh sách sản phẩm
          </RouterLink>
        </div>

        <!-- CART -->
        <template v-else>
          <div class="cart-toolbar">
            <div class="cart-summary">
              <span class="summary-icon">
                <i class="fa-solid fa-cart-shopping"></i>
              </span>

              <div>
                <small>Sản phẩm trong giỏ</small>

                <strong>
                  {{ cartCount }} sản phẩm
                </strong>
              </div>
            </div>

            <div class="cart-actions">
              <button
                  type="button"
                  class="btn btn-danger"
                  :disabled="creatingOrder"
                  @click="handleCreateOrder"
              >
                <i
                    :class="
        creatingOrder
          ? 'fa-solid fa-spinner fa-spin'
          : 'fa-solid fa-file-circle-plus'
      "
                ></i>

                {{
                  creatingOrder
                      ? "Đang tạo đơn..."
                      : "Tạo đơn hàng"
                }}
              </button>
              <button
                  type="button"
                  class="export-button"
                  :disabled="exporting"
                  @click="exportToExcel"
              >
                <i
                    :class="
                      exporting
                        ? 'fa-solid fa-spinner fa-spin'
                        : 'fa-solid fa-file-excel'
                    "
                ></i>

                {{ exporting ? "Đang xuất..." : "Xuất Excel" }}
              </button>

              <button
                  type="button"
                  class="clear-button"
                  @click="confirmClearCart"
              >
                <i class="fa-solid fa-trash-can"></i>
                Xóa tất cả
              </button>
            </div>
          </div>

          <div class="cart-layout">
            <!-- PRODUCT LIST -->
            <div class="cart-list">
              <article
                  v-for="item in cartItems"
                  :key="item.id"
                  class="cart-item"
              >
                <RouterLink
                    :to="`/san-pham/${item.id}`"
                    class="cart-item-image"
                >
                  <img
                      :src="item.image || noImage"
                      :alt="item.name || 'Sản phẩm An Yên'"
                      @error="handleImageError"
                  />
                </RouterLink>

                <div class="cart-item-content">
                  <span class="product-code">
                    Mã sản phẩm: {{ item.id }}
                  </span>

                  <RouterLink
                      :to="`/san-pham/${item.id}`"
                      class="product-name"
                  >
                    {{ item.name }}
                  </RouterLink>

                  <p
                      v-if="item.subname"
                      class="product-subname"
                  >
                    {{ item.subname }}
                  </p>

                  <div class="product-information">
                    <span v-if="item.material">
                      <i class="fa-solid fa-layer-group"></i>
                      {{ item.material }}
                    </span>

                    <span v-if="item.religion">
                      <i class="fa-solid fa-place-of-worship"></i>
                      {{ item.religion }}
                    </span>
                  </div>

                  <div class="product-price-row">
                    <strong>
                      {{ formatPrice(item.price) }}
                    </strong>

                    <del v-if="item.oldPrice">
                      {{ formatPrice(item.oldPrice) }}
                    </del>
                  </div>
                </div>

                <div class="cart-item-actions">
                  <div class="quantity-control">
                    <button
                        type="button"
                        class="quantity-btn"
                        @click="decreaseQuantity(item.id)"
                        :disabled="(item.quantity || 1) <= 1"
                    >
                      <i class="fa-solid fa-minus"></i>
                    </button>

                    <input
                        type="number"
                        class="quantity-input"
                        :value="item.quantity || 1"
                        min="1"
                        @change="handleQuantityChange(item.id, $event)"
                        @blur="handleQuantityBlur(item.id, $event)"
                    >

                    <button
                        type="button"
                        class="quantity-btn"
                        @click="increaseQuantity(item.id)"
                    >
                      <i class="fa-solid fa-plus"></i>
                    </button>
                  </div>

                  <RouterLink
                      :to="`/san-pham/${item.id}`"
                      class="detail-button"
                  >
                    Xem chi tiết
                  </RouterLink>

                  <button
                      type="button"
                      class="remove-button"
                      aria-label="Xóa khỏi giỏ hàng"
                      @click="removeProduct(item.id)"
                  >
                    <i class="fa-solid fa-trash-can"></i>
                  </button>
                </div>
              </article>
            </div>

            <!-- SIDEBAR -->
            <aside class="cart-sidebar">
              <div class="cart-sidebar-card">
                <div class="sidebar-logo">
                  <img
                      :src="logoAnYen"
                      alt="Logo An Yên"
                  />
                </div>

                <h3>Tóm tắt giỏ hàng</h3>

                <div class="sidebar-row">
                  <span>Số sản phẩm</span>
                  <strong>{{ cartCount }}</strong>
                </div>

                <div class="sidebar-row">
                  <span>Tổng giá tham khảo</span>
                  <strong>{{ formatPrice(totalPrice) }}</strong>
                </div>

                <p class="price-note">
                  Giá sản phẩm có thể thay đổi. Vui lòng liên hệ An Yên để được tư vấn chính xác.
                </p>

                <button
                    type="button"
                    class="sidebar-export-button"
                    :disabled="exporting"
                    @click="exportToExcel"
                >
                  <i class="fa-solid fa-download"></i>
                  Tải danh sách Excel
                </button>

                <RouterLink
                    to="/san-pham"
                    class="continue-button"
                >
                  Tiếp tục xem sản phẩm
                </RouterLink>
              </div>
            </aside>
          </div>
        </template>
      </div>
    </section>
    <el-dialog
        v-model="showOrderDialog"
        title="Thông tin tạo đơn hàng"
        width="560px"
        append-to-body
        :close-on-click-modal="false"
    >
      <div class="customer-order-form">
        <div class="order-form-group">
          <label>
            Tên khách hàng
            <span>*</span>
          </label>

          <el-input
              v-model="orderForm.tenKhachHang"
              maxlength="30"
              placeholder="Nhập tên khách hàng"
          />
        </div>

        <div class="order-form-group">
          <label>
            Số điện thoại
            <span>*</span>
          </label>

          <el-input
              v-model="orderForm.soDienThoai"
              maxlength="10"
              inputmode="numeric"
              placeholder="Ví dụ: 0901234567"
              @input="limitCustomerPhone"
          />
        </div>

        <div class="order-form-group">
          <label>
            CCCD
            <span>*</span>
          </label>

          <el-input
              v-model="orderForm.cccd"
              maxlength="12"
              inputmode="numeric"
              placeholder="Nhập đúng 12 chữ số"
              @input="limitCustomerCCCD"
          />
        </div>

        <div class="order-form-group">
          <label>
            Số nhà đường
            <span>*</span>
          </label>

          <el-input
              v-model="orderForm.soNhaDuong"
              maxlength="255"
              placeholder="Nhập số nhà đường"
          />
        </div>
        <div class="order-form-group">
          <label>
            Phường xá
            <span>*</span>
          </label>

          <el-input
              v-model="orderForm.phuongXa"
              maxlength="50"
              placeholder="Nhập địa chỉ phường xá"
          />
        </div>
        <div class="order-form-group">
          <label>
            Quận huyện
            <span>*</span>
          </label>

          <el-input
              v-model="orderForm.quanHuyen"
              maxlength="50"
              placeholder="Nhập quận huyện"
          />
        </div>
        <div class="order-form-group">
          <label>
            Địa chỉ
            <span>*</span>
          </label>

          <el-input
              v-model="orderForm.tinhThanh"
              maxlength="50"
              placeholder="Nhập tỉnh thành"
          />
        </div>

        <div class="order-form-group">
          <label>Ghi chú</label>

          <el-input
              v-model="orderForm.ghiChu"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="Ghi chú thêm cho đơn hàng"
          />
        </div>
      </div>

      <template #footer>
        <div class="row">
        <button
            class="btn btn-danger col-2 text-center"
            :disabled="creatingOrder"
            @click="showOrderDialog = false"
        >
          Hủy
        </button>
<div class="col-6"></div>
        <button
            class="btn btn-danger col-4 text-center"
            :disabled="creatingOrder"
            @click="submitCustomerOrder"
        >
          <i
              :class="
            creatingOrder
              ? 'fa-solid fa-spinner fa-spin'
              : 'fa-solid fa-check'
          "
          ></i>

          {{
            creatingOrder
                ? "Đang tạo đơn..."
                : "Xác nhận tạo đơn"
          }}
        </button>
        </div>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { computed, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import ExcelJS from "exceljs";

import { useCart } from "../../services/useCart.js";

import noImage from "../../assets/images/noimage.jpg";
import logoAnYen from "../../assets/images/icon/logoAnYen.png";
import {
  taoDonHangKhachHang
} from "../../services/donHangKhachHangService.js";
const {
  cartItems,
  cartCount,
  removeFromCart,
  clearCart,
  increaseQuantity,
  decreaseQuantity,
  updateQuantity
} = useCart();

const exporting = ref(false);
const showOrderDialog = ref(false);
const creatingOrder = ref(false);

const orderForm = ref({
  tenKhachHang: "",
  soDienThoai: "",
  cccd: "",
  soNhaDuong: "",
  phuongXa: "",
  quanHuyen: "",
  tinhThanh: "",
  ghiChu: ""
});
function onlyDigits(value = "") {
  return String(value).replace(/\D/g, "");
}

function normalizeVietnamPhone(value = "") {
  let phone = String(value)
      .trim()
      .replace(/\s+/g, "");

  if (phone.startsWith("+84")) {
    phone = "0" + phone.slice(3);
  }

  phone = phone.replace(/\D/g, "");

  if (
      phone.startsWith("84") &&
      phone.length === 11
  ) {
    phone = "0" + phone.slice(2);
  }

  return phone;
}

function isValidVietnamPhone(value = "") {
  const phone =
      normalizeVietnamPhone(value);

  return /^0(3|5|7|8|9)[0-9]{8}$/.test(
      phone
  );
}

function isValidCCCD(value = "") {
  return /^[0-9]{12}$/.test(
      String(value).trim()
  );
}

function limitCustomerPhone() {
  orderForm.value.soDienThoai =
      normalizeVietnamPhone(
          orderForm.value.soDienThoai
      ).slice(0, 10);
}

function limitCustomerCCCD() {
  orderForm.value.cccd =
      onlyDigits(
          orderForm.value.cccd
      ).slice(0, 12);
}
function normalizeText(value = "") {
  return String(value)
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .replace(/đ/g, "d")
      .replace(/Đ/g, "D")
      .toLowerCase()
      .trim();
}

function isCoffinProduct(item) {
  const categoryText = normalizeText(
      [
        item.loai,
        item.subname
      ]
          .filter(Boolean)
          .join(" ")
  );

  if (categoryText.includes("quan tai")) {
    return true;
  }

  /*
   * Dùng tên sản phẩm làm phương án dự phòng.
   * Có ích cho dữ liệu cũ chưa có trường loai.
   */
  const productName =
      normalizeText(item.name);

  return productName.includes("quan tai");
}

const coffinItems = computed(() => {
  return cartItems.value.filter(
      isCoffinProduct
  );
});

const hasCoffinProduct = computed(() => {
  return coffinItems.value.length > 0;
});
async function handleCreateOrder() {
  if (cartItems.value.length === 0) {
    ElMessage.warning(
        "Giỏ hàng chưa có sản phẩm"
    );

    return;
  }

  /*
   * Có quan tài thì chặn ngay,
   * không bắt khách nhập thông tin rồi mới báo.
   */
  if (hasCoffinProduct.value) {
    const coffinNames = coffinItems.value
        .map(item => item.name)
        .filter(Boolean)
        .join(", ");

    await ElMessageBox.alert(
        coffinNames
            ? `Đơn hàng có sản phẩm quan tài: ${coffinNames}. Vui lòng liên hệ nhân viên An Yên để được tư vấn và tạo hợp đồng.`
            : "Đơn hàng có sản phẩm quan tài. Vui lòng liên hệ nhân viên An Yên để được tư vấn và tạo hợp đồng.",
        "Cần liên hệ nhân viên",
        {
          confirmButtonText: "Đã hiểu",
          type: "warning",
          closeOnClickModal: false,
          closeOnPressEscape: false
        }
    );

    return;
  }

  showOrderDialog.value = true;
}
function getTodayLocalDate() {
  const date = new Date();

  const year = date.getFullYear();

  const month = String(
      date.getMonth() + 1
  ).padStart(2, "0");

  const day = String(
      date.getDate()
  ).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

function buildCustomerOrderPayload() {
  return {
    tenKhachHang:
    orderForm.value.tenKhachHang,

    soDienThoai:
    orderForm.value.soDienThoai,

    cccd:
    orderForm.value.cccd,

    soNhaDuong:
    orderForm.value.soNhaDuong,

    phuongXa:
    orderForm.value.phuongXa,

    quanHuyen:
    orderForm.value.quanHuyen,

    tinhThanh:
    orderForm.value.tinhThanh,

    email: "",

    ghiChu:
    orderForm.value.ghiChu,

    phuongThucThanhToan: 0,

    trangThaiThanhToan: 0,

    items: cartItems.value.map(item => ({
      maSanPham:
          Number(item.id),

      soLuong:
          Math.max(
              1,
              Number(item.quantity) || 1
          )
    }))
  };
}
function resetOrderForm() {
  orderForm.value = {
    tenKhachHang: "",
    soDienThoai: "",
    cccd: "",
    soNhaDuong:"",
    phuongXa:"",
    quanHuyen:"",
    tinhThanh:"",
    ghiChu: ""
  };
}

async function submitCustomerOrder() {
  if (creatingOrder.value) {
    return;
  }

  // Chuẩn hóa dữ liệu
  orderForm.value.tenKhachHang = String(
      orderForm.value.tenKhachHang || ""
  ).trim();

  orderForm.value.soDienThoai = normalizeVietnamPhone(
      orderForm.value.soDienThoai
  ).slice(0, 10);

  orderForm.value.cccd = onlyDigits(
      orderForm.value.cccd
  ).slice(0, 12);

  orderForm.value.soNhaDuong = String(
      orderForm.value.soNhaDuong || ""
  ).trim();

  orderForm.value.tinhThanh = String(
      orderForm.value.tinhThanh || ""
  ).trim();

  orderForm.value.phuongXa = String(
      orderForm.value.phuongXa || ""
  ).trim();

  orderForm.value.quanHuyen = String(
      orderForm.value.quanHuyen || ""
  ).trim();

  orderForm.value.ghiChu = String(
      orderForm.value.ghiChu || ""
  ).trim();

  /*
   * 1. KIỂM TRA TÊN KHÁCH HÀNG
   */
  if (!orderForm.value.tenKhachHang) {
    ElMessage.warning("Vui lòng nhập tên khách hàng");
    return;
  }

  if (orderForm.value.tenKhachHang.length > 30) {
    ElMessage.warning("Tên khách hàng tối đa 30 ký tự");
    return;
  }

  /*
   * 2. KIỂM TRA SỐ ĐIỆN THOẠI
   */
  if (!orderForm.value.soDienThoai) {
    ElMessage.warning("Vui lòng nhập số điện thoại");
    return;
  }

  if (!isValidVietnamPhone(orderForm.value.soDienThoai)) {
    ElMessage.warning(
        "Số điện thoại không hợp lệ. Ví dụ: 0901234567"
    );
    return;
  }

  /*
   * 3. KIỂM TRA CCCD BẮT BUỘC
   */
  if (!orderForm.value.cccd) {
    ElMessage.warning("Vui lòng nhập CCCD");
    return;
  }

  if (!isValidCCCD(orderForm.value.cccd)) {
    ElMessage.warning("CCCD phải gồm đúng 12 chữ số");
    return;
  }

  /*
   * 4. KIỂM TRA ĐỊA CHỈ BẮT BUỘC
   */
  if (!orderForm.value.soNhaDuong) {
    ElMessage.warning("Vui lòng nhập số nhà");
    return;
  }

  if (orderForm.value.soNhaDuong.length > 255) {
    ElMessage.warning("Số nhà tối đa 255 ký tự");
    return;
  }

  if (!orderForm.value.quanHuyen) {
    ElMessage.warning("Vui lòng nhập quận huyện");
    return;
  }

  if (orderForm.value.quanHuyen.length > 50) {
    ElMessage.warning("Quận huyện tối đa 50 ký tự");
    return;
  }

  if (!orderForm.value.phuongXa) {
    ElMessage.warning("Vui lòng nhập phường xá");
    return;
  }

  if (orderForm.value.phuongXa.length > 50) {
    ElMessage.warning("Phường xá tối đa 50 ký tự");
    return;
  }

  if (!orderForm.value.tinhThanh) {
    ElMessage.warning("Vui lòng nhập tỉnh thành");
    return;
  }

  if (orderForm.value.tinhThanh.length > 50) {
    ElMessage.warning("Tỉnh thành tối đa 50 ký tự");
    return;
  }
  /*
   * 5. KIỂM TRA GIỎ HÀNG
   */
  if (cartItems.value.length === 0) {
    ElMessage.warning("Giỏ hàng chưa có sản phẩm");
    showOrderDialog.value = false;
    return;
  }

  /*
   * 6. KIỂM TRA LẠI QUAN TÀI
   *
   * Dù đã kiểm tra ở handleCreateOrder,
   * vẫn nên kiểm tra thêm lần nữa trước khi gọi API.
   */
  if (hasCoffinProduct.value) {
    showOrderDialog.value = false;

    const coffinNames = coffinItems.value
        .map(item => item.name)
        .filter(Boolean)
        .join(", ");

    await ElMessageBox.alert(
        coffinNames
            ? `Đơn hàng có sản phẩm quan tài: ${coffinNames}. Vui lòng liên hệ nhân viên An Yên để được tư vấn và tạo hợp đồng.`
            : "Đơn hàng có sản phẩm quan tài. Vui lòng liên hệ nhân viên An Yên để được tư vấn và tạo hợp đồng.",
        "Cần liên hệ nhân viên",
        {
          confirmButtonText: "Đã hiểu",
          type: "warning",
          closeOnClickModal: false
        }
    );

    return;
  }

  /*
   * 7. XÁC NHẬN TRƯỚC KHI TẠO
   */
  try {
    await ElMessageBox.confirm(
        `Bạn có chắc muốn tạo đơn hàng gồm ${cartCount.value} sản phẩm không?`,
        "Xác nhận tạo đơn",
        {
          confirmButtonText: "Tạo đơn",
          cancelButtonText: "Kiểm tra lại",
          type: "info"
        }
    );
  } catch {
    return;
  }

  creatingOrder.value = true;

  try {
    const payload = buildCustomerOrderPayload();

    console.log("Payload gửi tạo đơn:", payload);

    const createdOrder =
        await taoDonHangKhachHang(payload);

    const orderCode =
        createdOrder?.maCode ||
        createdOrder?.maDonHang ||
        "";

    ElMessage.success(
        orderCode
            ? `Tạo đơn hàng ${orderCode} thành công`
            : "Tạo đơn hàng thành công"
    );

    /*
     * Chỉ xóa giỏ khi API thành công.
     */
    clearCart();

    resetOrderForm();

    showOrderDialog.value = false;
  } catch (error) {
    console.error("Lỗi tạo đơn hàng:", error);

    const message =
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        error?.message ||
        "Không thể tạo đơn hàng";

    ElMessage.error(message);
  } finally {
    creatingOrder.value = false;
  }
}
const totalPrice = computed(() => {
  return cartItems.value.reduce((total, product) => {
    const price = Number(product.price);
    const quantity = Number(product.quantity) || 1;

    return total + (
        Number.isFinite(price)
            ? price * quantity
            : 0
    );
  }, 0);
});

function formatPrice(value) {
  if (
      value === null
      || value === undefined
      || value === ""
  ) {
    return "Liên hệ";
  }

  const numberValue = Number(value);

  if (!Number.isFinite(numberValue)) {
    return "Liên hệ";
  }

  return numberValue.toLocaleString("vi-VN") + " đ";
}

function handleImageError(event) {
  const image = event.currentTarget;

  if (image.dataset.fallbackApplied === "true") {
    return;
  }

  image.dataset.fallbackApplied = "true";
  image.src = noImage;
}

function removeProduct(productId) {
  removeFromCart(productId);

  ElMessage.success(
      "Đã xóa sản phẩm khỏi giỏ hàng"
  );
}

function handleQuantityChange(productId, event) {
  const value = parseInt(event.target.value);
  if (value && value > 0) {
    updateQuantity(productId, value);
  } else {
    // Reset về 1 nếu nhập giá trị không hợp lệ
    event.target.value = 1;
    updateQuantity(productId, 1);
  }
}

function handleQuantityBlur(productId, event) {
  const value = parseInt(event.target.value);
  if (!value || value < 1) {
    event.target.value = 1;
    updateQuantity(productId, 1);
  }
}

async function confirmClearCart() {
  try {
    await ElMessageBox.confirm(
        "Bạn có chắc muốn xóa toàn bộ sản phẩm đã lưu không?",
        "Xóa danh sách",
        {
          confirmButtonText: "Xóa tất cả",
          cancelButtonText: "Hủy",
          type: "warning"
        }
    );

    clearCart();

    ElMessage.success(
        "Đã xóa toàn bộ sản phẩm"
    );
  } catch {
    // Người dùng bấm hủy
  }
}

function formatSavedDate(dateValue) {
  if (!dateValue) {
    return "";
  }

  const date = new Date(dateValue);

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  return date.toLocaleString("vi-VN");
}

function createExcelFileName() {
  const date = new Date();

  const day = String(
      date.getDate()
  ).padStart(2, "0");

  const month = String(
      date.getMonth() + 1
  ).padStart(2, "0");

  const year = date.getFullYear();

  const hours = String(
      date.getHours()
  ).padStart(2, "0");

  const minutes = String(
      date.getMinutes()
  ).padStart(2, "0");

  return `Danh-sach-san-pham-An-Yen-${day}-${month}-${year}-${hours}-${minutes}.xlsx`;
}

function downloadExcelFile(buffer, fileName) {
  const blob = new Blob(
      [buffer],
      {
        type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
      }
  );

  const downloadUrl = URL.createObjectURL(blob);

  const link = document.createElement("a");

  link.href = downloadUrl;
  link.download = fileName;

  document.body.appendChild(link);

  link.click();
  link.remove();

  URL.revokeObjectURL(downloadUrl);
}

async function loadLogoBuffer() {
  const response = await fetch(logoAnYen);

  if (!response.ok) {
    throw new Error(
        "Không thể tải logo An Yên"
    );
  }

  return await response.arrayBuffer();
}

async function exportToExcel() {
  if (cartItems.value.length === 0) {
    ElMessage.warning(
        "Chưa có sản phẩm để xuất Excel"
    );

    return;
  }

  exporting.value = true;

  try {
    const workbook = new ExcelJS.Workbook();

    workbook.creator = "Hệ thống An Yên";
    workbook.company = "An Yên";
    workbook.subject = "Danh sách sản phẩm khách hàng quan tâm";
    workbook.title = "Danh sách sản phẩm An Yên";
    workbook.created = new Date();

    const worksheet = workbook.addWorksheet(
        "Sản phẩm đã lưu",
        {
          views: [
            {
              showGridLines: false,
              state: "frozen",
              ySplit: 7
            }
          ]
        }
    );

    worksheet.columns = [
      {
        key: "index",
        width: 8
      },
      {
        key: "code",
        width: 18
      },
      {
        key: "name",
        width: 40
      },
      {
        key: "category",
        width: 22
      },
      {
        key: "material",
        width: 22
      },
      {
        key: "religion",
        width: 22
      },
      {
        key: "price",
        width: 20
      },
      {
        key: "savedAt",
        width: 24
      }
    ];

    /*
     * LOGO
     */
    try {
      const logoBuffer = await loadLogoBuffer();

      const logoId = workbook.addImage({
        buffer: logoBuffer,
        extension: "png"
      });

      worksheet.addImage(
          logoId,
          {
            tl: {
              col: 0.2,
              row: 0.2
            },
            ext: {
              width: 120,
              height: 70
            }
          }
      );
    } catch (logoError) {
      console.warn(
          "Không thể thêm logo vào Excel:",
          logoError
      );
    }

    /*
     * TIÊU ĐỀ
     */
    worksheet.mergeCells("C1:H2");

    const titleCell = worksheet.getCell("C1");

    titleCell.value = "DANH SÁCH SẢN PHẨM AN YÊN";
    titleCell.font = {
      name: "Arial",
      size: 18,
      bold: true,
      color: {
        argb: "FF1B7896"
      }
    };
    titleCell.alignment = {
      vertical: "middle",
      horizontal: "center"
    };

    worksheet.mergeCells("C3:H3");

    const descriptionCell = worksheet.getCell("C3");

    descriptionCell.value =
        "Danh sách sản phẩm khách hàng đang quan tâm";
    descriptionCell.font = {
      name: "Arial",
      size: 11,
      italic: true,
      color: {
        argb: "FF666666"
      }
    };
    descriptionCell.alignment = {
      horizontal: "center"
    };

    worksheet.mergeCells("A5:H5");

    const dateCell = worksheet.getCell("A5");

    dateCell.value =
        `Thời điểm xuất: ${new Date().toLocaleString("vi-VN")}`;
    dateCell.font = {
      name: "Arial",
      size: 10,
      italic: true
    };
    dateCell.alignment = {
      horizontal: "right"
    };

    /*
     * HEADER BẢNG
     */
    const headerRow = worksheet.getRow(7);

    headerRow.values = [
      "STT",
      "Mã sản phẩm",
      "Tên sản phẩm",
      "Phân loại",
      "Chất liệu",
      "Tôn giáo",
      "Giá tham khảo",
      "Thời gian lưu"
    ];

    headerRow.height = 28;

    headerRow.eachCell((cell) => {
      cell.font = {
        name: "Arial",
        size: 11,
        bold: true,
        color: {
          argb: "FFFFFFFF"
        }
      };

      cell.fill = {
        type: "pattern",
        pattern: "solid",
        fgColor: {
          argb: "FF1B7896"
        }
      };

      cell.alignment = {
        vertical: "middle",
        horizontal: "center",
        wrapText: true
      };

      cell.border = {
        top: {
          style: "thin",
          color: {
            argb: "FFD9E3E8"
          }
        },
        left: {
          style: "thin",
          color: {
            argb: "FFD9E3E8"
          }
        },
        bottom: {
          style: "thin",
          color: {
            argb: "FFD9E3E8"
          }
        },
        right: {
          style: "thin",
          color: {
            argb: "FFD9E3E8"
          }
        }
      };
    });

    /*
     * DỮ LIỆU SẢN PHẨM
     */
    cartItems.value.forEach(
        (product, index) => {
          const row = worksheet.addRow({
            index: index + 1,
            code: product.id,
            name: product.name || "",
            category: product.subname || "",
            material: product.material || "",
            religion: product.religion || "",
            price: Number(product.price) || 0,
            savedAt: formatSavedDate(
                product.addedAt
            )
          });

          row.height = 25;

          row.eachCell((cell) => {
            cell.font = {
              name: "Arial",
              size: 10
            };

            cell.alignment = {
              vertical: "middle",
              wrapText: true
            };

            cell.border = {
              top: {
                style: "thin",
                color: {
                  argb: "FFE3E9EC"
                }
              },
              left: {
                style: "thin",
                color: {
                  argb: "FFE3E9EC"
                }
              },
              bottom: {
                style: "thin",
                color: {
                  argb: "FFE3E9EC"
                }
              },
              right: {
                style: "thin",
                color: {
                  argb: "FFE3E9EC"
                }
              }
            };
          });

          row.getCell(1).alignment = {
            vertical: "middle",
            horizontal: "center"
          };

          row.getCell(2).alignment = {
            vertical: "middle",
            horizontal: "center"
          };

          row.getCell(7).numFmt =
              '#,##0 "đ"';

          if (index % 2 !== 0) {
            row.eachCell((cell) => {
              cell.fill = {
                type: "pattern",
                pattern: "solid",
                fgColor: {
                  argb: "FFF3F8FA"
                }
              };
            });
          }
        }
    );

    /*
     * TỔNG KẾT
     */
    const summaryRowNumber =
        worksheet.rowCount + 2;

    worksheet.mergeCells(
        `A${summaryRowNumber}:F${summaryRowNumber}`
    );

    const summaryLabel = worksheet.getCell(
        `A${summaryRowNumber}`
    );

    summaryLabel.value =
        `Tổng cộng: ${cartCount.value} sản phẩm`;

    summaryLabel.font = {
      name: "Arial",
      bold: true,
      size: 11
    };

    summaryLabel.alignment = {
      horizontal: "right"
    };

    const totalCell = worksheet.getCell(
        `G${summaryRowNumber}`
    );

    totalCell.value = totalPrice.value;
    totalCell.numFmt = '#,##0 "đ"';
    totalCell.font = {
      name: "Arial",
      bold: true,
      size: 11,
      color: {
        argb: "FF1B7896"
      }
    };

    worksheet.getCell(
        `H${summaryRowNumber}`
    ).value = "";

    /*
     * GHI CHÚ
     */
    const noteRowNumber =
        summaryRowNumber + 3;

    worksheet.mergeCells(
        `A${noteRowNumber}:H${noteRowNumber + 1}`
    );

    const noteCell = worksheet.getCell(
        `A${noteRowNumber}`
    );

    noteCell.value =
        "Lưu ý: Giá sản phẩm trong danh sách chỉ mang tính tham khảo tại thời điểm xuất. Vui lòng liên hệ An Yên để được tư vấn và xác nhận giá chính xác.";

    noteCell.font = {
      name: "Arial",
      size: 10,
      italic: true,
      color: {
        argb: "FF666666"
      }
    };

    noteCell.alignment = {
      vertical: "middle",
      horizontal: "left",
      wrapText: true
    };

    /*
     * XUẤT FILE
     */
    const buffer =
        await workbook.xlsx.writeBuffer();

    downloadExcelFile(
        buffer,
        createExcelFileName()
    );

    ElMessage.success(
        "Xuất file Excel thành công"
    );
  } catch (error) {
    console.error(
        "Lỗi xuất file Excel:",
        error
    );

    ElMessage.error(
        "Không thể xuất file Excel"
    );
  } finally {
    exporting.value = false;
  }
}
</script>

<style scoped>
.cart-page {
  --navy: #0b3453;
  --navy-dark: #07263d;
  --red: #a60b27;
  --red-dark: #85091f;
  --gold: #b48226;
  --cream: #fbf6ef;
  --paper: #ffffff;
  --line: #e7e0d8;
  --muted: #718087;

  min-height: 100vh;
  background: #f7f4ef;
  color: var(--navy);
  font-family: "Be Vietnam Pro", Arial, sans-serif;
}

.cart-container {
  width: min(1200px, calc(100% - 40px));
  margin: 0 auto;
}

/* Hero giữ phong cách An Yên, không dùng màu cam sàn TMĐT */
.cart-hero {
  padding: 132px 0 42px;
  color: #ffffff;
  background:
      radial-gradient(circle at 80% 20%, rgba(255,255,255,.13), transparent 34%),
      linear-gradient(135deg, rgba(11,52,83,.98), rgba(21,64,96,.93));
  border-bottom: 4px solid rgba(166, 11, 39, .9);
}

.cart-hero-content {
  max-width: 720px;
}

.cart-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #f0d7a8;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 3px;
}

.cart-hero h1 {
  margin: 0 0 10px;
  font-size: clamp(34px, 4vw, 50px);
  font-weight: 800;
}

.cart-hero p {
  max-width: 650px;
  margin: 0;
  color: rgba(255,255,255,.83);
  font-size: 15px;
  line-height: 1.75;
}

.breadcrumb-bar {
  background: #ffffff;
  border-bottom: 1px solid #e8e3dc;
}

.breadcrumb {
  min-height: 52px;
  display: flex;
  align-items: center;
  gap: 11px;
  font-size: 13px;
}

.breadcrumb a {
  color: var(--red);
  text-decoration: none;
  font-weight: 700;
}

.breadcrumb i {
  color: #b3bcc1;
  font-size: 9px;
}

.breadcrumb span {
  color: #6f7b81;
}

.cart-content-section {
  padding: 34px 0 76px;
}

/* Thanh đầu trang bố trí theo sàn TMĐT */
.cart-toolbar {
  min-height: 68px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 14px;
  padding: 12px 18px;
  background: var(--paper);
  border: 1px solid var(--line);
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(11, 52, 83, .05);
}

.cart-summary {
  display: flex;
  align-items: center;
  gap: 12px;
}

.summary-icon {
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #ffffff;
  background: var(--red);
  box-shadow: 0 5px 14px rgba(166,11,39,.18);
}

.cart-summary div {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.cart-summary small {
  color: #7c888e;
  font-size: 11px;
}

.cart-summary strong {
  color: var(--navy);
  font-size: 16px;
}

.cart-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.export-button,
.clear-button,
.sidebar-export-button,
.continue-button,
.detail-button,
.view-products-button {
  border: none;
  text-decoration: none;
  cursor: pointer;
  transition: all .22s ease;
}

.export-button,
.clear-button {
  min-height: 40px;
  padding: 0 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 5px;
  font-size: 13px;
  font-weight: 700;
}

.export-button {
  color: #ffffff;
  background: var(--navy);
}

.export-button:hover {
  background: var(--navy-dark);
  transform: translateY(-1px);
}

.clear-button {
  color: var(--red);
  background: #fff4f5;
  border: 1px solid rgba(166,11,39,.18);
}

.clear-button:hover {
  color: #ffffff;
  background: var(--red);
}

.export-button:disabled,
.sidebar-export-button:disabled {
  opacity: .62;
  cursor: not-allowed;
  transform: none;
}

.cart-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 330px;
  gap: 18px;
  align-items: start;
}

/* Danh sách dạng hàng ngang giống cách bố trí Shopee */
.cart-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cart-item {
  position: relative;
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr) 190px;
  gap: 18px;
  align-items: center;
  min-height: 154px;
  padding: 14px 16px;
  background: #ffffff;
  border: 1px solid var(--line);
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(11,52,83,.045);
  transition: border-color .22s ease, box-shadow .22s ease, transform .22s ease;
}

.cart-item::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  width: 4px;
  border-radius: 6px 0 0 6px;
  background: transparent;
  transition: background .22s ease;
}

.cart-item:hover {
  border-color: rgba(166,11,39,.28);
  box-shadow: 0 8px 24px rgba(11,52,83,.09);
  transform: translateY(-1px);
}

.cart-item:hover::before {
  background: var(--red);
}

.cart-item-image {
  width: 132px;
  height: 120px;
  overflow: hidden;
  border-radius: 5px;
  background: #f1f2f2;
  border: 1px solid #ece7e1;
}

.cart-item-image img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  transition: transform .3s ease;
}

.cart-item:hover .cart-item-image img {
  transform: scale(1.04);
}

.cart-item-content {
  min-width: 0;
}

.product-code {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  margin-bottom: 7px;
  padding: 0 8px;
  border-radius: 3px;
  color: var(--red);
  background: rgba(166,11,39,.07);
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .3px;
}

.product-name {
  display: block;
  width: fit-content;
  max-width: 100%;
  margin-bottom: 6px;
  overflow: hidden;
  color: var(--navy);
  font-size: 17px;
  font-weight: 800;
  text-decoration: none;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-name:hover {
  color: var(--red);
}

.product-subname {
  margin: 0 0 10px;
  color: #6f7d83;
  font-size: 12px;
}

.product-information {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  margin-bottom: 11px;
}

.product-information span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #65747a;
  font-size: 11px;
}

.product-information i {
  color: var(--gold);
}

.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.product-price-row strong {
  color: var(--red);
  font-size: 19px;
  font-weight: 900;
}

.product-price-row del {
  color: #a2aaae;
  font-size: 12px;
}

.cart-item-actions {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: center;
  gap: 9px;
  padding-left: 16px;
  border-left: 1px solid #eee8e2;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}

.quantity-btn {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #ddd7d0;
  border-radius: 4px;
  background: #ffffff;
  color: var(--navy);
  cursor: pointer;
  transition: all .22s ease;
}

.quantity-btn:hover:not(:disabled) {
  border-color: var(--red);
  color: var(--red);
  background: #fff7f7;
}

.quantity-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.quantity-input {
  width: 50px;
  height: 32px;
  text-align: center;
  font-size: 14px;
  font-weight: 700;
  color: var(--navy);
  border: 1px solid #ddd7d0;
  border-radius: 4px;
  background: #ffffff;
  padding: 0;
  -moz-appearance: textfield;
}

.quantity-input::-webkit-outer-spin-button,
.quantity-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.quantity-input:focus {
  outline: none;
  border-color: var(--red);
}

.detail-button {
  min-height: 40px;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 5px;
  color: #ffffff;
  background: var(--red);
  font-size: 12px;
  font-weight: 800;
}

.detail-button:hover {
  background: var(--red-dark);
  box-shadow: 0 5px 14px rgba(166,11,39,.18);
}

.remove-button {
  width: 100%;
  min-height: 36px;
  border: 1px solid #ddd7d0;
  border-radius: 5px;
  color: #7b858a;
  background: #ffffff;
  cursor: pointer;
  transition: all .22s ease;
}

.remove-button::after {
  content: " Xóa khỏi giỏ";
  margin-left: 7px;
  font-family: "Be Vietnam Pro", Arial, sans-serif;
  font-size: 11px;
  font-weight: 700;
}

.remove-button:hover {
  color: var(--red);
  border-color: rgba(166,11,39,.38);
  background: #fff7f7;
}

.cart-sidebar {
  position: sticky;
  top: 92px;
}

.cart-sidebar-card {
  overflow: hidden;
  padding: 0 22px 22px;
  background: #ffffff;
  border: 1px solid var(--line);
  border-radius: 6px;
  box-shadow: 0 5px 18px rgba(11,52,83,.07);
}

.cart-sidebar-card::before {
  content: "TÓM TẮT DANH SÁCH";
  display: block;
  margin: 0 -22px 18px;
  padding: 15px 22px;
  color: #ffffff;
  background: var(--navy);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: .5px;
}

.sidebar-logo {
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.sidebar-logo img {
  max-width: 118px;
  max-height: 50px;
  object-fit: contain;
}

.cart-sidebar-card h3 {
  margin: 0 0 14px;
  color: var(--navy);
  font-size: 18px;
  font-weight: 800;
  text-align: center;
}

.sidebar-row {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 13px 0;
  border-bottom: 1px solid #ece7e1;
  font-size: 13px;
}

.sidebar-row span {
  color: #6f7d83;
}

.sidebar-row strong {
  color: var(--navy);
  text-align: right;
  font-weight: 800;
}

.sidebar-row:last-of-type strong {
  color: var(--red);
  font-size: 17px;
}

.price-note {
  margin: 16px 0;
  padding: 12px;
  border-left: 3px solid var(--gold);
  border-radius: 3px;
  color: #65747a;
  background: #faf7f1;
  font-size: 11px;
  line-height: 1.65;
}

.sidebar-export-button,
.continue-button {
  width: 100%;
  min-height: 43px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 800;
}

.sidebar-export-button {
  margin-bottom: 9px;
  color: #ffffff;
  background: var(--red);
}

.sidebar-export-button:hover {
  background: var(--red-dark);
}

.continue-button {
  color: var(--navy);
  background: #f6f2ec;
  border: 1px solid #e2dbd2;
}

.continue-button:hover {
  color: var(--red);
  background: #ffffff;
  border-color: rgba(166,11,39,.3);
}

.empty-cart {
  padding: 72px 28px;
  background: #ffffff;
  border: 1px solid var(--line);
  border-radius: 7px;
  text-align: center;
  box-shadow: 0 6px 22px rgba(11,52,83,.06);
}

.empty-cart-icon {
  width: 84px;
  height: 84px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  border-radius: 50%;
  color: var(--red);
  background: rgba(166,11,39,.08);
  font-size: 32px;
}

.empty-cart h2 {
  margin: 0 0 10px;
  color: var(--navy);
  font-size: 24px;
  font-weight: 800;
}

.empty-cart p {
  max-width: 520px;
  margin: 0 auto 24px;
  color: #738087;
  line-height: 1.7;
}

.view-products-button {
  min-height: 44px;
  padding: 0 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  border-radius: 5px;
  color: #ffffff;
  background: var(--red);
  font-weight: 800;
}

.view-products-button:hover {
  background: var(--red-dark);
}

@media (max-width: 1050px) {
  .cart-layout {
    grid-template-columns: 1fr;
  }

  .cart-sidebar {
    position: static;
  }

  .cart-sidebar-card {
    max-width: none;
  }
}

@media (max-width: 768px) {
  .cart-container {
    width: min(100% - 28px, 1200px);
  }

  .cart-hero {
    padding: 108px 0 38px;
  }

  .cart-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .cart-actions {
    width: 100%;
  }

  .export-button,
  .clear-button {
    flex: 1;
  }

  .cart-item {
    grid-template-columns: 105px minmax(0, 1fr);
    min-height: auto;
    gap: 13px;
    padding: 13px;
  }

  .cart-item-image {
    width: 105px;
    height: 105px;
  }

  .cart-item-actions {
    grid-column: 1 / -1;
    min-height: auto;
    flex-direction: row;
    padding: 12px 0 0;
    border-top: 1px solid #eee8e2;
    border-left: none;
  }

  .detail-button,
  .remove-button {
    flex: 1;
  }

  .product-name {
    font-size: 16px;
  }
}

@media (max-width: 500px) {
  .cart-content-section {
    padding: 24px 0 56px;
  }

  .cart-hero h1 {
    font-size: 30px;
  }

  .cart-hero p {
    font-size: 13px;
  }

  .cart-actions {
    flex-direction: column;
  }

  .export-button,
  .clear-button {
    width: 100%;
  }

  .cart-item {
    grid-template-columns: 88px minmax(0, 1fr);
  }

  .cart-item-image {
    width: 88px;
    height: 88px;
  }

  .product-information {
    gap: 6px 10px;
  }

  .product-price-row strong {
    font-size: 17px;
  }

  .remove-button::after {
    content: " Xóa";
  }
}
</style>
