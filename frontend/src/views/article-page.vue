<script setup>
import { ArrowLeft, CloudRain } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
</script>
<template><section class="content-narrow empty-article-page"><RouterLink class="back-link" to="/archive"><ArrowLeft :size="16" /> 返回文章归档</RouterLink><div class="empty-state empty-article-state"><CloudRain :size="26" /><strong>这篇文章还没有内容</strong><span>文章发布后，正文、目录、阅读进度和评论功能会显示在这里。</span><RouterLink class="button button-primary" to="/studio">进入作者后台</RouterLink></div></section></template>
