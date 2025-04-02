<template>
  <div class="main-content">
    <div>角色管理</div>
    <el-divider></el-divider>
    <div class="main-container-mc" style="margin-bottom: 14px">
      <el-button class="btn-style" type="primary" @click="onAdd">
        创建角色
      </el-button>
    </div>

    <div class="main-container-mc">
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="founder" label="创建人" />
        <el-table-column prop="data" label="创建时间" />
        <el-table-column prop="desc" label="描述" />
        <el-table-column prop="operate" label="操作" width="150">
          <template #default>
            <el-button type="primary" link>编辑</el-button>
            <el-button type="danger" link>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>

  <RoleDialogComponents :roleDialog="roleDialog" />
</template>

<script setup lang="ts">
import { reactive } from "vue";
import RoleDialogComponents from "./components/RoleDialog.vue";

interface User {
  name?: string; // 菜单名称
  founder?: string; // 创建人
  grade?: string; // 等级
  desc?: string; // 组件路径
  data?: string; // 更新时间
}

const tableData: User[] = [
  {
    name: "超级管理员",
    founder: "admin",
    data: "2023-04-03 12:03:34",
    desc: "系统所有权",
  },
  {
    name: "部门管理员",
    founder: "admin",
    desc: "普通管理员级别权限",
    data: "2023-04-03 12:03:34",
  },
  {
    name: "普通用户",
    founder: "admin",
    desc: "普通用户基本功能权限",
    data: "2023-04-03 12:03:34",
  },
];

const roleDialog = reactive({
  visible: false,
});

/**
 * 新增
 */
const onAdd = () => {
  roleDialog.visible = true;
};
</script>

<style scoped lang="less">
.btn-style {
  min-width: 90px;
}
</style>
