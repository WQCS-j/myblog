<script setup>
import { ArrowDown, ArrowUpRight, Hash, Search } from 'lucide-vue-next'
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import heroImage from '../assets/green-water-hero.jpg'

const searchKeyword = ref('')
const technologyTags = ['Vue 3', 'JavaScript', 'Node.js', 'Express', 'MySQL', 'Git']
</script>
<template>
  <div class="home-page">
    <section class="home-hero" :style="{ backgroundImage: `url(${heroImage})` }">
      <div class="home-hero-overlay"></div>
      <div class="home-hero-content">
        <p class="hero-kicker">COMPUTER SCIENCE STUDENT / PERSONAL BLOG</p>
        <p class="hero-wish">祝你<br /><em>大获全胜</em></p>
        <p class="hero-subtitle">把正在学习的技术，认真写成自己的作品。</p>
        <div class="hero-links"><RouterLink class="hero-link" to="/about">认识博主 <ArrowUpRight :size="16" /></RouterLink><RouterLink class="hero-link hero-link-muted" to="/archive">查看技术文章 <ArrowDown :size="16" /></RouterLink></div>
      </div>
      <div class="home-hero-caption"><span>MYBLOG / GREEN SEASON</span><span>WELCOME, VISITOR</span></div>
    </section>
    <section class="home-intro content-grid section-space"><div class="home-intro-copy"><p class="eyebrow">关于这个博客 / ABOUT THIS BLOG</p><h1>你好，我是一个正在成长的计算机专业学生。</h1><p>这里记录前端、后端、数据库、算法学习，以及每一个正在完成的软件项目。希望这些文字能让思路留下来，也让下一次出发更清楚。</p><RouterLink class="button button-primary" to="/about">查看我的技术方向 <ArrowUpRight :size="15" /></RouterLink></div><div class="home-focus"><p class="eyebrow">目前专注 / CURRENT 技术方向 / FOCUS</p><strong>全栈博客平台</strong><span>Vue 3 · Express · MySQL</span><p>从界面、接口到数据持久化，边学习边把完整项目做出来。</p></div></section>
    <section class="content-grid section-space home-articles"><div class="main-column"><div class="section-heading"><div><p class="eyebrow">TECHNICAL JOURNAL</p><h2>我的技术文章</h2></div><span class="count">00 ARTICLES</span></div><div class="empty-state empty-article-state"><strong>还没有发布文章</strong><span>等我写下第一篇关于代码、课程或项目的记录。</span><RouterLink class="button button-primary" to="/studio">进入作者后台 <ArrowUpRight :size="15" /></RouterLink></div></div><aside class="side-column"><div class="search-box"><Search :size="17" /><input v-model="searchKeyword" placeholder="搜索我的文章..." /></div><div class="side-block"><p class="eyebrow">技术栈 / TECH STACK</p><div class="tag-cloud"><RouterLink v-for="tag in technologyTags" :key="tag" to="/archive"><Hash :size="13" />{{ tag }}</RouterLink></div></div><div class="side-block home-note"><p class="eyebrow">小小记录 / A SMALL NOTE</p><p>保持好奇，保持动手。每个小项目，都是理解世界的一种方式。</p></div></aside></section>
  </div>
</template>

