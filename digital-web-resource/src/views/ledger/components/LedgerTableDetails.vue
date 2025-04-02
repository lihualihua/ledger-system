<template>
  <!-- 查看详情 -->
  <el-container>
    <el-header>
      <div class="layout-header-inner">
        <div class="head">
          <a class="title">{{ tempName }} - 台账详细信息</a>
          <div class="btn" v-if="identify === 'edit'">
            <el-button class="btn-style" @click="queryList('refresh')">
              <el-icon class="is-loading" v-show="refresh"><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button
              type="primary"
              class="btn-style"
              color="#dbdbdb"
              @click="onEdit(isDisabledref)"
            >
              {{ isDisabledref ? "取消编辑" : "编辑" }}
            </el-button>
            <el-button type="primary" class="btn-style" @click="onSave">
              保存
            </el-button>
          </div>
        </div>
      </div>
    </el-header>

    <el-main>
      <div class="common-input" v-if="identify === 'edit'">
        <el-form label-width="140px" v-show="isDisabledref">
          <el-form-item label="公共编辑区域：">
            <el-input
              v-model="commonObj.columnValue"
              show-word-limit
              type="textarea"
              maxlength="1000"
              @input="onCommonInput"
            />
          </el-form-item>
        </el-form>
      </div>

      <div class="layout-main">
        <el-table
          :data="tableData"
          border
          v-loading="tableLoading"
          highlight-current-row
          height="750"
          style="width: 100%"
        >
          <el-table-column
            v-for="column in tableColumns"
            :key="column.prop"
            :label="column.label"
            :prop="column.prop"
            :width="column.attrs.width"
            :fixed="column.attrs.fixed"
          >
            <!-- 表头 -->
            <template #header>
              <div class="text-ellipsis">
                <span :title="column.label">
                  {{ column.label }}
                </span>
              </div>
            </template>

            <!-- 表体 -->
            <template #default="{ row }">
              <!-- 如果列需要输入框 -->
              <div class="text-ellipsis">
                <span
                  v-if="
                    row.editable &&
                    column.label !== '指派人(工号)*' &&
                    column.label !== '行标题(非必填)'
                  "
                >
                  <el-input
                    v-if="row[column.prop].edit"
                    v-model="row[column.prop].value"
                    placeholder="请输入内容"
                    :class="
                      row[column.prop].rowId === commonObj.rowId &&
                      row[column.prop].columnId === commonObj.columnId
                        ? 'cut'
                        : ''
                    "
                    @focus="
                      onFocus(
                        row[column.prop].rowId,
                        row[column.prop].columnId,
                        row[column.prop].value
                      )
                    "
                    @blur="
                      onBlur(
                        row[column.prop].id,
                        row[column.prop].value,
                        row[column.prop].columnId,
                        row[column.prop].rowId
                      )
                    "
                    @input="
                      onInput(
                        row[column.prop].rowId,
                        row[column.prop].columnId,
                        row[column.prop].value
                      )
                    "
                  />
                  <span v-else>{{ row[column.prop].value }}</span>
                </span>
                <span v-else>
                  <span :title="row[column.prop].value">
                    {{ truncateText(row[column.prop].value) }}
                  </span>
                </span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { $onlineEdit, $queryDetailedInfo } from "@/api/ledger";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { Refresh } from "@element-plus/icons-vue";

// 路由
const route = useRoute();
// 公共输入框
const commonObj = reactive<{
  rowId: string;
  columnId: string;
  columnValue: string;
}>({
  rowId: "",
  columnId: "",
  columnValue: "",
});

// 台账ID
const tempId = ref<string>("");
// 台账Name
const tempName = ref<string>("");
// 标识 区分 查看（view） 还是 编辑（edit）
const identify = ref<string>("");
// 公共输入框禁用和可用
const isDisabledref = ref<boolean>(false);

// 加载loading
const tableLoading = ref<boolean>(false);
// 刷新
const refresh = ref<boolean>(false);

// 表头
const tableColumns = ref<
  { label: string; prop: string; attrs: any; editable: boolean }[]
>([]);
// 表体数据
const tableData = ref([] as any);

// 编辑后的值
const editArray = ref<
  { id: string; value: string; columnId: string; rowId: string }[]
>([]);

/**
 * table 查询列表
 */
const queryList = (name?: string) => {
  tableLoading.value = true;
  // 刷新
  if (name) {
    refresh.value = true;
  }
  $queryDetailedInfo(tempId.value)
    .then((res: any) => {
      res.data.data.forEach((item: any) => {
        item.editable = false;
      });
      tableColumns.value = res.data.columns;
      tableData.value = res.data.data;
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

      onClear(); // 清空所有参数值
    });
};

/**
 * 截断文本函数
 */
const truncateText = (text: string | number | any) => {
  if (text.length > 8) {
    return text.slice(0, 8) + "...";
  }
  return text;
};

/**
 * 编辑
 */
const onEdit = (bool: boolean) => {
  if (!bool) {
    isDisabledref.value = true;
    tableData.value.forEach((item: any) => {
      item.editable = true;
    });
  } else {
    queryList();
    isDisabledref.value = false;
    tableData.value.forEach((item: any) => {
      item.editable = false;
    });
    onClear(); // 清空所有参数值
  }
};

/**
 * 保存
 */
const onSave = () => {
  tableData.value.forEach((item: any) => {
    item.editable = false;
  });
  if (editArray.value.length == 0) {
    ElMessage({
      message: "请填写台账数据后在提交",
      type: "warning",
    });
    return;
  }

  $onlineEdit(tempId.value, editArray.value)
    .then((res: any) => {
      ElMessage({
        message: res.data,
        type: "success",
      });
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {
      onClear(); // 清空所有参数值
    });
};

/**
 * @Blur
 * Input 值改变 失去焦点
 * @字段id
 * @字段值
 * @columnId 列名称
 * @rowId 行序号
 */
const onBlur = (id: string, value: string, columnId: string, rowId: string) => {
  const obj = {
    id,
    value,
    columnId,
    rowId,
  };

  if (editArray.value.length > 0) {
    let num = 0;
    for (let i = 0; i < editArray.value.length; i++) {
      const elementID = editArray.value[i].id;
      if (elementID === id) {
        editArray.value[i].value = value;
        num = 1;
      }
    }

    if (num == 0) {
      editArray.value.push(obj);
    }
  } else {
    editArray.value.push(obj);
  }

  console.log(editArray.value);
};

/**
 * @Focus
 * input 获取焦点 事件
 */
const onFocus = (rowId: string, columnId: string, value: string) => {
  commonObj.rowId = rowId;
  commonObj.columnId = columnId;
  commonObj.columnValue = value;
};

/**
 * @Input
 * input 实时更新事件
 */
const onInput = (rowId: string, columnId: string, value: string) => {
  onFocus(rowId, columnId, value);
};

/**
 * 公共Input
 * @input 实时更新事件
 */
const onCommonInput = () => {
  for (let i = 0; i < tableColumns.value.length; i++) {
    const label = tableColumns.value[i].label;

    for (let j = 0; j < tableData.value.length; j++) {
      const element = tableData.value[j][label];
      if (
        element.columnId === commonObj.columnId &&
        element.rowId === commonObj.rowId
      ) {
        tableData.value[j][label].value = commonObj.columnValue;

        editArray.value.forEach((res: any) => {
          if (
            res.columnId === commonObj.columnId &&
            res.rowId === commonObj.rowId
          ) {
            res.value = commonObj.columnValue;
          }
        });
      }
    }
  }
};

/**
 * 清空所有参数值
 */
const onClear = () => {
  isDisabledref.value = false;
  commonObj.rowId = "";
  commonObj.columnId = "";
  commonObj.columnValue = "";
  editArray.value.length = 0;
};

onMounted(() => {
  /**
   * 获取路由地址
   * 区分 查看（view） 还是 编辑（edit）
   */
  const path = route.path;
  identify.value = path.split("/").slice(-1).join("/");

  // 获取路由参数
  tempId.value = route.query.standingbookId as string;
  tempName.value = route.query.fileName as string;
  queryList();
});
</script>

<style scoped lang="less">
::v-deep(.el-header) {
  background: #f5f8fa;
}
::v-deep(.el-main) {
  padding: 0px 20px;
}
::v-deep(.el-table .cell) {
  height: 34px;
}
::v-deep(.el-table th.el-table__cell.is-leaf) {
  background: #f5f7fa !important;
}

.layout-header-inner {
  max-width: 100%;
  height: 60px;
  line-height: 60px;
  padding: 0;
  .head {
    display: flex;
    .title {
      flex: 1 1 0%;
    }
    .btn {
      width: 300px;
      text-align: right;
      .btn-style {
        min-width: 90px;
      }
    }
  }
}

.common-input {
  margin-top: 20px;
}

.layout-main {
  max-width: 100%;
  height: calc(100vh - 130px);
  padding: 0;
  margin-top: 40px;
  overflow: auto;
}

.layout-main::-webkit-scrollbar {
  width: 4px;
  height: 10px;
  overflow: visible;
}
.layout-main::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
.layout-main::-webkit-scrollbar-thumb {
  border: solid transparent;
  border-width: 1px 0;
  background-clip: padding-box;
  border-radius: 10px;
  background: #e2e6f0;
}
.layout-main::-webkit-scrollbar-thumb:hover {
  background: #d8dce6;
  border-radius: 10px;
}

.text-ellipsis {
  line-height: 32px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cut {
  border: 1px solid red;
  border-radius: 4px;
}
</style>
