import { $loginPost } from "../utils/url.ts";
import { ElNotification } from "element-plus";
import other from "../utils/other";
import { useUserStore } from "../stort/user";
import { UserPar } from "../interface/login.ts";
import { Prompt } from "@/common/enum.ts";

const userStore = useUserStore();
const { VITE_PWD_ENC_KEY } = import.meta.env;

// 登录方法
export const $Login = async (params: UserPar) => {
  // 密码加密
  if (VITE_PWD_ENC_KEY) {
    params.password = other.encryption(params.password, VITE_PWD_ENC_KEY);
  }
  const url = `/oauth2/token?username=${params.username}&grant_type=password&scope=server&password=${params.password}`;
  let ret = await $loginPost(url);
  if (ret.username) {
    ElNotification({
      title: "通知",
      message: Prompt.LOGIN_SUCCEE,
      type: "success",
    });

    // 持久化存储用户信息
    const user: any = {
      userid: ret.user_info.id,
      username: ret.username,
      displayName: ret.user_info.displayName,
      permissions: [],
      isAuthenticated: true,
      token: ret.access_token,
    };
    // 获取权限
    for (let i = 0; i < ret.user_info.roles.length; i++) {
      user.permissions.push(ret.user_info.roles[i]);
    }

    userStore.setUser(user); // 存储用户信息

    // 浏览器存储用户信息
    const user_info = {
      userid: ret.user_info.id,
      username: ret.username,
      displayName: ret.user_info.displayName,
      permissions: user.permissions,
      isAuthenticated: true,
      token: ret.access_token,
    };
    // 将用户信息存储到浏览器缓存中
    sessionStorage.setItem("user_Info", JSON.stringify(user_info));
    return true;
  } else {
    ElNotification({
      title: "通知",
      message: ret.message,
      type: "error",
    });
    return false;
  }
};
