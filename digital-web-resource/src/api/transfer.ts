import eventBus from "@/common/eventBus";
import { FeaturePar } from "@/interface/document";
import { $post } from "@/utils/url";
import { ElMessage } from "element-plus";

/**
 * 转移到 接口
 */
export const $transferPost = async (
  parentId: string,
  params: Array<FeaturePar>
) => {
  const url = `/api/doc/file/move/${parentId}`;
  await $post(url, params);

  ElMessage({
    message: `文件转移成功！`,
    type: "success",
  });
  eventBus.emit("onEventBus", "");
  return;
};
