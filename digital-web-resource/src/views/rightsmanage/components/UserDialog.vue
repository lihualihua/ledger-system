<template>
  <el-dialog
    draggable
    width="570"
    title="新增用户"
    v-model="props.userDialog.visible"
    :close-on-click-modal="false"
    :destroy-on-close="true"
  >
    <el-form
      ref="ruleFormRef"
      :model="ruleForm"
      :rules="rules"
      label-width="140"
    >
      <el-form-item label="账号：">
        <el-input
          v-model="ruleForm.accountNum"
          placeholder="输入账号"
          style="width: 300px"
        />
      </el-form-item>
      <el-form-item label="姓名：">
        <el-input
          v-model="ruleForm.name"
          placeholder="输入姓名"
          style="width: 300px"
        />
      </el-form-item>
      <el-form-item label="性别：">
        <el-radio-group v-model="ruleForm.sex">
          <el-radio value="1">男</el-radio>
          <el-radio value="0">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="电话：">
        <el-input
          v-model="ruleForm.phone"
          placeholder="输入电话"
          style="width: 300px"
        />
      </el-form-item>
      <el-form-item label="邮箱：">
        <el-input
          v-model="ruleForm.mail"
          placeholder="输入邮箱"
          style="width: 300px"
        />
      </el-form-item>
      <el-form-item label="部门：" prop="section">
        <el-select
          v-model="ruleForm.section"
          placeholder="选择部门"
          style="width: 300px"
        >
          <el-option
            v-for="item in options"
            :key="item.value"
            :value="item.value"
            :label="item.label"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="角色：" prop="role">
        <el-radio-group v-model="ruleForm.role">
          <el-radio value="1">超级管理员</el-radio>
          <el-radio value="2">部门管理员</el-radio>
          <el-radio value="3">普通用户</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="状态：" prop="status">
        <el-radio-group v-model="ruleForm.status">
          <el-radio :value="true">正常</el-radio>
          <el-radio :value="false">失效</el-radio>
        </el-radio-group>
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
  userDialog: {
    type: Object,
    default: {},
  },
});

const options = [
  {
    value: "1",
    label: "技术部",
  },
  {
    value: "2",
    label: "金融部",
  },
];

interface RuleForm {
  accountNum?: string; // 账号
  name?: string; // 姓名
  sex?: string; // 性别
  phone?: string; // 电话
  mail?: string; // 邮箱
  section?: string; // 部门
  role?: string; // 角色
  status?: boolean; // 状态
}
const ruleFormRef = ref<FormInstance>();
const ruleForm = reactive<RuleForm>({
  accountNum: "",
  name: "",
  sex: "1",
  phone: "",
  mail: "",
  section: "",
  role: "1",
  status: true,
});

const rules = reactive<FormRules<RuleForm>>({
  // name: [
  //   {
  //     required: true,
  //     message: "菜单名称不能为空",
  //     trigger: "blur"
  //   }
  // ],
  // icon: [
  //   {
  //     required: true,
  //     message: "图标不能为空",
  //     trigger: "change"
  //   }
  // ]
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
  props.userDialog.visible = false;
};
</script>

<style scoped lang="less">
.icon {
  width: 18px;
  height: 18px;
  position: relative;
  top: 4px;
}

.dialog-footer {
  .btn-style {
    min-width: 90px;
  }
}
</style>
