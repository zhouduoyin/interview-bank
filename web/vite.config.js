import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

/**
 * Vite 工程化配置。
 * - 开发/预览端口、代理目标、部署子路径均可用环境变量覆盖
 * - 生产构建默认不生成 sourcemap，需要排查线上问题时设 VITE_BUILD_SOURCEMAP=true
 */
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const proxyTarget = env.VITE_PROXY_TARGET || 'http://127.0.0.1:8090'

  return {
    base: env.VITE_PUBLIC_BASE || '/',
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      host: env.VITE_DEV_HOST || '127.0.0.1',
      port: Number(env.VITE_DEV_PORT || 5173),
      strictPort: env.VITE_STRICT_PORT === 'true',
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: true,
          secure: false
        }
      }
    },
    preview: {
      host: env.VITE_PREVIEW_HOST || '127.0.0.1',
      port: Number(env.VITE_PREVIEW_PORT || 4173)
    },
    build: {
      outDir: 'dist',
      emptyOutDir: true,
      sourcemap: env.VITE_BUILD_SOURCEMAP === 'true',
      assetsInlineLimit: 4096,
      chunkSizeWarningLimit: 2500
    }
  }
})
