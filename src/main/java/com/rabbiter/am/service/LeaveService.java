package com.rabbiter.am.service;

import com.rabbiter.am.dao.LeaveDao;
import com.rabbiter.am.entity.Employee;
import com.rabbiter.am.entity.Leave;
import com.rabbiter.am.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LeaveService {

    @Autowired
    private LeaveDao leaveDao;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskTypeService taskTypeService;
    @Autowired
    private EmployeeService employeeService;

    public int deleteById(String id) {
        return leaveDao.deleteById(id);
    }

    public int insert(Leave leave) {
        String id1 = UUID.randomUUID().toString();
        leave.setId(id1);
        String id2 = UUID.randomUUID().toString();
        leave.setApprovalID(id2);
        leave.setStatus("0");
        leaveDao.insert(leave);
        Task task = new Task();
        task.setId(id2);
        task.setApplyID(id1);
        task.setApplyTime(leave.getApplyTime());
        task.setTypeID(leave.getTaskTypeID());
        task.setApplyNumber(leave.getApplyNumber());
        String username = leave.getApplyName();
        String typeName = taskTypeService.selectById(leave.getTaskTypeID()).getName();
        task.setName(username+"的"+typeName);
        task.setStatus("0");
        Employee employee = employeeService.findByNumber(leave.getApplyNumber());
        Employee employee1 = employeeService.getLeader(employee);
        task.setReceiveNumber(employee1.getNumber());
        taskService.insert(task);
        return 0;
    }

    public Leave selectById(String id) {
        return leaveDao.selectById(id);
    }

    public int update(Leave leave) {
        return leaveDao.update(leave);
    }

    public List<Leave> findByEmployeeNumber(String number){
        List<Leave> leaveList = leaveDao.findByEmployeeNumber(number);
        for(Leave item : leaveList){
            if(item.getStatus().equals("1") || item.getStatus().equals("2")){
                Task task = taskService.selectById(item.getApprovalID());
                Employee employee = employeeService.findByNumber(task.getApprovalNumber());
                item.setApprovalName(employee.getName());
                item.setApprovalTime(task.getApprovalTime());
            }
        }
        return leaveList;
    }
}
