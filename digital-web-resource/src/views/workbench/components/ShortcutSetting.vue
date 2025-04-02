<template>
  <el-dialog
    draggable
    width="600"
    :title="active ? '创建快捷方式' : '快捷方式'"
    v-model="props.dialogObj.visible"
    :close-on-click-modal="false"
    :destroy-on-close="true"
  >
    <!-- 快捷方式 -->
    <div class="feature-content" v-show="!active">
      <ul class="feature-ul">
        <li
          :class="['feature-li', item.checked ? 'cut' : '']"
          v-for="(item, index) in list"
          :key="index"
        >
          <div class="feature-div">
            <span
              v-show="!item.checked"
              class="feature-close"
              title="删除"
              @click="shortcutDelete(item.id)"
            >
              <el-icon><Close /></el-icon>
            </span>

            <span class="feature-select'" v-show="item.checked">
              <el-icon color="#409eff"><Select /></el-icon>
            </span>
          </div>
          <div @click="onSelect(item.id!, item.checked)">
            <SvgIcon
              name="public-document-select"
              width="30px"
              height="30px"
              class="feature-icon"
            ></SvgIcon>
            <span class="feature-name">{{ item.name }}</span>
          </div>
        </li>

        <li class="check-tips" v-if="list.length === 0">
          <span class="check-span">
            暂无数据<a class="check-a" @click="onShortcut">创建快捷方式</a>
          </span>
        </li>
      </ul>
    </div>

    <!-- 创建快捷方式 -->
    <div class="shortcut-form" v-show="active">
      <el-form
        ref="formRef"
        :model="shortcutForm"
        :rules="rules"
        label-width="140px"
      >
        <el-form-item label="名称：" prop="name">
          <el-select
            v-model="shortcutForm.name"
            placeholder="请选择名称"
            style="width: 300px"
            @change="changeName"
          >
            <el-option
              v-for="(item, index) in options"
              :key="index"
              :label="item.name"
              :value="item.name"
              :disabled="item.disabled"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="图标：">
          <span>{{ shortcutForm.icon != "" ? shortcutForm.icon : "--" }}</span>
        </el-form-item>
        <el-form-item label="访问地址：" prop="path">
          <el-input
            v-model="shortcutForm.path"
            placeholder="输入访问地址"
            maxlength="60"
            style="width: 300px"
          />
        </el-form-item>
        <el-form-item label="状态：">
          <el-switch v-model="statusValue" />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <span
          class="tag-manage"
          v-permission="`${permission}`"
          @click="onShortcut"
        >
          {{ active ? "返回" : "创建快捷方式" }}
        </span>
        <el-button class="btn-style" type="primary" @click="onOK(formRef)">
          保存
        </el-button>
        <el-button class="btn-style" @click="onCancel(formRef, 'cancel')">
          取 消
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { Close, Select } from "@element-plus/icons-vue";
import SvgIcon from "@/components/SvgIcon/index.vue";
import { reactive, ref, watch } from "vue";
import { ElMessage, FormInstance, FormRules } from "element-plus";
import { ShortcutList, ShortcutPar } from "@/interface/workbench";
import {
  $addShortcut,
  $queryAllShortcut,
  $selectShortcut,
  $shortcutDelete,
} from "@/api/workbench";
import { Permission, Returndata } from "@/common/enum";

const props = defineProps({
  dialogObj: {
    type: Object,
    default: {},
  },
});

// 用户权限（区分超级管理员和普通户用）
const permission = ref<string>("");

// 子组件调用父组件方法（通讯）
const emit = defineEmits(["shortcut-event"]);

const active = ref<boolean>(false);
// 创建快捷方式数据
const options = [
  {
    name: "公共文档",
    icon: "public-document-select",
    path: "/document/commondoc",
    order: 1,
    disabled: false,
  },
  {
    name: "我的文档",
    icon: "my-file-select",
    path: "/document/minefile",
    order: 2,
    disabled: false,
  },
  {
    name: "部门文档",
    icon: "department-file-select",
    path: "/document/section",
    order: 3,
    disabled: false,
  },
  {
    name: "智能搜索",
    icon: "icon-view-24",
    path: "/search",
    order: 4,
    disabled: false,
  },
];

/**
 * 创建快捷方式
 * form表单空值验证
 */
const formRef = ref<FormInstance>();
const rules = reactive<FormRules>({
  name: [{ required: true, message: "名称不能为空", trigger: "change" }],
  path: [{ required: true, message: "路由地址不能为空", trigger: "blur" }],
});

/**
 * 创建快捷方式
 * @icon 图标
 * @name 名称
 * @path 前端路由地址
 * @status 状态，0(false)：不启用，1(true)：启用
 * @order 排序
 */
const shortcutForm = reactive<ShortcutPar>({
  icon: "",
  name: "",
  path: "",
  status: null,
  order: null,
});

// 创建快捷方式 状态
const statusValue = ref<boolean>(true);

// 查询所有快捷键
const list = ref<ShortcutList[]>([]);
// 用户选择快捷键
const shortcutList = ref<string[]>([]);

/**
 * 确定
 */
const onOK = (formEl: FormInstance | undefined) => {
  // 配置快捷键方式
  if (!active.value) {
    $selectShortcut(shortcutList.value)
      .then((res: any) => {
        if (res.code === Returndata.code) {
          ElMessage({
            message: "配置成功",
            type: "success",
          });
        }
        emit("shortcut-event");
      })
      .catch((err) => {
        console.log("处理错误信息：", err);
      })
      .finally(() => {});
    props.dialogObj.visible = false;
    return;
  }

  // 创建快捷键方式
  if (!formEl) return;
  formEl.validate((valid) => {
    if (valid) {
      statusValue.value ? (shortcutForm.status = 1) : (shortcutForm.status = 0);

      $addShortcut(shortcutForm)
        .then((res: any) => {
          if (res.code === Returndata.code) {
            ElMessage({
              message: "创建成功",
              type: "success",
            });
            queryShortcut("");
          }
        })
        .catch((err) => {
          console.log("处理错误信息：", err);
        })
        .finally(() => {});
    }
  });
};

/**
 * 创建快捷键方式
 * 选择名称关联 图标 路由
 */
const changeName = (val: string) => {
  options.forEach((item) => {
    if (item.name === val) {
      shortcutForm.icon = item.icon;
      shortcutForm.path = item.path;
      shortcutForm.order = item.order;
    }
  });
};

/**
 * 查找所有常用功能(快捷键)
 */
const queryShortcut = (val?: string) => {
  $queryAllShortcut()
    .then((res: any) => {
      if (res.code === Returndata.code) {
        res.data.forEach((item: any) => {
          item.checked = false;
        });
        list.value = res.data;
        if (val) {
          writeBack();
        }
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

/**
 * 数据回写
 */
const writeBack = () => {
  const num = props.dialogObj.list.length;
  if (num > 0) {
    const data = props.dialogObj.list;
    for (let i = 0; i < list.value.length; i++) {
      const id = list.value[i].id;
      for (let j = 0; j < data.length; j++) {
        const ids = data[j].id;
        if (id === ids) {
          list.value[i].checked = true;
          shortcutList.value.push(ids);
          active.value = false;
        }
      }
    }
  }
};

/**
 * 删除
 */
const shortcutDelete = (id?: string) => {
  $shortcutDelete(id)
    .then((res: any) => {
      if (res.code === Returndata.code) {
        ElMessage({
          message: "删除成功",
          type: "success",
        });
        queryShortcut("");
      }
    })
    .catch((err) => {
      console.log("处理错误信息：", err);
    })
    .finally(() => {});
};

/**
 * 配置快捷键
 */
const onSelect = (id: string, bool?: boolean) => {
  if (!bool) {
    shortcutList.value.push(id);
    list.value.forEach((res: ShortcutList) => {
      if (id === res.id) {
        res.checked = true;
      }
    });
  } else {
    // 如果复选框被取消勾选，从 selectedIds 中移除 ID
    shortcutList.value = shortcutList.value.filter((item) => item !== id);
    list.value.forEach((res: ShortcutList) => {
      if (id === res.id) {
        res.checked = false;
      }
    });
  }
};

/**
 * 创建快捷方式/ 返回
 */
const onShortcut = () => {
  active.value = !active.value;
  // 返回
  if (!active.value) {
    writeBack();
  }
  onCancel(formRef.value);
  shortcutForm.icon = "";
  shortcutForm.name = "";
};

/**
 * 取消
 */
const onCancel = (formEl: FormInstance | undefined, sign?: string) => {
  if (!formEl) return;
  formEl.resetFields();
  if (sign) {
    props.dialogObj.visible = false;
    setTimeout(() => {
      active.value = false;
    }, 1000);
  }
};

// 监听 visible 的变化，当模态框打开时调用 初始化 方法
watch(
  () => props.dialogObj.visible,
  (newVal) => {
    if (newVal) {
      // 用户权限（区分超级管理员和普通户用）
      permission.value = Permission.super_admin;

      // 查找所有常用功能(快捷键)
      queryShortcut("write");
    }
  },
  { immediate: true }
);
</script>

<style scoped lang="less">
.feature-content {
  overflow: auto;
  min-height: 160px;
  .feature-ul {
    width: 100%;

    .feature-li {
      width: 100px;
      height: 85px;
      cursor: pointer;
      text-align: center;
      display: inline-block;
      margin: 6px;
      .feature-div {
        height: 14px;
        text-align: right;
        .feature-close {
          position: relative;
          right: 2px;
          color: #b9bcc0;
          display: none;
        }
        .feature-close:hover {
          color: #8e8f91;
        }
        .feature-select {
          position: relative;
          right: 2px;
          color: #b9bcc0;
          display: inline;
        }
      }
      .feature-icon {
        width: 30px;
        height: 30px;
        display: inline-block;
      }
      .feature-name {
        font-size: 13px;
        display: block;
      }
    }

    .feature-li:hover {
      border-radius: 8px;
      outline: none;
      box-shadow: 0px 0px 12px rgba(0, 0, 0, 0.12);
      .feature-close {
        display: inline;
      }
    }
    .cut {
      border-radius: 8px;
      box-shadow: 0px 0px 12px rgba(0, 0, 0, 0.12);
    }

    .check-tips {
      text-align: center;
      .check-span {
        line-height: 140px;
        color: #b8b9ba;
        .check-a {
          cursor: pointer;
          color: #78afeb;
          margin-left: 10px;
          text-decoration: none; /* 默认情况下不显示下划线 */
        }
        .check-a:hover {
          color: #409eff;
          text-decoration: underline; /* 鼠标悬停时显示下划线 */
        }
      }
    }
  }
}

.shortcut-form {
  min-height: 160px;
  padding-top: 40px;
}

.dialog-footer {
  .tag-manage {
    position: relative;
    right: 20px;
    color: rgba(0, 0, 0, 0.48);
    font-size: 14px;
    cursor: pointer;
    text-decoration: none; /* 默认情况下不显示下划线 */
  }
  .tag-manage:hover {
    text-decoration: underline; /* 鼠标悬停时显示下划线 */
  }
  .btn-style {
    min-width: 90px;
  }
}
</style>
