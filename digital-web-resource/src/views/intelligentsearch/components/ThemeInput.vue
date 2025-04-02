<template>
  <!-- 主题 -->
  <div class="advanced-box">
    <div class="advanced-and"></div>
    <div class="advanced-select">
      <span>主题</span>
      <span class="advanced-line"></span>
      <el-icon
        :class="['advanced-icon', searchPar.fileName != '' ? 'clear' : '']"
        title="清空"
        @click="onClear()"
      >
        <Close />
      </el-icon>
      <div class="advanced-records" v-if="props.advancedSearch.visible">
        <ul class="advanced-records-ul">
          <div class="records-line"></div>
          <li
            class="advanced-records-li"
            v-for="item in fileNameList"
            :key="item.id"
            @click="onAcquire(item.content)"
          >
            {{ item.content }}
          </li>
        </ul>
      </div>
    </div>
    <input
      v-model="searchPar.fileName"
      maxlength="100"
      autocomplete="off"
      @input="onRecords()"
      @focus="onFocus()"
      @blur="closeDropdown"
      class="advanced-input"
      @keyup.enter="onSearch"
    />
    <div class="advanced-butt" v-show="!unpack.isExpanded">
      <el-button type="primary" class="advanced-sub" @click="onSearch">
        高级检索
      </el-button>
      <el-button link class="search-advanced" @click="onBack">
        基础搜索
        <el-icon class="el-icon--right"><ArrowRight /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { Close, ArrowRight } from "@element-plus/icons-vue";
import { HistoryRecords } from "@/interface/search";
import { $content, $historyList } from "@/api/search";
import { Returndata } from "@/common/enum";

/**
 * 子组件调用父组件方法（通讯）
 * @send-back 返回基础搜索（1）
 * @search-event 搜索查询
 */
const emit = defineEmits(["send-back", "search-event"]);

const props = defineProps({
  // 入参
  searchPar: {
    type: Object,
    default: {},
  },
  // 全局关闭历史记录列表下拉
  advancedSearch: {
    type: Object,
    default: {},
  },
  // 高级搜索特殊参数
  unpack: {
    type: Object,
    default: {},
  },
});

// 主题历史记录
const fileNameList = ref<HistoryRecords[]>([]);

/**
 * 查询历史记录入参
 * @content 搜索值
 */
const historyPar = reactive({
  content: "",
});

/**
 * 搜索--模糊搜索
 */
const onFocus = () => {
  onRecords();
};

/**
 * input框输入事件
 * 主题 搜索历史记录
 */
const onRecords = () => {
  if (props.searchPar.fileName === "") {
    shut();
    return;
  }
  historyPar.content = props.searchPar.fileName;
  $historyList(historyPar).then((res: any) => {
    // 查询条数大于0
    if (res.code === Returndata.code && res.data.total > 0) {
      fileNameList.value = res.data.records; // 清空历史记录列表
      props.advancedSearch.visible = true;
    } else {
      shut(); // 关闭搜索历史记录
      fileNameList.value.length = 0; // 清空历史记录列表
    }
  });
};

/**
 * 获取历史记录中的值
 * 点击事件
 */
const onAcquire = (name: string) => {
  props.searchPar.fileName = name;
  shut();
};

/**
 * 搜索查询
 */
const onSearch = () => {
  $content(historyPar); // 新增搜索历史记录
  shut();
  emit("search-event");
};

/**
 * input搜索--清空
 */
const onClear = () => {
  props.searchPar.fileName = "";
  shut(); // 关闭搜索历史记录
};

/**
 * 返回基础搜索
 */
const onBack = () => {
  emit("send-back", 1);
};

/**
 * 关闭搜索历史记录
 */
const shut = () => {
  props.advancedSearch.visible = false;
};

/**
 * 如果点击发生在下拉框外部，则关闭下拉框
 */
const closeDropdown = (event: FocusEvent) => {
  if (
    !props.advancedSearch.dropdownref?.contains(event.relatedTarget as Node)
  ) {
    event.stopPropagation(); // 阻止事件冒泡
  }
};
</script>

<style scoped lang="less">
.advanced-box {
  margin-bottom: 12px;
  .advanced-and {
    width: 100px;
    line-height: 40px;
    margin-right: 16px;
    display: inline-flex;
  }
  .advanced-select {
    display: inline-block;
    position: absolute;
    width: 120px;
    height: 40px;
    line-height: 40px;
    font-size: 14px;
    cursor: pointer;
    span {
      color: #939090;
    }
    .advanced-line {
      width: 1px;
      height: 26px;
      position: absolute;
      top: 8px;
      left: 114px;
      background-color: #d2d2d2;
    }
    .advanced-icon {
      display: none;
      position: absolute;
      top: 13px;
      left: 576px;
      color: #9195a3;
    }
    .clear {
      display: flex;
    }
    .advanced-records {
      position: absolute;
      left: 114px;
      font-size: 14px;
      cursor: pointer;
      .advanced-records-ul {
        width: 486px;
        text-align: left;
        position: absolute;
        top: -3px;
        z-index: 9;
        margin-top: 1px;
        padding: 10px 0 10px 0;
        background-color: #fff;
        border-radius: 0 0 10px 10px;
        border: 1px solid #d2d2d2;
        border-top: none;
      }
      .records-line {
        width: 92%;
        height: 1px;
        margin: -10px 0 10px 8px;
        background-color: #d2d2d2;
      }
      .advanced-records-li {
        height: 30px;
        line-height: 30px;
        padding-left: 14px;
      }
      .advanced-records-li:hover {
        background: #f5f5f7;
        color: #4e6ef2;
      }
    }
  }
  .advanced-input {
    width: 600px;
    height: 40px;
    line-height: 40px;
    padding: 0 30px 0 120px;
    font-size: 14px;
    border: 1px solid #cdd4dc;
    border-radius: 4px;
  }
}

.advanced-butt {
  display: inline-block;
  margin-left: 20px;
  .advanced-sub {
    width: 96px;
    height: 38px;
    font-size: 16px;
  }
  .search-advanced {
    color: rgba(0, 0, 0, 0.48);
  }
  .search-advanced:hover {
    color: #409eff;
  }
}
</style>
