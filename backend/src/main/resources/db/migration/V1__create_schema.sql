-- =====================================================================
-- V1：檢點系統資料表
--
-- 列舉值一律存英文代碼（對應 Java Enum 的 @Enumerated(EnumType.STRING)），
-- 中文意思寫在 COMMENT。約束都有名字，違反時錯誤訊息才看得出是哪條規則。
--
-- 建表順序依外鍵相依：
--   設定資料：area → employee → shift → checklist_template → template_version
--            → checklist_item → checklist_item_option → equipment → inspection_plan
--   每日紀錄：inspection_record → inspection_result → inspection_result_option → abnormality
-- =====================================================================


-- ---------------------------------------------------------------------
-- 設定資料（管理員維護）
-- ---------------------------------------------------------------------

CREATE TABLE area
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    name       VARCHAR(50) NOT NULL COMMENT '區域或產線，例如 A 棟倉儲區',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_area_name UNIQUE (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='區域';


CREATE TABLE employee
(
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    employee_no   VARCHAR(20) NOT NULL COMMENT '工號，登入帳號',
    name          VARCHAR(50) NOT NULL,
    password_hash VARCHAR(60) NOT NULL COMMENT 'BCrypt 雜湊',
    role          VARCHAR(20) NOT NULL COMMENT 'INSPECTOR 檢點員 / SUPERVISOR 主管 / ADMIN 管理員',
    active        BOOLEAN     NOT NULL DEFAULT TRUE COMMENT '離職改為 FALSE，不刪除，舊紀錄才查得到是誰檢的',
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_employee_no UNIQUE (employee_no),
    CONSTRAINT ck_employee_role CHECK (role IN ('INSPECTOR', 'SUPERVISOR', 'ADMIN'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='員工';


CREATE TABLE shift
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    name       VARCHAR(20) NOT NULL COMMENT '早班 / 中班 / 夜班',
    seq        INT         NOT NULL COMMENT '第幾班，每班檢點的 period_key 用它，例如 2026-10-03#2',
    start_time TIME        NOT NULL,
    end_time   TIME        NOT NULL COMMENT '早於 start_time 代表跨午夜，檢點日算開班那天',
    active     BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_shift_name UNIQUE (name),
    CONSTRAINT uk_shift_seq UNIQUE (seq)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='班別';


CREATE TABLE checklist_template
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    code        VARCHAR(30)  NOT NULL COMMENT '表單編號',
    name        VARCHAR(100) NOT NULL COMMENT '表單名稱，例如 堆高機每日作業前檢點',
    kind        VARCHAR(20)  NOT NULL DEFAULT 'INSPECTION' COMMENT 'INSPECTION 定期檢點 / PERMIT 作業許可（預留）',
    legal_basis VARCHAR(255) NULL COMMENT '法規依據',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_template_code UNIQUE (code),
    CONSTRAINT ck_template_kind CHECK (kind IN ('INSPECTION', 'PERMIT'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='檢點範本';


-- 改項目就發新版本，舊紀錄仍對應當時的版本。
-- 「同一範本同時只能有一個 PUBLISHED」由後端檢查：MySQL 沒有條件式唯一索引。
CREATE TABLE template_version
(
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    template_id  BIGINT      NOT NULL,
    version_no   INT         NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT 草稿 / PUBLISHED 發布 / RETIRED 停用',
    published_at DATETIME    NULL,
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_version_no UNIQUE (template_id, version_no),
    CONSTRAINT fk_version_template FOREIGN KEY (template_id) REFERENCES checklist_template (id),
    CONSTRAINT ck_version_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'RETIRED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='範本版本';


CREATE TABLE checklist_item
(
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    version_id BIGINT         NOT NULL,
    seq        INT            NOT NULL COMMENT '排列順序',
    section    VARCHAR(50)    NULL COMMENT '部位，例如 驅動、電氣',
    name       VARCHAR(200)   NOT NULL COMMENT '檢點項目，例如 煞車功能是否正常',
    method     VARCHAR(100)   NULL COMMENT '檢點方法，例如 目視、操作測試',
    standard   VARCHAR(200)   NULL COMMENT '判定基準，例如 無漏油',
    item_type  VARCHAR(20)    NOT NULL COMMENT 'SINGLE 單選 / MULTI 多選 / TEXT 文字 / NUMBER 數值',
    min_value  DECIMAL(12, 3) NULL COMMENT '數值項目下限，低於算異常',
    max_value  DECIMAL(12, 3) NULL COMMENT '數值項目上限，高於算異常',
    unit       VARCHAR(20)    NULL COMMENT '數值單位，例如 kgf/cm²',
    PRIMARY KEY (id),
    CONSTRAINT uk_item_seq UNIQUE (version_id, seq),
    CONSTRAINT fk_item_version FOREIGN KEY (version_id) REFERENCES template_version (id) ON DELETE CASCADE,
    CONSTRAINT ck_item_type CHECK (item_type IN ('SINGLE', 'MULTI', 'TEXT', 'NUMBER')),
    CONSTRAINT ck_item_range CHECK (min_value IS NULL OR max_value IS NULL OR min_value <= max_value)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='檢點項目';


-- 單選、多選項目的選項。畫面顯示 label，判斷與統計看 result_level。
-- 「每項至少一個 NORMAL 選項」由後端檢查。
CREATE TABLE checklist_item_option
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    item_id      BIGINT       NOT NULL,
    seq          INT          NOT NULL,
    label        VARCHAR(100) NOT NULL COMMENT '顯示文字，例如 輕微磨損',
    result_level VARCHAR(20)  NOT NULL COMMENT 'NORMAL 正常 / ATTENTION 注意 / ABNORMAL 異常 / NOT_APPLICABLE 不適用',
    PRIMARY KEY (id),
    CONSTRAINT uk_option_seq UNIQUE (item_id, seq),
    CONSTRAINT fk_option_item FOREIGN KEY (item_id) REFERENCES checklist_item (id) ON DELETE CASCADE,
    CONSTRAINT ck_option_level CHECK (result_level IN ('NORMAL', 'ATTENTION', 'ABNORMAL', 'NOT_APPLICABLE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='檢點項目選項';


CREATE TABLE equipment
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    code       VARCHAR(30)  NOT NULL COMMENT '設備編號，例如 FL-03',
    name       VARCHAR(100) NOT NULL COMMENT '設備名稱，例如 堆高機',
    category   VARCHAR(50)  NULL COMMENT '類別',
    area_id    BIGINT       NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'IN_SERVICE' COMMENT 'IN_SERVICE 使用中 / UNDER_REPAIR 待修 / RETIRED 報廢',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_equipment_code UNIQUE (code),
    CONSTRAINT fk_equipment_area FOREIGN KEY (area_id) REFERENCES area (id),
    CONSTRAINT ck_equipment_status CHECK (status IN ('IN_SERVICE', 'UNDER_REPAIR', 'RETIRED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='設備';


-- 哪台設備套用哪張範本、多久檢一次。
-- 頻率 = 單位 × 間隔，例如 WEEK × 2 = 每兩週、MONTH × 3 = 每季。
-- schedule_days 依單位解讀：
--   SHIFT / DAY：要檢的星期，逗號分隔，例如 MON,TUE,WED,THU,FRI,SAT,SUN（預設七天全勾）
--   WEEK：排在星期幾，例如 WED
--   MONTH：幾號，例如 15；大於當月天數時算月底
--   YEAR：月-日，例如 03-15
-- 待檢點與漏檢由排程即時計算，不預先產生任務資料。
CREATE TABLE inspection_plan
(
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    equipment_id       BIGINT       NOT NULL,
    template_id        BIGINT       NOT NULL,
    frequency_unit     VARCHAR(10)  NOT NULL COMMENT 'SHIFT 班 / DAY 日 / WEEK 週 / MONTH 月 / YEAR 年',
    frequency_interval INT          NOT NULL DEFAULT 1 COMMENT '每幾個單位一次，大於 1 時從 start_date 起算',
    schedule_days      VARCHAR(100) NULL COMMENT '要檢的日子，格式見表頭說明',
    start_date         DATE         NOT NULL COMMENT '從哪天開始算',
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    -- 同一台設備同一張範本只能有一個排程，否則 period_key 會對不上
    CONSTRAINT uk_plan_equipment_template UNIQUE (equipment_id, template_id),
    CONSTRAINT fk_plan_equipment FOREIGN KEY (equipment_id) REFERENCES equipment (id),
    CONSTRAINT fk_plan_template FOREIGN KEY (template_id) REFERENCES checklist_template (id),
    CONSTRAINT ck_plan_unit CHECK (frequency_unit IN ('SHIFT', 'DAY', 'WEEK', 'MONTH', 'YEAR')),
    CONSTRAINT ck_plan_interval CHECK (frequency_interval >= 1)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='檢點排程';


-- ---------------------------------------------------------------------
-- 每日紀錄（現場檢點產生，送出後不能改、不能刪）
-- ---------------------------------------------------------------------

-- 一筆紀錄 = 紙本月表上的一格：哪台設備、哪個期間、誰檢的。
-- 唯一鍵用 period_key 而不是「日期 + 班別」：每日檢點沒有班別，shift_id 是 NULL，
-- 而 MySQL 的唯一索引允許多個 NULL，會擋不住同一天重複送出。
--   每日 2026-10-03 / 每班 2026-10-03#2 / 每週 2026-W40 / 每月 2026-10 / 每年 2026
CREATE TABLE inspection_record
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    equipment_id        BIGINT       NOT NULL,
    template_id         BIGINT       NOT NULL COMMENT '唯一鍵用',
    template_version_id BIGINT       NOT NULL COMMENT '依據哪個版本檢點',
    period_key          VARCHAR(20)  NOT NULL COMMENT '期間，格式見表頭說明',
    business_date       DATE         NOT NULL COMMENT '檢點日（Asia/Taipei），夜班跨午夜算開班那天',
    shift_id            BIGINT       NULL COMMENT '每班檢點才有',
    status              VARCHAR(20)  NOT NULL COMMENT 'COMPLETED 完成 / DOWNTIME 停機（不算漏檢）',
    overall_result      VARCHAR(20)  NULL COMMENT 'NORMAL / ATTENTION / ABNORMAL，取各項最嚴重；停機時為 NULL',
    remark              VARCHAR(500) NULL COMMENT '建議改善事項，對應紙本表底部',
    inspector_id        BIGINT       NOT NULL,
    submitted_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by         BIGINT       NULL COMMENT '主管覆核',
    reviewed_at         DATETIME     NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_record_period UNIQUE (equipment_id, template_id, period_key),
    CONSTRAINT fk_record_equipment FOREIGN KEY (equipment_id) REFERENCES equipment (id),
    CONSTRAINT fk_record_template FOREIGN KEY (template_id) REFERENCES checklist_template (id),
    CONSTRAINT fk_record_version FOREIGN KEY (template_version_id) REFERENCES template_version (id),
    CONSTRAINT fk_record_shift FOREIGN KEY (shift_id) REFERENCES shift (id),
    CONSTRAINT fk_record_inspector FOREIGN KEY (inspector_id) REFERENCES employee (id),
    CONSTRAINT fk_record_reviewer FOREIGN KEY (reviewed_by) REFERENCES employee (id),
    CONSTRAINT ck_record_status CHECK (status IN ('COMPLETED', 'DOWNTIME')),
    CONSTRAINT ck_record_result CHECK (overall_result IN ('NORMAL', 'ATTENTION', 'ABNORMAL')),
    INDEX idx_record_business_date (business_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='檢點紀錄';


-- 一個項目一列。選了哪些選項另外存在 inspection_result_option（多選才放得下）。
CREATE TABLE inspection_result
(
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    record_id    BIGINT         NOT NULL,
    item_id      BIGINT         NOT NULL,
    result       VARCHAR(20)    NULL COMMENT '單選看選項、多選取最嚴重、數值依上下限；文字項目為 NULL',
    number_value DECIMAL(12, 3) NULL COMMENT '數值項目的量測值',
    note         VARCHAR(500)   NULL COMMENT '說明：ATTENTION / ABNORMAL 必填；文字項目的內容也存這裡',
    PRIMARY KEY (id),
    CONSTRAINT uk_result_item UNIQUE (record_id, item_id),
    CONSTRAINT fk_result_record FOREIGN KEY (record_id) REFERENCES inspection_record (id),
    CONSTRAINT fk_result_item FOREIGN KEY (item_id) REFERENCES checklist_item (id),
    CONSTRAINT ck_result_level CHECK (result IN ('NORMAL', 'ATTENTION', 'ABNORMAL', 'NOT_APPLICABLE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='檢點結果（逐項）';


-- 單選一列、多選好幾列。
CREATE TABLE inspection_result_option
(
    id        BIGINT NOT NULL AUTO_INCREMENT,
    result_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_result_option UNIQUE (result_id, option_id),
    CONSTRAINT fk_result_option_result FOREIGN KEY (result_id) REFERENCES inspection_result (id),
    CONSTRAINT fk_result_option_option FOREIGN KEY (option_id) REFERENCES checklist_item_option (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='檢點結果選了哪些選項';


-- 結果為 ATTENTION 或 ABNORMAL 時自動開單。
-- action_taken 就是職業安全衛生管理辦法第 80 條要求記錄的「改善措施」。
CREATE TABLE abnormality
(
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    result_id    BIGINT        NOT NULL COMMENT '由哪一項結果開出',
    equipment_id BIGINT        NOT NULL COMMENT '冗餘存一份，依設備查異常不必經過三張表',
    status       VARCHAR(20)   NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN 待處理 / IN_PROGRESS 處理中 / CLOSED 已結案',
    action_taken VARCHAR(1000) NULL COMMENT '處置內容（改善措施）',
    closed_by    BIGINT        NULL,
    closed_at    DATETIME      NULL,
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_abnormality_result UNIQUE (result_id),
    CONSTRAINT fk_abnormality_result FOREIGN KEY (result_id) REFERENCES inspection_result (id),
    CONSTRAINT fk_abnormality_equipment FOREIGN KEY (equipment_id) REFERENCES equipment (id),
    CONSTRAINT fk_abnormality_closer FOREIGN KEY (closed_by) REFERENCES employee (id),
    CONSTRAINT ck_abnormality_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'CLOSED')),
    INDEX idx_abnormality_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='異常單';
