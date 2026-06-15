<template>
  <div class="app-page">
    <el-table :data="rows" border stripe>
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="userName" label="提交人" width="120" />
      <el-table-column prop="collegeName" label="学院" width="160" />
      <el-table-column label="类目" width="80">
        <template #default="{ row }">
          <el-tag>{{ ['','荣誉','作品','成绩','活动','其他'][row.category] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="achieveLevel" label="级别" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status===1 ? 'warning' : 'info'">
            {{ ['草稿','院审中','教务审中','已发布','驳回'][row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="提交时间" width="180" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="onAudit(row, 1)">通过</el-button>
          <el-button size="small" type="danger" @click="onAudit(row, 2)">驳回</el-button>
          <el-button size="small" @click="onView(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="flex-bar" style="justify-content:flex-end;margin-top:12px">
      <el-pagination layout="total, prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="load" />
    </div>

    <el-dialog v-model="detailVisible" title="资料详情" width="780px" top="5vh">
      <ProfileDetail v-if="detail" :data="detail" />
      <el-divider v-if="history.length" content-position="left">审核历史</el-divider>
      <el-timeline v-if="history.length">
        <el-timeline-item v-for="h in history" :key="h.id"
          :type="h.result === 1 ? 'success' : 'danger'"
          :timestamp="h.createdAt">
          <strong>{{ h.auditLevel === 1 ? '院级' : '校级' }} · {{ h.result === 1 ? '通过' : '驳回' }}</strong>
          <div>{{ h.auditorName }}</div>
          <div v-if="h.comment" class="cmt">{{ h.comment }}</div>
        </el-timeline-item>
      </el-timeline>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="detail && (detail.status === 1 || detail.status === 2)"
          type="success" @click="onAudit(detail, 1)">通过</el-button>
        <el-button v-if="detail && (detail.status === 1 || detail.status === 2)"
          type="danger" @click="onAudit(detail, 2)">驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiAuditPending, apiAuditDo, apiAuditHistory, type ProfileDTO } from '@/api/admin'
import { apiProfileDetail } from '@/api/user'
import ProfileDetail from '@/components/ProfileDetail.vue'

const rows = ref<ProfileDTO[]>([])
const total = ref(0); const page = ref(1); const size = 10
const detailVisible = ref(false); const detail = ref<any>(null); const history = ref<any[]>([])

async function load(p = 1) {
  page.value = p
  const r = await apiAuditPending(p, size)
  rows.value = r.records; total.value = r.total
}

async function onAudit(row: any, result: 1 | 2) {
  let comment = ''
  if (result === 2) {
    const r = await ElMessageBox.prompt('请输入驳回原因', '驳回', {
      confirmButtonText: '确定', cancelButtonText: '取消',
      inputValidator: (v: string) => (v && v.length >= 2) || '请输入有效原因'
    })
    comment = r.value
  } else {
    try {
      const r = await ElMessageBox.prompt('审核意见（选填）', '通过', { confirmButtonText: '确定', cancelButtonText: '取消' })
      comment = r.value
    } catch { return }
  }
  await apiAuditDo(row.id, result, comment)
  ElMessage.success(result === 1 ? '已通过' : '已驳回')
  detailVisible.value = false
  load(page.value)
}

async function onView(row: any) {
  detail.value = await apiProfileDetail(row.id)
  history.value = (await apiAuditHistory(row.id) as unknown as any[]) || []
  detailVisible.value = true
}

onMounted(() => load(1))
</script>

<style scoped>
.cmt { color: #606266; margin-top: 4px; }
</style>
