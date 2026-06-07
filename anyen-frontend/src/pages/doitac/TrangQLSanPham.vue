<script setup>
import { ref } from "vue";
import axios from "axios";

const selectedFile = ref(null);
const imageUrl = ref("");

const handleFileChange = (event) => {
  selectedFile.value = event.target.files[0];
};

const uploadImage = async () => {
  if (!selectedFile.value) {
    alert("Vui lòng chọn ảnh");
    return;
  }

  try {
    const formData = new FormData();
    formData.append("file", selectedFile.value);

    const response = await axios.post(
        "http://localhost:8080/api/upload",
        formData
    );

    imageUrl.value = response.data;
  } catch (error) {
    console.error(error);
    alert("Upload thất bại");
  }
};
</script>

<template>
  <h4>Trang Sản Phẩm</h4>

  <input
      type="file"
      accept="image/*"
      @change="handleFileChange"
  />

  <button @click="uploadImage">
    Upload
  </button>

  <div v-if="imageUrl">
    <p>{{ imageUrl }}</p>

    <img
        :src="imageUrl"
        alt="Preview"
        width="300"
    />
  </div>
</template>

<style scoped>
button {
  margin-left: 10px;
}

img {
  margin-top: 20px;
  border: 1px solid #ddd;
}
</style>