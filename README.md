# 社区房屋租赁管理系统

厦门理工学院 计算机与信息工程学院 · 2023 级软件工程 2班 ·「事已至此先吃饭」组

基于 **[芋道 yudao（ruoyi-vue-pro）](https://gitee.com/zhijiantianya/ruoyi-vue-pro)** 框架二次开发的社区房屋租赁管理系统，
面向社区/物业直营租赁场景，覆盖 **房源上架 → 在线选房 → 申请审批 → 合同签署 → 账单缴纳 → 报修服务** 的全流程数字化管理。

---

## 一、仓库说明（重要，先读这段）

本仓库**只收录本组的二次开发产出，不含上游框架源码**。

上游芋道框架属第三方开源代码，且其目录自带独立 `.git`。若把框架源码混入本仓库，
git 会把整个目录识别为 submodule 指针（仅存一个 commit hash，内容不入库），
导致仓库实际是空的、且嵌套 `.git` 无法被正常索引。

因此本仓库采用 **覆盖式（overlay）** 结构：目录层级与上游框架**完全一致**，
把本仓库的目录直接覆盖到上游框架克隆中即可得到完整可运行的项目。
为接入 rental 模块而对上游框架所做的少量必要改动，以补丁形式保存在 `patches/`。

## 二、目录结构

```
Rental-Housing-System/
├── ruoyi-vue-pro/
│   ├── yudao-module-rental/        # 后端租赁业务模块（Maven 模块）
│   └── sql/mysql/rental*.sql       # 租赁业务建表 / 菜单 / 种子数据脚本（16 个）
├── yudao-ui-admin-vue3/
│   └── src/
│       ├── views/rental/           # 管理端页面（15 个功能目录）
│       └── api/rental/             # 管理端接口封装
├── h5/                             # 移动端 H5（业主端 + 租客端，Vite + Vue3 + Element Plus）
├── patches/
│   ├── backend/                    # 对后端框架的必要改动
│   └── admin-ui/                   # 对管理端框架的必要改动
├── 需求与用例规约.md
├── start.bat                       # 一键启动（Redis → 后端 → 管理端）
└── pc端.bat                        # 启动 H5
```

## 三、环境要求

| 软件 | 版本 |
|---|---|
| JDK | 17 |
| Maven | 3.9+ |
| MySQL | 8.0（端口 3306） |
| Redis | 6.x+（端口 6379） |
| Node.js / pnpm | v22 / v10 |

> **Redis 是后端的硬依赖**：Redis 未启动时，后端所有接口都会抛「系统异常」
> （Redisson `Connection refused 127.0.0.1:6379`）。排查接口异常时先确认 6379 在监听。

## 四、部署步骤

### 1. 获取上游框架

```bash
git clone https://gitee.com/zhijiantianya/ruoyi-vue-pro.git
git clone https://gitee.com/yudaocode/yudao-ui-admin-vue3.git
```

本组开发所用的上游基线 commit：

| 仓库 | 基线 commit |
|---|---|
| ruoyi-vue-pro | `01a0eaf573a962eda54c83e46551cdd6bfbf207c` |
| yudao-ui-admin-vue3 | `aab14fb0e74720dd09e964ae066f8bbde9f9012e` |

### 2. 覆盖本组代码

把本仓库的 `ruoyi-vue-pro/`、`yudao-ui-admin-vue3/` 两个目录，分别覆盖到上一步克隆出的框架目录中。
（`h5/` 为独立前端，放在任意位置即可，无需覆盖。）

### 3. 应用框架改动补丁

```bash
cd ruoyi-vue-pro
git apply ../patches/backend/01-register-rental-module.patch   # 注册 rental 模块到父 pom 与 yudao-server
git apply ../patches/backend/02-disable-multi-tenant.patch     # 关闭多租户（本系统为单社区）

cd ../yudao-ui-admin-vue3
git apply ../patches/admin-ui/01-branding-and-routing.patch    # 系统标题/租户开关/首页跳转/登录页
```

> `patches/` 中**不含数据库连接配置的改动**——那部分含本机 MySQL 密码，属本地环境配置，
> 请自行修改 `yudao-server/src/main/resources/application-{dev,local}.yaml` 的 `username` / `password`。

### 4. 初始化数据库

```bash
# 4.1 框架基础表（上游脚本）
mysql -uroot -p < ruoyi-vue-pro/sql/mysql/ruoyi-vue-pro.sql

# 4.2 租赁业务脚本，按文件名顺序全部导入
for f in ruoyi-vue-pro/sql/mysql/rental*.sql; do
  mysql -uroot -p ruoyi-vue-pro < "$f"
done
```

> **关于中文乱码**：中文 Windows 的 mysql 客户端默认 `character_set_client=gbk`，
> 直接导 UTF-8 的脚本会把中文菜单名、列注释写成一堆乱码。
> 本仓库全部 16 个 `rental*.sql` 的头部均已加 `SET NAMES utf8mb4;`，**请勿删除该行**。

### 5. 启动

```bash
redis-server --port 6379     # 1. 先启 Redis
start.bat                    # 2. 一键启动：后端(48080) + 管理端(80)
pc端.bat                     # 3. 启动 H5(8081)
```

> 修改后端代码后，需重新 `mvn package` 才会被 `start.bat` 加载
> —— `start.bat` 运行的是预编译的 `yudao-server/target/yudao-server.jar`。
> 开发期建议直接在 IDE 中运行 `YudaoServerApplication` 以便热调试。

### 6. 演示账号

| 端 | 账号 | 密码 |
|---|---|---|
| 管理端 | `admin` | `admin123` |
| 业主端 H5 | `13800000001` ~ `13800000004`、`13800000011` ~ `13800000014`、`13603914844`、`13603914848` | `123456` |
| 租客端 H5 | `13900000001` ~ `13900000005`、`13900000011` ~ `13900000015`、`13900001111`、`13900001112` | `123456` |

`rental-seed-data.sql` 是一条可重放的演示数据脚本，自带 `TRUNCATE`，
可重复执行以重置演示数据（会清空租赁业务表，但**保留** `rental_contract_template` 合同模板表）。

## 五、已实现功能

### 后端 `yudao-module-rental`

| 模块 | 说明 |
|---|---|
| 房源管理 | 房源 CRUD、房源编号自动生成、上架/下架/占用状态流转 |
| 租客管理 | 实名信息扩展、身份证唯一校验、手机号+密码登录 |
| 业主管理 | 业主信息、银行卡号、手机号+密码登录 |
| 租房申请 | 租客提交申请 → 房东审批（同意仅生成待签署合同，不占用房源） |
| 合同管理 | 合同编号自动生成、合同模板套用、双方签署、生效时 CAS 抢占房源 |
| 账单管理 | 首期账单生成、租金账单、水电账单、押金、支付记录 |
| 抄表管理 | 水电抄表记录、峰谷计价、计价模式 |
| 退租结算 | 退租申请、结算账单 |
| 维修工单 | 租客报修 → 房东/管理端处理 |
| 公告通知 | 公告发布与阅读状态记录 |
| 租客服务 | 房源收藏、预约看房（房东确认 → 完成） |
| 业主端接口 | `/rental/owner-app/**`，独立 token 鉴权 |
| 租客端接口 | `/rental/tenant-app/**`，独立 token 鉴权 |

**申请流程（关键设计）**：审批权归**房东**而非管理员。链路为
「租客申请 → 房东同意（生成待签署合同 + 首期账单，**不改房源状态**）→ 双方签约 + 缴清首期账单 → 合同生效并占用房源」。

同一房源可对多个租客同意，采用**先到先得**：第一个满足条件的合同通过
`HouseMapper.updateStatusByIdAndStatus(1→3)` 的 CAS 操作抢占房源，其余未生效合同自动置「已取消」、对应申请置「已失效」。

### 前端

**管理端**（`yudao-ui-admin-vue3`，端口 80）：房源、租客、业主、合同、合同模板、申请、账单、水电账单、
支付记录、抄表、退租结算、维修工单、公告、看房预约、数据看板 —— 共 15 个功能页面。

**H5**（`h5/`，端口 8081）：业主端与租客端共用一套代码与 `api.js` / `auth.js`，
登录页按角色分 tab（业主 / 租客），token 按角色分键存储。

- 业主端：首页、我的房源、看房预约、租房申请、合同、我的账单、抄表、维修、个人中心
- 租客端：首页、找房、我的收藏、我的预约、公告、租房申请、合同、我的账单、报修、退租、个人中心

## 六、已知限制

- **物业费**（`propertyFeeAmount`）目前没有写入路径，且没有周期性账单生成任务，
  因此租金账单页面实际只会出现 `billType=0` 的首期账单、物业费列恒为 0.00。
- 业主端 / 租客端的登录 token 由后端**内存**存储，**后端重启即失效**，需重新登录。
- 部分表中 `tenant_user_id` / `user_id` 列注释写作「关联 member_user」，属历史误标；
  本项目未启用 member 模块，这些列实际存储的是 `rental_tenant_info.id`。

## 七、开源协议

本组二次开发部分（`yudao-module-rental`、`h5/`、管理端 rental 页面等）为独立完成，知识产权归本组所有。
上游框架 ruoyi-vue-pro、yudao-ui-admin-vue3 均遵循 MIT 协议，允许免费使用、修改与二次开发。
