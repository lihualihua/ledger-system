import eventBus from "@/common/eventBus";
import { AddTag } from "@/interface/addtag";
import { $get, $post } from "@/utils/url";
import { ElMessage } from "element-plus";

/**
 * 新建标签
 */
export const $addTag = async (params: AddTag, name?: string) => {
  const res = await $post("/api/doc/tag/save", params);
  if (res) {
    ElMessage({
      message: name == "add" ? `新建标签成功！` : "修改标签成功",
      type: "success",
    });
    eventBus.emit("onTagEventBus");
    return res;
  }
  return res;
};

/**
 * 查询标签列表
 */
export const $queryTag = async () => {
  const res = await $get("/api/doc/tag/findMyTags");
  return res;
};

/**
 * 删除标签
 */
export const $deleteTag = async (id?: string) => {
  const res = await $get(`/api/doc/tag/delete/${id}`);
  return res;
};

/**
 * 文件添加
 */
export const $addAndDeleteTag = async (params: {}) => {
  const res = await $post(`/api/doc/tag/saveTags2Files`, params);
  return res;
};

/**
 * 单文件回写已添加标签
 *
 */
export const $findTagByFileId = async (fileId?: string) => {
  const res = await $get(`/api/doc/tag/findTagByFileId/${fileId}`);
  return res;
};

/**
 * 根据标签ID查询相关文件
 */
export const $tagListData = async (url: string, params: { id: string }) => {
  const res = await $post(url, params);
  return res;
};
