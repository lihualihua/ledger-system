import { defineStore } from "pinia";

interface UserState {
  user: {
    // 用户id
    userid?: string;
    // 用户昵称
    username: string;
    // 用户真实姓名
    displayName: string;
    // 权限
    permissions: string[];
    // 登录状态认证
    isAuthenticated: boolean;
    // 登录状态
    token: string;
  };
}

/**
 * pinia持久化存储
 * @userid 用户id
 * @username 用户昵称
 * @displayName 用户真实姓名
 * @permissions 权限
 */
export const useUserStore = defineStore("user", {
  state: (): UserState => ({
    user: {
      username: "",
      displayName: "",
      permissions: [],
      isAuthenticated: false,
      token: "",
    },
  }),
  actions: {
    setUser(user: UserState["user"]) {
      this.user = user;
    },
    // 登出全部清空
    clearUser() {
      sessionStorage.clear();
      localStorage.clear();
      this.user = {
        username: "",
        displayName: "",
        permissions: [],
        isAuthenticated: false,
        token: "",
      };
    },
  },
  getters: {
    // 按钮权限控制
    hasPermission: (state) => (permission: string) => {
      return state.user.permissions.includes(permission);
    },
  },
});
