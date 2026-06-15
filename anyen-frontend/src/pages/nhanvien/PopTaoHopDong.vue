<script setup>
import { ref, watch, nextTick } from "vue";
import { ElMessage } from "element-plus";
import html2canvas from "html2canvas";
import jsPDF from "jspdf";
import {
  createHopDong,
  getDonHangDetailForHopDong,
  getDonHangOptionsForHopDong
} from "../../services/hopDongService.js";
import ContractPreview from "../../components/PreviewHopDong.vue";
import {
  Document,
  User,
  Avatar,
  HomeFilled,
  Calendar,
  View,
  ZoomOut,
  Minus,
  Plus,
  Printer,
  Download,
  Delete
} from '@element-plus/icons-vue';
const visible = defineModel();


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
  burialDatetime: ""
});

const services = ref([]);

const addService = () => {
  services.value.push({ name: '', price: 0 });
};

const removeService = (index) => {
  services.value.splice(index, 1);
};

const zoomLevel = ref(100);
const previewRef = ref(null);

const zoomIn = () => {
  if (zoomLevel.value < 200) zoomLevel.value += 10;
};

const zoomOut = () => {
  if (zoomLevel.value > 50) zoomLevel.value -= 10;
};

const resetZoom = () => {
  zoomLevel.value = 100;
};

const userStr = localStorage.getItem("user");
const user = userStr ? JSON.parse(userStr) : null;

contract.value.employee =
    user?.hoTen ||
    user?.tenNhanVien ||
    "";

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
      backgroundColor: "#ffffff"
    });

    const imgData = canvas.toDataURL("image/png");

    const pdf = new jsPDF("p", "mm", "a4");

    pdf.addImage(imgData, "PNG", 0, 0, 210, 297);

    pdf.save(`${contract.value.contractCode || "hop-dong"}.pdf`);

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

  const styles = Array.from(document.querySelectorAll('style, link[rel="stylesheet"]'))
      .map(style => style.outerHTML)
      .join('');

  const printWindow = window.open('', '', 'width=800,height=900');

  printWindow.document.write(`
    <html>
      <head>
        <title>In Hợp Đồng</title>
        ${styles}
        <style>
          @media print {
            @page { size: A4; margin: 0; }
            body {
              margin: 0;
              padding: 0;
              background: white;
              -webkit-print-color-adjust: exact;
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
            input, .dotted-input {
              background: transparent !important;
            }
          }
        </style>
      </head>
      <body>
        <div>
          ${content}
        </div>
        <script>
          window.onload = () => {
            setTimeout(() => {
              window.print();
              window.close();
            }, 300);
          };
        <` + `/script>
      </body>
    </html>
  `);

  printWindow.document.close();
};

const emit = defineEmits(["success"]);

const selectedMaDonHang = ref(null);
const donHangOptions = ref([]);
const loadingOrders = ref(false);
const loadingDetail = ref(false);
const saving = ref(false);

const orderProducts = ref([]);
const selectedDonHangDetail = ref(null);

const formatMoney = (value) => {
  return Number(value || 0).toLocaleString("vi-VN") + " đ";
};

const loadDonHangOptions = async () => {
  try {
    loadingOrders.value = true;

    const data = await getDonHangOptionsForHopDong();

    donHangOptions.value = data.map((item) => ({
      ...item,
      daCoHopDong: item.daCoHopDong === true
    }));

  } catch (error) {
    console.error("Lỗi load đơn hàng:", error);
    ElMessage.error("Không thể tải danh sách đơn hàng");
  } finally {
    loadingOrders.value = false;
  }
};
const onSelectDonHang = async (maDonHang) => {
  if (!maDonHang) return;

  try {
    loadingDetail.value = true;

    const data = await getDonHangDetailForHopDong(maDonHang);
    selectedDonHangDetail.value = data;

    contract.value.orderCode =
        data.maDonHangText || `DH${String(data.maDonHang).padStart(4, "0")}`;

    const generateCode = () => {
      const now = new Date();

      return (
          "HD" +
          now.getFullYear() +
          String(now.getMonth() + 1).padStart(2, "0") +
          String(now.getDate()).padStart(2, "0") +
          Math.floor(Math.random() * 10000)
              .toString()
              .padStart(4, "0")
      );
    };

    contract.value.contractCode = generateCode();
    contract.value.contractDate = new Date().toISOString().slice(0, 10);
    contract.value.employee = data.tenNhanVien || "";

    contract.value.customerName = data.tenKhachHang || "";
    contract.value.citizenId = data.cccd || "";
    contract.value.address = data.diaChi || "";
    contract.value.phone = data.soDienThoai || "";

    orderProducts.value = (data.sanPhams || []).map((item) => ({
      name: item.tenSanPham || "---",
      quantity: item.soLuong || 0,
      price: Number(item.giaTien || 0),
      thanhTien: Number(item.thanhTien || 0),
      loai: item.loai || ""
    }));

  } catch (error) {
    console.error("Lỗi load chi tiết đơn hàng:", error);

    selectedDonHangDetail.value = null;
    selectedMaDonHang.value = null;
    orderProducts.value = [];

    ElMessage.error(
        error.response?.data?.message || "Không thể tải chi tiết đơn hàng"
    );
  } finally {
    loadingDetail.value = false;

  }
};
const saveContract = async () => {
  console.log("Đã bấm nút lưu hợp đồng");

  if (!selectedMaDonHang.value || !selectedDonHangDetail.value) {
    ElMessage.error("Vui lòng chọn và load đơn hàng trước khi lưu hợp đồng");
    return;
  }

  try {
    saving.value = true;

    const today = new Date().toISOString().slice(0, 10);

    const saved = await createHopDong({
      maDonHang: Number(selectedMaDonHang.value),
      ngayKyHD: contract.value.contractDate
          ? contract.value.contractDate.substring(0, 10)
          : today,
      ngayViet: today,
      trangThai: "Chờ ký"
    });

    ElMessage.success("Lưu hợp đồng thành công");

    emit("success", saved);

    visible.value = false;
  } catch (error) {
    console.error("Lỗi lưu hợp đồng:", error);

    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể lưu hợp đồng"
    );
  } finally {
    saving.value = false;
  }
};
  watch(
      () => visible.value,
      async (isOpen) => {
        if (!isOpen) return;

        selectedMaDonHang.value = null;
        selectedDonHangDetail.value = null;
        orderProducts.value = [];
        services.value = [];

        await loadDonHangOptions();

        console.log(
            "DON HANG OPTIONS:",
            donHangOptions.value
        );
      },
      {
        immediate: true
      }

  );

selectedMaDonHang.value = "";
</script>

<template>
  <el-dialog
      v-model="visible"
      width="95%"
      top="2vh"
      class="custom-contract-dialog"
      :show-close="true"
      :z-index="10050"
  >
    <template #header>
      <div class="dialog-header">
        <el-icon class="header-icon"><Document /></el-icon>
        <span>Tạo hợp đồng mai táng</span>
      </div>
    </template>

    <div class="contract-layout">
      <!-- LEFT FORM PANEL -->
      <div class="form-panel">
        <el-form label-position="top" :model="contract" class="custom-form">

          <div class="section-title">
            <el-icon><Document /></el-icon>
            <span>1. THÔNG TIN HỢP ĐỒNG</span>
          </div>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Đơn hàng" required>

                <select
                    v-model="selectedMaDonHang"
                    @change="onSelectDonHang(selectedMaDonHang)"
                    class="native-select"
                >
                  <option disabled value="">
                    Chọn mã đơn hàng
                  </option>

                  <option
                      v-for="item in donHangOptions"
                      :key="item.maDonHang"
                      :value="item.maDonHang"
                      :disabled="item.daCoHopDong"
                  >
                    {{ item.daCoHopDong ? '' : '' }}
                    {{ item.maDonHangText }}
                    {{ item.tenKhachHang ? ' - ' + item.tenKhachHang : '' }}
                    {{ item.daCoHopDong ? ' - Đã có hợp đồng' : '' }}
                  </option>
                </select>

              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Số hợp đồng" required>
                <el-input v-model="contract.contractCode" placeholder="Số hợp đồng (Tự động)" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Ngày lập hợp đồng" required>
                <el-date-picker
                    v-model="contract.contractDate"
                    type="datetime"
                    placeholder="Chọn Ngày lập hợp đồng"
                    format="DD/MM/YYYY HH:mm"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    style="width: 100%;"
                    :teleported="false"
                    placement="top-start"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Nhân viên lập">
                <el-input
                    v-model="contract.employee"
                    placeholder="Nhân viên lập hợp đồng"
                    disabled
                />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="section-title mt-4">
            <el-icon><User /></el-icon>
            <span>2. THÔNG TIN TANG CHỦ</span>
          </div>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Họ và tên" required>
                <el-input v-model="contract.customerName" placeholder="Nhập họ và tên tang chủ" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Số CCCD/CMND" required>
                <el-input v-model="contract.citizenId" placeholder="Nhập số CCCD/CMND" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="Địa chỉ" required>
            <el-input v-model="contract.address" placeholder="Nhập địa chỉ tang chủ" />
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Số điện thoại" required>
                <el-input v-model="contract.phone" placeholder="Nhập số điện thoại tang chủ" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Quan hệ với người mất" required>
                <el-select v-model="contract.relationship" placeholder="Chọn quan hệ" style="width: 100%">
                  <el-option label="Con trai" value="Con trai" />
                  <el-option label="Con gái" value="Con gái" />
                  <el-option label="Chồng" value="Chồng" />
                  <el-option label="Vợ" value="Vợ" />
                  <el-option label="Khác" value="Khác" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <div class="section-title mt-4">
            <span>3. SẢN PHẨM / DỊCH VỤ TRONG ĐƠN HÀNG</span>
          </div>

          <el-table
              v-loading="loadingDetail"
              :data="orderProducts"
              border
              style="width: 100%"
          >
            <el-table-column
                type="index"
                label="STT"
                width="60"
            />

            <el-table-column
                prop="name"
                label="Tên sản phẩm / dịch vụ"
            />

            <el-table-column
                prop="loai"
                label="Loại"
                width="160"
            />

            <el-table-column
                prop="quantity"
                label="Số lượng"
                width="100"
                align="center"
            />

            <el-table-column
                label="Đơn giá"
                width="150"
                align="right"
            >
              <template #default="{ row }">
                {{ formatMoney(row.price) }}
              </template>
            </el-table-column>

            <el-table-column
                label="Thành tiền"
                width="160"
                align="right"
            >
              <template #default="{ row }">
                {{ formatMoney(row.thanhTien || row.price * row.quantity) }}
              </template>
            </el-table-column>
          </el-table>


          <div class="section-title mt-4">
            <el-icon><Avatar /></el-icon>
            <span>4. THÔNG TIN NGƯỜI MẤT</span>
          </div>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Họ và tên" required>
                <el-input v-model="contract.deceasedName" placeholder="Nhập họ tên người mất" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Ngày mất" required>
                <el-date-picker
                    v-model="contract.deathDate"
                    type="datetime"
                    placeholder="nhập ngày mất"
                    format="DD/MM/YYYY HH:mm"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    style="width: 100%;"
                    :teleported="false"
                    placement="top-start"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Tuổi">
                <el-input v-model="contract.age" placeholder="Nhập tuổi" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Giới tính">
                <el-select v-model="contract.gender" placeholder="Chọn giới tính" style="width: 100%">
                  <el-option label="Nữ" value="Nữ" />
                  <el-option label="Nam" value="Nam" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <div class="section-title mt-4">
            <el-icon><HomeFilled /></el-icon>
            <span>5. THÔNG TIN NƠI AN TÁNG</span>
          </div>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Cơ sở mai táng" required>
                <el-select v-model="contract.facility" placeholder="Chọn cơ sở" style="width: 100%">
                  <el-option label="Công viên nghĩa trang An Yên" value="Công viên nghĩa trang An Yên" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Khu mộ" required>
                <el-select v-model="contract.cemeteryArea" placeholder="Chọn khu mộ" style="width: 100%">
                  <el-option label="Khu A" value="Khu A" />
                  <el-option label="Khu B" value="Khu B" />
                  <el-option label="Khu C" value="Khu C" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Số mộ" required>
                <el-input v-model="contract.graveNumber" placeholder="Nhập số mộ" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Ngày giờ an táng" required>
                <el-date-picker
                    v-model="contract.burialDatetime"
                    type="datetime"
                    placeholder="Chọn ngày giờ an táng"
                    format="DD/MM/YYYY HH:mm"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    style="width: 100%;"
                    :teleported="false"
                    placement="top-start"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="section-title mt-4 flex-between">
            <div class="title-left">
              <span>6. DỊCH VỤ SỬ DỤNG</span>
            </div>
            <el-button size="small" plain class="add-service-btn" @click="addService">
              + Thêm dịch vụ
            </el-button>
          </div>

          <div v-for="(service, index) in services" :key="index" class="service-row">
            <el-row :gutter="10" align="middle">
              <el-col :span="13">
                <el-input v-model="service.name" placeholder="Tên dịch vụ/phụ thu" />
              </el-col>
              <el-col :span="8">
                <el-input v-model="service.price" placeholder="Giá tiền" type="number" />
              </el-col>
              <el-col :span="3" style="text-align: right;">
                <el-button type="danger" :icon="Delete" circle size="small" @click="removeService(index)" plain />
              </el-col>
            </el-row>
          </div>

        </el-form>
      </div>

      <!-- RIGHT PREVIEW PANEL -->
      <div class="preview-panel">
        <div class="preview-header">
          <div class="preview-title">XEM TRƯỚC HỢP ĐỒNG</div>
          <div class="preview-actions">
            <el-icon class="action-icon" @click="resetZoom"><ZoomOut /></el-icon>
            <el-icon class="action-icon" @click="zoomOut"><Minus /></el-icon>
            <span class="zoom-text">{{ zoomLevel }}%</span>
            <el-icon class="action-icon" @click="zoomIn"><Plus /></el-icon>

            <el-icon
                class="action-icon"
                title="Tải PDF"
                @click="handleDownloadPDF"
            >
              <Download />
            </el-icon>

            <el-icon
                class="action-icon"
                title="In hợp đồng"
                @click="handlePrint"
            >
              <Printer />
            </el-icon>
          </div>
        </div>
        <div class="preview-content">
          <div :style="{ transform: `scale(${zoomLevel / 100})`, transformOrigin: 'top center', transition: 'transform 0.2s' }">
            <div ref="previewRef">
              <ContractPreview
                  :contract="contract"
                  :extraServices="services"
                  :orderProducts="orderProducts"
              />
            </div>
          </div>
        </div>
      </div>

    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-left">
          <el-icon style="margin-right: 6px;"><View /></el-icon>
          Xem trước
        </el-button>
        <div class="btn-right">
          <el-button @click="visible = false" class="cancel-btn">Hủy</el-button>
          <el-button
              type="primary"
              class="save-btn"
              :loading="saving"
              @click="saveContract"
          >
            <el-icon style="margin-right: 6px;"><Document /></el-icon>
            Lưu hợp đồng
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>
<style scoped src="../../assets/styles/PopTaoHopDong.css"></style>