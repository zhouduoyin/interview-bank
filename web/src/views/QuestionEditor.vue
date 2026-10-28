<template>
  <div v-loading="loading" class="page editor-page">
    <div class="editor-head">
      <div>
        <router-link to="/questions" class="back-link">← 返回题库</router-link>
        <h2>{{ isEdit ? '编辑题目' : '记录一道新题' }}</h2>
        <p class="muted">参考答案写下来才算真正消化；标签和分类能让以后更快找到它。</p>
      </div>
      <div class="editor-actions">
        <el-button v-if="isEdit" type="danger" plain @click="remove">删除</el-button>
        <el-button @click="$router.back()">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">
          <el-icon><Check /></el-icon>保存题目
        </el-button>
      </div>
    </div>

    <section class="card">
      <div class="card-head">
        <h3>基本信息</h3>
        <span class="muted">Ctrl / ⌘ + S 快速保存</span>
      </div>
      <el-input
        v-model="form.title"
        class="title-input"
        size="large"
        maxlength="300"
        show-word-limit
        placeholder="题目，例如：MySQL 索引为什么使用 B+ 树？"
      />
      <div class="form-grid">
        <label class="field">
          <span>分类</span>
          <el-select v-model="form.categoryId" placeholder="选择分类" clearable>
            <el-option v-for="c in appState.categories" :key="c.id" :label="c.name" :value="c.id">
              <span class="dot" :style="{ background: c.color || 'var(--text-3)' }" />
              <span>{{ c.name }}</span>
            </el-option>
          </el-select>
        </label>
        <label class="field">
          <span>难度</span>
          <el-select v-model="form.difficulty">
            <el-option v-for="(v, k) in DIFFICULTY" :key="k" :label="v.label" :value="k" />
          </el-select>
        </label>
        <label class="field">
          <span>掌握程度</span>
          <el-select v-model="form.mastery">
            <el-option v-for="(v, k) in MASTERY" :key="k" :label="v.label" :value="k" />
          </el-select>
        </label>
        <label class="field">
          <span>收藏</span>
          <el-switch v-model="form.starred" active-text="重点关注" />
        </label>
        <label class="field">
          <span>来源公司</span>
          <el-input v-model="form.sourceCompany" placeholder="例如：字节跳动" maxlength="120" />
        </label>
        <label class="field">
          <span>岗位</span>
          <el-input v-model="form.sourcePosition" placeholder="例如：Java 后端" maxlength="120" />
        </label>
        <label class="field field-wide">
          <span>标签</span>
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入后回车创建，如：Redis、并发"
          >
            <el-option v-for="t in appState.tags" :key="t.name" :label="t.name" :value="t.name" />
          </el-select>
        </label>
      </div>
    </section>

    <section class="card">
      <div class="card-head">
        <div>
          <h3>题目描述</h3>
          <p>可以放面试官的原话、追问，或者代码片段。</p>
        </div>
      </div>
      <MarkdownEditor v-model="form.content" :min-height="200" placeholder="面试官问：…&#10;&#10;要求 / 场景 / 追问：" />
    </section>

    <section class="card">
      <div class="card-head">
        <div>
          <h3>参考答案</h3>
          <p>用自己的话写一遍，比复制标准答案更有效。</p>
        </div>
        <el-button v-if="!form.answer" size="small" text type="primary" @click="insertAnswerTemplate">插入答案模板</el-button>
      </div>
      <MarkdownEditor v-model="form.answer" :min-height="320" placeholder="## 一句话回答&#10;&#10;## 关键点&#10;&#10;- …" />
    </section>

    <section class="card">
      <div class="card-head">
        <div>
          <h3>我的理解 / 追问备忘</h3>
          <p>记录面试官追问、自己踩过的坑，下次不会忘。</p>
        </div>
      </div>
      <el-input v-model="form.note" type="textarea" :rows="3" placeholder="例如：被追问为什么是 0.75 负载因子……" />
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import { api } from '@/api'
import { appState, refreshMeta } from '@/store/app'
import { DIFFICULTY, MASTERY } from '@/utils/dict'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => Boolean(route.params.id))
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  title: '',
  content: '',
  answer: '',
  note: '',
  categoryId: null,
  difficulty: 'MEDIUM',
  mastery: 'NEW',
  tags: [],
  sourceCompany: '',
  sourcePosition: '',
  starred: false
})

function fill(q) {
  form.title = q.title || ''
  form.content = q.content || ''
  form.answer = q.answer || ''
  form.note = q.note || ''
  form.categoryId = q.category?.id ?? null
  form.difficulty = q.difficulty || 'MEDIUM'
  form.mastery = q.mastery || 'NEW'
  form.tags = q.tags || []
  form.sourceCompany = q.sourceCompany || ''
  form.sourcePosition = q.sourcePosition || ''
  form.starred = Boolean(q.starred)
}

function payload() {
  return {
    title: form.title.trim(),
    content: form.content,
    answer: form.answer,
    note: form.note,
    categoryId: form.categoryId,
    difficulty: form.difficulty,
    mastery: form.mastery,
    tags: form.tags,
    sourceCompany: form.sourceCompany || null,
    sourcePosition: form.sourcePosition || null,
    starred: form.starred
  }
}

async function save() {
  if (!form.title.trim()) {
    ElMessage.warning('先给题目起一个标题吧')
    return
  }
  if (saving.value) return
  saving.value = true
  try {
    const saved = isEdit.value
      ? await api.questions.update(route.params.id, payload())
      : await api.questions.create(payload())
    ElMessage.success(isEdit.value ? '已保存' : '题目已记录，开始安排复习')
    await refreshMeta()
    router.push(`/questions/${saved.id}`)
  } catch (e) {
    /* ignore */
  } finally {
    saving.value = false
  }
}

async function remove() {
  try {
    await ElMessageBox.confirm('删除这道题后不可恢复，确定吗？', '删除题目', { type: 'warning' })
    await api.questions.remove(route.params.id)
    ElMessage.success('已删除')
    await refreshMeta()
    router.push('/questions')
  } catch (e) {
    /* ignore */
  }
}

function insertAnswerTemplate() {
  form.answer = `## 一句话回答

## 关键点

- 

## 代码 / 示例

\`\`\`java
// 
\`\`\`

## 可能的追问
`
}

function shortcut(event) {
  const mod = event.metaKey || event.ctrlKey
  if (mod && event.key.toLowerCase() === 's') {
    event.preventDefault()
    save()
  }
}

onMounted(async () => {
  if (isEdit.value) {
    loading.value = true
    try {
      const q = await api.questions.get(route.params.id)
      fill(q)
    } finally {
      loading.value = false
    }
  }
  window.addEventListener('keydown', shortcut)
})

onBeforeUnmount(() => window.removeEventListener('keydown', shortcut))
</script>
