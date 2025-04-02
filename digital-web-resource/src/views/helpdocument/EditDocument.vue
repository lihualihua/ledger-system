<template>
  <div class="edit-content">
    <div>{{ tabValue === "add" ? "创建业务导航" : "编辑业务导航" }}</div>
    <div class="edit-controls">
      <div class="dialog-footer">
        <el-button link @click="onPreview">
          <el-icon><View /></el-icon>
          预览
        </el-button>
        <el-button type="primary" class="btn-style" @click="queryBut(formRef)">
          保存
        </el-button>
      </div>
    </div>
    <el-divider></el-divider>

    <div class="edit-left">
      <el-form
        ref="formRef"
        :model="textForm"
        :rules="rules"
        label-width="140px"
      >
        <el-form-item label="标题：">
          <span>{{ titleName }}</span>
        </el-form-item>

        <el-form-item label="已创建历史版本：" v-if="tabValue === 'add'">
          <el-popover placement="right" :width="100" trigger="click">
            <template #reference>
              <el-button type="info" text bg size="small">查看</el-button>
            </template>
            <p class="title">历史版本号</p>
            <p
              v-for="(item, index) in versionList"
              :key="index"
              class="records"
            >
              <span v-if="index == 0">新</span> {{ item }}
            </p>
          </el-popover>
        </el-form-item>

        <el-form-item label="版本号：" prop="version">
          <el-input
            v-model="textForm.version"
            style="width: 300px"
            placeholder="输入版本号"
            :disabled="tabValue === 'add' ? false : true"
          />
        </el-form-item>

        <el-form-item label="描述：" prop="desc">
          <el-input
            v-model="textForm.desc"
            style="width: 600px"
            :rows="3"
            maxlength="100"
            type="textarea"
            show-word-limit
            placeholder="输入描述..."
          />
        </el-form-item>

        <el-form-item label="文本内容：">
          <WangEditorComponents
            :content="textForm.content"
            @wangeditor-event="onWangEditor"
          />
        </el-form-item>
      </el-form>
    </div>
  </div>

  <!-- 预览 -->
  <PreviewDrawerComponents :drawerObj="drawerObj" />
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from "vue";
import { FormRules, FormInstance, ElMessage } from "element-plus";
import PreviewDrawerComponents from "./components/PreviewDrawer.vue";
import WangEditorComponents from "@/components/WangEditor/index.vue";
import {
  $saveAllVersion,
  $saveContent,
  $saveVersionContent,
} from "@/api/servicenavigation";
import { BusinessPar, BusinessTable } from "@/interface/servicenavigation";
import { useRoute } from "vue-router";

// 路由
const route = useRoute();

// 标题
const titleName = ref<string>("");
// 历史版本
const versionList = ref<[]>([]);
// 编码
const codeValue = ref<string>("");
// 版本号
const versionName = ref<string>("");
// 标识 add：创建 edit：编辑
const tabValue = ref<string>("add");

/**
 * 抽屉
 */
const drawerObj = reactive({
  drawer: false,
  content: null,
});

/**
 * 保存入参
 * @code 编码
 * @version 版本号
 * @content 内容
 * @originalContent 原始数据内容
 * @code 描述
 */
const textForm = reactive<BusinessTable>({
  code: "",
  version: "",
  content: "",
  originalContent: "",
  desc: "",
});
/**
 * form表单空值验证
 */
const formRef = ref<FormInstance>();
const rules = reactive<FormRules>({
  version: [
    { required: true, message: "版本号不能为空", trigger: "blur" },
    {
      pattern: /^v[0-9a-z._]{2,9}$/,
      message:
        "版本号输入错误，版本格式v1_2025.1，小写v开头、长度为10，包含数字、下划线、点",
      trigger: "blur",
    },
  ],
});

// 确定
const queryBut = (formEl: FormInstance | undefined) => {
  textForm.code = codeValue.value;
  textForm.content = textForm.content;

  if (!formEl) return;
  formEl.validate((valid) => {
    if (valid) {
      $saveVersionContent(textForm)
        .then((res: any) => {
          if (res) {
            ElMessage({
              message: "保存成功！",
              type: "success",
            });
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
 * 预览
 */
const onPreview = () => {
  drawerObj.drawer = true;
};

/**
 * 预览
 * 文本内容值
 */
const onWangEditor = (param: any) => {
  const html = param.getHtml();
  textForm.originalContent = "`" + html + "`"; // 保存 原始数据

  textForm.content = html; // 保存 数据数据格式
  drawerObj.content = param; // 抽屉 原始数据 预览数据格式
};

/**
 * 查询最新版本数据
 * 创建 && 编辑
 */
const addContent = (params: BusinessPar) => {
  $saveContent(params)
    .then((res: any) => {
      titleName.value = res.data.title;
      if (tabValue.value === "edit") {
        textForm.version = versionName.value;
        textForm.desc = res.data.desc;

        let result = res.data.content.replace(/`/g, "");
        textForm.content = result;
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

/**
 * 创建
 * 查询所有版本号
 */
const saveAllVersion = () => {
  $saveAllVersion(codeValue.value, "")
    .then((res: any) => {
      versionList.value = res.data;
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

onMounted(() => {
  // 获取路由参数 code 版本号  标识
  tabValue.value = route.query.tab as string; // 标识 add：创建 edit：编辑
  codeValue.value = route.query.code as string; // code 编码值
  versionName.value = route.query.version as string; // 版本号

  // 创建
  if (tabValue.value === "add") {
    saveAllVersion(); // 查询所有版本号
  }

  const params = {
    code: codeValue.value,
    version: versionName.value,
  };

  addContent(params); // 查询最新版本数据
});
</script>

<style scoped lang="less">
.edit-content {
  min-height: calc(100vh - 100px);
  background: #fff;
  padding: 20px;

  .edit-controls {
    width: 30%;
    float: right;
    position: relative;
    top: -24px;
    .dialog-footer {
      text-align: right;
      .btn-style {
        min-width: 80px;
      }
    }
  }

  .edit-left {
    width: 80%;
    float: left;

    .el-upload__tip {
      padding-left: 20px;
    }
  }
}
.title {
  margin-bottom: 6px;
}
.records {
  height: 26px;
  line-height: 26px;
  padding-left: 6px;
  cursor: context-menu;

  span {
    color: #409eff;
    padding-right: 4px;
  }
}
.records:hover {
  background-color: #f5f7fa;
}
</style>
