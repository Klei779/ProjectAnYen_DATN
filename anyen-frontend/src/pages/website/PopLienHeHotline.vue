<template>
  <div
      v-if="show"
      class="modal-overlay"
      @click.self="$emit('close')"
  >
    <div class="modal-content">

      <button
          class="close-btn"
          @click="$emit('close')"
      >
        ✕
      </button>

      <h2>☎ Hotline An Yên</h2>

      <div class="phone-list">

        <div
            v-for="item in hotlines"
            :key="item.id"
            class="phone-card"
        >
          <div class="phone-role">
            {{ item.chucVu }}
          </div>

          <a
              class="phone-number"
              :href="`tel:${item.soDienThoai}`"
          >
            {{ item.soDienThoai }}
          </a>
        </div>

      </div>

    </div>
  </div>
</template>

<script setup>
defineProps({
  show: Boolean
});

defineEmits(["close"]);

  const hotlines = [
  {
    id: 1,
    chucVu: "Tư vấn",
    soDienThoai: "0826772109"
  },
  {
    id: 2,
    chucVu: "Hỗ trợ khách hàng",
    soDienThoai: "0357402033"
  },
  {
    id: 3,
    chucVu: "Trực 24/7",
    soDienThoai: "0392168473"
  }
  ]
</script>

<style scoped src="../../assets/styles/components/PopLienHeHotline.css"></style>