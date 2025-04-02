/**
 * 列表字段
 * 出参
 */
export interface ShareTableData {
  // 文件ID
  id?: string;
  // 文件名
  name?: string;
  // 分享链接
  shareUrl?: string;
  // 访问模式，0：内部公开，1：用户或部门访问
  accessMode?: number;
  // 分享人
  createBy?: string;
  // 分享时间
  createTime?: string;
  // 更新时间
  updateTime?: string;
  // 分享状态 有效期标志 0：自定义日期，1：永久有效，2：一年，3：六个月，4：一个月  必填
  validFlag?: number;
  // 描述
  desc?: string;
  // 更新人
  updateBy?: string;
  // 分享人 ID
  userId?: number;
  // 有效期日期
  validDate?: string;
  // 基于分享文件id生成的md5值
  md5Value?: string;
  // 分享部门
  shareDepts?: Array<string>;
  // 分享用户
  shareUsers?: Array<string>;
  // 分享用户装换成字符串
  shareUsersValue?: string;
}

/**
 * 分享链接入参
 */
export interface sharePar {
  // 文件名
  name?: string;
  // 分享文件ID
  fileIds?: Array<string>;
  // 有效期标识，0：自定义日期，1：永久有效，2：一年，3：六个月，4：一个月
  validFlag?: string;
  // 有效期日期，如果有效期标识字段为0，标识自定义，需要传 validDate
  validDate?: string;
  // 描述
  desc?: string;
  // 分享url
  shareUrl?: string;
  // 分享的用户ID
  shareUserIds?: Array<number>;
  // 分享的部门ID
  shareDeptIds?: Array<number>;
}

/**
 * 接收者
 */
export interface ShareUsers {
  // 用户id
  userId?: string;
  // 用户姓名
  displayName?: string;
  // 用户域名
  username?: string;
}
