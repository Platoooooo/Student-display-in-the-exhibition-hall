<template>
  <el-container style="height:100vh">
    <el-aside width="220px" class="aside">
      <div class="logo">校友成果展览</div>
      <el-menu :default-active="route.path" router background-color="#1A3A6E" text-color="#E8F1FF" active-text-color="#00E5FF">
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon>仪表盘</el-menu-item>
        <el-menu-item index="/audit"><el-icon><Document /></el-icon>审核中心</el-menu-item>
        <el-menu-item index="/library"><el-icon><Files /></el-icon>资料库</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ route.meta.title }}</span>
        <el-dropdown @command="onCmd">
          <span class="user">
            <el-avatar :size="28" />
            {{ user.info?.realName }} ({{ user.info?.roleName }})
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main><RouterView /></el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { DataAnalysis, Document, Files, ArrowDown } from '@element-plus/icons-vue'

const route = useRoute(); const router = useRouter()
const user = useUserStore()

async function onCmd(c: string) {
  if (c === 'logout') { await user.logout(); router.push('/login') }
}
</script>

<style scoped>
.aside { background: #1A3A6E; }
.logo { color: #00E5FF; font-size: 18px; font-weight: 600; padding: 18px; text-align: center;
  letter-spacing: 2px; border-bottom: 1px solid rgba(255,255,255,.08); }
.header { display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1px solid #ebeef5; }
.user { cursor: pointer; display: inline-flex; align-items: center; gap: 6px; }
:deep(.el-menu) { border-right: none; }
</style>
