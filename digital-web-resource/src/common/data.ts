import { DocType } from "@/interface/document";

/**
 * 文档管理
 */
export const listData = [
  {
    id: 1,
    label: "全部",
    name: "all",
    icon: "all-monochrome-select",
  },
  {
    id: 2,
    label: "DOC",
    name: "doc",
    icon: "icon-docx-select",
  },
  {
    id: 3,
    label: "XLS",
    name: "xls",
    icon: "icon-xlsx-select",
  },
  {
    id: 4,
    label: "PPT",
    name: "ppt",
    icon: "icon-ppt-select",
  },
  {
    id: 5,
    label: "PDF",
    name: "pdf",
    icon: "icon-pdf-select",
  },
  {
    id: 6,
    label: "图片",
    name: "picture",
    icon: "img-monochrome-select",
  },
  {
    id: 7,
    label: "其他",
    name: "other",
    icon: "icon-other-file-select",
  },
];

// 分享时间
export const shareType = [
  { id: 1, name: "永久生效", value: "1" },
  { id: 2, name: "一月内", value: "4" },
  { id: 3, name: "半年内", value: "3" },
  { id: 4, name: "一年内", value: "2" },
  { id: 5, name: "自定义日期", value: "5" },
];

// 分享链接
export const shareData = [
  { id: 1, name: "我的分享", value: 2 },
  { id: 2, name: "分享给我", value: 3 },
];

// 回收站
export const keywordData = [
  { id: 1, name: "我的文件", value: 2 },
  { id: 2, name: "部门文档", value: 3 },
  { id: 1, name: "公共文档", value: 1 },
];

// 智能搜索 文档类型
export const documentType = [
  { id: 1, name: "全部", value: "" },
  { id: 2, name: "DOC", value: "doc" },
  { id: 3, name: "XLS", value: "xls" },
  { id: 4, name: "PPT", value: "ppt" },
  { id: 5, name: "PDF", value: "pdf" },
  { id: 6, name: "文件夹", value: "1" },
  { id: 7, name: "其他", value: "other" },
];

// 智能搜索 快速访问
export const timeList = [
  { id: 1, name: "不限", value: "" },
  { id: 2, name: "一天内", value: "1" },
  { id: 3, name: "一周内", value: "7" },
  { id: 4, name: "一月内", value: "30" },
  { id: 5, name: "半年内", value: "180" },
  { id: 6, name: "一年内", value: "365" },
];

// 智能搜索
export const searchLink = [
  { id: 1, name: "AND", value: "and" },
  { id: 2, name: "OR", value: "or" },
];

/**
 * 文档图标
 * 用于映射
 */
export const docIcon: DocType = {
  file: "icon-file-fill-24",
  doc: "icon-doc-fill-24",
  docx: "icon-doc-fill-24",
  xls: "icon-excel-fill-24",
  xlsx: "icon-excel-fill-24",
  et: "icon-excel-fill-24",
  ett: "icon-excel-fill-24",
  ppt: "icon-ppt-fill-24",
  pptx: "icon-ppt-fill-24",
  pdf: "icon-pdf-fill-24",
  svg: "icon-ai-fill-24",

  jpg: "icon-ai-fill-24",
  png: "icon-ai-fill-24",
  gif: "icon-ai-fill-24",
  tif: "icon-ai-fill-24",
  tiff: "icon-ai-fill-24",
  bmp: "icon-ai-fill-24",
  jpeg: "icon-ai-fill-24",
  psd: "icon-ai-fill-24",
  raw: "icon-ai-fill-24",
  eps: "icon-ai-fill-24",

  txt: "icon-txt-fill-24",
  json: "icon-nor-m-24",
  zip: "icon-zip-fill-24",
  rar: "icon-zip-fill-24",
  drawio: "icon-nor-m-24",
  dwg: "icon-nor-m-24",
};

/**
 * 下载 功能
 * 文件类型请求头
 */
export const downloadHeader: DocType = {
  xls: "application/vnd.ms-excel",
  xlsx: "application/vnd.ms-excel",
  doc: "application/msword",
  docx: "application/msword",
  ppt: "application/vnd.openxmlformats-officedocument.presentationml.presentation",
  pptx: "application/vnd.openxmlformats-officedocument.presentationml.presentation",
  pdf: "application/pdf",
  zip: "application/zip",
  rar: "application/x-tar",
  txt: "text/plain",
  json: "application/json",
  jpg: "image/jpeg",
  png: "image/png",
  gif: "image/gif",
  svg: "application/octet-strem",
  drawio: "application/octet-strem",
  et: "application/octet-strem",
  ett: "application/octet-strem",
  tif: "application/octet-strem",
  bmp: "application/octet-strem",
  dwg: "application/octet-strem",
  bat: "application/octet-strem",
  exe: "application/octet-strem",
};

/**
 * 消息中心
 */
export const messageCenter = [
  {
    id: 1,
    label: "系统消息",
    name: "systemmessage",
    num: 0,
    icon: "icon-system-message",
    icon2: "icon-system-message-bright",
  },
  {
    id: 2,
    label: "通知",
    num: 0,
    name: "notification",
    icon: "icon-notification-24",
    icon2: "icon-notification-bright",
  },
];

/**
 * 状态映射关系
 */
export const statusMap: { [key: number]: string } = {
  1: "已发布",
  2: "已归档",
  3: "已废除",
};

/**
 * 菜单图标
 */
export const menyIcon = [
  {
    value: "HomeFilled",
  },
  {
    value: "Management",
  },
  {
    value: "Grid",
  },
  {
    value: "Promotion",
  },
  {
    value: "Platform",
  },
  {
    value: "Tools",
  },
  {
    value: "InfoFilled",
  },
  {
    value: "CircleCheckFilled",
  },
  {
    value: "SuccessFilled",
  },
  {
    value: "WarningFilled",
  },
  {
    value: "CircleCloseFilled",
  },
  {
    value: "QuestionFilled",
  },
  {
    value: "WarnTriangleFilled",
  },
  {
    value: "MoreFilled",
  },
  {
    value: "Menu",
  },
  {
    value: "UploadFilled",
  },
  {
    value: "HelpFilled",
  },
  {
    value: "Share",
  },
  {
    value: "StarFilled",
  },
  {
    value: "Comment",
  },
  {
    value: "Histogram",
  },
  {
    value: "DeleteFilled",
  },
  {
    value: "RemoveFilled",
  },
  {
    value: "CirclePlusFilled",
  },
  {
    value: "Checked",
  },
  {
    value: "Ticket",
  },
  {
    value: "Failed",
  },
  {
    value: "TrendCharts",
  },
  {
    value: "VideoCameraFilled",
  },
  {
    value: "List",
  },
  {
    value: "PictureFilled",
  },
  {
    value: "CameraFilled",
  },
  {
    value: "BellFilled",
  },
  {
    value: "LocationFilled",
  },
  {
    value: "Opportunity",
  },
  {
    value: "PhoneFilled",
  },
  {
    value: "WalletFilled",
  },
  {
    value: "GoodsFilled",
  },
  {
    value: "Flag",
  },
  {
    value: "BrushFilled",
  },
  {
    value: "Briefcase",
  },
  {
    value: "Stamp",
  },
  {
    value: "Shop",
  },
  {
    value: "Folder",
  },
  {
    value: "Search",
  },
  {
    value: "User",
  },
  {
    value: "Operation",
  },
  {
    value: "Delete",
  },
  {
    value: "Finished",
  },
  {
    value: "MessageBox",
  },
  {
    value: "Message",
  },
  {
    value: "Edit",
  },
  {
    value: "EditPen",
  },
  {
    value: "Filter",
  },
  {
    value: "Compass",
  },
  {
    value: "PieChart",
  },
  {
    value: "Warning",
  },
  {
    value: "MuteNotification",
  },
  {
    value: "Bell",
  },
  {
    value: "Refresh",
  },
  {
    value: "Lock",
  },
  {
    value: "Unlock",
  },
  {
    value: "Hide",
  },
  {
    value: "View",
  },
  {
    value: "ChatDotSquare",
  },
  {
    value: "ChatLineSquare",
  },
  {
    value: "ChatLineRound",
  },
  {
    value: "Odometer",
  },
  {
    value: "Position",
  },
  {
    value: "Discount",
  },
  {
    value: "Setting",
  },
  {
    value: "ChatDotRound",
  },
  {
    value: "Connection",
  },
  {
    value: "Notification",
  },
  {
    value: "Star",
  },
  {
    value: "Pointer",
  },
  {
    value: "Service",
  },
  {
    value: "Link",
  },
  {
    value: "Aim",
  },
  {
    value: "Search",
  },
  {
    value: "DataLine",
  },
  {
    value: "Reading",
  },
  {
    value: "FirstAidKit",
  },
  {
    value: "FolderAdd",
  },
  {
    value: "DocumentRemove",
  },
  {
    value: "CopyDocument",
  },
  {
    value: "DataAnalysis",
  },
  {
    value: "SetUp",
  },
  {
    value: "Postcard",
  },
  {
    value: "Collection",
  },
  {
    value: "Memo",
  },
  {
    value: "Tickets",
  },
  {
    value: "Notebook",
  },
  {
    value: "Document",
  },
  {
    value: "FolderOpened",
  },
  {
    value: "Files",
  },
  {
    value: "FolderChecked",
  },
  {
    value: "DocumentAdd",
  },
  {
    value: "ScaleToOriginal",
  },
  {
    value: "Monitor",
  },
  {
    value: "OfficeBuilding",
  },
  {
    value: "School",
  },
  {
    value: "Wallet",
  },
  {
    value: "Stopwatch",
  },
  {
    value: "CollectionTag",
  },
  {
    value: "Coin",
  },
  {
    value: "Paperclip",
  },
  {
    value: "Suitcase",
  },
  {
    value: "Cpu",
  },
  {
    value: "Calendar",
  },
  {
    value: "Printer",
  },
  {
    value: "MagicStick",
  },
];
