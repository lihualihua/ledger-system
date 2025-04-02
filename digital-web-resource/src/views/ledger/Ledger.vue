<template>
  <!-- 台账 -->
  <div class="main-content">
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane label="我的分配" name="myallocation">
        <LedgerInitiateTableComponents />
      </el-tab-pane>
      <el-tab-pane label="分配给我" name="assigntome">
        <LedgerReceiveTableComponents />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { TabsPaneContext } from "element-plus";
import LedgerInitiateTableComponents from "./components/LedgerInitiateTable.vue";
import LedgerReceiveTableComponents from "./components/LedgerReceiveTable.vue";
import { useRoute } from "vue-router";
import router from "@/router";

const activeName = ref<string>("myallocation");

/**
 * tab 切换查询
 */
const handleTabClick = (tab: TabsPaneContext) => {
  router.push({
    path: "/ledger",
    query: { tabIdentification: tab.props.name },
  });
};

onMounted(() => {
  // 获取路由参数中的id
  const route = useRoute();
  const queryId = route.query.tabIdentification;
  activeName.value = queryId as string;
});
</script>

<style scoped lang="less"></style>
