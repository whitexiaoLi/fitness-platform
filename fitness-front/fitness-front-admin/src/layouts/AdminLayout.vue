<template>
  <el-container class="admin-layout">
    <!-- Sidebar -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo" @click="$router.push('/')">
        <span v-if="!isCollapse">📊 Fitness Admin</span>
        <span v-else>📊</span>
      </div>
      <el-menu
        :router="true"
        :default-active="activeMenu"
        :collapse="isCollapse"
        :default-openeds="defaultOpeneds"
        background-color="#1a1a2e"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>数据概览</template>
        </el-menu-item>

        <el-sub-menu index="/members">
          <template #title>
            <el-icon><User /></el-icon>
            <span>会员管理</span>
          </template>
          <el-menu-item index="/members/list">
            <el-icon><List /></el-icon>
            <template #title>会员列表</template>
          </el-menu-item>
          <el-menu-item index="/members/cards">
            <el-icon><CreditCard /></el-icon>
            <template #title>会员卡管理</template>
          </el-menu-item>
          <el-menu-item index="/members/metrics">
            <el-icon><Monitor /></el-icon>
            <template #title>体测记录</template>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/courses">
          <template #title>
            <el-icon><Reading /></el-icon>
            <span>课程管理</span>
          </template>
          <el-menu-item index="/courses/group">
            <el-icon><Calendar /></el-icon>
            <template #title>团课排课</template>
          </el-menu-item>
          <el-menu-item index="/courses/pt">
            <el-icon><Star /></el-icon>
            <template #title>私教管理</template>
          </el-menu-item>
          <el-menu-item index="/courses/bookings">
            <el-icon><DocumentChecked /></el-icon>
            <template #title>约课记录</template>
          </el-menu-item>
          <el-menu-item index="/courses/review">
            <el-icon><Checked /></el-icon>
            <template #title>课程审核</template>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/coaches">
          <template #title>
            <el-icon><Avatar /></el-icon>
            <span>教练管理</span>
          </template>
          <el-menu-item index="/coaches/profile">
            <el-icon><UserFilled /></el-icon>
            <template #title>教练档案</template>
          </el-menu-item>
          <el-menu-item index="/coaches/schedule">
            <el-icon><Clock /></el-icon>
            <template #title>排班管理</template>
          </el-menu-item>
          <el-menu-item index="/coaches/performance">
            <el-icon><TrendCharts /></el-icon>
            <template #title>业绩统计</template>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/finance">
          <template #title>
            <el-icon><Money /></el-icon>
            <span>财务管理</span>
          </template>
          <el-menu-item index="/finance/transactions">
            <el-icon><Sell /></el-icon>
            <template #title>交易流水</template>
          </el-menu-item>
          <el-menu-item index="/finance/bills">
            <el-icon><Files /></el-icon>
            <template #title>对账单</template>
          </el-menu-item>
          <el-menu-item index="/finance/refunds">
            <el-icon><Refund /></el-icon>
            <template #title>退款管理</template>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/settings">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </template>
          <el-menu-item index="/settings/staff">
            <el-icon><Grid /></el-icon>
            <template #title>员工管理</template>
          </el-menu-item>
          <el-menu-item index="/settings/roles">
            <el-icon><Lock /></el-icon>
            <template #title>角色权限</template>
          </el-menu-item>
          <el-menu-item index="/settings/logs">
            <el-icon><Notebook /></el-icon>
            <template #title>操作日志</template>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- Right area -->
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="breadcrumbParent">{{ breadcrumbParent }}</el-breadcrumb-item>
            <el-breadcrumb-item v-if="breadcrumb">{{ breadcrumb }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ store.user?.nickname || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <span>{{ store.user?.username }}</span>
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import {
  DataAnalysis, User, Reading, Avatar, Money, Setting,
  List, CreditCard, Monitor, Calendar, Star, DocumentChecked, Checked,
  UserFilled, Clock, TrendCharts, Sell, Files, Grid, Lock, Notebook,
  Fold, Expand, ArrowDown,
} from '@element-plus/icons-vue'

const route = useRoute()
const store = useAdminStore()
const isCollapse = ref(false)

const breadcrumbMap = {
  Dashboard: '数据概览',
  // 会员管理
  MemberList: '会员列表', MemberCards: '会员卡管理', MemberMetrics: '体测记录',
  // 课程管理
  GroupCourse: '团课排课', PrivateCoach: '私教管理', BookingRecords: '约课记录', CourseReview: '课程审核',
  // 教练管理
  CoachProfile: '教练档案', CoachSchedule: '排班管理', CoachPerformance: '业绩统计',
  // 财务管理
  Transactions: '交易流水', Bills: '对账单', Refunds: '退款管理',
  // 系统设置
  StaffManage: '员工管理', RolePermission: '角色权限', OperationLog: '操作日志',
}

const parentMap = {
  MemberList: '会员管理', MemberCards: '会员管理', MemberMetrics: '会员管理',
  GroupCourse: '课程管理', PrivateCoach: '课程管理', BookingRecords: '课程管理', CourseReview: '课程管理',
  CoachProfile: '教练管理', CoachSchedule: '教练管理', CoachPerformance: '教练管理',
  Transactions: '财务管理', Bills: '财务管理', Refunds: '财务管理',
  StaffManage: '系统设置', RolePermission: '系统设置', OperationLog: '系统设置',
}

const defaultOpeneds = computed(() => {
  const parents = ['/members', '/courses', '/coaches', '/finance', '/settings']
  // Only keep the currently active parent open when collapsed
  const parent = parentMap[route.name]
  if (!parent) return parents
  const idx = ['会员管理', '课程管理', '教练管理', '财务管理', '系统设置'].indexOf(parent)
  return idx >= 0 ? [parents[idx]] : parents
})

const activeMenu = computed(() => route.path)

const breadcrumb = computed(() => breadcrumbMap[route.name] || '')
const breadcrumbParent = computed(() => parentMap[route.name] || '')

function handleLogout() {
  store.logout()
}
</script>

<style scoped>
.admin-layout { height: 100vh; }

.aside {
  background: #1a1a2e;
  overflow-x: hidden;
  overflow-y: auto;
  transition: width 0.3s;
}
.aside .el-menu {
  border-right: none;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  overflow: hidden;
  white-space: nowrap;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
  height: 50px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #666;
}
.collapse-btn:hover { color: #409eff; }

.header-right { display: flex; align-items: center; }
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
}
.username { font-size: 14px; }

.main-content {
  background: #f0f2f5;
  min-height: calc(100vh - 50px);
}
</style>
