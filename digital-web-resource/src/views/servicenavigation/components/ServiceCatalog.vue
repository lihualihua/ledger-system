<template>
  <!-- 流程域 -->
  <div class="service-catalog">
    <div class="service-but">
      <el-button type="primary" @click="handleAdd">新增</el-button>
      <el-button @click="queryList('refresh')">
        <el-icon class="is-loading" v-show="refresh"><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-table
      v-loading="tableLoading"
      :data="tableData"
      height="620"
      highlight-current-row
      style="width: 100%"
    >
      <el-table-column prop="order" label="序号" width="60" />
      <el-table-column prop="name" label="流程域名称" />
      <el-table-column prop="updateBy" label="修改人" width="140" />
      <el-table-column prop="updateTime" label="修改时间" width="200" sortable>
        <template #default="scope">
          {{ formatDate(scope.row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" sortable>
        <template #default="scope">
          <el-switch
            v-model="tableData[scope.$index].status"
            :active-value="1"
            :inactive-value="0"
            @change="onSwitch(scope.row, tableData[scope.$index].status)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="location" label="模版存储路径" width="340">
        <template #default="scope">
          <div class="table-hover">
            <span :title="scope.row.location">
              {{ scope.row.location }}
            </span>
          </div>
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
      <el-table-column fixed="right" label="操作" width="140">
        <template #default="scope">
          <el-button
            link
            type="primary"
            size="small"
            @click="onEdit(scope.row)"
          >
            编辑
          </el-button>
          <el-button
            link
            type="primary"
            size="small"
            v-show="scope.row.status == 0"
            @click="onSwitch(scope.row, 1)"
          >
            启用
          </el-button>
          <el-button
            link
            type="warning"
            size="small"
            v-show="scope.row.status == 1"
            @click="onSwitch(scope.row, 0)"
          >
            禁用
          </el-button>

          <el-button
            link
            type="danger"
            size="small"
            :disabled="scope.row.status == 0 ? false : true"
            @click="onDelete(scope.row.id, scope.row.name)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog
    draggable
    v-model="visible"
    title="新增流程域"
    width="600"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    :before-close="onClose"
  >
    <el-form
      ref="formRef"
      :rules="rules"
      :model="form"
      label-width="150px"
      class="upload-form"
    >
      <el-form-item label="流程域名称：" prop="name">
        <el-input
          v-model="form.name"
          placeholder="输入流程域名称"
          style="width: 300px"
        />
      </el-form-item>
      <el-form-item label="模版上传路径：" prop="folderId">
        <el-select
          v-model="form.folderId"
          placeholder="选择模版上传路径"
          style="width: 300px"
        >
          <el-option
            v-for="item in directoryData"
            :key="item.fileId"
            :label="item.fileName"
            :value="item.fileId"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="流程域排序：" prop="order">
        <el-input-number
          v-model="form.order"
          :min="1"
          :max="100"
          controls-position="right"
        />
      </el-form-item>

      <el-form-item label="业务状态：" prop="status">
        <el-switch
          v-model="form.status"
          :active-value="1"
          :inactive-value="0"
        />
      </el-form-item>

      <el-form-item label="流程域描述：" prop="desc">
        <el-input
          v-model="form.desc"
          style="width: 300px"
          :rows="3"
          maxlength="100"
          type="textarea"
          show-word-limit
          placeholder="输入流程域描述..."
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-style" @click="onClose()"> 关闭 </el-button>
        <el-button class="btn-style" type="primary" @click="handleOk(formRef)">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { formatDate } from "@/utils/other";
import { ElMessage, ElMessageBox, FormInstance, FormRules } from "element-plus";
import {
  $directoryList,
  $domainCreate,
  $domainDelete,
  $domainUpdate,
  $queryDomainList,
} from "@/api/servicenavigation";
import { Refresh } from "@element-plus/icons-vue";
import { FlowTable } from "@/interface/servicenavigation";
import { AddEdit, Returndata } from "@/common/enum";

// 模态框
const visible = ref<boolean>(false);
// 业务状态 1:启用  0：禁用
const active = ref("");
// 刷新
const refresh = ref<boolean>(false);
// 提示语标识
const tips = ref<string>("");
// 加载loading
const tableLoading = ref<boolean>(false);
// 存储目录列表
const directoryData = ref<{ fileId: string; fileName: string }[]>();

/**
 * 新增 & 编辑 入参
 */
const form = reactive<FlowTable>({
  name: "",
  folderId: "",
  status: 1,
  desc: "",
  order: 1,
});

// form表单
const formRef = ref<FormInstance>();
const rules = reactive<FormRules<FlowTable>>({
  name: [{ required: true, message: "流程域名称不能为空", trigger: "blur" }],
  folderId: [
    { required: true, message: "模版上传路径不能为空", trigger: "change" },
  ],
});

// table 列表
const tableData = ref<FlowTable[]>([]);

/**
 * 模态框 新增
 */
const handleAdd = () => {
  handleClose(formRef.value);
  visible.value = true;
  active.value = AddEdit.Add;
};

/**
 * 模态框 确定
 */
const handleOk = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.validate((valid) => {
    if (valid) {
      onSubmit();
    }
  });
};

/**
 *  新增 & 编辑
 */
const onSubmit = () => {
  // 调用 新增接口
  if (active.value === AddEdit.Add) {
    $domainCreate(form)
      .then((res: any) => {
        if (res.code === Returndata.code) {
          ElMessage({
            message: "新增成功",
            type: "success",
          });
          // 模态框 关闭
          onClose();
          queryList();
        }
      })
      .catch((err) => {
        console.log("处理错误信息：", err);
      })
      .finally(() => {});
  } else {
    // 调用 编辑接口
    $domainUpdate(form)
      .then((res: any) => {
        if (res.code === Returndata.code) {
          ElMessage({
            message: `${tips.value}成功`,
            type: "success",
          });
          // 模态框 关闭
          onClose();
          queryList();
        }

        // 启用 & 禁用
        if (form.status) {
          const index = tableData.value.findIndex(
            (item) => item.id === form.id
          );
          tableData.value[index].status = form.status;
        }
      })
      .catch((err) => {
        console.log("处理错误信息：", err);
      })
      .finally(() => {});
  }
};

/**
 * 模态框 关闭
 */
const handleClose = (formEl: FormInstance | undefined) => {
  form.id = "";
  form.name = "";
  form.status = 1;
  form.desc = "";
  form.folderId = "";
  form.order = 1;
  if (!formEl) return;
  formEl.resetFields();
};
const onClose = () => {
  visible.value = false;
  handleClose(formRef.value);
};

/**
 * table 查询列表
 */
const queryList = (name?: string) => {
  // 刷新
  if (name) {
    refresh.value = true;
  }
  tableLoading.value = true;
  $queryDomainList("")
    .then((res: any) => {
      tableData.value = res.data;
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

/**
 * table 编辑
 */
const onEdit = (obj: FlowTable) => {
  tips.value = "修改";
  visible.value = true;
  active.value = AddEdit.EDIT;
  form.id = obj.id;
  form.name = obj.name;
  form.status = obj.status;
  form.desc = obj.desc;
  form.folderId = obj.folderId;
  form.order = obj.order;
};

/**
 * table 启用 禁用
 */
const onSwitch = (obj: FlowTable, num?: number) => {
  num == 0 ? (tips.value = "禁用") : (tips.value = "启用");
  active.value = AddEdit.EDIT;
  form.id = obj.id;
  form.name = obj.name;
  form.status = num;
  form.desc = obj.desc;
  form.folderId = obj.folderId;
  form.order = obj.order;
  onSubmit(); // 新增 & 编辑
};

/**
 * table 删除
 */
const onDelete = (id: string, name: string) => {
  ElMessageBox.confirm(`是否确定删除 ${name} ？`, "提示", {
    confirmButtonText: " 确定 ",
    cancelButtonText: " 取消 ",
    type: "warning",
  })
    .then(() => {
      $domainDelete(id)
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
 * 查找附件存储目录列表
 */
const directoryList = () => {
  $directoryList()
    .then((res: any) => {
      directoryData.value = res.data;
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

onMounted(() => {
  directoryList(); // 查找附件存储目录列表
  queryList(); // 查询列表
});
</script>

<style scoped lang="less">
.service-catalog {
  padding: 0 10px;
  .service-but {
    height: 40px;
    line-height: 40px;
    margin-bottom: 14px;
    ::v-deep(.el-button) {
      width: 95px;
    }
  }

  .table-hover {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}

.dialog-footer {
  .btn-style {
    min-width: 90px;
  }
}
</style>
