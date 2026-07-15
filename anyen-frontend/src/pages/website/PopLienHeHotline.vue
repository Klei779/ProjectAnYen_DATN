<template>
  <Teleport to="body">
    <div
        v-if="show"
        class="hotline-modal-overlay"
        @click.self="$emit('close')"
    >
      <div class="hotline-modal">
        <button
            type="button"
            class="hotline-close-btn"
            aria-label="Đóng popup"
            @click="$emit('close')"
        >
          ✕
        </button>

        <h2 class="hotline-title">☎ Hotline An Yên</h2>

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
  </Teleport>
</template>

<script setup>
defineProps({
  show: {
    type: Boolean,
    default: false
  }
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
];
</script>

<style scoped src="../../assets/styles/components/PopLienHeHotline.css"></style>