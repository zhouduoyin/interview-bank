<template>
  <div class="stat-card" :class="`tone-${tone}`">
    <div class="stat-icon">
      <el-icon><component :is="icon" /></el-icon>
    </div>
    <div class="stat-main">
      <div class="stat-label">{{ label }}</div>
      <div class="stat-value">
        {{ value }}<span v-if="suffix" class="stat-suffix">{{ suffix }}</span>
      </div>
      <div v-if="hint" class="stat-hint">{{ hint }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  label: { type: String, required: true },
  value: { type: [Number, String], default: 0 },
  suffix: { type: String, default: '' },
  hint: { type: String, default: '' },
  icon: { type: [Object, Function], default: null },
  tone: { type: String, default: 'indigo' }
})
</script>
