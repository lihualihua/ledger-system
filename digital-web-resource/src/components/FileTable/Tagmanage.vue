<template>
  <el-dialog
    draggable
    v-model="props.tagManage.visible"
    title="管理个人标签"
    width="600"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    :before-close="onCancel"
  >
    <div class="tag-manage-tips" v-show="active">
      <el-icon color="#F56C6C" size="14px" class="transfer-icon">
        <WarningFilled />
      </el-icon>
      {{ titleName }}
    </div>
    <div class="tag-content">
      <span class="tag-hint">注：个人标签最多添加 100 个，管理员不限</span>
      <ul class="tag-ul">
        <div class="tag-overflow">
          <li class="tag-li" v-for="(item, index) in tagData" :key="item.id">
            <div class="tag-name">
              <el-input
                v-if="item.id === showEdit"
                size="small"
                v-model="tagData[index].name"
                placeholder="输入标签名称"
                maxlength="12"
                @blur="handleBlur(item.id, item.name, item.numID)"
                class="custom-input"
              ></el-input>
              <span v-else>
                <SvgIcon
                  name="icon-tag-24"
                  width="20px"
                  height="20px"
                  class="icon-img"
                ></SvgIcon>
                <span class="tag-title">{{ item.name }}</span>
              </span>
            </div>
            <div class="tag-operate">
              <span
                class="tag-edit edit"
                title="编辑"
                @click="onEdit(item.id, item.name)"
              >
                <el-icon><Edit /></el-icon>
              </span>
              <span
                class="tag-edit delete"
                title="删除"
                @click="onDelete(item.id, item.name)"
              >
                <el-icon><Delete /></el-icon>
              </span>
            </div>
          </li>
        </div>
        <li class="addtag-li">
          <span @click="onNewTag">
            <el-icon size="18px" class="addtag-icon">
              <CirclePlus />
            </el-icon>
            <span>新建标签</span>
          </span>
        </li>
      </ul>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-style" @click="onCancel">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { $addTag, $deleteTag, $queryTag } from "@/api/addtag";
import { TagList } from "@/interface/addtag";
import {
  CirclePlus,
  Delete,
  Edit,
  WarningFilled,
} from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import SvgIcon from "@/components/SvgIcon/index.vue";
import eventBus from "@/common/eventBus";

const props = defineProps({
  tagManage: {
    type: Object,
    default: {},
  },
});

// 编辑切换
const showEdit = ref<string>("");
// 标签名称
const tagName = ref<string>("");
// 个人标签列表
const tagData = ref<TagList[]>([]);
// 标签为空提示
const active = ref<boolean>(false);
// 标签提示语
const titleName = ref<string>("");
// 区分创建、编辑标识
const isTag = ref<string>("add");

// 监听 visible 的变化，当模态框打开时调用 初始化 方法
watch(
  () => props.tagManage.visible,
  (newVal) => {
    if (newVal) {
      queryList();
    }
  },
  { immediate: true }
);

/**
 * 编辑标签
 */
const onEdit = (id?: string, name?: string) => {
  showEdit.value = id!;
  tagName.value = name!;
  isTag.value = "edit";
};

/**
 * 编辑标签
 * blur失去焦点事件
 */
const handleBlur = (id?: string, name?: string, numid?: number) => {
  if (name === "") {
    titleName.value = "标签名称不能为空！";
    active.value = true;
    return;
  }

  // 编辑标签名称是否相同 则不调用方法
  for (let i = 0; i < tagData.value.length; i++) {
    const nameValue = tagData.value[i].name;
    const idValue = tagData.value[i].id;
    if (nameValue === name && idValue !== id) {
      const index = tagData.value.findIndex((item) => item.numID === numid);
      tagData.value[index].name = tagName.value;
      showEdit.value = "";
      ElMessage.error("该标签名称已存在，请勿重复添加！");
      return;
    }
  }

  active.value = false;

  // 满足条件调佣新建标签接口
  const obj = {
    id: isTag.value === "add" ? "" : id,
    name: name,
    desc: "",
    status: 1,
  };
  $addTag(obj, isTag.value)
    .then((res: any) => {
      for (const item of tagData.value) {
        if (item.name === name) {
          item.id = res.data.id;
          item.name = res.data.name;
          item.status = res.data.status;
          item.createTime = res.data.createTime;
          eventBus.emit("onTagEventBus");
        }
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {
      showEdit.value = "";
    });
};

/**
 * 删除标签
 */
const onDelete = (id?: string, name?: string) => {
  active.value = false;
  // 标签名称为空，假删除
  if (name === "") {
    const index = tagData.value.findIndex((item) => item.id === id);
    tagData.value.splice(index, 1);
    return;
  }
  ElMessageBox.confirm(`是否确定删除 ${name} 标签？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
    draggable: true,
  })
    .then(() => {
      $deleteTag(id).then((res: any) => {
        if (res) {
          ElMessage({
            message: `${name} 标签删除成功！`,
            type: "success",
          });
          eventBus.emit("onTagEventBus");
          queryList();
        }
      });
    })
    .catch(() => {
      console.log("取消删除");
    });
};

/**
 * 创建标签
 */
const onNewTag = () => {
  isTag.value = "add";
  let num = Math.floor(Math.random() * 9000) + 1000;
  const numID = num.toString();
  tagData.value.push({
    id: numID,
    name: "",
  });
  showEdit.value = numID;
};

/**
 * 查询标签列表
 */
const queryList = () => {
  $queryTag()
    .then((res: any) => {
      res.data.forEach((item: any) => {
        item.numID = Math.floor(Math.random() * 10000);
      });
      tagData.value = res.data;
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

/**
 * @取消按钮
 * @visible 模态框关闭
 */
const onCancel = () => {
  props.tagManage.visible = false;
  showEdit.value = "";
  active.value = false;
};
</script>

<style scoped lang="less">
// el-input
::v-deep(.custom-input .el-input__inner) {
  height: 32px;
  line-height: 21px;
  padding: 0;
  font-size: 14px;
  border: 1px solid #409eff;
}
::v-deep(.el-input--small .el-input__wrapper) {
  padding: 0;
}

// 滚动条
.tag-overflow::-webkit-scrollbar {
  width: 4px;
  height: 10px;
  overflow: visible;
}
.tag-overflow::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
.tag-overflow::-webkit-scrollbar-thumb {
  border: solid transparent;
  border-width: 1px 0;
  background-clip: padding-box;
  background: #d8dce5;
}

.tag-manage-tips {
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

.tag-content {
  padding: 0 30px;
  .tag-hint {
    font-size: 12px;
    display: block;
    color: #939090;
    height: 20px;
    line-height: 20px;
    margin-bottom: 14px;
  }
  .tag-ul {
    border: 1px solid #f7f5f5;
    color: #000;
    .tag-overflow {
      max-height: 360px;
      overflow: auto;
      .tag-li {
        height: 40px;
        line-height: 40px;
        padding-left: 30px;
        font-size: 14px;
        border-bottom: 1px solid #e5e5e5;
        .tag-name {
          width: 70%;
          float: left;
          .custom-input {
            width: 80%;
            position: relative;
            top: -2px;
          }
          .icon-img {
            position: relative;
            top: 5px;
          }
          .tag-title {
            padding-left: 8px;
          }
        }
        .tag-operate {
          width: 30%;
          float: right;
          text-align: center;
          .tag-edit {
            padding: 0 10px;
            cursor: pointer;
          }
          .edit:hover {
            color: #409eff;
          }
          .delete:hover {
            color: #f56c6c;
          }
        }
      }
      .tag-li:hover {
        background-color: #ecf5ff;
      }
    }

    .addtag-li {
      width: 100%;
      height: 40px;
      line-height: 40px;
      text-align: left;
      cursor: pointer;
      color: #409eff;
      padding-left: 30px;
      border-bottom: 1px solid #f7f5f5;
      .addtag-icon {
        position: relative;
        top: 4px;
        margin-right: 8px;
      }
    }
    .addtag-li:hover {
      color: #2085ee;
    }
  }
}

.dialog-footer {
  .btn-style {
    min-width: 90px;
  }
}
</style>
