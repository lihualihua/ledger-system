import eventBus from "@/common/eventBus";
import { FeaturePar } from "@/interface/document";
import { $post } from "@/utils/url";
import { ElMessage } from "element-plus";

/**
 * 恢复文件
 */
export const $recover = async (params: FeaturePar[]) => {
  const res = await $post("/api/doc/file/recover", params);
  if (res) {
    ElMessage({
      message: "恢复成功！",
      type: "success",
    });
    eventBus.emit("onDeleteEventBus", params);
    return res;
  }
  return res;
};

/**
 * 永久删除
 */
export const $permanentDelete = async (params: FeaturePar[]) => {
  let fileArray: string[] = [];
  params.forEach((res: FeaturePar) => {
    fileArray.push(res.fileId!);
  });
  const res = await $post(`/api/doc/file/delete`, fileArray);
  if (res) {
    ElMessage({
      message: "永久删除成功！",
      type: "success",
    });
    eventBus.emit("onDeleteEventBus", params);
    return res;
  }
  return res;
};
