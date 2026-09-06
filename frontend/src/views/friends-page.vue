<script setup>
import { onMounted, ref } from 'vue'
import { ArrowLeft, ExternalLink } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
import { requestApi } from '../services/api'
const links=ref([]);const loading=ref(true);const status=ref('')
onMounted(async()=>{try{links.value=(await requestApi('/friend-links')).data}catch(error){status.value=error.message}finally{loading.value=false}})
</script>
<template><section class="content-wide"><RouterLink class="back-link" to="/"><ArrowLeft :size="16" /> 返回首页</RouterLink><div class="page-intro"><p class="eyebrow">友情链接 / FRIEND LINKS</p><h1>认识一些<br /><em>有趣的网站。</em></h1></div><div v-if="loading" class="empty-state">正在加载...</div><div v-else-if="status" class="empty-state">{{status}}</div><div v-else-if="!links.length" class="empty-state">暂时还没有友情链接。</div><div v-else class="about-columns"><a v-for="link in links" :key="link.id" :href="link.url" target="_blank"><p class="eyebrow">{{link.name}} <ExternalLink :size="13" /></p><p>{{link.description}}</p></a></div></section></template>
