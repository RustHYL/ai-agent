<template>
  <ChatPage
    title="AI 恋爱大师"
    description="进入页面后会自动生成聊天室 ID，并通过 SSE 调用后端恋爱大师接口，实时展示 AI 回复。"
    :stream-chat="doChatWithLoveAppSse"
    session-label="聊天室 ID"
    ai-name="恋爱大师"
    :ai-avatar="loveAvatar"
  />
</template>

<script setup>
import ChatPage from '../components/ChatPage.vue';
import { doChatWithLoveAppSse } from '../services/aiApi';

const loveAvatar =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#ec4899"/><stop offset="1" stop-color="#f43f5e"/></linearGradient></defs><rect width="64" height="64" rx="32" fill="url(#g)"/><path d="M32 44s-12-7.5-12-16a6 6 0 0 1 12-2 6 6 0 0 1 12 2c0 8.5-12 16-12 16z" fill="#fff"/></svg>`,
  );
</script>
