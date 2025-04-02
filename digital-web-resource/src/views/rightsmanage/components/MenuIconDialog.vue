<template>
  <el-dialog
    draggable
    width="570"
    title="菜单图标"
    v-model="props.iconDialog.visible"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    @close="onReset"
  >
    <div class="icon-item-dv">
      <ul class="icon-list">
        <li
          v-for="item in menyIcon"
          :class="['icon-item', active === item.value ? 'cut' : '']"
          @click="onIcon(item.value)"
        >
          <span class="svg-icon">
            <Icon :icon="item.value" class="icon" />
          </span>
        </li>
      </ul>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-style" @click="onReset"> 取消 </el-button>
        <el-button class="btn-style" type="primary" @click="onSubmit">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { menyIcon } from "@/common/data";

const props = defineProps({
  iconDialog: {
    type: Object,
    default: {},
  },
});

const emit = defineEmits(["menuicon-event"]);

const active = ref<string>("");

/**
 * 选择菜单图标
 */
const onIcon = (value: string) => {
  active.value = value;
};

/**
 * 确定
 */
const onSubmit = async () => {
  emit("menuicon-event", active.value);
  props.iconDialog.visible = false;
};

/**
 * 取消
 */
const onReset = () => {
  emit("menuicon-event", "");
  props.iconDialog.visible = false;
  active.value = "";
};
</script>

<style scoped lang="less">
.icon-item-dv {
  width: 100%;
  height: 100%;
  padding: 0 20px;
  margin-bottom: 10px;

  .icon-list {
    height: 400px;
    overflow: auto;
    list-style: none;
    padding: 0 !important;
    border-left: 1px solid #dcdfe6;
    border-radius: 4px;
    display: grid;
    grid-template-columns: repeat(7, 1fr);

    .icon-item {
      text-align: center;
      color: #606266;
      height: 60px;
      font-size: 13px;
      border-right: 1px solid #dcdfe6;
      border-bottom: 1px solid #dcdfe6;
      transition: background-color var(--el-transition-duration);

      .svg-icon {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
        cursor: pointer;

        .icon {
          width: 18px;
          height: 18px;
        }
      }
    }

    .icon-item:hover {
      background-color: #f2f6fc;
    }
    .cut {
      background-color: #f2f6fc;
    }
  }

  .icon-list .icon-item:nth-child(-n + 7) {
    border-top: 1px solid #dcdfe6;
  }

  // 滚动条
  .icon-list::-webkit-scrollbar {
    width: 4px;
    height: 10px;
    overflow: visible;
  }
  .icon-list::-webkit-scrollbar-button {
    width: 0;
    height: 0;
  }
  .icon-list::-webkit-scrollbar-thumb {
    border: solid transparent;
    border-width: 1px 0;
    background-clip: padding-box;
    background: #d8dce5;
  }
}

.dialog-footer {
  .btn-style {
    min-width: 90px;
  }
}
</style>
