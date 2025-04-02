/**
 * 流程域列表
 */
export interface FlowTable {
  // 业务目录ID
  id?: string;
  // 业务名称
  name?: string;
  // 存储目录路径
  folderId?: string;
  // 状态
  status?: number;
  // 描述
  desc?: string;
  // 顺序
  order?: number;
  // 模版路径
  location?: string;
  // 创建者
  createBy?: string;
  // 创建时间
  createTime?: string;
  // 更新人
  updateBy?: string;
  // 更新时间
  updateTime?: string;
}

/**
 * 业务导航新增 入参
 */
export interface BusinessPar {
  // 编码，首次保存时由后端生成，修改时传递该参数
  code?: string;
  // 流程域ID
  domainId?: string;
  // 标题
  title?: string;
  // 版本号
  version?: string;
  // 状态 1：启用，0：不启用
  status?: number;
  // 描述
  desc?: string;
  // 排序
  order?: number;
  // 图标
  icon?: string;
}

/**
 * 业务导航 列表出参
 */
export interface BusinessTable {
  // 创建者
  createBy?: string;
  // 创建时间
  createTime?: string;
  // 更新人
  updateBy?: string;
  // 更新时间
  updateTime?: string;
  // id
  id?: string;
  // 标题
  title?: string;
  // 编码
  code?: string;
  // 状态 1：启用，0：不启用
  status?: number;
  // 排序
  order?: number;
  // 图标
  icon?: string;
  // 版本号
  version?: string;
  // 流程域ID
  domainId?: string;

  historyList?: Array<string>;
  // 内容
  content?: string;
  // 原始数据内容
  originalContent?: string;
  // 描述
  desc?: string;
}

/**
 * 目录树
 */
export interface HelpDocTree {
  // id
  id?: string;
  // 内容
  text?: string;
  // 标题
  name?: string;
  // 子节点
  children?: HelpDocTree[] | null;
}
