<template>
  <div class="container-fluid p-4">

    <div class="row g-4">

      <!-- LEFT -->
      <div class="col-xl-8 col-lg-7">

        <div class="card shadow-sm border-0">

          <div class="card-body">

            <h4 class="fw-bold mb-4">
              Demo Bản Đồ & Tính Khoảng Cách
            </h4>

            <div class="alert alert-info">
              <small>Demo sử dụng Nominatim (OpenStreetMap) và OSRM - miễn phí, không cần API key</small>
            </div>

            <div class="mb-3">
              <label class="form-label fw-semibold">
                Địa chỉ A (Nhập địa chỉ tại Việt Nam)
              </label>

              <input
                  v-model="diaChiA"
                  class="form-control"
                  placeholder="Ví dụ: 123 Lê Hồng Phong, Vũng Tàu"
              >
            </div>

            <div class="mb-4">
              <button
                  class="btn btn-primary"
                  @click="timDiaChiA"
                  :disabled="loading || !diaChiA.trim()"
              >
                <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
                Tìm vị trí & Nhân viên đề xuất
              </button>
            </div>

            <div class="mb-4">
              <label class="form-label fw-semibold">
                Nhân viên đề xuất (Top 5 gần nhất)
              </label>

              <div v-if="loadingNhanVien" class="text-center py-3">
                <div class="spinner-border spinner-border-sm text-primary"></div>
                <span class="ms-2 text-muted">Đang tìm nhân viên...</span>
              </div>

              <div v-else-if="nhanVienDeXuat.length === 0" class="text-center py-3 text-muted">
                <small>Nhập địa chỉ để tìm nhân viên đề xuất</small>
              </div>

              <div v-else>

                <el-select
                    v-model="selectedNhanVien"
                    placeholder="Chọn nhân viên"
                    style="width: 100%"
                    size="large"
                    @change="handleSelectNhanVien"
                >
                  <el-option
                      v-for="nv in nhanVienDeXuat"
                      :key="nv.maNhanVien"
                      :label="`${nv.hoTen} — ${nv.khoangCachText} — ${nv.donDangXuLy} đơn đang xử lý`"
                      :value="nv"
                  >
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                      <div>
                        <div class="fw-semibold">{{ nv.hoTen }}</div>
                        <div class="small text-muted">{{ nv.khoangCachText }}</div>
                      </div>
                      <div class="text-end">
                        <el-tag
                            :type="nv.trangThaiLamViec === 'RANH' ? 'success' : nv.trangThaiLamViec === 'BAN' ? 'danger' : 'warning'"
                            size="small"
                        >
                          {{ nv.trangThaiLamViecText }}
                        </el-tag>
                        <div class="small text-muted">{{ nv.donDangXuLy }} đơn</div>
                      </div>
                    </div>
                  </el-option>
                </el-select>

                <div v-if="selectedNhanVien" class="mt-3 p-3 bg-light rounded-3">

                  <div class="d-flex justify-content-between align-items-start mb-2">
                    <div>
                      <h6 class="fw-bold mb-1">{{ selectedNhanVien.hoTen }}</h6>
                      <small class="text-muted">{{ selectedNhanVien.diaChiDayDu || selectedNhanVien.tinhThanh }}</small>
                    </div>
                    <el-tag
                        :type="selectedNhanVien.trangThaiLamViec === 'RANH' ? 'success' : selectedNhanVien.trangThaiLamViec === 'BAN' ? 'danger' : 'warning'"
                    >
                      {{ selectedNhanVien.trangThaiLamViecText }}
                    </el-tag>
                  </div>

                  <div class="row g-2 small text-secondary">

                    <div class="col-6">
                      <div class="text-muted">Khoảng cách:</div>
                      <div class="fw-semibold text-dark">{{ selectedNhanVien.khoangCachText }}</div>
                    </div>

                    <div class="col-6">
                      <div class="text-muted">Đang xử lý:</div>
                      <div class="fw-semibold text-dark">{{ selectedNhanVien.donDangXuLy }} đơn</div>
                    </div>

                    <div class="col-6">
                      <div class="text-muted">Đã hoàn thành:</div>
                      <div class="fw-semibold text-dark">{{ selectedNhanVien.donHoanThanh }} đơn</div>
                    </div>

                    <div class="col-6">
                      <div class="text-muted">SĐT:</div>
                      <div class="fw-semibold text-dark">{{ selectedNhanVien.soDienThoai }}</div>
                    </div>

                  </div>

                  <button
                      class="btn btn-primary w-100 mt-3"
                      @click="giaoViec"
                  >
                    Giao việc
                  </button>

                </div>

              </div>

            </div>

            <!-- MAP -->
            <div class="map-wrapper">

              <l-map
                  ref="mapRef"
                  v-model:zoom="zoom"
                  :center="center"
                  style="height:380px"
              >

                <l-tile-layer
                    url="https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}"
                    attribution="Tiles © Esri"
                />

                <l-marker v-if="locationA" :lat-lng="locationA">

                  <l-popup>
                    <strong>Khách hàng:</strong> {{ diaChiA }}
                  </l-popup>

                </l-marker>

                <l-marker v-if="selectedNhanVienLocation" :lat-lng="selectedNhanVienLocation">

                  <l-popup>
                    <strong>Nhân viên:</strong> {{ selectedNhanVien?.hoTen }}
                  </l-popup>

                </l-marker>

                <l-polyline
                    v-if="routePath.length > 0"
                    :lat-lngs="routePath"
                    color="#2196F3"
                    :weight="5"
                    :opacity="0.8"
                />

              </l-map>

            </div>

          </div>

        </div>

      </div>

      <!-- RIGHT -->
      <div class="col-xl-4 col-lg-5">

        <div class="card shadow-sm border-0 mb-4">

          <div class="card-body">

            <h5 class="fw-bold">
              Hướng dẫn
            </h5>

            <ol class="small text-secondary mt-3">

              <li>Nhập địa chỉ A (bất kỳ địa chỉ tại Việt Nam).</li>

              <li>Địa chỉ B đã cố định: 123 Nguyễn Huệ, Quận 1, TP Hồ Chí Minh.</li>

              <li>Click "Tìm vị trí A" để hiển thị trên bản đồ.</li>

              <li>Click "Tính khoảng cách A → B" để tính khoảng cách thực tế.</li>

            </ol>

          </div>

        </div>

        <div class="card shadow-sm border-0">

          <div class="card-body">

            <h5 class="fw-bold mb-3">
              Thông tin API
            </h5>

            <div class="small text-secondary">

              <div class="mb-2">
                <strong>Nominatim:</strong> Chuyển địa chỉ thành tọa độ
              </div>

              <div class="mb-2">
                <strong>OSRM:</strong> Tính khoảng cách và đường đi
              </div>

              <div>
                <strong>Giới hạn:</strong> ~1 request/giây cho Nominatim công cộng
              </div>

            </div>

          </div>

        </div>

      </div>

    </div>

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

// Tính đường đi từ nhân viên đến khách hàng
const calculateRouteToEmployee = async (employeeLat, employeeLng, customerLat, customerLng) => {
  try {
    const url = `https://router.project-osrm.org/route/v1/driving/${employeeLng},${employeeLat};${customerLng},${customerLat}?overview=full&geometries=geojson`;

    const response = await fetch(url);
    const data = await response.json();

    if (data && data.routes && data.routes.length > 0) {
      const geometry = data.routes[0].geometry;
      const path = geometry.coordinates.map(coord => [coord[1], coord[0]]);
      return path;
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
    }
  }
};

const giaoViec = () => {
  if (!selectedNhanVien.value) {
    alert("Vui lòng chọn nhân viên để giao việc");
    return;
  }

  alert(`Đã giao việc cho nhân viên: ${selectedNhanVien.value.hoTen}`);
  // TODO: Gọi API để giao việc thực sự
};
</script>