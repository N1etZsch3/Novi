<template>
  <div class="settings-view">
    <div class="settings-container">
      <div class="mb-4 d-flex align-items-center justify-content-between">
        <div class="d-flex align-items-center">
          <button 
            class="btn btn-icon rounded-circle me-3 hover-bg" 
            @click="router.push('/chat')"
            style="width:40px;height:40px;background:var(--bg-input);color:var(--text-main);"
          >
            <i class="bi bi-arrow-left"></i>
          </button>
          <h4 class="fw-bold mb-0">设置</h4>
        </div>
        <!-- Mobile toggle -->
        <button 
          class="btn btn-outline-secondary border-0 d-lg-none" 
          type="button" 
          @click="isSettingsOpen = true"
        >
          <i class="bi bi-list fs-4"></i>
        </button>
      </div>
      <div class="row g-4">
        <!-- 左侧导航（桌面） -->
        <div class="col-lg-3 d-none d-lg-block">
          <nav class="nav flex-column nav-pills nav-pills-settings">
            <button 
              class="nav-link text-start mb-2"
              :class="{ active: activeTab === 'profile' }"
              @click="activeTab = 'profile'"
            >
              <i class="bi bi-person me-2"></i>个人资料
            </button>
            <button 
              class="nav-link text-start mb-2"
              :class="{ active: activeTab === 'persona' }"
              @click="activeTab = 'persona'"
            >
              <i class="bi bi-magic me-2"></i>AI 设定
            </button>
            <button 
              class="nav-link text-start text-danger mt-4"
              @click="handleLogout"
            >
              <i class="bi bi-box-arrow-left me-2"></i>退出登录
            </button>
          </nav>
        </div>

        <!-- 右侧内容 -->
        <div class="col-lg-9">
          <!-- 个人资料标签页 -->
          <div v-show="activeTab === 'profile'">
            <!-- 个人信息卡片 -->
            <div class="settings-card">
              <h5 class="fw-bold mb-4">个人信息</h5>
              <form @submit.prevent="handleSaveProfile">
                <div class="row g-3">
                  <div class="col-md-6">
                    <label class="form-label small fw-bold text-muted">用户名</label>
                    <input 
                      type="text" 
                      class="form-control rounded-3" 
                      v-model="profileForm.username" 
                      disabled
                    >
                  </div>
                  <div class="col-md-6">
                    <label class="form-label small fw-bold text-muted">昵称</label>
                    <input 
                      type="text" 
                      class="form-control rounded-3" 
                      v-model="profileForm.nickname"
                    >
                  </div>
                  <div class="col-12">
                    <label class="form-label small fw-bold text-muted">邮箱</label>
                    <input 
                      type="email" 
                      class="form-control rounded-3" 
                      v-model="profileForm.email"
                    >
                  </div>
                </div>
                <div class="mt-4 text-end">
                  <button class="btn btn-primary rounded-pill px-4" type="submit">
                    保存更改
                  </button>
                </div>
              </form>
            </div>

            <!-- 安全设置卡片 -->
            <div class="settings-card">
              <h5 class="fw-bold mb-4 text-danger">安全设置</h5>
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-muted">新密码</label>
                  <div class="input-group password-group">
                    <input 
                      :type="showNewPassword ? 'text' : 'password'"
                      class="form-control rounded-start-3" 
                      v-model="passwordForm.newPassword"
                      placeholder="如不修改请留空"
                    >
                    <span 
                      class="input-group-text rounded-end-3 cursor-pointer"
                      @click="showNewPassword = !showNewPassword"
                    >
                      <i :class="showNewPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
                    </span>
                  </div>
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-muted">当前密码</label>
                  <div class="input-group password-group">
                    <input 
                      :type="showCurrentPassword ? 'text' : 'password'"
                      class="form-control rounded-start-3" 
                      v-model="passwordForm.currentPassword"
                      placeholder="修改密码需验证"
                    >
                    <span 
                      class="input-group-text rounded-end-3 cursor-pointer"
                      @click="showCurrentPassword = !showCurrentPassword"
                    >
                      <i :class="showCurrentPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
                    </span>
                  </div>
                </div>
              </div>
              <div class="mt-4 text-end">
                <button 
                  class="btn btn-outline-danger rounded-pill px-4" 
                  @click="handleChangePassword"
                >
                  修改密码
                </button>
              </div>
            </div>
          </div>

          <!-- AI设定标签页 -->
          <div v-show="activeTab === 'persona'">
            <!-- 核心性格卡片 -->
            <div class="settings-card">
              <h5 class="fw-bold mb-4">核心性格</h5>
              <div class="row row-cols-2 row-cols-lg-3 g-3 mb-4">
                <div 
                  v-for="persona in personalities" 
                  :key="persona.configKey"
                  class="col"
                >
                  <div 
                    class="persona-card"
                    :class="{ selected: selectedPersona === persona.configKey }"
                    @click="selectedPersona = persona.configKey"
                  >
                    <i :class="getPersonaIcon(persona.configKey)" class="persona-icon"></i>
                    <div class="persona-name">{{ (persona.description || persona.configKey).split('性格')[0] }}</div>
                    <div class="small text-muted mt-2 text-truncate" style="font-size: 0.8rem; opacity: 0.8;">
                      {{ persona.configValue }}
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="selectedPersona === 'custom'" class="mt-3">
                <label class="form-label small fw-bold text-muted">自定义 Prompt</label>
                <textarea 
                  class="form-control rounded-3" 
                  v-model="customPersona"
                  rows="3"
                  placeholder="输入你想要的 Prompt..."
                ></textarea>
              </div>
            </div>

            <!-- 交互偏好卡片 -->
            <div class="settings-card">
              <h5 class="fw-bold mb-4">交互偏好</h5>
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-muted">回复风格</label>
                  <CustomSelect 
                    v-model="preferences.tone" 
                    :options="toneOptions" 
                  />
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-muted">语言</label>
                  <CustomSelect 
                    v-model="preferences.language" 
                    :options="languageOptions" 
                  />
                </div>
                <div class="col-12">
                  <label class="form-label small fw-bold text-muted">如何称呼你</label>
                  <input 
                    type="text" 
                    class="form-control rounded-3" 
                    v-model="preferences.addressName"
                  >
                </div>
              </div>
              <div class="mt-4 text-end">
                <button 
                  class="btn btn-primary rounded-pill px-4"
                  @click="handleSavePreferences"
                >
                  应用设置
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Mobile Settings Offcanvas -->
    <div class="offcanvas-backdrop fade show" v-if="isSettingsOpen" @click="isSettingsOpen = false"></div>
    <div class="offcanvas offcanvas-end rounded-start-4" :class="{ show: isSettingsOpen }" tabindex="-1" id="settingsOffcanvas" style="width: 280px;">
      <div class="offcanvas-header border-bottom">
        <h5 class="offcanvas-title fw-bold">设置</h5>
        <button type="button" class="btn-close" @click="isSettingsOpen = false"></button>
      </div>
      <div class="offcanvas-body">
        <nav class="nav flex-column nav-pills nav-pills-settings">
          <button 
            class="nav-link text-start mb-2"
            :class="{ active: activeTab === 'profile' }"
            @click="activeTab = 'profile'; isSettingsOpen = false"
          >
            <i class="bi bi-person me-2"></i>个人资料
          </button>
          <button 
            class="nav-link text-start mb-2"
            :class="{ active: activeTab === 'persona' }"
            @click="activeTab = 'persona'; isSettingsOpen = false"
          >
            <i class="bi bi-magic me-2"></i>AI 设定
          </button>
          <button 
            class="nav-link text-start text-danger mt-4"
            @click="handleLogout"
          >
            <i class="bi bi-box-arrow-left me-2"></i>退出登录
          </button>
        </nav>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import { getProfile } from '@/api/auth'
import { updateProfile, changePassword } from '@/api/user'
import { getPersonalities, getTones, getUserPreferences, updateUserPreferences } from '@/api/prompt'
import { useToast } from '@/composables/useToast'
import CustomSelect from '@/components/common/CustomSelect.vue'

const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()
const { success, error } = useToast()

const isSettingsOpen = ref(false)
const activeTab = ref('profile')

// 个人资料表单
const profileForm = ref({
  username: '',
  nickname: '',
  email: ''
})

// 密码表单
const passwordForm = ref({
  newPassword: '',
  currentPassword: ''
})

const showNewPassword = ref(false)
const showCurrentPassword = ref(false)

// AI设定
const personalities = ref([])
const tones = ref([])
const selectedPersona = ref('default')
const customPersona = ref('')
const preferences = ref({
  tone: '',
  language: '',
  addressName: ''
})

const toneOptions = computed(() => tones.value.map(t => ({
  label: t.description || t.configKey.replace('tone_', ''),
  value: t.configKey.replace('tone_', '')
})))

const languageOptions = [
  { label: '自动', value: '' },
  { label: '中文', value: 'zh_CN' },
  { label: 'English', value: 'en_US' }
]

// 图标映射
const iconMap = {
  'default': 'bi-emoji-smile',
  'witty': 'bi-emoji-laughing',
  'gentle': 'bi-flower1',
  'professional': 'bi-briefcase',
  'tsundere': 'bi-lightning',
  'custom': 'bi-pencil-square'
}

function getPersonaIcon(key) {
  const id = key.replace('personality_', '')
  return iconMap[id] || 'bi-star'
}

onMounted(async () => {
  await loadUserProfile()
  await loadPersonalities()
  await loadTones()
  // await loadUserPersona() // Removed separate call
  await loadUserPreferences()
})

async function loadUserProfile() {
  try {
    const res = await getProfile()
    if ((res.code === 1 || res.code === 200) && res.data) {
      profileForm.value = {
        username: res.data.username || '',
        nickname: res.data.nickname || '',
        email: res.data.email || ''
      }
    }
  } catch (err) {
    console.error('加载用户资料失败:', err)
  }
}

async function loadPersonalities() {
  try {
    const res = await getPersonalities()
    if ((res.code === 1 || res.code === 200) && res.data) {
      personalities.value = res.data
      // Append Custom option
      personalities.value.push({
        configKey: 'custom',
        configValue: '完全由你决定',
        description: '自定义'
      })
    }
  } catch (err) {
    console.error('加载性格选项失败:', err)
  }
}

async function loadTones() {
  try {
    const res = await getTones()
    if ((res.code === 1 || res.code === 200) && res.data) {
      tones.value = res.data
    }
  } catch (err) {
    console.error('加载语气选项失败:', err)
  }
}



async function loadUserPreferences() {
  try {
    const res = await getUserPreferences()
    if ((res.code === 1 || res.code === 200) && res.data) {
      // API returns combined preferences
      const data = res.data
      preferences.value = {
        tone: data.toneStyle || 'normal', // Handle tone_ prefix if needed, but novi-v5 says toneStyle
        language: data.language || '',
        addressName: data.userAddressName || ''
      }
      selectedPersona.value = data.personalityMode || 'default'
      // If custom persona is selected, we might need to fetch custom prompt?
      // novi-v5.html doesn't seem to load custom prompt separately in loadPreferences, 
      // but it might be part of the response or handled differently.
      // Assuming personalityMode 'custom' implies using a custom prompt stored elsewhere or in the same object?
      // Looking at novi-v5.html line 3017: pMode = document.getElementById('psCustomPersona').value || 'custom';
      // It seems custom prompt is just a value in the input.
    }
  } catch (err) {
    console.error('加载用户偏好失败:', err)
  }
}

async function handleSaveProfile() {
  try {
    const res = await updateProfile({
      nickname: profileForm.value.nickname,
      email: profileForm.value.email
    })
    if (res.code === 1 || res.code === 200) {
      success('个人信息更新成功')
      userStore.setUserInfo(res.data || profileForm.value)
    } else {
      error(res.msg || '更新失败')
    }
  } catch (err) {
    console.error('更新失败:', err)
    error('更新失败')
  }
}

async function handleChangePassword() {
  if (!passwordForm.value.newPassword) {
    error('请输入新密码')
    return
  }
  if (!passwordForm.value.currentPassword) {
    error('请输入当前密码')
    return
  }

  try {
    const res = await changePassword({
      oldPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword
    })
    if (res.code === 1 || res.code === 200) {
      success('密码修改成功，请重新登录')
      passwordForm.value = { newPassword: '', currentPassword: '' }
      setTimeout(() => {
        handleLogout()
      }, 1500)
    } else {
      error(res.msg || '修改失败')
    }
  } catch (err) {
    console.error('修改密码失败:', err)
    error('修改密码失败')
  }
}

async function handleSavePreferences() {
  try {
    const data = {
      personalityMode: selectedPersona.value === 'custom' ? customPersona.value : selectedPersona.value,
      toneStyle: preferences.value.tone,
      language: preferences.value.language || null,
      userAddressName: preferences.value.addressName
    }
    
    // If selectedPersona is NOT custom, personalityMode should be the key (e.g. 'witty')
    // If selectedPersona IS custom, personalityMode should be the custom prompt text?
    // Re-reading novi-v5.html line 3016:
    // if (customCard.classList.contains('selected')) { pMode = customInput.value || 'custom'; }
    // else { pMode = sel.id.replace('card-', ''); }
    // So if custom, it sends the PROMPT as personalityMode? That seems odd but that's what the code says.
    // Wait, line 3017: `pMode = document.getElementById('psCustomPersona').value || 'custom';`
    // Yes, it sends the custom prompt content as `personalityMode` if custom is selected.
    
    if (selectedPersona.value === 'custom') {
        data.personalityMode = customPersona.value || 'custom'
    } else {
        data.personalityMode = selectedPersona.value
    }

    const res = await updateUserPreferences(data)
    
    if (res.code === 1 || res.code === 200) {
      success('设置已保存')
    } else {
      error(res.msg || '保存失败')
    }
  } catch (err) {
    console.error('保存AI设定失败:', err)
    error('保存失败')
  }
}

function handleLogout() {
  authStore.logout()
  router.push('/auth')
}
</script>

<style scoped>
.settings-view {
  background-color: var(--bg-body);
  overflow-y: auto;
  height: 100%;
}

.settings-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.hover-bg:hover {
  filter: brightness(0.95);
}

[data-bs-theme="dark"] .hover-bg:hover {
  filter: brightness(1.1);
}

/* 导航pills样式 - 精确匹配原版 */
.nav-pills-settings .nav-link {
  color: var(--text-sub);
  border-radius: 12px;
  padding: 0.8rem 1.2rem;
  margin-bottom: 0.5rem;
  transition: all 0.2s;
}

.nav-pills-settings .nav-link:hover {
  background-color: rgba(0, 0, 0, 0.03);
}

[data-bs-theme="dark"] .nav-pills-settings .nav-link:hover {
  background-color: rgba(255, 255, 255, 0.05);
}

.nav-pills-settings .nav-link.active {
  background-color: var(--primary-color);
  color: #fff;
  font-weight: bold;
}

/* 设置卡片 - 精确匹配原版 */
.settings-card {
  background: var(--bg-card);
  border-radius: 20px;
  border: none;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.02);
  margin-bottom: 2rem;
}

/* Persona卡片 - 精确匹配原版 */
.persona-card {
  border: 2px solid transparent;
  border-radius: 16px;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.2s;
  background-color: var(--bg-input);
  height: 100%;
  text-align: center;
  position: relative;
}

.persona-card:hover {
  transform: translateY(-2px);
  background-color: var(--bg-input-focus);
  border-color: var(--primary-color);
}

.persona-card.selected {
  border-color: var(--primary-color);
  background-color: rgba(78, 110, 242, 0.05);
}

.persona-card.selected::after {
  content: '\F26E';
  font-family: 'bootstrap-icons';
  position: absolute;
  top: 10px;
  right: 10px;
  color: var(--primary-color);
}

.persona-icon {
  font-size: 2.5rem;
  color: var(--primary-color);
  margin-bottom: 0.8rem;
  display: block;
}

.persona-card.selected .persona-icon {
  color: var(--primary-color);
  filter: brightness(1.1);
}

.persona-name {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-main);
}

/* 密码显示切换 */
.password-group .input-group-text {
  cursor: pointer;
  user-select: none;
  background-color: var(--bg-input);
  border-left: 0;
}

.password-group .form-control {
  border-right: 0;
}

.password-group .form-control:focus + .input-group-text {
  border-color: #86b7fe;
}

.cursor-pointer {
  cursor: pointer;
}

/* 容器 */
.settings-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.settings-view {
  height: 100vh;
  background-color: var(--bg-body);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}
</style>
