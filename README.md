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
│   └── sql/mysql/rental*.sql       # 租赁业务建表 / 菜单 / 种子数据脚本（18 个）
├── yudao-ui-admin-vue3/
│   └── src/
│       ├── views/rental/           # 管理端页面（15 个功能目录）
│       └── api/rental/             # 管理端接口封装
├── h5/                             # 移动端 H5（业主端 + 租客端，Vite + Vue3 + Element Plus）
├── patches/
│   ├── backend/                    # 对后端框架的必要改动
│   └── admin-ui/                   # 对管理端框架的必要改动
├── scripts/
│   └── lan-access.ps1              # 局域网地址检测与连通性自测
├── 需求与用例规约.md
├── start.bat                       # 一键启动（Redis → 后端 → 管理端）
├── pc端.bat                        # 启动 H5
└── 局域网访问.bat                  # 查看手机/其他电脑的访问地址
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
git apply ../patches/admin-ui/02-lan-api-proxy.patch           # 接口改相对路径 + 本地代理（局域网访问）
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
> 本仓库全部 18 个 `rental*.sql` 的头部均已加 `SET NAMES utf8mb4;`，**请勿删除该行**。

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
| 业主端 H5 | `13800000001` 沈志远 / `13800000002` 何丽云 / `13800000003` 周振华 / `13800000004` 徐婉如 / `13603914844` 吴宇杰 | `123456` |
| 租客端 H5 | `13900000001` 陈嘉禾 / `13900000002` 林晓彤 / `13900000003` 高鹏 / `13900000004` 苏婉清 / `13900000005` 郑昊 / `13900001111` 白露 | `123456` |

`rental-seed-data.sql` 是一条可重放的演示数据脚本，自带 `TRUNCATE`，
可重复执行以重置演示数据（会清空租赁业务表，但**保留** `rental_contract_template` 合同模板表）。

演示数据覆盖全部业务状态，便于逐个演示：房源（上架/已出租/待审核/已驳回/已下架）、
申请（待审批/已通过/已驳回/已签约/已失效）、合同（待签署/生效中/即将到期/已退租）、
账单（待缴/已缴/已逾期含滞纳金）、抄表（已出账/待审核）、维修（四种状态）、退租（待处理/已处理）、
公告（已发布含置顶/草稿），另有阅读记录、收藏、看房预约。

### 7. 局域网访问（手机演示）

```bash
局域网访问.bat   # 自动检测本机 IP，打印访问地址并自测连通性
```

三个端默认都监听 `0.0.0.0`，同一局域网内的手机/其他电脑可直接访问：

| 端 | 地址 |
|---|---|
| 管理端（PC） | `http://<本机IP>/` |
| H5（手机） | `http://<本机IP>:8081/` |

**前提**：两台设备在同一局域网，且路由器未开启 AP 隔离（学校/公司网络常开）。
防火墙需放行 `java.exe` 与 `node.exe` 的入站连接。
`scripts/lan-access.ps1` 会自测三个服务，但**本机自测走回环、绕过防火墙**，
真正的判据只有另一台设备能否打开。

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
| 退租结算 | 租客申请 → **业主处理**（房屋验收 + 费用结算 → 结算单、合同置已退租、房源下架）；业主可驳回（合同回退） |
| 房源详情 | 租客端查看房源详情：图片、户型、地址、房东信息（不暴露身份证/银行卡） |
| 维修工单 | 租客报修 → 房东/管理端处理 |
| 公告通知 | 公告发布与阅读状态记录 |
| 租客服务 | 房源收藏、预约看房（房东确认 → 完成） |
| 账号注销 | 租客/业主自助注销：校验无进行中合同与申请 → 释放手机号与身份证号 → 逻辑删除 |
| 业主端接口 | `/rental/owner-app/**`，独立 token 鉴权 |
| 租客端接口 | `/rental/tenant-app/**`，独立 token 鉴权 |

**申请流程（关键设计）**：审批权归**房东**而非管理员。链路为
「租客申请 → 房东同意（生成待签署合同 + 首期账单，**不改房源状态**）→ 双方签约 + 缴清首期账单 → 合同生效并占用房源」。

同一房源可对多个租客同意，采用**先到先得**：第一个满足条件的合同通过
`HouseMapper.updateStatusByIdAndStatus(1→3)` 的 CAS 操作抢占房源，其余未生效合同自动置「已取消」、对应申请置「已失效」。

**付款方式（押N付M）**：N（押几个月）与 M（一次付几个月）各自取 1~3，共 9 种组合，
存在房源上、由房东出租房时设定。押金 = N × 月租金，首期应缴 = 押金 + M × 月租金，首期账期也是 M 个月。
租金、押金、付款方式三项均为房源属性，**租客申请时只读、后端强制用房源的值覆盖**。
规则封装在 `util/PaymentMethodUtils.java` 与 `h5/src/utils/payment.js`（两处需同步修改）。

### 前端

**管理端**（`yudao-ui-admin-vue3`，端口 80）：房源、租客、业主、合同、合同模板、申请、账单、水电账单、
支付记录、抄表、退租结算、维修工单、公告、看房预约、数据看板 —— 共 15 个功能页面。

**H5**（`h5/`，端口 8081）：业主端与租客端共用一套代码与 `api.js` / `auth.js`，
登录页按角色分 tab（业主 / 租客），token 按角色分键存储。

- 业主端：首页、我的房源（含上下架）、租房申请、看房预约、水电抄表、维修工单、合同、**退租处理**、我的账单、我的
- 租客端：首页、找房、**房源详情**、我的收藏、我的预约、我的申请、我的合同、我的账单、退租申请、公告、报修、我的

**移动端形态**：底部 tab（4 个高频入口）+「更多」九宫格；所有列表为卡片而非表格；
`menus.js` 是菜单唯一数据源，路由由它生成（`import.meta.glob`），**加页面只改一处**。

**响应式策略**：Element Plus 的窄屏覆盖（弹窗宽度、表单标签上下排、触控目标）统一收在
`src/styles/element-mobile.css` 的 `@media (max-width: 640px)` 里，桌面宽度下行为不变；
卡片布局则是全宽度一致的，宽屏下呈现为居中的手机宽度列（`--app-content-max: 640px`）。

## 六、已知限制

- **物业费**（`propertyFeeAmount`）目前没有写入路径，且没有周期性账单生成任务，
  因此租金账单页面实际只会出现 `billType=0` 的首期账单、物业费列恒为 0.00。
- 业主端 / 租客端的登录 token 由后端**内存**存储，**后端重启即失效**，需重新登录。
- 部分表中 `tenant_user_id` / `user_id` 列注释写作「关联 member_user」，属历史误标；
  本项目未启用 member 模块，这些列实际存储的是 `rental_tenant_info.id`。
- **列表无分页**：前端一律 `pageNo:1, pageSize:100` 一次拉全量。演示数据量下无影响，
  数据量增大后需要补分页。
- **账号注销后历史列表中的姓名/电话会变空**：逻辑删除使 `selectBatchIds` 查不到该账号。
  合同号、金额、账单凭据链仍完整。
- 演示图片（`h5/public/demo/*.svg`）为占位图，非真实房源照片。

## 七、开源协议

本组二次开发部分（`yudao-module-rental`、`h5/`、管理端 rental 页面等）为独立完成，知识产权归本组所有。
上游框架 ruoyi-vue-pro、yudao-ui-admin-vue3 均遵循 MIT 协议，允许免费使用、修改与二次开发。
