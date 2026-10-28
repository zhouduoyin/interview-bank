<template>
  <div class="app-shell" :class="{ 'is-collapsed': appState.collapsed }">
    <aside class="sidebar">
      <router-link to="/" class="brand">
        <span class="brand-mark">题</span>
        <span class="brand-text">
          <b>面试题库</b>
          <small>Interview Field Notes</small>
        </span>
      </router-link>

      <nav class="nav">
        <div class="nav-group-label">工作区</div>
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="nav-item"
          :class="{ active: isActive(item) }"
          :title="item.label"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span class="nav-label">{{ item.label }}</span>
          <span v-if="item.badge && appState.dueCount" class="nav-badge">{{ appState.dueCount }}</span>
        </router-link>
      </nav>

      <div class="sidebar-bottom">
        <div class="sidebar-streak">
          <span class="streak-flame">🔥</span>
          <div>
            <b>{{ appState.streak }} 天</b>
            <small>连续复习</small>
          </div>
        </div>
        <button class="theme-toggle" type="button" @click="toggleTheme">
          <el-icon><Sunny v-if="isDark" /><Moon v-else /></el-icon>
          <span>{{ isDark ? '浅色模式' : '深色模式' }}</span>
        </button>
        <router-link to="/styleguide" class="sidebar-link">设计规范 <span>↗</span></router-link>
        <div class="sidebar-tip">数据保存在本机 H2 数据库</div>
      </div>
    </aside>

    <div class="main">
      <header class="topbar">
        <button class="icon-btn" type="button" title="收起 / 展开侧栏" @click="toggleCollapsed">
          <el-icon><Expand v-if="appState.collapsed" /><Fold v-else /></el-icon>
        </button>

        <div class="topbar-title">
          <h1>{{ route.meta.title }}</h1>
          <span>{{ route.meta.subtitle }}</span>
        </div>

        <div class="topbar-actions">
          <div class="global-search">
            <el-icon class="search-icon"><Search /></el-icon>
            <input
              ref="searchRef"
              v-model="keyword"
              type="text"
              placeholder="搜索题目、公司、标签"
              @keyup.enter="doSearch"
            />
            <kbd>Ctrl K</kbd>
          </div>
          <span class="today-label">{{ today }}</span>
          <el-button type="primary" class="quick-btn" @click="quickAdd?.open()">
            <el-icon><Plus /></el-icon>
            <span>快速记题</span>
          </el-button>
        </div>
      </header>

      <main class="content">
        <slot />
      </main>
    </div>

    <QuickAddDialog ref="quickAdd" @saved="onSaved" />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Collection, DataAnalysis, House, ChatLineSquare, AlarmClock, Setting,
  Plus, Search, Fold, Expand, Moon, Sunny
} from '@element-plus/icons-vue'
import QuickAddDialog from '@/components/QuickAddDialog.vue'
import { appState, refreshMeta, toggleCollapsed, toggleTheme } from '@/store/app'
import { api } from '@/api'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const quickAdd = ref(null)
const searchRef = ref(null)
const keyword = ref('')

const navItems = [
  { to: '/', label: '工作台', icon: House },
  { to: '/questions', label: '题库', icon: Collection },
  { to: '/review', label: '今日复习', icon: AlarmClock, badge: true },
  { to: '/interviews', label: '面试记录', icon: ChatLineSquare },
  { to: '/insights', label: '数据洞察', icon: DataAnalysis },
  { to: '/data', label: '数据管理', icon: Setting }
]

const isDark = computed(() => appState.theme === 'dark')
const today = dayjs().format('M 月 D 日 dddd')

function isActive(item) {
  if (item.to === '/') return route.path === '/'
  return route.path === item.to || route.path.startsWith(item.to + '/')
}

function doSearch() {
  const q = keyword.value.trim()
  if (!q) return
  router.push({ path: '/questions', query: { keyword: q } })
}

function onSaved() {
  refreshMeta()
}

function handler(event) {
  const isMac = navigator.platform.toLowerCase().includes('mac')
  const mod = isMac ? event.metaKey : event.ctrlKey
  if (mod && event.key.toLowerCase() === 'k') {
    event.preventDefault()
    searchRef.value?.focus()
  }
  if (mod && event.key.toLowerCase() === 'n') {
    event.preventDefault()
    quickAdd.value?.open()
  }
}

onMounted(async () => {
  await refreshMeta()
  try {
    const stats = await api.dashboard()
    appState.streak = stats?.overview?.streakDays || 0
  } catch (e) {
    /* ignore */
  }
  window.addEventListener('keydown', handler)
})

onBeforeUnmount(() => window.removeEventListener('keydown', handler))
</script>
