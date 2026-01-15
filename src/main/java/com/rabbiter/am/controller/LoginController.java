package com.rabbiter.am.controller;

import com.rabbiter.am.config.Result;
import com.rabbiter.am.config.ResultCode;
import com.rabbiter.am.entity.Employee;
import com.rabbiter.am.service.EmployeeService;
import com.rabbiter.am.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private JwtUtil jwtUtil;

    @ResponseBody
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public Result login(@RequestBody Employee employee, HttpServletResponse response){
        int s;
        Employee employee1 = employeeService.findByNumber(employee.getNumber());
        if(employee1 != null){
            if(employee.getPassword().equals(employee1.getPassword())){
                s = 1;
            }else {
                s = 0;
            }
        }else {
            s = -1;
        }
        if(s == 1){
            // 生成JWT Token
            String token = jwtUtil.generateToken(employee1.getNumber(), employee1.getName());
            
            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("employeeNumber", employee1.getNumber());
            data.put("employeeName", employee1.getName());
            
            return Result.success(data);
        }else if(s == 0){
            return Result.failure(ResultCode.USER_LOGIN_ERROR);
        }else if(s == -1){
            return Result.failure(ResultCode.USER_NOT_EXIST);
        }else {
            return Result.failure(ResultCode.SERVER_ERROR);
        }
    }
}
