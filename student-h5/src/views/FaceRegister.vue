<template>
  <van-nav-bar title="人脸录入" left-arrow @click-left="$router.back()" />
  <div class="wrap">
    <p class="hint">说明：人脸录入后，当你站在校园展览大屏前，将自动切换到你的专属成果展示。</p>
    <van-cell title="上传清晰正脸照" />
    <van-uploader v-model="files" :max-count="1" :after-read="onRead" accept="image/*" capture="user" />
    <div v-if="imgUrl" class="preview">
      <img :src="imgUrl" />
      <van-button block type="primary" :loading="loading" @click="onRegister">提交录入</van-button>
    </div>
    <p class="tip">{{ mockMode ? '⚠️ 当前为 Mock 模式（未配置 ArcFace SDK），请配置后重启后端' : '✅ 真实 ArcFace SDK 就绪' }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { showToast } from 'vant'
import { apiUpload, apiFaceRegister, apiFaceExtract } from '@/api'

const files = ref<any[]>([])
const imgUrl = ref('')
const loading = ref(false)
const mockMode = ref(false)

async function onRead(file: any) {
  const f = Array.isArray(file) ? file[0] : file
  const r = await apiUpload(f.file, 'face')
  imgUrl.value = r.url
}

/** 将上传的图片发送到后端，调用 ArcFace SDK 提取真实特征 */
async function extractRealFeature(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = async () => {
      const base64 = (reader.result as string).split(',')[1]
      try {
        const res = await apiFaceExtract(base64)
        mockMode.value = !!res.mock
        if (res.featureBase64) resolve(res.featureBase64)
        else reject(new Error(res.msg || '未检测到人脸，请用清晰正脸照'))
      } catch (e: any) { reject(e) }
    }
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

async function onRegister() {
  loading.value = true
  try {
    const file = files.value[0]?.file
    if (!file) { showToast('请先拍照或选择照片'); return }
    const featureBase64 = await extractRealFeature(file)
    await apiFaceRegister({ featureBase64, faceImageUrl: imgUrl.value })
    showToast('录入成功！已提取真实人脸特征')
  } catch (e: any) {
    showToast(e.message || '录入失败，请重试')
  } finally { loading.value = false }
}
</script>

<style scoped>
.wrap { padding: 16px; }
.hint { color: #666; font-size: 13px; line-height: 1.6; margin: 0 0 12px; }
.preview img { width: 100%; border-radius: 8px; margin: 12px 0; }
.tip { color: #999; font-size: 12px; margin-top: 16px; }
</style>
