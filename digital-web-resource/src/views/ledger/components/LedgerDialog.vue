<template>
  <el-dialog
    draggable
    width="570"
    title="导入台账"
    v-model="props.importObj.visible"
    :close-on-click-modal="false"
    :destroy-on-close="true"
  >
    <el-form ref="ruleFormRef" label-width="140">
      <el-form-item label="上传台账模版：">
        <el-upload
          ref="upload"
          class="upload-demo"
          action
          :limit="1"
          accept=".xlsx,.xls"
          :show-file-list="true"
          :auto-upload="false"
          :on-exceed="handleExceed"
          :on-change="onUpload"
          :on-remove="handleRemove"
        >
          <div class="el-upload__text">
            <el-button type="primary" link>
              <el-icon :size="20" class="icon-upload"><UploadFilled /></el-icon>
              点击上传
            </el-button>
          </div>
        </el-upload>
      </el-form-item>

      <el-form-item label="模版样式：">
        <el-checkbox
          v-model="typeValue"
          label="我的模版"
          @change="changeCheckbox"
        />
        <div class="question-filled">
          <el-tooltip placement="right" effect="light">
            <template #content>
              注：上传我的模版；<br />
              1：选择对应上传模版，生成对应模版样式；<br />
              2：不选为系统默认模版样式；<br />
              3：选错则报错。
            </template>
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
      </el-form-item>

      <div class="content" v-show="active">
        <ul class="list">
          <li
            v-for="(item, index) in customTemplateTable"
            :key="index"
            :title="item.name"
            :class="['lt-li', templateID === item.id ? 'cut' : '']"
            @click="onTemplate(item.id)"
          >
            <span>模版 {{ index + 1 }}：</span>{{ item.name }}
          </li>
          <li class="no-data" v-if="customTemplateTable.length === 0">
            暂无数据
          </li>
        </ul>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-style" @click="onReset">取消</el-button>
        <el-button class="btn-style" type="primary" @click="onSubmit">
          导入
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import {
  ElMessage,
  genFileId,
  UploadInstance,
  UploadRawFile,
  UploadUserFile,
} from "element-plus";
import { Returndata } from "@/common/enum";
import { QuestionFilled, UploadFilled } from "@element-plus/icons-vue";
import { $findMyTemplates, $upload } from "@/api/ledger";
import { TemplateList } from "@/interface/ledger";

const props = defineProps({
  importObj: {
    type: Object,
    default: {},
  },
});

/**
 * 子组件传参给父组件
 */
const emit = defineEmits(["child-event"]);

const upload = ref<UploadInstance>();
// 用于存储上传台账模版文件
const fileList = ref<UploadUserFile[]>([]);

// 选择模版类型
const typeValue = ref<boolean>(false);
// 我的模版 加载myLoading
const myLoading = ref<boolean>(false);

// 我的模版选中/取消
const active = ref<boolean>(false);

// 模版ID选中
const templateID = ref<string>("");

// 我的模版
const customTemplateTable = ref<TemplateList[]>([]);

/**
 * 自定义模版 列表
 */
const customTemplateList = () => {
  myLoading.value = true;
  $findMyTemplates()
    .then((res: any) => {
      if (res.data && res.data.records.length > 0) {
        res.data.records.forEach((item: any) => {
          item.name = item.name.split(".")[0];
        });
        customTemplateTable.value = res.data.records;
        myLoading.value = false;
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {
      myLoading.value = false;
    });
};

/**
 * @上传台账模版
 * 当超出限制时，执行的钩子函数
 */
const handleExceed = (item: any) => {
  upload.value!.clearFiles();
  const file = item[0] as UploadRawFile;
  file.uid = genFileId();
  upload.value!.handleStart(file);
};

/**
 * @上传台账模版
 * 选择上传文件
 */
const onUpload = (item: any) => {
  fileList.value.length = 0; // 清空

  fileList.value.push(item);
};

/**
 * @上传台账模版
 * 删除
 */
const handleRemove = (_file: any, uploadFiles: any) => {
  fileList.value = uploadFiles;
};

/**
 * @模版样式
 * 勾选我的模版
 */
const changeCheckbox = (value: boolean) => {
  active.value = value;
  if (!value) {
    templateID.value = "";
  }
};

/**
 * @模版样式
 * 选择我的模版
 */
const onTemplate = (id: string) => {
  templateID.value = id;
};

/**
 * 模态框 确定
 */
const onSubmit = () => {
  if (fileList.value.length > 0) {
    fileList.value.forEach((file: any) => {
      const formData = new FormData();
      formData.append("file", file.raw);

      if (templateID.value === "") {
        templateID.value = "0";
      }

      $upload(formData, templateID.value)
        .then((res: any) => {
          if (res && res.code === Returndata.code) {
            ElMessage({
              message: "模版上传成功",
              type: "success",
            });

            // 调用查询列表方法
            emit("child-event");
          }
        })
        .catch((err) => {
          console.log("错误信息：", err);
        })
        .finally(() => {
          onReset();
        });
    });
  } else {
    ElMessage({
      message: "选择上传台账模版",
      type: "error",
    });
  }
};

/**
 * 模态框 取消
 */
const onReset = () => {
  typeValue.value = false;
  active.value = false;
  templateID.value = "";
  fileList.value.length = 0;
  customTemplateTable.value.length = 0;

  props.importObj.visible = false;
};

// 监听 visible 的变化，当模态框打开时调用 初始化 方法
watch(
  () => props.importObj.visible,
  (newVal) => {
    if (newVal) {
      customTemplateList();
    }
  },
  { immediate: true }
);
</script>

<style scoped lang="less">
.title-name {
  color: #303133;
  font-weight: 500;
}

.question-filled {
  position: relative;
  top: 2px;
  left: 10px;
  color: #939090;
  cursor: pointer;
}
.question-filled:hover {
  color: #606266;
}

.content {
  border: 1px dashed #d6d6d6;
  font-size: 12px;
  color: #606266;
  border-radius: 4px;
  padding: 10px 20px 10px 40px;
  margin: 0 10px 20px 10px;

  .list {
    overflow: hidden;

    .lt-li {
      width: 50%;
      height: 30px;
      line-height: 30px;
      padding-right: 22px;
      cursor: pointer;
      float: left;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
    .lt-li:hover {
      color: #409eff;
    }

    .cut {
      color: #409eff;
      font-weight: 600;
    }

    .no-data {
      color: #b8b9ba;
      text-align: center;
    }
  }
}

.upload-demo {
  width: 310px;
  .el-upload__text {
    color: #409eff;

    .icon-upload {
      position: relative;
      margin-right: 6px;
    }
  }
}

.dialog-footer {
  flex: auto;
  .btn-style {
    min-width: 90px;
  }
}
</style>
