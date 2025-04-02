<template>
  <el-dialog
    draggable
    width="570"
    title="新增角色"
    v-model="props.roleDialog.visible"
    :close-on-click-modal="false"
    :destroy-on-close="true"
  >
    <el-form
      ref="ruleFormRef"
      :model="ruleForm"
      :rules="rules"
      label-width="140"
    >
      <el-form-item label="角色名称：" prop="name">
        <el-input
          v-model="ruleForm.name"
          placeholder="输入角色名称"
          style="width: 300px"
        />
      </el-form-item>

      <el-form-item label="描述：">
        <el-input
          v-model="ruleForm.desc"
          style="width: 300px"
          :rows="4"
          type="textarea"
          placeholder="输入描述信息"
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

const props = defineProps({
  roleDialog: {
    type: Object,
    default: {},
  },
});

interface RuleForm {
  name: string; // 名称
  desc: string; // 描述
}
const ruleFormRef = ref<FormInstance>();
const ruleForm = reactive<RuleForm>({
  name: "",
  desc: "",
});

const rules = reactive<FormRules<RuleForm>>({
  name: [
    {
      required: true,
      message: "名称不能为空",
      trigger: "blur",
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
  props.roleDialog.visible = false;
};
</script>

<style scoped lang="less">
.dialog-footer {
  .btn-style {
    min-width: 90px;
  }
}
</style>
