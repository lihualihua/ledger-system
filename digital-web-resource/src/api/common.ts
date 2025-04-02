import { $get } from "@/utils/url";

// 查询部门列表
export const $querySectionList = async () => {
  const res = await $get("/api/dept/list/1/100");
  return res;
};

// 根据部门ID分页查找用户列表
export const $queryUserList = async (name: string, id?: number) => {
  let url = "";
  if (id) {
    url = `/api/user/findUser/1/100?username=${name}&deptId=${id}`;
  } else {
    url = `/api/user/findUser/1/100?username=${name}`;
  }
  const res = await $get(url);
  return res;
};
