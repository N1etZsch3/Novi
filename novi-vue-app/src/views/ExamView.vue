<template>
  <div :class="['chat-view-container', { 'sidebar-collapsed': isSidebarCollapsed }]">
    <!-- Sidebar -->
    <Sidebar current-mode="exam" :collapsed="isSidebarCollapsed" :mobile-open="isSidebarOpen"
      @toggle-sidebar="toggleSidebar" @switch-mode="switchMode">
      <ExamHistoryList ref="historyListRef" :history="history" :selected-id="currentRecordId" @select="loadRecord"
        :collapsed="isSidebarCollapsed" @delete="deleteRecord" @batch-delete="deleteBatchRecords" @new-question="resetPaper" />
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
          <h6 class="mb-0 fw-bold" style="color: var(--text-main);">AI 智能出题助手</h6>
        </div>
        <div class="d-flex align-items-center gap-3">
          <div class="form-check form-switch mb-0" title="深色模式">
            <input class="form-check-input theme-switcher" type="checkbox" id="themeSwitchExam"
              :checked="isDarkMode" @change="toggleTheme">
            <label class="form-check-label" for="themeSwitchExam">
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
        <ExamConfigPanel
          v-model="config"
          :models="models"
          :subjects="subjects"
          :types="types"
          :loading="loading"
          @subject-change="handleSubjectChange"
          @type-change="handleTypeChange"
          @switch-model="handleSwitchModel"
          @generate="generate"
        />

        <!-- Right: Preview Panel -->
        <ExamPreviewPanel
          :data="paperData"
          :loading="loading"
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
import ExamHistoryList from '@/components/exam/ExamHistoryList.vue'
import ExamConfigPanel from '@/components/exam/ExamConfigPanel.vue'
import ExamPreviewPanel from '@/components/exam/ExamPreviewPanel.vue'
import ConfirmModal from '@/components/common/ConfirmModal.vue'

import { getSubjects, getQuestionTypes, generatePaper, getGenerationHistory, deleteHistory, deleteHistories, getHistoryRecord } from '@/api/exam'
import { getModelList, switchModel as switchModelApi } from '@/api/model'
import { getProfile } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'


const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()
const { success, error, info } = useToast()

// State
const isSidebarCollapsed = ref(false)
const isSidebarOpen = ref(false)
const isMobile = ref(window.innerWidth <= 768)
const isDarkMode = computed(() => themeStore.currentTheme === 'dark')

const models = ref([])
const subjects = ref([])
const types = ref([])
const history = ref([])

const loading = ref(false)
const paperData = ref(null) // { questions: [], recordId: ... }
const currentRecordId = ref(null)

const config = ref({
  subject: null,
  questionType: null,
  topic: '',
  difficulty: 'medium',
  quantity: 1,
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
  await loadSubjects() // Will also load types for first subject if available
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
      
      // Load saved model or default
      const savedModel = localStorage.getItem('novi_exam_model')
      if (savedModel && models.value.find(m => m.modelName === savedModel)) {
        config.value.model = savedModel
      } else if (models.value.length > 0) {
        // Find active model or first
        const active = models.value.find(m => m.isActive)
        config.value.model = active ? active.modelName : models.value[0].modelName
      }
      
      // Init config enableThinking based on model
      if (config.value.model) {
        const m = models.value.find(m => m.modelName === config.value.model)
        // Only set default if user hasn't toggled (optional, but adhering to novi-v5 logic isn't strict here, v5 logic was intricate)
        // novi-v5 just enabled container flex/none.
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
        const first = subjects.value[0]
        config.value.subject = first.name
        await loadTypes(first.id)
      }
    }
  } catch (e) { error('加载科目失败') }
}

async function loadTypes(subjectId) {
  try {
    const res = await getQuestionTypes(subjectId)
    if ((res.code === 1 || res.code === 200) && res.data) {
      types.value = res.data
      if (types.value.length > 0) {
        config.value.questionType = types.value[0].name
      } else {
          config.value.questionType = null
      }
    }
  } catch (e) { error('加载题型失败') }
}

function handleSubjectChange(id) {
    loadTypes(id)
}

function handleTypeChange(val) {
    config.value.questionType = val
}

async function handleSwitchModel(modelName) {
    try {
        await switchModelApi(modelName) // Assuming endpoint supports this
        localStorage.setItem('novi_exam_model', modelName)
        // Not showing toast as per novi-v5 silent logic used in dropdown sometimes, 
        // but explicit switch usually shows toast. v5 'examModel' change calls switchModel with silent depending on context.
        // We can show success here.
        success('模型切换成功')
    } catch (e) { error('切换模型失败') }
}

async function generate() {
    loading.value = true
    try {
        // v5 payload: { subject, questionType, theme, difficulty, quantity: int, model, enableThinking }
        const payload = {
            subject: config.value.subject,
            questionType: config.value.questionType,
            theme: config.value.topic,
            difficulty: config.value.difficulty,
            quantity: config.value.quantity,
            model: config.value.model,
            enableThinking: config.value.enableThinking
        }
        
        const res = await generatePaper(payload)
        if (res.code === 1 || res.code === 200) {
            const result = res.data
            let questions = []
            try {
                if (!result.questions) {
                    console.warn("Result questions is null/empty")
                } else {
                    let parsed = (typeof result.questions === 'string') ? JSON.parse(result.questions) : result.questions
                    
                    // Smart extraction: Handle { questions: [...] } or { items: [...] } wrappers
                    if (!Array.isArray(parsed) && parsed) {
                        if (Array.isArray(parsed.questions)) {
                            parsed = parsed.questions
                        } else if (Array.isArray(parsed.items)) {
                            parsed = parsed.items
                        } else if (Array.isArray(parsed.data)) {
                             parsed = parsed.data
                        } else {
                            // Fallback: treat as single item
                            parsed = [parsed]
                        }
                    }
                    
                    const arr = Array.isArray(parsed) ? parsed : [parsed]
                    // Filter out null/undefined items
                    questions = arr.filter(item => item !== null && item !== undefined && (typeof item === 'object'))
                }
            } catch (e) {
                console.error("JSON Parse Error", e)
                // Don't throw, just show empty or error state
                error("题目数据解析失败，请查看控制台")
            }
            
            // If no questions found
            if (questions.length === 0) {
                 console.warn("No valid questions found after parsing")
                 // Add a fallback dummy question so user sees something instead of blank
                 questions.push({
                     title: "解析结果为空",
                     stem: "未能从AI响应中提取到有效题目，请重试或检查日志。"
                 })
            }

            // Deep clone to break any potential reference issues
            const detailsStruct = [{
                id: result.recordId,
                order: 1,
                questionType: result.questionType,
                questionTypeName: result.questionType,
                questions: questions
            }]
            
            console.log('Generating Paper Data questions:', questions)

            paperData.value = { 
                paperName: `${result.subject || '试卷'} - ${result.theme || '自动生成'}`,
                subjectName: result.subject || '未命名科目',
                totalQuestions: result.quantity || questions.length,
                // ExamPreviewPanel expects 'questions' directly, not 'details'
                questions: JSON.parse(JSON.stringify(questions)),
                recordId: result.recordId
            }
            currentRecordId.value = result.recordId
            success('试卷生成成功！')
            await loadHistory()
        } else {
            error(res.msg || '生成失败')
        }
    } catch (e) {
        console.error("Generate Error", e)
        error(e.message || '生成失败')
    } finally {
        loading.value = false
    }
}

async function loadHistory() {
    try {
        const res = await getGenerationHistory()
        if (res.code === 1 || res.code === 200) {
            history.value = res.data
        }
    } catch (e) { console.error('Load History Failed', e) }
}

async function loadRecord(id) {
    if (historyListRef.value && historyListRef.value.isEditMode) return // Should not happen as item click handles this
    
    try {
        const res = await getHistoryRecord(id)
        if (res.code === 200 || res.code === 1) {
             const result = res.data
             let questions = []
             try {
                 let parsed = (typeof result.questions === 'string') ? JSON.parse(result.questions) : result.questions
                 
                  // Smart extraction: Handle { questions: [...] } or { items: [...] } wrappers
                    if (!Array.isArray(parsed) && parsed) {
                        if (Array.isArray(parsed.questions)) {
                            parsed = parsed.questions
                        } else if (Array.isArray(parsed.items)) {
                            parsed = parsed.items
                        } else if (Array.isArray(parsed.data)) {
                             parsed = parsed.data
                        } else {
                            parsed = [parsed]
                        }
                    }
                 
                 questions = Array.isArray(parsed) ? parsed : [parsed]
                 questions = questions.filter(item => item !== null && item !== undefined && (typeof item === 'object'))
             } catch (e) { questions = [] }
             
             if (questions.length === 0) {
                 questions.push({
                     title: "解析结果为空",
                     stem: "未能从AI响应中提取到有效题目。"
                 })
             }
             
             paperData.value = { 
                paperName: `${result.subject || '试卷'} - ${result.theme || '自动生成'}`,
                subjectName: result.subject || '未命名科目',
                totalQuestions: result.quantity || questions.length,
                // ExamPreviewPanel expects 'questions' directly
                questions: JSON.parse(JSON.stringify(questions)),
                recordId: result.recordId
             }
             currentRecordId.value = result.recordId
             
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
    openConfirmModal('删除记录', '确认删除这条出题记录吗？此操作无法撤销。', 'danger', async () => {
        try {
            await deleteHistory(id)
            success('删除成功')
            if (currentRecordId.value === id) {
                resetPaper()
            }
            await loadHistory()
        } catch (e) { error('删除失败') }
    })
}

async function deleteBatchRecords(ids) {
    openConfirmModal('批量删除', `确认删除这 ${ids.length} 条记录吗？`, 'danger', async () => {
        try {
            await deleteHistories(ids)
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
  else if (mode === 'paper') router.push('/paper')
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
