<script setup>
import heroSectionTrangSanPham from '../../assets/images/TrangSanPham/heroSection_TrangSanPham.png'
import logoAnYen from "../../assets/images/icon/logoAnYen.png";
import flowerIcon from "../../assets/images/icon/flower_icon.png";
import dividerIcon from "../../assets/images/icon/divider_icon.png";
import chungNhanImg from "../../assets/images/TrangGioiThieu/chungNhanUyTin_TrangGioiThieu.png";
import anhNen1Img from "../../assets/images/TrangGioiThieu/anhNen1.png";
import tangLeImg from "../../assets/images/TrangGioiThieu/hinh-anh-tang-le-1.png";
import nhanVienImg from "../../assets/images/TrangGioiThieu/nhanVienTangLe.png";
import LeeMeiYu from "../../assets/images/TrangGioiThieu/LeeMeiYu-img.png";
import Cocolonet from "../../assets/images/TrangGioiThieu/cocolonet.png";
import YapKimSiong from "../../assets/images/TrangGioiThieu/YapKimSiong.png";
import BanKinhSu from "../../assets/images/TrangGioiThieu/BanKinhSu.png";
import HoaThuong from "../../assets/images/TrangGioiThieu/hoaThuong.png";
import DucCha from "../../assets/images/TrangGioiThieu/ducCha.png";
</script>
<template>
  <section class="hero-section" :style="{ backgroundImage: `url(${heroSectionTrangSanPham})` }">
    <div class="hero-logo">
      <img :src="logoAnYen" alt="logo" class="hero-AnYen" style = ""/>
    </div>
    <p class="hero-subtitle-1">DỊCH VỤ MAI TÁNG AN YÊN</p>
    <div class="hero-divider">
      <img :src="flowerIcon" alt="Hoa" class="lotus-icon-img" />
    </div>
    <p class="hero-subtitle-2">AN LÀNH TIỄN BIỆT - YÊN TÂM VĨNH HẰNG</p>
    <div class="hero-divider">
      <img :src="dividerIcon" alt="Ngan" class="divider-icon" />
    </div>
    <div class="view-more-btn">
    <button class="XemDichVu-btn">XEM DỊCH VỤ</button>
    <button class="LienHe-btn">LIÊN HỆ VỚI CHÚNG TÔI </button>
    </div>

  </section>
  <section class="hero-section-2" :style="{ backgroundImage: `url(${anhNen1Img})` }">
    <div class="card-list">
      <img :src="tangLeImg" alt="TangLe" class="tangLe-img" />
      <div class="title">
      <h2>Sự ra đời của An Yên</h2>
      <p>Tổ chức một tang lễ hiện đại, trang trọng và chuyên nghiệp đang đánh dấu sự phát triển của xã hội, cũng như giúp bày tỏ sự kính trọng và tình yêu sâu sắc dành cho người đã khuất. An Yên ra đời để trở thành một phần trong sự phát triển tư duy mới đó.
        An Yên luôn hướng đến chất lượng hoàn hảo tất cả các khâu trong tang lễ và là người sẵn lòng đồng hành cùng gia quyến để chia sẻ nỗi niềm và tiễn đưa người đã khuất về nơi vĩnh hằng.</p>
      <p><strong>
        Trước – Trong – Sau tang lễ:</strong></p>
      <p>
        An Yên vận hành một hệ thống cơ sở vật chất hiện đại, cùng đội ngũ năng động, tâm huyết, với quy trình chuẩn hóa về chất lượng dịch vụ. Sẵn sàng đáp ứng được sự hài lòng và tin tưởng của khách hàng khi lựa chọn <strong> Dịch vụ Tang lễ An Yên.</strong></p>
      <h2>Tại sao chọn An Yên</h2>
      <p>Đội ngũ nhân sự tận tâm, chuyên nghiệp, được đào tạo bài bản.<br>
        Sản phẩm dịch vụ được chuẩn hóa phù hợp với nhu cầu đại đa số khách hàng.<br>
        Hệ thống quy trình chuẩn hóa các khâu thực hiện. Cam kết không phát sinh chi phí.<br>
        Tiềm lực phát triển cơ sở vật chất đầy đủ, hiện đại, kết nối các đối tác uy tín, chất lượng.</p>
      </div>
    </div>
    <div class="card-list">
      <div class="card-container">
        <h3 class="card-container-H3"><strong>Tầm Nhìn</strong></h3>
        <hr>
        <p style="text-align: left;">An Yên là công ty cung cấp dịch vụ tang lễ trọn gói chuyên nghiệp tại thị trường Việt Nam</p>
      </div>

      <div class="card-container">
        <h3 class="card-container-H3"><strong>Sứ Mệnh</strong></h3>
        <hr>
        <p style="text-align: left;">An Yên là người bạn đồng hành cùng tang quyến trong hành trình tiễn đưa người đã khuất về nơi vĩnh hằng một cách tận tâm và trọn vẹn</p>
      </div>

      <div class="card-container">
        <h3 class="card-container-H3"><strong>Giá Trị</strong></h3>
        <hr>
        <p style="text-align: left;">An Yên luôn hướng đến sự phát triển toàn diện cùng các tiêu chí TẬN TÂM – CHUYÊN NGHIỆP – MINH BẠCH</p>
      </div>
    </div>
  </section>
  <section class="hero-section-2">
    <div class="card-list-2">
      <img :src="nhanVienImg" alt="nhanVien" class="nhanVien-img" />
      <div class="title">
        <p style="font-size: clamp(2.5px, 2.1vw, 39px);"> <strong>Năng lực chứng minh</strong></p><br>
        <ul class="vision-list">
          <li>Trẻ trung, nhiệt huyết, có trình độ</li>
          <li>Tác phong lịch sự, chuyên nghiệp</li>
          <li>Phục vụ tận tâm, chu đáo 24/7</li>
          <li>Luôn học hỏi và không ngừng nâng cấp chất lượng dịch vụ khách hàng</li>
          <li>Hằng năm được tham gia các chương trình đào tạo kiến thức, nghiệp vụ bài bản trong và ngoài nước</li>
        </ul>
      </div>
    </div>
      <div>
        <p class="hero-subtitle-3">
          BAN CỐ VẤN NGHIỆP VỤ<br>
          CHUYÊN GIA NƯỚC NGOÀI</p>
        <p style="font-size: clamp(2px, 1.4vw, 36px);text-align: center;padding: 2%">Về mặt kiến thức & nghiệp vụ tổ chức vận hành dịch vụ tang lễ, An Yên được tư vấn, <br>
          hướng dẫn kỹ càng từ đội ngũ chuyên gia Quốc tế có kinh nghiệm lâu năm trong cùng lĩnh vực</p>
        <div class="card-list-1">
          <div class="card-container-1">
            <img :src="LeeMeiYu" alt="logo" class="hero-congSu" style = ""/>
            <p style="font-size: clamp(2px, 1.3vw, 36px);color:darkgoldenrod;padding-top: 1%"><strong>Bà Lee Mei Yu & Cộng Sự</strong></p>
            <p>Chuyên gia tư vấn 30 năm kinh nghiệm tổ chức tang lễ tại Đài Loan</p>
          </div>

          <div class="card-container-1">
            <img :src="Cocolonet" alt="logo" class="hero-congSu" style = ""/>
            <p style="font-size: clamp(2px, 1.3vw, 36px);color:darkgoldenrod;padding-top: 1%"><strong>Tập đoàn Cocolonet Nhật Bản</strong></p>
            <p>80 năm kinh nghiệm ngành dịch vụ tang lễ</p>
          </div>

          <div class="card-container-1">
            <img :src="YapKimSiong" alt="logo" class="hero-congSu" style = ""/>
            <p style="font-size: clamp(2px, 1.3vw, 36px);color:darkgoldenrod;padding-top: 1%"><strong>Ông Yap Kim Siong</strong></p>
            <p>Chuyên gia tư vấn 20 Năm kinh nghiệm tổ chức tang lễ tại Malaysia</p>
          </div>
        </div>
        <p class="hero-subtitle-3">
          BAN TƯ VẤN NGHI LỄ<br>
          GIÁO HỘI PHẬT GIÁO – CÔNG GIÁO</p>
        <p style="font-size: clamp(2px, 1.4vw, 36px);text-align: center;padding-top: 2%">Phật giáo, Công giáo. Cao Đài, Tin Lành, Người Gốc Hoa… thường có các nghi thức tổ chức tang lễ khác nhau.<br> An Yên phối hợp cùng Ban tư vẫn nghi lễ là thành viên của các Giáo Hội Phật giáo – Công giáo Việt Nam<br> tổ chức tang lễ một cách trọn vẹn cho người đã khuất.</p>
        <div class="card-list-1">
          <div class="card-container-1">
            <img :src="BanKinhSu" alt="logo" class="hero-congSu" style = ""/>
            <p style="font-size: clamp(2px, 1.28vw, 36px); padding-top: 5%">Ban Kinh Sư Chuyên Trách:<br>
              Điều Hành và Cố Vấn Nghi Lễ Phật Giáo</p>
          </div>

          <div class="card-container-1">
            <img :src="HoaThuong" alt="logo" class="hero-congSu" style = ""/>
            <img :src="DucCha" alt="logo" class="hero-congSu" style = ""/>
            <p style="font-size: clamp(2px, 1.28vw, 36px); padding-top: 5%">Cùng nhiều Quý Hòa Thượng, Đức Cha<br>
              và Ban Hành Giáo tại Việt Nam</p>
          </div>
        </div>
      </div>

  </section>
  <section class="hero-section-2" :style="{ backgroundImage: `url(${chungNhanImg})` }">
    <div class="view-more-btn-2">
      <button class="TimHieu-btn">TÌM HIỂU DỊCH VỤ</button>
    </div>
  </section>


</template>
<style scoped src="../../assets/styles/TrangGioiThieu.css"></style>