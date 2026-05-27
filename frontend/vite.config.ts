import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from 'path'

export default defineConfig({
  plugins: [react(), tailwindcss()],

  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },

  server: {
    port: 5173,
    proxy: {
      // REST API → Spring Boot (IntelliJ run config on :8080)
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // WebSocket STOMP → Spring Boot
      '/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
      },
    },
  },

  build: {
    outDir: 'dist',
    sourcemap: false,
    rollupOptions: {
      output: {
        // Split large vendor chunks for better caching
        manualChunks(id) {
          if (id.includes('react-dom') || id.includes('react-router')) return 'vendor'
          if (id.includes('@tanstack')) return 'query'
          if (id.includes('recharts')) return 'charts'
          if (id.includes('@stomp') || id.includes('sockjs')) return 'stomp'
        },
      },
    },
  },
})