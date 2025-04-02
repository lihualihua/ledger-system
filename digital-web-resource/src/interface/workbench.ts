/**
 * 查找公共文件、我的文件、部门文件总数 出参
 */
export interface FileTotality {
  // 公共文档总数
  commonFileCount?: string | number;
  // 个人文档总数
  userFileCount?: string | number;
  // 部门文档总数
  deptFileCount?: string | number;
}

/**
 * 创建常用功能 入参
 */
export interface ShortcutPar {
  // 图标
  icon?: string;
  // 名称
  name?: string;
  // 前端路由地址
  path?: string;
  // 状态，0：不启用，1：启用
  status?: null | number;
  // 排序
  order?: null | number;
}

/**
 * 查询快捷键列表
 */
export interface ShortcutList {
  // 创建者
  createBy?: string;
  // 创建时间
  createTime?: string;
  // 图标
  icon?: string;
  // id
  id?: string;
  // 名称
  name?: string;
  // 排序
  order?: null | number;
  // 前端路由地址
  path?: string;
  // 状态，0：不启用，1：启用
  status?: null | number;
  // 更新者
  updateBy?: string;
  // 更新时间
  updateTime?: string;
  // 选中、未选中
  checked?: boolean;
}
