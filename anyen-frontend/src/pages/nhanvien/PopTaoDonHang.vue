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

          <div class="form-row-2">
            <div class="form-group autocomplete-wrap">
              <label>Tên khách hàng <span>*</span></label>
              <input
                  v-model="form.tenKhachHang"
                  type="text"
                  maxlength="50"
                  placeholder="Nhập tên khách hàng"
                  autocomplete="off"
                  @input="handleCustomerNameInput"
                  @focus="handleCustomerNameFocus"
              />

              <div
                  v-if="showCustomerSuggestions && customerSuggestions.length > 0"
                  class="suggestion-box"
              >
                <div
                    v-for="kh in customerSuggestions"
                    :key="kh.maKhachHang"
                    class="suggestion-item"
                    @mousedown.prevent="selectCustomer(kh)"
                >
                  <strong>{{ kh.tenKhachHang }}</strong>
                  <span v-if="kh.soDienThoai"> — {{ kh.soDienThoai }}</span>
                  <small v-if="getCustomerAddressSummary(kh)"> — {{ getCustomerAddressSummary(kh) }}</small>
                </div>
              </div>
            </div>

            <div class="form-group">
              <label>Số điện thoại <span>*</span></label>
              <input
                  v-model="form.soDienThoai"
                  type="text"
                  maxlength="10"
                  inputmode="tel"
                  placeholder="VD: 0901234567"
                  @input="limitOrderPhoneInput"
              />
            </div>
          </div>

          <div class="form-row-2">
            <div class="form-group">
              <label>CCCD</label>
              <input
                  v-model="form.cccd"
                  type="text"
                  maxlength="12"
                  inputmode="numeric"
                  placeholder="Nhập CCCD 12 số"
                  @input="limitOrderCCCDInput"
              />
            </div>

            <div class="form-group">
              <label>Email</label>
              <input
                  v-model="form.email"
                  type="email"
                  maxlength="100"
                  placeholder="Nhập email"
              />
            </div>
          </div>

          <!-- SỐ NHÀ, TÊN ĐƯỜNG -->
          <div class="form-group">
            <label>Số nhà, tên đường</label>
            <input
                v-model="form.soNhaDuong"
                type="text"
                maxlength="255"
                placeholder="Ví dụ: 123 Nguyễn Văn Linh"
            />
          </div>

          <!-- PHƯỜNG/XÃ & QUẬN/HUYỆN -->
          <div class="form-row-2">
            <div class="form-group">
              <label>Phường/Xã</label>
              <input
                  v-model="form.phuongXa"
                  type="text"
                  maxlength="100"
                  placeholder="Ví dụ: Phường Tân Phong"
              />
            </div>

            <div class="form-group">
              <label>Quận/Huyện</label>
              <input
                  v-model="form.quanHuyen"
                  type="text"
                  maxlength="100"
                  placeholder="Ví dụ: Quận 7"
              />
            </div>
          </div>

          <!-- TỈNH/THÀNH PHỐ -->
          <div class="form-group">
            <label>Tỉnh/Thành phố</label>
            <input
                v-model="form.tinhThanh"
                type="text"
                maxlength="100"
                placeholder="Ví dụ: Thành phố Hồ Chí Minh"
            />
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
              <input v-model="form.ngayTaoDon" type="date" readonly disabled />
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

            <div class="cart-header-actions">
              <button
                  type="button"
                  class="btn-primary-outline btn-add-product-top"
                  @click="openProductModal('PRODUCT')"
              >
                + Thêm
              </button>

            </div>
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

    <!-- POPUP CHỌN SẢN PHẨM & COMBO -->
    <div v-if="showProductModal" class="product-popup-overlay">
      <div class="product-popup product-popup-new">
        <div class="product-popup-header">
          <div>
            <h3>Chọn sản phẩm / Combo</h3>
            <p>Chọn sản phẩm lẻ từ các đối tác hoặc chọn gói combo ưu đãi</p>
          </div>

          <button class="icon-close" @click="closeProductModal">×</button>
        </div>

        <!-- TABS CHỌN SẢN PHẨM HOẶC COMBO -->
        <div class="modal-tab-nav">
          <button
              type="button"
              class="modal-tab-item"
              :class="{ active: activeModalTab === 'PRODUCT' }"
              @click="activeModalTab = 'PRODUCT'"
          >
            <i class="fa-solid fa-box-open"></i>
            <span>Sản phẩm lẻ ({{ filteredPartnerProducts.length }})</span>
          </button>

          <button
              type="button"
              class="modal-tab-item"
              :class="{ active: activeModalTab === 'COMBO' }"
              @click="activeModalTab = 'COMBO'"
          >
            <i class="fa-solid fa-gift"></i>
            <span>Combo ưu đãi ({{ filteredCombos.length }})</span>
          </button>
        </div>

        <!-- TOOLBAR SẢN PHẨM LẺ -->
        <div v-if="activeModalTab === 'PRODUCT'" class="product-popup-toolbar product-toolbar-new">
          <select v-model="selectedProductPartnerId">
            <option value="ALL">Tất cả đối tác</option>

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
              placeholder="Tìm theo tên sản phẩm, loại, đối tác..."
          />
        </div>

        <!-- TOOLBAR COMBO -->
        <div v-else class="product-popup-toolbar combo-toolbar-new">
          <input
              v-model="comboKeyword"
              type="text"
              placeholder="Tìm theo tên combo, mô tả, sản phẩm trong combo..."
          />
        </div>

        <div class="product-popup-content">
          <!-- TAB 1: SẢN PHẨM LẺ -->
          <div v-if="activeModalTab === 'PRODUCT'" class="product-list-panel">
            <div class="panel-title">Danh sách sản phẩm lẻ</div>

            <div v-if="productLoading" class="empty-box">
              <i class="fa-solid fa-spinner fa-spin"></i> Đang tải sản phẩm...
            </div>

            <div v-else-if="productError" class="empty-box">
              {{ productError }}
            </div>

            <div v-else-if="filteredPartnerProducts.length === 0" class="empty-box">
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
                    <img :src="sp.hinhAnh" alt="" />
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

          <!-- TAB 2: COMBO SẢN PHẨM -->
          <div v-else class="combo-list-panel">
            <div class="panel-title">Danh sách combo ưu đãi</div>

            <div v-if="comboLoading" class="empty-box">
              <i class="fa-solid fa-spinner fa-spin"></i> Đang tải combo...
            </div>

            <div v-else-if="comboError" class="empty-box">
              {{ comboError }}
            </div>

            <div v-else-if="filteredCombos.length === 0" class="empty-box">
              Không có combo phù hợp.
            </div>

            <div v-else class="combo-grid">
              <div
                  v-for="combo in filteredCombos"
                  :key="combo.comboId"
                  class="combo-card"
              >
                <div class="combo-card-header">
                  <div class="combo-img-wrap">
                    <img :src="normalizeImage(combo.hinhAnh || (combo.hinhAnhDaiDiens && combo.hinhAnhDaiDiens[0]))" alt="" />
                    <span class="combo-badge">🎁 Combo #{{ combo.comboId }}</span>
                  </div>

                  <div class="combo-info">
                    <h4 class="combo-name">{{ combo.tenCombo }}</h4>
                    <p class="combo-desc" :title="combo.moTa">{{ combo.moTa || 'Gói combo sản phẩm ưu đãi' }}</p>

                    <div class="combo-pricing">
                      <span class="combo-price">{{ formatMoney(combo.gia) }}</span>
                      <span v-if="combo.tongGiaSanPham && Number(combo.tongGiaSanPham) > Number(combo.gia)" class="combo-original-price">
                        {{ formatMoney(combo.tongGiaSanPham) }}
                      </span>
                      <span v-if="combo.tongGiaSanPham && Number(combo.tongGiaSanPham) > Number(combo.gia)" class="combo-save-badge">
                        Tiết kiệm {{ formatMoney(Number(combo.tongGiaSanPham) - Number(combo.gia)) }}
                      </span>
                    </div>
                  </div>
                </div>

                <!-- DANH SÁCH SẢN PHẨM TRONG COMBO -->
                <div class="combo-items-section">
                  <div class="combo-items-title">
                    Gồm {{ combo.sanPhams?.length || 0 }} sản phẩm trong gói:
                  </div>

                  <div class="combo-items-list">
                    <div
                        v-for="p in (combo.sanPhams || [])"
                        :key="p.maSanPham"
                        class="combo-item-row"
                    >
                      <img :src="buildImageUrl(p.hinhAnh)" class="combo-item-thumb" alt="" />
                      <div class="combo-item-meta">
                        <div class="combo-item-name">{{ p.tenSanPham }}</div>
                        <div class="combo-item-sub">
                          SL: <strong>x{{ p.soLuongTrongCombo || 1 }}</strong> • {{ p.tenDoiTac || getPartnerName(p.maDoiTac) }} • {{ p.loai }}
                        </div>
                      </div>
                      <div class="combo-item-price">
                        {{ formatMoney(p.giaTien) }}
                      </div>
                    </div>
                  </div>
                </div>

                <div class="combo-card-footer">
                  <button
                      type="button"
                      class="btn-apply-combo"
                      @click="addComboToOrder(combo)"
                  >
                    <i class="fa-solid fa-cart-plus"></i> Thêm combo này vào đơn
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- SIDEBAR: ĐÃ THÊM VÀO GIỎ -->
          <div class="selected-panel">
            <div class="panel-title">
              <span>Đã chọn ({{ tempItems.length }} SP)</span>
              <button v-if="tempItems.length > 0" class="btn-clear-inline" @click="clearTempItems">Xóa hết</button>
            </div>

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
                  <div class="note-qty-controls">
                    <button class="mini-qty-btn" @click="decreaseTempQty(item.maSanPham)">−</button>
                    <span>{{ item.soLuong }}</span>
                    <button class="mini-qty-btn" @click="increaseTempQty(item)">+</button>
                  </div>
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
              Lưu sản phẩm vào đơn
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from "vue";
import {
  getKhachHangTaoDonHang,
  getSanPhamTaoDonHang
} from "../../services/donHangService.js";
import {
  getCombosAdmin
} from "../../services/comboAdminService.js";

const userStr = localStorage.getItem("user");
const userDangNhap = userStr ? JSON.parse(userStr) : null;

const tenNhanVienDangNhap =
    userDangNhap?.hoTen ||
    userDangNhap?.tenNhanVien ||
    userDangNhap?.tenDangNhap ||
    "";

const emit = defineEmits(["close", "submit", "save-draft"]);

const partners = ref([]);
const allProducts = ref([]);
const allCustomers = ref([]);
const combos = ref([]);

const productLoading = ref(false);
const productError = ref("");
const comboLoading = ref(false);
const comboError = ref("");

const showCustomerSuggestions = ref(false);

const showProductModal = ref(false);
const activeModalTab = ref("PRODUCT"); // 'PRODUCT' | 'COMBO'
const productKeyword = ref("");
const comboKeyword = ref("");
const selectedProductPartnerId = ref("ALL");

const tempItems = ref([]);

function getTodayLocalDate() {
  const d = new Date();

  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

const today = getTodayLocalDate();

const form = ref({
  maKhachHang: null,
  tenKhachHang: "",
  soDienThoai: "",
  cccd: "",
  email: "",
  soNhaDuong: "",
  phuongXa: "",
  quanHuyen: "",
  tinhThanh: "",
  diaChi: "",
  nhanVienPhuTrach: tenNhanVienDangNhap,
  ngayTaoDon: today,
  nguonTaoDon: "Khách đã trao đổi trước",
  ghiChu: "",
  items: [],
});

function onlyDigits(value = "") {
  return String(value).replace(/\D/g, "");
}

function normalizeVietnamPhone(value = "") {
  let phone = String(value).trim().replace(/\s+/g, "");

  if (phone.startsWith("+84")) {
    phone = "0" + phone.slice(3);
  }

  phone = phone.replace(/\D/g, "");

  if (phone.startsWith("84") && phone.length === 11) {
    phone = "0" + phone.slice(2);
  }

  return phone;
}

function isValidCCCD(value = "") {
  return /^[0-9]{12}$/.test(String(value).trim());
}

function isValidVietnamPhone(value = "") {
  const phone = normalizeVietnamPhone(value);

  return /^0(3|5|7|8|9)[0-9]{8}$/.test(phone);
}

function limitOrderCCCDInput() {
  form.value.cccd = onlyDigits(form.value.cccd).slice(0, 12);
}

function limitOrderPhoneInput() {
  form.value.soDienThoai = normalizeVietnamPhone(form.value.soDienThoai).slice(0, 10);
}

function buildImageUrl(path) {
  if (!path) {
    return "https://cdn-icons-png.flaticon.com/512/679/679720.png";
  }

  if (path.startsWith("http") || path.startsWith("data:")) {
    return path;
  }

  if (path.startsWith("/uploads/")) {
    return `http://localhost:8080${path}`;
  }

  if (path.startsWith("/images/")) {
    return path;
  }

  if (path.startsWith("/")) {
    return path;
  }

  return `http://localhost:8080/uploads/${path}`;
}

function normalizeImage(url) {
  if (!url) return "https://cdn-icons-png.flaticon.com/512/679/679720.png";
  if (/^(https?:|data:|blob:)/i.test(url)) return url;
  if (url.startsWith("//")) return `https:${url}`;
  if (url.startsWith("/")) return `http://localhost:8080${url}`;
  return `http://localhost:8080/${url}`;
}

function normalizeProduct(sp) {
  return {
    maSanPham: sp.maSanPham,
    tenSanPham: sp.tenSanPham,
    loai: sp.loai || "Chưa phân loại",
    giaTien: Number(sp.giaTien || 0),
    tonKho: Number(sp.tonKho || sp.soLuong || 0),
    maDoiTac: sp.maDoiTac,
    tenDoiTac: sp.tenDoiTac || "Không rõ đối tác",
    hinhAnh: buildImageUrl(sp.hinhAnh),
    trangThai: sp.trangThai,
  };
}

function normalizeCustomer(kh) {
  return {
    maKhachHang: kh.maKhachHang ?? kh.MaKhachHang ?? null,
    tenKhachHang: kh.tenKhachHang ?? kh.TenKhachHang ?? "",
    soDienThoai: normalizeVietnamPhone(kh.soDienThoai ?? kh.SoDienThoai ?? "").slice(0, 10),
    cccd: onlyDigits(kh.cccd ?? kh.CCCD ?? "").slice(0, 12),
    email: kh.email ?? kh.Email ?? "",
    diaChi: kh.diaChi ?? kh.DiaChi ?? "",
    soNhaDuong: kh.soNhaDuong ?? kh.SoNhaDuong ?? "",
    phuongXa: kh.phuongXa ?? kh.PhuongXa ?? "",
    quanHuyen: kh.quanHuyen ?? kh.QuanHuyen ?? "",
    tinhThanh: kh.tinhThanh ?? kh.TinhThanh ?? "",
  };
}

function getCustomerAddressSummary(kh) {
  const parts = [kh.soNhaDuong, kh.phuongXa, kh.quanHuyen, kh.tinhThanh]
    .map(s => String(s || "").trim())
    .filter(Boolean);
  if (parts.length > 0) {
    return parts.join(", ");
  }
  return kh.diaChi || "";
}

function formatFullAddress(soNhaDuong, phuongXa, quanHuyen, tinhThanh, fallbackDiaChi = "") {
  const parts = [soNhaDuong, phuongXa, quanHuyen, tinhThanh]
    .map(s => String(s || "").trim())
    .filter(Boolean);
  if (parts.length > 0) {
    return parts.join(", ");
  }
  return String(fallbackDiaChi || "").trim();
}

function rebuildPartners() {
  const map = new Map();

  allProducts.value.forEach((sp) => {
    if (!sp.maDoiTac) return;

    map.set(Number(sp.maDoiTac), {
      maDoiTac: Number(sp.maDoiTac),
      tenDoiTac: sp.tenDoiTac || "Không rõ đối tác",
    });
  });

  partners.value = Array.from(map.values());
}

async function loadProductsFromDatabase() {
  productLoading.value = true;
  productError.value = "";

  try {
    const data = await getSanPhamTaoDonHang();

    allProducts.value = (data || []).map(normalizeProduct);

    rebuildPartners();
  } catch (error) {
    console.error("Lỗi tải danh sách sản phẩm:", error);

    productError.value = "Không tải được danh sách sản phẩm.";
    allProducts.value = [];
    partners.value = [];
  } finally {
    productLoading.value = false;
  }
}

function removeVietnameseTones(str = "") {
  return String(str || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .replace(/Đ/g, "D")
    .toLowerCase()
    .trim();
}

async function loadCustomersFromDatabase() {
  try {
    const data = await getKhachHangTaoDonHang();
    const list = Array.isArray(data) ? data : (data?.items || data?.content || []);
    allCustomers.value = list.map(normalizeCustomer);
    console.log("Đã tải danh sách khách hàng:", allCustomers.value.length, allCustomers.value);
  } catch (error) {
    console.error("Lỗi tải danh sách khách hàng:", error);
    allCustomers.value = [];
  }
}

async function loadCombosFromDatabase() {
  comboLoading.value = true;
  comboError.value = "";
  try {
    const res = await getCombosAdmin();
    const list = Array.isArray(res) ? res : (res?.data || res?.items || []);
    combos.value = list;
    console.log("Đã tải danh sách combo:", combos.value.length);
  } catch (error) {
    console.error("Lỗi tải danh sách combo:", error);
    comboError.value = "Không thể tải danh sách combo.";
    combos.value = [];
  } finally {
    comboLoading.value = false;
  }
}

function handleWindowClick(event) {
  const target = event.target;
  if (!target || !target.closest || !target.closest(".autocomplete-wrap")) {
    showCustomerSuggestions.value = false;
  }
}

onMounted(() => {
  loadProductsFromDatabase();
  loadCustomersFromDatabase();
  loadCombosFromDatabase();
  window.addEventListener("click", handleWindowClick);
});

onUnmounted(() => {
  window.removeEventListener("click", handleWindowClick);
});

const customerSuggestions = computed(() => {
  const rawKeyword = String(form.value.tenKhachHang || "").trim();
  if (!rawKeyword) return [];

  const keywordNormalized = removeVietnameseTones(rawKeyword);
  const keywordLower = rawKeyword.toLowerCase();

  return allCustomers.value
      .filter((kh) => {
        const rawTen = String(kh.tenKhachHang || "");
        const rawSdt = String(kh.soDienThoai || "");
        const rawCccd = String(kh.cccd || "");

        const tenNormalized = removeVietnameseTones(rawTen);
        const sdtNormalized = rawSdt.toLowerCase();
        const cccdNormalized = rawCccd.toLowerCase();

        return (
          rawTen.toLowerCase().includes(keywordLower) ||
          tenNormalized.includes(keywordNormalized) ||
          sdtNormalized.includes(keywordLower) ||
          sdtNormalized.includes(keywordNormalized) ||
          cccdNormalized.includes(keywordLower)
        );
      })
      .slice(0, 8);
});

const filteredPartnerProducts = computed(() => {
  const keyword = productKeyword.value.trim().toLowerCase();

  return allProducts.value.filter((sp) => {
    const matchPartner =
        selectedProductPartnerId.value === "ALL" ||
        sp.maDoiTac === Number(selectedProductPartnerId.value);

    const matchKeyword =
        !keyword ||
        String(sp.tenSanPham || "").toLowerCase().includes(keyword) ||
        String(sp.loai || "").toLowerCase().includes(keyword) ||
        String(sp.tenDoiTac || "").toLowerCase().includes(keyword);

    return matchPartner && matchKeyword;
  });
});

const activeCombos = computed(() => {
  return combos.value.filter((c) => Number(c.trangThai) === 1);
});

const filteredCombos = computed(() => {
  const kw = comboKeyword.value.trim().toLowerCase();

  return activeCombos.value.filter((c) => {
    if (!kw) return true;
    const name = String(c.tenCombo || "").toLowerCase();
    const desc = String(c.moTa || "").toLowerCase();
    const id = String(c.comboId || "");
    const prods = (c.sanPhams || [])
        .map((p) => String(p.tenSanPham || "").toLowerCase())
        .join(" ");

    return (
        name.includes(kw) ||
        desc.includes(kw) ||
        id.includes(kw) ||
        prods.includes(kw)
    );
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
  if (maDoiTac === "ALL") {
    return "Tất cả đối tác";
  }

  const found = partners.value.find(
      (p) => p.maDoiTac === Number(maDoiTac)
  );

  return found ? found.tenDoiTac : "Không rõ đối tác";
}

function formatMoney(value) {
  return new Intl.NumberFormat("vi-VN").format(value || 0) + " ₫";
}

function handleCustomerNameInput() {
  form.value.maKhachHang = null;
  showCustomerSuggestions.value = String(form.value.tenKhachHang || "").trim().length > 0;
}

function handleCustomerNameFocus() {
  if (String(form.value.tenKhachHang || "").trim().length > 0) {
    showCustomerSuggestions.value = true;
  }
}

function selectCustomer(kh) {
  form.value.maKhachHang = kh.maKhachHang;
  form.value.tenKhachHang = kh.tenKhachHang || "";
  form.value.soDienThoai = normalizeVietnamPhone(kh.soDienThoai || "").slice(0, 10);
  form.value.cccd = onlyDigits(kh.cccd || "").slice(0, 12);
  form.value.email = kh.email || "";
  form.value.soNhaDuong = kh.soNhaDuong || "";
  form.value.phuongXa = kh.phuongXa || "";
  form.value.quanHuyen = kh.quanHuyen || "";
  form.value.tinhThanh = kh.tinhThanh || "";
  form.value.diaChi = kh.diaChi || formatFullAddress(kh.soNhaDuong, kh.phuongXa, kh.quanHuyen, kh.tinhThanh);

  showCustomerSuggestions.value = false;
}

function cloneItems(items) {
  return JSON.parse(JSON.stringify(items));
}

async function openProductModal(tab = "PRODUCT") {
  activeModalTab.value = tab;
  showCustomerSuggestions.value = false;
  tempItems.value = cloneItems(form.value.items);

  selectedProductPartnerId.value = "ALL";
  productKeyword.value = "";
  comboKeyword.value = "";

  showProductModal.value = true;
  await nextTick();

  if (allProducts.value.length === 0) {
    await loadProductsFromDatabase();
  } else {
    loadProductsFromDatabase();
  }

  if (combos.value.length === 0) {
    await loadCombosFromDatabase();
  } else {
    loadCombosFromDatabase();
  }
}

function addComboToOrder(combo) {
  if (!combo.sanPhams || combo.sanPhams.length === 0) {
    alert("Combo này hiện chưa có sản phẩm nào");
    return;
  }

  let addedCount = 0;
  const outOfStockProducts = [];

  combo.sanPhams.forEach((comboItem) => {
    const foundInAll = allProducts.value.find(
        (p) => p.maSanPham === comboItem.maSanPham
    );
    const tonKho = foundInAll
        ? Number(foundInAll.tonKho || 0)
        : Number(comboItem.soLuong ?? 999);
    const qtyToAdd = Number(comboItem.soLuongTrongCombo || 1);

    if (tonKho <= 0) {
      outOfStockProducts.push(comboItem.tenSanPham);
      return;
    }

    const existing = tempItems.value.find(
        (item) => item.maSanPham === comboItem.maSanPham
    );
    if (existing) {
      const newQty = existing.soLuong + qtyToAdd;
      if (newQty > tonKho) {
        existing.soLuong = tonKho;
      } else {
        existing.soLuong = newQty;
      }
    } else {
      tempItems.value.push({
        maSanPham: comboItem.maSanPham,
        tenSanPham: comboItem.tenSanPham,
        loai: comboItem.loai || "Chưa phân loại",
        giaTien: Number(comboItem.giaTien || 0),
        tonKho: tonKho,
        maDoiTac: comboItem.maDoiTac,
        tenDoiTac: comboItem.tenDoiTac || getPartnerName(comboItem.maDoiTac),
        hinhAnh: buildImageUrl(comboItem.hinhAnh),
        soLuong: Math.min(qtyToAdd, tonKho),
      });
    }
    addedCount++;
  });

  if (outOfStockProducts.length > 0) {
    alert(
        `Đã thêm sản phẩm của combo '${combo.tenCombo}' vào đơn hàng. Một số sản phẩm đã hết tồn kho: ${outOfStockProducts.join(
            ", "
        )}`
    );
  }
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
  if (Number(sp.tonKho || 0) <= 0) {
    alert("Sản phẩm này đã hết hàng");
    return;
  }

  const existing = getTempItem(sp.maSanPham);

  if (existing) {
    if (existing.soLuong >= Number(sp.tonKho || 0)) {
      alert(`Sản phẩm '${sp.tenSanPham}' chỉ còn ${sp.tonKho}`);
      return;
    }

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

  if (existing.soLuong >= Number(sp.tonKho || 0)) {
    alert(`Sản phẩm '${sp.tenSanPham}' chỉ còn ${sp.tonKho}`);
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
  if (item.soLuong >= Number(item.tonKho || 0)) {
    alert(`Sản phẩm '${item.tenSanPham}' chỉ còn ${item.tonKho}`);
    return;
  }

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
  form.value.tenKhachHang = String(form.value.tenKhachHang || "").trim();
  form.value.soDienThoai = normalizeVietnamPhone(form.value.soDienThoai).slice(0, 10);
  form.value.cccd = onlyDigits(form.value.cccd).slice(0, 12);
  form.value.email = String(form.value.email || "").trim();
  form.value.soNhaDuong = String(form.value.soNhaDuong || "").trim();
  form.value.phuongXa = String(form.value.phuongXa || "").trim();
  form.value.quanHuyen = String(form.value.quanHuyen || "").trim();
  form.value.tinhThanh = String(form.value.tinhThanh || "").trim();
  form.value.diaChi = formatFullAddress(
    form.value.soNhaDuong,
    form.value.phuongXa,
    form.value.quanHuyen,
    form.value.tinhThanh,
    form.value.diaChi
  );
  form.value.ghiChu = String(form.value.ghiChu || "").trim();

  if (!form.value.tenKhachHang) {
    alert("Vui lòng nhập tên khách hàng");
    return;
  }

  if (form.value.tenKhachHang.length > 50) {
    alert("Tên khách hàng tối đa 50 ký tự");
    return;
  }

  if (!form.value.soDienThoai) {
    alert("Vui lòng nhập số điện thoại");
    return;
  }

  if (!isValidVietnamPhone(form.value.soDienThoai)) {
    alert("Số điện thoại Việt Nam không hợp lệ. Ví dụ: 0901234567");
    return;
  }

  if (form.value.cccd && !isValidCCCD(form.value.cccd)) {
    alert("CCCD phải gồm đúng 12 chữ số");
    return;
  }

  if (form.value.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    alert("Email không đúng định dạng");
    return;
  }

  if (form.value.soNhaDuong && form.value.soNhaDuong.length > 255) {
    alert("Số nhà, tên đường không được vượt quá 255 ký tự");
    return;
  }

  if (form.value.phuongXa && form.value.phuongXa.length > 100) {
    alert("Phường/Xã không được vượt quá 100 ký tự");
    return;
  }

  if (form.value.quanHuyen && form.value.quanHuyen.length > 100) {
    alert("Quận/Huyện không được vượt quá 100 ký tự");
    return;
  }

  if (form.value.tinhThanh && form.value.tinhThanh.length > 100) {
    alert("Tỉnh/Thành phố không được vượt quá 100 ký tự");
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

<style scoped src="../../assets/styles/nhanvien/QLDonHang/PopTaoDonHang.css"></style>