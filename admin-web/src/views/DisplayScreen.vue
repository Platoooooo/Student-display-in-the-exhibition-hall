<template>
  <div class="display-screen">
    <div class="stars"></div>

    <!-- 摄像头（隐藏，仅用于识别人脸） -->
    <video ref="videoEl" autoplay playsinline muted class="hidden-cam"></video>
    <canvas ref="canvasEl" class="hidden-cam"></canvas>

    <!-- 顶部标题栏 -->
    <div class="header">
      <h1 class="title">{{ personalMode ? '欢迎回来' : '校友成果展览' }}</h1>
      <p class="subtitle">{{ personalMode ? `👤 ${personalUserName}` : 'Alumni Achievement Exhibition' }}</p>
    </div>

    <!-- 主内容区 -->
    <div class="main" v-if="current">
      <div class="left-panel">
        <div class="cover-box" :class="{ changing: isChanging }">
          <div class="cover-img" :style="{ backgroundImage: `url(${current.coverUrl || ''})` }"></div>
          <div class="cover-gradient"></div>
        </div>
        <div class="meta-info">
          <span class="college-tag">{{ current.collegeName }}</span>
          <span class="year-tag">{{ current.graduationYear }} 届</span>
          <span class="level-tag" v-if="current.achieveLevel">{{ current.achieveLevel }}</span>
        </div>
      </div>

      <div class="right-panel">
        <div class="card" :class="{ changing: isChanging }">
          <div class="category-badge">{{ categoryMap[current.category] || '其他' }}</div>
          <h2 class="card-title">{{ current.title }}</h2>
          <h3 class="card-author">{{ current.userName }} · {{ current.major }}</h3>
          <p class="card-desc">{{ current.description }}</p>
          <div class="card-tags" v-if="current.tags?.length">
            <span class="tag" v-for="t in current.tags" :key="t">{{ t }}</span>
          </div>
          <div class="card-footer">
            <span>{{ current.achieveDate }}</span>
            <span v-if="current.issuingOrg"> · {{ current.issuingOrg }}</span>
          </div>
        </div>
        <div class="progress">
          <span v-for="(_, i) in currentList" :key="i" class="dot" :class="{ active: i === currentIndex }"></span>
        </div>
      </div>
    </div>

    <div class="empty" v-else>
      <p>暂无展示数据</p>
    </div>

    <div class="footer">
      <span :class="{ matched: faceMatched }">{{ faceMatched ? '🔍 已识别' : '🔍 扫脸中' }}</span>
      <span style="margin-left:12px">{{ now }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import http from '@/api/http'

interface ProfileDTO {
  id: number; userId: number; userName: string; collegeName?: string; major?: string;
  title: string; category: number; description?: string; coverUrl?: string;
  achieveDate?: string; achieveLevel?: string; issuingOrg?: string;
  tags?: string[]; graduationYear?: number;
}

const categoryMap: Record<number, string> = { 1: '🏆 荣誉', 2: '🎨 作品', 3: '🏅 成绩', 4: '📋 其他' }

const playlist = ref<ProfileDTO[]>([])
const personalList = ref<ProfileDTO[]>([])
const currentIndex = ref(0)
const isChanging = ref(false)
const now = ref('')
let timer: number | null = null
let clockTimer: number | null = null

// 人脸识别
const videoEl = ref<HTMLVideoElement>()
const canvasEl = ref<HTMLCanvasElement>()
const faceMatched = ref(false)
const personalMode = ref(false)
const personalUserName = ref('')
let scanTimer: number | null = null
let personalTimeout: number | null = null
let stream: MediaStream | null = null

const currentList = computed(() =>
  personalMode.value && personalList.value.length ? personalList.value : playlist.value
)
const current = computed(() => currentList.value[currentIndex.value] ?? null)

function next() {
  if (currentList.value.length <= 1) return
  isChanging.value = true
  setTimeout(() => {
    currentIndex.value = (currentIndex.value + 1) % currentList.value.length
    isChanging.value = false
  }, 600)
}

function updateClock() {
  now.value = new Date().toLocaleString('zh-CN', { hour12: false })
}

// ========== 人脸扫描 ==========
async function startCamera() {
  try {
    stream = await navigator.mediaDevices.getUserMedia({ video: { width: 640, height: 480, facingMode: 'user' } })
    if (videoEl.value) videoEl.value.srcObject = stream
    scanTimer = window.setInterval(scanFace, 1500) // 每 1.5 秒扫一次
  } catch (e) {
    console.warn('摄像头不可用，跳过人脸识别')
  }
}

async function scanFace() {
  if (!videoEl.value || !canvasEl.value) return
  const v = videoEl.value, c = canvasEl.value
  if (v.readyState < 2) return

  c.width = 320; c.height = 240
  c.getContext('2d')?.drawImage(v, 0, 0, 320, 240)
  const base64 = c.toDataURL('image/jpeg', 0.8).split(',')[1]

  try {
    const res = await http.post<any, any>('/api/face/recognize-image', { imageBase64: base64, deviceId: 'web-display' })
    if (res.matched && res.userId) {
      faceMatched.value = true
      if (!personalMode.value) {
        enterPersonalMode(res)
      }
      resetPersonalTimer()
    }
  } catch { /* 未检测到人脸或识别失败，忽略 */ }
}

function enterPersonalMode(result: any) {
  personalMode.value = true
  personalUserName.value = result.userName || ''
  personalList.value = result.profiles || []
  currentIndex.value = 0
  if (timer) clearInterval(timer)
  if (personalList.value.length > 1) {
    timer = window.setInterval(next, 6000)
  }
}

function resetPersonalTimer() {
  if (personalTimeout) clearTimeout(personalTimeout)
  personalTimeout = window.setTimeout(exitPersonalMode, 15000) // 15 秒无新人脸退出
}

function exitPersonalMode() {
  personalMode.value = false
  faceMatched.value = false
  personalList.value = []
  personalUserName.value = ''
  currentIndex.value = 0
  if (timer) clearInterval(timer)
  timer = window.setInterval(next, 8000)
}

function stopCamera() {
  if (scanTimer) clearInterval(scanTimer)
  if (personalTimeout) clearTimeout(personalTimeout)
  if (stream) { stream.getTracks().forEach(t => t.stop()); stream = null }
}

// ========== 生命周期 ==========
onMounted(async () => {
  updateClock()
  clockTimer = window.setInterval(updateClock, 1000)

  try {
    const data = await http.get<any, ProfileDTO[]>('/api/display/playlist')
    if (Array.isArray(data) && data.length) {
      playlist.value = data
      timer = window.setInterval(next, 8000)
    }
  } catch (e) {
    console.error('大屏加载失败', e)
  }

  startCamera()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (clockTimer) clearInterval(clockTimer)
  stopCamera()
})
</script>

<style scoped>
.display-screen {
  position: fixed; inset: 0;
  background: #0f141e;
  color: #fff;
  font-family: 'HarmonyOS Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  display: flex; flex-direction: column;
  overflow: hidden;
  user-select: none;
}
.hidden-cam { position: absolute; opacity: 0; pointer-events: none; width: 1px; height: 1px; }
.stars {
  position: absolute; inset: 0;
  background:
    radial-gradient(0.5px 0.5px at 10% 15%, rgba(255,255,255,0.8), transparent),
    radial-gradient(0.5px 0.5px at 25% 40%, rgba(255,255,255,0.6), transparent),
    radial-gradient(0.5px 0.5px at 40% 10%, rgba(255,255,255,0.7), transparent),
    radial-gradient(0.5px 0.5px at 55% 60%, rgba(255,255,255,0.5), transparent),
    radial-gradient(0.5px 0.5px at 70% 25%, rgba(255,255,255,0.9), transparent),
    radial-gradient(0.5px 0.5px at 85% 50%, rgba(255,255,255,0.6), transparent),
    radial-gradient(0.5px 0.5px at 15% 75%, rgba(255,255,255,0.7), transparent),
    radial-gradient(0.5px 0.5px at 60% 85%, rgba(255,255,255,0.5), transparent),
    radial-gradient(0.5px 0.5px at 90% 15%, rgba(255,255,255,0.8), transparent),
    radial-gradient(0.5px 0.5px at 35% 55%, rgba(255,255,255,0.6), transparent),
    radial-gradient(1px 1px at 50% 35%, rgba(120,180,255,0.4), transparent),
    radial-gradient(1px 1px at 20% 80%, rgba(120,180,255,0.3), transparent),
    radial-gradient(1px 1px at 75% 40%, rgba(120,180,255,0.4), transparent);
  pointer-events: none;
}
.header { position: relative; z-index: 2; padding: 32px 48px 0; }
.title {
  font-size: 26px; font-weight: 600; letter-spacing: 6px;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.subtitle { font-size: 12px; color: rgba(255,255,255,0.3); letter-spacing: 4px; margin-top: 2px; }
.main { flex: 1; display: flex; padding: 24px 48px 80px; gap: 48px; align-items: center; }
.left-panel { flex: 0 0 45%; }
.cover-box {
  position: relative; width: 100%; aspect-ratio: 16/10; border-radius: 12px;
  overflow: hidden; transition: opacity 0.6s, transform 0.6s;
}
.cover-box.changing { opacity: 0; transform: scale(0.95); }
.cover-img {
  width: 100%; height: 100%;
  background: linear-gradient(135deg, #1a1f2e, #0d1b2a);
  background-size: cover; background-position: center;
  animation: kenburns 8s ease-in-out infinite alternate;
}
@keyframes kenburns {
  from { transform: scale(1); }
  to { transform: scale(1.12) translate(-2%, 1%); }
}
.cover-gradient {
  position: absolute; inset: 0;
  background: linear-gradient(to top, rgba(15,20,30,0.4) 0%, transparent 50%);
}
.meta-info { display: flex; gap: 10px; margin-top: 16px; }
.college-tag, .year-tag, .level-tag { padding: 4px 12px; border-radius: 4px; font-size: 13px; letter-spacing: 1px; }
.college-tag { background: rgba(79,172,254,0.15); color: #4facfe; }
.year-tag { background: rgba(255,255,255,0.06); color: rgba(255,255,255,0.6); }
.level-tag { background: rgba(255,193,7,0.15); color: #ffc107; }
.right-panel { flex: 1; display: flex; flex-direction: column; justify-content: center; }
.card { transition: opacity 0.6s, transform 0.6s; }
.card.changing { opacity: 0; transform: translateY(20px); }
.category-badge { display: inline-block; font-size: 18px; font-weight: 500; margin-bottom: 16px; letter-spacing: 2px; }
.card-title {
  font-size: 42px; font-weight: 700; line-height: 1.3; margin-bottom: 12px;
  background: linear-gradient(135deg, #fff 0%, #c0d0e0 100%);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.card-author { font-size: 18px; font-weight: 400; color: rgba(255,255,255,0.5); margin-bottom: 24px; }
.card-desc {
  font-size: 16px; line-height: 1.8; color: rgba(255,255,255,0.65); max-width: 560px;
  display: -webkit-box; -webkit-line-clamp: 4; -webkit-box-orient: vertical; overflow: hidden;
}
.card-tags { display: flex; gap: 8px; flex-wrap: wrap; margin: 20px 0; }
.tag {
  padding: 4px 14px; border-radius: 20px; font-size: 13px;
  border: 1px solid rgba(255,255,255,0.12); color: rgba(255,255,255,0.55); letter-spacing: 1px;
}
.card-footer { font-size: 13px; color: rgba(255,255,255,0.25); margin-top: 8px; }
.progress { display: flex; gap: 8px; margin-top: 48px; }
.dot { width: 6px; height: 6px; border-radius: 3px; background: rgba(255,255,255,0.15); transition: all 0.4s; }
.dot.active { width: 32px; background: #4facfe; }
.footer {
  position: absolute; bottom: 24px; right: 48px; z-index: 2;
  font-size: 13px; color: rgba(255,255,255,0.18); letter-spacing: 2px;
}
.footer .matched { color: #4facfe; }
.empty { flex: 1; display: flex; align-items: center; justify-content: center; font-size: 20px; color: rgba(255,255,255,0.2); letter-spacing: 4px; }
</style>
