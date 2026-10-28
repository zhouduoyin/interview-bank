<template>
  <div class="heatmap">
    <div class="heatmap-months">
      <span v-for="m in months" :key="m.index" :style="{ gridColumn: m.index + 1 }">{{ m.label }}</span>
    </div>
    <div class="heatmap-body">
      <div class="heatmap-days">
        <span></span><span>一</span><span></span><span>三</span><span></span><span>五</span><span></span>
      </div>
      <div class="heatmap-grid">
        <div v-for="(week, wi) in weeks" :key="wi" class="heatmap-week">
          <div
            v-for="(cell, ci) in week"
            :key="ci"
            :class="['heatmap-cell', cell ? `lv-${level(cell.count)}` : 'is-empty']"
            :title="cell ? `${cell.date} · ${cell.count} 次回想` : ''"
          />
        </div>
      </div>
    </div>
    <div class="heatmap-legend">
      <span>少</span>
      <i class="heatmap-cell lv-0" />
      <i class="heatmap-cell lv-1" />
      <i class="heatmap-cell lv-2" />
      <i class="heatmap-cell lv-3" />
      <i class="heatmap-cell lv-4" />
      <span>多</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import dayjs from 'dayjs'

const props = defineProps({
  cells: { type: Array, default: () => [] }
})

const weeks = computed(() => {
  const list = props.cells || []
  if (!list.length) return []
  const pad = dayjs(list[0].date).day()
  const filled = [...Array(pad).fill(null), ...list]
  const rows = []
  for (let i = 0; i < filled.length; i += 7) rows.push(filled.slice(i, i + 7))
  return rows
})

const months = computed(() => {
  const result = []
  let last = ''
  weeks.value.forEach((week, index) => {
    const cell = week.find(Boolean)
    if (!cell) return
    const label = dayjs(cell.date).format('M月')
    if (label !== last) {
      result.push({ index, label })
      last = label
    }
  })
  return result
})

function level(count) {
  if (!count) return 0
  if (count <= 1) return 1
  if (count <= 3) return 2
  if (count <= 6) return 3
  return 4
}
</script>
