import { Prompt } from "@/common/enum";
import { useUserStore } from "@/stort/user";
import axios from "axios";
import { ElNotification } from "element-plus";
import { useRouter } from "vue-router";

// 配置代理
const {
  VITE_OAUTH_PASSWORD_CLIENT,
  VITE_ENV,
  VITE_API_URL,
  VITE_ADMIN_PROXY_PATH,
} = import.meta.env;

// 初始化一个axios对象
const instance = axios.create({
  baseURL: VITE_ENV == "development" ? VITE_API_URL : VITE_ADMIN_PROXY_PATH, // 服务器路径
  timeout: 30000,
  headers: {
    skipToken: true,
    Authorization: "Basic " + window.btoa(VITE_OAUTH_PASSWORD_CLIENT),
    "Content-Type": "application/json; application/octet-stream",
  },
});

// 请求拦截器
instance.interceptors.request.use(
  function (config) {
    // 获取登录用户信息token 向请求头添加token
    const formatInfo = sessionStorage.getItem("user_Info");
    if (formatInfo) {
      const userInfo = JSON.parse(formatInfo);
      if (userInfo.token) {
        config.headers["Authorization"] = `Bearer ${userInfo.token}`;
      }
    }
    return config;
  },
  function (error) {
    // 对请求错误做些什么
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  function (response) {
    if (response.headers["content-disposition"]) {
      response.data.contentDisposition =
        response.headers["content-disposition"];
    }
    // 对响应数据做点什么
    return response;
  },
  function (error) {
    if (error.response) {
      const { status, data } = error.response;
      switch (status) {
        case 400:
          ElNotification({
            title: "提示",
            message: data.msg,
            type: "error",
          });
          break;
        case 401:
          ElNotification({
            title: "提示",
            message: data.msg,
            type: "error",
          });
          break;
        case 404:
          ElNotification({
            title: "提示",
            message: "接口请求异常！",
            type: "error",
          });
          break;
        case 424: // 424状态 指令过去
          const userStore = useUserStore();
          const router = useRouter();
          ElNotification({
            title: "提示",
            message: Prompt.TOKEN_EXPIRATION,
            type: "error",
          });
          userStore.clearUser();
          router.push("/");
          break;
        case 500:
          if (data.msg) {
            ElNotification({
              title: "提示",
              message: data.msg,
              type: "error",
            });
          } else {
            ElNotification({
              title: "通知",
              message: "服务器错误，请联系管理员！",
              type: "error",
            });
          }
          break;
      }
    } else if (error.request) {
      // 请求已经成功发起，但没有收到响应
      ElNotification({
        title: "提示",
        message: "接口请求失败！",
        type: "error",
      });
    } else {
      // 某种原因在设置请求时触发了错误
      ElNotification({
        title: "提示",
        message: error.message,
        type: "error",
      });
    }

    return Promise.reject(error);
  }
);

export default instance;
