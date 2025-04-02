<template>
  <!-- 类型选择搜索 -->
  <div class="advanced-menu">
    <div class="advanced-title">
      <span>类型搜索:</span>
    </div>
    <div class="advanced-content">
      <ul class="keyword-ul">
        <li
          v-for="item in documentType"
          :key="item.id"
          :class="['keyword-li', searchPar.isWord == item.value ? 'word' : '']"
          @click="onDocument(item.value)"
        >
          <span>{{ item.name }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { documentType } from "@/common/data";

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

// 类型搜索
const onDocument = (name: string) => {
  if (name === props.searchPar.isWord) {
    props.searchPar.isWord = "";
    return;
  }
  props.searchPar.isWord = name;
};
</script>

<style scoped lang="less">
.advanced-menu {
  width: auto;
  min-height: 40px;
  margin-bottom: 10px;
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 如果需要垂直居中，可以加上这个属性 */

  .advanced-title {
    width: 107px;
    height: 40px;
    line-height: 40px;
    margin-right: 16px;
    text-align: right;
  }

  .advanced-content {
    width: 620px;
    .keyword-ul {
      height: 40px;
      margin: auto;
      padding: 0;

      .keyword-li {
        width: 85px;
        height: 30px;
        line-height: 30px;
        font-size: 14px;
        float: left;
        text-align: center;
        cursor: pointer;
        color: #000000;
        margin: 5px 8px 0 8px;
        border-radius: 2px;
        border: 1px solid #dcdfe6;
      }
      .keyword-li:hover {
        color: #409eff;
        font-style: normal;
        border: 1px solid #d9ecff;
        background-color: #ecf5ff;
      }
      .word {
        color: #409eff;
        font-weight: 600;
        border: 1px solid #d9ecff;
        background-color: #ecf5ff;
      }
    }
  }
}
</style>
