package com.rabbiter.am.context;

/**
 * 用户上下文持有者
 * 使用ThreadLocal存储当前请求的用户信息，保证线程安全
 */
public class UserContext {

    /**
     * 用户信息线程本地变量
     */
    private static final ThreadLocal<UserInfo> userThreadLocal = new ThreadLocal<>();

    /**
     * 设置当前用户信息
     * @param userInfo 用户信息
     */
    public static void setUser(UserInfo userInfo) {
        userThreadLocal.set(userInfo);
    }

    /**
     * 获取当前用户信息
     * @return 用户信息，如果未设置则返回null
     */
    public static UserInfo getUser() {
        return userThreadLocal.get();
    }

    /**
     * 获取当前用户的员工工号
     * @return 员工工号，如果未设置则返回null
     */
    public static String getEmployeeNumber() {
        UserInfo userInfo = userThreadLocal.get();
        return userInfo != null ? userInfo.getEmployeeNumber() : null;
    }

    /**
     * 获取当前用户的员工姓名
     * @return 员工姓名，如果未设置则返回null
     */
    public static String getEmployeeName() {
        UserInfo userInfo = userThreadLocal.get();
        return userInfo != null ? userInfo.getEmployeeName() : null;
    }

    /**
     * 清除当前用户信息
     * 必须在请求结束时调用，避免内存泄漏
     */
    public static void clear() {
        userThreadLocal.remove();
    }

    /**
     * 用户信息内部类
     */
    public static class UserInfo {
        private String employeeNumber;
        private String employeeName;

        public UserInfo() {
        }

        public UserInfo(String employeeNumber, String employeeName) {
            this.employeeNumber = employeeNumber;
            this.employeeName = employeeName;
        }

        public String getEmployeeNumber() {
            return employeeNumber;
        }

        public void setEmployeeNumber(String employeeNumber) {
            this.employeeNumber = employeeNumber;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        @Override
        public String toString() {
            return "UserInfo{" +
                    "employeeNumber='" + employeeNumber + '\'' +
                    ", employeeName='" + employeeName + '\'' +
                    '}';
        }
    }
}

