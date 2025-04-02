<template>
  <div class="advanced-menu">
    <div class="advanced-title">
      <span>时间范围:</span>
    </div>
    <div class="advanced-content">
      <div class="advanced-parcel">
        <el-date-picker
          v-model="searchPar.dataTime"
          :default-time="defaultTime"
          type="daterange"
          format="YYYY/MM/DD"
          value-format="YYYY-MM-DD"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          @change="datePicker('time')"
        />
        <span class="advanced-date">更新时间：</span>
        <el-select
          v-model="searchPar.selectValue"
          placeholder="选择"
          class="date-select"
          @change="datePicker('')"
        >
          <el-option
            v-for="item in timeList"
            :key="item.value"
            :label="item.name"
            :value="item.value"
          />
        </el-select>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { timeList } from "@/common/data";

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

// 开始时间和结束时间
const defaultTime = ref<[Date, Date]>([
  new Date(2000, 1, 1, 0, 0, 0),
  new Date(2000, 2, 1, 23, 59, 59),
]);

// 时间范围 & 更新时间 切换方法
const datePicker = (name: string) => {
  name === "time"
    ? (props.searchPar.selectValue = "")
    : (props.searchPar.dataTime = "");
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
  }

  .advanced-parcel {
    text-align: left;
    margin: 3px 0 0 8px;
    .advanced-date {
      margin-left: 54px;
      color: #909399;
    }
    .date-select {
      width: 105px;
      position: relative;
      top: -2px;
    }
  }
}
</style>
