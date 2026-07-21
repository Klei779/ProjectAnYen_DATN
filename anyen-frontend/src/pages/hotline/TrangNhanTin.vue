<template>
  <div class="chat-page">
    <aside class="session-panel">
      <div class="panel-header">
        <div>
          <span class="eyebrow">TƯ VẤN TRỰC TUYẾN</span>
          <h1>Hội thoại khách hàng</h1>
        </div>

        <button class="refresh-button" type="button" @click="refreshAll" :disabled="loadingSessions">
          <i class="fa-solid fa-rotate-right"></i>
        </button>
      </div>

      <div class="online-box">
        <span class="online-dot"></span>
        <div>
          <strong>{{ currentUser?.hoTen || "Nhân viên tư vấn" }}</strong>
          <small>Đang online và sẵn sàng nhận phiên</small>
        </div>
      </div>

      <div class="search-box">
        <i class="fa-solid fa-magnifying-glass"></i>
        <input v-model.trim="searchKeyword" placeholder="Tìm theo tên hoặc nội dung..." />
      </div>

      <p v-if="pageError" class="error-box">{{ pageError }}</p>

      <div class="session-list">
        <button
          v-for="session in filteredSessions"
          :key="session.id"
          type="button"
          class="session-item"
          :class="{ active: selectedSession?.id === session.id }"
          @click="selectSession(session)"
        >
          <div class="avatar">{{ getInitials(session.name) }}</div>

          <div class="session-main">
            <div class="session-topline">
              <strong>{{ session.name }}</strong>
              <time>{{ formatSessionTime(session.lastMessageAt) }}</time>
            </div>

            <div class="session-preview-row">
              <span class="session-preview">{{ session.lastMessage || "Chưa có tin nhắn" }}</span>
              <span v-if="session.unread > 0" class="unread-badge">{{ session.unread }}</span>
            </div>

            <div class="session-meta">
              <span class="status-pill" :class="`status-${session.status}`">
                {{ session.statusLabel }}
              </span>
              <small v-if="session.ownerName">{{ session.ownerName }}</small>
              <small v-else>Chưa có người nhận</small>
            </div>
          </div>
        </button>

        <div v-if="!loadingSessions && filteredSessions.length === 0" class="empty-list">
          <i class="fa-regular fa-comments"></i>
          <p>Chưa có phiên tư vấn phù hợp.</p>
        </div>
      </div>
    </aside>

    <main v-if="selectedSession" class="conversation-panel">
      <header class="conversation-header">
        <div class="customer-info">
          <div class="avatar large">{{ getInitials(selectedSession.name) }}</div>
          <div>
            <h2>{{ selectedSession.name }}</h2>
            <p>
              {{ selectedSession.statusLabel }}
              <span v-if="selectedSession.ownerName"> · {{ selectedSession.ownerName }}</span>
            </p>
          </div>
        </div>

        <div class="header-actions">
          <button
            v-if="!selectedSession.ownerId && selectedSession.status !== 2"
            type="button"
            class="claim-button"
            :disabled="claiming"
            @click="claimSelectedSession"
          >
            {{ claiming ? "Đang nhận..." : "Nhận tư vấn" }}
          </button>

          <button
            v-if="selectedSession.ownerId === currentEmployeeId && selectedSession.status !== 2"
            type="button"
            class="close-session-button"
            :disabled="closingSession"
            @click="closeSelectedSession"
          >
            Kết thúc phiên
          </button>
        </div>
      </header>

      <section ref="messageContainer" class="message-list">
        <div class="handoff-note">
          <i class="fa-solid fa-robot"></i>
          <span>Phần hội thoại với chatbot và phần nhân viên tiếp tục đều được lưu chung tại đây.</span>
        </div>

        <div v-if="loadingMessages" class="loading-text">Đang tải hội thoại...</div>

        <template v-else>
          <div
            v-for="(message, index) in selectedSession.messages"
            :key="message.id"
            class="message-group"
          >
            <div v-if="shouldShowDate(index)" class="date-divider">
              {{ formatDateLabel(message.createdAt) }}
            </div>

            <div class="message-row" :class="message.sender">
              <div v-if="message.sender !== 'me'" class="sender-avatar" :class="message.sender">
                {{ message.sender === "ai" ? "AI" : getInitials(selectedSession.name) }}
              </div>

              <div class="bubble-wrap">
                <small v-if="message.sender !== 'me'" class="sender-name">
                  {{ message.sender === "ai" ? "Trợ lý AI An Yên" : selectedSession.name }}
                </small>
                <div class="message-bubble">{{ message.content }}</div>
                <div class="message-time">
                  {{ formatMessageTime(message.createdAt) }}
                  <span v-if="message.sender === 'me'"> · {{ message.seen ? "Đã xem" : "Đã gửi" }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="selectedSession.messages.length === 0" class="empty-messages">
            Chưa có nội dung hội thoại.
          </div>
        </template>
      </section>

      <footer class="composer">
        <textarea
          ref="messageInput"
          v-model="newMessage"
          rows="1"
          maxlength="2000"
          :disabled="!canReply || sendingMessage"
          :placeholder="composerPlaceholder"
          @input="resizeTextarea"
          @keydown.enter.exact.prevent="sendMessage"
        ></textarea>

        <button
          type="button"
          class="send-button"
          :disabled="!canReply || sendingMessage || !newMessage.trim()"
          @click="sendMessage"
        >
          <i class="fa-solid fa-paper-plane"></i>
        </button>
      </footer>
    </main>

    <main v-else class="empty-conversation">
      <i class="fa-regular fa-comments"></i>
      <h2>Chọn một phiên tư vấn</h2>
      <p>Nhân viên sẽ thấy đầy đủ tin nhắn của khách, chatbot và nhân viên trong cùng một luồng.</p>
    </main>
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
  claimStaffChatSession,
  closeStaffChatSession,
  getStaffChatSessions,
  getStaffMessages,
  heartbeatStaffChat,
  sendStaffMessage,
} from "../../services/tuVanService.js";

const POLL_INTERVAL = 2500;
const HEARTBEAT_INTERVAL = 20000;

const searchKeyword = ref("");
const sessions = ref([]);
const selectedSession = ref(null);
const newMessage = ref("");

const loadingSessions = ref(false);
const loadingMessages = ref(false);
const sendingMessage = ref(false);
const claiming = ref(false);
const closingSession = ref(false);

const pageError = ref("");

const messageContainer = ref(null);
const messageInput = ref(null);

let pollTimer = null;
let heartbeatTimer = null;
let pollRunning = false;
let sessionsRequestRunning = false;
let visibleMessageRequestCount = 0;

/*
 * Không cho cùng một phiên gửi nhiều request tải tin nhắn
 * trong cùng một thời điểm.
 */
const messageRequests = new Set();

const currentUser = computed(() => {
  try {
    return JSON.parse(localStorage.getItem("user") || "null");
  } catch {
    return null;
  }
});

const currentEmployeeId = computed(() =>
    Number(currentUser.value?.id || 0)
);

const filteredSessions = computed(() => {
  const keyword = searchKeyword.value
      .toLowerCase()
      .trim();

  if (!keyword) {
    return sessions.value;
  }

  return sessions.value.filter((session) => {
    const customerName = String(
        session.name || ""
    ).toLowerCase();

    const lastMessage = String(
        session.lastMessage || ""
    ).toLowerCase();

    return (
        customerName.includes(keyword) ||
        lastMessage.includes(keyword)
    );
  });
});

const canReply = computed(() => {
  const session = selectedSession.value;

  return Boolean(
      session &&
      session.ownerId === currentEmployeeId.value &&
      session.status !== 2
  );
});

const composerPlaceholder = computed(() => {
  const session = selectedSession.value;

  if (!session) {
    return "Chọn một phiên tư vấn";
  }

  if (session.status === 2) {
    return "Phiên tư vấn đã kết thúc";
  }

  if (!session.ownerId) {
    return "Nhấn “Nhận tư vấn” để trả lời";
  }

  if (session.ownerId !== currentEmployeeId.value) {
    return "Phiên đang do nhân viên khác phụ trách";
  }

  return "Nhập nội dung để tiếp tục cuộc trò chuyện...";
});

/* =========================================================
 * REFRESH THỦ CÔNG
 * ========================================================= */

async function refreshAll() {
  await heartbeat();
  await loadSessions(false);

  const session = selectedSession.value;

  if (
      session &&
      session.ownerId === currentEmployeeId.value
  ) {
    await loadMessages(
        session.id,
        false,
        true
    );
  }
}

/* =========================================================
 * HEARTBEAT
 * Chỉ khai báo duy nhất một lần.
 * ========================================================= */

async function heartbeat() {
  try {
    await heartbeatStaffChat();
  } catch (error) {
    console.error(
        "Không thể cập nhật trạng thái online:",
        error
    );
  }
}

/* =========================================================
 * TẢI DANH SÁCH PHIÊN
 * silent = true: polling âm thầm, không nhấp nháy loading.
 * ========================================================= */

async function loadSessions(silent = false) {
  if (sessionsRequestRunning) {
    return;
  }

  sessionsRequestRunning = true;

  if (!silent) {
    loadingSessions.value = true;
  }

  try {
    const response = await getStaffChatSessions();

    const incoming = Array.isArray(response.data)
        ? response.data
        : [];

    const selectedId = selectedSession.value?.id;

    /*
     * Giữ lại object phiên cũ để Vue không render lại toàn bộ
     * danh sách và khu vực hội thoại sau mỗi lần polling.
     */
    const oldSessionsById = new Map(
        sessions.value.map((session) => [
          session.id,
          session,
        ])
    );

    const nextSessions = incoming.map((rawSession) => {
      const id = Number(rawSession.maPhien);

      const ownerId =
          rawSession.maNhanVienPhuTrach == null
              ? null
              : Number(rawSession.maNhanVienPhuTrach);

      const normalizedSession = {
        id,
        name: rawSession.tenKhachHang,
        ownerId,
        ownerName: rawSession.tenNhanVienPhuTrach,
        status: Number(rawSession.trangThai),
        statusLabel: rawSession.tenTrangThai,
        lastMessage: rawSession.tinNhanCuoi,
        lastMessageAt: rawSession.thoiGianTinNhanCuoi,
        unread: Number(
            rawSession.soTinNhanChuaDocNhanVien || 0
        ),
      };

      const oldSession = oldSessionsById.get(id);

      if (oldSession) {
        /*
         * Cập nhật trực tiếp object cũ.
         * Mảng messages vẫn được giữ nguyên.
         */
        Object.assign(
            oldSession,
            normalizedSession
        );

        return oldSession;
      }

      return {
        ...normalizedSession,
        messages: [],
      };
    });

    sessions.value = nextSessions;

    if (selectedId != null) {
      selectedSession.value =
          nextSessions.find(
              (session) => session.id === selectedId
          ) || null;
    }

    pageError.value = "";
  } catch (error) {
    pageError.value = getErrorMessage(
        error,
        "Không thể tải danh sách phiên tư vấn"
    );
  } finally {
    sessionsRequestRunning = false;

    if (!silent) {
      loadingSessions.value = false;
    }
  }
}

/* =========================================================
 * CHỌN PHIÊN
 * ========================================================= */

async function selectSession(session) {
  selectedSession.value = session;
  pageError.value = "";

  if (
      !session.ownerId &&
      session.status !== 2
  ) {
    await claimSelectedSession();
    return;
  }

  if (
      session.ownerId === currentEmployeeId.value
  ) {
    await loadMessages(
        session.id,
        true,
        false
    );
  } else {
    /*
     * Không cho nhân viên khác xem nội dung phiên
     * đang thuộc người khác.
     */
    session.messages = [];
  }
}

/* =========================================================
 * NHẬN PHIÊN
 * ========================================================= */

async function claimSelectedSession() {
  const session = selectedSession.value;

  if (!session || claiming.value) {
    return;
  }

  const sessionId = session.id;

  claiming.value = true;
  pageError.value = "";

  try {
    const response =
        await claimStaffChatSession(sessionId);

    const claimed = response.data;

    session.ownerId =
        claimed.maNhanVienPhuTrach == null
            ? null
            : Number(claimed.maNhanVienPhuTrach);

    session.ownerName =
        claimed.tenNhanVienPhuTrach;

    session.status =
        Number(claimed.trangThai);

    session.statusLabel =
        claimed.tenTrangThai;

    await loadSessions(true);

    await loadMessages(
        sessionId,
        true,
        false
    );
  } catch (error) {
    pageError.value = getErrorMessage(
        error,
        "Phiên này vừa được một nhân viên khác tiếp nhận"
    );

    await loadSessions(true);
  } finally {
    claiming.value = false;
  }
}

/* =========================================================
 * TẢI TIN NHẮN
 *
 * focusInput:
 *   true  = cuộn xuống cuối và focus input.
 *
 * silent:
 *   true  = polling âm thầm, không hiện loading.
 * ========================================================= */

async function loadMessages(
    sessionId,
    focusInput = false,
    silent = false
) {
  if (!sessionId) {
    return;
  }

  /*
   * Không tải trùng cùng một phiên.
   */
  if (messageRequests.has(sessionId)) {
    return;
  }

  messageRequests.add(sessionId);

  const shouldStickToBottom =
      focusInput ||
      isMessageListNearBottom();

  if (!silent) {
    visibleMessageRequestCount += 1;
    loadingMessages.value = true;
  }

  try {
    const response =
        await getStaffMessages(sessionId);

    const data = Array.isArray(response.data)
        ? response.data
        : [];

    const target = sessions.value.find(
        (session) => session.id === sessionId
    );

    if (!target) {
      return;
    }

    const incomingMessages = data.map(
        (message) => ({
          id: message.maTinNhan,

          sender:
              message.nguoiGui === "NHAN_VIEN"
                  ? "me"
                  : message.nguoiGui === "AI"
                      ? "ai"
                      : "customer",

          content: message.noiDung,
          createdAt: message.createdAt,
          seen: Boolean(message.daDoc),
        })
    );

    /*
     * Chỉ cập nhật mảng khi nội dung thực sự thay đổi.
     * Polling không còn render lại toàn bộ hội thoại.
     */
    const messagesChanged =
        haveMessagesChanged(
            target.messages,
            incomingMessages
        );

    if (messagesChanged) {
      target.messages = incomingMessages;
    }

    target.unread = 0;

    const isStillSelected =
        selectedSession.value?.id === sessionId;

    if (isStillSelected) {
      selectedSession.value = target;
    }

    /*
     * Chỉ cuộn khi:
     * - Đây vẫn là phiên đang mở.
     * - Có tin nhắn mới và người dùng đang gần cuối.
     * - Hoặc nhân viên vừa gửi/chọn phiên.
     */
    if (
        isStillSelected &&
        (
            focusInput ||
            (
                messagesChanged &&
                shouldStickToBottom
            )
        )
    ) {
      await scrollToBottom();
    }

    if (
        isStillSelected &&
        focusInput
    ) {
      focusComposerWithoutPageScroll();
    }
  } catch (error) {
    if (
        [403, 409].includes(
            error?.response?.status
        )
    ) {
      await loadSessions(true);
    } else {
      pageError.value = getErrorMessage(
          error,
          "Không thể tải nội dung hội thoại"
      );
    }
  } finally {
    messageRequests.delete(sessionId);

    if (!silent) {
      visibleMessageRequestCount = Math.max(
          0,
          visibleMessageRequestCount - 1
      );

      loadingMessages.value =
          visibleMessageRequestCount > 0;
    }
  }
}

/* =========================================================
 * KIỂM TRA TIN NHẮN CÓ THAY ĐỔI KHÔNG
 * ========================================================= */

function haveMessagesChanged(
    oldMessages = [],
    newMessages = []
) {
  if (
      oldMessages.length !==
      newMessages.length
  ) {
    return true;
  }

  return newMessages.some(
      (newMessageItem, index) => {
        const oldMessageItem =
            oldMessages[index];

        return (
            !oldMessageItem ||
            oldMessageItem.id !==
            newMessageItem.id ||
            oldMessageItem.content !==
            newMessageItem.content ||
            oldMessageItem.sender !==
            newMessageItem.sender ||
            oldMessageItem.seen !==
            newMessageItem.seen ||
            oldMessageItem.createdAt !==
            newMessageItem.createdAt
        );
      }
  );
}

/* =========================================================
 * GỬI TIN NHẮN
 * ========================================================= */

async function sendMessage() {
  const content = newMessage.value.trim();
  const session = selectedSession.value;

  if (
      !content ||
      !session ||
      !canReply.value ||
      sendingMessage.value
  ) {
    return;
  }

  const sessionId = session.id;

  sendingMessage.value = true;
  pageError.value = "";

  try {
    await sendStaffMessage(
        sessionId,
        content
    );

    newMessage.value = "";
    resetTextarea();

    /*
     * Nhân viên vừa gửi nên chủ động cuộn xuống cuối.
     */
    await loadMessages(
        sessionId,
        true,
        false
    );

    await loadSessions(true);
  } catch (error) {
    pageError.value = getErrorMessage(
        error,
        "Không thể gửi tin nhắn"
    );
  } finally {
    sendingMessage.value = false;
  }
}

/* =========================================================
 * KẾT THÚC PHIÊN
 * ========================================================= */

async function closeSelectedSession() {
  const session = selectedSession.value;

  if (
      !session ||
      closingSession.value
  ) {
    return;
  }

  const confirmed = window.confirm(
      "Kết thúc phiên tư vấn này?"
  );

  if (!confirmed) {
    return;
  }

  closingSession.value = true;
  pageError.value = "";

  try {
    await closeStaffChatSession(
        session.id
    );

    await loadSessions(true);
  } catch (error) {
    pageError.value = getErrorMessage(
        error,
        "Không thể kết thúc phiên tư vấn"
    );
  } finally {
    closingSession.value = false;
  }
}

/* =========================================================
 * POLLING
 * Không hiện loading và không cuộn khi không có tin mới.
 * ========================================================= */

async function poll() {
  if (pollRunning) {
    return;
  }

  pollRunning = true;

  try {
    await loadSessions(true);

    const session = selectedSession.value;

    if (
        session &&
        session.ownerId ===
        currentEmployeeId.value
    ) {
      await loadMessages(
          session.id,
          false,
          true
      );
    }
  } catch (error) {
    console.error(
        "Lỗi polling hội thoại:",
        error
    );
  } finally {
    pollRunning = false;
  }
}

/* =========================================================
 * TIMER
 * ========================================================= */

function startTimers() {
  stopTimers();

  pollTimer = window.setInterval(
      poll,
      POLL_INTERVAL
  );

  heartbeatTimer = window.setInterval(
      heartbeat,
      HEARTBEAT_INTERVAL
  );
}

function stopTimers() {
  if (pollTimer) {
    window.clearInterval(pollTimer);
    pollTimer = null;
  }

  if (heartbeatTimer) {
    window.clearInterval(heartbeatTimer);
    heartbeatTimer = null;
  }
}

/* =========================================================
 * TEXTAREA
 * ========================================================= */

function resizeTextarea() {
  const textarea = messageInput.value;

  if (!textarea) {
    return;
  }

  textarea.style.height = "auto";

  textarea.style.height =
      `${Math.min(
          textarea.scrollHeight,
          120
      )}px`;
}

function resetTextarea() {
  nextTick(() => {
    const textarea = messageInput.value;

    if (textarea) {
      textarea.style.height = "auto";
    }
  });
}

/* =========================================================
 * SCROLL
 * ========================================================= */

function isMessageListNearBottom(
    threshold = 120
) {
  const container =
      messageContainer.value;

  if (!container) {
    return true;
  }

  const remaining =
      container.scrollHeight -
      container.scrollTop -
      container.clientHeight;

  return remaining <= threshold;
}

async function scrollToBottom() {
  await nextTick();

  const container =
      messageContainer.value;

  if (!container) {
    return;
  }

  /*
   * Chỉ cuộn container tin nhắn,
   * không cuộn toàn bộ trang web.
   */
  container.scrollTop =
      container.scrollHeight;
}

function focusComposerWithoutPageScroll() {
  nextTick(() => {
    const input = messageInput.value;

    if (!input) {
      return;
    }

    try {
      input.focus({
        preventScroll: true,
      });
    } catch {
      /*
       * Trình duyệt cũ không hỗ trợ preventScroll.
       */
      input.focus();
    }
  });
}

/* =========================================================
 * FORMAT GIAO DIỆN
 * ========================================================= */

function getInitials(name) {
  const words = String(name || "?")
      .trim()
      .split(/\s+/)
      .filter(Boolean);

  if (words.length === 0) {
    return "?";
  }

  if (words.length === 1) {
    return words[0]
        .charAt(0)
        .toUpperCase();
  }

  return (
      words[words.length - 2].charAt(0) +
      words[words.length - 1].charAt(0)
  ).toUpperCase();
}

function formatMessageTime(value) {
  if (!value) {
    return "";
  }

  const date = new Date(value);

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

function formatSessionTime(value) {
  if (!value) {
    return "";
  }

  const date = new Date(value);
  const today = new Date();

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  if (isSameDate(date, today)) {
    return formatMessageTime(value);
  }

  return new Intl.DateTimeFormat(
      "vi-VN",
      {
        day: "2-digit",
        month: "2-digit",
      }
  ).format(date);
}

function formatDateLabel(value) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  const today = new Date();

  const yesterday = new Date(today);
  yesterday.setDate(
      today.getDate() - 1
  );

  if (isSameDate(date, today)) {
    return "Hôm nay";
  }

  if (isSameDate(date, yesterday)) {
    return "Hôm qua";
  }

  return new Intl.DateTimeFormat(
      "vi-VN",
      {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
      }
  ).format(date);
}

function shouldShowDate(index) {
  const messages =
      selectedSession.value?.messages || [];

  if (index === 0) {
    return true;
  }

  const currentDate = new Date(
      messages[index].createdAt
  );

  const previousDate = new Date(
      messages[index - 1].createdAt
  );

  return !isSameDate(
      currentDate,
      previousDate
  );
}

function isSameDate(first, second) {
  if (
      !(first instanceof Date) ||
      !(second instanceof Date) ||
      Number.isNaN(first.getTime()) ||
      Number.isNaN(second.getTime())
  ) {
    return false;
  }

  return (
      first.getFullYear() ===
      second.getFullYear() &&
      first.getMonth() ===
      second.getMonth() &&
      first.getDate() ===
      second.getDate()
  );
}

function getErrorMessage(
    error,
    fallback
) {
  return (
      error?.response?.data?.detail ||
      error?.response?.data?.message ||
      error?.message ||
      fallback
  );
}

/* =========================================================
 * LIFECYCLE
 * ========================================================= */

onMounted(async () => {
  await heartbeat();
  await loadSessions(false);
  startTimers();
});

onUnmounted(() => {
  stopTimers();
});
</script>

<style scoped>
.chat-page {
  height: calc(100dvh - 76px);
  min-height: 0;
  display: grid;
  overflow: hidden;
  grid-template-columns: 370px minmax(0, 1fr);
  background: #f5f7fb;
  color: #243044;
}

.session-panel {
  display: flex;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  flex-direction: column;
  border-right: 1px solid #e5e9f0;
  background: #fff;
}

.panel-header,
.conversation-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 22px;
  border-bottom: 1px solid #edf0f5;
}

.panel-header h1,
.conversation-header h2 {
  margin: 3px 0 0;
  font-size: 22px;
}

.eyebrow {
  color: #8c1731;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.11em;
}

.refresh-button,
.claim-button,
.close-session-button,
.send-button {
  border: 0;
  cursor: pointer;
}

.refresh-button {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: #f3f5f8;
  color: #526078;
}

.online-box {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 16px 18px 8px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #eefaf3;
}

.online-box div {
  display: flex;
  flex-direction: column;
}

.online-box small {
  margin-top: 2px;
  color: #557064;
}

.online-dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #22a45a;
  box-shadow: 0 0 0 4px rgba(34, 164, 90, 0.14);
}

.search-box {
  display: flex;
  align-items: center;
  gap: 9px;
  margin: 10px 18px 14px;
  padding: 11px 13px;
  border: 1px solid #e2e7ef;
  border-radius: 12px;
  background: #f8fafc;
}

.search-box input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
}

.error-box {
  margin: 0 18px 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fff0f2;
  color: #a21834;
  font-size: 13px;
}

.session-list {
  min-height: 0;
  flex: 1;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding: 0 10px 14px;
}

.session-item {
  width: 100%;
  display: flex;
  gap: 12px;
  padding: 14px 11px;
  border: 0;
  border-radius: 14px;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.session-item:hover,
.session-item.active {
  background: #f5f0f2;
}

.avatar,
.sender-avatar {
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: linear-gradient(135deg, #86152e, #b84059);
  color: #fff;
  font-weight: 800;
}

.avatar.large {
  width: 50px;
  height: 50px;
}

.session-main {
  min-width: 0;
  flex: 1;
}

.session-topline,
.session-preview-row,
.session-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.session-topline time,
.session-meta small,
.message-time {
  color: #8892a4;
  font-size: 11px;
}

.session-preview {
  min-width: 0;
  overflow: hidden;
  color: #657087;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  min-width: 22px;
  height: 22px;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: #8c1731;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
}

.session-meta {
  justify-content: flex-start;
  margin-top: 7px;
}

.status-pill {
  padding: 3px 7px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
}

.status-0 { background: #fff4d8; color: #8b5a00; }
.status-1 { background: #e8f7ee; color: #167744; }
.status-2 { background: #edf0f4; color: #657087; }

.conversation-panel {
  min-width: 0;
  min-height: 0;
  height: 100%;
  display: flex;
  overflow: hidden;
  flex-direction: column;
  background: #fff;
}

.customer-info {
  display: flex;
  align-items: center;
  gap: 13px;
}

.customer-info p {
  margin: 4px 0 0;
  color: #778197;
  font-size: 13px;
}

.header-actions {
  display: flex;
  gap: 9px;
}

.claim-button,
.close-session-button {
  padding: 10px 14px;
  border-radius: 10px;
  font-weight: 700;
}

.claim-button {
  background: #8c1731;
  color: #fff;
}

.close-session-button {
  background: #fff0f2;
  color: #9d1f3a;
}

.message-list {
  min-height: 0;
  flex: 1 1 auto;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding: 22px 6%;
  background: #f8fafc;
}

.handoff-note {
  max-width: 740px;
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 auto 22px;
  padding: 11px 14px;
  border: 1px solid #e3d8dc;
  border-radius: 12px;
  background: #fff;
  color: #6f4b57;
  font-size: 13px;
}

.date-divider {
  margin: 20px 0 14px;
  color: #929bad;
  font-size: 12px;
  text-align: center;
}

.message-row {
  display: flex;
  align-items: flex-end;
  gap: 9px;
  margin: 8px 0;
}

.message-row.me {
  justify-content: flex-end;
}

.sender-avatar {
  width: 32px;
  height: 32px;
  font-size: 11px;
}

.sender-avatar.ai {
  background: linear-gradient(135deg, #3858a6, #6280cf);
}

.bubble-wrap {
  max-width: min(72%, 720px);
}

.sender-name {
  display: block;
  margin: 0 0 4px 3px;
  color: #68738a;
}

.message-bubble {
  padding: 11px 14px;
  border: 1px solid #e3e7ed;
  border-radius: 16px 16px 16px 4px;
  background: #fff;
  line-height: 1.5;
  white-space: pre-wrap;
}

.message-row.ai .message-bubble {
  border-color: #dbe3f6;
  background: #f0f4ff;
}

.message-row.me .message-bubble {
  border-color: #8c1731;
  border-radius: 16px 16px 4px 16px;
  background: #8c1731;
  color: #fff;
}

.message-time {
  margin-top: 4px;
  text-align: right;
}

.composer {
  position: relative;
  z-index: 2;
  display: flex;
  flex: 0 0 auto;
  align-items: flex-end;
  gap: 12px;
  padding: 16px 22px;
  border-top: 1px solid #e8ebf0;
  background: #fff;
}

.composer textarea {
  flex: 1;
  min-height: 46px;
  max-height: 120px;
  resize: none;
  border: 1px solid #dce2ea;
  border-radius: 14px;
  outline: 0;
  padding: 12px 14px;
  font: inherit;
}

.composer textarea:focus {
  border-color: #a5455a;
  box-shadow: 0 0 0 3px rgba(140, 23, 49, 0.09);
}

.send-button {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: #8c1731;
  color: #fff;
}

button:disabled,
textarea:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.empty-conversation,
.empty-list,
.empty-messages,
.loading-text {
  display: grid;
  place-items: center;
  color: #7f899b;
  text-align: center;
}

.empty-conversation {
  padding: 40px;
}

.empty-conversation i,
.empty-list i {
  font-size: 48px;
  color: #c5cbd5;
}

@media (max-width: 900px) {
  .chat-page {
    grid-template-columns: 310px minmax(0, 1fr);
  }

  .message-list {
    padding-inline: 20px;
  }
}
</style>
