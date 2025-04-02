<template>
  <el-drawer v-model="drawer" title="通知" size="35%">
    <div>
      <p style="margin-bottom: 20px; font-size: 18px">Drawer 抽屉的基本用法</p>
      <div style="border: 1px solid red; padding: 20px; margin-bottom: 20px">
        <span style="font-size: 14px">
          你必须像 Dialog一样为 Drawer 设置 model-value 属性来控制 Drawer
          的显示与隐藏状态，该属性接受一个 boolean 类型。 Drawer 包含三部分:
          title & body & footer, 其中 title 是一个具名 slot, 你还可以通过 title
          属性来设置标题, 默认情况下它是一个空字符串, 其中 body 部分是 Drawer
          组件的主区域, 它包含了用户定义的主要内容. footer和title用法一致,
          用来显示页脚信息. 当 Drawer 打开时，默认设置是从右至左打开 30%
          浏览器宽度。 你可以通过传入对应的 direction 和 size
          属性来修改这一默认行为。 下面一个示例将展示如何使用 before-close
          API，更多详细用法请参考页面底部的 API 部分。
        </span>
      </div>
      <div style="font-size: 14px">
        <a>可点击跳转到任务重中</a>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref } from "vue";

const drawer = ref<boolean>(true);
</script>

<style scoped lang="less"></style>
