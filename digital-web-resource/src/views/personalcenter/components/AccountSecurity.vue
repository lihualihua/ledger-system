<template>
  <h4>账号安全</h4>
  <div
    style="
      background: #e5e5e5;
      background-color: rgb(0 0 0 / 0.06);
      height: 1px;
      margin: 20px 0;
    "
  ></div>
  <el-form label-position="left" label-width="140" style="font-weight: 600">
    <el-form-item label="账号安全等级">
      <div style="width: 300px; display: flex; position: relative; top: 4px">
        <AccountSecurityLevel :level="securityLevel" />
      </div>
    </el-form-item>
    <el-form-item label="旧密码">
      <el-input
        v-model="formInline.region"
        placeholder="输入旧密码"
        clearable
        type="password"
        :show-password="true"
        style="width: 300px"
      />
    </el-form-item>

    <el-form-item label="新密码">
      <el-input
        v-model="formInline.date"
        placeholder="输入新密码"
        clearable
        type="password"
        :show-password="true"
        style="width: 300px"
      />
    </el-form-item>
    <el-form-item label="确认新密码">
      <el-input
        v-model="formInline.date"
        placeholder="输入确认新密码"
        clearable
        type="password"
        :show-password="true"
        style="width: 300px"
      />
    </el-form-item>
    <el-form-item class="dialog-footer">
      <el-button class="btn-style" type="primary">修改密码</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import AccountSecurityLevel from "./AccountSecurityLevel.vue";

const formInline = reactive({
  user: "",
  region: "",
  date: "",
});

// 初始化安全等级
const securityLevel = ref(0);
</script>

<style scoped lang="less">
.dialog-footer {
  margin-top: 40px;
  .btn-style {
    min-width: 90px;
  }
}
</style>
