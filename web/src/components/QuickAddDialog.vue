<template>
  <el-dialog
    v-model="visible"
    title="快速记一道题"
    width="600px"
    align-center
    :close-on-click-modal="false"
    class="quick-add-dialog"
    @closed="reset"
  >
    <div class="quick-add">
      <div class="quick-add-tip">先把题目记下来，答案可以之后慢慢补。</div>
      <el-input
        v-model="form.title"
        size="large"
        maxlength="300"
        show-word-limit
        placeholder="题目，例如：Spring 循环依赖是怎么解决的？"
        @keyup.enter="submit"
      />
      <el-input
        v-model="form.content"
        type="textarea"
        :rows="3"
        placeholder="当时被问到的细节 / 面试官的追问（可选）"
      />
      <div class="quick-add-row">
        <el-select v-model="form.categoryId" placeholder="选择分类" clearable style="width: 170px">
          <el-option v-for="c in appState.categories" :key="c.id" :label="c.name" :value="c.id">
            <span class="dot" :style="{ background: c.color || 'var(--text-3)' }" />
            <span>{{ c.name }}</span>
          </el-option>
        </el-select>
        <el-select
          v-model="form.tags"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="标签，回车创建"
          style="flex: 1"
        >
          <el-option v-for="t in appState.tags" :key="t.name" :label="t.name" :value="t.name" />
        </el-select>
      </div>
      <div class="quick-add-source">
        <el-input v-model="form.sourceCompany" placeholder="公司（可选）" style="width: 180px" />
        <el-input v-model="form.sourcePosition" placeholder="岗位（可选）" style="width: 200px" />
      </div>
      <div class="quick-add-hint">
        <el-icon><InfoFilled /></el-icon>
        保存后会立刻进入复习计划，之后可以在详情页补充参考答案。
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" :disabled="!form.title.trim()" @click="submit">
        保存并继续完善
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { api } from '@/api'
import { appState, refreshMeta } from '@/store/app'

const router = useRouter()
const visible = ref(false)
const saving = ref(false)
const emit = defineEmits(['saved'])

const form = reactive({
  title: '',
  content: '',
  categoryId: null,
  tags: [],
  sourceCompany: '',
  sourcePosition: ''
})

function open() {
  visible.value = true
}

function reset() {
  form.title = ''
  form.content = ''
  form.categoryId = null
  form.tags = []
  form.sourceCompany = ''
  form.sourcePosition = ''
  saving.value = false
}

async function submit() {
  if (!form.title.trim() || saving.value) return
  saving.value = true
  try {
    const created = await api.questions.create({
      title: form.title.trim(),
      content: form.content,
      categoryId: form.categoryId,
      tags: form.tags,
      sourceCompany: form.sourceCompany || null,
      sourcePosition: form.sourcePosition || null
    })
    ElMessage.success('已记到题库，去补充答案吧')
    visible.value = false
    await refreshMeta()
    emit('saved', created)
    router.push(`/questions/${created.id}/edit`)
  } catch (e) {
    /* 错误提示已在请求层处理 */
  } finally {
    saving.value = false
  }
}

defineExpose({ open })
</script>
