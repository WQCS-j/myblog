<script setup>
import { ref } from 'vue'
const visible=ref(true)
function toggleButterfly(){visible.value=!visible.value}
</script>
<template><button v-if="visible" class="global-butterfly" title="关闭蝴蝶装饰" aria-label="关闭蝴蝶装饰" @click="toggleButterfly"><span class="butterfly-wing butterfly-wing-left"></span><span class="butterfly-body"></span><span class="butterfly-wing butterfly-wing-right"></span></button><button v-else class="butterfly-reopen" title="显示蝴蝶装饰" aria-label="显示蝴蝶装饰" @click="toggleButterfly">蝶</button></template>
