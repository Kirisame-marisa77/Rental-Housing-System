import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 社区房屋租赁 H5（业主端 + 租客端）：端口 8081，代理 /admin-api 到后端 48080
export default defineConfig({
  plugins: [vue()],
  server: {
    host: true,
    port: 8081,
    proxy: {
      '/admin-api': {
        target: 'http://localhost:48080',
        changeOrigin: true
      }
    }
  }
})
