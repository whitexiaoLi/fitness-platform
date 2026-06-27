import { onMounted, onUnmounted } from 'vue'

/**
 * 3D perspective tilt that follows the mouse.
 * Usage: useTilt3D(elRef, { maxDeg: 8 })
 * Element needs: transform-style: preserve-3d
 */
export function useTilt3D(elRef, options = {}) {
  const { maxDeg = 8, scale = 1.02 } = options

  function onMove(e) {
    const el = elRef?.value || elRef
    if (!el || window.innerWidth < 768) return
    const rect = el.getBoundingClientRect()
    const x = (e.clientX - rect.left) / rect.width - 0.5
    const y = (e.clientY - rect.top) / rect.height - 0.5
    el.style.transform = `perspective(800px) rotateY(${x * maxDeg}deg) rotateX(${-y * maxDeg}deg) scale3d(${scale},${scale},${scale})`
  }

  function onLeave() {
    const el = elRef?.value || elRef
    if (!el) return
    el.style.transform = ''
  }

  function bind(el) {
    el.addEventListener('mousemove', onMove, { passive: true })
    el.addEventListener('mouseleave', onLeave)
  }

  function unbind(el) {
    el.removeEventListener('mousemove', onMove)
    el.removeEventListener('mouseleave', onLeave)
  }

  onMounted(() => {
    const el = elRef?.value || elRef
    if (el) bind(el)
  })

  onUnmounted(() => {
    const el = elRef?.value || elRef
    if (el) unbind(el)
  })

  return { bind, unbind }
}
