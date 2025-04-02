<template>
  <div class="controls">
    <div class="controls-list">
      <el-button-group
        v-show="
          feature.numLength > 0 &&
          (feature.rootNode === Document.COMMON ||
            feature.rootNode === Document.MINEFILE ||
            feature.rootNode === Document.SECTION)
        "
      >
        <el-button @click="onCommon('download')">下载</el-button>
        <el-button @click="onCommon('share')">分享</el-button>
        <el-button
          @click="onCommon('delete')"
          v-permission="`${feature.permission}`"
        >
          删除
        </el-button>
        <el-button @click="onMore"> 更多 </el-button>
      </el-button-group>
      <div class="controls-menu" v-show="feature.moreStatus">
        <ul class="controls-ul">
          <li
            @click="onCommon('transfer')"
            v-permission="`${feature.permission}`"
          >
            <span>转移到</span>
          </li>
          <span v-show="feature.isFile">
            <li v-if="feature.status == 0">
              <span @click="onCommon('collectfile')">收藏文件</span>
            </li>
            <li v-else>
              <span @click="onCommon('uncollect')">取消收藏</span>
            </li>
          </span>
          <li>
            <span @click="onCommon('tag')">添加标签</span>
          </li>
          <span v-permission="`${feature.permission}`">
            <li v-show="feature.numLength == 1">
              <span @click="onCommon('rename')"> 重命名 </span>
            </li>
          </span>
          <span>
            <li v-show="feature.numLength == 1">
              <span @click="onCommon('details')"> 详细信息 </span>
            </li>
          </span>
        </ul>
      </div>

      <!-- 收藏夹：collect -->
      <el-button-group
        v-show="feature.numLength > 0 && feature.cutType === Document.COLLECT"
      >
        <el-button @click="onCommon('download')">下载</el-button>
        <el-button @click="onCommon('share')">分享</el-button>
        <el-button @click="onCommon('uncollect')">取消收藏</el-button>
        <el-button @click="onMoreCollect"> 更多 </el-button>
      </el-button-group>
      <div class="controls-menu" v-show="feature.collectStatus">
        <ul class="controls-ul">
          <li>
            <span @click="onCommon('tag')">添加标签</span>
          </li>
          <li v-show="feature.numLength == 1">
            <span @click="onCommon('details')"> 详细信息 </span>
          </li>
        </ul>
      </div>

      <!-- 分享记录 标识：share -->
      <el-button-group
        v-show="feature.numLength > 0 && feature.cutType === Document.SHARE"
      >
        <el-button
          v-show="feature.numLength == 1"
          @click="onCommon('interview')"
        >
          访问
        </el-button>
        <el-button
          v-show="feature.numLength == 1"
          @click="onCommon('copyshare')"
        >
          复制链接
        </el-button>
        <el-button
          v-show="feature.numLength >= 1 && feature.record === 2"
          @click="onCommon('unshare')"
        >
          取消分享
        </el-button>
      </el-button-group>

      <!-- 回收站 标识：recycle -->
      <el-button-group
        v-show="feature.numLength > 0 && feature.cutType === Document.RECYCLE"
      >
        <el-button @click="onCommon('recover')"> 还原 </el-button>
        <el-button @click="onCommon('permanentdelete')"> 永久删除 </el-button>
        <el-button @click="onCommon('details')" v-show="feature.numLength == 1">
          详细信息
        </el-button>
      </el-button-group>

      <!-- 个人标签 标识：tag -->
      <el-button-group
        v-show="feature.numLength > 0 && feature.cutType === Document.TAG"
      >
        <el-button @click="onCommon('download')">下载</el-button>
        <el-button @click="onCommon('share')">分享</el-button>
        <el-button @click="onCommon('tag')" v-show="feature.numLength == 1">
          标签
        </el-button>
      </el-button-group>
    </div>
  </div>

  <div
    class="dialogs"
    :style="{
      left: feature.left + 'px',
      top: feature.top + 'px',
    }"
    v-show="props.visible"
  >
    <!-- 收藏夹：collect -->
    <ul class="dialogs-list" v-show="feature.cutType === Document.COLLECT">
      <li class="list-item">
        <span class="txt" @click="onCommon('download')">下载</span>
      </li>
      <li class="list-item">
        <span class="txt" @click="onCommon('share')">分享</span>
      </li>
      <div class="spliter"></div>
      <li class="list-item">
        <span class="txt" @click="onCommon('uncollect')">取消收藏</span>
      </li>
      <li class="list-item">
        <span class="txt" @click="onCommon('tag')">添加标签</span>
      </li>
      <div class="spliter"></div>
      <li class="list-item">
        <span class="txt" @click="onCommon('details')">详细信息</span>
      </li>
    </ul>
    <!-- 分享记录: share-->
    <ul class="dialogs-list" v-show="feature.cutType === Document.SHARE">
      <li class="list-item">
        <span class="txt" @click="onCommon('interview')">访问链接</span>
      </li>
      <li class="list-item">
        <span class="txt" @click="onCommon('copyshare')">复制链接</span>
      </li>
      <li class="list-item" v-show="feature.record === 2">
        <span class="txt" @click="onCommon('unshare')">取消分享</span>
      </li>
    </ul>

    <!-- 回收站: recycle -->
    <ul class="dialogs-list" v-show="feature.cutType === Document.RECYCLE">
      <li class="list-item">
        <span class="txt" @click="onCommon('recover')">还原</span>
      </li>
      <li class="list-item">
        <span class="txt" @click="onCommon('permanentdelete')">永久删除</span>
      </li>
      <li class="list-item">
        <span class="txt" @click="onCommon('details')">详细信息</span>
      </li>
    </ul>

    <!-- 个人标签 标识：tag -->
    <ul class="dialogs-list" v-show="feature.cutType === Document.TAG">
      <li class="list-item">
        <span class="txt" @click="onCommon('download')">下载</span>
      </li>
      <li class="list-item">
        <span class="txt" @click="onCommon('share')">分享</span>
      </li>
      <li class="list-item" v-show="feature.pageheader === 0">
        <span class="txt" @click="onCommon('tag')">标签</span>
      </li>
      <div class="spliter"></div>
      <li class="list-item">
        <span class="txt" @click="onCommon('details')">详细信息</span>
      </li>
    </ul>

    <ul
      class="dialogs-list"
      v-show="
        feature.rootNode === Document.COMMON ||
        feature.rootNode === Document.MINEFILE ||
        feature.rootNode === Document.SECTION
      "
    >
      <li class="list-item">
        <span class="txt" @click="onCommon('download')">下载</span>
      </li>
      <li class="list-item">
        <span class="txt" @click="onCommon('share')">分享</span>
      </li>
      <div class="spliter"></div>
      <li class="list-item" v-permission="`${feature.permission}`">
        <span class="txt" @click="onCommon('delete')"> 删除 </span>
      </li>
      <li class="list-item" v-permission="`${feature.permission}`">
        <span class="txt" @click="onCommon('transfer')"> 转移到 </span>
      </li>
      <span v-show="feature.isFile">
        <li class="list-item" v-if="feature.status == 0">
          <span class="txt" @click="onCommon('collectfile')">收藏文件</span>
        </li>
        <li class="list-item" v-else>
          <span class="txt" @click="onCommon('uncollect')">取消收藏</span>
        </li>
      </span>
      <div class="spliter" v-permission="`${feature.permission}`"></div>
      <li class="list-item">
        <span class="txt" @click="onCommon('tag')">添加标签</span>
      </li>
      <li class="list-item" v-permission="`${feature.permission}`">
        <span class="txt" @click="onCommon('rename')"> 重命名 </span>
      </li>
      <div class="spliter"></div>
      <li class="list-item">
        <span class="txt" @click="onCommon('details')">详细信息</span>
      </li>
    </ul>
  </div>

  <!-- 回收提示 -->
  <el-dialog
    v-model="dialogVisible"
    title="删除文件"
    width="500"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    :before-close="handleClose"
  >
    <div class="modal-dialog">
      <div class="modal-content">
        <el-icon :size="26" color="#eebe77"><WarningFilled /></el-icon>
        <span class="modal-span"> 确定要删除这些文件（夹）？ </span>
      </div>
      <span class="modal-tips">删除后可在回收站找回</span>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-style" @click="dialogVisible = false">
          取消
        </el-button>
        <el-button class="btn-style" type="primary" @click="onOK">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 分享 -->
  <ShareComponents :shareObj="shareObj" />
  <!-- 转移 -->
  <TransferComponents :shiftObj="shiftObj" />
  <!-- 添加标签 -->
  <AddtagComponents :addtag="addtag" />
  <!-- 详细信息 -->
  <FiledetailsComponents :detailsObj="detailsObj" />
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { Document, Prompt } from "@/common/enum";
import { $collect, $deleteFile, $onDownload, $unCollect } from "@/api/document";
import { WarningFilled } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import vPermission from "@/stort/permission";
import { $unshare } from "@/api/share";
import { copyLink } from "@/utils/other";
import { $permanentDelete, $recover } from "@/api/recycle";
import ShareComponents from "./Share.vue";
import TransferComponents from "./Transfer.vue";
import AddtagComponents from "./Addtag.vue";
import FiledetailsComponents from "./Filedetails.vue";

const { VITE_FRAGMENT_DOWNLOAD_UPLOAD } = import.meta.env;

const props = defineProps({
  // 公共入参
  feature: {
    type: Object,
    default: {},
  },
  // 转移到 入参
  transfer: {
    type: Object,
    default: {},
  },
  // 右击 展示菜单
  visible: {
    type: Boolean,
    default: false,
  },
});

/**
 * 子组件传参给父组件
 * 收藏夹、回收站
 */
const emit = defineEmits(["child-event"]);

// 回收提示 模态框
const dialogVisible = ref<boolean>(false);

/**
 * 分享 入参
 * @visible 分享模态框
 * @fileList 分享文件ID
 */
const shareObj = reactive({
  visible: false,
  fileList: [],
});

/**
 * 转移 入参
 * @visible 隐藏显示模态框
 * @delFlag 删除标记，0：删除，1：正常
 */
const shiftObj = reactive({
  visible: false,
  fileList: [],
});

/**
 * 添加标签
 */
const addtag = reactive({
  visible: false,
  tagStatus: "",
  fileList: [],
});

/**
 * 详细信息
 */
const detailsObj = reactive({
  visible: false,
  fileId: "",
});

/**
 * 更多点击事件
 */
const onMore = (name: boolean) => {
  name
    ? (props.feature.moreStatus = !props.feature.moreStatus)
    : (props.feature.moreStatus = name);
};
/**
 * 收藏夹
 */
const onMoreCollect = (name: boolean) => {
  name
    ? (props.feature.collectStatus = !props.feature.collectStatus)
    : (props.feature.collectStatus = name);
};

/**
 * 公共操作方法
 */
const onCommon = (name: string) => {
  // 点击更多
  props.feature.moreStatus = false;
  // 收藏夹
  props.feature.collectStatus = false;

  const { fileParam } = props.feature;

  const sum = fileParam.length;
  switch (name) {
    case "download": // 下载
      // 批量打包下载
      elMessage(sum, Prompt.DOWNLOD);
      $onDownload(VITE_FRAGMENT_DOWNLOAD_UPLOAD, fileParam, sum);
      break;
    case "share": // 分享
      shareObj.visible = true;
      shareObj.fileList = fileParam;
      break;
    case "delete": // 删除
      dialogVisible.value = true;
      break;
    case "transfer": // 转移
      shiftObj.fileList = fileParam;
      // 合并对象
      Object.assign(shiftObj, props.transfer);
      shiftObj.visible = true;
      break;
    case "collectfile": // 收藏文件
      elMessage(sum, Prompt.COLLECT);
      $collect(sum, fileParam);
      break;
    case "uncollect": // 取消收藏
      elMessage(sum, Prompt.UNCOLLECT);
      $unCollect(sum, fileParam);
      break;
    case "tag": // 添加标签
      addtag.visible = true;
      addtag.tagStatus = props.feature.tagStatus;
      addtag.fileList = fileParam;
      break;
    case "rename": // 重命名
      const showEdit = fileParam[0].fileId;
      emit("child-event", showEdit);
      break;
    case "details": // 详细信息
      detailsObj.visible = true;
      detailsObj.fileId = fileParam[0].fileId;
      break;
    case "interview": // 访问分享链接
      const urlLink = fileParam[0].url;
      const target = "_blank";
      window.open(urlLink, target);
      break;
    case "copyshare": // 复制分享链接
      const shareUrl = fileParam[0].url;
      copyLink(shareUrl); // 复制公共方法
      // 弹出复制成功信息
      ElMessage({
        message: "复制成功，可进行分享链接",
        type: "success",
      });
      break;
    case "unshare": // 取消分享
      $unshare(sum, fileParam);
      break;
    case "recover": // 还原
      $recover(fileParam);
      break;
    case "permanentdelete": // 永久删除
      $permanentDelete(fileParam);
      break;

    default:
      break;
  }
};

// 回收提示 模态框 确定
const onOK = () => {
  const { fileParam } = props.feature;
  $deleteFile(fileParam); // 删除接口
  dialogVisible.value = false;
};

// 回收提示 模态框 取消
const handleClose = () => {
  dialogVisible.value = false;
};

// 提示语
const elMessage = (num: number, name?: string) => {
  if (num > 10) {
    ElMessage({
      message: name,
      type: "warning",
    });
    return;
  }
};
</script>

<style scoped lang="less">
::v-deep(.el-button-group) {
  float: right;
  padding-top: 10px;
}
// 操作 下载 分享 删除
.controls {
  width: 450px;
  height: 48px;
  line-height: 48px;
  .controls-list {
    height: 48px;
    // 修改el-button 组件原始样式
    .el-button {
      min-width: 65px;
      font-size: 12px;
      color: #000000;
    }
    .el-button:hover {
      border-color: #dcdfe6;
      background-color: #f4f4f4;
    }
    .el-button:active {
      border-color: #dcdfe6;
      background-color: #f4f4f4;
    }
    .controls-menu {
      width: 200px;
      padding: 6px 0;
      z-index: 200;
      position: relative;
      top: 50px;
      left: 254px;
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
            font-size: 12px;
            font-weight: 400;
            padding: 0 0 0 16px;
            color: #000000;
          }
        }
      }
      .controls-ul li:hover span {
        background-color: #f4f4f4;
      }
    }
  }
}

// 通过右击获取到的坐标定位
.dialogs {
  width: 320px;
  padding: 6px 0;
  z-index: 100;
  position: fixed;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0px 5px 12px 4px rgba(0, 0, 0, 0.08);
  .dialogs-list {
    padding: 0;
    margin: auto;
    cursor: pointer;
    .list-item {
      height: 40px;
      line-height: 40px;
      .txt {
        display: block;
        font-size: 12px;
        font-weight: 400;
        padding: 0 0 0 16px;
        color: rgba(0, 0, 0, 0.88);
      }
    }
    .spliter {
      margin: 4px 0;
      height: 1px;
      background-color: #0f2042;
      background-color: rgba(0, 0, 0, 0.04);
    }
  }
  .list-item:hover .txt {
    background-color: #f4f4f4;
  }
}

// 回收提示 模态框
.modal-dialog {
  .modal-content {
    .modal-span {
      margin-bottom: 2px;
      font-size: 16px;
      color: #000;
      position: absolute;
      margin-left: 4px;
    }
  }
  .modal-tips {
    padding-left: 30px;
    font-size: 14px;
    color: #868686;
  }
}
.dialog-footer {
  .btn-style {
    min-width: 90px;
  }
}
</style>
