<script setup>
import { computed, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import api from "../../api/api.js";
import {
  Close,
  Tickets,
  Calendar,
  Document,
  Money,
  Printer,
  Download,
} from "@element-plus/icons-vue";
import logoAnYen from "../../assets/images/icon/logoAnYen.png";

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  donHang: {
    type: Object,
    default: null,
  },
  mode: {
    type: String,
    default: "create",
  },
});

const emit = defineEmits(["update:modelValue", "created"]);

const form = ref({
  ngayIn: "",
  phuongThucThanhToan: "",
  trangThai: "Chưa thanh toán",
});

const loading = ref(false);

const isViewMode = computed(() => props.mode === "view");

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

      const pttt = dh.phuongThucThanhToan || dh.PhuongThucThanhToan;
      const phuongThucHopLe = ["Tiền mặt", "Chuyển khoản"].includes(pttt)
          ? pttt
          : "Tiền mặt";

      form.value = {
        ngayIn: formatDateInput(new Date()),
        phuongThucThanhToan: phuongThucHopLe,
        trangThai:
            props.mode === "view" && dh.trangThaiHoaDon
                ? dh.trangThaiHoaDon
                : dh.trangThaiThanhToan === "Đã thanh toán" ||
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
      props.donHang?.soDienThoaiKH ||
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
      item.phanLoai ||
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

// ===== In hóa đơn (Print) =====
const buildInvoiceHtml = () => {
  return `
    <!DOCTYPE html>
    <html lang="vi">
    <head>
      <meta charset="UTF-8" />
      <title>Hóa đơn - ${maCode.value}</title>
      <style>
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap');
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
          font-family: 'Inter', Arial, sans-serif;
          color: #1e293b;
          padding: 32px 40px;
          font-size: 15px;
          line-height: 1.6;
        }
        .print-header {
          text-align: center;
          margin-bottom: 28px;
          padding-bottom: 20px;
          border-bottom: 2px solid #e2e8f0;
        }
        .print-header h1 {
          font-size: 32px;
          font-weight: 900;
          color: #0f172a;
          margin-bottom: 4px;
          text-transform: uppercase;
          letter-spacing: 1px;
        }
        .print-header p { color: #64748b; font-size: 14px; }
        .section-title {
          font-size: 18px;
          font-weight: 800;
          color: #142d4d;
          margin: 20px 0 10px;
          padding-bottom: 6px;
          border-bottom: 1px solid #e2e8f0;
          text-transform: uppercase;
        }
        .info-grid {
          display: grid;
          grid-template-columns: 200px 1fr;
          gap: 8px 16px;
          margin-bottom: 12px;
        }
        .info-grid .lbl { color: #64748b; font-size: 15px; }
        .info-grid .val { color: #0f172a; font-weight: 600; font-size: 16px; }
        table {
          width: 100%;
          border-collapse: collapse;
          margin: 12px 0 20px;
          font-size: 15px;
        }
        th {
          background: #f1f5f9;
          color: #0f172a;
          font-weight: 700;
          padding: 8px 6px;
          border: 1px solid #e2e8f0;
          text-align: center;
        }
        td {
          padding: 8px 6px;
          border: 1px solid #e2e8f0;
          text-align: center;
          vertical-align: middle;
        }
        td.left { text-align: left; }
        .summary {
          margin-top: 14px;
          border-top: 2px solid #e2e8f0;
          padding-top: 12px;
        }
        .summary-row {
          display: flex;
          justify-content: space-between;
          padding: 6px 0;
          font-size: 16px;
        }
        .summary-total {
          display: flex;
          justify-content: space-between;
          padding: 12px 0 0;
          font-size: 24px;
          font-weight: 900;
          color: #14843f;
          border-top: 1px dashed #cbd5e1;
          margin-top: 8px;
        }
        @media print {
          body { padding: 16px 24px; }
        }
      </style>
    </head>
    <body>
      <div class="print-header">
        <img src="${logoAnYen.startsWith('http') ? logoAnYen : window.location.origin + logoAnYen}" alt="An Yên Logo" style="height: 60px; margin-bottom: 10px;" onerror="this.style.display='none'"/>
        <h1>Hóa đơn</h1>
        <p>Mã hóa đơn: ${maHoaDonPreview.value} &nbsp;|&nbsp; Ngày in: ${form.value.ngayIn ? new Date(form.value.ngayIn).toLocaleDateString("vi-VN") : new Date().toLocaleDateString("vi-VN")}</p>
      </div>

      <div class="section-title">Thông tin đơn hàng</div>
      <div class="info-grid">
        <span class="lbl">Mã đơn hàng</span><span class="val">${maCode.value}</span>
        <span class="lbl">Mã khách hàng</span><span class="val">${maKhachHang.value}</span>
        <span class="lbl">Khách hàng</span><span class="val">${tenKhachHang.value}</span>
        <span class="lbl">Số điện thoại</span><span class="val">${soDienThoai.value}</span>
        <span class="lbl">Nhân viên phụ trách</span><span class="val">${tenNhanVien.value}</span>
        <span class="lbl">Ngày tạo đơn</span><span class="val">${formatDateView(ngayTaoDon.value)}</span>
        <span class="lbl">Trạng thái đơn hàng</span><span class="val">${trangThaiDonHang.value}</span>
        <span class="lbl">Ghi chú</span><span class="val">${ghiChuDonHang.value}</span>
      </div>

      <div class="section-title">Thông tin hóa đơn</div>
      <div class="info-grid">
        <span class="lbl">Mã hóa đơn</span><span class="val">${maHoaDonPreview.value}</span>
        <span class="lbl">Ngày in hóa đơn</span><span class="val">${form.value.ngayIn ? new Date(form.value.ngayIn).toLocaleDateString("vi-VN") : ""}</span>
        <span class="lbl">Phương thức thanh toán</span><span class="val">${form.value.phuongThucThanhToan}</span>
        <span class="lbl">Trạng thái thanh toán</span><span class="val">${trangThaiThanhToan.value}</span>
        <span class="lbl">Trạng thái hóa đơn</span><span class="val">${form.value.trangThai}</span>
      </div>

      <div class="section-title">Sản phẩm / Dịch vụ</div>
      <table>
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
          ${rawChiTiet.value.map((item, i) => `
            <tr>
              <td>${i + 1}</td>
              <td>${getMaSanPham(item)}</td>
              <td class="left">${getTenSanPham(item)}<br/><small style="color:#64748b">${getLoaiSanPham(item)}</small></td>
              <td>${getSoLuong(item)}</td>
              <td>${formatCurrency(getThanhTien(item))}</td>
            </tr>
          `).join("")}
        </tbody>
      </table>

      <div class="summary">
        <div class="summary-row"><span>Tạm tính</span><b>${formatCurrency(tamTinh.value)}</b></div>
        <div class="summary-row"><span>Giảm giá</span><b>${formatCurrency(giamGia.value)}</b></div>
        <div class="summary-total"><span>TỔNG THANH TOÁN</span><span>${formatCurrency(tongThanhToan.value)}</span></div>
      </div>

      <div style="text-align: center; margin-top: 50px; color: #475569; font-size: 16px; font-style: italic;">
        <p>Cảm ơn quý khách đã tin tưởng và sử dụng dịch vụ của An Yên.</p>
        <p>Kính chúc quý gia đình bình an và nhiều sức khỏe!</p>
      </div>
    </body>
    </html>
  `;
};

const printInvoice = () => {
  const printWindow = window.open("", "_blank", "width=800,height=900");
  if (!printWindow) {
    ElMessage.warning("Trình duyệt đã chặn cửa sổ in. Vui lòng cho phép popup.");
    return;
  }
  printWindow.document.write(buildInvoiceHtml());
  printWindow.document.close();
  printWindow.onload = () => {
    printWindow.focus();
    printWindow.print();
  };
};

// ===== Tải file PDF =====
const downloadPDF = () => {
  const printWindow = window.open("", "_blank", "width=800,height=900");
  if (!printWindow) {
    ElMessage.warning("Trình duyệt đã chặn cửa sổ. Vui lòng cho phép popup.");
    return;
  }
  const htmlContent = buildInvoiceHtml();
  printWindow.document.write(htmlContent);
  printWindow.document.close();
  printWindow.onload = () => {
    printWindow.focus();
    // Trigger print dialog — user can choose "Save as PDF" in the print dialog
    printWindow.print();
    ElMessage.info('Chọn "Save as PDF" / "Lưu dưới dạng PDF" trong hộp thoại in để tải file PDF.');
  };
};

const submitHoaDon = async () => {
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

  try {
    loading.value = true;

    await api.post("/api/nhan-vien/hoa-don", payload);

    ElMessage.success("Tạo hóa đơn thành công");

    emit("update:modelValue", false);
    emit("created");
  } catch (error) {
    console.error("Lỗi tạo hóa đơn:", error);
    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Tạo hóa đơn thất bại"
    );
  } finally {
    loading.value = false;
  }
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
              <h2>{{ isViewMode ? "Xem hóa đơn" : "Tạo hóa đơn" }}</h2>
              <p>
                {{ isViewMode ? "Thông tin hóa đơn của đơn hàng" : "Xác nhận thông tin và tạo hóa đơn cho đơn hàng" }}
              </p>
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
                  <input v-model="form.ngayIn" type="date" :disabled="isViewMode" />
                  <el-icon>
                    <Calendar />
                  </el-icon>
                </div>
              </div>

              <div class="form-group">
                <label>Phương thức thanh toán <em>*</em></label>
                <select v-model="form.phuongThucThanhToan" :disabled="isViewMode">
                  <option>Tiền mặt</option>
                  <option>Chuyển khoản</option>
                </select>
              </div>

              <div class="form-group">
                <label>Trạng thái hóa đơn <em>*</em></label>
                <select v-model="form.trangThai" :disabled="isViewMode">
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
          <div class="footer-left">
            <button class="btn-action btn-print-invoice" @click="printInvoice">
              <el-icon><Printer /></el-icon>
              In hóa đơn
            </button>
          </div>

          <div class="footer-right">
            <button
                v-if="isViewMode"
                class="btn-submit"
                @click="closePopup"
            >
              Đóng
            </button>

            <button
                v-else
                class="btn-submit"
                :class="{ disabled: !canCreateInvoice || loading }"
                :disabled="!canCreateInvoice || loading"
                @click="submitHoaDon"
            >
              <el-icon>
                <Money />
              </el-icon>
              {{ loading ? "Đang tạo..." : "Tạo hóa đơn" }}
            </button>
          </div>
        </div>
    </div>
    </div>
  </Teleport>
</template>

<style scoped src="../../assets/styles/nhanvien/QLDonHang/PopTaoHoaDon.css"></style>