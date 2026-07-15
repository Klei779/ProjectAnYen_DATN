<template>
  <div class="container-fluid p-4">
    <div class="row g-4">
      <div class="col-xl-8 col-lg-7">
        <div class="card shadow-sm border-0">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-4">
              <div>
                <h4 class="fw-bold mb-1">Giao công việc cho nhân viên trực tiếp</h4>
                <p class="text-secondary mb-0">Tạo khách hàng và gửi thông báo tiếp nhận đến nhân viên được chọn.</p>
              </div>
              <el-button :loading="locating" @click="timDiaChi">Xác định vị trí</el-button>
            </div>

            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label fw-semibold">Họ và tên <span class="text-danger">*</span></label>
                <input v-model.trim="form.tenKhachHang" class="form-control" placeholder="Nhập họ và tên khách hàng" />
              </div>
              <div class="col-md-6">
                <label class="form-label fw-semibold">Số điện thoại <span class="text-danger">*</span></label>
                <input v-model.trim="form.soDienThoai" class="form-control" inputmode="numeric" placeholder="Nhập số điện thoại" />
              </div>
              <div class="col-12">
                <label class="form-label fw-semibold">Địa chỉ <span class="text-danger">*</span></label>
                <el-input v-model="form.diaChi" @blur="timDiaChi" placeholder="Nhập địa chỉ khách hàng" />
              </div>
              <div class="col-md-6">
                <label class="form-label fw-semibold">Nhu cầu hỗ trợ</label>
                <textarea v-model.trim="form.nhuCauHoTro" class="form-control" rows="3" placeholder="Nội dung khách hàng cần hỗ trợ"></textarea>
              </div>
              <div class="col-md-6">
                <label class="form-label fw-semibold">Ghi chú</label>
                <textarea v-model.trim="form.ghiChu" class="form-control" rows="3" placeholder="Ghi chú thêm"></textarea>
              </div>
            </div>

            <div class="map-wrapper mt-4">
              <l-map ref="mapRef" v-model:zoom="zoom" :center="center" style="height: 380px">
                <l-tile-layer
                    url="https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}"
                    attribution="Tiles © Esri"
                />
                <l-marker :lat-lng="marker"><l-popup>{{ diaChiChiTiet || form.diaChi || "Vị trí khách hàng" }}</l-popup></l-marker>
              </l-map>
            </div>
          </div>
        </div>
      </div>

      <div class="col-xl-4 col-lg-5">
        <div class="card shadow-sm border-0 mb-4">
          <div class="card-body">
            <h5 class="fw-bold">Quy trình</h5>
            <ol class="small text-secondary mt-3 mb-0">
              <li>Nhập thông tin khách hàng.</li>
              <li>Xác định vị trí để sắp xếp theo khoảng cách.</li>
              <li>Chọn nhân viên trực tiếp đang hoạt động.</li>
              <li>Nhân viên sẽ nhận nút tiếp nhận hoặc từ chối trong thông báo.</li>
            </ol>
          </div>
        </div>

        <div class="card shadow-sm border-0">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h5 class="fw-bold mb-0">Nhân viên trực tiếp</h5>
              <el-button link :loading="loadingEmployees" @click="loadEmployees">Tải lại</el-button>
            </div>

            <el-table v-loading="loadingEmployees" :data="employees" border stripe empty-text="Không có nhân viên đang hoạt động">
              <el-table-column prop="hoTen" label="Nhân viên" min-width="130" />
              <el-table-column label="Khoảng cách" width="105">
                <template #default="{ row }">{{ formatDistance(row.khoangCachKm) }}</template>
              </el-table-column>
              <el-table-column label="" width="82" align="center">
                <template #default="{ row }">
                  <el-button type="danger" size="small" :loading="assigningId === row.maNhanVien" @click="giaoCongViec(row)">Giao</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { LMap, LMarker, LPopup, LTileLayer } from "@vue-leaflet/vue-leaflet";
import api from "../../api/api.js";
import { geocodeAddress } from "../../services/GeocodingService";

const API_URL = "/api/nhan-vien/truc-tuyen/cong-viec";
const form = reactive({
  tenKhachHang: "",
  soDienThoai: "",
  diaChi: "",
  nhuCauHoTro: "",
  ghiChu: "",
  latitude: null,
  longitude: null
});

const mapRef = ref(null);
const zoom = ref(13);
const center = ref([10.776889, 106.700806]);
const marker = ref([10.776889, 106.700806]);
const diaChiChiTiet = ref("");
const employees = ref([]);
const locating = ref(false);
const loadingEmployees = ref(false);
const assigningId = ref(null);

onMounted(loadEmployees);

async function timDiaChi() {
  if (!form.diaChi.trim()) return;
  locating.value = true;
  try {
    const response = await geocodeAddress(form.diaChi.trim());
    const lat = Number(response.data.lat);
    const lng = Number(response.data.lon);
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) throw new Error("Tọa độ không hợp lệ");

    form.latitude = lat;
    form.longitude = lng;
    center.value = [lat, lng];
    marker.value = [lat, lng];
    zoom.value = 17;
    diaChiChiTiet.value = response.data.diaChiChiTiet || form.diaChi;

    await nextTick();
    mapRef.value?.leafletObject?.flyTo([lat, lng], 17);
    await loadEmployees();
  } catch (error) {
    console.error(error);
    ElMessage.warning("Không xác định được vị trí; bạn vẫn có thể giao việc theo danh sách nhân viên");
  } finally {
    locating.value = false;
  }
}

async function loadEmployees() {
  loadingEmployees.value = true;
  try {
    const params = {};
    if (form.latitude != null && form.longitude != null) {
      params.latitude = form.latitude;
      params.longitude = form.longitude;
    }
    const response = await api.get(`${API_URL}/nhan-vien-truc-tiep`, { params });
    employees.value = Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "Không tải được danh sách nhân viên trực tiếp"));
  } finally {
    loadingEmployees.value = false;
  }
}

async function giaoCongViec(employee) {
  if (!form.tenKhachHang.trim() || !form.soDienThoai.trim() || !form.diaChi.trim()) {
    ElMessage.warning("Vui lòng nhập đầy đủ họ tên, số điện thoại và địa chỉ");
    return;
  }
  if (!/^[0-9]{9,20}$/.test(form.soDienThoai)) {
    ElMessage.warning("Số điện thoại phải gồm 9 đến 20 chữ số");
    return;
  }

  assigningId.value = employee.maNhanVien;
  try {
    const response = await api.post(API_URL, {
      tenKhachHang: form.tenKhachHang.trim(),
      soDienThoai: form.soDienThoai.trim(),
      diaChi: form.diaChi.trim(),
      maNhanVien: employee.maNhanVien,
      latitude: form.latitude,
      longitude: form.longitude,
      nhuCauHoTro: form.nhuCauHoTro.trim(),
      ghiChu: form.ghiChu.trim()
    });
    ElMessage.success(response.data?.message || `Đã giao việc cho ${employee.hoTen}`);
    resetForm();
    await loadEmployees();
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "Không thể giao công việc"));
  } finally {
    assigningId.value = null;
  }
}

function resetForm() {
  form.tenKhachHang = "";
  form.soDienThoai = "";
  form.diaChi = "";
  form.nhuCauHoTro = "";
  form.ghiChu = "";
  form.latitude = null;
  form.longitude = null;
  diaChiChiTiet.value = "";
}

function formatDistance(value) {
  return value == null ? "Chưa rõ" : `${Number(value).toFixed(2)} km`;
}

function getErrorMessage(error, fallback) {
  return error?.response?.data?.message || error?.response?.data?.error || fallback;
}
</script>

<style scoped>
.map-wrapper { overflow: hidden; border-radius: 12px; border: 1px solid #e5e7eb; }
.form-control { min-height: 40px; }
textarea.form-control { resize: vertical; }
</style>
