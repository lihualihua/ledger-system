<template>
  <el-container @click="closeMenu">
    <el-header>
      <div class="layout-header-inner">
        <div class="logo" @click="onLoginImg">
          <div class="logo-svg">
            <SvgIcon name="icon-vite" width="40px" height="40px"></SvgIcon>
          </div>
          <a> 台账数据库 </a>
        </div>
        <div v-if="userName === ''" class="mod-act-group" @click="onLogin">
          <span class="mod-act-name">登 录</span>
        </div>
        <div v-else class="mod-act-group" @mouseleave="onMouse(false)">
          <span @mouseenter="onMouse(true)">
            <el-avatar :icon="UserFilled" class="index-avatar" />
            {{ userName }}
          </span>
          <ul v-show="isShow">
            <li @click="exit">
              <el-icon :size="18" class="li-icon"><SwitchButton /></el-icon>
              退出登录
            </li>
          </ul>
        </div>
      </div>
    </el-header>
    <el-main>
      <div class="layout-main">
        <div class="mod-action-list">
          <div class="mod-action-wrap">
            <el-button @click="onCommon" :disabled="sum > 0 ? false : true">
              下载
            </el-button>
          </div>
        </div>
        <div class="mod-breadcrumb">
          <ul>
            <li>
              <span>{{ name }}</span>
              的分享
            </li>
          </ul>
        </div>
      </div>
      <!-- 页眉 -->
      <div class="page-header">
        <div class="tablist">
          <ul class="tabul" v-show="breadcrumb.length > 0">
            <li class="tabli">
              <span @click="init()">全部</span>
            </li>
            <span
              class="file-span"
              v-show="breadcrumb.length > 0"
              v-for="(item, index) in breadcrumb"
              :key="item.id"
            >
              <SvgIcon
                name="icon-arrow"
                width="12px"
                height="12px"
                class="icon-arrow2"
              ></SvgIcon>
              <a
                :class="[
                  'file-name',
                  index + 1 === breadcrumb.length ? 'cut' : '',
                ]"
                @click="onPageHeader(item.id, item.fileId)"
              >
                <span :title="item.fileName">{{ item.fileName }}</span>
              </a>
            </span>
          </ul>
        </div>
      </div>

      <div
        class="layout-main"
        style="height: calc(100vh - 126px)"
        @contextmenu.prevent="contextmenuPrevent"
      >
        <el-table
          v-loading="tableLoading"
          :data="shareTable"
          ref="multipleTableRef"
          tooltip-effect="dark"
          style="width: 100%"
          height="680"
          highlight-current-row
          @row-contextmenu="rowContextmenu"
          @row-click="handleRowClick"
          @select="handleSelect"
          @select-all="handleSelectAll"
        >
          <el-table-column type="selection" width="40" />
          <el-table-column prop="fileName" label="名称" sortable>
            <template #default="scope">
              <i :class="['icon', scope.row.icon]"></i>
              <span
                class="table-on-hover"
                @click="
                  onHierarchy(
                    scope.row.fileType,
                    scope.row.fileId,
                    scope.row.fileName
                  )
                "
                >{{ scope.row.fileName }}</span
              >
            </template>
          </el-table-column>
          <el-table-column
            prop="updateTime"
            label="上次修改时间"
            sortable
            width="220"
          >
            <template #default="scope">
              <span class="table-color">
                {{ formatDate(scope.row.updateTime) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="fileSizeStr" label="大小" width="160">
            <template #default="scope">
              <span class="table-color">
                {{ scope.row.fileSizeStr ? scope.row.fileSizeStr : "--" }}
              </span>
            </template>
          </el-table-column>
        </el-table>
        <div class="list-group-info">
          共
          <b class="info-con">{{ total }}</b>
          项
        </div>
      </div>

      <div
        class="dialogs"
        :style="{
          left: coordinate.left + 'px',
          top: coordinate.top + 'px',
        }"
        v-show="visible"
      >
        <ul class="dialogs-list">
          <li class="list-item">
            <span class="txt" @click="onCommon">下载</span>
          </li>
        </ul>
      </div>
    </el-main>

    <!-- 登录 -->
    <el-dialog
      draggable
      width="400"
      v-model="dialogVisible"
      :destroy-on-close="true"
      :close-on-click-modal="false"
    >
      <div class="dialog-login">
        <div class="dialog-head">
          <h2 class="dialog-h2">欢迎登录</h2>
          <span>台账数据库系统</span>
        </div>
        <el-form
          ref="formRef"
          :size="formSize"
          :model="formData"
          status-icon
          :rules="rules"
        >
          <el-form-item prop="username">
            <el-input
              v-model="formData.username"
              :prefix-icon="User"
              size="large"
              placeholder="请输入账号"
              @input="
                formData.username = formData.username.replace(
                  /[\u4E00-\u9FA5]/g,
                  ''
                )
              "
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="formData.password"
              type="password"
              size="large"
              placeholder="请输入密码"
              show-password
              :prefix-icon="Lock"
              @input="
                formData.password = formData.password.replace(
                  /[\u4E00-\u9FA5]/g,
                  ''
                )
              "
              @keyup.enter.native="submitForm(formRef)"
            />
          </el-form-item>
          <el-form-item>
            <div class="login-but">
              <el-button
                type="primary"
                size="large"
                :disabled="btnVisible"
                @click="submitForm(formRef)"
              >
                <el-icon class="is-loading" v-show="btnVisible">
                  <Loading />
                </el-icon>
                登 录
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, toRaw } from "vue";
import {
  ComponentSize,
  ElMessage,
  FormInstance,
  FormRules,
} from "element-plus";
import { useRoute, useRouter } from "vue-router";
import {
  Lock,
  User,
  UserFilled,
  SwitchButton,
  Loading,
} from "@element-plus/icons-vue";
import { $Login } from "@/api/login";
import { useUserStore } from "@/stort/user";
import { formatDate } from "@/utils/other";
import { $queryShareList } from "@/api/sharelink";
import { $onDownload } from "@/api/document";
import { Prompt } from "@/common/enum";
import { FeaturePar, TableData } from "@/interface/document";
import { UserPar } from "@/interface/login";
import SvgIcon from "@/components/SvgIcon/index.vue";

const { VITE_FRAGMENT_DOWNLOAD_UPLOAD } = import.meta.env;
// 持久化存储
const userStore = useUserStore();
// 路由
const router = useRouter();
// 获取浏览器上的地址
const route = useRoute();

// 登录
const btnVisible = ref<boolean>(false);
const userName = ref<string>("");
const isShow = ref<boolean>(false);
const dialogVisible = ref<boolean>(false);
const formSize = ref<ComponentSize>("default");
const formRef = ref<FormInstance>();
const formData = reactive<UserPar>({
  username: "",
  password: "",
});
// 验证对象
const rules = reactive<FormRules<typeof formData>>({
  username: [
    { required: true, message: "请输入账号", trigger: "blur" },
    { min: 1, max: 18, message: "账号最长为18位", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 1, max: 18, message: "密码最长为18位", trigger: "blur" },
  ],
});

// 右击菜单显示、隐藏
const visible = ref<boolean>(false);
// 加载loading
const tableLoading = ref<boolean>(false);
// table
const multipleTableRef = ref();
// table值
const shareTable = ref<TableData[]>([]);
// 分享人
const name = ref<string>("");
// 文件查询总数
const total = ref<number>(0);
// 获取列表勾选多少个文件
const sum = ref<number>(0);
// 坐标
const coordinate = reactive({
  top: 0,
  left: 0,
});
// 页眉
const breadcrumb = ref<FeaturePar[]>([]);

/**
 * 查询列表 入参
 * @md5Value md5
 * @parentId 文件夹id，即fileId, 首次查询分享列表时，该字段为null
 */
const shareList = reactive({
  md5Value: "",
  parentId: "",
});

// 下载入参
const feature = reactive({
  fileParam: [] as any[],
});

onMounted(() => {
  init();
});

/**
 * 初始化用户是否登录状态
 */
const init = () => {
  breadcrumb.value.length = 0;

  // 打开新窗口 获取登录用户信息
  const userInfoJson = sessionStorage.getItem("user_Info");
  // 如果存在，解析用户信息
  if (userInfoJson) {
    shareList.md5Value = route.path.split("/").pop()!;
    shareList.parentId = "";
    const userInfo = JSON.parse(userInfoJson);
    userName.value = userInfo.displayName;
    // 判断token存不存在
    if (userInfo.token) {
      queryList();
    }
  } else {
    userName.value = "";
    name.value = "";
    shareTable.value.length = 0;
    dialogVisible.value = true;
  }
};

/**
 * 点击login事件
 */
const onLoginImg = () => {
  if (userName.value) {
    router.push("/index");
  } else {
    router.push("/");
  }
};

/**
 * 点击登录事件
 */
const onLogin = () => {
  dialogVisible.value = true;
};

// 登录提交表单
const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.validate(async (valid) => {
    // 调用接口打开
    if (valid) {
      btnVisible.value = true;
      await $Login(formData)
        .then((res: boolean) => {
          if (res) {
            isShow.value = false;
            dialogVisible.value = false;
            init();
          }
        })
        .catch(() => {})
        .finally(() => {
          btnVisible.value = false;
        });
    }
  });
};

/**
 * 查新列表接口
 */
const queryList = () => {
  feature.fileParam.length = 0;
  tableLoading.value = true;

  $queryShareList(shareList)
    .then((res: any) => {
      shareTable.value = res.data;
      total.value = res.data.length;
      if (res.data.length > 0) {
        name.value = res.data[0].createBy;
      }
    })
    .catch((err) => {
      // 处理错误
      console.log("查询列表失败：", err);
    })
    .finally(() => {
      // 不管成功还是失败，都会执行这里的代码
      tableLoading.value = false;
    });
};

/**
 * 下载
 */
const onCommon = () => {
  $onDownload(VITE_FRAGMENT_DOWNLOAD_UPLOAD, feature.fileParam, sum.value);
};

// 点击文件夹可查询
const onHierarchy = (type: string, fileId: string, fileName: string) => {
  if (type === null) {
    breadcrumb.value.push({
      id: Math.floor(Math.random() * 100 + 1),
      fileId: fileId,
      fileName: fileName,
      fileType: "",
    });
    shareList.parentId = fileId!;
    queryList();
  }
};

/**
 * 点击页头查询
 */
const onPageHeader = (id?: number, fileId?: string) => {
  shareList.parentId = fileId!;
  // 删除id的项后面的所有项
  const index = breadcrumb.value.findIndex((item) => item["id"] === id);
  if (index !== -1) {
    breadcrumb.value.splice(index + 1, breadcrumb.value.length - index - 1);
  }
  queryList();
};

/**
 * table
 * 全选
 */
const handleSelectAll = (selection: any) => {
  // 清空功能操作入参
  feature.fileParam.length = 0;
  handleCommon(selection);
};

/**
 * 单选 table数据
 * 勾选某一条数据 Checkbox 时触发的事件
 */
const handleSelect = (selection: any) => {
  // 清空功能操作入参
  feature.fileParam.length = 0;
  closeMenu();
  handleCommon(selection);
};
const handleCommon = (selection: any) => {
  let selectionValue: string | any[] = [];
  // 功能操作入参
  let obj = {};
  selection.forEach((res: any) => {
    selectionValue.push(toRaw(res));

    obj = {
      fileId: res.fileId,
      fileName: res.fileName,
      fileType: res.fileType !== null ? res.fileType : "file",
      size: res.fileSize,
    };
    feature.fileParam.push(obj);
  });
  sum.value = selection.length;
};

/**
 * table
 * 选中一条数据（关闭右键菜单）操作
 */
const handleRowClick = (row: any) => {
  clearCheckbox();

  // 功能操作入参
  const obj = {
    fileId: row.fileId,
    fileName: row.fileName,
    fileType: row.fileType !== null ? row.fileType : "file",
    size: row.fileSize,
  };
  feature.fileParam.push(obj);
  sum.value = 1;

  const rowValue = toRaw(row);
  multipleTableRef.value.toggleRowSelection(rowValue);
};

/**
 * table
 * 某一行右击事件
 */
const rowContextmenu = (row: any, _column: any, event: any) => {
  // 右击菜单显示、隐藏
  visible.value = true;
  // 清空功能操作入参
  feature.fileParam.length = 0;

  multipleTableRef.value!.setCurrentRow(row); // 右键莫一行高亮

  handleRowClick(row); // 选中一条数据（关闭右键菜单）

  // table 右击弹出操作框
  const clickX = event.pageX; //获取浏览器页面的x坐标
  const clickY = event.pageY; //获取浏览器页面的y坐标

  const windowWidth = window.innerWidth;
  const windowHeight = window.innerHeight;
  const menuWidth = 120; // 假设弹窗宽度为320px
  const menuHeight = 100; // 假设弹窗高度为100px

  // 计算弹窗的位置，使其不会超出窗口边界
  const left = Math.min(clickX, windowWidth - menuWidth);
  const top = Math.min(clickY, windowHeight - menuHeight);
  coordinate.top = top;
  coordinate.left = left;

  contextmenuPrevent();
};

// table 中右击，禁止浏览器弹窗 弹出自定义窗口
const contextmenuPrevent = () => {};

// 清空多选
const clearCheckbox = () => {
  feature.fileParam.length = 0; // 清空fileParam入参
  multipleTableRef.value.clearSelection();
};

// table 关闭右击菜单
const closeMenu = () => {
  visible.value = false;
};

// 鼠标移入/移出事件
const onMouse = (boo: boolean) => {
  isShow.value = boo;
};

// 退出登录
const exit = () => {
  ElMessage({
    message: Prompt.LOG_OUT,
    type: "success",
  });
  userName.value = "";
  name.value = "";
  shareTable.value.length = 0;
  userStore.clearUser();
};
</script>

<style scoped lang="less">
::v-deep(.el-header) {
  background: #f5f8fa;
}
::v-deep(.el-main) {
  padding: 0px 20px;
}
// 修改table组件样式
::v-deep(.el-table tr) {
  height: 56px;
}
::v-deep(.el-table td.el-table__cell div) {
  height: 55px;
  line-height: 56px;
}
::v-deep(.el-table .el-table__cell) {
  padding: 0;
}
// table组件 多选 selection样式
::v-deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner::before) {
  top: 7px;
}
::v-deep(.el-checkbox) {
  --el-checkbox-input-height: 18px;
  --el-checkbox-input-width: 18px;
  --el-checkbox-checked-bg-color: var(--el-color-white);
  --el-checkbox-checked-icon-color: var(--el-color-primary);
}
::v-deep(.el-checkbox__inner::after) {
  width: 5px;
  height: 10px;
  left: 5px;
  top: 0px;
  border: 2px solid var(--el-checkbox-checked-icon-color);
  border-left: 0;
  border-top: 0;
}
::v-deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner::before) {
  height: 3px;
}
.layout-header-inner {
  max-width: 1080px;
  height: 60px;
  line-height: 60px;
  padding: 0;
  margin: 0 auto;
  .logo {
    float: left;
    cursor: pointer;
    .logo-svg {
      float: left;
      width: 40px;
      height: 40px;
      margin: 11px 16px 0 0px;
    }
  }
  .mod-act-group {
    float: right;
    cursor: pointer;
    font-size: 14px;
    .index-avatar {
      width: 30px;
      height: 30px;
      position: relative;
      top: 4px;
      left: -10px;
    }

    ul {
      width: 140px;
      padding: 10px;
      border-radius: 4px;
      position: absolute;
      z-index: 200;
      background-color: #ffffff;
      border: 1px solid rgba(0, 0, 0, 0.08);
      box-shadow: 0px 5px 12px 4px rgba(0, 0, 0, 0.08);
      li {
        height: 40px;
        line-height: 40px;
        text-align: center;
        .li-icon {
          top: 4px;
        }
      }
      li:hover {
        color: #409eff;
        border-radius: 4px;
        background-color: #f4f4f4;
      }
    }
  }
}

.layout-main {
  max-width: 1080px;
  padding: 0;
  margin: 0 auto;
  .mod-action-list {
    float: right;
    width: 20%;
    height: 64px;
    .mod-action-wrap {
      float: right;
      margin: 14px 16px 10px 0;
    }
  }

  .mod-breadcrumb {
    overflow: hidden;
    width: 80%;
    height: 64px;
    line-height: 64px;
    font-size: 14px;
    ul {
      li {
        color: #000;
        font-weight: 700;
        text-decoration: none;
        span {
          margin-right: 5px;
        }
      }
    }
  }

  // table
  .icon {
    width: 40px;
    height: 40px;
    position: absolute;
    top: 8px;
    cursor: pointer;
  }
  .table-on-hover {
    color: #020202;
    cursor: pointer;
    padding-left: 50px;
    position: absolute;
    text-decoration: none; /* 默认情况下不显示下划线 */
  }
  .table-on-hover:hover {
    text-decoration: underline; /* 鼠标悬停时显示下划线 */
  }
  .table-color {
    color: #939090;
  }

  .list-group-info {
    text-align: center;
    padding: 28px 0;
    font-size: 13px;
    color: #777;
    .info-con {
      margin: 0 3px;
      font-weight: 400;
    }
  }
}

// 页眉
.page-header {
  max-width: 1080px;
  padding: 0;
  margin: 0 auto;
  font-size: 14px;
  .tablist {
    float: left;
    width: 100%;
    height: 24px;

    .tabul {
      height: 24px;
      padding: 0;
      margin: auto;
      line-height: 24px;

      .tabli {
        float: left;
        height: 24px;
        min-width: 42px;
        line-height: 24px;
        color: rgba(0, 0, 0, 0.64);
        cursor: pointer;
        font-style: normal;
        font-weight: 400;
        padding-right: 10px;
      }
      .tabli:hover {
        color: rgba(0, 0, 0, 0.88);
        font-style: normal;
      }
      .cut {
        color: rgba(0, 0, 0, 0.88);
        font-weight: 600;
      }
      .file-span {
        position: relative;
        top: -1px;
        left: -2px;
      }
      .icon-arrow2 {
        display: inline-block;
        position: relative;
        top: 2px;
      }
      .file-name {
        max-width: 134px;
        display: inline-flex;
        padding: 0 10px;
        cursor: pointer;
        span {
          /*文字超出隐藏变省略号*/
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        }
      }
      .file-name:hover {
        text-decoration: underline; /* 鼠标悬停时显示下划线 */
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
  }
  .list-item:hover .txt {
    background-color: #f4f4f4;
  }
}

// 登录
.dialog-login {
  padding: 24px;
  .dialog-head {
    margin-bottom: 40px;
    .dialog-h2 {
      font-size: 24px;
      display: inline-block;
      padding-right: 10px;
    }
  }

  .login-but {
    width: 100%;
    padding: 16px 0 10px 0;
    .el-button {
      width: 100%;
      font-weight: 600;
    }
  }
}
</style>
