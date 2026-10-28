<template>
  <div v-loading="loading" class="page dashboard-page">
    <section class="hero-card">
      <div class="hero-main">
        <div class="hero-eyebrow">{{ greeting() }} · {{ todayText }}</div>
        <h2 v-if="ov.due">
          今天有 <em>{{ ov.due }}</em> 道题等待复习
        </h2>
        <h2 v-else>今天的复习任务已经清空了 🎉</h2>
        <p>
          {{ ov.due ? '按遗忘曲线过一遍，比重读十遍更有效。' : '可以去题库里翻翻，或者把今天被问到的题记下来。' }}
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/review')">
            <el-icon><AlarmClock /></el-icon>
            {{ ov.due ? '开始今日复习' : '去复习' }}
          </el-button>
          <el-button size="large" class="hero-ghost" @click="$router.push('/questions/new')">
            <el-icon><Plus /></el-icon>
            记录一道题
          </el-button>
          <el-button size="large" class="hero-ghost" @click="$router.push('/interviews/new')">
            <el-icon><ChatLineSquare /></el-icon>
            记一场面试
          </el-button>
        </div>
      </div>
      <div class="hero-side">
        <div class="hero-streak">
          <span class="streak-num">{{ ov.streakDays || 0 }}</span>
          <span class="streak-unit">天连续复习</span>
        </div>
        <div class="week-dots">
          <i v-for="d in weekDots" :key="d.date" :class="{ on: d.on }" :title="`${d.date} · ${d.count} 次`" />
        </div>
        <div class="hero-side-note">本周已复习 {{ weekTotal }} 次</div>
      </div>
    </section>

    <section class="stat-grid">
      <StatCard label="题库总量" :value="ov.totalQuestions || 0" suffix="题" tone="indigo" :icon="Collection"
        :hint="`本月新增 ${ov.monthNew || 0} 题`" />
      <StatCard label="今日待复习" :value="ov.due || 0" suffix="题" tone="amber" :icon="AlarmClock"
        :hint="`今日已复习 ${ov.todayReviews || 0} 题`" />
      <StatCard label="已掌握" :value="ov.mastered || 0" suffix="题" tone="green" :icon="CircleCheck"
        :hint="`掌握率 ${ov.masteryRate || 0}%`" />
      <StatCard label="累计复习" :value="ov.totalReviews || 0" suffix="次" tone="rose" :icon="TrendCharts"
        :hint="`标签 ${ov.tagCount || 0} 个`" />
    </section>

    <div class="dash-grid">
      <div class="dash-main">
        <section class="card heatmap-card">
          <div class="card-head">
            <div>
              <h3>复习热力图</h3>
              <p>过去 17 周共复习 <b>{{ heatTotal }}</b> 次，保持节奏比突击更有效。</p>
            </div>
            <router-link to="/insights" class="text-link">查看洞察 →</router-link>
          </div>
          <Heatmap :cells="heatmap" />
        </section>

        <section class="card">
          <div class="card-head">
            <div>
              <h3>最近添加</h3>
              <p>看看最近记下的题，趁记忆还新鲜补上答案。</p>
            </div>
            <router-link to="/questions" class="text-link">全部题目 →</router-link>
          </div>
          <div v-if="recent.length" class="recent-list">
            <router-link v-for="q in recent" :key="q.id" :to="`/questions/${q.id}`" class="recent-item">
              <span class="recent-dot" :style="{ background: q.categoryColor || 'var(--text-3)' }" />
              <div class="recent-main">
                <b>{{ q.title }}</b>
                <small>{{ q.categoryName }} · {{ fromNow(q.createdAt) }}</small>
              </div>
              <span class="mini-pill" :style="masteryStyle(q.mastery)">{{ masteryLabel(q.mastery) }}</span>
            </router-link>
          </div>
          <EmptyState v-else title="题库还是空的" description="把第一道面试题记下来吧，之后会越来越顺手。">
            <template #action>
              <el-button type="primary" @click="$router.push('/questions/new')">记录第一道题</el-button>
            </template>
          </EmptyState>
        </section>
      </div>

      <div class="dash-side">
        <section class="card mastery-card">
          <div class="card-head">
            <h3>掌握情况</h3>
            <span class="muted">共 {{ ov.totalQuestions || 0 }} 题</span>
          </div>
          <div class="ring-wrap">
            <svg class="ring" viewBox="0 0 120 120">
              <circle class="ring-bg" cx="60" cy="60" r="52" />
              <circle
                class="ring-fg"
                cx="60"
                cy="60"
                r="52"
                :stroke-dasharray="`${ringDash} ${CIRC}`"
                transform="rotate(-90 60 60)"
              />
            </svg>
            <div class="ring-label">
              <b>{{ ov.masteryRate || 0 }}%</b>
              <span>掌握率</span>
            </div>
          </div>
          <div class="mastery-legend">
            <div><i class="dot" style="background: var(--accent-2)" /><span>已掌握</span><b>{{ ov.mastered || 0 }}</b></div>
            <div><i class="dot" style="background: var(--accent-4)" /><span>复习中</span><b>{{ ov.learning || 0 }}</b></div>
            <div><i class="dot" style="background: var(--text-3)" /><span>未掌握</span><b>{{ ov.newCount || 0 }}</b></div>
          </div>
        </section>

        <section class="card">
          <div class="card-head">
            <h3>分类分布</h3>
            <span class="muted">{{ categories.length }} 类</span>
          </div>
          <div class="cat-list">
            <div
              v-for="c in categories.slice(0, 6)"
              :key="c.name"
              class="cat-row clickable"
              @click="$router.push({ path: '/questions', query: { category: c.id } })"
            >
              <div class="cat-row-head">
                <span class="cat-name"><i class="dot" :style="{ background: c.color }" />{{ c.name }}</span>
                <span class="cat-count">{{ c.count }}</span>
              </div>
              <div class="cat-bar">
                <div class="cat-bar-fill" :style="{ width: `${Math.max(4, (c.count / maxCat) * 100)}%`, background: c.color }" />
              </div>
            </div>
            <div v-if="!categories.length" class="muted small">还没有分类数据</div>
          </div>
        </section>

        <section class="card">
          <div class="card-head">
            <h3>最近面试</h3>
            <router-link to="/interviews" class="text-link">全部 →</router-link>
          </div>
          <div v-if="upcoming.length" class="interview-mini-list">
            <router-link v-for="r in upcoming" :key="r.id" :to="`/interviews/${r.id}`" class="interview-mini">
              <span class="company-avatar" :style="{ background: colorOf(r.company) }">{{ initials(r.company) }}</span>
              <div>
                <b>{{ r.company }}</b>
                <small>{{ r.round || '面试' }} · {{ fmtDate(r.interviewDate, 'M月D日') }}</small>
              </div>
              <span class="mini-pill" :style="resultStyle(r.result)">{{ resultLabel(r.result) }}</span>
            </router-link>
          </div>
          <div v-else class="muted small">还没有面试记录，<router-link to="/interviews/new" class="text-link">记一场 →</router-link></div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import {
  AlarmClock, Plus, ChatLineSquare, Collection, CircleCheck, TrendCharts
} from '@element-plus/icons-vue'
import StatCard from '@/components/StatCard.vue'
import Heatmap from '@/components/Heatmap.vue'
import EmptyState from '@/components/EmptyState.vue'
import { api } from '@/api'
import { DIFFICULTY, MASTERY, RESULT } from '@/utils/dict'
import { colorOf, fmtDate, fromNow, greeting, initials } from '@/utils/format'

const loading = ref(true)
const data = ref(null)

const ov = computed(() => data.value?.overview || {})
const heatmap = computed(() => data.value?.heatmap || [])
const categories = computed(() => data.value?.categories || [])
const recent = computed(() => data.value?.recent || [])
const upcoming = computed(() => data.value?.upcoming || [])
const maxCat = computed(() => Math.max(1, ...categories.value.map((c) => c.count)))
const heatTotal = computed(() => heatmap.value.reduce((sum, c) => sum + c.count, 0))
const todayText = dayjs().format('M 月 D 日 dddd')

const CIRC = 2 * Math.PI * 52
const ringDash = computed(() => (CIRC * (ov.value.masteryRate || 0)) / 100)

const weekDots = computed(() => {
  const cells = heatmap.value.slice(-7)
  return cells.map((c) => ({ ...c, on: c.count > 0 }))
})
const weekTotal = computed(() => weekDots.value.reduce((sum, c) => sum + c.count, 0))

function masteryStyle(value) {
  const m = MASTERY[value] || MASTERY.NEW
  return { color: m.color, background: m.bg }
}
function masteryLabel(value) {
  return (MASTERY[value] || MASTERY.NEW).label
}
function resultStyle(value) {
  const r = RESULT[value] || RESULT.PENDING
  return { color: r.color, background: r.bg }
}
function resultLabel(value) {
  return (RESULT[value] || RESULT.PENDING).label
}

onMounted(async () => {
  try {
    data.value = await api.dashboard()
  } finally {
    loading.value = false
  }
})
</script>
