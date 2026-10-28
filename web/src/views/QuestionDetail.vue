<template>
  <div v-loading="loading" class="page detail-page">
    <div class="detail-crumbs">
      <router-link to="/questions" class="back-link">← 题库</router-link>
      <span class="muted">/</span>
      <span class="muted">题目详情</span>
    </div>

    <div v-if="question" class="detail-head">
      <div class="detail-head-main">
        <div class="badge-row">
          <span class="cat-pill" :style="catStyle">
            <i class="dot" :style="{ background: question.category?.color || 'var(--text-3)' }" />
            {{ question.category?.name || '未分类' }}
          </span>
          <span class="mini-pill" :style="diffStyle">{{ diffLabel }}</span>
          <span class="mini-pill" :style="masteryStyle">{{ masteryLabel }}</span>
          <span v-if="isDue" class="due-flag"><i />待复习</span>
          <span v-if="question.starred" class="star-flag">★ 重点</span>
        </div>
        <h1>{{ question.title }}</h1>
        <div class="detail-meta">
          <span v-if="question.sourceCompany">来源：{{ question.sourceCompany }}</span>
          <span v-if="question.sourcePosition">岗位：{{ question.sourcePosition }}</span>
          <span>创建于 {{ fmtDate(question.createdAt) }}</span>
          <span>更新于 {{ fromNow(question.updatedAt) }}</span>
        </div>
        <div v-if="question.tags?.length" class="detail-tags">
          <router-link
            v-for="t in question.tags"
            :key="t"
            :to="{ path: '/questions', query: { tag: t } }"
            class="tag-chip"
          >
            #{{ t }}
          </router-link>
        </div>
      </div>
      <div class="detail-actions">
        <el-button @click="toggleStar">
          <el-icon><StarFilled v-if="question.starred" /><Star v-else /></el-icon>
          {{ question.starred ? '已收藏' : '收藏' }}
        </el-button>
        <el-button @click="$router.push(`/questions/${question.id}/edit`)">
          <el-icon><EditPen /></el-icon>编辑
        </el-button>
        <el-dropdown trigger="click" @command="setMastery">
          <el-button>
            掌握程度<el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="MASTERED">标记为已掌握</el-dropdown-item>
              <el-dropdown-item command="LEARNING">标记为复习中</el-dropdown-item>
              <el-dropdown-item command="NEW">重置为未掌握</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="danger" plain @click="remove">删除</el-button>
      </div>
    </div>

    <div v-if="question" class="detail-grid">
      <div class="detail-main">
        <section class="card">
          <div class="card-head">
            <h3>题目</h3>
            <span class="muted small">Markdown 渲染</span>
          </div>
          <MarkdownView v-if="question.content" :content="question.content" />
          <p v-else class="muted">还没有填写题目描述，<router-link class="text-link" :to="`/questions/${question.id}/edit`">去补充</router-link></p>
        </section>

        <section class="card answer-card">
          <div class="card-head">
            <h3>参考答案</h3>
            <el-button text size="small" @click="showAnswer = !showAnswer">{{ showAnswer ? '先藏起来' : '展开答案' }}</el-button>
          </div>
          <template v-if="showAnswer">
            <MarkdownView v-if="question.answer" :content="question.answer" />
            <p v-else class="muted">还没有参考答案，<router-link class="text-link" :to="`/questions/${question.id}/edit`">去写一份</router-link></p>
          </template>
          <div v-else class="answer-hidden" @click="showAnswer = true">
            <el-icon><View /></el-icon>
            <span>先自己回忆一遍，再点击展开参考答案</span>
          </div>
        </section>

        <section v-if="question.note" class="card">
          <div class="card-head"><h3>我的理解 / 追问备忘</h3></div>
          <MarkdownView :content="question.note" />
        </section>

        <section v-if="interviews.length" class="card">
          <div class="card-head">
            <h3>出现在这些面试里</h3>
            <router-link to="/interviews" class="text-link">全部面试 →</router-link>
          </div>
          <div class="linked-interviews">
            <router-link v-for="it in interviews" :key="it.recordId" :to="`/interviews/${it.recordId}`" class="linked-interview">
              <span class="company-avatar" :style="{ background: colorOf(it.company) }">{{ initials(it.company) }}</span>
              <div>
                <b>{{ it.company }}</b>
                <small>{{ it.round || '面试' }} · {{ fmtDate(it.interviewDate, 'M月D日') }}</small>
                <p v-if="it.note">{{ it.note }}</p>
              </div>
              <span class="mini-pill" :style="resultStyle(it.result)">{{ resultLabel(it.result) }}</span>
            </router-link>
          </div>
        </section>
      </div>

      <aside class="detail-side">
        <section class="card review-card">
          <div class="card-head"><h3>复习状态</h3></div>
          <div class="review-stat-row">
            <div><b>{{ question.reviewCount }}</b><span>复习次数</span></div>
            <div><b>{{ question.intervalDays }}</b><span>间隔天数</span></div>
            <div><b>{{ question.easeFactor?.toFixed?.(2) ?? '2.50' }}</b><span>难度系数</span></div>
          </div>
          <div class="next-review" :class="{ due: isDue }">
            <el-icon><Clock /></el-icon>
            <div>
              <b>{{ dueLabel(question.nextReviewAt, question.reviewCount) }}</b>
              <small>{{ fmtDateTime(question.nextReviewAt) }}</small>
            </div>
          </div>
          <div class="rating-title">现在过一遍？</div>
          <div class="rating-grid">
            <button v-for="(r, key) in RATING" :key="key" type="button" class="rating-btn" :style="{ color: r.color, borderColor: `${r.color}33`, background: r.bg }" @click="rate(key)">
              <b>{{ r.label }}</b>
              <small>{{ r.hint }}</small>
            </button>
          </div>
        </section>

        <section class="card">
          <div class="card-head">
            <h3>复习历史</h3>
            <span class="muted small">{{ logs.length }} 次</span>
          </div>
          <div v-if="logs.length" class="timeline">
            <div v-for="log in logs" :key="log.id" class="timeline-item">
              <i :style="{ background: ratingColor(log.rating) }" />
              <div>
                <b>{{ ratingLabel(log.rating) }}</b>
                <small>{{ fmtDateTime(log.createdAt) }} · 间隔 {{ log.prevInterval }} → {{ log.intervalDays }} 天</small>
              </div>
            </div>
          </div>
          <p v-else class="muted small">还没有复习记录，点击上面的按钮开始第一次。</p>
        </section>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled, EditPen, ArrowDown, Clock, View } from '@element-plus/icons-vue'
import MarkdownView from '@/components/MarkdownView.vue'
import { api } from '@/api'
import { refreshDue, refreshMeta } from '@/store/app'
import { DIFFICULTY, MASTERY, RATING, RESULT } from '@/utils/dict'
import { colorOf, dueLabel, fmtDate, fmtDateTime, fromNow, initials } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const question = ref(null)
const logs = ref([])
const interviews = ref([])
const showAnswer = ref(true)

const isDue = computed(() => {
  if (!question.value) return false
  if (!question.value.nextReviewAt) return question.value.reviewCount === 0
  return new Date(question.value.nextReviewAt).getTime() <= Date.now()
})
const catStyle = computed(() => {
  const color = question.value?.category?.color || 'var(--text-3)'
  return {
    color: `color-mix(in srgb, ${color} 72%, var(--text))`,
    background: `color-mix(in srgb, ${color} 12%, transparent)`
  }
})
const diffStyle = computed(() => {
  const d = DIFFICULTY[question.value?.difficulty] || DIFFICULTY.MEDIUM
  return { color: d.color, background: d.bg }
})
const diffLabel = computed(() => (DIFFICULTY[question.value?.difficulty] || DIFFICULTY.MEDIUM).label)
const masteryStyle = computed(() => {
  const m = MASTERY[question.value?.mastery] || MASTERY.NEW
  return { color: m.color, background: m.bg }
})
const masteryLabel = computed(() => (MASTERY[question.value?.mastery] || MASTERY.NEW).label)

function ratingLabel(v) {
  return (RATING[v] || RATING.GOOD).label
}
function ratingColor(v) {
  return (RATING[v] || RATING.GOOD).color
}
function resultStyle(v) {
  const r = RESULT[v] || RESULT.PENDING
  return { color: r.color, background: r.bg }
}
function resultLabel(v) {
  return (RESULT[v] || RESULT.PENDING).label
}

async function load() {
  loading.value = true
  try {
    const [q, history, related] = await Promise.all([
      api.questions.get(route.params.id),
      api.questions.reviews(route.params.id),
      api.questions.interviews(route.params.id)
    ])
    question.value = q
    logs.value = history
    interviews.value = related
  } catch (e) {
    router.replace('/questions')
  } finally {
    loading.value = false
  }
}

async function toggleStar() {
  try {
    const updated = await api.questions.star(question.value.id)
    question.value.starred = updated.starred
  } catch (e) {
    /* ignore */
  }
}

async function setMastery(value) {
  try {
    question.value = await api.questions.mastery(question.value.id, value)
    ElMessage.success('掌握程度已更新')
    await refreshMeta()
  } catch (e) {
    /* ignore */
  }
}

async function rate(rating) {
  try {
    const updated = await api.questions.review(question.value.id, rating)
    question.value = updated
    const history = await api.questions.reviews(question.value.id)
    logs.value = history
    ElMessage.success(`已记录，${dueLabel(updated.nextReviewAt, updated.reviewCount)}`)
    await Promise.all([refreshMeta(), refreshDue()])
  } catch (e) {
    /* ignore */
  }
}

async function remove() {
  try {
    await ElMessageBox.confirm('删除后不可恢复，确定删除这道题吗？', '删除题目', { type: 'warning' })
    await api.questions.remove(question.value.id)
    ElMessage.success('已删除')
    await refreshMeta()
    router.push('/questions')
  } catch (e) {
    /* ignore */
  }
}

onMounted(load)
</script>
