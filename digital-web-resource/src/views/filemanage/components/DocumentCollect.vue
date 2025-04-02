<template>
  <div class="main-content">
    <div class="customTable" @contextmenu.prevent="contextmenuPrevent">
      <!-- 条件查询 -->
      <div class="condition">
        <div style="flex: 1" @click="closeMore">
          <div class="customtabs">
            <div class="tablist">
              <ul class="tabul">
                <li class="tabli">
                  <span>收藏夹</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
        <!-- 操作 下载 分享 删除 -->
        <FeatureComponents
          :feature="feature"
          :visible="visible"
          @child-event="onRename"
        />
        <!-- 搜索查询 -->
        <div class="search">
          <div class="search-style">
            <input
              v-model="searchName"
              placeholder="搜索"
              class="input-search"
              @change="queryList"
            />
            <Search class="img-search" @click="queryList" />
          </div>
          <div class="thumb">
            <SvgIcon
              :name="isActive ? 'icon-list' : 'icon-thumb'"
              class="icon-img"
            ></SvgIcon>
          </div>
        </div>
      </div>

      <!-- table 列表 -->
      <div v-if="isActive" class="main-container-mc">
        <!-- 文档列表 -->
        <div @click="closeMore">
          <el-table
            v-loading="tableLoading"
            :data="tableData"
            ref="multipleTableRef"
            tooltip-effect="dark"
            style="width: 100%"
            height="640"
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

                <span class="table-hover" :title="scope.row.fileName" v-else>
                  {{ scope.row.fileName }}
                </span>
              </template>
            </el-table-column>
            <el-table-column
              prop="createBy"
              label="上传者"
              sortable
              width="180"
            >
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, toRaw } from "vue";
import SvgIcon from "@/components/SvgIcon/index.vue";
import { Search } from "@element-plus/icons-vue";
import eventBus from "@/common/eventBus";
import { formatDate } from "@/utils/other";
import FeatureComponents from "@/components/FileTable/Feature.vue";
import { $listData, $renameFile } from "@/api/document";
import { Deldoc } from "@/common/enum";
import { TableData } from "@/interface/document";
import { useUserStore } from "@/stort/user";
import { useRoute } from "vue-router";
import { PagingPar } from "@/interface/common";

const route = useRoute();
const userStore = useUserStore();

// 搜索参数
const searchName = ref<string>("");

// 右击菜单显示、隐藏
const visible = ref<boolean>(false);

/**
 * 功能操作参数
 * @collectStatus 更多切换
 * @rootNode 根节切换类型
 * @cutType  功能切换类型
 * @numLength checkbox多选中个数
 * @top   坐标
 * @left  坐标
 * @isFile 回收、收藏 出现文件夹隐藏按钮
 * @status 判断是 0：未收藏、 1：已收藏 状态
 * @permission 用户权限
 * 入参 fileParam[]
 * @fileId 文档id
 * @fileName 文档名称
 * @fileType 文档后缀类型
 * @routesign 区分是不是收藏夹发出的请求
 */
const feature = reactive({
  collectStatus: false,
  rootNode: "collect",
  cutType: "collect",
  numLength: 0,
  top: 0,
  left: 0,
  isFile: true,
  status: 1,
  permission: "",
  fileParam: [] as any[],
});

// 重命名
const showEdit = ref<string>("");
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

onMounted(() => {
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
  feature.permission = roleCode;

  queryList();
});

/**
 * DocumentUpload.vue
 * 调用列表接口
 */
eventBus.on("onEventBus", (param: any) => {
  if (route.path === "/document/collect") {
    const index = tableData.value.findIndex(
      (item) => item.fileId === param.fileId
    );
    // 更新table取消收藏值
    if (Array.isArray(param)) {
      if (param[0].collectFlag == 0) {
        /**
         * 更新table收藏、取消收藏值
         * collectFlag: 0（收藏） 1（取消收藏）
         */
        for (let i = 0; i < param.length; i++) {
          const id = param[i].fileId;
          const index = tableData.value.findIndex((item) => item.fileId === id);

          // 判断是 0：未收藏、 1：已收藏 状态
          tableData.value.splice(index, 1);
        }
      }
    }

    // 更新table重命名
    if (param.status === "rename") {
      tableData.value[index].fileName = param.fileName;
      return;
    }

    feature.numLength = 0; // table checkbox选中个数
    feature.fileParam.length = 0;
  }
});

/**
 * 查询列表数据
 */
const queryList = () => {
  // 清空功能操作入参
  feature.numLength = 0; // table checkbox选中个数
  feature.fileParam.length = 0;

  tableLoading.value = true;
  const url = `/api/doc/query/collectFiles/${paging.currentPage}/${paging.pageSize}`;
  const obj = {
    delFlag: Deldoc.NORMAL,
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
    });
};

/**
 * table 全选
 */
const handleSelectAll = (selection: any) => {
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
};
const handleCommon = (selection: any, name: string) => {
  let selectionValue: string | any[] = [];

  // 功能操作入参
  let obj = {};
  selection.forEach((res: any) => {
    selectionValue.push(toRaw(res));

    obj = {
      fileId: res.fileId,
      fileName: res.fileName,
      fileType: res.fileType,
    };
    feature.fileParam.push(obj);
  });

  feature.numLength = selectionValue.length;
  // 关闭更多菜单
  if (name == "radio") {
    selectionValue.length === 0 ? closeMore() : "";
  }
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

  // 功能操作入参
  const obj = {
    fileId: row.fileId,
    fileName: row.fileName,
    fileType: row.fileType,
  };
  feature.fileParam.push(obj);
  feature.numLength = 1;

  const rowValue = toRaw(row);
  multipleTableRef.value.toggleRowSelection(rowValue);
};

/**
 * table
 * 某一行右击事件
 * closeMore() 关闭更多菜单
 * handleRowClick() 选中一条数据（关闭右键菜单）
 */
const rowContextmenu = (row: any, _column: any, event: any) => {
  // 右击菜单显示、隐藏
  visible.value = true;
  // 清空功能操作入参
  feature.fileParam.length = 0;
  // 右键莫一行高亮
  multipleTableRef.value!.setCurrentRow(row);

  closeMore();
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
  let params = { fileId: id, fileName: name };
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
 */
const closeMore = () => {
  feature.collectStatus = false;
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
  // 条件查询
  .condition {
    display: flex;
    padding-right: 10px;
    // 全部文档 DCO XLS PPT  PDF
    .customtabs {
      height: 48px;

      .tablist {
        float: left;

        .tabul {
          height: 48px;
          padding: 0;
          margin: auto;
          line-height: 48px;

          .tabli {
            float: left;
            height: 48px;
            min-width: 64px;
            line-height: 48px;
            color: rgba(0, 0, 0, 0.88);
            cursor: pointer;
            font-size: 16px;
            font-style: normal;
            font-weight: 600;
            padding: 0px 10px 0px 10px;
          }

          .cut {
            color: rgba(0, 0, 0, 0.88);
            font-weight: 600;
          }
          .icon-arrow2 {
            width: 16px;
            height: 16px;
            display: inline-block;
            position: relative;
            top: 2px;
          }
          .file-name {
            cursor: pointer;
          }
          .file-name:hover {
            text-decoration: underline; /* 鼠标悬停时显示下划线 */
          }
        }
      }

      .tabline {
        position: relative;
        width: 56px;
        height: 3px;
        background: rgba(0, 0, 0, 0.88);
        -webkit-transition: all 0.15s ease-in;
        transition: all 0.15s ease-in;
        top: 42px;
      }
    }

    // 搜索查询
    .search {
      min-width: 240px;
      max-width: 400px;
      height: 48px;
      line-height: 48px;
      margin-left: 26px;
      display: flex;

      .search-style {
        float: left;
        height: 48px;
        line-height: 41px;
        text-align: end;
        flex: 1 1 0%;

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
          right: 96px;
          top: 110px;
          color: #cdd0d6;
        }
        .img-search:hover {
          color: #70b1f1;
          cursor: pointer;
        }
      }

      .thumb {
        width: 24px;
        height: 24px;
        cursor: pointer;
        display: inline-block;
        position: relative;
        top: 12px;
        margin: 0 6px 0 10px;
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
