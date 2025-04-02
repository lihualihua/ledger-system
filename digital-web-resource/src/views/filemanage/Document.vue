<template>
  <!-- 文件库 -->
  <div class="dociment-content">
    <el-container>
      <el-aside width="220px" style="border-right: 1px solid #f6f2f2">
        <!-- 所有部门 -->
        <div
          class="section-label"
          v-if="listObj.ownership == 3 && options.length > 0"
        >
          <div class="section-container">
            <div class="section-title">
              <span>所有部门({{ options.length }})</span>
            </div>
            <!-- <div class="section-img">
              <span class="section-add change" title="新建标签">
                <el-icon><Plus /></el-icon>
              </span>
              <span class="change" title="标签管理">
                <el-icon><Setting /></el-icon>
              </span>
            </div> -->
          </div>

          <div class="section-content">
            <ul class="section-ul">
              <li class="section-li" v-for="item in options" :key="item.deptId">
                <a
                  :class="listObj.deptId === item.deptId ? 'tag' : 'tag-hover'"
                  @click="onSection(item.deptId)"
                >
                  <SvgIcon
                    :name="
                      listObj.deptId === item.deptId
                        ? item.iconSelect
                        : item.icon
                    "
                    width="20px"
                    height="20px"
                    class="icon-img"
                  ></SvgIcon>
                  <span :title="item.label">{{ item.label }}</span>
                </a>
              </li>
              <span v-if="options.length == 0" class="section-value">
                暂未查询到部门信息
              </span>
            </ul>
          </div>
          <div class="custom-line"></div>
        </div>

        <!-- 文件类型 -->
        <div
          class="custom"
          :style="{
            height: conHeight + 'px',
          }"
        >
          <div class="custom-tree">
            <ul>
              <li class="custom-li" v-for="item in listData" :key="item.id">
                <a
                  :class="fileCut === item.name ? 'cut' : 'cut_hover'"
                  @click="onTree(item.name)"
                >
                  <SvgIcon :name="item.icon" class="custom-icon"></SvgIcon>
                  <span>{{ item.label }}</span>
                </a>
              </li>
            </ul>
          </div>
        </div>

        <!-- 个人标签 -->
        <div class="personal-label">
          <div class="label-container">
            <div class="label-title">
              <!-- <el-icon><ArrowDownBold /></el-icon> -->
              <span>个人标签({{ tagData.length }})</span>
            </div>
            <div class="label-img">
              <span class="label-add change" title="新建标签" @click="addTag">
                <el-icon><Plus /></el-icon>
              </span>
              <span class="change" title="标签管理" @click="onTagManage">
                <el-icon><Setting /></el-icon>
              </span>
            </div>
          </div>

          <div
            class="label-content"
            :style="{
              height: isHeight + 'px',
            }"
          >
            <ul class="label-ul">
              <li class="label-li" v-for="item in tagData" :key="item.id">
                <a
                  :class="tagID === item.id ? 'tag' : 'tag-hover'"
                  @click="onTag(item.id)"
                >
                  <SvgIcon
                    name="icon-tag-24"
                    width="20px"
                    height="20px"
                    class="icon-img"
                  ></SvgIcon>
                  <span>{{ item.name }}</span>
                </a>
              </li>
              <span v-if="tagData.length == 0" class="null-value">
                暂未创建个人标签
              </span>
            </ul>
          </div>
          <div class="custom-line"></div>
        </div>
      </el-aside>

      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>

  <!-- 创建标签 -->
  <NewlabelComponents :tagObj="tagObj" />
  <!-- 管理个人标签 -->
  <TagmanageComponents :tagManage="tagManage" />
</template>

<script setup lang="ts">
import { reactive, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { Plus, Setting } from "@element-plus/icons-vue";
import { listData } from "@/common/data";
import eventBus from "@/common/eventBus";
import router from "@/router";
import { Returndata } from "@/common/enum";
import SvgIcon from "@/components/SvgIcon/index.vue";
import { $queryThreeRout } from "@/api/document";
import { useUserStore } from "@/stort/user";
import { $queryTag } from "@/api/addtag";
import { TagList } from "@/interface/addtag";
import NewlabelComponents from "@/components/FileTable/Newlabel.vue";
import TagmanageComponents from "@/components/FileTable/Tagmanage.vue";

// 获取用户信息
const userStore = useUserStore();

// 获取浏览器上的地址
const route = useRoute();
const path = ref<string>("/document/section");
//获取当前路由标记 tag个人标签
const routeSign = ref<string>("");

// 存储部门数组
const options = ref([] as any[]);
// 存储个人标签数组
const tagData = ref<TagList[]>([]);
// 查询条件高度（公共文档、个人文档）
const conHeight = ref<number>();

// 个人标签高度
const isHeight = ref<number>();

/**
 * 添加标签
 */
const tagObj = reactive({
  visible: false,
});
const tagID = ref<string>("");

/**
 * 管理个人标签
 */
const tagManage = reactive({
  visible: false,
});

/**
 * 个人标签切换 事件
 */
const onTag = (id?: string) => {
  // 清空部门id、文件类型高亮值
  listObj.deptId = "";
  fileCut.value = "";

  routeSign.value = "tag";
  tagID.value = id!;
  // 跳转路由
  router.push({ name: "Tag", params: { id: id } });
};

/**
 * 新建标签
 */
const addTag = () => {
  tagObj.visible = true;
};

/**
 * 管理个人标签 事件
 */
const onTagManage = () => {
  tagManage.visible = true;
};

/**
 * 查询标签列表
 */
const queryList = () => {
  $queryTag()
    .then((res: any) => {
      tagData.value = res.data;
    })
    .catch((err) => {
      // 处理错误
      console.log("处理错误信息：", err);
    })
    .finally(() => {
      // 不管成功还是失败，都会执行这里的代码
    });
};

/**
 * 查询列表入参
 * @parentId 件夹id，根目录为 0
 * @ownership  归属，1：公共，2：用户，3：部门
 * @category 文件分类（全部、文档、图片）
 * @fileType 文档类型(后缀)
 * @deptId  部门id
 */
const listObj = reactive({
  parentId: "0",
  ownership: 1,
  category: "",
  fileType: "",
  deptId: "",
});
// 查询切换
const fileCut = ref<string>("all");

/**
 * 超级管理员登录
 * 查询所有部门方法
 */
const querySection = async () => {
  // 获取持久化内的登录用户信息
  const { userid } = userStore.user;
  // 查询部门信息
  const ret = await $queryThreeRout(userid);
  if (ret.code === Returndata.code) {
    const data = ret.data;
    if (data.length > 1) {
      // 超级管理员
      data.forEach((res: any) => {
        let obj = {
          id: 3,
          deptId: String(res.deptId),
          label: res.name,
          name: "section",
          path: path.value,
          icon: "department-file",
          iconSelect: "department-file-select",
        };
        options.value.push(obj);
      });
    } else {
      isHeight.value = 376;
    }
    listObj.deptId = String(data[0].deptId);
    onTree("all"); // 查询条件方法
  } else {
    console.log(ret);
  }
};

/**
 * 点击部门事件
 */
const onSection = (id: string) => {
  listObj.deptId = id;
  onTree("all");
};

/**
 * 查询条件方法
 * @typename 条件参数
 * @all 全部标识
 * @picture 图片标识
 * @document 文档标识（包含：doc、xls、ppt、pdf）
 */
const onTree = (typename: string) => {
  // 个人标签判断
  if (routeSign.value === "tag") {
    tagID.value = "";
    if (listObj.ownership == 1) {
      router.push("/document/commondoc");
    } else if (listObj.ownership == 2) {
      router.push("/document/minefile");
    } else {
      if (listObj.deptId === "") {
        listObj.deptId = options.value[0].deptId;
      }
      router.push(path.value);
    }
  }

  fileCut.value = typename;
  if (typename === "all") {
    listObj.category = typename;
    listObj.fileType = "";
  } else if (typename === "picture") {
    listObj.category = typename;
    listObj.fileType = "";
  } else {
    listObj.category = "document";
    listObj.fileType = typename;
  }
  handleClick();
};

/**
 * 调用查询列表方法
 */
const handleClick = () => {
  // 延迟加载
  setTimeout(() => {
    eventBus.emit("treeObj", listObj);
  }, 10);
};

// 标签创建成功调用onTagEventBus通讯
eventBus.on("onTagEventBus", () => {
  queryList();
});

// 监听路由变化
watch(
  () => route.path,
  (newPath) => {
    /**
     * @commondoc 公共文档标识 1
     * @minefile 我的文件标识 2
     * @section 部门文件标识 3
     */
    if (newPath) {
      const routeName = newPath.split("/").pop()!;

      if (routeName === "commondoc") {
        conHeight.value = 320;
        isHeight.value = 376;

        options.value.length = 0; // 清空存储部门数组
        listObj.ownership = 1;
        onTree("all"); // 查询条件方法
      } else if (routeName === "minefile") {
        conHeight.value = 320;
        isHeight.value = 376;

        options.value.length = 0; // 清空存储部门数组
        listObj.ownership = 2;
        onTree("all"); // 查询条件方法
      } else if (routeName === "section") {
        conHeight.value = 280;
        isHeight.value = 200;

        listObj.ownership = 3;
        const userInfoJson = sessionStorage.getItem("user_Info");
        // 如果存在，解析用户信息
        if (userInfoJson) {
          if (options.value.length == 0) {
            querySection(); // 初始化 超级管理员登录 查询所有部门方法
          }
        }
      }
      queryList(); // 查询标签列表
    }
  },
  { immediate: true }
);
</script>

<style lang="less" scoped>
.dociment-content {
  border: 1px solid #e5e9f2;
  border-right: none;
  background: #fff;
  min-height: calc(100vh - 100px);
}

// 部门
.section-label {
  margin-top: 6px;
  font-size: 14px;
  .section-container {
    color: #939090;
    display: flex;
    padding-left: 10px;
    height: 40px;
    line-height: 40px;
    box-shadow: 0 10px 6px -10px rgba(0, 0, 0, 0.1);
    .section-title {
      flex: 0 0 140px;
      span {
        padding-left: 4px;
      }
    }
    .section-img {
      flex: 1;
      color: #000;
      text-align: center;
      cursor: pointer;
      .section-add {
        margin-right: 10px;
      }
      .change:hover {
        color: #409eff;
      }
    }
  }

  .section-content {
    width: 100%;
    height: 198px;
    overflow: auto;
    color: #303133;
    margin-bottom: 16px;
    .section-ul {
      margin-top: 10px;
      .section-li {
        height: 36px;
        line-height: 36px;
        margin-bottom: 2px;
        a {
          width: 200px;
          height: 36px;
          line-height: 36px;
          display: block;
          margin: 0 8px 0 8px;
          border-radius: 12px;
          padding: 0 19px 0 19px;
          cursor: pointer;

          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        }

        .tag {
          height: 36px;
          line-height: 36px;
          background: #ecf5ff;
          color: #409eff;
          font-weight: 600;
          margin: 0 8px 0 8px;
          padding: 0 19px 0 19px;
          border-radius: 12px;

          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        }
        .tag-hover:hover {
          height: 36px;
          line-height: 36px;
          color: #409eff;
          padding: 0 19px 0 19px;
          background: #ecf5ff;
          margin: 0 8px 0 8px;
          border-radius: 12px;
        }
        .icon-img {
          position: relative;
          top: 5px;
        }
        span {
          padding-left: 8px;
        }
      }

      .section-value {
        display: block;
        padding: 25px 30px;
        text-align: center;
        color: #939090;
      }
    }
  }
}

// 文件类型
.custom {
  height: 280px;
  margin-top: 14px;
  .custom-tree {
    height: 280px;
    font-size: 14px;
    .custom-li {
      margin-bottom: 2px;
    }
    a {
      height: 36px;
      line-height: 36px;
      display: block;
      color: #303133;
      margin: 0 8px 0 8px;
      border-radius: 12px;
      padding: 0 0 0 19px;
      cursor: pointer;

      .custom-icon {
        position: relative;
        top: 4px;
        display: inline-block;
        margin: 0 14px 0 0;
      }
    }

    .cut {
      height: 36px;
      line-height: 36px;
      background: #ecf5ff;
      color: #409eff;
      font-weight: 600;
      margin: 0 8px 0 8px;
      border-radius: 12px;
    }
    .cut_hover:hover {
      height: 36px;
      line-height: 36px;
      color: #409eff;
      background: #ecf5ff;
      margin: 0 8px 0 8px;
      border-radius: 12px;
    }
  }
}

// 个人标签
.personal-label {
  font-size: 14px;
  .label-container {
    color: #939090;
    display: flex;
    padding-left: 10px;
    height: 40px;
    line-height: 40px;
    box-shadow: 0 10px 6px -10px rgba(0, 0, 0, 0.1);
    .label-title {
      flex: 0 0 140px;
      span {
        padding-left: 4px;
      }
    }
    .label-img {
      flex: 1;
      color: #000;
      text-align: center;
      cursor: pointer;
      .label-add {
        margin-right: 10px;
      }
      .change:hover {
        color: #409eff;
      }
    }
  }

  .label-content {
    width: 100%;
    height: 220px;
    overflow: auto;
    color: #303133;
    .label-ul {
      margin-top: 10px;
      .label-li {
        height: 36px;
        line-height: 36px;
        margin-bottom: 2px;
        a {
          width: 200px;
          height: 36px;
          display: block;
          line-height: 36px;
          margin: 0 8px 0 8px;
          border-radius: 12px;
          padding: 0 19px 0 19px;
          cursor: pointer;

          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        }

        .tag {
          height: 36px;
          line-height: 36px;
          background: #ecf5ff;
          color: #409eff;
          font-weight: 600;
          margin: 0 8px 0 8px;
          padding: 0 19px 0 19px;
          border-radius: 12px;

          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        }
        .tag-hover:hover {
          height: 36px;
          line-height: 36px;
          color: #409eff;
          padding: 0 19px 0 19px;
          background: #ecf5ff;
          margin: 0 8px 0 8px;
          border-radius: 12px;
        }
        .icon-img {
          position: relative;
          top: 5px;
        }
        span {
          padding-left: 8px;
        }
      }

      .null-value {
        display: block;
        padding: 25px 30px;
        text-align: center;
        color: #939090;
      }
    }
  }
}

.custom-line {
  height: 1px;
  margin: 0 12px;
  background-color: #f6f2f2;
}

//部门 滚动条
.section-content::-webkit-scrollbar {
  width: 4px;
  height: 10px;
  overflow: visible;
}
.section-content::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
.section-content::-webkit-scrollbar-thumb {
  border: solid transparent;
  border-width: 1px 0;
  background-clip: padding-box;
  background: #d8dce5;
}

// 个人标签 滚动条
.label-content::-webkit-scrollbar {
  width: 4px;
  height: 10px;
  overflow: visible;
}
.label-content::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
.label-content::-webkit-scrollbar-thumb {
  border: solid transparent;
  border-width: 1px 0;
  background-clip: padding-box;
  background: #d8dce5;
}
</style>
