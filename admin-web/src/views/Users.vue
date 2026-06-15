<template>
  <div class="app-page">
    <div class="flex-bar">
      <div>
        <el-input v-model="kw" placeholder="用户名/姓名" clearable style="width:200px" @keyup.enter="load(1)" />
        <el-select v-model="role" placeholder="角色" clearable style="width:140px;margin-left:8px">
          <el-option v-for="r in ROLES" :key="r.value" :label="r.label" :value="r.value" />
        </el-select>
        <el-select v-model="collegeId" placeholder="学院" clearable style="width:200px;margin-left:8px">
          <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="load(1)" style="margin-left:8px">查询</el-button>
      </div>
      <el-button type="primary" @click="onAdd" v-if="userStore.info?.role === 5">新增用户</el-button>
    </div>

    <el-table :data="rows" border stripe>
      <el-table-column prop="username" label="账号" width="140" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="roleName" label="角色" width="100" />
      <el-table-column prop="collegeName" label="学院" width="180" />
      <el-table-column prop="major" label="专业" width="140" />
      <el-table-column prop="graduationYear" label="毕业年" width="90" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="onEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'danger' : 'success'"
            @click="onToggleStatus(row)" v-if="userStore.info?.role === 5">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="warning" @click="onResetPwd(row)" v-if="userStore.info?.role === 5">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="flex-bar" style="justify-content:flex-end;margin-top:12px">
      <el-pagination layout="total, prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="load" />
    </div>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑用户' : '新增用户'" width="540px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item :label="form.id ? '新密码' : '初始密码'" :required="!form.id">
          <el-input v-model="form.password" type="password" :placeholder="form.id ? '留空表示不修改' : ''" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.role" style="width:100%">
            <el-option v-for="r in ROLES" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院">
          <el-select v-model="form.collegeId" clearable style="width:100%">
            <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业"><el-input v-model="form.major" /></el-form-item>
        <el-form-item label="入学年份"><el-input-number v-model="form.enrollmentYear" :min="1990" :max="2100" /></el-form-item>
        <el-form-item label="毕业年份"><el-input-number v-model="form.graduationYear" :min="1990" :max="2100" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
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
import { apiUserList, apiUserSave, apiUserToggleStatus, apiUserResetPassword, apiCollegeList, type UserDTO } from '@/api/user'
import { useUserStore } from '@/stores/user'

const ROLES = [
  { value: 1, label: '学生' },
  { value: 2, label: '校友' },
  { value: 3, label: '院级审核' },
  { value: 4, label: '教务处' },
  { value: 5, label: '校级管理员' },
]

const userStore = useUserStore()
const rows = ref<UserDTO[]>([])
const total = ref(0); const page = ref(1); const size = 20
const kw = ref(''); const role = ref<number | undefined>(); const collegeId = ref<number | undefined>()
const colleges = ref<any[]>([])

const formVisible = ref(false)
const saving = ref(false)
const form = reactive<any>({
  id: undefined, username: '', password: '', realName: '', role: 1,
  collegeId: undefined, major: '', enrollmentYear: undefined, graduationYear: undefined,
  phone: '', email: '',
})

async function load(p = 1) {
  page.value = p
  const r = await apiUserList({ page: p, size, keyword: kw.value || undefined, role: role.value, collegeId: collegeId.value })
  rows.value = r.records; total.value = r.total
}

function onAdd() {
  Object.assign(form, { id: undefined, username: '', password: '', realName: '', role: 1,
    collegeId: undefined, major: '', enrollmentYear: undefined, graduationYear: undefined, phone: '', email: '' })
  formVisible.value = true
}

function onEdit(row: any) {
  Object.assign(form, { ...row, password: '' })
  formVisible.value = true
}

async function onSave() {
  saving.value = true
  try {
    await apiUserSave(form)
    ElMessage.success('保存成功')
    formVisible.value = false
    load(page.value)
  } finally { saving.value = false }
}

async function onToggleStatus(row: any) {
  await ElMessageBox.confirm(`确认${row.status === 1 ? '禁用' : '启用'}该用户?`, '提示', { type: 'warning' })
  await apiUserToggleStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('操作成功')
  load(page.value)
}

async function onResetPwd(row: any) {
  const r = await ElMessageBox.prompt(`重置「${row.realName}」密码`, '重置密码', {
    confirmButtonText: '确定', cancelButtonText: '取消', inputValue: '123456',
    inputValidator: (v: string) => v.length >= 6 || '密码至少 6 位'
  })
  await apiUserResetPassword(row.id, r.value)
  ElMessage.success('密码已重置')
}

onMounted(async () => {
  colleges.value = await apiCollegeList()
  load(1)
})
</script>
