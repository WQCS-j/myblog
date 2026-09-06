<script setup>
import { ref } from 'vue'
import { ArrowLeft, LockKeyhole } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
import { requestApi } from '../services/api'
const password=ref('');const content=ref(null);const status=ref('');const loading=ref(false)
async function accessPrivateContent(){if(!password.value)return status.value='请输入访问密码';loading.value=true;status.value='';try{content.value=(await requestApi('/private-content',{method:'POST',body:JSON.stringify({password:password.value})})).data}catch(error){status.value=error.message}finally{loading.value=false}}
</script>
<template><section class="content-narrow private-page"><RouterLink class="back-link" to="/"><ArrowLeft :size="16"/> 返回首页</RouterLink><div class="page-intro"><p class="eyebrow">秘密基地 / PRIVATE SPACE</p><h1>只对熟悉的人<br/><em>打开这一页。</em></h1><p>内容不会写入前端静态文件，只有验证通过后才会从服务器读取。</p></div><div v-if="!content" class="private-panel"><LockKeyhole :size="24"/><input v-model="password" type="password" placeholder="输入访问密码" @keyup.enter="accessPrivateContent"/><button class="button button-primary" :disabled="loading" @click="accessPrivateContent">{{loading?'验证中...':'验证身份'}}</button><p class="auth-status">{{status}}</p></div><article v-else class="prose"><h2>{{content.title}}</h2><p>{{content.content}}</p></article></section></template>
