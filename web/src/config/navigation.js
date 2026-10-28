import { AlarmClock, ChatLineSquare, Collection, Compass, DataAnalysis, House, Setting } from '@element-plus/icons-vue'

/**
 * 面试记忆助手的导航信息架构：捕获 → 回想 → 复盘。
 * 路由保持不变，避免已有链接、书签和浏览器历史失效。
 */
export const NAV_GROUPS = Object.freeze([
  {
    label: '每日循环',
    items: [
      { to: '/', label: '今日', icon: House },
      { to: '/review', label: '记忆训练', icon: AlarmClock, badge: true }
    ]
  },
  {
    label: '知识资产',
    items: [
      { to: '/questions', label: '题库', icon: Collection },
      { to: '/interviews', label: '面试档案', icon: ChatLineSquare }
    ]
  },
  {
    label: '复盘洞察',
    items: [{ to: '/insights', label: '成长洞察', icon: DataAnalysis }]
  },
  {
    label: '系统',
    items: [
      { to: '/method', label: '记忆方法', icon: Compass },
      { to: '/data', label: '设置与数据', icon: Setting }
    ]
  }
])

export const NAV_ITEMS = NAV_GROUPS.flatMap((group) => group.items)
