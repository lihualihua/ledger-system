<template>
  <el-dialog
    draggable
    v-model="props.addtag.visible"
    title="添加标签"
    width="700"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    :before-close="onCancel"
  >
    <div class="tag-content">
      <div class="tag-title">
        标签栏
        <span class="tag-select">
          （选择 {{ createFileIds.length }} 标签）
        </span>
      </div>
      <div class="tag-tips" v-show="tagData.length == 0">
        未查到相关标签，请先创建标签
      </div>

      <div class="tag-bar-div">
        <ul class="tag-bar-ul">
          <li
            v-for="item in tagData"
            :key="item.id"
            :class="['tag-bar-li', item.checked ? 'word' : '']"
            @click="onSelectTag(item.id!, item.checked)"
          >
            <span v-show="!item.checked">
              <SvgIcon
                name="icon-tag-gray-24"
                width="18px"
                height="18px"
                class="icon-img"
              ></SvgIcon>
            </span>
            <span v-show="item.checked">
              <SvgIcon
                name="icon-tag-24"
                width="18px"
                height="18px"
                class="icon-img"
              ></SvgIcon>
            </span>
            <span class="tag-name">{{ item.name }}</span>
          </li>
        </ul>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-style" @click="onCancel">关闭</el-button>
        <el-button class="btn-style" type="primary" @click="onOK">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { $addAndDeleteTag, $findTagByFileId, $queryTag } from "@/api/addtag";
import eventBus from "@/common/eventBus";
import SvgIcon from "@/components/SvgIcon/index.vue";
import { TagList } from "@/interface/addtag";
import { FeaturePar } from "@/interface/document";
import { ElMessage } from "element-plus";
import { ref, toRaw, watch } from "vue";
const props = defineProps({
  addtag: {
    type: Object,
    default: {},
  },
});

// 个人标签列表
const tagData = ref<TagList[]>([]);
// 勾选标签入参
const createFileIds = ref<string[]>([]);

// 监听 visible 的变化，当模态框打开时调用 初始化 方法
watch(
  () => props.addtag.visible,
  (newVal) => {
    if (newVal) {
      queryList();
    }
  },
  { immediate: true }
);

/**
 * 选择标签
 */
const onSelectTag = (id: string, bool?: boolean) => {
  if (!bool) {
    createFileIds.value.push(id);
    tagData.value.forEach((res: TagList) => {
      if (id === res.id) {
        res.checked = true;
      }
    });
  } else {
    // 如果复选框被取消勾选，从 selectedIds 中移除 ID
    createFileIds.value = createFileIds.value.filter((item) => item !== id);
    tagData.value.forEach((res: TagList) => {
      if (id === res.id) {
        res.checked = false;
      }
    });
  }
};

/**
 * 查询标签列表
 */
const queryList = () => {
  $queryTag()
    .then((res: any) => {
      res.data.forEach((item: any) => {
        item.checked = false;
      });
      tagData.value = res.data;
      queryAddLabe();
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {
      // 不管成功还是失败，都会执行这里的代码
    });
};

/**
 * 回写已添加标签
 */
const queryAddLabe = () => {
  const list = props.addtag.fileList;
  if (list.length === 1) {
    list.forEach((res: FeaturePar) => {
      const fileId = res.fileId;
      $findTagByFileId(fileId)
        .then((res: any) => {
          if (res) {
            const list = res.data;
            for (let i = 0; i < tagData.value.length; i++) {
              const id = tagData.value[i].id;
              for (let j = 0; j < list.length; j++) {
                const ids = list[j].id;
                if (id === ids) {
                  tagData.value[i].checked = true;
                  createFileIds.value.push(ids);
                }
              }
            }
          }
        })
        .catch((err) => {
          console.log("处理错误信息：", err);
        })
        .finally(() => {
          // 不管成功还是失败，都会执行这里的代码
        });
    });
  }
};

/**
 * 模态框 确定
 * 文件添加
 */
const onOK = () => {
  const list = props.addtag.fileList;
  let fileArray: string[] = [];
  list.forEach((res: FeaturePar) => {
    fileArray.push(res.fileId!);
  });

  const params = {
    tagIds: toRaw(createFileIds.value),
    fileIds: fileArray,
  };
  $addAndDeleteTag(params)
    .then((res: any) => {
      if (res) {
        ElMessage({
          message: "标签操作成功！",
          type: "success",
        });
        // 个人标签列表进入
        if (props.addtag.tagStatus === "list") {
          eventBus.emit("tagEventBus", fileArray);
        }
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {
      // 不管成功还是失败，都会执行这里的代码
      onCancel();
    });
};

/**
 * 取消按钮
 * @visible 模态框关闭
 */
const onCancel = () => {
  props.addtag.visible = false;
};
</script>

<style scoped lang="less">
.tag-content {
  width: auto;
  padding: 0px 10px 20px 10px;

  .tag-title {
    height: 22px;
    font-size: 16px;
    color: #939090;
    .tag-select {
      font-size: 12px;
    }
  }

  .tag-tips {
    height: 176px;
    line-height: 176px;
    font-size: 18px;
    color: #939090;
    text-align: center;
  }

  .tag-bar-div {
    width: 100%;
    height: 210px;
    overflow: auto;

    .tag-bar-ul {
      width: 100%;
      margin: auto;
      padding: 0;

      .tag-bar-li {
        min-width: 10%;
        height: 34px;
        line-height: 33px;
        font-size: 12px;
        float: left;
        cursor: pointer;
        margin: 16px 8px 0 8px;
        padding: 0 8px 0 8px;
        border-radius: 4px;
        border: 1px solid #dcdfe6;
      }
      .tag-bar-li:hover {
        color: #409eff;
        font-style: normal;
        border: 1px solid #d9ecff;
        background-color: #ecf5ff;
      }

      .icon-img {
        position: relative;
        top: 4px;
      }
      .tag-name {
        padding: 0 6px 0 6px;
      }
      .word {
        color: #409eff;
        border: 1px solid #d9ecff;
        background-color: #ecf5ff;
      }
    }
  }
}

.tag-bar-div::-webkit-scrollbar {
  width: 4px;
  height: 10px;
  overflow: visible;
}
.tag-bar-div::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
.tag-bar-div::-webkit-scrollbar-thumb {
  border: solid transparent;
  border-width: 1px 0;
  background-clip: padding-box;
  background: #d8dce5;
}

.dialog-footer {
  .btn-style {
    min-width: 90px;
  }
}
</style>
