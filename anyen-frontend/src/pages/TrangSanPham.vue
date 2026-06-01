<template>
  <section
      class="hero-section"
      :style="{
      backgroundImage: `url(${heroSectionTrangSanPham})`
    }"
  >
    <div class="hero-overlay">

      <h1>SẢN PHẨM</h1>

      <p>
        Tịnh tuyển từng sản phẩm – Giữ trọn lòng thành kính
      </p>

      <el-input class="search-box" placeholder="Tìm kiếm sản phẩm..." size="large">
        <template #prefix>
          <el-icon>
            <Search />
          </el-icon>
        </template>
      </el-input>

    </div>
  </section>
</template>

<script setup>
import heroSectionTrangSanPham
  from '../assets/images/TrangSanPham/heroSection_TrangSanPham.png'
import { Search } from '@element-plus/icons-vue'
</script>

<style scoped>

.hero-section{
  height: 500px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.hero-overlay{
  width:100%;
  height:100%;

  display:flex;
  flex-direction:column;

  justify-content:center;
  align-items:center;
}

.hero-overlay h1{
  font-size:42px;
  font-weight:700;

  margin-bottom:1px;

  color:#23314d;
}

.hero-overlay p{
  margin-bottom:24px;

  color:#444;
}

.search-box{
  width:500px;
}

</style>