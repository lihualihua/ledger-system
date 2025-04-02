<template>
  <!-- 上传者 -->
  <div class="advanced-box">
    <div class="advanced-and">
      <el-select
        v-model="searchPar.selectLink2"
        placeholder="Select"
        style="width: 100px"
      >
        <el-option
          v-for="item in searchLink"
          :key="item.value"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </div>
    <div class="advanced-select" v-show="unpack.isExpanded">
      <span>上传者</span>
      <span class="advanced-line"></span>
      <el-icon
        @click="onClear()"
        title="清空"
        :class="['advanced-icon', searchPar.search3 != '' ? 'clear' : '']"
      >
        <Close />
      </el-icon>

      <!-- <div class="advanced-records" v-show="isVisible3">
            <ul class="advanced-records-ul">
              <li class="advanced-records-li">vue基础3</li>
            </ul>
          </div> -->
    </div>
    <input
      v-model="searchPar.search3"
      maxlength="100"
      autocomplete="off"
      @focus="onFocus()"
      class="advanced-input"
      @keyup.enter="onSearch"
    />
  </div>
</template>

<script setup lang="ts">
import { searchLink } from "@/common/data";
import { Close } from "@element-plus/icons-vue";

// 子组件调用父组件方法（通讯）
const emit = defineEmits(["search-event"]);

const props = defineProps({
  // 入参
  searchPar: {
    type: Object,
    default: {},
  },
  // 关闭历史下拉
  unpack: {
    type: Object,
    default: {},
  },
});

// 搜索--模糊搜索
const onFocus = () => {
  props.searchPar.search3 === ""
    ? (props.unpack.isVisible3 = false)
    : (props.unpack.isVisible3 = true);
};

// input搜索--清空
const onClear = () => {
  props.searchPar.search3 = "";
  props.unpack.isVisible3 = false;
};

const onSearch = () => {
  emit("search-event");
};
</script>

<style scoped lang="less">
::v-deep(.el-select .el-input__inner) {
  text-align: center;
}

.advanced-box {
  margin-bottom: 12px;
  .advanced-and {
    width: 100px;
    line-height: 40px;
    margin-right: 16px;
    display: inline-flex;
  }
  .advanced-select {
    display: inline-block;
    position: absolute;
    width: 120px;
    height: 40px;
    line-height: 40px;
    font-size: 14px;
    cursor: pointer;
    span {
      color: #939090;
    }
    .advanced-line {
      width: 1px;
      height: 26px;
      position: absolute;
      top: 8px;
      left: 114px;
      background-color: #d2d2d2;
    }
    .advanced-icon {
      display: none;
      position: absolute;
      top: 13px;
      left: 576px;
      color: #9195a3;
    }
    .clear {
      display: flex;
    }
    .advanced-records {
      position: absolute;
      left: 114px;
      font-size: 14px;
      cursor: pointer;
      .advanced-records-ul {
        width: 486px;
        text-align: left;
        position: absolute;
        top: -1px;
        z-index: 9;
        margin-top: 1px;
        padding: 10px 0 10px 0;
        background-color: #fff;
        border-radius: 0 0 10px 10px;
        border: 1px solid #d2d2d2;
        border-top: none;
      }
      .records-line {
        width: 92%;
        height: 1px;
        margin: -10px 0 10px 8px;
        background-color: #d2d2d2;
      }
      .advanced-records-li {
        height: 30px;
        line-height: 30px;
        padding-left: 14px;
      }
      .advanced-records-li:hover {
        background: #f5f5f7;
        color: #4e6ef2;
      }
    }
  }
  .advanced-input {
    width: 600px;
    height: 40px;
    line-height: 40px;
    padding: 0 30px 0 120px;
    font-size: 14px;
    border: 1px solid #cdd4dc;
    border-radius: 4px;
  }
}
</style>
