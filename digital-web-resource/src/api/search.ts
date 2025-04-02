import { PagingPar } from "@/interface/common";
import { SearchPar } from "@/interface/search";
import { $post } from "@/utils/url";

// 查询列表数据
export const $listData = async (paging: PagingPar, params: SearchPar) => {
  const url = `/api/doc/query/searchFiles/fileList/${paging.currentPage}/${paging.pageSize}`;
  const res = await $post(url, params);
  return res;
};

// 新增历史记录
export const $content = async (params: { content: string }) => {
  const url = `/api/doc/query/add/searchFiles/content`;
  const res = await $post(url, params);
  return res;
};

// 查询历史记录列表
export const $historyList = async (params: { content: string }) => {
  const url = `/api/doc/query/searchFiles/historyList/1/7`;
  const res = await $post(url, params);
  return res;
};

// 查询标签列表
export const $queryTagList = async (params: {}) => {
  const url = `/api/doc/tag/findPublicTags/1/100`;
  const res = await $post(url, params);
  return res;
};

/**
 * 收藏 接口
 */
export const $collect = async (fileId?: string) => {
  const res = await $post(`/api/doc/file/collect/${fileId}`, {});
  return res;
};
