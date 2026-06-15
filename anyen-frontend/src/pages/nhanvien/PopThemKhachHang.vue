<script setup>
import { reactive, ref } from "vue";
import api from "../../api/api.js";

const emit = defineEmits(["close", "saved"]);

const API_URL = "/api/nhan-vien/khach-hang";

const saving = ref(false);
const errorMessage = ref("");

const form = reactive({
  tenKhachHang: "",
  cccd: "",
  diaChi: "",
  email: "",
  soDienThoai: "",
  ngayDangKy: "",
  nguonDangKy: "Tư vấn trực tiếp",
  nhuCauHoTro: "",
  ghiChu: "",
});

const close = () => {
  if (!saving.value) {
    emit("close");
  }
};

const buildPayload = () => {
  return {
    tenKhachHang: form.tenKhachHang.trim(),
    cccd: form.cccd.trim(),
    diaChi: form.diaChi.trim(),
    email: form.email.trim(),
    soDienThoai: form.soDienThoai.trim(),
    ngayDangKy: form.ngayDangKy || null,
    nguonDangKy: form.nguonDangKy.trim(),
    nhuCauHoTro: form.nhuCauHoTro.trim(),
    ghiChu: form.ghiChu.trim(),
  };
};

const submit = async () => {
  errorMessage.value = "";

  if (!form.tenKhachHang.trim()) {
    errorMessage.value = "Vui lòng nhập tên khách hàng";
    return;
  }

  try {
    saving.value = true;

    const res = await api.post(API_URL, buildPayload());

    emit("saved", res.data);
  } catch (error) {
    console.error("Lỗi thêm khách hàng:", error);

    errorMessage.value =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể thêm khách hàng";
  } finally {
    saving.value = false;
  }
};
</script>

<template>
  <div class="kh-popup-overlay" @click.self="close">
    <div class="kh-popup add-customer-popup">
      <div class="kh-popup-header">
        <div>
          <p class="eyebrow">KHÁCH HÀNG</p>

          <h3>Thêm khách hàng mới</h3>

          <span>
            Nhập thông tin theo các thuộc tính của khách hàng
          </span>
        </div>

        <button class="close-btn" type="button" @click="close">
          X
        </button>
      </div>

      <div class="kh-popup-body">
        <p v-if="errorMessage" class="form-error">
          {{ errorMessage }}
        </p>

        <div class="form-grid">
          <label>
            <span>Tên khách hàng <b>*</b></span>
            <input
                v-model="form.tenKhachHang"
                type="text"
                placeholder="Nhập tên khách hàng"
            />
          </label>

          <label>
            <span>Số điện thoại</span>
            <input
                v-model="form.soDienThoai"
                type="text"
                placeholder="Nhập số điện thoại"
            />
          </label>

          <label>
            <span>Email</span>
            <input
                v-model="form.email"
                type="email"
                placeholder="Nhập email"
            />
          </label>

          <label>
            <span>CCCD</span>
            <input
                v-model="form.cccd"
                type="text"
                placeholder="Nhập CCCD"
            />
          </label>

          <label>
            <span>Ngày đăng ký</span>
            <input
                v-model="form.ngayDangKy"
                type="datetime-local"
            />
          </label>

          <label>
            <span>Nguồn đăng ký</span>
            <select v-model="form.nguonDangKy">
              <option value="Tư vấn trực tiếp">Tư vấn trực tiếp</option>
              <option value="Hotline">Hotline</option>
              <option value="Website">Website</option>
              <option value="Facebook">Facebook</option>
              <option value="Khác">Khác</option>
            </select>
          </label>

          <label class="full">
            <span>Địa chỉ</span>
            <input
                v-model="form.diaChi"
                type="text"
                placeholder="Nhập địa chỉ"
            />
          </label>

          <label class="full">
            <span>Nhu cầu hỗ trợ</span>
            <textarea
                v-model="form.nhuCauHoTro"
                rows="3"
                placeholder="Ví dụ: tư vấn sản phẩm, gói dịch vụ, hợp đồng..."
            ></textarea>
          </label>

          <label class="full">
            <span>Ghi chú</span>
            <textarea
                v-model="form.ghiChu"
                rows="3"
                placeholder="Ghi chú thêm nếu có"
            ></textarea>
          </label>
        </div>
      </div>

      <div class="kh-popup-footer">
        <button
            class="cancel-btn"
            type="button"
            :disabled="saving"
            @click="close"
        >
          Hủy
        </button>

        <button
            class="save-btn"
            type="button"
            :disabled="saving"
            @click="submit"
        >
          {{ saving ? "Đang lưu..." : "Lưu khách hàng" }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.kh-popup-overlay,
.kh-popup-overlay *,
.kh-popup,
.kh-popup *,
.kh-popup input,
.kh-popup select,
.kh-popup textarea,
.kh-popup button {
  font-family: Arial, Helvetica, sans-serif !important;
}

.kh-popup-overlay {
  position: fixed;
  inset: 0;
  z-index: 10080;
  background: rgba(15, 23, 42, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px;
  box-sizing: border-box;
}

.kh-popup {
  width: min(94vw, 880px);
  max-height: 90vh;
  overflow: hidden;
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.28);
  display: flex;
  flex-direction: column;
}

.kh-popup-header {
  padding: 22px 26px;
  border-bottom: 1px solid #eef2f7;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
  flex-shrink: 0;
}

.eyebrow {
  margin: 0 0 8px;
  color: #dc2626;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.2;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.kh-popup-header h3 {
  margin: 0;
  color: #111827;
  font-size: 24px;
  font-weight: 500;
  line-height: 1.3;
}

.kh-popup-header span {
  display: block;
  margin-top: 8px;
  color: #64748b;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.5;
}

.close-btn {
  width: 38px;
  height: 38px;
  min-width: 38px;
  border: none;
  border-radius: 50%;
  background: #f8fafc;
  color: #334155;
  cursor: pointer;
  font-size: 15px;
  font-weight: 700;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: 0.18s ease;
}

.close-btn:hover {
  background: #fee2e2;
  color: #dc2626;
}

.kh-popup-body {
  padding: 22px 26px 26px;
  overflow-y: auto;
  flex: 1;
}

.form-error {
  margin: 0 0 16px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #fff1f2;
  color: #be123c;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 18px;
}

.form-grid label {
  color: #334155;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-grid label span {
  color: #334155;
  font-size: 14px;
  font-weight: 600;
}

.form-grid label b {
  color: #dc2626;
  font-weight: 700;
}

.form-grid input,
.form-grid select,
.form-grid textarea {
  width: 100%;
  border: 1px solid #dbe3ef;
  border-radius: 12px;
  background: #ffffff;
  color: #111827;
  font-size: 14px;
  font-weight: 400;
  outline: none;
  padding: 13px 14px;
  box-sizing: border-box;
  transition: 0.18s ease;
}

.form-grid input::placeholder,
.form-grid textarea::placeholder {
  color: #8b8b8b;
  font-weight: 400;
}

.form-grid textarea {
  resize: vertical;
  min-height: 96px;
  line-height: 1.5;
}

.form-grid input:focus,
.form-grid select:focus,
.form-grid textarea:focus {
  border-color: #dc2626;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
}

.full {
  grid-column: 1 / -1;
}

.kh-popup-footer {
  padding: 18px 26px;
  border-top: 1px solid #eef2f7;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-shrink: 0;
  background: #ffffff;
}

.cancel-btn,
.save-btn {
  height: 42px;
  border-radius: 12px;
  border: 1px solid transparent;
  padding: 0 20px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: 0.18s ease;
}

.cancel-btn {
  background: #ffffff;
  border-color: #dbe3ef;
  color: #475569;
}

.cancel-btn:hover {
  background: #f8fafc;
  color: #111827;
}

.save-btn {
  background: #ef3434;
  color: #ffffff;
  box-shadow: 0 10px 20px rgba(220, 38, 38, 0.22);
}

.save-btn:hover {
  background: #dc2626;
  box-shadow: 0 12px 24px rgba(220, 38, 38, 0.28);
}

.cancel-btn:disabled,
.save-btn:disabled {
  cursor: not-allowed;
  opacity: 0.75;
}

@media (max-width: 720px) {
  .kh-popup-overlay {
    padding: 10px;
  }

  .kh-popup {
    width: 100%;
    max-height: 94vh;
    border-radius: 14px;
  }

  .kh-popup-header,
  .kh-popup-body,
  .kh-popup-footer {
    padding-left: 16px;
    padding-right: 16px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .kh-popup-footer {
    flex-direction: column-reverse;
  }

  .cancel-btn,
  .save-btn {
    width: 100%;
  }
}
</style>