/***
 *
 * @userid 用户id
 * @username 用户昵称
 * @displayName 用户真实姓名
 * @permissions 权限
 * @isAuthenticated 登录状态认证
 * @token
 */
export interface UserInfo {
  // 用户id
  userid?: number;
  // 用户昵称
  username: string;
  // 用户真实姓名
  displayName: string;
  // 权限
  permissions: string[];
  // 登录状态认证
  isAuthenticated: boolean;
  // 登录状态
  token: string;
}

/**
 * 分页
 * 入参
 */
export interface PagingPar {
  // 当前页
  currentPage?: number;
  // 数据条数
  pageSize?: number;
  // 总条数
  total?: number;
}

/**
 * 部门列表
 */
export interface SectionList {
  // 创建者
  createBy?: string;
  // 创建时间
  createTime?: string;
  // 部门id
  deptId: number;
  // 部门名称
  name?: string;
  // 排序
  sortOrder: number;
  // 更新者
  updateBy?: string;
  // 更新时间
  updateTime?: string;
}

/**
 * 用户列表
 */
export interface UserList {
  // 头像地址
  avatar?: string;
  // 创建时间
  createTime?: string;
  // 部门id
  deptId?: number;
  //  部门
  deptName?: string;
  //  用户姓名
  displayName?: string;
  // 邮箱
  email?: string;
  // 手机号码
  phone?: string;
  // 用户角色信息
  roleList?: Array<string>;
  // 更新时间
  updateTime?: string;
  // 用户ID
  userId?: string;
  // 用户名称简写
  username?: string;

  checked?: boolean;
}

/**
 *
 */
export interface NavigationTree {
  id?: number;
  icon?: any;
  title?: string;
  path?: string;
  children?: NavigationTree[];
}
