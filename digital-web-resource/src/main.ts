import { createApp, createVNode } from "vue";
import "./style.css";
import App from "./App.vue";
import vPermission from "./stort/permission";

// 导入路由器 组件库
import router from "./router";
// 持久化存储
import { createPinia } from "pinia";
const pinia = createPinia();

import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import zhCn from "element-plus/es/locale/lang/zh-cn";

import * as Icons from "@element-plus/icons-vue"; // 引入Icon组件

// svg图标
import "virtual:svg-icons-register";

const app = createApp(App);

// 创建Icon组件
const Icon = (props: { icon: string }) => {
  const { icon } = props;
  if (icon) {
    return createVNode(Icons[icon as keyof typeof Icons]);
  }
};

app.use(router);
app.use(ElementPlus, {
  locale: zhCn,
});
app.use(pinia);
app.directive("permission", vPermission); // 创建权限指令
app.mount("#app");
app.component("Icon", Icon); // 注册Icon组件
