import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import EmptyState from '@/components/EmptyState.vue'

describe('EmptyState', () => {
  it('renders title, description and action slot', () => {
    const wrapper = mount(EmptyState, {
      props: {
        title: '还没有题目',
        description: '记录第一道面试题。'
      },
      slots: {
        action: '<button class="test-action">去记录</button>'
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(wrapper.text()).toContain('还没有题目')
    expect(wrapper.text()).toContain('记录第一道面试题。')
    expect(wrapper.find('.test-action').text()).toBe('去记录')
  })
})
