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
import ContractPreview from "../../pages/nhanvien/PreviewHopDong.vue";
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

const resetContractForm = () => {
  contract.value = {
    orderCode: "",
    contractCode: "",
    contractDate: "",
    employee: user?.hoTen || user?.tenNhanVien || "",

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
  };

  orderProducts.value = [];
  selectedDonHangDetail.value = null;
  clearErrors();
};


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
const emit = defineEmits(["success"]);

const selectedMaDonHang = ref(null);
const donHangOptions = ref([]);
const loadingOrders = ref(false);
watch(selectedMaDonHang, (value) => {
  if (!value) {
    resetContractForm();
  }
});
const loadingDetail = ref(false);
const saving = ref(false);
const errors = ref({
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
const clearErrors = () => {
  Object.keys(errors.value).forEach(key => {
    errors.value[key] = "";
  });
};
const validateForm = () => {
  clearErrors();

  let isValid = true;

  const now = new Date();

  // Tang chủ
  if (!contract.value.customerName?.trim()) {
    errors.value.customerName = "Vui lòng nhập họ tên tang chủ";
    isValid = false;
  } else if (contract.value.customerName.trim().length < 2) {
    errors.value.customerName = "Họ tên quá ngắn";
    isValid = false;
  }

  // CCCD
  if (!contract.value.citizenId?.trim()) {
    errors.value.citizenId = "Vui lòng nhập CCCD";
    isValid = false;
  } else if (!/^[0-9]{12}$/.test(contract.value.citizenId.trim())) {
    errors.value.citizenId = "CCCD phải gồm 12 chữ số";
    isValid = false;
  }

  // Địa chỉ
  if (!contract.value.address?.trim()) {
    errors.value.address = "Vui lòng nhập địa chỉ";
    isValid = false;
  }

  // SĐT
  if (!contract.value.phone?.trim()) {
    errors.value.phone = "Vui lòng nhập số điện thoại";
    isValid = false;
  } else if (
      !/^(0|\+84)[3|5|7|8|9][0-9]{8}$/.test(
          contract.value.phone.trim()
      )
  ) {
    errors.value.phone = "Số điện thoại không hợp lệ";
    isValid = false;
  }

  // Quan hệ
  if (!contract.value.relationship) {
    errors.value.relationship = "Vui lòng chọn quan hệ";
    isValid = false;
  }

  // Người mất
  if (!contract.value.deceasedName?.trim()) {
    errors.value.deceasedName = "Vui lòng nhập tên người mất";
    isValid = false;
  }

  if (!contract.value.deathDate) {
    errors.value.deathDate = "Vui lòng chọn ngày mất";
    isValid = false;
  } else {
    const deathDate = new Date(contract.value.deathDate);

    if (deathDate > now) {
      errors.value.deathDate = "Ngày mất không được lớn hơn hiện tại";
      isValid = false;
    }
  }

  // Tuổi
  if (!contract.value.age) {
    errors.value.age = "Vui lòng nhập tuổi";
    isValid = false;
  } else {
    const age = Number(contract.value.age);

    if (isNaN(age)) {
      errors.value.age = "Tuổi không hợp lệ";
      isValid = false;
    } else if (age < 0) {
      errors.value.age = "Tuổi không được âm";
      isValid = false;
    } else if (age > 150) {
      errors.value.age = "Tuổi không hợp lệ";
      isValid = false;
    }
  }

  // Giới tính
  if (!contract.value.gender) {
    errors.value.gender = "Vui lòng chọn giới tính";
    isValid = false;
  }

  // Ngày an táng
  if (!contract.value.burialDatetime) {
    errors.value.burialDatetime = "Vui lòng chọn ngày giờ an táng";
    isValid = false;
  } else if (contract.value.deathDate) {
    const deathDate = new Date(contract.value.deathDate);
    const burialDate = new Date(contract.value.burialDatetime);

    if (burialDate < deathDate) {
      errors.value.burialDatetime =
          "Ngày an táng phải sau ngày mất";
      isValid = false;
    }
  }

  // Dịch vụ phụ thu
  services.value.forEach((item) => {
    if (
        item.name?.trim() &&
        (!item.price || Number(item.price) <= 0)
    ) {
      isValid = false;
    }
  });

  return isValid;
};
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
    const now = new Date();

    contract.value.contractDate =
        now.toLocaleString("vi-VN", {
          day: "2-digit",
          month: "2-digit",
          year: "numeric",
          hour: "2-digit",
          minute: "2-digit",
        });
    contract.value.employee = data.tenNhanVien || "";

    contract.value.customerName = data.tenKhachHang || "";
    contract.value.citizenId = data.cccd || "";
    contract.value.address = data.diaChi || "";
    contract.value.phone = data.soDienThoai || "";
    contract.value.facility = "Không";
    contract.value.cemeteryArea = "Không";
    contract.value.graveNumber = "Không";

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

  const valid = validateForm();

  console.log(errors.value);

  if (!valid) {
    ElMessage.error("Form đang có lỗi");
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
      :z-index="10000"
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
                    :teleported="false"
                    placeholder="Chọn mã đơn hàng"
                    filterable
                    clearable
                    @clear="resetContractForm"
                    @change="onSelectDonHang"
                    style="width: 100%"
                    :loading="loadingOrders"
                >
                  <el-option
                      v-for="item in donHangOptions"
                      :key="item.maDonHang"
                      :label="`${item.maDonHangText}${item.tenKhachHang ? ' - ' + item.tenKhachHang : ''}${item.daCoHopDong ? ' - Đã có hợp đồng' : ''}`"
                      :value="item.maDonHang"
                      :disabled="item.daCoHopDong"
                  />
                </el-select>
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
                <el-input
                    v-model="contract.contractDate"
                    disabled
                    placeholder="Ngày lập hợp đồng"
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

                <div :class="{ 'is-invalid': errors.customerName }">
                  <el-input
                      v-model="contract.customerName"
                      placeholder="Nhập họ và tên tang chủ"
                      @input="errors.customerName = ''"
                  />
                </div>

                <div
                    v-if="errors.customerName"
                    class="error-text"
                >
                  {{ errors.customerName }}
                </div>

              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Số CCCD/CMND" required>

                <div :class="{ 'is-invalid': errors.citizenId }">
                  <el-input
                      v-model="contract.citizenId"
                      placeholder="Nhập số CCCD/CMND"
                      @input="errors.citizenId = ''"
                  />
                </div>

                <div
                    v-if="errors.citizenId"
                    class="error-text"
                >
                  {{ errors.citizenId }}
                </div>

              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="Địa chỉ" required>

            <div class="field-wrapper">

              <div :class="{ 'is-invalid': errors.address }">
                <el-input
                    v-model="contract.address"
                    placeholder="Nhập địa chỉ tang chủ"
                    @input="errors.address = ''"
                />
              </div>

              <div
                  v-if="errors.address"
                  class="error-text"
              >
                {{ errors.address }}
              </div>

            </div>

          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Số điện thoại" required>

                <div :class="{ 'is-invalid': errors.phone }">
                  <el-input
                      v-model="contract.phone"
                      placeholder="Nhập số điện thoại tang chủ"
                      @input="errors.phone = ''"
                  />
                </div>

                <div
                    v-if="errors.phone"
                    class="error-text"
                >
                  {{ errors.phone }}
                </div>

              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Quan hệ với người mất" required>

                <div class="field-wrapper">

                  <div :class="{ 'is-invalid': errors.relationship }">
                    <el-select
                        v-model="contract.relationship"
                        :teleported="false"
                        placeholder="Chọn quan hệ"
                        style="width: 100%"
                        @change="errors.relationship = ''"
                    >
                      <el-option label="Con trai" value="Con trai" />
                      <el-option label="Con gái" value="Con gái" />
                      <el-option label="Chồng" value="Chồng" />
                      <el-option label="Vợ" value="Vợ" />
                      <el-option label="Khác" value="Khác" />
                    </el-select>
                  </div>

                  <div
                      v-if="errors.relationship"
                      class="error-text"
                  >
                    {{ errors.relationship }}
                  </div>

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

                <div class="field-wrapper">

                  <div :class="{ 'is-invalid': errors.deceasedName }">
                    <el-input
                        v-model="contract.deceasedName"
                        placeholder="Nhập họ tên người mất"
                        @input="errors.deceasedName = ''"
                    />
                  </div>

                  <div
                      v-if="errors.deceasedName"
                      class="error-text"
                  >
                    {{ errors.deceasedName }}
                  </div>

                </div>

              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Ngày mất" required>

                <div class="field-wrapper">

                  <div :class="{ 'is-invalid': errors.deathDate }">
                    <el-date-picker
                        v-model="contract.deathDate"
                        type="datetime"
                        placeholder="Nhập ngày mất"
                        format="DD/MM/YYYY HH:mm"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%"
                        :teleported="false"
                        placement="top-start"
                        @change="errors.deathDate = ''"
                    />
                  </div>

                  <div
                      v-if="errors.deathDate"
                      class="error-text"
                  >
                    {{ errors.deathDate }}
                  </div>

                </div>

              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Tuổi">

                <div class="field-wrapper">

                  <div :class="{ 'is-invalid': errors.age }">
                    <el-input
                        v-model="contract.age"
                        placeholder="Nhập tuổi"
                        @input="errors.age = ''"
                    />
                  </div>

                  <div
                      v-if="errors.age"
                      class="error-text"
                  >
                    {{ errors.age }}
                  </div>

                </div>

              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Giới tính">

                <div class="field-wrapper">

                  <div :class="{ 'is-invalid': errors.gender }">
                    <el-select
                        v-model="contract.gender"
                        :teleported="false"
                        placeholder="Chọn giới tính"
                        style="width: 100%"
                        @change="errors.gender = ''"
                    >
                      <el-option label="Nữ" value="Nữ" />
                      <el-option label="Nam" value="Nam" />
                    </el-select>
                  </div>

                  <div
                      v-if="errors.gender"
                      class="error-text"
                  >
                    {{ errors.gender }}
                  </div>

                </div>

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
                <el-input
                    v-model="contract.facility"
                    placeholder="Nhập cơ sở mai táng"
                />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="Khu mộ" required>
                <el-input
                    v-model="contract.cemeteryArea"
                    placeholder="Nhập khu mộ"
                />
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