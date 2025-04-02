import { createRouter, createWebHashHistory } from "vue-router";
import NProgress from "nprogress";
import "nprogress/nprogress.css";

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: "/",
      meta: { title: "登录" },
      component: () => import("../views/login/Login.vue"),
    },
    // 重定向/login 或 / 都可以跳转到登录页面
    {
      path: "/login",
      redirect: "/",
    },
    {
      path: "/index",
      meta: { title: "文件管理系统" },
      component: () => import("../views/Index.vue"),
      children: [
        {
          path: "/messagecenter/:name",
          meta: { title: "消息中心" },
          component: () => import("../views/messagecenter/Messagecenter.vue"),
        },
        {
          path: "/personalcenter/:name",
          meta: { title: "个人中心" },
          component: () => import("../views/personalcenter/PersonalCenter.vue"),
        },
        {
          path: "",
          meta: { title: "工作台" },
          component: () => import("../views/workbench/Workbench.vue"),
        },
        {
          path: "/document",
          meta: { title: "文档库" },
          component: () => import("../views/filemanage/Document.vue"),
          children: [
            {
              path: "/document/commondoc",
              meta: { title: "公共文档" },
              component: () =>
                import("@/views/filemanage/components/DocumentTable.vue"),
            },
            {
              path: "/document/minefile",
              meta: { title: "我的文档" },
              component: () =>
                import("@/views/filemanage/components/DocumentTable.vue"),
            },
            {
              path: "/document/section",
              meta: { title: "部门文档" },
              component: () =>
                import("@/views/filemanage/components/DocumentTable.vue"),
            },
            // {
            //   path: "/document/tag/:id",
            //   name: "Tag",
            //   meta: { title: "个人标签" },
            //   component: () =>
            //     import("@/views/filemanage/components/DocumentTag.vue"),
            //   props: (route) => ({ id: route.params.id as string }),
            // },
          ],
        },
        {
          path: "/document/collect",
          meta: { title: "收藏夹" },
          component: () =>
            import("@/views/filemanage/components/DocumentCollect.vue"),
        },
        {
          path: "/document/share",
          meta: { title: "分享记录" },
          component: () =>
            import("@/views/filemanage/components/DocumentShare.vue"),
        },
        {
          path: "/document/recycle",
          meta: { title: "回收站" },
          component: () =>
            import("@/views/filemanage/components/DocumentRecycle.vue"),
        },
        {
          path: "/search",
          meta: { title: "智能搜索" },
          component: () => import("../views/intelligentsearch/Search.vue"),
        },
        {
          path: "/user",
          meta: { title: "用户管理" },
          component: () => import("../views/rightsmanage/UserManage.vue"),
        },
        {
          path: "/role",
          meta: { title: "角色管理" },
          component: () => import("../views/rightsmanage/RoleManage.vue"),
        },
        {
          path: "/menu",
          meta: { title: "菜单管理" },
          component: () => import("../views/rightsmanage/MenuManage.vue"),
        },
        {
          path: "/permission",
          meta: { title: "权限管理" },
          component: () => import("../views/rightsmanage/PermissionManage.vue"),
        },
        {
          path: "/communication",
          meta: { title: "通信费计算" },
          component: () => import("../views/business/Communication.vue"),
        },
        {
          path: "/servicenavigation",
          meta: { title: "业务导航" },
          component: () =>
            import("../views/servicenavigation/ServiceNavigation.vue"),
        },
        {
          path: "/ledger",
          meta: { title: "台账" },
          component: () => import("../views/ledger/Ledger.vue"),
        },
      ],
    },
    {
      path: "/sharelist/:id",
      meta: { title: "文件分享" },
      component: () => import("../views/sharelist/ShareLink.vue"),
    },
    {
      path: "/userhelpdocument",
      meta: { title: "业务导航信息" },
      component: () =>
        import("../views/workbench/components/UserServiceNavigation.vue"),
    },
    {
      path: "/helpdocument",
      meta: { title: "业务导航信息" },
      component: () => import("../views/helpdocument/HelpDocument.vue"),
    },
    {
      path: "/editdocument",
      meta: { title: "操作业务导航" },
      component: () => import("../views/helpdocument/EditDocument.vue"),
    },
    {
      path: "/ledgertabledetails/:name",
      meta: { title: "台账列表详情" },
      component: () =>
        import("../views/ledger/components/LedgerTableDetails.vue"),
    },
  ],
});

router.beforeEach((_to, _from, next) => {
  NProgress.start();
  next();
});

router.afterEach((to) => {
  if (typeof to.meta.title === "string") {
    document.title = "台账数据库 - " + to.meta.title;
  }
  NProgress.done();
});

export default router;
