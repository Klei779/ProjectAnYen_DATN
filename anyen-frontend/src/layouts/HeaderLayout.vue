<script setup>

import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import api from "../api/api.js";
import UserProfileDropdown from "../components/UserProfileDropdown.vue";

defineProps({
  pageTitle: String,
  user: Object,
  apiUrl: String,
  defaultUserName: String,
  profileIcon: String,
  notificationRoute: String
})

const emit = defineEmits(['logout'])

</script>

<template>
  <header class="page-topbar">
    <div class="topbar-left">
      <i class="fa-solid fa-bars"></i>
      <h2>{{ pageTitle }}</h2>
    </div>

    <div class="topbar-right">
      <div class="bell-wrapper" @click.stop="toggleMiniNoti">
        <i class="fa-regular fa-bell"></i>
        <span class="bell-badge" v-if="unreadCount > 0">{{ unreadCount }}</span>

        <!-- Mini Notification Dropdown -->
        <div class="mini-noti-dropdown" v-if="showMiniNoti" @click.stop>
          <div class="mini-header">
            <div class="header-title">
              <h4>Thông báo <span class="badge">{{ unreadCount }}</span></h4>
              <button class="mark-read-btn" @click.stop="markAllAsRead">Đánh dấu tất cả đã đọc</button>
            </div>
            <button class="close-mini-btn" @click.stop="showMiniNoti = false">
              <i class="fa-solid fa-xmark"></i>
            </button>
          </div>

          <div class="mini-list">
            <div v-for="item in miniNotifications" :key="item.maThongBao" class="mini-item" :class="{'unread': item.trangThai === 'CHUA_DOC'}">
              <div class="mini-icon" :class="getMiniIconClass(item)">
                <i :class="getMiniIconName(item)"></i>
              </div>
              <div class="mini-info">
                <h5>{{ item.tieuDe }}</h5>
                <p>{{ item.noiDung }}</p>
                <small>{{ item.ngayTao }}</small>
              </div>
              <button class="mini-view-btn" @click="goToNotification(item)">Xem</button>
            </div>
            <div v-if="miniNotifications.length === 0" class="empty-mini">Không có thông báo mới</div>
          </div>

          <div class="mini-footer">
            <button @click="goToAllNotifications">Xem tất cả thông báo</button>
          </div>
        </div>
      </div>

      <div class="user-profile-wrapper" style="position: relative; cursor: pointer; margin-left: 20px" @click.stop="toggleProfile">
        <div style="display: flex; align-items: center; gap: 10px;">
          <div class="avatar-small">
            <i class="fa-solid fa-user"></i>
          </div>
          <div class="user-short-info">
            <strong>{{ user?.hoTen || 'Đối tác' }}</strong>
          </div>
        </div>
        <UserProfileDropdown
            v-if="showProfile"
            :user="user"
            icon-class="bi bi-flower1"
            @logout="logout"
        />
      </div>

    </div>
  </header>
</template>

<style scoped>

</style>