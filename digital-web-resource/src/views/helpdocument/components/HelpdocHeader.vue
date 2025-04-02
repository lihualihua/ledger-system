<template>
  <div class="help-header">
    <div class="help-link">
      <span class="help-title">{{ props.title }}</span>
    </div>
    <div class="help-controls">
      <div class="help-item">
        <span class="title"> </span>
      </div>
      <div class="help-item" @mouseleave="onMouse(false)">
        <p @mouseenter="onMouse(true)">
          <span class="title">{{ versionName }} 版本</span>
          <el-icon class="help-icon"><CaretBottom /></el-icon>
        </p>
        <ul class="help-ul" v-show="isShow">
          <li
            v-for="(item, index) in versionList"
            :key="index"
            @click="onVersion(item)"
            :class="versionName === item ? 'cut' : ''"
          >
            {{ item }} 版本
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { CaretBottom } from "@element-plus/icons-vue";
import { $saveAllVersion } from "@/api/servicenavigation";
import { useUserStore } from "@/stort/user";
import { Permission } from "@/common/enum";

const userStore = useUserStore();

const props = defineProps({
  title: {
    type: String,
    default: "",
  },
  codeValue: {
    type: String,
    default: "",
  },
  version: {
    type: String,
    default: "",
  },
});

/**
 * 子组件传参给父组件
 */
const emit = defineEmits(["version-event"]);

const isShow = ref(false);

// 历史版本
const versionList = ref<[]>([]);
// 版本号
const versionName = ref<string>("");

// 鼠标移入/移出事件
const onMouse = (boo: boolean) => {
  isShow.value = boo;
};

/**
 * 查询所有版本号
 */
const saveAllVersion = (code: string, status?: number | string) => {
  $saveAllVersion(code, status)
    .then((res: any) => {
      versionList.value = res.data;
      if (status === "") {
        versionName.value = props.version;
      } else {
        versionName.value = res.data[0];
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

/**
 * 切换版本
 */
const onVersion = (version: string) => {
  versionName.value = version;
  emit("version-event", version);
};

onMounted(() => {
  /**
   * 按钮权限判断
   * @userStore 信息持久化
   * @SUPER_ADMIN 超级管理员
   * @EDPT_ADMIN 部门管理员
   */
  const { permissions } = userStore.user;
  let roleCode = "";
  permissions.forEach((res: any) => {
    roleCode = res.roleCode;
  });

  let status = null;
  // 超级管理员 || 部门管理员查询全部
  if (roleCode == Permission.super_admin || roleCode == Permission.edpt_admin) {
    status = "";
  } else {
    // 普通用户查询 已发布
    status = 1;
  }
  setTimeout(() => {
    saveAllVersion(props.codeValue, status);
  }, 100);
});
</script>

<style scoped lang="less">
// 头部
.help-header {
  display: flex;
  height: 60px;
  line-height: 60px;
  .help-link {
    flex: 0 0 800px;
    .help-title {
      font-size: 14px;
      font-weight: 600;
      color: #2c3e50;
    }
  }

  .help-controls {
    flex: 1;
    text-align: right;
    font-size: 14px;
    .help-item {
      min-width: 70px;
      padding-left: 20px;
      text-align: center;
      display: inline-block;

      .title {
        cursor: pointer;
      }
      .title:hover {
        color: #409eff;
      }
      // 历史版本
      .help-icon {
        color: #ccc;
        margin-left: 4px;
      }
      .help-ul {
        width: 120px;
        padding: 10px 0;
        border-radius: 4px;
        text-align: center;
        position: absolute;
        top: 60px;
        right: 18px;
        z-index: 200;
        background-color: #ffffff;
        border: 1px solid rgba(0, 0, 0, 0.08);
        box-shadow: 0px 5px 12px 4px rgba(0, 0, 0, 0.08);
        li {
          height: 36px;
          line-height: 36px;
          cursor: pointer;
        }
        li:hover {
          background-color: #f5f7fa;
        }
        .cut {
          color: #409eff;
          font-weight: 600;
          background-color: #f5f7fa;
        }
      }
    }
  }
}
</style>
