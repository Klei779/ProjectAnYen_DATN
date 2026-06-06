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
    soDienThoai: "0901111111"
  },
  {
    id: 2,
    chucVu: "Hỗ trợ khách hàng",
    soDienThoai: "0902222222"
  },
  {
    id: 3,
    chucVu: "Trực 24/7",
    soDienThoai: "0903333333"
  }
  ]
</script>

<style scoped>
.modal-overlay{
  position:fixed;
  inset:0;
  background:rgba(0,0,0,.5);
  display:flex;
  justify-content:center;
  align-items:center;
  z-index:9999;
}

.modal-content{
  background:white;
  width:400px;
  padding:30px;
  border-radius:12px;
  position:relative;
}

.close-btn{
  position:absolute;
  top:10px;
  right:15px;
  border:none;
  background:none;
  cursor:pointer;
  font-size:20px;
}

h2{
  text-align:center;
  margin-bottom:20px;
  color:#8b0016;
}

.phone-card{
  padding:12px;
  border:1px solid #eee;
  border-radius:8px;
}

.phone-role{
  font-weight:600;
  margin-bottom:6px;
}

.phone-number{
  color:#8b0016;
  text-decoration:none;
  font-weight:700;
}
</style>