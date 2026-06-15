<template>
  <div class="user-profile-dropdown">
    <div class="profile-header">
      <div class="avatar-large">
        <i :class="iconClass"></i>
      </div>
      <div class="user-info">
        <h5>{{ userName }}</h5>
        <span class="role-badge">{{ userRoleDisplay }}</span>
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

<style scoped>
.user-profile-dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  width: 280px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.1);
  border: 1px solid #e5e7eb;
  z-index: 1000;
  overflow: hidden;
  animation: dropdownIn 0.2s ease;
  transform-origin: top right;
}

@keyframes dropdownIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.profile-header {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e5e7eb;
}

.avatar-large {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: #2563eb;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.user-info h5 {
  margin: 0 0 5px 0;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.role-badge {
  display: inline-block;
  padding: 4px 10px;
  background: #e0e7ff;
  color: #4338ca;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.profile-body {
  padding: 15px 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  color: #475569;
  font-size: 14px;
}

.info-item i {
  color: #94a3b8;
  width: 16px;
  text-align: center;
}

.profile-footer {
  padding: 15px 20px;
  border-top: 1px solid #e5e7eb;
  background: #fafafa;
}

.btn-logout {
  width: 100%;
  padding: 10px;
  border: 1px solid #fee2e2;
  background: #fff;
  color: #ef4444;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
}

.btn-logout:hover {
  background: #fef2f2;
  border-color: #fca5a5;
}
</style>
