<template>
  <div class="page interviews-page">
    <div class="page-toolbar">
      <div class="page-count">
        <b>{{ filtered.length }}</b> 场面试
        <span class="muted" v-if="stats.pass">· 已通过 {{ stats.pass }}</span>
        <span class="muted" v-if="stats.pending">· 待定 {{ stats.pending }}</span>
      </div>
      <div class="page-toolbar-actions">
        <el-input
          v-model="keyword"
          class="filter-search small"
          placeholder="搜索公司或岗位"
          clearable
          :prefix-icon="Search"
        />
        <el-select v-model="resultFilter" placeholder="全部结果" clearable class="filter-select narrow">
          <el-option v-for="(v, k) in RESULT" :key="k" :label="v.label" :value="k" />
        </el-select>
        <el-button type="primary" @click="$router.push('/interviews/new')">
          <el-icon><Plus /></el-icon>新建面试档案
        </el-button>
      </div>
    </div>

    <div v-loading="loading">
      <div v-if="filtered.length" class="interview-grid">
        <article v-for="it in filtered" :key="it.id" class="iv-card" @click="$router.push(`/interviews/${it.id}`)">
          <div class="iv-card-head">
            <span class="company-avatar lg" :style="{ background: colorOf(it.company) }">{{
              initials(it.company)
            }}</span>
            <div class="iv-card-title">
              <h3>{{ it.company }}</h3>
              <p>
                {{ it.position || '未填写岗位' }}<span v-if="it.round"> · {{ it.round }}</span>
              </p>
            </div>
            <span class="mini-pill" :style="resultStyle(it.result)">{{ resultLabel(it.result) }}</span>
          </div>

          <div class="iv-meta-row">
            <span
              ><el-icon><Calendar /></el-icon>{{ fmtDate(it.interviewDate) }}</span
            >
            <span v-if="it.channel">{{ it.channel }}</span>
            <span v-if="it.salaryRange">{{ it.salaryRange }}</span>
            <span
              ><el-icon><Collection /></el-icon>{{ it.questionCount }} 道题</span
            >
          </div>

          <p class="iv-summary">{{ it.summaryExcerpt || '还没有写复盘，点击补充这场比赛的经验。' }}</p>

          <div v-if="it.questionTitles?.length" class="iv-question-chips">
            <span v-for="title in it.questionTitles.slice(0, 3)" :key="title">{{ title }}</span>
          </div>

          <div class="iv-footer">
            <el-rate :model-value="it.selfRating || 0" disabled size="small" />
            <div class="iv-actions" @click.stop>
              <el-button link size="small" @click="$router.push(`/interviews/${it.id}/edit`)">编辑</el-button>
              <el-button link size="small" type="danger" @click="remove(it)">删除</el-button>
            </div>
          </div>
        </article>
      </div>

      <EmptyState
        v-else-if="!loading"
        title="还没有面试档案"
        description="记下公司、轮次和现场被问到的题，复盘会变成下一次的底气。"
      >
        <template #action>
          <el-button type="primary" @click="$router.push('/interviews/new')">记录第一场面试</el-button>
        </template>
      </EmptyState>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Calendar, Collection } from '@element-plus/icons-vue'
import EmptyState from '@/components/EmptyState.vue'
import { api } from '@/api'
import { RESULT } from '@/utils/dict'
import { colorOf, fmtDate, initials } from '@/utils/format'

const loading = ref(true)
const list = ref([])
const keyword = ref('')
const resultFilter = ref(null)

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return list.value.filter((it) => {
    if (resultFilter.value && it.result !== resultFilter.value) return false
    if (!kw) return true
    return `${it.company} ${it.position || ''} ${it.round || ''}`.toLowerCase().includes(kw)
  })
})

const stats = computed(() => ({
  pass: list.value.filter((i) => i.result === 'PASS').length,
  pending: list.value.filter((i) => i.result === 'PENDING').length
}))

function resultStyle(value) {
  const r = RESULT[value] || RESULT.PENDING
  return { color: r.color, background: r.bg }
}
function resultLabel(value) {
  return (RESULT[value] || RESULT.PENDING).label
}

async function load() {
  loading.value = true
  try {
    list.value = await api.interviews.list()
  } finally {
    loading.value = false
  }
}

async function remove(it) {
  try {
    await ElMessageBox.confirm(`删除「${it.company} · ${it.round || '面试'}」的记录？`, '删除面试档案', {
      type: 'warning'
    })
    await api.interviews.remove(it.id)
    ElMessage.success('已删除')
    await load()
  } catch (e) {
    /* ignore */
  }
}

onMounted(load)
</script>
