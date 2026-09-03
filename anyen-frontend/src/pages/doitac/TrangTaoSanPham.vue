<script setup>
import {
  ref,
  computed,
  onMounted,
  onBeforeUnmount
} from "vue";

import { useRouter } from "vue-router";
import api from "../../api/api.js";

const props = defineProps({
  editId: {
    type: [Number, String],
    default: null
  }
});

const emit = defineEmits(["close", "saved"]);

const router = useRouter();

const API_URL = "/api/doi-tac/san-pham";
const UPLOAD_URL = "/api/upload";

const MAX_IMAGES = 10;
const MAX_FILE_SIZE = 5 * 1024 * 1024;

const DETAIL_TYPE = {
  TITLE: "TIEU_DE",
  TEXT: "NOI_DUNG",
  IMAGE: "HINH_ANH"
};

const showAdvanced = ref(true);
const isSubmitting = ref(false);
const isDragOver = ref(false);
const colorInputRef = ref(null);
const errors = ref({});

const imageFiles = ref([]);
const imagePreviews = ref([]);

const createBlockId = () => {
  return `${Date.now()}-${Math.random().toString(36).slice(2)}`;
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
  ngayTao: ""
});

const detailBlocks = ref([
  {
    id: createBlockId(),
    type: "title",
    content: "",
    file: null,
    previewUrl: ""
  }
]);

const defaultSwatches = [
  { label: "Nâu đậm", value: "#3e2723" },
  { label: "Nâu sáng", value: "#8d6e63" },
  { label: "Nâu vàng", value: "#a1887f" },
  { label: "Đen", value: "#1f2937" }
];

const customSwatches = ref([]);

const loaiOptions = [
  "Quan tài",
  "Bình tro cốt",
  "Tiểu quách",
  "Hoa tang lễ",
  "Vải liệm",
  "Phụ kiện",
  "Khác"
];

const tonGiaoOptions = [
  "Phật giáo",
  "Công giáo",
  "Tin lành",
  "Cao Đài",
  "Hòa Hảo",
  "Không yêu cầu"
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
  "Khác"
];

const allSwatches = computed(() => [
  ...defaultSwatches,
  ...customSwatches.value
]);

const tenSanPhamCount = computed(() => {
  return product.value.tenSanPham?.length || 0;
});

const ghiChuCount = computed(() => {
  return product.value.ghiChu?.length || 0;
});

const clearError = (field) => {
  if (errors.value[field]) {
    delete errors.value[field];
  }
};

const getCurrentUser = () => {
  try {
    return JSON.parse(localStorage.getItem("user") || "{}");
  } catch (error) {
    console.error("Không thể đọc thông tin tài khoản:", error);
    return {};
  }
};

const loadPartnerId = () => {
  const currentUser = getCurrentUser();

  product.value.maDoiTac =
      currentUser.id ??
      currentUser.maDoiTac ??
      currentUser.userId ??
      localStorage.getItem("id") ??
      "";
};

onMounted(async () => {
  if (props.editId) {
    try {
      const res = await api.get(`${API_URL}/${props.editId}`);
      const data = res.data;
      
      product.value = {
        ...product.value,
        ...data,
      };

      // Tải hình ảnh chính
      if (data.hinhAnh) {
        imagePreviews.value[0] = data.hinhAnh;
      }
      
      // Tải hình ảnh gallery
      const galleryImages = (data.hinhAnhList || []).filter(img => img.loaiHinhAnh === 'GALLERY' || !img.maChiTiet);
      galleryImages.forEach(img => {
        if (img.urlHinhAnh) {
          imagePreviews.value.push(img.urlHinhAnh);
        }
      });
      
      // Tải chi tiết
      if (data.chiTietList && data.chiTietList.length > 0) {
        detailBlocks.value = data.chiTietList.map(ct => ({
          id: createBlockId(),
          type: ct.loaiKhoi === 'TIEU_DE' ? 'title' : ct.loaiKhoi === 'NOI_DUNG' ? 'text' : 'image',
          content: ct.noiDung || '',
          file: null,
          previewUrl: ct.loaiKhoi === 'HINH_ANH' ? ct.noiDung : ''
        }));
      }
    } catch (err) {
      console.error("Lỗi khi tải thông tin sản phẩm:", err);
      alert("Không thể tải thông tin sản phẩm. Vui lòng thử lại!");
    }
  } else {
    const timestamp = Date.now()
        .toString(36)
        .toUpperCase();

    product.value.maSanPham = `SP-${timestamp}`;

    const now = new Date();
    const timezoneOffset = now.getTimezoneOffset() * 60 * 1000;

    product.value.ngayTao = new Date(
        now.getTime() - timezoneOffset
    )
        .toISOString()
        .slice(0, 16);

    loadPartnerId();

    // Dữ liệu mẫu để kiểm thử
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
  }
});

onBeforeUnmount(() => {
  imagePreviews.value.forEach((previewUrl) => {
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
    }
  });

  detailBlocks.value.forEach((block) => {
    if (block.previewUrl) {
      URL.revokeObjectURL(block.previewUrl);
    }
  });
});

const toggleAdvanced = () => {
  showAdvanced.value = !showAdvanced.value;
};

const selectColor = (swatch) => {
  product.value.mauSac = swatch.label;
  clearError("mauSac");
};

const isColorActive = (swatch) => {
  return product.value.mauSac === swatch.label;
};

const openColorPicker = () => {
  colorInputRef.value?.click();
};

const handleCustomColor = (event) => {
  const value = event.target.value;

  const colorExists = allSwatches.value.some(
      (swatch) => swatch.value === value
  );

  if (!colorExists) {
    customSwatches.value.push({
      label: value,
      value
    });
  }

  product.value.mauSac = value;
  clearError("mauSac");
};

const validateImageFile = (file) => {
  if (!(file instanceof File)) {
    return {
      valid: false,
      message: "Dữ liệu được chọn không phải là tệp hợp lệ."
    };
  }

  if (!file.type.startsWith("image/")) {
    return {
      valid: false,
      message: `${file.name} không phải là tệp hình ảnh.`
    };
  }

  if (file.size > MAX_FILE_SIZE) {
    return {
      valid: false,
      message: `${file.name} vượt quá dung lượng 5MB.`
    };
  }

  return {
    valid: true,
    message: ""
  };
};

const addImageFiles = (files) => {
  const remaining = MAX_IMAGES - imageFiles.value.length;

  if (remaining <= 0) {
    alert(`Chỉ được tải tối đa ${MAX_IMAGES} ảnh.`);
    return;
  }

  const validFiles = [];
  const errorMessages = [];

  files.forEach((file) => {
    const result = validateImageFile(file);

    if (result.valid) {
      validFiles.push(file);
    } else {
      errorMessages.push(result.message);
    }
  });

  const filesToAdd = validFiles.slice(0, remaining);

  filesToAdd.forEach((file) => {
    imageFiles.value.push(file);
    imagePreviews.value.push(URL.createObjectURL(file));
  });

  if (filesToAdd.length > 0) {
    clearError("hinhAnh");
  }

  if (validFiles.length > remaining) {
    errorMessages.push(
        `Chỉ thêm được ${remaining} ảnh vì giới hạn tối đa là ${MAX_IMAGES} ảnh.`
    );
  }

  if (errorMessages.length > 0) {
    alert(errorMessages.join("\n"));
  }
};

const handleImageUpload = (event) => {
  const files = Array.from(event.target.files || []);

  addImageFiles(files);

  event.target.value = "";
};

const handleDrop = (event) => {
  isDragOver.value = false;

  const files = Array.from(
      event.dataTransfer?.files || []
  );

  addImageFiles(files);
};

const removeImage = (index) => {
  const previewUrl = imagePreviews.value[index];

  if (previewUrl) {
    URL.revokeObjectURL(previewUrl);
  }

  imageFiles.value.splice(index, 1);
  imagePreviews.value.splice(index, 1);
};

const addDetailBlock = (type = "title") => {
  detailBlocks.value.push({
    id: createBlockId(),
    type,
    content: "",
    file: null,
    previewUrl: ""
  });
};

const removeDetailBlock = (index) => {
  const block = detailBlocks.value[index];

  if (block?.previewUrl) {
    URL.revokeObjectURL(block.previewUrl);
  }

  detailBlocks.value.splice(index, 1);

  if (detailBlocks.value.length === 0) {
    addDetailBlock("title");
  }
};

const handleDetailImageUpload = (index, event) => {
  const file = event.target.files?.[0];

  event.target.value = "";

  if (!file) {
    return;
  }

  const validation = validateImageFile(file);

  if (!validation.valid) {
    alert(validation.message);
    return;
  }

  const block = detailBlocks.value[index];

  if (!block) {
    return;
  }

  if (block.previewUrl) {
    URL.revokeObjectURL(block.previewUrl);
  }

  block.file = file;
  block.content = "";
  block.previewUrl = URL.createObjectURL(file);
};

const applyFormat = (blockIndex, command) => {
  const editor = document.getElementById(
      `editor-${blockIndex}`
  );

  if (!editor) {
    return;
  }

  editor.focus();

  if (command === "createLink") {
    const url = prompt("Nhập đường dẫn liên kết:");

    if (!url?.trim()) {
      return;
    }

    document.execCommand(
        "createLink",
        false,
        url.trim()
    );
  } else {
    document.execCommand(command, false, null);
  }

  detailBlocks.value[blockIndex].content =
      editor.innerHTML;
};

const clearEditorContent = (blockIndex) => {
  const block = detailBlocks.value[blockIndex];

  if (!block) {
    return;
  }

  block.content = "";

  const editor = document.getElementById(
      `editor-${blockIndex}`
  );

  if (editor) {
    editor.innerHTML = "";
  }
};

const syncEditorContent = (blockIndex, event) => {
  if (!detailBlocks.value[blockIndex]) {
    return;
  }

  detailBlocks.value[blockIndex].content =
      event.target.innerHTML;
};

const getEditorCharCount = (html) => {
  if (!html) {
    return 0;
  }

  const temporaryElement =
      document.createElement("div");

  temporaryElement.innerHTML = html;

  return temporaryElement.textContent
      ?.trim()
      .length || 0;
};

const getUploadedUrl = (responseData) => {
  if (!responseData) {
    return "";
  }

  if (typeof responseData === "string") {
    return responseData;
  }

  if (
      typeof responseData.data === "string"
  ) {
    return responseData.data;
  }

  return (
      responseData.url ||
      responseData.secureUrl ||
      responseData.secure_url ||
      responseData.imageUrl ||
      responseData.fileUrl ||
      responseData.data?.url ||
      responseData.data?.secureUrl ||
      responseData.data?.secure_url ||
      ""
  );
};

const getBackendErrorMessage = (
    error,
    defaultMessage
) => {
  const data = error.response?.data;

  if (typeof data === "string" && data.trim()) {
    return data;
  }

  return (
      data?.message ||
      data?.error ||
      error.message ||
      defaultMessage
  );
};

const uploadOneImage = async (file) => {
  const validation = validateImageFile(file);

  if (!validation.valid) {
    throw new Error(validation.message);
  }

  const formData = new FormData();

  /*
   * Tên "file" phải trùng với:
   * @RequestParam("file") MultipartFile file
   * ở Spring Boot.
   */
  formData.append("file", file, file.name);

  try {
    /*
     * Không tự đặt Content-Type.
     * Axios sẽ tự thêm multipart boundary.
     */
    const response = await api.post(
        UPLOAD_URL,
        formData
    );

    const uploadedUrl = getUploadedUrl(
        response.data
    );

    if (!uploadedUrl) {
      console.error(
          "API upload không trả về URL:",
          response.data
      );

      throw new Error(
          "Máy chủ không trả về đường dẫn ảnh."
      );
    }

    return uploadedUrl;
  } catch (error) {
    console.error("===== LỖI UPLOAD ẢNH =====");
    console.error("URL:", error.config?.url);
    console.error("Status:", error.response?.status);
    console.error("Response:", error.response?.data);
    console.error("Tên file:", file.name);
    console.error("Loại file:", file.type);
    console.error("Dung lượng:", file.size);
    console.error("==========================");

    throw new Error(
        getBackendErrorMessage(
            error,
            `Không thể tải ảnh ${file.name}`
        )
    );
  }
};

const uploadImages = async () => {
  if (imageFiles.value.length === 0) {
    return [];
  }

  const urls = [];

  for (const file of imageFiles.value) {
    const uploadedUrl =
        await uploadOneImage(file);

    urls.push(uploadedUrl);
  }

  return urls;
};

const uploadDetailImages = async () => {
  for (const block of detailBlocks.value) {
    if (
        block.type !== "image" ||
        !block.file
    ) {
      continue;
    }

    const uploadedUrl =
        await uploadOneImage(block.file);

    block.content = uploadedUrl;
    block.file = null;
  }
};

const normalizeEditorContent = (html) => {
  if (!html) {
    return "";
  }

  const temporaryElement =
      document.createElement("div");

  temporaryElement.innerHTML = html;

  const plainText =
      temporaryElement.textContent
          ?.replace(/\s+/g, " ")
          .trim() || "";

  if (!plainText) {
    return "";
  }

  return html.trim();
};

const buildGhiChu = () => {
  return product.value.ghiChu?.trim() || null;
};

const buildPayload = async () => {
  const galleryUrls = await uploadImages();

  await uploadDetailImages();

  // Nếu đang edit mà không upload ảnh mới, dùng ảnh cũ từ imagePreviews
  let finalGalleryUrls = galleryUrls;
  if (galleryUrls.length === 0 && props.editId) {
    // imagePreviews chứa URL ảnh cũ từ server
    finalGalleryUrls = imagePreviews.value.filter(url => url && typeof url === 'string');
  } else if (galleryUrls.length > 0 && props.editId) {
    // Kết hợp: ảnh cũ chưa bị xóa + ảnh mới upload
    const existingUrls = imagePreviews.value.filter(url => url && typeof url === 'string' && !url.startsWith('blob:'));
    finalGalleryUrls = [...existingUrls, ...galleryUrls];
  }

  const mainImageUrl = finalGalleryUrls[0] || "";

  const chiTietList = [];
  const hinhAnhList = [];

  let thuTuChiTiet = 0;
  let thuTuHinhAnh = 0;

  /*
   * Ảnh sản phẩm đưa vào bảng sanpham_hinhanh.
   */
  finalGalleryUrls.forEach((url, index) => {
    hinhAnhList.push({
      loaiHinhAnh:
          index === 0 ? "CHINH" : "GALLERY",
      urlHinhAnh: url,
      thuTu: thuTuHinhAnh++
    });
  });

  /*
   * Tiêu đề, nội dung và ảnh mô tả
   * đưa vào bảng sanpham_chitiet.
   */
  for (const block of detailBlocks.value) {
    if (block.type === "title") {
      const content = block.content?.trim();

      if (content) {
        chiTietList.push({
          loaiKhoi: DETAIL_TYPE.TITLE,
          noiDung: content,
          thuTu: thuTuChiTiet++
        });
      }

      continue;
    }

    if (block.type === "text") {
      const content = normalizeEditorContent(
          block.content
      );

      if (content) {
        chiTietList.push({
          loaiKhoi: DETAIL_TYPE.TEXT,
          noiDung: content,
          thuTu: thuTuChiTiet++
        });
      }

      continue;
    }

    if (
        block.type === "image" &&
        block.content
    ) {
      chiTietList.push({
        loaiKhoi: DETAIL_TYPE.IMAGE,
        noiDung: block.content,
        thuTu: thuTuChiTiet++
      });
    }
  }

  const payload = {
    tenSanPham:
        product.value.tenSanPham.trim(),

    loai: product.value.loai,

    noiThat:
        product.value.noiThat.trim(),

    quyCach:
        product.value.quyCach.trim(),

    tonGiao: product.value.tonGiao,

    giaTien: Number(
        product.value.giaTien || 0
    ),

    soLuong: Number(
        product.value.soLuong || 0
    ),

    thietKe:
        product.value.thietKe.trim(),

    xuatXu:
        product.value.xuatXu.trim(),

    ghiChu: buildGhiChu(),

    khuyenMai:
        product.value.khuyenMai === "" ||
        product.value.khuyenMai === null
            ? null
            : Number(product.value.khuyenMai),

    mauSac: product.value.mauSac,

    hinhAnh: mainImageUrl,

    vatLieu: product.value.vatLieu,

    trangThai: Number(
        product.value.trangThai
    ),

    kichThuoc:
        product.value.kichThuoc.trim(),

    trongLuong: Number(
        product.value.trongLuong || 0
    ),

    cnsx:
        product.value.cnsx.trim(),

    maDoiTac: product.value.maDoiTac,

    chiTietList,
    hinhAnhList
  };

  console.log(
      "Payload tạo sản phẩm:",
      JSON.parse(JSON.stringify(payload))
  );

  return payload;
};

const validateProduct = () => {
  errors.value = {};

  if (
      !product.value.tenSanPham ||
      !product.value.tenSanPham.trim()
  ) {
    errors.value.tenSanPham =
        "Vui lòng nhập tên sản phẩm!";
  }

  if (!product.value.loai) {
    errors.value.loai =
        "Vui lòng chọn loại sản phẩm!";
  }

  const giaTien = Number(
      product.value.giaTien
  );

  if (
      product.value.giaTien === "" ||
      product.value.giaTien === null ||
      Number.isNaN(giaTien) ||
      giaTien <= 0
  ) {
    errors.value.giaTien =
        "Vui lòng nhập giá bán là một số lớn hơn 0!";
  }

  const soLuong = Number(
      product.value.soLuong
  );

  if (
      product.value.soLuong === "" ||
      product.value.soLuong === null ||
      Number.isNaN(soLuong) ||
      soLuong < 0 ||
      !Number.isInteger(soLuong)
  ) {
    errors.value.soLuong =
        "Vui lòng nhập số lượng là số nguyên không âm!";
  }

  if (
      product.value.khuyenMai !== "" &&
      product.value.khuyenMai !== null
  ) {
    const khuyenMai = Number(
        product.value.khuyenMai
    );

    if (
        Number.isNaN(khuyenMai) ||
        khuyenMai < 0
    ) {
      errors.value.khuyenMai =
          "Khuyến mãi phải lớn hơn hoặc bằng 0!";
    } else if (
        product.value.khuyenMaiLoai ===
        "PHAN_TRAM" &&
        khuyenMai > 100
    ) {
      errors.value.khuyenMai =
          "Khuyến mãi phần trăm không được vượt quá 100%!";
    } else if (
        product.value.khuyenMaiLoai ===
        "SO_TIEN" &&
        khuyenMai >= giaTien
    ) {
      errors.value.khuyenMai =
          "Số tiền khuyến mãi phải nhỏ hơn giá bán!";
    }
  }

  if (!product.value.tonGiao) {
    errors.value.tonGiao =
        "Vui lòng chọn tôn giáo!";
  }

  if (
      !product.value.quyCach ||
      !product.value.quyCach.trim()
  ) {
    errors.value.quyCach =
        "Vui lòng nhập quy cách!";
  }

  if (
      !product.value.noiThat ||
      !product.value.noiThat.trim()
  ) {
    errors.value.noiThat =
        "Vui lòng nhập nội thất!";
  }

  if (
      !product.value.thietKe ||
      !product.value.thietKe.trim()
  ) {
    errors.value.thietKe =
        "Vui lòng nhập thiết kế!";
  }

  if (
      !product.value.xuatXu ||
      !product.value.xuatXu.trim()
  ) {
    errors.value.xuatXu =
        "Vui lòng nhập xuất xứ!";
  }

  if (!product.value.mauSac) {
    errors.value.mauSac =
        "Vui lòng chọn màu sắc!";
  }

  if (!product.value.vatLieu) {
    errors.value.vatLieu =
        "Vui lòng chọn vật liệu!";
  }

  if (
      !product.value.kichThuoc ||
      !product.value.kichThuoc.trim()
  ) {
    errors.value.kichThuoc =
        "Vui lòng nhập kích thước!";
  }

  if (
      !product.value.cnsx ||
      !product.value.cnsx.trim()
  ) {
    errors.value.cnsx =
        "Vui lòng nhập công nghệ sản xuất!";
  }

  const trongLuong = Number(
      product.value.trongLuong
  );

  if (
      product.value.trongLuong === "" ||
      product.value.trongLuong === null ||
      Number.isNaN(trongLuong) ||
      trongLuong <= 0
  ) {
    errors.value.trongLuong =
        "Trọng lượng phải là một số lớn hơn 0!";
  }

  if (imageFiles.value.length === 0 && imagePreviews.value.length === 0) {
    errors.value.hinhAnh =
        "Vui lòng tải lên ít nhất 1 hình ảnh sản phẩm!";
  }

  return Object.keys(errors.value).length === 0;
};

const scrollToFirstError = () => {
  requestAnimationFrame(() => {
    const element =
        document.querySelector(".has-error");

    element?.scrollIntoView({
      behavior: "smooth",
      block: "center"
    });
  });
};

const saveDraft = async () => {
  if (isSubmitting.value) {
    return;
  }

  isSubmitting.value = true;

  try {
    const payload = await buildPayload();

    payload.trangThai = 0;

    await api.post(API_URL, payload);

    alert("Đã lưu nháp sản phẩm!");

    await router.push(
        "/doi-tac/quan-ly-san-pham"
    );
  } catch (error) {
    console.error("===== LỖI LƯU NHÁP =====");
    console.error("URL:", error.config?.url);
    console.error("Status:", error.response?.status);
    console.error("Response:", error.response?.data);
    console.error("Message:", error.message);
    console.error("========================");

    alert(
        getBackendErrorMessage(
            error,
            "Không thể lưu nháp sản phẩm."
        )
    );
  } finally {
    isSubmitting.value = false;
  }
};

const publishProduct = async () => {
  if (isSubmitting.value) {
    return;
  }

  const isValid = validateProduct();

  console.log(
      "Validation result:",
      isValid,
      "Errors:",
      JSON.parse(JSON.stringify(errors.value))
  );

  if (!isValid) {
    scrollToFirstError();
    return;
  }

  isSubmitting.value = true;

  try {
    const payload = await buildPayload();

    if (props.editId) {
      // Sản phẩm đã duyệt -> chuyển về chờ duyệt
      if (product.value.trangThai === 1) {
        const confirmed = confirm(
          '⚠️ Sản phẩm này đã được duyệt.\n\nSau khi sửa, sản phẩm sẽ chuyển về trạng thái "Chờ duyệt" và tạm ẩn khỏi website cho đến khi quản lý duyệt lại.\n\nBạn có muốn tiếp tục?'
        );
        if (!confirmed) { isSubmitting.value = false; return; }
        payload.trangThai = 2; // Chờ duyệt
      }
      await api.put(`${API_URL}/${props.editId}`, payload);
      alert('Đã cập nhật sản phẩm thành công!');
      emit('saved');
    } else {
      await api.post(API_URL, payload);
      alert("Đã đăng sản phẩm thành công!");
      await router.push("/doi-tac/quan-ly-san-pham");
    }
  } catch (error) {
    console.error(
        "===== LỖI ĐĂNG SẢN PHẨM ====="
    );

    console.error("URL:", error.config?.url);
    console.error("Method:", error.config?.method);
    console.error("Status:", error.response?.status);
    console.error("Response:", error.response?.data);
    console.error("Message:", error.message);

    console.error(
        "================================"
    );

    alert(
        getBackendErrorMessage(
            error,
            "Không thể đăng sản phẩm. Vui lòng kiểm tra lại."
        )
    );
  } finally {
    isSubmitting.value = false;
  }
};

const cancelCreate = () => {
  const confirmed = confirm(
      "Bạn có chắc muốn hủy? Dữ liệu chưa lưu sẽ bị mất."
  );

  if (confirmed) {
    if (props.editId) {
      emit('close');
    } else {
      router.push("/doi-tac/quan-ly-san-pham");
    }
  }
};
</script>

<template>
  <div class="tao-sp-page">
    <!-- Top bar -->
    <div class="tao-sp-topbar">
      <button
          type="button"
          class="tao-sp-guide-btn"
      >
        <i class="fa-regular fa-circle-question"></i>
        Hướng dẫn tạo sản phẩm
      </button>
    </div>

    <div class="tao-sp-layout">
      <!-- CỘT TRÁI -->
      <div class="tao-sp-left">
        <!-- Section 1 -->
        <section class="tao-sp-section">
          <div class="tao-sp-section-head">
            <h2 class="tao-sp-section-title">
              1. Thông tin cơ bản
            </h2>
          </div>

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
              <label>
                Tên sản phẩm
                <span class="req">*</span>
              </label>

              <input
                  v-model="product.tenSanPham"
                  class="tao-sp-input"
                  :class="{
                  'has-error': errors.tenSanPham
                }"
                  placeholder="Nhập tên sản phẩm"
                  maxlength="120"
                  @input="clearError('tenSanPham')"
              />

              <span
                  v-if="errors.tenSanPham"
                  class="tao-sp-error-text"
              >
                {{ errors.tenSanPham }}
              </span>

              <span
                  v-else
                  class="tao-sp-char-count"
              >
                {{ tenSanPhamCount }}/120
              </span>
            </div>

            <div class="tao-sp-field">
              <label>
                Loại
                <span class="req">*</span>
              </label>

              <select
                  v-model="product.loai"
                  class="tao-sp-select"
                  :class="{
                  'has-error': errors.loai
                }"
                  @change="clearError('loai')"
              >
                <option value="">
                  Chọn loại sản phẩm
                </option>

                <option
                    v-for="option in loaiOptions"
                    :key="option"
                    :value="option"
                >
                  {{ option }}
                </option>
              </select>

              <span
                  v-if="errors.loai"
                  class="tao-sp-error-text"
              >
                {{ errors.loai }}
              </span>
            </div>

            <div class="tao-sp-field">
              <label>
                Giá bán
                <span class="req">*</span>
              </label>

              <div
                  class="tao-sp-input-wrap"
                  :class="{
                  'has-error': errors.giaTien
                }"
              >
                <input
                    v-model="product.giaTien"
                    type="number"
                    class="tao-sp-input"
                    placeholder="Nhập giá bán"
                    min="0"
                    @input="clearError('giaTien')"
                />

                <span class="tao-sp-suffix">
                  VND
                </span>
              </div>

              <span
                  v-if="errors.giaTien"
                  class="tao-sp-error-text"
              >
                {{ errors.giaTien }}
              </span>
            </div>

            <div class="tao-sp-field">
              <label>Khuyến mãi</label>

              <div class="tao-sp-promo-row">
                <select
                    v-model="product.khuyenMaiLoai"
                    class="tao-sp-select"
                    @change="clearError('khuyenMai')"
                >
                  <option value="PHAN_TRAM">
                    Phần trăm
                  </option>

                  <option value="SO_TIEN">
                    Số tiền
                  </option>
                </select>

                <div
                    class="tao-sp-input-wrap"
                    :class="{
                    'has-error': errors.khuyenMai
                  }"
                >
                  <input
                      v-model="product.khuyenMai"
                      type="number"
                      class="tao-sp-input"
                      placeholder="0"
                      min="0"
                      @input="clearError('khuyenMai')"
                  />

                  <span class="tao-sp-suffix">
                    {{
                      product.khuyenMaiLoai ===
                      "PHAN_TRAM"
                          ? "%"
                          : "VND"
                    }}
                  </span>
                </div>
              </div>

              <span
                  v-if="errors.khuyenMai"
                  class="tao-sp-error-text"
              >
                {{ errors.khuyenMai }}
              </span>
            </div>

            <div class="tao-sp-field">
              <label>
                Số lượng
                <span class="req">*</span>
              </label>

              <input
                  v-model="product.soLuong"
                  type="number"
                  class="tao-sp-input"
                  :class="{
                  'has-error': errors.soLuong
                }"
                  placeholder="Nhập số lượng"
                  min="0"
                  @input="clearError('soLuong')"
              />

              <span
                  v-if="errors.soLuong"
                  class="tao-sp-error-text"
              >
                {{ errors.soLuong }}
              </span>
            </div>

            <div class="tao-sp-field">
              <label>
                Tôn giáo
                <span class="req">*</span>
              </label>

              <select
                  v-model="product.tonGiao"
                  class="tao-sp-select"
                  :class="{
                  'has-error': errors.tonGiao
                }"
                  @change="clearError('tonGiao')"
              >
                <option value="">
                  Chọn tôn giáo
                </option>

                <option
                    v-for="option in tonGiaoOptions"
                    :key="option"
                    :value="option"
                >
                  {{ option }}
                </option>
              </select>

              <span
                  v-if="errors.tonGiao"
                  class="tao-sp-error-text"
              >
                {{ errors.tonGiao }}
              </span>
            </div>

            <div class="tao-sp-field">
              <label>
                Quy cách
                <span class="req">*</span>
              </label>

              <input
                  v-model="product.quyCach"
                  class="tao-sp-input"
                  :class="{
                  'has-error': errors.quyCach
                }"
                  placeholder="Nhập quy cách"
                  @input="clearError('quyCach')"
              />

              <span
                  v-if="errors.quyCach"
                  class="tao-sp-error-text"
              >
                {{ errors.quyCach }}
              </span>
            </div>
          </div>
        </section>

        <!-- Section 2 -->
        <section class="tao-sp-section">
          <div class="tao-sp-section-head">
            <h2 class="tao-sp-section-title">
              2. Thông tin nâng cao
            </h2>

            <button
                type="button"
                class="tao-sp-collapse-btn"
                @click="toggleAdvanced"
            >
              {{ showAdvanced ? "Ẩn" : "Hiện" }}

              <i
                  class="fa-solid"
                  :class="
                  showAdvanced
                    ? 'fa-chevron-up'
                    : 'fa-chevron-down'
                "
              ></i>
            </button>
          </div>

          <div
              v-show="showAdvanced"
              class="tao-sp-grid"
          >
            <div class="tao-sp-field">
              <label>
                Nội thất
                <span class="req">*</span>
              </label>

              <input
                  v-model="product.noiThat"
                  class="tao-sp-input"
                  :class="{
                  'has-error': errors.noiThat
                }"
                  placeholder="Nhập nội thất"
                  @input="clearError('noiThat')"
              />

              <span
                  v-if="errors.noiThat"
                  class="tao-sp-error-text"
              >
                {{ errors.noiThat }}
              </span>
            </div>

            <div class="tao-sp-field">
              <label>Mã đối tác</label>

              <input
                  :value="
                  product.maDoiTac ||
                  'Tự động lấy từ tài khoản'
                "
                  class="tao-sp-input"
                  disabled
              />
            </div>

            <div class="tao-sp-field">
              <label>
                Thiết kế
                <span class="req">*</span>
              </label>

              <input
                  v-model="product.thietKe"
                  class="tao-sp-input"
                  :class="{
                  'has-error': errors.thietKe
                }"
                  placeholder="Nhập thiết kế"
                  @input="clearError('thietKe')"
              />

              <span
                  v-if="errors.thietKe"
                  class="tao-sp-error-text"
              >
                {{ errors.thietKe }}
              </span>
            </div>

            <div class="tao-sp-field">
              <label>
                Xuất xứ
                <span class="req">*</span>
              </label>

              <input
                  v-model="product.xuatXu"
                  class="tao-sp-input"
                  :class="{
                  'has-error': errors.xuatXu
                }"
                  placeholder="Nhập xuất xứ"
                  @input="clearError('xuatXu')"
              />

              <span
                  v-if="errors.xuatXu"
                  class="tao-sp-error-text"
              >
                {{ errors.xuatXu }}
              </span>
            </div>

            <div class="tao-sp-field span-2">
              <label>
                Màu sắc
                <span class="req">*</span>
              </label>

              <div
                  class="tao-sp-color-row"
                  :class="{
                  'has-error': errors.mauSac
                }"
              >
                <button
                    v-for="swatch in allSwatches"
                    :key="swatch.value"
                    type="button"
                    class="tao-sp-swatch"
                    :class="{
                    active: isColorActive(swatch)
                  }"
                    :style="{
                    background: swatch.value
                  }"
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

              <span
                  v-if="errors.mauSac"
                  class="tao-sp-error-text"
              >
                {{ errors.mauSac }}
              </span>
            </div>

            <div class="tao-sp-field">
              <label>
                Vật liệu
                <span class="req">*</span>
              </label>

              <select
                  v-model="product.vatLieu"
                  class="tao-sp-select"
                  :class="{
                  'has-error': errors.vatLieu
                }"
                  @change="clearError('vatLieu')"
              >
                <option value="">
                  Chọn vật liệu
                </option>

                <option
                    v-for="option in vatLieuOptions"
                    :key="option"
                    :value="option"
                >
                  {{ option }}
                </option>
              </select>

              <span
                  v-if="errors.vatLieu"
                  class="tao-sp-error-text"
              >
                {{ errors.vatLieu }}
              </span>
            </div>

            <div class="tao-sp-field">
              <label>Trạng thái</label>

              <select
                  v-model.number="product.trangThai"
                  class="tao-sp-select"
              >
                <option :value="1">
                  Đang bán
                </option>

                <option :value="0">
                  Ẩn
                </option>
              </select>
            </div>

            <div class="tao-sp-field">
              <label>
                Kích thước
                <span class="req">*</span>
              </label>

              <input
                  v-model="product.kichThuoc"
                  class="tao-sp-input"
                  :class="{
                  'has-error': errors.kichThuoc
                }"
                  placeholder="VD: 120x60x80cm"
                  @input="clearError('kichThuoc')"
              />

              <span
                  v-if="errors.kichThuoc"
                  class="tao-sp-error-text"
              >
                {{ errors.kichThuoc }}
              </span>
            </div>

            <div class="tao-sp-field">
              <label>
                Trọng lượng
                <span class="req">*</span>
              </label>

              <div
                  class="tao-sp-input-wrap"
                  :class="{
                  'has-error': errors.trongLuong
                }"
              >
                <input
                    v-model="product.trongLuong"
                    type="number"
                    class="tao-sp-input"
                    placeholder="Nhập trọng lượng"
                    min="0"
                    @input="clearError('trongLuong')"
                />

                <span class="tao-sp-suffix">
                  kg
                </span>
              </div>

              <span
                  v-if="errors.trongLuong"
                  class="tao-sp-error-text"
              >
                {{ errors.trongLuong }}
              </span>
            </div>

            <div class="tao-sp-field">
              <label>
                Công nghệ sản xuất
                <span class="req">*</span>
              </label>

              <input
                  v-model="product.cnsx"
                  class="tao-sp-input"
                  :class="{
                  'has-error': errors.cnsx
                }"
                  placeholder="Nhập CNSX"
                  @input="clearError('cnsx')"
              />

              <span
                  v-if="errors.cnsx"
                  class="tao-sp-error-text"
              >
                {{ errors.cnsx }}
              </span>
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

              <span class="tao-sp-char-count">
                {{ ghiChuCount }}/300
              </span>
            </div>
          </div>
        </section>
      </div>

      <!-- CỘT PHẢI -->
      <div class="tao-sp-right">
        <!-- Section 3 -->
        <section class="tao-sp-section">
          <h2 class="tao-sp-section-title">
            3. Hình ảnh sản phẩm
          </h2>

          <label
              class="tao-sp-upload-zone"
              :class="{
              'drag-over': isDragOver,
              'has-error': errors.hinhAnh
            }"
              for="product-image-upload"
              @dragover.prevent="isDragOver = true"
              @dragleave.prevent="isDragOver = false"
              @drop.prevent="handleDrop"
          >
            <i class="fa-solid fa-cloud-arrow-up"></i>

            <span class="tao-sp-upload-title">
              Tải ảnh lên
            </span>

            <span class="tao-sp-upload-hint">
              Kéo thả hoặc click để chọn ảnh
            </span>

            <span class="tao-sp-upload-format">
              Định dạng: JPG, PNG, WEBP
              (Tối đa 5MB)
            </span>
          </label>

          <span
              v-if="errors.hinhAnh"
              class="tao-sp-error-text"
              style="
              margin-top: -10px;
              margin-bottom: 12px;
            "
          >
            {{ errors.hinhAnh }}
          </span>

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
                v-for="(image, index) in imagePreviews"
                :key="`${image}-${index}`"
                class="tao-sp-gallery-item"
            >
              <img
                  :src="image"
                  alt="Ảnh sản phẩm"
              />

              <button
                  type="button"
                  class="tao-sp-gallery-remove"
                  @click="removeImage(index)"
              >
                <i class="fa-solid fa-xmark"></i>
              </button>
            </div>

            <label
                v-if="imagePreviews.length < MAX_IMAGES"
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
            Có thể tải lên tối đa
            {{ MAX_IMAGES }} ảnh
          </p>
        </section>

        <!-- Section 4 -->
        <section class="tao-sp-section">
          <h2 class="tao-sp-section-title">
            4. Thông tin chi tiết
          </h2>

          <p class="tao-sp-detail-intro">
            Thêm nội dung mô tả chi tiết về sản phẩm
            của bạn
          </p>

          <div class="tao-sp-detail-toolbar">
            <button
                type="button"
                class="primary-outline"
                @click="addDetailBlock('title')"
            >
              <i class="fa-solid fa-plus"></i>
              Thêm tiêu đề
            </button>

            <button
                type="button"
                @click="addDetailBlock('text')"
            >
              <i class="fa-solid fa-align-left"></i>
              Thêm nội dung
            </button>

            <button
                type="button"
                @click="addDetailBlock('image')"
            >
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
              <label v-if="block.type === 'title'">
                Tiêu đề
              </label>

              <label v-else-if="block.type === 'text'">
                Nội dung văn bản
              </label>

              <label v-else-if="block.type === 'image'">
                Hình ảnh chi tiết
              </label>

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
                  style="
                  font-weight: bold;
                  font-size: 14px;
                "
              />

              <span class="tao-sp-char-count">
                {{ block.content.length }}/100
              </span>
            </template>

            <template v-else-if="block.type === 'text'">
              <div class="tao-sp-editor-toolbar">
                <button
                    type="button"
                    title="In đậm"
                    @mousedown.prevent="
                    applyFormat(index, 'bold')
                  "
                >
                  <b>B</b>
                </button>

                <button
                    type="button"
                    title="In nghiêng"
                    @mousedown.prevent="
                    applyFormat(index, 'italic')
                  "
                >
                  <i>I</i>
                </button>

                <button
                    type="button"
                    title="Gạch chân"
                    @mousedown.prevent="
                    applyFormat(index, 'underline')
                  "
                >
                  <u>U</u>
                </button>

                <span class="sep"></span>

                <button
                    type="button"
                    title="Danh sách"
                    @mousedown.prevent="
                    applyFormat(
                      index,
                      'insertUnorderedList'
                    )
                  "
                >
                  <i class="fa-solid fa-list-ul"></i>
                </button>

                <button
                    type="button"
                    title="Căn trái"
                    @mousedown.prevent="
                    applyFormat(index, 'justifyLeft')
                  "
                >
                  <i class="fa-solid fa-align-left"></i>
                </button>

                <button
                    type="button"
                    title="Căn giữa"
                    @mousedown.prevent="
                    applyFormat(index, 'justifyCenter')
                  "
                >
                  <i class="fa-solid fa-align-center"></i>
                </button>

                <button
                    type="button"
                    title="Liên kết"
                    @mousedown.prevent="
                    applyFormat(index, 'createLink')
                  "
                >
                  <i class="fa-solid fa-link"></i>
                </button>

                <span class="spacer"></span>

                <button
                    type="button"
                    title="Xóa nội dung"
                    @click="clearEditorContent(index)"
                >
                  <i class="fa-regular fa-trash-can"></i>
                </button>
              </div>

              <div
                  :id="`editor-${index}`"
                  class="tao-sp-editor-area"
                  contenteditable="true"
                  data-placeholder="Nhập nội dung chi tiết..."
                  @input="
                  syncEditorContent(index, $event)
                "
              ></div>

              <div class="tao-sp-editor-footer">
                {{
                  getEditorCharCount(block.content)
                }}
                ký tự
              </div>
            </template>

            <template v-else-if="block.type === 'image'">
              <label
                  v-if="!block.previewUrl"
                  class="tao-sp-upload-zone"
                  :for="`detail-img-${index}`"
                  style="
                  min-height: 120px;
                  padding: 30px;
                "
              >
                <i class="fa-solid fa-cloud-arrow-up"></i>

                <span class="tao-sp-upload-title">
                  Chọn ảnh
                </span>

                <span class="tao-sp-upload-format">
                  Định dạng: JPG, PNG, WEBP
                  (Tối đa 5MB)
                </span>
              </label>

              <input
                  :id="`detail-img-${index}`"
                  type="file"
                  accept="image/jpeg,image/png,image/webp"
                  hidden
                  @change="
                  handleDetailImageUpload(
                    index,
                    $event
                  )
                "
              />

              <div
                  v-if="block.previewUrl"
                  class="tao-sp-detail-img-preview"
                  style="
                  position: relative;
                  display: inline-block;
                  overflow: hidden;
                  border: 1px solid #e5e7eb;
                  border-radius: 8px;
                "
              >
                <img
                    :src="block.previewUrl"
                    alt="Ảnh mô tả chi tiết"
                    style="
                    display: block;
                    max-width: 100%;
                    max-height: 200px;
                    object-fit: contain;
                  "
                />

                <label
                    :for="`detail-img-${index}`"
                    class="tao-sp-detail-img-change"
                    style="
                    position: absolute;
                    right: 8px;
                    bottom: 8px;
                    padding: 4px 10px;
                    border-radius: 6px;
                    color: #ffffff;
                    background: rgba(0, 0, 0, 0.6);
                    font-size: 12px;
                    cursor: pointer;
                  "
                >
                  Thay ảnh
                </label>
              </div>
            </template>
          </div>

          <div
              style="
              display: flex;
              gap: 8px;
            "
          >
            <button
                type="button"
                class="tao-sp-add-block-btn"
                style="flex: 1"
                @click="addDetailBlock('title')"
            >
              <i class="fa-solid fa-plus"></i>
              Thêm tiêu đề
            </button>

            <button
                type="button"
                class="tao-sp-add-block-btn"
                style="flex: 1"
                @click="addDetailBlock('text')"
            >
              <i class="fa-solid fa-align-left"></i>
              Thêm nội dung
            </button>

            <button
                type="button"
                class="tao-sp-add-block-btn"
                style="flex: 1"
                @click="addDetailBlock('image')"
            >
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
          v-if="!props.editId"
          type="button"
          class="tao-sp-btn-draft"
          :disabled="isSubmitting"
          @click="saveDraft"
      >
        <i
            v-if="isSubmitting"
            class="fa-solid fa-circle-notch fa-spin"
            style="margin-right: 6px"
        ></i>

        {{
          isSubmitting
              ? "Đang lưu..."
              : "Lưu nháp"
        }}
      </button>

      <button
          type="button"
          class="tao-sp-btn-publish"
          :disabled="isSubmitting"
          @click="publishProduct"
      >
        <i
            v-if="!isSubmitting"
            class="fa-solid fa-paper-plane"
        ></i>

        <i
            v-else
            class="fa-solid fa-circle-notch fa-spin"
        ></i>

        {{
          isSubmitting
              ? (props.editId ? "Đang lưu..." : "Đang đăng...")
              : (props.editId ? "Lưu thay đổi" : "Đăng sản phẩm")
        }}
      </button>
    </div>
  </div>
</template>

<style
    scoped
    src="../../assets/styles/doitac/TaoSanPham/TrangTaoSanPham.css"
></style>