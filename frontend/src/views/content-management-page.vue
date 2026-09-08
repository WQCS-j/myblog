<script setup>
import { onMounted, ref } from 'vue'
import { ArrowLeft, Check, Plus, Save, Trash2 } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
import {
  createFriendLink,
  createInterest,
  createTimelineItem,
  deleteFriendLink,
  deleteInterest,
  deleteTimelineItem,
  getPrivateSettings,
  requestApi,
  updateFriendLink,
  updateInterest,
  updatePrivateSettings,
  updateProfile,
  updateTimelineItem
} from '../services/api'

const tabs = [
  { key: 'profile', label: 'About / 作者资料' },
  { key: 'timeline', label: '大事记' },
  { key: 'interests', label: '兴趣' },
  { key: 'friends', label: '友链' },
  { key: 'private', label: '秘密基地' }
]
const activeTab = ref('profile')
const loading = ref(true)
const saving = ref(false)
const status = ref('')
const profile = ref({ bio: '', skills: '[]', projects: '[]', contact: '{}', avatarUrl: '' })
const timeline = ref([])
const interests = ref([])
const friends = ref([])
const privateSettings = ref({ initialized: false, title: '', content: '', password: '' })
const selectedTimeline = ref(null)
const selectedInterest = ref(null)
const selectedFriend = ref(null)

function parseJson(value, fallback) {
  if (value && typeof value === 'object') return value
  try { return JSON.parse(value || JSON.stringify(fallback)) } catch { throw new Error('JSON 内容格式错误') }
}

function cloneItem(item) {
  return item ? JSON.parse(JSON.stringify(item)) : null
}

function setStatus(message) {
  status.value = message
  window.setTimeout(() => { if (status.value === message) status.value = '' }, 3500)
}

async function loadAll() {
  loading.value = true
  try {
    const [profileResult, timelineResult, interestsResult, friendsResult, privateResult] = await Promise.all([
      requestApi('/profile'),
      requestApi('/timeline'),
      requestApi('/interests'),
      requestApi('/friend-links'),
      getPrivateSettings()
    ])
    const item = profileResult.data || {}
    profile.value = {
      bio: item.bio || '',
      skills: JSON.stringify(parseJson(item.skills, []), null, 2),
      projects: JSON.stringify(parseJson(item.projects, []), null, 2),
      contact: JSON.stringify(parseJson(item.contact, {}), null, 2),
      avatarUrl: item.avatarUrl || ''
    }
    timeline.value = timelineResult.data || []
    interests.value = interestsResult.data || []
    friends.value = friendsResult.data || []
    privateSettings.value = { ...(privateResult.data || {}), password: '' }
  } catch (error) {
    setStatus(error.message)
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  saving.value = true
  try {
    const result = await updateProfile({
      bio: profile.value.bio,
      skills: parseJson(profile.value.skills, []),
      projects: parseJson(profile.value.projects, []),
      contact: parseJson(profile.value.contact, {}),
      avatarUrl: profile.value.avatarUrl
    })
    const item = result.data || {}
    profile.value.skills = JSON.stringify(parseJson(item.skills, []), null, 2)
    profile.value.projects = JSON.stringify(parseJson(item.projects, []), null, 2)
    profile.value.contact = JSON.stringify(parseJson(item.contact, {}), null, 2)
    setStatus('作者资料已保存')
  } catch (error) { setStatus(error.message) } finally { saving.value = false }
}

function newTimeline() { selectedTimeline.value = { eventDate: new Date().toISOString().slice(0, 10), title: '', content: '' } }
function newInterest() { selectedInterest.value = { category: '', title: '', summary: '', link: '' } }
function newFriend() { selectedFriend.value = { name: '', description: '', url: '' } }

async function saveTimeline() {
  if (!selectedTimeline.value) return
  saving.value = true
  try {
    const item = selectedTimeline.value
    const result = item.id ? await updateTimelineItem(item.id, item) : await createTimelineItem(item)
    if (item.id) timeline.value = timeline.value.map(entry => entry.id === item.id ? result.data : entry)
    else timeline.value.unshift(result.data)
    selectedTimeline.value = cloneItem(result.data)
    setStatus('大事记已保存')
  } catch (error) { setStatus(error.message) } finally { saving.value = false }
}

async function removeTimeline() {
  if (!selectedTimeline.value?.id || !window.confirm('确定删除这条大事记吗？')) return
  try { await deleteTimelineItem(selectedTimeline.value.id); timeline.value = timeline.value.filter(item => item.id !== selectedTimeline.value.id); selectedTimeline.value = null; setStatus('大事记已删除') }
  catch (error) { setStatus(error.message) }
}

async function saveInterest() {
  if (!selectedInterest.value) return
  saving.value = true
  try {
    const item = selectedInterest.value
    const result = item.id ? await updateInterest(item.id, item) : await createInterest(item)
    if (item.id) interests.value = interests.value.map(entry => entry.id === item.id ? result.data : entry)
    else interests.value.unshift(result.data)
    selectedInterest.value = cloneItem(result.data)
    setStatus('兴趣内容已保存')
  } catch (error) { setStatus(error.message) } finally { saving.value = false }
}

async function removeInterest() {
  if (!selectedInterest.value?.id || !window.confirm('确定删除这条兴趣内容吗？')) return
  try { await deleteInterest(selectedInterest.value.id); interests.value = interests.value.filter(item => item.id !== selectedInterest.value.id); selectedInterest.value = null; setStatus('兴趣内容已删除') }
  catch (error) { setStatus(error.message) }
}

async function saveFriend() {
  if (!selectedFriend.value) return
  saving.value = true
  try {
    const item = selectedFriend.value
    const result = item.id ? await updateFriendLink(item.id, item) : await createFriendLink(item)
    if (item.id) friends.value = friends.value.map(entry => entry.id === item.id ? result.data : entry)
    else friends.value.push(result.data)
    selectedFriend.value = cloneItem(result.data)
    setStatus('友情链接已保存')
  } catch (error) { setStatus(error.message) } finally { saving.value = false }
}

async function removeFriend() {
  if (!selectedFriend.value?.id || !window.confirm('确定删除这个友情链接吗？')) return
  try { await deleteFriendLink(selectedFriend.value.id); friends.value = friends.value.filter(item => item.id !== selectedFriend.value.id); selectedFriend.value = null; setStatus('友情链接已删除') }
  catch (error) { setStatus(error.message) }
}

async function savePrivateSettings() {
  saving.value = true
  try {
    const result = await updatePrivateSettings(privateSettings.value)
    privateSettings.value = { ...(result.data || {}), password: '' }
    setStatus('秘密基地设置已保存')
  } catch (error) { setStatus(error.message) } finally { saving.value = false }
}

onMounted(loadAll)
</script>

<template>
  <section class="content-wide management-page">
    <RouterLink class="back-link" to="/studio"><ArrowLeft :size="16" /> 返回作者后台</RouterLink>
    <div class="page-intro">
      <p class="eyebrow">内容管理 / CONTENT MANAGEMENT</p>
      <h1>把博客内容<br /><em>写成自己的样子。</em></h1>
      <p>这里的保存内容会写入 Java 后端数据库，公开页面会同步更新。</p>
    </div>

    <div v-if="loading" class="empty-state">正在加载内容管理数据...</div>
    <template v-else>
      <div class="management-tabs">
        <button v-for="tab in tabs" :key="tab.key" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">{{ tab.label }}</button>
      </div>
      <p v-if="status" class="management-status"><Check :size="15" /> {{ status }}</p>

      <section v-if="activeTab === 'profile'" class="management-section">
        <h2>About / 作者资料</h2>
        <label>个人简介<textarea v-model="profile.bio" rows="5" /></label>
        <label>技能 JSON<textarea v-model="profile.skills" rows="5" /></label>
        <label>项目 JSON<textarea v-model="profile.projects" rows="5" /></label>
        <label>联系方式 JSON<textarea v-model="profile.contact" rows="5" /></label>
        <label>头像地址<input v-model="profile.avatarUrl" /></label>
        <button class="button button-primary" :disabled="saving" @click="saveProfile"><Save :size="16" /> 保存作者资料</button>
      </section>

      <section v-else-if="activeTab === 'timeline'" class="management-section management-records">
        <div class="management-heading"><h2>个人大事记</h2><button class="button" @click="newTimeline"><Plus :size="16" /> 新建</button></div>
        <div class="management-record-layout">
          <div class="management-record-list">
            <button v-for="item in timeline" :key="item.id" :class="{ active: selectedTimeline?.id === item.id }" @click="selectedTimeline = cloneItem(item)">{{ item.eventDate }} · {{ item.title }}</button>
            <p v-if="!timeline.length" class="muted">暂无大事记</p>
          </div>
          <div v-if="selectedTimeline" class="management-form">
            <label>日期<input v-model="selectedTimeline.eventDate" type="date" /></label>
            <label>标题<input v-model="selectedTimeline.title" /></label>
            <label>详细内容<textarea v-model="selectedTimeline.content" rows="8" /></label>
            <div class="management-actions"><button class="icon-button danger" title="删除" @click="removeTimeline"><Trash2 :size="17" /></button><button class="button button-primary" :disabled="saving" @click="saveTimeline"><Save :size="16" /> 保存</button></div>
          </div>
          <div v-else class="empty-state">选择一条记录，或点击新建。</div>
        </div>
      </section>

      <section v-else-if="activeTab === 'interests'" class="management-section management-records">
        <div class="management-heading"><h2>兴趣内容</h2><button class="button" @click="newInterest"><Plus :size="16" /> 新建</button></div>
        <div class="management-record-layout">
          <div class="management-record-list">
            <button v-for="item in interests" :key="item.id" :class="{ active: selectedInterest?.id === item.id }" @click="selectedInterest = cloneItem(item)">{{ item.category }} · {{ item.title }}</button>
            <p v-if="!interests.length" class="muted">暂无兴趣内容</p>
          </div>
          <div v-if="selectedInterest" class="management-form">
            <label>分类<input v-model="selectedInterest.category" /></label>
            <label>标题<input v-model="selectedInterest.title" /></label>
            <label>简介<textarea v-model="selectedInterest.summary" rows="6" /></label>
            <label>外部链接<input v-model="selectedInterest.link" placeholder="https://" /></label>
            <div class="management-actions"><button class="icon-button danger" title="删除" @click="removeInterest"><Trash2 :size="17" /></button><button class="button button-primary" :disabled="saving" @click="saveInterest"><Save :size="16" /> 保存</button></div>
          </div>
          <div v-else class="empty-state">选择一条记录，或点击新建。</div>
        </div>
      </section>

      <section v-else-if="activeTab === 'friends'" class="management-section management-records">
        <div class="management-heading"><h2>友情链接</h2><button class="button" @click="newFriend"><Plus :size="16" /> 新建</button></div>
        <div class="management-record-layout">
          <div class="management-record-list">
            <button v-for="item in friends" :key="item.id" :class="{ active: selectedFriend?.id === item.id }" @click="selectedFriend = cloneItem(item)">{{ item.name }}</button>
            <p v-if="!friends.length" class="muted">暂无友情链接</p>
          </div>
          <div v-if="selectedFriend" class="management-form">
            <label>网站名称<input v-model="selectedFriend.name" /></label>
            <label>网站简介<textarea v-model="selectedFriend.description" rows="5" /></label>
            <label>网址<input v-model="selectedFriend.url" placeholder="https://" /></label>
            <div class="management-actions"><button class="icon-button danger" title="删除" @click="removeFriend"><Trash2 :size="17" /></button><button class="button button-primary" :disabled="saving" @click="saveFriend"><Save :size="16" /> 保存</button></div>
          </div>
          <div v-else class="empty-state">选择一条记录，或点击新建。</div>
        </div>
      </section>

      <section v-else class="management-section">
        <h2>秘密基地访问设置</h2>
        <p class="muted">{{ privateSettings.initialized ? '已初始化：留空密码表示只更新内容。' : '尚未初始化：第一次保存必须设置访问密码。' }}</p>
        <label>标题<input v-model="privateSettings.title" /></label>
        <label>私密内容<textarea v-model="privateSettings.content" rows="10" /></label>
        <label>访问密码<input v-model="privateSettings.password" type="password" placeholder="首次必填，修改时可留空" /></label>
        <button class="button button-primary" :disabled="saving" @click="savePrivateSettings"><Save :size="16" /> 保存秘密基地</button>
      </section>
    </template>
  </section>
</template>
