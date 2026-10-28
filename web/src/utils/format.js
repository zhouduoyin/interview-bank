import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

export { dayjs }

export function fmtDate(value, pattern = 'YYYY-MM-DD') {
  if (!value) return '—'
  return dayjs(value).format(pattern)
}

export function fmtDateTime(value) {
  if (!value) return '—'
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

export function fromNow(value) {
  if (!value) return '—'
  return dayjs(value).fromNow()
}

export function dueLabel(nextReviewAt, reviewCount = 0) {
  if (!nextReviewAt) return reviewCount === 0 ? '还没复习过' : '待安排'
  const target = dayjs(nextReviewAt)
  const now = dayjs()
  if (target.isBefore(now)) return '现在该复习了'
  const hours = target.diff(now, 'hour')
  if (hours < 1) return '不到 1 小时后复习'
  if (hours < 24) return `${hours} 小时后复习`
  return `${target.diff(now, 'day')} 天后复习`
}

export function weekdayCN(date) {
  return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][dayjs(date).day()]
}

export function pct(part, total) {
  if (!total) return 0
  return Math.round((part / total) * 100)
}

export function greeting() {
  const hour = dayjs().hour()
  if (hour < 6) return '夜深了'
  if (hour < 11) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
}

export function initials(name = '') {
  return name.trim().slice(0, 2) || '面'
}

const COLORS = ['#b8432a', '#3f5b45', '#2e5a74', '#8a6420', '#a34a5f', '#6b5b95', '#8a5a2b', '#2f7d7a']

export function colorOf(name = '') {
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = (hash * 31 + name.charCodeAt(i)) % 997
  return COLORS[hash % COLORS.length]
}

export function maskMarkdown(text, limit = 100) {
  if (!text) return ''
  const clean = String(text)
    .replace(/```[\s\S]*?```/g, ' [代码] ')
    .replace(/[#>*`[\]()!|_-]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
  return clean.length > limit ? `${clean.slice(0, limit)}…` : clean
}
