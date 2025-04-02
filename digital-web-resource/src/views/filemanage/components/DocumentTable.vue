<template>
  <div class="customTable" @contextmenu.prevent="contextmenuPrevent">
    <div style="display: flex">
      <div style="width: 228px">
        <div v-permission="`${feature.permission}`">
          <DocumentUploadComponent
            :fileID="listObj.parentId"
            :ownership="listObj.ownership"
            :uploadpull="feature"
            :deptID="listObj.deptId"
          />
        </div>
      </div>
      <div class="search-style" @click="closeMore">
        <input
          v-model="listObj.searchName"
          placeholder="搜索"
          class="input-search"
          @change="queryList"
        />
        <Search class="img-search" @click="queryList" />
      </div>
    </div>
    <!-- 条件查询 -->
    <div class="condition">
      <div style="flex: 1" @click="closeMore">
        <div class="customtabs">
          <div class="tablist">
            <ul class="tabul">
              <li
                :class="['tabli', breadcrumb.length == 0 ? 'cut' : '']"
                @click="onTab('all')"
              >
                <span>全部</span>
              </li>
              <span
                v-show="breadcrumb.length > 0"
                v-for="(item, index) in breadcrumb"
                :key="item.id"
                class="file-span"
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
                  <span class="tab-name" :title="item.fileName">
                    {{ item.fileName }}
                  </span>
                </a>
              </span>
            </ul>
          </div>
        </div>
      </div>
      <!-- 操作 下载 分享 删除 -->
      <FeatureComponents
        :feature="feature"
        :transfer="transfer"
        :visible="visible"
        @child-event="onRename"
      />
      <!-- 缩略图 -->
      <div class="thumb-center" @click="closeMore">
        <div class="thumb">
          <el-tooltip
            effect="dark"
            :show-arrow="false"
            :content="isActive ? '列表' : '缩略图'"
            placement="bottom"
          >
            <SvgIcon
              :name="isActive ? 'icon-list' : 'icon-thumb'"
              class="icon-img"
            ></SvgIcon>
          </el-tooltip>
        </div>
      </div>
    </div>

    <!-- table 列表 -->
    <div v-if="isActive" class="main-container-mc" @click="closeMore">
      <!-- 文档列表 -->
      <div>
        <el-table
          v-loading="tableLoading"
          :data="tableData"
          ref="multipleTableRef"
          tooltip-effect="dark"
          style="width: 100%"
          height="600"
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
              <el-input
                size="small"
                @blur="handleBlur(scope.row.fileId, scope.row.fileName)"
                v-if="scope.row.fileId === showEdit"
                v-model="tableData[scope.$index].fileName"
                class="custom-input"
              ></el-input>

              <div class="table-hover" v-else>
                <span
                  @click="
                    onHierarchy(
                      scope.row.fileType,
                      scope.row.fileId,
                      scope.row.fileName
                    )
                  "
                  :title="scope.row.fileName"
                >
                  {{ scope.row.fileName }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createBy" label="上传者" sortable width="180">
            <template #default="scope">
              <span class="table-color">{{ scope.row.createBy }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="fileSizeStr" label="大小" width="180">
            <template #default="scope">
              <span class="table-color">
                {{ scope.row.fileSizeStr ? scope.row.fileSizeStr : "--" }}
              </span>
            </template>
          </el-table-column>
          <el-table-column
            prop="createTime"
            label="上传时间"
            sortable
            width="220"
          >
            <template #default="scope">
              <span class="table-color">
                {{ formatDate(scope.row.createTime) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="paging">
        <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          v-model:current-page="paging.currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="paging.pageSize"
          layout="total, prev, pager, next, sizes"
          :total="paging.total"
          style="float: right"
        ></el-pagination>
      </div>
    </div>

    <!-- 缩略图 -->
    <div v-else>缩略图</div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, toRaw, onUnmounted } from "vue";
import { Search } from "@element-plus/icons-vue";
import { useUserStore } from "@/stort/user";
import eventBus from "@/common/eventBus";
import SvgIcon from "@/components/SvgIcon/index.vue";
import { formatDate } from "@/utils/other";
import DocumentUploadComponent from "./DocumentUpload.vue";
import FeatureComponents from "@/components/FileTable/Feature.vue";
import { $listData, $renameFile } from "@/api/document";
import { Deldoc, Document, Permission } from "@/common/enum";
import { FeaturePar, TableData } from "@/interface/document";
import vPermission from "@/stort/permission";
import { useRoute } from "vue-router";
import { PagingPar } from "@/interface/common";

const route = useRoute();
const userStore = useUserStore();

// 右击菜单显示、隐藏
const visible = ref<boolean>(false);

/**
 * 查询列表入参
 * @parentId 件夹id，根目录为 0
 * @ownership  归属，1：公共，2：用户，3：部门
 * @category 文件分类（全部、文档、图片）
 * @fileType 文档类型(后缀)
 * @delFlag  删除标记，0：删除，1：正常
 * @searchName 搜索参数
 * @deptId  部门id
 */
const listObj = reactive({
  parentId: "0",
  ownership: 2,
  category: "",
  fileType: "",
  delFlag: Deldoc.NORMAL,
  searchName: "",
  deptId: "",
});

/**
 * 转移到 入参
 * @parentId 件夹id，根目录为 0
 * @ownership  归属，1：公共，2：用户，3：部门
 * @category folder: 文件夹
 * @folderFlag 文件夹标识 1：文件夹
 */
const transfer = reactive({
  parentId: "0",
  ownership: 2,
  category: "folder",
  folderFlag: "1",
});

/**
 * 功能操作参数
 * @moreupload 上传按钮 下拉【DocumentUpload.vue】
 * @moreStatus 更多切换
 * @rootNode 根节切换类型
 * @cutType  功能切换类型
 * @numLength checkbox多选中个数
 * @top   坐标
 * @left  坐标
 * @isFile 回收、收藏 出现文件夹隐藏按钮
 * @status 判断是 0：未收藏、 1：已收藏 状态
 * @permission 用户权限
 * @ownership  归属，1：公共，2：用户，3：部门
 * 入参 fileParam[]
 * @fileId 文档id
 * @fileName 文档名称
 * @fileType 文档后缀类型
 * @size 文件大小
 */
const feature = reactive({
  moreupload: false,
  moreStatus: false,
  rootNode: "minefile",
  cutType: "all",
  numLength: 0,
  top: 0,
  left: 0,
  isFile: false,
  status: 0,
  permission: "",
  ownership: 2,
  fileParam: [] as any[],
});

// 页眉
const breadcrumb = ref<FeaturePar[]>([]);
// 判断是否点击了文件夹
const isClick = ref<boolean>(true);

// 重命名
const showEdit = ref<string>();
// 重命名临时存储旧的文档名称
const storeFileName = ref<string>();

// table 列表
const tableData = ref<TableData[]>([]);
// 分页
let paging = reactive<PagingPar>({
  currentPage: 1,
  pageSize: 20,
  total: 0,
});

// 缩略图 列表切换
const isActive = ref<boolean>(true);

// 加载loading
const tableLoading = ref<boolean>(false);
const multipleTableRef = ref();

watch(visible, (newValue) => {
  // 监听属性对象，newValue为新的值，也就是改变后的值
  if (newValue) {
    //菜单显示的时候
    document.body.addEventListener("click", closeMenu);
  } else {
    //菜单隐藏的时候
    document.body.removeEventListener("click", closeMenu);
  }
});

/**
 * DocumentUpload.vue
 * 上传文档、文件夹、新增文档夹
 * 调用列表接口
 */
eventBus.on("onEventBus", (param: any) => {
  if (
    route.path === "/document/minefile" ||
    route.path === "/document/commondoc" ||
    route.path === "/document/section"
  ) {
    if (param === "") {
      queryList(); // 初始化查询列表方法
      return;
    }
    // 判断 param返回是对象还是数组
    if (Array.isArray(param)) {
      /**
       * 更新table收藏、取消收藏值
       * collectFlag: 0（收藏） 1（取消收藏）
       */
      for (let i = 0; i < param.length; i++) {
        const id = param[i].fileId;
        const collectFlag = param[i].collectFlag;
        const index = tableData.value.findIndex((item) => item.fileId === id);
        tableData.value[index].collectFlag = collectFlag;
      }
      // 判断是 0：未收藏、 1：已收藏 状态
      feature.status = param[0].collectFlag;
      return;
    } else {
      // 判断为对象
      // 更新table重命名
      const index = tableData.value.findIndex(
        (item) => item.fileId === param.fileId
      );
      if (param.status === "rename") {
        tableData.value[index].fileName = param.fileName;
        return;
      }
    }
  }
});

const useEventBusListener = (
  eventName: string,
  handler: (param: any) => void
) => {
  onMounted(() => {
    eventBus.on(eventName, handler);
  });

  onUnmounted(() => {
    eventBus.off(eventName, handler);
  });
};

const init = (param: {
  parentId: string;
  ownership: number;
  category: string;
  fileType: string;
  deptId: string;
}) => {
  breadcrumb.value.length = 0; // 清空页眉
  tableData.value = []; // 首次加载初始化
  /**
   * 按钮权限判断
   * @userStore 信息持久化
   * 1：为公共文档
   * @SUPER_ADMIN 超级管理员
   */
  const { permissions } = userStore.user;
  let roleCode = "";
  permissions.forEach((res: any) => {
    roleCode = res.roleCode;
  });
  if (param.ownership == 1) {
    feature.permission = Permission.super_admin;
  } else {
    feature.permission = roleCode;
  }

  //查询列表入参 初始化
  listObj.parentId = param.parentId;
  listObj.ownership = param.ownership;
  listObj.category = param.category;
  listObj.fileType = param.fileType;
  listObj.deptId = param.deptId;
  listObj.searchName = "";

  feature.ownership = param.ownership;
  feature.numLength = 0; // table checkbox选中个数

  transfer.ownership = param.ownership;
  queryList();

  closeMore(); // 关闭更多菜单
};
// 使用 Composition API 封装事件监听逻辑
useEventBusListener("treeObj", init);

/**
 * 根据文档条件查询
 */
const onTab = (value: string) => {
  // 点击面包屑全部查全部赋值为 0
  if (
    value === Document.ALL ||
    value === Document.Document ||
    value === Document.PICTURE
  ) {
    breadcrumb.value.length = 0; // 清空页眉
    listObj.parentId = "0";
    listObj.fileType = "";
  }

  // 查询列表入参
  listObj.fileType = value;
  feature.cutType = value;

  queryList();
};

// 点击文件夹可查询
const onHierarchy = (type: string, fileId: string, fileName: string) => {
  listObj.parentId = fileId;
  if (type === null) {
    isClick.value = false;
    feature.numLength = 0;

    breadcrumb.value.push({
      id: Math.floor(Math.random() * 100 + 1),
      fileId: fileId,
      fileName: fileName,
      fileType: "",
    });
    queryList();
    return;
  }
};

/**
 * 点击页头查询
 */
const onPageHeader = (id?: number, fileId?: string) => {
  listObj.searchName = ""; // 情况搜索框数据
  // 删除id的项后面的所有项
  const index = breadcrumb.value.findIndex((item) => item["id"] === id);
  if (index !== -1) {
    breadcrumb.value.splice(index + 1, breadcrumb.value.length - index - 1);
  }
  listObj.parentId = fileId!;
  queryList();
};

/**
 * 查询列表数据
 */
const queryList = () => {
  // 清空功能操作入参
  feature.numLength = 0; // table checkbox选中个数
  feature.fileParam.length = 0;
  tableLoading.value = true;
  const url = `/api/doc/query/childFiles/${paging.currentPage}/${paging.pageSize}`;
  const obj = {
    parentId: listObj.parentId,
    ownership: listObj.ownership,
    category: listObj.category,
    fileType: listObj.fileType == "document" ? "" : listObj.fileType,
    delFlag: listObj.delFlag,
    fileName: listObj.searchName,
    deptId: listObj.deptId,
  };
  // 查询列表接口
  $listData(url, obj)
    .then((res: any) => {
      tableData.value = res.data.records;
      paging.currentPage = res.data.current;
      paging.pageSize = res.data.size;
      paging.total = res.data.total;
    })
    .catch((err) => {
      // 处理错误
      console.log("处理错误信息：", err);
    })
    .finally(() => {
      // 不管成功还是失败，都会执行这里的代码
      tableLoading.value = false;
      // 判断是否点击了文件夹
      isClick.value = true;
    });
};

/**
 * table 全选
 */
const handleSelectAll = (selection: any) => {
  // 清空功能操作入参
  feature.fileParam.length = 0;
  handleCommon(selection, "");
};

/**
 * table 单选/多选
 * 勾选某一条数据 Checkbox 多选框触发事件
 * @closeMenu() table 关闭右击菜单
 */
const handleSelect = (selection: any) => {
  // 清空功能操作入参
  feature.fileParam.length = 0;
  closeMenu();

  handleCommon(selection, "radio");

  // 勾选文件夹不显示收藏
  const bool = hasNullField(selection, "fileType", "");
  bool ? (feature.isFile = false) : (feature.isFile = true);

  // 收藏判断
  const num = hasNullField(selection, "", "collectFlag");
  num ? (feature.status = 0) : (feature.status = 1);
};
const handleCommon = (selection: any, name: string) => {
  let obj = {};
  selection.forEach((res: any) => {
    // file: 文件夹标识
    obj = {
      fileId: res.fileId,
      fileName: res.fileName,
      fileType: res.fileType !== null ? res.fileType : "file",
      size: res.fileSize,
    };
    feature.fileParam.push(obj);
  });
  feature.numLength = selection.length;
  // 关闭更多菜单
  if (name == "radio") {
    selection.length === 0 ? closeMore() : "";
  }
};
// 数组中的fileType字段是否为null
const hasNullField = (array: any, field: string, flag: number | string) => {
  if (field != "") {
    return array.some((item: any) => item[field] === null);
  }
  return array.some((item: any) => item[flag] == 0);
};

/**
 * table 单选
 * 选中一条数据（关闭右键菜单）操作
 * clearCheckbox() 清空多选
 */
const handleRowClick = (row: any) => {
  // fileId 相同则不关闭重命名输入框
  if (showEdit.value !== row.fileId) {
    showEdit.value = "";
  }

  clearCheckbox();

  // 勾选文件夹不显示收藏
  row.fileType === null ? (feature.isFile = false) : (feature.isFile = true);

  // 功能操作入参 file: 文件夹标识
  const obj = {
    fileId: row.fileId,
    fileName: row.fileName,
    fileType: row.fileType !== null ? row.fileType : "file",
    size: row.fileSize,
  };
  feature.fileParam.push(obj);

  if (isClick.value) {
    feature.numLength = 1;
  }

  // 判断是否未收藏、已收藏
  feature.status = row.collectFlag;

  const rowValue = toRaw(row);
  multipleTableRef.value.toggleRowSelection(rowValue);
};

/**
 * table
 * 某一行右击事件
 * handleRowClick() 选中一条数据（关闭右键菜单）
 */
const rowContextmenu = (row: any, _column: any, event: any) => {
  // 右击菜单显示、隐藏
  visible.value = true;
  // 清空功能操作入参
  feature.fileParam.length = 0;
  // 右键莫一行高亮
  multipleTableRef.value!.setCurrentRow(row);

  handleRowClick(row);

  // table 右击弹出操作框
  const clickX = event.pageX; //获取浏览器页面的x坐标
  const clickY = event.pageY; //获取浏览器页面的y坐标

  const windowWidth = window.innerWidth;
  const windowHeight = window.innerHeight;
  const menuWidth = 120; // 假设弹窗宽度为320px
  const menuHeight = 400; // 假设弹窗高度为400px

  // 计算弹窗的位置，使其不会超出窗口边界
  const left = Math.min(clickX, windowWidth - menuWidth);
  const top = Math.min(clickY, windowHeight - menuHeight);
  feature.top = top;
  feature.left = left;

  contextmenuPrevent();
  closeMore(); // 关闭更多菜单
};

// 分页
const handleCurrentChange = (val: number) => {
  paging.currentPage = val;
  queryList();
};

// 每页显示多少条
const handleSizeChange = (val: number) => {
  paging.pageSize = val;
  queryList();
};

/**
 * 重命名
 */
const onRename = (fileId: string) => {
  showEdit.value = fileId;
  const index = tableData.value.findIndex((item) => item.fileId === fileId);
  storeFileName.value = tableData.value[index].fileName;
};

/**
 * 重命名
 * 调用接口
 */
const handleBlur = (id: number, name: string) => {
  showEdit.value = "";
  // 文件名相同 则不调用重命名方法
  if (name === storeFileName.value) {
    return;
  }
  const params = { fileId: id, fileName: name };
  $renameFile(params); // 调用接口
};

/**
 * table 中右击，禁止浏览器弹窗 弹出自定义窗口
 * 显示菜单
 */
const contextmenuPrevent = () => {};
// 清空多选
const clearCheckbox = () => {
  feature.fileParam.length = 0; // 清空fileParam入参
  multipleTableRef.value.clearSelection();
};
/**
 * 调用Feature.vue子组件中的方法
 * @moreStatus 关闭更多菜单
 * @moreupload 上传按钮 下拉
 */
const closeMore = () => {
  feature.moreStatus = false;
  feature.moreupload = false;
};

// table 关闭右击菜单
const closeMenu = () => {
  visible.value = false;
};
</script>

<style scoped lang="less">
// 修改table组件样式
::v-deep(.el-table tr) {
  height: 56px;
}
::v-deep(.el-table .el-table__cell) {
  padding: 0;
}
::v-deep(.el-loading-mask) {
  z-index: 20;
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

// el-input
::v-deep(.custom-input .el-input__inner) {
  height: 22px;
  line-height: 21px;
  padding: 0;
  border: 1px solid #409eff;
}
::v-deep(.el-input--small .el-input__wrapper) {
  padding: 0;
}

.customTable {
  margin-bottom: 20px;
  .search-style {
    flex: 1 1 0%;
    height: 48px;
    line-height: 41px;
    padding-right: 18px;
    text-align: end;

    input::placeholder {
      font-size: 12px;
      color: #909090; /* 将颜色代码修改为需要的颜色 */
    }

    .input-search {
      width: 200px;
      height: 36px;
      border-radius: 22px;
      transition: width 0.5s;
      border: 1px solid #e4e7ed;
      padding: 0 14px;
    }
    .input-search:focus {
      width: 300px; /* 聚焦时宽度 */
      height: 36px;
      border: 1px solid #70b1f1;
    }
    .img-search {
      width: 18px;
      height: 18px;
      margin-right: 8px;
      position: absolute;
      z-index: 1;
      right: 58px;
      top: 106px;
      color: #cdd0d6;
    }
    .img-search:hover {
      color: #70b1f1;
      cursor: pointer;
    }
  }
  // 条件查询
  .condition {
    display: flex;
    padding-right: 10px;
    // 全部文档 DCO XLS PPT  PDF
    .customtabs {
      height: 48px;
      font-size: 14px;

      .tablist {
        float: left;

        .tabul {
          height: 48px;
          padding: 0;
          margin: auto;
          line-height: 48px;

          .tabli {
            float: left;
            width: 74px;
            height: 48px;
            min-width: 64px;
            line-height: 48px;
            cursor: pointer;
            font-style: normal;
            font-weight: 400;
            padding: 0px 14px 0px 10px;
            text-align: center;
          }
          .tabli:hover {
            text-decoration: underline;
          }

          .file-span {
            position: relative;
            left: -16px;
          }
          .icon-arrow2 {
            display: inline-block;
          }
          .file-name {
            max-width: 134px;
            display: inline-flex;
            padding: 0 10px;
            cursor: pointer;
            .tab-name {
              /*文字超出隐藏变省略号*/
              overflow: hidden;
              white-space: nowrap;
              text-overflow: ellipsis;
            }
          }
          .file-name:hover {
            text-decoration: underline; /* 鼠标悬停时显示下划线 */
          }
          .cut {
            color: rgba(0, 0, 0, 0.88);
            font-weight: 600;
          }
        }
      }
    }

    // 搜索查询
    .thumb-center {
      width: 40px;
      height: 48px;
      line-height: 48px;
      margin-left: 0px;
      .thumb {
        width: 24px;
        height: 24px;
        cursor: pointer;
        display: inline-block;
        position: relative;
        top: 8px;
        left: 10px;
      }
      .thumb:hover {
        background-color: #f4f4f4;
      }
      .icon-img {
        position: absolute;
        left: 3px;
        top: 3px;
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
  .custom-input {
    width: 80%;
    padding-left: 50px;
  }

  .table-hover {
    max-width: 90%;
    display: block;
    color: #020202;
    cursor: pointer;
    padding-left: 50px;
    text-decoration: none; /* 默认情况下不显示下划线 */
    /*文字超出隐藏变省略号*/
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
  .table-hover:hover {
    cursor: pointer;
    text-decoration: underline; /* 鼠标悬停时显示下划线 */
  }

  .table-name {
    width: 85%;
    height: 55px;
    margin-left: 50px;
    padding: 5px 0;
    .table-title {
      width: 100%;
      height: 24px;
    }
    .table-link {
      width: 100%;
      height: 24px;
      font-size: 12px;
      .a {
        color: #3b93ff;
        text-decoration: underline;
      }
    }
  }
  .table-color {
    color: #939090;
    line-height: 55px;
  }

  // 分页
  .paging {
    width: 100%;
    height: 32px;
    margin: 36px 0 20px 0;
  }
}
</style>
