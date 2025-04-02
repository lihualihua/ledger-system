<template>
  <el-dialog
    draggable
    v-model="props.tagObj.visible"
    title="新建标签"
    width="600"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    :before-close="handleClose"
  >
    <el-form ref="formRef" :model="tagForm" :rules="rules" label-width="140px">
      <el-form-item label="标签名称：" prop="name">
        <el-input
          v-model="tagForm.name"
          placeholder="输入标签名称"
          id="name"
          maxlength="12"
          style="width: 300px"
        />
      </el-form-item>
    </el-form>
    <span class="tag-hint">注：个人标签最多添加 100 个，管理员不限</span>
    <template #footer>
      <div class="dialog-footer">
        <span class="tag-manage" @click="onTag">管理个人标签</span>
        <el-button class="btn-style" @click="onCancel(formRef)">取消</el-button>
        <el-button
          class="btn-style"
          type="primary"
          @click="uploadSure(formRef)"
        >
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
  <!-- 管理个人标签 -->
  <TagmanageComponents :tagManage="tagManage" />
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { FormInstance, FormRules } from "element-plus";
import { AddTag } from "@/interface/addtag";
import { $addTag } from "@/api/addtag";
import TagmanageComponents from "@/components/FileTable/Tagmanage.vue";

const props = defineProps({
  tagObj: {
    type: Object,
    default: {},
  },
});

/**
 * 管理个人标签
 */
const tagManage = reactive({
  visible: false,
});

// form表单
const formRef = ref<FormInstance>();
const rules = reactive<FormRules<AddTag>>({
  name: [
    { required: true, message: "标签名称不能为空", trigger: "blur" },
    { min: 1, max: 12, message: "标签名名称最长为12个字符", trigger: "blur" },
  ],
});

/**
 * 新建标签
 * @tagName 标签名称
 * @desc 描述
 * @status 状态
 */
const tagForm = reactive<AddTag>({
  id: "",
  name: "",
  desc: "",
  status: 1,
});

// 模态框 确定
const uploadSure = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.validate((valid) => {
    if (valid) {
      $addTag(tagForm, "add");
      handleClose();
    }
  });
};

/**
 * 管理个人标签
 */
const onTag = () => {
  props.tagObj.visible = false;
  tagManage.visible = true;
};

/**
 * @取消按钮
 * @visible 模态框关闭
 */
const onCancel = (formEl: FormInstance | undefined) => {
  props.tagObj.visible = false;
  if (!formEl) return;
  formEl.resetFields();
};
const handleClose = () => {
  onCancel(formRef.value);
};
</script>

<style scoped lang="less">
.tag-hint {
  font-size: 12px;
  display: block;
  color: #939090;
  height: 20px;
  line-height: 20px;
  margin-bottom: 14px;
  padding-left: 50px;
}
.dialog-footer {
  .tag-manage {
    position: relative;
    right: 20px;
    color: rgba(0, 0, 0, 0.48);
    font-size: 14px;
    cursor: pointer;
    text-decoration: none; /* 默认情况下不显示下划线 */
  }
  .tag-manage:hover {
    text-decoration: underline; /* 鼠标悬停时显示下划线 */
  }
  .btn-style {
    min-width: 90px;
  }
}
</style>
