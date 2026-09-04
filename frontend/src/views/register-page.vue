<script setup>
import { ref } from 'vue'
import { ArrowLeft, UserPlus } from 'lucide-vue-next'
import { RouterLink, useRouter } from 'vue-router'
import { requestApi } from '../services/api'
const router=useRouter();const form=ref({username:'',email:'',phone:'',password:'',agreementAccepted:false});const status=ref('');const loading=ref(false)
async function registerUser(){if(!form.value.username||!form.value.password)return status.value='请填写用户名和密码';if(form.value.password.length<8)return status.value='密码至少需要 8 位';if(!form.value.agreementAccepted)return status.value='请先同意用户协议';loading.value=true;try{await requestApi('/auth/register',{method:'POST',body:JSON.stringify(form.value)});status.value='注册成功，请登录';setTimeout(()=>router.push('/login'),500)}catch(error){status.value=error.message}finally{loading.value=false}}
</script>
<template><section class="auth-page"><RouterLink class="back-link" to="/login"><ArrowLeft :size="16" /> 返回登录</RouterLink><div class="auth-panel"><div class="auth-icon"><UserPlus :size="22" /></div><p class="eyebrow">创建账号 / REGISTER</p><h1>加入这里。</h1><p>注册后可以留言，也可以申请成为作者。</p><label>用户名<input v-model="form.username" placeholder="3-30 个字符" /></label><label>邮箱<input v-model="form.email" type="email" placeholder="可选" /></label><label>手机号<input v-model="form.phone" placeholder="可选" /></label><label>密码<input v-model="form.password" type="password" placeholder="至少 8 位" /></label><label class="agreement"><input v-model="form.agreementAccepted" type="checkbox" /> 我同意用户协议</label><button class="button button-primary full-button" :disabled="loading" @click="registerUser">{{loading?'注册中...':'注册账号'}}</button><p class="auth-status">{{status}}</p></div></section></template>
