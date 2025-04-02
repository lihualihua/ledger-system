/**
 * 文档库
 * 上传文件/上传文件夹、新增文件夹
 * @ DocumentUpload.vue
 */
export enum Stauts {
  // 上传文件
  DOC = "doc",
  // 上传文件夹
  FILE = "file",
  // 新增文件夹
  ADD = "addFile",
}

/**
 * 文档库
 */
export enum Document {
  // 公共文档
  COMMON = "common",
  // 我的文件
  MINEFILE = "minefile",
  // 部门文档
  SECTION = "section",
  // 收藏夹
  COLLECT = "collect",
  // 分享链接
  SHARE = "share",
  // 回收站
  RECYCLE = "recycle",
  // 全部
  ALL = "all",
  // 文档
  Document = "document",
  // 图片
  PICTURE = "picture",
  // 标签
  TAG = "tag",
}

/**
 * 文档删除
 * 标记 0：删除，1：正常
 */
export enum Deldoc {
  DELETE = "0",
  NORMAL = "1",
}

/**
 * 接口请求成功
 * 返回 数据格式
 */
export enum Returndata {
  // 0 为返回成功
  code = 0,
  data,
  msg = "",
  success = "true",
}

/**
 * 提示语
 */
export enum Prompt {
  // 收藏
  COLLECT = "批量收藏最多10个文件（夹）",
  // 收藏
  UNCOLLECT = "批量取消收藏最多10个文件（夹）",
  // 下载
  DOWNLOD = "批量下载最多10个文件（夹）",
  // 重命名
  RENAME = "重命名成功！",
  // 删除
  DELETE = "删除成功！",
  // 分享链接
  FOUND_SHARE = "分享链接创建成功！",
  // 上传文件为空
  FILE_EMPTY = "选择上传文件！",
  // 文件上传大于500MB
  FILE_UPLOAD = " 文件已超过500MB，请联系管理员！",
  // 登录成功
  LOGIN_SUCCEE = "登录成功！",
  // 退出登录
  LOG_OUT = "退出登录！",
  // 请求令牌已过期
  TOKEN_EXPIRATION = "请求令牌已过期，请重新登录！",
}

/**
 * 权限管理标识
 */
export enum Permission {
  // 超级管理员
  super_admin = "SUPER_ADMIN",
  // 普通用户
  general_user = "GENERAL_USER",
  // 部门管理员
  edpt_admin = "EDPT_ADMIN",
}

/**
 * 新增：add ，编辑：edit
 */
export enum AddEdit {
  Add = "add",
  EDIT = "edit",
}
