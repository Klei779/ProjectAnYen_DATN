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
  trangThai: "Còn bán",
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
  { id: 1, tieuDe: "", noiDung: "" },
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

const handleCustomColor = (event) => {
  const hex = event.target.value;
  const label = `Màu ${hex.toUpperCase()}`;
  const exists = allSwatches.value.some((s) => s.value === hex);

  if (!exists) {
    customSwatches.value.push({ label, value: hex });
  }

  product.value.mauSac = label;
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

const addDetailBlock = () => {
  detailBlocks.value.push({
    id: Date.now(),
    tieuDe: "",
    noiDung: "",
  });
};

const removeDetailBlock = (index) => {
  if (detailBlocks.value.length === 1) {
    detailBlocks.value[0] = { id: Date.now(), tieuDe: "", noiDung: "" };
    return;
  }
  detailBlocks.value.splice(index, 1);
};

const applyFormat = (blockIndex, command) => {
  document.execCommand(command, false, null);
  const el = document.getElementById(`editor-${blockIndex}`);
  if (el) {
    detailBlocks.value[blockIndex].noiDung = el.innerHTML;
  }
};

const syncEditorContent = (blockIndex, event) => {
  detailBlocks.value[blockIndex].noiDung = event.target.innerHTML;
};

const getEditorCharCount = (html) => {
  const text = html.replace(/<[^>]*>/g, "").trim();
  return text.length;
};

const serializeDetailBlocks = () => {
  const blocks = detailBlocks.value
    .filter((b) => b.tieuDe.trim() || b.noiDung.replace(/<[^>]*>/g, "").trim())
    .map((b) => ({
      tieuDe: b.tieuDe.trim(),
      noiDung: b.noiDung.trim(),
    }));

  if (!blocks.length) return "";

  return blocks
    .map((b) => `${b.tieuDe}\n${b.noiDung.replace(/<[^>]*>/g, " ").trim()}`)
    .join("\n\n");
};

const buildGhiChu = () => {
  const parts = [];
  if (product.value.ghiChu.trim()) {
    parts.push(product.value.ghiChu.trim());
  }

  const detailText = serializeDetailBlocks();
  if (detailText) {
    parts.push(`[THÔNG TIN CHI TIẾT]\n${detailText}`);
  }

  return parts.join("\n\n") || null;
};

const uploadImages = async () => {
  if (!imageFiles.value.length) return "";

  const urls = [];
  for (const file of imageFiles.value) {
    const formData = new FormData();
    formData.append("file", file);
    const res = await api.post("/api/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    urls.push(res.data);
  }

  return urls[0] || "";
};

const buildPayload = async () => ({
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
  hinhAnh: await uploadImages(),
  vatLieu: product.value.vatLieu,
  trangThai: product.value.trangThai === "Còn bán" ? "Đang bán" : "Ẩn",
  kichThuoc: product.value.kichThuoc,
  trongLuong: product.value.trongLuong,
  cnsx: product.value.cnsx,
});

const validateProduct = () => {
  if (!product.value.tenSanPham.trim()) {
    alert("Vui lòng nhập tên sản phẩm!");
    return false;
  }
  if (!product.value.loai) {
    alert("Vui lòng chọn loại sản phẩm!");
    return false;
  }
  if (!product.value.giaTien || Number(product.value.giaTien) <= 0) {
    alert("Vui lòng nhập giá bán hợp lệ!");
    return false;
  }
  if (!product.value.soLuong || Number(product.value.soLuong) <= 0) {
    alert("Vui lòng nhập số lượng hợp lệ!");
    return false;
  }
  return true;
};

const saveDraft = async () => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;

  try {
    const payload = await buildPayload();
    payload.trangThai = "Ẩn";
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
  if (!validateProduct() || isSubmitting.value) return;
  isSubmitting.value = true;

  try {
    await api.post(API_URL, await buildPayload());
    alert("Đã đăng sản phẩm thành công!");
    router.push("/doi-tac/quan-ly-san-pham");
  } catch (error) {
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
            placeholder="Nhập tên sản phẩm"
            maxlength="120"
          />
          <span class="tao-sp-char-count">{{ tenSanPhamCount }}/120</span>
        </div>

        <div class="tao-sp-field">
          <label>Loại <span class="req">*</span></label>
          <select v-model="product.loai" class="tao-sp-select">
            <option value="">Chọn loại sản phẩm</option>
            <option v-for="opt in loaiOptions" :key="opt" :value="opt">
              {{ opt }}
            </option>
          </select>
        </div>

        <div class="tao-sp-field">
          <label>Giá bán <span class="req">*</span></label>
          <div class="tao-sp-input-wrap">
            <input
              v-model="product.giaTien"
              type="number"
              class="tao-sp-input"
              placeholder="Nhập giá bán"
              min="0"
            />
            <span class="tao-sp-suffix">VND</span>
          </div>
        </div>

        <div class="tao-sp-field">
          <label>Khuyến mãi</label>
          <div class="tao-sp-promo-row">
            <select v-model="product.khuyenMaiLoai" class="tao-sp-select">
              <option value="PHAN_TRAM">Phần trăm</option>
              <option value="SO_TIEN">Số tiền</option>
            </select>
            <div class="tao-sp-input-wrap">
              <input
                v-model="product.khuyenMai"
                type="number"
                class="tao-sp-input"
                placeholder="0"
                min="0"
              />
              <span class="tao-sp-suffix">
                {{ product.khuyenMaiLoai === "PHAN_TRAM" ? "%" : "VND" }}
              </span>
            </div>
          </div>
        </div>

        <div class="tao-sp-field">
          <label>Số lượng <span class="req">*</span></label>
          <input
            v-model="product.soLuong"
            type="number"
            class="tao-sp-input"
            placeholder="Nhập số lượng"
            min="0"
          />
        </div>

        <div class="tao-sp-field">
          <label>Tôn giáo</label>
          <select v-model="product.tonGiao" class="tao-sp-select">
            <option value="">Chọn tôn giáo</option>
            <option v-for="opt in tonGiaoOptions" :key="opt" :value="opt">
              {{ opt }}
            </option>
          </select>
        </div>

        <div class="tao-sp-field">
          <label>Quy cách</label>
          <input
            v-model="product.quyCach"
            class="tao-sp-input"
            placeholder="Nhập quy cách"
          />
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
          <label>Nội thất</label>
          <input
            v-model="product.noiThat"
            class="tao-sp-input"
            placeholder="Nhập nội thất"
          />
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
          <label>Thiết kế</label>
          <input
            v-model="product.thietKe"
            class="tao-sp-input"
            placeholder="Nhập thiết kế"
          />
        </div>

        <div class="tao-sp-field">
          <label>Xuất xứ</label>
          <input
            v-model="product.xuatXu"
            class="tao-sp-input"
            placeholder="Nhập xuất xứ"
          />
        </div>

        <div class="tao-sp-field span-2">
          <label>Màu sắc</label>
          <div class="tao-sp-color-row">
            <button
              v-for="swatch in allSwatches"
              :key="swatch.value"
              type="button"
              class="tao-sp-swatch"
              :class="{ active: isColorActive(swatch) }"
              :style="{ background: swatch.value }"
              :title="swatch.label"
              @click="selectColor(swatch)"
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
        </div>

        <div class="tao-sp-field">
          <label>Vật liệu</label>
          <select v-model="product.vatLieu" class="tao-sp-select">
            <option value="">Chọn vật liệu</option>
            <option v-for="opt in vatLieuOptions" :key="opt" :value="opt">
              {{ opt }}
            </option>
          </select>
        </div>

        <div class="tao-sp-field">
          <label>Trạng thái</label>
          <select v-model="product.trangThai" class="tao-sp-select">
            <option>Còn bán</option>
            <option>Ngưng bán</option>
          </select>
        </div>

        <div class="tao-sp-field">
          <label>Kích thước</label>
          <input
            v-model="product.kichThuoc"
            class="tao-sp-input"
            placeholder="VD: 120x60x80cm"
          />
        </div>

        <div class="tao-sp-field">
          <label>Trọng lượng</label>
          <div class="tao-sp-input-wrap">
            <input
              v-model="product.trongLuong"
              type="number"
              class="tao-sp-input"
              placeholder="Nhập trọng lượng"
              min="0"
            />
            <span class="tao-sp-suffix">kg</span>
          </div>
        </div>

        <div class="tao-sp-field">
          <label>Công nghệ sản xuất</label>
          <input
            v-model="product.cnsx"
            class="tao-sp-input"
            placeholder="Nhập CNSX"
          />
        </div>

        <div class="tao-sp-field span-3">
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

        <div class="tao-sp-field">
          <label>Ngày tạo</label>
          <input
            :value="product.ngayTao"
            class="tao-sp-input"
            type="datetime-local"
            disabled
          />
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
        :class="{ 'drag-over': isDragOver }"
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
        <button type="button" class="primary-outline" @click="addDetailBlock">
          <i class="fa-solid fa-plus"></i>
          Thêm tiêu đề
        </button>
        <button type="button">
          <i class="fa-solid fa-align-left"></i>
          Thêm nội dung
        </button>
        <button type="button">
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
          <label>Tiêu đề {{ index + 1 }}</label>
          <button
            type="button"
            class="tao-sp-detail-delete"
            title="Xóa khối nội dung"
            @click="removeDetailBlock(index)"
          >
            <i class="fa-regular fa-trash-can"></i>
          </button>
        </div>

        <input
          v-model="block.tieuDe"
          class="tao-sp-input"
          placeholder="Nhập tiêu đề"
          maxlength="100"
        />
        <span class="tao-sp-char-count">{{ block.tieuDe.length }}/100</span>

        <label style="margin-top: 12px; display: block; font-size: 13px; font-weight: 600; color: #374151;">
          Nội dung
        </label>

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
          <button type="button" title="Ảnh" @click="applyFormat(index, 'insertImage')">
            <i class="fa-regular fa-image"></i>
          </button>
          <span class="spacer"></span>
          <button
            type="button"
            title="Xóa nội dung"
            @click="block.noiDung = ''"
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
          {{ getEditorCharCount(block.noiDung) }} ký tự
        </div>
      </div>

      <button type="button" class="tao-sp-add-block-btn" @click="addDetailBlock">
        <i class="fa-solid fa-plus"></i>
        Thêm tiêu đề mới
      </button>
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
        Lưu nháp
      </button>
      <button
        type="button"
        class="tao-sp-btn-publish"
        :disabled="isSubmitting"
        @click="publishProduct"
      >
        <i class="fa-solid fa-paper-plane"></i>
        Đăng sản phẩm
      </button>
    </div>
  </div>
</template>

<style scoped src="../../assets/styles/doitac/TaoSanPham/TrangTaoSanPham.css"></style>
