<template>
  <div class="app-page">
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card>
          <template #header>大屏在线状态</template>
          <div class="online">
            <div class="num">{{ online }}</div>
            <div class="lab">在线大屏数</div>
            <el-button type="primary" plain size="small" @click="loadOnline" style="margin-top:8px">刷新</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <template #header>推送通知到大屏</template>
          <el-input v-model="notice" type="textarea" :rows="3" placeholder="输入通知文本，将以滚动条方式显示在大屏顶部" />
          <div style="margin-top:12px;text-align:right">
            <el-button @click="onRefreshPlaylist">刷新播放列表</el-button>
            <el-button type="primary" :disabled="!notice.trim()" @click="onPushNotice">推送通知</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top:16px">
      <template #header>强制切换到指定资料</template>
      <div>
        <el-select v-model="profileId" filterable remote :remote-method="onSearchProfiles"
          :loading="searching" placeholder="搜索资料标题..." style="width:60%">
          <el-option v-for="p in profileOpts" :key="p.id" :label="`${p.title} - ${p.userName || ''}`" :value="p.id" />
        </el-select>
        <el-button type="primary" :disabled="!profileId" @click="onPushProfile" style="margin-left:8px">推送到大屏</el-button>
      </div>
      <div class="hint">大屏接到推送后会立即切到该资料的全屏展示</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiPushNotice, apiPushProfile, apiRefreshDisplay, apiDisplayOnline } from '@/api/user'
import { apiLibrary, type ProfileDTO } from '@/api/admin'

const online = ref(0)
const notice = ref('')
const profileId = ref<number | undefined>()
const profileOpts = ref<ProfileDTO[]>([])
const searching = ref(false)

async function loadOnline() {
  const r = await apiDisplayOnline(); online.value = r.count
}

async function onSearchProfiles(kw: string) {
  if (!kw) { profileOpts.value = []; return }
  searching.value = true
  try {
    const r = await apiLibrary({ page: 1, size: 20, keyword: kw, isOnShelf: 1 })
    profileOpts.value = r.records
  } finally { searching.value = false }
}

async function onPushNotice() {
  await apiPushNotice(notice.value)
  ElMessage.success('通知已推送'); notice.value = ''
}

async function onPushProfile() {
  if (!profileId.value) return
  await apiPushProfile(profileId.value)
  ElMessage.success('已推送到大屏')
}

async function onRefreshPlaylist() {
  await apiRefreshDisplay()
  ElMessage.success('已通知大屏刷新')
}

onMounted(loadOnline)
</script>

<style scoped>
.online { text-align: center; padding: 12px 0; }
.num { font-size: 36px; font-weight: 700; color: #1A3A6E; }
.lab { color: #909399; margin-top: 4px; }
.hint { color: #909399; font-size: 12px; margin-top: 8px; }
</style>
