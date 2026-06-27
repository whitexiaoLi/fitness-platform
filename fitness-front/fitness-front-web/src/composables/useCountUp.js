import { ref, onMounted, onUnmounted } from 'vue'

/**
 * Animate a number from 0 to target when the element enters viewport.
 * Usage: const { displayValue, elRef } = useCountUp(() => targetValue, { duration: 600 })
 */
export function useCountUp(getTarget, options = {}) {
  const { duration = 600, decimals = 0, threshold = 0.3 } = options
  const displayValue = ref(decimals > 0 ? '0.0' : '0')
  const elRef = ref(null)
  let observer = null
  let rafId = null
  let animated = false

  function animate() {
    if (animated) return
    animated = true
    const target = Number(getTarget()) || 0
    if (target === 0) { displayValue.value = format(target); return }

    const start = performance.now()
    function step(now) {
      const elapsed = now - start
      const progress = Math.min(elapsed / duration, 1)
      // ease-out quad
      const eased = 1 - (1 - progress) * (1 - progress)
      const current = target * eased
      displayValue.value = format(current)
      if (progress < 1) {
        rafId = requestAnimationFrame(step)
      } else {
        displayValue.value = format(target)
      }
    }
    rafId = requestAnimationFrame(step)
  }

  function format(v) {
    return decimals > 0 ? v.toFixed(decimals) : String(Math.round(v))
  }

  onMounted(() => {
    if (!elRef.value) return
    observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting) {
        animate()
        observer?.disconnect()
      }
    }, { threshold })
    observer.observe(elRef.value)
  })

  onUnmounted(() => {
    observer?.disconnect()
    if (rafId) cancelAnimationFrame(rafId)
  })

  return { displayValue, elRef }
}
