<template>
  <!-- 业务导航 -->
  <el-dialog
    draggable
    v-model="props.addObj.visible"
    title="新增业务导航"
    width="600"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    :before-close="onClose"
  >
    <el-form ref="formRef" :rules="rules" :model="form" label-width="150px">
      <el-form-item label="流程域名称：">
        <span>{{ props.serviceObj.label }}</span>
      </el-form-item>
      <el-form-item label="标题：" prop="title">
        <el-input
          v-model="form.title"
          placeholder="输入标题"
          style="width: 300px"
        />
      </el-form-item>
      <el-form-item label="版本号：" prop="version">
        <el-input
          v-model="form.version"
          pattern="^V(\d+)\.(\d+)\.(\d+)$"
          placeholder="输入版本号"
          style="width: 300px"
        />
        <span class="remark-sn">
          注：版本格式 v1_2025.1，小写v开头、长度为10，包含数字、下划线、点
        </span>
      </el-form-item>

      <el-form-item label="描述：" prop="desc">
        <el-input
          v-model="form.desc"
          style="width: 300px"
          :rows="3"
          maxlength="100"
          type="textarea"
          show-word-limit
          placeholder="输入描述..."
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-style" @click="handleClose(formRef)">
          关闭
        </el-button>
        <el-button class="btn-style" type="primary" @click="handleOk(formRef)">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { Returndata } from "@/common/enum";
import { ElMessage, FormInstance, FormRules } from "element-plus";
import { BusinessPar } from "@/interface/servicenavigation";
import { $saveAdd } from "@/api/servicenavigation";

const props = defineProps({
  /**
   * 流程域
   * @id
   * @name
   */
  serviceObj: {
    type: Object,
    default: {},
  },
  /**
   * 新增
   * @visible
   */
  addObj: {
    type: Object,
    default: {},
  },
});

/**
 * 子组件传参给父组件
 */
const emit = defineEmits(["child-event"]);

/**
 * 新增 & 编辑 入参
 * @title 标题
 * @status 状态
 * @version 版本号
 * @domainId 流程域ID
 * @desc 描述
 */
const form = reactive<BusinessPar>({
  title: "",
  status: 0,
  version: "",
  domainId: "",
  desc: "",
});

// form表单
const formRef = ref<FormInstance>();
const rules = reactive<FormRules<BusinessPar>>({
  title: [{ required: true, message: "标题不能为空", trigger: "blur" }],
  version: [
    { required: true, message: "版本号不能为空", trigger: "blur" },
    {
      pattern: /^v[0-9a-z._]{2,9}$/,
      message: "版本号输入错误",
      trigger: "blur",
    },
  ],
});

/**
 * 模态框 确定
 */
const handleOk = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.validate((valid) => {
    if (valid) {
      form.domainId = props.serviceObj.id;
      $saveAdd(form)
        .then((res: any) => {
          if (res.code === Returndata.code) {
            ElMessage({
              message: "新增成功",
              type: "success",
            });
            emit("child-event");
            // 模态框 关闭
            handleClose(formRef.value);
          }
        })
        .catch((err) => {
          console.log("处理错误信息：", err);
        })
        .finally(() => {});
    }
  });
};

/**
 * 模态框 关闭
 */
const handleClose = (formEl: FormInstance | undefined) => {
  props.addObj.visible = false;
  if (!formEl) return;
  formEl.resetFields();
};
const onClose = () => {
  handleClose(formRef.value);
};
</script>

<style scoped lang="less">
.remark-sn {
  font-size: 12px;
  color: #939090;
  height: 20px;
  line-height: 20px;
}

.dialog-footer {
  .btn-style {
    min-width: 90px;
  }
}
</style>
