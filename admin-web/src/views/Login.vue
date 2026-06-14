<template>
  <div class="login-page">
    <el-card class="card">
      <h2>校友成果展览 · 管理后台</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0" @submit.prevent>
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="onSubmit">登 录</el-button>
      </el-form>
      <p class="tip">提示：默认账号 admin / cs_audit / jiaowu，密码均为 123456</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }],
}
const formRef = ref<FormInstance>()
const loading = ref(false)
const router = useRouter()

async function onSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  loading.value = true
  try {
    await useUserStore().login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-page { height: 100vh; display: flex; align-items: center; justify-content: center;
  background: radial-gradient(circle at 30% 30%, #1A3A6E, #0A1F3D); }
.card { width: 380px; padding: 24px; }
.card h2 { text-align: center; margin: 0 0 20px; color: #1A3A6E; }
.tip { color: #999; font-size: 12px; margin-top: 8px; text-align: center; }
</style>
