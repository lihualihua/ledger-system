import { $get, $post } from "@/utils/url";

/**
 * 通知
 * 查询消息列表
 */
export const $queryMessageList = async (current: number, size: number) => {
  const res = await $post(`/api/notification/findMyMessage/${current}/${size}`);
  return res;
};

/**
 * 通知
 * 更新通知为已读状态
 */
export const $updateStatus = async (id: string) => {
  const res = await $post(`/api/notification/updateReadStatus/${id}`);
  return res;
};

/**
 * 通知
 * 更新所有通知为已读状态
 */
export const $updateAllStatus = async () => {
  const res = await $post(`/api/notification/updateAllReadStatus`);
  return res;
};

/**
 * 通知
 * 查找未读通知总数
 */
export const $queryCount = async () => {
  const res = await $get(`/api/notification/findMyUnreadCount`);
  return res;
};
