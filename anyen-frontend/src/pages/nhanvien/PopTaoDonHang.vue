<template>
  <div class="popup-overlay">
    <div class="create-order-modal">
      <!-- HEADER -->
      <div class="modal-header">
        <div>
          <h2>Tạo đơn hàng mới</h2>
          <p>Nhập thông tin khách hàng, chọn đối tác và thêm sản phẩm</p>
        </div>

        <button class="icon-close" @click="$emit('close')">×</button>
      </div>

      <!-- TOP FORM -->
      <div class="top-form-grid">
        <!-- THÔNG TIN KHÁCH HÀNG -->
        <div class="card-box">
          <div class="card-title" style="color: #142d4d;">
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
              <input v-model="form.tenKhachHang" type="text"/>
            </div>

            <div class="form-group">
              <label>Số điện thoại <span>*</span></label>
              <input v-model="form.soDienThoai" type="text"/>
            </div>
          </div>

          <div class="form-row-2">
            <div class="form-group">
              <label>CCCD</label>
              <input v-model="form.cccd" type="text"/>
            </div>

            <div class="form-group">
              <label>Email</label>
              <input v-model="form.email" type="text"/>
            </div>
          </div>

          <div class="form-group">
            <label>Địa chỉ</label>
            <input v-model="form.diaChi" type="text"/>
          </div>
        </div>

        <!-- THÔNG TIN ĐƠN HÀNG -->
        <div class="card-box">
          <div class="card-title" style="color: #142d4d;">
            Thông tin đơn hàng
          </div>

          <div class="form-group">
            <label>Đối tác nhận đơn <span>*</span></label>
            <select v-model="form.maDoiTac" @change="onPartnerChange">
              <option value="">-- Chọn đối tác --</option>
              <option
                  v-for="dt in partners"
                  :key="dt.maDoiTac"
                  :value="dt.maDoiTac"
              >
                {{ dt.tenDoiTac }}
              </option>
            </select>
          </div>

          <div class="form-row-2">
            <div class="form-group">
              <label>Nhân viên phụ trách</label>
              <input v-model="form.nhanVienPhuTrach" type="text" readonly/>
            </div>

            <div class="form-group">
              <label>Ngày tạo đơn</label>
              <input v-model="form.ngayTaoDon" type="date"/>
            </div>
          </div>

          <div class="form-group">
            <label>Ghi chú tư vấn</label>
            <textarea
                v-model="form.ghiChu"
                rows="4"
                placeholder="Nhập ghi chú tư vấn của nhân viên..."
            ></textarea>
          </div>
        </div>
      </div>

      <!-- PRODUCT ACTION -->
      <div class="product-action-bar">
        <div class="left-note">
          Chọn sản phẩm thuộc đối tác đã chọn để đưa vào đơn hàng
        </div>

        <button
            class="btn-primary-outline"
            :disabled="!form.maDoiTac"
            @click="openProductModal"
        >
          + Chọn sản phẩm
        </button>
      </div>

      <!-- SẢN PHẨM TRONG ĐƠN -->
      <div class="card-box product-order-box">
        <div class="card-title" style="color: #142d4d;">
          Sản phẩm trong đơn
        </div>

        <div v-if="form.items.length === 0" class="empty-box">
          Chưa có sản phẩm nào trong đơn. Hãy bấm
          <strong>“Chọn sản phẩm”</strong>.
        </div>

        <div v-else class="order-product-table">
          <div class="table-head">
            <div>Sản phẩm</div>
            <div>Đơn giá</div>
            <div>SL</div>
            <div>Thành tiền</div>
            <div>Xóa</div>
          </div>

          <div
              v-for="item in form.items"
              :key="item.maSanPham"
              class="table-row"
          >
            <div class="product-cell">
              <div class="thumb">
                <img :src="item.hinhAnh" alt=""/>
              </div>
              <div class="product-info">
                <div class="product-name">{{ item.tenSanPham }}</div>
                <div class="product-sub">
                  {{ item.loai }} • Tồn: {{ item.tonKho }}
                </div>
              </div>
            </div>

            <div>{{ formatMoney(item.giaTien) }}</div>

            <div>
              <div class="qty-box">
                <button @click="decreaseQty(item)">−</button>
                <span>{{ item.soLuong }}</span>
                <button @click="increaseQty(item)">+</button>
              </div>
            </div>

            <div class="money-red">
              {{ formatMoney(item.giaTien * item.soLuong) }}
            </div>

            <div>
              <button class="btn-delete" @click="removeItem(item.maSanPham)">
                🗑
              </button>
            </div>
          </div>
        </div>


        <div class="summary-row">
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
      <div class="product-popup">
        <div class="product-popup-header">
          <div>
            <h3>Chọn sản phẩm</h3>
            <p>
              Đối tác:
              <strong>{{ selectedPartnerName }}</strong>
            </p>
          </div>

          <button class="icon-close" @click="closeProductModal">×</button>
        </div>

        <div class="product-popup-toolbar">
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
                  class="product-card"
              >
                <div class="product-card-left">
                  <div class="product-image">
                    <img :src="sp.hinhAnh" alt=""/>
                  </div>

                  <div class="product-card-info">
                    <div class="product-card-name">{{ sp.tenSanPham }}</div>
                    <div class="product-card-sub">{{ sp.loai }}</div>
                    <div class="product-card-price">
                      {{ formatMoney(sp.giaTien) }}
                    </div>
                    <div class="product-card-stock">Tồn: {{ sp.tonKho }}</div>
                  </div>
                </div>

                <div class="product-card-action">
                  <template v-if="getTempQty(sp.maSanPham) === 0">
                    <button class="btn-secondary" @click="addTempProduct(sp)">
                      Thêm
                    </button>
                  </template>

                  <template v-else>
                    <div class="qty-box big">
                      <button @click="decreaseTempQty(sp.maSanPham)">−</button>
                      <span>{{ getTempQty(sp.maSanPham) }}</span>
                      <button @click="increaseTempQty(sp)">+</button>
                    </div>
                  </template>
                </div>
              </div>
            </div>
          </div>

          <!-- RIGHT: SẢN PHẨM ĐÃ CHỌN -->
          <div class="selected-panel">
            <div class="panel-title">Sản phẩm đã chọn</div>

            <div v-if="tempItems.length === 0" class="empty-box">
              Chưa chọn sản phẩm nào.
            </div>

            <div v-else class="selected-list">
              <div
                  v-for="item in tempItems"
                  :key="item.maSanPham"
                  class="selected-item"
              >
                <div>
                  <div class="selected-name">{{ item.tenSanPham }}</div>
                  <div class="selected-sub">
                    {{ formatMoney(item.giaTien) }} × {{ item.soLuong }}
                  </div>
                </div>

                <div class="selected-right">
                  <strong>{{ formatMoney(item.giaTien * item.soLuong) }}</strong>
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
import {computed, ref} from "vue";

const showCustomerSuggestions = ref(false);

const emit = defineEmits(["close", "submit", "save-draft"]);

const partners = ref([
  {maDoiTac: 1, tenDoiTac: "Công ty Thiên Phúc"},
  {maDoiTac: 2, tenDoiTac: "Cơ sở An Lạc"},
  {maDoiTac: 3, tenDoiTac: "Hoa viên Vĩnh Hằng"},
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
    hinhAnh:
        "https://cdn-icons-png.flaticon.com/512/3659/3659898.png",
  },
  {
    maSanPham: 3,
    tenSanPham: "Bình tro cốt sứ trắng",
    loai: "Bình tro cốt",
    giaTien: 2500000,
    tonKho: 20,
    maDoiTac: 1,
    hinhAnh:
        "https://cdn-icons-png.flaticon.com/512/3534/3534012.png",
  },
  {
    maSanPham: 6,
    tenSanPham: "Bàn thờ tang lễ",
    loai: "Vật phẩm tang lễ",
    giaTien: 3200000,
    tonKho: 12,
    maDoiTac: 1,
    hinhAnh:
        "https://cdn-icons-png.flaticon.com/512/1046/1046874.png",
  },
  {
    maSanPham: 2,
    tenSanPham: "Quan tài gỗ căm xe cao cấp",
    loai: "Quan tài",
    giaTien: 18000000,
    tonKho: 5,
    maDoiTac: 2,
    hinhAnh:
        "https://cdn-icons-png.flaticon.com/512/3659/3659898.png",
  },
  {
    maSanPham: 4,
    tenSanPham: "Vòng hoa chia buồn",
    loai: "Hoa tang lễ",
    giaTien: 1500000,
    tonKho: 30,
    maDoiTac: 3,
    hinhAnh:
        "https://cdn-icons-png.flaticon.com/512/3468/3468379.png",
  },
]);

const today = new Date().toISOString().split("T")[0];

const customerKeyword = ref("");
const showProductModal = ref(false);
const productKeyword = ref("");

const form = ref({
  maKhachHang: null,
  tenKhachHang: "",
  soDienThoai: "",
  cccd: "",
  email: "",
  diaChi: "",
  maDoiTac: "",
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
  const found = partners.value.find(
      (p) => p.maDoiTac === Number(form.value.maDoiTac)
  );
  return found ? found.tenDoiTac : "";
});

const filteredPartnerProducts = computed(() => {
  const keyword = productKeyword.value.trim().toLowerCase();

  return allProducts.value.filter((sp) => {
    const matchPartner = sp.maDoiTac === Number(form.value.maDoiTac);
    const matchKeyword =
        !keyword || sp.tenSanPham.toLowerCase().includes(keyword);

    return matchPartner && matchKeyword;
  });
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

function formatMoney(value) {
  return new Intl.NumberFormat("vi-VN").format(value) + " ₫";
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

  // Bấm chọn xong thì ẩn gợi ý
  showCustomerSuggestions.value = false;
}

function onPartnerChange() {
  if (form.value.items.length > 0) {
    const ok = window.confirm(
        "Đổi đối tác sẽ xóa các sản phẩm đã chọn. Bạn có muốn tiếp tục không?"
    );

    if (!ok) return;

    form.value.items = [];
  }
}

function cloneItems(items) {
  return JSON.parse(JSON.stringify(items));
}

function openProductModal() {
  if (!form.value.maDoiTac) {
    alert("Vui lòng chọn đối tác trước");
    return;
  }

  tempItems.value = cloneItems(form.value.items);
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
  }
}

function removeItem(maSanPham) {
  form.value.items = form.value.items.filter(
      (item) => item.maSanPham !== maSanPham
  );
}

function saveDraft() {
  emit("save-draft", {...form.value});
  alert("Đã tạm lưu");
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

  if (!form.value.maDoiTac) {
    alert("Vui lòng chọn đối tác");
    return;
  }

  if (form.value.items.length === 0) {
    alert("Vui lòng chọn ít nhất 1 sản phẩm");
    return;
  }

  const payload = {
    ...form.value,
    tongTien: totalMoney.value,
  };

  emit("submit", payload);
}
</script>

<style scoped src="../../assets/styles/PopTaoDonHang.css"></style>