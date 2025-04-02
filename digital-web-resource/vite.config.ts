import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import { createSvgIconsPlugin } from "vite-plugin-svg-icons";
import path from "path";

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd());
  console.log(env.VITE_ADMIN_PROXY_PATH);

  return {
    base: "./",
    plugins: [
      vue(),
      createSvgIconsPlugin({
        // 指定需要缓存的图标文件夹
        iconDirs: [path.resolve(__dirname, "src/assets")],
        // 指定symbolId格式
        symbolId: "icon-[dir]-[name]",
      }),
    ],
    server: {
      host: "0.0.0.0", // 服务器地址
      open: env.VITE_OPEN === "true", // 是否自动打开浏览器
      hmr: true, // 启用热更新
      proxy: {
        [env.VITE_API_URL]: {
          target: env.VITE_ADMIN_PROXY_PATH,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ""),
          secure: false, // 如果目标地址是 HTTPS，设置为 true
        },
      },
    },

    // 别名配置，引用src路径下的东西可以通过@ 如import Statistics from "./components/Statistics.vue"
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
    build: {
      outDir: "dist", // 打包输出目录
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
              ".ico",
            ];
            if (imgExts.some((ext) => assetInfo.name?.endsWith(ext))) {
              return "img/[name]-[hash][ext]";
            }
            return "assets/[name]-[hash][ext]";
          },
          compact: true,
          manualChunks: {
            vue: ["vue", "vue-router", "pinia"],
          },
        },
      },
      terserOptions: {
        compress: {
          // 关闭未使用变量和函数的检查
          unused: false,
          dead_code: false,
          drop_console: true, // 删除 console
          drop_debugger: true, // 删除 debugger
        },
        format: {
          comments: false, // 删除所有注释
        },
        output: {
          // 移除注释内容
          comments: false,
        },
      },
    },
    css: { preprocessorOptions: { css: { charset: false } } },
  };
});
