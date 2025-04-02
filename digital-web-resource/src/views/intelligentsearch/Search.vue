<template>
  <!-- 智能搜索 -->
  <el-row>
    <el-col :span="24">
      <div class="main-content" @click.self="toggleDropdown" ref="dropdown">
        <el-affix
          :offset="60"
          :style="['background: #fff', 'height:' + styleHeight]"
        >
          <div style="background-color: #ffffff">
            <div class="search-img">
              <span>智能 <img src="../../image/wit-search.png" /> 搜索</span>
            </div>
            <!-- 简单搜索模块 -->
            <div class="search-box" v-if="common.isAdvanced === 1">
              <BasicSearchComponents
                :status="status"
                @send-switch="onSendSwitch"
                @tag-search="onAdvancedSearch"
                @simple-search="querySearch"
              />
            </div>

            <!-- 高级搜索模块 -->
            <div class="advanced-search-box" v-else>
              <AdvancedSearchComponents
                :advancedSearch="advancedSearch"
                @send-switch="onSendSwitch"
                @send-back="onAdvancedSearch"
                @searchClick="searchClick"
              />
            </div>

            <div class="secrch-condition">
              <span class="secrch-num">
                <el-checkbox
                  v-model="checkAll"
                  :indeterminate="isIndeterminate"
                  @change="handleCheckAll"
                  class="custom-size"
                />

                <span class="custom-tips">
                  <span>批量下载：</span>
                  <span title="批量下载">
                    <SvgIcon
                      name="icon-bulk-download"
                      width="22px"
                      height="22px"
                      class="svg-icon"
                      @click="onDownload()"
                    ></SvgIcon>
                  </span>
                  一共找到文件 {{ paging.total }} 条相匹配数据
                </span>
              </span>

              <!-- 页眉 -->
              <div class="tab-list">
                <div v-show="breadcrumb.length > 0">
                  <span class="tab-all" @click="onPageHeader(0, '')">全部</span>
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
                      <span class="tab-name" :title="item.fileName">
                        {{ item.fileName }}
                      </span>
                    </a>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </el-affix>

        <!-- 列表 -->
        <div
          class="secrch-list"
          v-loading="common.loading"
          element-loading-text="搜索数据加载中..."
          @click="toggleDropdown"
        >
          <div
            class="secrch-list-item"
            v-for="(item, index) in listData"
            :key="index"
          >
            <div class="secrch-list-item-image">
              <div style="float: left; padding-top: 5px; padding-right: 10px">
                <el-checkbox
                  class="custom-size"
                  :value="item.fileId"
                  v-model="item.checked"
                  @change="handleSelect($event, item)"
                />
              </div>
              <div class="icon" v-if="item.icon !== 'icon-nor-m-24'">
                <SvgIcon :name="item.icon" width="50px" height="50px"></SvgIcon>
              </div>
              <div class="icon" v-else>
                <i :class="['icon-svg', 'icon-nor-m-24']"></i>
              </div>
            </div>

            <div class="secrch-list-item-content">
              <ul>
                <li class="title">
                  <a
                    class="title-a"
                    @click="
                      onHierarchy(
                        item.fileType,
                        item.fileId,
                        item.fileName,
                        item.ownership
                      )
                    "
                    :title="item.fileName"
                  >
                    {{ item.fileName }}
                  </a>
                  <!-- <el-tag size="small">自己</el-tag>
                  <el-tag type="warning" size="small">部门</el-tag>
                  <el-tag type="danger" size="small">
                    <el-icon><Lock /></el-icon>
                  </el-tag> -->
                </li>
                <li class="mainbody">
                  <span class="mainbody-avatar">
                    <el-avatar size="small" src=""></el-avatar>
                  </span>
                  <span class="mainbody-name">{{ item.createBy }}</span>
                  <span class="mainbody-time">
                    {{ formatDate(item.createTime) }}
                  </span>
                  <span class="mainbody-size">
                    文件大小：{{
                      item.fileSize ? formatFileSize(item.fileSize) : "--"
                    }}
                  </span>
                  <div class="mainbody-status">
                    <ul>
                      <li>
                        <el-icon><View /></el-icon>
                        <span class="mainbody-num">
                          {{ item.previewCount }}
                        </span>
                      </li>
                      <li>
                        <span
                          v-if="item.fileType != null"
                          class="mainbody-collect"
                          title="收藏"
                          @click="onCollect(item.fileId)"
                        >
                          <el-icon><Star /></el-icon>
                          <span class="mainbody-num">
                            {{ item.collectCount }}
                          </span>
                        </span>
                        <span v-else>
                          <el-icon><Star /></el-icon>
                          <span class="mainbody-forbid">--</span>
                        </span>
                      </li>
                      <li>
                        <span
                          class="mainbody-collect"
                          title="单文件（夹）下载"
                          @click="onDownload(item.fileId)"
                        >
                          <el-icon><Download /></el-icon>
                          <span class="mainbody-num">
                            {{ item.downloadCount }}
                          </span>
                        </span>
                      </li>
                    </ul>
                  </div>
                </li>
              </ul>
            </div>
          </div>
          <el-empty description="暂无搜索信息" v-show="common.isEmpty" />
        </div>

        <!-- 分页 -->
        <div class="paging" v-show="common.isPagin">
          <el-pagination
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
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { View, Star, Download } from "@element-plus/icons-vue";
import { formatDate } from "@/utils/other";
import { $collect, $listData } from "@/api/search";
import { Returndata } from "@/common/enum";
import { FileSearchPar, SearchData, SearchPar } from "@/interface/search";
import { PagingPar } from "@/interface/common";
import { $onDownload } from "@/api/document";
import { ElMessage } from "element-plus";
import { formatFileSize } from "@/utils/other";
import { FeaturePar } from "@/interface/document";
import SvgIcon from "@/components/SvgIcon/index.vue";
import BasicSearchComponents from "./components/BasicSearch.vue";
import AdvancedSearchComponents from "./components/AdvancedSearch.vue";

const { VITE_FRAGMENT_DOWNLOAD_UPLOAD } = import.meta.env;

const styleHeight = ref<string>("");

/**
 * 简单搜索入参
 * @fileName
 * @searchType 基础查询标识
 * @category 全部文件标识
 * @delFlag 删除标记，0：删除，1：正常
 * @ownership 归属，1：公共，2：用户，3：部门
 * @parentId 文件夹ID
 */
const searchObj = reactive<FileSearchPar>({
  fileName: "",
  searchType: "base",
});

/**
 * 简单搜索
 * @isVisible 搜索历史记录下拉列表隐藏/显示
 * @titleValue 获取搜索类型下拉选中的value值
 * @searchName 搜索值入参
 * @dropdownref 引用整个下拉框容器，可以在点击外部时判断是否关闭下拉框
 */
const status = reactive({
  isVisible: false,
  titleValue: "",
  searchName: "",
  dropdownref: null,
});

/**
 * 高级搜索 全局
 * @visible 主题历史记录下拉框
 * @tagVisible 标签下拉框查询
 */
const advancedSearch = reactive({
  visible: false,
  tagVisible: false,
  dropdownref: null,
});

/**
 * *基础搜索 && 高级搜索公共变量
 * @isAdvanced 简单搜索 & 高级搜索 切换隐藏/显示
 * @isEmpty 查询列表为空，显示 空状态图片
 * @loading 列表查询数据时 先加载loading
 * @isPagin 查询数据小于10条不展示分页按钮
 * @indicate 区分基础搜索（base）和高级搜索（advanced）标识
 */
const common = reactive({
  isAdvanced: 1,
  isEmpty: true,
  loading: false,
  isPagin: false,
  indicate: "",
});

// 页眉
const breadcrumb = ref<FeaturePar[]>([]);

// 全选
const checkAll = ref<boolean>(false);
const isIndeterminate = ref<boolean>(false);
const checkedCities = ref<SearchData[]>([]);

// 高级搜索入参
const advanced = ref<SearchPar>({});
// table 列表
const listData = ref<SearchData[]>([]);

/**
 * 分页
 * @currentPage 当前页码
 * @pageSize 当前条数
 * @total 总条数
 */
const paging = reactive<PagingPar>({
  currentPage: 1,
  pageSize: 20,
  total: 0,
});

/**
 * 下载入参
 */
const feature = reactive({
  fileParam: [] as any[],
});

/**
 * 高级搜索
 * 子组件调用父组件方法
 */
const searchClick = (obj: SearchPar) => {
  advanced.value = obj;
  querySearch("advanced");
};

/**
 *  搜索查询列表
 */
const querySearch = (mark: string) => {
  listData.value = []; // 清空列表
  common.indicate = mark;
  common.loading = true;
  common.isEmpty = false;
  common.isPagin = false;

  let paramObj = {};
  if (mark === "base") {
    // 简单搜索 不同状态下的入参
    if (!searchObj.category) {
      searchObj.fileName = status.searchName;
    }
    paramObj = searchObj;
  } else {
    // 高级搜索入参
    paramObj = advanced.value;
  }

  // 查询列表
  $listData(paging, paramObj)
    .then((res: any) => {
      setTimeout(() => {
        res.data.records.forEach((item: any) => {
          item.checked = false;
        });
        listData.value = res.data.records;
        paging.currentPage = res.data.current;
        paging.pageSize = res.data.size;
        paging.total = res.data.total;

        // 查询数据小于10条不展示分页按钮
        res.data.total < 10
          ? (common.isPagin = false)
          : (common.isPagin = true);
      }, 500);
      // 对列表空状态处理
      res.data.records < 1 ? (common.isEmpty = true) : (common.isEmpty = false);
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {
      common.loading = false;
    });
};

// 点击文件夹可查询
const onHierarchy = (
  type?: string,
  fileId?: string,
  name?: string,
  ownership?: number
) => {
  if (type === null && common.indicate === "base") {
    searchObj.fileName = "";
    searchObj.category = "all";
    searchObj.delFlag = "1";
    searchObj.ownership = ownership;
    searchObj.parentId = fileId;

    breadcrumb.value.push({
      id: Math.floor(Math.random() * 100 + 1),
      fileId: fileId,
      fileName: name,
      fileType: "",
    });
    querySearch(common.indicate);
    return;
  }

  if (type === null && common.indicate === "advanced") {
    advanced.value.fileName = "";
    advanced.value.category = "all";
    advanced.value.delFlag = "1";
    advanced.value.ownership = ownership;
    advanced.value.parentId = fileId;

    breadcrumb.value.push({
      id: Math.floor(Math.random() * 100 + 1),
      fileId: fileId,
      fileName: name,
      fileType: "",
    });
    querySearch(common.indicate);
    return;
  }
};

/**
 * 点击页头查询
 */
const onPageHeader = (id?: number, fileId?: string) => {
  if (id == 0) {
    // 点击全部 还原初始化基础搜索参数
    initBasicSearch();
  }

  // 删除id的项后面的所有项
  if (fileId) {
    const index = breadcrumb.value.findIndex((item) => item["id"] === id);
    if (index !== -1) {
      breadcrumb.value.splice(index + 1, breadcrumb.value.length - index - 1);
    }
  } else {
    breadcrumb.value.length = 0; // 清空页眉
  }
  searchObj.parentId = fileId;
  querySearch(common.indicate);
};

// 分页
const handleCurrentChange = (val: number) => {
  paging.currentPage = val;
  querySearch(common.indicate);
};

// 每页显示多少条
const handleSizeChange = (val: number) => {
  paging.pageSize = val;
  querySearch(common.indicate);
};

// 全选
const handleCheckAll = (val: boolean) => {
  val ? val : (feature.fileParam.length = 0);

  let obj = {};
  listData.value.forEach((item: any) => {
    item.checked = val;
    if (val) {
      obj = {
        fileId: item.fileId,
        fileName: item.fileName,
        fileType: item.fileType !== null ? item.fileType : "file",
        size: item.fileSize,
      };
      feature.fileParam.push(obj);
    }
  });

  checkedCities.value = val ? listData.value : [];
  isIndeterminate.value = false;
};

// 单选
const handleSelect = (checked: boolean, row: SearchData) => {
  if (checked) {
    // 下载入参
    const obj = {
      fileId: row.fileId,
      fileName: row.fileName,
      fileType: row.fileType !== null ? row.fileType : "file",
      size: row.fileSize,
    };
    feature.fileParam.push(obj);
  } else {
    // 如果复选框被取消勾选，从 selectedIds 中移除 ID
    feature.fileParam = feature.fileParam.filter(
      (item) => item.fileId !== row.fileId
    );
  }
  // 控制全选/全不选状态
  if (feature.fileParam.length === listData.value.length) {
    isIndeterminate.value = false;
  } else if (feature.fileParam.length == 0) {
    isIndeterminate.value = false;
    checkAll.value = false;
  } else {
    isIndeterminate.value = true;
    checkAll.value = true;
  }
};

/**
 * 收藏
 */
const onCollect = async (id?: string) => {
  const ret = await $collect(id);
  if (ret && ret.code === Returndata.code) {
    ElMessage({
      message: "收藏文件成功！",
      type: "success",
    });
  }
};

/**
 * 单（批量）文件下载 方法
 */
const onDownload = (id?: string) => {
  const param = feature.fileParam;
  const sum = feature.fileParam.length;
  if (sum == 0) {
    ElMessage({
      message: "请选择下载文件（夹）！",
      type: "warning",
    });
    return;
  }
  if (id) {
    // 单文件下载
    for (let i = 0; i < param.length; i++) {
      const fileId = param[i].fileId;
      if (fileId === id) {
        const arrayList = [
          {
            fileId: param[i].fileId,
            fileName: param[i].fileName,
            fileType: param[i].fileType,
            size: param[i].size,
          },
        ];
        $onDownload(VITE_FRAGMENT_DOWNLOAD_UPLOAD, arrayList, 1);
      }
    }
  } else {
    $onDownload(VITE_FRAGMENT_DOWNLOAD_UPLOAD, feature.fileParam, sum);
  }
};

/**
 * 简单搜索&高级搜索切换
 * 点击事件
 */
const onAdvancedSearch = (num: number) => {
  common.isAdvanced = num;
  common.indicate = ""; // 清空基础搜索还是高级搜索标识
  common.isPagin = false; //不显示分页
  common.isEmpty = true; //显示loading

  status.searchName = ""; // 清空简单搜索入参

  breadcrumb.value.length = 0; //清空页眉
  listData.value = []; // 清空列表
  paging.total = 0; // 显示文件数清0

  checkAll.value = false; // 初始化全选按钮
  feature.fileParam.length = 0; // 清空下载入参

  initBasicSearch();
};

/**
 * 初始化基础搜索
 */
const initBasicSearch = () => {
  searchObj.category = "";
  searchObj.delFlag = "";
  searchObj.ownership = 0;
  searchObj.parentId = "";

  advanced.value.category = "";
  advanced.value.delFlag = "";
  advanced.value.ownership = 0;
  advanced.value.parentId = "";

  breadcrumb.value.length = 0; // 清空页眉
};

/**
 * 如果点击发生在下拉框外部，则关闭下拉框
 */
const toggleDropdown = () => {
  status.isVisible = false;
  advancedSearch.visible = false;
  advancedSearch.tagVisible = false;
};

/**
 *
 */
const onSendSwitch = (value: string) => {
  styleHeight.value = value;
};
</script>

<style scoped lang="less">
// el-checkbox样式
::v-deep(.el-checkbox.el-checkbox--large) {
  height: 32px;
}
.custom-size ::v-deep(.el-checkbox__input) {
  width: 18px;
  height: 18px;
}
.custom-size ::v-deep(.el-checkbox__inner) {
  width: 18px;
  height: 18px;
}
.custom-size ::v-deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #409eff;
  border-color: #409eff;
}
.custom-size ::v-deep(.el-checkbox__inner::after) {
  left: 7px;
  top: 3px;
}
.custom-size
  ::v-deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner::before) {
  top: 7px;
}

::v-deep(.el-affix--fixed) {
  background-color: #ffffff;
}
.search-img {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
  text-align: center;
  span {
    position: relative;
    top: -4px;
    img {
      width: 32px;
      height: 32px;
      margin: 0 5px;
      position: relative;
      top: 6px;
    }
  }
}
.search-box {
  height: 80px;
  text-align: center;
  box-shadow: 0 12px 10px -10px rgba(0, 0, 0, 0.1);
}

// 高级搜索
.advanced-search-box {
  text-align: center;
  padding: 0 10px 10px 10px;
  box-shadow: 5px 15px 15px -1px rgba(0, 0, 0, 0.08);
}

.secrch-condition {
  width: 60%;
  height: 76px;
  margin: auto;
  margin-top: 20px;
  font-weight: 400;
  margin-bottom: 10px;
  padding-bottom: 14px;
  font-size: 14px;
  .secrch-num {
    display: block;
    font-weight: 600;
    margin-bottom: 4px;
    color: rgba(0, 0, 0, 0.64);
    .custom-tips {
      position: relative;
      top: -5px;
      left: 20px;
      .svg-icon {
        display: inline-block;
        position: relative;
        top: 5px;
        margin-right: 10px;
      }
    }
  }

  .tab-list {
    width: 100%;
    height: 26px;
    line-height: 26px;
    float: left;
    font-size: 14px;
    color: #606266;
    margin-bottom: 10px;
    border-bottom: 1px solid #eee;
    border-bottom-color: #d9e0e9;
    .tab-all {
      float: left;
      min-width: 46px;
      cursor: pointer;
      font-style: normal;
      padding-left: 0;
    }
    .tab-all:hover {
      text-decoration: underline; /* 鼠标悬停时显示下划线 */
    }

    .tab-span {
      float: left;
      min-width: 64px;
      cursor: pointer;
      font-style: normal;
      padding: 0px 10px 0px 10px;
    }

    .icon-arrow2 {
      width: 16px;
      height: 16px;
      display: inline-block;
      position: relative;
      top: 1px;
      left: -15px;
    }
    .file-name {
      cursor: pointer;
      position: relative;
      top: 1px;
      .tab-name {
        float: right;
        min-width: 90px;
        max-width: 120px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .tab-name:hover {
        text-decoration: underline; /* 鼠标悬停时显示下划线 */
      }
    }

    .cut {
      color: rgba(0, 0, 0, 0.88);
      font-weight: 600;
    }
  }
}

// 列表
.secrch-list {
  width: 60%;
  margin: auto;
  min-height: calc(100vh - 386px);
  &-item {
    margin-top: 10px;
    height: 70px;
    border-bottom: 1px solid #dcdfe6;
    &-image {
      float: left;
      width: 100px;
      height: 60px;
      line-height: 60px;
      .icon {
        float: left;
        padding-top: 8px;
        height: 66px;
      }
      .icon-svg {
        width: 50px;
        height: 50px;
        position: absolute;
        cursor: pointer;
      }
    }

    &-content {
      width: 100%;
      height: 60px;
      ul {
        padding: 0;
        margin: auto;

        .title {
          padding: 4px 0;
          cursor: pointer;
          .title-a {
            width: 50%;
            position: relative;
            top: 4px;
            margin-top: -6px;
            color: #000000;

            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            display: inline-block;
            text-decoration: none;
          }
          .title-a:hover {
            text-decoration: underline; /* 鼠标悬停时显示下划线 */
          }
          // Tag 标签 el-tag 组件
          .el-tag--small {
            width: 36px;
            position: relative;
            top: 0px;
            padding: 0 5px;
            line-height: 17px;
            margin-left: 15px;
            text-align: center;
          }

          // el-avatar  组件样式
          .el-avatar--small {
            width: 24px;
            height: 24px;
            line-height: 24px;
          }
        }

        .mainbody {
          font-size: 12px;
          color: #7c7e83;
          height: 30px;
          &-avatar {
            float: left;
            width: 28px;
            margin-top: 4px;
          }
          &-name {
            float: left;
            width: 10%;
            margin-top: 7px;
          }
          &-time {
            float: left;
            margin: 7px 70px 0 0;
          }
          &-size {
            float: left;
            margin-top: 7px;
          }
          &-status {
            float: right;
            padding-top: 7px;
            ul {
              li {
                width: 55px;
                float: left;
                i {
                  width: 15px;
                }
              }
            }
          }
          &-collect {
            cursor: pointer;
            text-decoration: none; /* 默认情况下不显示下划线 */
          }
          &-collect:hover {
            color: #53a8ff;
            font-weight: 600;
            text-decoration: underline; /* 鼠标悬停时显示下划线 */
          }
          &-num {
            position: relative;
            top: -2px;
            left: 2px;
          }
          &-forbid {
            position: relative;
            top: -3px;
          }
        }
      }
    }
  }
}
.paging {
  width: 60%;
  padding-top: 20px;
  margin: auto;
  height: 70px;
}
</style>
