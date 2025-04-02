/**
 * 转移到树结构
 */
export interface Tree {
  id?: string;
  label?: string;
  loading?: boolean;
  children?: Tree[];
}
