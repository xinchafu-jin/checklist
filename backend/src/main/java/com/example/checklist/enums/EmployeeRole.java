package com.example.checklist.enums;

/**
 * 員工角色。
 */
public enum EmployeeRole {
    /** 現場執行檢點。 */
    INSPECTOR("檢點員"),
    /** 覆核紀錄、處理異常單。 */
    SUPERVISOR("主管"),
    /** 維護設備、範本、排程與人員。 */
    ADMIN("管理員");

    private final String label;

    EmployeeRole(String label) {
        this.label = label;
    }

    /** 顯示給使用者看的中文名稱。 */
    public String getLabel() {
        return label;
    }
}
