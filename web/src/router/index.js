import { createRouter, createWebHistory } from 'vue-router'
import { BRAND } from '@/config/brand'

const routes = [
  {
    path: '/',
    name: 'dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '今日', subtitle: '先回想，再记录，把今天学到的留住' }
  },
  {
    path: '/questions',
    name: 'questions',
    component: () => import('@/views/QuestionList.vue'),
    meta: { title: '题库', subtitle: '所有被问过的问题，都在这里' }
  },
  {
    path: '/questions/new',
    name: 'question-new',
    component: () => import('@/views/QuestionEditor.vue'),
    meta: { title: '记录新题', subtitle: '趁记忆还新鲜，把刚被问到的题留下来' }
  },
  {
    path: '/questions/:id/edit',
    name: 'question-edit',
    component: () => import('@/views/QuestionEditor.vue'),
    meta: { title: '编辑题目', subtitle: '补充答案、标签和记忆状态' }
  },
  {
    path: '/questions/:id',
    name: 'question-detail',
    component: () => import('@/views/QuestionDetail.vue'),
    meta: { title: '题目详情', subtitle: '看答案、做回想、关联面试档案' }
  },
  {
    path: '/review',
    name: 'review',
    component: () => import('@/views/Review.vue'),
    meta: { title: '记忆训练', subtitle: '主动回想一次，比重复阅读更有效' }
  },
  {
    path: '/method',
    name: 'method',
    component: () => import('@/views/Method.vue'),
    meta: { title: '记忆方法', subtitle: '理解间隔回想，知道下一步该做什么' }
  },
  {
    path: '/interviews',
    name: 'interviews',
    component: () => import('@/views/InterviewList.vue'),
    meta: { title: '面试档案', subtitle: '每一场面试，都是下一次的素材' }
  },
  {
    path: '/interviews/new',
    name: 'interview-new',
    component: () => import('@/views/InterviewEditor.vue'),
    meta: { title: '新建面试档案', subtitle: '记下公司、轮次和现场被问到的题' }
  },
  {
    path: '/interviews/:id/edit',
    name: 'interview-edit',
    component: () => import('@/views/InterviewEditor.vue'),
    meta: { title: '编辑面试档案', subtitle: '补充复盘和关联题目' }
  },
  {
    path: '/interviews/:id',
    name: 'interview-detail',
    component: () => import('@/views/InterviewDetail.vue'),
    meta: { title: '面试详情', subtitle: '复盘这场面试的表现' }
  },
  {
    path: '/insights',
    name: 'insights',
    component: () => import('@/views/Insights.vue'),
    meta: { title: '成长洞察', subtitle: '看清记忆结构和需要加固的方向' }
  },
  {
    path: '/data',
    name: 'data',
    component: () => import('@/views/DataCenter.vue'),
    meta: { title: '设置与数据', subtitle: '导入导出、分类标签与本地数据' }
  },
  {
    path: '/styleguide',
    name: 'styleguide',
    component: () => import('@/views/StyleGuide.vue'),
    meta: { title: '设计规范', subtitle: `${BRAND.name} · Design System v1.0` }
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · ${BRAND.name}` : `${BRAND.name} · ${BRAND.english}`
})

export default router
