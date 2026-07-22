<script setup>
import { ref, watch, nextTick } from "vue";
import { ElMessage } from "element-plus";
import html2canvas from "html2canvas";
import jsPDF from "jspdf";
import {
  createHopDong,
  getDonHangDetailForHopDong,
  getDonHangOptionsForHopDong,
  getNextHopDongCode
} from "../../services/hopDongService.js";
import ContractPreview from "../../pages/nhanvien/PreviewHopDong.vue";
import {
  Document,
  User,
  Avatar,
  HomeFilled,
  Calendar,
  ZoomOut,
  Minus,
  Plus,
  Printer,
  Download,
  Delete
} from "@element-plus/icons-vue";

const visible = defineModel();
const emit = defineEmits(["success", "saved"]);

const props = defineProps({
  initialMaDonHang: {
    type: Number,
    default: null
  }
});

const AUTO_CONTRACT_CODE = "Tự động sau khi lưu";

const userStr = localStorage.getItem("user");
const user = userStr ? JSON.parse(userStr) : null;

const getTodayDate = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
};

const selectedMaDonHang = ref(null);
const donHangOptions = ref([]);
const loadingOrders = ref(false);
const loadingDetail = ref(false);
const saving = ref(false);

const services = ref([]);
const orderProducts = ref([]);
const selectedDonHangDetail = ref(null);

const zoomLevel = ref(100);
const previewRef = ref(null);

const contract = ref({
  orderCode: "",
  contractCode: AUTO_CONTRACT_CODE,
  contractDate: "",
  employee: user?.hoTen || user?.tenNhanVien || "",

  deathCertificateNo: "",
  deathCertificateIssuePlace: "",
  contractStartDate: getTodayDate(),
  contractEndDate: "",

  customerName: "",
  citizenId: "",
  address: "",
  phone: "",
  relationship: "",

  deceasedName: "",
  deathDate: "",
  birthDate: "",
  gender: "",

  facility: "",
  cemeteryArea: "",
  graveNumber: "",
  burialDatetime: ""
});

const errors = ref({
  customerName: "",
  citizenId: "",
  address: "",
  phone: "",
  relationship: "",

  deathCertificateNo: "",
  deathCertificateIssuePlace: "",
  contractStartDate: "",
  contractEndDate: "",

  deceasedName: "",
  deathDate: "",
  birthDate: "",
  gender: "",
  facility: "",
  cemeteryArea: "",
  graveNumber: "",
  burialDatetime: ""
});

const MAX_LENGTHS = {
  customerName: 30,
  citizenId: 12,
  address: 40,
  phone: 10,
  relationship: 30,
  deceasedName: 30,
  deathCertificateNo: 10,
  deathCertificateIssuePlace: 30,
  facility: 30,
  cemeteryArea: 30,
  graveNumber: 20,
  serviceName: 15
};

const clearErrors = () => {
  Object.keys(errors.value).forEach((key) => {
    errors.value[key] = "";
  });
};

const clearFieldError = (fieldName) => {
  errors.value[fieldName] = "";
};

const trimContractTextFields = () => {
  Object.keys(contract.value).forEach((key) => {
    if (typeof contract.value[key] === "string") {
      contract.value[key] = contract.value[key].trim();
    }
  });

  services.value.forEach((item) => {
    if (typeof item.name === "string") {
      item.name = item.name.trim();
    }
  });
};

const parseDateValue = (value) => {
  if (!value) return null;

  const date = new Date(String(value).replace(" ", "T"));

  return Number.isNaN(date.getTime()) ? null : date;
};

const checkMaxLength = (field, label, maxLength) => {
  const value = contract.value[field] || "";

  if (value.length > maxLength) {
    errors.value[field] = `${label} tối đa ${maxLength} ký tự`;
    return false;
  }

  return true;
};

const onlyDigits = (value = "") => {
  return String(value).replace(/\D/g, "");
};

const normalizeVietnamPhone = (value = "") => {
  let phone = String(value).trim().replace(/\s+/g, "");

  if (phone.startsWith("+84")) {
    phone = "0" + phone.slice(3);
  }

  phone = phone.replace(/\D/g, "");

  if (phone.startsWith("84") && phone.length === 11) {
    phone = "0" + phone.slice(2);
  }

  return phone;
};

const isValidCCCD = (value = "") => {
  return /^[0-9]{12}$/.test(String(value).trim());
};

const isValidVietnamPhone = (value = "") => {
  const phone = normalizeVietnamPhone(value);

  return /^0(3|5|7|8|9)[0-9]{8}$/.test(phone);
};

const isValidDeathCertificateNo = (value = "") => {
  return /^[0-9]{10}$/.test(String(value).trim());
};

const limitCCCDInput = (fieldName = "citizenId") => {
  contract.value[fieldName] = onlyDigits(contract.value[fieldName]).slice(
      0,
      MAX_LENGTHS.citizenId
  );
};

const limitPhoneInput = (fieldName = "phone") => {
  contract.value[fieldName] = normalizeVietnamPhone(contract.value[fieldName]).slice(
      0,
      MAX_LENGTHS.phone
  );
};

const limitDeathCertificateNoInput = () => {
  contract.value.deathCertificateNo = onlyDigits(
      contract.value.deathCertificateNo
  ).slice(0, MAX_LENGTHS.deathCertificateNo);
};

const resetContractForm = () => {
  contract.value = {
    orderCode: "",
    contractCode: AUTO_CONTRACT_CODE,
    contractDate: "",
    employee: user?.hoTen || user?.tenNhanVien || "",

    deathCertificateNo: "",
    deathCertificateIssuePlace: "",
    contractStartDate: getTodayDate(),
    contractEndDate: "",

    customerName: "",
    citizenId: "",
    address: "",
    phone: "",
    relationship: "",

    deceasedName: "",
    deathDate: "",
    birthDate: "",
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

const addService = () => {
  services.value.push({
    name: "",
    price: 0
  });
};

const removeService = (index) => {
  services.value.splice(index, 1);
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

const formatMoney = (value) => {
  return Number(value || 0).toLocaleString("vi-VN") + " đ";
};

const validateForm = () => {
  clearErrors();
  trimContractTextFields();

  let isValid = true;
  const now = new Date();

  const required = (field, message) => {
    if (!contract.value[field]) {
      errors.value[field] = message;
      isValid = false;
      return false;
    }

    return true;
  };

  const max = (field, label, maxLength) => {
    const ok = checkMaxLength(field, label, maxLength);

    if (!ok) {
      isValid = false;
    }

    return ok;
  };

  if (required("customerName", "Vui lòng nhập họ tên tang chủ")) {
    if (contract.value.customerName.length < 2) {
      errors.value.customerName = "Họ tên quá ngắn";
      isValid = false;
    } else {
      max("customerName", "Họ tên tang chủ", MAX_LENGTHS.customerName);
    }
  }

  contract.value.citizenId = onlyDigits(contract.value.citizenId).slice(
      0,
      MAX_LENGTHS.citizenId
  );

  if (required("citizenId", "Vui lòng nhập CCCD")) {
    if (!isValidCCCD(contract.value.citizenId)) {
      errors.value.citizenId = "CCCD phải gồm đúng 12 chữ số";
      isValid = false;
    }
  }

  if (required("address", "Vui lòng nhập địa chỉ")) {
    max("address", "Địa chỉ", MAX_LENGTHS.address);
  }

  contract.value.phone = normalizeVietnamPhone(contract.value.phone).slice(
      0,
      MAX_LENGTHS.phone
  );

  if (required("phone", "Vui lòng nhập số điện thoại")) {
    if (!isValidVietnamPhone(contract.value.phone)) {
      errors.value.phone = "Số điện thoại Việt Nam không hợp lệ. Ví dụ: 0901234567";
      isValid = false;
    }
  }

  if (required("relationship", "Vui lòng chọn quan hệ")) {
    max("relationship", "Quan hệ", MAX_LENGTHS.relationship);
  }

  if (required("deceasedName", "Vui lòng nhập tên người mất")) {
    max("deceasedName", "Tên người mất", MAX_LENGTHS.deceasedName);
  }

  let deathDateValue = null;

  if (!contract.value.deathDate) {
    errors.value.deathDate = "Vui lòng chọn ngày mất";
    isValid = false;
  } else {
    deathDateValue = parseDateValue(contract.value.deathDate);

    if (!deathDateValue) {
      errors.value.deathDate = "Ngày mất không hợp lệ";
      isValid = false;
    } else if (deathDateValue > now) {
      errors.value.deathDate = "Ngày mất không được lớn hơn hiện tại";
      isValid = false;
    }
  }

  if (!contract.value.birthDate) {
    errors.value.birthDate = "Vui lòng chọn ngày sinh";
    isValid = false;
  } else {
    const birthDateValue = parseDateValue(contract.value.birthDate);

    if (!birthDateValue) {
      errors.value.birthDate = "Ngày sinh không hợp lệ";
      isValid = false;
    } else if (birthDateValue > now) {
      errors.value.birthDate = "Ngày sinh không được lớn hơn hiện tại";
      isValid = false;
    } else if (deathDateValue && birthDateValue > deathDateValue) {
      errors.value.birthDate = "Ngày sinh không được lớn hơn ngày mất";
      isValid = false;
    }
  }

  required("gender", "Vui lòng chọn giới tính");

  contract.value.deathCertificateNo = onlyDigits(
      contract.value.deathCertificateNo
  ).slice(0, MAX_LENGTHS.deathCertificateNo);

  if (required("deathCertificateNo", "Vui lòng nhập số giấy báo tử")) {
    if (!isValidDeathCertificateNo(contract.value.deathCertificateNo)) {
      errors.value.deathCertificateNo = "Số giấy báo tử phải gồm đúng 10 số";
      isValid = false;
    }
  }

  if (required("deathCertificateIssuePlace", "Vui lòng nhập nơi cấp giấy báo tử")) {
    max(
        "deathCertificateIssuePlace",
        "Nơi cấp giấy báo tử",
        MAX_LENGTHS.deathCertificateIssuePlace
    );
  }

  if (!contract.value.contractStartDate) {
    errors.value.contractStartDate = "Không lấy được ngày bắt đầu";
    isValid = false;
  }

  if (!contract.value.contractEndDate) {
    errors.value.contractEndDate = "Vui lòng chọn ngày kết thúc hợp đồng";
    isValid = false;
  } else {
    const startDate = parseDateValue(contract.value.contractStartDate);
    const endDate = parseDateValue(contract.value.contractEndDate);

    if (!endDate) {
      errors.value.contractEndDate = "Ngày kết thúc không hợp lệ";
      isValid = false;
    } else if (startDate && endDate < startDate) {
      errors.value.contractEndDate = "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu";
      isValid = false;
    }
  }

  if (required("facility", "Vui lòng nhập cơ sở mai táng")) {
    max("facility", "Cơ sở mai táng", MAX_LENGTHS.facility);
  }

  if (required("cemeteryArea", "Vui lòng nhập khu mộ")) {
    max("cemeteryArea", "Khu mộ", MAX_LENGTHS.cemeteryArea);
  }

  if (required("graveNumber", "Vui lòng nhập số mộ")) {
    max("graveNumber", "Số mộ", MAX_LENGTHS.graveNumber);
  }

  if (!contract.value.burialDatetime) {
    errors.value.burialDatetime = "Vui lòng chọn ngày giờ an táng";
    isValid = false;
  } else {
    const burialDate = parseDateValue(contract.value.burialDatetime);

    if (!burialDate) {
      errors.value.burialDatetime = "Ngày giờ an táng không hợp lệ";
      isValid = false;
    } else if (deathDateValue && burialDate < deathDateValue) {
      errors.value.burialDatetime = "Ngày an táng phải sau ngày mất";
      isValid = false;
    }
  }

  services.value.forEach((item, index) => {
    const serviceName = item.name || "";

    if (serviceName && serviceName.length > MAX_LENGTHS.serviceName) {
      ElMessage.warning(
          `Tên dịch vụ phụ thu dòng ${index + 1} tối đa ${MAX_LENGTHS.serviceName} ký tự`
      );
      isValid = false;
    }

    if (serviceName && (!item.price || Number(item.price) <= 0)) {
      ElMessage.warning(`Giá dịch vụ phụ thu dòng ${index + 1} phải lớn hơn 0`);
      isValid = false;
    }
  });

  return isValid;
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

    contract.value.contractStartDate = getTodayDate();
    contract.value.contractEndDate = "";
    await loadNextHopDongCode();

    const now = new Date();

    contract.value.contractDate = now.toLocaleString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    });

    contract.value.employee = data.tenNhanVien || user?.hoTen || user?.tenNhanVien || "";
    contract.value.customerName = data.tenKhachHang || "";
    contract.value.citizenId = onlyDigits(data.cccd || "").slice(
        0,
        MAX_LENGTHS.citizenId
    );
    contract.value.address = data.diaChi || "";
    contract.value.phone = normalizeVietnamPhone(data.soDienThoai || "").slice(
        0,
        MAX_LENGTHS.phone
    );
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
  if (!selectedMaDonHang.value) {
    ElMessage.error("Vui lòng chọn đơn hàng");
    return;
  }

  const valid = validateForm();

  if (!valid) {
    ElMessage.error("Vui lòng kiểm tra lại thông tin hợp đồng");
    return;
  }

  try {
    saving.value = true;

    const today = new Date().toISOString().slice(0, 10);

    // Chuyển đổi giới tính từ string sang boolean
    const genderValue = contract.value.gender === "Nam" ? true : 
                       contract.value.gender === "Nữ" ? false : null;

    const saved = await createHopDong({
      maDonHang: Number(selectedMaDonHang.value),

      ngayKyHD: contract.value.contractStartDate || today,
      ngayViet: today,
      ngayKetThuc: contract.value.contractEndDate || null,
      thoiHanKetThuc: contract.value.contractEndDate || null,

      trangThai: 1, // 1 = Chờ ký

      hoTenNguoiMat: contract.value.deceasedName || null,
      ngayMat: contract.value.deathDate || null,
      ngaySinh: contract.value.birthDate || null,
      gioiTinh: genderValue,

      soGiayBaoTu: contract.value.deathCertificateNo || null,
      noiCapGiayBaoTu: contract.value.deathCertificateIssuePlace || null,

      coSoMaiTang: contract.value.facility || null,
      khuMo: contract.value.cemeteryArea || null,
      soMo: contract.value.graveNumber || null,

      ngayGioAnTang: contract.value.burialDatetime || null
    });

    contract.value.contractCode =
        saved?.soHopDong ||
        saved?.maHopDongText ||
        `HD${String(saved?.maHopDong || "").padStart(7, "0")}`;

    ElMessage.success("Lưu hợp đồng thành công");

    emit("success", saved);
    emit("saved", saved);

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

  const styles = Array.from(
      document.querySelectorAll('style, link[rel="stylesheet"]')
  )
      .map((style) => style.outerHTML)
      .join("");

  const printWindow = window.open("", "", "width=800,height=900");

  if (!printWindow) {
    ElMessage.error("Trình duyệt đã chặn cửa sổ in");
    return;
  }

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

            input,
            .dotted-input {
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
        <\/script>
      </body>
    </html>
  `);

  printWindow.document.close();
};

watch(selectedMaDonHang, (value) => {
  if (!value) {
    resetContractForm();
  }
});

watch(
    () => visible.value,
    async (isOpen) => {
      if (!isOpen) return;

      selectedMaDonHang.value = null;
      selectedDonHangDetail.value = null;
      orderProducts.value = [];
      services.value = [];

      resetContractForm();

      await loadDonHangOptions();

      console.log("DON HANG OPTIONS:", donHangOptions.value);

      // Nếu có initialMaDonHang từ thông báo, tự động chọn đơn hàng
      if (props.initialMaDonHang) {
        selectedMaDonHang.value = props.initialMaDonHang;
        await onSelectDonHang(props.initialMaDonHang);
      }
    },
    {
      immediate: true
    }
);

const loadNextHopDongCode = async () => {
  try {
    const data = await getNextHopDongCode();

    contract.value.contractCode =
        data?.soHopDong ||
        data?.maHopDongText ||
        AUTO_CONTRACT_CODE;
  } catch (error) {
    console.error("Lỗi lấy số hợp đồng kế tiếp:", error);
    contract.value.contractCode = AUTO_CONTRACT_CODE;
  }
};
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
        <el-icon class="header-icon">
          <Document />
        </el-icon>
        <span>Tạo hợp đồng mai táng</span>
      </div>
    </template>

    <div class="contract-layout">
      <div class="form-panel">
        <el-form label-position="top" :model="contract" class="custom-form">
          <div class="section-title">
            <el-icon>
              <Document />
            </el-icon>
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
                <el-input
                    v-model="contract.contractCode"
                    disabled
                    placeholder="Số hợp đồng tự động"
                />
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
            <el-icon>
              <User />
            </el-icon>
            <span>2. THÔNG TIN TANG CHỦ</span>
          </div>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Họ và tên" required>
                <div :class="{ 'is-invalid': errors.customerName }">
                  <el-input
                      v-model="contract.customerName"
                      :maxlength="MAX_LENGTHS.customerName"
                      show-word-limit
                      placeholder="Nhập họ và tên tang chủ"
                      @input="clearFieldError('customerName')"
                  />
                </div>

                <div v-if="errors.customerName" class="error-text">
                  {{ errors.customerName }}
                </div>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="Số CCCD/CMND" required>
                <div :class="{ 'is-invalid': errors.citizenId }">
                  <el-input
                      v-model="contract.citizenId"
                      :maxlength="MAX_LENGTHS.citizenId"
                      inputmode="numeric"
                      placeholder="Nhập CCCD 12 số"
                      @input="() => { limitCCCDInput('citizenId'); clearFieldError('citizenId'); }"
                  />
                </div>

                <div v-if="errors.citizenId" class="error-text">
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
                    :maxlength="MAX_LENGTHS.address"
                    show-word-limit
                    placeholder="Nhập địa chỉ tang chủ"
                    @input="clearFieldError('address')"
                />
              </div>

              <div v-if="errors.address" class="error-text">
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
                      :maxlength="MAX_LENGTHS.phone"
                      inputmode="tel"
                      placeholder="VD: 0901234567"
                      @input="() => { limitPhoneInput('phone'); clearFieldError('phone'); }"
                  />
                </div>

                <div v-if="errors.phone" class="error-text">
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

                  <div v-if="errors.relationship" class="error-text">
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
            <el-table-column type="index" label="STT" width="60" />

            <el-table-column prop="name" label="Tên sản phẩm / dịch vụ" />

            <el-table-column prop="loai" label="Loại" width="160" />

            <el-table-column
                prop="quantity"
                label="Số lượng"
                width="100"
                align="center"
            />

            <el-table-column label="Đơn giá" width="150" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.price) }}
              </template>
            </el-table-column>

            <el-table-column label="Thành tiền" width="160" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.thanhTien || row.price * row.quantity) }}
              </template>
            </el-table-column>
          </el-table>

          <div class="section-title mt-4">
            <el-icon>
              <Avatar />
            </el-icon>
            <span>4. THÔNG TIN NGƯỜI MẤT</span>
          </div>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Họ và tên" required>
                <div class="field-wrapper">
                  <div :class="{ 'is-invalid': errors.deceasedName }">
                    <el-input
                        v-model="contract.deceasedName"
                        :maxlength="MAX_LENGTHS.deceasedName"
                        show-word-limit
                        placeholder="Nhập họ tên người mất"
                        @input="clearFieldError('deceasedName')"
                    />
                  </div>

                  <div v-if="errors.deceasedName" class="error-text">
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

                  <div v-if="errors.deathDate" class="error-text">
                    {{ errors.deathDate }}
                  </div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Ngày sinh" required>
                <div class="field-wrapper">
                  <div :class="{ 'is-invalid': errors.birthDate }">
                    <el-date-picker
                        v-model="contract.birthDate"
                        type="datetime"
                        placeholder="Nhập ngày sinh"
                        format="DD/MM/YYYY HH:mm"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%"
                        :teleported="false"
                        placement="top-start"
                        @change="errors.birthDate = ''"
                    />
                  </div>

                  <div v-if="errors.birthDate" class="error-text">
                    {{ errors.birthDate }}
                  </div>
                </div>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="Giới tính" required>
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

                  <div v-if="errors.gender" class="error-text">
                    {{ errors.gender }}
                  </div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <div class="section-title mt-4">
            <el-icon>
              <Calendar />
            </el-icon>
            <span>5. THÔNG TIN GIẤY BÁO TỬ VÀ THỜI GIAN HỢP ĐỒNG</span>
          </div>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Số giấy báo tử" required>
                <div class="field-wrapper">
                  <div :class="{ 'is-invalid': errors.deathCertificateNo }">
                    <el-input
                        v-model="contract.deathCertificateNo"
                        :maxlength="MAX_LENGTHS.deathCertificateNo"
                        inputmode="numeric"
                        placeholder="Nhập số giấy báo tử 10 số"
                        @input="() => { limitDeathCertificateNoInput(); clearFieldError('deathCertificateNo'); }"
                    />
                  </div>

                  <div v-if="errors.deathCertificateNo" class="error-text">
                    {{ errors.deathCertificateNo }}
                  </div>
                </div>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="Nơi cấp giấy báo tử" required>
                <div class="field-wrapper">
                  <div :class="{ 'is-invalid': errors.deathCertificateIssuePlace }">
                    <el-input
                        v-model="contract.deathCertificateIssuePlace"
                        :maxlength="MAX_LENGTHS.deathCertificateIssuePlace"
                        show-word-limit
                        placeholder="Nhập nơi cấp giấy báo tử"
                        @input="clearFieldError('deathCertificateIssuePlace')"
                    />
                  </div>

                  <div v-if="errors.deathCertificateIssuePlace" class="error-text">
                    {{ errors.deathCertificateIssuePlace }}
                  </div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Ngày bắt đầu hợp đồng" required>
                <el-date-picker
                    v-model="contract.contractStartDate"
                    type="date"
                    format="DD/MM/YYYY"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                    disabled
                    :teleported="false"
                />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="Ngày kết thúc hợp đồng" required>
                <div class="field-wrapper">
                  <div :class="{ 'is-invalid': errors.contractEndDate }">
                    <el-date-picker
                        v-model="contract.contractEndDate"
                        type="date"
                        placeholder="Chọn ngày kết thúc"
                        format="DD/MM/YYYY"
                        value-format="YYYY-MM-DD"
                        style="width: 100%"
                        :teleported="false"
                        placement="top-start"
                        @change="errors.contractEndDate = ''"
                    />
                  </div>

                  <div v-if="errors.contractEndDate" class="error-text">
                    {{ errors.contractEndDate }}
                  </div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <div class="section-title mt-4">
            <el-icon>
              <HomeFilled />
            </el-icon>
            <span>6. THÔNG TIN NƠI AN TÁNG</span>
          </div>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Cơ sở mai táng" required>
                <div class="field-wrapper">
                  <div :class="{ 'is-invalid': errors.facility }">
                    <el-input
                        v-model="contract.facility"
                        :maxlength="MAX_LENGTHS.facility"
                        show-word-limit
                        placeholder="Nhập cơ sở mai táng"
                        @input="clearFieldError('facility')"
                    />
                  </div>

                  <div v-if="errors.facility" class="error-text">
                    {{ errors.facility }}
                  </div>
                </div>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="Khu mộ" required>
                <div class="field-wrapper">
                  <div :class="{ 'is-invalid': errors.cemeteryArea }">
                    <el-input
                        v-model="contract.cemeteryArea"
                        :maxlength="MAX_LENGTHS.cemeteryArea"
                        show-word-limit
                        placeholder="Nhập khu mộ"
                        @input="clearFieldError('cemeteryArea')"
                    />
                  </div>

                  <div v-if="errors.cemeteryArea" class="error-text">
                    {{ errors.cemeteryArea }}
                  </div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Số mộ" required>
                <div class="field-wrapper">
                  <div :class="{ 'is-invalid': errors.graveNumber }">
                    <el-input
                        v-model="contract.graveNumber"
                        :maxlength="MAX_LENGTHS.graveNumber"
                        show-word-limit
                        placeholder="Nhập số mộ"
                        @input="clearFieldError('graveNumber')"
                    />
                  </div>

                  <div v-if="errors.graveNumber" class="error-text">
                    {{ errors.graveNumber }}
                  </div>
                </div>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="Ngày giờ an táng" required>
                <div class="field-wrapper">
                  <div :class="{ 'is-invalid': errors.burialDatetime }">
                    <el-date-picker
                        v-model="contract.burialDatetime"
                        type="datetime"
                        placeholder="Chọn ngày giờ an táng"
                        format="DD/MM/YYYY HH:mm"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%"
                        :teleported="false"
                        placement="top-start"
                        @change="errors.burialDatetime = ''"
                    />
                  </div>

                  <div v-if="errors.burialDatetime" class="error-text">
                    {{ errors.burialDatetime }}
                  </div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <div class="section-title mt-4 flex-between">
            <div class="title-left">
              <span>7. DỊCH VỤ SỬ DỤNG</span>
            </div>

            <el-button size="small" plain class="add-service-btn" @click="addService">
              + Thêm dịch vụ
            </el-button>
          </div>

          <div
              v-for="(service, index) in services"
              :key="index"
              class="service-row"
          >
            <el-row :gutter="10" align="middle">
              <el-col :span="13">
                <el-input
                    v-model="service.name"
                    :maxlength="MAX_LENGTHS.serviceName"
                    show-word-limit
                    placeholder="Tên dịch vụ/phụ thu"
                />
              </el-col>

              <el-col :span="8">
                <el-input
                    v-model="service.price"
                    placeholder="Giá tiền"
                    type="number"
                />
              </el-col>

              <el-col :span="3" style="text-align: right">
                <el-button
                    type="danger"
                    :icon="Delete"
                    circle
                    size="small"
                    @click="removeService(index)"
                    plain
                />
              </el-col>
            </el-row>
          </div>
        </el-form>
      </div>

      <div class="preview-panel">
        <div class="preview-header">
          <div class="preview-title">XEM TRƯỚC HỢP ĐỒNG</div>

          <div class="preview-actions">
            <el-icon class="action-icon" @click="resetZoom">
              <ZoomOut />
            </el-icon>

            <el-icon class="action-icon" @click="zoomOut">
              <Minus />
            </el-icon>

            <span class="zoom-text">{{ zoomLevel }}%</span>

            <el-icon class="action-icon" @click="zoomIn">
              <Plus />
            </el-icon>

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
          <div
              :style="{
              transform: `scale(${zoomLevel / 100})`,
              transformOrigin: 'top center',
              transition: 'transform 0.2s'
            }"
          >
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
          <el-button @click="visible = false" class="cancel-btn">
            Hủy
          </el-button>

          <el-button
              type="primary"
              class="save-btn"
              :loading="saving"
              @click="saveContract"
          >
            <el-icon style="margin-right: 6px">
              <Document />
            </el-icon>
            Lưu hợp đồng
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped src="../../assets/styles/nhanvien/QLHopDong/PopTaoHopDong.css"></style>