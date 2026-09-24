<template>
  <div class="home-page">
    <!-- 背景网格 -->
    <div class="grid-bg" aria-hidden="true"></div>

    <!-- Hero 终端窗口 -->
    <section class="hero-terminal">
      <div class="terminal-bar">
        <span class="terminal-dot dot-red"></span>
        <span class="terminal-dot dot-yellow"></span>
        <span class="terminal-dot dot-green"></span>
        <span class="terminal-title">ai-agent@frontend: ~</span>
      </div>
      <div class="terminal-body">
        <p class="terminal-line">
          <span class="prompt">$</span>
          <span class="cmd">run --apps</span>
        </p>
        <p class="terminal-line muted">
          › 正在加载 AI 应用矩阵...
          <span class="cursor">▋</span>
        </p>
        <h1 class="hero-title">
          <span class="hero-title-line">AI · AGENT</span>
          <span class="hero-title-line gradient-text">FRONTEND</span>
        </h1>
        <p class="hero-text">
          基于 Vue3 + Axios + SSE 构建的流式 AI 对话应用矩阵。<br />
          实时打字机效果 · 多智能体协作 · 响应式全端适配。
        </p>
        <div class="hero-stats">
          <div class="stat">
            <span class="stat-num">2</span>
            <span class="stat-label">AI 应用</span>
          </div>
          <div class="stat">
            <span class="stat-num">SSE</span>
            <span class="stat-label">流式协议</span>
          </div>
          <div class="stat">
            <span class="stat-num">∞</span>
            <span class="stat-label">对话轮次</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 应用卡片 -->
    <section class="app-grid">
      <RouterLink class="app-card app-card--love" to="/love">
        <div class="app-card-glow"></div>
        <div class="app-card-head">
          <div class="app-icon app-icon--love">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
            </svg>
          </div>
          <div>
            <span class="app-tag">// APP_01</span>
            <h2>AI 恋爱大师</h2>
          </div>
        </div>
        <p class="app-desc">情感分析 · 恋爱建议 · 回复润色</p>
        <div class="app-card-foot">
          <span class="app-status">
            <span class="status-dot"></span>
            online
          </span>
          <span class="app-enter">
            launch
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M5 12h14M12 5l7 7-7 7" />
            </svg>
          </span>
        </div>
      </RouterLink>

      <RouterLink class="app-card app-card--agent" to="/super-agent">
        <div class="app-card-glow"></div>
        <div class="app-card-head">
          <div class="app-icon app-icon--agent">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="10" rx="2" />
              <circle cx="12" cy="5" r="2" />
              <path d="M12 7v4M8 16h.01M16 16h.01" />
            </svg>
          </div>
          <div>
            <span class="app-tag">// APP_02</span>
            <h2>AI 超级智能体</h2>
          </div>
        </div>
        <p class="app-desc">任务拆解 · 工具调用 · 自主推理</p>
        <div class="app-card-foot">
          <span class="app-status">
            <span class="status-dot"></span>
            online
          </span>
          <span class="app-enter">
            launch
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M5 12h14M12 5l7 7-7 7" />
            </svg>
          </span>
        </div>
      </RouterLink>
    </section>

    <SiteFooter />
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import SiteFooter from '../components/SiteFooter.vue';

onMounted(() => {
  document.title = 'AI Agent 前端应用 | AI 恋爱大师 & AI 超级智能体 SSE 流式对话';
});
</script>
