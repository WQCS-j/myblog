<script setup>
import { onMounted, ref } from 'vue'
import { ArrowUpRight, FileText, Search } from 'lucide-vue-next'
import { RouterLink, useRoute } from 'vue-router'
import { requestApi } from '../services/api'
const route=useRoute();const articles=ref([]);const loading=ref(true);const status=ref('');const keyword=ref(String(route.query.keyword||''));const total=ref(0)
async function loadArticles(){loading.value=true;try{const result=await requestApi(`/articles?keyword=${encodeURIComponent(keyword.value)}`);articles.value=result.data.items;total.value=result.data.total}catch(error){status.value=error.message}finally{loading.value=false}}
function searchArticles(){loadArticles()}
onMounted(loadArticles)
</script>
<template><section class="archive-page content-wide"><div class="page-intro"><p class="eyebrow">技术文章 / TECHNICAL ARCHIVE</p><h1>我的文章，<br /><em>从第一篇开始。</em></h1><p>这里展示前端、后端、数据库、算法和计算机基础文章。</p></div><div class="archive-toolbar"><label><Search :size="15" /><input v-model="keyword" placeholder="搜索标题、摘要或正文" @keyup.enter="searchArticles" /></label><button @click="searchArticles">搜索</button></div><div v-if="loading" class="empty-state">正在加载文章...</div><div v-else-if="status" class="empty-state">{{status}}</div><div v-else-if="!articles.length" class="empty-state empty-article-state"><FileText :size="26" /><strong>暂无文章</strong><span>当前项目没有预置文章，你可以从作者后台创建第一篇。</span><RouterLink class="button button-primary" to="/studio">创建第一篇文章 <ArrowUpRight :size="15" /></RouterLink></div><div v-else class="article-list"><RouterLink v-for="article in articles" :key="article.id" class="article-row" :to="`/article/${article.id}`"><span class="article-index">{{String(article.id).padStart(2,'0')}}</span><span class="article-body"><span class="article-kicker">{{article.category||'技术记录'}} · {{article.author}}</span><h3>{{article.title}}</h3><p>{{article.summary}}</p></span><ArrowUpRight class="article-arrow" :size="17" /></RouterLink><p class="count">共找到 {{total}} 篇文章</p></div></section></template>
