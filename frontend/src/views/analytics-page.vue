<script setup>
import { computed, onMounted, ref } from 'vue'
import { BarChart3, RefreshCw } from 'lucide-vue-next'
import { requestApi } from '../services/api'

const articles = ref([])
const articleId = ref('')
const data = ref(null)
const status = ref('')
const loadingArticles = ref(true)
const loading = ref(false)
const selectedArticle = computed(() => articles.value.find(article => String(article.id) === String(articleId.value)) || null)
const sections = computed(() => data.value?.sections || [])
const maximumDwellSeconds = computed(() => Math.max(1, ...sections.value.map(section => Number(section.dwellSeconds) || 0)))

async function loadArticles() {
  loadingArticles.value = true
  status.value = ''

  try {
    const result = await requestApi('/articles?limit=50')
    articles.value = result.data.items || []
    if (!articleId.value && articles.value[0]) articleId.value = String(articles.value[0].id)
  } catch (error) {
    status.value = error.message
  } finally {
    loadingArticles.value = false
  }
}

async function loadHeatmap() {
  if (!articleId.value) {
    status.value = '请先选择文章'
    return
  }

  loading.value = true
  status.value = ''

  try {
    data.value = (await requestApi(`/reading-heatmap?articleId=${encodeURIComponent(articleId.value)}`)).data
  } catch (error) {
    data.value = null
    status.value = error.message
  } finally {
    loading.value = false
  }
}

function getSectionWidth(section) {
  return `${Math.round(((Number(section.dwellSeconds) || 0) / maximumDwellSeconds.value) * 100)}%`
}

function formatDuration(seconds) {
  const value = Number(seconds) || 0
  if (value < 60) return `${value} 秒`
  return `${Math.floor(value / 60)} 分 ${value % 60} 秒`
}

onMounted(async () => {
  await loadArticles()
  if (articleId.value) loadHeatmap()
})
</script>

<template>
  <section class="content-wide analytics-page">
    <div class="page-intro">
      <p class="eyebrow">数据分析 / READING ANALYTICS</p>
      <h1>看看读者，<br /><em>如何阅读。</em></h1>
      <p>只有作者和管理员可以查看阅读统计。</p>
    </div>

    <div v-if="loadingArticles" class="empty-state">正在加载可分析的文章...</div>
    <div v-else-if="!articles.length" class="empty-state">
      <BarChart3 :size="24" />
      <strong>还没有可分析的文章</strong>
      <span>发布文章并产生阅读记录后，这里会显示统计数据。</span>
    </div>
    <template v-else>
      <div class="analytics-toolbar">
        <label>
          <span>选择文章</span>
          <select v-model="articleId" @change="loadHeatmap">
            <option v-for="article in articles" :key="article.id" :value="String(article.id)">{{ article.title }}</option>
          </select>
        </label>
        <button class="icon-button" title="刷新阅读数据" :disabled="loading" @click="loadHeatmap">
          <RefreshCw :size="17" :class="{ spinning: loading }" />
        </button>
      </div>

      <div v-if="status" class="empty-state">
        <strong>无法加载阅读分析</strong>
        <span>{{ status }}</span>
        <button class="text-link" @click="loadHeatmap">重新加载</button>
      </div>
      <template v-else-if="data">
        <p class="analytics-selection">{{ selectedArticle?.title }}</p>
        <div class="analytics-summary">
          <div><span>阅读人数</span><strong>{{ data.summary?.readers || 0 }}</strong></div>
          <div><span>平均阅读时长</span><strong>{{ formatDuration(data.summary?.averageDuration) }}</strong></div>
          <div><span>平均滚动深度</span><strong>{{ data.summary?.averageScroll || 0 }}%</strong></div>
        </div>

        <section class="analytics-sections">
          <div class="section-heading">
            <div>
              <p class="eyebrow">SECTION DWELL TIME</p>
              <h2>段落停留情况</h2>
            </div>
          </div>
          <div v-if="!sections.length" class="empty-state">
            <BarChart3 :size="24" />
            <strong>暂时没有分段停留数据</strong>
            <span>阅读时长与滚动深度已经记录；分段统计将随 Java 后端实现后自动显示。</span>
          </div>
          <div v-else class="analytics-section-list">
            <div v-for="section in sections" :key="section.sectionKey" class="analytics-section-item">
              <div>
                <strong>{{ section.sectionKey }}</strong>
                <span>{{ formatDuration(section.dwellSeconds) }}</span>
              </div>
              <div class="analytics-bar"><span :style="{ width: getSectionWidth(section) }"></span></div>
            </div>
          </div>
        </section>
      </template>
    </template>
  </section>
</template>
