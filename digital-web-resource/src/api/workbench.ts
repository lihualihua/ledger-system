import { ShortcutPar } from "@/interface/workbench";
import { $get, $post } from "@/utils/url";

/**
 * 查找公共文件、我的文件、部门文件总数
 */
export const $queryFileCount = async () => {
  const res = await $get("/api/doc/query/findFileCount");
  return res;
};

/**
 * 创建快捷键
 */
export const $addShortcut = async (params: ShortcutPar) => {
  const res = await $post("/api/doc/function/save", params);
  return res;
};

/**
 * 查找所有常用功能(快捷键)
 */
export const $queryAllShortcut = async () => {
  const res = await $get("/api/doc/function/findAllFunction");
  return res;
};

/**
 * 删除常用功能(快捷键)
 */
export const $shortcutDelete = async (functionId?: string) => {
  const res = await $post(`/api/doc/function/delete/${functionId}`);
  return res;
};

/**
 * 用户绑定常用功能(快捷键)
 */
export const $selectShortcut = async (params: any) => {
  const res = await $post("/api/doc/function/binding", params);
  return res;
};

/**
 * 查找我的常用功能
 */
export const $queryShortcut = async () => {
  const res = await $get("/api/doc/function/findMyFunction");
  return res;
};
