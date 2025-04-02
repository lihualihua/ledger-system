<template>
  <svg
    aria-hidden="true"
    :style="'width:' + width + ';height:' + height + ';cursor: pointer;'"
  >
    <use :xlink:href="symbolId" />
  </svg>
</template>

<script setup lang="ts">
import { computed } from "vue";
const props = defineProps({
  // icon 名字
  name: {
    type: String,
    default: "",
  },
  // 长度
  width: {
    type: String,
    default: "18px",
  },
  // 高度
  height: {
    type: String,
    default: "18px",
  },
});
const symbolId = computed(() => `#icon-${props.name}`);
</script>
