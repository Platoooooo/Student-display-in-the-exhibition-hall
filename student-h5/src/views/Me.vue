<template>
  <van-nav-bar title="我的成果" />
  <van-tabs v-model:active="status" @change="load">
    <van-tab title="全部" :name="undefined" />
    <van-tab title="草稿" :name="0" />
    <van-tab title="审核中" :name="1" />
    <van-tab title="已发布" :name="3" />
    <van-tab title="驳回" :name="4" />
  </van-tabs>
  <van-empty v-if="!list.length" description="暂无数据" />
  <van-cell-group inset v-for="p in list" :key="p.id" style="margin:8px">
    <van-cell :title="p.title" :label="p.achieveLevel + ' · ' + (p.achieveDate || '')">
      <template #right-icon>
        <van-tag :type="statusType(p.status)" round>{{ statusText(p.status) }}</van-tag>
      </template>
    </van-cell>
    <van-cell v-if="p.rejectReason" title="驳回原因" :label="p.rejectReason" />
    <div style="display:flex;gap:8px;padding:8px 12px;justify-content:flex-end">
      <van-button v-if="p.status === 0 || p.status === 4" size="small" type="danger" plain @click="onDelete(p)">删除</van-button>
    </div>
  </van-cell-group>
  <div style="height:60px"></div>

  <van-button class="logout" plain block @click="onLogout">退出登录</van-button>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import { useRouter } from 'vue-router'
import { apiMyProfiles, apiProfileDelete } from '@/api'
import { useUserStore } from '@/stores/user'

const list = ref<any[]>([])
const status = ref<number | undefined>()
const router = useRouter()

async function load() {
  const r = await apiMyProfiles({ page: 1, size: 50, status: status.value })
  list.value = r.records
}
function statusText(s: number) { return ['草稿','院审中','教务审中','已发布','驳回'][s] }
function statusType(s: number) {
  return (['default','warning','warning','success','danger'] as const)[s]
}
async function onDelete(p: any) {
  await showConfirmDialog({ title: '删除', message: `确定删除「${p.title}」？` })
  await apiProfileDelete(p.id); showToast('已删除'); load()
}
async function onLogout() {
  await useUserStore().logout(); router.push('/login')
}
onMounted(load)
</script>

<style scoped>
.logout { margin: 16px; }
</style>
