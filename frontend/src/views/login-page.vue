<script setup>
import { ref } from 'vue'
import { ArrowLeft, LockKeyhole } from 'lucide-vue-next'
import { RouterLink, useRouter } from 'vue-router'
import { loginUser as requestLogin } from '../services/api'
const router = useRouter(); const account = ref(''); const password = ref(''); const status = ref(''); const loading = ref(false)
async function loginUser() { if (!account.value || !password.value) { status.value = '请输入账号和密码'; return } loading.value = true; status.value = ''
  try { const result = await requestLogin(account.value.trim(), password.value); localStorage.setItem('myblog-token', result.data.token); localStorage.setItem('myblog-user-role', result.data.user.role); localStorage.setItem('myblog-user', JSON.stringify(result.data.user)); window.dispatchEvent(new Event('myblog-auth-changed')); status.value = '登录成功'; setTimeout(() => router.push(result.data.user.role === 'author' || result.data.user.role === 'admin' ? '/studio' : '/'), 300) } catch (error) { status.value = error.message } finally { loading.value = false } }
</script>
<template><section class="auth-page"><RouterLink class="back-link" to="/"><ArrowLeft :size="16" /> 返回首页</RouterLink><div class="auth-panel"><div class="auth-icon"><LockKeyhole :size="22" /></div><p class="eyebrow">WELCOME BACK</p><h1>回来坐坐。</h1><p>登录后可以参与留言，也可以进入作者后台。</p><label>账号<input v-model="account" placeholder="用户名 / 邮箱 / 手机号" autocomplete="username" /></label><label>密码<input v-model="password" type="password" placeholder="请输入密码" autocomplete="current-password" @keyup.enter="loginUser" /></label><button class="button button-primary full-button" :disabled="loading" @click="loginUser">{{ loading ? '登录中...' : '登录' }}</button><p class="auth-status">{{ status }}</p><small>请使用数据库中已创建的账号登录</small><RouterLink class="text-link" to="/register">还没有账号？立即注册</RouterLink></div></section></template>

