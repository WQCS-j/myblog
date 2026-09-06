<script setup>
import { onMounted, ref } from 'vue'
import { ArrowLeft, ChevronDown } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
import { requestApi } from '../services/api'
const items=ref([]);const openId=ref(null);const loading=ref(true);const status=ref('')
onMounted(async()=>{try{items.value=(await requestApi('/timeline')).data}catch(error){status.value=error.message}finally{loading.value=false}})
</script>
<template><section class="content-narrow content-wide"><RouterLink class="back-link" to="/"><ArrowLeft :size="16" /> 返回首页</RouterLink><div class="page-intro"><p class="eyebrow">个人大事记 / TIMELINE</p><h1>一路走来的<br /><em>重要节点。</em></h1></div><div v-if="loading" class="empty-state">正在加载...</div><div v-else-if="status" class="empty-state">{{status}}</div><div v-else-if="!items.length" class="empty-state">暂时还没有大事记内容。</div><div v-else class="timeline-list"><article v-for="item in items" :key="item.id" class="timeline-item"><button @click="openId=openId===item.id?null:item.id"><span>{{item.eventDate}}</span><strong>{{item.title}}</strong><ChevronDown :size="16" /></button><p v-if="openId===item.id">{{item.content}}</p></article></div></section></template>
