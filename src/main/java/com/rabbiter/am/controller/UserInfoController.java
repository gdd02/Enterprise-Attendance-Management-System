package com.rabbiter.am.controller;

import com.rabbiter.am.config.Result;
import com.rabbiter.am.context.UserContext;
import com.rabbiter.am.entity.Employee;
import com.rabbiter.am.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息Controller
 * 演示如何使用UserContext获取当前登录用户信息
 */
@CrossOrigin
@RestController
@RequestMapping("/userinfo")
public class UserInfoController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 获取当前登录用户信息
     * 演示使用UserContext获取用户信息
     */
    @GetMapping("/current")
    public Result getCurrentUser() {
        // 从ThreadLocal中获取当前用户信息
        String employeeNumber = UserContext.getEmployeeNumber();
        String employeeName = UserContext.getEmployeeName();

        // 也可以获取完整的用户信息对象
        UserContext.UserInfo userInfo = UserContext.getUser();

        // 根据员工工号查询完整的员工信息
        Employee employee = employeeService.findByNumber(employeeNumber);

        Map<String, Object> data = new HashMap<>();
        data.put("employeeNumber", employeeNumber);
        data.put("employeeName", employeeName);
        data.put("employee", employee);

        return Result.success(data);
    }

    /**
     * 获取当前用户的基本信息
     * 只返回ThreadLocal中存储的信息，不查询数据库
     */
    @GetMapping("/basic")
    public Result getBasicInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("employeeNumber", UserContext.getEmployeeNumber());
        data.put("employeeName", UserContext.getEmployeeName());
        return Result.success(data);
    }

    /**
     * 演示：修改当前用户的个人信息
     * 自动使用当前登录用户的工号，无需前端传递
     */
    @PostMapping("/updateProfile")
    public Result updateProfile(@RequestBody Employee employee) {
        // 从ThreadLocal获取当前用户工号，确保只能修改自己的信息
        String currentEmployeeNumber = UserContext.getEmployeeNumber();
        
        // 设置员工工号为当前登录用户，防止越权修改
        employee.setNumber(currentEmployeeNumber);
        
        // 调用service更新
        int result = employeeService.update(employee);
        
        if (result > 0) {
            return Result.success();
        } else {
            return Result.failure(null);
        }
    }
}

