-- ================================================
-- Blueprint Digital Nexus - 权限系统初始化SQL (V4)
-- WMS（仓库管理系统）菜单结构 - 严格按照原型图
-- 说明：
--  - 正式业务菜单：严格按照原型图侧边栏实现
--  - 仅保留系统管理-菜单管理的按钮权限（用于验证）
--  - 测试菜单：保留3个测试菜单
-- ================================================

-- ================================================
-- 第一步：清空旧数据
-- ================================================
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM sys_role_menu;
DELETE FROM sys_user_role;
DELETE FROM sys_menu;
DELETE FROM sys_role;
SET FOREIGN_KEY_CHECKS = 1;

-- ================================================
-- 第二步：初始化角色数据
-- ================================================
INSERT INTO sys_role (role_name, role_key, description, status, is_deleted) VALUES
                                                                                ('超级管理员', 'SUPER_ADMIN', '系统最高权限，拥有所有菜单和功能', 1, 0),
                                                                                ('系统管理员', 'ADMIN', '系统管理权限，可以管理用户、角色、菜单', 1, 0),
                                                                                ('运营人员', 'OPERATOR', '运营人员，可以进行库存和订单管理', 1, 0),
                                                                                ('普通用户', 'USER', '普通用户，拥有基础业务操作权限', 1, 0),
                                                                                ('测试角色', 'TEST_ROLE', '测试角色，拥有部分业务查询权限', 1, 0);

-- ================================================
-- 第三步：初始化菜单数据
-- type: 0=目录, 1=菜单, 2=按钮
-- ================================================

-- ================================================
-- 【正式业务菜单】严格按原型图侧边栏实现
-- ================================================

-- --------------------
-- 首页
-- --------------------
INSERT INTO sys_menu (menu_name, parent_id, path, component, type, permission, icon, sort, status, is_deleted) VALUES
    ('首页', 0, '/dashboard', 'dashboard/index', 1, 'dashboard:view', 'home', 1, 1, 0);

-- --------------------
-- 商品管理
-- --------------------
INSERT INTO sys_menu (menu_name, parent_id, path, component, type, permission, icon, sort, status, is_deleted) VALUES
                                                                                                                   ('商品管理', 0, '/product', NULL, 0, NULL, 'goods', 2, 1, 0),
                                                                                                                   ('商品列表', 2, '/product/list', 'product/list', 1, 'product:list:view', 'list', 1, 1, 0),
                                                                                                                   ('商品分类', 2, '/product/category', 'product/category', 1, 'product:category:view', 'category', 2, 1, 0);

-- --------------------
-- 库存管理
-- --------------------
INSERT INTO sys_menu (menu_name, parent_id, path, component, type, permission, icon, sort, status, is_deleted) VALUES
                                                                                                                   ('库存管理', 0, '/inventory', NULL, 0, NULL, 'inventory', 3, 1, 0),
                                                                                                                   ('库存查询', 6, '/inventory/query', 'inventory/query', 1, 'inventory:query:view', 'search', 1, 1, 0),
                                                                                                                   ('库存流水', 6, '/inventory/record', 'inventory/record', 1, 'inventory:record:view', 'record', 2, 1, 0);

-- --------------------
-- 入库管理
-- --------------------
INSERT INTO sys_menu (menu_name, parent_id, path, component, type, permission, icon, sort, status, is_deleted) VALUES
                                                                                                                   ('入库管理', 0, '/inbound', NULL, 0, NULL, 'inbound', 4, 1, 0),
                                                                                                                   ('入库单', 10, '/inbound/order', 'inbound/order', 1, 'inbound:order:view', 'order', 1, 1, 0),
                                                                                                                   ('入库记录', 10, '/inbound/record', 'inbound/record', 1, 'inbound:record:view', 'log', 2, 1, 0);

-- --------------------
-- 出库管理
-- --------------------
INSERT INTO sys_menu (menu_name, parent_id, path, component, type, permission, icon, sort, status, is_deleted) VALUES
                                                                                                                   ('出库管理', 0, '/outbound', NULL, 0, NULL, 'outbound', 5, 1, 0),
                                                                                                                   ('出库单', 14, '/outbound/order', 'outbound/order', 1, 'outbound:order:view', 'order', 1, 1, 0),
                                                                                                                   ('出库记录', 14, '/outbound/record', 'outbound/record', 1, 'outbound:record:view', 'log', 2, 1, 0);

-- --------------------
-- 仓库管理
-- --------------------
INSERT INTO sys_menu (menu_name, parent_id, path, component, type, permission, icon, sort, status, is_deleted) VALUES
                                                                                                                   ('仓库管理', 0, '/warehouse', NULL, 0, NULL, 'warehouse', 6, 1, 0),
                                                                                                                   ('仓库列表', 18, '/warehouse/list', 'warehouse/list', 1, 'warehouse:list:view', 'list', 1, 1, 0);

-- --------------------
-- 系统管理
-- --------------------
INSERT INTO sys_menu (menu_name, parent_id, path, component, type, permission, icon, sort, status, is_deleted) VALUES
                                                                                                                   ('系统管理', 0, '/system', NULL, 0, NULL, 'system', 7, 1, 0),
                                                                                                                   ('用户管理', 21, '/system/user', 'system/user', 1, 'system:user:view', 'user', 1, 1, 0),
                                                                                                                   ('角色管理', 21, '/system/role', 'system/role', 1, 'system:role:view', 'role', 2, 1, 0),
                                                                                                                   ('菜单管理', 21, '/system/menu', 'system/menu', 1, 'system:menu:view', 'menu', 3, 1, 0),
                                                                                                                   ('操作日志', 21, '/system/log', 'system/log', 1, 'system:log:view', 'log', 4, 1, 0);

-- 系统管理 - 菜单管理的按钮权限（仅保留这部分用于验证）
INSERT INTO sys_menu (menu_name, parent_id, path, component, type, permission, icon, sort, status, is_deleted) VALUES
                                                                                                                   ('新增菜单', 24, '', '', 2, 'system:menu:add', '', 1, 1, 0),
                                                                                                                   ('编辑菜单', 24, '', '', 2, 'system:menu:edit', '', 2, 1, 0),
                                                                                                                   ('删除菜单', 24, '', '', 2, 'system:menu:delete', '', 3, 1, 0);

-- ================================================
-- 【测试菜单】保留3个测试菜单
-- ================================================

-- --------------------
-- 测试菜单1：测试商品管理
-- --------------------
INSERT INTO sys_menu (menu_name, parent_id, path, component, type, permission, icon, sort, status, is_deleted) VALUES
                                                                                                                   ('【测试】商品管理', 0, '/test-product', NULL, 0, NULL, 'goods', 100, 1, 0),
                                                                                                                   ('测试-新增商品', 28, '/test-product/add', 'test/productAdd', 1, 'test:product:add', 'add', 1, 1, 0),
                                                                                                                   ('测试-编辑商品', 28, '/test-product/edit', 'test/productEdit', 1, 'test:product:edit', 'edit', 2, 1, 0),
                                                                                                                   ('测试-删除商品', 28, '/test-product/delete', 'test/productDelete', 1, 'test:product:delete', 'delete', 3, 1, 0),
                                                                                                                   ('测试-商品详情', 28, '/test-product/detail', 'test/productDetail', 1, 'test:product:detail', 'detail', 4, 1, 0);

-- --------------------
-- 测试菜单2：测试订单管理
-- --------------------
INSERT INTO sys_menu (menu_name, parent_id, path, component, type, permission, icon, sort, status, is_deleted) VALUES
                                                                                                                   ('【测试】订单管理', 0, '/test-order', NULL, 0, NULL, 'order', 101, 1, 0),
                                                                                                                   ('测试-新增订单', 33, '/test-order/add', 'test/orderAdd', 1, 'test:order:add', 'add', 1, 1, 0),
                                                                                                                   ('测试-编辑订单', 33, '/test-order/edit', 'test/orderEdit', 1, 'test:order:edit', 'edit', 2, 1, 0),
                                                                                                                   ('测试-删除订单', 33, '/test-order/delete', 'test/orderDelete', 1, 'test:order:delete', 'delete', 3, 1, 0);

-- --------------------
-- 测试菜单3：测试通用操作
-- --------------------
INSERT INTO sys_menu (menu_name, parent_id, path, component, type, permission, icon, sort, status, is_deleted) VALUES
                                                                                                                   ('【测试】通用管理', 0, '/test-general', NULL, 0, NULL, 'component', 102, 1, 0),
                                                                                                                   ('测试-查询操作', 37, '/test-general/query', 'test/query', 1, 'test:general:query', 'search', 1, 1, 0),
                                                                                                                   ('测试-导出操作', 37, '/test-general/export', 'test/export', 1, 'test:general:export', 'export', 2, 1, 0),
                                                                                                                   ('测试-审核操作', 37, '/test-general/audit', 'test/audit', 1, 'test:general:audit', 'check', 3, 1, 0);

-- ================================================
-- 第四步：角色-菜单关联绑定
-- ================================================

-- 超级管理员 - 绑定所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu;

-- 系统管理员 - 绑定系统管理+首页+菜单管理按钮
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
                                                 (2, 1),   -- 首页
                                                 (2, 21),  -- 系统管理目录
                                                 (2, 22),  -- 用户管理
                                                 (2, 23),  -- 角色管理
                                                 (2, 24),  -- 菜单管理
                                                 (2, 25),  -- 新增菜单按钮
                                                 (2, 26),  -- 编辑菜单按钮
                                                 (2, 27),  -- 删除菜单按钮
                                                 (2, 20);  -- 操作日志

-- 运营人员 - 绑定所有业务菜单+测试菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 3, menu_id FROM sys_menu;

-- 普通用户 - 绑定业务菜单（无系统管理）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
-- 首页
(4, 1),
-- 商品管理
(4, 2), (4, 3), (4, 4),
-- 库存管理
(4, 6), (4, 7), (4, 8),
-- 入库管理
(4, 10), (4, 11), (4, 12),
-- 出库管理
(4, 14), (4, 15), (4, 16),
-- 仓库管理
(4, 18), (4, 19);

-- 测试角色 - 绑定业务菜单+全部测试菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
-- 首页
(5, 1),
-- 商品管理
(5, 2), (5, 3), (5, 4),
-- 库存管理
(5, 6), (5, 7), (5, 8),
-- 入库管理
(5, 10), (5, 11), (5, 12),
-- 出库管理
(5, 14), (5, 15), (5, 16),
-- 仓库管理
(5, 18), (5, 19),
-- 测试菜单1
(5, 28), (5, 29), (5, 30), (5, 31), (5, 32),
-- 测试菜单2
(5, 33), (5, 34), (5, 35), (5, 36),
-- 测试菜单3
(5, 37), (5, 38), (5, 39), (5, 40);

-- ================================================
-- 第五步：查询验证
-- ================================================
SELECT '===== 角色数据 =====' AS info;
SELECT * FROM sys_role;

SELECT '===== 菜单统计 =====' AS info;
SELECT
    type,
    COUNT(*) AS count,
    CASE
        WHEN type = 0 THEN '目录'
        WHEN type = 1 THEN '菜单'
        WHEN type = 2 THEN '按钮'
        END AS type_name
FROM sys_menu
GROUP BY type;

SELECT '===== 侧边栏菜单结构 =====' AS info;
SELECT
    m1.menu_id AS level1_id,
    m1.menu_name AS level1_name,
    m2.menu_id AS level2_id,
    m2.menu_name AS level2_name
FROM sys_menu m1
         LEFT JOIN sys_menu m2 ON m1.menu_id = m2.parent_id
WHERE m1.parent_id = 0
ORDER BY m1.sort, m2.sort;

SELECT '===== 各角色菜单数量 =====' AS info;
SELECT
    r.role_id,
    r.role_name,
    r.role_key,
    COUNT(DISTINCT rm.menu_id) AS menu_count
FROM sys_role r
         LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id
WHERE r.is_deleted = 0
GROUP BY r.role_id, r.role_name, r.role_key
ORDER BY r.role_id;


-- 查看所有菜单
SELECT menu_id, menu_name, parent_id, type, permission FROM sys_menu ORDER BY menu_id;

-- 查看各角色绑定的菜单
SELECT
    r.role_id, r.role_name,
    m.menu_id, m.menu_name, m.type, m.permission
FROM sys_role r
         JOIN sys_role_menu rm ON r.role_id = rm.role_id
         JOIN sys_menu m ON rm.menu_id = m.menu_id
ORDER BY r.role_id, m.menu_id;


-- 关闭外键约束
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 1. 清空所有错误的角色菜单权限
-- =============================================
TRUNCATE TABLE sys_role_menu;

-- =============================================
-- 2. 重新插入 100% 正确的权限（父+子菜单完整）
-- =============================================

-- 🔹 超级管理员 (1)：全权限
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu;

-- 🔹 系统管理员 (2)：首页 + 系统管理（父+子+按钮）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
                                                 (2,1), (2,16), (2,17), (2,18), (2,19), (2,20), (2,21), (2,22), (2,23);

-- 🔹 运营人员 (3)：全权限（和你原有一致，正确）
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 3, menu_id FROM sys_menu;

-- 🔹 普通用户 (4)：首页 + 业务菜单（父+子，无系统/测试）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
                                                 (4,1),
-- 商品
                                                 (4,2),(4,3),(4,4),
-- 库存
                                                 (4,5),(4,6),(4,7),
-- 入库
                                                 (4,8),(4,9),(4,10),
-- 出库
                                                 (4,11),(4,12),(4,13),
-- 仓库
                                                 (4,14),(4,15);

-- 🔹 测试角色 (5)：首页 + 业务菜单 + 测试菜单（父+子）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
                                                 (5,1),
-- 业务
                                                 (5,2),(5,3),(5,4),
                                                 (5,5),(5,6),(5,7),
                                                 (5,8),(5,9),(5,10),
                                                 (5,11),(5,12),(5,13),
                                                 (5,14),(5,15),
-- 测试商品
                                                 (5,24),(5,25),(5,26),(5,27),(5,28),
-- 测试订单
                                                 (5,29),(5,30),(5,31),(5,32),
-- 测试通用
                                                 (5,33),(5,34),(5,35),(5,36);

-- 开启外键约束
SET FOREIGN_KEY_CHECKS = 1;