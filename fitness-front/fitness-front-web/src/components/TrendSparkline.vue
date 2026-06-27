<template>
  <div class="trend-sparkline" v-if="points.length > 0">
    <svg :viewBox="`0 0 ${w} ${h}`" class="sparkline-svg" preserveAspectRatio="none">
      <polyline
        :points="linePoints"
        fill="none"
        :stroke="color"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
        class="sparkline-line"
      />
      <polygon
        :points="areaPoints"
        :fill="color"
        fill-opacity="0.08"
      />
    </svg>
    <div class="sparkline-labels">
      <span class="sparklabel sparklabel-start">{{ firstLabel }}</span>
      <span class="sparklabel sparklabel-end">{{ lastLabel }}</span>
    </div>
  </div>
  <div v-else class="trend-empty">暂无趋势数据</div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: { type: Array, default: () => [] }, // [{date, value}, ...]
  color: { type: String, default: '#667eea' },
})

const w = 300
const h = 60
const padX = 2
const padY = 8

const points = computed(() => {
  if (!props.data || props.data.length === 0) return []
  return props.data.map(d => ({ date: d.recordDate || d.date, value: Number(d.value || d.weight || 0) }))
})

const linePoints = computed(() => {
  if (points.value.length === 0) return ''
  const vals = points.value.map(p => p.value)
  const min = Math.min(...vals)
  const max = Math.max(...vals)
  const range = max - min || 1
  return points.value.map((p, i) => {
    const x = padX + (i / Math.max(points.value.length - 1, 1)) * (w - padX * 2)
    const y = h - padY - ((p.value - min) / range) * (h - padY * 2)
    return `${x},${y}`
  }).join(' ')
})

const areaPoints = computed(() => {
  if (points.value.length === 0) return ''
  const line = linePoints.value
  return `${padX},${h} ${line} ${w - padX},${h}`
})

const firstLabel = computed(() => points.value[0]?.date?.slice(5) || '')
const lastLabel = computed(() => points.value[points.value.length - 1]?.date?.slice(5) || '')
</script>

<style scoped>
.trend-sparkline {
  background: var(--color-bg-page);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 8px 12px 4px;
}
.sparkline-svg { width: 100%; height: 60px; display: block; }
.sparkline-line {
  stroke-dasharray: 1000;
  stroke-dashoffset: 1000;
  animation: sparkline-draw 1s var(--ease-out) forwards;
}
@keyframes sparkline-draw {
  to { stroke-dashoffset: 0; }
}
.sparkline-labels {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--color-text-weak);
  padding: 2px 0 0;
}
.trend-empty {
  text-align: center;
  padding: 16px;
  font-size: 13px;
  color: var(--color-text-weak);
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-md);
}
</style>
