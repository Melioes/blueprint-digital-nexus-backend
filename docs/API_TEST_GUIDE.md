# Blueprint Digital Nexus - API 测试指南

## 前置条件
1. 确保已执行 `sql/init_data.sql` 初始化数据库
2. 确保项目已启动（默认端口 8080）
3. 准备 Apifox 或 Postman 等 API 测试工具

---

## 一、基础信息

| 项目 | 值                            |
|------|------------------------------|
| 基础 URL | `https://localhost:8080`     |
| Token Header | `token` (或你的配置的token header) |
| 认证方式 | JWT Token                    |

---

## 二、测试用户登录

### 接口：登录接口

**请求方式**: `POST`
**接口地址**: `/admin/auth/login`

**请求 Body**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "userId": 1,
      "username": "admin",
      "realName": "超级管理员",
      "roles": ["SUPER_ADMIN"]
    }
  }
}
```

> **注意**: 需要先在数据库中创建一个测试管理员用户，或使用你现有的用户登录。

---

## 三、接口测试（使用 Apifox）

### 3.1 创建环境
1. 在 Apifox 中创建环境：
- 环境名称: `本地开发`
- 变量: `baseUrl` = `http://localhost:8080`

### 3.2 设置全局 Token
登录成功后，将返回的 token 保存到环境变量中，后续请求自动携带

---

## 四、接口列表

### 4.1 用户管理接口

| 接口 | 方法 | 路径 | 需要角色 | 需要权限 |
|------|------|--------|----------|
| 用户分页查询 | GET | `/admin/user/page?page=1&size=10&keyword=` | SUPER_ADMIN, ADMIN | system:user:view |
| 新增用户 | POST | `/admin/user` | SUPER_ADMIN, ADMIN | system:user:add |
| 修改用户 | PUT | `/admin/user` | SUPER_ADMIN, ADMIN | system:user:edit |
| 删除用户 | DELETE | `/admin/user/{id}` | SUPER_ADMIN, ADMIN | system:user:delete |
| 用户详情 | GET | `/admin/user/{id}` | SUPER_ADMIN, ADMIN | system:user:view |

#### 新增用户 - 请求示例
```json
{
  "username": "testuser",
  "password": "123456",
  "realName": "测试用户",
  "status": 1,
  "roleIds": [4]
}
```

---

### 4.2 角色管理接口

| 接口 | 方法 | 路径 | 需要角色 | 需要权限 |
|------|------|-----|--------|----------|
| 角色分页查询 | GET | `/admin/role/page?page=1&size=10` | SUPER_ADMIN, ADMIN | system:role:view |
| 新增角色 | POST | `/admin/role` | SUPER_ADMIN, ADMIN | system:role:add |
| 修改角色 | PUT | `/admin/role` | SUPER_ADMIN, ADMIN | system:role:edit |
| 删除角色 | DELETE | `/admin/role/{id}` | SUPER_ADMIN, ADMIN | system:role:delete |
| 角色详情 | GET | `/admin/role/{id}` | SUPER_ADMIN, ADMIN | system:role:view |
| 角色列表(下拉) | GET | `/admin/role/list?status=1` | SUPER_ADMIN, ADMIN | system:role:view |

#### 新增角色 - 请求示例
```json
{
  "roleName": "测试角色",
  "roleKey": "TEST_ROLE",
  "description": "这是一个测试角色",
  "status": 1
}
```

---

### 4.3 菜单管理接口

| 接口 | 方法 | 路径 | 需要角色 | 需要权限 |
|------|------|-----|--------|----------|
| 菜单树(所有) | GET | `/admin/menu/tree` | SUPER_ADMIN, ADMIN | system:menu:view |
| 用户菜单树 | GET | `/admin/menu/user/tree` | 登录即可 | - |
| 新增菜单 | POST | `/admin/menu` | SUPER_ADMIN, ADMIN | system:menu:add |
| 修改菜单 | PUT | `/admin/menu` | SUPER_ADMIN, ADMIN | system:menu:edit |
| 删除菜单 | DELETE | `/admin/menu/{id}` | SUPER_ADMIN, ADMIN | system:menu:delete |
| 菜单详情 | GET | `/admin/menu/{id}` | SUPER_ADMIN, ADMIN | system:menu:view |
| 绑定角色菜单 | POST | `/admin/menu/bindMenu` | SUPER_ADMIN, ADMIN | system:role:assign |
| 解绑角色菜单 | DELETE | `/admin/menu/unbindMenu` | SUPER_ADMIN, ADMIN | system:role:assign |
| 获取角色菜单 | GET | `/admin/menu/role/menus?roleId=1` | SUPER_ADMIN, ADMIN | system:role:view |

#### 绑定角色菜单 - 请求示例
```json
{
  "roleId": 4,
  "menuIds": [1, 2, 3, 30, 31, 32, 33, 34]
}
```

---

## 五、权限校验测试用例

### 5.1 测试不同角色的菜单差异

1. **测试步骤**：

#### 用 SUPER_ADMIN 登录
- 应该能访问所有接口
- 应该能看到完整菜单树

#### 用 ADMIN 登录
- 能访问用户、角色、菜单管理
- 不能访问运营相关菜单（可能受限，根据SQL绑定）

#### OPERATOR (运营人员) 登录
- 只能访问商品、库存、入库、出库、仓库管理
- 不能访问系统管理

#### 用 USER (普通用户) 登录
- 只能查看首页、商品列表、库存查询等

### 5.2 测试权限不足场景

**测试步骤**：
1. 用 OPERATOR 登录（运营人员账号登录）
2. 尝试调用 `/admin/user/page` (用户管理接口)
3. 应该返回：
```json
{
  "code": 500,
  "message": "无权限访问该接口"
}
```

---

## 六、初始化数据说明

### 6.1 角色列表

| role_id | role_name | role_key | 说明 |
|---------|-----------|----------|------|
| 1 | 超级管理员 | SUPER_ADMIN | 系统最高权限 |
| 2 | 系统管理员 | ADMIN | 系统管理 |
| 3 | 运营人员 | OPERATOR | 业务操作 |
| 4 | 普通用户 | USER | 基础查看权限 |

### 6.2 菜单结构（WMS仓储管理）

```
首页
├── 商品管理
│   ├── 商品列表
│   └── 商品分类
├── 库存管理
│   ├── 库存查询
│   └── 库存流水
├── 入库管理
│   ├── 入库单
│   └── 入库记录
├── 出库管理
│   ├── 出库单
│   └── 出库记录
├── 仓库管理
│   └── 仓库列表
└── 系统管理
    ├── 用户管理
    ├── 角色管理
    ├── 菜单管理
    └── 操作日志
```

---

## 七、常见问题

### 7.1 Token 无效或过期
- 重新登录获取新 Token

### 7.2 权限不足
- 检查用户绑定的角色
- 检查角色绑定的菜单权限是否正确

### 7.3 数据库中没有数据
- 执行 `sql/init_data.sql` 初始化数据
