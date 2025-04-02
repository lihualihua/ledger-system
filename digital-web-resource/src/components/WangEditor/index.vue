<template>
  <div style="border: 1px solid #ccc">
    <!-- 工具栏 -->
    <Toolbar
      style="border-bottom: 1px solid #ccc"
      :editor="editorRef"
      :defaultConfig="toolbarConfig"
      :mode="mode"
    />
    <!-- 文本框 -->
    <Editor
      style="height: 600px; overflow-y: hidden"
      v-model="props.content"
      :defaultConfig="editorConfig"
      :mode="mode"
      @onCreated="handleCreated"
      @onChange="contentChange"
    />
  </div>
</template>

<script setup lang="ts">
import "@wangeditor/editor/dist/css/style.css"; // 引入 css
import "@/css/wangEditor/wangeditor.css"; // 引入wangEditor 富文本框自定义 css
import { onBeforeUnmount, ref, shallowRef } from "vue";
import { Editor, Toolbar } from "@wangeditor/editor-for-vue";

const props = defineProps({
  content: {
    type: String,
    default: "",
  },
});

// 用于父组件接收wangeditor实时编辑的内容
const emit = defineEmits(["wangeditor-event"]);

// 预览值
const editors = ref(null);

// 内容值
// const content = ref<string>("");

// 工具栏模式
const mode = ref<string>("default"); // 或 'simple'
// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef();
// 工具栏配置
const toolbarConfig = {};
// 文本框内容
const editorConfig = { placeholder: "请输入内容..." };

// 编辑器内容、选区变化时的回调函数。
const contentChange = (editor: any) => {
  editors.value = editor;
  //用于父组件接收wangeditor实时编辑的内容;
  emit("wangeditor-event", editors.value);
};

// 组件创建时
const handleCreated = (editor: any) => {
  // editorRef.value = editor; // 记录 editor 实例，重要！
  editorRef.value = Object.seal(editor); // 一定要用 Object.seal() ，否则会报错
};

// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value;
  if (editor == null) return;
  editor.destroy();
});
</script>

<style scoped lang="less">
// 无序列表
::v-deep(ul li) {
  list-style: initial;
}
// 有序列表
::v-deep(ol li) {
  list-style-type: decimal;
}
</style>
