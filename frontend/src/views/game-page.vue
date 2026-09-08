<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { ArrowLeft, Pause, Play, RotateCcw, Sparkles, X } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'

const totalTime = 30
const target = 7
const gameState = ref('idle')
const score = ref(0)
const timeLeft = ref(totalTime)
const targetPosition = ref({ left: 50, top: 50 })
let timerId = null

const message = computed(() => {
  if (gameState.value === 'won') return '完成！今天的灵感已经收集齐了。'
  if (gameState.value === 'expired') return '时间到了，再试一次吧。'
  if (gameState.value === 'paused') return '游戏已暂停，准备好后继续。'
  if (gameState.value === 'exited') return '本轮游戏已退出。'
  if (gameState.value === 'idle') return '在倒计时结束前，收集 7 个灵感。'
  return '点击唯一的闪光目标，继续寻找下一处灵感。'
})

function moveTarget() {
  targetPosition.value = {
    left: 12 + Math.round(Math.random() * 76),
    top: 18 + Math.round(Math.random() * 63)
  }
}

function stopTimer() {
  if (timerId) {
    window.clearInterval(timerId)
    timerId = null
  }
}

function startTimer() {
  stopTimer()
  timerId = window.setInterval(() => {
    if (gameState.value !== 'playing') return
    timeLeft.value -= 1
    if (timeLeft.value <= 0) {
      timeLeft.value = 0
      gameState.value = 'expired'
      stopTimer()
    }
  }, 1000)
}

function startGame() {
  score.value = 0
  timeLeft.value = totalTime
  gameState.value = 'playing'
  moveTarget()
  startTimer()
}

function collect() {
  if (gameState.value !== 'playing') return
  score.value += 1
  if (score.value >= target) {
    gameState.value = 'won'
    stopTimer()
    return
  }
  moveTarget()
}

function pauseGame() {
  if (gameState.value === 'playing') gameState.value = 'paused'
}

function resumeGame() {
  if (gameState.value === 'paused') gameState.value = 'playing'
}

function exitGame() {
  stopTimer()
  gameState.value = 'exited'
}

onBeforeUnmount(stopTimer)
</script>

<template>
  <section class="game-page content-narrow">
    <RouterLink class="back-link" to="/"><ArrowLeft :size="16" /> 回到首页</RouterLink>
    <div class="game-header">
      <p class="eyebrow">小游戏 / A SMALL GAME</p>
      <h1>灵感捕手</h1>
      <p>{{ message }}</p>
    </div>

    <div class="game-toolbar">
      <div class="game-score">SCORE <strong>{{ score }} / {{ target }}</strong></div>
      <div class="game-time">TIME <strong>{{ String(timeLeft).padStart(2, '0') }}s</strong></div>
      <div class="game-actions">
        <button v-if="gameState === 'playing'" class="icon-button" title="暂停游戏" @click="pauseGame"><Pause :size="17" /></button>
        <button v-else-if="gameState === 'paused'" class="icon-button" title="继续游戏" @click="resumeGame"><Play :size="17" /></button>
        <button v-if="['playing', 'paused'].includes(gameState)" class="icon-button" title="退出游戏" @click="exitGame"><X :size="17" /></button>
        <button v-if="['playing', 'paused'].includes(gameState)" class="icon-button" title="重新开始" @click="startGame"><RotateCcw :size="17" /></button>
      </div>
    </div>

    <div class="game-board" :class="{ playing: gameState === 'playing' }">
      <button
        v-if="gameState === 'playing'"
        class="spark"
        :style="{ left: `${targetPosition.left}%`, top: `${targetPosition.top}%` }"
        aria-label="收集灵感"
        @click="collect"
      >
        <Sparkles :size="24" />
      </button>
      <div v-if="gameState !== 'playing'" class="game-overlay">
        <div class="win">
          <span v-if="gameState === 'won'">完成！</span>
          <span v-else-if="gameState === 'expired'">时间到</span>
          <span v-else-if="gameState === 'paused'">已暂停</span>
          <span v-else-if="gameState === 'exited'">已退出</span>
          <span v-else>准备开始</span>
          <small>{{ message }}</small>
        </div>
        <button class="button button-primary" @click="startGame">
          <RotateCcw v-if="['won', 'expired', 'exited'].includes(gameState)" :size="16" />
          <Play v-else :size="16" />
          {{ gameState === 'idle' || gameState === 'paused' ? '开始游戏' : '再玩一次' }}
        </button>
      </div>
    </div>
  </section>
</template>
