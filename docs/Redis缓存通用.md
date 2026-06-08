# WMS 系统 Redis + SpringCache 分批缓存改造需求文档

## 文档说明

本文档为 **WMS 仓储管理系统缓存改造统一规范与执行要求**，基于：

- Spring Boot
- SpringCache
- Redis
- MyBatis-Plus

### 核心目标

- 合理落地 Redis 缓存
- 统一 Key 规范，避免缓存冲突
- 零侵入原有业务代码
- 分批改造降低风险
- 遵循企业级开发标准

### 执行要求

**禁止全量一次性改造。**

必须按照模块逐步推进：

> 模块分析 → 方案评审 → 代码改造 → 测试验证 → 下一模块

每轮改造前必须先提交方案，审核通过后再开始编写代码。

---

# 一、全局最高优先级约束（所有模块必须遵守）

## 1.1 代码零侵入

仅允许新增缓存注解：

- `@Cacheable`
- `@CacheEvict`

严禁：

- 修改原有业务逻辑
- 删除原有代码
- 重构原有代码
- 调整业务流程

必须保持：

- 参数校验逻辑不变
- 事务逻辑不变
- SQL 不变
- 日志不变
- 异常处理不变
- 方法入参与出参不变

---

## 1.2 Redis Key 强制隔离

项目现有编码生成规则：

```text
WMS:CODE:xxx
```

例如：

```text
WMS:CODE:WAREHOUSE
WMS:CODE:PRODUCT
WMS:CODE:CATEGORY
```

该规则仅用于：

- 业务单号生成
- 自增计数器

本次缓存改造必须使用独立命名空间：

```text
WMS:WAREHOUSE
WMS:PRODUCT
WMS:CATEGORY
WMS:ROLE
```

两套 Key 完全隔离，禁止混用。

---

## 1.3 架构兼容

缓存改造必须兼容：

- SpringBoot
- SpringCache
- Redis
- MyBatis-Plus
- @Transactional
- 全局异常处理

不得引发：

- 事务失效
- 缓存异常
- 查询异常
- 数据一致性问题

---

## 1.4 风险规避

以下数据严禁缓存：

- 实时数据
- 高频变动数据
- 敏感数据
- 强一致性数据

避免：

- 数据脏读
- 缓存雪崩
- 缓存穿透
- 缓存与数据库不一致

---

## 1.5 分批改造（核心要求）

禁止：

```text
一次性改造全部模块
```

必须：

```text
单模块分析
↓
方案审批
↓
单模块改造
↓
测试通过
↓
进入下一模块
```

---

# 二、模块缓存适用性判定规则

要求 AI 自动扫描所有：

```java
xxxServiceImpl
```

根据：

- 查询频率
- 修改频率
- 实时性要求

自动判定是否启用缓存。

---

## 第一类：✅ 推荐启用缓存（静态基础数据）

### 特征

- 修改频率极低
- 查询频率高
- 无强实时要求

### 适用模块

#### 基础仓储信息

- 仓库
- 库区
- 库位

#### 商品信息

- 商品分类
- 商品基础信息

#### 系统组织信息

- 角色
- 部门
- 组织架构

#### 公共配置

- 数据字典
- 系统枚举
- 下拉选项

### 缓存策略

查询：

```java
@Cacheable
```

新增/修改/删除：

```java
@CacheEvict(allEntries = true)
```

分页：

```text
禁止缓存
```

---

## 第二类：⚠️ 谨慎启用缓存（半动态数据）

### 特征

- 偶尔修改
- 查询较频繁
- 可接受短时间缓存

### 适用模块

- 系统配置
- 通用参数配置
- 非核心业务配置

### 缓存策略

查询：

```java
@Cacheable
```

写操作：

```java
@CacheEvict(allEntries = true)
```

分页：

```text
禁止缓存
```

复杂条件查询：

```text
禁止缓存
```

---

## 第三类：❌ 绝对禁止缓存

### 特征

- 实时性要求极高
- 高频变动
- 强一致性要求

### 禁止缓存模块

#### 用户认证

- 登录
- Token
- Session
- 用户信息

#### 库存相关

- 库存数量
- 库存流水
- 库存变动记录

#### 业务单据

- 入库单
- 出库单
- 盘点单
- 调拨单

#### 实时报表

- 实时统计
- 实时报表

#### 日志

- 操作日志
- 审计日志

#### 订单流转

- 订单
- 支付
- 审批流

### 执行要求

发现以上模块：

```text
直接跳过
```

不得添加任何缓存注解。

---

# 三、Redis 缓存统一规范

## 3.1 命名空间（value/cacheNames）

统一格式：

```text
WMS:模块英文大写名称
```

### 示例

| 业务模块 | 缓存 Value | 单号生成 Key |
|----------|------------|--------------|
| 仓库 | WMS:WAREHOUSE | WMS:CODE:WAREHOUSE |
| 商品 | WMS:PRODUCT | WMS:CODE:PRODUCT |
| 分类 | WMS:CATEGORY | WMS:CODE:CATEGORY |
| 角色 | WMS:ROLE | WMS:CODE:ROLE |

---

## 3.2 Key 编写规范

### 根据 ID 查询

```java
key = "#warehouseId"
```

```java
key = "#productId"
```

```java
key = "#id"
```

---

### 查询固定列表

```java
key = "'list'"
```

示例：

```java
@Cacheable(
    value = "WMS:WAREHOUSE",
    key = "'list'"
)
```

---

### 分页查询

```text
禁止缓存
```

---

## 3.3 缓存过期时间

统一在 Redis 配置中管理。

禁止：

```java
@Cacheable(
    unless = "...",
    cacheManager = "...",
    ttl = ...
)
```

禁止在注解内写死时间。

### 推荐时间

| 模块类型 | TTL |
|-----------|------|
| 仓库/角色/字典/部门 | 30天 |
| 商品/商品分类 | 7天 |
| 通用配置 | 1天 |
| 禁止缓存模块 | 不配置 |

---

# 四、代码编写强制规范

## 4.1 查询接口

符合条件：

```java
@Cacheable(
    value = "WMS:模块名",
    key = "规则Key"
)
```

---

## 4.2 新增接口

```java
@CacheEvict(
    value = "WMS:模块名",
    allEntries = true
)
```

---

## 4.3 修改接口

```java
@CacheEvict(
    value = "WMS:模块名",
    allEntries = true
)
```

---

## 4.4 删除接口

```java
@CacheEvict(
    value = "WMS:模块名",
    allEntries = true
)
```

---

## 4.5 分页接口

统一规则：

```text
禁止缓存
```

---

## 4.6 空值处理

沿用 SpringCache 默认机制：

```text
查询为空也允许缓存
```

无需额外处理。

---

## 4.7 注解顺序

如果原有代码：

```java
@Transactional
@Override
public void update(...)
```

则新增缓存注解时：

```java
@Transactional

// 新增缓存注解
@CacheEvict(...)

@Override
public void update(...)
```

不得调整原有注解顺序。

---

# 五、分批改造执行流程

## 步骤一：模块梳理与排序

推荐顺序：

```text
仓库模块
↓
商品分类模块
↓
商品模块
↓
角色模块
↓
其他可缓存模块
```

---

## 步骤二：方案提报（必须先做）

在任何代码改造前，必须提交：

# 《模块改造分析 & 实施方案》

包含内容：

### 模块名称

例如：

```text
Warehouse
```

### 功能简介

说明业务职责。

### 判定结论

是否适合缓存。

### 判定依据

说明原因。

### 待改造方法清单

标记：

- 查询
- 新增
- 修改
- 删除
- 分页

### 缓存方案

```text
value
key
TTL
```

### 风险说明

包括：

- 事务风险
- 数据一致性风险
- 空指针风险

### 整体实施思路

改造策略说明。

---

## 步骤三：代码编写

审核通过后执行。

要求：

- 仅新增缓存注解
- 原业务代码完全不动

新增位置统一标记：

```java
// 新增缓存注解
```

---

## 步骤四：模块交付与验证

输出：

### 完整 ServiceImpl 代码

可直接运行。

### 测试要点

验证：

- 原功能正常
- Redis 生效
- 缓存刷新正常

---

## 步骤五：进入下一模块

循环执行：

```text
方案提报
↓
审核
↓
改造
↓
验证
↓
下一模块
```

直到完成全部模块。

---

# 六、交付物要求

## 6.1 单模块交付物

每个模块必须提供：

### 1. 改造分析文档

《模块改造分析 & 实施方案》

### 2. 完整代码

改造后的 ServiceImpl。

### 3. 自检报告

检查项：

- 原代码是否未改动
- Key 是否符合规范
- Value 是否符合规范
- 写操作是否清理缓存
- 分页是否未缓存

---

## 6.2 最终全量交付物

### 模块汇总清单

分类：

#### 已缓存模块

列出全部模块。

#### 谨慎缓存模块

列出全部模块。

#### 禁止缓存模块

列出全部模块。

---

### 全量代码

所有改造后的代码文件。

---

### Apifox 测试用例

#### 场景一：基础功能测试

验证：

- 查询
- 新增
- 修改
- 删除

全部正常。

---

#### 场景二：缓存命中测试

步骤：

```text
第一次查询
↓
访问数据库

第二次查询
↓
命中 Redis
```

验证缓存生效。

---

#### 场景三：缓存一致性测试

步骤：

```text
查询
↓
缓存生成

修改数据
↓
缓存清理

再次查询
↓
返回最新数据
```

验证缓存刷新正常。

---

# 七、推荐运行模型

## 首选

### Doubao-Seed-2.0-Code

适合：

- Java
- SpringBoot
- Redis
- SpringCache
- MyBatis-Plus

特点：

- 代码理解能力强
- 规则执行严格
- 改动风险低

---

## 备选

### DeepSeek-V4-Pro

适合规则驱动任务。

### Qwen3.6-Plus

适合分步执行场景。

---

# 八、参考示例

## 示例一：详情查询

```java
// 新增缓存注解
@Cacheable(
    value = "WMS:WAREHOUSE",
    key = "#warehouseId"
)
@Override
public WarehouseVO getWarehouseById(Long warehouseId) {

    // 原有业务代码保持不变

}
```

---

## 示例二：列表查询

```java
// 新增缓存注解
@Cacheable(
    value = "WMS:WAREHOUSE",
    key = "'list'"
)
@Override
public List<WarehouseVO> getWarehouseList() {

    // 原有业务代码保持不变

}
```

---

## 示例三：修改数据

```java
// 新增缓存注解
@CacheEvict(
    value = "WMS:WAREHOUSE",
    allEntries = true
)
@Override
public void updateWarehouse(WarehouseDTO dto) {

    // 原有业务代码保持不变

}
```

---

# 结论

本规范采用：

- 零侵入改造
- 分模块实施
- Redis Key 隔离
- SpringCache 标准注解
- 企业级缓存治理方案

确保：

✅ 缓存收益最大化  
✅ 数据一致性可控  
✅ 改造风险最小化  
✅ 支持快速回滚与逐步验收