<template>
  <van-nav-bar title="人脸录入" left-arrow @click-left="$router.back()" />
  <div class="wrap">
    <p class="hint">说明：人脸录入后，当你站在校园展览大屏前，将自动切换到你的专属成果展示。</p>
    <van-cell title="上传清晰正脸照" />
    <van-uploader v-model="files" :max-count="1" :after-read="onRead" />
    <div v-if="imgUrl" class="preview">
      <img :src="imgUrl" />
      <van-button block type="primary" :loading="loading" @click="onRegister">提交录入</van-button>
    </div>
    <p class="tip">⚠️ 当前 Demo 后端使用占位特征向量；正式部署需在前端集成 ArcFace JS-SDK 抽取真实特征。</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { showToast } from 'vant'
import { apiUpload, apiFaceRegister } from '@/api'

const files = ref<any[]>([])
const imgUrl = ref('')
const loading = ref(false)

async function onRead(file: any) {
  const f = Array.isArray(file) ? file[0] : file
  const r = await apiUpload(f.file, 'face')
  imgUrl.value = r.url
}

/** 占位：将上传的人脸图URL转 1032 字节的 mock 特征 base64
 *  生产替换为 ArcFace JS-SDK 真特征 */
function mockFeature(): string {
  const buf = new Uint8Array(1032)
  for (let i = 0; i < buf.length; i++) buf[i] = Math.floor(Math.random() * 256)
  let bin = ''
  buf.forEach(b => bin += String.fromCharCode(b))
  return btoa(bin)
}

async function onRegister() {
  loading.value = true
  try {
    await apiFaceRegister({ featureBase64: mockFeature(), faceImageUrl: imgUrl.value })
    showToast('录入成功')
  } finally { loading.value = false }
}
</script>

<style scoped>
.wrap { padding: 16px; }
.hint { color: #666; font-size: 13px; line-height: 1.6; margin: 0 0 12px; }
.preview img { width: 100%; border-radius: 8px; margin: 12px 0; }
.tip { color: #999; font-size: 12px; margin-top: 16px; }
</style>
