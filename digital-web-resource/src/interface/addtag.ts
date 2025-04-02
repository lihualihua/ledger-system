/**
 * 新建标签
 */
export interface AddTag {
  // 标签id
  id?: string;
  // 标签名称
  name?: string;
  // 描述
  desc?: string;
  // 标签状态，1：启用，0：不启用
  status?: number;
}
/**
 * 标签列表
 */
export interface TagList {
  // 创建者
  createBy?: string;
  // 创建时间
  createTime?: string;
  // 描述
  desc?: string;
  // 标签ID
  id?: string;
  // 标签名称
  name?: string;
  // 标签状态，1：启用，0：不启用
  status?: number;
  // 更新人
  updateBy?: string;
  // 更新时间
  updateTime?: string;
  // 用户ID
  userId?: number;

  // 标签是否选中
  checked?: boolean;
  // 临时编号
  numID?: number;
}
