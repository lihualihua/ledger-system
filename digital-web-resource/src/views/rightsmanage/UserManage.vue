<template>
  <div class="main-content">
    <div>用户管理</div>
    <el-divider></el-divider>
    <div class="main-container-mc" style="margin-bottom: 14px">
      <el-form :inline="true" :model="formInline" class="demo-form-inline">
        <el-form-item label="账号（姓名）">
          <el-input
            v-model="formInline.user"
            placeholder="输入账号（姓名）"
            clearable
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-select
            v-model="formInline.region"
            placeholder="选择部门"
            clearable
            style="width: 240px"
          >
            <el-option label="金融部" value="shanghai" />
            <el-option label="技术部" value="beijing" />
          </el-select>
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input
            v-model="formInline.date"
            placeholder="输入邮箱"
            clearable
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary">查询</el-button>
          <el-button>重置</el-button>
        </el-form-item>
      </el-form>
      <el-button class="btn-style" type="primary" @click="onAdd">
        创建用户
      </el-button>
    </div>

    <div class="main-container-mc">
      <el-table :data="tableData" row-key="id" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="accountNum" label="账号" width="160" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="sex" label="性别" />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column prop="mail" label="邮箱" width="200" />
        <el-table-column prop="section" label="部门" />
        <el-table-column prop="role" label="角色" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <span v-show="scope.row.status">
              <el-tag type="success">正常</el-tag>
            </span>
            <span v-show="!scope.row.status">
              <el-tag type="info">失效</el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="创建人" />
        <el-table-column prop="data" label="创建时间" width="180" />
        <el-table-column fixed="right" prop="operate" label="操作" width="120">
          <template #default>
            <el-button type="primary" link>编辑</el-button>
            <el-button type="danger" link>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>

  <UserDialogComponents :userDialog="userDialog" />
</template>

<script setup lang="ts">
import { reactive } from "vue";
import UserDialogComponents from "./components/UserDialog.vue";

interface User {
  accountNum?: string; // 账号
  name?: string; // 姓名
  sex?: string; // 性别
  phone?: string; // 电话
  mail?: string; // 邮箱
  section?: string; // 部门
  role?: string; // 角色
  status?: boolean; // 角色
  creator?: string; // 创建人
  data?: string; // 创建时间
}

const tableData: User[] = [
  {
    accountNum: "ex_zhangsan2", // 账号
    name: "张三", // 姓名
    sex: "男", // 性别
    phone: "15099955652", // 电话
    mail: "qwer123@163.com", // 邮箱
    section: "技术部", // 部门
    role: "普通用户", // 角色
    status: true,
    creator: "admin", // 创建人
    data: "2025-02-01 12:21:00", // 创建时间
  },
  {
    accountNum: "ex_wangwu2", // 账号
    name: "王五", // 姓名
    sex: "男", // 性别
    phone: "15099955652", // 电话
    mail: "qwer123@163.com", // 邮箱
    section: "技术部", // 部门
    role: "普通用户", // 角色
    status: false,
    creator: "admin", // 创建人
    data: "2025-02-01 12:21:00", // 创建时间
  },
];

const userDialog = reactive({
  visible: false,
});

const formInline = reactive({
  user: "",
  region: "",
  date: "",
});

/**
 * 新增
 */
const onAdd = () => {
  userDialog.visible = true;
};
</script>

<style scoped lang="less">
.btn-style {
  min-width: 90px;
}
</style>
