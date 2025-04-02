<template>
  <div class="main-content">
    <div>权限管理</div>
    <el-divider style="margin-bottom: 0px"></el-divider>
    <div>
      <el-row>
        <el-col :span="3">
          <el-button @click="onAdd" style="width: 190px; margin-top: 10px">
            创建权限
          </el-button>
          <div class="custom-tree">
            <ul>
              <li v-for="item in list" :key="item.id">
                <a
                  :class="cut === item.name ? 'cut' : 'cut_hover'"
                  @click="onTree(item.name)"
                >
                  <span>{{ item.label }}</span>
                </a>
              </li>
            </ul>
          </div>
        </el-col>
        <el-col :span="21">
          <div class="dociment-content">
            <!-- 权限名称 操作权限 -->
            <div class="perm-all">
              <el-checkbox v-model="checked1" size="large" label="全部" />
              <div class="perm-operate">
                <a class="tag-manage" @click="onShortcut">编辑权限</a>
                <el-button type="primary" class="btn-style">保存</el-button>
              </div>
            </div>

            <div class="perm-title">
              <el-checkbox v-model="checked1" size="large" label="工作台" />
            </div>
            <ul class="perm-ul">
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="新增" />
              </li>
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="编辑" />
              </li>
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="删除" />
              </li>
            </ul>

            <div class="perm-title">
              <el-checkbox v-model="checked1" size="large" label="公共文档" />
            </div>
            <ul class="perm-ul">
              <li class="perm-li">
                <el-checkbox
                  v-model="checked1"
                  size="large"
                  label="创建文件夹"
                />
              </li>
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="上传文件" />
              </li>
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="下载" />
              </li>
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="分享" />
              </li>
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="删除" />
              </li>
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="转移到" />
              </li>
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="标签" />
              </li>
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="收藏" />
              </li>
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="取消收藏" />
              </li>
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="重命名" />
              </li>
            </ul>

            <div class="perm-title">
              <el-checkbox v-model="checked1" size="large" label="我的文档" />
            </div>
            <ul class="perm-ul">
              <li class="perm-li">
                <el-checkbox
                  v-model="checked1"
                  size="large"
                  label="创建文件夹"
                />
              </li>
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="上传文件" />
              </li>
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="下载" />
              </li>
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="分享" />
              </li>
              <li class="perm-li">
                <el-checkbox v-model="checked1" size="large" label="删除" />
              </li>
            </ul>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
  <PermissionDialogComponents :permDialog="permDialog" />
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import PermissionDialogComponents from "./components/PermissionDialog.vue";

const cut = ref<string>("minefile");
const active = ref<string>("all");

// 多选
const checked1 = ref<boolean>(false);

const list = reactive([
  {
    id: 1,
    label: "超级管理员",
    name: "common",
  },
  {
    id: 2,
    label: "部门管理员",
    name: "minefile",
  },
  {
    id: 3,
    label: "普通用户",
    name: "section",
  },
]);

const permDialog = reactive({
  visible: false,
});

const onTree = (name: string) => {
  cut.value = name;
  active.value = "all";
};

/**
 * 创建权限
 */
const onAdd = () => {
  permDialog.visible = true;
};

/**
 * 编辑权限
 */
const onShortcut = () => {};
</script>

<style scoped lang="less">
.custom-tree {
  font-size: 14px;
  margin-top: 14px;
  a {
    height: 36px;
    display: block;
    line-height: 36px;
    color: #555555;
    margin: 0 16px 12px 2px;
    padding: 0 0 0 34px;
    cursor: pointer;
  }

  .cut {
    height: 36px;
    line-height: 36px;
    background: #ecf5ff;
    color: #409eff;
    margin: 0 16px 12px 2px;
    border-radius: 4px;
  }
  .cut_hover:hover {
    height: 36px;
    line-height: 36px;
    color: #409eff;
    background: #ecf5ff;
    margin: 0 16px 12px 2px;
  }
  .custom-line {
    background: #e5e5e5;
    background-color: rgba(0, 0, 0, 0.04);
    height: 1px;
    margin: 20px;
  }
}

.dociment-content {
  min-height: calc(100vh - 190px);
  border-left: 1px solid #e5e9f2;
  padding: 20px 20px 0 20px;

  .perm-all {
    margin-bottom: 6px;

    ::v-deep(.el-checkbox__label) {
      font-size: 16px;
      font-weight: 600;
    }

    .perm-operate {
      display: inline;
      position: relative;
      top: -3px;
      float: right;

      .tag-manage {
        position: relative;
        right: 20px;
        top: 3px;
        color: rgba(0, 0, 0, 0.48);
        font-size: 14px;
        cursor: pointer;
        text-decoration: none; /* 默认情况下不显示下划线 */
      }
      .tag-manage:hover {
        text-decoration: underline; /* 鼠标悬停时显示下划线 */
      }
    }
  }

  .perm-title {
    height: 34px;
    ::v-deep(.el-checkbox__label) {
      font-weight: 600;
    }
  }

  .perm-ul {
    margin-bottom: 6px;
    .perm-li {
      width: 140px;
      display: inline-flex;
    }
  }
}

.btn-style {
  min-width: 90px;
}
</style>
