<template>
  <div class="page-placeholder">
    <el-card>
      <template #header><h3>{{ title }}</h3></template>
      <el-empty :description="desc" />
    </el-card>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '页面' },
  desc: { type: String, default: '功能开发中' },
})
</script>

<style scoped>
.page-placeholder { padding: 0; }
</style>
