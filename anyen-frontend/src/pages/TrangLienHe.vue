<template>
  <div style="padding:50px">
    <el-input
        v-model="text"
        placeholder="Test"
    />

    <p>{{ text }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const text = ref('')
</script>