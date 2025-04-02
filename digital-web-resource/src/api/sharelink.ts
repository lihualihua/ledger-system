import { $post } from "@/utils/url";

/**
 * 查询分享列表
 */
export const $queryShareList = async (params: {
  md5Value: string;
  parentId: string;
}) => {
  const url = `/api/doc/share/findShareFiles`;
  const res = await $post(url, params);
  return res;
};
