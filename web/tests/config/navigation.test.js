import { describe, expect, it } from 'vitest'
import { BRAND } from '@/config/brand'
import { NAV_GROUPS, NAV_ITEMS } from '@/config/navigation'

describe('brand and navigation', () => {
  it('uses the product name consistently', () => {
    expect(BRAND.name).toBe('面试记忆助手')
    expect(BRAND.mark).toBe('忆')
    expect(BRAND.tagline).toContain('面试')
  })

  it('keeps the memory loop as the primary information architecture', () => {
    expect(NAV_GROUPS.map((group) => group.label)).toEqual(['每日循环', '知识资产', '复盘洞察', '系统'])
    expect(NAV_ITEMS.map((item) => item.label)).toEqual([
      '今日',
      '记忆训练',
      '题库',
      '面试档案',
      '成长洞察',
      '记忆方法',
      '设置与数据'
    ])
  })

  it('has a unique route for every navigation item', () => {
    const routes = NAV_ITEMS.map((item) => item.to)
    expect(new Set(routes).size).toBe(routes.length)
    expect(routes).toContain('/review')
    expect(routes).toContain('/interviews')
    expect(routes).toContain('/method')
  })
})
