/**
 * 列表入参
 */
export interface SearchPar extends FileSearchPar {
  // 基础查询（文件名/【关键词/标签】）
  baseFullText?: string;
  // 文件名称
  fileName?: string;
  // 文件归属，1:公共，2:用户，3：部门
  ownership?: number;
  // 关键词/标签标识（传入and/or）
  tagAndOr?: string;
  // 关键词/标签
  tag?: string;
  // 上传者标识（传入and/or）
  createByAndOr?: string;
  // 上传者
  createBy?: string;
  // 文件类型（doc|xls|ppt|pdf）
  fileType?: string | number;
  // 是否文件夹，1：文件夹
  folderFlag?: number;
  // 上传起始日期
  startTime?: string;
  // 上传结束日期
  endTime?: string;
  // 更新时间（不限:’’,一天内:1,一周内:7,一月内:30,半年内:180,一年内:365）
  updateTimeSlot?: number | string;
  // 提示标识
  title?: string;
}

/**
 * 列表出参
 */
export interface SearchData {
  // 文件id
  fileId: string;
  // 文件名称
  fileName?: string;
  // 关键词/标签
  tag?: number;
  // 上传者
  createBy?: string;
  // 文件类型（png,doc,zip...）
  fileType?: string;
  // 文件大小
  fileSize?: number;
  // 文件路径
  filePath?: string;
  // 上传时间
  createTime?: string;
  // 更新时间
  updateTime?: string;
  //删除标记，0：删除，1：正常
  delFlag?: number;
  // 是否文件夹(1：文件夹)
  folderFlag?: number;
  //文件归属，1:公共，2:用户，3：部门
  ownership?: number;
  // 下载量
  downloadCount?: number;
  // 收藏量
  collectCount?: number;
  // 预览量
  previewCount?: number;
  // 图标
  icon?: string;

  createById?: number;
  // 是否勾选
  checked?: boolean;
}

/**
 * 搜索查询历史记录出参
 */
export interface HistoryRecords {
  // Id
  id?: number;
  // 用户id
  userId?: number;
  // 时间
  createTime?: string;
  // 更新时间
  updateTime?: string;
  // 历史内容
  content: string;
  // 上传者
  createBy?: string;
}

/**
 * 文件夹查询搜索入参
 */
export interface FileSearchPar {
  // 基础查询名称
  fileName?: string;
  // 区分基础搜索：base 和 高级搜索：advanced
  searchType?: string;
  // 全部文件标识
  category?: string;
  // 删除标记，0：删除，1：正常
  delFlag?: string;
  // 归属，1：公共，2：用户，3：部门
  ownership?: number;
  // 文件夹ID
  parentId?: string;
}
