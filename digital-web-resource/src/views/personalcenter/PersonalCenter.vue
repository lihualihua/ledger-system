<template>
  <div class="main-content">
    <div>个人中心</div>
    <el-divider></el-divider>
    <el-row class="per-row">
      <el-col :span="5">
        <div class="per-container">
          <div class="per-avatar">
            <div class="per-svg">
              <!-- <el-avatar :size="50" :src="circleUrl" class="index-avatar" /> -->
              <SvgIcon
                name="icon-user-avatar"
                width="50px"
                height="50px"
                class="index-avatar"
              ></SvgIcon>
            </div>
            <div class="per-content">
              <p class="per-title">{{ userName }}</p>
              <p class="per-ap">活跃 0 天</p>
            </div>
          </div>

          <div class="per-line"></div>

          <div>
            <h4>个人设置</h4>
            <ul class="per-ul">
              <li
                :class="['per-li', toggle === 'profile' ? 'cut' : '']"
                @click="onCommon('profile')"
              >
                <el-icon class="per-icon"><DocumentCopy /></el-icon>编辑资料
              </li>

              <div class="per-dv-line"></div>

              <li
                :class="['per-li', toggle === 'secure' ? 'cut' : '']"
                @click="onCommon('secure')"
              >
                <el-icon class="per-icon"><Lock /></el-icon>账号安全
              </li>

              <div class="per-dv-line"></div>

              <li
                :class="['per-li', toggle === 'mes' ? 'cut' : '']"
                @click="onCommon('mes')"
              >
                <el-icon class="per-icon"><Bell /></el-icon>消息管理
                <el-badge class="per-badge" :value="totality"> </el-badge>
              </li>

              <div class="per-dv-line"></div>

              <li class="per-li">
                <el-icon class="per-icon"><Coin /></el-icon>我的积分
              </li>
            </ul>
          </div>
        </div>
      </el-col>

      <el-col :span="19">
        <div class="personal-content" v-if="active === 'profile'">
          <PersonalInfoComponents />
        </div>
        <div class="personal-content" v-else="active === 'secure'">
          <AccountSecurityComponents />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { DocumentCopy, Lock, Bell, Coin } from "@element-plus/icons-vue";
import PersonalInfoComponents from "./components/PersonalInfo.vue";
import AccountSecurityComponents from "./components/AccountSecurity.vue";
import SvgIcon from "@/components/SvgIcon/index.vue";
import { useRoute, useRouter } from "vue-router";
import { $queryCount } from "@/api/messagecenter";
import { Returndata } from "@/common/enum";
import { useUserStore } from "@/stort/user";

// 路由
const route = useRoute();
const router = useRouter();
// 获取登录用户信息
const userStore = useUserStore();

// const circleUrl = ref("../image/assets.png");

// 用户信息
const userName = ref<string>("");

// 切换 编辑资料 & 账号安全
const active = ref<string>("profile");
// 消息总数
const totality = ref<string | number>("");

// 切换
const toggle = ref<string>("profile");

/**
 * @profile 编辑资料
 * @secure 账号安全
 * @systemmessage 消息管理
 */
const onCommon = (value: string) => {
  toggle.value = value;
  if (value === "mes") {
    // 消息管理
    sessionStorage.setItem("active", "/messagecenter/systemmessage");
    active.value = "/messagecenter/systemmessage";
    router.push("/messagecenter/systemmessage");
  } else {
    active.value = value;
    // 路由切换
    tabRoute(value);
  }
};

/**
 * 消息中心
 * 查找未读通知总数
 * @查询
 */
const queryNum = () => {
  $queryCount()
    .then((res: any) => {
      if (res.code === Returndata.code) {
        if (res.data.unreadCount === 0) {
          totality.value = ""; // 消息总数
        } else {
          totality.value = res.data.unreadCount; // 消息总数
        }
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

/**
 * 切换路由
 */
const tabRoute = (name: string) => {
  sessionStorage.setItem("active", "/personalcenter/" + name);
  router.push("/personalcenter/" + name);
};

// 页面刷新初始化加载
onMounted(() => {
  // 获取路由地址
  const path = route.path;
  const parts = path.split("/");
  const name = parts.slice(-1).join("/");

  active.value = name;
  toggle.value = name;

  tabRoute(name); // 切换路由

  userName.value = userStore.user.displayName;
  queryNum(); // 查找未读通知总数
});
</script>

<style scoped lang="less">
.per-row {
  padding: 0px 12px 0 40px;

  .per-container {
    border: 1px solid #e5e5e5;
    margin: 0 20px;
    padding: 20px;
    .per-avatar {
      display: flex;
      .per-svg {
        width: 66px;
        .index-avatar {
          position: relative;
          top: 6px;
          margin-right: 4px;
        }
      }

      .per-content {
        flex: 1 1 0%;
        padding-top: 10px;
        .per-title {
          margin-bottom: 4px;
        }
        .per-ap {
          font-size: 12px;
          color: #939090;
        }
      }
    }

    .per-line {
      background: #e5e5e5;
      background-color: rgb(0 0 0 / 12%);
      height: 1px;
      margin: 20px 0;
    }

    .per-ul {
      padding: 20px 20px;

      .per-li {
        height: 36px;
        line-height: 36px;
        cursor: pointer;
        font-size: 14px;

        .per-icon {
          color: #409eff;
          font-size: 20px;
          position: relative;
          top: 5px;
          margin-right: 10px;
        }
      }

      .per-li:hover {
        color: #409eff;
      }

      .cut {
        color: #409eff;
      }

      .per-dv-line {
        background: #e5e5e5;
        background-color: rgb(0 0 0 / 12%);
        height: 1px;
        margin: 6px 0;
      }

      .per-badge {
        position: relative;
        top: 4px;
        left: 6px;
      }
    }
  }

  .personal-content {
    padding-left: 40px;
  }
}
</style>
