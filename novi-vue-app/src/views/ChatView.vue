<template>
  <div :class="['chat-view-container', { 'sidebar-collapsed': isSidebarCollapsed }]">
    <!-- 侧边栏 -->
    <Sidebar 
      current-mode="chat" 
      :collapsed="isSidebarCollapsed" 
      :mobile-open="isSidebarOpen"
      @toggle-sidebar="toggleSidebar" 
      @switch-mode="switchMode"
    >
      <SessionList
        ref="sessionListRef"
        :sessions="sessions"
        :current-session-id="currentSessionId"
        :collapsed="isSidebarCollapsed"
        @new-chat="startNewChat"
        @select-session="selectSession"
        @delete-session="deleteSession"
        @delete-selected="deleteSelectedSessions"
      />
    </Sidebar>
    
    <!-- Mobile Overlay -->
    <div class="sidebar-overlay" v-if="isMobile && isSidebarOpen" @click="isSidebarOpen = false"></div>

    <!-- 主内容区 -->
    <main class="chat-main">
      <!-- 头部 -->
      <ChatHeader
        :title="currentTitle"
        v-model:api-mode="apiMode"
        @toggle-sidebar="toggleSidebar"
      />

      <!-- 聊天窗口 -->
      <ChatWindow 
        ref="chatWindowRef" 
        :messages="messages" 
        :typing-message-id="typingMessageId"
        :typewriter-content="typewriter.displayedContent.value"
        :auto-scroll-on-load="autoScrollOnLoad"
      />

      <!-- 输入区 -->
      <ChatInput
        ref="chatInputRef"
        :models="models"
        :selected-model="selectedModel"
        :disabled="isTyping"
        @send="sendMessage"
        @update:selectedModel="selectModel"
        @fullscreen="openFullScreen"
      />
    </main>

    <FullScreenInput
      v-model="isFullScreenOpen"
      :text="fullScreenText"
      @confirm="handleFullScreenConfirm"
    />
    
    <ConfirmModal
      v-model="showConfirmModal"
      :title="confirmTitle"
      :message="confirmMessage"
      :type="confirmType"
      @confirm="handleConfirmAction"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import { getSessions, getSessionHistory, sendMessage as sendMessageApi, sendMessageStream, deleteSession as deleteSessionApi } from '@/api/chat'
import { getModelList, getActiveModel, switchModel as switchModelApi } from '@/api/model'
import { getProfile } from '@/api/auth'
import { useTypewriter } from '@/composables/useTypewriter'
import { useToast } from '@/composables/useToast'

import Sidebar from '@/components/common/Sidebar.vue'
import ChatHeader from '@/components/chat/ChatHeader.vue'
import ChatWindow from '@/components/chat/ChatWindow.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import SessionList from '@/components/chat/SessionList.vue'
import FullScreenInput from '@/components/chat/FullScreenInput.vue'

const router = useRouter()
const chatStore = useChatStore()
const userStore = useUserStore()
const { success, error } = useToast()
const typewriter = useTypewriter()

// 状态
const sessions = ref([])
const currentSessionId = ref(null)
const messages = ref([])
const models = ref([])
const selectedModel = ref('')
const apiMode = ref('stream')
const isTyping = ref(false)
const typingMessageId = ref(null)
const autoScrollOnLoad = ref(true)

const chatWindowRef = ref(null)
const chatInputRef = ref(null)
const sessionListRef = ref(null)
const isSidebarCollapsed = ref(false)
const isMobile = ref(window.innerWidth <= 768)
const isSidebarOpen = ref(false)

// Listen for resize
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) {
    isSidebarOpen.value = false
  }
})

const currentTitle = computed(() => {
  const session = sessions.value.find(s => s.id === currentSessionId.value)
  return session?.title || 'Novi AI'
})

// 初始化
onMounted(async () => {
  await loadUserProfile()
  await loadModels()
  await loadSessions()
})

// 加载用户资料
async function loadUserProfile() {
  try {
    const res = await getProfile()
    if ((res.code === 1 || res.code === 200) && res.data) {
      userStore.setUserInfo(res.data)
    }
  } catch (err) {
    console.error('加载用户资料失败:', err)
  }
}

// 加载模型列表
async function loadModels() {
  try {
    const res = await getModelList()
    if ((res.code === 1 || res.code === 200) && res.data) {
      models.value = res.data.map(m => ({
        id: m.modelName,
        name: m.description || m.modelName,
        description: m.description
      }))
      
      // 获取当前激活的模型
      const activeRes = await getActiveModel()
      if ((activeRes.code === 1 || activeRes.code === 200) && activeRes.data) {
        selectedModel.value = activeRes.data.modelName
      } else if (models.value.length > 0) {
        selectedModel.value = models.value[0].id
      }
    }
  } catch (err) {
    console.error('加载模型失败:', err)
    // 如果加载失败，使用默认值
    if (models.value.length === 0) {
      models.value = [{ id: 'default', name: '默认模型', description: '' }]
      selectedModel.value = 'default'
    }
  }
}

// 加载会话列表
// 加载会话列表
async function loadSessions() {
  try {
    const res = await getSessions()
    if (res.code === 200 || res.code === 1) {
      sessions.value = (res.data || []).map(s => ({
        ...s,
        id: String(s.id)
      }))
    }
  } catch (err) {
    console.error('加载会话失败:', err)
  }
}
async function selectSession(sessionId) {
  try {
    currentSessionId.value = sessionId
    // 加载历史记录时不自动滚动到底部，让消息从左上角开始显示
    autoScrollOnLoad.value = false
    
    // 移动端自动关闭侧边栏
    if (isMobile.value) {
      isSidebarOpen.value = false
    }
    
    const res = await getSessionHistory(sessionId)
    if (res.code === 200 || res.code === 1) {
      messages.value = (res.data || []).map(m => ({
        ...m,
        id: String(m.id || Date.now()),
        role: m.role ? m.role.toLowerCase() : 'user'
      }))
    } else {
      error(res.msg || '加载会话失败')
    }
    // 加载完成后重置，以便新消息仍会滚动到底部
    autoScrollOnLoad.value = true
  } catch (err) {
    console.error(err)
    error('加载会话历史失败')
    autoScrollOnLoad.value = true
  }
}

// 新建会话
// 新建会话
function startNewChat() {
  if (!currentSessionId.value && messages.value.length === 0) {
    show('已经是最新对话', 'info')
    return
  }
  currentSessionId.value = null
  messages.value = []
}

// 发送消息
async function sendMessage(msg) {
  if (!msg || isTyping.value) return

  // 添加用户消息
  messages.value.push({
    id: 'user-' + Date.now(),
    role: 'user',
    content: msg
  })

  // 添加AI消息占位符
  const aiMessageId = 'ai-' + Date.now()
  messages.value.push({
    id: aiMessageId,
    role: 'assistant',
    content: '',
    reasoning: ''
  })

  isTyping.value = true
  typingMessageId.value = aiMessageId
  typewriter.start()

  try {
    const payload = {
      message: msg,
      sessionId: currentSessionId.value,
      model: selectedModel.value
    }

    if (apiMode.value === 'stream') {
      await handleStreamMessage(payload, aiMessageId)
    } else {
      await handleNormalMessage(payload, aiMessageId)
    }
  } catch (err) {
    error('发送消息失败: ' + (err.message || '未知错误'))
    // 移除失败的AI消息
    const index = messages.value.findIndex(m => m.id === aiMessageId)
    if (index > -1) {
      messages.value.splice(index, 1)
    }
  } finally {
    // 同步最终内容到消息
    const msg = messages.value.find(m => m.id === typingMessageId.value)
    if (msg) {
      msg.content = typewriter.getFullContent()
    }
    
    isTyping.value = false
    typingMessageId.value = null
    typewriter.stop()
    await loadSessions()
  }
}

// 处理流式消息
async function handleStreamMessage(payload, messageId) {
  const authStore = useAuthStore()
  const response = await sendMessageStream(payload, authStore.token)
  
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop()

    for (const line of lines) {
      if (!line.trim() || !line.startsWith('data:')) continue
      
      try {
        const evt = JSON.parse(line.slice(5))
        if (evt.eventType === 'CONTENT') {
          typewriter.push(evt.content)
        } else if (evt.eventType === 'REASONING') {
          // Find the current message and append reasoning
          // Note: Typewriter handles 'content'. Reasoning might need direct update or separate handling.
          // Since typewriter is for the main content, we might update the message directly for reasoning.
          const msg = messages.value.find(m => m.id === messageId)
          if (msg) {
            msg.reasoning = (msg.reasoning || '') + evt.content
          }
        } else if (evt.eventType === 'METADATA') {
          currentSessionId.value = evt.sessionId
        }
      } catch (e) {
        console.error('解析SSE事件失败:', e)
      }
    }
  }
  
  // Stream finished - content will be synced in finally block
}

// 处理普通消息
async function handleNormalMessage(payload, messageId) {
  const res = await sendMessageApi(payload)
  if (res.code === 200 || res.code === 1) {
    typewriter.push(res.data.response)
    // Content will be synced in finally block
    
    if (res.data.sessionId) {
      currentSessionId.value = res.data.sessionId
    }
  } else {
    throw new Error(res.msg || '发送失败')
  }
}

// 导入 useAuthStore
import { useAuthStore } from '@/stores/auth'
import ConfirmModal from '@/components/common/ConfirmModal.vue'

// Modal State
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmType = ref('primary')
let pendingConfirmAction = null

function openConfirmModal(title, message, type = 'primary', action) {
  confirmTitle.value = title
  confirmMessage.value = message
  confirmType.value = type
  pendingConfirmAction = action
  showConfirmModal.value = true
}

function handleConfirmAction() {
  if (pendingConfirmAction) pendingConfirmAction()
  pendingConfirmAction = null
}

// 删除会话
async function deleteSession(sessionId) {
  openConfirmModal('删除会话', '确认删除此会话？此操作无法撤销。', 'danger', async () => {
    try {
      await deleteSessionApi(sessionId)
      if (currentSessionId.value === sessionId) {
        startNewChat()
      }
      await loadSessions()
      success('删除成功')
    } catch (err) {
      error('删除失败')
    }
  })
}

// 批量删除会话
// 批量删除会话
async function deleteSelectedSessions(ids) {
  if (!ids || ids.length === 0) return
  
  openConfirmModal('批量删除', `确认删除这 ${ids.length} 条会话吗？`, 'danger', async () => {
    try {
      const promises = ids.map(id => deleteSessionApi(id))
      const results = await Promise.allSettled(promises)
      
      // Count success (fulfilled) AND 404s (already deleted) as success
      // If rejected, check reason. If 404, valid.
      let successCount = 0
      results.forEach(r => {
        if (r.status === 'fulfilled') successCount++
        else if (r.reason?.response?.status === 404) successCount++ // Treat 404 as success
      })
      
      await loadSessions()
      
      if (ids.includes(currentSessionId.value)) {
        startNewChat()
      }
      
      if (successCount > 0) {
        success(`已删除 ${successCount} 条会话`)
      } else {
        // If all failed with non-404, show error. But likely minimal chance.
        error('删除操作完成')
      }
      
      // Reset edit mode
      if (sessionListRef.value && sessionListRef.value.resetEditMode) {
        sessionListRef.value.resetEditMode()
      }
    } catch (err) {
      console.error('Batch delete logic error:', err)
      // Even on error, try to reload and reset
      await loadSessions()
      if (sessionListRef.value && sessionListRef.value.resetEditMode) {
        sessionListRef.value.resetEditMode()
      }
    }
  })
}

// 切换模型
async function selectModel(modelId) {
  if (selectedModel.value === modelId) return
  
  try {
    const res = await switchModelApi(modelId)
    if (res.code === 1 || res.code === 200) {
      selectedModel.value = modelId
      success('模型切换成功')
    } else {
      error(res.msg || '模型切换失败')
    }
  } catch (err) {
    console.error('切换模型失败:', err)
    error('切换模型失败')
  }
}

// 全屏输入
const fullScreenText = ref('')
const isFullScreenOpen = ref(false)

function openFullScreen() {
  fullScreenText.value = chatInputRef.value?.message || ''
  isFullScreenOpen.value = true
}

function handleFullScreenConfirm(text) {
  chatInputRef.value?.setMessage(text)
}

// 切换侧边栏
function toggleSidebar() {
  if (isMobile.value) {
    isSidebarOpen.value = !isSidebarOpen.value
  } else {
    isSidebarCollapsed.value = !isSidebarCollapsed.value
  }
}

// 切换模式
function switchMode(mode) {
  if (mode === 'exam') {
    router.push('/exam')
  } else if (mode === 'paper') {
    router.push('/paper')
  }
}
</script>

<style scoped>
.chat-view-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* 侧边栏折叠状态 */
.chat-view-container.sidebar-collapsed :deep(.sidebar) {
  width: var(--sidebar-width-collapsed);
}

.chat-view-container.sidebar-collapsed :deep(.text-label) {
  display: none;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-body);
  overflow: hidden;
}

/* Sidebar handled by Sidebar.vue */

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1030;
  backdrop-filter: blur(2px);
  transition: opacity 0.3s;
}
</style>
