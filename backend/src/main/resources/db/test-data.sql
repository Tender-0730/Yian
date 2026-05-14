-- ============================================
-- 颐安养老管理系统 — 测试数据
-- 密码: admin123 (BCrypt 加密)
-- 使用方法: 在 MySQL 中执行 source db/test-data.sql
-- ============================================

USE yian;

-- 1. 角色
INSERT INTO sys_role (role_name, role_code, description, created_at, updated_at) VALUES
('管理员', 'ADMIN', '系统管理员，拥有所有权限', NOW(), NOW()),
('护工', 'STAFF', '日常照护人员', NOW(), NOW()),
('护士', 'NURSE', '医护专业人员，可查看健康记录', NOW(), NOW());

-- 2. 用户（密码统一为 admin123，BCrypt）
INSERT INTO sys_user (username, password, real_name, phone, email, gender, status, created_at, updated_at) VALUES
('admin', '$2b$10$XQpdgA4MRE3bdE77Z.rYqunQztVJMo2IdpZPAnv0Vbrlm0DRB7DFq', '系统管理员', '13800000001', 'admin@yian.com', 1, 1, NOW(), NOW()),
('zhangli', '$2b$10$XQpdgA4MRE3bdE77Z.rYqunQztVJMo2IdpZPAnv0Vbrlm0DRB7DFq', '张丽', '13800000002', 'zhangli@yian.com', 2, 1, NOW(), NOW()),
('wangqiang', '$2b$10$XQpdgA4MRE3bdE77Z.rYqunQztVJMo2IdpZPAnv0Vbrlm0DRB7DFq', '王强', '13800000003', 'wangqiang@yian.com', 1, 1, NOW(), NOW()),
('linxia', '$2b$10$XQpdgA4MRE3bdE77Z.rYqunQztVJMo2IdpZPAnv0Vbrlm0DRB7DFq', '林霞', '13800000004', 'linxia@yian.com', 2, 1, NOW(), NOW());

-- 3. 用户-角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 2),
(4, 3);

-- 4. 楼栋
INSERT INTO building (building_name, floor_count, description, created_at, updated_at) VALUES
('A栋（康养楼）', 3, '主楼，一楼为活动大厅，二、三楼住老人', NOW(), NOW()),
('B栋（颐养楼）', 4, '独立楼栋，设置康复训练区和花园', NOW(), NOW());

-- 5. 房间（A栋15间 + B栋10间）
INSERT INTO room (building_id, floor, room_number, room_type, capacity, occupied, status, description, created_at, updated_at) VALUES
-- A栋 1楼（活动区，2间）
(1, 1, '101', 'ACTIVITY', 20, 0, 'AVAILABLE', '多功能活动大厅', NOW(), NOW()),
(1, 1, '102', 'ACTIVITY', 15, 0, 'AVAILABLE', '棋牌室', NOW(), NOW()),
-- A栋 2楼（老人住房）
(1, 2, '201', 'SINGLE', 1, 0, 'AVAILABLE', '单人房', NOW(), NOW()),
(1, 2, '202', 'DOUBLE', 2, 0, 'AVAILABLE', '双人房', NOW(), NOW()),
(1, 2, '203', 'DOUBLE', 2, 0, 'AVAILABLE', '双人房', NOW(), NOW()),
(1, 2, '204', 'DOUBLE', 2, 0, 'AVAILABLE', '双人房', NOW(), NOW()),
(1, 2, '205', 'SINGLE', 1, 0, 'AVAILABLE', '单人房', NOW(), NOW()),
-- A栋 3楼（老人住房）
(1, 3, '301', 'DOUBLE', 2, 0, 'AVAILABLE', '双人房', NOW(), NOW()),
(1, 3, '302', 'DOUBLE', 2, 0, 'AVAILABLE', '双人房', NOW(), NOW()),
(1, 3, '303', 'SINGLE', 1, 0, 'AVAILABLE', '单人房', NOW(), NOW()),
(1, 3, '304', 'SINGLE', 1, 0, 'AVAILABLE', '单人房', NOW(), NOW()),
-- B栋 1楼
(2, 1, '101', 'DOUBLE', 2, 0, 'AVAILABLE', '双人房', NOW(), NOW()),
(2, 1, '102', 'DOUBLE', 2, 0, 'AVAILABLE', '双人房', NOW(), NOW()),
-- B栋 2楼
(2, 2, '201', 'SINGLE', 1, 0, 'AVAILABLE', '单人房', NOW(), NOW()),
(2, 2, '202', 'SINGLE', 1, 0, 'AVAILABLE', '单人房', NOW(), NOW()),
(2, 2, '203', 'DOUBLE', 2, 0, 'AVAILABLE', '双人房', NOW(), NOW()),
-- B栋 3楼
(2, 3, '301', 'SINGLE', 1, 0, 'AVAILABLE', 'VIP单人房', NOW(), NOW()),
(2, 3, '302', 'SINGLE', 1, 0, 'AVAILABLE', 'VIP单人房', NOW(), NOW());

-- 6. 床位（每个住房房间配2个床位，SINGLE 房间虽然可放1张但系统预留2个槽位）
INSERT INTO bed (room_id, bed_number, status, created_at, updated_at)
SELECT id, CONCAT(room_number, '-1'), 'AVAILABLE', NOW(), NOW()
FROM room WHERE room_type IN ('SINGLE', 'DOUBLE', 'VIP');

INSERT INTO bed (room_id, bed_number, status, created_at, updated_at)
SELECT id, CONCAT(room_number, '-2'), 'AVAILABLE', NOW(), NOW()
FROM room WHERE room_type IN ('DOUBLE');

-- 7. 护理级别
INSERT INTO care_level (level_name, level_code, description, daily_fee, sort_order, created_at, updated_at) VALUES
('特级护理', 'LEVEL_SPECIAL', '全天24小时照护，适用于完全不能自理的老人', 300.00, 1, NOW(), NOW()),
('一级护理', 'LEVEL_1', '重度依赖，需要协助完成绝大多数日常活动', 200.00, 2, NOW(), NOW()),
('二级护理', 'LEVEL_2', '中度依赖，部分日常活动需要协助', 120.00, 3, NOW(), NOW()),
('三级护理', 'LEVEL_3', '轻度依赖，基本可自理，定期巡查即可', 60.00, 4, NOW(), NOW());

-- 8. 老人（10位，覆盖不同护理级别和状态）
INSERT INTO resident (name, gender, age, birthday, id_card, phone, emergency_name, emergency_phone, emergency_relation, admission_date, status, medical_history, allergies, remark, created_at, updated_at) VALUES
('张大爷', 1, 82, '1944-03-15', '310101194403152345', '13810000001', '张建国', '13910000001', '儿子', '2025-06-01', 'IN_RESIDENCE', '高血压、冠心病', '青霉素', '作息规律，每天散步', NOW(), NOW()),
('李奶奶', 2, 78, '1948-07-22', '310101194807223456', '13810000002', '李明华', '13910000002', '女儿', '2025-08-15', 'IN_RESIDENCE', '糖尿病', '海鲜', '需严格控制饮食', NOW(), NOW()),
('王国强', 1, 68, '1958-11-08', '310101195811083456', '13810000003', '王芳', '13910000003', '妹妹', '2025-10-10', 'IN_RESIDENCE', '无', '花粉', '半自理状态', NOW(), NOW()),
('赵阿姨', 2, 75, '1951-05-18', '310101195105184567', '13810000004', '赵明', '13910000004', '儿子', '2025-12-01', 'IN_RESIDENCE', '关节炎、腰椎间盘突出', '无', '行动不便，需轮椅辅助', NOW(), NOW()),
('孙爷爷', 1, 89, '1937-01-30', '310101193701305678', '13810000005', '孙刚', '13910000005', '侄子', '2025-04-20', 'IN_RESIDENCE', '高血压、糖尿病、心脏病', '青霉素、头孢', '需每天监测血压血糖', NOW(), NOW()),
('钱奶奶', 2, 84, '1942-09-12', '310101194209126789', '13810000006', '钱伟', '13910000006', '儿子', '2025-07-10', 'IN_RESIDENCE', '高血压', '无', '可自理，喜欢唱歌', NOW(), NOW()),
('陈大爷', 1, 72, '1954-12-25', '310101195412257890', '13810000007', '陈丽', '13910000007', '女儿', '2025-09-05', 'IN_RESIDENCE', '前列腺增生', '无', '晚上起夜较多', NOW(), NOW()),
('刘奶奶', 2, 91, '1935-06-08', '310101193506088901', '13810000008', '刘勇', '13910000008', '儿子', '2025-03-01', 'IN_RESIDENCE', '高血压、骨质疏松、白内障', '无', '需全天照护，意识偶有不清', NOW(), NOW()),
('周大爷', 1, 77, '1949-08-20', '310101194908209012', '13810000009', '周强', '13910000009', '儿子', '2025-05-15', 'CHECKED_OUT', '高血压', '无', '2026年1月退住回家', NOW(), NOW()),
('黄阿姨', 2, 65, '1961-02-14', '310101196102149123', '13810000010', '黄磊', '13910000010', '弟弟', '2026-01-05', 'IN_RESIDENCE', '无', '无', '新入住，身体较好，可自理', NOW(), NOW());

-- 9. 护理级别分配
INSERT INTO resident_care_level (resident_id, care_level_id, effective_date, status, created_at, updated_at) VALUES
(1, 2, '2025-06-01', 'ACTIVE', NOW(), NOW()),  -- 张大爷 → 一级
(2, 3, '2025-08-15', 'ACTIVE', NOW(), NOW()),  -- 李奶奶 → 二级
(3, 3, '2025-10-10', 'ACTIVE', NOW(), NOW()),  -- 王国强 → 二级
(4, 1, '2025-12-01', 'ACTIVE', NOW(), NOW()),  -- 赵阿姨 → 特级
(5, 1, '2025-04-20', 'ACTIVE', NOW(), NOW()),  -- 孙爷爷 → 特级
(6, 4, '2025-07-10', 'ACTIVE', NOW(), NOW()),  -- 钱奶奶 → 三级
(7, 3, '2025-09-05', 'ACTIVE', NOW(), NOW()),  -- 陈大爷 → 二级
(8, 1, '2025-03-01', 'ACTIVE', NOW(), NOW()),  -- 刘奶奶 → 特级
(9, 4, '2025-05-15', 'INACTIVE', NOW(), NOW()), -- 周大爷 → 三级(已退住)
(10, 4, '2026-01-05', 'ACTIVE', NOW(), NOW());  -- 黄阿姨 → 三级

-- 10. 入住记录（为在院老人创建，周大爷已退住）
INSERT INTO check_in_record (resident_id, bed_id, check_in_date, status, remark, created_at, updated_at)
SELECT r.id, b.id, r.admission_date, 'CHECKED_IN', NULL, NOW(), NOW()
FROM resident r
JOIN bed b ON b.id = (r.id % (SELECT COUNT(*) FROM bed)) + 1
WHERE r.status = 'IN_RESIDENCE';

-- 退住记录（周大爷）
INSERT INTO check_in_record (resident_id, bed_id, check_in_date, check_out_date, status, remark, created_at, updated_at)
SELECT r.id, b.id, r.admission_date, r.discharge_date, 'CHECKED_OUT', '康复退住', NOW(), NOW()
FROM resident r
CROSS JOIN bed b
WHERE r.status = 'CHECKED_OUT'
LIMIT 1;

-- 更新床位占用状态
UPDATE bed b
JOIN check_in_record ci ON ci.bed_id = b.id AND ci.status = 'CHECKED_IN'
SET b.status = 'OCCUPIED', b.updated_at = NOW();

-- 更新房间已占用数
UPDATE room r
SET occupied = (SELECT COUNT(*) FROM bed b WHERE b.room_id = r.id AND b.status = 'OCCUPIED'),
    updated_at = NOW();

-- 11. 健康记录（近3天，每位老人每天至少1条）
INSERT INTO health_record (resident_id, temperature, blood_systolic, blood_diastolic, heart_rate, blood_sugar, weight, oxygen, status, abnormal, notes, recorded_by, recorded_at, created_at)
SELECT
  r.id,
  36.5 + (RAND() * 0.8 - 0.4),           -- 体温 36.1-37.3
  120 + FLOOR(RAND() * 40),               -- 收缩压 120-160
  70 + FLOOR(RAND() * 20),                -- 舒张压 70-90
  65 + FLOOR(RAND() * 25),                -- 心率 65-90
  5.0 + (RAND() * 2.0),                   -- 血糖 5.0-7.0
  55 + (RAND() * 20),                     -- 体重 55-75
  95 + FLOOR(RAND() * 5),                 -- 血氧 95-100
  CASE WHEN RAND() > 0.2 THEN 'NORMAL' ELSE 'ABNORMAL' END,
  CASE WHEN RAND() > 0.2 THEN NULL ELSE '血压偏高，注意观察' END,
  NULL,
  2,
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 3) DAY),
  NOW()
FROM resident r
CROSS JOIN (SELECT 1 UNION SELECT 2 UNION SELECT 3) t  -- 每个老人3条
WHERE r.status = 'IN_RESIDENCE'
  AND r.id <= 8;  -- 只给在院老人（前8位）

-- 12. 膳食记录（昨天和今天）
INSERT INTO meal_record (resident_id, meal_date, meal_type, content, notes, created_at, updated_at)
SELECT r.id, CURDATE(), 'BREAKFAST',
       '小米粥、鸡蛋、全麦馒头、时蔬',
       CASE WHEN r.allergies LIKE '%海鲜%' THEN '忌海鲜' ELSE NULL END,
       NOW(), NOW()
FROM resident r WHERE r.status = 'IN_RESIDENCE';

INSERT INTO meal_record (resident_id, meal_date, meal_type, content, notes, created_at, updated_at)
SELECT r.id, CURDATE(), 'LUNCH',
       '清蒸鲈鱼、西红柿炒蛋、清炒西兰花、米饭',
       CASE WHEN r.medical_history LIKE '%糖尿病%' THEN '少油少盐，控制碳水' ELSE NULL END,
       NOW(), NOW()
FROM resident r WHERE r.status = 'IN_RESIDENCE';

INSERT INTO meal_record (resident_id, meal_date, meal_type, content, notes, created_at, updated_at)
SELECT r.id, CURDATE(), 'DINNER',
       '肉末豆腐、蒜蓉菠菜、紫菜蛋花汤、杂粮饭',
       NULL,
       NOW(), NOW()
FROM resident r WHERE r.status = 'IN_RESIDENCE';

INSERT INTO meal_record (resident_id, meal_date, meal_type, content, notes, created_at, updated_at)
SELECT r.id, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'BREAKFAST',
       '燕麦片粥、煮鸡蛋、蒸红薯、拌黄瓜',
       NULL, NOW(), NOW()
FROM resident r WHERE r.status = 'IN_RESIDENCE';

INSERT INTO meal_record (resident_id, meal_date, meal_type, content, notes, created_at, updated_at)
SELECT r.id, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'LUNCH',
       '红烧鸡肉、清炒豆角、土豆丝、米饭',
       CASE WHEN r.medical_history LIKE '%高血压%' THEN '少盐' ELSE NULL END,
       NOW(), NOW()
FROM resident r WHERE r.status = 'IN_RESIDENCE';

INSERT INTO meal_record (resident_id, meal_date, meal_type, content, notes, created_at, updated_at)
SELECT r.id, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'DINNER',
       '冬瓜排骨汤、炒青菜、米饭',
       NULL, NOW(), NOW()
FROM resident r WHERE r.status = 'IN_RESIDENCE';

-- 13. 饮食禁忌
INSERT INTO dietary_restriction (resident_id, restriction, description, created_at, updated_at) VALUES
(1, '忌烟酒', '高血压患者，避免饮酒和吸烟', NOW(), NOW()),
(2, '禁糖', '糖尿病患者，严格控制糖分摄入', NOW(), NOW()),
(2, '忌海鲜', '海鲜过敏，所有菜品不含海鲜成分', NOW(), NOW()),
(3, '忌辣', '偏好清淡口味，避免辛辣食物', NOW(), NOW()),
(5, '禁糖', '糖尿病，严格控制糖分摄入', NOW(), NOW()),
(5, '低盐', '高血压，每日盐摄入量不超过3g', NOW(), NOW()),
(8, '软食', '牙口不好，食物需煮软烂', NOW(), NOW());
