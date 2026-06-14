<template>
  <div class="app-page">
    <div class="flex-bar">
      <div>
        <el-input v-model="kw" placeholder="标题搜索" clearable style="width:200px" @keyup.enter="load(1)" />
        <el-select v-model="onShelf" placeholder="上架状态" clearable style="width:140px;margin-left:8px">
          <el-option label="已上架" :value="1" />
          <el-option label="未上架" :value="0" />
        </el-select>
        <el-button type="primary" @click="load(1)" style="margin-left:8px">查询</el-button>
      </div>
    </div>
    <el-table :data="rows" border stripe>
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="userName" label="作者" width="120" />
      <el-table-column prop="collegeName" label="学院" width="160" />
      <el-table-column label="上架" width="90">
        <template #default="{ row }">
          <el-switch :model-value="row.isOnShelf===1" @change="(v: boolean) => onShelfChange(row, v)" />
        </template>
      </el-table-column>
      <el-table-column label="权重" width="120">
        <template #default="{ row }">
          <el-input-number :model-value="row.displayWeight" :min="0" :max="999" size="small"
            @change="(v: any) => onWeight(row, v)" />
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column label="标签" min-width="160">
        <template #default="{ row }">
          <el-tag v-for="t in row.tags" :key="t" style="margin-right:4px">{{ t }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <div class="flex-bar" style="justify-content:flex-end;margin-top:12px">
      <el-pagination layout="total, prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="load" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiLibrary, apiSetShelf, apiSetWeight, type ProfileDTO } from '@/api/admin'

const rows = ref<ProfileDTO[]>([])
const total = ref(0); const page = ref(1); const size = 20
const kw = ref(''); const onShelf = ref<number | undefined>()

async function load(p = 1) {
  page.value = p
  const r = await apiLibrary({ page: p, size, keyword: kw.value || undefined, isOnShelf: onShelf.value })
  rows.value = r.records; total.value = r.total
}
async function onShelfChange(row: ProfileDTO, v: boolean) {
  await apiSetShelf(row.id, v ? 1 : 0)
  row.isOnShelf = v ? 1 : 0
  ElMessage.success(v ? '已上架' : '已下架')
}
async function onWeight(row: ProfileDTO, v: number) {
  await apiSetWeight(row.id, v); row.displayWeight = v
  ElMessage.success('权重已更新')
}
onMounted(() => load(1))
</script>
