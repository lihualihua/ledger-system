<template>
  <!-- 面相用户业务导航 -->
  <el-container>
    <el-header>
      <div class="layout-header-inner">
        <div class="logo">
          <div class="logo-svg">
            <el-icon :size="38" color="#409eff" title="业务导航">
              <Promotion />
            </el-icon>
          </div>
          <a>{{ titleName }}</a>
        </div>
        <div class="search-style">
          <input placeholder="搜索" class="input-search" />
          <Search class="img-search" />
        </div>
      </div>
    </el-header>

    <el-main>
      <div class="layout-main">
        <div v-if="tableData.length > 0">
          <el-table
            v-loading="tableLoading"
            :data="tableData"
            height="680"
            style="width: 100%"
          >
            <el-table-column prop="fileName" label="标题" sortable>
              <template #default="scope">
                <div class="table-hover">
                  <span :title="scope.row.title">
                    {{ scope.row.title }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="scope">
                <span class="table-color">
                  {{ formatDate(scope.row.createTime) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="desc" label="描述" width="340">
              <template #default="scope">
                <div class="table-hover">
                  <span :title="scope.row.desc">
                    {{ scope.row.desc }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="80">
              <template #default="scope">
                <el-button
                  link
                  type="primary"
                  size="small"
                  @click="onView(scope.row.code, scope.row.version)"
                >
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="list-group-info">
            共
            <b class="info-con">{{ total }}</b>
            项
          </div>
        </div>

        <div class="empty" v-if="tableData.length == 0 && !tableLoading">
          <div class="empty-container">
            <div class="empty-left">
              <p class="empty-title">
                您所浏览的{{ titleName }}文章不存在或者被删除
              </p>
              <p class="empty-remarks">如有疑惑，请与管理员进行联系</p>
              <p>
                <el-button
                  class="btn-style"
                  type="primary"
                  size="large"
                  @click="onBack"
                >
                  返回首页
                </el-button>
              </p>
            </div>
            <div class="empty-right">
              <el-empty />
            </div>
          </div>
        </div>
      </div>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { formatDate } from "@/utils/other";
import { TableData } from "@/interface/document";
import {
  $findWikiReleaseList,
  $saveProcessDomainTitle,
} from "@/api/servicenavigation";
import { Promotion } from "@element-plus/icons-vue";
import { useRouter, useRoute } from "vue-router";
import { Search } from "@element-plus/icons-vue";

// 路由
const router = useRouter();
// 路由 参数
const route = useRoute();

// 加载loading
const tableLoading = ref<boolean>(false);
// 标题
const titleName = ref<string>("");
// table值
const tableData = ref<TableData[]>([]);
// 文件查询总数
const total = ref<number>(0);

/**
 * table 查询列表
 */
const queryList = (id: string) => {
  const obj = {
    domainId: id,
  };
  tableLoading.value = true;
  $findWikiReleaseList(obj)
    .then((res: any) => {
      if (res.data && res.data.records.length > 0) {
        tableData.value = res.data.records;
        total.value = res.data.records.length;
      } else {
        tableData.value.length = 0;
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {
      setTimeout(() => {
        tableLoading.value = false; // 列表刷新
      }, 100);
    });
};

/**
 * 查询流程域标题
 */
const queryTitle = (id: string) => {
  $saveProcessDomainTitle(id)
    .then((res: any) => {
      titleName.value = res.data.name;
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

// 查看
const onView = (code?: string, version?: string) => {
  const route = router.resolve({
    path: "/helpdocument", // 目标路由的路径
    query: { code: code, version: version }, // 携带的参数
  });
  // 使用 window.open 打开新窗口
  window.open(route.href, "_blank");
};

/**
 * 点击返回首页
 */
const onBack = () => {
  // 关闭当前窗口
  window.close();
};

onMounted(() => {
  // 获取路由参数
  const id = route.query.id as string;
  queryTitle(id); // 查询流程域标题
  queryList(id); // 查询 table 查询列表
});
</script>

<style scoped lang="less">
::v-deep(.el-header) {
  background: #f5f8fa;
}
::v-deep(.el-main) {
  padding: 0px 20px;
}

.layout-header-inner {
  max-width: 1080px;
  height: 60px;
  line-height: 60px;
  padding: 0;
  margin: 0 auto;
  .logo {
    float: left;
    cursor: pointer;
    .logo-svg {
      float: left;
      width: 40px;
      height: 40px;
      margin: 11px 16px 0 0px;
    }
  }

  .search-style {
    float: right;
    height: 60px;
    line-height: 58px;
    padding-right: 20px;
    text-align: end;

    input::placeholder {
      font-size: 12px;
      color: #909090; /* 将颜色代码修改为需要的颜色 */
    }

    .input-search {
      width: 200px;
      height: 36px;
      border-radius: 22px;
      transition: width 0.5s;
      border: 1px solid #e4e7ed;
      padding: 0 14px;
    }
    .input-search:focus {
      width: 300px; /* 聚焦时宽度 */
      height: 36px;
      border: 1px solid #70b1f1;
    }
    .img-search {
      width: 18px;
      height: 18px;
      margin-right: 8px;
      position: absolute;
      z-index: 1;
      right: 442px;
      top: 22px;
      color: #cdd0d6;
    }
    .img-search:hover {
      color: #70b1f1;
      cursor: pointer;
    }
  }
}

.layout-main {
  max-width: 1080px;
  height: calc(100vh - 126px);
  padding: 0;
  margin: 0 auto;
  margin-top: 40px;

  .table-hover {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .list-group-info {
    text-align: center;
    padding: 28px 0;
    font-size: 13px;
    color: #777;
    .info-con {
      margin: 0 3px;
      font-weight: 400;
    }
  }

  .empty {
    width: 100%;
    padding: 0 20px;

    .empty-container {
      display: flex;
      padding-top: 100px;

      .empty-left {
        flex: 0 0 630px;
        padding: 90px 0 0 40px;

        .empty-title {
          padding-bottom: 20px;
          font-size: 24px;
          font-weight: 600;
        }

        .empty-remarks {
          padding-bottom: 30px;
          color: #939090;
        }

        .btn-style {
          min-width: 120px;
        }
      }
      .empty-right {
        flex: 1 1 auto;
      }
    }
  }
}
</style>
