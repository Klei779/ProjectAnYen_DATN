<template>
  <div class="hotline-page">
    <!-- MAIN -->
    <main class="hotline-main">

      <!-- CONTENT -->
      <section class="page-content">
        <div class="content-layout">
          <!-- LEFT CONTENT -->
          <section class="customer-panel">
            <div class="panel-card customer-card">
              <h2>Thông tin khách hàng</h2>

              <div class="form-group">
                <label>
                  Họ và tên
                  <span>*</span>
                </label>

                <input
                    v-model="hoTenKhachHang"
                    type="text"
                    placeholder="Nhập họ và tên khách hàng"
                />
              </div>

              <div class="form-group">
                <label>
                  Số điện thoại
                  <span>*</span>
                </label>

                <input
                    v-model="soDienThoaiKhachHang"
                    type="tel"
                    placeholder="Nhập số điện thoại khách hàng"
                />
              </div>

              <div class="form-group address-group">
                <label>
                  Địa chỉ
                  <span>*</span>
                </label>

                <div class="address-control">
                  <input
                      v-model="diaChiA"
                      type="text"
                      placeholder="Nhập địa chỉ khách hàng"
                      @keyup.enter="timDiaChiA"
                  />

                  <button
                      class="location-search-button"
                      :disabled="loading || !diaChiA.trim()"
                      @click="timDiaChiA"
                  >
                    <span
                        v-if="loading"
                        class="spinner-border spinner-border-sm"
                    ></span>

                    <i v-else class="fa-solid fa-location-crosshairs"></i>
                  </button>
                </div>
              </div>

              <!-- MAP -->
              <div class="map-container">
                <l-map
                    ref="mapRef"
                    v-model:zoom="zoom"
                    :center="center"
                    class="leaflet-map"
                >
                  <l-tile-layer
                      url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                      attribution="&copy; OpenStreetMap contributors"
                  />

                  <l-marker
                      v-if="locationA"
                      :lat-lng="locationA"
                  >
                    <l-popup>
                      <strong>Khách hàng:</strong>
                      {{ diaChiA }}
                    </l-popup>
                  </l-marker>

                  <l-marker
                      v-if="selectedNhanVienLocation"
                      :lat-lng="selectedNhanVienLocation"
                  >
                    <l-popup>
                      <strong>Nhân viên:</strong>
                      {{ selectedNhanVien?.hoTen }}
                    </l-popup>
                  </l-marker>

                  <l-polyline
                      v-if="routePath.length > 0"
                      :lat-lngs="routePath"
                      color="#e60012"
                      :weight="5"
                      :opacity="0.82"
                  />
                </l-map>

                <div
                    v-if="loadingRoute"
                    class="map-loading"
                >
                  <span class="spinner-border spinner-border-sm"></span>
                  Đang tìm đường đi...
                </div>
              </div>

              <!-- EMPLOYEE LIST -->
              <div class="employee-section">
                <div class="section-heading">
                  <h2>Nhân viên tư vấn gần nhất</h2>
                  <p>Danh sách nhân viên gần vị trí khách hàng nhất</p>
                </div>

                <div
                    v-if="loadingNhanVien"
                    class="employee-loading"
                >
                  <span class="spinner-border spinner-border-sm"></span>
                  Đang tìm nhân viên gần nhất...
                </div>

                <div
                    v-else-if="nhanVienDeXuat.length === 0"
                    class="empty-employees"
                >
                  Nhập địa chỉ khách hàng để tìm nhân viên đề xuất
                </div>

                <div
                    v-else
                    class="employee-table"
                >
                  <div class="employee-table-header">
                    <span>Nhân viên</span>
                    <span>Khoảng cách</span>
                    <span>Trạng thái</span>
                    <span>Thao tác</span>
                  </div>

                  <div
                      v-for="nv in nhanVienDeXuat"
                      :key="nv.maNhanVien"
                      class="employee-row"
                      :class="{
                      selected:
                        selectedNhanVien?.maNhanVien === nv.maNhanVien
                    }"
                      @click="handleSelectNhanVien(nv)"
                  >
                    <div class="employee-information">
                      <div class="employee-avatar">
                        <i class="fa-solid fa-lock"></i>
                      </div>

                      <div>
                        <strong>{{ nv.hoTen }}</strong>
                        <span>
                          {{ maskPhone(nv.soDienThoai) }}
                        </span>
                      </div>
                    </div>

                    <div
                        class="employee-distance"
                        :class="{
                        closest: nv === nhanVienDeXuat[0]
                      }"
                    >
                      {{ nv.khoangCachText || "Chưa xác định" }}
                    </div>

                    <div class="employee-status">
                      <span
                          class="status-dot"
                          :class="getEmployeeStatusClass(nv)"
                      ></span>

                      <span :class="getEmployeeStatusClass(nv)">
                        {{ nv.trangThaiLamViecText || "Chưa xác định" }}
                      </span>
                    </div>

                    <div class="employee-action">
                      <button
                          @click.stop="chonVaGiaoViec(nv)"
                      >
                        Giao việc
                      </button>
                    </div>
                  </div>
                </div>

                <p
                    v-if="nhanVienDeXuat.length > 0"
                    class="employee-note"
                >
                  Danh sách được sắp xếp theo khoảng cách gần nhất
                </p>
              </div>
            </div>
          </section>

          <!-- RIGHT CONTENT -->
          <aside class="right-content">
            <div class="panel-card instruction-card">
              <div class="instruction-title">
                <i class="fa-solid fa-circle-info"></i>
                <h3>Hướng dẫn sử dụng</h3>
              </div>

              <ol>
                <li>
                  Nhập thông tin khách hàng: họ tên, số điện thoại, địa chỉ.
                </li>
                <li>
                  Bản đồ sẽ hiển thị vị trí của khách hàng.
                </li>
                <li>
                  Hệ thống sẽ gợi ý nhân viên tư vấn gần nhất.
                </li>
                <li>
                  Chọn nhân viên và nhấn “Giao việc” để chuyển công việc.
                </li>
              </ol>
            </div>

            <div class="panel-card call-card">
              <h3>Thông tin cuộc gọi hiện tại</h3>

              <div class="call-content">
                <button
                    class="record-button"
                    :class="{ recording: isRecording }"
                    :disabled="isRecordingUploading"
                    @click="toggleRecording"
                >
                  <i
                      :class="
                      isRecording
                        ? 'fa-solid fa-stop'
                        : 'fa-solid fa-phone'
                    "
                  ></i>
                </button>

                <template v-if="isRecording">
                  <h4 class="recording-title">
                    Đang ghi âm cuộc gọi
                  </h4>

                  <p class="recording-time">
                    {{ formatTime(recordingDuration) }}
                  </p>

                  <div class="audio-wave active">
                    <span
                        v-for="item in 37"
                        :key="item"
                        :style="{
                        height: `${8 + ((item * 13) % 34)}px`
                      }"
                    ></span>
                  </div>
                </template>

                <template v-else-if="isRecordingUploading">
                  <h4>Đang lưu ghi âm</h4>
                  <p>Vui lòng chờ file được tải lên Cloudinary</p>

                  <span class="spinner-border text-danger"></span>
                </template>

                <template v-else-if="audioUrl">
                  <h4>Đã lưu cuộc gọi</h4>

                  <p>
                    Thời lượng:
                    {{ formatTime(recordingDuration) }}
                  </p>

                  <audio
                      :src="audioUrl"
                      controls
                      class="audio-player"
                  ></audio>

                  <button
                      class="delete-recording-button"
                      @click="deleteRecording"
                  >
                    <i class="fa-regular fa-trash-can"></i>
                    Xóa bản ghi
                  </button>
                </template>

                <template v-else>
                  <h4>Chưa có cuộc gọi</h4>

                  <p>
                    Khi có cuộc gọi đến, thông tin sẽ hiển thị tại đây
                  </p>

                  <div class="audio-wave">
                    <span
                        v-for="item in 37"
                        :key="item"
                        :style="{
                        height: `${5 + ((item * 11) % 28)}px`
                      }"
                    ></span>
                  </div>
                </template>
              </div>
            </div>
          </aside>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, nextTick } from "vue";

import {
  LMap,
  LTileLayer,
  LMarker,
  LPopup,
  LPolyline
} from "@vue-leaflet/vue-leaflet";

import { getNhanVienDeXuat } from "../../services/nhanVienService";
import api from "../../api/api.js";

const diaChiA = ref("");

const mapRef = ref(null);

const zoom = ref(13);

const center = ref([10.776889, 106.700806]);

const locationA = ref(null);

const loading = ref(false);

const nhanVienDeXuat = ref([]);
const selectedNhanVien = ref(null);
const loadingNhanVien = ref(false);
const routePath = ref([]); // Tọa độ đường đi từ nhân viên đến khách hàng
const selectedNhanVienLocation = ref(null); // Tọa độ nhân viên được chọn
const loadingRoute = ref(false); // Loading khi tính đường đi

// Recording state
const isRecording = ref(false);
const isRecordingUploading = ref(false);
const recordingDuration = ref(0);
const audioUrl = ref(null);
const mediaRecorder = ref(null);
const audioChunks = ref([]);
const recordingTimer = ref(null);

// Geocode với Nominatim API
const geocodeWithNominatim = async (address) => {
  try {
    const encodedAddress = encodeURIComponent(address);
    const url = `https://nominatim.openstreetmap.org/search?q=${encodedAddress}&format=json&limit=1&countrycodes=vn`;

    const response = await fetch(url, {
      headers: {
        'User-Agent': 'AnYen Funeral Service Demo'
      }
    });

    const data = await response.json();

    if (data && data.length > 0) {
      return {
        lat: parseFloat(data[0].lat),
        lng: parseFloat(data[0].lon),
        displayName: data[0].display_name
      };
    }

    return null;
  } catch (error) {
    console.error("Lỗi khi geocode:", error);
    return null;
  }
};

// Tính khoảng cách với OSRM API
const calculateDistanceWithOSRM = async (lat1, lng1, lat2, lng2) => {
  try {
    const url = `https://router.project-osrm.org/route/v1/driving/${lng1},${lat1};${lng2},${lat2}?overview=full&geometries=geojson`;

    const response = await fetch(url);
    const data = await response.json();

    if (data && data.routes && data.routes.length > 0) {
      const distance = data.routes[0].distance; // mét
      const duration = data.routes[0].duration; // giây
      const geometry = data.routes[0].geometry; // GeoJSON coordinates

      // Decode GeoJSON geometry to array of [lat, lng]
      const path = geometry.coordinates.map(coord => [coord[1], coord[0]]);

      return {
        distance: (distance / 1000).toFixed(2), // km
        duration: Math.round(duration / 60), // phút
        path: path // đường đi
      };
    }

    return null;
  } catch (error) {
    console.error("Lỗi khi tính khoảng cách:", error);
    return null;
  }
};

const timDiaChiA = async () => {
  if (!diaChiA.value.trim()) return;

  loading.value = true;

  try {
    const result = await geocodeWithNominatim(diaChiA.value);

    if (result) {
      locationA.value = [result.lat, result.lng];
      center.value = [result.lat, result.lng];
      zoom.value = 15;

      await nextTick();

      if (mapRef.value?.leafletObject) {
        mapRef.value.leafletObject.flyTo([result.lat, result.lng], 15);
      }

      // Lấy danh sách nhân viên đề xuất sau khi có tọa độ
      await fetchNhanVienDeXuat(result.lat, result.lng);
    } else {
      alert("Không tìm thấy địa chỉ. Vui lòng thử lại với địa chỉ khác.");
    }
  } catch (error) {
    console.error("Lỗi khi tìm địa chỉ:", error);
    alert("Có lỗi xảy ra khi tìm địa chỉ.");
  } finally {
    loading.value = false;
  }
};

const fetchNhanVienDeXuat = async (lat, lng) => {
  if (!lat || !lng) return;

  try {
    loadingNhanVien.value = true;
    const res = await getNhanVienDeXuat(lat, lng);
    nhanVienDeXuat.value = res.data || [];
    console.log("Nhân viên đề xuất:", nhanVienDeXuat.value);
  } catch (error) {
    console.error("Lỗi khi lấy nhân viên đề xuất:", error);
    alert("Không thể lấy danh sách nhân viên đề xuất. Vui lòng kiểm tra console.");
    nhanVienDeXuat.value = [];
  } finally {
    loadingNhanVien.value = false;
  }
};

// Tính đường đi từ nhân viên đến khách hàng (giới hạn Việt Nam)
const calculateRouteToEmployee = async (employeeLat, employeeLng, customerLat, customerLng) => {
  try {
    // Bounds Việt Nam: ~8.0 to 23.5 latitude, 102.0 to 110.0 longitude
    const bounds = "8.0,102.0,23.5,110.0";

    const url = `https://router.project-osrm.org/route/v1/driving/${employeeLng},${employeeLat};${customerLng},${customerLat}?overview=full&geometries=geojson`;

    const response = await fetch(url);
    const data = await response.json();

    if (data && data.routes && data.routes.length > 0) {
      const geometry = data.routes[0].geometry;
      const path = geometry.coordinates.map(coord => [coord[1], coord[0]]);

      // Filter path để chỉ giữ các điểm trong bounds Việt Nam
      const filteredPath = path.filter(coord => {
        const lat = coord[0];
        const lng = coord[1];
        return lat >= 8.0 && lat <= 23.5 && lng >= 102.0 && lng <= 110.0;
      });

      return filteredPath;
    }

    return [];
  } catch (error) {
    console.error("Lỗi khi tính đường đi:", error);
    return [];
  }
};

// Xử lý khi chọn nhân viên
const handleSelectNhanVien = async (employee) => {
  selectedNhanVien.value = employee;

  // Sử dụng tọa độ nhân viên từ backend response
  if (employee.latitude && employee.longitude) {
    const empLat = Number(employee.latitude);
    const empLng = Number(employee.longitude);

    selectedNhanVienLocation.value = [empLat, empLng];

    // Tính đường đi từ nhân viên đến khách hàng
    if (locationA.value) {
      loadingRoute.value = true;
      try {
        const route = await calculateRouteToEmployee(
          empLat,
          empLng,
          locationA.value[0],
          locationA.value[1]
        );
        routePath.value = route;

        // Zoom để thấy toàn bộ đường đi
        if (mapRef.value?.leafletObject && route.length > 0) {
          mapRef.value.leafletObject.fitBounds(route, { padding: [50, 50] });
        }
      } finally {
        loadingRoute.value = false;
      }
    }
  }
};

const dangGiaoViec = ref(false);

const giaoViec = async () => {
  if (dangGiaoViec.value) {
    return;
  }

  if (!selectedNhanVien.value) {
    alert("Vui lòng chọn nhân viên để giao việc");
    return;
  }

  const hoTen = hoTenKhachHang.value.trim();
  const soDienThoai = soDienThoaiKhachHang.value.replace(/\s/g, "").trim();
  const diaChi = diaChiA.value.trim();

  if (!hoTen || !soDienThoai || !diaChi) {
    alert("Vui lòng nhập đầy đủ thông tin khách hàng");
    return;
  }

  if (!/^[0-9]{9,20}$/.test(soDienThoai)) {
    alert("Số điện thoại phải gồm 9 đến 20 chữ số");
    return;
  }

  const payload = {
    maNhanVien: selectedNhanVien.value.maNhanVien,
    tenKhachHang: hoTen,
    soDienThoai,
    diaChi,
    latitude: locationA.value?.[0] ?? null,
    longitude: locationA.value?.[1] ?? null,
    nhuCauHoTro: "",
    ghiChu: ""
  };

  try {
    dangGiaoViec.value = true;
    console.log("Payload giao công việc:", payload);

    const response = await api.post("/api/nhan-vien/thong-bao/giao-cong-viec", payload);

    console.log("Kết quả giao việc:", response.data);

    alert(`Đã giao việc cho nhân viên: ${selectedNhanVien.value.hoTen}`);

    hoTenKhachHang.value = "";
    soDienThoaiKhachHang.value = "";
    diaChiA.value = "";

    selectedNhanVien.value = null;
    nhanVienDeXuat.value = [];
    locationA.value = null;
    routePath.value = [];
    selectedNhanVienLocation.value = null;

    audioUrl.value = null;
    recordingDuration.value = 0;

    center.value = [10.776889, 106.700806];
    zoom.value = 13;
  } catch (error) {
    console.error("Lỗi giao công việc:", error);
    console.error("HTTP status:", error.response?.status);
    console.error("Backend response:", error.response?.data);

    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.response?.data?.detail ||
      "Không thể giao việc cho nhân viên";

    alert(message);
  } finally {
    dangGiaoViec.value = false;
  }
};

// Ghi âm cuộc gọi
const toggleRecording = async () => {
  if (isRecording.value) {
    stopRecording();
  } else {
    await startRecording();
  }
};

const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

    mediaRecorder.value = new MediaRecorder(stream);
    audioChunks.value = [];

    mediaRecorder.value.ondataavailable = (event) => {
      audioChunks.value.push(event.data);
    };

    mediaRecorder.value.onstop = async () => {
      const audioBlob = new Blob(audioChunks.value, { type: 'audio/webm' });
      const audioFile = new File([audioBlob], 'recording.webm', { type: 'audio/webm' });

      // Tạo URL local để preview
      audioUrl.value = URL.createObjectURL(audioBlob);

      // Upload lên Cloudinary
      await uploadToCloudinary(audioFile);

      // Stop all tracks
      stream.getTracks().forEach(track => track.stop());
    };

    mediaRecorder.value.start();
    isRecording.value = true;
    recordingDuration.value = 0;

    // Timer đếm thời gian
    recordingTimer.value = setInterval(() => {
      recordingDuration.value++;
    }, 1000);

  } catch (error) {
    console.error('Lỗi khi bắt đầu ghi âm:', error);
    alert('Không thể truy cập micro. Vui lòng cấp quyền micro.');
  }
};

const stopRecording = () => {
  if (mediaRecorder.value && isRecording.value) {
    mediaRecorder.value.stop();
    isRecording.value = false;
    clearInterval(recordingTimer.value);
  }
};

const uploadToCloudinary = async (file) => {
  isRecordingUploading.value = true;

  try {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', 'recording_preset'); // Unsigned preset từ Cloudinary
    formData.append('resource_type', 'video'); // Audio được upload như video resource
    formData.append('public_id', `recording_${Date.now()}`); // Unique ID cho file

    const response = await fetch(
      `https://api.cloudinary.com/v1_1/dnj7xhvs1/video/upload`,
      {
        method: 'POST',
        body: formData
      }
    );

    const data = await response.json();

    if (data.secure_url) {
      audioUrl.value = data.secure_url;
      console.log('Upload thành công:', data.secure_url);
      // TODO: Lưu URL vào đơn hàng khi tạo đơn
    } else {
      throw new Error('Upload thất bại');
    }

  } catch (error) {
    console.error('Lỗi khi upload lên Cloudinary:', error);
    alert('Không thể upload ghi âm lên Cloudinary');
  } finally {
    isRecordingUploading.value = false;
  }
};

const deleteRecording = () => {
  audioUrl.value = null;
  recordingDuration.value = 0;
  audioChunks.value = [];
};

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins}:${secs.toString().padStart(2, '0')}`;
};

const hoTenKhachHang = ref("");
const soDienThoaiKhachHang = ref("");

const maskPhone = (phone) => {
  if (!phone) return "**********";

  const value = String(phone);

  if (value.length <= 3) {
    return "***";
  }

  return `${value.slice(0, 3)}*******`;
};

const getEmployeeStatusClass = (employee) => {
  const status = employee?.trangThaiLamViec;

  if (status === "RANH") {
    return "available";
  }

  if (status === "BAN") {
    return "busy";
  }

  return "offline";
};

const chonVaGiaoViec = async (employee) => {
  selectedNhanVien.value = employee;
  await giaoViec();
};
</script>

<style scoped>
.hotline-page {
  --primary-red: #e60012;
  --soft-red: #fff0f1;
  --dark-text: #151b2b;
  --muted-text: #687083;
  --border-color: #e8eaf0;
  --page-background: #fafbfc;

  display: flex;
  width: 100%;
  min-height: 100vh;
  background: #fafbfc;
  color: #151b2b;
  font-family: Inter, "Segoe UI", Arial, sans-serif;
}

/* MAIN */

.hotline-main {
  width: 100%;
  min-height: 100vh;
  margin-left: 0;
}

/* CONTENT */

.page-content {
  padding: 20px 22px 25px;
}

.content-layout {
  display: grid;
  grid-template-columns: minmax(650px, 1.7fr) minmax(350px, 0.95fr);
  gap: 22px;
  max-width: 1500px;
  margin: 0 auto;
}

.panel-card {
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: 11px;
  box-shadow: 0 2px 12px rgba(24, 32, 50, 0.025);
}

.customer-card {
  padding: 24px;
}

.customer-card > h2,
.section-heading h2 {
  margin: 0;
  color: #171717;
  font-size: 19px;
  font-weight: 700;
}

.form-group {
  margin-top: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #20283a;
  font-size: 13px;
  font-weight: 600;
}

.form-group label span {
  color: var(--primary-red);
}

.form-group input {
  width: 100%;
  height: 40px;
  padding: 0 14px;
  color: #222a3d;
  font-size: 13px;
  outline: none;
  background: #ffffff;
  border: 1px solid #dfe3ea;
  border-radius: 6px;
  transition: 0.2s ease;
}

.form-group input::placeholder {
  color: #a0a6b3;
}

.form-group input:focus {
  border-color: #ee7780;
  box-shadow: 0 0 0 3px rgba(230, 0, 18, 0.08);
}

.address-control {
  display: flex;
  gap: 8px;
}

.address-control input {
  flex: 1;
}

.location-search-button {
  display: grid;
  place-items: center;
  width: 42px;
  height: 40px;
  color: #ffffff;
  background: var(--primary-red);
  border: none;
  border-radius: 6px;
}

.location-search-button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

/* MAP */

.map-container {
  position: relative;
  height: 235px;
  margin-top: 8px;
  overflow: hidden;
  background: #e9edf2;
  border: 1px solid #e0e3e8;
  border-radius: 7px;
}

.leaflet-map {
  width: 100%;
  height: 100% !important;
}

.map-loading {
  position: absolute;
  z-index: 600;
  right: 15px;
  bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 13px;
  color: #4c5465;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 7px;
  box-shadow: 0 3px 12px rgba(20, 30, 50, 0.12);
}

/* EMPLOYEES */

.employee-section {
  margin-top: 28px;
}

.section-heading p {
  margin: 6px 0 16px;
  color: #81889a;
  font-size: 12px;
}

.employee-loading,
.empty-employees {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  min-height: 110px;
  color: #7d8594;
  font-size: 13px;
  border: 1px dashed #dfe3ea;
  border-radius: 7px;
}

.employee-table {
  overflow: hidden;
  border: 1px solid #e0e3e8;
  border-radius: 7px;
}

.employee-table-header,
.employee-row {
  display: grid;
  grid-template-columns: 1.65fr 0.85fr 1.15fr 0.8fr;
  align-items: center;
  column-gap: 15px;
}

.employee-table-header {
  min-height: 39px;
  padding: 0 16px;
  color: #2d3548;
  font-size: 11px;
  font-weight: 600;
  background: #fafafa;
  border-bottom: 1px solid #e4e6eb;
}

.employee-row {
  min-height: 62px;
  padding: 8px 16px;
  cursor: pointer;
  border-bottom: 1px solid #eceef2;
  transition: 0.18s ease;
}

.employee-row:last-child {
  border-bottom: none;
}

.employee-row:hover,
.employee-row.selected {
  background: #fff8f8;
}

.employee-information {
  display: flex;
  align-items: center;
  gap: 13px;
  min-width: 0;
}

.employee-avatar {
  display: grid;
  flex: 0 0 40px;
  place-items: center;
  width: 40px;
  height: 40px;
  color: #344057;
  background: #f1f1f4;
  border-radius: 50%;
}

.employee-information > div:last-child {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.employee-information strong {
  overflow: hidden;
  color: #242b3c;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.employee-information span {
  color: #9096a3;
  font-size: 11px;
  letter-spacing: 1px;
}

.employee-distance {
  color: #374054;
  font-size: 12px;
}

.employee-distance.closest {
  color: var(--primary-red);
}

.employee-status {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-dot.available {
  background: #12af48;
}

.status-dot.busy {
  background: #ff9518;
}

.status-dot.offline {
  background: #9ca2ae;
}

.employee-status .available {
  color: #0eac42;
}

.employee-status .busy {
  color: #ef890d;
}

.employee-status .offline {
  color: #8a919e;
}

.employee-action {
  display: flex;
  justify-content: flex-end;
}

.employee-action button {
  min-width: 82px;
  height: 34px;
  color: var(--primary-red);
  font-size: 11px;
  font-weight: 600;
  background: #ffffff;
  border: 1px solid var(--primary-red);
  border-radius: 6px;
  transition: 0.18s ease;
}

.employee-action button:hover {
  color: #ffffff;
  background: var(--primary-red);
}

.employee-note {
  margin: 17px 0 0;
  color: #778094;
  font-size: 11px;
}

/* RIGHT */

.right-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.instruction-card {
  padding: 23px 25px;
}

.instruction-title {
  display: flex;
  align-items: center;
  gap: 16px;
}

.instruction-title i {
  color: var(--primary-red);
  font-size: 18px;
}

.instruction-title h3,
.call-card > h3 {
  margin: 0;
  color: #1e1e1e;
  font-size: 15px;
  font-weight: 700;
}

.instruction-card ol {
  margin: 20px 0 0;
  padding-left: 20px;
  color: #71798b;
  font-size: 12px;
  line-height: 2.25;
}

.instruction-card li {
  padding-left: 9px;
}

.call-card {
  display: flex;
  min-height: 620px;
  flex-direction: column;
  padding: 25px;
}

.call-card > h3 {
  margin-bottom: 26px;
}

.call-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding-top: 12px;
  text-align: center;
}

.record-button {
  display: grid;
  place-items: center;
  width: 98px;
  height: 98px;
  margin-bottom: 24px;
  color: var(--primary-red);
  font-size: 41px;
  background: #fff0f1;
  border: none;
  border-radius: 50%;
  transition: 0.2s ease;
}

.record-button:hover {
  transform: scale(1.04);
}

.record-button.recording {
  color: #ffffff;
  background: var(--primary-red);
  box-shadow: 0 0 0 10px rgba(230, 0, 18, 0.1);
}

.call-content h4 {
  margin: 0;
  color: #151515;
  font-size: 15px;
  font-weight: 700;
}

.call-content p {
  margin: 14px 0 26px;
  color: #7e8595;
  font-size: 12px;
}

.recording-title,
.recording-time {
  color: var(--primary-red) !important;
}

.recording-time {
  font-size: 20px !important;
  font-weight: 700;
}

.audio-wave {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  width: 100%;
  min-height: 60px;
  margin-top: 5px;
  overflow: hidden;
}

.audio-wave span {
  display: block;
  width: 2px;
  max-height: 38px;
  background: #ffd2d5;
  border-radius: 2px;
}

.audio-wave.active span {
  background: #ff8d96;
  animation: wave 0.85s ease-in-out infinite alternate;
}

.audio-wave.active span:nth-child(3n) {
  animation-delay: 0.12s;
}

.audio-wave.active span:nth-child(4n) {
  animation-delay: 0.22s;
}

.audio-player {
  width: 100%;
  height: 42px;
  margin-top: 4px;
}

.delete-recording-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 130px;
  height: 38px;
  margin-top: 18px;
  color: var(--primary-red);
  font-size: 12px;
  font-weight: 600;
  background: #ffffff;
  border: 1px solid #f2a5ab;
  border-radius: 6px;
}

@keyframes wave {
  from {
    transform: scaleY(0.45);
    opacity: 0.65;
  }

  to {
    transform: scaleY(1);
    opacity: 1;
  }
}

/* LEAFLET */

:deep(.leaflet-control-zoom a) {
  color: #4a5365;
}

:deep(.leaflet-control-attribution) {
  font-size: 9px;
}

/* RESPONSIVE */

@media (max-width: 1200px) {
  .content-layout {
    grid-template-columns: minmax(560px, 1.5fr) minmax(310px, 0.9fr);
  }

  .hotline-sidebar {
    width: 225px;
  }

  .hotline-main {
    width: calc(100% - 225px);
    margin-left: 225px;
  }
}

@media (max-width: 992px) {
  .hotline-sidebar {
    width: 78px;
    padding-inline: 10px;
  }

  .sidebar-brand {
    padding-inline: 0;
  }

  .brand-logo {
    font-size: 20px;
  }

  .sidebar-brand h3,
  .sidebar-brand p,
  .menu-item span,
  .logout-button span,
  .staff-status-card {
    display: none;
  }

  .menu-item,
  .logout-button {
    justify-content: center;
    padding: 0;
  }

  .menu-item.active::before {
    left: -10px;
  }

  .hotline-main {
    width: calc(100% - 78px);
    margin-left: 78px;
  }

  .content-layout {
    grid-template-columns: 1fr;
  }

  .call-card {
    min-height: 440px;
  }
}

@media (max-width: 700px) {
  .hotline-sidebar {
    display: none;
  }

  .hotline-main {
    width: 100%;
    margin-left: 0;
  }

  .top-header {
    height: 65px;
    padding: 0 15px;
  }

  .header-left {
    gap: 12px;
  }

  .header-left h1 {
    font-size: 16px;
  }

  .header-lock,
  .header-user span {
    display: none;
  }

  .page-content {
    padding: 12px;
  }

  .customer-card,
  .instruction-card,
  .call-card {
    padding: 18px 15px;
  }

  .employee-table {
    overflow-x: auto;
  }

  .employee-table-header,
  .employee-row {
    grid-template-columns: 200px 110px 120px 100px;
    min-width: 560px;
  }

  .map-container {
    height: 270px;
  }
}
</style>