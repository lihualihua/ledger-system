<template>
  <el-drawer
    v-model="props.drawerObj.drawer"
    title="预览"
    direction="rtl"
    size="100%"
    class="my-custom-drawer"
  >
    <el-container>
      <el-aside class="tree-aside">
        <div
          class="tree-content active"
          ref="scrollableDiv"
          @mouseenter="showScrollbar"
          @mouseleave="hideScrollbar"
        >
          <ul class="one-ul">
            <li class="one-li" v-for="(item, index) in toc1" :key="index">
              <div :class="['one-div', item.id == oneID ? 'cut' : '']">
                <p class="one-p" @click="scrollToHeading(item.id!)">
                  {{ item.text }}
                </p>
              </div>

              <ul v-if="item.children && item.children!.length > 0">
                <li
                  class="two-li"
                  v-for="(item2, index2) in item.children"
                  :key="index2"
                >
                  <div :class="['two-div', item2.id == oneID ? 'sign' : '']">
                    <p class="two-p" @click="scrollToHeading(item2.id!)">
                      {{ item2.text }}
                    </p>
                  </div>
                </li>
              </ul>
            </li>
          </ul>
        </div>
      </el-aside>

      <el-main style="padding: 0">
        <div class="editor-container">
          <div class="editor-content-view">
            <div v-html="valueHtml"></div>
          </div>

          <el-aside width="312px">
            <div class="three-tree" v-if="toc2.length > 0">
              <el-affix :offset="130">
                <h3 class="three-h3">CONTENTS</h3>
                <div
                  class="three-div active2"
                  ref="rightScrollable"
                  @mouseenter="showRightScrollbar"
                  @mouseleave="hideRightScrollbar"
                >
                  <ul class="three-ul">
                    <li
                      class="three-li"
                      v-for="(item, index) in toc2"
                      :key="index"
                    >
                      <div :class="item.id == threeID ? 'three' : ''">
                        <p
                          class="three-p"
                          @click="rightScrollToHeading(item.id!)"
                        >
                          {{ item.text }}
                        </p>
                      </div>

                      <ul v-if="item.children && item.children!.length > 0">
                        <li
                          class="four-li"
                          v-for="(item2, index2) in item.children"
                          :key="index2"
                        >
                          <div
                            :class="[
                              'four-div',
                              item2.id == threeID ? 'three' : '',
                            ]"
                          >
                            <p
                              class="four-p"
                              @click="rightScrollToHeading(item2.id!)"
                            >
                              {{ item2.text }}
                            </p>
                          </div>
                        </li>
                      </ul>
                    </li>
                  </ul>
                </div>
              </el-affix>
            </div>
          </el-aside>
        </div>
      </el-main>
    </el-container>
  </el-drawer>
</template>

<script setup lang="ts">
import "@wangeditor/editor/dist/css/style.css"; // 引入 css
import "@/css/wangEditor/wangeditor.css"; // 引入wangEditor 富文本框自定义 css
import { onMounted, ref, watch } from "vue";
import { HelpDocTree } from "@/interface/servicenavigation";

const props = defineProps({
  drawerObj: {
    type: Object,
    default: {},
  },
});

// 左侧一级、二级
const oneID = ref<string>("");
// 左侧 1、2级树
const toc1 = ref<HelpDocTree[]>([]);
// 左侧 鼠标停靠移除特效
const scrollableDiv = ref<HTMLElement | null>(null);

// 中间内容
const valueHtml = ref("");
// 右边 2、3级数
const toc2 = ref<HelpDocTree[]>([]);
// 右侧 鼠标停靠移除特效
const rightScrollable = ref<HTMLElement | null>(null);
// 左侧一级、二级
const threeID = ref<string>("");

/**
 * 左侧移入事件
 */
const showScrollbar = (): void => {
  if (scrollableDiv.value) {
    scrollableDiv.value.classList.add("tree-content");
  }
};
/**
 * 左侧移出事件
 */
const hideScrollbar = (): void => {
  if (scrollableDiv.value) {
    scrollableDiv.value.classList.remove("tree-content");
  }
};

/**
 * 右侧移入事件
 */
const showRightScrollbar = (): void => {
  if (rightScrollable.value) {
    rightScrollable.value.classList.add("three-div");
  }
};
/**
 * 右侧移出事件
 */
const hideRightScrollbar = (): void => {
  if (rightScrollable.value) {
    rightScrollable.value.classList.remove("three-div");
  }
};

/**
 * 通过内容自动识别标题目录
 * H1、H2 左侧目录
 * H3、H4 右侧目录
 */
const generateToc = (editor: any) => {
  const html = editor.getHtml();
  const parser = new DOMParser();
  const doc = parser.parseFromString(html, "text/html");
  const headings = doc.querySelectorAll("h1, h2, h3, h4, h5");

  headings.forEach((heading, index) => {
    let num = 0;
    let num2 = 0;
    if (heading.textContent !== "") {
      const id = `heading-${index}`;
      heading.setAttribute("id", id);

      // H1、H2 标签 区域逻辑
      if (toc1.value.length == 0 && heading.tagName === "H2") {
        toc1.value.push({
          id,
          text: heading.textContent!,
          children: [],
          name: "h2",
        });
        return;
      }

      // 初始化 有可能第一个标题为H1或H2
      if (heading.tagName === "H1") {
        toc1.value.push({
          id,
          text: heading.textContent!,
          children: [],
          name: "h1",
        });
      }

      if (heading.tagName === "H2") {
        // 判断 toc 中 children 是否存在
        if (toc1.value.length > 0) {
          const nums = toc1.value.length;
          for (let i = 0; i < toc1.value.length; i++) {
            // 存在 进入
            if (toc1.value[i].name === "h1") {
              num += 1;
              toc1.value[nums - 1].children?.push({
                id,
                text: heading.textContent!,
              });
              break;
            }
          }
        }
      }
      if (num == 0) {
        if (heading.tagName === "H2") {
          toc1.value.push({
            id,
            text: heading.textContent!,
            children: [],
            name: "h2",
          });
        }
      }

      // H3、H4 标签 区域逻辑
      if (toc2.value.length == 0 && heading.tagName === "H4") {
        toc2.value.push({
          id,
          text: heading.textContent!,
          children: [],
          name: "h4",
        });
        return;
      }

      // 初始化 有可能第一个标题为H3或H4
      if (heading.tagName === "H3") {
        toc2.value.push({
          id,
          text: heading.textContent!,
          children: [],
          name: "h3",
        });
      }

      if (heading.tagName === "H4") {
        // 判断 toc 中 children 是否存在
        if (toc2.value.length > 0) {
          const nums = toc2.value.length;
          for (let i = 0; i < toc2.value.length; i++) {
            // 存在 进入
            if (toc2.value[i].name === "h3") {
              num2 += 1;
              toc2.value[nums - 1].children?.push({
                id,
                text: heading.textContent!,
              });
              break;
            }
          }
        }
      }
      if (num2 == 0) {
        if (heading.tagName === "H4") {
          toc2.value.push({
            id,
            text: heading.textContent!,
            children: [],
            name: "h4",
          });
        }
      }
    }
  });

  valueHtml.value = doc.body.innerHTML;
};

/**
 * 左侧锚点事件 点击滑动对应位置
 * 滚动到指定的标题位置
 */
const scrollToHeading = (id: string) => {
  oneID.value = id!;
  const element = document.getElementById(id);
  if (element) {
    element.scrollIntoView({ behavior: "smooth" }); // 平滑滚动
  }
};

/**
 * 右侧锚点事件 点击滑动对应位置
 * 滚动到指定的标题位置
 */
const rightScrollToHeading = (id: string) => {
  threeID.value = id!;
  const element = document.getElementById(id);
  if (element) {
    element.scrollIntoView({ behavior: "smooth" }); // 平滑滚动
  }
};

/**
 * 预览事件
 */
const onPreview = () => {
  generateToc(props.drawerObj.content);
};

// 监听 drawer 的变化，当模态框打开时调用 初始化 方法
watch(
  () => props.drawerObj.drawer,
  (newVal) => {
    if (newVal) {
      // 清空当前的目录
      toc1.value = [];
      toc2.value = [];
      onPreview(); // 预览事件
    }
  },
  { immediate: true }
);

onMounted(() => {
  // 初始状态下可能想要隐藏滚动条
  // 左侧移除特效
  if (scrollableDiv.value) {
    scrollableDiv.value.classList.remove("tree-content");
  }
  // 右侧移除特效
  if (rightScrollable.value) {
    rightScrollable.value.classList.remove("three-div");
  }
});
</script>

<style>
/* 抽屉样式 */
.my-custom-drawer .el-drawer__body {
  padding: 0px;
}
.my-custom-drawer .el-drawer__header {
  margin-bottom: 0px;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}
</style>

<style scoped lang="less">
// 左侧下拉树
.tree-aside {
  width: 344px;
  height: 100%;
  font-size: 14px;
}
// 左侧下拉树
.active {
  overflow: hidden; /* 隐藏滚动条 */
  height: calc(100vh - 66px);
  padding: 48px;
  border-right: 0.01rem solid rgb(243 243 243);
}
// 左侧下拉树
.tree-content {
  height: calc(100vh - 66px);
  padding: 48px;
  overflow: auto;
  font-family: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, "Fira Sans", "Droid Sans", "Helvetica Neue", sans-serif';
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  font-size: 16px;
  color: #2c3e50;
  border-right: 0.01rem solid rgb(243 243 243);

  ul li {
    list-style: none;
  }
}
// 左侧下拉树一级
.one-ul {
  width: 242px;
  cursor: pointer;

  .one-li {
    font-size: 15px;
    line-height: 36px;
    .one-div {
      display: flex;
      .one-p {
        font-weight: 600;
        flex: 1 1 auto;
        padding: 0 20px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
        color: #303133;
        font-size: 14px;
      }
    }
    .one-div:hover {
      font-weight: 600;
      color: #409eff;
    }

    // 二级
    .two-li {
      line-height: 36px;
      font-size: 13px;
      .two-div {
        display: flex;
        .two-p {
          flex: 1 1 auto;
          padding-left: 30px;
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
          color: #606266;
        }
      }
      .two-div:hover {
        color: #409eff;
      }
    }
  }

  .cut {
    color: #409eff;
    font-weight: 600;
    font-size: 15px;
    border-radius: 8px;
    background-color: #409eff1a;
  }
  .sign {
    color: #409eff;
    font-weight: 600;
    font-size: 13px;
    border-radius: 8px;
    background-color: #409eff1a;
  }
}
// 左侧下拉树滚动条
.tree-content::-webkit-scrollbar {
  width: 4px;
  height: 10px;
  overflow: visible;
}
.tree-content::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
.tree-content::-webkit-scrollbar-thumb {
  border: solid transparent;
  border-width: 1px 0;
  background-clip: padding-box;
  border-radius: 10px;
  background: #e2e6f0;
}
.tree-content::-webkit-scrollbar-thumb:hover {
  background: #d8dce6;
  border-radius: 10px;
}

// 中间内容
.editor-container {
  display: flex;
  overflow: auto;
  height: calc(100vh - 66px);
  margin: 0 auto;
  padding: 48px 0 48px 64px;
  list-style: initial !important;
  // 无序列表
  ::v-deep(ul li) {
    list-style: initial;
  }
  // 有序列表
  ::v-deep(ol li) {
    list-style-type: decimal;
  }
}

// 中间滚动条
.editor-container::-webkit-scrollbar {
  width: 4px;
  height: 10px;
  overflow: visible;
}
.editor-container::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
.editor-container::-webkit-scrollbar-thumb {
  border: solid transparent;
  border-width: 1px 0;
  background-clip: padding-box;
  background: #d8dce5;
}

// 右侧下拉树
.active2 {
  overflow: hidden; /* 隐藏滚动条 */
  height: calc(100vh - 180px);
  width: 212px;
  font-size: 12px;
  padding-left: 14px;
}
.three-div {
  width: 212px;
  height: calc(100vh - 180px);
  overflow: auto;
  font-size: 12px;
  padding-left: 14px;
}
// 右边 三树
.three-tree {
  padding: 0 48px 0 64px;

  .three-h3 {
    font-size: 12px;
    line-height: 30px;
    padding-left: 14px;
    color: #606266;
    font-weight: 600;
    text-transform: uppercase;
    margin-top: 0;
  }

  ul li {
    list-style: none;
  }

  .three-ul {
    width: 168px;
    .three-li {
      line-height: 30px;
      color: #909399;
      white-space: nowrap;
      text-decoration: none;
      text-overflow: ellipsis;
      overflow: hidden;
      max-width: 100%;
      outline: none;
      cursor: pointer;

      // 四级
      .four-li {
        line-height: 30px;
        .four-div {
          display: flex;
          .four-p {
            flex: 1 1 auto;
            padding-left: 12px;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
          }
        }
        .four-div:hover {
          color: #409eff;
        }
      }
    }
  }

  .three {
    color: #409eff;
  }
}

// 右侧滚动条
.three-div::-webkit-scrollbar {
  width: 4px;
  height: 10px;
  overflow: visible;
}
.three-div::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
.three-div::-webkit-scrollbar-thumb {
  border: solid transparent;
  border-width: 1px 0;
  background-clip: padding-box;
  background: #d8dce5;
}

// wangeditor 预览接收内容
::v-deep(.editor-content-view) {
  width: 76%;
  border-radius: 0px;
  margin-top: 0px;
  overflow-x: visible;
}

// wangeditor 引用样式
::v-deep(.editor-content-view code) {
  background-color: #f5f7fa;
  border-radius: 8px;
  color: #595959;
  font-family: 微软雅黑;
  font-size: 0.9rem;
}

// wangeditor 代码块样式
::v-deep(.editor-content-view blockquote) {
  border-left: 8px solid #409eff;
  background-color: #409eff1a;
  border-radius: 4px;
  font-size: 0.9rem;
}
</style>
