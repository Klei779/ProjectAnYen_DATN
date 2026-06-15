<script setup>
import { computed, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import {
  Close,
  Tickets,
  Calendar,
  Document,
  Money,
} from "@element-plus/icons-vue";

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  donHang: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(["update:modelValue", "submit"]);

const form = ref({
  ngayIn: "",
  phuongThucThanhToan: "",
  trangThai: "Chưa thanh toán",
});

const formatDateInput = (value) => {
  if (!value) {
    const today = new Date();
    return today.toISOString().slice(0, 10);
  }

  const d = new Date(value);
  if (Number.isNaN(d.getTime())) {
    return new Date().toISOString().slice(0, 10);
  }

  return d.toISOString().slice(0, 10);
};

watch(
    () => props.donHang,
    (dh) => {
      if (!dh) return;

      form.value = {
        ngayIn: formatDateInput(new Date()),
        phuongThucThanhToan: dh.phuongThucThanhToan || dh.PhuongThucThanhToan || "Tiền mặt",
        trangThai:
            dh.trangThaiThanhToan === "Đã thanh toán" ||
            dh.TrangThaiThanhToan === "Đã thanh toán"
                ? "Đã thanh toán"
                : "Chưa thanh toán",
      };
    },
    { immediate: true }
);

const closePopup = () => {
  emit("update:modelValue", false);
};

const formatCurrency = (value) => {
  const number = Number(value || 0);
  return number.toLocaleString("vi-VN") + " đ";
};

const formatDateView = (value) => {
  if (!value) return "Không có";

  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;

  return d.toLocaleDateString("vi-VN");
};

const maDonHang = computed(() => {
  return props.donHang?.maDonHang || props.donHang?.MaDonHang || "";
});

const maCode = computed(() => {
  if (props.donHang?.maCode) return props.donHang.maCode;
  if (!maDonHang.value) return "DH----";
  return "DH" + String(maDonHang.value).padStart(4, "0");
});

const maKhachHang = computed(() => {
  const id = props.donHang?.maKhachHang || props.donHang?.MaKhachHang;
  if (!id) return "Không có";
  return "KH" + String(id).padStart(4, "0");
});

const maHoaDonPreview = computed(() => {
  const id = props.donHang?.maHoaDon || props.donHang?.MaHoaDon;
  if (id) return "HD" + String(id).padStart(4, "0");
  return "Tự sinh khi lưu";
});

const tenKhachHang = computed(() => {
  return props.donHang?.tenKhachHang || props.donHang?.TenKhachHang || "Không có";
});

const soDienThoai = computed(() => {
  return (
      props.donHang?.soDienThoai ||
      props.donHang?.SoDienThoai ||
      props.donHang?.sdt ||
      "Không có"
  );
});

const tenNhanVien = computed(() => {
  return props.donHang?.tenNhanVien || props.donHang?.HoTenNhanVien || "Không có";
});

const ngayTaoDon = computed(() => {
  return props.donHang?.ngayTaoDon || props.donHang?.NgayTaoDon;
});

const trangThaiDonHang = computed(() => {
  return props.donHang?.trangThai || props.donHang?.TrangThai || "Không có";
});

const trangThaiThanhToan = computed(() => {
  return (
      props.donHang?.trangThaiThanhToan ||
      props.donHang?.TrangThaiThanhToan ||
      "Chưa thanh toán"
  );
});

const ghiChuDonHang = computed(() => {
  return props.donHang?.ghiChu || props.donHang?.GhiChu || "Không có";
});

const rawChiTiet = computed(() => {
  return (
      props.donHang?.chiTietDonHangs ||
      props.donHang?.chiTietDonHang ||
      props.donHang?.chiTiet ||
      props.donHang?.items ||
      props.donHang?.sanPhams ||
      []
  );
});

const chiTietHienThi = computed(() => {
  return rawChiTiet.value.slice(0, 4);
});

const soSanPhamConLai = computed(() => {
  return Math.max(rawChiTiet.value.length - chiTietHienThi.value.length, 0);
});

const getMaSanPham = (item) => {
  const id = item.maSanPham || item.MaSanPham || item.sanPham?.maSanPham;
  if (!id) return "SP----";
  return "SP" + String(id).padStart(4, "0");
};

const getTenSanPham = (item) => {
  return (
      item.tenSanPham ||
      item.TenSanPham ||
      item.sanPham?.tenSanPham ||
      item.sanPham?.TenSanPham ||
      "Sản phẩm / dịch vụ"
  );
};

const getLoaiSanPham = (item) => {
  return (
      item.loai ||
      item.Loai ||
      item.sanPham?.loai ||
      item.sanPham?.Loai ||
      "Không có"
  );
};

const getSoLuong = (item) => {
  return Number(item.soLuong || item.SoLuong || 1);
};

const getGiaTien = (item) => {
  return Number(item.giaTien || item.GiaTien || item.donGia || 0);
};

const getThanhTien = (item) => {
  return getSoLuong(item) * getGiaTien(item);
};

const tamTinh = computed(() => {
  if (rawChiTiet.value.length > 0) {
    return rawChiTiet.value.reduce((sum, item) => sum + getThanhTien(item), 0);
  }

  return Number(props.donHang?.tongTien || props.donHang?.TongTien || 0);
});

const giamGia = computed(() => {
  return Number(props.donHang?.giamGia || props.donHang?.GiamGia || 0);
});

const tongThanhToan = computed(() => {
  const tong = Number(props.donHang?.tongTien || props.donHang?.TongTien || 0);
  if (tong > 0) return tong;

  return Math.max(tamTinh.value - giamGia.value, 0);
});

const canCreateInvoice = computed(() => {
  return ["Chờ thanh toán", "Hoàn thành"].includes(trangThaiDonHang.value);
});

const badgeTrangThaiDonClass = computed(() => {
  if (trangThaiDonHang.value === "Chờ thanh toán") return "badge-purple";
  if (trangThaiDonHang.value === "Hoàn thành") return "badge-green";
  if (trangThaiDonHang.value === "Đã hủy") return "badge-red";
  return "badge-gray";
});

const badgeThanhToanClass = computed(() => {
  if (trangThaiThanhToan.value === "Đã thanh toán") return "badge-green";
  if (trangThaiThanhToan.value === "Đã đặt cọc") return "badge-blue";
  return "badge-orange";
});

const submitHoaDon = () => {
  if (!props.donHang) {
    ElMessage.error("Không tìm thấy đơn hàng");
    return;
  }

  if (!canCreateInvoice.value) {
    ElMessage.warning("Chỉ được tạo hóa đơn khi đơn hàng ở trạng thái Chờ thanh toán hoặc Hoàn thành");
    return;
  }

  if (!form.value.ngayIn) {
    ElMessage.warning("Vui lòng chọn ngày in hóa đơn");
    return;
  }

  if (!form.value.phuongThucThanhToan) {
    ElMessage.warning("Vui lòng chọn phương thức thanh toán");
    return;
  }

  const payload = {
    maDonHang: maDonHang.value,
    ngayIn: form.value.ngayIn,
    tongTien: tongThanhToan.value,
    phuongThucThanhToan: form.value.phuongThucThanhToan,
    trangThai: form.value.trangThai,
  };

  emit("submit", payload);
};
</script>

<template>
  <Teleport to="body">
    <div v-if="modelValue" class="invoice-overlay">
      <div class="invoice-popup">
        <!-- Header -->
        <div class="popup-header">
          <div class="title-wrap">
            <div class="title-icon">
              <el-icon>
                <Tickets />
              </el-icon>
            </div>

            <div>
              <h2>Tạo hóa đơn</h2>
              <p>Xác nhận thông tin và tạo hóa đơn cho đơn hàng</p>
            </div>
          </div>

          <button class="btn-close" @click="closePopup">
            <el-icon>
              <Close />
            </el-icon>
          </button>
        </div>

        <!-- Body 3 cột -->
        <div class="popup-body">
          <!-- Cột 1 -->
          <section class="panel order-info-panel">
            <h3>
              <span></span>
              Thông tin đơn hàng
            </h3>

            <div class="info-list">
              <div class="info-row">
                <label>Mã đơn hàng</label>
                <b>{{ maCode }}</b>
              </div>

              <div class="info-row">
                <label>Mã khách hàng</label>
                <b>{{ maKhachHang }}</b>
              </div>

              <div class="info-row">
                <label>Khách hàng</label>
                <b>{{ tenKhachHang }}</b>
              </div>

              <div class="info-row">
                <label>Số điện thoại</label>
                <b>{{ soDienThoai }}</b>
              </div>

              <div class="info-row">
                <label>Nhân viên phụ trách</label>
                <b>{{ tenNhanVien }}</b>
              </div>

              <div class="info-row">
                <label>Ngày tạo đơn</label>
                <b>{{ formatDateView(ngayTaoDon) }}</b>
              </div>

              <div class="info-row">
                <label>Trạng thái đơn hàng</label>
                <b>
                  <span class="badge" :class="badgeTrangThaiDonClass">
                    {{ trangThaiDonHang }}
                  </span>
                </b>
              </div>

              <div class="info-row">
                <label>Phương thức thanh toán</label>
                <b>{{ form.phuongThucThanhToan }}</b>
              </div>

              <div class="info-row">
                <label>Trạng thái thanh toán</label>
                <b>
                  <span class="badge" :class="badgeThanhToanClass">
                    {{ trangThaiThanhToan }}
                  </span>
                </b>
              </div>

              <div class="info-row note-row">
                <label>Ghi chú đơn hàng</label>
                <b>{{ ghiChuDonHang }}</b>
              </div>
            </div>
          </section>

          <!-- Cột 2 -->
          <section class="panel invoice-form-panel">
            <h3>
              <span></span>
              Thông tin hóa đơn
            </h3>

            <div class="form-grid">
              <div class="form-group">
                <label>Mã hóa đơn</label>
                <input :value="maHoaDonPreview" disabled />
              </div>

              <div class="form-group">
                <label>Ngày in hóa đơn <em>*</em></label>
                <div class="input-icon">
                  <input v-model="form.ngayIn" type="date" />
                  <el-icon>
                    <Calendar />
                  </el-icon>
                </div>
              </div>

              <div class="form-group">
                <label>Phương thức thanh toán <em>*</em></label>
                <select v-model="form.phuongThucThanhToan">
                  <option>Tiền mặt</option>
                  <option>Chuyển khoản</option>
                </select>
              </div>

              <div class="form-group">
                <label>Trạng thái hóa đơn <em>*</em></label>
                <select v-model="form.trangThai">
                  <option>Chưa thanh toán</option>
                  <option>Đã thanh toán</option>
                  <option>Đã in</option>
                </select>
              </div>

              <div class="form-group">
                <label>Tổng tiền</label>
                <input class="money-input" :value="formatCurrency(tongThanhToan)" disabled />
              </div>
            </div>
          </section>

          <!-- Cột 3 -->
          <section class="panel product-panel">
            <div class="product-header">
              <h3>
                <span></span>
                Sản phẩm trong hóa đơn
              </h3>
              <small>{{ rawChiTiet.length }} sản phẩm / dịch vụ</small>
            </div>

            <div class="product-table-wrap">
              <table class="product-table">
                <thead>
                <tr>
                  <th>STT</th>
                  <th>Mã SP</th>
                  <th>Tên sản phẩm / dịch vụ</th>
                  <th>SL</th>
                  <th>Thành tiền</th>
                </tr>
                </thead>

                <tbody>
                <tr v-if="chiTietHienThi.length === 0">
                  <td colspan="5" class="empty-product">
                    Chưa có chi tiết sản phẩm trong dữ liệu đơn hàng.
                  </td>
                </tr>

                <tr v-for="(item, index) in chiTietHienThi" :key="index">
                  <td>{{ index + 1 }}</td>
                  <td>{{ getMaSanPham(item) }}</td>
                  <td>
                    <b>{{ getTenSanPham(item) }}</b>
                    <small>{{ getLoaiSanPham(item) }}</small>
                  </td>
                  <td>{{ getSoLuong(item) }}</td>
                  <td>{{ formatCurrency(getThanhTien(item)) }}</td>
                </tr>

                <tr v-if="soSanPhamConLai > 0">
                  <td colspan="5" class="more-row">
                    + {{ soSanPhamConLai }} sản phẩm / dịch vụ khác
                  </td>
                </tr>
                </tbody>
              </table>
            </div>

            <div class="summary-box">
              <div class="sum-row">
                <span>Tạm tính</span>
                <b>{{ formatCurrency(tamTinh) }}</b>
              </div>

              <div class="sum-row">
                <span>Giảm giá</span>
                <b>{{ formatCurrency(giamGia) }}</b>
              </div>

              <div class="sum-total">
                <span>Tổng thanh toán</span>
                <strong>{{ formatCurrency(tongThanhToan) }}</strong>
              </div>
            </div>
          </section>
        </div>

        <div v-if="!canCreateInvoice" class="warning-box">
          Chỉ được tạo hóa đơn khi đơn hàng ở trạng thái
          <b>Chờ thanh toán</b> hoặc <b>Hoàn thành</b>.
        </div>

        <!-- Footer -->
        <div class="popup-footer">
          <button
              class="btn-submit"
              :class="{ disabled: !canCreateInvoice }"
              @click="submitHoaDon"
          >
            <el-icon>
              <Money />
            </el-icon>
            Tạo hóa đơn
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped src="../../assets/styles/nhanvien/QLDonHang/PopTaoHoaDon.css"></style>