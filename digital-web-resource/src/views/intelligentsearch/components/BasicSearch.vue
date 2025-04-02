<template>
  <!-- 基础搜索 -->
  <div class="search-select">
    <div class="search-div">
      <span>主题</span>
    </div>
    <span class="search-line"></span>
    <el-icon
      @click="onClear"
      title="清空"
      :class="['search-icon', props.status.searchName != '' ? 'clear' : '']"
    >
      <Close />
    </el-icon>
    <!-- 展示历史记录列表 -->
    <div class="search-records" v-if="props.status.isVisible">
      <ul class="records-ul">
        <li
          class="records-li"
          v-for="item in historyList"
          :key="item.id"
          @click="fetchRecord(item.content)"
        >
          {{ item.content }}
        </li>
      </ul>
    </div>
  </div>

  <input
    v-model="props.status.searchName"
    maxlength="100"
    autocomplete="off"
    @input="onRecords"
    @focus="onFocus"
    @blur="closeDropdown"
    class="search-input"
    @keyup.enter="onSearch()"
  />
  <div class="search-span">
    <input
      type="submit"
      value="搜 索"
      class="search-submit"
      @click="onSearch()"
    />
    <el-button link class="search-advanced" @click="onAdvancedSearch">
      高级搜索
      <el-icon class="el-icon--right"><ArrowRight /></el-icon>
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { HistoryRecords } from "@/interface/search";
import { Close, ArrowRight } from "@element-plus/icons-vue";
import { $content, $historyList } from "@/api/search";
import { Returndata } from "@/common/enum";

const props = defineProps({
  // 入参
  status: {
    type: Object,
    default: {},
  },
});

/**
 * 子组件调用父组件方法（通讯）
 * @tag-search 切换基础搜索（1）和高级搜索（2）
 * @simple-search 查询列表事件
 */
const emit = defineEmits(["send-switch", "tag-search", "simple-search"]);

// 历史列表
const historyList = ref<HistoryRecords[]>([]);

/**
 * 查询历史记录入参
 * @content 搜索值
 */
const historyPar = reactive({
  content: "",
});

/**
 * 搜索--模糊搜索 方法
 */
const onFocus = () => {
  onRecords();
};

/**
 * input框输入事件
 * 查询历史记录列表 方法
 */
const onRecords = () => {
  if (props.status.searchName === "") {
    shut();
    return;
  }
  // 简单搜索
  historyPar.content = props.status.searchName; // 入参
  $historyList(historyPar).then((res: any) => {
    // 查询条数大于0
    if (res.code === Returndata.code && res.data.total > 0) {
      props.status.isVisible = true;
      historyList.value = res.data.records;
    } else {
      shut(); // 关闭搜索历史记录
      historyList.value.length = 0; // 清空历史记录列表
    }
  });
};

/**
 * 获取历史记录中的值
 * 点击事件
 */
const fetchRecord = (name: string) => {
  props.status.searchName = name;
  shut();
};

/**
 * 搜索--清空按钮
 * 点击事件
 */
const onClear = () => {
  props.status.searchName = "";
  shut();
};

/**
 * 切换高级搜索
 */
const onAdvancedSearch = () => {
  shut(); // 关闭搜索历史记录
  emit("send-switch", "556px");
  emit("tag-search", 2);
};

/**
 * 搜索查询
 */
const onSearch = () => {
  $content(historyPar); // 新增搜索历史记录
  shut(); // 关闭搜索历史记录
  emit("simple-search", "base");
};

/**
 * 关闭搜索历史记录
 */
const shut = () => {
  props.status.isVisible = false;
};

/**
 * 如果点击发生在下拉框外部，则关闭下拉框
 */
const closeDropdown = (event: FocusEvent) => {
  if (!props.status.dropdownref?.contains(event.relatedTarget as Node)) {
    event.stopPropagation(); // 阻止事件冒泡
  }
};
</script>

<style scoped lang="less">
.search-select {
  width: 120px;
  height: 44px;
  line-height: 44px;
  font-size: 14px;
  cursor: pointer;
  position: absolute;
  display: inline-block;
  .search-div {
    font-weight: 500;
    font-size: 16px;
    span {
      color: rgba(0, 0, 0, 0.64);
    }
  }
  .search-line {
    width: 1px;
    height: 26px;
    position: absolute;
    top: 10px;
    left: 114px;
    background-color: #d2d2d2;
  }

  .search-icon {
    display: none;
    position: absolute;
    top: 16px;
    left: 496px;
    color: #9195a3;
  }
  .clear {
    display: flex;
  }

  .search-records {
    position: absolute;
    left: 113px;
    font-size: 14px;
    cursor: pointer;

    .records-ul {
      width: 408px;
      text-align: left;
      margin-top: 1px;
      padding: 10px 0 10px 0;
      border-radius: 0 0 10px 10px;
      position: absolute;
      top: 2px;
      z-index: 9;
      background-color: #fff;
      border: 1px solid rgba(0, 0, 0, 0.08);
      border-top: none;
      box-shadow: 0px 5px 12px 4px rgba(0, 0, 0, 0.08);
    }

    .records-li {
      height: 30px;
      line-height: 30px;
      padding-left: 14px;
    }

    .records-li:hover {
      color: #4e6ef2;
      background-color: #f5f5f7;
    }
  }
}

.search-input {
  width: 520px;
  height: 44px;
  line-height: 44px;
  font-size: 16px;
  padding: 0 30px 0 120px;
  border: 2px solid #a8a9ab;
  border-right: none;
  border-radius: 4px 0px 0px 4px;
}

.search-span {
  height: 44px;
  line-height: 44px;
  display: inline-block;

  .search-submit {
    cursor: pointer;
    width: 108px;
    height: 44px;
    line-height: 43px;
    padding: 0;
    background: 0 0;
    background-color: #409eff;
    border-radius: 0 4px 4px 0;
    font-size: 17px;
    color: #fff;
    box-shadow: none;
    font-weight: 400;
    border: none;
    outline: 0;
  }

  .search-submit:hover {
    background-color: #337ecc;
  }

  .search-advanced {
    margin-left: 10px;
    color: rgba(0, 0, 0, 0.48);
  }

  .search-advanced:hover {
    color: #409eff;
  }
}
</style>
