<script setup>
import { computed, onMounted, ref } from 'vue'
import { ArrowUpRight } from 'lucide-vue-next'
import { requestApi } from '../services/api'

const loading = ref(true)
const status = ref('')
const profile = ref({ bio: '', skills: [], projects: [], contact: {}, avatarUrl: '' })

function parseJson(value, fallback) {
  if (value && typeof value === 'object') return value
  try { return JSON.parse(value || JSON.stringify(fallback)) } catch { return fallback }
}

function normalizeList(value) {
  const parsed = parseJson(value, [])
  return Array.isArray(parsed) ? parsed : []
}

function normalizeContact(value) {
  const parsed = parseJson(value, {})
  return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {}
}

const contactEntries = computed(() => Object.entries(profile.value.contact || {}))

async function loadProfile() {
  loading.value = true
  status.value = ''
  try {
    const result = await requestApi('/profile')
    const data = result.data || {}
    profile.value = {
      bio: data.bio || '',
      skills: normalizeList(data.skills),
      projects: normalizeList(data.projects),
      contact: normalizeContact(data.contact),
      avatarUrl: data.avatarUrl || ''
    }
  } catch (error) {
    status.value = error.message
  } finally {
    loading.value = false
  }
}

function itemLabel(item) {
  if (typeof item === 'string') return item
  return item?.name || item?.title || item?.label || item?.description || ''
}

onMounted(loadProfile)
</script>

<template>
  <section class="content-wide about-page">
    <div class="about-hero">
      <div>
        <p class="eyebrow">关于博主 / ABOUT THE DEVELOPER</p>
        <h1>你好，我是<br /><em>一名计算机专业学生。</em></h1>
        <div v-if="loading" class="empty-state">正在加载作者资料...</div>
        <div v-else-if="status" class="empty-state">
          <strong>作者资料暂时无法加载</strong>
          <span>{{ status }}</span>
          <button class="text-link" @click="loadProfile">重新加载 <ArrowUpRight :size="15" /></button>
        </div>
        <p v-else class="large-copy">{{ profile.bio || '正在整理个人介绍，欢迎先看看我的文章和项目。' }}</p>
      </div>
      <div class="portrait" :style="profile.avatarUrl ? { backgroundImage: `url(${profile.avatarUrl})` } : {}">
        <span>CS / 2026</span>
      </div>
    </div>

    <div v-if="!loading && !status" class="about-columns">
      <div>
        <p class="eyebrow">技术方向 / FOCUS</p>
        <div v-if="profile.skills.length" class="about-list">
          <span v-for="(skill, index) in profile.skills" :key="index">{{ itemLabel(skill) }}</span>
        </div>
        <p v-else>Vue 3 前端开发、Java Spring Boot 后端服务、MySQL 数据建模，以及可部署、可维护的完整项目。</p>
      </div>
      <div>
        <p class="eyebrow">项目记录 / PROJECTS</p>
        <div v-if="profile.projects.length" class="about-projects">
          <div v-for="(project, index) in profile.projects" :key="index">
            <strong>{{ project.title || project.name || itemLabel(project) }}</strong>
            <span v-if="project.description">{{ project.description }}</span>
          </div>
        </div>
        <p v-else>数据结构与算法、操作系统、计算机网络、软件工程、Git 协作和测试驱动的开发流程。</p>
      </div>
      <div>
        <p class="eyebrow">联系方式 / CONTACT</p>
        <p v-if="contactEntries.length">
          <template v-for="([key, value], index) in contactEntries" :key="key">
            <a v-if="String(value).startsWith('http')" :href="value" target="_blank" rel="noreferrer">{{ key }} ↗</a>
            <a v-else-if="String(value).includes('@')" :href="`mailto:${value}`">{{ value }}</a>
            <span v-else>{{ key }}：{{ value }}</span>
            <br v-if="index < contactEntries.length - 1" />
          </template>
        </p>
        <p v-else><a href="mailto:hello@zheye.example">hello@zheye.example</a><br /><a href="https://github.com" target="_blank" rel="noreferrer">GitHub ↗</a></p>
      </div>
    </div>
  </section>
</template>
