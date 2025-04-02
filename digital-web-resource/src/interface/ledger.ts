/**
 * table列表
 */
export interface LedgerTable {
  // 编号
  id?: number;
  // 台账id
  tempId?: string;
  // 类型，1：行模板，2：单元格模板
  type?: number;
  // 名称
  name?: string;
  // 状态，1：发布，2：归档，3：撤销
  status?: number;
  // 删除标记
  delFlag?: number;
  // 创建人
  createBy?: string;
  // 创建时间
  createTime?: string;
  // 更新人
  updateBy?: string;
  // 更新时间
  updateTime?: string;
}

export interface TemplateList {
  // 模版ID
  id: string;
  // 模版名称
  name?: string;
  // 模版路径
  path?: string;
  // 模版类型
  systemFlag?: string;
  // 上传时间
  createTime?: string;
}
