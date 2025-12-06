<template>
  <div class="message-row" :class="[role, { 'animate-in': shouldAnimate }]" ref="messageRef">
    <!-- 头像 -->
    <div :class="['avatar', role === 'user' ? 'avatar-user' : 'avatar-ai']">
      <i :class="role === 'user' ? 'bi bi-person-fill' : 'bi bi-stars'"></i>
    </div>
    
    <!-- 消息气泡 -->
    <div 
      v-if="content || typewriterContent"
      :class="['message-bubble', role, 'markdown-body']"
      :id="bubbleId"
      v-html="renderedContent"
    ></div>
    <!-- Loading State -->
    <div v-else-if="role === 'assistant'" class="message-bubble assistant loading-bubble">
      <div class="typing-indicator">
        <span></span><span></span><span></span>
      </div>
    </div>
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

// 组件挂载后移除动画类，避免重复播放
onMounted(() => {
  setTimeout(() => {
    shouldAnimate.value = false
  }, 400)
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

.message-bubble {
  max-width: 75%;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 1rem;
  line-height: 1.6;
  word-wrap: break-word; /* Ensure wrapping */
  position: relative;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);

  /* Adaptive Width (Required for Vue implementation to match V5 behavior) */
  width: fit-content;
  min-width: 0;
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
