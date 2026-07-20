<template>
  <div class="messenger-page">
    <div class="messenger-container">
      <!-- Danh sách hội thoại -->
      <aside
          class="conversation-sidebar"
          :class="{ 'mobile-hidden': selectedConversation && isMobileChatOpen }"
      >
        <div class="sidebar-header">
          <div>
            <p class="sidebar-label">Trò chuyện</p>
            <h1>Tin nhắn</h1>
          </div>

          <button
              type="button"
              class="icon-button"
              title="Tạo cuộc trò chuyện"
              @click="createNewConversation"
          >
            <svg viewBox="0 0 24 24">
              <path
                  d="M12 5v14M5 12h14"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
              />
            </svg>
          </button>
        </div>

        <div class="search-box">
          <svg viewBox="0 0 24 24">
            <circle
                cx="11"
                cy="11"
                r="7"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
            />
            <path
                d="m16 16 4 4"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
            />
          </svg>

          <input
              v-model.trim="searchKeyword"
              type="text"
              placeholder="Tìm kiếm cuộc trò chuyện..."
          />

          <button
              v-if="searchKeyword"
              type="button"
              class="clear-search"
              @click="searchKeyword = ''"
          >
            ×
          </button>
        </div>

        <div class="conversation-list">
          <button
              v-for="conversation in filteredConversations"
              :key="conversation.id"
              type="button"
              class="conversation-item"
              :class="{
              active: selectedConversation?.id === conversation.id
            }"
              @click="selectConversation(conversation)"
          >
            <div class="avatar-wrapper">
              <img
                  v-if="conversation.avatar"
                  :src="conversation.avatar"
                  :alt="conversation.name"
                  class="avatar"
              />

              <div
                  v-else
                  class="avatar avatar-placeholder"
                  :style="{ background: conversation.avatarColor }"
              >
                {{ getInitials(conversation.name) }}
              </div>

              <span
                  v-if="conversation.online"
                  class="online-indicator"
              ></span>
            </div>

            <div class="conversation-content">
              <div class="conversation-row">
                <span class="conversation-name">
                  {{ conversation.name }}
                </span>

                <span class="conversation-time">
                  {{ conversation.lastMessageTime }}
                </span>
              </div>

              <div class="conversation-row">
                <span
                    class="conversation-preview"
                    :class="{ unread: conversation.unread > 0 }"
                >
                  {{ conversation.lastMessage }}
                </span>

                <span
                    v-if="conversation.unread > 0"
                    class="unread-badge"
                >
                  {{ conversation.unread > 99 ? "99+" : conversation.unread }}
                </span>
              </div>
            </div>
          </button>

          <div
              v-if="filteredConversations.length === 0"
              class="empty-conversation"
          >
            <div class="empty-icon">💬</div>
            <p>Không tìm thấy cuộc trò chuyện</p>
          </div>
        </div>

        <div class="current-user">
          <div class="avatar avatar-placeholder current-user-avatar">
            NV
          </div>

          <div class="current-user-info">
            <strong>Nguyễn Văn A</strong>
            <span>Đang hoạt động</span>
          </div>

          <button
              type="button"
              class="icon-button"
              title="Cài đặt"
          >
            <svg viewBox="0 0 24 24">
              <circle cx="5" cy="12" r="1.7" />
              <circle cx="12" cy="12" r="1.7" />
              <circle cx="19" cy="12" r="1.7" />
            </svg>
          </button>
        </div>
      </aside>

      <!-- Nội dung chat -->
      <main
          v-if="selectedConversation"
          class="chat-section"
          :class="{ 'mobile-visible': isMobileChatOpen }"
      >
        <header class="chat-header">
          <button
              type="button"
              class="back-button"
              @click="closeMobileChat"
          >
            <svg viewBox="0 0 24 24">
              <path
                  d="m15 18-6-6 6-6"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
              />
            </svg>
          </button>

          <div class="avatar-wrapper">
            <img
                v-if="selectedConversation.avatar"
                :src="selectedConversation.avatar"
                :alt="selectedConversation.name"
                class="avatar"
            />

            <div
                v-else
                class="avatar avatar-placeholder"
                :style="{
                background: selectedConversation.avatarColor
              }"
            >
              {{ getInitials(selectedConversation.name) }}
            </div>

            <span
                v-if="selectedConversation.online"
                class="online-indicator"
            ></span>
          </div>

          <div class="chat-user-info">
            <strong>{{ selectedConversation.name }}</strong>
            <span>
              {{
                selectedConversation.online
                    ? "Đang hoạt động"
                    : `Hoạt động ${selectedConversation.lastActive}`
              }}
            </span>
          </div>


        </header>

        <section
            ref="messageContainer"
            class="message-container"
        >
          <div class="conversation-start">
            <div
                class="avatar avatar-placeholder large-avatar"
                :style="{
                background: selectedConversation.avatarColor
              }"
            >
              {{ getInitials(selectedConversation.name) }}
            </div>

            <h2>{{ selectedConversation.name }}</h2>

            <p>
              Đây là phần bắt đầu cuộc trò chuyện của bạn với
              {{ selectedConversation.name }}.
            </p>
          </div>

          <template
              v-for="(message, index) in selectedConversation.messages"
              :key="message.id"
          >
            <div
                v-if="shouldShowDate(index)"
                class="message-date"
            >
              {{ formatDateLabel(message.createdAt) }}
            </div>

            <div
                class="message-row"
                :class="{
                'message-sent': message.sender === 'me',
                'message-received': message.sender !== 'me'
              }"
            >
              <div
                  v-if="message.sender !== 'me'"
                  class="message-avatar-space"
              >
                <div
                    v-if="shouldShowAvatar(index)"
                    class="avatar avatar-placeholder message-avatar"
                    :style="{
                    background: selectedConversation.avatarColor
                  }"
                >
                  {{ getInitials(selectedConversation.name) }}
                </div>
              </div>

              <div class="message-wrapper">
                <div
                    v-if="message.image"
                    class="message-image"
                >
                  <img
                      :src="message.image"
                      alt="Hình ảnh trong tin nhắn"
                  />
                </div>

                <div
                    v-if="message.content"
                    class="message-bubble"
                >
                  {{ message.content }}
                </div>

                <div class="message-meta">
                  <span>{{ formatMessageTime(message.createdAt) }}</span>

                  <span
                      v-if="message.sender === 'me'"
                      class="message-status"
                  >
                    {{ message.status === "seen" ? "Đã xem" : "Đã gửi" }}
                  </span>
                </div>
              </div>
            </div>
          </template>

          <div
              v-if="selectedConversation.typing"
              class="typing-row"
          >
            <div
                class="avatar avatar-placeholder message-avatar"
                :style="{
                background: selectedConversation.avatarColor
              }"
            >
              {{ getInitials(selectedConversation.name) }}
            </div>

            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </section>

        <footer class="message-input-area">
          <input
              ref="fileInput"
              type="file"
              accept="image/*"
              hidden
              @change="handleImageUpload"
          />

          <button
              type="button"
              class="input-action-button"
              title="Đính kèm hình ảnh"
              @click="fileInput?.click()"
          >
            <svg viewBox="0 0 24 24">
              <rect
                  x="3"
                  y="4"
                  width="18"
                  height="16"
                  rx="2"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.8"
              />
              <circle
                  cx="8"
                  cy="9"
                  r="1.5"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.8"
              />
              <path
                  d="m4 17 5-5 4 4 2-2 5 4"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.8"
                  stroke-linejoin="round"
              />
            </svg>
          </button>

          <div class="message-input-wrapper">
            <textarea
                ref="messageInput"
                v-model="newMessage"
                rows="1"
                maxlength="2000"
                placeholder="Nhập tin nhắn..."
                @input="resizeTextarea"
                @keydown.enter.exact.prevent="sendMessage"
            ></textarea>

            <button
                type="button"
                class="emoji-button"
                title="Biểu tượng cảm xúc"
                @click="toggleEmojiPanel"
            >
              😊
            </button>

            <div
                v-if="showEmojiPanel"
                class="emoji-panel"
            >
              <button
                  v-for="emoji in emojis"
                  :key="emoji"
                  type="button"
                  @click="addEmoji(emoji)"
              >
                {{ emoji }}
              </button>
            </div>
          </div>

          <button
              type="button"
              class="send-button"
              :disabled="!newMessage.trim()"
              title="Gửi tin nhắn"
              @click="sendMessage"
          >
            <svg viewBox="0 0 24 24">
              <path
                  d="m4 4 17 8-17 8 3-8-3-8Z"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.8"
                  stroke-linejoin="round"
              />
              <path
                  d="M7 12h14"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.8"
                  stroke-linecap="round"
              />
            </svg>
          </button>
        </footer>
      </main>

      <!-- Chưa chọn hội thoại -->
      <main
          v-else
          class="empty-chat-section"
      >
        <div class="empty-chat-content">
          <div class="empty-chat-icon">
            <svg viewBox="0 0 24 24">
              <path
                  d="M20 11.5a7.5 7.5 0 0 1-8 7.5 9.5 9.5 0 0 1-3.7-.7L4 20l1.2-3.6A7.2 7.2 0 0 1 4 12.5 7.5 7.5 0 0 1 12 5a7.5 7.5 0 0 1 8 6.5Z"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.7"
                  stroke-linejoin="round"
              />
            </svg>
          </div>

          <h2>Tin nhắn của bạn</h2>
          <p>Chọn một cuộc trò chuyện để bắt đầu nhắn tin.</p>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import {
  computed,
  nextTick,
  onMounted,
  ref
} from "vue";

const searchKeyword = ref("");
const newMessage = ref("");
const selectedConversation = ref(null);
const isMobileChatOpen = ref(false);
const showEmojiPanel = ref(false);

const messageContainer = ref(null);
const messageInput = ref(null);
const fileInput = ref(null);

const emojis = [
  "😀",
  "😂",
  "😍",
  "🥰",
  "😎",
  "😭",
  "👍",
  "❤️",
  "🎉",
  "🔥",
  "🙏",
  "✅"
];

const conversations = ref([
  {
    id: 1,
    name: "Trần Thị B",
    avatar: null,
    avatarColor: "linear-gradient(135deg, #7c3aed, #a855f7)",
    online: true,
    lastActive: "vừa xong",
    lastMessage: "Dạ, em đã nhận được thông tin rồi ạ.",
    lastMessageTime: "08:25",
    unread: 2,
    typing: false,
    messages: [
      {
        id: 101,
        sender: "other",
        content: "Chào anh, anh cần em hỗ trợ gì không ạ?",
        createdAt: "2026-07-19T08:10:00",
        status: "seen"
      },
      {
        id: 102,
        sender: "me",
        content: "Em kiểm tra giúp anh thông tin đơn hàng mới nhé.",
        createdAt: "2026-07-19T08:14:00",
        status: "seen"
      },
      {
        id: 103,
        sender: "other",
        content: "Dạ được ạ. Anh gửi mã đơn hàng cho em nhé.",
        createdAt: "2026-07-19T08:18:00",
        status: "seen"
      },
      {
        id: 104,
        sender: "me",
        content: "Mã đơn hàng là DH000128.",
        createdAt: "2026-07-19T08:20:00",
        status: "seen"
      },
      {
        id: 105,
        sender: "other",
        content: "Dạ, em đã nhận được thông tin rồi ạ.",
        createdAt: "2026-07-19T08:25:00",
        status: "seen"
      }
    ]
  },
  {
    id: 2,
    name: "Nguyễn Văn C",
    avatar: null,
    avatarColor: "linear-gradient(135deg, #0f766e, #14b8a6)",
    online: false,
    lastActive: "15 phút trước",
    lastMessage: "Cảm ơn bạn đã hỗ trợ.",
    lastMessageTime: "07:50",
    unread: 0,
    typing: false,
    messages: [
      {
        id: 201,
        sender: "other",
        content: "Bạn đã cập nhật trạng thái hợp đồng chưa?",
        createdAt: "2026-07-19T07:40:00",
        status: "seen"
      },
      {
        id: 202,
        sender: "me",
        content: "Mình đã cập nhật xong rồi nhé.",
        createdAt: "2026-07-19T07:45:00",
        status: "seen"
      },
      {
        id: 203,
        sender: "other",
        content: "Cảm ơn bạn đã hỗ trợ.",
        createdAt: "2026-07-19T07:50:00",
        status: "seen"
      }
    ]
  },
  {
    id: 3,
    name: "Phạm Minh Anh",
    avatar: null,
    avatarColor: "linear-gradient(135deg, #b45309, #f59e0b)",
    online: true,
    lastActive: "vừa xong",
    lastMessage: "Bạn đã gửi một hình ảnh.",
    lastMessageTime: "Hôm qua",
    unread: 0,
    typing: false,
    messages: [
      {
        id: 301,
        sender: "other",
        content: "Đây là hình ảnh sản phẩm mới.",
        createdAt: "2026-07-18T16:30:00",
        status: "seen"
      },
      {
        id: 302,
        sender: "other",
        content: "",
        image: "https://placehold.co/600x400?text=Hinh+anh+san+pham",
        createdAt: "2026-07-18T16:31:00",
        status: "seen"
      }
    ]
  },
  {
    id: 4,
    name: "Bộ phận chăm sóc khách hàng",
    avatar: null,
    avatarColor: "linear-gradient(135deg, #be123c, #fb7185)",
    online: false,
    lastActive: "1 giờ trước",
    lastMessage: "Vui lòng kiểm tra yêu cầu mới.",
    lastMessageTime: "Thứ 7",
    unread: 5,
    typing: false,
    messages: [
      {
        id: 401,
        sender: "other",
        content: "Vui lòng kiểm tra yêu cầu mới từ khách hàng.",
        createdAt: "2026-07-18T09:00:00",
        status: "seen"
      }
    ]
  },
  {
    id: 5,
    name: "Lê Hoàng Nam",
    avatar: null,
    avatarColor: "linear-gradient(135deg, #1d4ed8, #60a5fa)",
    online: false,
    lastActive: "2 giờ trước",
    lastMessage: "Hẹn gặp bạn vào ngày mai.",
    lastMessageTime: "Thứ 6",
    unread: 0,
    typing: false,
    messages: [
      {
        id: 501,
        sender: "me",
        content: "Hẹn gặp bạn vào ngày mai.",
        createdAt: "2026-07-17T17:20:00",
        status: "seen"
      }
    ]
  }
]);

const filteredConversations = computed(() => {
  const keyword = searchKeyword.value.toLowerCase();

  if (!keyword) {
    return conversations.value;
  }

  return conversations.value.filter((conversation) => {
    return (
        conversation.name.toLowerCase().includes(keyword) ||
        conversation.lastMessage.toLowerCase().includes(keyword)
    );
  });
});

function getInitials(name) {
  if (!name) return "?";

  const words = name
      .trim()
      .split(/\s+/)
      .filter(Boolean);

  if (words.length === 1) {
    return words[0].charAt(0).toUpperCase();
  }

  return (
      words[words.length - 2].charAt(0) +
      words[words.length - 1].charAt(0)
  ).toUpperCase();
}

async function selectConversation(conversation) {
  selectedConversation.value = conversation;
  conversation.unread = 0;
  isMobileChatOpen.value = true;
  showEmojiPanel.value = false;

  await scrollToBottom();

  nextTick(() => {
    messageInput.value?.focus();
  });
}

function closeMobileChat() {
  isMobileChatOpen.value = false;
}

function createNewConversation() {
  window.alert("Bạn có thể mở popup chọn người nhận tại đây.");
}

async function sendMessage() {
  const content = newMessage.value.trim();

  if (!content || !selectedConversation.value) {
    return;
  }

  const message = {
    id: Date.now(),
    sender: "me",
    content,
    createdAt: new Date().toISOString(),
    status: "sent"
  };

  selectedConversation.value.messages.push(message);
  selectedConversation.value.lastMessage = content;
  selectedConversation.value.lastMessageTime = formatMessageTime(
      message.createdAt
  );

  moveConversationToTop(selectedConversation.value.id);

  newMessage.value = "";
  showEmojiPanel.value = false;

  resetTextareaHeight();
  await scrollToBottom();

  // Khi kết nối backend, gọi API tại đây.
  // await messageService.sendMessage({
  //   conversationId: selectedConversation.value.id,
  //   content
  // });
}

function moveConversationToTop(conversationId) {
  const index = conversations.value.findIndex(
      (item) => item.id === conversationId
  );

  if (index <= 0) {
    return;
  }

  const [conversation] = conversations.value.splice(index, 1);
  conversations.value.unshift(conversation);
}

async function handleImageUpload(event) {
  const file = event.target.files?.[0];

  if (!file || !selectedConversation.value) {
    return;
  }

  if (!file.type.startsWith("image/")) {
    window.alert("Vui lòng chọn một file hình ảnh.");
    event.target.value = "";
    return;
  }

  const imageUrl = URL.createObjectURL(file);

  const message = {
    id: Date.now(),
    sender: "me",
    content: "",
    image: imageUrl,
    createdAt: new Date().toISOString(),
    status: "sent"
  };

  selectedConversation.value.messages.push(message);
  selectedConversation.value.lastMessage = "Bạn đã gửi một hình ảnh.";
  selectedConversation.value.lastMessageTime = formatMessageTime(
      message.createdAt
  );

  moveConversationToTop(selectedConversation.value.id);
  event.target.value = "";

  await scrollToBottom();

  // Với backend thật, hãy upload file bằng FormData.
}

function toggleEmojiPanel() {
  showEmojiPanel.value = !showEmojiPanel.value;
}

function addEmoji(emoji) {
  newMessage.value += emoji;

  nextTick(() => {
    messageInput.value?.focus();
    resizeTextarea();
  });
}

function resizeTextarea() {
  const textarea = messageInput.value;

  if (!textarea) {
    return;
  }

  textarea.style.height = "auto";
  textarea.style.height = `${Math.min(textarea.scrollHeight, 120)}px`;
}

function resetTextareaHeight() {
  nextTick(() => {
    if (messageInput.value) {
      messageInput.value.style.height = "auto";
    }
  });
}

async function scrollToBottom() {
  await nextTick();

  if (messageContainer.value) {
    messageContainer.value.scrollTop =
        messageContainer.value.scrollHeight;
  }
}

function formatMessageTime(dateValue) {
  return new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit"
  }).format(new Date(dateValue));
}

function formatDateLabel(dateValue) {
  const messageDate = new Date(dateValue);
  const today = new Date();

  const yesterday = new Date(today);
  yesterday.setDate(today.getDate() - 1);

  if (isSameDate(messageDate, today)) {
    return "Hôm nay";
  }

  if (isSameDate(messageDate, yesterday)) {
    return "Hôm qua";
  }

  return new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric"
  }).format(messageDate);
}

function isSameDate(dateA, dateB) {
  return (
      dateA.getFullYear() === dateB.getFullYear() &&
      dateA.getMonth() === dateB.getMonth() &&
      dateA.getDate() === dateB.getDate()
  );
}

function shouldShowDate(index) {
  const messages = selectedConversation.value?.messages || [];

  if (index === 0) {
    return true;
  }

  const currentDate = new Date(messages[index].createdAt);
  const previousDate = new Date(messages[index - 1].createdAt);

  return !isSameDate(currentDate, previousDate);
}

function shouldShowAvatar(index) {
  const messages = selectedConversation.value?.messages || [];
  const currentMessage = messages[index];
  const nextMessage = messages[index + 1];

  return (
      !nextMessage ||
      nextMessage.sender !== currentMessage.sender
  );
}

onMounted(() => {
  if (conversations.value.length > 0) {
    selectedConversation.value = conversations.value[0];
    selectedConversation.value.unread = 0;
    scrollToBottom();
  }
});
</script>

<style scoped>
* {
  box-sizing: border-box;
}

button,
input,
textarea {
  font: inherit;
}

button {
  border: none;
}

.messenger-page {
  --primary-color: #8b5e3c;
  --primary-dark: #6f472b;
  --primary-light: #f1e8e0;
  --border-color: #e8e8e8;
  --text-primary: #252525;
  --text-secondary: #777;
  --background-light: #f7f7f8;

  min-height: 100vh;
  padding: 24px;
  background:
      radial-gradient(circle at top left, #f8eee6 0, transparent 32%),
      #f5f5f5;
  color: var(--text-primary);
}

.messenger-container {
  display: flex;
  width: min(1400px, 100%);
  height: calc(100vh - 48px);
  min-height: 620px;
  margin: 0 auto;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 22px;
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.08);
}

.conversation-sidebar {
  display: flex;
  flex: 0 0 370px;
  flex-direction: column;
  min-width: 0;
  background: #fff;
  border-right: 1px solid var(--border-color);
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 22px 14px;
}

.sidebar-label {
  margin: 0 0 4px;
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1.5px;
  text-transform: uppercase;
}

.sidebar-header h1 {
  margin: 0;
  font-size: 27px;
  line-height: 1.2;
}

.icon-button {
  display: inline-flex;
  width: 42px;
  height: 42px;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #555;
  background: #f5f5f5;
  border-radius: 50%;
  cursor: pointer;
  transition:
      color 0.2s,
      background 0.2s,
      transform 0.2s;
}

.icon-button:hover {
  color: var(--primary-color);
  background: var(--primary-light);
  transform: translateY(-1px);
}

.icon-button svg {
  width: 21px;
  height: 21px;
}

.search-box {
  position: relative;
  margin: 0 18px 14px;
}

.search-box > svg {
  position: absolute;
  top: 50%;
  left: 14px;
  width: 19px;
  height: 19px;
  color: #999;
  transform: translateY(-50%);
}

.search-box input {
  width: 100%;
  height: 46px;
  padding: 0 42px;
  color: var(--text-primary);
  background: var(--background-light);
  border: 1px solid transparent;
  border-radius: 14px;
  outline: none;
  transition:
      border-color 0.2s,
      background 0.2s,
      box-shadow 0.2s;
}

.search-box input:focus {
  background: #fff;
  border-color: rgba(139, 94, 60, 0.4);
  box-shadow: 0 0 0 4px rgba(139, 94, 60, 0.08);
}

.clear-search {
  position: absolute;
  top: 50%;
  right: 13px;
  color: #999;
  background: transparent;
  font-size: 22px;
  cursor: pointer;
  transform: translateY(-52%);
}

.conversation-list {
  flex: 1;
  min-height: 0;
  padding: 0 10px;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 12px;
  padding: 12px;
  color: inherit;
  text-align: left;
  background: transparent;
  border-radius: 16px;
  cursor: pointer;
  transition:
      background 0.2s,
      transform 0.2s;
}

.conversation-item:hover {
  background: #f8f6f4;
}

.conversation-item.active {
  background: var(--primary-light);
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  display: flex;
  width: 50px;
  height: 50px;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  color: #fff;
  background: linear-gradient(135deg, #8b5e3c, #bd8b66);
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.online-indicator {
  position: absolute;
  right: 1px;
  bottom: 1px;
  width: 13px;
  height: 13px;
  background: #22c55e;
  border: 2px solid #fff;
  border-radius: 50%;
}

.conversation-content {
  min-width: 0;
  flex: 1;
}

.conversation-row {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.conversation-row + .conversation-row {
  margin-top: 5px;
}

.conversation-name {
  overflow: hidden;
  font-size: 15px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-time {
  flex-shrink: 0;
  color: #aaa;
  font-size: 11px;
}

.conversation-preview {
  min-width: 0;
  overflow: hidden;
  color: var(--text-secondary);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-preview.unread {
  color: var(--text-primary);
  font-weight: 700;
}

.unread-badge {
  display: flex;
  min-width: 21px;
  height: 21px;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  padding: 0 6px;
  color: #fff;
  background: var(--primary-color);
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
}

.empty-conversation {
  padding: 50px 20px;
  color: var(--text-secondary);
  text-align: center;
}

.empty-conversation p {
  margin: 10px 0 0;
}

.empty-icon {
  font-size: 35px;
}

.current-user {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 15px 18px;
  border-top: 1px solid var(--border-color);
}

.current-user-avatar {
  width: 43px;
  height: 43px;
  background: linear-gradient(135deg, #3f3f46, #71717a);
  font-size: 13px;
}

.current-user-info {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
}

.current-user-info strong {
  overflow: hidden;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.current-user-info span {
  margin-top: 3px;
  color: #22a35a;
  font-size: 12px;
}

.current-user .icon-button {
  width: 37px;
  height: 37px;
}

.chat-section {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  background: #fff;
}

.chat-header {
  display: flex;
  min-height: 78px;
  align-items: center;
  gap: 13px;
  padding: 13px 22px;
  border-bottom: 1px solid var(--border-color);
}

.chat-user-info {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
}

.chat-user-info strong {
  overflow: hidden;
  font-size: 16px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-user-info span {
  margin-top: 4px;
  color: var(--text-secondary);
  font-size: 12px;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.back-button {
  display: none;
  width: 39px;
  height: 39px;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #555;
  background: transparent;
  border-radius: 50%;
  cursor: pointer;
}

.back-button:hover {
  background: #f2f2f2;
}

.back-button svg {
  width: 23px;
  height: 23px;
}

.message-container {
  flex: 1;
  min-height: 0;
  padding: 24px 30px;
  overflow-y: auto;
  background:
      linear-gradient(rgba(255, 255, 255, 0.91), rgba(255, 255, 255, 0.91)),
      radial-gradient(circle at 20% 20%, #f0e4da 0, transparent 30%),
      radial-gradient(circle at 80% 80%, #ebe7e4 0, transparent 35%);
  scroll-behavior: smooth;
}

.conversation-start {
  max-width: 420px;
  margin: 10px auto 34px;
  text-align: center;
}

.large-avatar {
  width: 74px;
  height: 74px;
  margin: 0 auto 13px;
  font-size: 21px;
}

.conversation-start h2 {
  margin: 0;
  font-size: 19px;
}

.conversation-start p {
  margin: 8px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.message-date {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 25px 0 18px;
  color: #999;
  font-size: 11px;
  font-weight: 600;
}

.message-date::before,
.message-date::after {
  width: 40px;
  height: 1px;
  margin: 0 9px;
  background: #e6e6e6;
  content: "";
}

.message-row {
  display: flex;
  width: 100%;
  align-items: flex-end;
  margin: 5px 0;
}

.message-sent {
  justify-content: flex-end;
}

.message-received {
  justify-content: flex-start;
}

.message-avatar-space {
  width: 39px;
  margin-right: 8px;
  flex-shrink: 0;
}

.message-avatar {
  width: 34px;
  height: 34px;
  font-size: 10px;
}

.message-wrapper {
  display: flex;
  max-width: min(70%, 620px);
  flex-direction: column;
}

.message-sent .message-wrapper {
  align-items: flex-end;
}

.message-received .message-wrapper {
  align-items: flex-start;
}

.message-bubble {
  padding: 11px 15px;
  line-height: 1.5;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}

.message-sent .message-bubble {
  color: #fff;
  background: var(--primary-color);
  border-radius: 17px 17px 4px 17px;
}

.message-received .message-bubble {
  color: var(--text-primary);
  background: #f0f0f1;
  border-radius: 17px 17px 17px 4px;
}

.message-image {
  max-width: 360px;
  overflow: hidden;
  border-radius: 15px;
}

.message-image img {
  display: block;
  width: 100%;
  max-height: 420px;
  object-fit: cover;
}

.message-meta {
  display: flex;
  gap: 7px;
  margin-top: 4px;
  padding: 0 5px;
  color: #aaa;
  font-size: 10px;
}

.message-status {
  color: var(--primary-color);
}

.typing-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  margin-top: 10px;
}

.typing-indicator {
  display: flex;
  height: 39px;
  align-items: center;
  gap: 4px;
  padding: 0 15px;
  background: #efeff0;
  border-radius: 17px 17px 17px 4px;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background: #969696;
  border-radius: 50%;
  animation: typing 1.3s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes typing {
  0%,
  60%,
  100% {
    opacity: 0.4;
    transform: translateY(0);
  }

  30% {
    opacity: 1;
    transform: translateY(-4px);
  }
}

.message-input-area {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 14px 20px;
  background: #fff;
  border-top: 1px solid var(--border-color);
}

.input-action-button,
.send-button {
  display: flex;
  width: 44px;
  height: 44px;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 50%;
  cursor: pointer;
}

.input-action-button {
  color: var(--primary-color);
  background: var(--primary-light);
}

.input-action-button:hover {
  background: #e9dbcf;
}

.input-action-button svg,
.send-button svg {
  width: 21px;
  height: 21px;
}

.message-input-wrapper {
  position: relative;
  display: flex;
  min-height: 44px;
  flex: 1;
  align-items: flex-end;
  background: #f3f3f4;
  border: 1px solid transparent;
  border-radius: 22px;
  transition:
      background 0.2s,
      border-color 0.2s;
}

.message-input-wrapper:focus-within {
  background: #fff;
  border-color: rgba(139, 94, 60, 0.4);
}

.message-input-wrapper textarea {
  width: 100%;
  max-height: 120px;
  min-height: 42px;
  padding: 11px 44px 10px 17px;
  overflow-y: auto;
  color: var(--text-primary);
  background: transparent;
  border: none;
  outline: none;
  resize: none;
  line-height: 1.45;
}

.emoji-button {
  position: absolute;
  right: 6px;
  bottom: 5px;
  display: flex;
  width: 34px;
  height: 34px;
  align-items: center;
  justify-content: center;
  background: transparent;
  border-radius: 50%;
  cursor: pointer;
}

.emoji-button:hover {
  background: #e5e5e5;
}

.emoji-panel {
  position: absolute;
  right: 0;
  bottom: calc(100% + 10px);
  display: grid;
  width: 230px;
  padding: 12px;
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 15px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.13);
  grid-template-columns: repeat(6, 1fr);
  z-index: 10;
}

.emoji-panel button {
  display: flex;
  width: 33px;
  height: 33px;
  align-items: center;
  justify-content: center;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  font-size: 19px;
}

.emoji-panel button:hover {
  background: #f0f0f0;
}

.send-button {
  color: #fff;
  background: var(--primary-color);
  transition:
      background 0.2s,
      transform 0.2s,
      opacity 0.2s;
}

.send-button:hover:not(:disabled) {
  background: var(--primary-dark);
  transform: translateY(-1px);
}

.send-button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.empty-chat-section {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  background:
      radial-gradient(circle at center, #faf6f2 0, #fff 65%);
}

.empty-chat-content {
  padding: 30px;
  text-align: center;
}

.empty-chat-icon {
  display: flex;
  width: 82px;
  height: 82px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 18px;
  color: var(--primary-color);
  background: var(--primary-light);
  border-radius: 50%;
}

.empty-chat-icon svg {
  width: 42px;
  height: 42px;
}

.empty-chat-content h2 {
  margin: 0 0 8px;
  font-size: 22px;
}

.empty-chat-content p {
  margin: 0;
  color: var(--text-secondary);
}

.conversation-list::-webkit-scrollbar,
.message-container::-webkit-scrollbar,
.message-input-wrapper textarea::-webkit-scrollbar {
  width: 6px;
}

.conversation-list::-webkit-scrollbar-thumb,
.message-container::-webkit-scrollbar-thumb,
.message-input-wrapper textarea::-webkit-scrollbar-thumb {
  background: #d5d5d5;
  border-radius: 20px;
}

@media (max-width: 900px) {
  .messenger-page {
    padding: 0;
  }

  .messenger-container {
    height: 100vh;
    min-height: 100vh;
    border: none;
    border-radius: 0;
  }

  .conversation-sidebar {
    flex-basis: 320px;
  }

  .message-wrapper {
    max-width: 80%;
  }
}

@media (max-width: 700px) {
  .conversation-sidebar {
    width: 100%;
    flex-basis: 100%;
    border-right: none;
  }

  .conversation-sidebar.mobile-hidden {
    display: none;
  }

  .chat-section {
    display: none;
    width: 100%;
  }

  .chat-section.mobile-visible {
    display: flex;
  }

  .back-button {
    display: flex;
  }

  .chat-header {
    min-height: 68px;
    padding: 10px 12px;
  }

  .chat-header .avatar {
    width: 43px;
    height: 43px;
  }

  .chat-actions .icon-button {
    width: 37px;
    height: 37px;
  }

  .chat-actions .icon-button:nth-child(1),
  .chat-actions .icon-button:nth-child(2) {
    display: none;
  }

  .message-container {
    padding: 18px 12px;
  }

  .conversation-start {
    margin-bottom: 25px;
  }

  .message-wrapper {
    max-width: 82%;
  }

  .message-input-area {
    gap: 7px;
    padding: 10px;
  }

  .input-action-button,
  .send-button {
    width: 41px;
    height: 41px;
  }

  .message-avatar-space {
    width: 31px;
    margin-right: 5px;
  }

  .message-avatar {
    width: 29px;
    height: 29px;
    font-size: 9px;
  }

  .emoji-panel {
    right: -50px;
  }
}
</style>