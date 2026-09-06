<script setup>
import { onMounted, ref } from 'vue'
import { BarChart3 } from 'lucide-vue-next'
import { requestApi } from '../services/api'
const articleId=ref('');const data=ref(null);const status=ref('');const loading=ref(false)
async function loadHeatmap(){if(!articleId.value)return status.value='请输入文章编号';loading.value=true;try{data.value=(await requestApi(`/reading-heatmap?articleId=${articleId.value}`)).data}catch(error){status.value=error.message}finally{loading.value=false}}
onMounted(()=>{})
</script>
<template><section class="content-wide analytics-page"><div class="page-intro"><p class="eyebrow">数据分析 / READING ANALYTICS</p><h1>看看读者<br/><em>如何阅读。</em></h1><p>只有作者和管理员可以查看阅读统计。</p></div><div class="archive-toolbar"><label>文章编号 <input v-model="articleId" type="number" placeholder="例如 1" /></label><button @click="loadHeatmap">{{loading?'加载中...':'查询数据'}}</button></div><div v-if="status" class="empty-state">{{status}}</div><div v-if="data" class="analytics-summary"><div><span>阅读人数</span><strong>{{data.summary.readers||0}}</strong></div><div><span>平均阅读时长</span><strong>{{data.summary.averageDuration||0}} 秒</strong></div><div><span>平均滚动深度</span><strong>{{data.summary.averageScroll||0}}%</strong></div></div><div v-if="data&&!data.sections.length" class="empty-state"><BarChart3 :size="24"/>暂时没有分段停留数据。</div></section></template>
