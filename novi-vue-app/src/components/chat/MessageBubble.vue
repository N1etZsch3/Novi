<template>
  <div class="message-row" :class="[role, { 'animate-in': shouldAnimate }]" ref="messageRef">
    <!-- 头像 -->
    <div :class="['avatar', role === 'user' ? 'avatar-user' : 'avatar-ai']">
      <i :class="role === 'user' ? 'bi bi-person-fill' : 'bi bi-stars'"></i>
    </div>
    
    <!-- Assistant Message Wrapper (Stacks Reasoning & Content) -->
    <div v-if="role === 'assistant'" class="message-content-wrapper">
      <!-- 深度思考过程 (Reasoning) -->
      <div v-if="reasoning" class="message-bubble assistant reasoning-bubble">
        <div class="reasoning-header" @click="toggleReasoning">
          <i class="bi bi-lightbulb-fill me-2 text-warning"></i>
          <span>深度思考过程</span>
          <i class="bi bi-chevron-down ms-auto" :class="{ 'rotate-180': isReasoningExpanded }"></i>
        </div>
        <div v-show="isReasoningExpanded" class="reasoning-content markdown-body" v-html="renderedReasoning"></div>
      </div>

      <!-- 消息气泡 -->
      <div 
        v-if="content || typewriterContent"
        class="message-bubble assistant markdown-body"
        :id="bubbleId"
        v-html="renderedContent"
      ></div>
      
      <!-- Loading State (Only if no content and no reasoning yet) -->
      <div v-else-if="!reasoning" class="message-bubble assistant loading-bubble">
        <div class="typing-indicator">
          <span></span><span></span><span></span>
        </div>
      </div>
    </div>

    <!-- User Message Bubble -->
    <div 
      v-else
      class="message-bubble user markdown-body"
      :id="bubbleId"
      v-html="renderedContent"
    ></div>

  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useMarkdown } from '@/composables/useMarkdown'

const props = defineProps({
  role: {
    type: String,
    required: true, // 'user' or 'assistant'
    validator: (value) => ['user', 'assistant'].includes(value)
  },
  content: {
    type: String,
    default: ''
  },
  reasoning: {
    type: String,
    default: ''
  },
  bubbleId: {
    type: String,
    default: ''
  },
  isTyping: {
    type: Boolean,
    default: false
  },
  typewriterContent: {
    type: String,
    default: ''
  }
})

const { render } = useMarkdown()

const messageRef = ref(null)
const shouldAnimate = ref(true)
const isReasoningExpanded = ref(true)

function toggleReasoning() {
  isReasoningExpanded.value = !isReasoningExpanded.value
}

// 组件挂载后移除动画类，避免重复播放
onMounted(() => {
  setTimeout(() => {
    shouldAnimate.value = false
  }, 400)
})

const renderedReasoning = computed(() => {
  if (!props.reasoning) return ''
  return render(props.reasoning)
})

const renderedContent = computed(() => {
  // 打字状态下使用 typewriterContent，否则使用 content
  const text = props.isTyping ? props.typewriterContent : props.content
  if (!text) return ''
  return render(text)
})
</script>

<style scoped>
/* Copied from novi-v5.html */
.message-row {
  display: flex;
  margin-bottom: 1.5rem;
  align-items: flex-start;
}

.message-row.user {
  justify-content: flex-end;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 1.2rem;
  overflow: hidden;
  transition: none; /* Reset transition */
}

/* Reset hover effect from previous iteration */
.avatar:hover {
  transform: none;
}

.message-row.assistant .avatar {
  margin-right: 12px;
}

.message-row.user .avatar {
  margin-left: 12px;
  order: 2;
}

.avatar-ai {
  background: #fff;
  color: var(--primary-color);
}

.avatar-user {
  background: var(--primary-color);
  color: #fff;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 75%;
}

.message-bubble {
  max-width: 75%; /* Default for user bubbles */
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 1rem;
  line-height: 1.6;
  word-wrap: break-word; /* Ensure wrapping */
  position: relative;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);

  /* Adaptive Width */
  width: fit-content;
  min-width: 0;
}

/* Override max-width for bubbles inside wrapper since wrapper handles it */
.message-content-wrapper .message-bubble {
  max-width: 100%;
}

.message-bubble.assistant {
  background: #ffffff;
  color: #1f1f1f;
}

[data-bs-theme="dark"] .message-bubble.assistant {
  background: #2c2c2c;
  color: #e3e3e3;
}

.message-bubble.user {
  background: #95ec69; /* WeChat Green */
  color: #000000 !important;
  order: 1;
}

[data-bs-theme="dark"] .message-bubble.user {
  background: #2b7c38;
  color: #e3e3e3 !important;
}

.message-bubble.user :deep(*) {
  color: inherit !important;
  margin-bottom: 0;
}

/* Reasoning Bubble Styles */
.reasoning-bubble {
  background-color: #f8f9fa; /* Light gray */
  border-left: 4px solid #ffc107; /* Warning color */
  margin-bottom: 8px;
  box-shadow: 0 1px 1px rgba(0,0,0,0.05);
}

[data-bs-theme="dark"] .reasoning-bubble {
  background-color: #383838;
  border-left-color: #ffc107;
}

.reasoning-header {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding-bottom: 0;
  font-weight: 600;
  font-size: 0.9rem;
  color: #666;
  user-select: none;
}

[data-bs-theme="dark"] .reasoning-header {
  color: #aaa;
}

.reasoning-content {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid rgba(0,0,0,0.05);
  font-family: monospace;
  font-size: 0.9rem;
  color: #555;
  white-space: pre-wrap;
}

[data-bs-theme="dark"] .reasoning-content {
  border-top-color: rgba(255,255,255,0.1);
  color: #ccc;
}

.rotate-180 {
  transform: rotate(180deg);
  transition: transform 0.2s;
}


/* Loading Animation */
.loading-bubble {
  padding: 12px 16px;
  min-width: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.typing-indicator {
  display: flex;
  gap: 4px;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background-color: var(--text-sub);
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

@media (max-width: 768px) {
  /* novi-v5.html doesn't have specific mobile media query for bubble width but let's keep it safe */
}

/* 消息入场动画 - 更丝滑 */
.message-row.animate-in.user {
  animation: slideFromRight 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.message-row.animate-in.assistant {
  animation: slideFromLeft 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

@keyframes slideFromRight {
  0% {
    opacity: 0;
    transform: translateX(30px) scale(0.98);
  }
  100% {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}

@keyframes slideFromLeft {
  0% {
    opacity: 0;
    transform: translateX(-30px) scale(0.98);
  }
  100% {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}
</style>
