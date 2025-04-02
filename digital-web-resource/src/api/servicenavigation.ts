import {
  BusinessPar,
  BusinessTable,
  FlowTable,
} from "@/interface/servicenavigation";
import { $get, $post } from "@/utils/url";

/**
 * 流程域
 * 新增
 */
export const $domainCreate = async (params: FlowTable) => {
  const res = await $post("/api/nav/process/domain/create", params);
  return res;
};

/**
 * 流程域
 * 查询列表
 */
export const $queryDomainList = async (params?: string) => {
  const res = await $get(
    `/api/nav/process/domain/findAllProcessDomain${params}`
  );
  return res;
};

/**
 * 流程域
 * 编辑
 */
export const $domainUpdate = async (params: FlowTable) => {
  const res = await $post("/api/nav/process/domain/update", params);
  return res;
};

/**
 * 流程域
 * 删除
 */
export const $domainDelete = async (id: string) => {
  const res = await $post(`/api/nav/process/domain/delete/${id}`);
  return res;
};

/**
 * 流程域
 * 查找附件存储目录列表
 */
export const $directoryList = async () => {
  const res = await $get("/api/nav/process/domain/findStorageDirList");
  return res;
};

/**
 * 业务导航
 * 新增
 */
export const $saveAdd = async (params: BusinessPar) => {
  const res = await $post("/api/nav/wiki/save", params);
  return res;
};

/**
 * 业务导航
 * 查询列表
 */
export const $serviceList = async (
  domainId: string,
  current: number,
  size: number
) => {
  const res = await $get(
    `/api/nav/wiki/findWikiList/${domainId}/${current}/${size}`
  );
  return res;
};

/**
 * 业务导航（创建新版本 && 编辑）
 * 查询版本数据
 */
export const $saveContent = async (params: BusinessPar) => {
  const res = await $post("/api/nav/wiki/findWikiContent", params);
  return res;
};

/**
 * 业务导航
 * 查询当前数据所有版本号
 */
export const $saveAllVersion = async (
  wikiCode: string,
  status?: number | string
) => {
  const res = await $get(
    `/api/nav/wiki/findWikiVersionList/${wikiCode}?status=${status}`
  );
  return res;
};

/**
 * 业务导航
 * 保存 创建新版本
 */
export const $saveVersionContent = async (params: BusinessTable) => {
  const res = await $post("/api/nav/wiki/saveContent", params);
  return res;
};

/**
 * 业务导航
 * 删除
 *
 */
export const $serviceDelete = async (id: string) => {
  const res = await $get(`/api/nav/wiki/delete/${id}`);
  return res;
};

/**
 * 工作台 - 业务导航
 * 查询列表
 */
export const $findWikiReleaseList = async (params: { domainId: string }) => {
  const res = await $post(`/api/nav/wiki/findWikiReleaseList/1/1000`, params);
  return res;
};

/**
 * 工作台 - 业务导航
 * 根据ID查找流程域标题
 */
export const $saveProcessDomainTitle = async (id: string) => {
  const res = await $get(`/api/nav/process/domain/findById/${id}`);
  return res;
};
