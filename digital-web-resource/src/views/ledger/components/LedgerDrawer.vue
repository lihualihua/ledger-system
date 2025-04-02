<template>
  <el-drawer v-model="props.templateObj.visible" direction="rtl" size="40%">
    <template #header>
      <h4 class="title-name">台账模板库</h4>
    </template>
    <template #default>
      <div class="tip">
        <p>系统模版</p>
        <el-upload
          action
          accept=".xlsx,.xls"
          :show-file-list="false"
          :auto-upload="false"
          :on-change="uploadTemplateSystem"
        >
          <template #trigger>
            <el-button link type="primary">
              系统模版
              <el-icon :size="18" class="icon-upload"><UploadFilled /></el-icon>
            </el-button>
          </template>
        </el-upload>
      </div>

      <div
        class="container"
        v-loading="loading"
        element-loading-text="数据加载中..."
        v-if="systemTable.length > 0"
      >
        <div class="content" v-for="(item, index) in systemTable" :key="index">
          <div class="title" :title="item.name">
            <span>模版 {{ index + 1 }}：{{ item.name }}</span>
          </div>
          <div class="time">{{ formatDate(item.createTime) }}</div>

          <el-button
            type="primary"
            link
            @click="onDownload(0, item.id, item.name)"
          >
            下载模版
          </el-button>
          <el-button
            type="danger"
            link
            @click="onDelete(item.id, item.name!, 'sys')"
            v-permission="`${permission}`"
          >
            删除
          </el-button>
        </div>
      </div>
      <div class="no-data" v-else>暂无数据</div>

      <div class="tip">
        <p>我的模版</p>
        <el-upload
          action
          accept=".xlsx,.xls"
          :show-file-list="false"
          :auto-upload="false"
          :on-change="uploadTemplate"
        >
          <template #trigger>
            <el-button link type="primary">
              我的模版
              <el-icon :size="18" class="icon-upload"><UploadFilled /></el-icon>
            </el-button>
          </template>
        </el-upload>
      </div>
      <div
        class="container list-webkit"
        v-loading="myLoading"
        element-loading-text="数据加载中..."
        v-if="customTable.length > 0"
      >
        <div class="content" v-for="(item, index) in customTable" :key="index">
          <div class="title" :title="item.name">
            <span>模版 {{ index + 1 }}：{{ item.name }}</span>
          </div>
          <div class="time">{{ formatDate(item.createTime) }}</div>

          <el-button
            type="primary"
            link
            @click="onDownload(1, item.id, item.name)"
          >
            下载模版
          </el-button>
          <el-button type="danger" link @click="onDelete(item.id, item.name!)">
            删除
          </el-button>
        </div>
      </div>
      <div class="no-data" v-else>暂无数据</div>
    </template>
  </el-drawer>

  <el-dialog
    draggable
    width="570"
    title="下载自定义模版"
    v-model="visible"
    :close-on-click-modal="false"
    :destroy-on-close="true"
  >
    <div class="form-content">
      <el-form ref="ruleFormRef" label-width="140">
        <el-form-item label="选择模版类型：">
          <el-radio-group v-model="typeValue">
            <el-radio :value="1">行分发模板</el-radio>
            <el-radio :value="2">单元格分发模板</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-style" @click="onReset"> 取消 </el-button>
        <el-button class="btn-style" type="primary" @click="onSubmit">
          下载
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { useUserStore } from "@/stort/user";
import {
  $deleteTemplate,
  $download,
  $findMyTemplates,
  $findSysTemplateList,
  $uploadTemplatePost,
} from "@/api/ledger";
import { Permission, Returndata } from "@/common/enum";
import { TemplateList } from "@/interface/ledger";
import { ElMessage, ElMessageBox } from "element-plus";
import { formatDate } from "@/utils/other";
import vPermission from "@/stort/permission";
import { UploadFilled } from "@element-plus/icons-vue";

const props = defineProps({
  templateObj: {
    type: Object,
    default: {},
  },
});

const userStore = useUserStore();

// 按钮权限
const permission = ref<string>("");

const visible = ref<boolean>(false);
// 选择模版类型
const typeValue = ref<number>(1);
// 模版ID
const templateId = ref<string>("");
// 模版名称
const excelName = ref<string>("");
// 系统默认模版 加载loading
const loading = ref<boolean>(false);
// 我的模版 加载myLoading
const myLoading = ref<boolean>(false);

// 系统模版
const systemTable = ref<TemplateList[]>([]);
// 自定义模版
const customTable = ref<TemplateList[]>([]);

/**
 * 系统默认模版 列表
 */
const systemTemplateList = () => {
  loading.value = true;
  $findSysTemplateList()
    .then((res: any) => {
      if (res.data && res.data.length > 0) {
        res.data.forEach((item: any) => {
          item.name = item.name.split(".")[0];
        });

        systemTable.value = res.data;
        loading.value = false;
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {
      loading.value = false;
    });
};

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
        customTable.value = res.data.records;
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
 * @上传系统模版
 * @systemFlag （系统模版：1）
 */
const uploadTemplateSystem = (item: any) => {
  commonFun(1, item, "系统模版上传成功");
};

/**
 * @上传自定义模版
 * @systemFlag （自定义模版：0）
 */
const uploadTemplate = (item: any) => {
  commonFun(0, item, "自定义模版上传成功");
};

/**
 * @上传系统模版
 * @上传自定义模版
 * 公共接口
 */
const commonFun = (systemFlag: number, item: any, tips: string) => {
  const formData = new FormData();
  formData.append("file", item.raw);
  $uploadTemplatePost(systemFlag, formData)
    .then((res: any) => {
      if (res && res.code === Returndata.code) {
        ElMessage({
          message: tips,
          type: "success",
        });
        systemFlag == 0 ? customTemplateList() : systemTemplateList();
      }
    })
    .catch((err) => {
      console.log("文件上传处理错误信息：", err);
    });
};

/**
 * 删除模版
 */
const onDelete = (id: string, name: string, mark?: string) => {
  ElMessageBox.confirm(`是否确定删除《${name}》模版？`, "提示", {
    confirmButtonText: " 确定 ",
    cancelButtonText: " 取消 ",
    type: "warning",
  })
    .then(() => {
      $deleteTemplate(id)
        .then((res: any) => {
          if (res && res.code === Returndata.code) {
            ElMessage({
              message: "模版删除成功",
              type: "success",
            });

            if (mark === "sys") {
              systemTemplateList();
            } else {
              customTemplateList();
            }
          }
        })
        .catch((err) => {
          console.log("错误信息：", err);
        });
    })
    .catch(() => {});
};

/**
 * 下载模版
 */
const onDownload = (num: number, id: string, name?: string) => {
  // 系统默认模版
  if (num == 0) {
    $download(num, id, name);
    return;
  }

  // 我的模版
  visible.value = true;
  templateId.value = id;
  excelName.value = name!;
};

/**
 * 自定义模版
 * 模态框 确定
 */
const onSubmit = () => {
  $download(typeValue.value, templateId.value, excelName.value);
  onReset();
};

/**
 * 模态框 取消
 */
const onReset = () => {
  visible.value = false;
  typeValue.value = 1;
};

// 监听 visible 的变化，当模态框打开时调用 初始化 方法
watch(
  () => props.templateObj.visible,
  (newVal) => {
    if (newVal) {
      /**
       * 按钮权限判断
       * @userStore 信息持久化
       * @SUPER_ADMIN 超级管理员
       */
      const { permissions } = userStore.user;
      let roleCode = "";
      permissions.forEach((res: any) => {
        roleCode = res.roleCode;
      });
      // 系统默认模版 超级管理员 展示删除按钮
      if (roleCode === Permission.super_admin) {
        permission.value = Permission.super_admin;
      }

      systemTemplateList();
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

.tip {
  padding: 10px 16px;
  background-color: #409eff1a;
  border-radius: 4px;
  border-left: 5px solid #409eff;
  margin-bottom: 20px;
  display: flex;

  p {
    font-size: 14px;
    color: #303133;
    flex: 1 1 0%;
  }

  .icon-upload {
    position: relative;
    margin-left: 4px;
  }

  ::v-deep(.el-button.is-link:hover) {
    color: #1180f3;
  }
}

.container {
  margin-bottom: 24px;
  .content {
    display: flex;
    height: 40px;
    line-height: 40px;
    font-size: 14px;

    .title {
      width: 60%;
      color: #606266;
      padding: 0 30px;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
    .time {
      width: 22%;
      font-size: 12px;
      color: #939090;
    }
  }
  .content:hover {
    background-color: #f5f7fa;
  }
}

.no-data {
  text-align: center;
  line-height: 80px;
  color: #b8b9ba;
}

.list-webkit {
  height: 60%;
  overflow: auto;
}

// 滚动条
.list-webkit::-webkit-scrollbar {
  width: 4px;
  height: 10px;
  overflow: visible;
}
.list-webkit::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
.list-webkit::-webkit-scrollbar-thumb {
  border: solid transparent;
  border-width: 1px 0;
  background-clip: padding-box;
  background: #d8dce5;
}

.form-content {
  margin-top: 24px;
}

.dialog-footer {
  .btn-style {
    min-width: 90px;
  }
}
</style>
