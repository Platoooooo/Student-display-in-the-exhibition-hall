<template>
  <div class="app-page">
    <div class="flex-bar">
      <span class="muted">展示标签用于资料分类与大屏筛选展示</span>
      <el-button type="primary" @click="onAdd">新增标签</el-button>
    </div>
    <el-table :data="tags" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column label="颜色" width="160">
        <template #default="{ row }">
          <el-tag :color="row.color" effect="dark" disable-transitions>{{ row.color }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="onEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="onDelete(row)" v-if="userStore.info?.role === 5">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑标签' : '新增标签'" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="form.color" />
          <span style="margin-left:12px">{{ form.color }}</span>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiTagList, apiTagSave, apiTagDelete, type TagDTO } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const tags = ref<TagDTO[]>([])
const formVisible = ref(false)
const saving = ref(false)
const form = reactive<TagDTO>({ name: '', color: '#409EFF', sortOrder: 0 })

async function load() { tags.value = await apiTagList() }

function onAdd() { Object.assign(form, { id: undefined, name: '', color: '#409EFF', sortOrder: 0 }); formVisible.value = true }
function onEdit(row: TagDTO) { Object.assign(form, row); formVisible.value = true }

async function onSave() {
  saving.value = true
  try { await apiTagSave(form); ElMessage.success('保存成功'); formVisible.value = false; load() }
  finally { saving.value = false }
}
async function onDelete(row: TagDTO) {
  await ElMessageBox.confirm(`确认删除「${row.name}」?`, '提示', { type: 'warning' })
  await apiTagDelete(row.id!); ElMessage.success('已删除'); load()
}

onMounted(load)
</script>

<style scoped>
.muted { color: #909399; }
</style>
