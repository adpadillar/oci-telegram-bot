import { defineConfig } from "vite";
import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";

// https://vite.dev/config/

export default defineConfig(({ command }) => {
  if (command === "serve") {
    return {
      plugins: [react(), tailwindcss()],
      build: {
        outDir: "./build",
      },
      server: {
        watch: {
          usePolling: true,
        },
        hmr: {
          overlay: true,
        },
        proxy: {
          "/api": "http://localhost:8081",
        },
      },
      optimizeDeps: {
        force: true,
      },
    };
  } else {
    // command === 'build'
    return {
      plugins: [react(), tailwindcss()],
      build: {
        outDir: "./build",
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
    };
  }
});
