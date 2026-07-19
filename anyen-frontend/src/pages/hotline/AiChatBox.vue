<script setup>
import { nextTick, ref } from "vue";
import { guiTinNhanAi } from "../../services/aiService.js";

const isOpen = ref(false);
const inputMessage = ref("");
const loading = ref(false);
const messageContainer = ref(null);

const messages = ref([
  {
    role: "assistant",
    content:
        "Xin chào, tôi là trợ lý tư vấn của An Yên. " +
        "Tôi có thể hỗ trợ bạn tìm hiểu sản phẩm, dịch vụ và chính sách."
  }
]);

const toggleChat = async () => {
  isOpen.value = !isOpen.value;

  if (isOpen.value) {
    await scrollToBottom();
  }
};

const scrollToBottom = async () => {
  await nextTick();

  if (messageContainer.value) {
    messageContainer.value.scrollTop =
        messageContainer.value.scrollHeight;
  }
};

const sendMessage = async () => {
  const content = inputMessage.value.trim();

  if (!content || loading.value) {
    return;
  }

  messages.value.push({
    role: "user",
    content
  });

  inputMessage.value = "";
  loading.value = true;

  await scrollToBottom();

  try {
    const answer = await guiTinNhanAi(content);

    messages.value.push({
      role: "assistant",
      content: answer
    });
  } catch (error) {
    messages.value.push({
      role: "assistant",
      content:
          error.message ||
          "Trợ lý đang gặp lỗi. Vui lòng thử lại sau."
    });
  } finally {
    loading.value = false;
    await scrollToBottom();
  }
};

const handleKeydown = (event) => {
  if (
      event.key === "Enter" &&
      !event.shiftKey
  ) {
    event.preventDefault();
    sendMessage();
  }
};
</script>

<template>
  <div class="ai-chat-wrapper">
    <Transition name="chat-window">
      <section
          v-if="isOpen"
          class="ai-chat-box"
      >
        <header class="chat-header">
          <div class="assistant-info">
            <div class="assistant-avatar">
              <i class="fa-solid fa-seedling"></i>
            </div>

            <div class="assistant-detail">
              <h3>Trợ lý An Yên</h3>

              <span class="assistant-status">
                                <span class="status-dot"></span>
                                Đang hoạt động
                            </span>
            </div>
          </div>

          <button
              type="button"
              class="close-button"
              aria-label="Đóng khung chat"
              @click="isOpen = false"
          >
            <i class="fa-solid fa-xmark"></i>
          </button>
        </header>

        <div
            ref="messageContainer"
            class="chat-messages"
        >
          <div
              v-for="(item, index) in messages"
              :key="index"
              :class="[
                            'message-row',
                            item.role === 'user'
                                ? 'message-user'
                                : 'message-assistant'
                        ]"
          >
            <div
                v-if="item.role === 'assistant'"
                class="message-avatar"
            >
              <i class="fa-solid fa-seedling"></i>
            </div>

            <div class="message-bubble">
              {{ item.content }}
            </div>
          </div>

          <div
              v-if="loading"
              class="message-row message-assistant"
          >
            <div class="message-avatar">
              <i class="fa-solid fa-seedling"></i>
            </div>

            <div class="message-bubble typing-bubble">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>

        <form
            class="chat-input-area"
            @submit.prevent="sendMessage"
        >
                    <textarea
                        v-model="inputMessage"
                        rows="1"
                        maxlength="1000"
                        placeholder="Nhập câu hỏi của bạn..."
                        :disabled="loading"
                        @keydown="handleKeydown"
                    ></textarea>

          <button
              type="submit"
              class="send-button"
              :disabled="
                            loading ||
                            !inputMessage.trim()
                        "
              aria-label="Gửi tin nhắn"
          >
            <i class="fa-solid fa-paper-plane"></i>
          </button>
        </form>

        <p class="chat-warning">
          Trợ lý AI có thể trả lời chưa chính xác.
          Vui lòng xác nhận với nhân viên An Yên.
        </p>
      </section>
    </Transition>

    <button
        type="button"
        class="chat-toggle-button"
        :class="{ active: isOpen }"
        aria-label="Mở trợ lý AI"
        @click="toggleChat"
    >
      <i
          :class="
                    isOpen
                        ? 'fa-solid fa-xmark'
                        : 'fa-solid fa-comments'
                "
      ></i>
    </button>
  </div>
</template>

<style scoped>
.ai-chat-wrapper {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 5000;
}

.chat-toggle-button {
  width: 62px;
  height: 62px;
  display: flex;
  align-items: center;
  justify-content: center;

  border: none;
  border-radius: 50%;

  background:
      linear-gradient(
          135deg,
          #7b0d20,
          #a91d36
      );

  color: #ffffff;
  font-size: 22px;
  cursor: pointer;

  box-shadow:
      0 12px 30px
      rgba(123, 13, 32, 0.32);

  transition:
      transform 0.25s ease,
      box-shadow 0.25s ease;
}

.chat-toggle-button:hover {
  transform: translateY(-3px);

  box-shadow:
      0 16px 34px
      rgba(123, 13, 32, 0.38);
}

.chat-toggle-button.active {
  transform: rotate(90deg);
}

.ai-chat-box {
  position: absolute;
  right: 0;
  bottom: 78px;

  width: 390px;
  height: 570px;

  display: flex;
  flex-direction: column;

  overflow: hidden;

  border:
      1px solid
      rgba(139, 16, 36, 0.12);

  border-radius: 22px;
  background: #ffffff;

  box-shadow:
      0 22px 60px
      rgba(25, 15, 17, 0.22);
}

.chat-header {
  min-height: 78px;
  padding: 15px 18px;

  display: flex;
  align-items: center;
  justify-content: space-between;

  background:
      linear-gradient(
          135deg,
          #74101f,
          #9c1830
      );

  color: #ffffff;
}

.assistant-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.assistant-avatar {
  width: 45px;
  height: 45px;

  display: flex;
  align-items: center;
  justify-content: center;

  border-radius: 50%;

  background:
      rgba(255, 255, 255, 0.16);

  font-size: 19px;
}

.assistant-detail h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
}

.assistant-status {
  margin-top: 4px;

  display: flex;
  align-items: center;
  gap: 6px;

  font-size: 12px;
  opacity: 0.86;
}

.status-dot {
  width: 7px;
  height: 7px;

  border-radius: 50%;
  background: #8ee59d;
}

.close-button {
  width: 35px;
  height: 35px;

  display: flex;
  align-items: center;
  justify-content: center;

  border: none;
  border-radius: 50%;

  background:
      rgba(255, 255, 255, 0.1);

  color: #ffffff;
  font-size: 18px;
  cursor: pointer;
}

.close-button:hover {
  background:
      rgba(255, 255, 255, 0.2);
}

.chat-messages {
  flex: 1;
  padding: 18px 14px;

  overflow-y: auto;

  background:
      radial-gradient(
          circle at top right,
          rgba(139, 16, 36, 0.06),
          transparent 38%
      ),
      #f8f5f3;
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-thumb {
  border-radius: 10px;
  background: #d2c3c5;
}

.message-row {
  width: 100%;

  display: flex;
  align-items: flex-end;
  gap: 8px;

  margin-bottom: 14px;
}

.message-user {
  justify-content: flex-end;
}

.message-assistant {
  justify-content: flex-start;
}

.message-avatar {
  width: 30px;
  height: 30px;

  flex-shrink: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  border-radius: 50%;

  background: #8b1024;
  color: #ffffff;
  font-size: 12px;
}

.message-bubble {
  max-width: 79%;

  padding: 11px 14px;

  border-radius: 16px;

  font-size: 14px;
  line-height: 1.55;

  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.message-assistant .message-bubble {
  border-bottom-left-radius: 5px;

  background: #ffffff;
  color: #333333;

  box-shadow:
      0 3px 12px
      rgba(0, 0, 0, 0.06);
}

.message-user .message-bubble {
  border-bottom-right-radius: 5px;

  background: #8b1024;
  color: #ffffff;
}

.chat-input-area {
  padding: 12px;

  display: flex;
  align-items: flex-end;
  gap: 9px;

  border-top: 1px solid #eeeeee;
  background: #ffffff;
}

.chat-input-area textarea {
  flex: 1;

  min-height: 43px;
  max-height: 100px;

  padding: 10px 14px;

  resize: none;

  border: 1px solid #dddddd;
  border-radius: 21px;

  background: #fafafa;
  color: #333333;

  font-family: inherit;
  font-size: 14px;
  line-height: 1.45;

  outline: none;
}

.chat-input-area textarea:focus {
  border-color: #8b1024;
  background: #ffffff;
}

.chat-input-area textarea:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.send-button {
  width: 43px;
  height: 43px;

  flex-shrink: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  border: none;
  border-radius: 50%;

  background: #8b1024;
  color: #ffffff;

  cursor: pointer;

  transition:
      transform 0.2s ease,
      opacity 0.2s ease;
}

.send-button:hover:not(:disabled) {
  transform: scale(1.05);
}

.send-button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.chat-warning {
  margin: 0;
  padding: 0 14px 11px;

  background: #ffffff;
  color: #888888;

  font-size: 10px;
  line-height: 1.4;
  text-align: center;
}

.typing-bubble {
  min-width: 58px;

  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.typing-bubble span {
  width: 6px;
  height: 6px;

  border-radius: 50%;
  background: #999999;

  animation: typing 1.2s infinite;
}

.typing-bubble span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing-bubble span:nth-child(3) {
  animation-delay: 0.3s;
}

.chat-window-enter-active,
.chat-window-leave-active {
  transition:
      opacity 0.22s ease,
      transform 0.22s ease;
}

.chat-window-enter-from,
.chat-window-leave-to {
  opacity: 0;
  transform:
      translateY(12px)
      scale(0.97);
}

@keyframes typing {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.45;
  }

  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

@media (max-width: 520px) {
  .ai-chat-wrapper {
    right: 14px;
    bottom: 14px;
  }

  .chat-toggle-button {
    width: 56px;
    height: 56px;
  }

  .ai-chat-box {
    position: fixed;
    inset: 12px;

    width: auto;
    height: auto;

    border-radius: 18px;
  }
}
</style>