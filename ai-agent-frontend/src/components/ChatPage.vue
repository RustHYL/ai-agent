<template>
  <div class="chat-page">
    <header class="chat-header">
      <div>
        <button class="back-button" type="button" @click="goHome">
          返回主页
        </button>
        <h1>{{ title }}</h1>
        <p>{{ description }}</p>
      </div>

      <div class="session-panel">
        <span class="session-label">{{ sessionLabel }}</span>
        <code class="session-id">{{ sessionId }}</code>
        <button class="session-button" type="button" @click="resetSession">
          新建会话
        </button>
      </div>
    </header>

    <main ref="messageContainerRef" class="message-list">
      <div v-if="messages.length === 0" class="empty-state">
        <h2>开始新的对话</h2>
        <p>输入你的问题后，AI 会通过 SSE 实时返回内容，并在同一个消息气泡中逐字拼接。</p>
      </div>

      <div
        v-for="message in messages"
        :key="message.id"
        class="message-row"
        :class="message.role"
      >
        <div class="message-avatar" :class="message.role">
          <img :src="getAvatar(message)" :alt="getAvatarAlt(message)" />
        </div>

        <div class="message-body">
          <div class="message-role">
            {{
              message.role === 'user'
                ? '我'
                : message.variant === 'thinking'
                  ? 'AI 思考过程'
                  : aiName
            }}
          </div>

          <div class="message-bubble" :class="message.variant ? `message-bubble--${message.variant}` : ''">
            <template v-if="message.variant === 'thinking'">
              <div
                v-for="(step, index) in message.steps"
                :key="step.id"
                class="thinking-step"
                :class="{ 'thinking-step--separated': index > 0 }"
              >
                <div class="thinking-step-title">{{ step.title }}</div>
                <div class="message-content">{{ step.content }}</div>
              </div>
            </template>

            <div v-else class="message-content">{{ message.content }}</div>
          </div>
        </div>
      </div>
    </main>

    <footer class="chat-input-panel">
      <textarea
        v-model="inputValue"
        class="chat-textarea"
        rows="4"
        placeholder="请输入内容，按 Enter 发送，Shift + Enter 换行"
        :disabled="isStreaming"
        @keydown="handleTextareaKeydown"
      />

      <div class="chat-actions">
        <span class="stream-status" :class="{ active: isStreaming }">
          {{ isStreaming ? 'AI 正在输入中...' : '准备就绪' }}
        </span>
        <button
          class="send-button"
          type="button"
          :disabled="isStreaming || !trimmedInput"
          @click="sendMessage"
        >
          发送
        </button>
      </div>
    </footer>

    <SiteFooter />
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, reactive, ref, watch, watchEffect } from 'vue';
import { useRouter } from 'vue-router';
import { createChatId, createMessage } from '../utils/chat';
import SiteFooter from './SiteFooter.vue';

const props = defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    required: true,
  },
  streamChat: {
    type: Function,
    required: true,
  },
  sessionLabel: {
    type: String,
    default: '聊天室 ID',
  },
  responseMode: {
    type: String,
    default: 'default',
  },
  aiName: {
    type: String,
    default: 'AI',
  },
  aiAvatar: {
    type: String,
    default: '',
  },
  userAvatar: {
    type: String,
    default: '',
  },
});

const router = useRouter();
const inputValue = ref('');
const isStreaming = ref(false);
const messages = ref([]);
const sessionId = ref(createChatId());
const messageContainerRef = ref(null);

watchEffect(() => {
  if (props.title) {
    document.title = `${props.title} | AI Agent 前端应用`;
  }
});

watchEffect(() => {
  if (props.title) {
    document.title = `${props.title} | AI Agent 前端应用`;
  }
});
let activeStream = null;

const trimmedInput = computed(() => inputValue.value.trim());

const DEFAULT_AI_AVATAR =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#2563eb"/><stop offset="1" stop-color="#7c3aed"/></linearGradient></defs><rect width="64" height="64" rx="32" fill="url(#g)"/><text x="50%" y="54%" text-anchor="middle" font-size="30" font-family="Arial" fill="#fff" dominant-baseline="middle">AI</text></svg>`,
  );

const DEFAULT_USER_AVATAR =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64"><rect width="64" height="64" rx="32" fill="#334155"/><text x="50%" y="54%" text-anchor="middle" font-size="28" font-family="Arial" fill="#e2e8f0" dominant-baseline="middle">我</text></svg>`,
  );

function getAvatar(message) {
  if (message.role === 'user') {
    return props.userAvatar || DEFAULT_USER_AVATAR;
  }
  return props.aiAvatar || DEFAULT_AI_AVATAR;
}

function getAvatarAlt(message) {
  return message.role === 'user' ? '用户头像' : `${props.aiName}头像`;
}

function scrollToBottom() {
  nextTick(() => {
    if (messageContainerRef.value) {
      messageContainerRef.value.scrollTop = messageContainerRef.value.scrollHeight;
    }
  });
}

function closeActiveStream() {
  if (activeStream) {
    activeStream.close();
    activeStream = null;
  }
  isStreaming.value = false;
}

function goHome() {
  router.push('/');
}

function resetSession() {
  closeActiveStream();
  sessionId.value = createChatId();
  messages.value = [];
  inputValue.value = '';
}

function handleTextareaKeydown(event) {
  if (event.key !== 'Enter' || event.shiftKey || event.isComposing) {
    return;
  }

  event.preventDefault();
  sendMessage();
}

function ensureFinalMessage(finalMessageRef) {
  if (finalMessageRef.value) {
    return finalMessageRef.value;
  }

  const finalMessage = reactive(createMessage('assistant', '', { variant: 'final' }));
  finalMessageRef.value = finalMessage;
  messages.value.push(finalMessage);
  return finalMessage;
}

function appendDefaultAssistantChunk(assistantMessage, chunk) {
  assistantMessage.content += chunk;
  nextTick(() => {
    scrollToBottom();
  });
}

function appendSelfManusChunk(thinkingMessage, finalMessageRef, chunk) {
  const text = chunk.trim();
  if (!text) {
    return;
  }

  const stepMatch = text.match(/^Step\s+(\d+):\s*([\s\S]*)$/);
  if (stepMatch) {
    thinkingMessage.steps.push({
      id: `step_${stepMatch[1]}_${Math.random().toString(36).slice(2, 8)}`,
      title: `步骤 ${stepMatch[1]}`,
      content: stepMatch[2] || '处理中...',
    });
    return;
  }

  const finalMessage = ensureFinalMessage(finalMessageRef);
  finalMessage.content += chunk;
}

function sendMessage() {
  if (!trimmedInput.value || isStreaming.value) {
    return;
  }

  const userText = trimmedInput.value;
  const userMessage = reactive(createMessage('user', userText));
  const isSelfManusMode = props.responseMode === 'self-manus';
  const assistantMessage = reactive(isSelfManusMode
    ? createMessage('assistant', '', { variant: 'thinking', steps: [] })
    : createMessage('assistant', ''));
  const finalMessageRef = ref(null);

  messages.value.push(userMessage, assistantMessage);
  inputValue.value = '';
  isStreaming.value = true;
  scrollToBottom();

  let finished = false;
  const finishStream = () => {
    if (finished) {
      return;
    }
    finished = true;
    isStreaming.value = false;
    activeStream = null;
    scrollToBottom();
  };

  activeStream = props.streamChat({
    message: userText,
    chatId: sessionId.value,
    onChunk(chunk) {
      if (isSelfManusMode) {
        appendSelfManusChunk(assistantMessage, finalMessageRef, chunk);
      } else {
        appendDefaultAssistantChunk(assistantMessage, chunk);
      }
      scrollToBottom();
    },
    onComplete() {
      finishStream();
    },
    onError(error) {
      const errorText = error?.message || '当前会话已结束，未接收到有效响应。请重试。';
      if (isSelfManusMode) {
        ensureFinalMessage(finalMessageRef).content =
          finalMessageRef.value?.content || errorText;
      } else if (!assistantMessage.content) {
        assistantMessage.content = errorText;
      }
      finishStream();
    },
  });
}

watch(
  messages,
  () => {
    scrollToBottom();
  },
  { deep: true },
);

onBeforeUnmount(() => {
  closeActiveStream();
});
</script>
