<template>
  <div class="md-editor" :class="{ compact }">
    <div class="md-toolbar">
      <div class="md-tools">
        <button
          v-for="tool in tools"
          :key="tool.label"
          type="button"
          class="md-tool"
          :title="tool.title"
          @click="tool.run"
        >
          <span v-html="tool.label" />
        </button>
      </div>
      <el-radio-group v-model="mode" size="small" class="md-mode">
        <el-radio-button value="edit">编辑</el-radio-button>
        <el-radio-button value="split">分屏</el-radio-button>
        <el-radio-button value="preview">预览</el-radio-button>
      </el-radio-group>
    </div>
    <div class="md-body" :class="`mode-${mode}`" :style="{ minHeight: `${minHeight}px` }">
      <textarea
        v-show="mode !== 'preview'"
        ref="textareaRef"
        v-model="text"
        class="md-textarea"
        :placeholder="placeholder"
        spellcheck="false"
      />
      <div v-if="mode !== 'edit'" class="md-preview">
        <MarkdownView v-if="text" :content="text" />
        <div v-else class="md-preview-empty">预览会实时显示在这里 ✍️</div>
      </div>
    </div>
    <div class="md-footer">
      <span>{{ text.length }} 字符</span>
      <span>支持 Markdown · 代码块 · 表格</span>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue'
import MarkdownView from './MarkdownView.vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '用 Markdown 记录题目、答案或复盘…' },
  minHeight: { type: Number, default: 220 },
  compact: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])
const mode = ref('edit')
const textareaRef = ref(null)

const text = computed({
  get: () => props.modelValue || '',
  set: (value) => emit('update:modelValue', value)
})

function applySelection(transform) {
  const el = textareaRef.value
  if (!el) return
  const start = el.selectionStart
  const end = el.selectionEnd
  const value = text.value
  const selected = value.slice(start, end)
  const result = transform(value, selected, start, end)
  text.value = result.value
  nextTick(() => {
    el.focus()
    el.setSelectionRange(result.start, result.end)
  })
}

function wrap(before, after = '') {
  applySelection((value, selected, start, end) => {
    const inner = selected || '文本'
    return {
      value: value.slice(0, start) + before + inner + after + value.slice(end),
      start: start + before.length,
      end: start + before.length + inner.length
    }
  })
}

function linePrefix(prefix) {
  applySelection((value, selected, start, end) => {
    const from = value.lastIndexOf('\n', start - 1) + 1
    const block = value.slice(from, end) || '内容'
    const next = block
      .split('\n')
      .map((line) => (line.startsWith(prefix) ? line : prefix + line))
      .join('\n')
    return {
      value: value.slice(0, from) + next + value.slice(end),
      start: from,
      end: from + next.length
    }
  })
}

function insertBlock(snippet) {
  applySelection((value, selected, start, end) => {
    const prefix = start > 0 && value[start - 1] !== '\n' ? '\n\n' : ''
    const block = `${prefix}${snippet}`
    return {
      value: value.slice(0, start) + block + value.slice(end),
      start: start + block.length,
      end: start + block.length
    }
  })
}

const tools = [
  { label: 'H2', title: '标题', run: () => linePrefix('## ') },
  { label: '<b>B</b>', title: '加粗', run: () => wrap('**', '**') },
  { label: '‹/›', title: '行内代码', run: () => wrap('`', '`') },
  { label: '```', title: '代码块', run: () => insertBlock('```java\n// 代码\n```') },
  { label: '•', title: '无序列表', run: () => linePrefix('- ') },
  { label: '❝', title: '引用', run: () => linePrefix('> ') },
  { label: '🔗', title: '链接', run: () => wrap('[', '](https://)') },
  { label: '—', title: '分割线', run: () => insertBlock('\n---\n') }
]
</script>
