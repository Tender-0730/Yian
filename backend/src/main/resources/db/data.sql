-- =====================================================
-- 颐安养老管理系统 - 初始化数据
-- =====================================================

-- 角色
INSERT INTO sys_role (role_name, role_code, description) VALUES
('超级管理员', 'ADMIN', '系统所有权限'),
('护理人员',   'NURSE',  '护理记录、健康记录操作权限'),
('普通员工',   'STAFF',  '信息查看权限');

-- 管理员账号（用户名: admin，密码: admin123）
INSERT INTO sys_user (username, password, real_name, status) VALUES
('admin', '$2a$12$WIsroWZ/f6NHOg/Or/UPV.tFWlrdh/rsop8eDc1UrJ6b44NLtfzkO', '系统管理员', 1);

-- 管理员角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 护理级别
INSERT INTO care_level (level_name, level_code, description, daily_fee, sort_order) VALUES
('基础护理', 'BASIC',     '日常生活照料，定期巡视，基础健康监测',       80.00,  1),
('中级护理', 'MEDIUM',    '基础护理+专项健康监测+康复指导',           150.00, 2),
('特级护理', 'INTENSIVE', '24小时专人照护，生命体征监测，医疗护理',    300.00, 3);

-- 楼栋
INSERT INTO building (building_name, floor_count, description) VALUES
('颐和楼', 3, '主楼，1-3层');

-- 房间
INSERT INTO room (building_id, floor, room_number, room_type, capacity, occupied, status, description) VALUES
(1, 1, '101', '单人间', 1, 0, 'ACTIVE', '一楼单人间采光好'),
(1, 1, '102', '双人间', 2, 0, 'ACTIVE', '一楼双人间'),
(1, 2, '201', '单人间', 1, 0, 'ACTIVE', '二楼单人间');

-- 床位
INSERT INTO bed (room_id, bed_number, status) VALUES
(1, '101-A', 'AVAILABLE'),
(2, '102-A', 'AVAILABLE'),
(2, '102-B', 'AVAILABLE'),
(3, '201-A', 'AVAILABLE');
