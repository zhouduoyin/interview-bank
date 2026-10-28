<template>
  <div v-loading="loading" class="page interview-detail">
    <div class="detail-crumbs">
      <router-link to="/interviews" class="back-link">← 返回面试记录</router-link>
      <span class="muted">/</span>
      <span class="muted">{{ record?.company || '详情' }}</span>
    </div>

    <div v-if="record" class="interview-hero card">
      <span class="company-avatar xl" :style="{ background: colorOf(record.company) }">{{
        initials(record.company)
      }}</span>
      <div class="interview-hero-main">
        <div class="badge-row">
          <span class="mini-pill" :style="resultStyle">{{ resultLabel }}</span>
          <span class="mini-pill neutral">{{ record.round || '面试' }}</span>
          <span class="muted small">{{ fmtDate(record.interviewDate) }} · {{ weekdayCN(record.interviewDate) }}</span>
        </div>
        <h1>{{ record.company }}</h1>
        <p class="muted">{{ record.position || '未填写岗位' }}</p>
      </div>
      <div class="interview-hero-actions">
        <el-button @click="$router.push(`/interviews/${record.id}/edit`)">
          <el-icon><EditPen /></el-icon>编辑
        </el-button>
        <el-button type="danger" plain @click="remove">删除</el-button>
      </div>
    </div>

    <div v-if="record" class="interview-detail-grid">
      <div class="detail-main">
        <section class="card">
          <div class="card-head">
            <h3>面试复盘</h3>
            <span class="muted small">{{ record.summary ? 'Markdown' : '' }}</span>
          </div>
          <MarkdownView v-if="record.summary" :content="record.summary" />
          <p v-else class="muted">
            还没有写复盘，<router-link class="text-link" :to="`/interviews/${record.id}/edit`"
              >趁记忆新鲜补上</router-link
            >。
          </p>
        </section>

        <section class="card">
          <div class="card-head">
            <h3>本场聊到的题</h3>
            <span class="muted small">{{ record.questions?.length || 0 }} 道</span>
          </div>
          <div v-if="record.questions?.length" class="interview-questions">
            <div v-for="(iq, i) in record.questions" :key="iq.id" class="interview-question">
              <span class="linked-index">{{ i + 1 }}</span>
              <div class="interview-question-main">
                <router-link :to="`/questions/${iq.question.id}`" class="interview-question-title">
                  {{ iq.question.title }}
                </router-link>
                <p v-if="iq.note" class="interview-question-note">{{ iq.note }}</p>
                <div class="interview-question-meta">
                  <span class="mini-pill" :style="masteryStyle(iq.question.mastery)">{{
                    masteryLabel(iq.question.mastery)
                  }}</span>
                  <span class="muted small">{{ iq.question.reviewCount }} 次复习</span>
                  <router-link :to="`/questions/${iq.question.id}`" class="text-link small">查看答案 →</router-link>
                </div>
              </div>
            </div>
          </div>
          <p v-else class="muted">
            这场面试还没有关联题目，<router-link class="text-link" :to="`/interviews/${record.id}/edit`"
              >去关联</router-link
            >。
          </p>
        </section>
      </div>

      <aside class="detail-side">
        <section class="card">
          <div class="card-head"><h3>面试信息</h3></div>
          <div class="info-list">
            <div>
              <span>日期</span><b>{{ fmtDate(record.interviewDate) }}</b>
            </div>
            <div>
              <span>轮次</span><b>{{ record.round || '—' }}</b>
            </div>
            <div>
              <span>渠道</span><b>{{ record.channel || '—' }}</b>
            </div>
            <div>
              <span>薪资</span><b>{{ record.salaryRange || '—' }}</b>
            </div>
            <div>
              <span>结果</span><b :style="{ color: resultColor }">{{ resultLabel }}</b>
            </div>
            <div>
              <span>自评</span><b>{{ record.selfRating ? `${record.selfRating} / 5` : '—' }}</b>
            </div>
            <div>
              <span>创建时间</span><b>{{ fmtDate(record.createdAt) }}</b>
            </div>
          </div>
          <el-rate v-if="record.selfRating" :model-value="record.selfRating" disabled class="detail-rate" />
        </section>

        <section class="card">
          <div class="card-head"><h3>这场面试的题</h3></div>
          <div class="side-question-list">
            <router-link
              v-for="iq in record.questions"
              :key="iq.id"
              :to="`/questions/${iq.question.id}`"
              class="side-question"
            >
              <i class="dot" :style="{ background: iq.question.category?.color || 'var(--text-3)' }" />
              <span>{{ iq.question.title }}</span>
            </router-link>
          </div>
          <p v-if="!record.questions?.length" class="muted small">暂无关联题目</p>
        </section>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen } from '@element-plus/icons-vue'
import MarkdownView from '@/components/MarkdownView.vue'
import { api } from '@/api'
import { MASTERY, RESULT } from '@/utils/dict'
import { colorOf, fmtDate, initials, weekdayCN } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const record = ref(null)

const resultLabel = computed(() => (RESULT[record.value?.result] || RESULT.PENDING).label)
const resultColor = computed(() => (RESULT[record.value?.result] || RESULT.PENDING).color)
const resultStyle = computed(() => {
  const r = RESULT[record.value?.result] || RESULT.PENDING
  return { color: r.color, background: r.bg }
})

function masteryStyle(value) {
  const m = MASTERY[value] || MASTERY.NEW
  return { color: m.color, background: m.bg }
}
function masteryLabel(value) {
  return (MASTERY[value] || MASTERY.NEW).label
}

async function load() {
  loading.value = true
  try {
    record.value = await api.interviews.get(route.params.id)
  } catch (e) {
    router.replace('/interviews')
  } finally {
    loading.value = false
  }
}

async function remove() {
  try {
    await ElMessageBox.confirm('删除这份面试记录？关联的题目不会被删除。', '删除面试记录', { type: 'warning' })
    await api.interviews.remove(record.value.id)
    ElMessage.success('已删除')
    router.push('/interviews')
  } catch (e) {
    /* ignore */
  }
}

onMounted(load)
</script>
