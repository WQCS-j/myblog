<script setup>
import { ref, computed } from 'vue'
import { ArrowLeft, RotateCcw, Sparkles } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
const gameState = ref('idle'); const score = ref(0); const target = ref(7); const message = computed(() => gameState.value === 'won' ? '恭喜，你找到了今天的好心情。' : '点击漂浮的纸片，收集 7 个灵感。')
function startGame() { score.value = 0; gameState.value = 'playing' }
function collect() { if (gameState.value !== 'playing') return; score.value++; if (score.value >= target.value) gameState.value = 'won' }
</script>
<template><section class="game-page content-narrow"><RouterLink class="back-link" to="/"><ArrowLeft :size="16" /> 回到首页</RouterLink><div class="game-header"><p class="eyebrow">小游戏 / A SMALL GAME</p><h1>灵感捕手</h1><p>{{ message }}</p></div><div class="game-board" :class="{ playing: gameState === 'playing' }"><button v-for="n in (gameState === 'playing' ? 7 : 0)" :key="n" class="spark" :style="{ left: `${12 + (n * 17) % 76}%`, top: `${16 + (n * 23) % 68}%` }" @click="collect"><Sparkles :size="22" /></button><div v-if="gameState !== 'playing'" class="game-overlay"><div v-if="gameState === 'won'" class="win">完成！<small>收集到 {{ score }} 个灵感</small></div><button class="button button-primary" @click="startGame"><RotateCcw v-if="gameState === 'won'" :size="16" />{{ gameState === 'won' ? '再玩一次' : '开始游戏' }}</button></div><div class="game-score">SCORE <strong>{{ score }} / {{ target }}</strong></div></div></section></template>

