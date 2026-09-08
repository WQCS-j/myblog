<script setup>
import { computed, onMounted, ref } from 'vue'
import { BarChart3, FilePenLine, LayoutDashboard, Plus, Save, Trash2, Upload } from 'lucide-vue-next'
import { RouterLink, useRouter } from 'vue-router'
import { createDraft, deleteDraft as requestDeleteDraft, getDrafts, publishDraft, requestApi, updateDraft } from '../services/api'

const router = useRouter()
const drafts = ref([])
const categories = ref([])
const selected = ref(null)
const saved = ref(true)
const loading = ref(true)
const saving = ref(false)
const status = ref('')
const searchKeyword = ref('')
const canPublish = computed(() => {
  const draft = selected.value
  return Boolean(draft && (draft.title || '').trim().length >= 2 && (draft.content || '').trim())
})

function selectDraft(draft) {
  if (!saved.value && selected.value?.id !== draft.id && !window.confirm('当前草稿尚未保存，确定切换吗？')) return
  selected.value = { ...draft }
  saved.value = true
  status.value = ''
}

async function loadDrafts() {
  loading.value = true
  status.value = ''

  try {
    const result = await getDrafts(searchKeyword.value)
    drafts.value = result.data || []
    const current = selected.value && drafts.value.find(draft => draft.id === selected.value.id)
    if (current) selected.value = { ...current }
    else selected.value = drafts.value[0] ? { ...drafts.value[0] } : null
    saved.value = true
  } catch (error) {
    status.value = error.message
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const result = await requestApi('/categories')
    categories.value = result.data || []
  } catch {
    categories.value = []
  }
}

async function createNewDraft() {
  if (!saved.value && !window.confirm('当前草稿尚未保存，确定新建草稿吗？')) return

  try {
    const result = await createDraft()
    drafts.value.unshift(result.data)
    selected.value = { ...result.data }
    saved.value = true
    status.value = '草稿已创建'
  } catch (error) {
    status.value = error.message
  }
}

function markChanged() {
  saved.value = false
  status.value = ''
}

async function saveDraft() {
  if (!selected.value) return
  saving.value = true

  try {
    const result = await updateDraft(selected.value.id, selected.value)
    selected.value = { ...result.data }
    drafts.value = drafts.value.map(draft => draft.id === result.data.id ? result.data : draft)
    saved.value = true
    status.value = '草稿已保存'
  } catch (error) {
    status.value = error.message
  } finally {
    saving.value = false
  }
}

async function removeDraft() {
  if (!selected.value || !window.confirm('确定删除这篇草稿吗？')) return

  try {
    await requestDeleteDraft(selected.value.id)
    status.value = '草稿已删除'
    selected.value = null
    await loadDrafts()
  } catch (error) {
    status.value = error.message
  }
}

async function publishSelected() {
  if (!canPublish.value) {
    status.value = '请先填写至少 2 个字的标题和正文内容'
    return
  }
  if (!window.confirm('确定发布这篇文章吗？')) return

  try {
    const result = await publishDraft(selected.value.id)
    saved.value = true
    await loadDrafts()
    const articleId = result.data?.articleId ?? result.data?.id
    if (articleId) {
      router.push(`/article/${articleId}`)
      return
    }

    // Java compatibility fallback: the temporary { slug } response cannot
    // guarantee that the detail endpoint accepts a slug instead of an ID.
    status.value = '文章已发布，正在前往归档查看'
    router.push('/archive')
  } catch (error) {
    status.value = error.message
  }
}

onMounted(() => {
  loadDrafts()
  loadCategories()
})
</script>

<template>
  <section class="studio content-wide">
    <div class="studio-top">
      <div>
        <p class="eyebrow">作者后台 / AUTHOR STUDIO</p>
        <h1>作者后台</h1>
      </div>
      <div class="studio-actions">
        <span class="save-state">{{ status || (saved ? '已保存' : '有未保存修改') }}</span>
        <button class="button button-primary" @click="createNewDraft"><Plus :size="16" /> 新建草稿</button>
      </div>
    </div>

    <div class="studio-layout">
       <aside class="studio-nav">
        <RouterLink to="/studio/content"><FilePenLine :size="16" /> Content</RouterLink>
        <a class="active"><LayoutDashboard :size="16" /> 概览</a>
        <a><FilePenLine :size="16" /> 草稿箱 <b>{{ drafts.length }}</b></a>
        <RouterLink to="/analytics"><BarChart3 :size="16" /> 数据分析</RouterLink>
      </aside>

      <div class="studio-main">
        <div class="draft-list">
          <div class="studio-section-title">
            <h2>我的草稿</h2>
            <span>仅自己可见</span>
          </div>
          <form class="search-box" @submit.prevent="loadDrafts">
            <input v-model="searchKeyword" placeholder="搜索草稿" aria-label="搜索草稿" />
          </form>
          <div v-if="loading" class="empty-state">正在加载草稿...</div>
          <template v-else>
            <button
              v-for="draft in drafts"
              :key="draft.id"
              class="draft-item"
              :class="{ selected: selected?.id === draft.id }"
              @click="selectDraft(draft)"
            >
              <span class="draft-status"></span>
              <span>
                <strong>{{ draft.title || '未命名草稿' }}</strong>
                <small>{{ new Date(draft.updatedAt).toLocaleString() }}</small>
              </span>
            </button>
            <div v-if="!drafts.length" class="empty-state">还没有草稿，开始写下第一篇吧。</div>
          </template>
        </div>

        <div v-if="selected" class="editor-panel">
          <input v-model="selected.title" class="editor-title" placeholder="文章标题" @input="markChanged" />
          <div class="editor-meta">
            <label>
              <span>分类</span>
              <select v-model="selected.categoryId" @change="markChanged">
                <option :value="null">未分类</option>
                <option v-for="item in categories" :key="item.id" :value="item.id">{{ item.name }}</option>
              </select>
            </label>
            <span>{{ selected.content?.length || 0 }} 字符</span>
          </div>
          <textarea v-model="selected.summary" class="editor-summary" maxlength="2000" placeholder="文章摘要（可选）" @input="markChanged"></textarea>
          <textarea v-model="selected.content" class="editor-text" placeholder="从这里开始写 Markdown 内容..." @input="markChanged"></textarea>
          <div class="editor-footer">
            <span>发布检查：{{ selected.title.trim().length < 2 ? '标题至少需要 2 个字' : !selected.content.trim() ? '正文不能为空' : '基础检查已通过' }}</span>
            <div>
              <button class="icon-button danger" title="删除草稿" @click="removeDraft"><Trash2 :size="17" /></button>
              <button class="button" :disabled="!canPublish" @click="publishSelected"><Upload :size="16" /> 发布</button>
              <button class="button button-primary" :disabled="saving || saved" @click="saveDraft">
                <Save :size="16" /> {{ saving ? '保存中...' : '保存草稿' }}
              </button>
            </div>
          </div>
        </div>
        <div v-else-if="!loading" class="editor-panel empty-state">请选择草稿或新建一篇草稿。</div>
      </div>
    </div>
  </section>
</template>
