<template>
  <div class="user-profile-dropdown">
    <div class="profile-header">
      <div class="avatar-large">
        <i :class="iconClass"></i>
      </div>
      <div class="user-info">
        <h5>{{ userName }}</h5>
        <span class="role-badge">{{ vaitro }}</span>
      </div>
    </div>
    
    <div class="profile-body">
      <div class="info-item">
        <i class="fa-solid fa-envelope"></i>
        <span>{{ userEmail || 'Chưa cập nhật' }}</span>
      </div>
      <div class="info-item">
        <i class="fa-solid fa-phone"></i>
        <span>{{ userPhone || 'Chưa cập nhật' }}</span>
      </div>
    </div>

    <div class="profile-footer">
      <button class="btn-logout" @click="$emit('logout')">
        <i class="fa-solid fa-right-from-bracket"></i> Đăng xuất
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  user: {
    type: Object,
    default: () => ({})
  },
  iconClass: {
    type: String,
    default: "fa-solid fa-user"
  }
});

defineEmits(['logout']);
const vaitro = computed(() => props.user?.tenVaiTro || 'bug');
const userName = computed(() => props.user?.hoTen || props.user?.tenDangNhap || 'Người dùng');
const userRoleDisplay = computed(() => {
  const role = props.user?.vaiTroChiTiet || props.user?.loaiTaiKhoan || '';
  if (role === 'ADMIN') return 'Quản lý An Yên';
  if (role === 'HOTLINE') return 'Nhân viên Hotline';
  if (role === 'NHANVIEN') return 'Nhân viên Trực tiếp';
  if (role === 'DOITAC') return 'Đối tác';
  return role;
});
const userEmail = computed(() => props.user?.email || '');
const userPhone = computed(() => props.user?.soDienThoai || '');

</script>
<style scoped src="../assets/styles/components/UserProfileDropDown.css"></style>
