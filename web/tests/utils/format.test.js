import { describe, expect, it } from 'vitest'
import { colorOf, dueLabel, fmtDate, greeting, initials, maskMarkdown, pct, weekdayCN } from '@/utils/format'

describe('format utils', () => {
  it('formats empty and normal dates', () => {
    expect(fmtDate(null)).toBe('—')
    expect(fmtDate('2026-10-28')).toBe('2026-10-28')
    expect(fmtDate('2026-10-28', 'MM/DD')).toBe('10/28')
  })

  it('calculates percentages safely', () => {
    expect(pct(1, 0)).toBe(0)
    expect(pct(1, 2)).toBe(50)
    expect(pct(2, 3)).toBe(67)
  })

  it('returns recall labels for missing schedules', () => {
    expect(dueLabel(null, 0)).toBe('还没开始')
    expect(dueLabel(null, 2)).toBe('待安排')
  })

  it('handles weekday, initials and greeting', () => {
    expect(weekdayCN('2026-10-28')).toBe('周三')
    expect(initials('周多银')).toBe('周多')
    expect(initials('')).toBe('面')
    expect(['夜深了', '早上好', '中午好', '下午好', '晚上好']).toContain(greeting())
  })

  it('strips markdown for previews', () => {
    expect(maskMarkdown('')).toBe('')
    expect(maskMarkdown('# 标题 **重点**')).not.toContain('#')
    expect(maskMarkdown('```js\nconst a = 1\n``` hello')).toContain('代码')
    expect(maskMarkdown('```js\nconst a = 1\n``` hello')).not.toContain('const')
    expect(maskMarkdown('abcdef', 3)).toBe('abc…')
  })

  it('assigns stable colors', () => {
    expect(colorOf('Redis')).toBe(colorOf('Redis'))
    expect(colorOf('')).toMatch(/^#/)
  })
})
