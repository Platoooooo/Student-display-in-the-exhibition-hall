<template>
  <div class="app-page">
    <div class="flex-bar">
      <div>
        <el-input v-model="kw" placeholder="标题搜索" clearable style="width:200px" @keyup.enter="load(1)" />
        <el-select v-model="onShelf" placeholder="上架状态" clearable style="width:140px;margin-left:8px">
          <el-option label="已上架" :value="1" />
          <el-option label="未上架" :value="0" />
        </el-select>
        <el-select v-model="category" placeholder="类目" clearable style="width:140px;margin-left:8px">
          <el-option v-for="(c, i) in CATEGORIES" :key="i" :label="c" :value="i + 1" />
        </el-select>
        <el-button type="primary" @click="load(1)" style="margin-left:8px">查询</el-button>
      </div>
    </div>
    <el-table :data="rows" border stripe>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="userName" label="作者" width="120" />
      <el-table-column prop="collegeName" label="学院" width="160" />
      <el-table-column label="类目" width="80">
        <template #default="{ row }">
          <el-tag size="small">{{ CATEGORIES[row.category - 1] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="上架" width="90">
        <template #default="{ row }">
          <el-switch :model-value="row.isOnShelf===1" @change="(v: any) => onShelfChange(row, v)" />
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
          <el-tag v-for="t in row.tags" :key="t" size="small" style="margin-right:4px">{{ t }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="onView(row)">详情</el-button>
          <el-button size="small" type="primary" plain @click="onEditTags(row)">标签</el-button>
          <el-button size="small" type="success" plain @click="onPush(row)">推到大屏</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="flex-bar" style="justify-content:flex-end;margin-top:12px">
      <el-pagination layout="total, prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="load" />
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="资料详情" width="780px" top="5vh">
      <ProfileDetail v-if="detail" :data="detail" />
    </el-dialog>

    <!-- 标签编辑 -->
    <el-dialog v-model="tagsVisible" title="设置标签" width="540px">
      <el-checkbox-group v-model="selectedTags">
        <el-checkbox v-for="t in allTags" :key="t.id" :value="t.id">
          <el-tag :color="t.color" effect="dark" size="small">{{ t.name }}</el-tag>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="tagsVisible = false">取消</el-button>
        <el-button type="primary" @click="onSaveTags">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiLibrary, apiSetShelf, apiSetWeight, apiSetTags, apiTags, type ProfileDTO } from '@/api/admin'
import { apiPushProfile, apiProfileDetail } from '@/api/user'
import ProfileDetail from '@/components/ProfileDetail.vue'

const CATEGORIES = ['荣誉', '作品', '成绩', '活动', '其他']

const rows = ref<ProfileDTO[]>([])
const total = ref(0); const page = ref(1); const size = 20
const kw = ref(''); const onShelf = ref<number | undefined>(); const category = ref<number | undefined>()
const detailVisible = ref(false); const detail = ref<any>(null)
const tagsVisible = ref(false); const allTags = ref<any[]>([]); const selectedTags = ref<number[]>([])
const editingId = ref<number>()

async function load(p = 1) {
  page.value = p
  const r = await apiLibrary({ page: p, size, keyword: kw.value || undefined, isOnShelf: onShelf.value, category: category.value })
  rows.value = r.records; total.value = r.total
}
async function onShelfChange(row: any, v: any) {
  await apiSetShelf(row.id, v ? 1 : 0)
  row.isOnShelf = v ? 1 : 0
  ElMessage.success(v ? '已上架' : '已下架')
}
async function onWeight(row: any, v: any) {
  await apiSetWeight(row.id, v); row.displayWeight = v
  ElMessage.success('权重已更新')
}
async function onView(row: any) {
  detail.value = await apiProfileDetail(row.id)
  detailVisible.value = true
}
async function onEditTags(row: any) {
  editingId.value = row.id
  if (!allTags.value.length) allTags.value = await apiTags()
  // 用名字回填 -> 找到 id（DTO 里只有 tag 名）
  selectedTags.value = allTags.value.filter((t: any) => row.tags?.includes(t.name)).map((t: any) => t.id)
  tagsVisible.value = true
}
async function onSaveTags() {
  await apiSetTags(editingId.value!, selectedTags.value)
  ElMessage.success('已保存'); tagsVisible.value = false
  load(page.value)
}
async function onPush(row: any) {
  if (row.isOnShelf !== 1) {
    ElMessage.warning('未上架的资料无法推送，请先上架')
    return
  }
  await apiPushProfile(row.id); ElMessage.success('已推送到大屏')
}
onMounted(() => load(1))
</script>
