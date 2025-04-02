<template>
  <div class="main-content">
    <div>消息中心</div>
    <el-divider></el-divider>

    <el-row>
      <el-col :span="4">
        <div class="custom-tree">
          <ul>
            <li v-for="item in treeList" :key="item.id">
              <a
                :class="cut === item.name ? 'cut' : 'cut_hover'"
                @click="onTree(item.name!)"
              >
                <el-divider
                  direction="vertical"
                  :class="
                    cut === item.name ? 'custom-highlight' : 'custom-divider'
                  "
                />
                <SvgIcon
                  :name="cut === item.name ? item.icon2 : item.icon"
                  class="svg-icon"
                ></SvgIcon>
                <span>{{ item.label }}</span>
                <span class="num">{{ item.num }}</span>
              </a>
            </li>
          </ul>
        </div>
      </el-col>

      <el-col :span="20">
        <div class="content-container">
          <div class="content-settings" v-if="ListData.length > 0">
            <span
              class="content-unread"
              v-if="notificationNum > 0"
              @click="onRead(cut)"
            >
              全部标记为已读
            </span>
            <span class="content-read" v-else>全部已读</span>
          </div>

          <el-tabs v-model="tabsName" class="demo-tabs">
            <el-tab-pane :label="titleName" name="all"></el-tab-pane>

            <div class="demo-container" v-if="ListData.length > 0">
              <ul class="list-ul">
                <!-- 系统消息 -->
                <!-- <li class="list-item">
                  <div class="img-avatar">
                    <div class="avatar">
                      <SvgIcon
                        name="icon-unread-message"
                        width="32"
                        height="32"
                        class="svg-icon img"
                      ></SvgIcon>
                    </div>
                  </div>
                  <div class="bd">
                    <div class="content">
                      <span class="title"> <span>【公告】</span></span>
                      <span class="unread">系统已初步建立完成</span>
                    </div>
                    <div class="ft">
                      <span>8 个月前</span>
                    </div>
                  </div>
                  <div class="more">
                    <el-button type="info" round color="#ddd">查看</el-button>
                  </div>
                </li> -->

                <!-- 通知 -->
                <li
                  class="list-item"
                  v-for="(item, index) in ListData"
                  :key="index"
                >
                  <div class="img-avatar">
                    <div class="avatar">
                      <SvgIcon
                        :name="
                          item.status == 1
                            ? 'icon-notification'
                            : 'cion-read-message'
                        "
                        width="32"
                        height="32"
                        class="svg-icon img"
                      ></SvgIcon>
                    </div>
                  </div>

                  <div class="bd">
                    <div class="content">
                      <span class="title"> <span>【通知】</span></span>
                      <span :class="item.status == 1 ? 'unread' : 'read'">
                        {{ item.title }}
                      </span>
                      <p class="read p">
                        {{ item.content }}
                      </p>
                    </div>

                    <div class="ft">
                      <span>{{ formatDate(item.createTime) }}</span>
                    </div>
                  </div>
                  <div class="more">
                    <el-button
                      type="info"
                      round
                      color="#ddd"
                      @click="onUpdateStatus(item.id!, item.event!)"
                    >
                      查看
                    </el-button>
                  </div>
                </li>
              </ul>
            </div>

            <!-- 暂无消息 -->
            <div class="block-no-results" v-else>
              <SvgIcon
                name="icon-no-news"
                width="160"
                height="139"
                class="svg-icon"
              ></SvgIcon>
              <div class="message">暂无消息</div>
            </div>

            <!-- 分页 -->
            <div v-if="paging.total && paging.total > 8">
              <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                v-model:current-page="paging.currentPage"
                :page-sizes="[10, 20, 50, 100]"
                :page-size="paging.pageSize"
                layout="total, prev, pager, next, sizes"
                :total="paging.total"
                class="pagination"
              ></el-pagination>
            </div>
          </el-tabs>
        </div>
      </el-col>
    </el-row>
  </div>
  <!-- <MessageDrawerComponents /> -->
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from "vue";
import SvgIcon from "@/components/SvgIcon/index.vue";
import { messageCenter } from "@/common/data";
import { formatDate } from "@/utils/other";
import {
  $queryCount,
  $queryMessageList,
  $updateAllStatus,
  $updateStatus,
} from "@/api/messagecenter";
import { MessageList, TreeList } from "@/interface/messagecenter";
import { Returndata } from "@/common/enum";
import { useRoute, useRouter } from "vue-router";
import { PagingPar } from "@/interface/common";
import eventBus from "@/common/eventBus";
// import MessageDrawerComponents from "./components/MessageDrawer.vue";

// 路由
const route = useRoute();
const router = useRouter();

// 左侧树选中
const cut = ref<string>("systemmessage");
// 左侧树 列表
const treeList = ref<TreeList[]>([]);
// 通知未读数量
const notificationNum = ref<number>(0);

// 右侧树 tabs 标签页
const tabsName = ref<string>("all");
// 内容 tabs 标签页
const titleName = ref<string>("");

// 信息列表
const ListData = ref<MessageList[]>([]);
/**
 * 分页
 * @currentPage 当前页码
 * @pageSize 当前条数
 * @total 总条数
 */
const paging = reactive<PagingPar>({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});

/**
 * 系统消息
 * 通知
 */
const onTree = (name: string) => {
  treeList.value = messageCenter;
  cut.value = name;

  // 路由切换
  sessionStorage.setItem("active", "/messagecenter/" + name);
  router.push("/messagecenter/" + name);

  queryNum(); // 查询未读总数
  if (name === "systemmessage") {
    titleName.value = "全部";
    ListData.value = [];
    paging.total = 0;
  } else {
    titleName.value = "邀请";
    queryList();
  }
};

/**
 * 通知
 * @查询列表
 */
const queryList = () => {
  $queryMessageList(paging.currentPage!, paging.pageSize!)
    .then((res: any) => {
      if (res.data && res.data.records.length > 0) {
        ListData.value = res.data.records;
        paging.currentPage = res.data.current;
        paging.pageSize = res.data.size;
        paging.total = res.data.total;
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};
// 分页
const handleCurrentChange = (val: number) => {
  paging.currentPage = val;
  queryList();
};

// 每页显示多少条
const handleSizeChange = (val: number) => {
  paging.pageSize = val;
  queryList();
};

/**
 * 通知
 * @查询未读总数
 */
const queryNum = () => {
  $queryCount()
    .then((res: any) => {
      if (res.code === Returndata.code) {
        treeList.value[1].num = res.data.unreadCount;
        notificationNum.value = res.data.unreadCount;
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

/**
 * 通知
 * @消息查看后变更状态
 */
const onUpdateStatus = (id: string, val: string) => {
  onLink(val); // 跳转超链接

  $updateStatus(id)
    .then((res: any) => {
      if (res.code === Returndata.code) {
        const index = ListData.value.findIndex((item) => item.id === id);
        ListData.value[index].status = 0;
        queryNum(); // 查询未读总数
        /**
         * index.vue 消息总数
         * 调用 查询方法
         */
        eventBus.emit("onMessageEventBus");
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

/**
 * 通知
 * @全部设置已读
 */
const onRead = (name: string) => {
  $updateAllStatus()
    .then((res: any) => {
      if (res.code === Returndata.code) {
        onTree(name);
        eventBus.emit("onMessageEventBus");
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

/**
 * 跳转超链接
 * @ledgerEdit 台账功能 标识
 */
const onLink = (val: string) => {
  if (val === "ledgerEdit") {
    router.push({
      path: "/ledger",
      query: { tabIdentification: "assigntome" },
    });

    // 存储选中后的菜单项
    sessionStorage.setItem("active", "/ledger");
    eventBus.emit("onActiveEventBus", "/ledger");
  }
};

watch(
  () => route.path,
  (newVal, _oldVal) => {
    const parts = newVal.split("/");
    const name = parts.slice(-1).join("/");
    onTree(name);
  }
);

onMounted(() => {
  // 获取路由地址
  const path = route.path;
  const parts = path.split("/");
  const name = parts.slice(-1).join("/");
  onTree(name);
});
</script>

<style scoped lang="less">
// 按钮
::v-deep(.el-button) {
  font-size: 12px;
}

.custom-tree {
  font-size: 14px;
  a {
    height: 40px;
    display: block;
    line-height: 40px;
    color: #999;
    margin: 0 2px 0 2px;
    padding: 0 0 0 24px;
    cursor: pointer;
  }
  .custom-divider {
    border-left: 3px solid #ffffff;
    margin-right: 24px;
  }
  .custom-highlight {
    border-left: 3px solid #6772ff;
    height: 20px;
    margin-right: 24px;
    position: relative;
    top: -1px;
  }

  .svg-icon {
    position: relative;
    top: 4px;
    margin-right: 10px;
  }

  .num {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    height: 16px;
    margin-left: 1em;
    background: #e4e4e4;
    color: #666;
    border-radius: 999px;
    font-size: 10px;
    padding: 0 0.5em;
    position: relative;
    top: -1px;
  }

  .cut {
    height: 40px;
    line-height: 40px;
    color: #333;
    margin: 0 2px 0 2px;
  }
  .cut_hover:hover {
    height: 40px;
    line-height: 40px;
    color: #333;
    margin: 0 2px 0 2px;
  }
}

.content-container {
  width: 96%;

  .content-settings {
    display: inline-block;
    position: absolute;
    right: 58px;
    top: 8px;
    z-index: 100;
    .content-unread {
      font-size: 12px;
      color: #4a54ff;
      cursor: pointer;
    }
    .content-read {
      font-size: 12px;
      color: #666;
    }
  }

  ::v-deep(.el-tabs__item) {
    width: 72px;
    font-size: 16px;
    color: #333;
  }

  .demo-tabs > .el-tabs__content {
    padding: 32px;
  }
  .demo-container {
    width: 100%;
    height: 534px;
    overflow: auto;
    border-bottom: 1px solid #ececec;
  }
  .list-ul {
    .list-item {
      display: flex;
      border-bottom: 1px solid #ececec;
      padding: 16px 24px;

      .img-avatar {
        display: flex;
        align-items: center;
        .avatar {
          width: 32px;
          height: 32px;
          margin-right: 10px;
          position: relative;
          .img {
            border-radius: 9999px;
            overflow: hidden;
            object-fit: cover;
          }
        }
      }

      .bd {
        flex: 1;
        max-width: 86%;
        font-size: 12px;
        .content {
          margin-right: 3em;
          max-height: 50px;
          overflow: hidden;

          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
          .title {
            color: #333;
          }
          // 未读
          .unread {
            color: #333;
          }
          // 已读
          .read {
            color: #999;
          }
          .p {
            padding-left: 4px;
          }
        }
        .ft {
          color: #999;
          margin-top: 8px;

          .skip {
            margin-left: 50px;
          }
        }
      }

      .more {
        align-self: center;
        margin-left: auto;
      }
    }
  }
}

.demo-container::-webkit-scrollbar {
  width: 4px;
  height: 10px;
  overflow: visible;
}
.demo-container::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
.demo-container::-webkit-scrollbar-thumb {
  border: solid transparent;
  border-width: 1px 0;
  background-clip: padding-box;
  border-radius: 10px;
  background: #e2e6f0;
}
.demo-container::-webkit-scrollbar-thumb:hover {
  background: #d8dce6;
  border-radius: 10px;
}

// 暂无消息
.block-no-results {
  display: block;
  width: 100%;
  text-align: center;
  padding-top: 100px;

  .message {
    font-size: 16px;
    margin-top: 20px;
    color: #666;
    text-align: center;
  }
}
.pagination {
  float: right;
  margin-top: 40px;
}
</style>
