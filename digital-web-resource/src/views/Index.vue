<template>
  <el-container>
    <!-- 左侧栏 -->
    <h4 :class="['h5-imager', status ? 'expand' : 'fold']">
      <SvgIcon
        name="icon-vite"
        width="34px"
        height="34px"
        class="logo-icon"
      ></SvgIcon>
      <span class="title-name">{{ status ? "" : "台账数据库" }}</span>
    </h4>

    <div class="menu-container">
      <el-menu
        :router="true"
        :collapse="status"
        :unique-opened="true"
        :default-active="active"
        :collapse-transition="false"
        text-color="#bfcbd9"
        background-color="#304156"
        active-text-color="#ffffff"
        class="el-menu-vertical index-menu"
      >
        <div v-for="item in treeList" :key="item.id">
          <el-menu-item
            v-if="!item.children"
            :key="item.id"
            :index="item.path"
            @click="onDirectory(item.path!)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>

          <el-sub-menu v-else :index="item.id + ''">
            <template #title>
              <el-icon><component :is="item.icon" /></el-icon>
              <span v-show="!status">{{ item.title }}</span>
            </template>
            <el-menu-item-group>
              <template v-for="menu in item.children" :key="menu.id">
                <el-menu-item
                  :index="menu.path"
                  @click="onDirectory(menu.path!)"
                >
                  <span class="index-title">{{ menu.title }}</span>
                </el-menu-item>
              </template>
            </el-menu-item-group>
          </el-sub-menu>
        </div>
      </el-menu>
    </div>

    <!-- 右侧栏 -->
    <el-container>
      <!-- 顶部 -->
      <el-header>
        <div class="index-left">
          <el-icon>
            <component :is="status ? Expand : Fold" @click="onSwitch(status)" />
          </el-icon>
        </div>

        <div class="index-middle">
          <!-- 通知 -->
          <div class="index-item" @mouseleave="onNotification(false)">
            <span class="title" @mouseenter="onNotification(true)">
              <el-badge
                :value="totality"
                :offset="[2, 16]"
                @click="onMessage('systemmessage')"
              >
                <el-icon :size="22" color="#b1b3b8" class="notification-icon">
                  <BellFilled />
                </el-icon>
              </el-badge>
            </span>
            <ul class="common-ul" v-show="isNotification">
              <li @click="onMessage('systemmessage')">
                <p class="tte">系统消息</p>
                <p class="num">0</p>
              </li>
              <li @click="onMessage('notification')">
                <p class="tte">通知</p>
                <p class="num">{{ notificationNum }}</p>
              </li>
            </ul>
          </div>
        </div>

        <!-- 用户登录 -->
        <div class="index-right" @mouseleave="onMouse(false)">
          <p @mouseenter="onMouse(true)">
            <!-- <el-avatar :icon="UserFilled" class="index-avatar" /> -->
            <SvgIcon
              name="icon-user-avatar"
              width="30px"
              height="30px"
              class="index-avatar"
            ></SvgIcon>
            {{ userName }}
          </p>
          <ul class="user-ul" v-show="isShow">
            <li @click="onPersonal">
              <el-icon :size="18" class="li-icon"><User /></el-icon>
              个人中心
            </li>
            <li @click="exit">
              <el-icon :size="18" class="li-icon"><SwitchButton /></el-icon>
              退出登录
            </li>
          </ul>
        </div>
      </el-header>

      <!-- 控制台 -->
      <el-main class="index-content">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  Fold,
  Grid,
  Expand,
  Promotion,
  HomeFilled,
  Management,
  SwitchButton,
  BellFilled,
  Platform,
  User,
} from "@element-plus/icons-vue";
import { useUserStore } from "@/stort/user";
import { Permission, Prompt, Returndata } from "@/common/enum";
import { $queryThreeRout } from "@/api/document";
import eventBus from "@/common/eventBus";
import { NavigationTree } from "@/interface/common";
import { $queryCount } from "@/api/messagecenter";
import SvgIcon from "@/components/SvgIcon/index.vue";

const userStore = useUserStore();
const router = useRouter();

// 通知
const isNotification = ref<boolean>(false);
// 通知未读总数
const notificationNum = ref<number>(0);
// 消息总数
const totality = ref<string | number>("");

// 用户登录
const isShow = ref<boolean>(false);
const status = ref<boolean>(false);
const active = ref<string>("");
const userName = ref<string>("");

// 左侧菜单栏
const treeList = ref<NavigationTree[]>([]);

// 超级管理员 || 部门管理员路由
const treeAdminList = reactive<NavigationTree[]>([
  {
    id: 2,
    icon: shallowRef(Management),
    title: "文档管理",
    children: [
      {
        id: 21,
        icon: " ",
        title: "公共文档",
        path: "/document/commondoc",
      },
      {
        id: 22,
        icon: " ",
        title: "我的文档",
        path: "/document/minefile",
      },
      {
        id: 23,
        icon: " ",
        title: "部门文档",
        path: "/document/section",
      },
      {
        id: 24,
        icon: " ",
        title: "收藏夹",
        path: "/document/collect",
      },
      {
        id: 25,
        icon: " ",
        title: "分享记录",
        path: "/document/share",
      },
      {
        id: 26,
        icon: " ",
        title: "回收站",
        path: "/document/recycle",
      },
      { id: 27, icon: " ", title: "智能搜索", path: "/search" },
    ],
  },
  {
    id: 3,
    icon: shallowRef(Grid),
    title: "常用工具",
    path: "/businessSupport",
    children: [
      {
        id: 41,
        icon: " ",
        title: "通信费计算",
        path: "/communication",
      },
    ],
  },
  {
    id: 4,
    icon: shallowRef(Promotion),
    title: "业务导航",
    path: "/servicenavigation",
  },
  {
    id: 1,
    icon: shallowRef(HomeFilled),
    title: "工作台",
    path: "/index",
  },
  // {
  //   id: 6,
  //   icon: shallowRef(Tools),
  //   title: "权限管理",
  //   children: [
  //     { icon: " ", title: "用户管理", path: "/user" },
  //     { icon: " ", title: "角色管理", path: "/role" },
  //     { icon: " ", title: "菜单管理", path: "/menu" },
  //     { icon: " ", title: "权限管理", path: "/permission" },
  //   ],
  // },
  {
    id: 5,
    icon: shallowRef(Platform),
    title: "台账管理",
    path: "/ledger",
  },
]);
// 普通用户
const treeUserList = reactive<NavigationTree[]>([
  {
    id: 2,
    icon: shallowRef(Management),
    title: "文档管理",
    children: [
      {
        id: 21,
        icon: " ",
        title: "公共文档",
        path: "/document/commondoc",
      },
      {
        id: 22,
        icon: " ",
        title: "我的文档",
        path: "/document/minefile",
      },
      {
        id: 23,
        icon: " ",
        title: "部门文档",
        path: "/document/section",
      },
      {
        id: 24,
        icon: " ",
        title: "收藏夹",
        path: "/document/collect",
      },
      {
        id: 25,
        icon: " ",
        title: "分享记录",
        path: "/document/share",
      },
      {
        id: 26,
        icon: " ",
        title: "回收站",
        path: "/document/recycle",
      },
      { id: 27, icon: " ", title: "智能搜索", path: "/search" },
    ],
  },
  {
    id: 3,
    icon: shallowRef(Grid),
    title: "常用工具",
    path: "/businessSupport",
    children: [
      {
        id: 41,
        icon: " ",
        title: "通信费计算",
        path: "/communication",
      },
    ],
  },
  {
    id: 1,
    icon: shallowRef(HomeFilled),
    title: "工作台",
    path: "/index",
  },
  {
    id: 4,
    icon: shallowRef(Platform),
    title: "台账管理",
    path: "/ledger",
  },
]);

// 菜单根据id进行排序
treeAdminList.sort(function (a: any, b: any) {
  return a.id - b.id;
});
// 菜单根据id进行排序
treeUserList.sort(function (a: any, b: any) {
  return a.id - b.id;
});

/**
 * 消息中心
 */
const onMessage = (name: string) => {
  sessionStorage.setItem("active", "/messagecenter/" + name);
  active.value = "/messagecenter/" + name;
  router.push("/messagecenter/" + name);
};

/**
 * 消息中心
 * 调用 查询方法
 */
eventBus.on("onMessageEventBus", () => {
  queryNum(); // 消息中心
});

/**
 * 消息中心
 * 查找未读通知总数
 * @查询
 */
const queryNum = () => {
  $queryCount()
    .then((res: any) => {
      if (res.code === Returndata.code) {
        notificationNum.value = res.data.unreadCount;
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
 * 左侧菜单栏
 * @查询部门
 */
const querySection = async (userid?: string) => {
  const ret = await $queryThreeRout(userid);
  if (ret.code === Returndata.code) {
    const data = ret.data;
    if (data.length > 0) {
      // 普通用户
      for (let i = 0; i < treeUserList.length; i++) {
        const title = treeUserList[i].title;
        if (title === "文档管理") {
          const list = treeUserList[i].children!;
          for (let j = 0; j < list.length; j++) {
            const name = list[j].title;
            if (name === "部门文档") {
              list[j].title = data[0].name;
            }
          }
        }
      }
    }
  } else {
    console.log(ret);
  }
};

/**
 * 存储选中后的菜单项
 */
const onDirectory = (path: string) => {
  /**
   * 消息中心
   * 通知 查询未读消息总数
   */
  queryNum();

  // 台账路由特殊处理
  if (path === "/ledger") {
    router.push({
      path: "/ledger",
      query: { tabIdentification: "myallocation" },
    });
  }

  // 业务导航
  if (path === "/servicenavigation") {
    router.push({
      path: "/servicenavigation",
      query: { tabIdentification: "servicenavigation" },
    });
  }

  sessionStorage.setItem("active", path);
  active.value = path;
};
/**
 * 存储选中后的菜单项
 */
eventBus.on("onActiveEventBus", (param: any) => {
  active.value = param;
});

// 展开、缩进
const onSwitch = (boo: boolean) => {
  status.value = !boo;
};

// 鼠标移入/移出事件
const onMouse = (boo: boolean) => {
  isShow.value = boo;
};

/**
 * 通知
 * 鼠标移入/移出事件
 */
const onNotification = (boo: boolean) => {
  isNotification.value = boo;
};

/**
 * 个人中心
 */
const onPersonal = () => {
  sessionStorage.setItem("active", "/personalcenter/profile");
  active.value = "/personalcenter/profile";
  router.push("/personalcenter/profile");
};

// 退出登录
const exit = () => {
  ElMessage({
    message: Prompt.LOG_OUT,
    type: "success",
  });
  userStore.clearUser();
  router.push("/");
};

// 页面刷新初始化加载
onMounted(() => {
  queryNum(); // 查找未读通知总数

  // 调用接口打开
  if (!userStore.user.username) {
    router.push("/");
  } else {
    userName.value = userStore.user.displayName;

    // 普通用户
    const { userid, permissions } = userStore.user;
    let roleCode = "";
    permissions.forEach((res: any) => {
      roleCode = res.roleCode;
    });
    // 普通用户
    if (roleCode === Permission.general_user) {
      treeList.value = treeUserList;
      querySection(userid);
    }
    // 超级管理员 || 部门管理员路由
    if (
      roleCode == Permission.super_admin ||
      roleCode == Permission.edpt_admin
    ) {
      treeList.value = treeAdminList;
    }
  }

  const activeValue = sessionStorage.getItem("active");
  if (activeValue !== null) {
    active.value = activeValue;
    router.push(activeValue);
  } else {
    active.value = "/index";
  }
});
</script>

<style scoped lang="less">
::v-deep(.el-main) {
  --el-main-padding: 16px;
}
.h5-imager {
  height: 59px;
  color: #fff;
  text-align: center;
  line-height: 59px;
  position: absolute;
  background-color: rgb(48, 65, 86);

  .logo-icon {
    position: relative;
    top: 12px;
    margin-right: 4px;
  }
  .title-name {
    font-size: 15px;
  }
}
.expand {
  width: 64px;
}
.fold {
  width: 210px;
}

.index-menu {
  top: 59px;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
  border-right: #b3c0d1;
  height: calc(100vh - 59px);
  .index-title {
    display: block;
    width: 200px;
    padding-left: 20px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
/* 整个滚动条 */
.index-menu::-webkit-scrollbar {
  width: 1px; /* 设置滚动条的宽度 */
}
.index-menu::-webkit-scrollbar-track {
  background: #303133; /* 设置轨道的背景颜色 */
}
/* 滚动条的滑块 */
.index-menu::-webkit-scrollbar-thumb {
  background: #303133; /* 设置滑块的背景颜色 */
}

.index-left {
  order: 1;
  flex: 0 0 80px;
  font-size: large;
  line-height: 60px;
  padding-left: 15px;
  i {
    font-size: 24px;
    cursor: pointer;
  }
}

.index-middle {
  flex: 1; /* 填充剩余空间 */
  order: 2; /* 中间 */
  color: #303133;
  line-height: 60px;
  font-size: 14px;
  padding: 0 14px 0 20px;
  text-align: right;
  .index-item {
    min-width: 55px;
    text-align: center;
    display: inline-block;
    .title {
      cursor: pointer;
      .notification-icon {
        top: 4px;
      }
      .notification-icon:hover {
        color: #73767a;
      }
    }
    .title:hover {
      color: #409eff;
    }
  }
}

.common-ul {
  width: 140px;
  padding: 10px 0;
  border-radius: 4px;
  cursor: pointer;
  position: absolute;
  z-index: 200;
  background-color: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0px 5px 12px 4px rgba(0, 0, 0, 0.08);
  li {
    display: flex;
    height: 40px;
    line-height: 40px;
    text-align: left;
    padding: 0 20px;
    .tte {
      width: 100px;
    }
    .num {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      height: 18px;
      margin-left: 1em;
      background: #e4e4e4;
      color: #666;
      border-radius: 999px;
      font-size: 12px;
      padding: 0.2em 0.7em;
      position: relative;
      top: 12px;
    }
  }
  li:hover {
    color: #409eff;
    border-radius: 4px;
    background-color: #f4f4f4;
    .num {
      color: #409eff;
    }
  }
}

.index-right {
  order: 3; /* 右边 */
  flex: 0 0 150px; /* 固定宽度 */
  font-size: 14px;
  cursor: pointer;
  line-height: 60px;
  .index-avatar {
    position: relative;
    top: 10px;
    margin-right: 4px;
  }
  .user-ul {
    width: 140px;
    padding: 10px 0;
    border-radius: 4px;
    position: absolute;
    z-index: 200;
    background-color: #ffffff;
    border: 1px solid rgba(0, 0, 0, 0.08);
    box-shadow: 0px 5px 12px 4px rgba(0, 0, 0, 0.08);
    li {
      height: 40px;
      line-height: 40px;
      text-align: center;
      .li-icon {
        top: 4px;
      }
    }
    li:hover {
      color: #409eff;
      border-radius: 4px;
      background-color: #f4f4f4;
    }
  }
}
// 控制台
.index-content {
  overflow-y: scroll;
  height: calc(100vh - 60px);
  background-color: #f0f2f5;
}
/* 整个滚动条 */
.index-content::-webkit-scrollbar {
  width: 8px;
  height: 10px;
  overflow: visible;
}
.index-content::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
.index-content::-webkit-scrollbar-thumb {
  border: solid transparent;
  border-width: 1px 0;
  background-clip: padding-box;
  background: #d8dce5;
}

.menu-container {
  // <el-menu> 菜单组件样式
  ::v-deep(.el-menu-item) {
    border-left: 3px solid #304156;
  }
  ::v-deep(.el-menu-item.is-active) {
    background-color: rgb(38, 52, 69);
    border-left: 3px solid #409eff;
  }
  ::v-deep(.el-menu-vertical:not(.el-menu--collapse)) {
    width: 210px;
  }
  ::v-deep(.el-menu-item-group__title) {
    padding: 0;
  }
  ::v-deep(.el-menu-item [class^="el-icon"]) {
    margin-left: -3px;
  }
}
// el-header 组件样式 样式
::v-deep(.el-header, .el-footer) {
  padding: 0;
  display: flex;
  background-color: #fff;
}
</style>
