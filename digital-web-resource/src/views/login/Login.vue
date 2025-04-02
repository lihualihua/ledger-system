<template>
  <div class="login">
    <div class="box">
      <el-card>
        <div class="dialog-head">
          <h2 class="dialog-h2">欢迎登录</h2>
          <span>台账数据库系统</span>
        </div>
        <el-form
          ref="formRef"
          :size="formSize"
          style="max-width: 600px"
          :model="formData"
          status-icon
          :rules="rules"
        >
          <el-form-item prop="username">
            <el-input
              v-model="formData.username"
              :prefix-icon="User"
              size="large"
              placeholder="请输入账号"
              @input="
                formData.username = formData.username.replace(
                  /[\u4E00-\u9FA5]/g,
                  ''
                )
              "
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="formData.password"
              type="password"
              size="large"
              placeholder="请输入密码"
              show-password
              :prefix-icon="Lock"
              @input="
                formData.password = formData.password.replace(
                  /[\u4E00-\u9FA5]/g,
                  ''
                )
              "
              @keyup.enter.native="submitForm(formRef)"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :disabled="visible"
              @click="submitForm(formRef)"
            >
              <el-icon class="is-loading" v-show="visible">
                <Loading />
              </el-icon>
              登 录
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stort/user";
import { Lock, User, Loading } from "@element-plus/icons-vue";
import { ComponentSize, FormInstance, FormRules } from "element-plus";
import { $Login } from "@/api/login";
import { UserPar } from "@/interface/login.ts";

const router = useRouter();
const userStore = useUserStore();

const formSize = ref<ComponentSize>("default");
const formRef = ref<FormInstance>();
const formData = reactive<UserPar>({
  username: "",
  password: "",
});

const visible = ref<boolean>(false);

// 验证对象
const rules = reactive<FormRules<typeof formData>>({
  username: [
    { required: true, message: "请输入账号", trigger: "blur" },
    { min: 1, max: 18, message: "账号最长为18位", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 1, max: 18, message: "密码最长为18位", trigger: "blur" },
  ],
});

// 提交表单
const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.validate(async (valid) => {
    // 调用接口打开
    if (valid) {
      visible.value = true;
      await $Login(formData)
        .then((res: boolean) => {
          if (res) {
            router.push("/index");
          }
        })
        .catch(() => {})
        .finally(() => {
          visible.value = false;
        });
    }
  });
};

onMounted(() => {
  if (userStore.user.username) {
    router.push("/index");
  }
});
</script>

<style scoped lang="less">
::v-deep(.el-form-item__label) {
  color: #fff;
}
.login {
  width: 100vw;
  height: 100vh;
  background-image: url(@/image/bgm_image.png);
  background-size: cover; /* 背景图片覆盖整个容器 */
  display: flex;
  align-items: center;
  justify-content: right;
  padding-right: 15%;
  .box {
    width: 400px;
    .dialog-head {
      margin: 10px 0 40px 0;
      .dialog-h2 {
        font-size: 24px;
        display: inline-block;
        padding-right: 10px;
      }
    }
    .el-button {
      width: 100%;
      font-weight: 600;
    }
    .el-icon {
      position: relative;
      left: -6px;
    }
  }
}
</style>
