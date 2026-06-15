<template>
  <van-nav-bar title="成果详情" left-arrow @click-left="$router.back()" />
  <div v-if="data" class="detail">
    <van-image v-if="data.coverUrl" :src="data.coverUrl" fit="cover" class="cover" />
    <div class="head">
      <h2>{{ data.title }}</h2>
      <van-tag :type="statusType" round style="margin-top:6px">{{ statusText }}</van-tag>
    </div>
    <van-cell-group inset>
      <van-cell title="类目" :value="['','荣誉','作品','成绩','活动','其他'][data.category]" />
      <van-cell title="级别" :value="data.achieveLevel || '-'" />
      <van-cell title="颁发机构" :value="data.issuingOrg || '-'" />
      <van-cell title="取得日期" :value="data.achieveDate || '-'" />
      <van-cell title="浏览量" :value="String(data.viewCount || 0)" />
    </van-cell-group>
    <van-cell-group inset title="描述" style="margin-top:8px">
      <div class="desc">{{ data.description || '（无）' }}</div>
    </van-cell-group>
    <van-cell-group v-if="data.tags && data.tags.length" inset title="标签" style="margin-top:8px">
      <div class="tags">
        <van-tag v-for="t in data.tags" :key="t" plain type="primary">{{ t }}</van-tag>
      </div>
    </van-cell-group>
    <van-cell-group v-if="data.mediaList && data.mediaList.length" inset title="附件" style="margin-top:8px">
      <div class="media">
        <template v-for="m in data.mediaList" :key="m.id">
          <van-image v-if="m.mediaType === 1" :src="m.fileUrl" fit="cover" />
          <video v-else-if="m.mediaType === 2" :src="m.fileUrl" controls class="video" />
        </template>
      </div>
    </van-cell-group>
    <van-cell-group v-if="history.length" inset title="审核历史" style="margin-top:8px;margin-bottom:80px">
      <van-cell v-for="h in history" :key="h.id"
        :title="(h.auditLevel === 1 ? '院级' : '校级') + ' · ' + (h.result === 1 ? '通过' : '驳回')"
        :label="`${h.auditorName || ''} · ${h.createdAt}\n${h.comment || ''}`" />
    </van-cell-group>

    <div class="actions">
      <van-button v-if="data.status === 4 || data.status === 0" block type="primary"
        @click="$router.push(`/submit?id=${data.id}`)">编辑并重新提交</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { apiProfileDetail, apiAuditHistory } from '@/api'

const route = useRoute()
const data = ref<any>(null)
const history = ref<any[]>([])

const statusText = computed(() => ['草稿', '院审中', '教务审中', '已发布', '驳回'][data.value?.status] || '-')
const statusType = computed(() => (['default', 'warning', 'warning', 'success', 'danger'] as const)[data.value?.status] || 'default')

onMounted(async () => {
  const id = Number(route.params.id)
  data.value = await apiProfileDetail(id)
  try { history.value = await apiAuditHistory(id) } catch {}
})
</script>

<style scoped>
.cover { width: 100%; height: 220px; }
.head { padding: 14px 16px; background: #fff; }
.head h2 { margin: 0; font-size: 17px; line-height: 1.5; }
.desc { padding: 14px; line-height: 1.7; color: #333; white-space: pre-wrap; }
.tags { padding: 10px; display: flex; flex-wrap: wrap; gap: 8px; }
.media { padding: 10px; display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; }
.media .van-image, .media .video { width: 100%; aspect-ratio: 1; border-radius: 6px; object-fit: cover; }
.actions { position: fixed; bottom: 0; left: 0; right: 0; padding: 12px 16px; background: #fff; border-top: 1px solid #eee; }
</style>
