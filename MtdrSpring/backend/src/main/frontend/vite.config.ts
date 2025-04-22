// vite.config.ts
import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig(({ command }) => {
  const baseConfig = {
    plugins: [react(), tailwindcss()],
    build: {
      outDir: './build',
    },
    server: {
      watch: {
        usePolling: true,
      },
      hmr: {
        overlay: true,
      },
    },
    optimizeDeps: {
      force: true,
    },
    test: {
      globals: true,
      environment: 'jsdom',
      setupFiles: './vitest.setup.ts',
    },
  }

  if (command === 'serve') {
    return {
      ...baseConfig,
      server: {
        ...baseConfig.server,
        proxy: {
          '/api': 'http://localhost:8081',
        },
      },
    }
  }

  return baseConfig
})
