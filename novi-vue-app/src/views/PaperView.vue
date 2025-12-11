<template>
  <div :class="['chat-view-container', { 'sidebar-collapsed': isSidebarCollapsed }]">
    <!-- Sidebar -->
    <Sidebar current-mode="paper" :collapsed="isSidebarCollapsed" :mobile-open="isSidebarOpen"
      @toggle-sidebar="toggleSidebar" @switch-mode="switchMode">
      <PaperHistoryList ref="historyListRef" :history="history" :selected-id="currentRecordId" @select="loadRecord"
        :collapsed="isSidebarCollapsed" @delete="deleteRecord" @batch-delete="deleteBatchRecords" @new-paper="resetPaper" />
    </Sidebar>

    <!-- Mobile Overlay -->
    <div class="sidebar-overlay" v-if="isMobile && isSidebarOpen" @click="isSidebarOpen = false"></div>

    <!-- Main Content -->
    <main class="chat-main">
      <!-- Header -->
      <header class="chat-header d-flex align-items-center justify-content-between px-4 py-3 border-bottom"
        style="background: var(--bg-header);">
        <div class="d-flex align-items-center">
          <button class="btn btn-link p-0 me-3 d-md-none" style="color: var(--text-main);"
            @click="isSidebarOpen = !isSidebarOpen">
            <i class="bi bi-list fs-4"></i>
          </button>
          <h6 class="mb-0 fw-bold" style="color: var(--text-main);">AI 套卷生成助手</h6>
        </div>
        <div class="d-flex align-items-center gap-3">
          <div class="form-check form-switch mb-0" title="深色模式">
            <input class="form-check-input theme-switcher" type="checkbox" id="themeSwitchPaper"
              :checked="isDarkMode" @change="toggleTheme">
            <label class="form-check-label" for="themeSwitchPaper">
              <i class="bi bi-moon-stars" style="color: var(--text-main);"></i>
            </label>
          </div>
          <button class="btn btn-outline-primary btn-sm rounded-pill px-3" @click="resetPaper">
            <i class="bi bi-arrow-clockwise me-1"></i> 重置
          </button>
        </div>
      </header>

      <div class="exam-container">
        <!-- Left: Config Panel -->
        <PaperConfigPanel
          v-model="config"
          :models="models"
          :subjects="subjects"
          :loading="loading"
          @subject-change="handleSubjectChange"
          @switch-model="handleSwitchModel"
          @generate="generate"
        />

        <!-- Right: Preview Panel -->
        <PaperPreviewPanel
          :data="paperData"
          :loading="loading"
          :streamData="streamData"
        />
      </div>
    </main>

    <!-- Confirm Modal -->
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
import { ref, onMounted, computed, nextTick } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { useRouter } from 'vue-router'
import Sidebar from '@/components/common/Sidebar.vue'
import PaperHistoryList from '@/components/paper/PaperHistoryList.vue'
import PaperConfigPanel from '@/components/paper/PaperConfigPanel.vue'
import PaperPreviewPanel from '@/components/paper/PaperPreviewPanel.vue'
import ConfirmModal from '@/components/common/ConfirmModal.vue'

import { getSubjects } from '@/api/exam'
import { getModelList, switchModel as switchModelApi } from '@/api/model'
import { getProfile } from '@/api/auth'
import { getPaperHistory, getPaperDetail, deletePaper, deletePapers, generatePaperStream } from '@/api/paper'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()
const { success, error } = useToast()

// State
const isSidebarCollapsed = ref(false)
const isSidebarOpen = ref(false)
const isMobile = ref(window.innerWidth <= 768)
const isDarkMode = computed(() => themeStore.currentTheme === 'dark')

const models = ref([])
const subjects = ref([])
const history = ref([])

const loading = ref(false)
const paperData = ref(null) // { details: [], recordId: ... }
const currentRecordId = ref(null)
const streamData = ref([])

const config = ref({
  subject: null,
  difficulty: 'medium',
  model: null,
  enableThinking: false
})

const historyListRef = ref(null)

// Modal State
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmType = ref('primary')
let pendingConfirmAction = null

// Init
onMounted(async () => {
  window.addEventListener('resize', handleResize)
  handleResize()

  await loadUserProfile()
  await loadModels()
  await loadSubjects()
  await loadHistory()
})

function handleResize() {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) isSidebarOpen.value = false
}

async function loadUserProfile() {
  try {
    const res = await getProfile()
    if ((res.code === 1 || res.code === 200) && res.data) {
      userStore.setUserInfo(res.data)
    }
  } catch (e) { console.error('Load Profile Error', e) }
}

async function loadModels() {
  try {
    const res = await getModelList()
    if ((res.code === 1 || res.code === 200) && res.data) {
      models.value = res.data
      const savedModel = localStorage.getItem('novi_paper_model')
      if (savedModel && models.value.find(m => m.modelName === savedModel)) {
        config.value.model = savedModel
      } else if (models.value.length > 0) {
        const active = models.value.find(m => m.isActive)
        config.value.model = active ? active.modelName : models.value[0].modelName
      }
    }
  } catch (e) { error('加载模型列表失败') }
}

async function loadSubjects() {
  try {
    const res = await getSubjects()
    if ((res.code === 1 || res.code === 200) && res.data) {
      subjects.value = res.data
      if (subjects.value.length > 0) {
        // Prefer ID over name if available, assuming API expects ID
        config.value.subject = subjects.value[0].id || subjects.value[0].name
      }
    }
  } catch (e) { error('加载科目失败') }
}

function handleSubjectChange(val) {
    config.value.subject = val
}

async function handleSwitchModel(modelName) {
    try {
        await switchModelApi(modelName)
        localStorage.setItem('novi_paper_model', modelName)
        success('模型切换成功')
    } catch (e) { error('切换模型失败') }
}

async function generate() {
    if (!config.value.subject) return error('请先选择科目')
    
    loading.value = true
    paperData.value = null // Clear history view
    currentRecordId.value = null
    streamData.value = [] // Clear stream
    
    const requestPayload = {
        subjectId: config.value.subject,
        paperConfig: null, // Auto mode
        model: config.value.model,
        enableThinking: config.value.enableThinking
    }

    // Call SSE API
    await generatePaperStream(
        requestPayload, 
        (data) => {
            // onMessage (event: question)
            streamData.value.push(data)
        },
        (err) => {
            // onError
            console.error('SSE Error', err)
            error('生成过程中发生错误: ' + (err.message || 'Unknown'))
            loading.value = false
        },
        (completeData) => {
            // onComplete
            loading.value = false
            success('试卷生成完成！')
            loadHistory() // Refresh sidebar
        }
    )
}

async function loadHistory() {
    try {
        const res = await getPaperHistory()
        if (res.code === 1 || res.code === 200) {
            history.value = res.data
        }
    } catch (e) { console.error('Load History Failed', e) }
}

async function loadRecord(id) {
    try {
        const res = await getPaperDetail(id)
        if (res.code === 200 || res.code === 1) {
             paperData.value = res.data
             currentRecordId.value = res.data.id
             if (isMobile.value) isSidebarOpen.value = false
        }
    } catch (e) { error('加载记录失败') }
}

function openConfirmModal(title, message, type = 'primary', action) {
    confirmTitle.value = title
    confirmMessage.value = message
    confirmType.value = type
    pendingConfirmAction = action
    showConfirmModal.value = true
}

function handleConfirmAction() {
    const action = pendingConfirmAction
    pendingConfirmAction = null
    // Defer action execution to next tick to avoid recursive updates
    if (action) {
        nextTick(() => {
            action()
        })
    }
}

async function deleteRecord(id) {
    openConfirmModal('删除记录', '确认删除这条套卷记录吗？此操作无法撤销。', 'danger', async () => {
        try {
            await deletePaper(id)
            success('删除成功')
            if (currentRecordId.value === id) resetPaper()
            await loadHistory()
        } catch (e) { error('删除失败') }
    })
}

async function deleteBatchRecords(ids) {
    openConfirmModal('批量删除', `确认删除这 ${ids.length} 条记录吗？`, 'danger', async () => {
        try {
            await deletePapers(ids)
            success(`成功删除 ${ids.length} 条记录`)
            if (ids.includes(currentRecordId.value)) {
                resetPaper()
            }
            await loadHistory()
            // Reset edit mode
            if (historyListRef.value) historyListRef.value.resetEditMode()
        } catch (e) { error('批量删除失败') }
    })
}

function resetPaper() {
    paperData.value = null
    currentRecordId.value = null
    streamData.value = []
}

function toggleTheme() {
    themeStore.toggleTheme()
}

function toggleSidebar() {
  if (isMobile.value) {
    isSidebarOpen.value = !isSidebarOpen.value
  } else {
    isSidebarCollapsed.value = !isSidebarCollapsed.value
  }
}

function switchMode(mode) {
  if (mode === 'chat') router.push('/chat')
  if (mode === 'exam') router.push('/exam')
}
</script>

<style scoped>
.chat-view-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

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

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1030;
  backdrop-filter: blur(2px);
}

.exam-container {
    display: flex;
    height: 100%;
    width: 100%;
    overflow: hidden;
}

@media (max-width: 992px) {
    .exam-container {
        flex-direction: column;
        overflow-y: auto;
    }
}
</style>
