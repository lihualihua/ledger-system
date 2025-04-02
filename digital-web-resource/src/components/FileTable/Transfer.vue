<template>
  <el-dialog
    draggable
    v-model="props.shiftObj.visible"
    title="转移到"
    width="480"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    :before-close="handleClose"
  >
    <div class="transfer-tips" v-show="active">
      <el-icon color="#F56C6C" size="14px" class="transfer-icon">
        <WarningFilled />
      </el-icon>
      不能将文件移动到自身或其子文件夹下
    </div>

    <div class="tree-container">
      <el-tree
        ref="treeRef"
        :data="treeData"
        node-key="id"
        :props="defaultProps"
        :load="loadNode"
        lazy
        :default-expanded-keys="['all']"
        :highlight-current="true"
        @node-click="handleClick"
        style="max-width: 600px"
      >
        <template #default="{ data }">
          <i :class="['icon', 'icon-file-fill-24']"></i>
          <span>{{ data.label }}</span>
        </template>
      </el-tree>
    </div>

    <div class="tree-btn">
      <div class="btn-content">
        <el-button class="btn-style" @click="handleClose"> 取消 </el-button>
        <el-button
          class="btn-style"
          type="primary"
          :disabled="active"
          @click="onOK"
        >
          确定
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import type Node from "element-plus/es/components/tree/src/model/node";
import { Tree } from "@/interface/Transfer";
import { $listData } from "@/api/document";
import { TableData } from "@/interface/document";
import { $transferPost } from "@/api/transfer";
import { ElTree } from "element-plus";
import { WarningFilled } from "@element-plus/icons-vue";

const props = defineProps({
  shiftObj: {
    type: Object,
    default: {},
  },
});

const treeData = ref([
  {
    id: "all",
    label: "全部",
    loaded: false,
    children: [],
  },
]);

const defaultProps = {
  children: "children",
  label: "label",
  id: "id",
};
// 引用 el-tree 组件实例
const treeRef = ref<InstanceType<typeof ElTree> | null>(null);
// 文件夹id
const parentId = ref<string>("");
// 提示语
const active = ref<boolean>(false);

/**
 * 监听 visible 的变化，当模态框打开时调用 初始化 方法
 */
watch(
  () => props.shiftObj.visible,
  (newVal) => {
    if (newVal) {
      initTree();
    }
  },
  { immediate: true }
);

/**
 * 查询文件夹下子节点
 */
const loadNode = (node: Node, resolve: (data: Tree[]) => void) => {
  // 关闭模态框不执行后面操作
  if (!props.shiftObj.visible) {
    return;
  }

  const list = props.shiftObj.fileList;
  // 文件（夹）移动到是否是自身或其子文件夹下
  for (let item of list) {
    if (item.fileId === node.data.id) {
      active.value = true;
      return resolve(node.data.children);
    }
  }

  if (node.level === 0) {
    return resolve(treeData.value);
  }
  if (node.data.loaded) {
    // 如果节点已经加载过子节点，则直接返回
    return resolve(node.data.children || []);
  }

  let fileId = "";
  if (node.data.id === "all") {
    fileId = props.shiftObj.parentId;
  } else {
    fileId = node.data.id;
  }

  const url = `/api/doc/query/childFiles/1/100`;
  const obj = {
    parentId: fileId,
    ownership: props.shiftObj.ownership,
    category: "folder",
    delFlag: "1",
    folderFlag: "1",
  };
  // 查询列表接口
  $listData(url, obj)
    .then((res: any) => {
      const data = res.data.records;
      const childNodes = data.map((item: TableData) => ({
        id: item.fileId, // 假设每个子节点有一个唯一的ID
        label: item.fileName, // 假设返回的数据包含一个name字段作为标签
        children: [], // 子节点同样初始化children为空数组
        loaded: false,
      }));
      // 更新节点数据
      node.data.children = childNodes;
      node.data.loaded = true;
      if (node.data.id === "all") {
        resolve(childNodes);
        return;
      }
      // 解析并显示子节点
      setTimeout(() => {
        resolve(childNodes);
      }, 500);
    })
    .catch((err) => {
      // 处理错误
      console.log("处理错误信息：", err);
      resolve([]); // 如果有错误发生，解析为空数组
    })
    .finally(() => {
      // 加载完成后无论成功与否都设置loading状态为false
      active.value = false;
      node.loaded = false;
    });
};

/**
 * 当节点被点击的时候触发
 */
const handleClick = (data: Tree) => {
  // 文件（夹）移动到是否是自身或其子文件夹下
  const list = props.shiftObj.fileList;
  for (let item of list) {
    if (item.fileId === data.id) {
      active.value = true;
      return;
    }
  }

  if (data.id === "all") {
    parentId.value = "0";
    return;
  }
  parentId.value = data.id!;
  active.value = false;
};

/**
 * 模态框 取消
 */
const onOK = () => {
  const list = props.shiftObj.fileList;
  $transferPost(parentId.value, list);
  props.shiftObj.visible = false;
};

/**
 * 模态框 取消
 */
const handleClose = () => {
  props.shiftObj.visible = false;
  initTree();
};

/**
 * 清空&初始化Tree组件数据
 */
const initTree = () => {
  // 清空并重置树结构
  if (treeRef.value) {
    treeRef.value.setCheckedKeys([]);
  }
  treeData.value = [
    {
      id: "all",
      label: "全部",
      loaded: false,
      children: [],
    },
  ];
};
</script>

<style scoped lang="less">
::v-deep(.el-tree-node__content) {
  height: 36px;
}
// 滚动条
::-webkit-scrollbar {
  width: 4px;
  height: 10px;
  overflow: visible;
}
::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
::-webkit-scrollbar-thumb {
  border: solid transparent;
  border-width: 1px 0;
  background-clip: padding-box;
  background: #d8dce5;
}

.transfer-tips {
  width: auto;
  height: 32px;
  position: absolute;
  top: 55px;
  left: 50%;
  z-index: 1;
  transform: translateX(-50%);
  text-wrap: nowrap;
  padding: 7px 10px 5px;
  font-size: 12px;
  color: #000;
  background-color: #fff8db;
  border: 1px solid #f3e5b1;
  bottom: 0;
  .transfer-icon {
    position: relative;
    top: 2px;
  }
}

.tree-container {
  height: 324px;
  overflow: auto;
  .icon {
    width: 22px;
    height: 22px;
    cursor: pointer;
    margin-right: 10px;
  }
}

.tree-btn {
  height: 58px;
  padding-top: 26px;
  box-sizing: border-box;
  box-shadow: 0 -12px 10px -10px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(24px);
  .btn-content {
    float: right;
    .btn-style {
      min-width: 90px;
    }
  }
}
</style>
