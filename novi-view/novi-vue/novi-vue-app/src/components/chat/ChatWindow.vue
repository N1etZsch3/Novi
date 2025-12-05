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
  }
})

const chatWindowRef = ref(null)

// 自动滚动到底部
watch(() => props.messages.length, async () => {
  await nextTick()
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
  padding: 1.5rem 1rem;
  scroll-behavior: smooth;
}

.chat-container {
  max-width: 850px;
  margin: 0 auto;
  width: 100%;
  padding-bottom: 2rem;
}

.avatar-ai {
  background: var(--bg-card);
  color: var(--primary-color);
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}
</style>
