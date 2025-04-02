<template>
  <div class="workbench">
    <div class="feature-title">
      <span class="title-name">常用功能</span>
      <span class="title-more" title="配置">
        <el-icon class="shortcut" @click="onShortcut"><Setting /></el-icon>
      </span>
    </div>
    <div class="feature-content">
      <ul class="feature-ul">
        <li
          class="feature-li"
          v-for="(item, index) in listData"
          :key="index"
          @click="onFeature(item.path!)"
        >
          <SvgIcon
            :name="item.icon"
            width="30px"
            height="30px"
            class="feature-icon"
          ></SvgIcon>
          <span class="feature-name">{{ item.name }}</span>
        </li>

        <li class="check-tips" v-if="listData.length === 0">
          <span class="check-span">暂无数据</span>
        </li>
      </ul>
    </div>
  </div>

  <ShortcutSettingComponents
    :dialogObj="dialogObj"
    @shortcut-event="queryAll"
  />
</template>

<script setup lang="ts">
import { $queryShortcut } from "@/api/workbench";
import { Returndata } from "@/common/enum";
import { ShortcutList } from "@/interface/workbench";
import { onMounted, reactive, ref } from "vue";
import { Setting } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import eventBus from "@/common/eventBus";
import SvgIcon from "@/components/SvgIcon/index.vue";
import ShortcutSettingComponents from "./ShortcutSetting.vue";

const router = useRouter();

const dialogObj = reactive({
  visible: false,
  list: [],
});

const listData = ref<ShortcutList[]>([]);

/**
 * 常用功能配置
 */
const onShortcut = () => {
  dialogObj.visible = true;
};

/**
 * 查询个人常用功能列表
 */
const queryAll = () => {
  $queryShortcut()
    .then((res: any) => {
      if (res.code === Returndata.code) {
        listData.value = res.data;
        dialogObj.list = res.data;
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

onMounted(() => {
  queryAll();
});

/**
 * 常用功能
 */
const onFeature = (name: string) => {
  // 存储选中后的菜单项
  sessionStorage.setItem("active", name);
  eventBus.emit("onActiveEventBus", name);
  router.push(name);
};
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
  // 标题公共
  .feature-title {
    width: 100%;
    height: 50px;
    line-height: 50px;
    .title-name {
      padding: 0 26px;
      font-weight: 600;
      margin-bottom: 4px;
    }
    .title-more {
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

  .feature-content {
    height: 180px;
    padding: 2px 14px 0 14px;
    overflow: auto;
    .feature-ul {
      width: 100%;
      height: 170px;

      .feature-li {
        width: 100px;
        height: 85px;
        cursor: pointer;
        text-align: center;
        padding-top: 14px;
        display: inline-block;

        .feature-icon {
          display: inline-block;
        }
        .feature-name {
          font-size: 13px;
          display: block;
          text-decoration: none;
        }
      }

      .feature-li:hover {
        border-radius: 8px;
        outline: none;
        box-shadow: 0px 0px 12px rgba(0, 0, 0, 0.12);
        .feature-close {
          display: inline;
        }
        .feature-name {
          text-decoration: underline;
        }
      }

      .check-tips {
        text-align: center;
        .check-span {
          line-height: 140px;
          color: #b8b9ba;
        }
      }
    }
  }
}

// 滚动条
.feature-content::-webkit-scrollbar {
  width: 4px;
  height: 10px;
  overflow: visible;
}
.feature-content::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
.feature-content::-webkit-scrollbar-thumb {
  border: solid transparent;
  border-width: 1px 0;
  background-clip: padding-box;
  background: #d8dce5;
}
</style>
