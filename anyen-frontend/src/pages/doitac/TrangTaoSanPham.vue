<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import api from "../../api/api.js";

const router = useRouter();
const API_URL = "/api/doi-tac/san-pham";

const showAdvanced = ref(true);
const isSubmitting = ref(false);
const isDragOver = ref(false);
const colorInputRef = ref(null);
const errors = ref({});

const clearError = (field) => {
  if (errors.value[field]) {
    delete errors.value[field];
  }
};

const product = ref({
  maSanPham: "",
  tenSanPham: "",
  loai: "",
  noiThat: "",
  quyCach: "",
  tonGiao: "",
  giaTien: "",
  maDoiTac: "",
  soLuong: "",
  thietKe: "",
  xuatXu: "",
  ghiChu: "",
  khuyenMai: "",
  khuyenMaiLoai: "PHAN_TRAM",
  mauSac: "",
  vatLieu: "",
  trangThai: 1,
  kichThuoc: "",
  trongLuong: "",
  cnsx: "",
  ngayTao: "",
});

const imageFiles = ref([]);
const imagePreviews = ref([]);
const maxImages = 10;

const defaultSwatches = [
  { label: "Nâu đậm", value: "#3e2723" },
  { label: "Nâu sáng", value: "#8d6e63" },
  { label: "Nâu vàng", value: "#a1887f" },
  { label: "Đen", value: "#1f2937" },
];
const customSwatches = ref([]);

const detailBlocks = ref([
  { id: Date.now(), type: 'title', content: '' }
]);

const loaiOptions = [
  "Quan tài",
  "Bình tro cốt",
  "Tiểu quách",
  "Hoa tang lễ",
  "Vải liệm",
  "Phụ kiện",
  "Khác",
];

const tonGiaoOptions = [
  "Phật giáo",
  "Công giáo",
  "Tin lành",
  "Cao Đài",
  "Hòa Hảo",
  "Không yêu cầu",
];

const vatLieuOptions = [
  "Gỗ Vàng Tâm",
  "Gỗ Dổi",
  "Gỗ Gụ",
  "Gỗ Pơ Mu",
  "Gỗ Sồi",
  "Gỗ Thông",
  "Inox",
  "Đá",
  "Khác",
];

const allSwatches = computed(() => [...defaultSwatches, ...customSwatches.value]);

const tenSanPhamCount = computed(() => product.value.tenSanPham.length);
const ghiChuCount = computed(() => product.value.ghiChu.length);

onMounted(() => {
  const timestamp = Date.now().toString(36).toUpperCase();
  product.value.maSanPham = `SP-${timestamp}`;
  product.value.ngayTao = new Date().toISOString().slice(0, 16);

  const partnerId = localStorage.getItem("id");
  if (partnerId) {
    product.value.maDoiTac = partnerId;
  }

  // Pre-fill with dummy data for testing (except images)
  product.value.tenSanPham = "Quan tài gỗ gụ cao cấp";
  product.value.loai = "Quan tài";
  product.value.noiThat = "Lót nhung đỏ";
  product.value.quyCach = "Trọn bộ";
  product.value.tonGiao = "Phật giáo";
  product.value.giaTien = "15000000";
  product.value.soLuong = "10";
  product.value.thietKe = "Truyền thống";
  product.value.xuatXu = "Việt Nam";
  product.value.ghiChu = "Sản phẩm được làm từ gỗ gụ tự nhiên, độ bền cao, phù hợp cho các lễ nghi tang lễ truyền thống.";
  product.value.khuyenMai = "";
  product.value.mauSac = "Nâu đậm";
  product.value.vatLieu = "Gỗ Gụ";
  product.value.trangThai = 1;
  product.value.kichThuoc = "200 x 60 x 50 cm";
  product.value.trongLuong = "80";
  product.value.cnsx = "Thủ công truyền thống";
});

const toggleAdvanced = () => {
  showAdvanced.value = !showAdvanced.value;
};

const selectColor = (swatch) => {
  product.value.mauSac = swatch.label;
};

const isColorActive = (swatch) => product.value.mauSac === swatch.label;

const openColorPicker = () => {
  colorInputRef.value?.click();
};

const handleCustomColor = (e) => {
  const val = e.target.value;
  if (!allSwatches.value.find((s) => s.value === val)) {
    customSwatches.value.push({ label: val, value: val });
  }
  product.value.mauSac = val;
  clearError('mauSac');
};

const handleImageUpload = (event) => {
  const files = Array.from(event.target.files || []);
  addImageFiles(files);
  event.target.value = "";
};

const addImageFiles = (files) => {
  const remaining = maxImages - imageFiles.value.length;
  const toAdd = files
    .filter((f) => f.type.startsWith("image/") && f.size <= 5 * 1024 * 1024)
    .slice(0, remaining);

  toAdd.forEach((file) => {
    imageFiles.value.push(file);
    imagePreviews.value.push(URL.createObjectURL(file));
  });
  
  if (toAdd.length > 0) {
    clearError('hinhAnh');
  }
};

const handleDrop = (event) => {
  isDragOver.value = false;
  addImageFiles(Array.from(event.dataTransfer?.files || []));
};

const removeImage = (index) => {
  URL.revokeObjectURL(imagePreviews.value[index]);
  imageFiles.value.splice(index, 1);
  imagePreviews.value.splice(index, 1);
};

const addDetailBlock = (type = 'title') => {
  detailBlocks.value.push({
    id: Date.now(),
    type: type,
    content: "",
    file: null,
    previewUrl: "",
  });
};

const removeDetailBlock = (index) => {
  if (detailBlocks.value[index].type === 'image' && detailBlocks.value[index].previewUrl) {
    URL.revokeObjectURL(detailBlocks.value[index].previewUrl);
  }
  detailBlocks.value.splice(index, 1);
  if (detailBlocks.value.length === 0) {
    addDetailBlock('title');
  }
};

const handleDetailImageUpload = (index, event) => {
  const file = event.target.files[0];
  if (file && file.type.startsWith("image/") && file.size <= 5 * 1024 * 1024) {
    if (detailBlocks.value[index].previewUrl) {
      URL.revokeObjectURL(detailBlocks.value[index].previewUrl);
    }
    detailBlocks.value[index].file = file;
    detailBlocks.value[index].previewUrl = URL.createObjectURL(file);
  }
  event.target.value = "";
};

const applyFormat = (blockIndex, command) => {
  document.execCommand(command, false, null);
  const el = document.getElementById(`editor-${blockIndex}`);
  if (el) {
    detailBlocks.value[blockIndex].content = el.innerHTML;
  }
};

const syncEditorContent = (blockIndex, event) => {
  detailBlocks.value[blockIndex].content = event.target.innerHTML;
};

const getEditorCharCount = (html) => {
  if (!html) return 0;
  const text = html.replace(/<[^>]*>/g, "").trim();
  return text.length;
};

const uploadDetailImages = async () => {
  for (const block of detailBlocks.value) {
    if (block.type === 'image' && block.file) {
      const formData = new FormData();
      formData.append("file", block.file);
      const res = await api.post("/api/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      block.content = res.data;
      block.file = null; // Mark as uploaded
    }
  }
};

const serializeDetailBlocks = () => {
  const blocksText = detailBlocks.value
    .filter(b => {
      if (b.type === 'title') return b.content.trim() !== '';
      if (b.type === 'text') return b.content.replace(/<[^>]*>/g, "").trim() !== '';
      if (b.type === 'image') return !!b.content;
      return false;
    })
    .map(b => {
      if (b.type === 'title') return `### ${b.content.trim()}`;
      if (b.type === 'text') return b.content.replace(/<[^>]*>/g, " ").trim();
      if (b.type === 'image') return `[HÌNH ẢNH: ${b.content}]`;
      return '';
    });

  return blocksText.join("\n\n");
};

const buildGhiChu = () => {
  return product.value.ghiChu.trim() || null;
};

const uploadImages = async () => {
  if (!imageFiles.value.length) return [];

  const urls = [];
  for (const file of imageFiles.value) {
    const formData = new FormData();
    formData.append("file", file);
    const res = await api.post("/api/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    urls.push(res.data);
  }

  return urls;
};

const buildPayload = async () => {
  const galleryUrls = await uploadImages();
  const mainImageUrl = galleryUrls.length > 0 ? galleryUrls[0] : "";
  await uploadDetailImages();
  
  const chiTietList = [];
  const hinhAnhList = [];
  
  let thuTuHinhAnh = 0;
  // Gallery images
  for (const url of galleryUrls) {
    hinhAnhList.push({
      loaiHinhAnh: 'GALLERY',
      urlHinhAnh: url,
      thuTu: thuTuHinhAnh++
    });
  }
  
  // Detail blocks
  let thuTuChiTiet = 0;
  
  for (const block of detailBlocks.value) {
    if (block.type === 'title') {
      const content = block.content.trim();
      if (content) {
        chiTietList.push({ loaiKhoi: 'TITLE', noiDung: content, thuTu: thuTuChiTiet++ });
      }
    } else if (block.type === 'text') {
      const content = block.content.replace(/<[^>]*>/g, " ").trim();
      if (content) {
        chiTietList.push({ loaiKhoi: 'TEXT', noiDung: content, thuTu: thuTuChiTiet++ });
      }
    } else if (block.type === 'image') {
      if (block.content) {
        hinhAnhList.push({ loaiHinhAnh: 'DETAIL_BLOCK', urlHinhAnh: block.content, thuTu: thuTuHinhAnh++ });
      }
    }
  }

  return {
    tenSanPham: product.value.tenSanPham.trim(),
    loai: product.value.loai,
    noiThat: product.value.noiThat,
    quyCach: product.value.quyCach,
    tonGiao: product.value.tonGiao,
    giaTien: Number(product.value.giaTien || 0),
    soLuong: Number(product.value.soLuong || 0),
    thietKe: product.value.thietKe,
    xuatXu: product.value.xuatXu,
    ghiChu: buildGhiChu(),
    khuyenMai:
      product.value.khuyenMai === "" ? null : Number(product.value.khuyenMai),
    mauSac: product.value.mauSac,
    hinhAnh: mainImageUrl,
    vatLieu: product.value.vatLieu,
    trangThai: product.value.trangThai === "Còn bán" ? 1 : 0,
    kichThuoc: product.value.kichThuoc,
    trongLuong: product.value.trongLuong,
    cnsx: product.value.cnsx,
    chiTietList,
    hinhAnhList
  };
};

const validateProduct = () => {
  errors.value = {}; // Reset errors

  if (!product.value.tenSanPham || !product.value.tenSanPham.trim()) {
    errors.value.tenSanPham = "Vui lòng nhập tên sản phẩm!";
  }
  if (!product.value.loai) {
    errors.value.loai = "Vui lòng chọn loại sản phẩm!";
  }
  
  const giaTien = Number(product.value.giaTien);
  if (product.value.giaTien === "" || product.value.giaTien === null || isNaN(giaTien) || giaTien <= 0) {
    errors.value.giaTien = "Vui lòng nhập giá bán là một số lớn hơn 0!";
  }
  
  const soLuong = Number(product.value.soLuong);
  if (product.value.soLuong === "" || product.value.soLuong === null || isNaN(soLuong) || soLuong < 0 || !Number.isInteger(soLuong)) {
    errors.value.soLuong = "Vui lòng nhập số lượng là một số nguyên không âm hợp lệ!";
  }

  if (product.value.khuyenMai !== "" && product.value.khuyenMai !== null) {
    const km = Number(product.value.khuyenMai);
    if (isNaN(km) || km < 0) {
      errors.value.khuyenMai = "Khuyến mãi phải là một số lớn hơn hoặc bằng 0!";
    } else if (product.value.khuyenMaiLoai === "PHAN_TRAM" && km > 100) {
      errors.value.khuyenMai = "Khuyến mãi theo phần trăm không được vượt quá 100%!";
    } else if (product.value.khuyenMaiLoai === "SO_TIEN" && km >= giaTien) {
      errors.value.khuyenMai = "Khuyến mãi giảm giá số tiền phải nhỏ hơn giá bán!";
    }
  }

  if (!product.value.tonGiao) {
    errors.value.tonGiao = "Vui lòng chọn tôn giáo!";
  }
  if (!product.value.quyCach || !product.value.quyCach.trim()) {
    errors.value.quyCach = "Vui lòng nhập quy cách!";
  }
  if (!product.value.noiThat || !product.value.noiThat.trim()) {
    errors.value.noiThat = "Vui lòng nhập nội thất!";
  }
  if (!product.value.thietKe || !product.value.thietKe.trim()) {
    errors.value.thietKe = "Vui lòng nhập thiết kế!";
  }
  if (!product.value.xuatXu || !product.value.xuatXu.trim()) {
    errors.value.xuatXu = "Vui lòng nhập xuất xứ!";
  }
  if (!product.value.mauSac) {
    errors.value.mauSac = "Vui lòng chọn màu sắc!";
  }
  if (!product.value.vatLieu) {
    errors.value.vatLieu = "Vui lòng chọn vật liệu!";
  }
  if (!product.value.kichThuoc || !product.value.kichThuoc.trim()) {
    errors.value.kichThuoc = "Vui lòng nhập kích thước!";
  }
  if (!product.value.cnsx || !product.value.cnsx.trim()) {
    errors.value.cnsx = "Vui lòng nhập công nghệ sản xuất!";
  }

  const tl = Number(product.value.trongLuong);
  if (product.value.trongLuong === "" || product.value.trongLuong === null || isNaN(tl) || tl <= 0) {
    errors.value.trongLuong = "Trọng lượng phải là một số lớn hơn 0!";
  }

  if (imageFiles.value.length === 0) {
    errors.value.hinhAnh = "Vui lòng tải lên ít nhất 1 hình ảnh sản phẩm!";
  }

  return Object.keys(errors.value).length === 0;
};

const saveDraft = async () => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;

  try {
    const payload = await buildPayload();
    payload.trangThai = 0;
    await api.post(API_URL, payload);
    alert("Đã lưu nháp sản phẩm!");
    router.push("/doi-tac/quan-ly-san-pham");
  } catch (error) {
    console.error("Lỗi lưu nháp:", error);
    alert("Không thể lưu nháp sản phẩm.");
  } finally {
    isSubmitting.value = false;
  }
};

const publishProduct = async () => {
  if (isSubmitting.value) return;

  const isValid = validateProduct();
  console.log("Validation result:", isValid, "Errors:", JSON.parse(JSON.stringify(errors.value)));

  if (!isValid) {
    // Cuộn đến ô lỗi đầu tiên
    const firstErrorField = Object.keys(errors.value)[0];
    if (firstErrorField) {
      const el = document.querySelector('.has-error');
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    }
    return;
  }

  isSubmitting.value = true;

  try {
    const payload = await buildPayload();
    await api.post(API_URL, payload);
    alert("Đã đăng sản phẩm thành công!");
    router.push("/doi-tac/quan-ly-san-pham");
  } catch (error) {
    console.log(error.response);
    console.log(error.response?.data);
    console.log(error.response?.status);
      console.error("Lỗi đăng sản phẩm:", error);
    alert("Không thể đăng sản phẩm. Kiểm tra lại thông tin.");
  } finally {
    isSubmitting.value = false;
  }
};

const cancelCreate = () => {
  if (confirm("Bạn có chắc muốn hủy? Dữ liệu chưa lưu sẽ bị mất.")) {
    router.push("/doi-tac/quan-ly-san-pham");
  }
};
</script>

<template>
  <div class="tao-sp-page">
    <!-- Top bar -->
    <div class="tao-sp-topbar">

      <button type="button" class="tao-sp-guide-btn">
        <i class="fa-regular fa-circle-question"></i>
        Hướng dẫn tạo sản phẩm
      </button>
    </div>

    <div class="tao-sp-layout">

      <div class="tao-sp-left">
    <!-- Section 1: Thông tin cơ bản -->
    <section class="tao-sp-section">
      <h2 class="tao-sp-section-title">1. Thông tin cơ bản</h2>

      <div class="tao-sp-grid">
        <div class="tao-sp-field">
          <label>Mã sản phẩm</label>
          <input
            class="tao-sp-input"
            :value="product.maSanPham"
            disabled
          />
        </div>

        <div class="tao-sp-field">
          <label>Tên sản phẩm <span class="req">*</span></label>
          <input
            v-model="product.tenSanPham"
            class="tao-sp-input"
            :class="{ 'has-error': errors.tenSanPham }"
            placeholder="Nhập tên sản phẩm"
            maxlength="120"
            @input="clearError('tenSanPham')"
          />
          <span class="tao-sp-error-text" v-if="errors.tenSanPham">{{ errors.tenSanPham }}</span>
          <span class="tao-sp-char-count" v-else>{{ tenSanPhamCount }}/120</span>
        </div>

        <div class="tao-sp-field">
          <label>Loại <span class="req">*</span></label>
          <select 
            v-model="product.loai" 
            class="tao-sp-select"
            :class="{ 'has-error': errors.loai }"
            @change="clearError('loai')"
          >
            <option value="">Chọn loại sản phẩm</option>
            <option v-for="opt in loaiOptions" :key="opt" :value="opt">
              {{ opt }}
            </option>
          </select>
          <span class="tao-sp-error-text" v-if="errors.loai">{{ errors.loai }}</span>
        </div>

        <div class="tao-sp-field">
          <label>Giá bán <span class="req">*</span></label>
          <div class="tao-sp-input-wrap" :class="{ 'has-error': errors.giaTien }">
            <input
              v-model="product.giaTien"
              type="number"
              class="tao-sp-input"
              placeholder="Nhập giá bán"
              min="0"
              @input="clearError('giaTien')"
            />
            <span class="tao-sp-suffix">VND</span>
          </div>
          <span class="tao-sp-error-text" v-if="errors.giaTien">{{ errors.giaTien }}</span>
        </div>

        <div class="tao-sp-field">
          <label>Khuyến mãi</label>
          <div class="tao-sp-promo-row">
            <select v-model="product.khuyenMaiLoai" class="tao-sp-select" @change="clearError('khuyenMai')">
              <option value="PHAN_TRAM">Phần trăm</option>
              <option value="SO_TIEN">Số tiền</option>
            </select>
            <div class="tao-sp-input-wrap" :class="{ 'has-error': errors.khuyenMai }">
              <input
                v-model="product.khuyenMai"
                type="number"
                class="tao-sp-input"
                placeholder="0"
                min="0"
                @input="clearError('khuyenMai')"
              />
              <span class="tao-sp-suffix">
                {{ product.khuyenMaiLoai === "PHAN_TRAM" ? "%" : "VND" }}
              </span>
            </div>
          </div>
          <span class="tao-sp-error-text" v-if="errors.khuyenMai">{{ errors.khuyenMai }}</span>
        </div>

        <div class="tao-sp-field">
          <label>Số lượng <span class="req">*</span></label>
          <input
            v-model="product.soLuong"
            type="number"
            class="tao-sp-input"
            :class="{ 'has-error': errors.soLuong }"
            placeholder="Nhập số lượng"
            min="0"
            @input="clearError('soLuong')"
          />
          <span class="tao-sp-error-text" v-if="errors.soLuong">{{ errors.soLuong }}</span>
        </div>

        <div class="tao-sp-field">
          <label>Tôn giáo <span class="req">*</span></label>
          <select v-model="product.tonGiao" class="tao-sp-select" :class="{ 'has-error': errors.tonGiao }" @change="clearError('tonGiao')">
            <option value="">Chọn tôn giáo</option>
            <option v-for="opt in tonGiaoOptions" :key="opt" :value="opt">
              {{ opt }}
            </option>
          </select>
          <span class="tao-sp-error-text" v-if="errors.tonGiao">{{ errors.tonGiao }}</span>
        </div>

        <div class="tao-sp-field">
          <label>Quy cách <span class="req">*</span></label>
          <input
            v-model="product.quyCach"
            class="tao-sp-input"
            :class="{ 'has-error': errors.quyCach }"
            placeholder="Nhập quy cách"
            @input="clearError('quyCach')"
          />
          <span class="tao-sp-error-text" v-if="errors.quyCach">{{ errors.quyCach }}</span>
        </div>
      </div>
    </section>

    <!-- Section 2: Thông tin nâng cao -->
    <section class="tao-sp-section">
      <div class="tao-sp-section-head">
        <h2 class="tao-sp-section-title">2. Thông tin nâng cao</h2>
        <button type="button" class="tao-sp-collapse-btn" @click="toggleAdvanced">
          {{ showAdvanced ? "Ẩn" : "Hiện" }}
          <i
            class="fa-solid"
            :class="showAdvanced ? 'fa-chevron-up' : 'fa-chevron-down'"
          ></i>
        </button>
      </div>

      <div v-show="showAdvanced" class="tao-sp-grid">
        <div class="tao-sp-field">
          <label>Nội thất <span class="req">*</span></label>
          <input
            v-model="product.noiThat"
            class="tao-sp-input"
            :class="{ 'has-error': errors.noiThat }"
            placeholder="Nhập nội thất"
            @input="clearError('noiThat')"
          />
          <span class="tao-sp-error-text" v-if="errors.noiThat">{{ errors.noiThat }}</span>
        </div>

        <div class="tao-sp-field">
          <label>Mã đối tác</label>
          <input
            :value="product.maDoiTac || 'Tự động lấy từ tài khoản'"
            class="tao-sp-input"
            disabled
          />
        </div>

        <div class="tao-sp-field">
          <label>Thiết kế <span class="req">*</span></label>
          <input
            v-model="product.thietKe"
            class="tao-sp-input"
            :class="{ 'has-error': errors.thietKe }"
            placeholder="Nhập thiết kế"
            @input="clearError('thietKe')"
          />
          <span class="tao-sp-error-text" v-if="errors.thietKe">{{ errors.thietKe }}</span>
        </div>

        <div class="tao-sp-field">
          <label>Xuất xứ <span class="req">*</span></label>
          <input
            v-model="product.xuatXu"
            class="tao-sp-input"
            :class="{ 'has-error': errors.xuatXu }"
            placeholder="Nhập xuất xứ"
            @input="clearError('xuatXu')"
          />
          <span class="tao-sp-error-text" v-if="errors.xuatXu">{{ errors.xuatXu }}</span>
        </div>

        <div class="tao-sp-field span-2">
          <label>Màu sắc <span class="req">*</span></label>
          <div class="tao-sp-color-row" :class="{ 'has-error': errors.mauSac }">
            <button
              v-for="swatch in allSwatches"
              :key="swatch.value"
              type="button"
              class="tao-sp-swatch"
              :class="{ active: isColorActive(swatch) }"
              :style="{ background: swatch.value }"
              :title="swatch.label"
              @click="selectColor(swatch); clearError('mauSac')"
            ></button>
            <button
              type="button"
              class="tao-sp-swatch-add"
              title="Thêm màu"
              @click="openColorPicker"
            >
              <i class="fa-solid fa-plus"></i>
            </button>
            <input
              ref="colorInputRef"
              type="color"
              class="tao-sp-color-picker-hidden"
              @input="handleCustomColor"
            />
          </div>
          <span class="tao-sp-error-text" v-if="errors.mauSac">{{ errors.mauSac }}</span>
        </div>

        <div class="tao-sp-field">
          <label>Vật liệu <span class="req">*</span></label>
          <select v-model="product.vatLieu" class="tao-sp-select" :class="{ 'has-error': errors.vatLieu }" @change="clearError('vatLieu')">
            <option value="">Chọn vật liệu</option>
            <option v-for="opt in vatLieuOptions" :key="opt" :value="opt">
              {{ opt }}
            </option>
          </select>
          <span class="tao-sp-error-text" v-if="errors.vatLieu">{{ errors.vatLieu }}</span>
        </div>

        <div class="tao-sp-field">
          <label>Trạng thái</label>
          <select v-model.number="product.trangThai" class="tao-sp-select">
            <option :value="1">Đang bán</option>
            <option :value="0">Ẩn</option>
          </select>
        </div>

        <div class="tao-sp-field">
          <label>Kích thước <span class="req">*</span></label>
          <input
            v-model="product.kichThuoc"
            class="tao-sp-input"
            :class="{ 'has-error': errors.kichThuoc }"
            placeholder="VD: 120x60x80cm"
            @input="clearError('kichThuoc')"
          />
          <span class="tao-sp-error-text" v-if="errors.kichThuoc">{{ errors.kichThuoc }}</span>
        </div>

        <div class="tao-sp-field">
          <label>Trọng lượng <span class="req">*</span></label>
          <div class="tao-sp-input-wrap" :class="{ 'has-error': errors.trongLuong }">
            <input
              v-model="product.trongLuong"
              type="number"
              class="tao-sp-input"
              placeholder="Nhập trọng lượng"
              min="0"
              @input="clearError('trongLuong')"
            />
            <span class="tao-sp-suffix">kg</span>
          </div>
          <span class="tao-sp-error-text" v-if="errors.trongLuong">{{ errors.trongLuong }}</span>
        </div>

        <div class="tao-sp-field">
          <label>Công nghệ sản xuất <span class="req">*</span></label>
          <input
            v-model="product.cnsx"
            class="tao-sp-input"
            :class="{ 'has-error': errors.cnsx }"
            placeholder="Nhập CNSX"
            @input="clearError('cnsx')"
          />
          <span class="tao-sp-error-text" v-if="errors.cnsx">{{ errors.cnsx }}</span>
        </div>
        
        <div class="tao-sp-field">
          <label>Ngày tạo</label>
          <input
              :value="product.ngayTao"
              class="tao-sp-input"
              type="datetime-local"
              disabled
          />
        </div>

        <div class="tao-sp-field span-full">
          <label>Ghi chú</label>
          <textarea
            v-model="product.ghiChu"
            class="tao-sp-textarea"
            placeholder="Nhập ghi chú"
            maxlength="300"
            rows="3"
          ></textarea>
          <span class="tao-sp-char-count">{{ ghiChuCount }}/300</span>
        </div>


      </div>
    </section>
      </div>

      <div class="tao-sp-right">

    <!-- Section 3: Hình ảnh sản phẩm -->
    <section class="tao-sp-section">
      <h2 class="tao-sp-section-title">3. Hình ảnh sản phẩm</h2>

      <label
        class="tao-sp-upload-zone"
        :class="{ 'drag-over': isDragOver, 'has-error': errors.hinhAnh }"
        for="product-image-upload"
        @dragover.prevent="isDragOver = true"
        @dragleave.prevent="isDragOver = false"
        @drop.prevent="handleDrop"
      >
        <i class="fa-solid fa-cloud-arrow-up"></i>
        <span class="tao-sp-upload-title">Tải ảnh lên</span>
        <span class="tao-sp-upload-hint">Kéo thả hoặc click để chọn ảnh</span>
        <span class="tao-sp-upload-format">Định dạng: JPG, PNG (Tối đa 5MB)</span>
      </label>
      <span class="tao-sp-error-text" style="margin-bottom: 12px; margin-top: -10px;" v-if="errors.hinhAnh">{{ errors.hinhAnh }}</span>
      <input
        id="product-image-upload"
        type="file"
        accept="image/jpeg,image/png,image/webp"
        multiple
        hidden
        @change="handleImageUpload"
      />

      <div class="tao-sp-gallery">
        <div
          v-for="(img, idx) in imagePreviews"
          :key="idx"
          class="tao-sp-gallery-item"
        >
          <img :src="img" alt="Ảnh sản phẩm" />
          <button
            type="button"
            class="tao-sp-gallery-remove"
            @click="removeImage(idx)"
          >
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <label
          v-if="imagePreviews.length < maxImages"
          class="tao-sp-gallery-add"
          for="product-image-upload-more"
        >
          <i class="fa-solid fa-plus"></i>
          <span>Thêm ảnh</span>
        </label>
        <input
          id="product-image-upload-more"
          type="file"
          accept="image/jpeg,image/png,image/webp"
          multiple
          hidden
          @change="handleImageUpload"
        />
      </div>

      <p class="tao-sp-upload-note">
        Có thể tải lên tối đa {{ maxImages }} ảnh
      </p>
    </section>

    <!-- Section 4: Thông tin chi tiết -->
    <section class="tao-sp-section">
      <h2 class="tao-sp-section-title">4. Thông tin chi tiết</h2>
      <p class="tao-sp-detail-intro">
        Thêm nội dung mô tả chi tiết về sản phẩm của bạn
      </p>

      <div class="tao-sp-detail-toolbar">
        <button type="button" class="primary-outline" @click="addDetailBlock('title')">
          <i class="fa-solid fa-plus"></i>
          Thêm tiêu đề
        </button>
        <button type="button" @click="addDetailBlock('text')">
          <i class="fa-solid fa-align-left"></i>
          Thêm nội dung
        </button>
        <button type="button" @click="addDetailBlock('image')">
          <i class="fa-solid fa-image"></i>
          Thêm ảnh
        </button>
      </div>

      <div
        v-for="(block, index) in detailBlocks"
        :key="block.id"
        class="tao-sp-detail-block"
      >
        <div class="tao-sp-detail-block-head">
          <label v-if="block.type === 'title'">Tiêu đề</label>
          <label v-else-if="block.type === 'text'">Nội dung văn bản</label>
          <label v-else-if="block.type === 'image'">Hình ảnh chi tiết</label>
          <button
            type="button"
            class="tao-sp-detail-delete"
            title="Xóa khối"
            @click="removeDetailBlock(index)"
          >
            <i class="fa-regular fa-trash-can"></i>
          </button>
        </div>

        <template v-if="block.type === 'title'">
          <input
            v-model="block.content"
            class="tao-sp-input"
            placeholder="Nhập tiêu đề..."
            maxlength="100"
            style="font-weight: bold; font-size: 14px;"
          />
          <span class="tao-sp-char-count">{{ block.content.length }}/100</span>
        </template>

        <template v-else-if="block.type === 'text'">
          <div class="tao-sp-editor-toolbar">
            <button type="button" title="In đậm" @click="applyFormat(index, 'bold')">
              <b>B</b>
            </button>
            <button type="button" title="In nghiêng" @click="applyFormat(index, 'italic')">
              <i>I</i>
            </button>
            <button type="button" title="Gạch chân" @click="applyFormat(index, 'underline')">
              <u>U</u>
            </button>
            <span class="sep"></span>
            <button type="button" title="Danh sách" @click="applyFormat(index, 'insertUnorderedList')">
              <i class="fa-solid fa-list-ul"></i>
            </button>
            <button type="button" title="Căn trái" @click="applyFormat(index, 'justifyLeft')">
              <i class="fa-solid fa-align-left"></i>
            </button>
            <button type="button" title="Căn giữa" @click="applyFormat(index, 'justifyCenter')">
              <i class="fa-solid fa-align-center"></i>
            </button>
            <button type="button" title="Liên kết" @click="applyFormat(index, 'createLink')">
              <i class="fa-solid fa-link"></i>
            </button>
            <span class="spacer"></span>
            <button
              type="button"
              title="Xóa nội dung"
              @click="block.content = ''; document.getElementById(`editor-${index}`).innerHTML = ''"
            >
              <i class="fa-regular fa-trash-can"></i>
            </button>
          </div>

          <div
            :id="`editor-${index}`"
            class="tao-sp-editor-area"
            contenteditable="true"
            data-placeholder="Nhập nội dung chi tiết..."
            @input="syncEditorContent(index, $event)"
          ></div>
          <div class="tao-sp-editor-footer">
            {{ getEditorCharCount(block.content) }} ký tự
          </div>
        </template>

        <template v-else-if="block.type === 'image'">
          <label v-if="!block.previewUrl" class="tao-sp-upload-zone" style="min-height: 120px; padding: 30px;" :for="`detail-img-${index}`">
            <i class="fa-solid fa-cloud-arrow-up"></i>
            <span class="tao-sp-upload-title">Chọn ảnh</span>
            <span class="tao-sp-upload-format">Định dạng: JPG, PNG (Tối đa 5MB)</span>
          </label>
          <input
            :id="`detail-img-${index}`"
            type="file"
            accept="image/jpeg,image/png,image/webp"
            hidden
            @change="handleDetailImageUpload(index, $event)"
          />
          <div v-if="block.previewUrl" class="tao-sp-detail-img-preview" style="position: relative; display: inline-block; border-radius: 8px; overflow: hidden; border: 1px solid #e5e7eb;">
            <img :src="block.previewUrl" style="max-height: 200px; max-width: 100%; display: block; object-fit: contain;" />
            <label :for="`detail-img-${index}`" class="tao-sp-detail-img-change" style="position: absolute; bottom: 8px; right: 8px; background: rgba(0,0,0,0.6); color: #fff; padding: 4px 10px; border-radius: 6px; font-size: 12px; cursor: pointer;">
              Thay ảnh
            </label>
          </div>
        </template>
      </div>

      <div style="display: flex; gap: 8px;">
        <button type="button" class="tao-sp-add-block-btn" @click="addDetailBlock('title')" style="flex: 1;">
          <i class="fa-solid fa-plus"></i>
          Thêm tiêu đề
        </button>
        <button type="button" class="tao-sp-add-block-btn" @click="addDetailBlock('text')" style="flex: 1;">
          <i class="fa-solid fa-align-left"></i>
          Thêm nội dung
        </button>
        <button type="button" class="tao-sp-add-block-btn" @click="addDetailBlock('image')" style="flex: 1;">
          <i class="fa-solid fa-image"></i>
          Thêm ảnh
        </button>
      </div>
    </section>
      </div>

    </div>
    <!-- Footer actions -->
    <div class="tao-sp-footer">
      <button
        type="button"
        class="tao-sp-btn-cancel"
        :disabled="isSubmitting"
        @click="cancelCreate"
      >
        Hủy bỏ
      </button>
      <button
        type="button"
        class="tao-sp-btn-draft"
        :disabled="isSubmitting"
        @click="saveDraft"
      >
        <i v-if="isSubmitting" class="fa-solid fa-circle-notch fa-spin" style="margin-right: 6px;"></i>
        {{ isSubmitting ? 'Đang lưu...' : 'Lưu nháp' }}
      </button>
      <button
        type="button"
        class="tao-sp-btn-publish"
        :disabled="isSubmitting"
        @click="publishProduct"
      >
        <i v-if="!isSubmitting" class="fa-solid fa-paper-plane"></i>
        <i v-else class="fa-solid fa-circle-notch fa-spin"></i>
        {{ isSubmitting ? 'Đang đăng...' : 'Đăng sản phẩm' }}
      </button>
    </div>
  </div>
</template>

<style scoped src="../../assets/styles/doitac/TaoSanPham/TrangTaoSanPham.css"></style>
