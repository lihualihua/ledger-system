/**
 * 查询列表
 * 入参
 */
export interface DataPar {
  // 件夹id，根目录为 0
  parentId?: string;
  // 归属，1：公共，2：用户，3：部门
  ownership?: number;
  // 文件分类
  category?: string;
  //文档类型
  fileType?: string;
  //删除标记，0：删除，1：正常
  delFlag?: string;
  // 模糊搜索
  search?: string;
}

/**
 * 新增文件夹 &上传文件 & 上传文件夹
 * 入参
 */
export interface UploadFilePar {
  // 文件夹ID 0：根目录
  parentId?: string;
  // 1: 公共文档  2：我的文件 3：部门文档
  ownership?: number;
  // 文件夹名称
  fileName?: string;
  // 图标
  icon?: string;
  // 状态
  status?: string;
  // 部门id
  deptId?: string;
}

/**
 * 功能操作 如：下载、删除、重命名、分享等
 * 入参
 */
export interface FeaturePar {
  id?: number;
  // 文件id
  fileId?: string;
  // 文件夹名称
  fileName?: string;
  // 文档后缀类型
  fileType: string;
  // 文件大小
  size?: number;
}

/**
 * 分片上传（下载）
 */
export interface FileChunk {
  // file?: File;
  // index?: number;
  // 开始位置（上传、下载）
  start: number;
  // 结束位置（上传、下载）
  end: number;
  // 每个节点的中间值（下载）
  data?: ArrayBuffer | null;
  // 分片上传
  promise?: Promise<any> | undefined;
  // 分片上传进度
  progress?: number;
}

/**
 * 计算文件大小
 * 入参
 */
export interface UploadListPar {
  // 文件名称
  name?: string;
  // 文件大小
  size?: string;
}

/**
 * 列表字段
 * 出参
 */
export interface TableData extends ForEach {
  // 收藏量
  collectCount?: number;
  // 创建者
  createBy?: string;
  // 创建时间
  createTime?: string;
  // 删除标识：1-删除，0-正常
  delFlag?: number;
  // 下载量
  downloadCount?: number;
  // 文件编号
  fileId?: string;
  // 文件名
  fileName?: string;
  // 文件路径
  filePath?: number;
  // 文件大小
  fileSize?: number;
  // 文件类型
  fileType?: string;
  // 文件夹标记,1：文件夹
  folderFlag?: number;
  // 文件归属,1:公共，2：用户，3：部门
  ownership?: number;
  // 父级文件夹id
  parentId?: string;
  // 预览量
  previewCount?: number;
  // 状态
  status?: number;
  // 更新人
  updateBy?: string;
  // 更新时间
  updateTime?: string;
  // 图标
  icon?: string;
  /**
   * 未收藏: 0
   * 已收藏: 1
   */
  collectFlag?: number;
}

/**
 * 文档类型
 */
export interface DocType {
  [propName: string]: string;
}

/**
 * ForEach 循环
 */
export interface ForEach {
  forEach?: any;
}

/**
 * 数据返回结构
 */
export interface Export {
  code?: number;
  data?: number;
  msg?: string;
  success?: boolean;
}
