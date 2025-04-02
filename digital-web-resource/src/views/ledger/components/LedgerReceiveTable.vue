<template>
  <!-- 分配给我 -->
  <div class="ledger-feature main-container-mc">
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
      <el-table-column prop="createBy" label="分配人" width="300" />
      <el-table-column prop="updateTime" label="创建时间" width="300">
        <template #default="scope">
          {{ formatDate(scope.row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="180">
        <template #default="scope">
          <el-button
            link
            type="primary"
            size="small"
            style="margin-right: 16px"
            @click="onOnlineEdit(scope.row.tempId, scope.row.name)"
          >
            在线编辑
          </el-button>

          <el-upload
            action
            accept=".xlsx,.xls"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="onUpload"
            style="display: inline; margin-right: 10px"
          >
            <template #trigger>
              <el-button
                link
                type="primary"
                size="small"
                @click="onBtnUpload(scope.row.tempId)"
              >
                导入
              </el-button>
            </template>
          </el-upload>

          <el-button
            link
            type="primary"
            size="small"
            @click="onDownload(scope.row.tempId)"
          >
            下载
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

  <LedgerDialogComponents :templateObj="templateObj" />
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { $downloadLedger, $editorUpload, $queryInvite } from "@/api/ledger";
import { formatDate } from "@/utils/other";
import { PagingPar } from "@/interface/common";
import { Refresh } from "@element-plus/icons-vue";
import LedgerDialogComponents from "./LedgerDrawer.vue";
import { LedgerTable } from "@/interface/ledger";
import { ElMessage } from "element-plus";
import { Returndata } from "@/common/enum";
import { useRouter } from "vue-router";

// 路由
const router = useRouter();

// 模版下载模态框
const templateObj = reactive({
  visible: false,
});

// 加载loading
const tableLoading = ref<boolean>(false);
// 刷新
const refresh = ref<boolean>(false);

// table 列表
const tableData = ref<LedgerTable[]>([]);

// 获取文件tempId
const tempId = ref<string>("");

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
  $queryInvite(paging.currentPage!, paging.pageSize!)
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
 * 导入模版
 */
const onUpload = (item: any) => {
  const formData = new FormData();
  formData.append("file", item.raw);
  $editorUpload(tempId.value, formData)
    .then((res: any) => {
      if (res && res.code === Returndata.code) {
        ElMessage({
          message: "模版上传成功",
          type: "success",
        });
        queryList(); // 查询列表
      }
    })
    .catch((err) => {
      console.log("文件上传处理错误信息：", err);
    });
};
/**
 * 上传文件获取tempId
 */
const onBtnUpload = (id: string) => {
  tempId.value = id;
};

/**
 * 下载台账Excel数据
 */
const onDownload = (id: string) => {
  $downloadLedger({ tempId: id });
};

/**
 * 在线编辑
 */
const onOnlineEdit = (id: string, name: string) => {
  const fileName = name.split(".")[0];
  const route = router.resolve({
    path: "/ledgertabledetails/" + "edit", // 目标路由的路径
    query: { standingbookId: id, fileName }, // 携带的参数
  });
  // 使用 window.open 打开新窗口
  window.open(route.href, "_blank");
};

onMounted(() => {
  queryList();
});
</script>

<style scoped lang="less">
// 隐藏表头的全选框
::v-deep(.el-table__header-wrapper .el-checkbox) {
  display: none;
}

.ledger-feature {
  height: 32px;
  margin: 12px 0 16px 0;

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
