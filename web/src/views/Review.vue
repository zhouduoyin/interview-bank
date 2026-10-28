<template>
  <div class="page review-page">
    <div v-if="loading" v-loading="true" class="review-loading" />

    <template v-else-if="finished">
      <div class="review-done card">
        <div class="done-emoji">🎯</div>
        <h2>本轮记忆训练完成</h2>
        <p class="muted">回想并核对了 {{ doneCount }} 道题，记忆正在被加固。</p>
        <div class="done-grid">
          <div v-for="(r, key) in RATING" :key="key" class="done-item" :style="{ color: r.color, background: r.bg }">
            <b>{{ counts[key] }}</b>
            <span>{{ r.label }}</span>
          </div>
        </div>
        <div class="done-actions">
          <el-button type="primary" @click="restart">再来一轮</el-button>
          <el-button @click="$router.push('/')">回到今日</el-button>
        </div>
        <p class="done-tip">下一批训练会按间隔回想自动安排，明天见。</p>
      </div>
    </template>

    <template v-else-if="!queue.length">
      <EmptyState
        title="今天没有需要加固的记忆"
        description="所有题目都还在记忆保鲜期内。可以去题库补充新题，或者提前训练收藏的题。"
      >
        <template #action>
          <el-button type="primary" @click="$router.push('/questions/new')">记录一道新题</el-button>
          <el-button @click="$router.push('/questions?starred=1')">看看收藏的题</el-button>
        </template>
      </EmptyState>
    </template>

    <template v-else>
      <div class="review-top">
        <div class="review-progress-text">
          <span>今日训练</span>
          <b>{{ index + 1 }}</b>
          <span class="muted">/ {{ queue.length }}</span>
        </div>
        <div class="review-progress">
          <div class="review-progress-fill" :style="{ width: `${((index + 1) / queue.length) * 100}%` }" />
        </div>
        <div class="review-counts">
          <span><i style="background: var(--primary)" />{{ counts.FORGOT }}</span>
          <span><i style="background: var(--accent-3)" />{{ counts.HARD }}</span>
          <span><i style="background: var(--accent-4)" />{{ counts.GOOD }}</span>
          <span><i style="background: var(--accent-2)" />{{ counts.EASY }}</span>
        </div>
      </div>

      <transition name="card-swap" mode="out-in">
        <article :key="current.id" class="review-card-main card">
          <div class="review-card-head">
            <span class="cat-pill" :style="catStyle">
              <i class="dot" :style="{ background: current.category?.color || 'var(--text-3)' }" />
              {{ current.category?.name || '未分类' }}
            </span>
            <span class="mini-pill" :style="diffStyle">{{ diffLabel }}</span>
            <span v-if="current.reviewCount === 0" class="mini-pill" style="color: #7c3aed; background: #f3e8ff"
              >第一次见</span
            >
            <span class="spacer" />
            <span class="muted small">已回想 {{ current.reviewCount }} 次</span>
          </div>

          <h1 class="review-question-title">{{ current.title }}</h1>
          <div v-if="current.content" class="review-question-content">
            <MarkdownView :content="current.content" />
          </div>

          <div v-if="current.tags?.length" class="review-tags">
            <span v-for="t in current.tags" :key="t" class="tag-chip">#{{ t }}</span>
          </div>

          <div v-if="!revealed" class="reveal-area">
            <el-button type="primary" size="large" class="reveal-btn" @click="reveal">
              <el-icon><View /></el-icon>
              核对答案
            </el-button>
            <p class="key-hint">先在心里过一遍，再按 <kbd>空格</kbd> 核对</p>
          </div>

          <div v-else class="answer-area">
            <div class="answer-divider"><span>参考答案</span></div>
            <MarkdownView v-if="current.answer" :content="current.answer" />
            <p v-else class="muted">
              这道题还没有写参考答案，<router-link class="text-link" :to="`/questions/${current.id}/edit`"
                >去补充</router-link
              >
            </p>

            <div class="rating-title">刚才回忆得怎么样？</div>
            <div class="rating-grid four">
              <button
                v-for="(r, key) in RATING"
                :key="key"
                type="button"
                class="rating-btn big"
                :style="{ color: r.color, borderColor: `${r.color}44`, background: r.bg }"
                @click="rate(key)"
              >
                <span class="rating-key">{{ r.key }}</span>
                <b>{{ r.label }}</b>
                <small>{{ r.hint }}</small>
              </button>
            </div>
            <p class="key-hint">也可以按 <kbd>1</kbd> <kbd>2</kbd> <kbd>3</kbd> <kbd>4</kbd> 评价</p>
          </div>
        </article>
      </transition>

      <div class="review-bottom">
        <el-button text @click="skip">
          这题先跳过
          <el-icon class="el-icon--right"><Right /></el-icon>
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { View, Right } from '@element-plus/icons-vue'
import MarkdownView from '@/components/MarkdownView.vue'
import EmptyState from '@/components/EmptyState.vue'
import { api } from '@/api'
import { refreshDue, refreshMeta } from '@/store/app'
import { DIFFICULTY, RATING } from '@/utils/dict'

const loading = ref(true)
const queue = ref([])
const index = ref(0)
const revealed = ref(false)
const finished = ref(false)
const doneCount = ref(0)
const counts = reactive({ FORGOT: 0, HARD: 0, GOOD: 0, EASY: 0 })

const current = computed(() => queue.value[index.value] || null)
const catStyle = computed(() => {
  const color = current.value?.category?.color || 'var(--text-3)'
  return {
    color: `color-mix(in srgb, ${color} 72%, var(--text))`,
    background: `color-mix(in srgb, ${color} 12%, transparent)`
  }
})
const diffStyle = computed(() => {
  const d = DIFFICULTY[current.value?.difficulty] || DIFFICULTY.MEDIUM
  return { color: d.color, background: d.bg }
})
const diffLabel = computed(() => (DIFFICULTY[current.value?.difficulty] || DIFFICULTY.MEDIUM).label)

async function load() {
  loading.value = true
  finished.value = false
  index.value = 0
  revealed.value = false
  doneCount.value = 0
  Object.keys(counts).forEach((k) => (counts[k] = 0))
  try {
    const data = await api.review.queue(30)
    queue.value = data.items || []
  } finally {
    loading.value = false
  }
}

function reveal() {
  revealed.value = true
}

function next() {
  revealed.value = false
  if (index.value + 1 >= queue.value.length) {
    finished.value = true
    refreshMeta()
    refreshDue()
    return
  }
  index.value += 1
}

function skip() {
  if (index.value + 1 >= queue.value.length) {
    finished.value = true
    return
  }
  index.value += 1
  revealed.value = false
}

async function rate(rating) {
  if (!current.value) return
  try {
    await api.questions.review(current.value.id, rating)
    counts[rating] += 1
    doneCount.value += 1
    next()
  } catch (e) {
    /* ignore */
  }
}

function restart() {
  load()
}

function onKey(event) {
  if (finished.value || loading.value || !current.value) return
  const tag = (event.target?.tagName || '').toLowerCase()
  if (tag === 'input' || tag === 'textarea') return
  if (!revealed.value && (event.code === 'Space' || event.key === 'Enter')) {
    event.preventDefault()
    reveal()
    return
  }
  if (revealed.value && ['1', '2', '3', '4'].includes(event.key)) {
    const map = { 1: 'FORGOT', 2: 'HARD', 3: 'GOOD', 4: 'EASY' }
    rate(map[event.key])
  }
}

onMounted(() => {
  load()
  window.addEventListener('keydown', onKey)
})
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))
</script>
