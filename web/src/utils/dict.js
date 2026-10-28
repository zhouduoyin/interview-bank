/* 语义色全部走 CSS 变量，深浅色主题自动适配。 */
export const DIFFICULTY = {
  EASY: {
    label: '简单',
    color: 'var(--accent-2)',
    bg: 'color-mix(in srgb, var(--accent-2) 12%, transparent)',
    dot: 'var(--accent-2)'
  },
  MEDIUM: {
    label: '中等',
    color: 'var(--accent-3)',
    bg: 'color-mix(in srgb, var(--accent-3) 13%, transparent)',
    dot: 'var(--accent-3)'
  },
  HARD: {
    label: '困难',
    color: 'var(--primary)',
    bg: 'color-mix(in srgb, var(--primary) 12%, transparent)',
    dot: 'var(--primary)'
  }
}

export const MASTERY = {
  NEW: {
    label: '未掌握',
    color: 'var(--text-3)',
    bg: 'color-mix(in srgb, var(--text-3) 12%, transparent)',
    dot: 'var(--text-3)'
  },
  LEARNING: {
    label: '复习中',
    color: 'var(--accent-4)',
    bg: 'color-mix(in srgb, var(--accent-4) 12%, transparent)',
    dot: 'var(--accent-4)'
  },
  MASTERED: {
    label: '已掌握',
    color: 'var(--accent-2)',
    bg: 'color-mix(in srgb, var(--accent-2) 12%, transparent)',
    dot: 'var(--accent-2)'
  }
}

export const RATING = {
  FORGOT: {
    label: '忘记了',
    short: '忘记',
    key: '1',
    color: 'var(--primary)',
    bg: 'color-mix(in srgb, var(--primary) 12%, transparent)',
    hint: '明天再来'
  },
  HARD: {
    label: '有点难',
    short: '困难',
    key: '2',
    color: 'var(--accent-3)',
    bg: 'color-mix(in srgb, var(--accent-3) 13%, transparent)',
    hint: '短期巩固'
  },
  GOOD: {
    label: '记得',
    short: '记得',
    key: '3',
    color: 'var(--accent-4)',
    bg: 'color-mix(in srgb, var(--accent-4) 12%, transparent)',
    hint: '正常间隔'
  },
  EASY: {
    label: '很简单',
    short: '简单',
    key: '4',
    color: 'var(--accent-2)',
    bg: 'color-mix(in srgb, var(--accent-2) 12%, transparent)',
    hint: '拉长间隔'
  }
}

export const RESULT = {
  PENDING: {
    label: '待定',
    color: 'var(--text-3)',
    bg: 'color-mix(in srgb, var(--text-3) 12%, transparent)'
  },
  PASS: {
    label: '通过',
    color: 'var(--accent-2)',
    bg: 'color-mix(in srgb, var(--accent-2) 12%, transparent)'
  },
  FAIL: {
    label: '未通过',
    color: 'var(--primary)',
    bg: 'color-mix(in srgb, var(--primary) 12%, transparent)'
  }
}

export const ROUNDS = ['一面', '二面', '三面', '四面', 'HR 面', '笔试', '交叉面', '其他']
export const CHANNELS = ['BOSS 直聘', '拉勾', '内推', '官网投递', '猎头', '校招', '其他']

export function meta(map, key, fallback = {}) {
  return map[key] || fallback
}
