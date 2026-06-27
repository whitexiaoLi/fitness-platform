import { onMounted, onUnmounted } from 'vue'

/**
 * Scroll-triggered reveal animation using Intersection Observer.
 * Adds 'revealed' class when element enters viewport.
 *
 * Usage:
 *   const el = ref(null)
 *   useScrollReveal(el, { delay: 100, threshold: 0.15 })
 *   <div ref="el" class="reveal-item">...</div>
 */
export function useScrollReveal(elRef, options = {}) {
  const { delay = 0, threshold = 0.1, rootMargin = '0px 0px -30px 0px' } = options
  let observer = null

  onMounted(() => {
    const el = elRef?.value || elRef
    if (!el) return

    if (delay) el.style.transitionDelay = delay + 'ms'

    observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('revealed')
          observer.unobserve(entry.target)
        }
      })
    }, { threshold, rootMargin })

    observer.observe(el)
  })

  onUnmounted(() => {
    observer?.disconnect()
  })
}
