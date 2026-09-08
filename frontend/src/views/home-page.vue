<script setup>
import { ArrowDown, ArrowUpRight, FileText, Hash, Search } from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import heroImage from '../assets/green-water-hero.jpg'
import { requestApi } from '../services/api'

const router = useRouter()
const searchKeyword = ref('')
const articles = ref([])
const totalArticles = ref(0)
const loadingArticles = ref(true)
const articleStatus = ref('')
const technologyTags = ['Vue 3', 'Java', 'Spring Boot', 'MySQL', 'Git', 'JWT']
const articleCountLabel = computed(() => `${String(totalArticles.value).padStart(2, '0')} ARTICLES`)

async function loadLatestArticles() {
  loadingArticles.value = true
  articleStatus.value = ''

  try {
    const result = await requestApi('/articles?limit=3')
    articles.value = result.data.items || []
    totalArticles.value = result.data.total || 0
  } catch (error) {
    articleStatus.value = error.message
  } finally {
    loadingArticles.value = false
  }
}

function searchArticles() {
  const keyword = searchKeyword.value.trim()
  router.push({ path: '/archive', query: keyword ? { keyword } : {} })
}

onMounted(loadLatestArticles)
</script>

<template>
  <div class="home-page">
    <section class="home-hero" :style="{ backgroundImage: `url(${heroImage})` }">
      <div class="home-hero-overlay"></div>
      <span class="hero-butterfly hero-butterfly-one" aria-hidden="true"></span>
      <span class="hero-butterfly hero-butterfly-two" aria-hidden="true"></span>
      <div class="home-hero-content">
        <p class="hero-kicker">COMPUTER SCIENCE STUDENT / PERSONAL BLOG</p>
        <p class="hero-wish">祝你<br /><em>大获全胜</em></p>
        <p class="hero-subtitle">把正在学习的技术，认真写成自己的作品。</p>
        <div class="hero-links">
          <RouterLink class="hero-link" to="/about">认识博主 <ArrowUpRight :size="16" /></RouterLink>
          <RouterLink class="hero-link hero-link-muted" to="/archive">查看技术文章 <ArrowDown :size="16" /></RouterLink>
        </div>
      </div>
      <div class="home-hero-caption"><span>MYBLOG / GREEN SEASON</span><span>WELCOME, VISITOR</span></div>
    </section>

    <section class="home-intro content-grid section-space">
      <div class="home-intro-copy">
        <p class="eyebrow">关于这个博客 / ABOUT THIS BLOG</p>
        <h1>你好，我是一名正在成长的计算机专业学生。</h1>
        <p>这里记录前端、后端、数据库、算法学习，以及每一个正在完成的软件项目。希望这些文字能让思路留下来，也让下一次出发更清楚。</p>
        <RouterLink class="button button-primary" to="/about">查看我的技术方向 <ArrowUpRight :size="15" /></RouterLink>
      </div>
      <div class="home-focus">
        <p class="eyebrow">目前专注 / CURRENT FOCUS</p>
        <strong>全栈博客平台</strong>
        <span>Vue 3 / Java / MySQL</span>
        <p>从界面、接口到数据持久化，边学习边把完整项目做出来。</p>
      </div>
    </section>

    <section class="content-grid section-space home-articles">
      <div class="main-column">
        <div class="section-heading">
          <div>
            <p class="eyebrow">TECHNICAL JOURNAL</p>
            <h2>我的技术文章</h2>
          </div>
          <span class="count">{{ articleCountLabel }}</span>
        </div>

        <div v-if="loadingArticles" class="empty-state">正在加载文章...</div>
        <div v-else-if="articleStatus" class="empty-state">
          <strong>文章暂时无法加载</strong>
          <span>{{ articleStatus }}</span>
          <button class="text-link" @click="loadLatestArticles">重新加载</button>
        </div>
        <div v-else-if="!articles.length" class="empty-state empty-article-state">
          <FileText :size="26" />
          <strong>还没有发布文章</strong>
          <span>等我写下第一篇关于代码、课程或项目的记录。</span>
          <RouterLink class="button button-primary" to="/studio">进入作者后台 <ArrowUpRight :size="15" /></RouterLink>
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
          <RouterLink class="text-link" to="/archive">查看全部文章 <ArrowUpRight :size="15" /></RouterLink>
        </div>
      </div>

      <aside class="side-column">
        <form class="search-box" @submit.prevent="searchArticles">
          <Search :size="17" />
          <input v-model="searchKeyword" placeholder="搜索我的文章..." aria-label="搜索文章" />
        </form>
        <div class="side-block">
          <p class="eyebrow">技术栈 / TECH STACK</p>
          <div class="tag-cloud">
            <RouterLink v-for="tag in technologyTags" :key="tag" to="/archive"><Hash :size="13" />{{ tag }}</RouterLink>
          </div>
        </div>
        <div class="side-block home-note">
          <p class="eyebrow">小小记录 / A SMALL NOTE</p>
          <p>保持好奇，保持动手。每一个小项目，都是理解世界的一种方式。</p>
        </div>
      </aside>
    </section>
  </div>
</template>
