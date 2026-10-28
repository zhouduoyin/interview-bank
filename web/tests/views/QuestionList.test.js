import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { createMemoryHistory, createRouter } from 'vue-router'
import QuestionList from '@/views/QuestionList.vue'
import { appState } from '@/store/app'
import { api } from '@/api'

vi.mock('@/api', () => ({
  api: {
    questions: {
      list: vi.fn().mockResolvedValue({ content: [], totalElements: 0, totalPages: 0 }),
      star: vi.fn(),
      mastery: vi.fn(),
      remove: vi.fn(),
      batch: vi.fn()
    }
  }
}))

function makeRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/questions', component: QuestionList },
      { path: '/questions/:id', component: { template: '<div />' } },
      { path: '/questions/:id/edit', component: { template: '<div />' } },
      { path: '/questions/new', component: { template: '<div />' } },
      { path: '/data', component: { template: '<div />' } }
    ]
  })
}

describe('QuestionList 分类导航', () => {
  beforeEach(() => {
    appState.categories = [
      { id: 1, name: 'Java 基础', color: '#a2762a', questionCount: 4 },
      { id: 2, name: 'Spring', color: '#3f5b45', questionCount: 3 }
    ]
  })

  it('渲染全部分类与每个分类的题目数量', async () => {
    const router = makeRouter()
    await router.push('/questions')
    await router.isReady()

    const wrapper = mount(QuestionList, { global: { plugins: [router, ElementPlus] } })
    await flushPromises()

    const items = wrapper.findAll('.category-item')
    expect(items).toHaveLength(3)
    expect(items[0].text()).toContain('全部题目')
    expect(items[0].text()).toContain('7')
    expect(items[1].text()).toContain('Java 基础')
    expect(items[1].text()).toContain('4')
    expect(items[2].text()).toContain('Spring')
  })

  it('点击分类后按分类请求，并同步到 URL', async () => {
    const router = makeRouter()
    await router.push('/questions')
    await router.isReady()

    const wrapper = mount(QuestionList, { global: { plugins: [router, ElementPlus] } })
    await flushPromises()

    await wrapper.findAll('.category-item')[1].trigger('click')
    await flushPromises()

    expect(api.questions.list).toHaveBeenLastCalledWith(expect.objectContaining({ categoryId: 1 }))
    expect(router.currentRoute.value.query.category).toBe('1')
    expect(wrapper.findAll('.category-item')[1].classes()).toContain('active')
  })

  it('题目没有描述时不显示占位文案', async () => {
    api.questions.list.mockResolvedValueOnce({
      content: [
        {
          id: 99,
          title: '只记录了问题和答案的题目',
          excerpt: '',
          category: { id: 1, name: 'JavaScript 核心', color: '#b8862f' },
          difficulty: 'MEDIUM',
          mastery: 'NEW',
          starred: false,
          tags: [],
          updatedAt: '2026-10-28T00:00:00Z',
          nextReviewAt: '2026-10-28T00:00:00Z'
        }
      ],
      totalElements: 1,
      totalPages: 1
    })

    const router = makeRouter()
    await router.push('/questions')
    await router.isReady()

    const wrapper = mount(QuestionList, { global: { plugins: [router, ElementPlus] } })
    await flushPromises()

    expect(wrapper.find('.q-card').exists()).toBe(true)
    expect(wrapper.text()).not.toContain('还没有填写题目描述')
  })

  it('切换表格视图时使用整行宽度', async () => {
    const router = makeRouter()
    await router.push('/questions')
    await router.isReady()

    const wrapper = mount(QuestionList, { global: { plugins: [router, ElementPlus] } })
    await flushPromises()

    await wrapper.find('button[title="表格视图"]').trigger('click')
    expect(wrapper.find('.question-browser').classes()).toContain('table-mode')
  })
})
