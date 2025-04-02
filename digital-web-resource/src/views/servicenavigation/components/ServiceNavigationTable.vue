<template>
  <!-- 业务导航 -->
  <div class="service-content">
    <div class="service-header">
      <el-button
        type="primary"
        :disabled="active ? false : true"
        @click="newEdition('add')"
      >
        创建新版本
      </el-button>
      <el-button
        type="primary"
        @click="onAdd"
        :disabled="props.serviceObj.numValue > 0 ? false : true"
      >
        新增
      </el-button>
      <el-button @click="queryList('refresh')">
        <el-icon class="is-loading" v-show="refresh"><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-table
      ref="serviceTableRef"
      :data="tableData"
      v-loading="tableLoading"
      :border="false"
      :tree-props="treeProps"
      row-key="id"
      height="560"
      highlight-current-row
      @select="handleSelect"
      style="width: 100%"
    >
      <el-table-column type="selection" width="40" :selectable="selectable" />
      <el-table-column prop="title" label="标题">
        <template #default="scope">
          <span :title="scope.row.title">
            {{ scope.row.isTitle }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="version" label="版本号" width="140" />
      <el-table-column prop="updateBy" label="修改人" width="140" />
      <el-table-column prop="updateTime" label="修改时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @click="onSwitch(scope.row, scope.row.status)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="desc" label="描述" width="340">
        <template #default="scope">
          <div class="table-hover">
            <span :title="scope.row.desc">
              {{ scope.row.desc }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="200">
        <template #default="scope">
          <el-button
            link
            type="primary"
            size="small"
            @click="onView(scope.row.code, scope.row.version)"
          >
            查看
          </el-button>
          <el-button
            link
            type="primary"
            size="small"
            @click="newEdition('edit', scope.row.code, scope.row.version)"
          >
            编辑
          </el-button>
          <el-button
            link
            type="primary"
            size="small"
            v-show="scope.row.status == 0"
            @click="onSwitch(scope.row, (scope.row.status = 1))"
          >
            发布
          </el-button>
          <el-button
            link
            type="warning"
            size="small"
            v-show="scope.row.status == 1"
            @click="onSwitch(scope.row, (scope.row.status = 0))"
          >
            下架
          </el-button>
          <el-button
            link
            type="danger"
            size="small"
            :disabled="scope.row.status == 0 ? false : true"
            @click="onDelete(scope.row.id, scope.row.version)"
          >
            删除
          </el-button>
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

  <!-- 新增 -->
  <ServiceNavigationAdd
    :addObj="addObj"
    :serviceObj="props.serviceObj"
    @child-event="queryList"
  />
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { formatDate } from "@/utils/other";
import { Refresh } from "@element-plus/icons-vue";
import ServiceNavigationAdd from "./ServiceNavigationAdd.vue";
import { BusinessTable } from "@/interface/servicenavigation";
import {
  $saveAdd,
  $serviceDelete,
  $serviceList,
} from "@/api/servicenavigation";
import eventBus from "@/common/eventBus";
import { Returndata } from "@/common/enum";
import { ElMessage, ElMessageBox } from "element-plus";
import { PagingPar } from "@/interface/common";
import { useRouter } from "vue-router";
// 路由
const router = useRouter();

/**
 * 流程域
 * @id
 * @name
 */
const props = defineProps({
  serviceObj: {
    type: Object,
    default: {},
  },
});

/**
 * 新增
 */
const addObj = reactive({
  visible: false,
});

// 创建新版本 显示隐藏
const active = ref<boolean>(false);
// 刷新
const refresh = ref<boolean>(false);
// 加载loading
const tableLoading = ref<boolean>(false);
// table 列表
const tableData = ref<BusinessTable[]>([]);
const treeProps = reactive({
  checkStrictly: true,
});
// 分页
let paging = reactive<PagingPar>({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});
// table
const serviceTableRef = ref();
// table 子数据id集
const sonIDList = ref<string[]>([]);
const selectable = (row: any) => !sonIDList.value.includes(row.id);

// 启用 & 禁用标识
const tips = ref<string>("");
// 单选获取code
const codeValue = ref<string>("");
// 版本号
const versionValue = ref<string>("");

/**
 * 新增
 */
const onAdd = () => {
  addObj.visible = true;
};

/**
 * 查看
 */
const onView = (code?: string, version?: string) => {
  const route = router.resolve({
    path: "/helpdocument", // 目标路由的路径
    query: { code: code, version: version }, // 携带的参数
  });
  // 使用 window.open 打开新窗口
  window.open(route.href, "_blank");
};

/**
 * 创建新版本 && 编辑版本
 */
const newEdition = (tab: string, code?: string, version?: string) => {
  // 使用 router.resolve 来解析路由
  let param = {};
  if (tab === "add") {
    param = {
      tab: tab,
      code: codeValue.value,
      version: versionValue.value,
    };
  } else {
    param = { tab: tab, code: code, version: version };
  }
  const route = router.resolve({
    path: "/editdocument", // 目标路由的路径
    query: param, // 携带的参数
  });

  // 使用 window.open 打开新窗口
  window.open(route.href, "_blank");
};

/**
 * table 查询列表
 */
const queryList = (name?: string) => {
  active.value = false;
  // 刷新
  if (name) {
    refresh.value = true;
  }
  tableLoading.value = true;
  $serviceList(props.serviceObj.id, paging.currentPage!, paging.pageSize!)
    .then((res: any) => {
      if (res.data && res.data.records.length > 0) {
        const data = res.data.records;
        data.forEach((item: any) => {
          const title = item.title;
          // 判断字符串长度是否超过16，如果超过则进行截取并在末尾添加省略号
          let resultTitle =
            title.length > 16 ? title.slice(0, 16) + "..." : title;
          item.isTitle = resultTitle;
          if (item.children.length > 0) {
            item.children.forEach((item2: any) => {
              const title2 = item2.title;
              // 判断字符串长度是否超过16，如果超过则进行截取并在末尾添加省略号
              let resultTitle2 =
                title2.length > 16 ? title2.slice(0, 16) + "..." : title2;
              item2.isTitle = resultTitle2;

              sonIDList.value.push(item2.id);
            });
          }
        });

        tableData.value = data;
        paging.currentPage = res.data.current;
        paging.pageSize = res.data.size;
        paging.total = res.data.total;
      } else {
        tableData.value.length = 0;
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {
      setTimeout(() => {
        tableLoading.value = false; // 列表刷新
        if (name) {
          refresh.value = false; // 刷新按钮 刷新
        }
      }, 100);
    });
};

// 每页显示多少条
const handleSizeChange = (val: number) => {
  paging.pageSize = val;
  queryList();
};

/**
 * table
 * 分页
 */
const handleCurrentChange = (val: any) => {
  paging.currentPage = val;
  queryList();
};

/**
 * 单选
 */
const handleSelect = (selection: any) => {
  // 清空多选
  serviceTableRef.value.clearSelection();

  // 获取选中的值
  if (selection.length > 0) {
    active.value = true;
    let id = {};
    selection.forEach((res: any) => {
      id = res;
      codeValue.value = res.code;
      versionValue.value = res.version;
    });
    serviceTableRef.value.toggleRowSelection(id);
  } else {
    active.value = false;
  }
};

/**
 * table 启用 禁用
 */
const onSwitch = (obj: any, num: number) => {
  num == 0 ? (tips.value = "下架") : (tips.value = "发布");
  const switchObj = {
    code: obj.code,
    status: num,
    version: obj.version,
  };

  $saveAdd(switchObj)
    .then((res: any) => {
      if (res.code === Returndata.code) {
        ElMessage({
          message: `${tips.value}成功`,
          type: "success",
        });
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

/**
 * 删除
 */
const onDelete = (id: string, name: string) => {
  ElMessageBox.confirm(`是否确定删除该 ${name} 版本？`, "提示", {
    confirmButtonText: " 确定 ",
    cancelButtonText: " 取消 ",
    type: "warning",
  })
    .then(() => {
      $serviceDelete(id)
        .then((res: any) => {
          if (res.code === Returndata.code) {
            ElMessage({
              message: "删除成功",
              type: "success",
            });
            queryList();
          }
        })
        .catch((err) => {
          console.log("处理错误信息：", err);
        })
        .finally(() => {});
    })
    .catch(() => {});
};

/**
 * 调用列表接口
 */
eventBus.on("onServiceEventBus", () => {
  queryList();
});
</script>

<style scoped lang="less">
/* 隐藏表头的全选框 */
::v-deep(.el-table__header-wrapper .el-checkbox) {
  display: none;
}

.service-content {
  .service-header {
    margin-bottom: 14px;
    ::v-deep(.el-button) {
      width: 95px;
      margin-right: 12px;
      margin-left: 0px;
    }
  }

  .table-hover {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
// 分页
.paging {
  width: 100%;
  height: 32px;
  margin: 40px 0 20px 0;
}
</style>
