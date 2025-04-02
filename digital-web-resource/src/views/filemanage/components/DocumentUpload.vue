<template>
  <div class="light-but">
    <el-button type="primary" color="#dbdbdb" @click="uploadDoc(Stauts.ADD)">
      <SvgIcon name="add-s" class="cas-upload"></SvgIcon>
      文件夹
    </el-button>

    <el-button type="primary" @click="onOff">
      <SvgIcon name="upload-s" class="cas-upload"></SvgIcon>
      上传
    </el-button>
    <div class="controls-menu" v-show="props.uploadpull.moreupload">
      <ul class="controls-ul">
        <li>
          <span @click="uploadDoc(Stauts.DOC)">
            <SvgIcon
              name="icon-doc-s"
              width="22px"
              height="22px"
              class="svg-icon"
            ></SvgIcon>
            文件
          </span>
        </li>
        <li style="display: none">
          <span @click="uploadDoc(Stauts.FILE)">
            <SvgIcon
              name="icon-file-s"
              width="22px"
              height="22px"
              class="svg-icon"
            ></SvgIcon>
            文件夹
          </span>
        </li>
      </ul>
    </div>
  </div>

  <!-- 新增文件夹、上传文件、上传文件夹 -->
  <el-dialog
    draggable
    width="570"
    :title="title"
    v-model="docVisible"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    :before-close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="uploadForm"
      :rules="rules"
      label-width="140px"
      class="upload-form"
    >
      <el-form-item label="当前位置：">
        <span v-if="uploadForm.ownership == 1">公共文档</span>
        <span v-if="uploadForm.ownership == 2">我的文件</span>
        <span v-if="uploadForm.ownership == 3">部门文档</span>
      </el-form-item>

      <el-form-item v-if="toggle !== Stauts.ADD" label="上传文件：">
        <!-- 上传文件 -->
        <div v-if="toggle === Stauts.DOC">
          <el-upload
            class="upload-demo"
            v-loading="formLoading"
            :auto-upload="false"
            :limit="10"
            action="#"
            :multiple="true"
            :show-file-list="true"
            :on-change="uploadSuccess"
            :on-remove="handleRemove"
            drag
          >
            <el-icon class="icon-upload"><UploadFilled /></el-icon>
            <div class="el-upload__text" style="height: 56px">
              将文件拖到此处，或 <em>点击上传</em>
              <span style="color: red; font-size: 12px">
                最大文件500MB，超出请联系管理员！
              </span>
            </div>
          </el-upload>
        </div>

        <!-- 上传 文件夹 -->
        <div v-if="toggle === Stauts.FILE">
          <input
            type="file"
            webkitdirectory
            directory
            multiple
            @change="handleFile"
          />
        </div>
      </el-form-item>

      <!-- 创建文件夹 -->
      <el-form-item v-else label="文件夹名称：" prop="fileName">
        <el-input
          v-model="uploadForm.fileName"
          placeholder="输入文件夹名称"
          id="fileName"
          maxlength="60"
          style="width: 300px"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-style" @click="onCancel(formRef)">
          取消
        </el-button>
        <el-button
          class="btn-style"
          type="primary"
          :disabled="!butVisible"
          @click="uploadSure(formRef)"
        >
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import axios from "axios";
import {
  ElMessage,
  FormInstance,
  FormRules,
  MessageHandler,
  UploadRawFile,
  UploadUserFile,
} from "element-plus";
import { docIcon } from "@/common/data";
import eventBus from "@/common/eventBus";
import { useUserStore } from "@/stort/user";
import { Prompt, Stauts } from "@/common/enum";
import { $addFile, $uploadFile } from "@/api/document";
import SvgIcon from "@/components/SvgIcon/index.vue";
import { UploadFilled } from "@element-plus/icons-vue";
import { FileChunk, UploadFilePar } from "@/interface/document";
import { UploadBig50MB, UploadEachPiece, UploadTotal } from "@/utils/other";

// 获取登录用户信息 token
const userStore = useUserStore();
const token = userStore.user.token;

const { VITE_FRAGMENT_DOWNLOAD_UPLOAD } = import.meta.env;

const props = defineProps({
  // 件夹id，根目录为 0
  fileID: {
    type: String,
    default: "0",
  },
  // 归属，1：公共，2：用户，3：部门
  ownership: {
    type: Number,
    default: 2,
  },
  /**
   * 上传下拉隐藏显示
   * uploadpull.moreupload
   */
  uploadpull: {
    type: Object,
    default: {},
  },
  // 部门ID
  deptID: {
    type: String,
    default: "",
  },
});

// 上传 文件&文件夹 隐藏\显示
const toggle = ref<string>("");
// 模态框标题
const title = ref<string>("");

// form表单
const formRef = ref<FormInstance>();
const rules = reactive<FormRules<UploadFilePar>>({
  fileName: [
    { required: true, message: "文件夹名称不能为空", trigger: "blur" },
    { min: 1, max: 60, message: "文件夹名称最长为60个字符", trigger: "blur" },
  ],
});

/**
 * 新建文件夹、上传文件入参
 * @parentId  文件夹ID 0：根目录
 * @ownership 1: 公共文档  2：我的文件 3：部门文档
 * @fileName 文件夹名称
 */
const uploadForm = reactive<UploadFilePar>({
  parentId: "0",
  ownership: 2,
  fileName: "",
  deptId: "",
});
const butVisible = ref<boolean>(true); // 文件大于500MB 提交按钮禁用
const docVisible = ref<boolean>(false); // 上传文件
const formLoading = ref<boolean>(false); // 上传loading加载效果
const fileList = ref<UploadUserFile[]>([]); //用于存储文件列表的数组

// 分片上传 上传文件信息
const fileData = ref<UploadRawFile | null>(null);
const uploadProgress = ref<number>(0); // 分片上传 进度条
// 分片上传 计算上传文件大小
const fileSize = ref<number>(0);
const chunks: FileChunk[] = [];
// 分片上传 文件上传中提示语
let messageInstance: MessageHandler | null = null;
// 分片上传 统计上传文件个数
let statistics = ref<number>(0);
// 分片上传 计算文件数
let calculate = ref<number>(0);
// 分片上传 url
const uploadUrl = VITE_FRAGMENT_DOWNLOAD_UPLOAD; // 替换为实际的上传URL

/**
 * @上传文件 模态框
 * @toggle 文件&文件夹切换
 * @active 文件&文件夹 隐藏\显示
 * @docVisible 上传文件模态框
 */
const uploadDoc = (name: string) => {
  if (name === Stauts.ADD) {
    title.value = "新建文件夹";
  }
  if (name === Stauts.DOC) {
    title.value = "上传文件";
  }
  if (name === Stauts.FILE) {
    title.value = "上传文件夹";
  }

  uploadForm.ownership = props.ownership;
  uploadForm.parentId = String(props.fileID);
  toggle.value = name;
  docVisible.value = true;
  props.uploadpull.moreupload = false;
};

/**
 * @新增文件夹
 * @上传文件
 * @上传文件夹
 */
const uploadSure = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.validate((valid) => {
    if (valid) {
      // 获取部门ID
      uploadForm.deptId = props.deptID;

      // 新增文件夹
      if (toggle.value === Stauts.ADD) {
        $addFile(uploadForm);
      }

      // 上传文档
      if (toggle.value === Stauts.DOC) {
        if (fileList.value.length == 0) {
          ElMessage.error(Prompt.FILE_EMPTY);
          return;
        }
        formLoading.value = true;
        // 将所有 的 upload 组件中的文件对象放入到 FormData 对象中
        fileList.value.forEach((file: any) => {
          // 映射对应的图标
          const cutout = file.name.split(".").reverse();
          const suffix = cutout[0].toLowerCase();
          let svgImg = docIcon[suffix];
          if (svgImg === undefined) {
            svgImg = "icon-nor-m-24";
          }

          fileData.value = null;
          // 文件超过50MB调用分片上传
          if (file.size > UploadBig50MB) {
            calculate.value += 1;
            // 显示加载中消息
            if (calculate.value === 1) {
              messageInstance = ElMessage({
                message: "文件正在分片上传中...",
                type: "success",
                duration: 0, // 不自动关闭
                showClose: true, // 显示关闭按钮
              });
            }

            fileData.value = file.raw;
            fileSize.value = file.size;
            // 分片上传
            handleUpload(svgImg);
          } else {
            // 单文件上传文件流
            const formData = new FormData();
            formData.append("file", file.raw);
            $uploadFile(uploadForm, formData, svgImg); // 调用后端接口
          }
        });
      }
      // 取消按钮
      onCancel(formRef.value);
    }
  });
};

/**
 * @单个文件上传
 * @分片上传
 * 选中
 */
const uploadSuccess = (item: any) => {
  // 文件上传大于500MB提示
  if (item.size > UploadTotal) {
    butVisible.value = false;
    ElMessage.warning(Prompt.FILE_UPLOAD);
    return;
  }
  butVisible.value = true;
  fileList.value.push(item);
};

/**
 * @上传文件
 * 删除
 */
const handleRemove = (_file: any, uploadFiles: any) => {
  fileList.value = uploadFiles;
  if (uploadFiles.length === 0) {
    butVisible.value = true;
    return;
  }
  // 判断文件是否还存在大于500MB
  for (let i = 0; i < uploadFiles.length; i++) {
    const size = uploadFiles[i].size;
    if (size > UploadTotal) {
      butVisible.value = false;
      return;
    }
    butVisible.value = true;
  }
};

/**
 * @分片上传文件
 */
const uploadChunk = async (
  chunk: any,
  index: number,
  total: number,
  fileTotal: number,
  svgImg: string
): Promise<void> => {
  // 从文件中创建一个指定范围的Blob对象作为分片。
  try {
    const formData = new FormData();
    formData.append("file", chunk);
    formData.append("fileName", fileData.value!.name);
    formData.append("fileSize", fileTotal.toString());
    formData.append("chunkIndex", index.toString());
    formData.append("totalChunks", total.toString());

    const url =
      uploadUrl +
      `/api/doc/file/shardUpload/${uploadForm.ownership}/${uploadForm.parentId}?icon=${svgImg}&deptId=${uploadForm.deptId}`;
    const ret = await axios.post(url, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
        Authorization: `Bearer ${token}`,
      },
      onUploadProgress: (progressEvent) => {
        // 计算上传进度
        const progress =
          ((index + progressEvent.loaded / UploadEachPiece) / total) * 100;
        uploadProgress.value = Math.min(progress, 100);
        console.log(uploadProgress.value);
        console.log(`Upload progress: ${uploadProgress.value.toFixed(2)}%`);
        if (index + 1 === total) {
          uploadProgress.value = 100;
        }
      },
    });
    // 判断文件是否上传合并成功
    if (ret.data.data) {
      statistics.value += 1;
    }
    if (statistics.value === calculate.value) {
      // 关闭加载中消息
      if (messageInstance) {
        messageInstance.close();
      }
      ElMessage({
        message: ret.data.msg,
        type: "success",
      });
      eventBus.emit("onEventBus", "");
    }
  } catch (err: any) {
    console.error(err);
    throw new Error(
      `上传数据块错误 ${chunk.start}-${chunk.end}: ${err.message}`
    );
  }
};

/**
 * @分片上传文件处理整个上传过程
 * 包括初始化分片、并发上传、处理错误和通知服务器合并分片。
 */
const handleUpload = async (svgImg: string) => {
  if (!fileData.value) return;

  const totalChunks = Math.ceil(fileSize.value / UploadEachPiece);
  for (let chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
    const start = chunkIndex * UploadEachPiece;
    const end = Math.min(fileData.value.size, start + UploadEachPiece);
    const chunk = fileData.value.slice(start, end);
    chunks.push({
      start,
      end,
      promise: uploadChunk(
        chunk,
        chunkIndex,
        totalChunks,
        fileSize.value,
        svgImg
      ),
    });
  }
};

/**
 * @上传文件夹
 */
const handleFile = (event: Event) => {
  // const formData = new FormData();
  // const files = event.target.files;
  // traverseFiles(files, formData);

  // debugger;
  const files = (event.target as HTMLInputElement).files;
  if (!files) return;

  let totalSize = 0;
  for (let file of files) {
    totalSize += file.size;
    if (totalSize > 500 * 1024 * 1024) {
      ElMessage({
        message: "单个文件已超过500MB，请联系管理员",
        type: "warning",
      });
      return;
    }

    fileList.value.push(file);
  }
  fileList.value.forEach((file: any) => {
    const svgImg = "icon-nor-m-24";
    fileData.value = file;
    fileSize.value = file.size;
    handleUpload(svgImg);
  });

  // const tree = handleFloder(files);
  // console.log(tree);
  // for (let i in tree) {
  //   if (!(tree[i] instanceof File)) {
  //     console.log(`创建文件夹, 开始递归`, tree[i]);
  //     traverseTree(tree[i]);
  //   } else {
  //     console.log(`文件, 开始递归`, tree[i]);
  //   }
  // }
};

// 遍历文件夹中的所有文件
// 处理目录结构
// const handleFloder = (list: any) => {
//   let tree: any = {};
//   for (let i = 0; i < list.length; i++) {
//     const pathParts = list[i].webkitRelativePath.split("/");
//     let current = tree;
//     pathParts.forEach((part: string | number) => {
//       if (!current[part]) {
//         current[part] = {};
//       }
//       if (current[list[i].name]) {
//         current[list[i].name] = list[i];
//       }
//       current = current[part];
//     });
//   }
//   return tree;
// };

// // 递归遍历树节点
// const traverseTree = (file: any) => {
//   if (file instanceof File) {
//     console.log("这里是文件----------");
//     console.log(file);
//     return;
//   } else {
//     // 是文件夹，读取文件夹里的文件，遍历文件夹
//     for (let i in file) {
//       if (!(file[i] instanceof File)) {
//         console.log("这里是文件夹,可以在这里进行操作================");
//         console.log(file[i]);
//       }
//       traverseTree(file[i]);
//     }
//   }
// };

// 上传按钮点击
const onOff = (name: boolean) => {
  name
    ? (props.uploadpull.moreupload = !props.uploadpull.moreupload)
    : (props.uploadpull.moreupload = name);
};

/**
 * @取消按钮
 * @formLoading 上传加载关闭
 * @docVisible 上传文件模态框关闭
 * @fileList 清空展示列表
 */
const onCancel = (formEl: FormInstance | undefined) => {
  formLoading.value = false;
  docVisible.value = false;
  butVisible.value = true;
  fileList.value.length = 0;
  if (!formEl) return;
  formEl.resetFields();
};
const handleClose = () => {
  onCancel(formRef.value);
};
</script>

<style scoped lang="less">
::v-deep(.el-progress--circle .el-progress__text) {
  left: -4px;
}
// 按钮
.light-but {
  height: 48px;
  line-height: 48px;
  padding: 0 10px;
  ::v-deep(.el-button) {
    width: 95px;
    line-height: 2px;
  }
  .cas-upload {
    margin-right: 6px;
  }

  .controls-menu {
    width: 200px;
    padding: 6px 0;
    z-index: 200;
    position: relative;
    left: 106px;
    border: 1px solid rgba(0, 0, 0, 0.08);
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0px 5px 12px 4px rgba(0, 0, 0, 0.08);
    .controls-ul {
      padding: 0;
      margin: auto;
      cursor: pointer;

      li {
        height: 40px;
        line-height: 40px;

        span {
          display: block;
          font-size: 14px;
          font-weight: 400;
          padding: 0 0 0 16px;
          color: rgba(0, 0, 0, 0.88);
          .svg-icon {
            display: inline-block;
            position: relative;
            top: 5px;
            margin-right: 10px;
          }
        }
      }
    }
    .controls-ul li:hover span {
      background-color: #f4f4f4;
    }
  }
}

// 上传文件
.upload-demo {
  width: 310px;
  // 改变上传组件样式 el-upload
  ::v-deep(.el-upload-dragger) {
    padding: 5px 60px;
  }
  .upload-form {
    max-width: 480px;
    padding-left: 80px;
  }
  .icon-upload {
    font-size: 70px;
    color: #a8abb2;
  }
  .icon-question {
    cursor: pointer;
    position: absolute;
    left: 324px;
    margin-top: -20px;
  }
}

.dialog-footer {
  .btn-style {
    min-width: 90px;
  }
}
</style>
