<template>
  <div class="customTable" @contextmenu.prevent="contextmenuPrevent">
    <!-- 条件查询 -->
    <div class="condition">
      <div style="flex: 1">
        <div class="customtabs">
          <div class="tablist">
            <ul class="tabul">
              <li class="tabli">
                <span>个人标签</span>
              </li>
            </ul>
          </div>
        </div>
        <div class="tab-list">
          <div v-show="breadcrumb.length > 0">
            <span class="tab-span" @click="onPageHeader(0, '')">全部</span>
            <span
              v-for="(item, index) in breadcrumb"
              :key="item.id"
              class="tab-span"
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
                <span class="tab-name" :title="item.fileName">{{
                  item.fileName
                }}</span>
              </a>
            </span>
          </div>
        </div>
      </div>
      <!-- 操作 下载 分享 删除 -->
      <FeatureComponents :feature="feature" :visible="visible" />
      <!-- 搜索查询 -->
      <div class="search">
        <div class="search-style">
          <input
            v-model="tagObj.fileName"
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
      <el-table
        v-loading="tableLoading"
        :data="tableData"
        ref="multipleTableRef"
        tooltip-effect="dark"
        style="width: 100%"
        height="628"
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
            <div class="table-hover">
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
import { ref, reactive, watch, onMounted, toRaw } from "vue";
import SvgIcon from "@/components/SvgIcon/index.vue";
import { Search } from "@element-plus/icons-vue";
import eventBus from "@/common/eventBus";
import { formatDate } from "@/utils/other";
import FeatureComponents from "@/components/FileTable/Feature.vue";
import { FeaturePar, TableData } from "@/interface/document";
import { useRoute } from "vue-router";
import { PagingPar } from "@/interface/common";
import { $tagListData } from "@/api/addtag";
// 获取浏览器上的地址
const route = useRoute();

// 右击菜单显示、隐藏
const visible = ref<boolean>(false);

/**
 * 功能操作参数
 * @tagStatus 个人标签列表
 * @rootNode 根节切换类型
 * @cutType  功能切换类型
 * @numLength checkbox多选中个数
 * @top   坐标
 * @left  坐标
 * 入参 fileParam[]
 * @fileId 文档id
 * @fileName 文档名称
 * @fileType 文档后缀类型
 * @routesign 区分是不是收藏夹发出的请求
 */
const feature = reactive({
  tagStatus: "list",
  rootNode: "tag",
  cutType: "tag",
  numLength: 0,
  top: 0,
  left: 0,
  fileParam: [] as any[],
});

// 页眉
const breadcrumb = ref<FeaturePar[]>([]);
// 判断是否点击了文件夹
const isClick = ref<boolean>(true);

/**
 * 查询列表 入参
 * @id 个人标签ID
 * @parentId  文件夹id
 * @fileName 模糊搜索
 * @folderFlag 文件夹标识 1：文件夹
 */
const tagObj = reactive({
  id: "",
  parentId: "",
  fileName: "",
});

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

// 监听右键菜单
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

// 监听路由变化
watch(
  () => route.fullPath,
  (newPath) => {
    if (newPath) {
      // 清空页眉
      breadcrumb.value.length = 0;
      // 清空列表入参
      tagObj.parentId = "";
      tagObj.fileName = "";
      tagObj.id = route.path.split("/").pop()!;
      queryList();
    }
  }
);

onMounted(() => {
  init();
});

/**
 * Feature.vue
 * 调用列表接口
 */
eventBus.on("tagEventBus", (param: any) => {
  // 更新table取消收藏值
  for (let i = 0; i < param.length; i++) {
    const id = param[i];
    const index = tableData.value.findIndex((item) => item.fileId === id);
    tableData.value.splice(index, 1);
  }
});
// 初始化
const init = () => {
  tagObj.id = route.path.split("/").pop()!;
  queryList();
};

/**
 * 查询列表数据
 * @id 标签ID
 * @parentId 文件夹id
 * @fileName 文件夹名称
 */
const queryList = () => {
  // 清空功能操作入参
  feature.fileParam.length = 0;

  tableLoading.value = true;
  const url = `/api/doc/tag/findFilesByTagId/${paging.currentPage}/${paging.pageSize}`;
  const obj = {
    id: tagObj.id,
    parentId: tagObj.parentId,
    fileName: tagObj.fileName,
  };
  // 查询列表接口
  $tagListData(url, obj)
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
 * 点击文件夹可查询
 */
const onHierarchy = (type: string, fileId: string, fileName: string) => {
  tagObj.parentId = fileId;
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
  tagObj.fileName = ""; // 清空搜索框数据
  // 删除id的项后面的所有项
  if (fileId) {
    const index = breadcrumb.value.findIndex((item) => item["id"] === id);
    if (index !== -1) {
      breadcrumb.value.splice(index + 1, breadcrumb.value.length - index - 1);
    }
  } else {
    breadcrumb.value.length = 0; // 清空页眉
  }
  tagObj.parentId = fileId!;
  queryList();
};

/**
 * table 全选
 */
const handleSelectAll = (selection: any) => {
  handleCommon(selection);
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
      fileType: res.fileType,
    };
    feature.fileParam.push(obj);
  });

  feature.numLength = selectionValue.length;
};

/**
 * table 单选
 * 选中一条数据（关闭右键菜单）操作
 * clearCheckbox() 清空多选
 */
const handleRowClick = (row: any) => {
  clearCheckbox();
  // 功能操作入参
  const obj = {
    fileId: row.fileId,
    fileName: row.fileName,
    fileType: row.fileType,
  };
  feature.fileParam.push(obj);
  if (isClick.value) {
    feature.numLength = 1;
  }
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
 * table 中右击，禁止浏览器弹窗 弹出自定义窗口
 * 显示菜单
 */
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

    .tab-list {
      height: 20px;
      float: left;
      font-size: 14px;
      .tab-span {
        float: left;
        height: 20px;
        min-width: 60px;
        line-height: 20px;
        cursor: pointer;
        font-style: normal;
        padding: 0px 10px 0px 10px;
      }

      .icon-arrow2 {
        width: 16px;
        height: 16px;
        display: inline-block;
        position: relative;
        top: 2px;
        left: -15px;
      }
      .file-name {
        cursor: pointer;
        .tab-name {
          float: right;
          width: 120px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
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
          right: 92px;
          top: 106px;
          color: #cdd0d6;
        }
        .img-search:hover {
          color: #70b1f1;
          cursor: pointer;
        }
      }

      .thumb {
        width: 30px;
        height: 24px;
        cursor: pointer;
        display: inline-block;
        position: relative;
        top: 12px;
        margin-left: 10px;
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
    margin: 40px 0 20px 0;
  }
}
</style>
