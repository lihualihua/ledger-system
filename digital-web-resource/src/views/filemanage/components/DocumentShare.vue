<template>
  <div class="main-content">
    <div class="customTable" @contextmenu.prevent="contextmenuPrevent">
      <!-- 条件查询 -->
      <div class="condition">
        <div style="flex: 1" @click="closeMore">
          <div class="customtabs">
            <div class="tablist">
              <ul class="tabul">
                <li
                  v-for="item in shareData"
                  :key="item.id"
                  :class="['tabli', feature.record == item.value ? 'word' : '']"
                  @click="hildShare(item.value)"
                >
                  <span>{{ item.name }}</span>
                </li>
              </ul>
            </div>
            <!-- 下划线 -->
            <div class="tabline" v-show="underline" :style="styleObject"></div>
          </div>
        </div>
        <!-- 操作 下载 分享 删除,通过右击获取到的坐标定位 -->
        <FeatureComponents :feature="feature" :visible="visible" />
        <!-- 搜索查询 -->
        <div class="search">
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

      <div v-if="isActive" class="main-container-mc">
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
            <el-table-column prop="name" label="名称" sortable>
              <template #default="scope">
                <i :class="['icon', 'shared-link-m']"></i>
                <div class="table-name">
                  <div class="table-title" :title="scope.row.name">
                    <span class="table-span">{{ scope.row.name }}</span>
                  </div>
                  <div class="table-link">
                    <span style="float: left">链接：</span>
                    <span class="table-hyperlink" :title="scope.row.shareUrl">
                      <a
                        @click="accessLink(scope.row.shareUrl)"
                        target="_blank"
                        class="a"
                      >
                        {{ scope.row.shareUrl }}
                      </a>
                    </span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column
              prop="createBy"
              label="分享者"
              sortable
              width="140"
            >
              <template #default="scope">
                <span class="table-color">{{ scope.row.createBy }}</span>
              </template>
            </el-table-column>

            <el-table-column
              prop="createTime"
              label="分享时间"
              sortable
              width="220"
            >
              <template #default="scope">
                <span class="table-color">
                  {{ formatDate(scope.row.createTime) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="validFlag" label="状态" sortable width="120">
              <template #default="scope">
                <span class="table-color">
                  {{ getStatus(scope.row.validFlag, scope.row.validDate) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column
              prop="modifytime"
              label="接收者"
              sortable
              width="200"
            >
              <template #default="scope">
                <el-tooltip
                  :content="scope.row.shareUsersValue"
                  placement="top"
                  effect="light"
                  v-if="scope.row.shareUsersValue"
                >
                  <div class="table-province">
                    <span class="table-color">
                      {{ scope.row.shareUsersValue }}
                    </span>
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="desc" label="描述" sortable width="200">
              <template #default="scope">
                <el-tooltip
                  :content="scope.row.desc"
                  placement="top"
                  effect="light"
                  v-if="scope.row.desc"
                >
                  <div class="table-province">
                    <span class="table-color">
                      {{ scope.row.desc }}
                    </span>
                  </div>
                </el-tooltip>
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
import FeatureComponents from "@/components/FileTable/Feature.vue";
import { $queryList } from "@/api/share";
import { formatDate } from "@/utils/other";
import { ShareTableData, ShareUsers } from "@/interface/share";
import eventBus from "@/common/eventBus";
import { PagingPar } from "@/interface/common";
import { shareData } from "@/common/data";

// 定义状态映射
const statusMap = {
  1: "永久生效",
  2: "一年内",
  3: "半年内",
  4: "一月内",
  5: "",
} as const;

/**
 * 功能操作参数
 * @moreStatus 更多切换
 * @rootNode 根节点参数
 * @cutType  文件类型参数
 * @numLength checkbox选中个数
 * @top left  坐标
 * @record 2: 我的分享  3：分享给我
 * 入参 fileParam[]
 */
const feature = reactive({
  moreStatus: false,
  rootNode: "",
  cutType: "share",
  numLength: 0,
  top: 0,
  left: 0,
  record: 2,
  fileParam: [] as any[],
});

// 缩略图 列表切换
const isActive = ref<boolean>(true);
// table
const multipleTableRef = ref();
// 以下是自定义菜单数据
const visible = ref<boolean>(false); // 右击菜单显示、隐藏
// 加载loading
const tableLoading = ref<boolean>(false);
// table 列表
const tableData = ref<ShareTableData[]>([]);

// 下划线
const underline = ref<boolean>(true);
let styleObject = reactive({
  left: "16px",
  width: "48px",
});

// 分页
let paging = reactive<PagingPar>({
  currentPage: 1,
  pageSize: 20,
  total: 0,
});

watch(visible, (newValue) => {
  // 监听属性对象，newValue为新的值，也就是改变后的值
  if (newValue) {
    //菜单显示的时候
    document.body.addEventListener("click", closeMenu);
  } else {
    //菜单隐藏的时候
    // 移除body上添加的事件处理程序
    document.body.removeEventListener("click", closeMenu);
  }
});

onMounted(() => {
  onShare();
});

eventBus.on("shareEventBus", (param: any) => {
  for (let i = 0; i < param.length; i++) {
    const id = param[i].id;
    const index = tableData.value.findIndex((item) => item.id === id);
    tableData.value.splice(index, 1);
  }

  feature.numLength = 0; // table checkbox选中个数
  feature.fileParam.length = 0;
});

/**
 * 我的分享和分享给我
 */
const hildShare = (id: number) => {
  feature.numLength = 0;
  feature.record = id;
  onShare();
};

/**
 * @我的分享 2
 * @分享给我 3
 */
const onShare = () => {
  tableData.value.length = 0;
  let url = "";
  tableLoading.value = true;
  // 清空功能操作入参
  feature.numLength = 0; // table checkbox选中个数
  feature.fileParam.length = 0;
  if (feature.record === 2) {
    styleObject = {
      left: "16px",
      width: "48px",
    };
    // 我的分享
    url = `/api/doc/share/findMyShare/${paging.currentPage}/${paging.pageSize}`;
  } else {
    styleObject = {
      left: "122px",
      width: "48px",
    };
    // 分享给我
    url = `/api/doc/share/findShareWithMe/${paging.currentPage}/${paging.pageSize}`;
  }

  // 查询列表接口
  $queryList(url)
    .then((res: any) => {
      const data = res.data.records;
      data.forEach((item: any) => {
        if (item.shareUsers && item.shareUsers.length > 0) {
          const result = item.shareUsers
            .map((val: ShareUsers) => val.displayName)
            .join("，");
          item.shareUsersValue = result;
        } else {
          item.shareUsersValue = "";
        }
      });

      tableData.value = data;
      paging.currentPage = res.data.current;
      paging.pageSize = res.data.size;
      paging.total = res.data.total;
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {
      // 不管成功还是失败，都会执行这里的代码
      tableLoading.value = false;
    });
};

// 获取状态描述的方法
const getStatus = (status: keyof typeof statusMap, validDate: string) => {
  // 自定义时间
  if (status === 5) {
    return statusMap[status] || validDate;
  }
  return statusMap[status] || "未知状态";
};

// 分页
const handleCurrentChange = (val: number) => {
  paging.currentPage = val;
  onShare();
};

// 每页显示多少条
const handleSizeChange = (val: number) => {
  paging.pageSize = val;
  onShare();
};

/**
 * 访问分享链接
 */
const accessLink = (shareUrl: string) => {
  const target = "_blank";
  window.open(shareUrl, target);
};

/**
 * table
 * 全选
 */
const handleSelectAll = (selection: any) => {
  // 清空功能操作入参
  feature.fileParam.length = 0;
  handleCommon(selection, "");
};

/**
 * table
 * 单选
 * 勾选某一条数据 Checkbox 时触发的事件
 */
const handleSelect = (selection: any) => {
  // 清空功能操作入参
  feature.fileParam.length = 0;
  closeMenu(); // table 关闭右击菜单
  handleCommon(selection, "radio");
};
const handleCommon = (selection: any, name: string) => {
  let obj = {};
  selection.forEach((res: any) => {
    obj = {
      id: res.id,
      url: res.shareUrl,
    };
    feature.fileParam.push(obj);
  });
  feature.numLength = selection.length;
  // 关闭更多菜单
  if (name == "radio") {
    selection.length === 0 ? closeMore() : "";
  }
};

/**
 * table
 * 选中一条数据（关闭右键菜单）操作
 */
const handleRowClick = (row: any) => {
  const rowValue = toRaw(row);
  clearCheckbox(); // 清空多选
  // 单选 表示点击不是同一条数据，勾选该条数据
  const obj = {
    id: row.id,
    url: row.shareUrl,
  };
  feature.fileParam.push(obj);

  feature.numLength = 1;
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
  // 右键莫一行高亮
  multipleTableRef.value!.setCurrentRow(row);

  handleRowClick(row); // 选中一条数据（关闭右键菜单）

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

// table 中右击，禁止浏览器弹窗 弹出自定义窗口
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
  feature.moreStatus = false;
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
::v-deep(.el-table td.el-table__cell div) {
  height: 55px;
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
            cursor: pointer;
            font-size: 16px;
            font-style: normal;
            padding: 0px 30px 0px 10px;
          }
          .word {
            color: rgba(0, 0, 0, 0.88);
            font-weight: 600;
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
      width: 40px;
      height: 48px;
      line-height: 48px;
      .thumb {
        width: 24px;
        height: 24px;
        cursor: pointer;
        display: inline-block;
        position: relative;
        top: 6px;
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
  .table-name {
    width: 85%;
    height: 55px;
    margin-left: 50px;
    padding: 5px 0;
    .table-title {
      width: 100%;
      height: 24px;
      color: #020202;
      .table-span {
        max-width: 67%;
        display: block;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
    .table-link {
      width: 100%;
      height: 24px;
      font-size: 12px;
      .table-hyperlink {
        max-width: 90%;
        float: left;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      .a {
        cursor: pointer;
        color: #3b93ff;
        text-decoration: none;
      }
      .a:hover {
        text-decoration: underline; /* 鼠标悬停时显示下划线 */
      }
    }
  }
  .table-color {
    color: #939090;
    line-height: 55px;
  }
  .table-province {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  // 分页
  .paging {
    width: 100%;
    height: 32px;
    margin: 36px 0 20px 0;
  }
}
</style>
