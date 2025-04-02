import eventBus from "@/common/eventBus";
import { FeaturePar } from "@/interface/document";
import { $get, $post } from "@/utils/url";
import { ElMessage } from "element-plus";

// 查询列表数据
export const $queryList = async (url: string) => {
  const res = await $post(url);
  return res;
};

// 取消分享
export const $unshare = async (sum: number, param: FeaturePar[]) => {
  let num = 0;
  let array = [];
  for (let i = 0; i < param.length; i++) {
    const id = param[i].id;
    num += 1;
    array.push({
      id,
    });
    await $get(`/api/doc/share/cancel/${id}`);
  }
  if (num == sum) {
    ElMessage({
      message: `取消 ${sum} 个分享链接！`,
      type: "success",
    });
    eventBus.emit("shareEventBus", param);
    return;
  }
};
