<template>
  <div class="detail-page">

    <!-- Loading -->
    <div v-if="loading" class="loading">
      Đang tải bài viết...
    </div>


    <!-- Nội dung -->
    <div v-else-if="tinTuc" class="container">

      <div class="back">
        <button @click="goBack">
          ← Quay lại
        </button>
      </div>


      <article class="article">


        <img
            :src="tinTuc.anhDaiDien || defaultImage"
            class="thumbnail"
        />


        <h1>
          {{ tinTuc.tieuDe }}
        </h1>


        <div class="meta">

          Ngày đăng:
          {{ formatDate(tinTuc.ngayDang) }}

        </div>



        <p class="summary">

          {{ tinTuc.tomTat }}

        </p>



        <div
            class="content"
            v-html="tinTuc.noiDung"
        >
        </div>


      </article>


    </div>


  </div>
</template>



<script setup>

import { ref,onMounted } from "vue";
import { useRoute,useRouter } from "vue-router";
import axios from "axios";


const route = useRoute();

const router = useRouter();


const tinTuc = ref(null);

const loading = ref(true);



const defaultImage =
    "https://via.placeholder.com/800x500";



const getDetail = async()=>{

  try{

    const id = route.params.id;


    const response =
        await axios.get(
            `http://localhost:8080/api/tin-tuc/${id}`
        );


    tinTuc.value = response.data;


  }
  catch(error){

    console.log(
        "Lỗi lấy chi tiết tin tức",
        error
    );

  }
  finally{

    loading.value=false;

  }

};



const goBack = ()=>{

  router.push("/tin-tuc");

};



const formatDate=(date)=>{

  if(!date)
    return "";


  return new Date(date)
      .toLocaleDateString("vi-VN");

};



onMounted(()=>{

  getDetail();

});


</script>



<style scoped>


.detail-page{

  background:#f8f5f2;

  min-height:100vh;

  padding:40px 0;

}



.container{

  width:85%;

  margin:auto;

}



.back button{

  border:none;

  background:#8b5e3c;

  color:white;

  padding:10px 20px;

  border-radius:20px;

  cursor:pointer;

}



.article{

  background:white;

  margin-top:30px;

  padding:40px;

  border-radius:15px;

}



.thumbnail{

  width:100%;

  height:450px;

  object-fit:cover;

  border-radius:10px;

}



.article h1{

  color:#8b5e3c;

  font-size:36px;

  margin-top:30px;

}



.meta{

  color:#888;

  margin:15px 0;

}



.summary{

  font-size:18px;

  font-weight:500;

  line-height:1.7;

  color:#555;

  border-left:4px solid #8b5e3c;

  padding-left:15px;

}



.content{

  margin-top:30px;

  line-height:1.8;

  font-size:17px;

  color:#333;

}



.loading{

  text-align:center;

  padding:100px;

  font-size:20px;

}



/* mobile */

@media(max-width:768px){


  .container{

    width:92%;

  }


  .article{

    padding:20px;

  }


  .thumbnail{

    height:250px;

  }


  .article h1{

    font-size:26px;

  }



}


</style>