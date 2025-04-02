<template>
  <h4>设置</h4>
  <div class="info-line"></div>
  <el-form label-position="left" label-width="140" style="font-weight: 600">
    <el-form-item label="头像">
      <div class="info-avatar">
        <div class="info-size">
          <!-- <el-avatar :size="60" :src="circleUrl" class="index-avatar" /> -->
          <SvgIcon
            name="icon-user-avatar"
            width="50px"
            height="50px"
            class="index-avatar"
          ></SvgIcon>
        </div>
        <div class="info-content">
          <p class="info-title">点击更换头像</p>
          <p class="info-des">图片格式支持PNG、JPG、GIF，大小 4 MB</p>
        </div>
      </div>
    </el-form-item>
    <el-form-item label="账号">
      <el-input v-model="formInline.accountNum" disabled style="width: 400px" />
    </el-form-item>
    <el-form-item label="姓名">
      <el-input v-model="formInline.name" disabled style="width: 400px" />
    </el-form-item>
    <el-form-item label="手机号码">
      <el-input
        v-model="formInline.phone"
        placeholder="输入手机号码"
        clearable
        style="width: 400px"
      />
    </el-form-item>
    <el-form-item label="电话号码">
      <el-input
        v-model="formInline.telephone"
        placeholder="输入电话号码"
        clearable
        style="width: 400px"
      />
    </el-form-item>
    <el-form-item label="邮箱">
      <el-input
        v-model="formInline.email"
        placeholder="输入邮箱"
        clearable
        style="width: 400px"
      />
    </el-form-item>
    <el-form-item label="个人简介">
      <el-input
        v-model="formInline.desc"
        style="width: 500px"
        :rows="4"
        type="textarea"
        placeholder="输入个人简介..."
      />
    </el-form-item>
    <el-form-item class="dialog-footer">
      <el-button class="btn-style" type="primary">保存</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { onMounted, reactive } from "vue";
import SvgIcon from "@/components/SvgIcon/index.vue";
import { useUserStore } from "@/stort/user";

// 获取登录用户信息
const userStore = useUserStore();

const formInline = reactive({
  user: "",
  accountNum: "",
  name: "",
  phone: "",
  telephone: "",
  email: "",
  desc: "",
});

// 页面刷新初始化加载
onMounted(() => {
  formInline.accountNum = userStore.user.username;
  formInline.name = userStore.user.displayName;
});
</script>

<style scoped lang="less">
.info-line {
  background: #e5e5e5;
  background-color: rgb(0 0 0 / 0.06);
  height: 1px;
  margin: 20px 0;
}

.info-avatar {
  display: flex;
  .info-size {
    width: 70px;
  }
}

.info-content {
  flex: 1;

  .info-title {
    font-size: 14px;
    font-weight: 400;
    color: #409eff;
  }

  .info-des {
    font-size: 12px;
    font-weight: 400;
    color: #939090;
  }
}

.index-avatar {
  position: relative;
  top: 10px;
  margin-right: 4px;
}

.dialog-footer {
  margin-top: 40px;
  .btn-style {
    min-width: 90px;
    border-radius: 30px;
  }
}
</style>
