<template>
  <div class="main-content">
    <div>菜单管理</div>
    <el-divider></el-divider>
    <div class="main-container-mc" style="margin-bottom: 14px">
      <el-button type="primary" class="btn-style" @click="onAdd">
        新增菜单
      </el-button>
    </div>

    <div class="main-container-mc">
      <el-table
        :data="tableData"
        default-expand-all
        row-key="id"
        style="width: 100%"
      >
        <el-table-column
          fixed="left"
          prop="name"
          label="菜单名称"
          width="160"
        />
        <el-table-column prop="icon" label="图标" width="70">
          <template #default="scope">
            <Icon :icon="scope.row.icon" class="icon" />
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <span v-if="scope.row.type == 1">
              <el-tag type="primary">菜单</el-tag>
            </span>
            <span v-else>
              <el-tag type="info">目录</el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="routePath" label="路由地址" />
        <el-table-column prop="status" label="权限标识">
          <template #default="scope">
            <span v-show="scope.row.status">
              超级管理员、部门管理员、普通用户
            </span>
            <span v-show="!scope.row.status">普通用户</span>
          </template>
        </el-table-column>
        <el-table-column prop="componentPath" label="组件路径" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <span v-show="scope.row.status">
              <el-tag type="success">启用</el-tag>
            </span>
            <span v-show="!scope.row.status">
              <el-tag type="danger">禁用</el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="data" label="更新时间" width="200" />
        <el-table-column fixed="right" prop="operate" label="操作" width="150">
          <template #default>
            <el-button type="primary" link>编辑</el-button>
            <el-button type="danger" link>禁用</el-button>
            <el-button type="danger" link>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>

  <MenuDialogComponents :menuDialog="menuDialog" />
</template>

<script setup lang="ts">
import { reactive } from "vue";
import MenuDialogComponents from "./components/MenuDialog.vue";

interface User {
  id?: number; // 序号id
  name?: string; // 菜单名称
  icon?: string; // 图标
  sort?: number; // 排序
  type?: number; // 1: 菜单  2：目录
  routePath?: string; // 路由地址
  componentPath?: string; // 组件路径
  status?: boolean; // true：已启用  false: 已禁用
  data?: string; // 更新时间
  children?: User[];
}

const tableData: User[] = [
  {
    id: 1,
    name: "工作台",
    icon: "HomeFilled",
    sort: 1,
    type: 1,
    componentPath: "workbench/Workbench",
    routePath: "/index",
    status: true,
    data: "2023-04-03 12:03:34",
  },
  {
    id: 2,
    name: "文档管理",
    icon: "Management",
    sort: 2,
    type: 2,
    componentPath: "filemanage/",
    routePath: "",
    status: true,
    data: "2023-04-03 12:03:34",
    children: [
      {
        id: 3,
        name: "文档库",
        icon: "Folder",
        sort: 3,
        type: 1,
        componentPath: "filemanage/Document",
        routePath: "/document",
        status: true,
        data: "2023-04-03 12:03:34",
      },
      {
        id: 4,
        name: "智能搜索",
        icon: "Search",
        sort: 3,
        type: 1,
        componentPath: "",
        routePath: "/search",
        status: true,
        data: "2023-04-03 12:03:34",
      },
    ],
  },
  {
    id: 5,
    name: "权限管理",
    icon: "Tools",
    sort: 4,
    type: 2,
    routePath: "",
    status: true,
    data: "2023-04-03 12:03:34",
    children: [
      {
        id: 6,
        name: "用户管理",
        icon: "User",
        sort: 5,
        type: 1,
        routePath: "/user",
        status: true,
        data: "2023-04-03 12:03:34",
      },
      {
        id: 7,
        name: "角色管理",
        icon: "UserFilled",
        sort: 6,
        type: 1,
        routePath: "/role",
        status: false,
        data: "2023-04-03 12:03:34",
      },
    ],
  },
];

const menuDialog = reactive({
  visible: false,
});

/**
 * 新增
 */
const onAdd = () => {
  menuDialog.visible = true;
};
</script>

<style scoped lang="less">
.btn-style {
  min-width: 90px;
}

.icon {
  width: 18px;
  height: 18px;
  position: relative;
  top: 2px;
}
</style>
