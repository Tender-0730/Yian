-- =====================================================
-- 颐安养老管理系统 - 建表语句
-- DB: MySQL 8.0+, InnoDB, utf8mb4
-- =====================================================

-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT        AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username        VARCHAR(50)   NOT NULL COMMENT '登录账号',
    password        VARCHAR(255)  NOT NULL COMMENT '密码（BCrypt加密）',
    real_name       VARCHAR(50)   DEFAULT NULL COMMENT '真实姓名',
    phone           VARCHAR(20)   DEFAULT NULL COMMENT '手机号',
    email           VARCHAR(100)  DEFAULT NULL COMMENT '邮箱',
    avatar          VARCHAR(500)  DEFAULT NULL COMMENT '头像URL',
    gender          TINYINT(1)    DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    status          TINYINT(1)    DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    last_login_time DATETIME      DEFAULT NULL COMMENT '最后登录时间',
    created_at      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT(1)    DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统用户';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_name   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(50)  NOT NULL COMMENT '角色编码：ADMIN/NURSE/STAFF',
    description VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统角色';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联';

-- 老人信息表
CREATE TABLE IF NOT EXISTS resident (
    id                 BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name               VARCHAR(50)  NOT NULL COMMENT '姓名',
    gender             TINYINT(1)   DEFAULT NULL COMMENT '性别：1-男 2-女',
    age                INT          DEFAULT NULL COMMENT '年龄',
    birthday           DATE         DEFAULT NULL COMMENT '出生日期',
    id_card            VARCHAR(18)  DEFAULT NULL COMMENT '身份证号',
    phone              VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    avatar             VARCHAR(500) DEFAULT NULL COMMENT '照片URL',
    emergency_name     VARCHAR(50)  DEFAULT NULL COMMENT '紧急联系人姓名',
    emergency_phone    VARCHAR(20)  DEFAULT NULL COMMENT '紧急联系人电话',
    emergency_relation VARCHAR(20)  DEFAULT NULL COMMENT '紧急联系人关系',
    admission_date     DATE         DEFAULT NULL COMMENT '入住日期',
    discharge_date     DATE         DEFAULT NULL COMMENT '退住日期',
    status             VARCHAR(20)  DEFAULT 'CHECKED_IN' COMMENT '状态：CHECKED_IN-已入住 CHECKED_OUT-已退住',
    medical_history    TEXT         DEFAULT NULL COMMENT '既往病史',
    allergies          VARCHAR(500) DEFAULT NULL COMMENT '过敏史',
    remark             VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at         DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted         TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_status (status),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='老人信息';

-- 楼栋表
CREATE TABLE IF NOT EXISTS building (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    building_name VARCHAR(50)  NOT NULL COMMENT '楼栋名称',
    floor_count   INT          DEFAULT NULL COMMENT '楼层数',
    description   VARCHAR(200) DEFAULT NULL COMMENT '描述',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='楼栋';

-- 房间表
CREATE TABLE IF NOT EXISTS room (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    building_id BIGINT       NOT NULL COMMENT '所属楼栋ID',
    floor       INT          NOT NULL COMMENT '所在楼层',
    room_number VARCHAR(10)  NOT NULL COMMENT '房间号',
    room_type   VARCHAR(20)  DEFAULT NULL COMMENT '房型：SINGLE-单人间 DOUBLE-双人间 TRIPLE-三人间 MULTI-多人间',
    capacity    INT          DEFAULT 1 COMMENT '总床位数',
    occupied    INT          DEFAULT 0 COMMENT '已占床位数',
    status      VARCHAR(20)  DEFAULT 'IN_USE' COMMENT '状态：IN_USE-使用中 FULL-已满 MAINTENANCE-维修中',
    description VARCHAR(200) DEFAULT NULL COMMENT '描述',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_room (building_id, room_number),
    INDEX idx_floor (building_id, floor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='房间';

-- 床位表
CREATE TABLE IF NOT EXISTS bed (
    id          BIGINT      AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    room_id     BIGINT      NOT NULL COMMENT '所属房间ID',
    bed_number  VARCHAR(10) NOT NULL COMMENT '床位号',
    status      VARCHAR(20) DEFAULT 'AVAILABLE' COMMENT '状态：AVAILABLE-空闲 OCCUPIED-已占用 MAINTENANCE-维修中',
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_bed (room_id, bed_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='床位';

-- 入住记录表
CREATE TABLE IF NOT EXISTS check_in_record (
    id             BIGINT      AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    resident_id    BIGINT      NOT NULL COMMENT '老人ID',
    bed_id         BIGINT      NOT NULL COMMENT '床位ID',
    check_in_date  DATE        NOT NULL COMMENT '入住日期',
    check_out_date DATE        DEFAULT NULL COMMENT '退住日期',
    status         VARCHAR(20) DEFAULT 'CHECKED_IN' COMMENT '状态：CHECKED_IN-入住中 CHECKED_OUT-已退住',
    remark         VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at     DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_resident (resident_id),
    INDEX idx_bed (bed_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='入住记录';

-- 护理级别定义表
CREATE TABLE IF NOT EXISTS care_level (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    level_name  VARCHAR(50)   NOT NULL COMMENT '级别名称',
    level_code  VARCHAR(20)   NOT NULL COMMENT '级别编码：BASIC-基础护理 MEDIUM-中级护理 INTENSIVE-特级护理',
    description VARCHAR(500)  DEFAULT NULL COMMENT '护理内容描述',
    daily_fee   DECIMAL(10,2) DEFAULT NULL COMMENT '日护理费（元）',
    sort_order  INT           DEFAULT 0 COMMENT '排序权重',
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_level_code (level_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='护理级别';

-- 老人护理级别关联表
CREATE TABLE IF NOT EXISTS resident_care_level (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    resident_id     BIGINT       NOT NULL COMMENT '老人ID',
    care_level_id   BIGINT       NOT NULL COMMENT '护理级别ID',
    effective_date  DATE         NOT NULL COMMENT '生效日期',
    expire_date     DATE         DEFAULT NULL COMMENT '失效日期',
    status          VARCHAR(20)  DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-生效中 EXPIRED-已失效',
    updated_by      BIGINT       DEFAULT NULL COMMENT '操作人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_resident (resident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='老人护理级别关联';

-- 健康记录表
CREATE TABLE IF NOT EXISTS health_record (
    id               BIGINT        AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    resident_id      BIGINT        NOT NULL COMMENT '老人ID',
    temperature      DECIMAL(4,1)  DEFAULT NULL COMMENT '体温（℃）',
    blood_systolic   INT           DEFAULT NULL COMMENT '收缩压（mmHg）',
    blood_diastolic  INT           DEFAULT NULL COMMENT '舒张压（mmHg）',
    heart_rate       INT           DEFAULT NULL COMMENT '心率（次/分钟）',
    blood_sugar      DECIMAL(5,1)  DEFAULT NULL COMMENT '血糖（mmol/L）',
    weight           DECIMAL(5,1)  DEFAULT NULL COMMENT '体重（kg）',
    oxygen           INT           DEFAULT NULL COMMENT '血氧饱和度（%）',
    status           VARCHAR(20)   DEFAULT 'COMPLETED' COMMENT '状态：COMPLETED-已完成 PENDING-未完成 ABNORMAL-异常',
    abnormal         VARCHAR(500)  DEFAULT NULL COMMENT '异常情况描述',
    notes            VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    recorded_by      BIGINT        DEFAULT NULL COMMENT '护理人员ID',
    recorded_at      DATETIME      DEFAULT NULL COMMENT '测量时间',
    created_at       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_resident_time (resident_id, recorded_at),
    INDEX idx_recorded_by (recorded_by),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='健康记录';

-- 膳食记录表
CREATE TABLE IF NOT EXISTS meal_record (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    resident_id BIGINT       NOT NULL COMMENT '老人ID',
    meal_date   DATE         NOT NULL COMMENT '日期',
    meal_type   VARCHAR(20)  NOT NULL COMMENT '餐别：BREAKFAST-早餐 LUNCH-午餐 DINNER-晚餐 DESSERT-点心 FRUIT-水果',
    content     VARCHAR(200) NOT NULL COMMENT '膳食内容',
    notes       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_meal (resident_id, meal_date, meal_type),
    INDEX idx_date (meal_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='膳食记录';

-- 饮食禁忌表
CREATE TABLE IF NOT EXISTS dietary_restriction (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    resident_id  BIGINT       NOT NULL COMMENT '老人ID',
    restriction  VARCHAR(100) NOT NULL COMMENT '禁忌内容',
    description  VARCHAR(200) DEFAULT NULL COMMENT '描述',
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_resident (resident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='饮食禁忌';

-- =====================================================
-- 费用管理模块
-- =====================================================

-- 费用配置表
CREATE TABLE IF NOT EXISTS fee_config (
    id             BIGINT        AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    fee_name       VARCHAR(50)   NOT NULL COMMENT '费用名称（床位费/护理费/伙食费/杂费）',
    default_amount DECIMAL(10,2) NOT NULL COMMENT '默认金额',
    charge_unit    VARCHAR(20)   NOT NULL COMMENT '计费单位：DAILY-按天 MONTHLY-按月 ONE_TIME-一次性',
    description    VARCHAR(200)  DEFAULT NULL COMMENT '费用说明',
    sort_order     INT           DEFAULT 0 COMMENT '排序权重',
    status         VARCHAR(20)   DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用 DISABLED-禁用',
    created_at     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_fee_name (fee_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='费用配置';

-- 账单表
CREATE TABLE IF NOT EXISTS bill (
    id            BIGINT        AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    resident_id   BIGINT        NOT NULL COMMENT '老人ID',
    bill_no       VARCHAR(32)   NOT NULL COMMENT '账单编号',
    bill_period   VARCHAR(7)    NOT NULL COMMENT '账单周期（YYYY-MM）',
    total_amount  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '账单总金额',
    paid_amount   DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已付金额',
    status        VARCHAR(20)   DEFAULT 'UNPAID' COMMENT '状态：UNPAID-未支付 PARTIAL-部分支付 PAID-已付清 CANCELLED-已作废',
    remark        VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    generated_at  DATETIME      DEFAULT NULL COMMENT '账单生成时间',
    created_at    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_bill (resident_id, bill_period),
    INDEX idx_status (status),
    INDEX idx_resident (resident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='账单';

-- 账单明细表
CREATE TABLE IF NOT EXISTS bill_item (
    id            BIGINT        AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    bill_id       BIGINT        NOT NULL COMMENT '账单ID',
    fee_config_id BIGINT        DEFAULT NULL COMMENT '费用配置ID（可为NULL，配置被删后仍保留快照）',
    fee_name      VARCHAR(50)   NOT NULL COMMENT '费用名称快照',
    amount        DECIMAL(10,2) NOT NULL COMMENT '金额快照',
    description   VARCHAR(200)  DEFAULT NULL COMMENT '费用描述',
    INDEX idx_bill (bill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='账单明细';

-- 缴费记录表
CREATE TABLE IF NOT EXISTS payment_record (
    id             BIGINT        AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    bill_id        BIGINT        NOT NULL COMMENT '账单ID',
    resident_id    BIGINT        NOT NULL COMMENT '老人ID（冗余，方便按老人查缴费记录）',
    amount         DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    payment_method VARCHAR(20)   DEFAULT 'CASH' COMMENT '支付方式：CASH/WECHAT/ALIPAY/BANK_TRANSFER',
    paid_at        DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '支付时间',
    received_by    BIGINT        DEFAULT NULL COMMENT '收款人ID',
    remark         VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    created_at     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_bill (bill_id),
    INDEX idx_resident (resident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='缴费记录';

-- =====================================================
-- 用药管理模块
-- =====================================================

-- 药品字典表
CREATE TABLE IF NOT EXISTS drug_dict (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    drug_name     VARCHAR(100) NOT NULL COMMENT '药品名称',
    specification VARCHAR(100) DEFAULT NULL COMMENT '规格（如：10mg×30片）',
    manufacturer  VARCHAR(100) DEFAULT NULL COMMENT '生产厂家',
    unit          VARCHAR(20)  DEFAULT NULL COMMENT '单位（片/支/瓶）',
    category      VARCHAR(50)  DEFAULT NULL COMMENT '药品分类',
    description   VARCHAR(500) DEFAULT NULL COMMENT '说明',
    status        VARCHAR(20)  DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用 DISABLED-禁用',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='药品字典';

-- 药品库存表
CREATE TABLE IF NOT EXISTS drug_inventory (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    drug_id         BIGINT       NOT NULL COMMENT '药品ID',
    quantity        INT          NOT NULL DEFAULT 0 COMMENT '当前库存数量',
    alert_threshold INT          DEFAULT NULL COMMENT '库存预警阈值',
    location        VARCHAR(100) DEFAULT NULL COMMENT '存放位置',
    batch_no        VARCHAR(50)  DEFAULT NULL COMMENT '批号',
    expire_date     DATE         DEFAULT NULL COMMENT '有效期',
    status          VARCHAR(20)  DEFAULT 'ACTIVE' COMMENT '库存状态：ACTIVE-正常 LOW_STOCK-低库存 DEPLETED-已用完',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_drug (drug_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='药品库存';

-- 出入库记录表
CREATE TABLE IF NOT EXISTS drug_inventory_log (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    inventory_id    BIGINT       NOT NULL COMMENT '库存ID',
    drug_id         BIGINT       NOT NULL COMMENT '药品ID',
    change_type     VARCHAR(20)  NOT NULL COMMENT '变更类型：INBOUND-入库 OUTBOUND-出库 ADJUST-盘点调整',
    change_quantity INT          NOT NULL COMMENT '变更数量（入库为正，出库为负）',
    before_quantity INT          NOT NULL COMMENT '变更前数量',
    after_quantity  INT          NOT NULL COMMENT '变更后数量',
    operator_id     BIGINT       DEFAULT NULL COMMENT '操作人ID',
    reason          VARCHAR(200) DEFAULT NULL COMMENT '变更原因',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_inventory (inventory_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='出入库记录';

-- 用药处方表
CREATE TABLE IF NOT EXISTS drug_prescription (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    resident_id   BIGINT       NOT NULL COMMENT '老人ID',
    drug_id       BIGINT       NOT NULL COMMENT '药品ID',
    dosage        VARCHAR(50)  DEFAULT NULL COMMENT '用法用量（如：1片/次）',
    frequency     VARCHAR(50)  DEFAULT NULL COMMENT '用药频次文字（如：一日3次 饭后）',
    doses_per_day INT          DEFAULT 1 COMMENT '每日服药次数',
    start_date    DATE         DEFAULT NULL COMMENT '开始日期',
    end_date      DATE         DEFAULT NULL COMMENT '结束日期',
    status        VARCHAR(20)  DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-用药中 STOPPED-已停用',
    prescribed_by BIGINT       DEFAULT NULL COMMENT '开方医生ID',
    notes         VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_resident (resident_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用药处方';

-- 服药记录表
CREATE TABLE IF NOT EXISTS drug_record (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    prescription_id BIGINT       NOT NULL COMMENT '处方ID',
    resident_id     BIGINT       NOT NULL COMMENT '老人ID',
    drug_id         BIGINT       NOT NULL COMMENT '药品ID',
    drug_name       VARCHAR(100) DEFAULT NULL COMMENT '药品名称快照',
    administered_at DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '用药时间',
    administered_by BIGINT       DEFAULT NULL COMMENT '给药人ID',
    status          VARCHAR(20)  DEFAULT 'TAKEN' COMMENT '状态：TAKEN-已服用 MISSED-漏服 REFUSED-拒服',
    notes           VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_resident_time (resident_id, administered_at),
    INDEX idx_prescription (prescription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='服药记录';

-- =====================================================
-- 外出登记模块
-- =====================================================

-- 外出登记表
CREATE TABLE IF NOT EXISTS outing_record (
    id                   BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    resident_id          BIGINT       NOT NULL COMMENT '老人ID',
    out_time             DATETIME     NOT NULL COMMENT '外出时间',
    expected_return_time DATETIME     NOT NULL COMMENT '预计返回时间',
    actual_return_time   DATETIME     DEFAULT NULL COMMENT '实际返回时间',
    destination          VARCHAR(200) DEFAULT NULL COMMENT '目的地',
    companion            VARCHAR(50)  DEFAULT NULL COMMENT '陪同人',
    reason               VARCHAR(200) DEFAULT NULL COMMENT '外出原因',
    status               VARCHAR(20)  DEFAULT 'OUT' COMMENT '状态：OUT-外出中 RETURNED-已返回',
    notes                VARCHAR(500) DEFAULT NULL COMMENT '备注',
    registered_by        BIGINT       DEFAULT NULL COMMENT '登记人ID',
    created_at           DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_resident (resident_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='外出登记';

-- =====================================================
-- 护工排班模块
-- =====================================================

-- 护工信息表
CREATE TABLE IF NOT EXISTS nurse_info (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id          BIGINT       NOT NULL COMMENT '系统用户ID',
    employee_no      VARCHAR(50)  NOT NULL COMMENT '工号',
    phone            VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    shift_preference VARCHAR(20)  DEFAULT NULL COMMENT '班次偏好：MORNING/AFTERNOON/NIGHT',
    status           VARCHAR(20)  DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-在职 INACTIVE-离职',
    remark           VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user (user_id),
    UNIQUE KEY uk_employee_no (employee_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='护工信息';

-- 排班表
CREATE TABLE IF NOT EXISTS nurse_schedule (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    nurse_id   BIGINT       NOT NULL COMMENT '护工ID',
    shift_date DATE         NOT NULL COMMENT '排班日期',
    shift_type VARCHAR(20)  NOT NULL COMMENT '班次类型：MORNING-早班 AFTERNOON-中班 NIGHT-夜班',
    status     VARCHAR(20)  DEFAULT 'SCHEDULED' COMMENT '状态：SCHEDULED-已排班 COMPLETED-已完成 ABSENT-缺勤',
    notes      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_schedule (nurse_id, shift_date, shift_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='排班表';

-- 交接班记录表
CREATE TABLE IF NOT EXISTS shift_handover (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    schedule_id     BIGINT       NOT NULL COMMENT '排班ID',
    shift_date      DATE         NOT NULL COMMENT '排班日期（冗余）',
    from_nurse_id   BIGINT       NOT NULL COMMENT '交班人ID',
    to_nurse_id     BIGINT       NOT NULL COMMENT '接班人ID',
    handover_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '交接时间',
    key_notes       TEXT         DEFAULT NULL COMMENT '重点事项',
    resident_notes  TEXT         DEFAULT NULL COMMENT '老人情况备注',
    material_check  VARCHAR(500) DEFAULT NULL COMMENT '物资清点',
    status          VARCHAR(20)  DEFAULT 'PENDING' COMMENT '状态：COMPLETED-已完成 PENDING-待确认',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_schedule (schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='交接班记录';
