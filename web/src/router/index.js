import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '工作台', subtitle: '今天也离 offer 更近一点' }
  },
  {
    path: '/questions',
    name: 'questions',
    component: () => import('@/views/QuestionList.vue'),
    meta: { title: '题库', subtitle: '所有记录过的面试题与参考答案' }
  },
  {
    path: '/questions/new',
    name: 'question-new',
    component: () => import('@/views/QuestionEditor.vue'),
    meta: { title: '记录一道题', subtitle: '把刚被问到的问题记下来，趁热打铁' }
  },
  {
    path: '/questions/:id/edit',
    name: 'question-edit',
    component: () => import('@/views/QuestionEditor.vue'),
    meta: { title: '编辑题目', subtitle: '补充答案、标签和掌握情况' }
  },
  {
    path: '/questions/:id',
    name: 'question-detail',
    component: () => import('@/views/QuestionDetail.vue'),
    meta: { title: '题目详情', subtitle: '看答案、做复习、关联面试记录' }
  },
  {
    path: '/review',
    name: 'review',
    component: () => import('@/views/Review.vue'),
    meta: { title: '今日复习', subtitle: '按遗忘曲线，把该复习的题过一遍' }
  },
  {
    path: '/interviews',
    name: 'interviews',
    component: () => import('@/views/InterviewList.vue'),
    meta: { title: '面试记录', subtitle: '每场面试都值得复盘' }
  },
  {
    path: '/interviews/new',
    name: 'interview-new',
    component: () => import('@/views/InterviewEditor.vue'),
    meta: { title: '新建面试记录', subtitle: '记下公司、轮次和现场被问到的题' }
  },
  {
    path: '/interviews/:id/edit',
    name: 'interview-edit',
    component: () => import('@/views/InterviewEditor.vue'),
    meta: { title: '编辑面试记录', subtitle: '补充复盘和关联题目' }
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
    meta: { title: '数据洞察', subtitle: '看清自己的复习结构和薄弱环节' }
  },
  {
    path: '/data',
    name: 'data',
    component: () => import('@/views/DataCenter.vue'),
    meta: { title: '数据管理', subtitle: '导入导出、分类标签与本地数据' }
  },
  {
    path: '/styleguide',
    name: 'styleguide',
    component: () => import('@/views/StyleGuide.vue'),
    meta: { title: '设计规范', subtitle: 'Interview Field Notes · Design System v1.0' }
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
  document.title = to.meta.title ? `${to.meta.title} · 面试题库` : '面试题库 · Interview Bank'
})

export default router
