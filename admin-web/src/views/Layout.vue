<template>
  <el-container style="height:100vh">
    <el-aside width="220px" class="aside">
      <div class="logo">校友成果展览</div>
      <el-menu :default-active="route.path" router background-color="#1A3A6E" text-color="#E8F1FF" active-text-color="#00E5FF">
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon>仪表盘</el-menu-item>
        <el-menu-item index="/audit"><el-icon><Document /></el-icon>审核中心</el-menu-item>
        <el-menu-item index="/library"><el-icon><Files /></el-icon>资料库</el-menu-item>
        <el-menu-item v-if="canAdmin" index="/users"><el-icon><User /></el-icon>用户管理</el-menu-item>
        <el-menu-item v-if="canAdmin" index="/tags"><el-icon><PriceTag /></el-icon>标签管理</el-menu-item>
        <el-menu-item v-if="canAdmin" index="/display"><el-icon><Monitor /></el-icon>大屏控制</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ route.meta.title }}</span>
        <el-dropdown @command="onCmd">
          <span class="user">
            <el-avatar :size="28" :src="user.info?.avatarUrl" />
            {{ user.info?.realName }} ({{ user.info?.roleName }})
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main><RouterView /></el-main>
    </el-container>

    <el-dialog v-model="pwdVisible" title="修改密码" width="420px">
      <el-form :model="pwd" label-width="100px">
        <el-form-item label="原密码"><el-input v-model="pwd.oldPassword" type="password" /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwd.newPassword" type="password" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="onSavePwd">保存</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { DataAnalysis, Document, Files, ArrowDown, User, PriceTag, Monitor } from '@element-plus/icons-vue'
import http from '@/api/http'

const route = useRoute(); const router = useRouter()
const user = useUserStore()
const canAdmin = computed(() => user.info && [4, 5].includes(user.info.role))

const pwdVisible = ref(false)
const pwd = reactive({ oldPassword: '', newPassword: '' })

async function onCmd(c: string) {
  if (c === 'logout') { await user.logout(); router.push('/login') }
  if (c === 'password') { pwd.oldPassword = ''; pwd.newPassword = ''; pwdVisible.value = true }
}

async function onSavePwd() {
  if (!pwd.oldPassword || !pwd.newPassword) return ElMessage.warning('请输入完整')
  if (pwd.newPassword.length < 6) return ElMessage.warning('新密码至少 6 位')
  await http.put('/api/user/change-password', pwd)
  ElMessage.success('修改成功，请重新登录')
  pwdVisible.value = false
  await user.logout(); router.push('/login')
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
