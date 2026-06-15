<template>
  <div v-if="data" class="pd">
    <div class="head">
      <h3>{{ data.title }}</h3>
      <el-tag :type="statusType" effect="dark">{{ statusText }}</el-tag>
    </div>
    <el-image v-if="data.coverUrl" :src="data.coverUrl" fit="cover" class="cover" />
    <el-descriptions :column="2" border style="margin-top:12px">
      <el-descriptions-item label="作者">{{ data.userName }}</el-descriptions-item>
      <el-descriptions-item label="学院">{{ data.collegeName }}</el-descriptions-item>
      <el-descriptions-item label="专业">{{ data.major }}</el-descriptions-item>
      <el-descriptions-item label="毕业年">{{ data.graduationYear }}</el-descriptions-item>
      <el-descriptions-item label="类目">{{ ['','荣誉','作品','成绩','活动','其他'][data.category] }}</el-descriptions-item>
      <el-descriptions-item label="级别">{{ data.achieveLevel || '-' }}</el-descriptions-item>
      <el-descriptions-item label="颁发机构">{{ data.issuingOrg || '-' }}</el-descriptions-item>
      <el-descriptions-item label="取得日期">{{ data.achieveDate || '-' }}</el-descriptions-item>
      <el-descriptions-item label="权重">{{ data.displayWeight }}</el-descriptions-item>
      <el-descriptions-item label="浏览次数">{{ data.viewCount }}</el-descriptions-item>
      <el-descriptions-item label="上架">{{ data.isOnShelf === 1 ? '是' : '否' }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ data.updatedAt }}</el-descriptions-item>
      <el-descriptions-item v-if="data.rejectReason" label="驳回原因" :span="2">
        <span style="color:#F56C6C">{{ data.rejectReason }}</span>
      </el-descriptions-item>
    </el-descriptions>

    <div v-if="data.description" class="block">
      <h4>详细描述</h4>
      <div class="desc">{{ data.description }}</div>
    </div>

    <div v-if="data.tags?.length" class="block">
      <h4>标签</h4>
      <el-tag v-for="t in data.tags" :key="t" style="margin-right:6px">{{ t }}</el-tag>
    </div>

    <div v-if="data.mediaList?.length" class="block">
      <h4>附件 ({{ data.mediaList.length }})</h4>
      <div class="media">
        <template v-for="m in data.mediaList" :key="m.id">
          <el-image v-if="m.mediaType === 1" :src="m.fileUrl" fit="cover" :preview-src-list="imageUrls" />
          <video v-else-if="m.mediaType === 2" :src="m.fileUrl" controls />
          <a v-else :href="m.fileUrl" target="_blank" class="doc">📄 {{ m.fileName }}</a>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps<{ data: any }>()
const statusText = computed(() => ['草稿','院审中','教务审中','已发布','驳回'][props.data?.status] || '-')
const statusType = computed(() => (['info','warning','warning','success','danger'] as const)[props.data?.status] || 'info')
const imageUrls = computed(() => (props.data?.mediaList || []).filter((m: any) => m.mediaType === 1).map((m: any) => m.fileUrl))
</script>

<style scoped>
.pd { max-height: 70vh; overflow-y: auto; padding-right: 8px; }
.head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.head h3 { margin: 0; }
.cover { width: 100%; max-height: 280px; border-radius: 6px; }
.block { margin-top: 14px; }
.block h4 { margin: 0 0 8px; color: #1A3A6E; font-size: 14px; }
.desc { line-height: 1.7; color: #333; white-space: pre-wrap; }
.media { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.media .el-image, .media video { width: 100%; aspect-ratio: 1; border-radius: 4px; object-fit: cover; }
.doc { padding: 12px; background: #F5F7FA; border-radius: 4px; text-decoration: none; color: #1A3A6E; }
</style>
