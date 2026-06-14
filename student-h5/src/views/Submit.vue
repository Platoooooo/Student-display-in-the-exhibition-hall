<template>
  <van-nav-bar title="提交成果" left-arrow @click-left="$router.back()" />
  <van-form @submit="onSubmit">
    <van-cell-group inset>
      <van-field v-model="form.title" name="title" label="标题" placeholder="如：全国大学生计算机大赛一等奖" :rules="[{ required: true }]" />
      <van-field name="category" label="类目">
        <template #input>
          <van-radio-group v-model="form.category" direction="horizontal">
            <van-radio :name="1">荣誉</van-radio>
            <van-radio :name="2">作品</van-radio>
            <van-radio :name="3">成绩</van-radio>
            <van-radio :name="4">活动</van-radio>
          </van-radio-group>
        </template>
      </van-field>
      <van-field v-model="form.achieveLevel" name="achieveLevel" label="级别" placeholder="国家级 / 省级 / 校级" />
      <van-field v-model="form.issuingOrg" name="issuingOrg" label="颁发机构" placeholder="如教育部 / XX 协会" />
      <van-field name="achieveDate" label="取得日期">
        <template #input>
          <van-button size="small" @click.prevent="datePicker = true">{{ form.achieveDate || '选择日期' }}</van-button>
          <van-popup v-model:show="datePicker" position="bottom">
            <van-date-picker :model-value="dateArr" @confirm="onDate" @cancel="datePicker = false" />
          </van-popup>
        </template>
      </van-field>
      <van-field v-model="form.description" name="description" label="描述" type="textarea" rows="3" autosize maxlength="500" show-word-limit />
    </van-cell-group>

    <van-cell-group inset title="封面" style="margin-top:12px">
      <van-uploader v-model="coverList" :max-count="1" :after-read="onCoverRead" />
    </van-cell-group>

    <van-cell-group inset title="附件（图片/视频，最多9个）" style="margin-top:12px">
      <van-uploader v-model="mediaList" :max-count="9" multiple :after-read="onMediaRead" />
    </van-cell-group>

    <div style="margin: 16px;">
      <van-button block type="primary" native-type="submit" :loading="loading">提 交 审 核</van-button>
      <van-button block plain style="margin-top:8px" @click.prevent="onDraft">保存草稿</van-button>
    </div>
  </van-form>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { showToast } from 'vant'
import { apiUpload, apiProfileSubmit, apiProfileDraft } from '@/api'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const datePicker = ref(false)

const form = reactive<any>({
  title: '', category: 1, description: '', coverUrl: '',
  achieveDate: '', achieveLevel: '', issuingOrg: '',
  mediaList: [] as any[], tagIds: [] as number[],
})

const coverList = ref<any[]>([])
const mediaList = ref<any[]>([])
const dateArr = computed(() => form.achieveDate ? form.achieveDate.split('-') : [])

function onDate(v: any) {
  form.achieveDate = v.selectedValues.join('-')
  datePicker.value = false
}

async function onCoverRead(file: any) {
  const f = Array.isArray(file) ? file[0] : file
  const r = await apiUpload(f.file, 'cover')
  form.coverUrl = r.url
  showToast('封面上传成功')
}

async function onMediaRead(file: any) {
  const list = Array.isArray(file) ? file : [file]
  for (const f of list) {
    const r = await apiUpload(f.file, 'media')
    const isVideo = (f.file.type || '').startsWith('video')
    form.mediaList.push({ mediaType: isVideo ? 2 : 1, fileUrl: r.url, fileName: r.name, fileSize: r.size })
  }
  showToast('附件上传完成')
}

async function onSubmit() {
  loading.value = true
  try { await apiProfileSubmit(form); showToast('已提交，等待院级审核'); router.push('/me') }
  finally { loading.value = false }
}
async function onDraft() {
  await apiProfileDraft(form); showToast('草稿已保存'); router.push('/me')
}
</script>
