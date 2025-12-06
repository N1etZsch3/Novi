<template>
  <div class="chat-window" id="chatWindow" ref="chatWindowRef">
    <div class="chat-container">
      <!-- 空状态 -->
      <div v-if="messages.length === 0" class="text-center mt-5 pt-5">
        <div class="avatar avatar-ai mx-auto mb-3" style="width: 64px; height: 64px; font-size: 2rem; border: none; background: transparent;">
          <i class="bi bi-stars text-primary"></i>
        </div>
        <h3 class="fw-bold" style="color: var(--text-main);">你好，我是 Novi</h3>
        <p style="color: var(--text-sub);">我可以帮你写代码、做计划，或者仅仅聊聊天。</p>
      </div>
      
      <!-- 消息列表 -->
      <MessageBubble
        v-for="(message, index) in messages"
        :key="message.id || index"
        :role="message.role"
        :content="message.content"
        :bubble-id="message.id"
        :is-typing="message.id === typingMessageId"
        :typewriter-content="message.id === typingMessageId ? typewriterContent : ''"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import MessageBubble from './MessageBubble.vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  typingMessageId: {
    type: String,
    default: null
  },
  typewriterContent: {
    type: String,
    default: ''
  },
  // 是否在加载时自动滚动到底部（加载历史记录时为 false）
  autoScrollOnLoad: {
    type: Boolean,
    default: true
  }
})

const chatWindowRef = ref(null)

// 消息数量变化时根据条件滚动到底部
watch(() => props.messages.length, async (newLen, oldLen) => {
  await nextTick()
  // 只在新增消息时滚动（newLen > oldLen），或者明确要求自动滚动时
  if (props.autoScrollOnLoad || (oldLen !== undefined && newLen > oldLen)) {
    scrollToBottom()
  }
})

// 打字内容变化时滚动到底部
watch(() => props.typewriterContent, () => {
  scrollToBottom()
})

function scrollToBottom() {
  if (chatWindowRef.value) {
    chatWindowRef.value.scrollTop = chatWindowRef.value.scrollHeight
  }
}

defineExpose({
  scrollToBottom
})
</script>

<style scoped>
.chat-window {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 1.5rem 1rem;
  /* 不使用 smooth scroll，避免与消息动画冲突 */
}

.chat-container {
  max-width: 850px; /* Aligned exactly with ChatInput.vue */
  margin: 0 auto;
  width: 100%;
  padding-bottom: 2rem;
  /* 防止动画时产生额外的渲染区域 */
  overflow: hidden;
}

.avatar-ai {
  background: #ffffff;
  color: var(--primary-color);
  width: 40px;
  height: 40px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
  box-shadow: none; /* simple */
}
</style>
