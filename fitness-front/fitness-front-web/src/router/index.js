import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { guest: true } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { guest: true } },
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue'), meta: { auth: true } },
  { path: '/courses', name: 'CourseList', component: () => import('@/views/CourseList.vue'), meta: { auth: true } },
  { path: '/courses/:id', name: 'CourseDetail', component: () => import('@/views/CourseDetail.vue'), meta: { auth: true } },
  { path: '/training', name: 'Training', component: () => import('@/views/Training.vue'), meta: { auth: true } },
  { path: '/diet', name: 'Diet', component: () => import('@/views/Diet.vue'), meta: { auth: true } },
  { path: '/metrics', name: 'Metrics', component: () => import('@/views/BodyMetrics.vue'), meta: { auth: true } },
  { path: '/ai', name: 'AiChat', component: () => import('@/views/AiChat.vue'), meta: { auth: true } },
  { path: '/profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { auth: true } },
  { path: '/coach', name: 'CoachDashboard', component: () => import('@/views/CoachDashboard.vue'), meta: { auth: true, role: 'COACH' } },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.auth && !userStore.isLoggedIn()) return next('/login')
  if (to.meta.guest && userStore.isLoggedIn()) return next('/')
  if (to.meta.role && userStore.user?.role !== to.meta.role) return next('/')
  next()
})

export default router
