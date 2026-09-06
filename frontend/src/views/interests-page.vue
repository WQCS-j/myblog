<script setup>
import { onMounted, ref, computed } from 'vue'
import { ArrowLeft } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
import { requestApi } from '../services/api'
const items=ref([]);const category=ref('全部');const status=ref('');const loading=ref(true);const categories=computed(()=>['全部',...new Set(items.value.map(item=>item.category))]);const visibleItems=computed(()=>category.value==='全部'?items.value:items.value.filter(item=>item.category===category.value))
onMounted(async()=>{try{items.value=(await requestApi('/interests')).data}catch(error){status.value=error.message}finally{loading.value=false}})
</script>
<template><section class="content-wide"><RouterLink class="back-link" to="/"><ArrowLeft :size="16" /> 返回首页</RouterLink><div class="page-intro"><p class="eyebrow">个人兴趣 / INTERESTS</p><h1>技术之外，<br /><em>也有喜欢的事。</em></h1></div><div class="tag-cloud"><button v-for="item in categories" :key="item" class="tag-cloud a" @click="category=item">{{item}}</button></div><div v-if="loading" class="empty-state">正在加载...</div><div v-else-if="status" class="empty-state">{{status}}</div><div v-else-if="!visibleItems.length" class="empty-state">暂时还没有兴趣内容。</div><div v-else class="about-columns"><article v-for="item in visibleItems" :key="item.id"><p class="eyebrow">{{item.category}}</p><h2>{{item.title}}</h2><p>{{item.summary}}</p><a v-if="item.link" :href="item.link" target="_blank">了解更多 ↗</a></article></div></section></template>
