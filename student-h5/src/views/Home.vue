<template>
  <van-nav-bar title="首页" />
  <div class="welcome">
    <h2>{{ user.info?.realName }} 你好 👋</h2>
    <p>{{ user.info?.collegeName }} · {{ user.info?.roleName }}</p>
  </div>
  <van-cell-group inset style="margin-top:16px">
    <van-cell title="提交新成果" is-link to="/submit" icon="edit" />
    <van-cell title="我的成果" is-link to="/me" icon="orders-o" />
    <van-cell title="人脸录入" is-link to="/face" icon="smile-o" :value="faceTip" />
  </van-cell-group>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { apiFaceStatus } from '@/api'

const user = useUserStore()
const faceTip = ref('')
onMounted(async () => {
  if (!user.info) await user.fetchMe()
  try {
    const r = await apiFaceStatus()
    faceTip.value = r.registered ? '已录入' : '未录入'
  } catch {}
})
</script>

<style scoped>
.welcome { background: linear-gradient(135deg, #0A1F3D, #1A3A6E); color: #fff; padding: 24px; }
.welcome h2 { margin: 0; }
.welcome p { margin: 6px 0 0; opacity: .8; }
</style>
