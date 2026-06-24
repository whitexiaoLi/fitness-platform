import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { guest: true } },
  { path: '/', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { auth: true } },
  { path: '/users', name: 'UserManage', component: () => import('@/views/UserManage.vue'), meta: { auth: true } },
  { path: '/courses', name: 'CourseReview', component: () => import('@/views/CourseReview.vue'), meta: { auth: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const store = useAdminStore()
  if (to.meta.auth && !store.isLoggedIn()) return next('/login')
  if (to.meta.guest && store.isLoggedIn()) return next('/')
  next()
})

export default router
