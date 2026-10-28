<template>
  <div v-loading="loading" class="page insights-page">
    <section class="stat-grid">
      <StatCard
        label="记忆题库"
        :value="ov.totalQuestions || 0"
        suffix="题"
        tone="indigo"
        :icon="Collection"
        :hint="`${ov.newCount || 0} 题还没开始训练`"
      />
      <StatCard
        label="稳固率"
        :value="ov.masteryRate || 0"
        suffix="%"
        tone="green"
        :icon="CircleCheck"
        :hint="`长期记忆 ${ov.mastered || 0} 题`"
      />
      <StatCard
        label="累计回想"
        :value="ov.totalReviews || 0"
        suffix="次"
        tone="rose"
        :icon="TrendCharts"
        :hint="`平均每题 ${avgReviews} 次`"
      />
      <StatCard
        label="面试档案"
        :value="ov.interviewCount || 0"
        suffix="场"
        tone="amber"
        :icon="ChatLineSquare"
        :hint="`连续回想 ${ov.streakDays || 0} 天`"
      />
    </section>

    <div class="insight-grid">
      <div class="insight-main">
        <section class="card">
          <div class="card-head">
            <div>
              <h3>最近 30 天回想趋势</h3>
              <p>保持每天一点点，比周末突击更稳。</p>
            </div>
            <span class="pill">共 {{ trendTotal }} 次</span>
          </div>
          <div class="trend-chart">
            <svg viewBox="0 0 620 190" preserveAspectRatio="none" class="trend-svg">
              <defs>
                <linearGradient id="trendFill" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" style="stop-color: var(--primary); stop-opacity: 0.28" />
                  <stop offset="100%" style="stop-color: var(--primary); stop-opacity: 0" />
                </linearGradient>
              </defs>
              <line
                v-for="g in [0.25, 0.5, 0.75, 1]"
                :key="g"
                x1="10"
                :y1="170 - g * 150"
                x2="610"
                :y2="170 - g * 150"
                class="trend-grid"
              />
              <path v-if="chart.area" :d="chart.area" fill="url(#trendFill)" />
              <path v-if="chart.line" :d="chart.line" fill="none" class="trend-line" />
              <circle v-for="(p, i) in chart.pts" :key="i" :cx="p[0]" :cy="p[1]" r="3" class="trend-dot">
                <title>{{ trend[i].date }} · {{ trend[i].count }} 次</title>
              </circle>
            </svg>
            <div class="trend-axis">
              <span>{{ trend.length ? trend[0].date.slice(5) : '' }}</span>
              <span>{{ trend.length ? trend[Math.floor(trend.length / 2)].date.slice(5) : '' }}</span>
              <span>{{ trend.length ? trend[trend.length - 1].date.slice(5) : '' }}</span>
            </div>
          </div>
        </section>

        <section class="card">
          <div class="card-head">
            <div>
              <h3>各分类记忆结构</h3>
              <p>颜色越偏绿，说明这一类记忆越稳固。</p>
            </div>
            <div class="legend-inline">
              <span><i style="background: var(--text-3)" />待开始</span>
              <span><i style="background: var(--accent-4)" />加固中</span>
              <span><i style="background: var(--accent-2)" />长期记忆</span>
            </div>
          </div>
          <div class="mastery-bars">
            <div v-for="c in masteryByCategory" :key="c.name" class="mastery-bar-row">
              <div class="mastery-bar-head">
                <span>{{ c.name }}</span>
                <span class="muted small">{{ c.mastered }}/{{ c.newCount + c.learning + c.mastered }} 长期记忆</span>
              </div>
              <div class="stacked-bar">
                <i :style="{ width: `${ratio(c.newCount, c)}%`, background: 'var(--text-3)' }" />
                <i :style="{ width: `${ratio(c.learning, c)}%`, background: 'var(--accent-4)' }" />
                <i :style="{ width: `${ratio(c.mastered, c)}%`, background: 'var(--accent-2)' }" />
              </div>
            </div>
            <p v-if="!masteryByCategory.length" class="muted small">还没有数据</p>
          </div>
        </section>
      </div>

      <div class="insight-side">
        <section class="card">
          <div class="card-head"><h3>题目难度分布</h3></div>
          <div class="donut-row">
            <div class="donut-wrap">
              <svg viewBox="0 0 120 120">
                <circle cx="60" cy="60" r="46" class="donut-bg" />
                <circle
                  v-for="s in difficultySegments"
                  :key="s.label"
                  cx="60"
                  cy="60"
                  r="46"
                  fill="none"
                  :stroke="s.color"
                  stroke-width="14"
                  :stroke-dasharray="`${s.dash} ${CIRC}`"
                  :stroke-dashoffset="-s.offset"
                  transform="rotate(-90 60 60)"
                  stroke-linecap="butt"
                />
              </svg>
              <div class="donut-center">
                <b>{{ ov.totalQuestions || 0 }}</b>
                <span>道题</span>
              </div>
            </div>
            <div class="donut-legend">
              <div v-for="s in difficultySegments" :key="s.label">
                <i :style="{ background: s.color }" />
                <span>{{ s.label }}</span>
                <b>{{ s.count }}</b>
              </div>
            </div>
          </div>
        </section>

        <section class="card">
          <div class="card-head">
            <h3>高频标签</h3>
            <span class="muted small">{{ tags.length }} 个</span>
          </div>
          <div class="tag-stat-list">
            <div
              v-for="t in tags.slice(0, 12)"
              :key="t.name"
              class="tag-stat-row clickable"
              @click="router.push({ path: '/questions', query: { tag: t.name } })"
            >
              <span class="tag-stat-name">#{{ t.name }}</span>
              <div class="tag-stat-bar"><i :style="{ width: `${Math.max(6, (t.count / maxTag) * 100)}%` }" /></div>
              <b>{{ t.count }}</b>
            </div>
            <p v-if="!tags.length" class="muted small">还没有打标签</p>
          </div>
        </section>

        <section class="card attention-card">
          <div class="card-head"><h3>需要加强</h3></div>
          <div v-if="weakCategories.length" class="weak-list">
            <router-link
              v-for="c in weakCategories"
              :key="c.name"
              :to="{ path: '/questions', query: { tag: '' } }"
              class="weak-item"
              @click.prevent="goCategory(c)"
            >
              <span class="cat-name"
                ><i class="dot" :style="{ background: c.color || 'var(--text-3)' }" />{{ c.name }}</span
              >
              <span class="weak-rate">{{ c.rate }}%</span>
            </router-link>
          </div>
          <p v-else class="muted small">暂时没有明显薄弱的分类，继续保持。</p>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Collection, CircleCheck, TrendCharts, ChatLineSquare } from '@element-plus/icons-vue'
import StatCard from '@/components/StatCard.vue'
import { api } from '@/api'
import { DIFFICULTY } from '@/utils/dict'

const router = useRouter()
const loading = ref(true)
const data = ref(null)
const CIRC = 2 * Math.PI * 46

const ov = computed(() => data.value?.overview || {})
const trend = computed(() => data.value?.trend || [])
const trendTotal = computed(() => trend.value.reduce((s, d) => s + d.count, 0))
const tags = computed(() => data.value?.tags || [])
const maxTag = computed(() => Math.max(1, ...tags.value.map((t) => t.count)))
const masteryByCategory = computed(() => data.value?.masteryByCategory || [])
const categories = computed(() => data.value?.categories || [])
const avgReviews = computed(() => {
  if (!ov.value.totalQuestions) return 0
  return (ov.value.totalReviews / ov.value.totalQuestions).toFixed(1)
})

const chart = computed(() => {
  const list = trend.value
  if (!list.length) return { pts: [], line: '', area: '' }
  const max = Math.max(1, ...list.map((d) => d.count))
  const w = 600
  const h = 150
  const top = 10
  const step = list.length > 1 ? w / (list.length - 1) : 0
  const pts = list.map((d, i) => [10 + i * step, top + (1 - d.count / max) * h])
  const line = pts.map((p, i) => `${i ? 'L' : 'M'}${p[0].toFixed(1)},${p[1].toFixed(1)}`).join(' ')
  const area = `${line} L${pts[pts.length - 1][0].toFixed(1)},180 L${pts[0][0].toFixed(1)},180 Z`
  return { pts, line, area }
})

const difficultySegments = computed(() => {
  const counts = {}
  ;(data.value?.difficulties || []).forEach((d) => (counts[d.difficulty] = d.count))
  const total = Math.max(1, ov.value.totalQuestions || 1)
  let acc = 0
  return Object.entries(DIFFICULTY).map(([key, meta]) => {
    const count = counts[key] || 0
    const length = (count / total) * CIRC
    const segment = {
      label: meta.label,
      color: meta.dot,
      count,
      dash: length,
      offset: acc
    }
    acc += length
    return segment
  })
})

const weakCategories = computed(() => {
  return categories.value
    .filter((c) => c.count >= 2)
    .map((c) => ({ ...c, rate: Math.round((c.mastered / c.count) * 100) }))
    .filter((c) => c.rate < 60)
    .sort((a, b) => a.rate - b.rate)
    .slice(0, 4)
})

function ratio(part, item) {
  const total = item.newCount + item.learning + item.mastered
  return total ? (part / total) * 100 : 0
}

function goCategory(c) {
  router.push({ path: '/questions', query: { category: c.id } })
}

onMounted(async () => {
  try {
    data.value = await api.insights()
  } finally {
    loading.value = false
  }
})
</script>
