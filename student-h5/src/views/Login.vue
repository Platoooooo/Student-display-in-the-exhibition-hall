<template>
  <div class="login">
    <div class="head">
      <h2>校友成果展览</h2>
      <p>填报你的荣誉与作品，闪耀学校大屏</p>
    </div>
    <van-cell-group inset>
      <van-field v-model="form.username" label="账号" placeholder="学号 / 用户名" />
      <van-field v-model="form.password" type="password" label="密码" placeholder="请输入密码" />
    </van-cell-group>
    <div style="margin: 24px 16px;">
      <van-button block type="primary" :loading="loading" @click="onSubmit">登 录</van-button>
    </div>
    <p class="tip">默认账号：student01 / alumni01，密码 123456</p>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'

const form = reactive({ username: '', password: '' })
const loading = ref(false)
const router = useRouter()

async function onSubmit() {
  if (!form.username || !form.password) return showToast('请输入账号密码')
  loading.value = true
  try {
    await useUserStore().login(form.username, form.password)
    router.push('/')
  } finally { loading.value = false }
}
</script>

<style scoped>
.login { min-height: 100vh; background: linear-gradient(180deg, #0A1F3D 0%, #1A3A6E 60%, #fff 60%); padding-top: 60px; }
.head { text-align: center; color: #fff; padding: 0 24px 36px; }
.head h2 { font-size: 26px; margin: 0 0 8px; letter-spacing: 2px; }
.head p { opacity: .85; margin: 0; font-size: 13px; }
.tip { text-align: center; color: #999; font-size: 12px; }
</style>
