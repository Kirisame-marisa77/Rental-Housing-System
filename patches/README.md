# 上游框架改动补丁

本目录保存「为了让租户模块跑起来」而对**上游芋道框架**所做的必要改动。
这些改动不属于本组业务代码，但缺了它们项目无法启动，因此以补丁形式单独留存。

## 使用方式

先按主 README 完成上游框架克隆与本仓库代码覆盖，然后在**对应框架目录**中执行：

```bash
cd ruoyi-vue-pro
git apply ../patches/backend/01-register-rental-module.patch
git apply ../patches/backend/02-disable-multi-tenant.patch

cd ../yudao-ui-admin-vue3
git apply ../patches/admin-ui/01-branding-and-routing.patch
```

补丁基于以下上游基线生成，若上游版本不同可能产生冲突：

| 仓库 | 基线 commit |
|---|---|
| ruoyi-vue-pro | `01a0eaf573a962eda54c83e46551cdd6bfbf207c` |
| yudao-ui-admin-vue3 | `aab14fb0e74720dd09e964ae066f8bbde9f9012e` |

## 补丁清单

### `backend/01-register-rental-module.patch`

把 `yudao-module-rental` 注册进 Maven 构建：

- 根 `pom.xml`：在 `<modules>` 中加入 `yudao-module-rental`
- `yudao-server/pom.xml`：加入对该模块的依赖

**不加的后果**：模块不参与编译，后端启动后 `/admin-api/rental/**` 全部 404。

### `backend/02-disable-multi-tenant.patch`

`yudao-server/src/main/resources/application.yaml` 中 `yudao.tenant.enable: true → false`。

**为什么关**：本系统是**单社区**直营管理，不存在多租户隔离需求；
开启多租户后所有业务表都要求 `tenant_id`，且登录需先按租户名换取租户编号，与业务模型不符。

### `admin-ui/01-branding-and-routing.patch`

管理端前端的品牌与路由调整：

- `.env`：系统标题改为「社区房屋租赁管理系统」、`VITE_APP_TENANT_ENABLE=false`、默认登录租户名同步修改
- `.env.local`：`VITE_APP_API_ENCRYPT_ENABLE=false`（本地后端未开启接口加解密，前端必须同步关闭，否则登录报错）
- `src/api/login/index.ts`：租户名做 `encodeURIComponent`（中文租户名直接拼进 URL 会出错）
- `src/router/modules/remaining.ts`：首页重定向改为 `/rental/house`，隐藏原首页
- `src/views/Login/**`、`src/locales/zh-CN.ts`：登录页去掉租户选择框、文案调整

## 未纳入补丁的改动

`yudao-server/src/main/resources/application-{dev,local}.yaml` 中数据库连接、Redis 地址等改动
**刻意不做成补丁**——其中含开发者本机的 MySQL 密码。请自行按本地环境填写：

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro?...
          username: root
          password: <你的密码>
```
