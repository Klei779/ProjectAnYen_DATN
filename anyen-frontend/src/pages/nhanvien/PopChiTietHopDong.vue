<script setup>
import { nextTick, ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import html2canvas from "html2canvas";
import jsPDF from "jspdf";

import PreviewHopDong from "./PreviewHopDong.vue";
import {
  getChiTietHopDong,
  getDonHangDetailForHopDong,
  cancelHopDong,
} from "../../services/hopDongService.js";

const visible = defineModel();

const props = defineProps({
  hopDongId: {
    type: [Number, String],
    default: null,
  },
});

const emit = defineEmits(["canceled"]);

const loading = ref(false);
const canceling = ref(false);
const zoomLevel = ref(100);
const previewRef = ref(null);

const hopDongDetail = ref(null);
const donHangDetail = ref(null);
const orderProducts = ref([]);
const extraServices = ref([]);

const contract = ref({
  orderCode: "",
  contractCode: "",
  contractDate: "",
  employee: "",

  customerName: "",
  citizenId: "",
  address: "",
  phone: "",
  relationship: "",

  deceasedName: "",
  deathDate: "",
  age: "",
  gender: "",

  facility: "",
  cemeteryArea: "",
  graveNumber: "",
  burialDatetime: "",

  deathCertificateNo: "",
  issuedPlace: "",
  executionDate: "",
});

const formatMoney = (value) => {
  return Number(value || 0).toLocaleString("vi-VN") + " đ";
};

const formatDate = (value) => {
  if (!value) return "---";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleDateString("vi-VN");
};

const displayStatus = (status) => {
  if (status === "Đã ký / Hiệu lực") return "Đang hiệu lực";
  if (!status) return "---";
  return status;
};

const statusClass = (status) => {
  const value = displayStatus(status);

  if (value === "Đang hiệu lực") return "green";
  if (value === "Chờ ký") return "yellow";
  if (value === "Sắp hết hạn") return "purple";
  if (value === "Đã hết hạn") return "red";
  if (value === "Đã hủy") return "gray";

  return "gray";
};

const getEndDate = (item) => {
  if (!item) return "---";

  if (item.ngayHetHan) return formatDate(item.ngayHetHan);
  if (item.ngayKetThuc) return formatDate(item.ngayKetThuc);

  if (!item.ngayKyHD) return "---";

  const date = new Date(item.ngayKyHD);

  if (Number.isNaN(date.getTime())) return "---";

  date.setMonth(date.getMonth() + 3);

  return formatDate(date);
};

const resetData = () => {
  hopDongDetail.value = null;
  donHangDetail.value = null;
  orderProducts.value = [];
  extraServices.value = [];

  contract.value = {
    orderCode: "",
    contractCode: "",
    contractDate: "",
    employee: "",

    customerName: "",
    citizenId: "",
    address: "",
    phone: "",
    relationship: "",

    deceasedName: "",
    deathDate: "",
    age: "",
    gender: "",

    facility: "",
    cemeteryArea: "",
    graveNumber: "",
    burialDatetime: "",

    deathCertificateNo: "",
    issuedPlace: "",
    executionDate: "",
  };
};

const fillContractFromData = (hopDong, donHang) => {
  contract.value.orderCode =
      hopDong.maDonHangText ||
      donHang?.maDonHangText ||
      (hopDong.maDonHang ? `DH${String(hopDong.maDonHang).padStart(4, "0")}` : "");

  contract.value.contractCode =
      hopDong.maHopDongText ||
      (hopDong.maHopDong ? `HD${String(hopDong.maHopDong).padStart(4, "0")}` : "");

  contract.value.contractDate = hopDong.ngayKyHD || hopDong.ngayViet || "";
  contract.value.employee = donHang?.tenNhanVien || "";

  contract.value.customerName =
      donHang?.tenKhachHang ||
      hopDong.tenKhachHang ||
      "";

  contract.value.citizenId = donHang?.cccd || "";
  contract.value.address = donHang?.diaChi || "";
  contract.value.phone =
      donHang?.soDienThoai ||
      hopDong.soDienThoai ||
      "";

  contract.value.facility = "Công viên nghĩa trang An Yên";
  contract.value.executionDate = hopDong.ngayKyHD || hopDong.ngayViet || "";
};

const loadDetail = async () => {
  if (!props.hopDongId) return;

  try {
    loading.value = true;
    resetData();

    const hopDong = await getChiTietHopDong(props.hopDongId);
    hopDongDetail.value = hopDong;

    let donHang = null;

    if (hopDong.maDonHang) {
      donHang = await getDonHangDetailForHopDong(hopDong.maDonHang);
      donHangDetail.value = donHang;

      orderProducts.value = (donHang.sanPhams || []).map((item) => ({
        name: item.tenSanPham || "---",
        quantity: item.soLuong || 0,
        price: Number(item.giaTien || 0),
        thanhTien: Number(item.thanhTien || 0),
        loai: item.loai || "",
      }));
    }

    fillContractFromData(hopDong, donHang);
  } catch (error) {
    console.error("Lỗi load chi tiết hợp đồng:", error);
    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể tải chi tiết hợp đồng"
    );
  } finally {
    loading.value = false;
  }
};

const zoomIn = () => {
  if (zoomLevel.value < 200) {
    zoomLevel.value += 10;
  }
};

const zoomOut = () => {
  if (zoomLevel.value > 50) {
    zoomLevel.value -= 10;
  }
};

const resetZoom = () => {
  zoomLevel.value = 100;
};

const handleDownloadPDF = async () => {
  try {
    if (!previewRef.value) {
      ElMessage.error("Không có nội dung hợp đồng để tải");
      return;
    }

    ElMessage.info("Đang xuất file PDF...");

    const oldZoom = zoomLevel.value;
    zoomLevel.value = 100;

    await nextTick();

    const paper = previewRef.value.querySelector(".contract-paper");

    if (!paper) {
      zoomLevel.value = oldZoom;
      ElMessage.error("Không tìm thấy khung hợp đồng");
      return;
    }

    const canvas = await html2canvas(paper, {
      scale: 2.5,
      useCORS: true,
      backgroundColor: "#ffffff",
    });

    const imgData = canvas.toDataURL("image/png");
    const pdf = new jsPDF("p", "mm", "a4");

    pdf.addImage(imgData, "PNG", 0, 0, 210, 297);

    const fileName = contract.value.contractCode || "hop-dong";
    pdf.save(`${fileName}.pdf`);

    zoomLevel.value = oldZoom;

    ElMessage.success("Tải PDF thành công");
  } catch (error) {
    console.error("Lỗi xuất PDF:", error);
    ElMessage.error("Không thể xuất PDF");
  }
};

const handlePrint = () => {
  if (!previewRef.value) return;

  const content = previewRef.value.innerHTML;

  const styles = Array.from(
      document.querySelectorAll('style, link[rel="stylesheet"]')
  )
      .map((style) => style.outerHTML)
      .join("");

  const printWindow = window.open("", "", "width=800,height=900");

  printWindow.document.write(`
    <html>
      <head>
        <title>In hợp đồng</title>
        ${styles}
        <style>
          @media print {
            @page {
              size: A4;
              margin: 0;
            }

            body {
              margin: 0;
              padding: 0;
              background: white;
              -webkit-print-color-adjust: exact;
              print-color-adjust: exact;
            }

            .preview-wrapper {
              height: auto !important;
              overflow: visible !important;
              padding: 0 !important;
              background: transparent !important;
            }

            .contract-paper {
              box-shadow: none !important;
              margin: 0 !important;
              padding: 15mm !important;
              width: 100% !important;
              min-height: auto !important;
            }

            input,
            .dotted-input {
              background: transparent !important;
            }
          }
        </style>
      </head>

      <body>
        <div>${content}</div>

        <script>
          window.onload = () => {
            setTimeout(() => {
              window.print();
              window.close();
            }, 300);
          };
        <\/script>
      </body>
    </html>
  `);

  printWindow.document.close();
};

const handleCancelContract = async () => {
  if (!hopDongDetail.value?.maHopDong) return;

  if (displayStatus(hopDongDetail.value.trangThai) === "Đã hủy") {
    ElMessage.warning("Hợp đồng này đã bị hủy trước đó");
    return;
  }

  try {
    await ElMessageBox.confirm(
        `Bạn có chắc muốn hủy hợp đồng ${contract.value.contractCode}?`,
        "Xác nhận hủy hợp đồng",
        {
          confirmButtonText: "Hủy hợp đồng",
          cancelButtonText: "Không",
          type: "warning",
        }
    );

    canceling.value = true;

    const updated = await cancelHopDong(hopDongDetail.value.maHopDong);

    hopDongDetail.value = updated;

    ElMessage.success("Hủy hợp đồng thành công");
    emit("canceled", updated);
  } catch (error) {
    if (error === "cancel" || error === "close") return;

    console.error("Lỗi hủy hợp đồng:", error);

    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể hủy hợp đồng"
    );
  } finally {
    canceling.value = false;
  }
};

watch(
    () => visible.value,
    async (isOpen) => {
      if (isOpen) {
        await loadDetail();
      } else {
        resetData();
      }
    }
);

watch(
    () => props.hopDongId,
    async () => {
      if (visible.value) {
        await loadDetail();
      }
    }
);
</script>

<template>
  <el-dialog
      v-model="visible"
      width="95%"
      top="2vh"
      class="view-contract-dialog"
      :show-close="false"
      :z-index="10060"
  >
    <template #header>
      <div class="view-dialog-header">
        <div>
          <p>XEM HỢP ĐỒNG</p>
          <h3>
            {{ contract.contractCode || "Chi tiết hợp đồng" }}
          </h3>
        </div>

        <button class="close-text-btn" type="button" @click="visible = false">
          X
        </button>
      </div>
    </template>

    <div v-if="loading" class="view-loading">
      Đang tải chi tiết hợp đồng...
    </div>

    <div v-else class="view-contract-layout">
      <div class="view-info-panel">
        <div class="info-card">
          <div class="info-card-head">
            <h4>Thông tin hợp đồng</h4>

            <span
                v-if="hopDongDetail"
                class="status-pill"
                :class="statusClass(hopDongDetail.trangThai)"
            >
              {{ displayStatus(hopDongDetail.trangThai) }}
            </span>
          </div>

          <div class="info-grid">
            <p>
              <span>Mã hợp đồng</span>
              <b>{{ contract.contractCode || "---" }}</b>
            </p>

            <p>
              <span>Mã đơn hàng</span>
              <b>{{ contract.orderCode || "---" }}</b>
            </p>

            <p>
              <span>Ngày ký</span>
              <b>{{ formatDate(hopDongDetail?.ngayKyHD) }}</b>
            </p>

            <p>
              <span>Ngày viết</span>
              <b>{{ formatDate(hopDongDetail?.ngayViet) }}</b>
            </p>

            <p>
              <span>Ngày hết hạn</span>
              <b>{{ getEndDate(hopDongDetail) }}</b>
            </p>

            <p>
              <span>Giá trị hợp đồng</span>
              <b>{{ formatMoney(hopDongDetail?.giaTriHopDong) }}</b>
            </p>
          </div>
        </div>

        <div class="info-card">
          <h4>Thông tin khách hàng</h4>

          <div class="info-grid">
            <p>
              <span>Khách hàng</span>
              <b>{{ contract.customerName || "---" }}</b>
            </p>

            <p>
              <span>Số điện thoại</span>
              <b>{{ contract.phone || "---" }}</b>
            </p>

            <p>
              <span>CCCD</span>
              <b>{{ contract.citizenId || "---" }}</b>
            </p>

            <p class="full">
              <span>Địa chỉ</span>
              <b>{{ contract.address || "---" }}</b>
            </p>
          </div>
        </div>

        <div class="info-card">
          <h4>Sản phẩm / dịch vụ trong đơn hàng</h4>

          <table class="product-table">
            <thead>
            <tr>
              <th>STT</th>
              <th>Tên</th>
              <th>SL</th>
              <th>Đơn giá</th>
              <th>Thành tiền</th>
            </tr>
            </thead>

            <tbody>
            <tr
                v-for="(item, index) in orderProducts"
                :key="index"
            >
              <td>{{ index + 1 }}</td>
              <td>{{ item.name }}</td>
              <td>{{ item.quantity }}</td>
              <td>{{ formatMoney(item.price) }}</td>
              <td>{{ formatMoney(item.thanhTien || item.price * item.quantity) }}</td>
            </tr>

            <tr v-if="orderProducts.length === 0">
              <td colspan="5" class="empty-cell">
                Chưa có sản phẩm / dịch vụ
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <div class="left-actions">
          <button class="print-btn" type="button" @click="handlePrint">
            In hợp đồng
          </button>

          <button class="download-btn" type="button" @click="handleDownloadPDF">
            Tải PDF
          </button>

          <button
              class="cancel-contract-btn"
              type="button"
              :disabled="canceling || displayStatus(hopDongDetail?.trangThai) === 'Đã hủy'"
              @click="handleCancelContract"
          >
            {{ canceling ? "Đang hủy..." : "Hủy hợp đồng" }}
          </button>
        </div>
      </div>

      <div class="view-preview-panel">
        <div class="preview-top">
          <div>
            <h4>File PDF hợp đồng</h4>
            <p>Xem trước nội dung hợp đồng trước khi in hoặc tải PDF</p>
          </div>

          <div class="zoom-actions">
            <button type="button" @click="resetZoom">100%</button>
            <button type="button" @click="zoomOut">-</button>
            <span>{{ zoomLevel }}%</span>
            <button type="button" @click="zoomIn">+</button>
          </div>
        </div>

        <div class="preview-scroll">
          <div
              :style="{
                transform: `scale(${zoomLevel / 100})`,
                transformOrigin: 'top center',
                transition: 'transform 0.2s'
              }"
          >
            <div ref="previewRef">
              <PreviewHopDong
                  :contract="contract"
                  :extra-services="extraServices"
                  :order-products="orderProducts"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="view-dialog-footer">
        <button type="button" class="footer-close-btn" @click="visible = false">
          Đóng
        </button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.view-dialog-header,
.view-dialog-header *,
.view-contract-layout,
.view-contract-layout *,
.view-dialog-footer,
.view-dialog-footer * {
  font-family: Arial, Helvetica, sans-serif !important;
}

.view-dialog-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.view-dialog-header p {
  margin: 0 0 5px;
  color: #dc2626;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
}

.view-dialog-header h3 {
  margin: 0;
  color: #111827;
  font-size: 22px;
  font-weight: 600;
}

.close-text-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: #f8fafc;
  color: #334155;
  cursor: pointer;
  font-size: 15px;
  font-weight: 700;
}

.close-text-btn:hover {
  background: #fee2e2;
  color: #dc2626;
}

.view-loading {
  min-height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  font-size: 14px;
}

.view-contract-layout {
  height: 76vh;
  display: grid;
  grid-template-columns: 430px minmax(0, 1fr);
  gap: 18px;
}

.view-info-panel {
  overflow-y: auto;
  padding-right: 4px;
}

.info-card {
  background: #ffffff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 14px;
}

.info-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.info-card h4 {
  margin: 0 0 14px;
  color: #111827;
  font-size: 15px;
  font-weight: 700;
}

.info-card-head h4 {
  margin-bottom: 0;
}

.status-pill {
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
}

.status-pill.green {
  background: #eaf9ef;
  color: #17934a;
}

.status-pill.yellow {
  background: #fff6df;
  color: #d49000;
}

.status-pill.purple {
  background: #f8efff;
  color: #8b3fd1;
}

.status-pill.red {
  background: #fee2e2;
  color: #dc2626;
}

.status-pill.gray {
  background: #f1f5f9;
  color: #64748b;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-grid p {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.info-grid p.full {
  grid-column: 1 / -1;
}

.info-grid span {
  color: #64748b;
  font-size: 12px;
  font-weight: 400;
}

.info-grid b {
  color: #111827;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
}

.product-table {
  width: 100%;
  border-collapse: collapse;
}

.product-table th,
.product-table td {
  border-bottom: 1px solid #eef2f7;
  padding: 9px 6px;
  font-size: 12px;
  text-align: left;
}

.product-table th {
  color: #475569;
  font-weight: 600;
}

.product-table td {
  color: #111827;
  font-weight: 400;
}

.empty-cell {
  text-align: center !important;
  color: #64748b !important;
  padding: 18px 6px !important;
}

.left-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.print-btn,
.download-btn,
.cancel-contract-btn,
.footer-close-btn,
.zoom-actions button {
  height: 38px;
  border-radius: 10px;
  border: 1px solid transparent;
  padding: 0 14px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
}

.print-btn {
  background: #ffffff;
  color: #334155;
  border-color: #dbe3ef;
}

.download-btn {
  background: #111827;
  color: #ffffff;
}

.cancel-contract-btn {
  background: #dc2626;
  color: #ffffff;
}

.cancel-contract-btn:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.view-preview-panel {
  min-width: 0;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  background: #ffffff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.preview-top {
  padding: 14px 16px;
  border-bottom: 1px solid #eef2f7;
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.preview-top h4 {
  margin: 0;
  color: #111827;
  font-size: 15px;
  font-weight: 700;
}

.preview-top p {
  margin: 5px 0 0;
  color: #64748b;
  font-size: 12px;
}

.zoom-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.zoom-actions button {
  height: 30px;
  padding: 0 10px;
  background: #ffffff;
  border-color: #dbe3ef;
  color: #334155;
}

.zoom-actions span {
  min-width: 46px;
  text-align: center;
  color: #334155;
  font-size: 13px;
}

.preview-scroll {
  flex: 1;
  overflow: auto;
  background: #f0f2f5;
}

.view-dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.footer-close-btn {
  background: #ffffff;
  border-color: #dbe3ef;
  color: #334155;
}

@media (max-width: 1100px) {
  .view-contract-layout {
    height: auto;
    grid-template-columns: 1fr;
  }

  .view-preview-panel {
    height: 72vh;
  }
}

@media (max-width: 720px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .preview-top {
    flex-direction: column;
  }

  .left-actions {
    flex-direction: column;
  }

  .print-btn,
  .download-btn,
  .cancel-contract-btn {
    width: 100%;
  }
}
</style>