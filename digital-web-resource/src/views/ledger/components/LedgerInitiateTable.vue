<template>
  <!-- 我的分配 -->
  <div class="ledger-feature main-container-mc">
    <el-button type="primary" @click="onImportAccount">导入台账</el-button>
    <el-button @click="onExport">模版库</el-button>
    <el-button @click="queryList('refresh')">
      <el-icon class="is-loading" v-show="refresh"><Refresh /></el-icon>
      刷新
    </el-button>
  </div>

  <div class="main-container-mc">
    <el-table
      :data="tableData"
      v-loading="tableLoading"
      :border="false"
      height="570"
      highlight-current-row
      style="width: 100%"
    >
      <el-table-column prop="name" label="台账名称" />
      <el-table-column prop="type" label="类型" width="300" sortable>
        <template #default="scope">
          <span v-if="scope.row.type === 1">行模板</span>
          <span v-else>单元格模板</span>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="创建时间" width="300" sortable>
        <template #default="scope">
          {{ formatDate(scope.row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="200" sortable>
        <template #default="scope">
          <span v-if="scope.row.status === 1 || scope.row.status === 2">
            <el-tag
              :type="scope.row.status === 1 ? 'success' : 'primary'"
              effect="dark"
            >
              {{ statusMap[scope.row.status] }}
            </el-tag>
          </span>
          <span v-else>
            <el-tag type="info" effect="dark">
              {{ statusMap[scope.row.status] }}
            </el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="240">
        <template #default="scope">
          <div v-if="scope.row.status == 1 || scope.row.status == 2">
            <el-button
              link
              type="primary"
              size="small"
              v-show="scope.row.status == 2 ? false : true"
              @click="onTransact(scope.row.tempId)"
            >
              催办
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              v-show="scope.row.status == 2 ? false : true"
              @click="onView(scope.row.tempId, scope.row.name)"
            >
              查看
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              @click="onDownload(scope.row.tempId)"
            >
              下载
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              :disabled="scope.row.status == 2 ? true : false"
              @click="onArchiving(scope.row.tempId, scope.row.name)"
            >
              归档
            </el-button>

            <el-button
              link
              type="warning"
              size="small"
              :disabled="scope.row.status == 2 ? true : false"
              @click="onCancel(scope.row.tempId, scope.row.name)"
            >
              撤销
            </el-button>
          </div>

          <div v-else style="text-align: center">
            <el-button
              link
              type="primary"
              size="small"
              @click="onDownload(scope.row.tempId)"
            >
              下载
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="onDelete(scope.row.tempId, scope.row.name)"
            >
              删除
            </el-button>
          </div>
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

  <!-- 导入台账 -->
  <LedgerDialogComponents :importObj="importObj" @child-event="queryList" />
  <!-- 台账模板库 -->
  <LedgerDrawerComponents :templateObj="templateObj" />
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import {
  $deleteCancel,
  $downloadLedger,
  $getTransact,
  $queryInitiate,
  $updateCancel,
  $updateStatus,
} from "@/api/ledger";
import { formatDate } from "@/utils/other";
import { PagingPar } from "@/interface/common";
import { Refresh } from "@element-plus/icons-vue";
import { LedgerTable } from "@/interface/ledger";
import { useRouter } from "vue-router";
import { Returndata } from "@/common/enum";
import { ElMessage, ElMessageBox } from "element-plus";
import { statusMap } from "@/common/data";
import LedgerDialogComponents from "./LedgerDialog.vue";
import LedgerDrawerComponents from "./LedgerDrawer.vue";

// 路由
const router = useRouter();

// 导入台账
const importObj = reactive({
  visible: false,
});

// 模版库
const templateObj = reactive({
  visible: false,
});

// 催办 记录每个数据的请求状态
const requestStatus = reactive<{ [key: string]: boolean }>({});
// 催办 检查某条数据是否正在请求
const isRequesting = (id: string): boolean => !!requestStatus[id];

// 加载loading
const tableLoading = ref<boolean>(false);
// 刷新
const refresh = ref<boolean>(false);
// table 列表
const tableData = ref<LedgerTable[]>([]);
// 分页
let paging = reactive<PagingPar>({
  currentPage: 1,
  pageSize: 20,
  total: 0,
});

/**
 * table 查询列表
 */
const queryList = (name?: string) => {
  tableLoading.value = true;
  // 刷新
  if (name) {
    refresh.value = true;
  }
  $queryInitiate(paging.currentPage!, paging.pageSize!)
    .then((res: any) => {
      if (res.data && res.data.records.length > 0) {
        const data = res.data.records;
        tableData.value = data;
        paging.currentPage = res.data.current;
        paging.pageSize = res.data.size;
        paging.total = res.data.total;
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
 * 催办
 */
const onTransact = (id: string) => {
  if (isRequesting(id)) {
    ElMessage({
      message: "您已催办，请勿重复操作",
      type: "warning",
    });
    return;
  }

  // 设置当前数据为请求中
  requestStatus[id] = true;

  $getTransact(id)
    .then((res: any) => {
      if (res.code === Returndata.code) {
        ElMessage({
          message: res.data,
          type: "success",
        });
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {
      setTimeout(() => {
        // 请求完成后清除状态
        requestStatus[id] = false;
      }, 10000);
    });
};

/**
 * 查看
 */
const onView = (id: string, name: string) => {
  const fileName = name.split(".")[0];
  const route = router.resolve({
    path: "/ledgertabledetails/" + "view", // 目标路由的路径
    query: { standingbookId: id, fileName }, // 携带的参数
  });
  // 使用 window.open 打开新窗口
  window.open(route.href, "_blank");
};

/**
 * 下载台账Excel数据
 */
const onDownload = (id: string) => {
  const obj = { tempId: id };
  $downloadLedger(obj);
};

/**
 * 归档
 */
const onArchiving = (id: string, name: string) => {
  ElMessageBox.confirm(`确定归档 ${name} 台账？`, "提示", {
    confirmButtonText: " 确定 ",
    cancelButtonText: " 取消 ",
    type: "warning",
    draggable: true,
  })
    .then(() => {
      $updateStatus(id)
        .then((res: any) => {
          if (res.code === Returndata.code) {
            const index = tableData.value.findIndex(
              (item) => item.tempId === id
            );
            tableData.value[index].status = 2;
            ElMessage({
              message: `${name} 已归档`,
              type: "success",
            });
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
 * 撤销
 */
const onCancel = (id: string, name: string) => {
  ElMessageBox.confirm(`是否确定撤销 ${name} 台账？`, "提示", {
    confirmButtonText: " 确定 ",
    cancelButtonText: " 取消 ",
    type: "warning",
    draggable: true,
  })
    .then(() => {
      $updateCancel(id)
        .then((res: any) => {
          if (res.code === Returndata.code) {
            const index = tableData.value.findIndex(
              (item) => item.tempId === id
            );
            tableData.value[index].status = 3;
            ElMessage({
              message: "撤销成功",
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
 * 删除
 */
const onDelete = (id: string, name: string) => {
  ElMessageBox.confirm(`是否确定删除 ${name} 数据？`, "提示", {
    confirmButtonText: " 确定 ",
    cancelButtonText: " 取消 ",
    type: "warning",
  })
    .then(() => {
      $deleteCancel(id)
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
 * 导入台账
 */
const onImportAccount = () => {
  importObj.visible = true;
};

/**
 * 模版库
 */
const onExport = () => {
  templateObj.visible = true;
};

onMounted(() => {
  queryList();
});
</script>

<style scoped lang="less">
// table隐藏表头的全选框
::v-deep(.el-table__header-wrapper .el-checkbox) {
  display: none;
}

.ledger-feature {
  height: 32px;
  margin: 12px 0 16px 0;

  // 按钮
  ::v-deep(.el-button) {
    min-width: 95px;
  }
}

// 分页
.paging {
  width: 100%;
  height: 32px;
  margin: 40px 0 20px 0;
}
</style>
