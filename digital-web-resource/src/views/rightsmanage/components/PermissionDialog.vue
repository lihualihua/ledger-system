<template>
  <el-dialog
    draggable
    width="570"
    title="新增权限"
    v-model="props.permDialog.visible"
    :close-on-click-modal="false"
    :destroy-on-close="true"
  >
    <el-form
      ref="ruleFormRef"
      :model="ruleForm"
      :rules="rules"
      label-width="140"
    >
      <el-form-item label="菜单目录：" prop="title">
        <el-tree-select
          v-model="ruleForm.title"
          :data="data"
          :render-after-expand="false"
          :default-expand-all="true"
          placeholder="选择菜单目录"
          style="width: 300px"
        />
      </el-form-item>
      <el-form-item label="权限名称：" prop="name">
        <el-input
          v-model="ruleForm.name"
          placeholder="输入权限名称"
          style="width: 300px"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-style" @click="resetForm(ruleFormRef)">
          取消
        </el-button>
        <el-button
          class="btn-style"
          type="primary"
          @click="submitForm(ruleFormRef)"
        >
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";

const data = [
  {
    value: "1",
    label: "父节点",
    children: [
      {
        value: "1-1",
        label: "工作台",
      },
      {
        value: "1-2",
        label: "文档管理",
        children: [
          {
            value: "1-3",
            label: "文档库",
          },
          {
            value: "1-4",
            label: "智能搜索",
          },
        ],
      },
      {
        value: "1-5",
        label: "权限管理",
        children: [
          {
            value: "1-6",
            label: "用户管理",
          },
          {
            value: "1-7",
            label: "角色管理",
          },
        ],
      },
    ],
  },
];

const props = defineProps({
  permDialog: {
    type: Object,
    default: {},
  },
});

interface RuleForm {
  title: string; //
  name: string; // 权限名称
}
const ruleFormRef = ref<FormInstance>();
const ruleForm = reactive<RuleForm>({
  title: "",
  name: "",
});

const rules = reactive<FormRules<RuleForm>>({
  name: [
    {
      required: true,
      message: "权限名称不能为空",
      trigger: "blur",
    },
  ],
  title: [
    {
      required: true,
      message: "菜单目录不能为空",
      trigger: "change",
    },
  ],
});

/**
 * 确定
 */
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  await formEl.validate((valid, fields) => {
    if (valid) {
      console.log("submit!");
    } else {
      console.log("error submit!", fields);
    }
  });
};

/**
 * 取消
 */
const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.resetFields();
  props.permDialog.visible = false;
};
</script>

<style scoped lang="less">
.btn-style {
  min-width: 90px;
}
</style>
