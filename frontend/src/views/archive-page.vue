<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ArrowLeft, ArrowRight, ArrowUpRight, FileText, Search, X } from 'lucide-vue-next'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { requestApi } from '../services/api'

const route = useRoute()
const router = useRouter()
const articles = ref([])
const categories = ref([])
const tags = ref([])
const loading = ref(true)
const status = ref('')
const total = ref(0)
const page = ref(1)
const limit = 10
const keyword = ref('')
const category = ref('')
const tag = ref('')
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / limit)))
const canGoPrevious = computed(() => page.value > 1)
const canGoNext = computed(() => page.value < totalPages.value)

function syncFiltersFromRoute() {
  keyword.value = String(route.query.keyword || '')
  category.value = String(route.query.category || '')
  tag.value = String(route.query.tag || '')
  page.value = Math.max(1, Number(route.query.page || 1))
}

function buildArticleQuery() {
  const query = new URLSearchParams({ page: String(page.value), limit: String(limit) })
  if (keyword.value.trim()) query.set('keyword', keyword.value.trim())
  if (category.value) query.set('category', category.value)
  if (tag.value) query.set('tag', tag.value)
  return query.toString()
}

async function loadArticles() {
  loading.value = true
  status.value = ''

  try {
    const result = await requestApi(`/articles?${buildArticleQuery()}`)
    articles.value = result.data.items || []
    total.value = result.data.total || 0
    page.value = result.data.page || page.value
  } catch (error) {
    status.value = error.message
  } finally {
    loading.value = false
  }
}

async function loadFilterOptions() {
  try {
    const [categoryResult, tagResult] = await Promise.all([
      requestApi('/categories'),
      requestApi('/tags')
    ])
    categories.value = categoryResult.data || []
    tags.value = tagResult.data || []
  } catch {
    // Article browsing remains available when optional filter data is unavailable.
  }
}

function updateRoute(nextPage = 1) {
  const query = {}
  if (keyword.value.trim()) query.keyword = keyword.value.trim()
  if (category.value) query.category = category.value
  if (tag.value) query.tag = tag.value
  if (nextPage > 1) query.page = String(nextPage)
  router.push({ path: '/archive', query })
}

function searchArticles() {
  updateRoute(1)
}

function clearFilters() {
  keyword.value = ''
  category.value = ''
  tag.value = ''
  updateRoute(1)
}

function changePage(nextPage) {
  if (nextPage >= 1 && nextPage <= totalPages.value) updateRoute(nextPage)
}

watch(
  () => route.query,
  () => {
    syncFiltersFromRoute()
    loadArticles()
  },
  { deep: true }
)

onMounted(() => {
  syncFiltersFromRoute()
  loadFilterOptions()
  loadArticles()
})
</script>

<template>
  <section class="archive-page content-wide">
    <div class="page-intro">
      <p class="eyebrow">技术文章 / TECHNICAL ARCHIVE</p>
      <h1>我的文章，<br /><em>从第一篇开始。</em></h1>
      <p>这里展示前端、后端、数据库、算法和计算机基础文章。</p>
    </div>

    <form class="archive-toolbar" @submit.prevent="searchArticles">
      <label>
        <Search :size="15" />
        <input v-model="keyword" placeholder="搜索标题、摘要或正文" aria-label="搜索文章" />
      </label>
      <button type="submit">搜索</button>
    </form>

    <div class="archive-filters">
      <label class="archive-filter">
        <span>分类</span>
        <select v-model="category" @change="searchArticles">
          <option value="">全部分类</option>
          <option v-for="item in categories" :key="item.id" :value="item.name">{{ item.name }}</option>
        </select>
      </label>
      <label class="archive-filter">
        <span>标签</span>
        <select v-model="tag" @change="searchArticles">
          <option value="">全部标签</option>
          <option v-for="item in tags" :key="item.id" :value="item.name">{{ item.name }}</option>
        </select>
      </label>
      <button v-if="keyword || category || tag" class="filter-reset" type="button" @click="clearFilters">
        <X :size="14" /> 清除筛选
      </button>
    </div>

    <div v-if="loading" class="empty-state">正在加载文章...</div>
    <div v-else-if="status" class="empty-state">
      <strong>文章暂时无法加载</strong>
      <span>{{ status }}</span>
      <button class="text-link" @click="loadArticles">重新加载</button>
    </div>
    <div v-else-if="!articles.length" class="empty-state empty-article-state">
      <FileText :size="26" />
      <strong>没有找到文章</strong>
      <span>试试更换关键词或清除筛选条件。</span>
      <button v-if="keyword || category || tag" class="button button-primary" @click="clearFilters">查看全部文章</button>
      <RouterLink v-else class="button button-primary" to="/studio">创建第一篇文章 <ArrowUpRight :size="15" /></RouterLink>
    </div>
    <div v-else class="article-list">
      <RouterLink v-for="article in articles" :key="article.id" class="article-row" :to="`/article/${article.id}`">
        <span class="article-index">{{ String(article.id).padStart(2, '0') }}</span>
        <span class="article-body">
          <span class="article-kicker">{{ article.category || '技术笔记' }} / {{ article.author }}</span>
          <h3>{{ article.title }}</h3>
          <p>{{ article.summary || '点击阅读完整文章。' }}</p>
        </span>
        <ArrowUpRight class="article-arrow" :size="17" />
      </RouterLink>

      <div v-if="totalPages > 1" class="archive-pagination">
        <button class="icon-button" :disabled="!canGoPrevious" title="上一页" @click="changePage(page - 1)">
          <ArrowLeft :size="16" />
        </button>
        <span>第 {{ page }} / {{ totalPages }} 页，共 {{ total }} 篇</span>
        <button class="icon-button" :disabled="!canGoNext" title="下一页" @click="changePage(page + 1)">
          <ArrowRight :size="16" />
        </button>
      </div>
    </div>
  </section>
</template>
