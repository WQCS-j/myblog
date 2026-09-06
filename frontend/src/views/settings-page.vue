<script setup>
import { ref } from 'vue'
import { ArrowLeft, KeyRound } from 'lucide-vue-next'
import { RouterLink, useRouter } from 'vue-router'
import { requestApi } from '../services/api'
const router=useRouter();const oldPassword=ref('');const newPassword=ref('');const status=ref('');const loading=ref(false)
async function changePassword(){if(newPassword.value.length<8)return status.value='新密码至少需要 8 位';loading.value=true;try{await requestApi('/auth/change-password',{method:'POST',body:JSON.stringify({oldPassword:oldPassword.value,newPassword:newPassword.value})});localStorage.clear();status.value='密码已修改，请重新登录';setTimeout(()=>router.push('/login'),700)}catch(error){status.value=error.message}finally{loading.value=false}}
</script>
<template><section class="content-narrow auth-page"><RouterLink class="back-link" to="/"><ArrowLeft :size="16"/> 返回首页</RouterLink><div class="auth-panel"><div class="auth-icon"><KeyRound :size="22"/></div><p class="eyebrow">账号设置 / ACCOUNT SETTINGS</p><h1>修改密码。</h1><p>修改成功后当前登录状态会失效，需要重新登录。</p><label>原密码<input v-model="oldPassword" type="password" /></label><label>新密码<input v-model="newPassword" type="password" placeholder="至少 8 位" /></label><button class="button button-primary full-button" :disabled="loading" @click="changePassword">{{loading?'提交中...':'确认修改'}}</button><p class="auth-status">{{status}}</p></div></section></template>
