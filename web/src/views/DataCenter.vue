<template>
  <div class="page data-page">
    <div class="data-grid">
      <section class="card">
        <div class="card-head"><h3>导出备份</h3></div>
        <p class="muted">把题库、复习进度和面试记录全部导出，换电脑或重装也不怕丢。</p>
        <div class="data-actions">
          <a class="el-button el-button--primary" href="/api/data/export">
            <el-icon><Download /></el-icon>下载 JSON 备份
          </a>
          <a class="el-button" href="/api/data/export/markdown">
            <el-icon><Document /></el-icon>导出 Markdown
          </a>
        </div>
        <ul class="data-hints">
          <li>JSON 备份包含全部数据，可以在新环境重新导入。</li>
          <li>Markdown 版本适合放进 Obsidian、语雀等笔记软件复习。</li>
        </ul>
      </section>

      <section class="card">
        <div class="card-head"><h3>导入数据</h3></div>
        <el-upload
          drag
          :auto-upload="false"
          :show-file-list="false"
          accept=".json,application/json"
          :on-change="onFile"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">拖入 JSON 文件，或 <em>点击选择</em></div>
          <template #tip>
            <div class="el-upload__tip">导入时按标题去重，已存在的题目会跳过，不会覆盖。</div>
          </template>
        </el-upload>
        <div class="paste-toggle">
          <el-button link type="primary" @click="showPaste = !showPaste">{{
            showPaste ? '收起粘贴框' : '或者直接粘贴 JSON'
          }}</el-button>
          <span v-if="fileName" class="muted small">已选择：{{ fileName }}</span>
        </div>
        <el-input
          v-if="showPaste"
          v-model="pasteText"
          type="textarea"
          :rows="6"
          placeholder='{"questions":[{"title":"...","content":"...","categoryName":"Java 基础"}]}'
        />
        <div class="data-actions">
          <el-button type="primary" :loading="importing" :disabled="!hasImportText" @click="doImport">
            <el-icon><Upload /></el-icon>开始导入
          </el-button>
          <el-button text @click="resetImport">清空</el-button>
        </div>
      </section>
    </div>

    <section class="card">
      <div class="card-head">
        <div>
          <h3>分类管理</h3>
          <p>分类显示在题库筛选和统计里，删除分类不会删除题目。</p>
        </div>
        <span class="muted small">{{ categories.length }} 个分类</span>
      </div>
      <div class="cat-manage-add">
        <el-color-picker v-model="newCategory.color" />
        <el-input v-model="newCategory.name" placeholder="新分类名称" maxlength="60" @keyup.enter="addCategory" />
        <el-input v-model="newCategory.description" placeholder="一句描述（可选）" maxlength="120" />
        <el-button type="primary" :disabled="!newCategory.name.trim()" @click="addCategory">添加分类</el-button>
      </div>
      <el-table :data="categories" size="small">
        <el-table-column label="分类" min-width="140">
          <template #default="{ row }">
            <span class="cat-pill simple">
              <i class="dot" :style="{ background: row.color || 'var(--text-3)' }" />{{ row.name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column prop="questionCount" label="题目数" width="80" />
        <el-table-column label="操作" width="140" align="right">
          <template #default="{ row }">
            <el-button link size="small" @click="editCategory(row)">重命名</el-button>
            <el-button link size="small" type="danger" @click="deleteCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section class="card">
      <div class="card-head">
        <div>
          <h3>标签管理</h3>
          <p>重命名会把所有题目里的旧标签一起替换。</p>
        </div>
        <span class="muted small">{{ tags.length }} 个标签</span>
      </div>
      <div v-if="tags.length" class="manage-tags">
        <div v-for="t in tags" :key="t.name" class="manage-tag">
          <span class="manage-tag-name">#{{ t.name }}</span>
          <small>{{ t.count }}</small>
          <button type="button" title="重命名" @click="renameTag(t)">✎</button>
          <button type="button" title="删除标签" class="danger" @click="deleteTag(t)">×</button>
        </div>
      </div>
      <p v-else class="muted small">还没有标签，在题目里添加后会出现在这里。</p>
    </section>

    <section class="card danger-card">
      <div class="card-head"><h3>数据安全</h3></div>
      <p class="muted">
        默认使用 H2 文件数据库（运行目录下的 <code>data/</code> 文件夹）；MySQL 模式保存在
        <code>interview_bank</code> 数据库。两种模式都不经过任何第三方服务器。
      </p>
      <div class="danger-row">
        <div>
          <b>清除示例数据</b>
          <p>只删除首次启动内置的示例题目、分类和面试记录，自己记录的内容会保留。</p>
        </div>
        <el-button @click="clearDemo">清除示例数据</el-button>
      </div>
      <div class="danger-row">
        <div>
          <b>清空全部数据</b>
          <p>删除所有题目、分类、标签、面试记录和复习历史，操作不可恢复，请先导出备份。</p>
        </div>
        <el-button type="danger" plain @click="clearAll">清空全部</el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Document, UploadFilled, Upload } from '@element-plus/icons-vue'
import { api } from '@/api'
import { refreshMeta } from '@/store/app'

const categories = ref([])
const tags = ref([])
const importing = ref(false)
const showPaste = ref(false)
const pasteText = ref('')
const fileName = ref('')
const fileContent = ref('')
const newCategory = reactive({ name: '', color: '#a2762a', description: '' })

const hasImportText = computed(() => Boolean((fileContent.value || pasteText.value).trim()))

async function load() {
  try {
    const [cats, tagList] = await Promise.all([api.categories.list(), api.tags.list()])
    categories.value = cats
    tags.value = tagList
  } catch (e) {
    /* ignore */
  }
}

function onFile(uploadFile) {
  const raw = uploadFile.raw
  if (!raw) return
  fileName.value = raw.name
  const reader = new FileReader()
  reader.onload = () => {
    fileContent.value = String(reader.result || '')
    ElMessage.success('文件已读取，点击「开始导入」')
  }
  reader.readAsText(raw, 'utf-8')
}

function resetImport() {
  fileContent.value = ''
  pasteText.value = ''
  fileName.value = ''
}

async function doImport() {
  const text = (fileContent.value || pasteText.value).trim()
  if (!text) return
  let payload
  try {
    payload = JSON.parse(text)
  } catch (e) {
    ElMessage.error('JSON 格式不正确，请检查文件内容')
    return
  }
  importing.value = true
  try {
    const result = await api.data.import(payload)
    ElMessage.success(
      `导入完成：题目 ${result.questions} 道，面试记录 ${result.interviews} 条，跳过重复 ${result.skipped} 道`
    )
    resetImport()
    await Promise.all([load(), refreshMeta()])
  } catch (e) {
    /* ignore */
  } finally {
    importing.value = false
  }
}

async function addCategory() {
  if (!newCategory.name.trim()) return
  try {
    await api.categories.create({ ...newCategory, name: newCategory.name.trim() })
    ElMessage.success('分类已添加')
    newCategory.name = ''
    newCategory.description = ''
    await Promise.all([load(), refreshMeta()])
  } catch (e) {
    /* ignore */
  }
}

async function editCategory(row) {
  try {
    const { value } = await ElMessageBox.prompt('输入新的分类名称', '重命名分类', {
      inputValue: row.name,
      inputValidator: (v) => (v && v.trim() ? true : '名称不能为空')
    })
    await api.categories.update(row.id, { name: value.trim() })
    ElMessage.success('已更新')
    await Promise.all([load(), refreshMeta()])
  } catch (e) {
    /* ignore */
  }
}

async function deleteCategory(row) {
  try {
    await ElMessageBox.confirm(
      `删除分类「${row.name}」？该分类下的 ${row.questionCount} 道题会变成未分类，题目本身不会删除。`,
      '删除分类',
      { type: 'warning' }
    )
    await api.categories.remove(row.id)
    ElMessage.success('已删除')
    await Promise.all([load(), refreshMeta()])
  } catch (e) {
    /* ignore */
  }
}

async function renameTag(tag) {
  try {
    const { value } = await ElMessageBox.prompt(`把 #${tag.name} 重命名为：`, '重命名标签', { inputValue: tag.name })
    const target = (value || '').trim()
    if (!target || target === tag.name) return
    const result = await api.tags.rename(tag.name, target)
    ElMessage.success(`已更新 ${result.affected} 道题`)
    await Promise.all([load(), refreshMeta()])
  } catch (e) {
    /* ignore */
  }
}

async function deleteTag(tag) {
  try {
    await ElMessageBox.confirm(`从所有题目里移除标签 #${tag.name}？（${tag.count} 道题）`, '删除标签', {
      type: 'warning'
    })
    await api.tags.remove(tag.name)
    ElMessage.success('标签已删除')
    await Promise.all([load(), refreshMeta()])
  } catch (e) {
    /* ignore */
  }
}

async function clearDemo() {
  try {
    await ElMessageBox.confirm('只删除示例数据，自己记录的题目不受影响。确定继续？', '清除示例数据', {
      type: 'warning'
    })
    const result = await api.data.clearDemo()
    ElMessage.success(`已清除：${result.questions} 道题、${result.interviews} 条面试记录`)
    await Promise.all([load(), refreshMeta()])
  } catch (e) {
    /* ignore */
  }
}

async function clearAll() {
  try {
    const { value } = await ElMessageBox.prompt('此操作不可恢复。请输入「清空」以确认。', '清空全部数据', {
      confirmButtonText: '确认清空',
      inputPlaceholder: '清空'
    })
    if (value !== '清空') {
      ElMessage.warning('输入不匹配，已取消')
      return
    }
    const result = await api.data.clearAll()
    ElMessage.success(`已清空 ${result.questions} 道题、${result.interviews} 条面试记录`)
    await Promise.all([load(), refreshMeta()])
  } catch (e) {
    /* ignore */
  }
}

onMounted(load)
</script>
