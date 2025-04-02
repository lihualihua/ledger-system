<template>
  <el-dialog
    draggable
    v-model="props.shareGoals.visible"
    title="选择分享用户"
    width="700"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    :before-close="handleClose"
  >
    <div class="share-dialog">
      <div class="search-style">
        <input
          v-model="searchName"
          placeholder="搜索全部用户"
          class="input-search"
          @change="searchQuery"
        />
        <Search class="img-search" @click="searchQuery" />
      </div>
      <el-tabs v-model="activeName" class="demo-tabs">
        <el-tab-pane label="分享用户" name="first">
          <div class="personage-content">
            <div class="personage-left">
              <ul class="section-ul">
                <li
                  :class="['section-li', item.deptId == sectionID ? 'cut' : '']"
                  v-for="(item, index) in sectionList"
                  :key="index"
                >
                  <p class="section-p" @click="onSection(item.deptId)">
                    {{ item.name }}
                  </p>
                </li>
              </ul>
            </div>

            <div class="personage-right">
              <ul class="list-ul">
                <li
                  v-if="userList.length > 0"
                  v-for="(item, index) in userList"
                  :key="index"
                  :class="['list-li', item.checked ? 'active' : '']"
                  @click="
                    onUser(
                      item.userId!,
                      item.displayName!,
                      item.username!,
                      item.checked
                    )
                  "
                >
                  <p class="list-name">
                    <el-icon class="list-icon"><Avatar /></el-icon>
                    <span>{{ item.displayName }} - {{ item.username }}</span>
                  </p>
                  <p class="list-select" v-show="item.checked">
                    <el-icon><Select /></el-icon>
                  </p>
                </li>

                <li class="check-tips" v-else>
                  <span class="check-span">暂无数据</span>
                </li>
              </ul>
            </div>
          </div>

          <p class="sharer-people">
            分享人<span class="sharer-num"> ( {{ userArray.length }} )</span>
          </p>
          <div>
            <el-tag
              v-for="item in userArray"
              :key="item.userId"
              closable
              type="primary"
              style="margin: 4px 10px"
              @close="
                onUser(item.userId!, item.displayName!, item.username!, true)
              "
            >
              {{ item.displayName }}
            </el-tag>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-style" @click="handleClose"> 取消 </el-button>
        <el-button class="btn-style" type="primary" @click="handleOK">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { $querySectionList, $queryUserList } from "@/api/common";
import { SectionList, UserList } from "@/interface/common";
import { Select, Avatar, Search } from "@element-plus/icons-vue";
import eventBus from "@/common/eventBus";

const props = defineProps({
  shareGoals: {
    type: Object,
    default: {},
  },
});

/**
 * 子组件传参给父组件
 * 收藏夹、回收站
 */
const emit = defineEmits(["share-event"]);

const activeName = ref("first");

// 部门列表数组
const sectionList = ref<SectionList[]>([]);
// 获取部门id
const sectionID = ref<number>();
// 用户列表数组
const userList = ref<UserList[]>([]);
// 获取用户ID
const userIDArray = ref<string[]>([]);
// 获取用户ID和名称
const userArray = ref<UserList[]>([]);
// 搜索参数
const searchName = ref<string>("");

/**
 * 查询部门列表
 */
const querySection = () => {
  $querySectionList()
    .then((res: any) => {
      const data = res.data.records;
      sectionID.value = data[0].deptId;
      sectionList.value = data;
      queryList();
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

/**
 * 分享个人
 * 点击部门事件
 */
const onSection = (id: number) => {
  sectionID.value = id;
  searchName.value = "";
  queryList();
};

/**
 * 分享用户
 * 点击用户事件
 */
const onUser = (id: string, name: string, username: string, bool?: boolean) => {
  if (!bool) {
    const displayName = name + " - " + username;
    userArray.value.push({ userId: id, displayName });

    userIDArray.value.push(id);
    userList.value.forEach((res: UserList) => {
      if (id === res.userId) {
        res.checked = true;
      }
    });
  } else {
    // 如果复选框被取消勾选，从 userIDArray 中移除 ID
    userArray.value = userArray.value.filter((item) => item.userId !== id);
    userIDArray.value = userIDArray.value.filter((item) => item !== id);
    userList.value.forEach((res: UserList) => {
      if (id === res.userId) {
        res.checked = false;
      }
    });
  }
};

/**
 * 搜索查询
 *
 */
const searchQuery = () => {
  sectionID.value = null!;
  queryList();
};

/**
 * 查询用户列表数据
 */
const queryList = () => {
  $queryUserList(searchName.value, sectionID.value)
    .then((res: any) => {
      res.data.records.forEach((item: any) => {
        if (userIDArray.value.length > 0) {
          userIDArray.value.forEach((val: any) => {
            if (item.userId === val) {
              item.checked = true;
            }
          });
        } else {
          item.checked = false;
        }
      });
      const data = res.data.records;
      userList.value = data;
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

/**
 * 确定
 */
const handleOK = () => {
  emit("share-event", userIDArray.value, userArray.value);
  handleClose();
};

// 分享 模态框 取消
const handleClose = () => {
  props.shareGoals.visible = false;
};

// 清空用户所有数据
eventBus.on("onShareEventBus", () => {
  searchName.value = ""; // 清空搜索参数
  sectionList.value.length = 0; // 清空部门列表数组
  sectionID.value = null!; // 清空部门id
  userList.value.length = 0; // 清空用户列表数组
  userIDArray.value.length = 0; // 清空用户id
  userArray.value.length = 0; // 清空用户ID和名称
});

onMounted(() => {
  querySection(); // 查询部门列表
});

// 监听 visible 的变化，当模态框打开时调用 初始化 方法
watch(
  () => props.shareGoals.visible,
  (newVal) => {
    if (newVal) {
      if (userIDArray.value.length === 0) {
        querySection(); // 查询部门列表
      }
    }
  },
  { immediate: true }
);
</script>

<style scoped lang="less">
.share-dialog {
  // border: 1px solid red;
  width: 100%;
  // height: 430px;
  padding: 10px 20px;

  // 搜索
  .search-style {
    height: 48px;
    line-height: 48px;
    margin-bottom: 6px;
    position: absolute;
    z-index: 9;
    right: 36px;
    top: 54px;
    text-align: end;

    input::placeholder {
      font-size: 12px;
      color: #909090; /* 将颜色代码修改为需要的颜色 */
    }

    .input-search {
      width: 200px;
      height: 36px;
      border-radius: 22px;
      transition: width 0.5s;
      border: 1px solid #e4e7ed;
      padding: 0 14px;
    }
    .input-search:focus {
      width: 300px; /* 聚焦时宽度 */
      height: 36px;
      border: 1px solid #70b1f1;
    }
    .img-search {
      width: 18px;
      height: 18px;
      margin-right: 8px;
      position: absolute;
      z-index: 1;
      right: 4px;
      top: 14px;
      color: #cdd0d6;
    }
    .img-search:hover {
      color: #70b1f1;
      cursor: pointer;
    }
  }

  .personage-content {
    width: 100%;
    height: 300px;
    display: flex;
    margin-top: 6px;
    // border: 1px solid red;
    // 部门
    .personage-left {
      height: 290px;
      overflow: auto;
      flex: 0 0 220px;
      border-right: 1px solid #eaecf2;

      .section-ul {
        width: 210px;
        height: 290px;
        cursor: pointer;
        .section-li {
          height: 36px;
          line-height: 36px;
          font-size: 13px;
          .section-p {
            padding: 0 14px;
          }
        }
        .section-li:hover {
          color: #409eff;
          border-radius: 8px;
          background-color: #f5f7fa;
        }
      }
    }

    // 用户
    .personage-right {
      width: 100%;
      height: 290px;
      overflow: auto;
      flex: 1 1 auto;
      padding: 0 10px;
      .list-ul {
        width: 100%;
        height: 290px;
        cursor: pointer;

        .list-li {
          height: 36px;
          line-height: 36px;
          display: flex;

          .list-name {
            padding-left: 40px;
            flex: 0 0 240px;
            .list-icon {
              position: relative;
              top: 3px;
              font-size: 16px;
            }
            span {
              padding-left: 10px;
            }
          }

          .list-select {
            flex: 1 1 auto;
            text-align: center;
          }
        }
        .list-li:hover {
          color: #409eff;
          background-color: #f5f7fa;
        }

        .check-tips {
          height: 50px;
          line-height: 50px;
          text-align: center;
          .check-span {
            height: 260px;
            line-height: 260px;
            color: #b8b9ba;
          }
        }
      }
    }
  }

  .cut {
    color: #409eff;
    border-radius: 8px;
    background-color: #ecf5ff;
  }
  .active {
    color: #409eff;
    font-weight: 600;
    background-color: #ecf5ff;
  }

  .sharer-people {
    height: 30px;
    line-height: 40px;
    padding-left: 8px;
    margin-bottom: 10px;
    box-sizing: border-box;
    box-shadow: rgba(0, 0, 0, 0.1) 0px -10px 10px -7px;
    backdrop-filter: blur(24px);
    .sharer-num {
      font-size: 12px;
    }
  }
}

// 滚动条
::-webkit-scrollbar {
  width: 4px;
  height: 10px;
  overflow: visible;
}
::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
::-webkit-scrollbar-thumb {
  border: solid transparent;
  border-width: 1px 0;
  background-clip: padding-box;
  background: #d8dce5;
}

.dialog-footer {
  text-align: right;
  .btn-style {
    min-width: 90px;
  }
}
</style>
