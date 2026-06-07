<script setup>
import { ref } from "vue";
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
</script>

<template>
  <el-dialog
      v-model="visible"
      width="95%"
      top="2vh"
      class="custom-contract-dialog"
      :show-close="true"
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
              <el-form-item label="Số đơn hàng" required>
                <el-input v-model="contract.orderCode" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Số hợp đồng" required>
                <el-input v-model="contract.contractCode" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Ngày lập hợp đồng" required>
                <el-input v-model="contract.contractDate" placeholder="DD/MM/YYYY">
                  <template #suffix>
                    <el-icon><Calendar /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Nhân viên lập">
                <el-select v-model="contract.employee" style="width: 100%">
                  <el-option label="Nguyễn Văn A" value="Nguyễn Văn A" />
                </el-select>
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
                <el-input v-model="contract.customerName" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Số CCCD/CMND" required>
                <el-input v-model="contract.citizenId" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="Địa chỉ" required>
            <el-input v-model="contract.address" />
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Số điện thoại" required>
                <el-input v-model="contract.phone" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Quan hệ với người mất" required>
                <el-select v-model="contract.relationship" style="width: 100%">
                  <el-option label="Con trai" value="Con trai" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <div class="section-title mt-4">
            <img src="https://api.iconify.design/mdi:lotus.svg?color=%238a181a" alt="lotus" class="custom-svg-icon" />
            <span>3. THÔNG TIN NGƯỜI MẤT</span>
          </div>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Họ và tên" required>
                <el-input v-model="contract.deceasedName" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Ngày mất" required>
                <el-input v-model="contract.deathDate">
                  <template #suffix>
                    <el-icon><Calendar /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Tuổi">
                <el-input v-model="contract.age" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Giới tính">
                <el-select v-model="contract.gender" style="width: 100%">
                  <el-option label="Nữ" value="Nữ" />
                  <el-option label="Nam" value="Nam" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <div class="section-title mt-4">
            <el-icon><HomeFilled /></el-icon>
            <span>4. THÔNG TIN NƠI AN TÁNG</span>
          </div>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Cơ sở mai táng" required>
                <el-select v-model="contract.facility" style="width: 100%">
                  <el-option label="Công viên nghĩa trang An Yên" value="Công viên nghĩa trang An Yên" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Khu mộ" required>
                <el-select v-model="contract.cemeteryArea" style="width: 100%">
                  <el-option label="Khu A" value="Khu A" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="Số mộ" required>
                <el-input v-model="contract.graveNumber" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Ngày giờ an táng" required>
                <el-input v-model="contract.burialDatetime">
                  <template #suffix>
                    <el-icon><Calendar /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <div class="section-title mt-4 flex-between">
            <div class="title-left">
              <span>5. DỊCH VỤ SỬ DỤNG</span>
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
              <ContractPreview :contract="contract" :extraServices="services" />
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
          <el-button type="primary" class="save-btn">
            <el-icon style="margin-right: 6px;"><Document /></el-icon>
            Lưu hợp đồng
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
:deep(.custom-contract-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.custom-contract-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

:deep(.custom-contract-dialog .el-dialog__body) {
  padding: 0 !important;
}

.dialog-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.header-icon {
  margin-right: 8px;
  font-size: 20px;
}

.contract-layout {
  display: grid;
  grid-template-columns: 450px 1fr;
  height: 75vh;
}

/* LEFT FORM PANEL */
.form-panel {
  overflow-y: auto;
  padding: 20px 24px;
  border-right: 1px solid #eaeaea;
  background-color: #fff;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: bold;
  color: #8a181a;
  margin-bottom: 16px;
  text-transform: uppercase;
}

.section-title .el-icon {
  margin-right: 8px;
  font-size: 18px;
}

.custom-svg-icon {
  width: 18px;
  height: 18px;
  margin-right: 8px;
}

.mt-4 {
  margin-top: 24px;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-left {
  display: flex;
  align-items: center;
}

.add-service-btn {
  color: #8a181a;
  border-color: #8a181a;
}
.add-service-btn:hover {
  background-color: #fcf0f0;
  color: #8a181a;
  border-color: #8a181a;
}

.service-row {
  margin-bottom: 12px;
}

/* Form Overrides */
:deep(.custom-form .el-form-item__label) {
  font-weight: 500;
  color: #333;
  padding-bottom: 4px;
  line-height: 1.2;
}

:deep(.custom-form .el-form-item) {
  margin-bottom: 16px;
}

:deep(.custom-form .el-input__wrapper),
:deep(.custom-form .el-select .el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  border-radius: 6px;
}

:deep(.custom-form .el-form-item.is-required:not(.is-no-asterisk) > .el-form-item__label-wrap > .el-form-item__label:before), 
:deep(.custom-form .el-form-item.is-required:not(.is-no-asterisk) > .el-form-item__label:before) {
  color: #f56c6c;
  margin-right: 4px;
}

/* RIGHT PREVIEW PANEL */
.preview-panel {
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  overflow: hidden;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background-color: #fdfdfd;
  border-bottom: 1px solid #eaeaea;
}

.preview-title {
  font-weight: bold;
  font-size: 13px;
  color: #333;
}

.preview-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #666;
}

.action-icon {
  cursor: pointer;
  font-size: 16px;
}

.action-icon:hover {
  color: #333;
}

.zoom-text {
  font-size: 13px;
  font-weight: 500;
}

.preview-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

/* FOOTER */
:deep(.el-dialog__footer) {
  padding: 16px 20px;
  border-top: 1px solid #eee;
  background-color: #fff;
  margin-top: 0;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-left {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #606266;
}

.btn-right {
  display: flex;
  gap: 12px;
}

.cancel-btn {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #606266;
}

.save-btn {
  background-color: #8a181a;
  border-color: #8a181a;
}
.save-btn:hover {
  background-color: #a31c1e;
  border-color: #a31c1e;
}
</style>