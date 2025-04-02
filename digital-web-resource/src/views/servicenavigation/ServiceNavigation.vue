<template>
  <div class="main-content">
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane label="业务导航" name="servicenavigation">
        <el-tabs
          tab-position="left"
          type="border-card"
          v-model="tabsValue"
          @tab-click="handleClick"
        >
          <el-tab-pane
            v-for="item in editableTabs"
            :key="item.name"
            :label="item.name"
            :name="item.id"
          >
          </el-tab-pane>
          <ServiceNavigationTable :serviceObj="serviceObj" />
        </el-tabs>
      </el-tab-pane>

      <el-tab-pane label="流程域" name="processdomain">
        <ServiceCatalog />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { TabsPaneContext } from "element-plus";
import ServiceNavigationTable from "./components/ServiceNavigationTable.vue";
import ServiceCatalog from "./components/ServiceCatalog.vue";
import { $queryDomainList } from "@/api/servicenavigation";
import { FlowTable } from "@/interface/servicenavigation";
import { Returndata } from "@/common/enum";
import eventBus from "@/common/eventBus";
import router from "@/router";
import { useRoute } from "vue-router";

/**
 * 左侧流程域名称
 */
const serviceObj = reactive({
  id: "",
  label: "",
  numValue: 0,
});

const activeName = ref<string>("servicenavigation");

const tabsValue = ref<string>("");
// Tabs 数据
const editableTabs = ref<FlowTable[]>([{ id: "1", name: " 暂无数据 " }]);

/**
 * tab 切换查询
 */
const handleTabClick = (tab: TabsPaneContext) => {
  if (tab.props.name === "servicenavigation") {
    queryList(); // 查询左侧 Tabs数据
  }

  router.push({
    path: "/servicenavigation",
    query: { tabIdentification: tab.props.name },
  });
};

/**
 * 查询左侧 Tabs数据
 */
const queryList = () => {
  const status = "?status=1";
  $queryDomainList(status)
    .then((res: any) => {
      if (res.code === Returndata.code && res.data.length > 0) {
        tabsValue.value = res.data[0].id;
        serviceObj.id = res.data[0].id;
        serviceObj.label = res.data[0].name;
        serviceObj.numValue = res.data.length;
        editableTabs.value = res.data;
        /**
         * 事件总线通讯
         * 调用子组件查询列表接口
         */
        eventBus.emit("onServiceEventBus");
      }
    })
    .catch((err) => {
      serviceObj.numValue = 0;
      editableTabs.value = [{ id: "1", name: " 暂无数据 " }];
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

/**
 * 点击流程域名称查询
 */
const handleClick = (tab: TabsPaneContext, _event: Event) => {
  serviceObj.id = tab.props.name as string;
  serviceObj.label = tab.props.label;
  /**
   * 事件总线通讯
   * 调用子组件查询列表接口
   */
  eventBus.emit("onServiceEventBus");
};

onMounted(() => {
  // 获取路由参数中的id
  const route = useRoute();
  const queryId = route.query.tabIdentification;
  activeName.value = queryId as string;

  queryList(); // 查询 Tabs数据
});
</script>

<style scoped lang="less">
::v-deep(.el-tabs--left) {
  height: 722px;
}
</style>
