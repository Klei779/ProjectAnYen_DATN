<script setup>
import { ref,watch } from "vue";
import { ElMessage } from "element-plus";
import {
  createHopDong,
  getDonHangDetailForHopDong,
  getDonHangOptionsForHopDong
} from "../../services/hopDongService.js";
import ContractPreview from "./PreviewHopDong.vue";
import {Document, User, Avatar, HomeFilled, Calendar, View, ZoomOut, Minus, Plus, Printer, Delete} from '@element-plus/icons-vue';

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
const errors = ref({
  selectedMaDonHang: "",

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
contract.value.employee = user?.hoTen || user?.tenNhanVien || "";
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
              padding: 15mm !important; /* Optional: adjust printed margins here */
              width: 100% !important;
              min-height: auto !important;
            }
            /* Hide the dotted border and background from the input elements for a cleaner print */
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
const emit = defineEmits(["created"]);
const selectedMaDonHang = ref(null);
const donHangOptions = ref([]);
const loadingOrders = ref(false);
const loadingDetail = ref(false);
const saving = ref(false);
const orderProducts = ref([]);
const selectedDonHangDetail = ref(null);

const clearErrors = () => {
  Object.keys(errors.value).forEach(key => {
    errors.value[key] = "";
  });
};

const formatMoney = (value) => {
  return Number(value || 0).toLocaleString("vi-VN") + " đ";
};

const loadDonHangOptions = async () => {
  try {
    loadingOrders.value = true;

    const data = await getDonHangOptionsForHopDong();

    console.log("API DATA:", data);

    donHangOptions.value = data;
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
const validateForm = () => {
  clearErrors();

  let isValid = true;

  // Đơn hàng
  if (!selectedMaDonHang.value) {
    errors.value.selectedMaDonHang = "Vui lòng chọn đơn hàng";
    isValid = false;
  }

  // Tang chủ
  if (!contract.value.customerName?.trim()) {
    errors.value.customerName = "Không được để trống";
    isValid = false;
  }

  if (!contract.value.citizenId?.trim()) {
    errors.value.citizenId = "Không được để trống";
    isValid = false;
  } else if (!/^[0-9]{9,12}$/.test(contract.value.citizenId)) {
    errors.value.citizenId = "CCCD chỉ được nhập số";
    isValid = false;
  }

  if (!contract.value.address?.trim()) {
    errors.value.address = "Không được để trống";
    isValid = false;
  }

  if (!contract.value.phone?.trim()) {
    errors.value.phone = "Không được để trống";
    isValid = false;
  } else {
    const phoneRegex = /^(0|\+84)[3|5|7|8|9][0-9]{8}$/;

    if (!phoneRegex.test(contract.value.phone.trim())) {
      errors.value.phone = "Số điện thoại không hợp lệ";
      isValid = false;
    }
  }

  if (!contract.value.relationship) {
    errors.value.relationship = "Vui lòng chọn quan hệ";
    isValid = false;
  }

  // Người mất
  if (!contract.value.deceasedName?.trim()) {
    errors.value.deceasedName = "Không được để trống";
    isValid = false;
  }

  if (!contract.value.deathDate) {
    errors.value.deathDate = "Vui lòng chọn ngày mất";
    isValid = false;
  }

  if (contract.value.age !== "") {
    const age = Number(contract.value.age);

    if (isNaN(age)) {
      errors.value.age = "Tuổi phải là số";
      isValid = false;
    } else if (age < 0 || age > 150) {
      errors.value.age = "Tuổi phải từ 0 đến 150";
      isValid = false;
    }
  }

  if (!contract.value.gender) {
    errors.value.gender = "Vui lòng chọn giới tính";
    isValid = false;
  }

  // Nơi an táng
  if (!contract.value.facility) {
    errors.value.facility = "Vui lòng chọn cơ sở";
    isValid = false;
  }

  if (!contract.value.cemeteryArea) {
    errors.value.cemeteryArea = "Vui lòng chọn khu mộ";
    isValid = false;
  }

  if (!contract.value.graveNumber?.trim()) {
    errors.value.graveNumber = "Không được để trống";
    isValid = false;
  }

  if (!contract.value.burialDatetime) {
    errors.value.burialDatetime = "Vui lòng chọn ngày giờ an táng";
    isValid = false;
  }

  return isValid;
};

const saveContract = async () => {
  if (!validateForm()) {
    return;
  }
  try {
    saving.value = true;

    const today = new Date().toISOString().slice(0, 10);

    const saved = await createHopDong({
      maDonHang: selectedMaDonHang.value,
      ngayKyHD: contract.value.contractDate ? contract.value.contractDate.substring(0, 10) : today,
      ngayViet: today,
      trangThai: "Chờ ký"
    });

    ElMessage.success("Lưu hợp đồng thành công");
    emit("success", saved);
    visible.value = false;
  } catch (error) {
    console.error("Lỗi lưu hợp đồng:", error);
    ElMessage.error(
        error.response?.data?.message
        || error.response?.data
        || "Không thể lưu hợp đồng"
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
                <el-select
                    v-model="selectedMaDonHang"
                    placeholder="Chọn mã đơn hàng"
                    filterable
                    :loading="loadingOrders"
                    :teleported="false"
                    style="width: 100%"
                    @change="onSelectDonHang"
                    :class="{ 'is-invalid': errors.selectedMaDonHang }"
                >
                  <el-option
                      v-for="item in donHangOptions"
                      :key="item.maDonHang"
                      :label="item.maDonHangText"
                      :value="item.maDonHang"
                  />
                </el-select>
                <div v-if="errors.selectedMaDonHang" class="error-text">
                  {{ errors.selectedMaDonHang }}
                </div>

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
                <el-input
                    v-model="contract.customerName"
                    placeholder="Nhập họ và tên tang chủ"
                    :class="{ 'is-invalid': errors.customerName }"
                />
                <div v-if="errors.customerName" class="error-text">
                  {{ errors.customerName }}
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Số CCCD/CMND" required>
                <el-input
                    v-model="contract.citizenId"
                    placeholder="Nhập số CCCD/CMND"
                    :class="{ 'is-invalid': errors.citizenId }"
                />

                <div v-if="errors.citizenId" class="error-text">
                  {{ errors.citizenId }}
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="Địa chỉ" required>
            <el-input
                v-model="contract.address"
                placeholder="Nhập địa chỉ tang chủ"
                :class="{ 'is-invalid': errors.address }"
            />

            <div v-if="errors.address" class="error-text">
              {{ errors.address }}
            </div>
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Số điện thoại" required>
                <el-input
                    v-model="contract.phone"
                    placeholder="Nhập số điện thoại tang chủ"
                    :class="{ 'is-invalid': errors.phone }"
                />

                <div v-if="errors.phone" class="error-text">
                  {{ errors.phone }}
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Quan hệ với người mất" required>
                <el-select v-model="contract.relationship" :class="{ 'is-invalid': errors.relationship }" :teleported="false" placeholder="Chọn quan hệ" style="width: 100%">
                  <el-option label="Con trai" value="Con trai" />
                  <el-option label="Con gái" value="Con gái" />
                  <el-option label="Chồng" value="Chồng" />
                  <el-option label="Vợ" value="Vợ" />
                  <el-option label="Khác" value="Khác" />
                </el-select>
                <div v-if="errors.relationship" class="error-text">
                  {{ errors.relationship }}
                </div>
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
                <el-input
                    v-model="contract.deceasedName"
                    placeholder="Nhập họ tên người mất"
                    :class="{ 'is-invalid': errors.deceasedName }"
                />

                <div v-if="errors.deceasedName" class="error-text">
                  {{ errors.deceasedName }}
                </div>
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
                <div v-if="errors.deathDate" class="error-text">
                  {{ errors.deathDate }}
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Tuổi">
                <el-input
                    v-model="contract.age"
                    type="number"
                    placeholder="Nhập tuổi"
                    :class="{ 'is-invalid': errors.age }"
                />

                <div v-if="errors.age" class="error-text">
                  {{ errors.age }}
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Giới tính">
                <el-select v-model="contract.gender" placeholder="Chọn giới tính" :teleported="false" style="width: 100%">
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
            <el-col :span="8">
              <el-form-item label="Cơ sở" required>
                <el-select
                    v-model="contract.facility"
                    placeholder="Chọn cơ sở"
                    :teleported="false"
                    style="width:100%"
                >
                  <el-option label="An Yên 1" value="An Yên 1" />
                  <el-option label="An Yên 2" value="An Yên 2" />
                </el-select>

                <div v-if="errors.facility" class="error-text">
                  {{ errors.facility }}
                </div>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="Khu mộ" required>
                <el-input
                    v-model="contract.cemeteryArea"
                    placeholder="Nhập khu mộ"
                />

                <div v-if="errors.cemeteryArea" class="error-text">
                  {{ errors.cemeteryArea }}
                </div>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="Số mộ" required>
                <el-input
                    v-model="contract.graveNumber"
                    placeholder="Nhập số mộ"
                />

                <div v-if="errors.graveNumber" class="error-text">
                  {{ errors.graveNumber }}
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
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
            <el-icon class="action-icon" @click="handlePrint"><Printer /></el-icon>
          </div>
        </div>
        <div class="preview-content">
          <div :style="{ transform: `scale(${zoomLevel / 100})`, transformOrigin: 'top center', transition: 'transform 0.2s' }">
            <div ref="previewRef">
              <ContractPreview :contract="contract" :extraServices="services" :orderProducts="orderProducts" />
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
<style scoped src="../../assets/styles/nhanvien/QLHopDong/PopTaoHopDong.css"></style>