<template>
  <div class="empty-state">
    <div class="empty-icon"><el-icon><component :is="icon" /></el-icon></div>
    <h3>{{ title }}</h3>
    <p>{{ description }}</p>
    <div class="empty-action"><slot name="action" /></div>
  </div>
</template>

<script setup>
import { Collection } from '@element-plus/icons-vue'

defineProps({
  title: { type: String, default: '还没有内容' },
  description: { type: String, default: '记录第一条内容，慢慢积累起来。' },
  icon: { type: [Object, Function], default: Collection }
})
</script>
