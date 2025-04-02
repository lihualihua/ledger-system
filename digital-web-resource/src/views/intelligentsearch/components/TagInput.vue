<template>
  <!-- 标签搜索 -->
  <div class="advanced-box">
    <!-- AND & OR -->
    <div class="advanced-and">
      <el-select
        v-model="searchPar.selectLink1"
        placeholder="Select"
        style="width: 100px"
      >
        <el-option
          v-for="item in searchLink"
          :key="item.value"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </div>

    <div class="advanced-select" v-show="unpack.isExpanded">
      <span>标签</span>
      <span class="advanced-line"></span>
      <el-icon
        @click="onClear()"
        title="清空"
        :class="['advanced-icon', searchPar.tag.length > 0 ? 'clear' : '']"
      >
        <Close />
      </el-icon>

      <div class="tag-records" v-if="advancedSearch.tagVisible">
        <ul class="tag-records-ul">
          <li
            v-for="item in keywordList"
            :key="item.id"
            :class="['tag-records-li', item.checked ? 'word' : '']"
            @click="onAcquire(item.id, item.checked)"
          >
            <span class="tag-name" :title="item.name">
              {{ item.name }}
            </span>
          </li>
        </ul>
        <div class="tag-records-close">
          <span class="close-span" @click="shut">关闭</span>
        </div>
      </div>
    </div>

    <input
      v-model="searchPar.tagName"
      maxlength="100"
      autocomplete="off"
      @input="onRecords()"
      @focus="onFocus()"
      @blur="closeDropdown"
      class="advanced-input"
      @keyup.enter="onSearch"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { $queryTagList } from "@/api/search";
import { searchLink } from "@/common/data";
import { Returndata } from "@/common/enum";
import { TagList } from "@/interface/addtag";
import { Close } from "@element-plus/icons-vue";

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

// 子组件调用父组件方法（通讯）
const emit = defineEmits(["search-event"]);

// 标签模糊搜索记录
const keywordList = ref<TagList[]>([]);

/**
 * 搜索--模糊搜索
 */
const onFocus = () => {
  onRecords();
};

/**
 * 模糊查询标签列表
 */
const onRecords = () => {
  if (props.searchPar.tagName === "") {
    shut();
    return;
  }
  const params = {
    tag: [props.searchPar.tagName],
  };
  $queryTagList(params).then((res: any) => {
    if (res.code === Returndata.code && res.data.records.length > 0) {
      props.advancedSearch.tagVisible = true;

      const data = res.data.records;
      data.forEach((res: any) => {
        res.checked = false;
      });
      keywordList.value = data; // 清空历史记录列表
    } else {
      shut(); // 关闭搜索历史记录
      keywordList.value.length = 0; // 清空历史记录列表
    }
  });
};

// 回写文件名
const onAcquire = (id?: string, bool?: boolean) => {
  if (!bool) {
    // 勾选
    keywordList.value.forEach((res: any) => {
      if (id === res.id) {
        res.checked = true;
      }
    });
  } else {
    // 如果复选框被取消勾选，从 selectedIds 中移除 ID
    keywordList.value.forEach((res: any) => {
      if (id === res.id) {
        res.checked = false;
      }
    });
  }

  // 格式 标签iput输入框值格式
  props.searchPar.tagName = "";
  props.searchPar.tag.length = 0;

  keywordList.value.forEach((res: any) => {
    if (res.checked) {
      props.searchPar.tagName = props.searchPar.tagName + "+" + res.name;
      props.searchPar.tag.push(res.name);
    }
  });
  props.searchPar.tagName = props.searchPar.tagName.substring(1);
};

/**
 * input搜索--清空
 */
const onClear = () => {
  props.searchPar.tagName = "";
  props.searchPar.tag.length = 0;
  shut(); // 关闭搜索历史记录
};

/**
 * 搜索查询
 */
const onSearch = () => {
  shut();
  emit("search-event");
};

/**
 * 关闭搜索历史记录
 */
const shut = () => {
  props.advancedSearch.tagVisible = false;
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
::v-deep(.el-select .el-input__inner) {
  text-align: center;
}

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
        top: -1px;
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

    // 标签
    .tag-records {
      width: 486px;
      position: absolute;
      left: 114px;
      z-index: 9;
      margin-top: 1px;
      padding: 10px 8px;
      background-color: rgb(255, 255, 255);
      border-radius: 0px 0px 10px 10px;
      border-top: none;
      border-right: 1px solid rgb(210, 210, 210);
      border-bottom: 1px solid rgb(210, 210, 210);
      border-left: 1px solid rgb(210, 210, 210);
      border-image: initial;
      .tag-records-ul {
        text-align: left;
        line-height: 14px;
        .tag-records-li {
          height: 30px;
          line-height: 30px;
          width: 105px;
          border: 1px solid #d2d2d2;
          display: inline-block;
          margin: 6px;
          text-align: center;
          padding: 0 10px;
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        }

        .word {
          border: 1px solid #d9ecff;
          background-color: #ecf5ff;
          .tag-name {
            color: #409eff;
            font-weight: 600;
          }
        }
        .tag-records-li:hover {
          color: #409eff;
          border: 1px solid #d9ecff;
          background: #f5f5f7;
          .tag-name {
            color: #409eff;
          }
        }
      }

      .tag-records-close {
        height: 20px;
        line-height: 20px;
        float: right;
        .close-span:hover {
          color: #337ecc;
        }
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
</style>
