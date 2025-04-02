<template>
  <div class="progress-bar">
    <div
      class="progress"
      :style="{ width: `${level * 25}%`, backgroundColor: getLevelColor() }"
    ></div>
  </div>
  <p class="progress-title">{{ levelText }}</p>
</template>

<script lang="ts" setup>
import { computed } from "vue";

// 定义安全等级，范围从 1 到 3
const props = defineProps<{
  level: number;
}>();

// 计算安全等级文本
const levelText = computed(() => {
  switch (props.level) {
    case 1:
      return "高风险";
    case 2:
      return "中风险";
    case 3:
      return "低风险";
    default:
      return "未知";
  }
});

// 根据安全等级计算进度条颜色
const getLevelColor = () => {
  switch (props.level) {
    case 1:
      return "#FF6347"; // 低等级 - 红色
    case 2:
      return "#FFA500"; // 中等级 - 橙色
    case 3:
      return "#228B22"; // 高等级 - 绿色
    default:
      return "#808080"; // 未知等级 - 灰色
  }
};
</script>

<style scoped>
.progress-bar {
  width: 100%;
  height: 12px;
  background-color: #f3f3f3;
  border-radius: 5px;
  flex: 1 1 0%;
}

.progress {
  height: 12px;
  text-align: center;
  line-height: 12px;
  color: white;
  border-radius: 5px;
}
.progress-title {
  width: 100px;
  height: 20px;
  line-height: 20px;
  position: relative;
  top: -4px;
  left: 10px;
}
</style>
