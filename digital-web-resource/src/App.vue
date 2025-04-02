<template>
  <router-view></router-view>
</template>

<script setup lang="ts">
import { useUserStore } from "./stort/user";

const userStore = useUserStore();

// 获取登录用户信息
const userName = sessionStorage.getItem("user_Info");
if (userName) {
  const userInfo = JSON.parse(userName);
  userStore.setUser(userInfo);
}
</script>
