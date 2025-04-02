import * as CryptoJS from "crypto-js";

// 分片下载 总容量500MB
export const TotalCapacity = 500 * 1024 * 1024;
// 分片下载 每个分片30MB
export const EachPiece = 30 * 1024 * 1024;

// 分片上传 总容量500MB
export const UploadTotal = 500 * 1024 * 1024;
// 分片上传 每个分片10MB
export const UploadEachPiece = 10 * 1024 * 1024;
// 分片上传 大于50MB调用分片
export const UploadBig50MB = 50 * 1024 * 1024;

const other = {
  encryption: (src: string, keyWord: string) => {
    return encryption(src, keyWord);
  },
  decryption: (src: string, keyWord: string) => {
    return decryption(src, keyWord);
  },
  formatFileSize: (size: number) => {
    return formatFileSize(size);
  },
  formatDate: (dateStr: any) => {
    return formatDate(dateStr);
  },
  copyLink: (url?: string) => {
    return copyLink(url);
  },
  donwloadFile: (data: any, fileName: any) => {
    return donwloadFile(data, fileName);
  },
  formatTime: (value: string | number | undefined) => {
    return formatTime(value);
  },
  getGreeting: () => {
    return getGreeting();
  },
};

/**
 *加密处理
 */
export function encryption(src: string, keyWord: string) {
  const key = CryptoJS.enc.Utf8.parse(keyWord);
  // 加密
  var encrypted = CryptoJS.AES.encrypt(src, key, {
    iv: key,
    mode: CryptoJS.mode.CFB,
    padding: CryptoJS.pad.NoPadding,
  });
  return CryptoJS.enc.Hex.parse(encrypted.ciphertext.toString()).toString();
}

/**
 *  解密
 * @param {*} params 参数列表
 * @returns 明文
 */
export function decryption(src: string, keyWord: string) {
  const key = CryptoJS.enc.Utf8.parse(keyWord);
  // 解密逻辑
  var decryptd = CryptoJS.AES.decrypt(src, key, {
    iv: key,
    mode: CryptoJS.mode.CFB,
    padding: CryptoJS.pad.NoPadding,
  });

  return decryptd.toString(CryptoJS.enc.Utf8);
}

/**
 * 计算文件大小函数
 *
 */
export function formatFileSize(size: number) {
  if (size < 1024 * 1024) {
    const temp = size / 1024;
    return temp.toFixed(2) + "KB";
  } else if (size < 1024 * 1024 * 1024) {
    const temp = size / (1024 * 1024);
    return temp.toFixed(2) + "MB";
  } else {
    const temp = size / (1024 * 1024 * 1024);
    return temp.toFixed(2) + "GB";
  }
}

/**
 * 时间格式化
 */
export function formatDate(dateStr: any) {
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const seconds = String(date.getSeconds()).padStart(2, "0");
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

/**
 * 复制链接功能
 */
export function copyLink(url?: string) {
  // 创建输入框元素
  const input = document.createElement("input");
  // 将想要复制的值
  input.value = url!;
  document.body.appendChild(input);
  // 选中输入框
  input.select();
  // 执行浏览器复制命令
  document.execCommand("Copy");

  // 复制后移除输入框
  input.remove();
}

/**
 * 单文件下载
 * 打压缩包zip下载
 */
export function donwloadFile(data: any, mineType: string, name?: string) {
  let fileName = "";
  // 获取 Content-Disposition 头信息
  const contentDisposition = data.contentDisposition;
  if (contentDisposition) {
    const matches = contentDisposition.match(/filename=([^;]+)/);
    if (matches && matches[1]) {
      fileName = matches[1].replace(/"/g, ""); // 移除双引号
    }
  } else {
    fileName = name!;
  }
  // 创建一个Blob对象
  const blob = new Blob([data], { type: mineType });
  // 创建一个指向Blob的URL
  window.URL = window.URL || window.webkitURL;
  const href = URL.createObjectURL(blob);
  // 创建一个a标签用于下载
  const link = document.createElement("a");
  link.href = href;
  link.download = decodeURIComponent(fileName); // 指定下载文件的名称
  document.body.appendChild(link);
  // 触发下载
  link.click();
  // 清理
  document.body.removeChild(link);
  window.URL.revokeObjectURL(href);
}

/**
 * 高级搜索更新时间
 */
export function formatTime(value: string | number | undefined) {
  let name = "";
  if (value === "1") {
    name = "一天内";
  }
  if (value === "7") {
    name = "一周内";
  }
  if (value === "30") {
    name = "一月内";
  }
  if (value === "180") {
    name = "半年内";
  }
  if (value === "365") {
    name = "一年内";
  }
  return name;
}

/**
 * 根据当前时间显示不同的问候语
 */
export function getGreeting() {
  const now = new Date();
  const hours = now.getHours();

  if (hours >= 0 && hours < 6) {
    return "凌晨好！";
  } else if (hours >= 6 && hours < 9) {
    return "早上好！";
  } else if (hours >= 9 && hours < 12) {
    return "上午好！";
  } else if (hours >= 12 && hours < 14) {
    return "中午好！";
  } else if (hours >= 14 && hours < 18) {
    return "下午好！";
  } else {
    return "晚上好！";
  }
}

// 统一批量导出
export default other;
