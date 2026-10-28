<template>
  <div class="page questions-page">
    <div class="page-toolbar">
      <div class="page-count">
        <b>{{ result.totalElements }}</b> 道题
        <span v-if="activeFilterCount" class="muted">（已筛选 {{ activeFilterCount }} 个条件）</span>
      </div>
      <div class="page-toolbar-actions">
        <div class="view-switch">
          <button type="button" :class="{ active: view === 'card' }" title="卡片视图" @click="setView('card')">
            <el-icon><Grid /></el-icon>
          </button>
          <button type="button" :class="{ active: view === 'table' }" title="表格视图" @click="setView('table')">
            <el-icon><List /></el-icon>
          </button>
        </div>
        <el-button type="primary" @click="$router.push('/questions/new')">
          <el-icon><Plus /></el-icon>新建题目
        </el-button>
      </div>
    </div>

    <div class="card filter-card">
      <div class="filter-row">
        <el-input
          v-model="filters.keyword"
          class="filter-search"
          placeholder="搜索标题、内容、答案或公司…"
          clearable
          :prefix-icon="Search"
          @input="onKeyword"
          @clear="reload"
        />
        <el-select
          v-model="filters.difficulty"
          placeholder="难度"
          clearable
          class="filter-select narrow"
          @change="reload"
        >
          <el-option v-for="(v, k) in DIFFICULTY" :key="k" :label="v.label" :value="k" />
        </el-select>
        <el-select v-model="filters.mastery" placeholder="掌握程度" clearable class="filter-select" @change="reload">
          <el-option v-for="(v, k) in MASTERY" :key="k" :label="v.label" :value="k" />
        </el-select>
        <el-select
          v-model="filters.tag"
          placeholder="标签"
          clearable
          filterable
          class="filter-select narrow"
          @change="reload"
        >
          <el-option v-for="t in appState.tags" :key="t.name" :label="`${t.name} (${t.count})`" :value="t.name" />
        </el-select>
        <el-select v-model="filters.sort" class="filter-select narrow" @change="reload">
          <el-option label="最近更新" value="updated" />
          <el-option label="最近创建" value="created" />
          <el-option label="复习次数" value="reviews" />
          <el-option label="优先复习" value="due" />
        </el-select>
        <el-checkbox v-model="filters.starred" class="filter-star" @change="reload">只看收藏</el-checkbox>
      </div>
    </div>

    <div class="question-browser">
      <aside class="category-rail" aria-label="题库分类">
        <div class="category-rail-head">
          <span>题库分类</span>
          <b>{{ appState.categories.length }} 类</b>
        </div>
        <button
          type="button"
          class="category-item"
          :class="{ active: !filters.categoryId }"
          :aria-pressed="!filters.categoryId"
          @click="selectCategory(null)"
        >
          <i class="cat-dot all" />
          <span class="cat-name">全部题目</span>
          <span class="cat-count">{{ totalCategoryCount }}</span>
        </button>
        <button
          v-for="c in appState.categories"
          :key="c.id"
          type="button"
          class="category-item"
          :class="{ active: filters.categoryId === c.id }"
          :aria-pressed="filters.categoryId === c.id"
          @click="selectCategory(c.id)"
        >
          <i class="cat-dot" :style="{ background: c.color || 'var(--text-3)' }" />
          <span class="cat-name">{{ c.name }}</span>
          <span class="cat-count">{{ c.questionCount || 0 }}</span>
        </button>
        <router-link to="/data" class="category-manage">管理分类 <span>→</span></router-link>
      </aside>

      <div class="question-results">
        <div v-if="view === 'card'" v-loading="loading">
          <div v-if="result.content.length" class="question-grid">
            <article v-for="q in result.content" :key="q.id" class="q-card" @click="open(q.id)">
              <div class="q-card-top">
                <span class="cat-pill clickable" :style="catStyle(q)" @click.stop="openCategory(q)">
                  <i class="dot" :style="{ background: q.category?.color || 'var(--text-3)' }" />
                  {{ q.category?.name || '未分类' }}
                </span>
                <div class="q-card-actions" @click.stop>
                  <button type="button" class="star-btn" :class="{ on: q.starred }" title="收藏" @click="toggleStar(q)">
                    <el-icon><StarFilled v-if="q.starred" /><Star v-else /></el-icon>
                  </button>
                  <el-dropdown trigger="click" @command="(cmd) => onCommand(cmd, q)">
                    <button type="button" class="more-btn">
                      <el-icon><MoreFilled /></el-icon>
                    </button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="edit">编辑</el-dropdown-item>
                        <el-dropdown-item command="MASTERED" divided>标记为已掌握</el-dropdown-item>
                        <el-dropdown-item command="LEARNING">标记为复习中</el-dropdown-item>
                        <el-dropdown-item command="NEW">重置为未掌握</el-dropdown-item>
                        <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>

              <h3 class="q-title">{{ q.title }}</h3>
              <p v-if="q.excerpt" class="q-excerpt">{{ q.excerpt }}</p>

              <div class="q-tags">
                <span v-for="t in q.tags.slice(0, 3)" :key="t" class="tag-chip" @click.stop="openTag(t)">#{{ t }}</span>
                <span v-if="q.tags.length > 3" class="tag-more">+{{ q.tags.length - 3 }}</span>
              </div>

              <div class="q-footer">
                <span class="mini-pill" :style="diffStyle(q.difficulty)">{{ diffLabel(q.difficulty) }}</span>
                <span class="mini-pill" :style="masteryStyle(q.mastery)">{{ masteryLabel(q.mastery) }}</span>
                <span v-if="isDue(q)" class="due-flag"><i />待复习</span>
                <span v-else class="q-meta">{{ q.reviewCount }} 次复习</span>
                <span v-if="q.sourceCompany" class="q-company">{{ q.sourceCompany }}</span>
              </div>
            </article>
          </div>
          <EmptyState
            v-else-if="!loading"
            title="没有找到题目"
            :description="hasFilter ? '试试调整筛选条件，或换一个关键词。' : '题库还是空的，先记录第一道题吧。'"
          >
            <template #action>
              <el-button v-if="hasFilter" @click="clearFilters">清空筛选</el-button>
              <el-button v-else type="primary" @click="$router.push('/questions/new')">记录第一道题</el-button>
            </template>
          </EmptyState>
        </div>

        <div v-else class="card table-card">
          <div v-if="selection.length" class="batch-bar">
            <span
              >已选 <b>{{ selection.length }}</b> 题</span
            >
            <el-button size="small" @click="batch('mastery', 'MASTERED')">标记已掌握</el-button>
            <el-button size="small" @click="batch('star')">加入收藏</el-button>
            <el-button size="small" type="danger" plain @click="batchDelete">删除</el-button>
            <el-button size="small" text @click="tableRef?.clearSelection()">取消选择</el-button>
          </div>
          <el-table
            ref="tableRef"
            v-loading="loading"
            :data="result.content"
            row-key="id"
            @selection-change="(rows) => (selection = rows)"
          >
            <el-table-column type="selection" width="44" />
            <el-table-column label="题目" min-width="300">
              <template #default="{ row }">
                <div class="table-title">
                  <button
                    type="button"
                    class="star-btn small"
                    :class="{ on: row.starred }"
                    @click.stop="toggleStar(row)"
                  >
                    <el-icon><StarFilled v-if="row.starred" /><Star v-else /></el-icon>
                  </button>
                  <router-link :to="`/questions/${row.id}`" class="table-link">{{ row.title }}</router-link>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="分类" width="120">
              <template #default="{ row }">
                <span class="cat-pill simple clickable" @click.stop="openCategory(row)">
                  <i class="dot" :style="{ background: row.category?.color || 'var(--text-3)' }" />
                  {{ row.category?.name || '未分类' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="难度" width="90">
              <template #default="{ row }">
                <span class="mini-pill" :style="diffStyle(row.difficulty)">{{ diffLabel(row.difficulty) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="掌握" width="100">
              <template #default="{ row }">
                <span class="mini-pill" :style="masteryStyle(row.mastery)">{{ masteryLabel(row.mastery) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="复习" width="90">
              <template #default="{ row }">
                <span :class="{ 'due-text': isDue(row) }">{{ isDue(row) ? '待复习' : `${row.reviewCount} 次` }}</span>
              </template>
            </el-table-column>
            <el-table-column label="更新时间" width="120">
              <template #default="{ row }">
                <span class="muted">{{ fromNow(row.updatedAt) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="$router.push(`/questions/${row.id}`)"
                  >查看</el-button
                >
                <el-button link size="small" @click="$router.push(`/questions/${row.id}/edit`)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-if="result.totalPages > 1" class="pagination-row">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="result.totalElements"
            :page-size="size"
            :current-page="page + 1"
            @current-change="changePage"
          />
          <span class="muted page-info">第 {{ page + 1 }} / {{ result.totalPages }} 页</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Grid, List, Star, StarFilled, MoreFilled } from '@element-plus/icons-vue'
import EmptyState from '@/components/EmptyState.vue'
import { api } from '@/api'
import { appState, refreshMeta } from '@/store/app'
import { DIFFICULTY, MASTERY } from '@/utils/dict'
import { fromNow } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const view = ref(localStorage.getItem('ib-view') || 'card')
const page = ref(0)
const size = 9
const result = ref({ content: [], totalElements: 0, totalPages: 0 })
const selection = ref([])
const tableRef = ref(null)
const filters = reactive({
  keyword: '',
  categoryId: null,
  difficulty: null,
  mastery: null,
  tag: null,
  starred: false,
  sort: 'updated'
})

let timer = null

const hasFilter = computed(() =>
  Boolean(
    filters.keyword || filters.categoryId || filters.difficulty || filters.mastery || filters.tag || filters.starred
  )
)
const activeFilterCount = computed(
  () =>
    [filters.keyword, filters.categoryId, filters.difficulty, filters.mastery, filters.tag, filters.starred].filter(
      Boolean
    ).length
)
const totalCategoryCount = computed(() =>
  appState.categories.reduce((sum, category) => sum + (Number(category.questionCount) || 0), 0)
)

function catStyle(q) {
  const color = q.category?.color || 'var(--text-3)'
  return {
    color: `color-mix(in srgb, ${color} 72%, var(--text))`,
    background: `color-mix(in srgb, ${color} 12%, transparent)`
  }
}
function diffStyle(value) {
  const d = DIFFICULTY[value] || DIFFICULTY.MEDIUM
  return { color: d.color, background: d.bg }
}
function diffLabel(value) {
  return (DIFFICULTY[value] || DIFFICULTY.MEDIUM).label
}
function masteryStyle(value) {
  const m = MASTERY[value] || MASTERY.NEW
  return { color: m.color, background: m.bg }
}
function masteryLabel(value) {
  return (MASTERY[value] || MASTERY.NEW).label
}
function isDue(q) {
  if (!q.nextReviewAt) return q.reviewCount === 0
  return new Date(q.nextReviewAt).getTime() <= Date.now()
}

function load() {
  loading.value = true
  api.questions
    .list({
      keyword: filters.keyword.trim() || null,
      categoryId: filters.categoryId,
      difficulty: filters.difficulty,
      mastery: filters.mastery,
      tag: filters.tag,
      starred: filters.starred ? true : null,
      sort: filters.sort,
      page: page.value,
      size
    })
    .then((data) => {
      result.value = data
    })
    .catch(() => {})
    .finally(() => {
      loading.value = false
    })
}

function reload() {
  page.value = 0
  load()
}
function changePage(p) {
  page.value = p - 1
  load()
}
function onKeyword() {
  clearTimeout(timer)
  timer = setTimeout(reload, 350)
}
function setView(v) {
  view.value = v
  localStorage.setItem('ib-view', v)
}
function clearFilters() {
  Object.assign(filters, { keyword: '', categoryId: null, difficulty: null, mastery: null, tag: null, starred: false })
  if (Object.keys(route.query).length) {
    router.replace({ path: '/questions' })
  } else {
    reload()
  }
}

function applyRouteQuery() {
  const categoryId = Number(route.query.category)
  filters.keyword = route.query.keyword ? String(route.query.keyword) : ''
  filters.tag = route.query.tag ? String(route.query.tag) : null
  filters.categoryId = Number.isInteger(categoryId) && categoryId > 0 ? categoryId : null
  filters.starred = route.query.starred === '1'
}

function selectCategory(id) {
  const categoryId = id ?? null
  filters.categoryId = categoryId
  filters.keyword = ''
  filters.tag = null
  filters.starred = false
  page.value = 0

  const query = categoryId ? { category: String(categoryId) } : {}
  const alreadyOnUrl =
    String(route.query.category || '') === String(categoryId || '') &&
    !route.query.keyword &&
    !route.query.tag &&
    route.query.starred !== '1'

  if (alreadyOnUrl) {
    load()
    return
  }
  router.replace({ path: '/questions', query })
}

function open(id) {
  router.push(`/questions/${id}`)
}
function openCategory(q) {
  if (q.category?.id) selectCategory(q.category.id)
}
function openTag(tag) {
  router.push({ path: '/questions', query: { tag } })
}

async function toggleStar(q) {
  try {
    const updated = await api.questions.star(q.id)
    q.starred = updated.starred
  } catch (e) {
    /* ignore */
  }
}

async function onCommand(cmd, q) {
  if (cmd === 'edit') return router.push(`/questions/${q.id}/edit`)
  if (cmd === 'delete') return removeOne(q)
  try {
    await api.questions.mastery(q.id, cmd)
    q.mastery = cmd
    ElMessage.success('掌握程度已更新')
  } catch (e) {
    /* ignore */
  }
}

async function removeOne(q) {
  try {
    await ElMessageBox.confirm(`删除「${q.title}」？删除后不可恢复。`, '删除题目', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger'
    })
    await api.questions.remove(q.id)
    ElMessage.success('已删除')
    await Promise.all([load(), refreshMeta()])
  } catch (e) {
    /* 取消 */
  }
}

async function batch(action, value) {
  const ids = selection.value.map((r) => r.id)
  if (!ids.length) return
  try {
    await api.questions.batch({ ids, action, mastery: value })
    ElMessage.success('批量操作完成')
    tableRef.value?.clearSelection()
    await load()
  } catch (e) {
    /* ignore */
  }
}

async function batchDelete() {
  const ids = selection.value.map((r) => r.id)
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${ids.length} 道题吗？`, '批量删除', { type: 'warning' })
    await api.questions.batch({ ids, action: 'delete' })
    ElMessage.success('已删除')
    tableRef.value?.clearSelection()
    await Promise.all([load(), refreshMeta()])
  } catch (e) {
    /* ignore */
  }
}

onMounted(() => {
  applyRouteQuery()
  load()
})

watch(
  () => route.query,
  () => {
    applyRouteQuery()
    page.value = 0
    load()
  }
)
</script>
