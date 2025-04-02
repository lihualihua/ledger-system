<template>
  <el-row :gutter="24">
    <el-col :span="12" style="padding-right: 4px; padding-left: 4px">
      <div class="workbench">
        <img src="../../image/workbench-img.png" class="container-image" />
        <div class="container-content">
          <span class="container-title">{{ salutation }}</span>
          <div class="container-file">
            <div class="container-common">
              <ul class="common-ul">
                <li class="common-title">公共文件</li>
                <li class="common-num">{{ fileTotal.commonFileCount }}</li>
              </ul>
            </div>
            <div class="container-my">
              <ul class="common-ul">
                <li class="common-title">我的文件</li>
                <li class="common-num">{{ fileTotal.userFileCount }}</li>
              </ul>
            </div>
            <div class="container-section">
              <ul class="common-ul">
                <li class="common-title">部门文件</li>
                <li class="common-num">{{ fileTotal.deptFileCount }}</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </el-col>

    <!-- 常用功能 -->
    <el-col :span="12" style="padding-right: 4px; padding-left: 4px">
      <ShortcutComponents />
    </el-col>
  </el-row>

  <el-row :gutter="24">
    <el-col :span="24" style="padding-right: 4px; padding-left: 4px">
      <div
        class="home-line"
        v-loading="loading"
        element-loading-text="数据加载中..."
      >
        <div class="feature-title">
          <span class="title-name">业务导航</span>
        </div>
        <div class="file-navigation">
          <ul class="service-content">
            <li
              class="service-li"
              v-for="item in editableTabs"
              :key="item.id"
              @click="onView(item.id)"
            >
              <p class="service-name">
                <span>{{ item.name }}</span>
              </p>
            </li>

            <li class="service-tips" v-if="editableTabs.length === 0">
              <span class="service-span">暂无数据</span>
            </li>
          </ul>
        </div>
      </div>
    </el-col>
  </el-row>

  <el-row :gutter="24">
    <el-col :span="24" style="padding-right: 4px; padding-left: 4px">
      <div class="next-part">
        <div class="feature-title">
          <span class="title-name">审批</span>
          <span class="title-more">
            查看全部
            <el-icon class="icon"><ArrowRight /></el-icon>
          </span>
        </div>
        <div class="check">
          <ul class="check-ul">
            <li class="check-tips">
              <span class="check-span">暂无数据</span>
            </li>
          </ul>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ArrowRight } from "@element-plus/icons-vue";
import { getGreeting } from "@/utils/other";
import { useUserStore } from "@/stort/user";
import { $queryFileCount } from "@/api/workbench";
import { FileTotality } from "@/interface/workbench";
import { $queryDomainList } from "@/api/servicenavigation";
import { Returndata } from "@/common/enum";
import { FlowTable } from "@/interface/servicenavigation";
import ShortcutComponents from "./components/Shortcut.vue";
import { useRouter } from "vue-router";

// 路由
const router = useRouter();
const userStore = useUserStore();

// 提示语，包含 上午好、中午好、下午好等
const salutation = ref<string>("");
// 业务导航
const editableTabs = ref<FlowTable[]>([]);
// 业务导航 加载loading
const loading = ref<boolean>(false);

/**
 * 文件总数
 * @commonFileCount 公共文档
 * @userFileCount 个人文档
 * @deptFileCount 部门文档
 */
const fileTotal = reactive<FileTotality>({
  commonFileCount: "",
  userFileCount: "",
  deptFileCount: "",
});

/**
 * 查询文件总数
 */
const fileCount = () => {
  $queryFileCount()
    .then((res: any) => {
      const data = res.data;
      fileTotal.commonFileCount = data.commonFileCount;
      fileTotal.userFileCount = data.userFileCount;
      fileTotal.deptFileCount = data.deptFileCount;
    })
    .catch((err) => {
      fileTotal.commonFileCount = 0;
      fileTotal.userFileCount = 0;
      fileTotal.deptFileCount = 0;
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

/**
 * 查询业务导航
 */
const queryList = () => {
  loading.value = true;
  const status = "?status=1";
  $queryDomainList(status)
    .then((res: any) => {
      if (res.code === Returndata.code && res.data.length > 0) {
        editableTabs.value = res.data;
        loading.value = false;
      }
    })
    .catch((err) => {
      editableTabs.value = [];
      console.log("处理错误信息：", err);
    })
    .finally(() => {
      loading.value = false;
    });
};

/**
 * 点击查看业务导航详细信息
 */
const onView = (id?: string) => {
  const route = router.resolve({
    path: "/userhelpdocument", // 目标路由的路径
    query: { id: id }, // 携带的参数
  });
  // 使用 window.open 打开新窗口
  window.open(route.href, "_blank");
};

onMounted(() => {
  // 如果选择这种方式，你需要定义一个响应式数据属性来存储问候语
  let name = "";
  const { displayName, username } = userStore.user;
  username == "admin" ? (name = "超级管理员") : (name = displayName);
  const date = getGreeting();
  salutation.value = date + name + "，欢迎您回来。";

  fileCount(); // 查询文件总数
  queryList(); // 查询业务导航
});
</script>

<style scoped lang="less">
.workbench {
  width: 100%;
  height: 240px;
  border-radius: 12px;
  margin-bottom: 10px;
  background: #fafafa;
  border: 0px solid #e4e7ed;
  box-shadow: 0px 0px 4px rgba(0, 0, 0, 0.1);
}
// 背景图片
.container-image {
  width: 100%;
  height: 240px;
  border-radius: 12px;
}

//背景图片内容
.container-content {
  width: 32%;
  height: 200px;
  position: absolute;
  top: 20px;
  left: 36px;

  .container-title {
    position: absolute;
    top: 20px;
    left: 2px;
    font-weight: 700;
    font-size: 18px;
  }
  .container-file {
    width: 100%;
    height: 120px;
    display: flex;
    position: relative;
    top: 78px;
    .container-common {
      flex: 1 1 0%;
    }
    .container-my {
      width: 34%;
      padding-left: 6%;
    }
    .container-section {
      width: 35%;
      padding-left: 6%;
    }

    .common-ul {
      cursor: pointer;
      margin-top: 36px;
      text-align: center;
      .common-title {
        margin-bottom: 6px;
        font-weight: 400;
      }
      .common-num {
        font-size: 20px;
        font-weight: 600;
      }
    }
  }
}

// 标题公共
.feature-title {
  width: 100%;
  height: 50px;
  line-height: 64px;
  .title-name {
    padding: 0 26px;
    font-weight: 600;
    margin-bottom: 4px;
  }
  .title-more {
    height: 50px;
    float: right;
    font-size: 12px;
    padding: 0 16px;
    cursor: pointer;
    color: #929395;
    text-decoration: none;
    .shortcut {
      font-size: 18px;
    }
    .shortcut:hover {
      color: #409eff;
    }
    .icon {
      position: relative;
      top: 1px;
      left: 2px;
    }
  }
  .title-more:hover {
    text-decoration: underline;
  }
}

.next-part {
  width: 100%;
  height: 306px;
  border-radius: 12px;
  background: #fafafa;
  border: 0px solid #e4e7ed;
  box-shadow: 0px 0px 4px rgba(0, 0, 0, 0.1);
}

//审批
.check {
  width: 100%;
  height: 240px;
  .check-ul {
    width: 100%;
    height: 240px;
    padding: 0 20px 0 26px;
    .check-li {
      height: 50px;
      line-height: 50px;
      display: flex;
      .check-img {
        flex: 1 1 0%;
        margin-right: 6px;
        .check-icon {
          width: 18px;
          height: 18px;
          display: inline-block;
          position: relative;
          top: 3px;
        }
      }
      .check-title {
        width: 280px;
        font-size: 14px;
        .check-h4 {
          height: 26px;
          line-height: 26px;
          padding-top: 4px;
        }
        .check-describe {
          display: block;
          height: 20px;
          line-height: 20px;
          font-size: 12px;
          color: #929395;
        }
      }
      .check-time {
        width: 90px;
        font-size: 12px;
        color: #929395;
        text-align: right;
      }
    }

    .check-tips {
      height: 50px;
      line-height: 50px;
      text-align: center;
      .check-span {
        color: #b8b9ba;
      }
    }
  }
}

// 业务导航
.home-line {
  width: 100%;
  min-height: 260px;
  margin-bottom: 10px;
  border-radius: 12px;
  background: #fafafa;
  border: 0px solid #e4e7ed;
  box-shadow: 0px 0px 4px rgba(0, 0, 0, 0.1);

  .file-navigation {
    padding: 10px 26px;
    .service-content {
      font-size: 14px;
      overflow: hidden;
      .service-li {
        width: 18%;
        height: 40px;
        line-height: 40px;
        padding: 0 10px;
        float: left;
        margin: 10px 13px 10px 18px;
        text-align: center;
        display: flex;
        cursor: pointer;
        color: #606266;
        border-radius: 4px;
        border: 1px solid #dcdfe6;
        background-color: #f2f6fc;

        .service-name {
          flex-grow: 1;
        }
      }

      .service-li:hover {
        color: #409eff;
        font-weight: 600;
        border: 1px solid #409eff;
      }

      .service-tips {
        width: 100%;
        font-size: 16px;
        min-height: 160px;
        line-height: 160px;
        text-align: center;
        .service-span {
          color: #b8b9ba;
        }
      }
    }
  }
}
</style>
