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
        <p v-if="responseMode === 'self-manus'">
          发送问题后，智能体会展示可收起的执行过程，并在下方给出最终交付。
        </p>
        <p v-else>输入你的问题后，AI 会通过 SSE 实时返回内容，并在同一个消息气泡中逐字拼接。</p>
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

        <div class="message-body" :class="{ 'message-body--agent': message.variant === 'agent' }">
          <div class="message-role">
            {{
              message.role === 'user'
                ? '我'
                : message.variant === 'thinking'
                  ? '执行过程'
                  : message.variant === 'final'
                    ? '最终交付'
                    : aiName
            }}
          </div>

          <div class="message-bubble" :class="message.variant ? `message-bubble--${message.variant}` : ''">
            <template v-if="message.variant === 'agent'">
              <section v-if="message.groups.length || message.pending" class="agent-process">
                <header class="agent-process-header">
                  <span class="agent-process-title">执行过程</span>
                  <button
                    class="agent-process-toggle"
                    type="button"
                    :aria-expanded="message.processOpen"
                    @click="toggleProcess(message, $event)"
                  >
                    {{ message.processOpen ? '收起' : '展开' }}
                  </button>
                </header>
                <div v-show="message.processOpen" class="agent-process-body">
                  <p v-if="!message.groups.length" class="agent-pending">正在分析任务...</p>
                  <article
                    v-for="group in message.groups"
                    :key="group.step"
                    class="agent-step"
                  >
                    <h3 class="agent-step-title">步骤 {{ group.step }}</h3>
                    <div
                      v-for="item in group.items"
                      :key="item.id"
                      class="agent-trace"
                      :class="[
                        item.type ? `agent-trace--${item.type.toLowerCase()}` : '',
                        { 'agent-trace--collapsible': item.collapsible },
                      ]"
                    >
                      <button
                        v-if="item.collapsible"
                        class="agent-trace-toggle"
                        type="button"
                        :aria-expanded="item.expanded"
                        @click="toggleTrace(item, $event)"
                      >
                        <span class="agent-tag">{{ item.label }}</span>
                        <span class="agent-chevron" :class="{ open: item.expanded }">></span>
                      </button>
                      <span v-else class="agent-tag">{{ item.label }}</span>
                      <p v-show="!item.collapsible || item.expanded" class="agent-trace-content">{{ item.content }}</p>
                    </div>
                  </article>
                </div>
              </section>
              <section v-if="message.answer" class="agent-answer">
                <h3 class="agent-answer-title">最终交付</h3>
                <div class="message-content">{{ message.answer }}</div>
              </section>
            </template>

            <template v-else-if="message.variant === 'thinking'">
              <div v-if="!message.steps.length" class="message-content">正在分析任务...</div>
              <div
                v-for="(step, index) in message.steps"
                :key="step.id"
                class="thinking-step"
                :class="[
                  step.type ? `thinking-step--${step.type.toLowerCase()}` : '',
                  { 'thinking-step--separated': index > 0 },
                ]"
              >
                <button
                  v-if="step.collapsible"
                  class="thinking-step-toggle"
                  type="button"
                  :aria-expanded="step.expanded"
                  @click="toggleTrace(step, $event)"
                >
                  <span class="thinking-step-title">{{ step.title }}</span>
                  <span class="agent-chevron" :class="{ open: step.expanded }">></span>
                </button>
                <div v-else class="thinking-step-title">{{ step.title }}</div>
                <div v-show="!step.collapsible || step.expanded" class="message-content">{{ step.content }}</div>
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
import { computed, nextTick, onBeforeUnmount, reactive, ref, watchEffect } from 'vue';
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

function appendDefaultAssistantChunk(assistantMessage, chunk) {
  assistantMessage.content += chunk;
  nextTick(() => {
    scrollToBottom();
  });
}

const AGENT_OUTPUT_TITLES = {
  THOUGHT: '思考',
  ACTION: '行动',
  OBSERVATION: '观察',
  ANSWER: '最终交付',
};

function ensureStepGroup(agentMessage, step) {
  const stepNo = step || agentMessage.groups.length + 1;
  let group = agentMessage.groups.find((item) => item.step === stepNo);
  if (!group) {
    group = { step: stepNo, items: [] };
    agentMessage.groups.push(group);
  }
  return group;
}

function appendAgentItem(agentMessage, step, type, label, content) {
  if (!content) {
    return;
  }
  const group = ensureStepGroup(agentMessage, step);
  const collapsible = type === 'ACTION' || type === 'OBSERVATION';
  group.items.push({
    id: `trace_${step || group.step}_${type || 'text'}_${Math.random().toString(36).slice(2, 8)}`,
    type,
    label,
    content,
    collapsible,
    expanded: false,
  });
}

function appendAnswer(agentMessage, text) {
  if (!text) {
    return;
  }
  agentMessage.answer = agentMessage.answer ? `${agentMessage.answer}\n${text}` : text;
}

function appendSelfManusChunk(agentMessage, chunk) {
  if (chunk && typeof chunk === 'object' && chunk.type) {
    const label = chunk.label || AGENT_OUTPUT_TITLES[chunk.type] || chunk.type;
    if (chunk.type === 'ANSWER') {
      appendAnswer(agentMessage, chunk.content || '');
      return;
    }
    appendAgentItem(agentMessage, chunk.step, chunk.type, label, chunk.content || '');
    return;
  }

  const text = String(chunk || '').trim();
  if (!text) {
    return;
  }

  const stepMatch = text.match(/^Step\s+(\d+):\s*([\s\S]*)$/);
  if (stepMatch) {
    appendAgentItem(agentMessage, Number(stepMatch[1]), 'THOUGHT', '步骤记录', stepMatch[2] || '处理中...');
    return;
  }

  appendAnswer(agentMessage, text);
}

function toggleProcess(message, event) {
  event?.stopPropagation();
  message.processOpen = !message.processOpen;
}

function toggleTrace(item, event) {
  event?.preventDefault();
  event?.stopPropagation();
  item.expanded = !item.expanded;
}

function sendMessage() {
  if (!trimmedInput.value || isStreaming.value) {
    return;
  }

  const userText = trimmedInput.value;
  const userMessage = reactive(createMessage('user', userText));
  const isSelfManusMode = props.responseMode === 'self-manus';
  const assistantMessage = reactive(isSelfManusMode
    ? createMessage('assistant', '', {
      variant: 'agent',
      processOpen: true,
      pending: true,
      groups: [],
      answer: '',
    })
    : createMessage('assistant', ''));

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
    if (isSelfManusMode) {
      assistantMessage.pending = false;
      if (!assistantMessage.groups.length && !assistantMessage.answer) {
        const index = messages.value.indexOf(assistantMessage);
        if (index >= 0) {
          messages.value.splice(index, 1);
        }
      }
    }
    isStreaming.value = false;
    activeStream = null;
    scrollToBottom();
  };

  activeStream = props.streamChat({
    message: userText,
    chatId: sessionId.value,
    onChunk(chunk) {
      if (isSelfManusMode) {
        appendSelfManusChunk(assistantMessage, chunk);
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
        assistantMessage.answer = assistantMessage.answer || errorText;
      } else if (!assistantMessage.content) {
        assistantMessage.content = errorText;
      }
      finishStream();
    },
  });
}

onBeforeUnmount(() => {
  closeActiveStream();
});
</script>
