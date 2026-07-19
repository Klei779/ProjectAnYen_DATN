<template>
  <div class="customer-chat">
    <Transition name="launcher-pop" mode="out-in">
    <button
        v-if="!isOpen"
        type="button"
        class="chat-launcher"
        aria-label="Mở trợ lý tư vấn An Yên"
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

    <section
        v-else
        class="chat-box"
        :class="{ minimized: isMinimized }"
    >
      <header class="chat-header">
        <div class="staff-avatar-wrapper">
          <div class="staff-avatar">
            AI
          </div>

          <span class="online-dot"></span>
        </div>

        <div class="staff-info">
          <strong>{{ staffDisplayName }}</strong>

          <span>
            <span class="status-dot"></span>
            {{ sessionStatusText }}
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
        <!-- Chưa tạo phiên -->
        <main
            v-if="!sessionInfo"
            class="message-container customer-name-panel"
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

            <p>
              Vui lòng cho An Yên biết tên để trợ lý AI
              hỗ trợ anh/chị chính xác hơn.
            </p>
          </div>

          <form
              class="customer-name-form"
              @submit.prevent="startChatSession"
          >
            <label for="customer-chat-name">
              Tên của bạn
            </label>

            <input
                id="customer-chat-name"
                ref="nameInput"
                v-model.trim="customerName"
                type="text"
                maxlength="100"
                autocomplete="name"
                placeholder="Nhập họ và tên"
                :disabled="creatingSession"
            />

            <p
                v-if="chatError"
                class="chat-error"
            >
              {{ chatError }}
            </p>

            <button
                type="submit"
                :disabled="
                creatingSession ||
                customerName.trim().length < 2
              "
            >
              {{
                creatingSession
                    ? "Đang kết nối..."
                    : "Bắt đầu tư vấn"
              }}
            </button>
          </form>
        </main>

        <!-- Đã có phiên -->
        <template v-else>
          <main
              ref="messageContainer"
              class="message-container"
          >
            <div class="welcome-message compact-welcome">
              <strong>
                Xin chào {{ sessionInfo.tenKhachHang }}!
              </strong>

              <p>
                Trợ lý AI của An Yên sẵn sàng hỗ trợ anh/chị.
              </p>
            </div>

            <div
                v-if="loadingMessages && messages.length === 0"
                class="chat-loading"
            >
              Đang tải tin nhắn...
            </div>

            <div
                v-else-if="messages.length === 0"
                class="empty-message-hint"
            >
              Anh/chị có thể mô tả nhu cầu, ngân sách hoặc
              dịch vụ cần tư vấn. Trợ lý AI sẽ hỏi thêm những
              thông tin cần thiết trước khi chuyển đến Hotline.
            </div>

            <div
                v-for="message in messages"
                :key="message.id"
                class="message-row"
                :class="
                message.sender === 'customer'
                  ? 'sent'
                  : 'received'
              "
            >
              <div
                  v-if="message.sender !== 'customer'"
                  class="small-avatar"
              >
                {{ message.sender === "ai" ? "AI" : "NV" }}
              </div>

              <div class="message-content">
                <div class="message-bubble">
                  {{ message.content }}
                </div>

                <div class="message-time">
                  {{ formatTime(message.createdAt) }}

                  <span
                      v-if="message.sender === 'customer'"
                  >
                    · {{ message.seen ? "Đã xem" : "Đã gửi" }}
                  </span>

                  <span
                      v-else-if="message.sender === 'ai'"
                  >
                    · Trợ lý AI
                  </span>
                </div>
              </div>
            </div>

            <!-- AI đang xử lý -->
            <div
                v-if="aiThinking"
                class="typing-row"
            >
              <div class="small-avatar">
                AI
              </div>

              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>

            <p
                v-if="chatError"
                class="chat-error message-error"
            >
              {{ chatError }}
            </p>

            <div
                v-if="readyForHotline && !customerConfirmed"
                class="ai-status-box waiting-confirmation"
            >
              <strong>Thông tin đã được tổng hợp</strong>

              <p>
                Anh/chị vui lòng kiểm tra và xác nhận thông tin
                để An Yên chuyển đến nhân viên Hotline.
              </p>
            </div>

            <div
                v-if="customerConfirmed"
                class="ai-status-box confirmed"
            >
              <strong>Đã chuyển đến Hotline</strong>

              <p>
                Thông tin của anh/chị đã được gửi đến nhân viên
                An Yên để tiếp tục hỗ trợ.
              </p>
            </div>

            <div
                v-if="sessionInfo.trangThai === 2"
                class="closed-session-box"
            >
              <p>Phiên tư vấn này đã kết thúc.</p>

              <button
                  type="button"
                  @click="resetChatSession"
              >
                Bắt đầu phiên mới
              </button>
            </div>
          </main>

          <footer
              v-if="sessionInfo.trangThai !== 2"
              class="chat-footer"
          >
            <div class="input-wrapper">
              <textarea
                  ref="messageInput"
                  v-model="newMessage"
                  rows="1"
                  maxlength="2000"
                  placeholder="Nhập nội dung cần tư vấn..."
                  :disabled="sendingMessage || aiThinking"
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
                :disabled="
                sendingMessage ||
                aiThinking ||
                !newMessage.trim()
              "
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
        </template>

        <div class="chat-note">
          Trợ lý AI có thể hỗ trợ tiếp nhận thông tin ban đầu.
          Thông tin của bạn được bảo mật.
        </div>
      </template>
    </section>
    </Transition>
  </div>
</template>

<script setup>
import {
  computed,
  nextTick,
  onMounted,
  onUnmounted,
  ref,
} from "vue";

import {
  createCustomerChatSession,
  getCustomerChatSession,
  getCustomerMessages,
  markCustomerMessagesRead,
  phanTichTinNhanAi,
  sendCustomerMessage,
} from "../../services/tuVanService.js";

const STORAGE_KEY = "anyen_customer_chat_session";
const AI_MESSAGES_KEY = "anyen_customer_ai_messages";
const POLL_INTERVAL = 2500;

const isOpen = ref(false);
const isMinimized = ref(false);
const unreadCount = ref(0);
const newMessage = ref("");
const showEmoji = ref(false);
const customerName = ref("");
const sessionInfo = ref(null);
const messages = ref([]);

const creatingSession = ref(false);
const loadingMessages = ref(false);
const sendingMessage = ref(false);
const aiThinking = ref(false);
const chatError = ref("");

const readyForHotline = ref(false);
const customerConfirmed = ref(false);

const messageContainer = ref(null);
const messageInput = ref(null);
const nameInput = ref(null);

let pollTimer = null;
let polling = false;
let knownMessageIds = new Set();

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
  "✅",
];

const staffDisplayName = computed(() => {
  if (sessionInfo.value?.maNhanVienPhuTrach) {
    return "Nhân viên tư vấn An Yên";
  }

  return "Trợ lý AI An Yên";
});

const sessionStatusText = computed(() => {
  if (!sessionInfo.value) {
    return "Sẵn sàng hỗ trợ";
  }

  if (sessionInfo.value.trangThai === 2) {
    return "Phiên đã kết thúc";
  }

  if (sessionInfo.value.maNhanVienPhuTrach) {
    return "Nhân viên đang tư vấn";
  }

  if (customerConfirmed.value) {
    return "Đã chuyển đến Hotline";
  }

  return "Trợ lý AI đang trực tuyến";
});

function openChat() {
  isOpen.value = true;
  isMinimized.value = false;
  unreadCount.value = 0;

  nextTick(async () => {
    if (sessionInfo.value) {
      await loadMessages();
      await markVisibleMessagesRead();
      messageInput.value?.focus();
    } else {
      nameInput.value?.focus();
    }

    await scrollToBottom();
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
    unreadCount.value = 0;

    nextTick(async () => {
      await markVisibleMessagesRead();
      await scrollToBottom();
      messageInput.value?.focus();
    });
  }
}

async function startChatSession() {
  const name = customerName.value.trim();

  if (name.length < 2) {
    return;
  }

  creatingSession.value = true;
  chatError.value = "";

  try {
    const response =
        await createCustomerChatSession(name);

    sessionInfo.value = response.data;

    saveSession(response.data);

    messages.value = [];
    knownMessageIds = new Set();

    readyForHotline.value = false;
    customerConfirmed.value = false;

    clearLocalAiMessages();

    addAiMessage(
        `Xin chào ${name}. An Yên có thể hỗ trợ anh/chị ` +
        "tư vấn nhu cầu, ngân sách và tiếp nhận thông tin " +
        "để chuyển đến nhân viên Hotline."
    );

    await loadMessages(true);

    startPolling();

    await scrollToBottom();

    nextTick(() => {
      messageInput.value?.focus();
    });
  } catch (error) {
    chatError.value = getErrorMessage(
        error,
        "Không thể bắt đầu phiên tư vấn"
    );
  } finally {
    creatingSession.value = false;
  }
}

async function sendMessage() {
  const content = newMessage.value.trim();
  const token = sessionInfo.value?.tokenPhien;

  if (
      !content ||
      !token ||
      sendingMessage.value ||
      aiThinking.value
  ) {
    return;
  }

  sendingMessage.value = true;
  aiThinking.value = true;
  chatError.value = "";

  /*
   * Hiển thị ngay tin nhắn của khách để giao diện
   * không phải chờ API tải lại.
   */
  const temporaryCustomerMessage = {
    id: `customer-temp-${Date.now()}`,
    sender: "customer",
    content,
    createdAt: new Date().toISOString(),
    seen: false,
    local: true,
  };

  messages.value.push(temporaryCustomerMessage);

  newMessage.value = "";
  showEmoji.value = false;

  resetTextarea();

  await scrollToBottom();

  try {
    /*
     * Bước 1: lưu tin nhắn khách vào bảng tinnhantuvan.
     */
    await sendCustomerMessage(token, content);

    /*
     * Bước 2: gửi cùng nội dung sang AI.
     */
    const aiResponse =
        await phanTichTinNhanAi(token, content);

    const aiData = aiResponse?.data?.data;

    if (!aiData) {
      throw new Error(
          "API AI không trả về dữ liệu"
      );
    }

    readyForHotline.value =
        Boolean(aiData.readyForHotline);

    customerConfirmed.value =
        Boolean(aiData.customerConfirmed);

    const aiReply =
        aiData.reply ||
        "An Yên đã ghi nhận thông tin của anh/chị.";

    /*
     * Hiện câu trả lời AI trong hộp chat.
     */
    addAiMessage(aiReply);

    /*
     * Lưu tạm câu AI vào sessionStorage.
     * Câu AI vẫn còn khi khách chuyển trang hoặc reload.
     */
    saveLocalAiMessages();

    /*
     * Tải lại tin nhắn thật từ backend.
     */
    await loadMessages();

    await scrollToBottom();
  } catch (error) {
    /*
     * Xóa tin nhắn tạm nếu lưu tin khách thất bại.
     * Nếu tin đã lưu nhưng AI lỗi thì loadMessages()
     * vẫn lấy lại được tin khách từ backend.
     */
    await loadMessages();

    chatError.value = getErrorMessage(
        error,
        "Trợ lý AI hiện chưa thể phản hồi. " +
        "Anh/chị vui lòng thử lại."
    );
  } finally {
    sendingMessage.value = false;
    aiThinking.value = false;

    await scrollToBottom();

    nextTick(() => {
      messageInput.value?.focus();
    });
  }
}

function addAiMessage(content) {
  if (!content || !content.trim()) {
    return;
  }

  const aiMessage = {
    id:
        "ai-local-" +
        Date.now() +
        "-" +
        Math.random().toString(36).slice(2),
    sender: "ai",
    content: content.trim(),
    createdAt: new Date().toISOString(),
    seen: true,
    localAi: true,
  };

  messages.value.push(aiMessage);

  saveLocalAiMessages();
}

async function refreshSession() {
  const token = sessionInfo.value?.tokenPhien;

  if (!token) {
    return;
  }

  const response =
      await getCustomerChatSession(token);

  sessionInfo.value = response.data;

  saveSession(response.data);
}

async function loadMessages(initialLoad = false) {
  const token = sessionInfo.value?.tokenPhien;

  if (!token || loadingMessages.value) {
    return;
  }

  loadingMessages.value = true;

  try {
    /*
     * Giữ lại tin AI đang được lưu tạm trên trình duyệt.
     */
    const localAiMessages =
        getLocalAiMessages();

    const response =
        await getCustomerMessages(token);

    const apiMessages = Array.isArray(response.data)
        ? response.data
        : [];

    if (!initialLoad) {
      const newStaffMessages =
          apiMessages.filter((message) => {
            return (
                message.nguoiGui === "NHAN_VIEN" &&
                !knownMessageIds.has(message.maTinNhan)
            );
          });

      if (
          newStaffMessages.length > 0 &&
          (!isOpen.value || isMinimized.value)
      ) {
        unreadCount.value +=
            newStaffMessages.length;
      }
    }

    const mappedApiMessages =
        apiMessages.map((message) => {
          let sender = "staff";

          if (
              message.nguoiGui === "KHACH_HANG"
          ) {
            sender = "customer";
          } else if (
              message.nguoiGui === "AI"
          ) {
            sender = "ai";
          }

          return {
            id: message.maTinNhan,
            sender,
            content: message.noiDung,
            createdAt: message.createdAt,
            seen: Boolean(message.daDoc),
            localAi: false,
          };
        });

    /*
     * Ghép tin từ DB với các câu AI lưu tạm.
     */
    messages.value = [
      ...mappedApiMessages,
      ...localAiMessages,
    ].sort((first, second) => {
      return (
          new Date(first.createdAt).getTime() -
          new Date(second.createdAt).getTime()
      );
    });

    knownMessageIds = new Set(
        apiMessages.map(
            (message) => message.maTinNhan
        )
    );

    if (
        isOpen.value &&
        !isMinimized.value
    ) {
      unreadCount.value = 0;

      await markVisibleMessagesRead();
    }
  } catch (error) {
    if (error?.response?.status === 404) {
      resetChatSession();
    } else {
      chatError.value = getErrorMessage(
          error,
          "Mất kết nối với hệ thống tư vấn"
      );
    }
  } finally {
    loadingMessages.value = false;
  }
}

async function pollChat() {
  if (
      polling ||
      !sessionInfo.value?.tokenPhien
  ) {
    return;
  }

  polling = true;

  try {
    await refreshSession();
    await loadMessages();
  } catch (error) {
    console.error(
        "Không thể cập nhật phiên tư vấn:",
        error
    );
  } finally {
    polling = false;
  }
}

async function markVisibleMessagesRead() {
  const token =
      sessionInfo.value?.tokenPhien;

  if (
      !token ||
      !isOpen.value ||
      isMinimized.value
  ) {
    return;
  }

  try {
    await markCustomerMessagesRead(token);

    messages.value.forEach((message) => {
      if (message.sender === "staff") {
        message.seen = true;
      }
    });
  } catch (error) {
    console.error(
        "Không thể đánh dấu tin nhắn đã đọc:",
        error
    );
  }
}

function saveSession(session) {
  sessionStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        tokenPhien: session.tokenPhien,
        tenKhachHang: session.tenKhachHang,
      })
  );
}

function saveLocalAiMessages() {
  const token =
      sessionInfo.value?.tokenPhien;

  if (!token) {
    return;
  }

  const localAiMessages =
      messages.value.filter(
          (message) => message.localAi
      );

  const storageData = getAllStoredAiMessages();

  storageData[token] = localAiMessages;

  sessionStorage.setItem(
      AI_MESSAGES_KEY,
      JSON.stringify(storageData)
  );
}

function getLocalAiMessages() {
  const token =
      sessionInfo.value?.tokenPhien;

  if (!token) {
    return [];
  }

  const storageData = getAllStoredAiMessages();
  const storedMessages = storageData[token];

  return Array.isArray(storedMessages)
      ? storedMessages
      : [];
}

function getAllStoredAiMessages() {
  try {
    return JSON.parse(
        sessionStorage.getItem(
            AI_MESSAGES_KEY
        ) || "{}"
    );
  } catch {
    return {};
  }
}

function clearLocalAiMessages() {
  const token =
      sessionInfo.value?.tokenPhien;

  if (!token) {
    return;
  }

  const storageData =
      getAllStoredAiMessages();

  delete storageData[token];

  sessionStorage.setItem(
      AI_MESSAGES_KEY,
      JSON.stringify(storageData)
  );
}

async function restoreSession() {
  try {
    const stored = JSON.parse(
        sessionStorage.getItem(
            STORAGE_KEY
        ) || "null"
    );

    if (!stored?.tokenPhien) {
      return;
    }

    customerName.value =
        stored.tenKhachHang || "";

    const response =
        await getCustomerChatSession(
            stored.tokenPhien
        );

    sessionInfo.value = response.data;

    unreadCount.value = Number(
        response.data
            ?.soTinNhanChuaDocKhach || 0
    );

    await loadMessages(true);

    startPolling();
  } catch {
    sessionStorage.removeItem(
        STORAGE_KEY
    );

    sessionInfo.value = null;
  }
}

function resetChatSession() {
  clearLocalAiMessages();

  sessionStorage.removeItem(
      STORAGE_KEY
  );

  sessionInfo.value = null;
  messages.value = [];
  knownMessageIds = new Set();
  unreadCount.value = 0;
  newMessage.value = "";
  chatError.value = "";
  readyForHotline.value = false;
  customerConfirmed.value = false;
  aiThinking.value = false;

  stopPolling();

  nextTick(() => {
    nameInput.value?.focus();
  });
}

function startPolling() {
  stopPolling();

  pollTimer = window.setInterval(
      pollChat,
      POLL_INTERVAL
  );
}

function stopPolling() {
  if (pollTimer) {
    window.clearInterval(pollTimer);

    pollTimer = null;
  }
}

function toggleEmoji() {
  showEmoji.value =
      !showEmoji.value;
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
  const textarea =
      messageInput.value;

  if (!textarea) {
    return;
  }

  textarea.style.height = "auto";

  textarea.style.height =
      `${Math.min(
          textarea.scrollHeight,
          90
      )}px`;
}

function resetTextarea() {
  nextTick(() => {
    if (messageInput.value) {
      messageInput.value.style.height =
          "auto";
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
  const date = new Date(dateValue);

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  return new Intl.DateTimeFormat(
      "vi-VN",
      {
        hour: "2-digit",
        minute: "2-digit",
      }
  ).format(date);
}

function getErrorMessage(
    error,
    fallback
) {
  const data = error?.response?.data;

  if (typeof data === "string") {
    return data;
  }

  if (data?.message) {
    return data.message;
  }

  return (
      error?.message ||
      Object.values(data || {})[0] ||
      fallback
  );
}

onMounted(restoreSession);
onUnmounted(stopPolling);
</script>

<style scoped>
.customer-chat {
  --chat-primary: #8b1024;
  --chat-primary-dark: #650817;
  --chat-primary-light: #f8e8ec;
  --chat-text: #292929;
  --chat-secondary: #7b7b7b;

  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 9999;
  font-family: Inter, Arial, sans-serif;
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
  background: linear-gradient(
      135deg,
      #b51230 0%,
      #8c091f 100%
  );
  border: none;
  border-radius: 50%;
  box-shadow:
      0 10px 30px
      rgba(112, 20, 40, 0.35);
  cursor: pointer;
  transition:
      transform 0.2s,
      background 0.2s;
}

.chat-launcher:hover {
  background: linear-gradient(
      135deg,
      #b51230 50%,
      #8c091f 50%
  );
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
  width: 370px;
  height: 560px;
  overflow: hidden;
  background: white;
  border: 1px solid #e7e7e7;
  border-radius: 18px;
  box-shadow:
      0 16px 50px
      rgba(0, 0, 0, 0.18);
  flex-direction: column;
  animation: openChat 0.2s ease-out;
}

.chat-box.minimized {
  height: 70px;
}

/* Bong bóng xuất hiện */
.launcher-pop-enter-active {
  animation: launcherAppear 0.42s
  cubic-bezier(0.22, 1, 0.36, 1);
}

.launcher-pop-leave-active {
  animation: launcherDisappear 0.2s ease-in forwards;
}

/* Khung chat mở lên */
.launcher-pop-enter-active.chat-box {
  animation: chatBoxOpen 0.42s
  cubic-bezier(0.22, 1, 0.36, 1);
}

/* Khung chat đóng xuống */
.launcher-pop-leave-active.chat-box {
  animation: chatBoxClose 0.25s ease-in forwards;
}

@keyframes launcherAppear {
  0% {
    opacity: 0;
    transform: translateY(25px) scale(0.5);
  }

  65% {
    opacity: 1;
    transform: translateY(-5px) scale(1.08);
  }

  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes launcherDisappear {
  from {
    opacity: 1;
    transform: scale(1);
  }

  to {
    opacity: 0;
    transform: translateY(12px) scale(0.65);
  }
}

@keyframes chatBoxOpen {
  0% {
    opacity: 0;
    transform:
        translateY(45px)
        scale(0.82);
    transform-origin: bottom right;
  }

  65% {
    opacity: 1;
    transform:
        translateY(-5px)
        scale(1.02);
    transform-origin: bottom right;
  }

  100% {
    opacity: 1;
    transform:
        translateY(0)
        scale(1);
    transform-origin: bottom right;
  }
}

@keyframes chatBoxClose {
  from {
    opacity: 1;
    transform:
        translateY(0)
        scale(1);
    transform-origin: bottom right;
  }

  to {
    opacity: 0;
    transform:
        translateY(35px)
        scale(0.82);
    transform-origin: bottom right;
  }
}

.chat-header {
  display: flex;
  min-height: 70px;
  align-items: center;
  gap: 11px;
  padding: 11px 13px;
  color: white;
  background: linear-gradient(
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
  border-radius: 50%;
  font-weight: 700;
}

.staff-avatar {
  width: 46px;
  height: 46px;
  color: var(--chat-primary);
  background: white;
  font-size: 13px;
}

.small-avatar {
  width: 29px;
  height: 29px;
  flex-shrink: 0;
  color: white;
  background: var(--chat-primary);
  font-size: 9px;
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
  background:
      rgba(255, 255, 255, 0.14);
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
          rgba(255, 255, 255, 0.94),
          rgba(255, 255, 255, 0.94)
      ),
      radial-gradient(
          circle at top left,
          #f4e4e8,
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
  line-height: 1.5;
}

.compact-welcome {
  margin-bottom: 8px;
}

.customer-name-panel {
  display: flex;
  justify-content: center;
  flex-direction: column;
}

.customer-name-form {
  display: flex;
  padding: 0 16px;
  gap: 10px;
  flex-direction: column;
}

.customer-name-form label {
  color: var(--chat-text);
  font-size: 12px;
  font-weight: 700;
}

.customer-name-form input {
  width: 100%;
  box-sizing: border-box;
  padding: 11px 12px;
  border: 1px solid #d7d7d7;
  border-radius: 10px;
  outline: none;
}

.customer-name-form input:focus {
  border-color: var(--chat-primary);
  box-shadow:
      0 0 0 3px
      rgba(139, 16, 36, 0.12);
}

.customer-name-form button,
.closed-session-box button {
  padding: 11px 14px;
  color: white;
  background: var(--chat-primary);
  border: 0;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 700;
}

.customer-name-form button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.chat-loading,
.empty-message-hint {
  padding: 14px;
  color: var(--chat-secondary);
  background:
      rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  font-size: 12px;
  line-height: 1.5;
  text-align: center;
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
  line-height: 1.48;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}

.sent .message-bubble {
  color: white;
  background: var(--chat-primary);
  border-radius:
      15px 15px 4px 15px;
}

.received .message-bubble {
  color: var(--chat-text);
  background: #f0f0f1;
  border-radius:
      15px 15px 15px 4px;
}

.message-time {
  margin-top: 3px;
  padding: 0 3px;
  color: #999;
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
  border-radius:
      15px 15px 15px 4px;
}

.typing-indicator span {
  width: 5px;
  height: 5px;
  background: #929292;
  border-radius: 50%;
  animation:
      typing 1.2s infinite ease-in-out;
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

.ai-status-box {
  margin-top: 13px;
  padding: 12px;
  border-radius: 12px;
  font-size: 11px;
  line-height: 1.5;
}

.ai-status-box strong {
  display: block;
  margin-bottom: 4px;
  font-size: 12px;
}

.ai-status-box p {
  margin: 0;
}

.ai-status-box.waiting-confirmation {
  color: #854d0e;
  background: #fefce8;
  border: 1px solid #fde68a;
}

.ai-status-box.confirmed {
  color: #166534;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
}

.chat-error {
  margin: 0;
  color: #b42318;
  font-size: 11px;
  line-height: 1.4;
}

.message-error {
  margin-top: 10px;
  text-align: center;
}

.closed-session-box {
  margin-top: 14px;
  padding: 14px;
  color: #9a3412;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 12px;
  text-align: center;
}

.closed-session-box p {
  margin: 0 0 10px;
  font-size: 12px;
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
  border-color:
      rgba(139, 16, 36, 0.4);
}

.input-wrapper textarea {
  width: 100%;
  max-height: 90px;
  min-height: 39px;
  box-sizing: border-box;
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
  box-shadow:
      0 12px 35px
      rgba(0, 0, 0, 0.15);
  grid-template-columns:
    repeat(5, 1fr);
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
  background:
      var(--chat-primary-dark);
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
  color: #999;
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
    height:
        min(
            560px,
            calc(100vh - 20px)
        );
  }

  .chat-launcher {
    width: 56px;
    height: 56px;
  }
}
</style>