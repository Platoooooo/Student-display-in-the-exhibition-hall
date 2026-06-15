<template>
  <div class="app-page">
    <el-row :gutter="16">
      <el-col :span="4" v-for="c in cards" :key="c.label">
        <el-card>
          <div class="num">{{ stat?.[c.key] ?? '-' }}</div>
          <div class="lab">{{ c.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card>
          <template #header>各学院已发布资料数</template>
          <div v-if="stat?.byCollege">
            <div v-for="(v, k) in stat.byCollege" :key="k" class="bar">
              <div class="bar-label">{{ k }}</div>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: barWidth(v, maxCollege) + '%' }"></div>
              </div>
              <div class="bar-value">{{ v }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>资料类目分布</template>
          <div v-if="stat?.byCategory">
            <div v-for="(v, k) in stat.byCategory" :key="k" class="bar">
              <div class="bar-label">{{ k }}</div>
              <div class="bar-track">
                <div class="bar-fill cat" :style="{ width: barWidth(v, maxCategory) + '%' }"></div>
              </div>
              <div class="bar-value">{{ v }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top:16px" v-if="stat?.recognizeTrend">
      <template #header>近 7 天人脸识别量</template>
      <div class="trend">
        <div v-for="t in stat.recognizeTrend" :key="t.date" class="trend-col">
          <div class="trend-bar" :style="{ height: trendHeight(t.count) + 'px' }">
            <span class="trend-num">{{ t.count }}</span>
          </div>
          <div class="trend-day">{{ t.date.slice(5) }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
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

const maxCollege = computed(() => stat.value?.byCollege ? Math.max(1, ...Object.values(stat.value.byCollege as Record<string, number>)) : 1)
const maxCategory = computed(() => stat.value?.byCategory ? Math.max(1, ...Object.values(stat.value.byCategory as Record<string, number>)) : 1)
const maxTrend = computed(() => stat.value?.recognizeTrend ? Math.max(1, ...stat.value.recognizeTrend.map((t: any) => t.count)) : 1)

function barWidth(v: number, max: number) { return Math.round((v / max) * 100) }
function trendHeight(v: number) { return Math.round((v / maxTrend.value) * 120) + 4 }

onMounted(async () => { stat.value = await apiDashboard() })
</script>

<style scoped>
.num { font-size: 28px; font-weight: 600; color: #1A3A6E; }
.lab { color: #909399; margin-top: 4px; font-size: 13px; }

.bar { display: flex; align-items: center; gap: 10px; padding: 6px 0; }
.bar-label { width: 130px; font-size: 13px; color: #606266; flex-shrink: 0; }
.bar-track { flex: 1; height: 18px; background: #F0F2F5; border-radius: 9px; overflow: hidden; }
.bar-fill { height: 100%; background: linear-gradient(90deg, #1A3A6E, #00E5FF); border-radius: 9px; transition: width .4s; }
.bar-fill.cat { background: linear-gradient(90deg, #67C23A, #95D475); }
.bar-value { width: 40px; text-align: right; font-weight: 600; color: #1A3A6E; }

.trend { display: flex; align-items: flex-end; height: 160px; gap: 12px; padding: 16px; }
.trend-col { flex: 1; display: flex; flex-direction: column; align-items: center; }
.trend-bar { width: 100%; max-width: 50px; background: linear-gradient(180deg, #00E5FF, #1A3A6E); border-radius: 4px 4px 0 0; position: relative; transition: height .4s; }
.trend-num { position: absolute; top: -20px; left: 50%; transform: translateX(-50%); font-size: 12px; color: #1A3A6E; font-weight: 600; }
.trend-day { font-size: 12px; color: #909399; margin-top: 6px; }
</style>
