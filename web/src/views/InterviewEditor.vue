<template>
  <div v-loading="loading" class="page interview-editor">
    <div class="editor-head">
      <div>
        <router-link to="/interviews" class="back-link">← 返回面试记录</router-link>
        <h2>{{ isEdit ? '编辑面试记录' : '记录一场面试' }}</h2>
        <p class="muted">把公司、轮次、被问到的题和复盘放在一起，下次面试前翻一翻。</p>
      </div>
      <div class="editor-actions">
        <el-button v-if="isEdit" type="danger" plain @click="remove">删除</el-button>
        <el-button @click="$router.back()">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">
          <el-icon><Check /></el-icon>保存记录
        </el-button>
      </div>
    </div>

    <section class="card">
      <div class="card-head"><h3>面试信息</h3></div>
      <div class="form-grid">
        <label class="field">
          <span>公司 *</span>
          <el-input v-model="form.company" placeholder="例如：字节跳动" maxlength="120" />
        </label>
        <label class="field">
          <span>岗位</span>
          <el-input v-model="form.position" placeholder="例如：Java 后端开发" maxlength="120" />
        </label>
        <label class="field">
          <span>轮次</span>
          <el-select v-model="form.round" filterable allow-create default-first-option placeholder="选择或输入">
            <el-option v-for="r in ROUNDS" :key="r" :label="r" :value="r" />
          </el-select>
        </label>
        <label class="field">
          <span>面试日期</span>
          <el-date-picker v-model="form.interviewDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </label>
        <label class="field">
          <span>结果</span>
          <el-select v-model="form.result">
            <el-option v-for="(v, k) in RESULT" :key="k" :label="v.label" :value="k" />
          </el-select>
        </label>
        <label class="field">
          <span>渠道</span>
          <el-select v-model="form.channel" filterable allow-create clearable placeholder="内推 / BOSS…">
            <el-option v-for="c in CHANNELS" :key="c" :label="c" :value="c" />
          </el-select>
        </label>
        <label class="field">
          <span>薪资范围</span>
          <el-input v-model="form.salaryRange" placeholder="例如：30-40K · 15 薪" maxlength="60" />
        </label>
        <label class="field">
          <span>整体自评</span>
          <el-rate v-model="form.selfRating" :max="5" show-score score-template="{value} 分" />
        </label>
      </div>
    </section>

    <section class="card">
      <div class="card-head">
        <div>
          <h3>本场聊到的题</h3>
          <p>关联已有题目会自动带出你的答案；也可以现场新建一道。</p>
        </div>
        <span class="muted small">已关联 {{ form.questions.length }} 道</span>
      </div>

      <div class="link-adder">
        <el-select
          v-model="pendingQuestionId"
          filterable
          remote
          :remote-method="searchQuestions"
          :loading="searching"
          placeholder="搜索题库中的题目"
          class="link-select"
          clearable
        >
          <el-option v-for="q in options" :key="q.id" :label="q.title" :value="q.id">
            <span class="option-title">{{ q.title }}</span>
            <span class="option-cat">{{ q.category?.name || '未分类' }}</span>
          </el-option>
        </el-select>
        <el-input v-model="pendingNote" placeholder="现场回答 / 追问（可选）" class="link-note" @keyup.enter="addSelected" />
        <el-button type="primary" plain :disabled="!pendingQuestionId" @click="addSelected">添加</el-button>
      </div>
      <div class="quick-create-row">
        <el-icon><Plus /></el-icon>
        <el-input v-model="quickTitle" placeholder="没有这题？直接输入题目，回车现场新建" @keyup.enter="createQuickQuestion" />
        <el-button text type="primary" :disabled="!quickTitle.trim()" :loading="creating" @click="createQuickQuestion">新建并加入</el-button>
      </div>

      <div v-if="form.questions.length" class="linked-list">
        <div v-for="(item, i) in form.questions" :key="item.questionId" class="linked-item">
          <span class="linked-index">{{ i + 1 }}</span>
          <div class="linked-main">
            <router-link :to="`/questions/${item.questionId}`" target="_blank" class="linked-title">{{ item.title }}</router-link>
            <el-input v-model="item.note" size="small" placeholder="这道题的回答 / 面试官追问" class="linked-note-input" />
          </div>
          <el-button text type="danger" size="small" @click="removeQuestion(i)">移除</el-button>
        </div>
      </div>
      <p v-else class="muted small">还没有关联题目，面试记录可以先只写复盘。</p>
    </section>

    <section class="card">
      <div class="card-head">
        <div>
          <h3>面试复盘</h3>
          <p>答得好的、答得差的、需要补的，都写下来。</p>
        </div>
      </div>
      <MarkdownEditor
        v-model="form.summary"
        :min-height="280"
        placeholder="## 整体感受&#10;&#10;## 答得好的&#10;&#10;## 需要补的&#10;&#10;## 下一步计划&#10;"
      />
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Plus } from '@element-plus/icons-vue'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import { api } from '@/api'
import { refreshMeta } from '@/store/app'
import { CHANNELS, RESULT, ROUNDS } from '@/utils/dict'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => Boolean(route.params.id))
const loading = ref(false)
const saving = ref(false)
const searching = ref(false)
const creating = ref(false)
const options = ref([])
const pendingQuestionId = ref(null)
const pendingNote = ref('')
const quickTitle = ref('')

const form = reactive({
  company: '',
  position: '',
  round: '一面',
  interviewDate: dayjs().format('YYYY-MM-DD'),
  result: 'PENDING',
  channel: '',
  salaryRange: '',
  summary: '',
  selfRating: 0,
  questions: []
})

function payload() {
  return {
    company: form.company.trim(),
    position: form.position || null,
    round: form.round || null,
    interviewDate: form.interviewDate || dayjs().format('YYYY-MM-DD'),
    result: form.result,
    channel: form.channel || null,
    salaryRange: form.salaryRange || null,
    summary: form.summary,
    selfRating: form.selfRating || null,
    questions: form.questions.map((q) => ({ questionId: q.questionId, note: q.note || null }))
  }
}

async function searchQuestions(keyword = '') {
  searching.value = true
  try {
    const data = await api.questions.list({ keyword: keyword || null, page: 0, size: 20, sort: 'updated' })
    options.value = data.content.filter((q) => !form.questions.some((item) => item.questionId === q.id))
  } catch (e) {
    /* ignore */
  } finally {
    searching.value = false
  }
}

function addSelected() {
  const q = options.value.find((o) => o.id === pendingQuestionId.value)
  if (!q) return
  form.questions.push({ questionId: q.id, title: q.title, note: pendingNote.value })
  pendingQuestionId.value = null
  pendingNote.value = ''
}

function removeQuestion(index) {
  form.questions.splice(index, 1)
}

async function createQuickQuestion() {
  const title = quickTitle.value.trim()
  if (!title || creating.value) return
  creating.value = true
  try {
    const created = await api.questions.create({ title })
    form.questions.push({ questionId: created.id, title: created.title, note: '' })
    quickTitle.value = ''
    ElMessage.success('已新建题目并加入本场面试')
    await refreshMeta()
  } catch (e) {
    /* ignore */
  } finally {
    creating.value = false
  }
}

async function save() {
  if (!form.company.trim()) {
    ElMessage.warning('先填写公司名称')
    return
  }
  saving.value = true
  try {
    const saved = isEdit.value
      ? await api.interviews.update(route.params.id, payload())
      : await api.interviews.create(payload())
    ElMessage.success('已保存')
    await refreshMeta()
    router.push(`/interviews/${saved.id}`)
  } catch (e) {
    /* ignore */
  } finally {
    saving.value = false
  }
}

async function remove() {
  try {
    await ElMessageBox.confirm('删除这份面试记录？关联的题目不会被删除。', '删除面试记录', { type: 'warning' })
    await api.interviews.remove(route.params.id)
    ElMessage.success('已删除')
    router.push('/interviews')
  } catch (e) {
    /* ignore */
  }
}

function shortcut(event) {
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 's') {
    event.preventDefault()
    save()
  }
}

onMounted(async () => {
  searchQuestions('')
  if (isEdit.value) {
    loading.value = true
    try {
      const r = await api.interviews.get(route.params.id)
      form.company = r.company || ''
      form.position = r.position || ''
      form.round = r.round || '一面'
      form.interviewDate = r.interviewDate || dayjs().format('YYYY-MM-DD')
      form.result = r.result || 'PENDING'
      form.channel = r.channel || ''
      form.salaryRange = r.salaryRange || ''
      form.summary = r.summary || ''
      form.selfRating = r.selfRating || 0
      form.questions = (r.questions || []).map((iq) => ({
        questionId: iq.question.id,
        title: iq.question.title,
        note: iq.note || ''
      }))
    } finally {
      loading.value = false
    }
  }
  window.addEventListener('keydown', shortcut)
})

onBeforeUnmount(() => window.removeEventListener('keydown', shortcut))
</script>
