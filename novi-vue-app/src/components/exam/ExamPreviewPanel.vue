<template>
  <div class="exam-preview-panel">
    <!-- Loading Overlay -->
    <div v-if="loading" id="paperLoading" class="loading-overlay">
      <div class="text-center">
        <div class="spinner-border text-primary mb-3" role="status"></div>
        <h5 class="fw-bold">AI 正在根据考纲出题中...</h5>
        <p class="text-muted small">分析考点 · 组织文本 · 生成解析</p>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="!paperData && !loading" id="paperEmpty"
      class="h-100 d-flex flex-column align-items-center justify-content-center text-center text-muted">
      <div>
        <i class="bi bi-file-earmark-text display-1 opacity-25"></i>
        <p class="mt-3">在左侧配置参数后点击生成</p>
      </div>
    </div>

    <!-- Paper Content -->
    <Transition name="paper-fade" mode="out-in">
      <div v-if="paperData && !loading" id="paperContent" :key="paperData?.recordId">
      <div class="d-flex justify-content-end mb-3 gap-2">
        <div class="form-check form-switch pt-2">
          <input class="form-check-input" type="checkbox" id="showAnswersSwitch" v-model="showAnswers">
          <label class="form-check-label" for="showAnswersSwitch">显示解析</label>
        </div>
        <button class="btn btn-success rounded-pill px-4" @click="exportWord">
          <i class="bi bi-file-word me-1"></i> 导出 Word
        </button>
      </div>

      <div class="paper-card" id="paperExportArea" ref="paperExportArea">
        <div class="paper-title" id="paperTitleText">{{ paperTitle }}</div>
        <!-- Subtitle container is hidden in v5 code, so we omit or hide it -->
        <div id="paperSubtitleContainer" class="paper-subtitle d-none"></div>
        
        <div class="text-center mb-4">
          <span class="exam-meta-tag" id="tagSubject">科目：{{ meta.subject || '英语' }}</span>
          <span class="exam-meta-tag" id="tagType">包含题型：{{ questions.length }} 大题</span>
          <span class="exam-meta-tag" id="tagDiff">难度：{{ meta.difficulty || '标准' }}</span>
        </div>

        <!-- Rendered Body -->
        <div id="paperBody" v-html="paperBodyHtml"></div>

        <!-- Answers Body -->
        <div id="paperAnswers" class="mt-5 pt-4 border-top" v-show="showAnswers" v-html="answersBodyHtml">
        </div>
      </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useToast } from '@/composables/useToast'

const props = defineProps({
  loading: { type: Boolean, default: false },
  data: { type: Object, default: null } // { questions: [], recordId: ... }
})

const { success } = useToast()

const paperData = computed(() => props.data)
const showAnswers = ref(false)

// Reset showAnswers when data changes
watch(() => props.data, () => {
  showAnswers.value = false
})

const questions = computed(() => paperData.value?.questions || [])
const meta = computed(() => questions.value[0] || {})
const paperTitle = computed(() => meta.value.title || 'AI 智能生成试卷')

// Global index logic needs to be handled carefully in a computed property
// We will generate the HTML strings for body and answers.
const renderedContent = computed(() => {
  if (!questions.value || questions.value.length === 0) return { body: '', answers: '' }

  let contentHtml = ''
  let answersContainerHtml = ''
  let globalQuestionIndex = 1

  questions.value.forEach((q, index) => {
    let partItems = [] // Items for this part (answers)
    let localAnswerIndex = 0

    let finalTitle = q.title || `Part ${index + 1} `
    let processedContent = ''

    if (q.content) {
      processedContent = q.content

      // 0. Extract Title
      if (q.title) {
        const contentTrimmed = processedContent.trim()
        const firstLine = contentTrimmed.split('\n')[0].trim()
        let isTitleMatch = false
        const partPrefixRegex = /^(Part\s+([IVX]+|\d+))/i
        const titlePartMatch = q.title.match(partPrefixRegex)

        if (titlePartMatch) {
          const contentPartMatch = firstLine.match(partPrefixRegex)
          if (contentPartMatch && contentPartMatch[1].toLowerCase() === titlePartMatch[1].toLowerCase()) {
            isTitleMatch = true
          }
        }

        if (!isTitleMatch) {
           const normalize = (str) => str.replace(/[^\w\u4e00-\u9fa5]/g, '').toLowerCase()
           const cleanTitle = normalize(q.title)
           const cleanLine = normalize(firstLine)
           if (cleanTitle.length > 2 && cleanLine.length > 2) {
               if (cleanLine.startsWith(cleanTitle) || cleanTitle.startsWith(cleanLine)) {
                   isTitleMatch = true
               }
           }
        }

        if (isTitleMatch) {
            finalTitle = firstLine
            processedContent = contentTrimmed.substring(firstLine.length).trim()
        }
      }

      // 1. Handle {{index}}
      if (processedContent.includes('{{index}}')) {
          processedContent = processedContent.replace(/{{index}}/g, () => {
              const currentNum = globalQuestionIndex++
              
              let ansStr = ''
              let anaStr = ''

              if (Array.isArray(q.answers)) {
                  ansStr = q.answers[localAnswerIndex] || ''
              } else if (q.answer) {
                  ansStr = typeof q.answer === 'string' ? q.answer : (q.answer.correct || '')
              }

              if (Array.isArray(q.analyses)) {
                  anaStr = q.analyses[localAnswerIndex] || ''
              } else if (q.analysis) {
                  anaStr = q.analysis
              }

              partItems.push({ num: currentNum, ans: ansStr, ana: anaStr })
              localAnswerIndex++
              return currentNum
          })
      } else if (Array.isArray(q.answers) && q.answers.length > 0) {
           q.answers.forEach((ans, i) => {
               const currentNum = globalQuestionIndex++
               partItems.push({
                   num: currentNum,
                   ans: ans.correct || ans,
                   ana: q.analyses ? q.analyses[i] : (q.analysis || '')
               })
           })
      }

      // 2. Format Placeholders
      processedContent = processedContent
        .replace(/{{subtitle:(.*?)}}/g, '<div class="text-center fw-bold my-3 fs-5">$1</div>')
        .replace(/{{br}}/g, '<br/>')
        .replace(/{{indent}}\s*/g, '<span style="display:inline-block; width: 2em;"></span>')
        .replace(/{{short_blank}}/g, '<span class="fw-bold mx-1">________</span>')
        .replace(/{{long_line}}/g, '<div class="my-2 text-muted">___________________________________________________________________</div>')

      processedContent = `<div class="question-content lh-lg text-break">${processedContent}</div>`
    } else {
        // Fallback logic
         switch (q.ui_type) {
            case 'cloze_passage':
            case 'reading_cloze':
                let stemHtml = q.stem || ''
                stemHtml = stemHtml.replace(/\[\[__(\d+)__\]\]/g, (match, id) => {
                    return `<span class="fw-bold mx-1 text-primary">[${id}]</span><span class="blank-space border-bottom border-dark d-inline-block text-center" style="min-width: 60px; height: 1.5em; vertical-align: bottom;"></span>`
                })
                processedContent = `<div class="question-content lh-lg">${stemHtml}</div>`
                if (Array.isArray(q.answers)) {
                    q.answers.forEach(a => partItems.push({ num: a.index, ans: a.correct, ana: a.analysis }))
                }
                break;
            case 'sort_sentence':
                const words = q.stem.split('/').map(w => `<span class="badge bg-light text-dark border me-1 mb-1 fw-normal fs-6">${w.trim()}</span>`).join(' ')
                processedContent = `<div class="card bg-light border-0 mb-3"><div class="card-body text-center"><div class="mb-3 d-flex flex-wrap justify-content-center">${words}</div></div></div><div class="mb-2"><strong>Your Answer:</strong></div><div class="border-bottom border-secondary mb-4" style="height: 30px;"></div>`
                if (q.answer) partItems.push({ num: index + 1, ans: q.answer.correct, ana: q.answer.analysis })
                break;
            default:
                let oldText = (q.article_content || '').replace(/\n/g, '<br>')
                processedContent = `<div class="question-content">${oldText}</div>`
                if (q.answer) partItems.push({ num: index + 1, ans: typeof q.answer === 'string' ? q.answer : q.answer.correct, ana: q.analysis })
                break;
        }
    }

    const scoreDesc = q.score_desc ? `<span class="ms-2 badge bg-secondary-subtle text-secondary-emphasis border fw-normal">${q.score_desc}</span>` : ''
    const instruction = q.instruction ? `<div class="text-muted small mb-3 border-start border-4 border-primary ps-2">${q.instruction}</div>` : ''

    contentHtml += `<div class="mb-5"><h4 class="fw-bold mb-3">${scoreDesc}</h4>${instruction}${processedContent}</div>`

    // Answers Table
    if (partItems.length > 0) {
         if (index > 0) answersContainerHtml += '<hr class="my-5">'
         answersContainerHtml += `<div class="mb-4"><h6 class="fw-bold text-secondary mb-3">${finalTitle} 参考答案</h6>`
         answersContainerHtml += `
            <div class="table-responsive">
                <table class="table table-bordered table-hover align-middle mb-0 mobile-optimized-table">
                    <thead class="table-light">
                        <tr>
                            <th style="width: 60px;" class="text-center">#</th>
                            <th style="width: 25%;" class="text-center">答案</th>
                            <th class="text-start ps-4">解析</th>
                        </tr>
                    </thead>
                    <tbody>`
        
         partItems.forEach(item => {
             answersContainerHtml += `
                 <tr>
                     <td class="text-center fw-bold text-secondary">${item.num}</td>
                     <td class="text-center text-primary fw-bold text-break">${item.ans}</td>
                     <td class="text-muted small text-break text-start ps-4" style="line-height: 1.6;">${item.ana || '暂无解析'}</td>
                 </tr>`
         })
         answersContainerHtml += `</tbody></table></div></div>`
    }
  })

  return { body: contentHtml, answers: answersContainerHtml }
})

const paperBodyHtml = computed(() => renderedContent.value.body)
const answersBodyHtml = computed(() => renderedContent.value.answers)

function exportWord() {
    const header = "<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'><head><meta charset='utf-8'><title>Export HTML to Word Document with JavaScript</title></head><body>"
    const footer = "</body></html>"
    const styles = `
    <style>
        @page {
            size: 21.0cm 29.7cm;
            margin: 2.54cm 2.54cm 2.54cm 2.54cm;
            mso-page-orientation: portrait;
        }
        body {
            font-family: "Times New Roman", "SimSun", serif;
            font-size: 12pt;
            line-height: 1.5;
        }
        .paper-title {
            text-align: center;
            font-size: 18pt;
            font-weight: bold;
            margin-bottom: 20px;
            font-family: "SimHei", sans-serif;
        }
        .exam-meta-tag {
            display: inline-block;
            margin: 0 10px;
            font-size: 10.5pt;
            color: #555;
        }
        h4 {
            font-size: 14pt;
            font-weight: bold;
            margin-top: 20px;
            margin-bottom: 10px;
            font-family: "SimHei", sans-serif;
        }
        .question-content {
            text-indent: 0;
            line-height: 1.6;
            text-align: justify;
            margin-bottom: 10px;
            font-size: 12pt;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            margin-bottom: 20px;
        }
        th, td {
            border: 1px solid #000;
            padding: 8px;
            font-size: 10.5pt;
        }
        th {
            background-color: #f2f2f2;
            text-align: center;
            font-weight: bold;
        }
        td {
            text-align: left;
            vertical-align: middle;
        }
        .blank-space {
            display: inline-block;
            border-bottom: 1px solid #000;
            min-width: 50px;
        }
        /* Hide elements not needed for export */
        .d-none {
            display: none !important;
        }
    </style>`

    const exportArea = document.getElementById("paperExportArea")
    // Clone node
    const clone = exportArea.cloneNode(true)
    // Remove d-none elements from clone
    clone.querySelectorAll('.d-none').forEach(el => el.remove())

    const sourceHTML = clone.innerHTML
    const source = header + styles + sourceHTML + footer
    
    const blob = new Blob(['\ufeff', source], { type: 'application/msword' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `AI智能试卷_${new Date().toISOString().slice(0, 10)}.doc`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    success('文档导出中...')
}
</script>

<style scoped>
.exam-preview-panel {
    flex: 1;
    padding: 2rem;
    overflow-y: auto;
    background-color: var(--bg-body);
    position: relative;
}

.paper-card {
    background-color: var(--bg-card);
    max-width: 800px;
    margin: 0 auto;
    min-height: 1100px;
    padding: 60px 50px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    border: 1px solid var(--border-color);
    border-radius: 2px;
    color: var(--text-main);
    font-family: "Times New Roman", "SimSun", serif;
    transition: all 0.3s ease;
}

.paper-title {
    text-align: center;
    font-weight: bold;
    font-size: 1.8rem;
    margin-bottom: 1.5rem;
    font-family: "SimHei", sans-serif;
}

.paper-subtitle {
    text-align: center;
    font-size: 1.1rem;
    margin-bottom: 2rem;
    color: var(--text-sub);
    border-bottom: 2px solid var(--border-color);
    padding-bottom: 1.5rem;
    line-height: 1.6;
}

.exam-meta-tag {
    font-size: 0.9rem;
    padding: 4px 10px;
    background: rgba(78, 110, 242, 0.08);
    color: var(--primary-color);
    border-radius: 4px;
    display: inline-block;
    margin: 0 5px;
    font-family: sans-serif;
}

.loading-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 100;
    backdrop-filter: blur(2px);
}

[data-bs-theme="dark"] .loading-overlay {
    background: rgba(0, 0, 0, 0.7);
}

:deep(.question-content) {
    font-size: 1.15rem;
    line-height: 2;
    text-align: justify;
    margin-bottom: 1rem;
}

:deep(table) {
  /* Inherited styles usually work, but let's be safe */
}

/* Dark Mode Overrides */
[data-bs-theme="dark"] :deep(.table-light th) {
    background-color: #2c2c2c !important;
    color: var(--text-main) !important;
    border-color: var(--border-color) !important;
}

[data-bs-theme="dark"] :deep(.table-bordered) {
    border-color: var(--border-color) !important;
}

[data-bs-theme="dark"] :deep(.badge.bg-secondary-subtle) {
    background-color: rgba(255, 255, 255, 0.1) !important;
    color: var(--text-sub) !important;
    border-color: var(--border-color) !important;
}

/* Mobile Optimized Table Styles */
@media (max-width: 768px) {
  :deep(.mobile-optimized-table) {
    display: block;
  }

  :deep(.mobile-optimized-table thead) {
    display: none;
  }

  :deep(.mobile-optimized-table tbody) {
    display: block;
  }

  :deep(.mobile-optimized-table tr) {
    display: block;
    margin-bottom: 1rem;
    border: 1px solid var(--border-color);
    border-radius: 0.5rem;
    padding: 1rem;
    background-color: var(--bg-card);
  }

  :deep(.mobile-optimized-table td) {
    display: block;
    text-align: left !important;
    padding: 0.5rem 0 !important;
    border: none !important;
  }

  :deep(.mobile-optimized-table td:first-child) {
    font-size: 0.85rem;
    color: var(--text-sub);
    padding-bottom: 0.25rem !important;
  }

  :deep(.mobile-optimized-table td:first-child::before) {
    content: "题号 ";
  }

  :deep(.mobile-optimized-table td:nth-child(2)) {
    font-size: 1.1rem;
    padding-bottom: 0.75rem !important;
    border-bottom: 1px solid var(--border-color) !important;
  }

  :deep(.mobile-optimized-table td:nth-child(2)::before) {
    content: "答案: ";
    font-weight: normal;
    color: var(--text-sub);
    font-size: 0.85rem;
  }

  :deep(.mobile-optimized-table td:last-child) {
    padding-top: 0.75rem !important;
    padding-left: 0 !important;
    font-size: 0.95rem;
    line-height: 1.7;
  }

  :deep(.mobile-optimized-table td:last-child::before) {
    content: "解析: ";
    font-weight: 600;
    color: var(--text-main);
    display: block;
    margin-bottom: 0.25rem;
  }
}

/* Paper Content Animation */
.paper-fade-enter-active,
.paper-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.paper-fade-enter-from,
.paper-fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
