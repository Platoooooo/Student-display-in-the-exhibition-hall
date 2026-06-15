import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from '@vant/auto-import-resolver'
import path from 'node:path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'VITE_')
  const apiTarget = env.VITE_API_BASE_URL || 'http://localhost:8080'

  return {
    plugins: [vue(), Components({ resolvers: [VantResolver()] })],
    resolve: { alias: { '@': path.resolve(__dirname, 'src') } },
    server: {
      host: '0.0.0.0',
      port: 5174,
      proxy: { '/api': { target: apiTarget, changeOrigin: true } },
    },
    build: { outDir: 'dist', sourcemap: false },
  }
})
