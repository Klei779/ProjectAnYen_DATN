<template>
  <div class="popup-overlay">
    <div class="create-order-modal fixed-order-modal">
      <!-- HEADER -->
      <div class="modal-header">
        <div>
          <h2>Tạo đơn hàng mới</h2>
          <p>Nhập thông tin khách hàng, thông tin đơn và chọn sản phẩm</p>
        </div>

        <button class="icon-close" @click="$emit('close')">×</button>
      </div>

      <!-- BODY -->
      <div class="create-order-3col">
        <!-- CỘT 1: THÔNG TIN KHÁCH HÀNG -->
        <div class="card-box small-card">
          <div class="card-title title-blue">
            Thông tin khách hàng
          </div>

          <div class="form-group autocomplete-wrap">
            <label>Tên / SĐT khách hàng <span>*</span></label>
            <input
                v-model="customerKeyword"
                type="text"
                placeholder="Nhập tên hoặc số điện thoại khách hàng"
                @input="handleCustomerKeywordInput"
            />

            <div
                v-if="showCustomerSuggestions && customerSuggestions.length > 0"
                class="suggestion-box"
            >
              <div
                  v-for="kh in customerSuggestions"
                  :key="kh.maKhachHang"
                  class="suggestion-item"
                  @click="selectCustomer(kh)"
              >
                <strong>{{ kh.tenKhachHang }}</strong>
                <span> — {{ kh.soDienThoai }}</span>
                <small> — {{ kh.diaChi }}</small>
              </div>
            </div>
          </div>

          <div class="form-row-2">
            <div class="form-group">
              <label>Tên khách hàng <span>*</span></label>
              <input v-model="form.tenKhachHang" type="text" />
            </div>

            <div class="form-group">
              <label>Số điện thoại <span>*</span></label>
              <input v-model="form.soDienThoai" type="text" />
            </div>
          </div>

          <div class="form-row-2">
            <div class="form-group">
              <label>CCCD</label>
              <input v-model="form.cccd" type="text" />
            </div>

            <div class="form-group">
              <label>Email</label>
              <input v-model="form.email" type="text" />
            </div>
          </div>

          <div class="form-group">
            <label>Địa chỉ</label>
            <input v-model="form.diaChi" type="text" />
          </div>
        </div>

        <!-- CỘT 2: THÔNG TIN ĐƠN HÀNG -->
        <div class="card-box small-card">
          <div class="card-title title-blue">
            Thông tin đơn hàng
          </div>

          <div class="form-row-2">
            <div class="form-group">
              <label>Nhân viên phụ trách</label>
              <input v-model="form.nhanVienPhuTrach" type="text" readonly />
            </div>

            <div class="form-group">
              <label>Ngày tạo đơn</label>
              <input v-model="form.ngayTaoDon" type="date" />
            </div>
          </div>

          <div class="form-group">
            <label>Đối tác trong đơn</label>
            <input :value="partnerSummary" type="text" readonly />
          </div>

          <div class="form-group">
            <label>Ghi chú tư vấn</label>
            <textarea
                v-model="form.ghiChu"
                rows="3"
                placeholder="Nhập ghi chú tư vấn của nhân viên..."
            ></textarea>
          </div>
        </div>

        <!-- CỘT 3: GIỎ HÀNG -->
        <div class="cart-panel card-box">
          <div class="cart-header">
            <div>
              <div class="card-title title-blue cart-title">
                Giỏ hàng
              </div>
              <p>
                {{ form.items.length }} sản phẩm •
                {{ selectedPartnerNames.length }} đối tác
              </p>
            </div>

            <button
                class="btn-primary-outline btn-add-product-top"
                @click="openProductModal"
            >
              + Thêm sản phẩm
            </button>
          </div>

          <div v-if="form.items.length === 0" class="empty-box cart-empty">
            Chưa có sản phẩm nào trong đơn.
          </div>

          <div v-else class="cart-list">
            <div
                v-for="item in form.items"
                :key="item.maSanPham"
                class="cart-item"
            >
              <div class="cart-product-main">
                <div class="thumb cart-thumb">
                  <img :src="item.hinhAnh" alt="" />
                </div>

                <div class="cart-product-info">
                  <div class="product-name">
                    {{ item.tenSanPham }}
                  </div>

                  <div class="product-sub">
                    {{ item.tenDoiTac || getPartnerName(item.maDoiTac) }}
                    •
                    {{ item.loai }}
                  </div>

                  <div class="cart-price">
                    {{ formatMoney(item.giaTien) }}
                  </div>
                </div>
              </div>

              <div class="cart-item-bottom">
                <div class="qty-box">
                  <button @click="decreaseQty(item)">−</button>
                  <span>{{ item.soLuong }}</span>
                  <button @click="increaseQty(item)">+</button>
                </div>

                <div class="money-red">
                  {{ formatMoney(item.giaTien * item.soLuong) }}
                </div>

                <button
                    class="btn-delete"
                    @click="removeItem(item.maSanPham)"
                >
                  🗑
                </button>
              </div>
            </div>
          </div>

          <div class="summary-row cart-summary">
            <div class="summary-item">
              <span>Tạm tính</span>
              <strong>{{ formatMoney(subtotal) }}</strong>
            </div>

            <div class="summary-divider"></div>

            <div class="summary-item total">
              <span>Tổng thanh toán</span>
              <strong>{{ formatMoney(totalMoney) }}</strong>
            </div>
          </div>
        </div>
      </div>

      <!-- FOOTER -->
      <div class="modal-footer">
        <button class="btn-cancel" @click="$emit('close')">Hủy</button>

        <div class="footer-right">
          <button class="btn-primary" @click="submitOrder">
            Lưu và gửi đối tác
          </button>
        </div>
      </div>
    </div>

    <!-- POPUP CHỌN SẢN PHẨM FULL -->
    <div v-if="showProductModal" class="product-popup-overlay">
      <div class="product-popup product-popup-new">
        <div class="product-popup-header">
          <div>
            <h3>Chọn sản phẩm</h3>
            <p>Chọn sản phẩm từ nhiều đối tác khác nhau</p>
          </div>

          <button class="icon-close" @click="closeProductModal">×</button>
        </div>

        <div class="product-popup-toolbar product-toolbar-new">
          <select v-model="selectedProductPartnerId">
            <option
                v-for="dt in partners"
                :key="dt.maDoiTac"
                :value="dt.maDoiTac"
            >
              {{ dt.tenDoiTac }}
            </option>
          </select>

          <input
              v-model="productKeyword"
              type="text"
              placeholder="Tìm tên sản phẩm..."
          />
        </div>

        <div class="product-popup-content">
          <!-- LEFT: DANH SÁCH SẢN PHẨM -->
          <div class="product-list-panel">
            <div class="panel-title">Danh sách sản phẩm</div>

            <div v-if="filteredPartnerProducts.length === 0" class="empty-box">
              Không có sản phẩm phù hợp.
            </div>

            <div v-else class="product-grid">
              <div
                  v-for="sp in filteredPartnerProducts"
                  :key="sp.maSanPham"
                  class="product-card product-card-new"
              >
                <div class="product-card-left">
                  <div class="product-image square-product-image">
                    <img :src="sp.hinhAnh" alt=""/>
                  </div>

                  <div class="product-card-info">
                    <div class="product-card-name">{{ sp.tenSanPham }}</div>
                    <div class="product-card-sub">
                      {{ sp.loai }} • {{ getPartnerName(sp.maDoiTac) }}
                    </div>
                    <div class="product-card-price">
                      {{ formatMoney(sp.giaTien) }}
                    </div>
                    <div class="product-card-stock">Tồn: {{ sp.tonKho }}</div>
                  </div>
                </div>

                <div class="product-card-action">
                  <template v-if="getTempQty(sp.maSanPham) === 0">
                    <button class="btn-add-cart" @click="addTempProduct(sp)">
                      🛒 Thêm
                    </button>
                  </template>

                  <template v-else>
                    <div class="cart-qty-pill">
                      <span class="cart-icon">🛒</span>
                      <button @click="decreaseTempQty(sp.maSanPham)">−</button>
                      <strong>{{ getTempQty(sp.maSanPham) }}</strong>
                      <button @click="increaseTempQty(sp)">+</button>
                    </div>
                  </template>
                </div>
              </div>
            </div>
          </div>

          <!-- RIGHT: NOTE SẢN PHẨM ĐÃ CHỌN -->
          <div class="selected-panel">
            <div class="panel-title">Đã thêm vào giỏ</div>

            <div v-if="tempItems.length === 0" class="empty-box">
              Chưa chọn sản phẩm nào.
            </div>

            <div v-else class="selected-note-list">
              <div
                  v-for="item in tempItems"
                  :key="item.maSanPham"
                  class="selected-note"
              >
                <div class="note-cart-icon">🛒</div>

                <div class="note-content">
                  <div class="selected-name">{{ item.tenSanPham }}</div>
                  <div class="selected-sub">
                    {{ item.tenDoiTac }} • {{ formatMoney(item.giaTien) }}
                  </div>
                </div>

                <div class="note-right">
                  <span>x{{ item.soLuong }}</span>
                  <button
                      class="btn-delete small"
                      @click="removeTempItem(item.maSanPham)"
                  >
                    ✕
                  </button>
                </div>
              </div>
            </div>

            <div class="selected-total">
              <span>Tạm tính</span>
              <strong>{{ formatMoney(tempSubtotal) }}</strong>
            </div>
          </div>
        </div>

        <div class="product-popup-footer">
          <button class="btn-cancel" @click="closeProductModal">Hủy</button>

          <div class="footer-right">
            <button class="btn-secondary" @click="clearTempItems">
              Xóa hết
            </button>
            <button class="btn-primary" @click="saveProductsToOrder">
              Lưu sản phẩm
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from "vue";

const showCustomerSuggestions = ref(false);

const emit = defineEmits(["close", "submit", "save-draft"]);

const partners = ref([
  { maDoiTac: 1, tenDoiTac: "Công ty Thiên Phúc" },
  { maDoiTac: 2, tenDoiTac: "Cơ sở An Lạc" },
  { maDoiTac: 3, tenDoiTac: "Hoa viên Vĩnh Hằng" },
]);

const allCustomers = ref([
  {
    maKhachHang: 1,
    tenKhachHang: "Nguyễn Văn An",
    soDienThoai: "0901234567",
    cccd: "079203001234",
    email: "an.nguyen@gmail.com",
    diaChi: "Quận 1, TP.HCM",
  },
  {
    maKhachHang: 2,
    tenKhachHang: "Nguyễn Văn Bình",
    soDienThoai: "0908888888",
    cccd: "079203009999",
    email: "binh.nguyen@gmail.com",
    diaChi: "Bình Dương",
  },
  {
    maKhachHang: 3,
    tenKhachHang: "Nguyễn Văn Cường",
    soDienThoai: "0911222333",
    cccd: "079205001111",
    email: "cuong.nguyen@gmail.com",
    diaChi: "Đồng Nai",
  },
]);

const allProducts = ref([
  {
    maSanPham: 1,
    tenSanPham: "Quan tài gỗ thông tiêu chuẩn",
    loai: "Quan tài",
    giaTien: 8500000,
    tonKho: 10,
    maDoiTac: 1,
    hinhAnh: "https://cdn-icons-png.flaticon.com/512/3659/3659898.png",
  },
  {
    maSanPham: 3,
    tenSanPham: "Bình tro cốt sứ trắng",
    loai: "Bình tro cốt",
    giaTien: 2500000,
    tonKho: 20,
    maDoiTac: 1,
    hinhAnh: "https://cdn-icons-png.flaticon.com/512/3534/3534012.png",
  },
  {
    maSanPham: 6,
    tenSanPham: "Bàn thờ tang lễ",
    loai: "Vật phẩm tang lễ",
    giaTien: 3200000,
    tonKho: 12,
    maDoiTac: 1,
    hinhAnh: "https://cdn-icons-png.flaticon.com/512/1046/1046874.png",
  },
  {
    maSanPham: 2,
    tenSanPham: "Quan tài gỗ căm xe cao cấp",
    loai: "Quan tài",
    giaTien: 18000000,
    tonKho: 5,
    maDoiTac: 2,
    hinhAnh: "https://cdn-icons-png.flaticon.com/512/3659/3659898.png",
  },
  {
    maSanPham: 4,
    tenSanPham: "Vòng hoa chia buồn",
    loai: "Hoa tang lễ",
    giaTien: 1500000,
    tonKho: 30,
    maDoiTac: 3,
    hinhAnh: "https://cdn-icons-png.flaticon.com/512/3468/3468379.png",
  },
]);

const today = new Date().toISOString().split("T")[0];

const customerKeyword = ref("");
const showProductModal = ref(false);
const productKeyword = ref("");
const selectedProductPartnerId = ref(1);

const form = ref({
  maKhachHang: null,
  tenKhachHang: "",
  soDienThoai: "",
  cccd: "",
  email: "",
  diaChi: "",
  nhanVienPhuTrach: "Võ Thị Mai",
  ngayTaoDon: today,
  nguonTaoDon: "Khách đã trao đổi trước",
  ghiChu: "",
  items: [],
});

const tempItems = ref([]);

const customerSuggestions = computed(() => {
  const keyword = customerKeyword.value.trim().toLowerCase();
  if (!keyword) return [];

  return allCustomers.value
      .filter(
          (kh) =>
              kh.tenKhachHang.toLowerCase().includes(keyword) ||
              kh.soDienThoai.includes(keyword)
      )
      .slice(0, 5);
});

const selectedPartnerName = computed(() => {
  return getPartnerName(selectedProductPartnerId.value);
});

const filteredPartnerProducts = computed(() => {
  const keyword = productKeyword.value.trim().toLowerCase();

  return allProducts.value.filter((sp) => {
    const matchPartner = sp.maDoiTac === Number(selectedProductPartnerId.value);
    const matchKeyword =
        !keyword || sp.tenSanPham.toLowerCase().includes(keyword);

    return matchPartner && matchKeyword;
  });
});

const selectedPartnerNames = computed(() => {
  const names = form.value.items.map((item) => {
    return item.tenDoiTac || getPartnerName(item.maDoiTac);
  });

  return [...new Set(names)];
});

const partnerSummary = computed(() => {
  if (selectedPartnerNames.value.length === 0) {
    return "Chưa chọn sản phẩm";
  }

  return selectedPartnerNames.value.join(", ");
});

const subtotal = computed(() => {
  return form.value.items.reduce(
      (sum, item) => sum + item.giaTien * item.soLuong,
      0
  );
});

const totalMoney = computed(() => subtotal.value);

const tempSubtotal = computed(() => {
  return tempItems.value.reduce(
      (sum, item) => sum + item.giaTien * item.soLuong,
      0
  );
});

function getPartnerName(maDoiTac) {
  const found = partners.value.find(
      (p) => p.maDoiTac === Number(maDoiTac)
  );

  return found ? found.tenDoiTac : "Không rõ đối tác";
}

function formatMoney(value) {
  return new Intl.NumberFormat("vi-VN").format(value || 0) + " ₫";
}

function handleCustomerKeywordInput() {
  form.value.maKhachHang = null;
  form.value.tenKhachHang = customerKeyword.value;

  showCustomerSuggestions.value = customerKeyword.value.trim().length > 0;
}

function selectCustomer(kh) {
  form.value.maKhachHang = kh.maKhachHang;
  form.value.tenKhachHang = kh.tenKhachHang;
  form.value.soDienThoai = kh.soDienThoai;
  form.value.cccd = kh.cccd;
  form.value.email = kh.email;
  form.value.diaChi = kh.diaChi;

  customerKeyword.value = kh.tenKhachHang;
  showCustomerSuggestions.value = false;
}

function cloneItems(items) {
  return JSON.parse(JSON.stringify(items));
}

function openProductModal() {
  tempItems.value = cloneItems(form.value.items);

  if (tempItems.value.length > 0) {
    selectedProductPartnerId.value = tempItems.value[0].maDoiTac;
  } else {
    selectedProductPartnerId.value = partners.value[0]?.maDoiTac || "";
  }

  productKeyword.value = "";
  showProductModal.value = true;
}

function closeProductModal() {
  showProductModal.value = false;
}

function getTempItem(maSanPham) {
  return tempItems.value.find((item) => item.maSanPham === maSanPham);
}

function getTempQty(maSanPham) {
  const item = getTempItem(maSanPham);
  return item ? item.soLuong : 0;
}

function addTempProduct(sp) {
  const existing = getTempItem(sp.maSanPham);

  if (existing) {
    existing.soLuong += 1;
    return;
  }

  tempItems.value.push({
    maSanPham: sp.maSanPham,
    tenSanPham: sp.tenSanPham,
    loai: sp.loai,
    giaTien: sp.giaTien,
    tonKho: sp.tonKho,
    maDoiTac: sp.maDoiTac,
    tenDoiTac: getPartnerName(sp.maDoiTac),
    hinhAnh: sp.hinhAnh,
    soLuong: 1,
  });
}

function increaseTempQty(sp) {
  const existing = getTempItem(sp.maSanPham);

  if (!existing) {
    addTempProduct(sp);
    return;
  }

  existing.soLuong += 1;
}

function decreaseTempQty(maSanPham) {
  const existing = getTempItem(maSanPham);
  if (!existing) return;

  if (existing.soLuong <= 1) {
    removeTempItem(maSanPham);
    return;
  }

  existing.soLuong -= 1;
}

function removeTempItem(maSanPham) {
  tempItems.value = tempItems.value.filter(
      (item) => item.maSanPham !== maSanPham
  );
}

function clearTempItems() {
  tempItems.value = [];
}

function saveProductsToOrder() {
  form.value.items = cloneItems(tempItems.value);
  showProductModal.value = false;
}

function increaseQty(item) {
  item.soLuong += 1;
}

function decreaseQty(item) {
  if (item.soLuong > 1) {
    item.soLuong -= 1;
    return;
  }

  removeItem(item.maSanPham);
}

function removeItem(maSanPham) {
  form.value.items = form.value.items.filter(
      (item) => item.maSanPham !== maSanPham
  );
}

function submitOrder() {
  if (!form.value.tenKhachHang.trim()) {
    alert("Vui lòng nhập tên khách hàng");
    return;
  }

  if (!form.value.soDienThoai.trim()) {
    alert("Vui lòng nhập số điện thoại");
    return;
  }

  if (form.value.items.length === 0) {
    alert("Vui lòng chọn ít nhất 1 sản phẩm");
    return;
  }

  const payload = {
    ...form.value,
    doiTacs: selectedPartnerNames.value,
    tongTien: totalMoney.value,
  };

  emit("submit", payload);
}
</script>

<style scoped src="../../assets/styles/PopTaoDonHang.css"></style>