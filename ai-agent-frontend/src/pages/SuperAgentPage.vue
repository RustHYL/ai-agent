<template>
  <ChatPage
    title="AI 超级智能体"
    description="使用与聊天室一致的交互形式，通过 SSE 调用超级智能体接口，实时拼接 AI 回复内容。"
    :stream-chat="doChatWithSelfManus"
    session-label="本地会话 ID"
    response-mode="self-manus"
    ai-name="超级智能体"
    :ai-avatar="agentAvatar"
  />
</template>

<script setup>
import ChatPage from '../components/ChatPage.vue';
import { doChatWithSelfManus } from '../services/aiApi';

const agentAvatar =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#0ea5e9"/><stop offset="1" stop-color="#6366f1"/></linearGradient></defs><rect width="64" height="64" rx="32" fill="url(#g)"/><rect x="18" y="22" width="28" height="20" rx="6" fill="#fff"/><circle cx="26" cy="32" r="3" fill="#0f172a"/><circle cx="38" cy="32" r="3" fill="#0f172a"/><path d="M32 18v-4M24 16l-3-3M40 16l3-3" stroke="#fff" stroke-width="2" stroke-linecap="round"/></svg>`,
  );
</script>
