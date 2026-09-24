<template>
  <footer class="site-footer">
    <div class="site-footer-inner">
      <p class="site-footer-copy">
        © {{ year }} {{ siteName }} · {{ slogan }}
      </p>
      <p class="site-footer-links">
        <a href="https://www.codefather.cn" target="_blank" rel="noopener noreferrer">编程导航</a>
        <span class="site-footer-dot">·</span>
        <a href="https://github.com/RustHYL" target="_blank" rel="noopener noreferrer">GitHub</a>
        <span class="site-footer-dot">·</span>
        <span>京 ICP 备 2026000000 号</span>
      </p>
      <p class="site-footer-desc">
        本站致力于提供 AI 应用一站式体验，包含 AI 恋爱大师与 AI 超级智能体等流式对话应用。
      </p>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue';

defineProps({
  siteName: {
    type: String,
    default: 'AI Agent 前端应用',
  },
  slogan: {
    type: String,
    default: '让 AI 触手可及',
  },
});

const year = computed(() => new Date().getFullYear());
</script>
