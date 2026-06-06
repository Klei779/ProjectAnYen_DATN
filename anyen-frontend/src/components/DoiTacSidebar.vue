<template>
  <aside class="sidebar">

    <div class="logo-section">
      <img
          src="../assets/images/icon/logoAnYen.png.png"
          alt="An Yên"
      />
    </div>

    <div class="partner-card">

      <div class="avatar">
        <i class="bi bi-flower1"></i>
      </div>

      <div>
        <h6 class="mb-1">Đối tác An Yên</h6>
        <small>Đối tác bạc</small>
      </div>

    </div>

    <nav class="menu">

      <RouterLink
          to="/doi-tac/tong-quan"
          class="menu-item"
      >
        Tổng quan
      </RouterLink>

      <RouterLink
          to="/doi-tac/quan-ly-san-pham"
          class="menu-item"
      >
        Quản lý sản phẩm
      </RouterLink>

      <RouterLink
          to="/doi-tac/quan-ly-don-hang"
          class="menu-item"
      >
        Quản lý đơn hàng
      </RouterLink>

      <RouterLink
          to="/doi-tac/thong-bao"
          class="menu-item"
      >
        Thông báo
      </RouterLink>

      <RouterLink
          to="/doi-tac/thong-tin-tai-khoan"
          class="menu-item"
      >
        Thông tin tài khoản
      </RouterLink>

      <RouterLink
          to="/doi-tac/doi-mat-khau"
          class="menu-item"
      >
        Đổi mật khẩu
      </RouterLink>

    </nav>

  </aside>
</template>

<style scoped src="../assets/styles/DoiTacSidebar.css"></style>