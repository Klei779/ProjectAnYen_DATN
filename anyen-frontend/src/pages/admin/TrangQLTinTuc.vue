<template>
  <div class="container-fluid py-4 tin-tuc-page">
    <div
        class="d-flex flex-wrap justify-content-between
               align-items-center gap-3 mb-4"
    >
      <div>
        <h3 class="fw-bold mb-1">
          Quản lý tin tức
        </h3>

        <p class="text-muted mb-0">
          Tạo, cập nhật, ẩn/hiện và xóa bài viết
          trên website.
        </p>
      </div>

      <button
          class="btn btn-main"
          @click="openCreate"
      >
        <i class="fa-solid fa-plus me-2"></i>
        Tạo tin tức
      </button>
    </div>

    <!-- THÔNG BÁO -->
    <div
        v-if="notice.text"
        class="alert d-flex justify-content-between
               align-items-center"
        :class="
          notice.type === 'success'
            ? 'alert-success'
            : 'alert-danger'
        "
    >
      <span>
        {{ notice.text }}
      </span>

      <button
          class="btn-close"
          @click="notice.text = ''"
      ></button>
    </div>

    <!-- BỘ LỌC -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body">
        <div class="row g-3 align-items-end">
          <div class="col-lg-5">
            <label class="form-label fw-semibold">
              Tìm theo tiêu đề
            </label>

            <input
                v-model.trim="filter.keyword"
                class="form-control"
                placeholder="Nhập tiêu đề..."
                @keyup.enter="applyFilter"
            />
          </div>

          <div class="col-lg-2 col-md-4">
            <label class="form-label fw-semibold">
              Loại tin
            </label>

            <select
                v-model="filter.loaiTin"
                class="form-select"
                @change="applyFilter"
            >
              <option value="">
                Tất cả
              </option>

              <option :value="1">
                Kiến thức
              </option>

              <option :value="2">
                Phong tục
              </option>

              <option :value="3">
                Thông báo
              </option>

              <option :value="4">
                Hoạt động
              </option>
            </select>
          </div>

          <div class="col-lg-2 col-md-4">
            <label class="form-label fw-semibold">
              Trạng thái
            </label>

            <select
                v-model="filter.trangThai"
                class="form-select"
                @change="applyFilter"
            >
              <option value="">
                Tất cả
              </option>

              <option :value="1">
                Đang hiển thị
              </option>

              <option :value="0">
                Đang ẩn
              </option>
            </select>
          </div>

          <div
              class="col-lg-3 col-md-4
                     d-flex gap-2"
          >
            <button
                class="btn btn-main flex-grow-1"
                @click="applyFilter"
            >
              <i class="fa-solid fa-filter me-1"></i>
              Lọc
            </button>

            <button
                class="btn btn-outline-secondary"
                @click="resetFilter"
            >
              <i class="fa-solid fa-rotate-left"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- BẢNG DANH SÁCH -->
    <div
        class="card border-0 shadow-sm
               overflow-hidden"
    >
      <div
          class="card-header bg-white py-3
                 d-flex justify-content-between
                 align-items-center"
      >
        <div>
          <h5 class="fw-bold mb-1">
            Danh sách bài viết
          </h5>

          <small class="text-muted">
            Có {{ total }} bài viết
          </small>
        </div>

        <button
            class="btn btn-outline-secondary btn-sm"
            :disabled="loading"
            @click="loadNews"
        >
          <i
              class="fa-solid fa-rotate me-1"
              :class="{ 'fa-spin': loading }"
          ></i>

          Tải lại
        </button>
      </div>

      <div class="table-responsive">
        <table
            class="table table-hover
                   align-middle mb-0 news-table"
        >
          <thead class="table-light">
          <tr>
            <th>Mã</th>
            <th>Bài viết</th>
            <th>Loại tin</th>
            <th>Trạng thái</th>
            <th>Ngày đăng</th>
            <th class="text-end">
              Hành động
            </th>
          </tr>
          </thead>

          <tbody>
          <tr v-if="loading">
            <td
                colspan="6"
                class="text-center py-5 text-muted"
            >
              <span
                  class="spinner-border
                         spinner-border-sm me-2"
              ></span>

              Đang tải dữ liệu...
            </td>
          </tr>

          <tr v-else-if="items.length === 0">
            <td
                colspan="6"
                class="text-center py-5 text-muted"
            >
              Chưa có bài viết phù hợp.
            </td>
          </tr>

          <template v-else>
            <tr
                v-for="item in items"
                :key="item.maTinTuc"
            >
              <td class="fw-bold text-brown">
                #{{ item.maTinTuc }}
              </td>

              <td>
                <div
                    class="d-flex align-items-center
                           gap-3 article-cell"
                >
                  <img
                      class="thumb"
                      :src="imageUrl(item.anhDaiDien)"
                      :alt="item.tieuDe"
                      @error="fallbackImage"
                  />

                  <div class="article-text">
                    <div
                        class="fw-semibold text-truncate"
                        :title="item.tieuDe"
                    >
                      {{ item.tieuDe }}
                    </div>

                    <small
                        class="text-muted d-block
                               text-truncate"
                        :title="item.tomTat"
                    >
                      {{ item.tomTat }}
                    </small>
                  </div>
                </div>
              </td>

              <td>
                <span
                    class="badge rounded-pill"
                    :class="
                      categoryClass(item.loaiTin)
                    "
                >
                  {{ categoryName(item.loaiTin) }}
                </span>
              </td>

              <td>
                <span
                    class="badge rounded-pill"
                    :class="
                      Number(item.trangThai) === 1
                        ? 'text-bg-success-subtle text-success-emphasis'
                        : 'text-bg-secondary-subtle text-secondary-emphasis'
                    "
                >
                  {{
                    Number(item.trangThai) === 1
                        ? "Đang hiển thị"
                        : "Đang ẩn"
                  }}
                </span>
              </td>

              <td class="text-muted text-nowrap">
                {{ formatDate(item.ngayDang) }}
              </td>

              <td>
                <div
                    class="d-flex justify-content-end
                           gap-2"
                >
                  <!-- XEM -->
                  <button
                      class="btn action view"
                      title="Xem"
                      @click="viewPublic(item)"
                  >
                    <i class="fa-regular fa-eye"></i>
                  </button>

                  <!-- SỬA -->
                  <button
                      class="btn action edit"
                      title="Sửa"
                      @click="
                        openEdit(item.maTinTuc)
                      "
                  >
                    <i
                        class="fa-regular
                               fa-pen-to-square"
                    ></i>
                  </button>

                  <!-- ẨN / HIỆN -->
                  <button
                      class="btn action"
                      :class="
                        Number(item.trangThai) === 1
                          ? 'hide-news'
                          : 'show-news'
                      "
                      :disabled="
                        rowLoading[item.maTinTuc]
                      "
                      @click="toggleStatus(item)"
                  >
                    <span
                        v-if="
                          rowLoading[item.maTinTuc]
                        "
                        class="spinner-border
                               spinner-border-sm"
                    ></span>

                    <i
                        v-else
                        :class="
                          Number(item.trangThai) === 1
                            ? 'fa-solid fa-eye-slash'
                            : 'fa-solid fa-eye'
                        "
                    ></i>
                  </button>

                  <!-- XÓA -->
                  <button
                      class="btn action delete"
                      title="Xóa"
                      @click="deleteTarget = item"
                  >
                    <i
                        class="fa-regular
                               fa-trash-can"
                    ></i>
                  </button>
                </div>
              </td>
            </tr>
          </template>
          </tbody>
        </table>
      </div>

      <!-- PHÂN TRANG -->
      <div
          v-if="totalPages > 1"
          class="card-footer bg-white
                 d-flex justify-content-between
                 align-items-center"
      >
        <small class="text-muted">
          Trang {{ page }} / {{ totalPages }}
        </small>

        <ul class="pagination pagination-sm mb-0">
          <li
              class="page-item"
              :class="{ disabled: page === 1 }"
          >
            <button
                class="page-link"
                @click="changePage(page - 1)"
            >
              Trước
            </button>
          </li>

          <li
              v-for="p in pages"
              :key="p"
              class="page-item"
              :class="{ active: p === page }"
          >
            <button
                class="page-link"
                @click="changePage(p)"
            >
              {{ p }}
            </button>
          </li>

          <li
              class="page-item"
              :class="{
                disabled: page === totalPages
              }"
          >
            <button
                class="page-link"
                @click="changePage(page + 1)"
            >
              Sau
            </button>
          </li>
        </ul>
      </div>
    </div>

    <!-- MODAL TẠO / SỬA -->
    <div
        v-if="showForm"
        class="modal-layer"
        @click.self="closeForm"
    >
      <div class="news-modal">
        <div class="modal-head">
          <div>
            <h4 class="fw-bold mb-1">
              {{
                editingId
                    ? "Chỉnh sửa tin tức"
                    : "Tạo tin tức mới"
              }}
            </h4>

            <small class="text-muted">
              {{
                editingId
                    ? `Bài viết #${editingId}`
                    : "Nhập thông tin bài viết"
              }}
            </small>
          </div>

          <button
              class="btn-close"
              @click="closeForm"
          ></button>
        </div>

        <form @submit.prevent="saveNews">
          <div class="modal-content-body">
            <div class="row g-4">
              <!-- NỘI DUNG -->
              <div class="col-lg-8">
                <div class="mb-3">
                  <label
                      class="form-label fw-semibold"
                  >
                    Tiêu đề
                    <span class="text-danger">*</span>
                  </label>

                  <input
                      v-model="form.tieuDe"
                      class="form-control"
                      :class="{
                        'is-invalid': errors.tieuDe
                      }"
                      maxlength="150"
                      placeholder="Nhập tiêu đề"
                      @input="delete errors.tieuDe"
                  />

                  <div
                      class="d-flex
                             justify-content-between"
                  >
                    <small class="text-danger">
                      {{ errors.tieuDe }}
                    </small>

                    <small class="text-muted">
                      {{ form.tieuDe.length }}/150
                    </small>
                  </div>
                </div>

                <div class="mb-3">
                  <label
                      class="form-label fw-semibold"
                  >
                    Tóm tắt
                    <span class="text-danger">*</span>
                  </label>

                  <textarea
                      v-model="form.tomTat"
                      class="form-control"
                      :class="{
                        'is-invalid': errors.tomTat
                      }"
                      rows="3"
                      maxlength="500"
                      placeholder="Nhập nội dung tóm tắt"
                      @input="delete errors.tomTat"
                  ></textarea>

                  <div
                      class="d-flex
                             justify-content-between"
                  >
                    <small class="text-danger">
                      {{ errors.tomTat }}
                    </small>

                    <small class="text-muted">
                      {{ form.tomTat.length }}/500
                    </small>
                  </div>
                </div>

                <div>
                  <label
                      class="form-label fw-semibold"
                  >
                    Nội dung HTML
                    <span class="text-danger">*</span>
                  </label>

                  <textarea
                      v-model="form.noiDung"
                      class="form-control code-editor"
                      :class="{
                        'is-invalid': errors.noiDung
                      }"
                      rows="18"
                      placeholder="<h2>Tiêu đề</h2><p>Nội dung...</p>"
                      @input="delete errors.noiDung"
                  ></textarea>

                  <small class="text-danger">
                    {{ errors.noiDung }}
                  </small>
                </div>
              </div>

              <!-- THÔNG TIN XUẤT BẢN -->
              <div class="col-lg-4">
                <div class="side-box mb-3">
                  <h6 class="fw-bold mb-3">
                    Thông tin xuất bản
                  </h6>

                  <label
                      class="form-label fw-semibold"
                  >
                    Loại tin
                    <span class="text-danger">*</span>
                  </label>

                  <select
                      v-model="form.loaiTin"
                      class="form-select mb-1"
                      :class="{
                        'is-invalid': errors.loaiTin
                      }"
                      @change="delete errors.loaiTin"
                  >
                    <option value="">
                      Chọn loại tin
                    </option>

                    <option :value="1">
                      Kiến thức
                    </option>

                    <option :value="2">
                      Phong tục
                    </option>

                    <option :value="3">
                      Thông báo
                    </option>

                    <option :value="4">
                      Hoạt động
                    </option>
                  </select>

                  <small
                      class="text-danger d-block mb-3"
                  >
                    {{ errors.loaiTin }}
                  </small>

                  <label
                      class="form-label fw-semibold"
                  >
                    Trạng thái
                  </label>

                  <select
                      v-model="form.trangThai"
                      class="form-select"
                  >
                    <option :value="1">
                      Hiển thị ngay
                    </option>

                    <option :value="0">
                      Lưu ở trạng thái ẩn
                    </option>
                  </select>
                </div>

                <!-- ẢNH -->
                <div class="side-box">
                  <h6 class="fw-bold mb-3">
                    Ảnh đại diện
                  </h6>

                  <div class="preview mb-3">
                    <img
                        v-if="preview"
                        :src="preview"
                        alt="Ảnh đại diện"
                        @error="fallbackImage"
                    />

                    <div
                        v-else
                        class="preview-empty"
                    >
                      <i
                          class="fa-regular
                                 fa-image fs-2"
                      ></i>

                      <span>
                        Chưa chọn ảnh
                      </span>
                    </div>
                  </div>

                  <label
                      class="btn btn-outline-main
                             w-100"
                  >
                    <i
                        class="fa-solid
                               fa-cloud-arrow-up me-1"
                    ></i>

                    {{
                      preview
                          ? "Đổi ảnh"
                          : "Chọn ảnh"
                    }}

                    <input
                        type="file"
                        hidden
                        accept="image/png,
                                image/jpeg,
                                image/webp,
                                image/gif"
                        @change="selectImage"
                    />
                  </label>

                  <button
                      v-if="preview"
                      class="btn btn-outline-danger
                             btn-sm w-100 mt-2"
                      type="button"
                      @click="removeImage"
                  >
                    Xóa ảnh
                  </button>

                  <small
                      class="text-muted d-block mt-2"
                  >
                    JPG, PNG, WEBP, GIF; tối đa
                    10 MB.
                  </small>

                  <small class="text-danger">
                    {{ errors.anhDaiDien }}
                  </small>

                  <div
                      v-if="uploading"
                      class="progress mt-3"
                  >
                    <div
                        class="progress-bar"
                        :style="{
                          width: `${progress}%`
                        }"
                    >
                      {{ progress }}%
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-foot">
            <button
                class="btn btn-outline-secondary"
                type="button"
                :disabled="saving || uploading"
                @click="closeForm"
            >
              Hủy
            </button>

            <button
                class="btn btn-main"
                type="submit"
                :disabled="saving || uploading"
            >
              <span
                  v-if="saving"
                  class="spinner-border
                         spinner-border-sm me-2"
              ></span>

              {{
                saving
                    ? "Đang lưu..."
                    : editingId
                        ? "Cập nhật bài viết"
                        : "Tạo bài viết"
              }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- MODAL XÓA -->
    <div
        v-if="deleteTarget"
        class="modal-layer"
        @click.self="deleteTarget = null"
    >
      <div class="confirm-box text-center">
        <i
            class="fa-regular
                   fa-trash-can delete-icon"
        ></i>

        <h4 class="fw-bold mt-3">
          Xóa bài viết?
        </h4>

        <p class="text-muted">
          Bài viết
          <strong>
            “{{ deleteTarget.tieuDe }}”
          </strong>
          sẽ bị xóa vĩnh viễn.
        </p>

        <div
            class="d-flex justify-content-center
                   gap-2 mt-4"
        >
          <button
              class="btn btn-outline-secondary"
              :disabled="deleting"
              @click="deleteTarget = null"
          >
            Không xóa
          </button>

          <button
              class="btn btn-danger"
              :disabled="deleting"
              @click="removeNews"
          >
            <span
                v-if="deleting"
                class="spinner-border
                       spinner-border-sm me-2"
            ></span>

            Xóa bài viết
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  computed,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
} from "vue";

import noImage
  from "../../assets/images/noimage.jpg";

import {
  changeTinTucStatus,
  createTinTuc,
  deleteTinTuc,
  getTinTucAdmin,
  getTinTucAdminById,
  updateTinTuc,
  uploadAnhTinTuc,
} from "../../services/tinTucAdminService.js";

const items = ref([]);
const loading = ref(false);
const total = ref(0);
const page = ref(1);
const totalPages = ref(0);

const pageSize = 10;

const rowLoading = reactive({});

const filter = reactive({
  keyword: "",
  loaiTin: "",
  trangThai: "",
});

const notice = reactive({
  type: "success",
  text: "",
});

let noticeTimer;

const showForm = ref(false);
const editingId = ref(null);
const saving = ref(false);

const selectedFile = ref(null);
const preview = ref("");
const uploading = ref(false);
const progress = ref(0);

let objectUrl;

const deleteTarget = ref(null);
const deleting = ref(false);

const form = reactive(emptyForm());
const errors = reactive({});

const pages = computed(() => {
  const result = [];

  const start = Math.max(
      1,
      Math.min(
          page.value - 2,
          totalPages.value - 4
      )
  );

  for (
      let current = start;
      current <= Math.min(
          totalPages.value,
          start + 4
      );
      current++
  ) {
    result.push(current);
  }

  return result;
});

onMounted(() => {
  loadNews();
});

onBeforeUnmount(() => {
  releasePreview();

  document.body.style.overflow = "";

  clearTimeout(noticeTimer);
});

function emptyForm() {
  return {
    tieuDe: "",
    tomTat: "",
    noiDung: "",
    anhDaiDien: "",
    loaiTin: "",
    trangThai: 1,
  };
}

async function loadNews() {
  loading.value = true;

  try {
    const data = await getTinTucAdmin({
      keyword:
          filter.keyword || undefined,

      loaiTin:
          filter.loaiTin === ""
              ? undefined
              : Number(filter.loaiTin),

      trangThai:
          filter.trangThai === ""
              ? undefined
              : Number(filter.trangThai),

      page: page.value,

      pageSize,
    });

    items.value =
        Array.isArray(data.items)
            ? data.items
            : [];

    total.value =
        Number(data.total || 0);

    totalPages.value =
        Number(data.totalPages || 0);

    if (
        page.value > totalPages.value
        && totalPages.value > 0
    ) {
      page.value = totalPages.value;

      await loadNews();
    }
  } catch (error) {
    showNotice(
        "error",
        errorMessage(
            error,
            "Không thể tải tin tức."
        )
    );
  } finally {
    loading.value = false;
  }
}

function applyFilter() {
  page.value = 1;

  loadNews();
}

function resetFilter() {
  Object.assign(
      filter,
      {
        keyword: "",
        loaiTin: "",
        trangThai: "",
      }
  );

  applyFilter();
}

function changePage(value) {
  if (
      value >= 1
      && value <= totalPages.value
      && value !== page.value
  ) {
    page.value = value;

    loadNews();
  }
}

function resetForm() {
  Object.assign(
      form,
      emptyForm()
  );

  Object.keys(errors).forEach(
      key => delete errors[key]
  );

  editingId.value = null;
  selectedFile.value = null;
  preview.value = "";
  progress.value = 0;

  releasePreview();
}

function openCreate() {
  resetForm();

  showForm.value = true;

  document.body.style.overflow =
      "hidden";
}

async function openEdit(id) {
  resetForm();

  editingId.value = id;
  showForm.value = true;
  saving.value = true;

  document.body.style.overflow =
      "hidden";

  try {
    const item =
        await getTinTucAdminById(id);

    Object.assign(
        form,
        {
          tieuDe:
              item.tieuDe || "",

          tomTat:
              item.tomTat || "",

          noiDung:
              item.noiDung || "",

          anhDaiDien:
              item.anhDaiDien || "",

          loaiTin:
              item.loaiTin ?? "",

          trangThai:
              Number(item.trangThai) === 0
                  ? 0
                  : 1,
        }
    );

    preview.value =
        item.anhDaiDien
            ? imageUrl(item.anhDaiDien)
            : "";
  } catch (error) {
    closeForm(true);

    showNotice(
        "error",
        errorMessage(
            error,
            "Không thể tải bài viết."
        )
    );
  } finally {
    saving.value = false;
  }
}

function closeForm(force = false) {
  if (
      !force
      && (
          saving.value
          || uploading.value
      )
  ) {
    return;
  }

  showForm.value = false;

  document.body.style.overflow = "";

  resetForm();
}

function validate() {
  Object.keys(errors).forEach(
      key => delete errors[key]
  );

  if (!form.tieuDe.trim()) {
    errors.tieuDe =
        "Vui lòng nhập tiêu đề.";
  }

  if (!form.tomTat.trim()) {
    errors.tomTat =
        "Vui lòng nhập tóm tắt.";
  }

  if (!form.noiDung.trim()) {
    errors.noiDung =
        "Vui lòng nhập nội dung.";
  }

  if (
      ![1, 2, 3, 4].includes(
          Number(form.loaiTin)
      )
  ) {
    errors.loaiTin =
        "Vui lòng chọn loại tin.";
  }

  return Object.keys(errors).length === 0;
}

async function saveNews() {
  if (!validate()) {
    showNotice(
        "error",
        "Vui lòng kiểm tra các trường bắt buộc."
    );

    return;
  }

  saving.value = true;

  try {
    let image =
        form.anhDaiDien || null;

    if (selectedFile.value) {
      uploading.value = true;
      progress.value = 0;

      const result =
          await uploadAnhTinTuc(
              selectedFile.value,

              event => {
                if (event.total) {
                  progress.value = Math.round(
                      (
                          event.loaded * 100
                      ) / event.total
                  );
                }
              }
          );

      image = result.url;

      uploading.value = false;
    }

    const payload = {
      tieuDe:
          form.tieuDe.trim(),

      tomTat:
          form.tomTat.trim(),

      noiDung:
          form.noiDung.trim(),

      anhDaiDien:
      image,

      loaiTin:
          Number(form.loaiTin),

      trangThai:
          Number(form.trangThai),
    };

    if (editingId.value) {
      await updateTinTuc(
          editingId.value,
          payload
      );
    } else {
      await createTinTuc(payload);
    }

    showNotice(
        "success",
        editingId.value
            ? "Cập nhật tin tức thành công."
            : "Tạo tin tức thành công."
    );

    closeForm(true);

    page.value = 1;

    await loadNews();
  } catch (error) {
    uploading.value = false;

    backendErrors(error);

    showNotice(
        "error",
        errorMessage(
            error,
            "Không thể lưu bài viết."
        )
    );
  } finally {
    saving.value = false;
  }
}

async function toggleStatus(item) {
  const newStatus =
      Number(item.trangThai) === 1
          ? 0
          : 1;

  rowLoading[item.maTinTuc] = true;

  try {
    await changeTinTucStatus(
        item.maTinTuc,
        newStatus
    );

    item.trangThai = newStatus;

    showNotice(
        "success",
        newStatus === 1
            ? "Đã hiển thị bài viết."
            : "Đã ẩn bài viết."
    );
  } catch (error) {
    showNotice(
        "error",
        errorMessage(
            error,
            "Không thể đổi trạng thái."
        )
    );
  } finally {
    rowLoading[item.maTinTuc] = false;
  }
}

async function removeNews() {
  if (!deleteTarget.value) {
    return;
  }

  deleting.value = true;

  try {
    await deleteTinTuc(
        deleteTarget.value.maTinTuc
    );

    deleteTarget.value = null;

    if (
        items.value.length === 1
        && page.value > 1
    ) {
      page.value--;
    }

    showNotice(
        "success",
        "Xóa tin tức thành công."
    );

    await loadNews();
  } catch (error) {
    showNotice(
        "error",
        errorMessage(
            error,
            "Không thể xóa bài viết."
        )
    );
  } finally {
    deleting.value = false;
  }
}

function selectImage(event) {
  const file =
      event.target.files?.[0];

  event.target.value = "";

  if (!file) {
    return;
  }

  if (!file.type.startsWith("image/")) {
    errors.anhDaiDien =
        "Tệp phải là hình ảnh.";

    return;
  }

  if (
      file.size
      > 10 * 1024 * 1024
  ) {
    errors.anhDaiDien =
        "Ảnh không được vượt quá 10 MB.";

    return;
  }

  delete errors.anhDaiDien;

  selectedFile.value = file;

  releasePreview();

  objectUrl =
      URL.createObjectURL(file);

  preview.value = objectUrl;
}

function removeImage() {
  selectedFile.value = null;

  form.anhDaiDien = "";

  preview.value = "";

  releasePreview();
}

function releasePreview() {
  if (objectUrl) {
    URL.revokeObjectURL(objectUrl);

    objectUrl = null;
  }
}

function backendErrors(error) {
  const data =
      error.response?.data;

  if (
      !data
      || typeof data !== "object"
  ) {
    return;
  }

  [
    "tieuDe",
    "tomTat",
    "noiDung",
    "anhDaiDien",
    "loaiTin",
  ].forEach(field => {
    if (
        typeof data[field] === "string"
    ) {
      errors[field] = data[field];
    }
  });
}

function errorMessage(
    error,
    fallback
) {
  const data =
      error.response?.data;

  if (typeof data === "string") {
    return data;
  }

  if (data?.message) {
    return data.message;
  }

  if (
      data
      && typeof data === "object"
  ) {
    return (
        Object.values(data)
            .find(
                value =>
                    typeof value === "string"
            )
        || fallback
    );
  }

  return fallback;
}

function showNotice(
    type,
    text
) {
  notice.type = type;
  notice.text = text;

  clearTimeout(noticeTimer);

  noticeTimer = setTimeout(
      () => {
        notice.text = "";
      },
      4500
  );
}

function categoryName(value) {
  return {
    1: "Kiến thức",
    2: "Phong tục",
    3: "Thông báo",
    4: "Hoạt động",
  }[Number(value)] || "Khác";
}

function categoryClass(value) {
  return {
        1: "text-bg-primary-subtle text-primary-emphasis",

        2: "purple",

        3: "text-bg-warning-subtle text-warning-emphasis",

        4: "text-bg-info-subtle text-info-emphasis",
      }[Number(value)]
      || "text-bg-secondary-subtle";
}

function formatDate(value) {
  if (!value) {
    return "Chưa cập nhật";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "Chưa cập nhật";
  }

  return new Intl.DateTimeFormat(
      "vi-VN",
      {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }
  ).format(date);
}

function imageUrl(path) {
  if (!path) {
    return noImage;
  }

  const value =
      String(path)
          .trim()
          .replaceAll("\\", "/");

  if (
      /^(https?:|data:|blob:)/i
          .test(value)
  ) {
    return value;
  }

  const baseUrl =
      import.meta.env.VITE_API_BASE_URL
      || "http://localhost:8080";

  if (value.startsWith("/")) {
    return baseUrl + value;
  }

  if (
      value.startsWith("images/")
      || value.startsWith("uploads/")
  ) {
    return `${baseUrl}/${value}`;
  }

  return (
      `${baseUrl}/images/tintuc/${value}`
  );
}

function fallbackImage(event) {
  event.currentTarget.onerror = null;

  event.currentTarget.src = noImage;
}

function viewPublic(item) {
  if (
      Number(item.trangThai) !== 1
  ) {
    showNotice(
        "error",
        "Bài viết đang ẩn."
    );

    return;
  }

  window.open(
      `/tin-tuc/${item.maTinTuc}`,
      "_blank",
      "noopener,noreferrer"
  );
}
</script>

<style scoped>
.tin-tuc-page {
  min-height: 100%;
  background: #f6f7fb;
}

.btn-main {
  color: #ffffff;
  background: #a77b49;
  border-color: #a77b49;
}

.btn-main:hover {
  color: #ffffff;
  background: #8d6538;
  border-color: #8d6538;
}

.btn-outline-main {
  color: #8d6538;
  border-color: #a77b49;
}

.text-brown {
  color: #8d6538;
}

.news-table {
  min-width: 1050px;
}

.news-table th {
  color: #667085;
  font-size: 12px;
  text-transform: uppercase;
  white-space: nowrap;
}

.article-cell {
  min-width: 380px;
}

.article-text {
  min-width: 0;
  max-width: 430px;
}

.thumb {
  width: 84px;
  height: 58px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.purple {
  color: #6f42c1;
  background: #eee7fb;
}

.action {
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.view {
  color: #2857a4;
  background: #edf4ff;
}

.edit {
  color: #99651e;
  background: #fff7e8;
}

.hide-news {
  color: #667085;
  background: #f2f4f7;
}

.show-news {
  color: #18794e;
  background: #ecfdf3;
}

.delete {
  color: #c4324d;
  background: #fff1f2;
}

.page-item.active .page-link {
  background: #a77b49;
  border-color: #a77b49;
}

.modal-layer {
  position: fixed;
  inset: 0;
  z-index: 12000;
  padding: 20px;
  background: rgba(15, 23, 42, 0.58);
  display: flex;
  align-items: center;
  justify-content: center;
}

.news-modal {
  width: min(1180px, 96vw);
  max-height: 94vh;
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow:
      0 24px 75px rgba(15, 23, 42, 0.3);
}

.modal-head,
.modal-foot {
  padding: 17px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.modal-head {
  border-bottom: 1px solid #e5e7eb;
}

.modal-foot {
  border-top: 1px solid #e5e7eb;
  justify-content: flex-end;
}

.modal-content-body {
  padding: 20px;
  max-height: calc(94vh - 145px);
  overflow-y: auto;
}

.side-box {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fafbfc;
}

.code-editor {
  min-height: 390px;
  font-family:
      Consolas,
      "Courier New",
      monospace;
}

.preview {
  height: 180px;
  border: 1px dashed #cfd5df;
  border-radius: 10px;
  overflow: hidden;
  background: #ffffff;
}

.preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #98a2b3;
}

.confirm-box {
  width: min(440px, 96vw);
  padding: 28px;
  border-radius: 16px;
  background: #ffffff;
  box-shadow:
      0 24px 75px rgba(15, 23, 42, 0.3);
}

.delete-icon {
  width: 60px;
  height: 60px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #c4324d;
  background: #fff1f2;
  font-size: 24px;
}

@media (max-width: 768px) {
  .modal-foot {
    flex-direction: column-reverse;
  }

  .modal-foot .btn {
    width: 100%;
  }
}
</style>