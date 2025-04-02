<template>
  <el-dialog
    draggable
    v-model="props.detailsObj.visible"
    title="详细信息"
    width="480"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    :before-close="handleClose"
  >
    <div class="details-container">
      <div class="details-title">
        <i :class="['icon', messageValue.icon]"></i>
        <p class="file-name" :title="messageValue.fileName">
          {{ messageValue.fileName }}
        </p>
      </div>
      <div class="details-content">
        <p class="details-content-para">
          <span class="file-label">文件类型</span>
          <span class="file-value" v-if="messageValue.fileType == null">
            文件目录
          </span>
          <span class="file-value">{{ messageValue.fileType }}</span>
        </p>
        <p class="details-content-para">
          <span class="file-label">文件大小</span>
          <span class="file-value">{{ messageValue.fileSizeSumStr }}</span>
        </p>

        <p class="details-content-para">
          <span class="file-label">包含</span>
          <span class="file-value">
            {{ messageValue.fileCount }}个文件，
            {{ messageValue.folderCount }}个文件夹
          </span>
        </p>
        <p class="details-content-para">
          <span class="file-label">修改时间</span>
          <span class="file-value">
            {{ formatDate(messageValue.createTime) }}
          </span>
        </p>
        <p class="details-content-para">
          <span class="file-label">文件路径</span>
          <span class="file-value">
            {{ messageValue.location }}
          </span>
        </p>
        <p class="details-content-para" v-if="messageValue.downloadUrl != null">
          <span class="file-label">文件地址复制</span>
          <span class="file-value">
            <el-button
              type="primary"
              text
              @click="onCopy(messageValue.downloadUrl)"
            >
              复制
            </el-button>
          </span>
        </p>
        <p class="details-content-para">
          <span class="file-label">标签名称</span>
          <span class="file-value" v-if="tags.length > 0">
            <el-tag
              type="primary"
              style="margin-right: 10px"
              v-for="(item, index) in tags"
              :key="index"
            >
              {{ item.name }}
            </el-tag>
          </span>
          <span class="file-value" v-else> -- </span>
        </p>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from "vue";
import { $fileInfo } from "@/api/document";
import { copyLink, formatDate } from "@/utils/other";
import { ElMessage } from "element-plus";

const props = defineProps({
  detailsObj: {
    type: Object,
    default: {},
  },
});

/**
 * @icon 文件图标
 * @fileName 文件名称
 * @createTime 创建时间
 * @fileSizeSum 文件大小
 * @fileSizeSumStr 文件大小（格式化后）
 * @fileType 文件类型
 * @folderCount 文件夹个数
 * @fileCount 文件个数
 * @folderFlag
 * @location 文件路径
 * @downloadUrl 复制地址
 */
const messageValue = reactive({
  icon: "",
  fileName: "",
  fileType: "",
  fileSizeSumStr: "",
  folderCount: 0,
  fileCount: 0,
  createTime: "",
  location: "",
  downloadUrl: "",
});

/**
 * @tags  关联标签
 */
const tags = ref<{ id: string; name: string }[]>([]);

// 监听 visible 的变化，当模态框打开时调用 初始化 方法
watch(
  () => props.detailsObj.visible,
  (newVal) => {
    if (newVal) {
      queryMessage();
    }
  },
  { immediate: true }
);

const queryMessage = () => {
  const fileId = props.detailsObj.fileId;
  $fileInfo(fileId)
    .then((res: any) => {
      messageValue.icon = res.data.icon;
      messageValue.fileName = res.data.fileName;
      messageValue.fileType = res.data.fileType;
      messageValue.fileSizeSumStr = res.data.fileSizeSumStr;
      messageValue.folderCount = res.data.folderCount;
      messageValue.fileCount = res.data.fileCount;
      messageValue.createTime = res.data.createTime;
      messageValue.location = res.data.location;
      messageValue.downloadUrl = res.data.downloadUrl;
      tags.value = res.data.tags;
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

/**
 * 复制
 */
const onCopy = (url: string) => {
  copyLink(url); // 复制公共方法
  ElMessage({
    message: "复制成功",
    type: "success",
  });
};

/**
 * 模态框 取消
 */
const handleClose = () => {
  props.detailsObj.visible = false;
  messageValue.icon = "";
  messageValue.fileName = "";
  messageValue.fileType = "";
  messageValue.fileSizeSumStr = "";
  messageValue.folderCount = 0;
  messageValue.fileCount = 0;
  messageValue.createTime = "";
  messageValue.location = "";
  messageValue.downloadUrl = "";
};
</script>

<style scoped lang="less">
.details-container {
  width: 100%;
  margin-bottom: 20px;
  padding: 0 50px;
  .details-title {
    width: 100%;
    height: 50px;
    display: flex;
    margin-bottom: 10px;
    .icon {
      flex: 0 0 50px;
    }
    .file-name {
      line-height: 48px;
      -webkit-box-flex: 1;
      -webkit-flex: 1 1 auto;
      flex: 1 1 auto;
      color: #020202;
      word-break: break-all;
      padding-left: 44px;
      font-size: 16px;
      font-weight: 600;

      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
  }

  .details-content {
    width: 100%;
    .details-content-para {
      width: 100%;
      font-size: 14px;
      line-height: 28px;
      display: flex;
      margin-top: 6px;

      .file-label {
        -webkit-box-flex: 0;
        -webkit-flex: 0 0 62px;
        flex: 0 0 86px;
        color: #555;
        text-align: left;
        margin-right: 30px;
      }
      .file-value {
        -webkit-box-flex: 1;
        -webkit-flex: 1 1 auto;
        flex: 1 1 auto;
        color: #020202;
        word-break: break-all;
      }
    }
  }
}
</style>
