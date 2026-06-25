import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const Placeholder = () => import('@/components/Placeholder.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { guest: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { auth: true },
    redirect: '/dashboard',
    children: [
      // 数据概览
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue') },

      // 会员管理
      { path: 'members/list', name: 'MemberList', component: () => import('@/views/UserManage.vue') },
      { path: 'members/cards', name: 'MemberCards', component: () => import('@/views/MemberCards.vue') },
      { path: 'members/metrics', name: 'MemberMetrics', component: () => import('@/views/MemberMetrics.vue') },

      // 课程管理
      { path: 'courses/group', name: 'GroupCourse', component: () => import('@/views/GroupCourse.vue') },
      { path: 'courses/pt', name: 'PrivateCoach', component: () => import('@/views/PrivateCoach.vue') },
      { path: 'courses/bookings', name: 'BookingRecords', component: () => import('@/views/BookingRecords.vue') },
      { path: 'courses/review', name: 'CourseReview', component: () => import('@/views/CourseReview.vue') },

      // 教练管理
      { path: 'coaches/profile', name: 'CoachProfile', component: () => import('@/views/CoachProfile.vue') },
      { path: 'coaches/schedule', name: 'CoachSchedule', component: () => import('@/views/CoachSchedule.vue') },
      { path: 'coaches/performance', name: 'CoachPerformance', component: () => import('@/views/CoachPerformance.vue') },

      // 财务管理
      { path: 'finance/transactions', name: 'Transactions', component: Placeholder },
      { path: 'finance/bills', name: 'Bills', component: Placeholder },
      { path: 'finance/refunds', name: 'Refunds', component: Placeholder },

      // 系统设置
      { path: 'settings/staff', name: 'StaffManage', component: () => import('@/views/StaffManage.vue') },
      { path: 'settings/roles', name: 'RolePermission', component: () => import('@/views/RolePermission.vue') },
      { path: 'settings/logs', name: 'OperationLog', component: () => import('@/views/OperationLog.vue') },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const store = useAdminStore()
  if (to.matched.some((r) => r.meta.auth) && !store.isLoggedIn()) return next('/login')
  if (to.meta.guest && store.isLoggedIn()) return next('/dashboard')
  next()
})

export default router
