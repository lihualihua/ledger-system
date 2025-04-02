<template>
  <el-dialog
    draggable
    width="570"
    title="新增菜单"
    v-model="props.menuDialog.visible"
    :close-on-click-modal="false"
    :destroy-on-close="true"
  >
    <el-form
      ref="ruleFormRef"
      :model="ruleForm"
      :rules="rules"
      label-width="140"
    >
      <el-form-item label="上级菜单：">
        <el-tree-select
          v-model="ruleForm.menuTitle"
          :data="data"
          check-strictly
          :render-after-expand="false"
          :default-expand-all="true"
          style="width: 300px"
        />
      </el-form-item>

      <el-form-item label="菜单类型：">
        <el-radio-group v-model="ruleForm.menuType" @change="onMenuType">
          <el-radio value="2">目录</el-radio>
          <el-radio value="1">菜单</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="菜单名称：" prop="name">
        <el-input
          v-model="ruleForm.name"
          placeholder="输入菜单名称"
          style="width: 300px"
        />
      </el-form-item>

      <el-form-item label="菜单图标：">
        <div class="menu-icon">
          <Icon :icon="ruleForm.icon" class="icon" />
          <span v-if="ruleForm.icon"> {{ ruleForm.icon }}</span>
          <span v-else class="menu-tips">选择菜单图标</span>
        </div>
        <el-button type="primary" @click="onIcon">选择</el-button>
      </el-form-item>

      <el-form-item label="路由地址：" prop="routeAddress">
        <el-input
          v-model="ruleForm.routeAddress"
          placeholder="输入路由地址"
          style="width: 300px"
          :disabled="cut"
        />
      </el-form-item>

      <el-form-item label="组件路径：">
        <el-input
          v-model="ruleForm.componentPath"
          placeholder="输入组件路径"
          style="width: 300px"
        />
      </el-form-item>

      <el-form-item label="权限标识：" prop="limits">
        <el-checkbox-group v-model="ruleForm.limits">
          <el-checkbox value="1" disabled>超级管理员</el-checkbox>
          <el-checkbox value="2">部门管理员</el-checkbox>
          <el-checkbox value="3">普通用户</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="菜单排序：" prop="sort">
        <el-input-number
          v-model="ruleForm.sort"
          :min="1"
          :max="30"
          controls-position="right"
        />
      </el-form-item>

      <el-form-item label="菜单状态：" prop="status">
        <el-radio-group v-model="ruleForm.status">
          <el-radio value="1">启用</el-radio>
          <el-radio value="2">禁用</el-radio>
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

  <MenuIconDialog :iconDialog="iconDialog" @menuicon-event="menuiconEvent" />
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import MenuIconDialog from "./MenuIconDialog.vue";

const props = defineProps({
  menuDialog: {
    type: Object,
    default: {},
  },
});

// 菜单图标 模态框
const iconDialog = reactive({
  visible: false,
});

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

// 切换菜单类型 联动 路由地址
const cut = ref<boolean>(true);

interface RuleForm {
  menuTitle: string;
  menuType: string;
  name: string;
  icon: string;
  sort: number;
  routeAddress: string;
  componentPath: string;
  limits: string[];
  status: string;
}
const ruleFormRef = ref<FormInstance>();
const ruleForm = reactive<RuleForm>({
  menuTitle: "1",
  menuType: "2",
  name: "",
  icon: "",
  sort: 1,
  routeAddress: "--",
  componentPath: "",
  limits: ["1"],
  status: "1",
});

const rules = reactive<FormRules<RuleForm>>({
  name: [
    {
      required: true,
      message: "菜单名称不能为空",
      trigger: "blur",
    },
  ],
  sort: [
    {
      type: "date",
      required: true,
      message: "排序不能为空",
      trigger: "blur",
    },
  ],
  routeAddress: [
    {
      required: true,
      message: "路由地址不能为空",
      trigger: "blur",
    },
  ],
  limits: [
    {
      required: true,
      message: "权限字符不能为空",
      trigger: "change",
    },
  ],
});

/**
 * 选择菜单类型
 */
const onMenuType = (value: string | number | boolean) => {
  console.log(value);
  if (value == "1") {
    cut.value = false;
    ruleForm.routeAddress = "";
  } else {
    cut.value = true;
    ruleForm.routeAddress = "--";
  }
};

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
  props.menuDialog.visible = false;
};

/**
 * 菜单图标
 */
const onIcon = () => {
  iconDialog.visible = true;
  props.menuDialog.visible = false;
};

/**
 * 子组件菜单图标 参数
 */
const menuiconEvent = (val: string) => {
  if (val) {
    ruleForm.icon = val;
  }
  props.menuDialog.visible = true;
};
</script>

<style scoped lang="less">
.menu-icon {
  width: 230px;
  height: 32px;
  margin-right: 10px;
  box-shadow: 0 0 0 1px #dcdfe6;
  padding: 1px 11px;
  border-radius: 4px;
  background-color: #f5f7fa;
}
.icon {
  width: 18px;
  height: 18px;
  position: relative;
  top: 3px;
  margin-right: 10px;
}

.menu-tips {
  color: #babbbd;
}

.dialog-footer {
  .btn-style {
    min-width: 90px;
  }
}
</style>
