import {
  DataPar,
  FeaturePar,
  FileChunk,
  UploadFilePar,
} from "@/interface/document";
import { $downloadGet, $get, $post, $uploadPost } from "@/utils/url";
import { donwloadFile, EachPiece, TotalCapacity } from "@/utils/other";
import { docIcon, downloadHeader } from "@/common/data";
import { Prompt, Returndata } from "@/common/enum";
import eventBus from "@/common/eventBus";
import { ElMessage, ElMessageBox } from "element-plus";
import axios from "axios";
import { sharePar } from "@/interface/share";
import { useUserStore } from "@/stort/user";

// 获取登录用户信息 token
const userStore = useUserStore();
const token = userStore.user.token;

export const request = (res: any, param?: any, mes?: string) => {
  if (res && res.code === Returndata.code) {
    if (mes) {
      ElMessage({
        message: mes,
        type: "success",
      });
      eventBus.emit("onEventBus", param);
      return;
    } else {
      /**
       * 事件总线通讯
       * 调用查询列表接口
       */
      eventBus.emit("onEventBus", "");
    }
  } else {
    ElMessage({
      message: res.msg,
      type: "warning",
    });
  }
};

/**
 * 查询部门信息
 */
export const $queryThreeRout = async (userId?: string) => {
  const res = await $get(`/api/dept/getDeptByUserId/${userId}`);
  return res;
};

// 查询列表数据
export const $listData = async (url: string, params: DataPar) => {
  const res = await $post(url, params);
  return res;
};

/**
 * 文件上传 接口
 * @parentId 父级文件夹id
 * @ownership 归属，1：公共，2：用户，3：部门
 * @svgImg 图标
 * @deptId 部门id
 */
export const $uploadFile = async (
  params: UploadFilePar,
  formData: any,
  svgImg: string
) => {
  const url = `/api/doc/file/upload/${params.ownership}/${params.parentId}?icon=${svgImg}&deptId=${params.deptId}`;
  $uploadPost(url, formData)
    .then((res: any) => {
      request(res);
    })
    .catch((err) => {
      // 处理错误
      console.log("文件上传处理错误信息：", err);
    });
};

/**
 * 新增文件夹 接口
 * @parentId 父级文件夹id
 * @fileName 文件夹/文件名称
 * @ownership 归属，1：公共，2：用户，3：部门
 * @icon 图标
 */
export const $addFile = async (params: UploadFilePar) => {
  let addFile: UploadFilePar = {
    parentId: params.parentId,
    fileName: params.fileName,
    ownership: params.ownership,
    icon: docIcon["file"],
    deptId: params.deptId,
  };
  const res = await $post("/api/doc/file/createFolders", addFile);
  request(res);
};

/**
 * 查询下载文件夹
 * 文件大小
 */
export const $fileSizeSum = async (params: Array<FeaturePar>) => {
  const res = await $post("/api/doc/query/findFileSizeSum ", params);
  return res;
};

// 下载 入口
export const $onDownload = async (
  url: string,
  fileParam: Array<FeaturePar>,
  sum: number
) => {
  // 限制文件下载个数
  if (sum > 10) {
    return;
  }
  let bool = false;
  // 判断是文件夹或者选择多文档
  for (let i = 0; i < fileParam.length; i++) {
    const type = fileParam[i].fileType;
    if (type === "file" || sum > 1) {
      bool = true;
      break;
    }
  }

  // 500MB
  if (bool) {
    // 多个文件或一个文件夹
    // 查询下载文件夹大小
    const res = await $fileSizeSum(fileParam);

    const fileSize = res.data.fileSizeSum;

    // 文档（fileSize）小于500MB进入
    if (fileSize <= TotalCapacity) {
      // 调用打包文件
      $packZip(url, fileParam, fileSize);
    } else {
      // 弹出提示框
      ElMessageBox.confirm("下载文件大于500MB，只允许单文件下载！", "下载", {
        cancelButtonText: "关闭",
        type: "warning",
        showConfirmButton: false,
      });
      return;
    }
  } else {
    // 单文件
    const fileSize = fileParam[0].size!;
    // 单文件下载大于30MB
    if (fileSize > EachPiece) {
      // 分片下载
      const fileId = fileParam[0].fileId!;
      $downloadFile(url, fileId);
      return;
    }
    // 单文件下载
    $download(fileParam);
  }
};

/**
 * 多文件批量打包接口
 */
export const $packZip = async (
  location: string,
  fileParam: Array<FeaturePar>,
  size: number
) => {
  let messageInstance = null;
  try {
    // 显示加载中消息
    messageInstance = ElMessage({
      message: "正在打包压缩中，请勿重复操作！",
      type: "success",
      duration: 0, // 不自动关闭
      showClose: true, // 显示关闭按钮
    });

    const res = await $post("/api/doc/file/zip", fileParam);
    const fileId = res.data.fileId;

    // 关闭加载中消息
    if (messageInstance) {
      messageInstance.close();
    }

    // size 小于 30MB
    if (size < EachPiece) {
      const fileParam = [
        {
          fileId: fileId,
          fileName: "",
          fileType: "zip",
        },
      ];
      // 单文件下载 接口
      $download(fileParam);
    } else {
      // 大于30MB调用 分片下载
      $downloadFile(location, fileId);
    }
  } catch (error) {
    // 如果有加载中消息，先关闭它
    if (messageInstance) {
      messageInstance.close();
    }
  }
};

/**
 * 单文件下载
 */
export const $download = async (fileParam: Array<FeaturePar>) => {
  // 文件下载
  const object = fileParam[0];
  const type = object.fileType;
  if (type) {
    const fileType = object.fileType.toLowerCase();

    // @fileType 文件后缀类型
    const url = `/api/doc/file/download/${object.fileId}`;
    try {
      const data = await $downloadGet(url);

      if (fileType && downloadHeader[fileType]) {
        const mineType = downloadHeader[fileType];
        donwloadFile(data, mineType, object.fileName);
      } else {
        // 不包含的后缀调用
        donwloadFile(data, "application/octet-strem", object.fileName);
      }
    } catch (error) {
      ElMessage.error("文件下载失败！");
    }
  }
};

/***
 * 分片下载
 * 文件操作 30MB 调用
 */
export const $downloadFile = async (location: string, fileId: string) => {
  let messageInstance = null;
  // 显示加载中消息
  messageInstance = ElMessage({
    message: "文件下载中...",
    type: "success",
    duration: 0, // 不自动关闭
    showClose: true, // 显示关闭按钮
  });

  const url = location + `/api/doc/file/segment/download/${fileId}`; // 替换为你的后端地址
  const response = await axios.head(url, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  const totalSize = parseInt(response.headers["content-length"], 10);

  const chunkSize = 30 * 1024 * 1024; // 每个分片30MB

  const chunks: FileChunk[] = [];

  for (let start = 0; start < totalSize; start += chunkSize) {
    const end = Math.min(start + chunkSize - 1, totalSize - 1);
    const chunk = await fetchFileChunk(url, start, end);
    chunks.push(chunk);
  }

  // 合并所有分片
  const combinedArray = new Uint8Array(totalSize);
  let currentPos = 0;

  for (const chunk of chunks) {
    combinedArray.set(new Uint8Array(chunk.data!), currentPos);
    currentPos += chunk.data!.byteLength;
  }

  let fileName = "";
  const contentDisposition = response.headers["content-disposition"];
  if (contentDisposition) {
    const matches = contentDisposition.match(/filename=([^;]+)/);
    if (matches && matches[1]) {
      fileName = matches[1].replace(/"/g, ""); // 移除双引号
    }
  }

  // 创建Blob对象并触发下载
  const blob = new Blob([combinedArray.buffer], {
    type: "application/zip",
  });

  // 创建一个指向Blob的URL
  window.URL = window.URL || window.webkitURL;
  const href = URL.createObjectURL(blob);

  const link = document.createElement("a");
  link.href = href;
  link.download = decodeURIComponent(fileName); // 指定下载文件的名称
  document.body.appendChild(link);
  // 触发下载
  link.click();
  // 清理
  document.body.removeChild(link);
  window.URL.revokeObjectURL(href);

  // 如果有加载中消息，先关闭它
  setTimeout(() => {
    messageInstance.close();
  }, 3000);
};
export const fetchFileChunk = async (
  url: string,
  start: number,
  end: number
): Promise<FileChunk> => {
  const response = await axios.get(url, {
    responseType: "arraybuffer",
    headers: {
      Authorization: `Bearer ${token}`,
      Range: `bytes=${start}-${end}`,
    },
  });
  return { start, end, data: response.data };
};

/**
 * 重命名 接口
 * @fileId 当前文件夹/文件 id
 * @fileName 文件夹/文件名称
 * rename 重命名标识
 */
export const $renameFile = async (params: UploadFilePar) => {
  const res = await $post("/api/doc/file/rename", params);
  params.status = "rename";
  request(res, params, Prompt.RENAME);
};

/**
 * 收藏 接口
 */
export const $collect = async (sum: number, fileParam: FeaturePar[]) => {
  let len = 0;
  let array = [];
  let res = {};
  for (let i = 0; i < fileParam.length; i++) {
    const fileId = fileParam[i].fileId;
    array.push({
      fileId,
      collectFlag: 1,
    });
    len += 1;
    res = await $post(`/api/doc/file/collect/${fileId}`, {});
  }
  if (len == sum) {
    request(res, array, `收藏 ${sum} 个文件（夹）成功`);
  }
};

/**
 * 取消收藏 接口
 */
export const $unCollect = async (sum: number, fileParam: FeaturePar[]) => {
  let num = 0;
  let array = [];
  let res = {};
  for (let i = 0; i < fileParam.length; i++) {
    const fileId = fileParam[i].fileId;
    num += 1;
    array.push({
      fileId,
      collectFlag: 0,
    });
    res = await $post(`/api/doc/file/collect/remove/${fileId}`, {});
  }
  if (num == sum) {
    request(res, array, `取消收藏 ${sum} 个文件（夹）成功`);
  }
};

/**
 * 删除 接口
 */
export const $deleteFile = async (array: Array<{ fileId: number }>) => {
  const res = await $post("/api/doc/file/recycle", array);
  request(res, "", Prompt.DELETE);
};

/**
 * 分享 接口
 */
export const $shareFile = async (param: sharePar) => {
  const res = await $post("/api/doc/share/save", param);
  return res;
};

/**
 * 查询详细信息
 */
export const $fileInfo = async (fileId: string) => {
  const res = await $get(`/api/doc/query/findFileDetailInfo/${fileId}`);
  return res;
};
