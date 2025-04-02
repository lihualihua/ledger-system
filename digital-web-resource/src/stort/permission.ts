import { DirectiveBinding } from "vue";
import { useUserStore } from "./user";

/***
 * 创建权限指令
 * 控制按钮权限
 */
const vPermission: any = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    applyPermission(el, binding);
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    applyPermission(el, binding);
  },
};

const applyPermission = (el: HTMLElement, binding: DirectiveBinding) => {
  const { value } = binding;
  if (Array.isArray(value)) {
    if (value.length > 0 && !value.some((role) => hasPermission(role))) {
      el.style.display = "none";
    } else {
      el.style.display = "";
    }
  } else {
    if (!hasPermission(value)) {
      el.style.display = "none";
    } else {
      el.style.display = "";
    }
  }
};

const hasPermission = (permission: string): boolean => {
  const userStore = useUserStore();
  const permissions = userStore.user.permissions;
  let array: any = [];
  permissions.forEach((res: any) => {
    array.push(res.roleCode);
  });
  return array.includes(permission);
};

export default vPermission;
