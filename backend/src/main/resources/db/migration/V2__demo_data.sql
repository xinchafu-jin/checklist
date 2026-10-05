-- =====================================================================
-- V2：示範資料
--
-- 示範帳號（密碼都是 Demo@1234，BCrypt 雜湊由 Spring 的 BCryptPasswordEncoder 產生）：
--   A0001 系統管理員（ADMIN）
--   S0001 林主任（SUPERVISOR）
--   I0001 王小明、I0002 陳美玲（INSPECTOR）
--   I0003 李志豪（INSPECTOR，已離職停用，示範舊紀錄仍查得到人）
--
-- 檢點範本前三張取自成功大學環安衛中心「各類機械設備及作業自動檢查表單（參考例）」的每日作業前檢點表，
-- 項目、部位與法條照原表；紙本只能打勾，所以全部用預設四選項。
-- 第四張空壓機日常點檢是自己設計的，示範紙本做不到的功能：數值上下限、自訂選項、多選、文字。
--
-- id 直接指定，方便閱讀與之後的測試；AUTO_INCREMENT 會從目前最大值之後繼續。
-- 範本項目的 id 依範本分段：堆高機 101～、衝剪機械 201～、壓力容器 301～、空壓機 401～。
-- =====================================================================


-- ---------------------------------------------------------------------
-- 區域、員工、班別
-- ---------------------------------------------------------------------

INSERT INTO area (id, name)
VALUES (1, 'A 棟倉儲區'),
       (2, 'B 棟加工區'),
       (3, 'C 棟出貨區'),
       (4, '公用設施區');

INSERT INTO employee (id, employee_no, name, password_hash, role, active)
VALUES (1, 'A0001', '系統管理員', '$2a$10$Znpw.ZNh87jqnBHblUMSqOJFsSBgk2aJRiTXg17Gk/eXAddCYVDXi', 'ADMIN', TRUE),
       (2, 'S0001', '林主任', '$2a$10$Znpw.ZNh87jqnBHblUMSqOJFsSBgk2aJRiTXg17Gk/eXAddCYVDXi', 'SUPERVISOR', TRUE),
       (3, 'I0001', '王小明', '$2a$10$Znpw.ZNh87jqnBHblUMSqOJFsSBgk2aJRiTXg17Gk/eXAddCYVDXi', 'INSPECTOR', TRUE),
       (4, 'I0002', '陳美玲', '$2a$10$Znpw.ZNh87jqnBHblUMSqOJFsSBgk2aJRiTXg17Gk/eXAddCYVDXi', 'INSPECTOR', TRUE),
       (5, 'I0003', '李志豪', '$2a$10$Znpw.ZNh87jqnBHblUMSqOJFsSBgk2aJRiTXg17Gk/eXAddCYVDXi', 'INSPECTOR', FALSE);

-- 夜班 22:00～06:00 跨午夜，檢點日算開班那天
INSERT INTO shift (id, name, seq, start_time, end_time)
VALUES (1, '早班', 1, '06:00:00', '14:00:00'),
       (2, '中班', 2, '14:00:00', '22:00:00'),
       (3, '夜班', 3, '22:00:00', '06:00:00');


-- ---------------------------------------------------------------------
-- 檢點範本：每張一個已發布的第 1 版
-- ---------------------------------------------------------------------

INSERT INTO checklist_template (id, code, name, kind, legal_basis)
VALUES (1, 'FL-DAILY', '堆高機作業檢點（每日作業前）', 'INSPECTION', '職業安全衛生管理辦法第 50 條'),
       (2, 'PR-DAILY', '衝剪機械（動力驅動式）作業檢點（每日作業前）', 'INSPECTION', '職業安全衛生管理辦法第 59 條'),
       (3, 'PV-DAILY', '第一種壓力容器操作作業檢點（每日作業前）', 'INSPECTION',
        '職業安全衛生管理辦法第 64 條、鍋爐及壓力容器安全規則第 28 條'),
       (4, 'AC-DAILY', '空壓機日常點檢', 'INSPECTION', NULL);

INSERT INTO template_version (id, template_id, version_no, status, published_at)
VALUES (1, 1, 1, 'PUBLISHED', '2026-10-01 08:00:00'),
       (2, 2, 1, 'PUBLISHED', '2026-10-01 08:00:00'),
       (3, 3, 1, 'PUBLISHED', '2026-10-01 08:00:00'),
       (4, 4, 1, 'PUBLISHED', '2026-10-01 08:00:00');


-- 堆高機（成大原表 13 項；第 12、13 項只有電動式需要，柴油車選「不適用」）
INSERT INTO checklist_item (id, version_id, seq, section, name, item_type)
VALUES (101, 1, 1, NULL, '水箱', 'SINGLE'),
       (102, 1, 2, NULL, '機油', 'SINGLE'),
       (103, 1, 3, NULL, '煞車油', 'SINGLE'),
       (104, 1, 4, NULL, '汽油', 'SINGLE'),
       (105, 1, 5, NULL, '離合器', 'SINGLE'),
       (106, 1, 6, NULL, '煞車能力', 'SINGLE'),
       (107, 1, 7, NULL, '叉舉系統', 'SINGLE'),
       (108, 1, 8, NULL, '潤滑部分', 'SINGLE'),
       (109, 1, 9, NULL, '輪胎氣壓', 'SINGLE'),
       (110, 1, 10, NULL, '方向盤、喇叭', 'SINGLE'),
       (111, 1, 11, NULL, '油壓系統', 'SINGLE'),
       (112, 1, 12, NULL, '傳達鏈條（電氣式）', 'SINGLE'),
       (113, 1, 13, NULL, '電瓶（電氣式）', 'SINGLE');

-- 衝剪機械（成大原表 13 項，分四個部位）
INSERT INTO checklist_item (id, version_id, seq, section, name, item_type)
VALUES (201, 2, 1, '驅動部分', '曲柄軸', 'SINGLE'),
       (202, 2, 2, '驅動部分', '飛輪', 'SINGLE'),
       (203, 2, 3, '驅動部分', '離合器', 'SINGLE'),
       (204, 2, 4, '驅動部分', '滑塊', 'SINGLE'),
       (205, 2, 5, '驅動部分', '連接螺栓', 'SINGLE'),
       (206, 2, 6, '驅動部分', '連桿', 'SINGLE'),
       (207, 2, 7, '驅動部分', 'V 皮帶', 'SINGLE'),
       (208, 2, 8, '電氣部分', '馬達', 'SINGLE'),
       (209, 2, 9, '電氣部分', '線路', 'SINGLE'),
       (210, 2, 10, '空氣部分', '壓力計', 'SINGLE'),
       (211, 2, 11, '空氣部分', '濾清器', 'SINGLE'),
       (212, 2, 12, '其他', '制動裝置', 'SINGLE'),
       (213, 2, 13, '其他', '安全裝置', 'SINGLE');

-- 第一種壓力容器（成大原表 7 項）
INSERT INTO checklist_item (id, version_id, seq, section, name, item_type)
VALUES (301, 3, 1, NULL, '監視溫度、壓力等運轉狀態，並確認安全閥、壓力錶及其他安全裝置無異狀', 'SINGLE'),
       (302, 3, 2, NULL, '避免急劇負荷變動之現象', 'SINGLE'),
       (303, 3, 3, NULL, '保持汽壓在最高使用壓力之下', 'SINGLE'),
       (304, 3, 4, NULL, '保持安全閥之功能正常', 'SINGLE'),
       (305, 3, 5, NULL, '檢點及調整自動控制裝置，以保持功能正常', 'SINGLE'),
       (306, 3, 6, NULL, '保持冷卻水裝置之功能正常', 'SINGLE'),
       (307, 3, 7, NULL, '其他', 'SINGLE');

-- 空壓機日常點檢（自訂，示範數值、自訂選項、多選、文字）
INSERT INTO checklist_item (id, version_id, seq, section, name, method, standard, item_type, min_value, max_value,
                            unit)
VALUES (401, 4, 1, '運轉狀態', '排氣壓力', '讀壓力錶', '6.0～8.0 kgf/cm²', 'NUMBER', 6.000, 8.000, 'kgf/cm²'),
       (402, 4, 2, '運轉狀態', '排氣溫度', '讀溫度計', '100 ℃ 以下', 'NUMBER', NULL, 100.000, '℃'),
       (403, 4, 3, '運轉狀態', '累計運轉時數', '讀控制面板', '只記錄，不判定', 'NUMBER', NULL, NULL, '小時'),
       (404, 4, 4, '運轉狀態', '運轉聲音與震動', '聽、觸摸', '無異音、無異常震動', 'SINGLE', NULL, NULL, NULL),
       (405, 4, 5, '潤滑', '潤滑油油位', '看油位窗', '介於上下刻度之間', 'SINGLE', NULL, NULL, NULL),
       (406, 4, 6, '外觀', '漏油、漏氣位置', '目視、聽', '無漏油、無漏氣', 'MULTI', NULL, NULL, NULL),
       (407, 4, 7, '外觀', '冷凝水排放', '開啟排水閥', '已排放，無積水', 'SINGLE', NULL, NULL, NULL),
       (408, 4, 8, '其他', '其他觀察事項', NULL, NULL, 'TEXT', NULL, NULL, NULL);


-- 預設四選項：前三張的所有項目與空壓機的冷凝水排放（407）
INSERT INTO checklist_item_option (item_id, seq, label, result_level)
SELECT i.id, o.seq, o.label, o.result_level
FROM checklist_item i
         CROSS JOIN (SELECT 1 AS seq, '正常' AS label, 'NORMAL' AS result_level
                     UNION ALL
                     SELECT 2, '需注意', 'ATTENTION'
                     UNION ALL
                     SELECT 3, '異常', 'ABNORMAL'
                     UNION ALL
                     SELECT 4, '不適用', 'NOT_APPLICABLE') o
WHERE i.version_id IN (1, 2, 3)
   OR i.id = 407;

-- 空壓機的自訂選項
INSERT INTO checklist_item_option (item_id, seq, label, result_level)
VALUES (404, 1, '無異音、無異常震動', 'NORMAL'),
       (404, 2, '輕微異音', 'ATTENTION'),
       (404, 3, '明顯異音或震動', 'ABNORMAL'),

       (405, 1, '介於上下刻度之間', 'NORMAL'),
       (405, 2, '接近下限，需補油', 'ATTENTION'),
       (405, 3, '低於下限', 'ABNORMAL'),

       (406, 1, '無漏油、無漏氣', 'NORMAL'),
       (406, 2, '氣管接頭漏氣', 'ATTENTION'),
       (406, 3, '油管接頭漏油', 'ABNORMAL'),
       (406, 4, '油氣分離桶漏油', 'ABNORMAL'),
       (406, 5, '冷卻器漏油', 'ABNORMAL');


-- ---------------------------------------------------------------------
-- 設備與排程
-- ---------------------------------------------------------------------

INSERT INTO equipment (id, code, name, category, area_id, status)
VALUES (1, 'FL-01', '柴油堆高機 2.5 噸', '堆高機', 1, 'IN_SERVICE'),
       (2, 'FL-02', '電動堆高機 2 噸', '堆高機', 3, 'IN_SERVICE'),
       (3, 'FL-03', '柴油堆高機 3 噸', '堆高機', 1, 'IN_SERVICE'),
       (4, 'PR-01', '110 噸沖床', '衝剪機械', 2, 'IN_SERVICE'),
       (5, 'PR-02', '60 噸沖床', '衝剪機械', 2, 'IN_SERVICE'),
       (6, 'PV-01', '空壓儲氣槽', '第一種壓力容器', 4, 'IN_SERVICE'),
       (7, 'AC-01', '螺旋式空壓機 30HP', '空壓機', 4, 'IN_SERVICE'),
       (8, 'AC-02', '螺旋式空壓機 20HP', '空壓機', 2, 'IN_SERVICE');

-- 示範不同的頻率設定：
--   堆高機、壓力容器每天檢（七天全勾）
--   沖床週日停工，只勾週一到週六
--   AC-01 全天運轉，每班檢；AC-02 只在平日開，週一到週五
INSERT INTO inspection_plan (equipment_id, template_id, frequency_unit, frequency_interval, schedule_days, start_date)
VALUES (1, 1, 'DAY', 1, 'MON,TUE,WED,THU,FRI,SAT,SUN', '2026-10-01'),
       (2, 1, 'DAY', 1, 'MON,TUE,WED,THU,FRI,SAT,SUN', '2026-10-01'),
       (3, 1, 'DAY', 1, 'MON,TUE,WED,THU,FRI,SAT,SUN', '2026-10-01'),
       (4, 2, 'DAY', 1, 'MON,TUE,WED,THU,FRI,SAT', '2026-10-01'),
       (5, 2, 'DAY', 1, 'MON,TUE,WED,THU,FRI,SAT', '2026-10-01'),
       (6, 3, 'DAY', 1, 'MON,TUE,WED,THU,FRI,SAT,SUN', '2026-10-01'),
       (7, 4, 'SHIFT', 1, 'MON,TUE,WED,THU,FRI,SAT,SUN', '2026-10-01'),
       (8, 4, 'DAY', 1, 'MON,TUE,WED,THU,FRI', '2026-10-01');
