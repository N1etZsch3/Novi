<template>
  <div class="paper-preview-container h-100 d-flex flex-column flex-grow-1" style="background: var(--bg-body);">
    
    <!-- Loading State (Initial) -->
    <div v-if="loading && pages.length === 0" class="flex-grow-1 d-flex flex-column align-items-center justify-content-center text-muted">
         <div class="spinner-border text-primary mb-3" role="status"></div>
         <p>AI 正在规划试卷结构...</p>
         <small class="text-opacity-50">这可能需要几秒钟</small>
    </div>

    <!-- Empty State -->
    <div v-else-if="!loading && pages.length === 0 && !data" class="flex-grow-1 d-flex flex-column align-items-center justify-content-center text-muted opacity-50">
        <i class="bi bi-file-earmark-text display-1 mb-3"></i>
        <p class="fs-5">选择配置并点击生成</p>
        <p class="small">AI 将为您创建一套完整的仿真试卷</p>
    </div>

    <!-- Toolbar (Visible when data exists) -->
    <div v-if="!loading && (data || streamData.length > 0)" class="d-flex align-items-center justify-content-between px-4 py-2 border-bottom bg-white fade-in">
        <div class="text-muted small">
            <span v-if="paperHeader">共 {{ paperHeader.totalQuestions || PagesToRender.reduce((acc, p) => acc + (p.questionTypeName ? 0 : 1), 0) }} 题</span>
        </div>
        <button class="btn btn-success btn-sm text-white shadow-sm" @click="exportToWord">
            <i class="bi bi-file-earmark-word me-1"></i> 导出 Word
        </button>
    </div>

    <!-- Paper Content Swiper/Scroll -->
    <div v-if="!loading && (data || streamData.length > 0)" class="paper-content-area flex-grow-1 overflow-auto custom-scrollbar p-3 p-lg-5">
        
        <!-- Header Info (Only visible if we have data) -->
        <div v-if="paperHeader" class="text-center mb-5 fade-in">
           <h2 class="fw-bold mb-3" style="font-family: 'SimHei', sans-serif;">{{ paperHeader.paperName }}</h2>
           <div class="d-inline-flex gap-3 text-muted small">
             <span>{{ paperHeader.subjectName }}</span>
             <span>|</span>
             <span>共 {{ paperHeader.totalQuestions || '...' }} 题</span>
           </div>
        </div>

        <!-- Render Pages -->
        <PaperPage 
          v-for="(page, idx) in PagesToRender" 
          :key="page.id" 
          :title="page.title" 
          :subtitle="page.subtitle" 
          :page-number="idx + 1"
          class="fade-in-up"
          :style="{ animationDelay: `${idx * 0.1}s` }"
        >
            <!-- Render Questions within the page -->
            <div v-html="page.htmlContent"></div>
        </PaperPage>

        <!-- Loading Indicator at bottom (Streaming) -->
        <div v-if="loading" class="text-center py-5 text-muted fade-in">
           <div class="spinner-grow spinner-grow-sm text-primary me-2" role="status"></div>
           <span>AI 正在撰写后续试题...</span>
        </div>

        <!-- Answer Sheet (Last Page) -->
        <PaperPage 
          v-if="!loading && allAnswers.length > 0" 
          title="参考答案与解析" 
          subtitle="Answer Sheet" 
          :page-number="PagesToRender.length + 1"
          class="fade-in-up"
        >
            <div class="answers-container">
               <div v-for="(part, pIdx) in allAnswers" :key="pIdx" class="mb-4">
                  <h5 class="fw-bold mb-3 border-bottom pb-2">{{ part.title }}</h5>
                  <div class="row g-4">
                    <div v-for="item in part.items" :key="item.num" class="col-12">
                      <div class="d-flex gap-3">
                        <!-- Number Badge -->
                        <div class="flex-shrink-0 pt-1">
                          <span class="badge rounded-pill bg-light text-dark border border-secondary fw-bold" 
                                style="font-size: 0.9rem; min-width: 2.5em;">
                            {{ item.num }}
                          </span>
                        </div>
                        
                        <!-- Content -->
                        <div class="flex-grow-1">
                           <!-- Answer Line -->
                           <div class="mb-2 d-flex align-items-center">
                              <span class="fw-bold me-2">参考答案:</span>
                              <span class="text-primary fw-bold fs-5 font-monospace">{{ item.ans }}</span>
                           </div>
                           
                           <!-- Analysis Box -->
                           <div class="bg-light p-3 rounded-2 border border-light-subtle">
                             <div class="d-flex align-items-center mb-2 text-muted small">
                               <i class="bi bi-lightbulb me-1"></i>
                               <span class="fw-bold">解析详情</span>
                             </div>
                             <div class="text-secondary" style="line-height: 1.8; font-size: 0.95rem; white-space: pre-wrap;">{{ item.ana || '暂无解析' }}</div>
                           </div>
                        </div>
                      </div>
                    </div>
                  </div>
               </div>
            </div>
        </PaperPage>

    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import PaperPage from './PaperPage.vue'

const props = defineProps({
  loading: Boolean,
  data: Object, 
  streamData: Array 
})

// Local state for streaming
const streamPages = ref([])
const streamAnswers = ref([])
const globalQuestionIndex = ref(1)

const isHistoryMode = computed(() => !!props.data && !props.loading && (!props.streamData || props.streamData.length === 0))

const paperHeader = computed(() => {
  if (isHistoryMode.value) {
    return {
      paperName: props.data.paperName,
      subjectName: props.data.subjectName,
      totalQuestions: props.data.totalQuestions
    }
  } else if (streamPages.value.length > 0) {
     // Calculate total questions from stream data
     const totalQuestions = globalQuestionIndex.value - 1 // globalQuestionIndex is always 1 ahead
     return {
       paperName: 'AI 智能生成试卷',
       subjectName: props.loading ? '正在生成...' : '生成完成',
       totalQuestions: props.loading ? '...' : totalQuestions
     }
  }
  return null
})

const PagesToRender = computed(() => {
  if (isHistoryMode.value) {
    return parseHistoryDataToPages(props.data)
  }
  return streamPages.value
})

const allAnswers = computed(() => {
  if (isHistoryMode.value) {
     return extractAnswersFromHistory(props.data)
  }
  return streamAnswers.value
})
// Computed for template check
const pages = computed(() => PagesToRender.value)


watch(() => props.loading, (val) => {
  if (val) {
    streamPages.value = []
    streamAnswers.value = []
    globalQuestionIndex.value = 1
  }
})

watch(() => props.streamData, (events) => {
  if (isHistoryMode.value) return 
  if (!events) return

  const pagesArr = []
  const answersArr = []
  let gIndex = 1

  events.forEach(event => {
    if (event.eventType === 'question') {
        const { bodyHtml, answerPart, nextIndex, subtitle } = processQuestionType(event, gIndex)
        gIndex = nextIndex
        
        pagesArr.push({
            id: event.questionType + '_' + event.order,
            title: `Part ${romanize(event.order)}  ${event.questionTypeName}`,
            subtitle: subtitle || `本部分共 ${JSON.parse(event.questions).length} 题`,
            htmlContent: bodyHtml
        })

        if (answerPart.items.length > 0) {
            answersArr.push(answerPart)
        }
    } else if (event.eventType === 'complete') {
       // Could update total count here if we stored it
    }
  })

  streamPages.value = pagesArr
  streamAnswers.value = answersArr
  globalQuestionIndex.value = gIndex
}, { deep: true, immediate: true })


// --- Rendering Logic ---

function romanize(num) {
  if (isNaN(num)) return NaN;
  var digits = String(+num).split(""),
      key = ["","C","CC","CCC","CD","D","DC","DCC","DCCC","CM",
             "","X","XX","XXX","XL","L","LX","LXX","LXXX","XC",
             "","I","II","III","IV","V","VI","VII","VIII","IX"],
      roman = "",
      i = 3;
  while (i--)
      roman = (key[+digits.pop() + (i * 10)] || "") + roman;
  return Array(+digits.join("") + 1).join("M") + roman;
}

function processQuestionType(data, startIndex) {
    let globalIndex = startIndex
    let contentHtml = ''
    let answerItems = []
    
    let questions = []
    try {
        questions = typeof data.questions === 'string' ? JSON.parse(data.questions) : data.questions
    } catch (e) {
        console.error('Parse questions failed', e)
        return { bodyHtml: '<div class="alert alert-danger">数据解析错误</div>', answerPart: { title: data.questionTypeName, items: [] }, nextIndex: globalIndex }
    }

    if (!questions || !Array.isArray(questions)) return { bodyHtml: '', answerPart: { title: '', items: [] }, nextIndex: globalIndex }

    const partTitle = `Part ${romanize(data.order)} ${data.questionTypeName}`
    let partSubtitle = ''

    questions.forEach((q, idx) => {
        if (!q) return // Skip null items

        let processedContent = ''
        let localAnsItems = []

        // Hoist score_desc to Page Subtitle if it's the first item
        let scoreDescHtml = ''
        if (q.score_desc) {
            if (idx === 0) {
                // If it's the first item, determine if we should hoist it to subtitle
                // Simple heuristic: if it contains "共" or is long, it's a section summary
                // Otherwise it might just be "2分" for the first question.
                // For now, let's hoist it if present, formatted as a badge.
                partSubtitle = `<span class="badge bg-secondary-subtle text-secondary-emphasis border fw-normal">${q.score_desc}</span>`
            } else {
                 scoreDescHtml = `<span class="badge bg-secondary-subtle text-secondary-emphasis border fw-normal float-end">${q.score_desc}</span>`
            }
        }

        if (q.content) {
            processedContent = q.content
            
             if (q.title) {
                const contentTrimmed = processedContent.trim()
                const firstLine = contentTrimmed.split('\n')[0].trim()
                const normalize = s => s.replace(/[^\w\u4e00-\u9fa5]/g, '').toLowerCase()
                if (normalize(firstLine).includes(normalize(q.title)) && firstLine.length < 50) {
                     processedContent = contentTrimmed.substring(firstLine.length).trim()
                }
             }

             if (processedContent.includes('{{index}}')) {
                 let localAnsIdx = 0
                 processedContent = processedContent.replace(/{{index}}/g, () => {
                     const currentNum = globalIndex++
                     let ansStr = ''
                     let anaStr = ''
                     if (Array.isArray(q.answers)) {
                        ansStr = q.answers[localAnsIdx] || ''
                     }
                     if (Array.isArray(q.analyses)) {
                        anaStr = q.analyses[localAnsIdx] || ''
                     }
                     
                     localAnsItems.push({ num: currentNum, ans: ansStr, ana: anaStr })
                     localAnsIdx++
                     return currentNum
                 })
             } else if (Array.isArray(q.answers) && q.answers.length > 0) {
                 q.answers.forEach((ans, i) => {
                     const currentNum = globalIndex++
                     localAnsItems.push({ 
                         num: currentNum, 
                         ans: ans.correct || ans, 
                         ana: q.analyses ? q.analyses[i] : (q.analysis || '') 
                     })
                 })
             } else {
                // No obvious placeholders or answers
             }

                // Detect if we have a subtitle placeholder
                // Only inject score badge if the subtitle starts with "Passage" (case-insensitive)
                // or if it's specifically requested. For "Key Details" etc, we don't want it.
                if (processedContent.includes('{{subtitle:')) {
                     processedContent = processedContent.replace(/{{subtitle:(.*?)}}/g, (match, p1) => {
                         const text = p1.trim()
                         let extraHtml = ''
                         // Only inject badge for Passage
                         if (scoreDescHtml && /^Passage/i.test(text)) {
                             const badgeHtml = scoreDescHtml.replace('float-end', '')
                             extraHtml = `<div class="mb-3 text-start">${badgeHtml}</div>`
                             scoreDescHtml = '' // Consume it so it doesn't appear in title
                         }
                         return `<h5 class="text-center fw-bold my-3">${p1}</h5>${extraHtml}`
                     })
                }

             processedContent = processedContent
                .replace(/{{br}}/g, '<br/>')
                .replace(/{{indent}}\s*/g, '<span style="display:inline-block; width: 2em;"></span>')
                .replace(/{{short_blank}}/g, '________ ') 
                .replace(/{{long_line}}/g, '<div class="my-2 text-muted">_______________________________________________________________________</div>') 
                .replace(/{{checkbox}}/g, '□')

             processedContent = `<div class="question-item mb-4"><div class="question-content lh-lg text-break">${processedContent}</div></div>`

        } else {
             // Fallback
             let text = q.desc || q.stem || '暂无内容'
             processedContent = `<div class="question-item mb-4"><div class="fw-bold mb-2">${globalIndex++}. ${q.title || ''}</div><div>${text}</div></div>`
        }

        
        // Remove redundant title if it looks like a Part header (e.g., "Part I ...")
        let titleToShow = q.title || ''
        if (titleToShow.trim().match(/^Part\s+[IVXLCDM]+\s+/i)) {
            titleToShow = ''
        } else if (titleToShow.includes('Part') && titleToShow.length < 30) {
            titleToShow = ''
        }

        const titleHtml = (titleToShow || scoreDescHtml) 
            ? `<div class="mb-2 text-dark clearfix">${titleToShow ? `<h5 class="fw-bold d-inline-block me-2">${titleToShow}</h5>` : ''} ${scoreDescHtml}</div>` 
            : ''

        contentHtml += `<div class="question-block mb-5">${titleHtml}${processedContent}</div>`
        answerItems.push(...localAnsItems)
    })

    return { 
        bodyHtml: contentHtml, 
        answerPart: { title: partTitle, items: answerItems },
        nextIndex: globalIndex,
        subtitle: partSubtitle
    }
}

function parseHistoryDataToPages(historyData) {
    if (!historyData || !historyData.details) return []
    const details = [...historyData.details].sort((a, b) => a.order - b.order)
    
    let gIndex = 1
    const pages = []
    
    details.forEach(d => {
        const mockEvent = {
            order: d.order || 1, // default if missing
            questionType: d.questionType,
            questionTypeName: d.questionTypeName,
            questions: d.questions 
        }
        
        const { bodyHtml, nextIndex, subtitle } = processQuestionType(mockEvent, gIndex)
        gIndex = nextIndex
        
        pages.push({
            id: d.id || `part_${d.order}`,
            title: `Part ${romanize(d.order)} ${d.questionTypeName}`,
            subtitle: subtitle || `本部分包含若干试题`, // History details might not have count easily avail unless parsed
            htmlContent: bodyHtml
        })
    })
    
    return pages
}

function extractAnswersFromHistory(historyData) {
    if (!historyData || !historyData.details) return []
    const details = [...historyData.details].sort((a, b) => a.order - b.order)
    
    const answers = []
    let gIndex = 1
    
    details.forEach(d => {
        const mockEvent = {
            order: d.order,
            questionTypeName: d.questionTypeName,
            questions: d.questions
        }
        const { answerPart, nextIndex } = processQuestionType(mockEvent, gIndex)
        gIndex = nextIndex
        if (answerPart.items.length) answers.push(answerPart)
    })
    
    return answers
}
// --- Export Logic ---
function exportToWord() {
    if (PagesToRender.value.length === 0) return

    const header = paperHeader.value || {}
    let htmlContent = `
    <html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
    <head>
    <meta charset="utf-8">
    <title>${header.paperName || 'Paper'}</title>
    <!--[if gte mso 9]>
    <xml>
        <w:WordDocument>
            <w:View>Print</w:View>
            <w:Zoom>100</w:Zoom>
            <w:DoNotOptimizeForBrowser/>
        </w:WordDocument>
    </xml>
    <![endif]-->
    <style>
        @page { 
            size: 21cm 29.7cm; 
            margin: 2.54cm 3.17cm 2.54cm 3.17cm;
            mso-page-orientation: portrait;
        }
        body { font-family: 'SimSun', '宋体', serif; font-size: 10.5pt; line-height: 1.5; }
        .paper-page { margin: 20px auto; padding: 0; width: 100%; max-width: 210mm; background: white; }
        h2 { text-align: center; font-family: 'SimHei', '黑体'; font-size: 18pt; margin: 20px 0; }
        .meta { text-align: center; color: #666; font-size: 9pt; margin-bottom: 30px; }
        h4 { font-family: 'SimHei', '黑体'; font-size: 14pt; margin-top: 20px; font-weight: bold; }
        h5 { text-align: center; font-family: 'SimHei', '黑体'; font-size: 12pt; font-weight: bold; margin: 10px 0; }
        .subtitle { color: #555; font-size: 9pt; margin-bottom: 10px; }
        .badge { display: inline-block; padding: 2px 5px; border: 1px solid #ccc; background: #f8f9fa; font-size: 8pt; border-radius: 3px; color: #333; }
        .question-item { margin-bottom: 15px; }
        .question-content { line-height: 1.8; }
        .page-break { page-break-after: always; }
        table { border-collapse: collapse; width: 100%; margin-top: 10px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; font-size: 10pt; }
        .answer-analysis { background: #fafafa; padding: 10px; margin-top: 5px; border: 1px solid #eee; }
        .text-primary { color: #0d6efd; }
        .text-center { text-align: center; }
        .fw-bold { font-weight: bold; }
        .fs-5 { font-size: 14pt; }
        .my-3 { margin: 15px 0; }
        /* Word-compatible paragraph indentation */
        p { margin: 0.5em 0; }
        .indent-para { text-indent: 2em; }
    </style>
    </head>
    <body>
        <div class="paper-page">
            <h2>${header.paperName}</h2>
            <div class="meta">
                <span>${header.subjectName}</span> | <span>共 ${header.totalQuestions} 题</span>
            </div>
    `

    // Pages - process content for Word compatibility
    PagesToRender.value.forEach((page, idx) => {
        // Convert span-based indents to Word-compatible paragraph indents
        let processedContent = page.htmlContent
            // Convert inline span indents to full-width spaces (works better in Word)
            .replace(/<span style="display:inline-block; width: 2em;"><\/span>/g, '\u3000\u3000')
            // Also handle any remaining indent spans with variations
            .replace(/<span[^>]*width:\s*2em[^>]*><\/span>/g, '\u3000\u3000')
        
        htmlContent += `
        <div class="section">
            ${page.title ? `<h4>${page.title}</h4>` : ''}
            ${page.subtitle ? `<div class="subtitle">${page.subtitle}</div>` : ''}
            <div>${processedContent}</div>
        </div>
        `
    })

    // Answers
    if (allAnswers.value.length > 0) {
        htmlContent += `<div class="page-break"></div>`
        htmlContent += `<h2>参考答案与解析</h2>`
        allAnswers.value.forEach(part => {
             htmlContent += `<h4>${part.title}</h4>`
             part.items.forEach(item => {
                 htmlContent += `
                 <div style="margin-bottom: 15px; border-bottom: 1px dashed #eee; padding-bottom: 10px;">
                    <div><strong>${item.num}.</strong> <span class="text-primary" style="font-weight:bold; font-size: 12pt;">${item.ans}</span></div>
                    <div class="answer-analysis" style="margin-top:5px; color:#555; background:#f9f9f9; padding:5px;">
                        <strong>[解析]</strong> ${item.ana || '暂无解析'}
                    </div>
                 </div>`
             })
        })
    }

    htmlContent += `</div></body></html>`

    // Add BOM for proper Unicode encoding in Word
    const blob = new Blob(['\ufeff', htmlContent], { type: 'application/msword;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${header.paperName || 'Generated_Paper'}.doc`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
}
</script>


<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.fade-in {
  animation: fadeIn 0.5s ease-out forwards;
}

.fade-in-up {
  opacity: 0;
  animation: fadeInUp 0.6s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 992px) {
  .paper-content-area {
    padding: 1rem !important;
  }
}
</style>
