<template>
  <div>
    <!-- 主题 -->
    <ThemeInputComponents
      :advancedSearch="props.advancedSearch"
      :searchPar="searchPar"
      :unpack="unpack"
      @search-event="onSearch"
      @send-back="onBack"
    />

    <div v-show="unpack.isExpanded">
      <!-- 标签搜索 -->
      <TagInputComponents
        :advancedSearch="props.advancedSearch"
        :searchPar="searchPar"
        :unpack="unpack"
        @search-event="onSearch"
      />

      <!-- 上传者 -->
      <UploaderInputComponents
        :searchPar="searchPar"
        :unpack="unpack"
        @search-event="onSearch"
      />

      <!-- 类型选择搜索 -->
      <FileTypeComponents :searchPar="searchPar" :unpack="unpack" />

      <!-- 时间范围搜索 -->
      <TimeFrameComponents :searchPar="searchPar" :unpack="unpack" />

      <div class="advanced-button">
        <el-button link @click="onReset">重置条件</el-button>
        <el-button type="primary" size="large" class="sub" @click="onSearch">
          检 索
        </el-button>
        <el-button class="sub" size="large" @click="onBack">
          基础搜索
        </el-button>
      </div>
    </div>

    <!-- 收起、展开 -->
    <div class="advanced-packupunfold" @click="onExpansion">
      <SvgIcon
        :name="unpack.isExpanded ? 'packup-s' : 'unfold-s'"
        width="4em"
        height="1em"
      ></SvgIcon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import SvgIcon from "@/components/SvgIcon/index.vue";
import ThemeInputComponents from "./ThemeInput.vue"; // 主题
import TagInputComponents from "./TagInput.vue"; // 标签
import UploaderInputComponents from "./UploaderInput.vue"; // 上传者
import FileTypeComponents from "./FileType.vue"; // 文件类型
import TimeFrameComponents from "./TimeFrame.vue"; // 时间范围

const props = defineProps({
  // 全局关闭历史记录列表下拉
  advancedSearch: {
    type: Object,
    default: {},
  },
});

/**
 * 子组件调用父组件方法（通讯）
 * @send-switch 展开收起
 * @send-back 返回基础搜索（1）
 * @searchClick 调用高级搜索方法
 */
const emit = defineEmits(["send-switch", "send-back", "searchClick"]);

/**
 * 查询搜索入参
 * @fileName 主题
 * @tag 标签
 * @search3 上传者
 * @selectLink1 标签/标签标识（传入and/or）
 * @selectLink2 标签/标签标识（传入and/or）
 * @isWord 类型搜索
 * @dataTime 时间范围
 * @selectValue 更新时间
 * @searchType 高级搜索：advanced
 */
const searchPar = reactive({
  fileName: "",
  tagName: "",
  tag: [] as any[],
  search3: "",
  selectLink1: "and",
  selectLink2: "and",
  isWord: "",
  dataTime: "",
  selectValue: "",
  searchType: "advanced",
});

// 收起展开高度
const height = ref<string>("");

/**
 * @isExpanded 收起/展开
 */
const unpack = reactive({
  isExpanded: true,
});

/**
 * 高级搜索入参
 * @fileName 文件名称
 * @ownership 文件归属，1:公共，2:用户，3：部门 暂不用
 * @tagAndOr 标签/标签标识（传入and/or）
 * @tag 标签/标签
 * @createByAndOr 上传者标识（传入and/or）
 * @createBy 上传者
 * @fileType 文件类型（doc|xls|ppt|pdf）
 * @folderFlag 是否文件夹，1：文件夹
 * @startTime 上传起始日期
 * @endTime 上传结束日期
 * @updateTimeSlot 更新时间（不限:’’,一天内:1,一周内:7,一月内:30,半年内:180,一年内:365）
 */
const onSearch = () => {
  props.advancedSearch.visible = false; // 高级搜索主题历史列表
  props.advancedSearch.tagVisible = false; // 高级搜索标签历史列表

  unpack.isExpanded = false;
  onHeight();

  let fileType: string | number = "";
  let folderFlag = 0;
  // searchPar.isWord=1 文件夹
  if (searchPar.isWord === "1") {
    fileType = "";
    folderFlag = 1;
  } else {
    fileType = searchPar.isWord;
  }
  let advanced = {
    fileName: searchPar.fileName,
    tagAndOr: searchPar.selectLink1,
    tag: searchPar.tag,
    createByAndOr: searchPar.selectLink2,
    createBy: searchPar.search3,
    fileType: fileType,
    folderFlag: folderFlag,
    startTime: searchPar.dataTime[0],
    endTime: searchPar.dataTime[1],
    updateTimeSlot: searchPar.selectValue,
    searchType: searchPar.searchType,
  };
  const newObject = { ...advanced };

  emit("searchClick", newObject);
};

/**
 * 返回简单搜索
 */
const onBack = () => {
  emit("send-back", 1);
  emit("send-switch", "237px");
};

/**
 * 重置条件
 */
const onReset = () => {
  searchPar.fileName = "";
  searchPar.selectLink1 = "and";
  searchPar.tagName = "";
  searchPar.tag.length = 0;
  searchPar.selectLink2 = "and";
  searchPar.search3 = "";
  searchPar.isWord = "";
  searchPar.dataTime = "";
  searchPar.selectValue = "";
};

/**
 * 收起、展开
 */
const onExpansion = () => {
  unpack.isExpanded = !unpack.isExpanded;
  onHeight();
};

const onHeight = () => {
  if (unpack.isExpanded) {
    height.value = "556px";
  } else {
    height.value = "237px";
  }
  emit("send-switch", height.value);
};
</script>

<style scoped lang="less">
.advanced-button {
  margin: 48px 0 10px 0;
  .sub {
    width: 160px;
    height: 34px;
    font-size: 16px;
    margin-left: 30px;
  }
}

.advanced-packupunfold {
  position: relative;
  top: 22px;
}
</style>
