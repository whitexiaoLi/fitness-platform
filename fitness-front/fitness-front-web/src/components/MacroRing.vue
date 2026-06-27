<template>
  <div class="macro-ring" :style="{ '--ring-color': color }">
    <svg viewBox="0 0 100 100" class="ring-svg">
      <circle cx="50" cy="50" r="42" fill="none" stroke="var(--color-border-light)" stroke-width="6" />
      <circle
        cx="50" cy="50" r="42"
        fill="none"
        :stroke="color"
        stroke-width="6"
        stroke-linecap="round"
        :stroke-dasharray="circumference"
        :stroke-dashoffset="dashOffset"
        class="ring-fill"
        transform="rotate(-90 50 50)"
      />
    </svg>
    <div class="ring-center">
      <span class="ring-value" ref="valueRef">{{ displayValue }}</span>
      <span class="ring-unit">{{ unit }}</span>
      <span class="ring-label">{{ label }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'

const props = defineProps({
  value: { type: Number, default: 0 },
  max: { type: Number, default: 100 },
  label: { type: String, default: '' },
  unit: { type: String, default: 'g' },
  color: { type: String, default: '#667eea' },
  decimals: { type: Number, default: 0 },
})

const circumference = 2 * Math.PI * 42 // ≈ 263.89
const dashOffset = computed(() => {
  const pct = Math.min(props.value / props.max, 1)
  return circumference * (1 - pct)
})

const valueRef = ref(null)
const displayValue = ref('0')

function format(v) {
  return props.decimals > 0 ? v.toFixed(props.decimals) : String(Math.round(v))
}

onMounted(() => {
  const target = props.value
  if (target === 0) { displayValue.value = format(0); return }
  const duration = 500
  const start = performance.now()
  function step(now) {
    const elapsed = now - start
    const progress = Math.min(elapsed / duration, 1)
    const eased = 1 - (1 - progress) * (1 - progress)
    displayValue.value = format(target * eased)
    if (progress < 1) requestAnimationFrame(step)
    else displayValue.value = format(target)
  }
  requestAnimationFrame(step)
})
</script>

<style scoped>
.macro-ring {
  position: relative;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ring-svg { width: 100%; height: 100%; }
.ring-fill { transition: stroke-dashoffset 0.6s var(--ease-out); }
.ring-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0;
}
.ring-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-title);
  line-height: 1.2;
}
.ring-unit {
  font-size: 10px;
  color: var(--color-text-secondary);
  font-weight: 500;
}
.ring-label {
  font-size: 10px;
  color: var(--color-text-weak);
  margin-top: 1px;
}
</style>
