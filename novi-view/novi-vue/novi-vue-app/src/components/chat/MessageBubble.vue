<template>
  <div class="message-row" :class="role">
    <!-- 头像 -->
    <div :class="['avatar', role === 'user' ? 'avatar-user' : 'avatar-ai']">
      <i :class="role === 'user' ? 'bi bi-person-fill' : 'bi bi-stars'"></i>
    </div>
    
    <!-- 消息气泡 -->
    <div 
      v-if="content"
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
import { computed } from 'vue'
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
  }
})

const { render } = useMarkdown()

const renderedContent = computed(() => {
  if (!props.content) return ''
  return render(props.content)
})
</script>

<style scoped>
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
  max-width: 80%;
  padding: 12px 18px;
  border-radius: 16px;
  font-size: 1rem;
  line-height: 1.6;
  word-wrap: break-word;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  position: relative;
  transition: all 0.2s ease;
}

.message-bubble.assistant {
  background: var(--bg-card);
  color: var(--text-main);
  border-top-left-radius: 4px;
  border: 1px solid var(--border-color);
}

.message-bubble.user {
  background: linear-gradient(135deg, #4e6ef2, #2b55e8);
  color: #fff !important;
  order: 1;
  border-top-right-radius: 4px;
  box-shadow: 0 4px 12px rgba(78, 110, 242, 0.3);
}

.message-bubble.user :deep(*) {
  color: inherit !important;
  margin-bottom: 0;
}

[data-bs-theme="dark"] .message-bubble.assistant {
  background: #2b2d31;
  border-color: #3f4148;
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
  opacity: 0.6;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>
