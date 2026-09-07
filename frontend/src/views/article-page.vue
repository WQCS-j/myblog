<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import { ArrowLeft, ArrowRight, Share2 } from 'lucide-vue-next'
import { RouterLink, useRoute } from 'vue-router'
import { requestApi } from '../services/api'

const route = useRoute()
const article = ref(null)
const loading = ref(true)
const status = ref('')
const progress = ref(0)
const readingStartedAt = ref(0)
const maximumScrollDepth = ref(0)
const readingDataSent = ref(false)
const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  highlight: (code, language) => {
    if (language && hljs.getLanguage(language)) {
      return `<pre><code class="hljs">${hljs.highlight(code, { language }).value}</code></pre>`
    }
    return `<pre><code>${markdown.utils.escapeHtml(code)}</code></pre>`
  }
})
const renderedContent = computed(() => article.value ? markdown.render(article.value.content || '') : '')

async function loadArticle() {
  loading.value = true
  status.value = ''
  readingDataSent.value = false
  maximumScrollDepth.value = 0

  try {
    const result = await requestApi(`/articles/${route.params.id}`)
    article.value = result.data
    readingStartedAt.value = Date.now()
  } catch (error) {
    status.value = error.message
  } finally {
    loading.value = false
  }
}

function updateProgress() {
  const max = document.documentElement.scrollHeight - window.innerHeight
  const nextProgress = max > 0 ? Math.min(100, Math.round((window.scrollY / max) * 100)) : 0
  progress.value = nextProgress
  maximumScrollDepth.value = Math.max(maximumScrollDepth.value, nextProgress)
}

async function recordReadingData() {
  if (!article.value?.id || !readingStartedAt.value || readingDataSent.value) return

  const durationSeconds = Math.max(1, Math.round((Date.now() - readingStartedAt.value) / 1000))
  readingDataSent.value = true

  try {
    await requestApi('/reading-data', {
      method: 'POST',
      keepalive: true,
      body: JSON.stringify({
        articleId: article.value.id,
        durationSeconds,
        scrollDepth: maximumScrollDepth.value
      })
    })
  } catch {
    // Reading telemetry must never interrupt the reading experience.
  }
}

function handleVisibilityChange() {
  if (document.visibilityState === 'hidden') recordReadingData()
}

async function shareArticle() {
  try {
    await navigator.clipboard.writeText(window.location.href)
    status.value = '文章链接已复制'
  } catch {
    status.value = '请复制浏览器地址分享'
  }
}

onMounted(() => {
  loadArticle()
  window.addEventListener('scroll', updateProgress, { passive: true })
  window.addEventListener('pagehide', recordReadingData)
  document.addEventListener('visibilitychange', handleVisibilityChange)
  updateProgress()
})

onUnmounted(() => {
  recordReadingData()
  window.removeEventListener('scroll', updateProgress)
  window.removeEventListener('pagehide', recordReadingData)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<template>
  <div class="article-page">
    <div class="reading-progress"><span :style="{ width: `${progress}%` }"></span></div>

    <section v-if="loading" class="content-narrow empty-article-page">
      <div class="empty-state">正在加载文章...</div>
    </section>
    <section v-else-if="status && !article" class="content-narrow empty-article-page">
      <div class="empty-state">
        {{ status }}
        <RouterLink class="button button-primary" to="/archive">返回文章列表</RouterLink>
      </div>
    </section>
    <section v-else-if="article" class="content-narrow">
      <div class="article-header">
        <RouterLink class="back-link" to="/archive"><ArrowLeft :size="16" /> 返回文章归档</RouterLink>
        <p class="eyebrow">{{ article.category || '技术笔记' }} / {{ article.author }}</p>
        <h1>{{ article.title }}</h1>
        <div class="article-info">
          <span>{{ new Date(article.publishedAt || article.createdAt).toLocaleDateString() }}</span>
          <span>{{ progress }}% 已阅读</span>
          <button class="text-link" @click="shareArticle"><Share2 :size="15" /> 分享文章</button>
        </div>
      </div>
      <div class="prose" v-html="renderedContent"></div>
      <div class="article-next">
        <RouterLink to="/archive"><ArrowLeft :size="16" /> 返回文章列表</RouterLink>
        <span v-if="progress >= 100">已完成阅读</span>
        <ArrowRight v-else :size="16" />
      </div>
      <p class="muted">{{ status }}</p>
    </section>
  </div>
</template>
