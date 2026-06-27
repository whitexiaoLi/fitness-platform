import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

/* Global design system */
import '@/styles/variables.css'
import '@/styles/reset.css'
import '@/styles/transitions.css'
import '@/styles/global.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')

// Global scroll reveal observer
const revealObserver = new IntersectionObserver((entries) => {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      entry.target.classList.add('revealed')
      revealObserver.unobserve(entry.target)
    }
  })
}, { threshold: 0.1, rootMargin: '0px 0px -30px 0px' })

// Observe existing and future .reveal-item elements
new MutationObserver(() => {
  document.querySelectorAll('.reveal-item:not(.revealed)').forEach(el => {
    if (!el.dataset.revealObserved) {
      el.dataset.revealObserved = '1'
      revealObserver.observe(el)
    }
  })
}).observe(document.body, { childList: true, subtree: true })

// Initial observation
setTimeout(() => {
  document.querySelectorAll('.reveal-item:not(.revealed)').forEach(el => {
    el.dataset.revealObserved = '1'
    revealObserver.observe(el)
  })
}, 100)

// Scroll-driven slide-in for .course-stagger and .scroll-fade elements
let entranceTicking = false
function updateEntranceElements() {
  const vh = window.innerHeight
  document.querySelectorAll('.course-stagger:not(.visible), .scroll-fade:not(.visible)').forEach(el => {
    const rect = el.getBoundingClientRect()
    // Element enters from bottom: when top is at viewport bottom → progress 0; when top is at 80% vh → progress 1
    const progress = Math.max(0, Math.min(1, (vh - rect.top) / (vh * 0.2)))
    const delay = parseInt(el.dataset.staggerDelay) || 0
    const p = Math.max(0, Math.min(1, (progress - delay / 1000) * 1.8))
    if (p >= 1) {
      // Fully visible: clear inline styles, let CSS take over, skip future updates
      el.style.opacity = ''
      el.style.transform = ''
      el.classList.add('visible')
    } else {
      el.style.opacity = p
      el.style.transform = `translateY(${(1 - p) * 30}px)`
    }
  })
  entranceTicking = false
}

window.addEventListener('scroll', () => {
  if (!entranceTicking) { entranceTicking = true; requestAnimationFrame(updateEntranceElements) }
}, { passive: true })

// Initial check + periodic cleanup
setTimeout(updateEntranceElements, 150)

// Re-check on DOM changes
new MutationObserver(() => { setTimeout(updateEntranceElements, 100) }).observe(document.body, { childList: true, subtree: true })
