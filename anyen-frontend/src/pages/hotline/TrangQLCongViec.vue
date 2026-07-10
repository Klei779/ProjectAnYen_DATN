<template>
  <div class="container-fluid p-4">

    <div class="row g-4">

      <!-- LEFT -->
      <div class="col-xl-8 col-lg-7">

        <div class="card shadow-sm border-0">

          <div class="card-body">

            <h4 class="fw-bold mb-4">
              Thông tin khách hàng
            </h4>

            <div class="mb-3">
              <label class="form-label fw-semibold">
                Họ và tên <span class="text-danger">*</span>
              </label>

              <input
                  v-model="form.tenKhachHang"
                  class="form-control"
                  placeholder="Nhập họ và tên khách hàng"
              >
            </div>

            <div class="mb-3">
              <label class="form-label fw-semibold">
                Số điện thoại <span class="text-danger">*</span>
              </label>

              <input
                  v-model="form.soDienThoai"
                  class="form-control"
                  placeholder="Nhập số điện thoại"
              >
            </div>

            <div class="mb-4">
              <label class="form-label fw-semibold">
                Địa chỉ <span class="text-danger">*</span>
              </label>

              <el-input
                  v-model="form.diaChi"
                  @blur="timDiaChi"
                  placeholder="Nhập địa chỉ khách hàng"
              />
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

                <l-marker :lat-lng="marker">

                  <l-popup>
                    {{ diaChiChiTiet }}
                  </l-popup>

                </l-marker>

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

              <li>Nhập thông tin khách hàng.</li>

              <li>Hệ thống xác định vị trí.</li>

              <li>Tự động tìm nhân viên gần nhất.</li>

              <li>Chọn nhân viên và giao việc.</li>

            </ol>

          </div>

        </div>

        <div class="card shadow-sm border-0">

          <div class="card-body">

            <h5 class="fw-bold mb-3">
              Nhân viên gần nhất
            </h5>

            <el-table
                :data="employees"
                border
                stripe
                style="width:100%"
            >

              <el-table-column
                  prop="ten"
                  label="Nhân viên"
              />

              <el-table-column
                  prop="km"
                  label="Khoảng cách"
                  width="120"
              />

              <el-table-column
                  prop="status"
                  label="Trạng thái"
                  width="120"
              >

                <template #default="{ row }">

                  <el-tag
                      :type="row.status === 'Sẵn sàng' ? 'success' : 'warning'"
                  >
                    {{ row.status }}
                  </el-tag>

                </template>

              </el-table-column>

              <el-table-column
                  width="110"
                  align="center"
              >

                <template #default>

                  <el-button
                      type="danger"
                      size="small"
                  >
                    Giao
                  </el-button>

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
import { ref, reactive, nextTick } from "vue";

import {
  LMap,
  LTileLayer,
  LMarker,
  LPopup
} from "@vue-leaflet/vue-leaflet";

import { geocodeAddress } from "../../services/GeocodingService";

const form = reactive({
  tenKhachHang: "",
  soDienThoai: "",
  diaChi: ""
});

const mapRef = ref(null);

const zoom = ref(13);

const center = ref([10.776889, 106.700806]);

const marker = ref([10.776889, 106.700806]);

const diaChiChiTiet = ref("");

const employees = ref([]);

const timDiaChi = async () => {

  if (!form.diaChi.trim()) return;

  try {

    const res = await geocodeAddress(form.diaChi);

    console.log(res.data);

    const lat = Number(res.data.lat);
    const lng = Number(res.data.lon);

    center.value = [lat, lng];
    marker.value = [lat, lng];
    zoom.value = 17;

    diaChiChiTiet.value = res.data.diaChiChiTiet;

    await nextTick();

    if (mapRef.value?.leafletObject) {
      mapRef.value.leafletObject.flyTo([lat, lng], 17);
    }

  } catch (error) {

    console.error(error);

  }

};
</script>