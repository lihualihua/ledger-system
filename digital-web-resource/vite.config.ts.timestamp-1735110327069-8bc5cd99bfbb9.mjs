// vite.config.ts
import { defineConfig, loadEnv } from "file:///E:/ProjectFile/ledger-system/digital-web-resource/node_modules/vite/dist/node/index.js";
import vue from "file:///E:/ProjectFile/ledger-system/digital-web-resource/node_modules/@vitejs/plugin-vue/dist/index.mjs";
import { createSvgIconsPlugin } from "file:///E:/ProjectFile/ledger-system/digital-web-resource/node_modules/vite-plugin-svg-icons/dist/index.mjs";
import path from "path";
var __vite_injected_original_dirname = "E:\\ProjectFile\\ledger-system\\digital-web-resource";
var vite_config_default = defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd());
  console.log(env.VITE_ADMIN_PROXY_PATH);
  return {
    base: "./",
    plugins: [
      vue(),
      createSvgIconsPlugin({
        // 指定需要缓存的图标文件夹
        iconDirs: [path.resolve(__vite_injected_original_dirname, "src/assets")],
        // 指定symbolId格式
        symbolId: "icon-[dir]-[name]"
      })
    ],
    server: {
      host: "0.0.0.0",
      // 服务器地址
      open: env.VITE_OPEN === "true",
      // 是否自动打开浏览器
      hmr: true,
      // 启用热更新
      proxy: {
        [env.VITE_API_URL]: {
          target: env.VITE_ADMIN_PROXY_PATH,
          changeOrigin: true,
          rewrite: (path2) => path2.replace(/^\/api/, ""),
          secure: false
          // 如果目标地址是 HTTPS，设置为 true
        }
      }
    },
    // 别名配置，引用src路径下的东西可以通过@ 如import Statistics from "./components/Statistics.vue"
    resolve: {
      alias: {
        "@": path.resolve(__vite_injected_original_dirname, "./src")
      }
    },
    build: {
      outDir: "dist",
      // 打包输出目录
      rollupOptions: {
        output: {
          entryFileNames: "js/[name]-[hash].js",
          chunkFileNames: "js/[name]-[hash].js",
          assetFileNames(assetInfo) {
            if (assetInfo.name?.endsWith(".css")) {
              return "css/[name]-[hash].css";
            }
            const imgExts = [
              ".png",
              ".jpg",
              ".jpeg",
              ".webp",
              ".svg",
              ".gif",
              ".ico"
            ];
            if (imgExts.some((ext) => assetInfo.name?.endsWith(ext))) {
              return "img/[name]-[hash][ext]";
            }
            return "assets/[name]-[hash][ext]";
          },
          compact: true,
          manualChunks: {
            vue: ["vue", "vue-router", "pinia"]
          }
        }
      },
      terserOptions: {
        compress: {
          // 关闭未使用变量和函数的检查
          unused: false,
          dead_code: false,
          drop_console: true,
          // 删除 console
          drop_debugger: true
          // 删除 debugger
        },
        format: {
          comments: false
          // 删除所有注释
        },
        output: {
          // 移除注释内容
          comments: false
        }
      }
    },
    css: { preprocessorOptions: { css: { charset: false } } }
  };
});
export {
  vite_config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcudHMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJFOlxcXFxQcm9qZWN0RmlsZVxcXFxsZWRnZXItc3lzdGVtXFxcXGRpZ2l0YWwtd2ViLXJlc291cmNlXCI7Y29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2ZpbGVuYW1lID0gXCJFOlxcXFxQcm9qZWN0RmlsZVxcXFxsZWRnZXItc3lzdGVtXFxcXGRpZ2l0YWwtd2ViLXJlc291cmNlXFxcXHZpdGUuY29uZmlnLnRzXCI7Y29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2ltcG9ydF9tZXRhX3VybCA9IFwiZmlsZTovLy9FOi9Qcm9qZWN0RmlsZS9sZWRnZXItc3lzdGVtL2RpZ2l0YWwtd2ViLXJlc291cmNlL3ZpdGUuY29uZmlnLnRzXCI7aW1wb3J0IHsgZGVmaW5lQ29uZmlnLCBsb2FkRW52IH0gZnJvbSBcInZpdGVcIjtcbmltcG9ydCB2dWUgZnJvbSBcIkB2aXRlanMvcGx1Z2luLXZ1ZVwiO1xuaW1wb3J0IHsgY3JlYXRlU3ZnSWNvbnNQbHVnaW4gfSBmcm9tIFwidml0ZS1wbHVnaW4tc3ZnLWljb25zXCI7XG5pbXBvcnQgcGF0aCBmcm9tIFwicGF0aFwiO1xuXG4vLyBodHRwczovL3ZpdGVqcy5kZXYvY29uZmlnL1xuZXhwb3J0IGRlZmF1bHQgZGVmaW5lQ29uZmlnKCh7IG1vZGUgfSkgPT4ge1xuICAvLyBcdTUyQTBcdThGN0RcdTczQUZcdTU4ODNcdTUzRDhcdTkxQ0ZcbiAgY29uc3QgZW52ID0gbG9hZEVudihtb2RlLCBwcm9jZXNzLmN3ZCgpKTtcbiAgY29uc29sZS5sb2coZW52LlZJVEVfQURNSU5fUFJPWFlfUEFUSCk7XG5cbiAgcmV0dXJuIHtcbiAgICBiYXNlOiBcIi4vXCIsXG4gICAgcGx1Z2luczogW1xuICAgICAgdnVlKCksXG4gICAgICBjcmVhdGVTdmdJY29uc1BsdWdpbih7XG4gICAgICAgIC8vIFx1NjMwN1x1NUI5QVx1OTcwMFx1ODk4MVx1N0YxM1x1NUI1OFx1NzY4NFx1NTZGRVx1NjgwN1x1NjU4N1x1NEVGNlx1NTkzOVxuICAgICAgICBpY29uRGlyczogW3BhdGgucmVzb2x2ZShfX2Rpcm5hbWUsIFwic3JjL2Fzc2V0c1wiKV0sXG4gICAgICAgIC8vIFx1NjMwN1x1NUI5QXN5bWJvbElkXHU2ODNDXHU1RjBGXG4gICAgICAgIHN5bWJvbElkOiBcImljb24tW2Rpcl0tW25hbWVdXCIsXG4gICAgICB9KSxcbiAgICBdLFxuICAgIHNlcnZlcjoge1xuICAgICAgaG9zdDogXCIwLjAuMC4wXCIsIC8vIFx1NjcwRFx1NTJBMVx1NTY2OFx1NTczMFx1NTc0MFxuICAgICAgb3BlbjogZW52LlZJVEVfT1BFTiA9PT0gXCJ0cnVlXCIsIC8vIFx1NjYyRlx1NTQyNlx1ODFFQVx1NTJBOFx1NjI1M1x1NUYwMFx1NkQ0Rlx1ODlDOFx1NTY2OFxuICAgICAgaG1yOiB0cnVlLCAvLyBcdTU0MkZcdTc1MjhcdTcwRURcdTY2RjRcdTY1QjBcbiAgICAgIHByb3h5OiB7XG4gICAgICAgIFtlbnYuVklURV9BUElfVVJMXToge1xuICAgICAgICAgIHRhcmdldDogZW52LlZJVEVfQURNSU5fUFJPWFlfUEFUSCxcbiAgICAgICAgICBjaGFuZ2VPcmlnaW46IHRydWUsXG4gICAgICAgICAgcmV3cml0ZTogKHBhdGgpID0+IHBhdGgucmVwbGFjZSgvXlxcL2FwaS8sIFwiXCIpLFxuICAgICAgICAgIHNlY3VyZTogZmFsc2UsIC8vIFx1NTk4Mlx1Njc5Q1x1NzZFRVx1NjgwN1x1NTczMFx1NTc0MFx1NjYyRiBIVFRQU1x1RkYwQ1x1OEJCRVx1N0Y2RVx1NEUzQSB0cnVlXG4gICAgICAgIH0sXG4gICAgICB9LFxuICAgIH0sXG5cbiAgICAvLyBcdTUyMkJcdTU0MERcdTkxNERcdTdGNkVcdUZGMENcdTVGMTVcdTc1MjhzcmNcdThERUZcdTVGODRcdTRFMEJcdTc2ODRcdTRFMUNcdTg5N0ZcdTUzRUZcdTRFRTVcdTkwMUFcdThGQzdAIFx1NTk4MmltcG9ydCBTdGF0aXN0aWNzIGZyb20gXCIuL2NvbXBvbmVudHMvU3RhdGlzdGljcy52dWVcIlxuICAgIHJlc29sdmU6IHtcbiAgICAgIGFsaWFzOiB7XG4gICAgICAgIFwiQFwiOiBwYXRoLnJlc29sdmUoX19kaXJuYW1lLCBcIi4vc3JjXCIpLFxuICAgICAgfSxcbiAgICB9LFxuICAgIGJ1aWxkOiB7XG4gICAgICBvdXREaXI6IFwiZGlzdFwiLCAvLyBcdTYyNTNcdTUzMDVcdThGOTNcdTUxRkFcdTc2RUVcdTVGNTVcbiAgICAgIHJvbGx1cE9wdGlvbnM6IHtcbiAgICAgICAgb3V0cHV0OiB7XG4gICAgICAgICAgZW50cnlGaWxlTmFtZXM6IFwianMvW25hbWVdLVtoYXNoXS5qc1wiLFxuICAgICAgICAgIGNodW5rRmlsZU5hbWVzOiBcImpzL1tuYW1lXS1baGFzaF0uanNcIixcbiAgICAgICAgICBhc3NldEZpbGVOYW1lcyhhc3NldEluZm8pIHtcbiAgICAgICAgICAgIGlmIChhc3NldEluZm8ubmFtZT8uZW5kc1dpdGgoXCIuY3NzXCIpKSB7XG4gICAgICAgICAgICAgIHJldHVybiBcImNzcy9bbmFtZV0tW2hhc2hdLmNzc1wiO1xuICAgICAgICAgICAgfVxuICAgICAgICAgICAgY29uc3QgaW1nRXh0cyA9IFtcbiAgICAgICAgICAgICAgXCIucG5nXCIsXG4gICAgICAgICAgICAgIFwiLmpwZ1wiLFxuICAgICAgICAgICAgICBcIi5qcGVnXCIsXG4gICAgICAgICAgICAgIFwiLndlYnBcIixcbiAgICAgICAgICAgICAgXCIuc3ZnXCIsXG4gICAgICAgICAgICAgIFwiLmdpZlwiLFxuICAgICAgICAgICAgICBcIi5pY29cIixcbiAgICAgICAgICAgIF07XG4gICAgICAgICAgICBpZiAoaW1nRXh0cy5zb21lKChleHQpID0+IGFzc2V0SW5mby5uYW1lPy5lbmRzV2l0aChleHQpKSkge1xuICAgICAgICAgICAgICByZXR1cm4gXCJpbWcvW25hbWVdLVtoYXNoXVtleHRdXCI7XG4gICAgICAgICAgICB9XG4gICAgICAgICAgICByZXR1cm4gXCJhc3NldHMvW25hbWVdLVtoYXNoXVtleHRdXCI7XG4gICAgICAgICAgfSxcbiAgICAgICAgICBjb21wYWN0OiB0cnVlLFxuICAgICAgICAgIG1hbnVhbENodW5rczoge1xuICAgICAgICAgICAgdnVlOiBbXCJ2dWVcIiwgXCJ2dWUtcm91dGVyXCIsIFwicGluaWFcIl0sXG4gICAgICAgICAgfSxcbiAgICAgICAgfSxcbiAgICAgIH0sXG4gICAgICB0ZXJzZXJPcHRpb25zOiB7XG4gICAgICAgIGNvbXByZXNzOiB7XG4gICAgICAgICAgLy8gXHU1MTczXHU5NUVEXHU2NzJBXHU0RjdGXHU3NTI4XHU1M0Q4XHU5MUNGXHU1NDhDXHU1MUZEXHU2NTcwXHU3Njg0XHU2OEMwXHU2N0U1XG4gICAgICAgICAgdW51c2VkOiBmYWxzZSxcbiAgICAgICAgICBkZWFkX2NvZGU6IGZhbHNlLFxuICAgICAgICAgIGRyb3BfY29uc29sZTogdHJ1ZSwgLy8gXHU1MjIwXHU5NjY0IGNvbnNvbGVcbiAgICAgICAgICBkcm9wX2RlYnVnZ2VyOiB0cnVlLCAvLyBcdTUyMjBcdTk2NjQgZGVidWdnZXJcbiAgICAgICAgfSxcbiAgICAgICAgZm9ybWF0OiB7XG4gICAgICAgICAgY29tbWVudHM6IGZhbHNlLCAvLyBcdTUyMjBcdTk2NjRcdTYyNDBcdTY3MDlcdTZDRThcdTkxQ0FcbiAgICAgICAgfSxcbiAgICAgICAgb3V0cHV0OiB7XG4gICAgICAgICAgLy8gXHU3OUZCXHU5NjY0XHU2Q0U4XHU5MUNBXHU1MTg1XHU1QkI5XG4gICAgICAgICAgY29tbWVudHM6IGZhbHNlLFxuICAgICAgICB9LFxuICAgICAgfSxcbiAgICB9LFxuICAgIGNzczogeyBwcmVwcm9jZXNzb3JPcHRpb25zOiB7IGNzczogeyBjaGFyc2V0OiBmYWxzZSB9IH0gfSxcbiAgfTtcbn0pO1xuIl0sCiAgIm1hcHBpbmdzIjogIjtBQUE2VSxTQUFTLGNB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
