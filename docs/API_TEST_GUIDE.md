# BlueprintDigitalNexus - API 测试指南

> 更新日期：2026-06-11
> 测试工具：APIFox / Postman

---

## 前置条件

1. 确保已执行 `sql/init.sql` 初始化数据库
2. 确保 Redis 已启动
3. 确保项目已启动（默认端口 8443，HTTPS）

---

## 一、基础信息

| 项目 | 值 |
|------|------|
| 基础 URL | `https://localhost:8443` |
| 认证方式 | JWT Token（Header: `token`） |
| 响应格式 | `{"code": 1, "msg": "操作成功", "data": ...}` |
| Knife4j 文档 | `https://localhost:8443/doc.html`（admin / admin123） |

---

## 二、认证接口

### 2.1 登录

```
POST /admin/auth/login
```
```json
{
  "username": "test_super",
  "password": "你的密码"
}
```

**响应：**
```json
{
  "code": 1,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "test_super",
    "realName": "测试管理员"
  }
}
```

**登录后：** 在 APIFox 的环境变量里设置 `token`，后续请求 Header 带 `token: {{token}}`。

### 2.2 注册

```
POST /admin/auth/register
```
```json
{
  "username": "newuser",
  "password": "abc123",
  "realName": "新用户"
}
```

**校验规则：**
- 用户名：2-30字符，不能为空
- 密码：6-50字符，必须包含字母和数字，不能为空

---

## 三、各模块接口清单

### 3.1 仓库管理 `/admin/warehouse`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询 |
| GET | `/list` | 列表（无分页） |
| GET | `/{warehouseId}` | 详情 |
| POST | | 新增仓库 |
| PUT | | 修改仓库 |
| DELETE | `/{warehouseId}` | 删除仓库 |

### 3.2 商品管理 `/admin/product`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询 |
| GET | `/list` | 列表 |
| GET | `/{productId}` | 详情 |
| POST | `/add` | 新增商品 |
| PUT | `/update` | 修改商品 |
| DELETE | `/{id}` | 删除商品 |

### 3.3 商品分类 `/admin/category`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/tree` | 分类树 |
| POST | `/add` | 新增分类 |
| PUT | `/update` | 修改分类 |
| DELETE | `/{id}` | 删除分类 |

### 3.4 库存管理 `/admin/inventory`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询 |
| GET | `/list` | 列表 |
| GET | `/{id}` | 详情 |
| GET | `/by-warehouse-product` | 按仓库+商品查询 |
| POST | `/add` | 新增库存 |
| POST | `/adjust` | 调整库存（IN/OUT/覆盖） |

### 3.5 入库管理 `/admin/inbound`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询 |
| GET | `/{id}` | 详情 |
| POST | | 创建入库单 |
| PUT | | 修改入库单 |
| DELETE | `/{id}` | 删除入库单 |
| POST | `/confirm?id=` | 确认入库（增加库存） |
| POST | `/cancel?id=` | 取消入库（减少库存） |

### 3.6 出库管理 `/admin/outbound`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询 |
| GET | `/{id}` | 详情 |
| POST | | 创建出库单 |
| PUT | | 修改出库单 |
| DELETE | `/{id}` | 删除出库单 |
| POST | `/confirm?id=` | 确认出库（扣减库存） |
| POST | `/cancel?id=` | 取消出库（恢复库存） |

### 3.7 用户管理 `/admin/user`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询 |
| GET | `/{id}` | 详情 |
| POST | | 新增用户 |
| PUT | | 修改用户 |
| DELETE | `/{id}` | 删除用户 |

### 3.8 角色管理 `/admin/role`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询 |
| GET | `/{id}` | 详情 |
| GET | `/list` | 角色下拉列表 |
| POST | | 新增角色 |
| PUT | | 修改角色 |
| DELETE | `/{id}` | 删除角色 |

### 3.9 菜单管理 `/admin/menu`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/tree` | 菜单树 |
| GET | `/{menuId}` | 详情 |
| GET | `/user/tree` | 当前用户菜单树 |
| GET | `/role/menus?roleId=` | 角色菜单ID列表 |
| POST | | 新增菜单 |
| PUT | | 修改菜单 |
| DELETE | `/{menuId}` | 删除菜单 |
| POST | `/bindMenu` | 绑定角色菜单 |
| DELETE | `/unbindMenu` | 解绑角色菜单 |

### 3.10 操作日志 `/admin/oper-log`（如有查询接口）

操作日志通过 @OperLog 注解自动记录，查询 `sys_oper_log` 表即可。

### 3.11 库存变动日志 `/admin/stock-log`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询（支持 warehouseId、productId、type 筛选） |

### 3.12 首页看板 `/admin/dashboard`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 5个卡片 + 最近单据（Redis缓存10秒） |
| GET | `/trend?startDate=&endDate=` | 趋势图数据（默认近7天，最大90天） |

---

## 四、测试数据参考

### 4.1 数据库现有数据

**仓库（部分）：**

| warehouse_id | warehouse_name |
|---|---|
| 1 | 尝试修改编码 |
| 2 | 测试仓库2 |
| 3 | 测试仓库3 |
| 5 | 测试仓库5 |

**商品（部分）：**

| product_id | product_name | sku_code |
|---|---|---|
| 1 | 缓存测试-已修改 | SKU-20260512-999 |
| 2 | 测试商品2 | SKU-20260519-002 |
| 3 | 测试商品3 | SKU-20260519-003 |

### 4.2 创建出库单测试数据

```json
{
  "warehouseId": 1,
  "remark": "APIFox测试",
  "details": [
    { "productId": 1, "quantity": 10 },
    { "productId": 2, "quantity": 5 }
  ]
}
```

---

## 五、常见问题

### Q1：请求返回 401

检查 Token 是否过期（24小时有效期），重新登录获取新 Token。

### Q2：请求返回 403

当前用户没有该接口的权限，检查角色-菜单绑定。

### Q3：请求返回 "参数类型错误"

检查参数格式，如 `page` 和 `size` 必须是数字，日期格式必须是 `yyyy-MM-dd`。

### Q4：Redis 连接超时

检查 Redis 是否启动，`timeout` 配置是否合理（当前 2000ms）。
