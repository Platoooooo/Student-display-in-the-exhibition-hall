<template>
  <RouterView />
  <van-tabbar v-if="showTab" v-model="active" route>
    <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
    <van-tabbar-item icon="edit" to="/submit">提交</van-tabbar-item>
    <van-tabbar-item icon="user-o" to="/me">我的</van-tabbar-item>
  </van-tabbar>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
const route = useRoute()
const active = ref(0)
const showTab = computed(() => !['/login'].includes(route.path))
</script>

<style>
@font-face {
  font-family: 'HarmonyOS Sans SC';
  src: url('/fonts/HarmonyOS_SansSC_Regular.ttf') format('truetype');
  font-weight: 400;
  font-display: swap;
}
@font-face {
  font-family: 'HarmonyOS Sans SC';
  src: url('/fonts/HarmonyOS_SansSC_Bold.ttf') format('truetype');
  font-weight: 700;
  font-display: swap;
}
html, body {
  font-family: 'HarmonyOS Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
</style>
