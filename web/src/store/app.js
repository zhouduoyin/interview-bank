import { reactive } from 'vue'
import { api } from '@/api'

const savedTheme = localStorage.getItem('ib-theme') || 'light'
const savedCollapsed = localStorage.getItem('ib-collapsed') === '1'

export const appState = reactive({
  categories: [],
  tags: [],
  dueCount: 0,
  streak: 0,
  theme: savedTheme,
  collapsed: savedCollapsed
})

export function applyTheme() {
  const dark = appState.theme === 'dark'
  document.documentElement.classList.toggle('dark', dark)
  localStorage.setItem('ib-theme', appState.theme)
}

export function toggleTheme() {
  appState.theme = appState.theme === 'dark' ? 'light' : 'dark'
  applyTheme()
}

export function toggleCollapsed() {
  appState.collapsed = !appState.collapsed
  localStorage.setItem('ib-collapsed', appState.collapsed ? '1' : '0')
}

export async function refreshMeta() {
  try {
    const [categories, tags, queue] = await Promise.all([api.categories.list(), api.tags.list(), api.review.queue(1)])
    appState.categories = categories
    appState.tags = tags
    appState.dueCount = queue.dueCount
  } catch (e) {
    /* 后端未启动时保持空态 */
  }
}

export async function refreshDue() {
  try {
    const queue = await api.review.queue(1)
    appState.dueCount = queue.dueCount
  } catch (e) {
    /* ignore */
  }
}

applyTheme()
