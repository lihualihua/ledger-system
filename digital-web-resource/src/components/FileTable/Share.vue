<template>
  <div>
    <el-dialog
      draggable
      v-model="props.shareObj.visible"
      title="分享"
      width="700"
      :close-on-click-modal="false"
      :destroy-on-close="true"
      :before-close="handleClose"
    >
      <div class="file-dialog">
        <el-form
          ref="formRef"
          :model="shareForm"
          label-width="180px"
          class="upload-form"
        >
          <el-form-item label="分享文件名：">
            <div class="file-title">
              <div class="file-info" :title="title">
                <span class="file-name"> {{ titleName }} </span>
                <span class="file-svg" v-if="size > 1">
                  等{{ size }}个文件
                </span>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="分享时间：">
            <el-select
              v-model="shareForm.validFlag"
              @change="chaangeDate"
              style="width: 300px"
            >
              <el-option
                v-for="item in shareType"
                :key="item.id"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <div class="share-custom" v-show="custom">
            <el-date-picker
              v-model="shareForm.validDate"
              type="date"
              placeholder="选择有效期日期"
              :disabled-date="disabledDate"
              style="width: 300px"
              @change="onDatepicker"
            />
          </div>

          <el-form-item label="选择分享用户：">
            <el-input
              v-model="userName"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 8 }"
              style="width: 300px; margin-right: 10px"
              disabled="disabled"
            />
            <el-button type="primary" @click="onShareGoals"> 选择 </el-button>
          </el-form-item>

          <el-form-item label="分享描述：">
            <el-input
              v-model="shareForm.desc"
              style="width: 370px"
              :rows="3"
              type="textarea"
              placeholder="输入分享描述..."
            />
          </el-form-item>

          <el-form-item label="分享链接：">
            <el-input
              v-model="shareForm.shareUrl"
              style="width: 300px; margin-right: 10px"
              :disabled="true"
            />
            <el-button type="primary" :disabled="isCopy" @click="onCopy">
              复制
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button class="btn-style" type="primary" @click="createLink">
            创建链接
          </el-button>
          <el-button class="btn-style" @click="handleClose"> 关闭 </el-button>
        </div>
      </template>
    </el-dialog>
  </div>

  <DepartmentAndUsers :shareGoals="shareGoals" @share-event="shareEvent" />
</template>

<script setup lang="ts">
import { onBeforeUpdate, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { shareType } from "@/common/data";
import { copyLink, formatDate } from "@/utils/other";
import { sharePar } from "@/interface/share";
import { $shareFile } from "@/api/document";
import { Prompt, Returndata } from "@/common/enum";
import DepartmentAndUsers from "./DepartmentAndUsers.vue";
import { UserList } from "@/interface/common";
import eventBus from "@/common/eventBus";

const props = defineProps({
  shareObj: {
    type: Object,
    default: {},
  },
});

/**
 * 分享个人或部门
 * @visible 模态框隐藏/显示
 */
const shareGoals = reactive({
  visible: false,
});

// 分享文件名称
const title = ref<string>("");
const titleName = ref<string>("");
// 分享 勾选文件数量
const size = ref<number>(0);
// 分享 自定义时间 显示/隐藏
const custom = ref<boolean>(false);
// 分享 复制按钮 可用/禁用
const isCopy = ref<boolean>(true);
// 选择分享用户
const userName = ref<string>("");
/**
 * 保存分享文件参数
 */
const fileData = {
  name: "",
  fileIds: [],
  validFlag: "4",
  validDate: "",
  desc: "",
  shareUrl: "",
  shareUserIds: [],
  shareDeptIds: [],
};

/**
 * 保存分享文件参数
 * @fileId 分享文件ID
 * @validFlag 有效期标识，0：自定义日期，1：永久有效，2：一年，3：六个月，4：一个月
 * @validDate 有效期日期，如果有效期标识字段为0，标识自定义，需要传 validDate
 * @desc 描述
 * @shareUrl 分享url
 * @shareUserIds 分享的用户ID
 * @shareDeptIds 分享的部门ID
 */
const shareForm = reactive<sharePar>({ ...fileData });

onBeforeUpdate(() => {
  shareForm.fileIds = []; // 清空分享文件ID

  size.value = props.shareObj.fileList.length;
  if (props.shareObj.fileList.length > 0) {
    const name = props.shareObj.fileList[0].fileName;

    title.value = size.value > 1 ? name + ` 等${size.value}个文件` : name;
    titleName.value = name;

    // 获取分享入参
    const file = props.shareObj.fileList;
    shareForm.name = title.value;
    // 格式化 文件fileId
    file.forEach((item: { fileId: never }) => {
      return shareForm.fileIds!.push(item.fileId);
    });
  }
});

// 分享 自定义日期
const chaangeDate = (value: string) => {
  value === "5" ? (custom.value = true) : (custom.value = false);
};
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now();
};
// 分享 格式化日期
const onDatepicker = () => {
  shareForm.validDate = formatDate(shareForm.validDate);
};

/**
 * 分享 创建链接
 */
const createLink = async () => {
  try {
    // 调用创建分享链接接口
    const ret = await $shareFile(shareForm);
    if (ret.code === Returndata.code) {
      ElMessage.success(Prompt.FOUND_SHARE);
      shareForm.shareUrl = ret.data.shareUrl;
      if (shareForm.shareUrl) {
        isCopy.value = false;
      }
    }
  } catch (error) {
    console.error("捕获到错误: " + error);
  }
};

// 分享 复制
const onCopy = () => {
  copyLink(shareForm.shareUrl); // 复制公共方法
  // 弹出复制成功信息
  ElMessage({
    message: "复制成功，可进行分享链接",
    type: "success",
  });
};

// 分享 模态框 取消
const handleClose = () => {
  eventBus.emit("onShareEventBus");

  // 清空form表单
  Object.assign(shareForm, fileData);
  // 分享文件id数组
  fileData.fileIds.length = 0;
  custom.value = false; // 初始化选择日期
  userName.value = ""; // 选择分享用户

  props.shareObj.visible = false;
};

/**
 * 分享个人
 */
const onShareGoals = () => {
  shareGoals.visible = true;
};

/**
 * 接收子组件参数
 */
const shareEvent = (userIDArray: any[], userArray: UserList[]) => {
  shareForm.shareUserIds = userIDArray;
  const result = userArray.map((user: UserList) => user.displayName).join("，");
  userName.value = result;
};
</script>

<style scoped lang="less">
::v-deep(.el-dialog__footer) {
  text-align: center;
  padding-bottom: 16px;
}

.file-dialog {
  .file-title {
    width: 100%;
    height: 40px;
    .file-info {
      width: 82%;
      vertical-align: middle;
      font-size: 16px;
      font-weight: 500;
      color: rgba(0, 0, 0, 0.88);
      .file-name {
        max-width: 67%;
        float: left;
        display: block;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      .file-svg {
        display: block;
        float: left;
        margin-left: 10px;
      }
    }
  }

  .share-custom {
    margin-bottom: 18px;
    margin-left: 180px;
  }
}
.dialog-footer {
  .btn-style {
    min-width: 90px;
  }
}
</style>
