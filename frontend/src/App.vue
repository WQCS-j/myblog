<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { BookOpen, Menu, Moon, Search, Sun, UserRound, X } from 'lucide-vue-next'

const route = useRoute()
const menuOpen = ref(false)
const theme = ref(localStorage.getItem('myblog-theme') || 'light')
const userRole = ref(localStorage.getItem('myblog-user-role') || '')
const isDark = computed(() => theme.value === 'dark')
const isAuthor = computed(() => userRole.value === 'author' || userRole.value === 'admin')
function switchPageTheme() {
  theme.value = isDark.value ? 'light' : 'dark'
  localStorage.setItem('myblog-theme', theme.value)
  document.documentElement.dataset.theme = theme.value
}
document.documentElement.dataset.theme = theme.value
function syncUserRole() {
  userRole.value = localStorage.getItem('myblog-user-role') || ''
}
onMounted(() => window.addEventListener('myblog-auth-changed', syncUserRole))
onUnmounted(() => window.removeEventListener('myblog-auth-changed', syncUserRole))
</script>

<template>
  <div class="site-shell">
    <header class="topbar">
      <RouterLink class="brand" to="/" @click="menuOpen = false"><span class="brand-mark">折</span><span>折页</span></RouterLink>
      <nav :class="{ open: menuOpen }">
        <RouterLink to="/" @click="menuOpen = false">首页</RouterLink>
        <RouterLink to="/archive" @click="menuOpen = false">归档</RouterLink>
        <RouterLink to="/messages" @click="menuOpen = false">留言</RouterLink>
        <RouterLink to="/about" @click="menuOpen = false">关于我</RouterLink>
        <RouterLink to="/game" @click="menuOpen = false">小游戏</RouterLink>
        <RouterLink v-if="isAuthor" to="/studio" @click="menuOpen = false">作者后台</RouterLink>
      </nav>
      <div class="top-actions">
        <button class="icon-button" title="切换主题" @click="switchPageTheme"><Sun v-if="isDark" :size="18" /><Moon v-else :size="18" /></button>
        <RouterLink class="icon-button" title="登录" to="/login"><UserRound :size="18" /></RouterLink>
        <button class="icon-button mobile-menu" title="打开菜单" @click="menuOpen = !menuOpen"><X v-if="menuOpen" :size="20" /><Menu v-else :size="20" /></button>
      </div>
    </header>
    <main class="page-transition"><RouterView /></main>
    <footer class="footer"><span>折页 / ZHEYE</span><span>记录微小，保持好奇。© 2026</span><span>用心构建 / Built with patience.</span></footer>
  </div>
</template>


