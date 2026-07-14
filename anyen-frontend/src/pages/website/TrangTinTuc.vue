<template>
  <div class="tin-tuc-page">

    <!-- Banner -->
    <section class="banner" :style="{ backgroundImage: `url(${heroSectionTrangSanPham})` }">
      <div class="banner-content">
        <h1>Tin tức An Yên</h1>
        <p>
          Cập nhật những thông tin hữu ích về tang lễ,
          phong tục và hoạt động của An Yên.
        </p>
      </div>
    </section>


    <!-- Danh sách tin tức -->
    <section class="container">

      <div class="title">
        <h2>Tin tức mới nhất</h2>
      </div>


      <el-row :gutter="30">

        <el-col
            v-for="item in tinTucList"
            :key="item.maTinTuc"
            :xs="24"
            :sm="12"
            :md="8"
        >

          <div class="card">

            <img
                :src="item.anhDaiDien || defaultImage"
                class="image"
            />


            <div class="content">

              <h3>
                {{ item.tieuDe }}
              </h3>


              <p class="date">
                {{ formatDate(item.ngayDang) }}
              </p>


              <p class="summary">
                {{ item.tomTat }}
              </p>


              <button
                  @click="goDetail(item.maTinTuc)"
              >
                Đọc tiếp
              </button>


            </div>

          </div>

        </el-col>

      </el-row>


    </section>


  </div>
</template>



<script setup>

import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import heroSectionTrangSanPham from "../../assets/images/TrangSanPham/heroSection_TrangSanPham.png";


const router = useRouter();


const tinTucList = ref([]);


const defaultImage =
    "https://via.placeholder.com/600x400";



const getTinTuc = async () => {

  try {

    const response =
        await axios.get(
            "http://localhost:8080/api/tin-tuc"
        );


    tinTucList.value = response.data;


  } catch(error){

    console.log(
        "Lỗi lấy danh sách tin tức",
        error
    );

  }

};



const goDetail = (id)=>{

  router.push(
      `/tin-tuc/${id}`
  );

};



const formatDate = (date)=>{

  if(!date)
    return "";

  return new Date(date)
      .toLocaleDateString("vi-VN");

};



onMounted(()=>{

  getTinTuc();

});


</script>



<style scoped>


.tin-tuc-page{

  background:#f8f5f2;

  min-height:100vh;

}



/* Banner */

.banner{

  height:320px;

  background:
      linear-gradient(
          rgba(0,0,0,.45),
          rgba(0,0,0,.45)
      );

  background-size:cover;

  background-position:center;

  display:flex;

  align-items:center;

  justify-content:center;

  color: #15304e;

}


.banner-content{

  text-align:center;

}


.banner h1{

  font-size:42px;

  margin-bottom:15px;

}


.banner p{

  font-size:18px;

}



/* Content */


.container{

  width:90%;

  margin:50px auto;

}


.title{

  text-align:center;

  margin-bottom:40px;

}


.title h2{

  color:#8b5e3c;

  font-size:32px;

}



/* Card */


.card{

  background:white;

  border-radius:15px;

  overflow:hidden;

  margin-bottom:30px;

  box-shadow:
      0 5px 20px
      rgba(0,0,0,.08);

  transition:.3s;

}


.card:hover{

  transform:translateY(-5px);

}



.image{

  width:100%;

  height:220px;

  object-fit:cover;

}



.content{

  padding:20px;

}



.content h3{

  color:#8b5e3c;

  font-size:20px;

  min-height:55px;

}



.date{

  color:#999;

  font-size:14px;

}



.summary{

  color:#555;

  line-height:1.6;

  height:50px;

  overflow:hidden;

}



button{

  margin-top:15px;

  background:#8b5e3c;

  color:white;

  border:none;

  padding:10px 20px;

  border-radius:20px;

  cursor:pointer;

}


button:hover{

  background:#6f452c;

}



</style>