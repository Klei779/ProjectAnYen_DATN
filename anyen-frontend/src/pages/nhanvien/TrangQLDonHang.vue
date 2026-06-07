<script setup>
import { ref } from "vue";
import PopTaoHopDong from "../nhanvien/PopTaoHopDong.vue";

const showCreateContract = ref(false);
</script>

<template>
  <div class="page-container">
    <div class="header">
      <h4>Trang Quản Lý Đơn Hàng</h4>

      <el-button
          type="primary"
          @click="showCreateContract = true"
      >
        Tạo hợp đồng
      </el-button>
    </div>

    <PopTaoHopDong
        v-model="showCreateContract"
    />
  </div>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
