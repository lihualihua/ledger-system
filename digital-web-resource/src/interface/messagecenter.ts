/**
 * 列表
 */
export interface MessageList {
  // 内容
  content?: string;
  // 事件
  event?: string;
  // 通知编号
  id?: string;
  // 状态 1：未读   0：已读
  status?: number;
  // 标题
  title?: string;
  // 类型
  type?: string;
  // 发送人编号
  userId?: string;
  // 创建人
  createBy?: string;
  // 创建时间
  createTime?: string;
  // 更新人
  updateBy?: string;
  // 更新时间
  updateTime?: string;
}

/**
 *  右侧树
 */
export interface TreeList {
  // 序号
  id?: number;
  // 标题
  label?: string;
  // 标识
  name?: string;
  // 数量
  num?: number;
  // 图标
  icon?: string;
  // 高亮图标
  icon2?: string;
}
