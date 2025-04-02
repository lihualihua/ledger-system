import { donwloadFile } from "@/utils/other";
import {
  $downloadGet,
  $downloadPost,
  $get,
  $post,
  $uploadPost,
} from "@/utils/url";
import { ElMessage } from "element-plus";

/**
 * 我的分配
 * @查询列表
 */
export const $queryInitiate = async (current: number, size: number) => {
  const res = await $get(`/api/ledger/findMyTemplates/${current}/${size}`);
  return res;
};

/**
 * 我的分配
 * @导入台账
 */
export const $upload = async (params: any, tempId: string) => {
  const res = await $uploadPost(`/api/ledger/upload/${tempId}`, params);
  return res;
};
/**
 * 我的分配
 * @模板库
 */
export const $uploadTemplatePost = async (systemFlag: number, params: any) => {
  const res = await $uploadPost(
    `/api/ledger/template/upload/${systemFlag}`,
    params
  );
  return res;
};
/**
 * 我的分配
 * @模板库 -- 系统默认模版 列表
 */
export const $findSysTemplateList = async () => {
  const res = await $get(`/api/ledger/findSysTemplateList`);
  return res;
};

/**
 * 我的分配
 * @模板库 -- 自定义模版 列表
 */
export const $findMyTemplates = async () => {
  const res = await $post(`/api/ledger/findMyTemplates/1/100`);
  return res;
};

/**
 * 我的分配
 * @模板库 -- 下载
 */
export const $download = async (
  type: number,
  templateId: string,
  name?: string
) => {
  const url = `/api/ledger/downloadLedgerTemplate/${type}/${templateId}`;
  try {
    const data = await $downloadGet(url);
    donwloadFile(data, "application/vnd.ms-excel", name);
  } catch (error) {
    ElMessage.error("文件下载失败！");
  }
};

/**
 * 我的分配
 * @模板库 -- 删除模版
 */
export const $deleteTemplate = async (id: string) => {
  const res = await $post(`/api/ledger/template/${id}`);
  return res;
};

/**
 * 我的分配
 * @催办
 */
export const $getTransact = async (tempId: string) => {
  const res = await $get(`/api/ledger/urge/${tempId}`);
  return res;
};

/**
 * 我的分配
 * @查看
 */
export const $queryDetailedInfo = async (tempId: string) => {
  const res = await $get(`/api/ledger/findTemplateData/${tempId}`);
  return res;
};

/**
 * 我的分配
 * @归档
 */
export const $updateStatus = async (tempId: string) => {
  const res = await $get(`/api/ledger/archive/${tempId}`);
  return res;
};

/**
 * 我的分配
 * @撤销
 */
export const $updateCancel = async (tempId: string) => {
  const res = await $get(`/api/ledger/revoke/${tempId}`);
  return res;
};

/**
 * 我的分配
 * @删除
 */
export const $deleteCancel = async (tempId: string) => {
  const res = await $get(`/api/ledger/delete/${tempId}`);
  return res;
};

/**
 * 分配给我
 * @查询列表
 */
export const $queryInvite = async (current: number, size: number) => {
  const res = await $get(`/api/ledger/findTemplatesWithMe/${current}/${size}`);
  return res;
};

/**
 * 分配给我
 * @上传台账
 */
export const $editorUpload = async (id: string, params: any) => {
  const res = await $uploadPost(
    `/api/ledger/editor/upload?tempId=${id}`,
    params
  );
  return res;
};

/**
 * 我的分配 && 分配给我
 * @下载台账Excel数据
 */
export const $downloadLedger = async (params: object) => {
  const url = "/api/ledger/downloadLedgerExcelData";
  try {
    const data = await $downloadPost(url, params);
    donwloadFile(data, "application/vnd.ms-excel", "测试文件名");
  } catch (error) {
    ElMessage.error("文件下载失败！");
  }
};

/**
 * 分配给我
 * @在线编辑 保存
 */
export const $onlineEdit = async (tempId: string, params: any) => {
  const res = await $post(`/api/ledger/save/${tempId}`, params);
  return res;
};
