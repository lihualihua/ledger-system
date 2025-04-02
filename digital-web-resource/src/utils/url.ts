import { Returndata } from "@/common/enum";
import instance from "./request";
import { ElMessage } from "element-plus";

/**
 * 登录特殊接口
 */
export const $loginPost = async (url: string) => {
  let { data } = await instance.post(url);
  return data;
};

/**
 * get请求方法
 */
export const $get = async (url: string, params: object = {}) => {
  let { data } = await instance.get(url, { params });
  if (data && data.code === Returndata.code) {
    return data;
  }
  ElMessage.error(data.msg);
};

/**
 *  post请求方法
 */
export const $post = async (url: string, params: object = {}) => {
  let { data } = await instance.post(url, params);
  if (data && data.code === Returndata.code) {
    return data;
  }
  ElMessage.error(data.msg);
};

/**
 * 单（批量）上传文件
 */
export const $uploadPost = async (url: string, params: object = {}) => {
  let { data } = await instance.post(url, params, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  if (data && data.code === Returndata.code) {
    return data;
  }
  ElMessage.error(data.msg);
};

/**
 * 单文件下载
 */
export const $downloadGet = async (url: string) => {
  let { data } = await instance.get(url, {
    responseType: "blob",
  });
  return data;
};

/**
 * 文件下载
 * @台账用到
 */
export const $downloadPost = async (url: string, params: object = {}) => {
  let { data } = await instance.post(url, params, {
    responseType: "blob",
  });
  return data;
};
