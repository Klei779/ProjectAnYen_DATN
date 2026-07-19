<template>
  <div class="customer-chat">
    <!-- Nút mở chat -->
    <button
        v-if="!isOpen"
        type="button"
        class="chat-launcher"
        aria-label="Mở hộp thoại"
        @click="openChat"
    >
      <svg viewBox="0 0 24 24">
        <path
            d="M20 11.5a7.5 7.5 0 0 1-8 7.5 9.5 9.5 0 0 1-3.7-.7L4 20l1.2-3.6A7.2 7.2 0 0 1 4 12.5 7.5 7.5 0 0 1 12 5a7.5 7.5 0 0 1 8 6.5Z"
            fill="none"
            stroke="currentColor"
            stroke-width="1.8"
            stroke-linejoin="round"
        />
      </svg>

      <span
          v-if="unreadCount > 0"
          class="launcher-badge"
      >
        {{ unreadCount > 9 ? "9+" : unreadCount }}
      </span>
    </button>

    <!-- Khung chat -->
    <section
        v-else
        class="chat-box"
        :class="{ minimized: isMinimized }"
    >
      <!-- Header -->
      <header class="chat-header">
        <div class="staff-avatar-wrapper">
          <div class="staff-avatar">
            NV
          </div>
          <span class="online-dot"></span>
        </div>

        <div class="staff-info">
          <strong>Nhân viên hỗ trợ</strong>
          <span>
            <span class="status-dot"></span>
            Đang trực tuyến
          </span>
        </div>

        <div class="header-actions">
          <button
              type="button"
              class="header-button"
              :title="isMinimized ? 'Mở rộng' : 'Thu nhỏ'"
              @click="toggleMinimize"
          >
            <svg
                v-if="!isMinimized"
                viewBox="0 0 24 24"
            >
              <path
                  d="M6 12h12"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
              />
            </svg>

            <svg
                v-else
                viewBox="0 0 24 24"
            >
              <path
                  d="m7 14 5-5 5 5"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
              />
            </svg>
          </button>

          <button
              type="button"
              class="header-button"
              title="Đóng"
              @click="closeChat"
          >
            <svg viewBox="0 0 24 24">
              <path
                  d="m7 7 10 10M17 7 7 17"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
              />
            </svg>
          </button>
        </div>
      </header>

      <template v-if="!isMinimized">
        <!-- Nội dung tin nhắn -->
        <main
            ref="messageContainer"
            class="message-container"
        >
          <div class="welcome-message">
            <div class="welcome-icon">
              <svg viewBox="0 0 24 24">
                <path
                    d="M20 11.5a7.5 7.5 0 0 1-8 7.5 9.5 9.5 0 0 1-3.7-.7L4 20l1.2-3.6A7.2 7.2 0 0 1 4 12.5 7.5 7.5 0 0 1 12 5a7.5 7.5 0 0 1 8 6.5Z"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linejoin="round"
                />
              </svg>
            </div>

            <strong>An Yên xin chào!</strong>
            <p>Chúng tôi có thể hỗ trợ gì cho bạn?</p>
          </div>

          <div class="date-divider">
            Hôm nay
          </div>

          <div
              v-for="message in messages"
              :key="message.id"
              class="message-row"
              :class="message.sender === 'customer' ? 'sent' : 'received'"
          >
            <div
                v-if="message.sender === 'staff'"
                class="small-avatar"
            >
              NV
            </div>

            <div class="message-content">
              <div class="message-bubble">
                {{ message.content }}
              </div>

              <div class="message-time">
                {{ formatTime(message.createdAt) }}

                <span v-if="message.sender === 'customer'">
                  · {{ message.seen ? "Đã xem" : "Đã gửi" }}
                </span>
              </div>
            </div>
          </div>

          <!-- Nhân viên đang nhập -->
          <div
              v-if="staffTyping"
              class="typing-row"
          >
            <div class="small-avatar">
              NV
            </div>

            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </main>

        <!-- Nhập tin nhắn -->
        <footer class="chat-footer">
          <div class="input-wrapper">
            <textarea
                ref="messageInput"
                v-model="newMessage"
                rows="1"
                maxlength="1000"
                placeholder="Nhập nội dung tin nhắn..."
                @input="resizeTextarea"
                @keydown.enter.exact.prevent="sendMessage"
            ></textarea>

            <button
                type="button"
                class="emoji-button"
                title="Biểu tượng cảm xúc"
                @click="toggleEmoji"
            >
              😊
            </button>

            <div
                v-if="showEmoji"
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

        <div class="chat-note">
          Thông tin của bạn được bảo mật
        </div>
      </template>
    </section>
  </div>
</template>

<script setup>
import {
  nextTick,
  onMounted,
  ref,
  watch
} from "vue";

const isOpen = ref(false);
const isMinimized = ref(false);
const unreadCount = ref(1);
const newMessage = ref("");
const showEmoji = ref(false);
const staffTyping = ref(false);

const messageContainer = ref(null);
const messageInput = ref(null);

const emojis = [
  "😀",
  "😊",
  "😂",
  "😍",
  "🥰",
  "👍",
  "❤️",
  "🙏",
  "🎉",
  "✅"
];

const messages = ref([
  {
    id: 1,
    sender: "staff",
    content:
        "Xin chào! Tôi là nhân viên hỗ trợ của An Yên. Bạn cần hỗ trợ vấn đề gì ạ?",
    createdAt: new Date().toISOString(),
    seen: true
  }
]);

function openChat() {
  isOpen.value = true;
  isMinimized.value = false;
  unreadCount.value = 0;

  nextTick(() => {
    scrollToBottom();
    messageInput.value?.focus();
  });
}

function closeChat() {
  isOpen.value = false;
  isMinimized.value = false;
  showEmoji.value = false;
}

function toggleMinimize() {
  isMinimized.value = !isMinimized.value;
  showEmoji.value = false;

  if (!isMinimized.value) {
    nextTick(() => {
      scrollToBottom();
      messageInput.value?.focus();
    });
  }
}

async function sendMessage() {
  const content = newMessage.value.trim();

  if (!content) {
    return;
  }

  const message = {
    id: Date.now(),
    sender: "customer",
    content,
    createdAt: new Date().toISOString(),
    seen: false
  };

  messages.value.push(message);
  newMessage.value = "";
  showEmoji.value = false;

  resetTextarea();
  saveMessages();
  await scrollToBottom();

  // Xóa đoạn này khi kết nối WebSocket/backend thật.
  simulateStaffReply();
}

function simulateStaffReply() {
  staffTyping.value = true;

  window.setTimeout(async () => {
    staffTyping.value = false;

    const lastCustomerMessage = [...messages.value]
        .reverse()
        .find((message) => message.sender === "customer");

    if (lastCustomerMessage) {
      lastCustomerMessage.seen = true;
    }

    messages.value.push({
      id: Date.now() + 1,
      sender: "staff",
      content:
          "Cảm ơn bạn đã liên hệ. Nhân viên đang kiểm tra và sẽ hỗ trợ bạn ngay ạ.",
      createdAt: new Date().toISOString(),
      seen: true
    });

    saveMessages();
    await scrollToBottom();

    if (!isOpen.value || isMinimized.value) {
      unreadCount.value += 1;
    }
  }, 1200);
}

function toggleEmoji() {
  showEmoji.value = !showEmoji.value;
}

function addEmoji(emoji) {
  newMessage.value += emoji;
  showEmoji.value = false;

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
  textarea.style.height = `${Math.min(textarea.scrollHeight, 90)}px`;
}

function resetTextarea() {
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

function formatTime(dateValue) {
  return new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit"
  }).format(new Date(dateValue));
}

function saveMessages() {
  localStorage.setItem(
      "customerChatMessages",
      JSON.stringify(messages.value)
  );
}

function loadMessages() {
  try {
    const savedMessages = localStorage.getItem(
        "customerChatMessages"
    );

    if (savedMessages) {
      const parsedMessages = JSON.parse(savedMessages);

      if (Array.isArray(parsedMessages)) {
        messages.value = parsedMessages;
      }
    }
  } catch (error) {
    console.error("Không thể đọc lịch sử tin nhắn:", error);
  }
}

watch(isOpen, (value) => {
  if (value) {
    unreadCount.value = 0;
  }
});

onMounted(() => {
  loadMessages();
});
</script>

<style scoped>
.customer-chat {
  --chat-primary: #8b5e3c;
  --chat-primary-dark: #70482d;
  --chat-primary-light: #f3e9e1;
  --chat-text: #292929;
  --chat-secondary: #7b7b7b;

  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 9999;
  font-family:
      Inter,
      Arial,
      sans-serif;
}

button,
textarea {
  font: inherit;
}

.chat-launcher {
  position: relative;
  display: flex;
  width: 62px;
  height: 62px;
  align-items: center;
  justify-content: center;
  color: white;
  background: var(--chat-primary);
  border: none;
  border-radius: 50%;
  box-shadow: 0 10px 30px rgba(112, 72, 45, 0.35);
  cursor: pointer;
  transition:
      transform 0.2s,
      background 0.2s;
}

.chat-launcher:hover {
  background: var(--chat-primary-dark);
  transform: translateY(-3px);
}

.chat-launcher svg {
  width: 30px;
  height: 30px;
}

.launcher-badge {
  position: absolute;
  top: -4px;
  right: -3px;
  display: flex;
  min-width: 22px;
  height: 22px;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  color: white;
  background: #ef4444;
  border: 2px solid white;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
}

.chat-box {
  display: flex;
  width: 360px;
  height: 520px;
  overflow: hidden;
  background: white;
  border: 1px solid #e7e7e7;
  border-radius: 18px;
  box-shadow: 0 16px 50px rgba(0, 0, 0, 0.18);
  flex-direction: column;
  animation: openChat 0.2s ease-out;
}

.chat-box.minimized {
  height: 70px;
}

@keyframes openChat {
  from {
    opacity: 0;
    transform: translateY(15px) scale(0.97);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.chat-header {
  display: flex;
  min-height: 70px;
  align-items: center;
  gap: 11px;
  padding: 11px 13px;
  color: white;
  background:
      linear-gradient(
          135deg,
          var(--chat-primary),
          var(--chat-primary-dark)
      );
}

.staff-avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.staff-avatar,
.small-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--chat-primary);
  background: #fff;
  border-radius: 50%;
  font-weight: 700;
}

.staff-avatar {
  width: 46px;
  height: 46px;
  font-size: 13px;
}

.online-dot {
  position: absolute;
  right: 0;
  bottom: 1px;
  width: 12px;
  height: 12px;
  background: #22c55e;
  border: 2px solid var(--chat-primary);
  border-radius: 50%;
}

.staff-info {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
}

.staff-info strong {
  overflow: hidden;
  font-size: 15px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.staff-info > span {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.86);
  font-size: 11px;
}

.status-dot {
  width: 6px;
  height: 6px;
  background: #4ade80;
  border-radius: 50%;
}

.header-actions {
  display: flex;
  gap: 2px;
}

.header-button {
  display: flex;
  width: 34px;
  height: 34px;
  align-items: center;
  justify-content: center;
  color: white;
  background: transparent;
  border: none;
  border-radius: 50%;
  cursor: pointer;
}

.header-button:hover {
  background: rgba(255, 255, 255, 0.14);
}

.header-button svg {
  width: 19px;
  height: 19px;
}

.message-container {
  min-height: 0;
  flex: 1;
  padding: 15px 13px;
  overflow-y: auto;
  background:
      linear-gradient(
          rgba(255, 255, 255, 0.92),
          rgba(255, 255, 255, 0.92)
      ),
      radial-gradient(
          circle at top left,
          #f0e4da,
          transparent 40%
      );
  scroll-behavior: smooth;
}

.welcome-message {
  margin-bottom: 18px;
  color: var(--chat-text);
  text-align: center;
}

.welcome-icon {
  display: flex;
  width: 47px;
  height: 47px;
  align-items: center;
  justify-content: center;
  margin: 3px auto 9px;
  color: var(--chat-primary);
  background: var(--chat-primary-light);
  border-radius: 50%;
}

.welcome-icon svg {
  width: 25px;
  height: 25px;
}

.welcome-message strong {
  display: block;
  font-size: 14px;
}

.welcome-message p {
  margin: 5px 0 0;
  color: var(--chat-secondary);
  font-size: 12px;
}

.date-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 16px 0;
  color: #aaa;
  font-size: 10px;
}

.date-divider::before,
.date-divider::after {
  width: 35px;
  height: 1px;
  margin: 0 8px;
  background: #e4e4e4;
  content: "";
}

.message-row {
  display: flex;
  width: 100%;
  align-items: flex-end;
  gap: 6px;
  margin: 7px 0;
}

.message-row.sent {
  justify-content: flex-end;
}

.message-row.received {
  justify-content: flex-start;
}

.small-avatar {
  width: 29px;
  height: 29px;
  flex-shrink: 0;
  color: white;
  background: var(--chat-primary);
  font-size: 8px;
}

.message-content {
  display: flex;
  max-width: 78%;
  flex-direction: column;
}

.sent .message-content {
  align-items: flex-end;
}

.received .message-content {
  align-items: flex-start;
}

.message-bubble {
  padding: 9px 12px;
  font-size: 13px;
  line-height: 1.45;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}

.sent .message-bubble {
  color: white;
  background: var(--chat-primary);
  border-radius: 15px 15px 4px 15px;
}

.received .message-bubble {
  color: var(--chat-text);
  background: #f0f0f1;
  border-radius: 15px 15px 15px 4px;
}

.message-time {
  margin-top: 3px;
  padding: 0 3px;
  color: #aaa;
  font-size: 9px;
}

.typing-row {
  display: flex;
  align-items: flex-end;
  gap: 6px;
  margin-top: 7px;
}

.typing-indicator {
  display: flex;
  height: 36px;
  align-items: center;
  gap: 4px;
  padding: 0 13px;
  background: #f0f0f1;
  border-radius: 15px 15px 15px 4px;
}

.typing-indicator span {
  width: 5px;
  height: 5px;
  background: #929292;
  border-radius: 50%;
  animation: typing 1.2s infinite ease-in-out;
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
    transform: translateY(-3px);
  }
}

.chat-footer {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 10px;
  background: white;
  border-top: 1px solid #ededed;
}

.input-wrapper {
  position: relative;
  display: flex;
  min-height: 41px;
  flex: 1;
  align-items: flex-end;
  background: #f3f3f4;
  border: 1px solid transparent;
  border-radius: 21px;
}

.input-wrapper:focus-within {
  background: white;
  border-color: rgba(139, 94, 60, 0.4);
}

.input-wrapper textarea {
  width: 100%;
  max-height: 90px;
  min-height: 39px;
  padding: 10px 39px 9px 13px;
  overflow-y: auto;
  color: var(--chat-text);
  background: transparent;
  border: none;
  outline: none;
  resize: none;
  font-size: 13px;
  line-height: 1.4;
}

.emoji-button {
  position: absolute;
  right: 4px;
  bottom: 4px;
  display: flex;
  width: 33px;
  height: 33px;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 50%;
  cursor: pointer;
}

.emoji-button:hover {
  background: #e7e7e7;
}

.emoji-panel {
  position: absolute;
  right: 0;
  bottom: calc(100% + 8px);
  display: grid;
  width: 205px;
  padding: 9px;
  background: white;
  border: 1px solid #e5e5e5;
  border-radius: 13px;
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.15);
  grid-template-columns: repeat(5, 1fr);
}

.emoji-panel button {
  display: flex;
  width: 35px;
  height: 35px;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 7px;
  cursor: pointer;
  font-size: 18px;
}

.emoji-panel button:hover {
  background: #f1f1f1;
}

.send-button {
  display: flex;
  width: 41px;
  height: 41px;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: white;
  background: var(--chat-primary);
  border: none;
  border-radius: 50%;
  cursor: pointer;
}

.send-button:hover:not(:disabled) {
  background: var(--chat-primary-dark);
}

.send-button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.send-button svg {
  width: 19px;
  height: 19px;
}

.chat-note {
  padding: 0 10px 8px;
  color: #aaa;
  background: white;
  font-size: 9px;
  text-align: center;
}

.message-container::-webkit-scrollbar,
.input-wrapper textarea::-webkit-scrollbar {
  width: 5px;
}

.message-container::-webkit-scrollbar-thumb,
.input-wrapper textarea::-webkit-scrollbar-thumb {
  background: #d5d5d5;
  border-radius: 10px;
}

@media (max-width: 480px) {
  .customer-chat {
    right: 10px;
    bottom: 10px;
  }

  .chat-box {
    width: calc(100vw - 20px);
    height: min(520px, calc(100vh - 20px));
  }

  .chat-launcher {
    width: 56px;
    height: 56px;
  }
}
</style>