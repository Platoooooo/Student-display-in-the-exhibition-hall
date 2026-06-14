<template>
  <div class="app-page">
    <el-row :gutter="16">
      <el-col :span="4" v-for="c in cards" :key="c.label">
        <el-card><div class="num">{{ stat?.[c.key] ?? '-' }}</div><div class="lab">{{ c.label }}</div></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { apiDashboard } from '@/api/admin'

const cards = [
  { key: 'userCount', label: '用户总数' },
  { key: 'profileTotal', label: '资料总数' },
  { key: 'profilePublished', label: '已发布' },
  { key: 'profileOnShelf', label: '大屏展示' },
  { key: 'profilePending', label: '待审核' },
  { key: 'recognizeToday', label: '今日识别' },
]
const stat = ref<any>(null)
onMounted(async () => { stat.value = await apiDashboard() })
</script>

<style scoped>
.num { font-size: 28px; font-weight: 600; color: #1A3A6E; }
.lab { color: #909399; margin-top: 4px; font-size: 13px; }
</style>
